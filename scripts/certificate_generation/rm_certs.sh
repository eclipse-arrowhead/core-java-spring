#!/bin/bash

# Created by Emanuel Palm (https://github.com/emanuelpalm)

# With great power comes great responsibility.

# Removes all keystores, certificates and truststores potentially created by
# `mk_certs.sh`.

cd "$(dirname "$0")" || exit
cd ..
find cloud-* -regex ".*\.\(p12\|crt\|jks\|pub\|key\|ca\)" -exec rm -f {} \;