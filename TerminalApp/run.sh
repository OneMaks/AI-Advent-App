#!/bin/bash
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
java -jar "$SCRIPT_DIR/build/libs/kotlin-lmstudio-chat-1.0.0-all.jar" "$@"
