#!/usr/bin/env python3

from os import path
import argparse
import pprint
import json
import sys
import subprocess
from pathlib import Path
import tempfile
from shutil import copyfile

SCRIPT_PATH = path.dirname(path.abspath(__file__))
PROJECT_BASE_PATH = path.join(SCRIPT_PATH, '..')
ARTIFACT_DIR = path.join(PROJECT_BASE_PATH, "build", "libs")
TESTS_DIR = path.join(PROJECT_BASE_PATH, "tests")
TESTS_SOURCE_DIR = path.join(TESTS_DIR, "src")
CONFIG_FILE = path.join(TESTS_DIR, "config.json")
TMPDIR_OUTPUT_DIR_SUFFIX = "instrumented"
JAR_BASE_NAMES = ["asm_test", "util"]


def is_existing_file(p):
    if not p:
        return False
    file = Path(p)
    return file.exists() and file.is_file()


class ExecutionResult:
    def __init__(self, rv, stdout, stderr):
        self._stdout = stdout
        self._stderr = stderr
        self._return_value = rv

    @property
    def return_value(self):
        return self._return_value

    @property
    def stdout(self):
        return self._stdout

    @property
    def stderr(self):
        return self._stderr

    def __repr__(self):
        return ('ExecutionResult(return_value={self._return_value!r}, '
                'stdout={self._stdout!r}, stderr={self._stderr!r})'
                ).format(self=self)

    def __eq__(self, other):
        return self._return_value == other.return_value \
               and self._stdout == other.stdout \
               and self._stderr == other.stderr


class TestCase:
    def __init__(self, test_dict):
        self._name = test_dict.get('name')
        self._source = test_dict.get('source')
        self._source_path = path.join(TESTS_SOURCE_DIR, self._source)
        self._arguments = test_dict.get('arguments', [])
        self._javac_version = None  # test_dict.get('javac_version')
        self._regular_result = None
        self._instrumented_result = None


    @property
    def name(self):
        return self._name

    @property
    def source(self):
        return self._source

    @property
    def source_path(self):
        return self._source_path

    @property
    def arguments(self):
        return self._arguments

    @property
    def javac_version(self):
        return self._javac_version

    def __repr__(self):
        return ('TestCase(name={self._name!r}, source={self._source!r}, '
                'source_path={self._source_path!r}, '
                'arguments={self._arguments!r}, '
                'javac_version={self._javac_version!r})').format(self=self)


class TestResult:
    def __init__(self, test_case, regular_result, instrumented_result):
        self._test_case = test_case
        self._regular_result = regular_result
        self._instrumented_result = instrumented_result

    @property
    def successful(self):
        return not(self._regular_result is None or self._instrumented_result is None) and \
               self._regular_result == self._instrumented_result

    @property
    def regular_result(self):
        return self._regular_result

    @property
    def instrumented_result(self):
        return self._instrumented_result

    @instrumented_result.setter
    def instrumented_result(self, value):
        self._instrumented_result = value

    @regular_result.setter
    def regular_result(self, value):
        self._regular_result = value

    def __repr__(self):
        return ('TestResult(test_case={self._test_case}, '
                'regular_result={self._regular_result!r}, '
                'instrumented_result={self._instrumented_result!r})').format(self=self)


class Configuration:
    def __init__(self, test_cases):
        self._test_cases = test_cases
        self._version = "0.0.1-SNAPSHOT"

    @property
    def test_cases(self):
        return self._test_cases

    @property
    def version(self):
        return self._version

    def __repr__(self):
        return 'Configuration(version={self._version!r}, test_cases={self._test_cases!r})'.format(self=self)

    @version.setter
    def version(self, value):
        self._version = value


def format_jar_filename(base_name, version):
    jar_name = '{}-{}.jar'.format(base_name, version)
    return path.join(ARTIFACT_DIR, jar_name)


def check_output_files_existence(version):
    for jar_base_name in JAR_BASE_NAMES:
        jar_path = format_jar_filename(jar_base_name, version)
        if not is_existing_file(jar_path):
            print('Required jar file {} does not exist!'.format(jar_path))
            sys.exit(1)


def compile_project():
    compile_result = subprocess.run(["gradle", "jar"],
                                    universal_newlines=True,
                                    stdout=subprocess.PIPE,
                                    stderr=subprocess.PIPE,
                                    cwd=PROJECT_BASE_PATH
                                    )
    return compile_result.returncode == 0


def parse_config(config_path):
    if not is_existing_file(config_path):
        print('Can\'t find config file')
        sys.exit(1)

    with open(config_path, "r", encoding="utf-8") as config_in:
        config = json.load(config_in)

    # pprint.pprint(config)
    tests = []
    for test_case in config.get('tests', []):
        test = TestCase(test_case)
        tests.append(test)

    return Configuration(tests)


