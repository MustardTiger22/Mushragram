package com.dominikp.mobileapp.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import com.dominikp.mobileapp.R;
import com.dominikp.mobileapp.model.Upload;
import com.dominikp.mobileapp.databinding.ActivityUploadBinding;
import com.dominikp.mobileapp.model.UploadLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import lombok.SneakyThrows;

public class UploadActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private FirebaseUser mUser;
    private ActivityUploadBinding binding;
    private StorageTask mUploadTask;

    // Lokalizacja
    private static final int PERMISSION_ID = 44;
    private FusedLocationProviderClient mFusedLocationClient;
    private Double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityUploadBinding.inflate(getLayoutInflater());

        binding.buttonChooseImage.setOnClickListener(this);
        binding.buttonUpload.setOnClickListener(this);
        binding.buttonGetLocalization.setOnClickListener(this);
        binding.localizationLink.setOnClickListener(this);

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        bottomActionBarHandler();

        setContentView(binding.getRoot());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonUpload:
                if(mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(this, "Wrzucanie w trakcie...", Toast.LENGTH_SHORT).show();
                } else {
                    uploadFile();
                }
                break;
            case R.id.buttonChooseImage:
                    openFileChooser();
                break;
            case R.id.buttonGetLocalization:
                    getLastLocation();
                break;
            case R.id.localizationLink:
                    openLocalizationIntent();
                break;
        }
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();

            Picasso.get()
                    .load(mImageUri)
                    .into(binding.imageView);
        }
    }


    private void uploadFile() {
        if(mImageUri != null) {
            StorageReference fileReference = mStorageRef.child(UUID.randomUUID() + "." + getFileExtension(mImageUri));

            // Wrzucenie zdjecia do Firebase Storage
            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(task -> {
                        Handler handler = new Handler();
                        handler.postDelayed(() -> binding.progressBar.setProgress(0), 500);

                        Toast.makeText(this, "Zdjęcie zostało wrzucone.", Toast.LENGTH_LONG).show();

                        //Uzyskanie adres URL zdjecia
                        Task<Uri> urlTask = task.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful());
                            Uri downloadUrl = urlTask.getResult();

                        //Użytkownik automatycznie lubi swoje zdjęcie
                        HashMap<String, Boolean> likes = new HashMap<>();
                        likes.put(mUser.getUid(), true);

                        Upload upload = new Upload(
                                mUser.getUid(),
                                binding.fileName.getText().toString().trim(),
                                mUser.getDisplayName(),
                                downloadUrl.toString(),
                                1,
                                likes);


                        // Wrzucenie do bazy danych wpisu zdjęcia (kto wrzucił itp)
                        String uploadId = mDatabaseRef.push().getKey();
                        mDatabaseRef.child(uploadId).setValue(upload);
                        saveUploadLocation(uploadId);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            })
                    .addOnProgressListener(task -> {
                        //Aktualizacja progress bara
                        double progress = 100.0 * task.getBytesTransferred() / task.getTotalByteCount();
                        binding.progressBar.setProgress((int) progress);
            });
        } else {
            Toast.makeText(this, "Nie zostało wybrane zdjęcie.", Toast.LENGTH_SHORT).show();
        }
    }

    @SneakyThrows
    private void saveUploadLocation(String uploadId){
        if(latitude != null && longitude != null) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            String cityName = addresses.get(0).getLocality();
            String countryName = addresses.get(0).getCountryName();
            UploadLocation location = UploadLocation
                    .builder()
                    .latitude(latitude)
                    .longitude(longitude)
                    .city(cityName)
                    .country(countryName)
                    .build();
            mDatabaseRef.child(uploadId).child("location").setValue(location);
        }
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
        }
    };

    private boolean checkPermissions()
    {
        return ActivityCompat
                .checkSelfPermission(
                        this,
                        Manifest.permission
                                .ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED

                && ActivityCompat
                .checkSelfPermission(
                        this,
                        Manifest.permission
                                .ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }


    private void requestPermissions()
    {
        ActivityCompat.requestPermissions(this, new String[] {
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION },
                PERMISSION_ID);
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient
                = LocationServices
                .getFusedLocationProviderClient(this);

        mFusedLocationClient.
                requestLocationUpdates(
                        mLocationRequest,
                        mLocationCallback,
                        Looper.myLooper());
    }


    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        // Sprawdzenie, czy są nadane uprawnienia do lokalizacji
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        task -> {
                            Location location = task.getResult();
                            if (location == null) {
                                requestNewLocationData();
                            }
                            else {
                                binding.localizationLink.setVisibility(View.VISIBLE);
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        });
            }

            else {
                Toast.makeText(this, "Proszę włączyć lokalizacje...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }
        else {
            requestPermissions();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(
                        requestCode,
                        permissions,
                        grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0
                    && grantResults[0]
                    == PackageManager
                    .PERMISSION_GRANTED) {

                getLastLocation();
            }
        }
    }


    private void openLocalizationIntent() {
        if(latitude != null && longitude != null) {
            String urlGoogleMap = "https://www.google.com/maps/search/?api=1&query=%s,%s";
            urlGoogleMap = String.format(urlGoogleMap, latitude, longitude);

            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlGoogleMap)));
        }
    }

    // Obsługa dolnej nawigacji
    private void bottomActionBarHandler() {
        binding.bottomNavigation.setSelectedItemId(R.id.menuUpload);
        binding.bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.menuHome:
                    startActivity(new Intent(this, ImagesActivity.class));
                    break;
                case R.id.menuOverview:
                    startActivity(new Intent(this, OverviewActivity.class));
                    break;
                case R.id.menuUpload: break;
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

    private String getFileExtension(Uri uri) {
        ContentResolver resolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(resolver.getType(uri));
    }

}