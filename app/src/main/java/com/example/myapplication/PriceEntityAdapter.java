package com.example.myapplication;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PriceEntityAdapter extends RecyclerView.Adapter<PriceEntityAdapter.ViewHolder> {
    PriceEntity[] prices;
    String userName;
    DatabaseReference imageRef, priceRef;

    PriceEntityAdapter(PriceEntity[] prices){
        this.prices = prices;
        Log.e("CWallet", "Inside adapter");
    }

    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.fragment_price_card, parent, false);
        return new ViewHolder(listItem);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.e("CWallet", "Inside bind view holder");
        final PriceEntity myprice = prices[position];
        imageRef = FirebaseDatabase.getInstance().getReference().child("images").child(myprice.small_name.toLowerCase());
        priceRef = FirebaseDatabase.getInstance().getReference().child("price").child(myprice.small_name.toLowerCase());
        holder.name.setText(myprice.name);
        holder.smallName.setText(myprice.small_name);
        holder.holding.setText(myprice.holding + "");

        priceRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String priceString = snapshot.getValue()+"";
                holder.price.setText("₹ " + priceString);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CWallet", "Error fetching price");
            }
        });



        imageRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String url = (String) snapshot.getValue();
                Log.i("CWallet", url);
                Picasso.get().load(url).into(holder.logo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CWallet", "Error fetching image URL");
            }
        });

        holder.gridLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("CWallet", "Inside click of " + myprice.name);
                try{
                    Intent intent = new Intent(view.getContext(), PriceChange.class);
                    intent.putExtra("name", myprice.small_name.toLowerCase());
                    intent.putExtra("user_name", userName);
                    view.getContext().startActivity(intent);
                }
                catch(Exception ex){
                    Log.e("CWallet", ex.toString());
                }
            }
        });
    }

    public int getItemCount() {
        return prices.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView logo;
        public TextView name;
        public TextView smallName;
        public TextView holding;
        public TextView price;
        public GridLayout gridLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            this.logo = (ImageView) itemView.findViewById(R.id.entity_logo);
            this.name = (TextView) itemView.findViewById(R.id.entity_name);
            this.smallName = (TextView) itemView.findViewById(R.id.entity_small_name);
            this.price = (TextView) itemView.findViewById(R.id.entity_price);
            this.gridLayout = (GridLayout) itemView.findViewById(R.id.entity);
            this.holding = (TextView) itemView.findViewById(R.id.entity_holding);
        }
    }
}

