.PHONY: help build build-mac build-android clean release test

VERSION ?= $(shell cat VERSION 2>/dev/null || echo "1.0.0")

help:
	@echo "Mac Remote - Developer & Release Commands"
	@echo "-----------------------------------------"
	@echo "  make build         : Build both macOS and Android apps locally"
	@echo "  make build-mac     : Build macOS application bundle (.app)"
	@echo "  make build-android : Build Android APK (Debug)"
	@echo "  make test          : Run syntax checks and linting"
	@echo "  make clean         : Clean build artifacts, caches and temporary files"
	@echo "  make release VERSION=x.y.z : Prepare a new semantic release and git tag"

build: build-mac build-android

build-mac:
	@echo "Building macOS application bundle..."
	@cd mac-app && source venv/bin/activate && python3 setup.py py2app

build-android:
	@echo "Building Android debug APK..."
	@cd android-app && ./gradlew assembleDebug

test:
	@echo "Testing Python syntax..."
	@cd mac-app && python3 -m py_compile gui.py server.py input_controller.py discovery.py setup.py
	@echo "Testing Android build..."
	@cd android-app && ./gradlew check

clean:
	@echo "Cleaning up build artifacts..."
	@rm -rf mac-app/build mac-app/dist mac-app/*.egg-info
	@rm -rf android-app/build android-app/app/build android-app/.gradle
	@find . -name "*.pyc" -delete
	@find . -name "__pycache__" -delete
	@find . -name ".DS_Store" -delete
	@echo "Clean completed."

release:
	@./scripts/release.sh $(VERSION)
