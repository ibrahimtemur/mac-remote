#!/usr/bin/env bash

cd "$(dirname "$0")"

if [ ! -d "venv" ]; then
    echo "Creating virtual environment..."
    python3 -m venv venv
    source venv/bin/activate
    pip install --upgrade pip
    pip install -r requirements.txt
else
    source venv/bin/activate
fi

if [ "$1" == "--cli" ]; then
    echo "Starting Mac Remote Server (CLI)..."
    python3 server.py
else
    echo "Starting Mac Remote GUI..."
    python3 gui.py
fi
