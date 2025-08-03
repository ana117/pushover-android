# Pushover Android
<img width="160" height="160" alt="Pushover" src="https://github.com/ana117/pushover-android/blob/main/app/src/main/res/drawable/ic_pushover.png" />

This is a simple Android application designed to listen for incoming notifications and forward them to a customizable external endpoint. This allows users to centralize their notifications or integrate them with other services.
Companion app for [Pushover Desktop](https://github.com/ana117/pushover-desktop).

## Features
- **Notification Listener:** Monitors your device for new notifications.
- **Customizable Endpoint:** Easily configure the URL where notifications will be sent.
- **Background Service:** Runs in the background to continuously forward notifications.

## Getting Started
These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites
*   Android Studio
*   An Android device or emulator.

### Installation
1.  **Clone the repository:**
    ```bash
    git clone https://github.com/ana117/pushover-android.git
    ```

2.  **Open in Android Studio (or your chosen editor):**
    - Open Android Studio and select "Open an existing Android Studio project".
    - Navigate to the cloned `PushoverAndroid` directory and open it.

3.  **Build and Run:**
    - Connect your Android device or start an emulator.
    - Click the "Run" button (green triangle) in Android Studio to deploy the app to your device/emulator.

## Usage
1.  **Grant Notification Access:**
    - Upon first launch, the app will prompt you to grant "Notification Listener" permission. You must enable this for the app to function correctly.
    - Alternatively, you can go to your phone's settings: `Settings -> Apps & notifications -> Special app access -> Notification access` and enable "Pushover Android".

2.  **Configure Endpoint:**
    - Open the Pushover Android app.
    - Enter your desired external endpoint URL in the provided field. This is where the notification data will be sent.
    - Make sure your endpoint is capable of receiving HTTP POST requests.

Now, whenever a new notification arrives on your phone, Pushover will attempt to forward its details to your configured endpoint.

