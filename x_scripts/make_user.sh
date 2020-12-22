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
    /a/beryllium_nitrate $1 | tail -n 1
}


if [[ $# -lt 2 || -z $1 || -z $2 ]]; then
    error "usage: $(basename $0) user side [pass]"
elif [[ $# -gt 3 ]]; then
    error "usage: $(basename $0) user side [pass]"
elif [[ $2 != 'LEFT' && $2 != 'RIGHT' ]]; then
    log "unknown side: $2"
    error "usage: $(basename $0) user side [pass]"
fi

if [[ $# -ne 3 || -z $3 ]]; then
    log 'no password provided, generating an (almost) insecure password'
    fin_pass=$(generate)
else
    fin_pass=$2
fi


v_id=$1
v_enabled=1
v_name=$v_id
v_side='LEFT'
v_pass=$(password $fin_pass)

db "INSERT INTO $tbl_name (id, enabled, name, side, timestamp, password) values ('$v_id', $v_enabled, '$v_name', '$v_side', $v_timestamp, '$v_pass')"
db "SELECT * FROM $tbl_name WHERE id='$v_id'"
log "$1 : $fin_pass"
echo
