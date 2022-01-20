#!/bin/bash

echo "Killing processes..."
pgrep java | xargs kill -9 $1