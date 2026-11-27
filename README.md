# Phone Battery Monitor

Monitor your Android phone's battery directly from your MacBook over a local Wi-Fi network.

Phone Battery Monitor is a small cross-platform project consisting of an Android companion app and a Python-based Mac server with a battery monitoring dashboard.

## Overview

The project allows an Android device to share its battery information with a MacBook. The Mac displays the received information through a clean, dark-themed dashboard.

No cloud account or external battery-monitoring service is required. Both devices communicate over the same local Wi-Fi network.

## Features

- **Battery percentage:** View your phone's current battery level.
- **Charging status:** Know whether your phone is charging.
- **Battery temperature:** Monitor battery temperature in Celsius.
- **Battery voltage:** View the battery's voltage.
- **Connection status:** See whether your Android device is connected.
- **Automatic updates:** Battery information refreshes while sharing is enabled.
- **Local communication:** Transfer battery information over your local Wi-Fi network.
- **Shared-token authentication:** Protect the communication endpoint with a matching token.

## Screenshots

### macOS Dashboard

![Phone Battery Monitor macOS Dashboard](screenshots/mac-dashboard.png)

### Android Companion App

![Phone Battery Monitor Android Notification](screenshots/android-sharing.png)

## Tech Stack

| Component | Technology |
|---|---|
| Android application | Kotlin / Android |
| Mac-side server | Python |
| Communication | HTTP over local Wi-Fi |
| Dashboard | Web-based interface |
| Development environment | Android Studio / macOS |

## How It Works

1. The Android companion app collects battery information from the device.
2. The app sends the information to the Mac server over the local network.
3. The Python server receives and processes the incoming data.
4. The dashboard displays the latest battery information.

Both devices must be connected to the same Wi-Fi network.

## Getting Started

### Requirements

- An Android phone.
- A Mac running macOS.
- Python installed on the Mac.
- Android Studio.
- Both devices connected to the same Wi-Fi network.

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/phone-battery-monitor.git

cd phone-battery-monitor
```

### 2. Start the Mac server

```bash
cd mac-server

python3 -m venv venv

source venv/bin/activate

pip install -r requirements.txt
```

Configure the shared authentication token in `server.py` and use the same token in the Android application.

Start the server:

```bash
python server.py
```

### 3. Find your Mac's local IP address

On your Mac terminal, run:

```bash
ipconfig getifaddr en0
```

Use the returned IP address in the Android application.

### 4. Configure the Android application

1. Open the `android-app` directory in Android Studio.
2. Build and install the application on your Android device.
3. Enter your Mac's local IP address.
4. Configure the same shared token used by the Mac server.
5. Start battery sharing.

### 5. Open the dashboard

On your Mac, open:

```text
http://127.0.0.1:8765
```

Your phone's battery information should appear once the devices connect successfully.

## Screenshots

Place your screenshots inside the `screenshots/` directory:

```text
screenshots/
├── mac-dashboard.png
└── android-sharing.png
```

## Security Notes

- Use a unique shared token instead of the development default.
- Never commit personal authentication tokens or credentials.
- Keep the server restricted to your trusted local network.
- The current implementation uses local HTTP communication, not encrypted internet transport.
- Do not expose the server port directly to the public internet.

## Current Status

Working prototype tested with a Redmi 9 Prime and a MacBook Air.

## Future Improvements

- Battery history and usage graphs.
- Multiple-device support.
- Low-battery notifications on macOS.
- Improved connection management.
- Battery health and charging-cycle information, where supported by the device.

## License

This project is licensed under the MIT License.
