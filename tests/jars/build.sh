#!/bin/bash
_die() {
    [[ $1 ]] || {
        printf >&2 -- 'Usage:\n\t_die <message> [return code]\n'
        [[ $- == *i* ]] && return 1 || exit 1
    }

    printf >&2 -- '%s\n' "$1"
    exit "${2:-1}"
}

_cd_out () {
  cd - 2>&1 >/dev/null
}

JAR_FOLDERS=(
    tictactoe
    minesweeper
    binary_search_tree
    static_init_test
    media_types
    springboot_asciibytes
    forward_main
    str_array_clone
    annotation_test
    deserialization
    logback
    objenesis
    reflected_constructor
)

main() {
  for jar_folder in "${JAR_FOLDERS[@]}"; do
    echo "Building jar in '${jar_folder}'!"
    cd "${jar_folder}" || _die "Folder named '${jar_folder}' does not exist in '$(pwd)'! Quitting!"
    MAVEN_OPTS="-Dorg.slf4j.simpleLogger.defaultLogLevel=warn" bash build.sh
    _cd_out || _die "Can't cd back out of '${jar_folder}' in '$(pwd)'! Quitting!"
  done
}

main
