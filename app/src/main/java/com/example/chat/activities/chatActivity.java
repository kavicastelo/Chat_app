package com.example.chat.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.chat.adapters.chatAdapters;
import com.example.chat.databinding.ActivityChatBinding;
import com.example.chat.models.User;
import com.example.chat.models.chatMessage;
import com.example.chat.network.ApiClient;
import com.example.chat.network.ApiService;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.preferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class chatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User receiverUser;
    private List<chatMessage> chtMsg;
    private chatAdapters chtAdapters;
    private preferenceManager preManager;
    private FirebaseFirestore db;
    private String conversationId = null;
    private Boolean isReceiverAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preManager = new preferenceManager(getApplicationContext());
        loadReceiverDetails();
        setListeners();
        init();
        listenMessages();

    }

    private void init(){
        preManager = new preferenceManager(getApplicationContext());
        chtMsg = new ArrayList<>();
        chtAdapters = new chatAdapters(
                chtMsg,
                getBitmapFromEncodedString(receiverUser.image),
                preManager.getString(Constants.KEY_USER_ID)
        );
        binding.chatRecyclerView.setAdapter(chtAdapters);
        db = FirebaseFirestore.getInstance();
    }

    private void sendMessage(){
        HashMap<String,Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID,preManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID,receiverUser.id);
        message.put(Constants.KEY_MESSAGE,binding.inputMessage.getText().toString());
        message.put(Constants.KEY_TIMESTAMP,new Date());
        db.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        if(conversationId != null){
            updateConversation(binding.inputMessage.getText().toString());
        }else{
            HashMap<String,Object> conversation = new HashMap<>();
            conversation.put(Constants.KEY_SENDER_ID,preManager.getString(Constants.KEY_USER_ID));
            conversation.put(Constants.KEY_SENDER_NAME,preManager.getString(Constants.KEY_NAME));
            conversation.put(Constants.KEY_SENDER_IMAGE,preManager.getString(Constants.KEY_IMAGE));
            conversation.put(Constants.KEY_RECEIVER_ID,receiverUser.id);
            conversation.put(Constants.KEY_RECEIVER_NAME,receiverUser.name);
            conversation.put(Constants.KEY_RECEIVER_IMAGE,receiverUser.image);
            conversation.put(Constants.KEY_LAST_MESSAGE,binding.inputMessage.getText().toString());
            conversation.put(Constants.KEY_TIMESTAMP, new Date());
            addConversation(conversation);
        }
        if(!isReceiverAvailable){
            try{
                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.token);

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID,preManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME,preManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN,preManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE,binding.inputMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA,data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS,tokens);

                sendNotification(body.toString());
            }catch (Exception e){
                showToasts(e.getMessage());
            }
        }
        binding.inputMessage.setText(null);
    }

    private void addEmojis(){

    }

    private void showToasts(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String messageBody){
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()){
                    try{
                        if(response.body()!=null){
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if(responseJson.getInt("failure")==1){
                                JSONObject error = (JSONObject) results.get(0);
                                showToasts(error.getString("error"));
                                return;
                            }
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                    showToasts("Notification sent successfully");
                }else{
                    showToasts("Error: "+ response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showToasts(t.getMessage());
            }
        });
    }

    private void listenAvailabilityOfReceiver(){
        db.collection(Constants.KEY_COLLECTION_USERS).document(receiverUser.id).addSnapshotListener(chatActivity.this,(value, error) -> {
            if(error != null){
                return;
            }
            if(value != null){
                if(value.getLong(Constants.KEY_AVAILABILITY) != null){
                    int availability = Objects.requireNonNull(value.getLong(Constants.KEY_AVAILABILITY)
                    ).intValue();
                    isReceiverAvailable = availability == 1;
                }
                receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);
                if(receiverUser.image != null){
                    receiverUser.image = value.getString(Constants.KEY_IMAGE);
                    chtAdapters.setReceiverProfile(getBitmapFromEncodedString(receiverUser.image));
                    chtAdapters.notifyItemRangeChanged(0,chtMsg.size());
                }
            }
            if(isReceiverAvailable){
                binding.txtAvailability.setVisibility(View.VISIBLE);
            }else{
                binding.txtAvailability.setVisibility(View.GONE);
            }
        });
    }

    private void listenMessages(){
        db.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,preManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);
        db.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID,receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error != null){
            return;
        }
        if(value != null){
            int count = chtMsg.size();
            for(DocumentChange documentChange : value.getDocumentChanges()) {
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    chatMessage chatMessage = new chatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObj = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chtMsg.add(chatMessage);
                }
            }
            Collections.sort(chtMsg, (obj1, obj2) -> obj1.dateObj.compareTo(obj2.dateObj));
            if(count == 0){
                chtAdapters.notifyDataSetChanged();
            }else{
                chtAdapters.notifyItemRangeInserted(chtMsg.size(), chtMsg.size());
                binding.chatRecyclerView.smoothScrollToPosition(chtMsg.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if(conversationId == null){
            checkForConversation();
        }
    };

    private Bitmap getBitmapFromEncodedString(String encodedImg){
        if(encodedImg != null){
            byte[] bytes = Base64.decode(encodedImg, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
        }else{
            return null;
        }
    }

    private void loadReceiverDetails(){
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.txtName.setText(receiverUser.name);
    }

    private void setListeners(){
        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.layoutSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    private String getReadableDateTime(Date date){
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void addConversation(HashMap<String,Object> conversation){
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversation)
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
    }

    private void updateConversation(String message){
        DocumentReference documentReference =
                db.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversationId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_TIMESTAMP, new Date()
        );
    }

    private void checkForConversation(){
        if(chtMsg.size() != 0){
            checkForConversationRemotely(
                    preManager.getString(Constants.KEY_USER_ID),
                    receiverUser.id
            );
            checkForConversationRemotely(
                    receiverUser.id,
                    preManager.getString(Constants.KEY_USER_ID)
            );
        }
    }

    private void  checkForConversationRemotely(String senderId, String receivedId){
        db.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receivedId)
                .get()
                .addOnCompleteListener(conversationOnCompleteListener);
    }

    private final OnCompleteListener<QuerySnapshot> conversationOnCompleteListener = task -> {
        if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}