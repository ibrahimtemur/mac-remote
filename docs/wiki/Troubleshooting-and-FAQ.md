# 🔧 Troubleshooting & FAQ

Here you will find solutions to the most common questions and issues encountered when using Mac Remote.

---

## ❓ Frequently Asked Questions

### Q1: My Android device cannot find my Mac automatically.
**Checklist:**
1. **Wi-Fi Check:** Verify that both your Mac and Android phone are on the **exact same Wi-Fi network**.
2. **Guest Network / AP Isolation:** Some public or hotel Wi-Fi networks and "Guest" networks have *Client Isolation (AP Isolation)* turned on, which blocks devices on the same Wi-Fi from talking to each other. If on a guest network, use the **Ngrok** mode instead.
3. **macOS Firewall:**
   - Go to **System Settings > Network > Firewall**.
   - Ensure incoming connections to `Mac Remote.app` or Python are allowed.
4. **Manual Connection:** You can always connect directly using your Mac's local IP address:
   - On Mac, check your IP in `System Settings > Network > Wi-Fi > Details` (e.g. `192.168.1.50`).
   - In the Android app, enter: `ws://192.168.1.50:8080`.

---

### Q2: I am connected, but moving my finger doesn't move the Mac mouse.
**Cause:** macOS Accessibility permissions are missing or revoked.
**Solution:**
1. Open **System Settings > Privacy & Security > Accessibility**.
2. If `Mac Remote` is listed, turn the toggle **OFF and then ON again**.
3. If it is not listed, click the `+` button and add `/Applications/Mac Remote.app`.
4. Restart `Mac Remote.app`. The green indicator `✓ Accessibility: Granted` must appear.

---

### Q3: Why does Ngrok show `Ngrok Error: ...`?
**Possible Causes:**
1. **Missing or Expired AuthToken:** Sign in to [ngrok.com](https://ngrok.com) and confirm your AuthToken is active.
2. **Concurrent Tunnels:** Free Ngrok accounts permit only 1 active tunnel at a time. If another tunnel process is running on your computer, quit it before starting Mac Remote.
3. **Network Firewall:** Some corporate or school networks block the Ngrok protocol.

---

### Q4: Is my data safe? Does Mac Remote record my keys?
**Answer: Absolutely safe.**
- Local connections (`ws://192.168.x.x:8080`) are strictly peer-to-peer over your private home/office router.
- No analytics, trackers, telemetry, or server-side logging of keystrokes exist.
- When using Ngrok, your traffic is encrypted end-to-end via TLS (`wss://`).
- The full source code is public and open-source under the MIT license.
