package com.dominikp.mobileapp.activity;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.dominikp.mobileapp.R;
import com.dominikp.mobileapp.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.ConfirmPassword;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;

import java.time.LocalDateTime;
import java.util.List;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener, Validator.ValidationListener {
    private FirebaseAuth mAuth;
    private Validator validator;
    private ProgressBar progressBar;
    private DatabaseReference mDatabaseRef;

    @NotEmpty(message = "Nazwa użytkownika nie może być pusta.")
//    @Pattern(regex = "/^[a-zA-Z0-9]+([_ -]?[a-zA-Z0-9])*$", message = "Nieprawidłowa nazwa")
    private EditText displayName;
    @NotEmpty(message = "Adres e-mail nie może być pusty.")
    @Email(message = "Nieprawidłowy adres e-mail.")
    private EditText email;
    @Password(min = 6, scheme = Password.Scheme.ALPHA_NUMERIC_MIXED_CASE_SYMBOLS, message = "Hasło za słabe.")
    private EditText password;
    @ConfirmPassword(message = "Hasła się nie zgadzają.")
    private EditText confirmPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        progressBar = findViewById(R.id.progressBar);
        displayName = findViewById(R.id.displayName);

        validator = new Validator(this);
        validator.setValidationListener(this);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("users");

        findViewById(R.id.buttonRegister).setOnClickListener(this);
        findViewById(R.id.buttonBack).setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonRegister:
                validator.validate();
                break;
            case R.id.buttonBack:
                startActivity(new Intent(this, MainActivity.class));
                break;
        }
    }

    private void registerUser() {
        String email = this.email.getText().toString().trim();
        String password = this.password.getText().toString().trim();

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(displayName.getText().toString().trim())
                                .build();

                        user.updateProfile(profileUpdates);

                        // Zapisanie użytkownika do bazy
                        User userDb = User.builder()
                                .email(email)
                                .displayName(displayName.getText().toString().trim())
                                .createdAt(LocalDateTime.now().toString())
                                .build();
                        mDatabaseRef.child(user.getUid()).setValue(userDb);
                        // Przeniesienie do głownej aktywnosci
                        startActivity(new Intent(this, MainActivity.class));
                    } else {
                        if(task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(SignUpActivity.this, "Podany adres e-mail jest zajęty.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    @Override
    public void onValidationSucceeded() {
        registerUser();
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }
    }
}