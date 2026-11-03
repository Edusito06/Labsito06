package com.lab6.iot;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText etEmail, etPass;
    private CallbackManager callbackManager;
    private GoogleSignInClient googleClient;

    private final ActivityResultLauncher<Intent> googleLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    AuthCredential cred = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                    firebaseLogin(cred);
                } catch (ApiException e) {
                    Toast.makeText(this, "Error con Google: " + e.getStatusCode(), Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPass);

        // --- Correo y contraseña ---
        findViewById(R.id.btnLogin).setOnClickListener(v -> loginCorreo());

        // --- Google ---
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(getResources().getIdentifier(
                        "default_web_client_id", "string", getPackageName())))
                .requestEmail()
                .build();
        googleClient = GoogleSignIn.getClient(this, gso);
        findViewById(R.id.btnGoogle).setOnClickListener(v -> {
            // signOut para que siempre muestre el selector de cuenta
            googleClient.signOut().addOnCompleteListener(t ->
                    googleLauncher.launch(googleClient.getSignInIntent()));
        });

        // --- Facebook ---
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AuthCredential cred = FacebookAuthProvider.getCredential(
                        loginResult.getAccessToken().getToken());
                firebaseLogin(cred);
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Inicio con Facebook cancelado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginActivity.this, "Error con Facebook: " + error.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
        findViewById(R.id.btnFacebook).setOnClickListener(v ->
                LoginManager.getInstance().logInWithReadPermissions(
                        this, callbackManager, Arrays.asList("email", "public_profile")));

        findViewById(R.id.btnIrRegistro).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        findViewById(R.id.btnIrReset).setOnClickListener(v ->
                startActivity(new Intent(this, ResetActivity.class)));
    }

    private void loginCorreo() {
        String email = etEmail.getText().toString().trim();
        String pass = etPass.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Correo no válido");
            return;
        }
        if (TextUtils.isEmpty(pass)) {
            etPass.setError("Ingresa tu contraseña");
            return;
        }
        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(r -> irAlInicio())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_LONG).show());
    }

    private void firebaseLogin(AuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnSuccessListener(r -> irAlInicio())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "No se pudo iniciar sesión: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    private void irAlInicio() {
        Intent i = new Intent(this, HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }
}
