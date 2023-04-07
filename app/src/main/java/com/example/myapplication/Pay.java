package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;

public class Pay extends AppCompatActivity {
    EditText receiver, amount;
    TextView balanceText;
    String senderName, receiverName;
    DatabaseReference receiverRef, balanceRef, balanceRootRef;
    float balance;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        receiver = findViewById(R.id.receiver);
        amount = findViewById(R.id.amount);
        balanceText = findViewById(R.id.balance_text);
        Intent intent = getIntent();
        senderName = intent.getStringExtra("user");
        balanceRef = FirebaseDatabase.getInstance().getReference().child("balance").child(String.valueOf(senderName));
        balanceRootRef = FirebaseDatabase.getInstance().getReference().child("balance");

        balanceRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                balance = Float.parseFloat(snapshot.getValue() + "");
                balanceText.setText("₹ " + snapshot.getValue() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Pay.this, "Fail to get balance.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void scan(View view) {
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setPrompt("Scan a barcode or QR Code");
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                Log.e("CWallet", intentResult.getContents());
                receiver.setText(intentResult.getContents());

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void pay(View view) {
        Log.i("CWallet", "Inside pay");
        receiverName = String.valueOf(receiver.getText()).trim();
        if(senderName.equals(receiverName)){
            Toast.makeText(Pay.this, "Sender and Receiver cannot be same", Toast.LENGTH_SHORT).show();
        }
        receiverRef = FirebaseDatabase.getInstance().getReference().child("balance").child(receiverName);
        ValueEventListener responseListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Do stuff
                    Log.i("CWallet", "User Exists");
                    String enteredAmount = String.valueOf(amount.getText()).trim();
                    if (enteredAmount.equals("")){
                        return;
                    }
                    float amountToBeTransfer = Float.parseFloat(enteredAmount);
                    receiver.setText("");
                    amount.setText("");
                    transfer(balanceRef, receiverRef, amountToBeTransfer);
                } else {
                    // Do stuff
                    Toast.makeText(Pay.this, "User " + receiverName + " doesn't exists", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.i("CWallet", databaseError.toString());
            }
        };
        receiverRef.addValueEventListener(responseListener);
    }

    public void transfer(DatabaseReference sender, DatabaseReference receiver, float amount){
        if(amount > balance){
            Toast.makeText(Pay.this, "Insufficient Balance", Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            sender.setValue(balance - amount);
        }
        receiver.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("CWallet", "Error getting receiver balance", task.getException());
                    sender.setValue(balance);
                }
                else {

                    String balanceString = String.valueOf(task.getResult().getValue());
                    float receiverBalance = Float.parseFloat(balanceString);
                    receiver.setValue(amount + receiverBalance);
                    paymentSuccess();
                }
            }
        });
    }
    public void paymentSuccess(){
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.payment_success_clip);
        LayoutInflater li = getLayoutInflater();
        View layout = li.inflate(R.layout.activity_success_toast,(ViewGroup) findViewById(R.id.payment_success_toast));
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setText("Payment Success");
        toast.setGravity(Gravity.FILL, 0, 0);
        toast.setView(layout);//setting the view of custom toast layout
        mediaPlayer.start();
        toast.show();
    }
}
