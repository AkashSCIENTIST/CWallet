package com.example.myapplication;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class SignUp extends AppCompatActivity {
    EditText userName, passWord, recoveryPhase;
    DatabaseReference passwordsRootRef, usersRootRef, balanceRootRef, recoveryRootRef, passwordRef;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        userName = findViewById(R.id.usernameSignup);
        passWord = findViewById(R.id.passwordSignup);
        recoveryPhase = findViewById(R.id.recovery);
        passwordsRootRef = FirebaseDatabase.getInstance().getReference().child("passwords");
        balanceRootRef = FirebaseDatabase.getInstance().getReference().child("balance");
        usersRootRef = FirebaseDatabase.getInstance().getReference().child("users");
        recoveryRootRef = FirebaseDatabase.getInstance().getReference().child("recovery");
    }

    public void create(View view) {
        String username = String.valueOf(userName.getText()).trim();
        String password = String.valueOf(passWord.getText()).trim();
        String recoveryphase = String.valueOf(recoveryPhase.getText()).trim();
        Log.i("CWallet", username);
        Log.i("CWallet", password);
        Log.i("CWallet", recoveryphase);
        if(username.equals("") || password.equals("") || recoveryphase.equals("")){
            Toast.makeText(SignUp.this, "Please enter all details", Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            passwordRef = FirebaseDatabase.getInstance().getReference().child("passwords").child(username);

            ValueEventListener responseListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.getValue() != null)
                    Log.i("CWallet", snapshot.getValue().toString());
                    else Log.i("CWallet", "Ref not found");
                    if(snapshot.exists()){
                        alertUserExist();
                    }
                    else{
                        passwordsRootRef.child(username).setValue(password);
                        recoveryRootRef.child(username).setValue(recoveryphase);
                        balanceRootRef.child(username).setValue(10000);
                        usersRootRef.child(username).child("btc").setValue(new PriceEntity("BitCoin", "BTC", 0));
                        usersRootRef.child(username).child("doge").setValue(new PriceEntity("DogeCoin", "DOGE", 0));
                        usersRootRef.child(username).child("eth").setValue(new PriceEntity("Ethereum", "ETH", 0));
                        usersRootRef.child(username).child("ltc").setValue(new PriceEntity("LiteCoin", "LTC", 0));
                        usersRootRef.child(username).child("xpr").setValue(new PriceEntity("Monero", "XPR", 0));
                        Toast.makeText(SignUp.this, "User Created", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(view.getContext(), MainActivity.class);
                        view.getContext().startActivity(intent);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("CWallet", error.toString());
                }
            };
            passwordRef.addValueEventListener(responseListener);
        }
    }
    public void alertUserExist(){
        Toast.makeText(SignUp.this, "User already exist", Toast.LENGTH_SHORT).show();
    }
}
