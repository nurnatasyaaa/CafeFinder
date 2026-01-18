package com.example.cafefinder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText e1, e2;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        e1 = findViewById(R.id.editText);
        e2 = findViewById(R.id.editText2);

        mAuth = FirebaseAuth.getInstance();
    }

    public void loginUser(View v){

        String email = e1.getText().toString().trim();
        String password = e2.getText().toString().trim();

        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(this,"Fields cannot be empty",Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){

                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    }else{
                        Toast.makeText(this,"Login failed",Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
