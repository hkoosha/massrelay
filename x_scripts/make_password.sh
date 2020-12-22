#!/bin/bash

set -eu

. db.sh
auto_pass_len=8
tbl_name='client'

function generate() {
    # hmmm, bad.
    local r=''
    while [[ ${#r} -lt $auto_pass_len ]]; do
        r="$r$RANDOM"
    done
    echo "${r:0:$auto_pass_len}"
}

function password() {
    /a/beryllium_nitrate "$1" | tail -n 1
}


if [[ $# -lt 1 || -z $1 ]]; then
    error "usage: $(basename "$0") user [pass]"
elif [[ $# -gt 2 ]]; then
    error "usage: $(basename "$0") user [pass]"
fi

if [[ $# -ne 2 || -z $2 ]]; then
    log 'no password provided, generating an (almost) insecure password'
    fin_pass=$(generate)
else
    fin_pass=$2
fi


v_id=$1
v_pass=$(password "$fin_pass")

db "UPDATE $tbl_name SET password= '$v_pass' WHERE id='$v_id'"
log "$1 : $fin_pass"
echo
