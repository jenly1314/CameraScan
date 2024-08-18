#!/bin/bash

set -ex

# Generate the API docs
./gradlew dokkaHtml

mkdir -p docs/api
mv camera-scan/build/dokka/html/* docs/api

# Copy in special files that GitHub wants in the project root.
GITHUB_URL=https://github.com/jenly1314/CameraScan/
sed '/<!-- end -->/q' README.md > docs/index.md
sed -i "s|(app|(${GITHUB_URL}blob/master/app|g" docs/index.md
cat CHANGELOG.md | grep -v '## 版本日志' > docs/changelog.md

# Build the site locally
mkdocs build
