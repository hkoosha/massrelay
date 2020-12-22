#!/bin/bash

set -eu

. db.sh
tbl_name='permission'

if [[ $# -ne 3 ]] || [[ -z $1 ]] || [[ -z $2 ]] || [[ -z $3 ]]; then
    error "usage: $(basename "$0") left_client_id funcode  right_client_id"
fi

v_lid=$1
v_fun=$2
v_rid=$3

db "DELETE FROM $tbl_name WHERE left_id='$v_lid' AND right_id='$v_rid' AND funcode='$v_fun'" 
db "SELECT * FROM $tbl_name WHERE left_id='$v_lid' AND right_id='$v_rid'"

