# LifePing

**LifePing** is a privacy-first, welfare-check Android application designed to help individuals living alone confirm their well-being through scheduled check-ins. If a user fails to check in within a defined window, the app automatically notifies trusted contacts, providing a crucial safety net without compromising daily privacy.

This project is built as a robust MVP, leveraging modern Android development practices and a Clean Architecture approach.

---

## 📱 Key Features

### Core Functionality
-   **Scheduled Wellness Checks:** Customizable check-in intervals to fit your routine.
-   **"I'm OK" Confirmation:** Simple, one-tap check-in process.
-   **Missed Check-in Detection:** Background monitoring that detects when a check-in is missed.
-   **Emergency Escalation:** Automatically notifies designated trusted contacts via local notifications and **direct SMS** when a check-in is completely missed.
-   **Future Integrations:** Email and WhatsApp API integrations are partially implemented and coming soon.

### Modern User Experience
-   **Material Design 3:** A beautiful, adaptive UI with dynamic color support and dark/light mode.
-   **Authentication:** Secure login and registration via Email/Password and **Google Sign-In**.
-   **User Profiles:** comprehensive profile management with photo uploads (backed by Firebase Storage).
-   **Dashboard:** Real-time status updates, next check-in countdown, and historical stats.

---

## 🛠 Tech Stack

This project uses the latest Android technologies to ensure performance, maintainability, and scalability.

-   **Language:** [Kotlin](https://kotlinlang.org/) (100%)
-   **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
-   **Architecture:** MVVM (Model-View-ViewModel) with Clean Architecture principles.
-   **Dependency Injection:** [Hilt](https://dagger.dev/hilt/)
-   **Asynchronous Programming:** [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
-   **Navigation:** [Compose Navigation](https://developer.android.com/guide/navigation/navigation-compose)
-   **Local Storage:**
    -   [Room Database](https://developer.android.com/training/data-storage/room) for structured data.
    -   [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) for user preferences.
-   **Background Processing:** [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) for reliable check-in monitoring.
-   **Backend & Cloud (Firebase):**
    -   **Authentication:** Google Sign-In & Email/Password.
    -   **Firestore:** remote user profile and settings backup.
    -   **Cloud Storage:** Profile picture storage.
-   **Image Loading:** [Coil](https://coil-kt.github.io/coil/)

---

## 🏗 Architecture Overview

The app follows the **Guide to App Architecture** recommendations:

1.  **UI Layer:** Composable screens and ViewModels. ViewModels expose state via `StateFlow` and handle user intent.
2.  **Data Layer:** Repositories that abstract data sources.
    -   **Local:** Room logic and DataStore.
    -   **Remote:** Firebase implementation for auth and backups.

---

## 🚀 Getting Started

### Prerequisites
-   Android Studio Koala or newer.
-   JDK 17 or newer.
-   A Firebase project (for Auth/Firestore features).

### Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-username/lifeping.git
    cd lifeping
    ```

2.  **Set up Firebase:**
    -   Create a project in the [Firebase Console](https://console.firebase.google.com/).
    -   Add an Android app with package name `com.example.lifeping`.
    -   Download `google-services.json` and place it in the `app/` directory.
    -   Enable **Authentication** (Email/Password, Google Sign-In).
    -   Enable **Firestore** and **Storage**.

3.  **Build and Run:**
    -   Open the project in Android Studio.
    -   Sync Gradle files.
    -   Run the app on an emulator or physical device (API 26+).

---

## 🤝 Contributing

Contributions are welcome! If you have suggestions for improvements or bug fixes:

1.  Fork the repository.
2.  Create a feature branch (`git checkout -b feature/NewFeature`).
3.  Commit your changes.
4.  Push to the branch (`git push origin feature/NewFeature`).
5.  Open a Pull Request.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
