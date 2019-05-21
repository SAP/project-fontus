#!/usr/bin/env python3

from os import path

import argparse
import pprint
import json
import sys
import subprocess
from pathlib import Path

SCRIPT_PATH = path.dirname(path.abspath(__file__))
PROJECT_BASE_PATH = path.join(SCRIPT_PATH, '..')
ARTIFACT_DIR = path.join(PROJECT_BASE_PATH, "build", "libs")
TESTS_DIR = path.join(PROJECT_BASE_PATH, "tests")
TESTS_SOURCE_DIR = path.join(TESTS_DIR, "src")
CONFIG_FILE = path.join(TESTS_DIR, "config.json")

JAR_BASE_NAMES = ["asm_test", "util"]


def is_existing_file(p):
    if not p:
        return False
    file = Path(p)
    return file.exists() and file.is_file()


class ReturnValueMismatch(Exception):
    def __init__(self, expected_rv, rv):
        self.expected_return_value = expected_rv
        self.return_value = rv

class StdoutMismatch(Exception):
    def __init__(self, expected_stdout, stdout):
        self.expected_stdout = expected_stdout
        self.stdout = stdout

class StderrMismatch(Exception):
    def __init__(self, expected_stderr, stderr):
        self.expected_stderr = expected_stderr
        self.stderr = stderr

class ExpectedResult:
    def __init__(self, result_dict):
        self._return_value = result_dict.get('rv')
        self._stdout_file = result_dict.get('stdout')
        self._stderr_file = result_dict.get('stderr')
        self._stdout = ""
        self._stderr = ""
        if is_existing_file(self._stdout_file):
            with open(self._stdout_file, "r", encoding="utf-8") as stdout_in:
                self._stdout = stdout_in.read(-1)

        if is_existing_file(self._stderr_file):
            with open(self._stderr_file, "r", encoding="utf-8") as stderr_in:
                self._stderr = stderr_in.read(-1)


    def matches(self, cp):
        if not cp.returncode == self._return_value:
            raise ReturnValueMismatch(self._return_value, cp.returncode)
        if not cp.stdout == self._stdout:
            raise StdoutMismatch(self._stdout, cp.stdout)
        if not cp.stderr == self._stderr:
            raise StderrMismatch(self._stderr, cp.stderr)

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
        return ('ExpectedResult(return_value={self._return_value!r}, '
                'stdout={self._stdout!r}, stderr={self._stderr!r})'
                ).format(self=self)


class TestCase:
    def __init__(self, test_dict):
        self._name = test_dict.get('name')
        self._source = test_dict.get('source')
        self._source_path = path.join(TESTS_SOURCE_DIR, self._source)
        self._javac_version = test_dict.get('javac_version')
        self._expected_result = ExpectedResult(test_dict.get('expected_result'))

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
    def javac_version(self):
        return self._javac_version

    @property
    def expected_result(self):
        return self._expected_result

    def __repr__(self):
        return ('TestCase(name={self._name!r}, source={self._source!r}, '
                'source_path={self._source_path!r}, '
                'javac_version={self._javac_version!r}, '
                'expected_result={self._expected_result!r})').format(self=self)

class Configuration:
    def __init__(self, test_cases):
        self._test_cases = test_cases

    @property
    def test_cases(self):
        return self._test_cases

    def __repr__(self):
        return 'Configuration(test_cases={self._test_cases!r})'.format(self=self)

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

    config = None
    with open(config_path, "r", encoding="utf-8") as config_in:
        config = json.load(config_in)

    pprint.pprint(config)
    tests = []
    for test_case in config.get('tests', []):
        test = TestCase(test_case)
        tests.append(test)

    return Configuration(tests)

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--build-first", action="store_true")
    parser.add_argument("--version", default="0.0.1-SNAPSHOT")
    parser.add_argument("--config", default=CONFIG_FILE)

    args = parser.parse_args()
    print(args)
    if args.build_first:
        compile_project()

    if check_output_files_existence(args.version) is False:
        print('Failed to compile java project!')
        sys.exit(1)

    CONFIG = parse_config(args.config)
    pprint.pprint(CONFIG)
    print("done!")
