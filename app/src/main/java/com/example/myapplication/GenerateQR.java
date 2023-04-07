package com.example.myapplication;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class GenerateQR extends AppCompatActivity {
    ImageView image;
    TextView userNameText, balanceText;
    DatabaseReference balanceRef;
    String username;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        image = findViewById(R.id.photo);
        userNameText = findViewById(R.id.user_name);
        balanceText = findViewById(R.id.qrPageBalance);

        Intent intent = getIntent();
        username = intent.getStringExtra("user");

        balanceRef = FirebaseDatabase.getInstance().getReference().child("balance").child(String.valueOf(username));
        balanceRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i("CWallet", snapshot.getValue() + "");
                balanceText.setText("₹ " + snapshot.getValue() + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GenerateQR.this, "Fail to get balance.", Toast.LENGTH_SHORT).show();
            }
        });

        Bitmap bitmap = null;
        try {
            bitmap = encodeAsBitmap(username);
            image.setImageBitmap(bitmap);
            userNameText.setText(username);

        } catch (WriterException e) {
            Log.e("CWallet", e.toString());
            throw new RuntimeException(e);
        }
    }
    Bitmap encodeAsBitmap(String str) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(str, BarcodeFormat.QR_CODE, 400, 400);

        int w = bitMatrix.getWidth();
        int h = bitMatrix.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                pixels[y * w + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
        return bitmap;
    }
}
