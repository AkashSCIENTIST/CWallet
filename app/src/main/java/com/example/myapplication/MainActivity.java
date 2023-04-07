package com.example.myapplication;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    EditText username, password;
    DatabaseReference userExistRef;
    Button signInButton, signUpButton;
    ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        signInButton = findViewById(R.id.signInbuttonMain);
        signUpButton = findViewById(R.id.signUpbuttonMain);
        progressBar = findViewById(R.id.progressBar);
    }

    public void signin(View view) {
        String userName = String.valueOf(username.getText()).trim();
        String passWord = String.valueOf(password.getText()).trim();
        if(userName.equals("") || passWord.equals("")){
            Toast.makeText(MainActivity.this, "Fill the fields", Toast.LENGTH_SHORT).show();
            return;
        }
        signUpButton.setVisibility(View.INVISIBLE);
        signInButton.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        userExistRef = FirebaseDatabase.getInstance().getReference().child("passwords").child(userName);
        ValueEventListener responseListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    Toast.makeText(MainActivity.this, "User doesn't exist", Toast.LENGTH_SHORT).show();
                    signUpButton.setVisibility(View.VISIBLE);
                    signInButton.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                    password.setText("");
                    username.setText("");
                }
                else{
                    String pass = snapshot.getValue(String.class);
                    if(pass.equals(passWord)){
                        Log.i("CWallet", "Password matched");
                        Intent intent = new Intent(view.getContext(), UserActivity.class);
                        intent.putExtra("name", userName);
                        signUpButton.setVisibility(View.VISIBLE);
                        signInButton.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                        view.getContext().startActivity(intent);
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Wrong Credentials", Toast.LENGTH_SHORT).show();
                        signUpButton.setVisibility(View.VISIBLE);
                        signInButton.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                        password.setText("");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CWallet", error.toString());
            }
        };
        userExistRef.addValueEventListener(responseListener);
    }

    public void signup(View view) {
        Log.i("CWallet", "Password matched");
        Intent intent = new Intent(view.getContext(), SignUp.class);
        view.getContext().startActivity(intent);
    }
}