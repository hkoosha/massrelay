#!/bin/bash

set -eu

. db.sh
tbl_name='client'

if [[ $# -ne 1 || -z $1 ]]; then
    error "usage: $(basename "$0") user"
fi

v_id=$1

db "DELETE FROM $tbl_name WHERE id='$v_id'"
db "SELECT * FROM $tbl_name WHERE id='$v_id'"
echo
