#!/bin/bash

_die() {
    [[ $1 ]] || {
        printf >&2 -- 'Usage:\n\t_die <message> [return code]\n'
        [[ $- == *i* ]] && return 1 || exit 1
    }

    printf >&2 -- '%s\n' "$1"
    exit "${2:-1}"
}

CURRENT_DIR="$(pwd)"
./gradlew clean check jar publishToMavenLocal || _die "Build/Unit Test failed"
cd tests || _die "Can't change directory to ${CURRENT_DIR}/tests"
python run_system_tests.py --build-first
cd - || _die "Can't change directory back to ${CURRENT_DIR}"
