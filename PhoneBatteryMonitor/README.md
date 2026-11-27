# Phone Battery Monitor

A local-only battery dashboard for a Redmi 9 Prime and a Mac.

## How it works
- The Mac runs a small Python/Flask dashboard on your local Wi-Fi.
- The Android companion app reads Android's battery status and sends it to the Mac.
- No cloud service or account is involved.

## 1. Start the Mac dashboard

Open Terminal on your Mac:

```bash
cd mac-server
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python server.py
```

The server prints your Mac's local IP addresses and listens on port `8765`.
Open `http://127.0.0.1:8765` on your Mac.

If Python says Flask is missing, make sure the virtual environment is activated and run `pip install -r requirements.txt` again.

## 2. Build the Android companion app

1. Open Android Studio.
2. Choose **Open** and select the `android-app` folder.
3. Let Gradle sync finish.
4. Connect your Redmi 9 Prime with USB debugging enabled, or use wireless debugging.
5. Run the `app` configuration on your phone.
6. On the phone, enter your Mac's local IP address (shown in the Mac Terminal), port `8765`, and the shared token shown below.
7. Tap **Start sharing** and allow notifications if prompted.

Shared token (change this in both `mac-server/server.py` and the phone app if you want):
`redmi-local-2026`

## 3. Network notes
- Both devices must be on the same Wi-Fi network.
- If the phone cannot connect, check macOS Firewall and allow incoming connections for Python.
- Some guest Wi-Fi networks isolate devices from one another; use your normal home Wi-Fi.
- Keep the Android app's ongoing notification active while sharing. MIUI battery optimization may stop background work; set the app's battery saver setting to **No restrictions** if updates stop.

## MVP limitations
- This is a starter project, not a signed/release-ready app.
- The Android app sends an update every 60 seconds while its foreground service is running.
- The dashboard keeps the latest reading in memory; restarting the Mac server clears it.
- Battery health/capacity is not exposed reliably by standard Android APIs, so this version reports level, charging state, temperature and voltage where available.
