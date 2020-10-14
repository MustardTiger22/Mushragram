package com.dominikp.mobileapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.dominikp.mobileapp.R;
import com.dominikp.mobileapp.databinding.ActivityOneImageBinding;
import com.dominikp.mobileapp.model.Upload;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class OneImageActivity extends AppCompatActivity {
    private ActivityOneImageBinding binding;
    private DatabaseReference databaseReference;
    private Upload upload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityOneImageBinding.inflate(getLayoutInflater());

        String itemKey = getIntent().getExtras().getString("itemKey");

        databaseReference = FirebaseDatabase
                .getInstance()
                .getReference("uploads")
                .child(itemKey);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                upload = snapshot.getValue(Upload.class);
                if(upload.getLikes() == null) {
                    upload.setLikes(new HashMap<>());

                }

                binding.authorTextViewOneImage.setText(upload.getAuthor());
                binding.imageName.setText(upload.getTitle());
                binding.likeCounterTextViewOneImage.setText(String.valueOf(upload.getLikes().size()));

                Picasso.get()
                        .load(upload.getImageUrl())
                        .placeholder(R.mipmap.ic_launcher)
                        .fit()
                        .centerCrop()
                        .into(binding.imageViewOneImage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OneImageActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });



        setContentView(binding.getRoot());
    }
}