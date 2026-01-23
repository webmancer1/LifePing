# LifePing 

**LifePing** is an Android welfare-check application designed to help individuals living alone or at risk of isolation confirm their well-being through scheduled check-ins. If a user fails to check in within a defined time window, the app automatically notifies trusted contacts.

This project is built as a **privacy-first, local-first MVP** and is not intended to replace emergency or medical services.

---

##  Problem Statement

Many individuals live alone or have limited daily social interaction. In such cases, delayed awareness of an emergency or prolonged unresponsiveness can have serious consequences.

LifePing addresses this gap by providing a **simple, reliable confirmation mechanism** that escalates only when a user becomes silent.

---

##  Key Features (MVP)

- Scheduled well-being check-ins  
- One-tap **“I’m OK”** confirmation  
- Configurable check-in interval and grace period  
- Background detection of missed check-ins  
- Automatic escalation to trusted contacts  
- Local notifications and reminders  
- Status screen showing current state and history  

---

##  Out of Scope 

- Location tracking  
- Health or biometric data  
- Emergency services integration  
- Wearable devices  
- AI or predictive behavior analysis  

---

##  Tech Stack

- **Platform:** Android  
- **Language:** Kotlin  
- **UI:** Jetpack Compose  
- **Architecture:** MVVM  
- **Background Tasks:** WorkManager  
- **Persistence:** Room / DataStore  
- **Notifications:** Android Notification APIs  
- **Backend (Optional):** Firebase (Auth, Firestore, Cloud Functions)  

---

##  Architecture Overview

The app follows a **local-first MVVM architecture**:

- UI layer built with Jetpack Compose  
- ViewModels manage state and business logic  
- WorkManager handles background check-in evaluation  
- Notifications trigger reminders and escalation  
- Optional backend used only for alert delivery  

---

##   Installation
git clone https://github.com/your-username/lifeping.git

cd lifeping
