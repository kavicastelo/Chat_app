# Technical Documentation:

## 1. Introduction:

This is a text-based chat application(emojis and stickers are in development). users can sign up to the system using an
image, username, email, and password. they can use email and password for the login. after login or signup, users
navigate to the chat dashboard.

- Version number: **1.0.0**
- Platform: **Android**
- Language: **Java**
- JDK: **17**
- SDK: **Up to Android 5**
- Database: **Firebase**
- IDE: **Android Studio**
- Type: **Online social media**
- Users Coverage: **Small**

## 2. Architecture:

The Chat Application is an **android** base application built using **Java**. It follows a client-server architecture.

## 3. Authentication:

### Sign Up:

Users can sign up for the application using the following information

- Username: This will show to the others
- Email: One email can register once only
- Password: Use strong password
- Re-password: Verify the password

### Sign In:

Use the following information to log in to the system

- Email: Use registered email
- Password: Use the correct password according above email

Used Firebase Firestore to store user data. Images are encoded to String using Bitmap.

```groovy
FirebaseFirestore db = FirebaseFirestore.getInstance();
HashMap<String, Object> user = new HashMap<>();
user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
user.put(Constants.KEY_IMAGE, encodedImg);
db.collection(Constants.KEY_COLLECTION_USERS)
        .add(user)
        .addOnSuccessListener(documentReference -> {
            preManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
            preManager.putString(Constants.KEY_USER_ID, documentReference.getId());
            preManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
            preManager.putString(Constants.KEY_IMAGE, encodedImg);
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        })
        .addOnFailureListener(exception -> {
            showToasts(exception.getMessage());
        });
```

```groovy
FirebaseFirestore db = FirebaseFirestore.getInstance();
db.collection(Constants.KEY_COLLECTION_USERS)
        .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
        .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
        .get()
        .addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                preManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                preManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                preManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                preManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            } else {
                showToasts("Unable to SignIn");
            }
        });
```

## 4. User Interface:

- Used user-friendly and attractive user interfaces.
- User experiences are improved
- Very similar to Whatsapp application

## 5. Chat Functionality:

Users need to sign in or sign up first before starting the chats. After logging in or signing up they will navigate to
the main dashboard. Current chats are displayed here. The bottom of the screen shows a `+` button and users can start
new chats from here. After clicking the `+` button user can view the application's users list with their profile picture
and username. After clicking any of them user navigates to the chat area and both can chat in there. Both users can see
whether their last seen times, messages are delivered, seen, or only sent status.

## 6. Media Handling (Under Development):

Images, Audio, and Video sending are under the development process. I hope all functions will be integrated next release
version.

## 7. Notifications:

When the user gets a message, the application sends a notification when the application is in the background or closed.
notifications are handled by FirebaseMessagingService. Here is how to handle notifications by FirebaseMessagingService.

```groovy
public class MessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        User user = new User();
        user.id = message.getData().get(Constants.KEY_USER_ID);
        user.name = message.getData().get(Constants.KEY_NAME);
        user.token = message.getData().get(Constants.KEY_FCM_TOKEN);

        int notificationId = new Random().nextInt();
        String chanelId = "chat_message";

        Intent i = new Intent(this, chatActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.putExtra(Constants.KEY_USER, user);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, chanelId);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setContentTitle(user.name);
        builder.setContentText(message.getData().get(Constants.KEY_MESSAGE));
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(
                message.getData().get(Constants.KEY_MESSAGE)
        ));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pi);
        builder.setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence chanelName = "Chat";
            String chanelDescription = "This notification chanel is used for chat notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(chanelId, chanelName, importance);
            channel.setDescription(chanelDescription);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(notificationId, builder.build());
    }
}
```

## 8. Offline Handling:

When the user is offline application completely terminates. Senders can see messages with a `sent` status. When the user
comes back online, he/she gets all notifications for every message.

## 9. Dependencies:

```groovy
    // default
implementation 'androidx.appcompat:appcompat:1.4.1'
implementation 'com.google.android.material:material:1.5.0'
implementation 'androidx.constraintlayout:constraintlayout:2.1.3'
testImplementation 'junit:junit:4.13.2'
androidTestImplementation 'androidx.test.ext:junit:1.1.3'
androidTestImplementation 'androidx.test.espresso:espresso-core:3.4.0'

// scalable size (different sizes screens support)
implementation 'com.intuit.sdp:sdp-android:1.0.6'
implementation 'com.intuit.ssp:ssp-android:1.0.6'

//rounded image view
implementation 'com.makeramen:roundedimageview:2.3.0'

//firebase
implementation platform('com.google.firebase:firebase-bom:30.4.1')
implementation 'com.google.firebase:firebase-messaging:23.0.8'
implementation 'com.google.firebase:firebase-firestore:24.3.1'

//multidex
implementation 'androidx.multidex:multidex:2.0.1'

//Retrofit
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-scalars:2.9.0'
```

# Database Documentation:

## 1. Database Schema:

### User

The User entity represents the users' information including their name, profile picture, email, and password.

- `name`(String)
- `image`(String)
- `email`(String)
- `token`(String)
- `id`(String)

### chatMessage

chatMessage entity represents all messaging service information.

- `senderId`(String)
- `receiverId`(String)
- `message`(String)
- `dateTime`(String)
- `dateObj`(Date)
- `conversionId`(String)
- `conversionName`(String)
- `conversionImage`(String)

## 2. User Data:

- `name`(String) - Username (Nick name).
- `image`(String) - User's profile picture.
- `email`(String) - User's email address.
- `token`(String) - Unique user token.
- `id`(String) - Unique identifier for the user.

User data are stored when signing up to the application. Update and Delete methods are not integrated yet.

## 3. Chat Data:

- `senderId`(String) - Unique identifier for the sender
- `receiverId`(String) - Unique identifier for the receiver
- `message`(String) - Message content
- `dateTime`(String) - Date and time of the message
- `dateObj`(Date) - Date object
- `conversionId`(String) - Unique identifier for the conversion
- `conversionName`(String) - Name of the conversion
- `conversionImage`(String) - Profile picture of the conversion

Chat data are stored when sending messages. Update and Delete methods are not integrated yet.

# API Documentation

## ApiClient:

```groovy
public class ApiClient {

    private static Retrofit retrofit = null;
    private static ScalarsConverterFactory scalarsConverterFactory;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://fcm.googleapis.com/fcm/")
                    .addConverterFactory(scalarsConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

}
```

## API Services

```groovy
public interface ApiService {

    @POST("send")
    Call<String> sendMessage(
            @HeaderMap HashMap<String, String> headers,
            @Body String messageBody
    );

}
```

Only `sendMessage` API call is used here. Other database operations are done inside of functions themselves.

# Additional Considerations:

- **Security:** Mainly used register and sign-in options for security. Also used FCM tokens for users. Restricted
  multiple accounts for one email.
- **Performance:** Images are encoded into strings to increase the performance of the application.
- **Future Enhancements:** This application is now based on only texts. Future releases will support images, audio, and
  video too.
