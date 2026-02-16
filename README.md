# Local Grub 🍔

Local Grub is a modern Android application designed for ordering food from local vendors. It features a clean, intuitive interface and robust backend integration using Firebase.

## 🚀 Features

- **Phone Authentication**: Secure login via Firebase Phone Auth.
- **Dynamic Menu**: Browse a variety of dishes with real-time updates from Cloud Firestore.
- **Cart Management**: Easily add/remove items and manage quantities.
- **Order Tracking**: Keep track of your order history.
- **Profile Management**: Complete and update your user profile.
- **Offline Support**: Handles network connectivity gracefully with custom dialogs.

## 🛠 Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **Architecture**: MVVM (Model-View-ViewModel) with Clean Architecture principles.
- **UI Framework**: Native Android with [Material Components](https://material.io/components).
- **Dependency Injection**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Networking**: [Retrofit](https://square.github.io/retrofit/) & [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)
- **Database/Backend**: [Firebase](https://firebase.google.com/) (Auth, Firestore, Realtime Database, Cloud Messaging).
- **Image Loading**: [Glide](https://github.com/bumptech/glide)
- **Jetpack Components**:
    - Navigation Component
    - ViewBinding
    - ViewModel & LiveData/Flow
    - WorkManager

## 🏗 Project Structure

The project follows a modular structure based on Clean Architecture:

- `data`: Contains repositories, models, and remote/local data sources.
- `domain`: Contains business logic, use cases, and domain models.
- `ui`: Contains fragments, viewmodels, and adapters organized by feature.

## 🚦 Getting Started

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/local-grub.git
   ```
2. **Setup Firebase**:
   - Create a project on the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app and download the `google-services.json` file.
   - Place `google-services.json` in the `app/` directory.
   - Enable Phone Authentication and Firestore.
3. **Build and Run**:
   - Open the project in Android Studio.
   - Sync Gradle and run the app on an emulator or physical device.

## 📸 Screenshots

*(Add your screenshots here)*

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
Developed by **Kamlesh Prajapat** as part of an Internship Project.
