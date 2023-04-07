package com.example.myapplication;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
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
import com.squareup.picasso.Picasso;

public class PriceChange extends AppCompatActivity {
    DatabaseReference holdingRef;
    DatabaseReference entityRef;
    DatabaseReference balanceRef;
    DatabaseReference imageRef;
    DatabaseReference priceRef;
    public ImageView logo;
    public TextView name;
    public TextView holding;
    public TextView smallName;
    public TextView price;
    PriceEntity myPrice;
    EditText quantity;
    TextView balance;
    TextView deal;

    float priceValue;
    float holdingValue;
    String small_name;
    
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change);
        Intent intent = getIntent();
        String passedName = intent.getStringExtra("name");
        String userName = intent.getStringExtra("user_name");
        logo = findViewById(R.id.entity_logo);
        name = findViewById(R.id.entity_name);
        smallName = findViewById(R.id.entity_small_name);
        price = findViewById(R.id.entity_price);
        holding = findViewById(R.id.entity_holding);
        quantity = findViewById(R.id.quantity);
        deal = findViewById(R.id.deal_price);
        holdingRef = FirebaseDatabase.getInstance().getReference().child("users").child(userName).child(passedName).child("holding");
        entityRef = FirebaseDatabase.getInstance().getReference().child("users").child(userName).child(passedName);
        balanceRef = FirebaseDatabase.getInstance().getReference().child("balance").child(String.valueOf(userName));
        priceRef = FirebaseDatabase.getInstance().getReference().child("price").child(passedName);
        balance = findViewById(R.id.balance);
        priceValue = 0;



        quantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // todo need to add for future
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // todo need to add for future
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void afterTextChanged(Editable s) {
                try{
                    String value = s.toString();
                    float q = Float.parseFloat(value);
                    deal.setText("₹ " + (priceValue * q) + "");
                }
                catch(Exception e){
                    Log.e("CWallet", e.toString());
                }

            }
        });

        try{
            entityRef.addValueEventListener(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    myPrice = dataSnapshot.getValue(PriceEntity.class);
                    holdingValue = myPrice.holding;
                    small_name = myPrice.small_name.toLowerCase();

                    name.setText(myPrice.name);
                    smallName.setText(myPrice.small_name);
                    holding.setText(myPrice.holding + "");


                    Log.i("CWallet", "name " + myPrice.name);
                    Log.i("CWallet", "small_name " + myPrice.small_name);
                    Log.i("CWallet", "holding " + myPrice.holding);
                    imageRef = FirebaseDatabase.getInstance().getReference().child("images").child(myPrice.small_name.toLowerCase());

                    imageRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String url = (String) snapshot.getValue();
                            Log.i("CWallet", url);
                            Picasso.get().load(url).into(logo);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("CWallet", "Error fetching image URL");
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(PriceChange.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
                }
            });
            priceRef.addValueEventListener(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    priceValue = Float.parseFloat(snapshot.getValue()+"");
                    price.setText("₹ " + snapshot.getValue() + "");
                    Log.i("CWallet", "Price of " + small_name + " is " + priceValue);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(PriceChange.this, "Fail to get balance.", Toast.LENGTH_SHORT).show();
                }
            });
            balanceRef.addValueEventListener(new ValueEventListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    balance.setText("₹ " + snapshot.getValue() + "");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(PriceChange.this, "Fail to get balance.", Toast.LENGTH_SHORT).show();
                }
            });
        }
        catch (Exception e){
            Log.i("CWallet", e.toString());
        }
    }
    public void buy(View v){
        if(!String.valueOf(quantity.getText()).equals("")){
            float sell_qty = Float.parseFloat(String.valueOf(quantity.getText()));
            float balance_delta = priceValue * sell_qty;
            balanceRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    }
                    else {

                        String balanceString = String.valueOf(task.getResult().getValue());
                        float balance = Float.parseFloat(balanceString);
                        if(balance_delta <= balance){
                            float newBalance = balance - balance_delta;
                            balanceRef.setValue(newBalance);
                            holdingRef.setValue(myPrice.holding + sell_qty);
                            quantity.setText("");
                            deal.setText("₹ 0");
                        }
                        else{
                            Toast.makeText(PriceChange.this, "Insufficient Balance", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
        else{
            Toast.makeText(PriceChange.this, "PLease enter the quantity", Toast.LENGTH_SHORT).show();
        }
    }
    public void sell(View v){
        if(String.valueOf(quantity.getText()).length() > 8){
            Toast.makeText(PriceChange.this, "Quantity cannot have more than 8 numbers", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!String.valueOf(quantity.getText()).equals("")){
            float quantityEntered = Float.parseFloat(String.valueOf(quantity.getText()));
            if(myPrice.holding == 0){
                Toast.makeText(PriceChange.this, "You don't have quantity to sell", Toast.LENGTH_SHORT).show();
                return;
            }
            if(myPrice.holding < quantityEntered){
                Toast.makeText(PriceChange.this, "You don't have enough quantity to sell", Toast.LENGTH_SHORT).show();
                return;
            }
            int sell_qty = Integer.parseInt(String.valueOf(quantity.getText()));
            float balance_delta = priceValue * sell_qty;
            balanceRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                        Toast.makeText(PriceChange.this, "Error getting data", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String balanceString = String.valueOf(task.getResult().getValue());
                        float balance = Float.parseFloat(balanceString);
                        float newBalance = balance + balance_delta;
                        balanceRef.setValue(newBalance);
                        holdingRef.setValue(myPrice.holding - sell_qty);
                        quantity.setText("");
                        deal.setText("₹ 0");
                    }
                }
            });
        }
        else{
            Toast.makeText(PriceChange.this, "Please enter the quantity", Toast.LENGTH_SHORT).show();
        }
    }
}