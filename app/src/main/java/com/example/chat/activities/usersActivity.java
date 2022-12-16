package com.example.chat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.chat.adapters.usersAdapter;
import com.example.chat.databinding.ActivityUsersBinding;
import com.example.chat.listeners.userListeners;
import com.example.chat.models.User;
import com.example.chat.utilities.Constants;
import com.example.chat.utilities.preferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class usersActivity extends BaseActivity implements userListeners {

    private ActivityUsersBinding binding;
    private preferenceManager preManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preManager = new preferenceManager(getApplicationContext());

        getUsers();
    }

    private void setListeners(){
        binding.imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void getUsers(){
        loading(false);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() != null){
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if(currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if(users.size() > 0){
                            usersAdapter adapter = new usersAdapter(users,this);
                            binding.userRecyclerView.setAdapter(adapter);
                            binding.userRecyclerView.setVisibility(View.VISIBLE);
                        }
                        else {
                            showErrorMessage();
                        }
                    }
                    else {
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage(){
        binding.txtErrorMsg.setText(String.format("%s", "No users available"));
        binding.txtErrorMsg.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else{
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent i = new Intent(getApplicationContext(), chatActivity.class);
        i.putExtra(Constants.KEY_USER,user);
        startActivity(i);
        finish();
    }
}