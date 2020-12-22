#!/bin/bash

set -eux

rm -rf key
mkdir key

echo ''
openssl genrsa -out key/key 4096

echo ''
openssl req -days 3650 -batch -new -sha256 -key key/key -out key/csr

echo ''
openssl rsa -in key/key -out key/key.pem

echo ''
openssl x509 -in key/csr -out key/cert.pem -req -signkey key/key.pem -days 3650

echo ''
cat key/key.pem >> key/cert.pem

echo ''
openssl pkcs12 -export -in key/cert.pem -out key/p12 -passin pass:000000 -passout pass:000000

echo ''
base64 -w 0 < key/p12 > key/p12.keystore.b64

