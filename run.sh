#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"
exec java --enable-native-access=ALL-UNNAMED \
  -cp "out:lib/sqlite-jdbc-3.53.2.0.jar" \
  hotel.Main