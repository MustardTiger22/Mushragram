package com.dominikp.mobileapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.dominikp.mobileapp.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        binding.buttonSignin.setOnClickListener(this);
        binding.buttonSingup.setOnClickListener(this);

        setContentView(binding.getRoot());
    }


    public void signIn() {
        String email = binding.email.getText().toString().trim();
        String password = binding.password.getText().toString().trim();
        binding.progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(this, ProfileActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else {
                        Toast.makeText(MainActivity.this, "Niepoprawne dane.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonSignin:
                signIn();
                break;
            case R.id.buttonSingup :
                startActivity(new Intent(this, SignUpActivity.class));
                break;
        }
    }


}
