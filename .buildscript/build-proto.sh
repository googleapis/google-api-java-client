#!/bin/bash

set -ev

PROTO_VERSION="$1"
PROTO_FOLDER="/tmp/proto$PROTO_VERSION"

# Can't check for presence of directory as cache auto-creates it.
if [ ! -f "$PROTO_FOLDER/bin/protoc" ]; then
  wget -O - "https://github.com/google/protobuf/archive/v${PROTO_VERSION}.tar.gz" | tar xz -C /tmp
  cd "/tmp/protobuf-${PROTO_VERSION}"
  ./autogen.sh
  ./configure --prefix="$PROTO_FOLDER" --disable-shared
  make -j2
  make install
fi
echo "protoc is available at $PROTO_FOLDER/bin/protoc"
