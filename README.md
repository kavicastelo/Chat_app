# Technical Documentation:
1. Introduction:
- Brief overview of the application.
- Version number: 1.0.0
- Platform: Android
- Language: Java
- JDK: 17
- SDK: Up to Android 5
- Database: Firebase
- IDE: Android Studio
- Type: Online social media
- Users Coverage: Small
2. Architecture:
- Describe the overall architecture of the application, including client-server communication, data flow, and key components.
- Mention any architectural patterns used (e.g., MVVM, MVP).
3. Authentication:
- Explain the signup and login process.
- Describe the data transmitted during authentication.
- Explain how Firebase Authentication is integrated.
4. User Interface:
- Describe the user interface components, including chat dashboard, user profiles, and chat interfaces.
- Explain the navigation flow within the application.
5. Chat Functionality:
- Detail how users start new chats.
- Describe how chats are displayed on the main dashboard.
- Explain real-time message delivery and seen status.
6. Media Handling (Under Development):
- Mention that images, videos, and audios handling are in the development process.
7. Notifications:
- Describe how notifications are handled when the app is in the background or closed.
- Explain the Firebase Messaging Service implementation for message notifications.
8. Offline Handling:
- Explain how the application handles offline scenarios.
- Describe the message displayed when the user is offline.
9. Dependencies:
- List external libraries and frameworks used in the project, along with their versions.
- Include Firebase dependencies and any other third-party libraries.

# Database Documentation:
1. Database Schema:
- Describe the structure of the Firebase Realtime Database.
- Define the key entities and their relationships.
2. User Data:
- Detail the user data stored in the database, including image, username, email, and other relevant information.
- Explain how user data is retrieved and updated.
3. Chat Data:
- Explain how chat data is stored, including message content, timestamps, and sender/receiver information.
- Describe the structure of chat-related data.
- 
# API Documentation (Firebase Messaging Service):
1. onNewToken:
- Explain the purpose of this method.
- Detail what happens when a new FCM token is generated.
2. onMessageReceived:
- Describe the process of handling incoming messages.
- Explain how notifications are created and displayed to the user.
- Mention how the data payload is parsed and used to create notifications.

# Additional Considerations:
- Security: Describe any security measures implemented, especially regarding user data and authentication.
- Performance: Mention any optimizations made for performance, such as database indexing or image loading strategies.
- Future Enhancements: Discuss any planned features or improvements for future versions of the application.
