package com.lab6.iot;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPass = findViewById(R.id.etPass);
        EditText etPass2 = findViewById(R.id.etPass2);

        findViewById(R.id.btnRegistrar).setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPass.getText().toString();
            String pass2 = etPass2.getText().toString();

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Correo no válido");
                return;
            }
            if (pass.length() < 6) {
                etPass.setError("Mínimo 6 caracteres");
                return;
            }
            if (!pass.equals(pass2)) {
                etPass2.setError("Las contraseñas no coinciden");
                return;
            }
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                    .addOnSuccessListener(r -> {
                        Toast.makeText(this, "Cuenta creada", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(this, HomeActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "No se pudo registrar: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show());
        });
    }
}
