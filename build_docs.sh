#!/bin/bash

# The website is built using MkDocs with the Material theme.
# https://squidfunk.github.io/mkdocs-material/
# It requires Python to run.
# Install the packages with the following command:
# pip install mkdocs mkdocs-material mkdocs-redirects

set -ex

# Generate the API docs
./gradlew dokkaHtml

mv camera-scan/build/dokka/html docs/api

# Copy in special files that GitHub wants in the project root.
cp README.md docs/index.md
cp CHANGELOG.md docs/changelog.md

# Build the site locally
mkdocs build