class TestRunResult:
    def __init__(self, results):
        self._num_total = len(results)
        self._successful = []
        self._failed = []
        for test_result in results:
            if not test_result.successful:
                self._failed.append(test_result)
            else:
                self._successful.append(test_result)
        self._num_successful = len(self._successful)
        self._num_failed = len(self._failed)

    @property
    def num_failed(self):
        return self._num_failed

    def __repr__(self):
        out = 'Total Tests: {}, Failed: {}, Successful: {}'.format(self._num_total,
                                                                   self._num_failed,
                                                                   self._num_successful)
        if self._num_failed > 0:
            out = out + "\n\tFailed Tests:"
            for failed_test in self._failed:
                out = out + "\n" + '\t\t{}'.format(failed_test.test_case.name)
        return out


class TestRunner:
    def __init__(self, config):
        self._config = config

    @staticmethod
    def _run_regular_class_file(cwd, name, arguments):
        exec_result = subprocess.run(["java", name] + arguments,
                                     universal_newlines=True,
                                     stdout=subprocess.PIPE,
                                     stderr=subprocess.PIPE,
                                     cwd=cwd
                                    )
        return ExecutionResult(exec_result.returncode, exec_result.stdout, exec_result.stderr)

    def _instrument_class_file(self, cwd, name):
        class_name = '{}.class'.format(name)
        input_file = path.join(cwd, class_name)
        output_file = path.join(cwd, TMPDIR_OUTPUT_DIR_SUFFIX, class_name)
        exec_result = subprocess.run([
                                    "java",
                                    "-jar",
                                    format_jar_filename("asm_test", self._config.version),
                                    "-f",
                                    input_file,
                                    "-o",
                                    output_file
                                ],
                                universal_newlines=True,
                                stdout=subprocess.PIPE,
                                stderr=subprocess.PIPE,
                                cwd=cwd
                                )
        return ExecutionResult(exec_result.returncode, exec_result.stdout, exec_result.stderr)

    def _run_instrumented_class_file(self, cwd, name, arguments):
        exec_result = subprocess.run([
                                     "java",
                                     "-classpath",
                                     '.:{}'.format(format_jar_filename("utils", self._config.version)),
                                     name
                                     ] + arguments,
                                     universal_newlines=True,
                                     stdout=subprocess.PIPE,
                                     stderr=subprocess.PIPE,
                                     cwd=cwd
                                )
        return ExecutionResult(exec_result.returncode, exec_result.stdout, exec_result.stderr)

    @staticmethod
    def _compile_source_file(cwd, source):
        exec_result = subprocess.run(
                                     ["javac", source],
                                     universal_newlines=True,
                                     stdout=subprocess.PIPE,
                                     stderr=subprocess.PIPE,
                                     cwd=cwd
                                    )
        return exec_result.returncode == 0

    @staticmethod
    def _copy_source_file(target_dir, source):
        source_file = path.join(TESTS_SOURCE_DIR, source)
        dest_file = path.join(target_dir, source)
        copyfile(source_file, dest_file)

    def _run_test(self, base_dir, test):
        print('Running Test: "{}"'.format(test.name))
        src_file = test.source
        (base_name, _, _) = src_file.partition(".java")
        self._copy_source_file(base_dir, src_file)
        self._compile_source_file(base_dir, src_file)
        self._instrument_class_file(base_dir, base_name)
        regular_result = self._run_regular_class_file(base_dir, base_name, test.arguments)
        instrumented_result = self._run_instrumented_class_file(base_dir, base_name, test.arguments)
        return TestResult(test, regular_result, instrumented_result)

    def _run_tests(self, base_dir):
        test_results = []
        for test in self._config.test_cases:
            tr = self._run_test(base_dir, test)
            if not tr.successful:
                print('Test "{}" failed:\nRegular result: "{}", Instrumented Result: "{}"'.format(test.name,
                                                                                                  tr.regular_result,
                                                                                                  tr.instrumented_result
                                                                                                  )
                      )
            test_results.append(tr)
        return test_results

    def run_tests(self):
        with tempfile.TemporaryDirectory() as tmpdir:
            out_dir = Path(path.join(tmpdir, TMPDIR_OUTPUT_DIR_SUFFIX))
            out_dir.mkdir()
            test_results = self._run_tests(tmpdir)

        return TestRunResult(test_results)


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--build-first", action="store_true")
    parser.add_argument("--version", default="0.0.1-SNAPSHOT")
    parser.add_argument("--config", default=CONFIG_FILE)

    args = parser.parse_args()
    if args.build_first:
        compile_project()

    if check_output_files_existence(args.version) is False:
        print('Failed to compile java project!')
        sys.exit(1)

    CONFIG = parse_config(args.config)
    CONFIG.version = args.version
    runner = TestRunner(CONFIG)
    result = runner.run_tests()
    print(result)
    sys.exit(result.num_failed)
