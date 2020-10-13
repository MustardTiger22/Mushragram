package com.dominikp.mobileapp.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.dominikp.mobileapp.R;
import com.dominikp.mobileapp.model.Upload;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class UploadActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri mImageUri;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private FirebaseUser mUser;
    private com.dominikp.mobileapp.databinding.ActivityUploadBinding binding;
    private StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = com.dominikp.mobileapp.databinding.ActivityUploadBinding.inflate(getLayoutInflater());

        binding.buttonChooseImage.setOnClickListener(this);
        binding.buttonUpload.setOnClickListener(this);
        binding.showUploads.setOnClickListener(this);

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");
        mUser = FirebaseAuth.getInstance().getCurrentUser();

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
            case R.id.showUploads:
                    openImagesActivity();
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

    private String getFileExtension(Uri uri) {
        ContentResolver resolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(resolver.getType(uri));
    }

    private void uploadFile() {
        if(mImageUri != null) {
            StorageReference fileReference = mStorageRef.child(UUID.randomUUID() + "." + getFileExtension(mImageUri));

            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(task -> {
                        Handler handler = new Handler();
                        handler.postDelayed(() -> binding.progressBar.setProgress(0), 500);

                        Toast.makeText(this, "Zdjęcie zostało wrzucone.", Toast.LENGTH_LONG).show();

                        //Uzyskanie adres URL zdjecia
                        Task<Uri> urlTask = task.getStorage().getDownloadUrl();
                        while (!urlTask.isSuccessful());
                            Uri downloadUrl = urlTask.getResult();

                        Upload upload = new Upload(
                                mUser.getUid(),
                                binding.fileName.getText().toString().trim(),
                                downloadUrl.toString());

                        String uploadId = mDatabaseRef.push().getKey();
                        mDatabaseRef.child(uploadId).setValue(upload);

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            })
                    .addOnProgressListener(task -> {
                        double progress = 100.0 * task.getBytesTransferred() / task.getTotalByteCount();
                        binding.progressBar.setProgress((int) progress);
            });


        } else {
            Toast.makeText(this, "Nie zostało wybrane zdjęcie.", Toast.LENGTH_SHORT).show();
        }


    }

    private void openImagesActivity() {
        startActivity(new Intent(this, ImagesActivity.class));
    }
}