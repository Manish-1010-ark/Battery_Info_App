# 🔋 Battery Info App

A modern Android application for **real-time battery monitoring**—displaying charging power (W), battery percentage, and estimated time remaining, all wrapped in a sleek light‑themed UI with smooth animations.

---

## 📋 Table of Contents

- [Features](#features)  
- [Tech Stack](#tech-stack)  
- [Screenshots](#screenshots)  
- [Getting Started](#getting-started)  
  - [Prerequisites](#prerequisites)  
  - [Clone & Build](#clone--build)  
  - [Run on Device/Emulator](#run-on-deviceemulator)  
- [Contact](#contact)

---

## ✨ Features

- ⚡ **Live Charging Power** in watts (W)  
- 📈 **Power Trend Graph** showing data over the last 2 minutes  
- 🔋 **Battery Percentage** & **Time Remaining** updated every second  
- 🔔 **Persistent Notification** with real‑time stats  
- 🏠 **Home Screen Widget**—tap to open the app  
- 🎨 **Light Theme** with particle‑background and smooth transitions  
- 🛠️ **One‑Time Configuration** screen to detect device specs & units

---

## 💻 Tech Stack

- **Language**: Kotlin  
- **Architecture**: MVVM with LiveData & ViewModel  
- **UI**: Jetpack Compose  
- **Graphing**: MPAndroidChart  
- **Storage**: SharedPreferences for configuration  
- **Notification**: AndroidX Notification APIs  
- **Widget**: AppWidgetProvider + RemoteViews  
- **Build**: Gradle Kotlin DSL

---

## 📸 Screenshots

<details>
  <summary>🔋 Main Screen</summary>

  ![Main Screen](screenshots/screenshot3.jpg)  
  *Real‑time charging power, percentage & estimate*
</details>

<details>
  <summary>⚙️ Configuration Screen</summary>

  ![Configuration Screen](screenshots/screenshot1.jpg)  
  *One‑time setup for device specs & units detection*
</details>

<details>
  <summary>🔔 Notification</summary>

  ![Notification Screen](screenshots/screenshot2.jpg)  
  *Persistent notification with live stats*
</details>

<details>
  <summary>🏠 Home Screen Widget</summary>

  ![Widget Screen](screenshots/screenshot4.jpg)  
  *Interactive widget—tap to launch the app*
</details>

<details>
  <summary>📊 Power Trend Graph</summary>

  ![Graph Screen](screenshots/screenshot5.jpg)  
  *Live graph of charging power over 2 minutes*
</details>

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Arctic Fox or newer  
- Android SDK 26+ installed  
- Kotlin and Gradle Kotlin DSL support  

### Clone & Build

```bash
git clone https://github.com/your-username/battery-info-app.git
cd battery-info-app
