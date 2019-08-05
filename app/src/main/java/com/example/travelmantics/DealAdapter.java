package com.example.travelmantics;

import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;


import java.net.URI;
import java.util.ArrayList;

public class DealAdapter extends RecyclerView.Adapter<DealAdapter.DealHolder>{

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildEventListener;
    private ImageView mDealImageView;
    ArrayList<TravelDeal> mDeals;


    public DealAdapter(){
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;
        mDeals = FirebaseUtil.mDeals;


        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                TravelDeal td = dataSnapshot.getValue(TravelDeal.class);
                td.setId(dataSnapshot.getKey());
                mDeals.add(td);
                notifyItemInserted(mDeals.size()-1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //to test later.
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mDatabaseReference.addChildEventListener(mChildEventListener);
    }

    @NonNull
    @Override
    public DealHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_row, parent, false);
        return new DealHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DealHolder holder, int position) {
        TravelDeal td = mDeals.get(position);
        holder.bind(td);
    }

    @Override
    public int getItemCount() {
        return mDeals.size();
    }

    public class DealHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTitle;
        TextView tvDescription;
        TextView tvPrice;


        public DealHolder(@NonNull final View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvPrice = itemView.findViewById(R.id.tv_price);
            mDealImageView = itemView.findViewById(R.id.img_profile);
            itemView.setOnClickListener(this);
        }

        public void bind(TravelDeal deal){

            tvTitle.setText(deal.getTitle());
            tvDescription.setText(deal.getDescription());
            tvPrice.setText(deal.getPrice());
            showImage(deal.getImageUrl());

        }
        private void showImage(String pictureUrl) {
            if(pictureUrl != null && pictureUrl.isEmpty() == false){
                System.out.println("Hello App");
                int width = Resources.getSystem().getDisplayMetrics().widthPixels;
                Picasso.get()
                        .load(pictureUrl)
                        .resize(180, 180)
                        .centerCrop()
                        .into(mDealImageView);
            }
        }

        @Override
        public void onClick(View view) {
            Log.v("DealActivity", "Click : " + getAdapterPosition());
            int position = getAdapterPosition();
            TravelDeal selectedDeal = mDeals.get(position);
            Intent dealIntent = new Intent(view.getContext(), DealActivity.class);
            dealIntent.putExtra("Deal", selectedDeal);
            view.getContext().startActivity(dealIntent);
        }
    }
}