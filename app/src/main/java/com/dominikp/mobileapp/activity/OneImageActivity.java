package com.dominikp.mobileapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.dominikp.mobileapp.R;
import com.dominikp.mobileapp.adapter.CommentAdapter;
import com.dominikp.mobileapp.databinding.ActivityOneImageBinding;
import com.dominikp.mobileapp.model.Comment;
import com.dominikp.mobileapp.model.Upload;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class OneImageActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityOneImageBinding binding;
    private FirebaseUser mUser;
    private DatabaseReference mUpdateRef;
    private DatabaseReference mCommentsRef;
    private ValueEventListener mCommentsListener;
    private CommentAdapter mAdapter;
    private Upload mUpload;
    private List<Comment> mComments;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityOneImageBinding.inflate(getLayoutInflater());

        mComments = new ArrayList<>();

        String itemKey = getIntent().getExtras().getString("itemKey");

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        binding.buttonSendCommentOneImage.setOnClickListener(this);

        mAdapter = new CommentAdapter(this, mComments);

        binding.recyclerViewOneImage.setHasFixedSize(true);
        binding.recyclerViewOneImage.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewOneImage.setAdapter(mAdapter);

        mUpdateRef = FirebaseDatabase
                .getInstance()
                .getReference("uploads")
                .child(itemKey);

        mCommentsRef = FirebaseDatabase
                .getInstance()
                .getReference("uploads")
                .child(itemKey)
                .child("comments");

        mUpdateRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUpload = snapshot.getValue(Upload.class);
                if(mUpload.getLikes() == null) {
                    mUpload.setLikes(new HashMap<>());

                }

                binding.authorTextViewOneImage.setText(mUpload.getAuthor());
                binding.imageName.setText(mUpload.getTitle());
                binding.likeCounterTextViewOneImage.setText(String.valueOf(mUpload.getLikes().size()));

                Picasso.get()
                        .load(mUpload.getImageUrl())
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

        mCommentsListener = mCommentsRef.orderByChild("createdAt").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mComments.clear();

                for(DataSnapshot postSnapshot : snapshot.getChildren()) {
                    Comment comment = postSnapshot.getValue(Comment.class);
                    comment.setKey(postSnapshot.getKey());
                    if(mComments == null) {
                        mComments = new ArrayList<>();
                    }
                    mComments.add(comment);
                }

                Collections.reverse(mComments);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        setContentView(binding.getRoot());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonSendCommentOneImage:
                writeComment();
            break;
        }
    }

    private void writeComment() {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String createdAt = formatter.format(LocalDateTime.now());

        String text = binding.commentEditTextOneImage.getText().toString().trim();

        if(text.length() < 3) {
            Toast.makeText(this, "Komentarz musi posiadać więcej niż 3 znaki.", Toast.LENGTH_SHORT).show();
        } else {
            Comment newComment = Comment
                    .builder()
                    .author(mUser.getDisplayName())
                    .text(text)
                    .userId(mUser.getUid())
                    .createdAt(createdAt)
                    .build();

            String key = UUID.randomUUID().toString();
            mCommentsRef.child(key).setValue(newComment)
                    .addOnSuccessListener(aVoid -> {
                        binding.commentEditTextOneImage.setText("");
                        binding.commentEditTextOneImage.onEditorAction(EditorInfo.IME_ACTION_DONE);
                        Toast.makeText(this, "Dodano komentarz.", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCommentsRef.removeEventListener(mCommentsListener);
    }
}