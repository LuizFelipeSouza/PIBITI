package nevicelabs.pibiti;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.*;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * Tela de login pelo FirebaseUI
 */
public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private static Usuario usuario;
    private UsuarioDAO dao = new UsuarioDAO();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Google Sign-In padrão requisitando apenas email.
        // O token é encontrado nas credenciais do projeto no Google Console e é utilizado pelo Firebase
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.idClienteOAuth))
                .requestEmail()
                .build();

        // Criamos o GoogleSignInClient
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        /* Verificamos se o usuário já está logado. Se o usuário não estiver logado, account será null
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        token = account.getIdToken(); */

        // Autenticação do Firebase
        firebaseAuth = FirebaseAuth.getInstance();

        // Definido tamanho para o botão de Login
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        // Não sei pra que serve
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Verificamos se o usuário está conectado pelo Firebase
        FirebaseUser usuarioAtual = firebaseAuth.getCurrentUser();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                Log.i("", "Login da Google executado. Autenticando com Firebase");
                // Caso o login da Google tenha sido realizado com sucesso...
                GoogleSignInAccount acc = task.getResult(ApiException.class);
                // ... autenticar com o Firebase
                autenticarFirebaseComGoogle(acc);
                Log.i("","Método de autenticação com o Firebase executado!");
            } catch (ApiException e) {
                Log.i("", "Api Exception");
                e.printStackTrace();
            }
        }
    }

    /**
     * Neste método é feita a autneticação com o Firebase.
     * Após feita a atenticação, preenchemos os atributos do objeto Usuario
     * @param account
     */
    private void autenticarFirebaseComGoogle(GoogleSignInAccount account) {
        Log.i("", "Autenticando firebase com Google" + account.getId());

        AuthCredential credencial = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credencial)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("", "Sucesso ao fazer login");
                            FirebaseUser user = firebaseAuth.getCurrentUser();

                            Log.i("", "Settando informações do usuário");
                            usuario.setNome(user.getDisplayName());
                            usuario.setEmail(user.getEmail());

                            Log.i("", "Enviando objeto Usuario ao DAO");
                            dao.adicionar(usuario);

                            // updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Login Falhou!", Toast.LENGTH_SHORT).show();
                            // Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            // updateUI(null);
                        }
                    }
                });
    }

    public void sair(View view) {
        // SignOut do Firebase
        firebaseAuth.signOut();
        Log.i("", "Saindo do Firebase");

        // SignOut do Google
        googleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.i("", "Saindo do Google Sign In");
                        updateUI(null);
                    }
                });
    }

    public void iniciarActivity(View view) {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void updateUI(GoogleSignInAccount account) {
        // Faz alguma coisa
    }

    public static Usuario getUsuario() {
        return usuario;
    }
}

