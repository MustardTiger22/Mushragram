package com.dominikp.mobileapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.dominikp.mobileapp.databinding.ActivityMainBinding;
import com.dominikp.mobileapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // Sprawdzenie, czy użytkownik jest zalogowany
        if(mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(this, ImagesActivity.class);
            //Jeżeli użytkownik sie zaloguje nie będzie mogł wrócić do panelu logowania
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());

        binding.buttonSignin.setOnClickListener(this);
        binding.buttonSingup.setOnClickListener(this);

        setContentView(binding.getRoot());
    }


    // Logowanie
    public void signIn() {
        String email = binding.email.getText().toString().trim();
        String password = binding.password.getText().toString().trim();

        if(email.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, "Prosze wypełnić dane logowania.", Toast.LENGTH_SHORT).show();
        } else {
            binding.progressBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        binding.progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(this, ImagesActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else {
                            Toast.makeText(MainActivity.this, "Niepoprawne dane.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
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
