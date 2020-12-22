#!/bin/bash

set -eu

. db.sh
tbl_name='client'

if [[ $# -ne 1 || -z $1 ]]; then
    error "usage: $(basename "$0") user"
fi

v_id=$1

db "DELETE FROM z_client_request_history WHERE client_id='$v_id'"
db "DELETE FROM z_accounting WHERE client_id='$v_id'"
db "DELETE FROM z_access WHERE left_id='$v_id'"
db "DELETE FROM z_access WHERE right_id='$v_id'"
db "DELETE FROM permission WHERE left_id='$v_id'"
db "DELETE FROM permission WHERE right_id='$v_id'"
db "DELETE FROM $tbl_name WHERE id='$v_id'"
db "SELECT * FROM $tbl_name WHERE id='$v_id'"
echo
