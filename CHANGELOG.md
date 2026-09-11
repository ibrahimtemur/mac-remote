# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.1.0] - 2026-09-11

### Added
- **Interactive Setup & Connection Guide (Android):**
  - Added a dedicated setup guide button next to the "Mac Remote" header on the initial connection screen.
  - Step-by-step modal explaining how to download the Mac app from GitHub Releases, how to install and grant macOS Accessibility permissions, and how to pair using LAN or Ngrok.
  - Quick action button to open GitHub Releases page directly in the mobile browser.
- **Bilingual Interface Support (English & Turkish):**
  - **Android Client:** Added instant language switch button (🇹🇷 TR / 🇬🇧 EN) in the top-right corner of the connection screen, localizing all connection cards, scanning status, PIN dialog, and setup guide.
  - **macOS Server GUI:** Added a top-bar language selector (🇹🇷 Türkçe / 🇬🇧 English) dynamically localizing server status, Start/Stop buttons, Ngrok toggle, and accessibility permission status.
  - User language preference is automatically remembered and persisted across app restarts.

---

## [1.0.0] - 2026-09-11

### Added
- **Hardware-Level Trackpad Control:**
  - 1-finger move: Smooth mouse cursor navigation.
  - 1-finger tap: Left click.
  - 1-finger long press / 2-finger tap: Right click.
  - 2-finger drag: Fluid vertical and horizontal scrolling.
  - Double-tap and drag: Native text selection and window dragging (`kCGEventLeftMouseDragged`).
  - Dedicated "Select Text" toggle button for reliable multi-line selection.
- **Dynamic Screen Streaming:**
  - Ultra-low latency JPEG screen preview via WebSocket.
  - 4 dynamic resolution tiers: 800p (Fast), 1200p (Balanced), 1600p (Sharp Text), and 2200p (Ultra HD).
  - Preview cursor overlay toggle.
- **Media & System Controls:**
  - Play/Pause, Next track, Previous track.
  - 10-second fast-forward and rewind controls.
  - System volume control (Volume Up, Volume Down, Mute).
- **Integrated Virtual Keyboard:**
  - Expandable keyboard drawer with quick helper keys (Space, Backspace, Enter, Esc).
  - Direct full-text transmission without double-typing glitches.
- **Connectivity & Networking:**
  - Automatic local network discovery (LAN) via mDNS / Zeroconf (`_macremote._tcp.local.`).
  - Remote internet access (WAN) using automated Ngrok encrypted TLS tunnels.
  - 4-digit dynamic PIN handshake authentication.
- **DevOps & Architecture:**
  - Monorepo structure: `/mac-app` (Python 3.9+ / PyQt6) and `/android-app` (Kotlin / Jetpack Compose).
  - Automated CI build and multi-platform GitHub Release workflow.
