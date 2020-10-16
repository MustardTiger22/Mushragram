package com.dominikp.mobileapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.dominikp.mobileapp.R;
import com.dominikp.mobileapp.adapter.PlaceOverviewAdapter;
import com.dominikp.mobileapp.databinding.ActivityOverviewBinding;
import com.dominikp.mobileapp.model.PlaceOverview;
import com.dominikp.mobileapp.model.Upload;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OverviewActivity extends AppCompatActivity implements PlaceOverviewAdapter.OnItemClickListener{
    private ActivityOverviewBinding binding;
    private DatabaseReference mUpdatesRef;
    private DatabaseReference mMyUpdatesRef;
    private FirebaseUser mUser;
    private PlaceOverviewAdapter mAdapter;
    private List<PlaceOverview> mPlaces;
    private List<Upload> mUploads;
    private ValueEventListener mMyUpdatesListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityOverviewBinding.inflate(getLayoutInflater());

        mPlaces = new ArrayList<>();
        mUploads = new ArrayList<>();

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        mAdapter = new PlaceOverviewAdapter(this, mPlaces);
        mAdapter.setOnItemClickListener(OverviewActivity.this);

        binding.placesRecyclerView.setHasFixedSize(true);
        binding.placesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.placesRecyclerView.setAdapter(mAdapter);

        mUpdatesRef = FirebaseDatabase
                .getInstance()
                .getReference("uploads");

        mMyUpdatesRef = FirebaseDatabase
                .getInstance()
                .getReference("uploads");


        //Załadowanie z bazy postów użytkownika
        mMyUpdatesListener = mMyUpdatesRef.orderByChild("userId").equalTo(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUploads.clear();

                for(DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);
                    upload.setKey(postSnapshot.getKey());
                    if(mUploads == null) {
                        mUploads = new ArrayList<>();
                    }

                    mUploads.add(upload);
                }

                //Zsumowanie polubien postów użytkownika
                int likeCounter = mUploads.stream().reduce(0, (sum , upload) ->
                        sum + upload.getLikeCounter(), Integer::sum);
                binding.likesCounterTextView.setText(String.valueOf(likeCounter));
                binding.postCounterTextView.setText(String.valueOf(mUploads.size()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OverviewActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //Załadowanie z bazy trzech najbardziej lubionych postów
         mUpdatesRef.orderByChild("likeCounter").limitToLast(3).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //W celu zapobiegnięcia zdublikowania danych podczas zmian tablica zostaje wyczyszczona
                mPlaces.clear();

                for(DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Upload upload = postSnapshot.getValue(Upload.class);

                    PlaceOverview place = PlaceOverview
                            .builder()
                            .displayName(upload.getAuthor())
                            .uploadId(postSnapshot.getKey())
                            .imageUrl(upload.getImageUrl())
                            .build();

                    if(mPlaces == null) {
                        mPlaces = new ArrayList<>();
                    }
                    mPlaces.add(place);
                }

                Collections.reverse(mPlaces);

                //Zaktualizuj adapter jezeli wystapily zmiany
                mAdapter.notifyDataSetChanged();

                binding.progressCircle.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OverviewActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                binding.progressCircle.setVisibility(View.INVISIBLE);
            }
        });


        bottomActionBarHandler();
        setContentView(binding.getRoot());
    }

    // Obsługa dolnej nawigacji
    private void bottomActionBarHandler() {
        binding.bottomNavigation.setSelectedItemId(R.id.menuOverview);
        binding.bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menuHome:
                    startActivity(new Intent(this, ImagesActivity.class));
                    break;
                case R.id.menuOverview:
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
                    break;
            }
            return true;
        });
    }

    @Override
    public void onItemClick(int position) {
        PlaceOverview selectedItem = mPlaces.get(position);

        Intent intent = new Intent(this, OneImageActivity.class);
        intent.putExtra("itemKey", selectedItem.getUploadId());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUpdatesRef.removeEventListener(mMyUpdatesListener);
    }
}
