#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"
echo "Compiling Hotel Booking System..."
javac -d out src/hotel/model/*.java src/hotel/*.java src/hotel/web/*.java
echo "Compilation successful."
echo
echo "========================================"
echo " HOTEL BOOKING SYSTEM - WEB APPLICATION"
echo "========================================"
echo
exec java --enable-native-access=ALL-UNNAMED \
  -cp "out:lib/sqlite-jdbc-3.53.2.0.jar" \
  hotel.web.WebServer