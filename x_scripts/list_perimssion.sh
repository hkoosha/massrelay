#!/bin/bash

set -eu

. db.sh
tbl_name='permission'

if [[ $# -eq 1 ]]; then
    cond=" WHERE client_id LIKE '%$1%'"
elif [[ $# -gt 1 ]]; then
    error "usage: $(basename "$0") pattern"
else
    cond=""
fi

db "SELECT id, timestamp, left_id, funcode, right_id FROM $tbl_name $cond"

