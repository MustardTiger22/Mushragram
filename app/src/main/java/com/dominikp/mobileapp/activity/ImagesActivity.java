package com.dominikp.mobileapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.dominikp.mobileapp.R;
import com.dominikp.mobileapp.adapter.ImageAdapter;
import com.dominikp.mobileapp.model.Upload;
import com.dominikp.mobileapp.databinding.ActivityImagesBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ImagesActivity extends AppCompatActivity implements ImageAdapter.OnItemClickListener {
    private ActivityImagesBinding binding;
    private ImageAdapter mAdapter;
    private DatabaseReference mDatabaseRef;
    private FirebaseStorage mStorage;
    private FirebaseUser mUser;
    private ValueEventListener mDBListener;
    private List<Upload> mUploads;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityImagesBinding.inflate(getLayoutInflater());

        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mUploads = new ArrayList<>();

        mAdapter = new ImageAdapter(ImagesActivity.this, mUploads);

        binding.recyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(ImagesActivity.this);

        mStorage = FirebaseStorage.getInstance();

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        mDBListener = mDatabaseRef.orderByChild("createdAt").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //W celu zapobiegnięcia zdublikowania danych podczas zmian tablica zostaje wyczyszczona
                mUploads.clear();

                for(DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    upload.setKey(postSnapshot.getKey());
                    if(upload.getLikes() == null) {
                        upload.setLikes(new HashMap<>());

                    }
                    mUploads.add(upload);
                }

                //Firebase sortuje malejąco, zatem trzeba odwrócić tablice, aby posty były od najnowszych
                Collections.reverse(mUploads);
                //Zaktualizuj adapter jezeli wystapily zmiany
                mAdapter.notifyDataSetChanged();

                binding.progressCircle.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ImagesActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                binding.progressCircle.setVisibility(View.INVISIBLE);
            }
        });

        bottomActionBarHandler();

        setContentView(binding.getRoot());
    }



    @Override
    public void onItemClick(int position) {
        Upload selectedItem = mUploads.get(position);
        String selectedKey = selectedItem.getKey();

        Intent intent = new Intent(this, OneImageActivity.class);
        intent.putExtra("itemKey", selectedKey);
        startActivity(intent);
    }

    @Override
    public void onLikeClick(int position) {
        Upload selectedItem = mUploads.get(position);
        String selectedKey = selectedItem.getKey();

        // Dodawanie/usuwanie polubienia
        if(!selectedItem.getLikes().containsKey(mUser.getUid())) {
            mDatabaseRef
                    .child(selectedKey)
                    .child("likes")
                    .child(mUser.getUid())
                    .setValue(true).addOnSuccessListener(aVoid -> {
                        selectedItem.getLikes().put(mUser.getUid(), true);
                        selectedItem.incrementLikeCounter();
                updateLikeCounter(selectedItem, selectedKey);
            });
        } else {
            mDatabaseRef
                    .child(selectedKey)
                    .child("likes")
                    .child(mUser.getUid())
                    .removeValue()
                    .addOnSuccessListener(aVoid -> {
                        selectedItem.getLikes().remove(mUser.getUid());
                        selectedItem.decrementLikeCounter();
                        updateLikeCounter(selectedItem, selectedKey);
                    });
        }

    }

    private void updateLikeCounter(Upload selectedItem, String selectedKey) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("likeCounter", selectedItem.getLikeCounter());
        mDatabaseRef.child(selectedKey).updateChildren(result);
    }

    @Override
    public void onPlusClick(int position) {
        onLikeClick(position);
    }

    @Override
    public void onDeleteClick(int position) {
        Upload selectedItem = mUploads.get(position);
        String selectedKey = selectedItem.getKey();

        StorageReference imageRef = mStorage.getReferenceFromUrl(selectedItem.getImageUrl());
        imageRef.delete().addOnSuccessListener(aVoid -> {
            mDatabaseRef.child(selectedKey).removeValue();
            Toast.makeText(this, "Grzyb usunięty", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabaseRef.removeEventListener(mDBListener);
    }

    // Obsługa dolnej nawigacji
    private void bottomActionBarHandler() {
        binding.bottomNavigation.setSelectedItemId(R.id.menuHome);
        binding.bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menuHome: break;
                case R.id.menuOverview:
                    startActivity(new Intent(this, OverviewActivity.class));
                    break;
                case R.id.menuUpload:
                    startActivity(new Intent(this, UploadActivity.class));
                    break;
                case R.id.menuLogout:
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
            }
            return true;
        });
    }
}