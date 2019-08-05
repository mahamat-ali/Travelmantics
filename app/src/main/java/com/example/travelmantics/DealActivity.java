package com.example.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


public class DealActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 40;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;

    EditText titleEditText;
    EditText priceEditText;
    EditText descriptionEditText;
    ImageView mImageView;
    TravelDeal deal;
    Button btnImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);

        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;

        titleEditText = findViewById(R.id.title_et);
        priceEditText = findViewById(R.id.price_et);
        descriptionEditText = findViewById(R.id.description_et);
        mImageView = findViewById(R.id.image);
        btnImage = findViewById(R.id.btnImage);



        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");
//
        if(deal == null){
            deal = new TravelDeal();
            Log.v("InsertActitty", "Insert: " + deal);
        }

        this.deal = deal;

        titleEditText.setText(deal.getTitle());
        priceEditText.setText(deal.getPrice());
        descriptionEditText.setText(deal.getDescription());
        showImage(deal.getImageUrl());

        Button mButtonImage = findViewById(R.id.btnImage);
        mButtonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent, "Insert Picture"), REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){

            Uri imageUri = data.getData();
            StorageReference riversRef = FirebaseUtil.mStorageReference.child(imageUri.getLastPathSegment());
            riversRef.putFile(imageUri)
                    .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> url = taskSnapshot.getStorage().getDownloadUrl();
                    while (!url.isSuccessful());
                    String pictureUrl = url.getResult().toString();
                    String imageName = taskSnapshot.getStorage().getPath();
                    deal.setImageUrl(pictureUrl);
                    deal.setImageName(imageName);
                    Log.d("Url: ", pictureUrl);
                    Log.d("Name", imageName);
                    showImage(pictureUrl);
                }
            });

    }

    }

    private void showImage(String pictureUrl) {
        if(pictureUrl != null && pictureUrl.isEmpty() == false){
            System.out.println("Hello App");
            int width = Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.get()
                    .load(pictureUrl)
                    .resize(width, width*2/3)
                    .centerCrop()
                    .into(mImageView);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);

        if (FirebaseUtil.isAdmin == true) {
            menu.findItem(R.id.save_menu).setVisible(true);
            menu.findItem(R.id.delete).setVisible(true);
            titleEditText.setEnabled(true);
            priceEditText.setEnabled(true);
            descriptionEditText.setEnabled(true);
            btnImage.setEnabled(true);

        }else {
            menu.findItem(R.id.save_menu).setVisible(false);
            menu.findItem(R.id.delete).setVisible(false);
            titleEditText.setEnabled(false);
            priceEditText.setEnabled(false);
            descriptionEditText.setEnabled(false);
            btnImage.setEnabled(false);
        }
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.save_menu){
            saveDeal();
            Toast.makeText(this, "Deal saved", Toast.LENGTH_LONG).show();
            clean();
            getBack();
            return true;
        }else if(item.getItemId() == R.id.delete){
            deleteDeal();
            getBack();
            return true;
        }else{
            return super.onOptionsItemSelected(item);
        }
    }

    private void deleteDeal() {
        if(deal == null){
            Toast.makeText(this, "Create before deleting it.", Toast.LENGTH_LONG).show();
            return;
        }
        mDatabaseReference.child(deal.getId()).removeValue();
        if(deal.getImageName() != null && deal.getImageName().isEmpty() == false) {
            StorageReference picRef = FirebaseUtil.mFirebaseStorage.getReference().child(deal.getImageName());
            picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d("Delete Image", "Image Successfully Deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Delete Image", e.getMessage());
                }
            });
        }

    }

    private void saveDeal(){
        deal.setTitle(titleEditText.getText().toString());
        deal.setPrice(priceEditText.getText().toString());
        deal.setDescription(descriptionEditText.getText().toString());

        Log.v("DealActivity", "Saved: " + deal.getImageUrl());
        if(deal.getId() == null){
            mDatabaseReference.push().setValue(deal);
        }else{
            mDatabaseReference.child(deal.getId()).setValue(deal);
        }


    }



    public void getBack(){
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    private void clean(){
        titleEditText.setText("");
        priceEditText.setText("");
        descriptionEditText.setText("");
        titleEditText.requestFocus();
    }
}
