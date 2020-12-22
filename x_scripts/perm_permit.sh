#!/bin/bash

set -eu

. db.sh
tbl_name='permission'

if [[ $# -ne 3 ]] || [[ -z $1 ]] || [[ -z $2 ]] || [[ -z $3 ]]; then
    error "usage: $(basename $0) left_client_id funcode  right_client_id"
elif [[ $2  != 'TCP' && $2 != 'SERIAL' ]]; then
    log "unknown funcode: $2"
    error "usage: $(basename $0) left_client_id funcode  right_client_id"
fi

v_lid=$1
v_fun=$2
v_rid=$3
v_id=$(_db 'SELECT MAX(id) FROM permission' | tail -n 1)
if [[ $v_id =~ ^[0-9]+$ ]]; then
    v_id=1;
fi
v_id=$((v_id + 1))

db "INSERT INTO $tbl_name (id, funcode, timestamp, left_id, right_id) values ('$v_id', '$v_fun', '$v_timestamp', '$v_lid', '$v_rid')"
db "SELECT * FROM $tbl_name WHERE ID='$v_id'"

