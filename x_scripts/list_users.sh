#!/bin/bash

set -eu

. db.sh
tbl_name='client'

if [[ $# -eq 1 ]]; then
    cond=" WHERE id LIKE '%$1%'"
elif [[ $# -eq 2 ]]; then
    cond=" WHERE id LIKE '%$1%' AND side='$2'"
elif [[ $# -gt 2 ]]; then
    error "usage: $(basename "$0") pattern [side]"
else
    cond=""
fi

db "SELECT id, enabled, name, side, timestamp FROM $tbl_name $cond"

