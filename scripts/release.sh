#!/usr/bin/env bash

set -euo pipefail

# Ensure we are at project root
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

if [ -z "${1:-}" ]; then
  echo "❌ Error: Version number required."
  echo "Usage: ./scripts/release.sh <MAJOR.MINOR.PATCH>"
  echo "Example: ./scripts/release.sh 1.1.0"
  exit 1
fi

NEW_VERSION="${1#v}" # Strip leading v if provided

# Validate SemVer pattern
if ! [[ "$NEW_VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "❌ Error: '$NEW_VERSION' is not a valid Semantic Version (expected X.Y.Z)."
  exit 1
fi

TAG_NAME="v$NEW_VERSION"

echo "🚀 Preparing release for: $TAG_NAME"

# 1. Update VERSION file
echo "$NEW_VERSION" > VERSION
echo "✅ Updated VERSION file to $NEW_VERSION"

# 2. Check CHANGELOG.md
if ! grep -q "## \[$NEW_VERSION\]" CHANGELOG.md; then
  echo "⚠️ Warning: CHANGELOG.md does not contain an entry for '## [$NEW_VERSION]'."
  echo "Please add release notes under ## [$NEW_VERSION] in CHANGELOG.md before releasing."
  read -p "Do you want to continue anyway? (y/N) " -n 1 -r
  echo
  if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    exit 1
  fi
fi

# 3. Git Operations
if [ -d ".git" ]; then
  echo "📦 Staging changes..."
  git add VERSION CHANGELOG.md mac-app/setup.py android-app/app/build.gradle.kts || true
  
  if git diff-index --quiet HEAD --; then
    echo "ℹ️ No changes to commit."
  else
    git commit -m "chore(release): bump version to $TAG_NAME"
    echo "✅ Created release commit."
  fi

  if git rev-parse "$TAG_NAME" >/dev/null 2>&1; then
    echo "⚠️ Tag $TAG_NAME already exists."
  else
    git tag -a "$TAG_NAME" -m "Release $TAG_NAME"
    echo "✅ Created git tag $TAG_NAME"
  fi

  echo ""
  echo "🎉 Release $TAG_NAME prepared successfully!"
  echo "To publish, run:"
  echo "   git push origin main --tags"
else
  echo "ℹ️ Note: Git repository not initialized yet. Run 'git init' first."
fi
