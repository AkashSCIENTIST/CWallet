package com.example.myapplication;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;

public class UserActivity extends AppCompatActivity {
    DatabaseReference ref;
    DatabaseReference balanceRef;
    FirebaseFirestore db;
    TextView balance;
    TextView userNameText;
    String userName;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Intent intent = getIntent();
        userName = intent.getStringExtra("name");
        ref = FirebaseDatabase.getInstance().getReference().child("users").child(String.valueOf(userName));
        balanceRef = FirebaseDatabase.getInstance().getReference().child("balance").child(String.valueOf(userName));
        Log.e("CWallet", "Inside");
        balance = findViewById(R.id.balance);
        userNameText = findViewById(R.id.user_name);

        userNameText.setText(userName);


        try{
            balanceRef.addValueEventListener(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.i("CWallet", snapshot.getValue() + "");
                    balance.setText("₹ " + snapshot.getValue() + "");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UserActivity.this, "Fail to get balance.", Toast.LENGTH_SHORT).show();
                }
            });
        }
        catch(Exception e){
            Log.e("CWallet", e.toString());
        }

        try{
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ArrayList<PriceEntity> temp = new ArrayList<PriceEntity>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        PriceEntity price = snapshot.getValue(PriceEntity.class);
                        assert price != null;
                        Log.i("CWallet", "name " + price.name);
                        Log.i("CWallet", "small_name " + price.small_name);
                        Log.i("CWallet", "holding " + price.holding);
                        temp.add(price);
                    }
                    render(temp);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(UserActivity.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
        catch (Exception e){
            Log.i("CWallet", e.toString());
        }
    }

    public void render(ArrayList<PriceEntity> pricesList){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.entities_recycle_view);
        PriceEntity[] prices = pricesList.toArray(new PriceEntity[0]);
        PriceEntityAdapter adapter = new PriceEntityAdapter(prices);
        adapter.userName = userName;
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    public void qrPage(View view) {
        try {
            Intent intent = new Intent(UserActivity.this, GenerateQR.class);
            intent.putExtra("user", userName);
            UserActivity.this.startActivity(intent);
        }
        catch (Exception e) {
            Log.e("CWallet", e.toString());
        }
    }

    public void payPage(View view) {
        try {
            Intent intent = new Intent(UserActivity.this, Pay.class);
            intent.putExtra("user", userName);
            UserActivity.this.startActivity(intent);
        }
        catch (Exception e) {
            Log.e("CWallet", e.toString());
        }
    }
}