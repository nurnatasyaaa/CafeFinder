package com.example.cafefinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    EditText etEmail, etPassword, etName, etPhone;
    FirebaseAuth mAuth;
    DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etEmail = findViewById(R.id.editText);
        etPassword = findViewById(R.id.editText2);
        etName = findViewById(R.id.editTextName);
        etPhone = findViewById(R.id.editTextPhone);

        mAuth = FirebaseAuth.getInstance();
    }

    public void createUser(View v) {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        DatabaseReference userRef = FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(userId);

                        // Save user info
                        userRef.child("name").setValue(name);
                        userRef.child("phone").setValue(phone);
                        userRef.child("email").setValue(email);

                        Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show();

                        startActivity(new Intent(this, HomeActivity.class));
                        finish();

                    } else {
                        Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
