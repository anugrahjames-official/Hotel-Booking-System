#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"
javac -d out src/hotel/model/*.java src/hotel/*.java