#!/bin/bash

set -euf

export user='root'
export pass=''
export db_name='bs'
export v_timestamp="$(date +%s)000"

function error() {
    # shellcheck disable=SC2145
    echo -e "\nERROR :: $@ \n"
    exit 1
}

function log() {
    # shellcheck disable=SC2145
    echo ":: $@"
}

function _db() {
    mysql $db_name --user=$user --password="$pass" -e "$@" || error 'failed'
}

function db() {
    echo
    log "$1"
    _db "$1"
    echo
}


