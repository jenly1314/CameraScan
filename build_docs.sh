#!/bin/bash

# The website is built using MkDocs with the Material theme.
# https://squidfunk.github.io/mkdocs-material/
# It requires Python to run.
# Install the packages with the following command:
# pip install mkdocs mkdocs-material mkdocs-redirects

set -ex

# Generate the API docs
./gradlew dokkaHtml

mkdir -p docs/api
mv camera-scan/build/dokka/html/* docs/api

# Copy in special files that GitHub wants in the project root.
sed '/[end](#)/q' README.md > docs/index.md
cat CHANGELOG.md | grep -v '## 版本说明' > docs/changelog.md

# Build the site locally
mkdocs build
