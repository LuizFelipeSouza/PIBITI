package nevicelabs.pibiti;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

import java.util.Arrays;
import java.util.List;

/**
 * Tela de login pelo FirebaseUI
 */
public class LoginActivity extends AppCompatActivity {

    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 123;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser usuarioFirebase;
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Configurar Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.idClienteOAuth))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Verificamos se o usuário já está logado
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        if (account != null) {
            iniciarActivity();
        }

        // Definido tamanho para o botão de Login
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        // Lista dos provedores de autenticação
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build());

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser usuarioAtual = mAuth.getCurrentUser();
        // Atualiza a tela com o usuário atual
        atualizarUI(usuarioAtual);

        // Listener para monitorar as mudanças de estado de login
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser usuarioFirebaseAtual = firebaseAuth.getCurrentUser();
                setUsuarioFirebase(usuarioFirebaseAtual);

                if (usuarioFirebaseAtual != null) {
                    Log.d("Firebase Auth", "onAuthStateChanged: O usuário fez login: "
                            + usuarioFirebaseAtual.getUid() +'\n' + usuarioFirebaseAtual.getDisplayName());

                    // getInformacoesDoUsuario(usuarioFirebase);

                } else {
                    Log.d("Firebase Auth", "onAuthStateChanged: O usuário fez logout");
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount acc = task.getResult(ApiException.class);
                    // Log.i("Google Sign In", "Login realizado com sucesso!");
                    autenticarFirebaseComGoogle(acc);
                    getUsuarioFirebase();
                } catch (ApiException e) {
                    Log.w("Google Sign In", "Falha no Google Sign In: ", e);
                }
            }
        }
    }

    private void googleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Neste método é feita a autneticação com o Firebase.
     * Após feita a atenticação, preenchemos os atributos do objeto Usuario
     * @param account
     */
    private void autenticarFirebaseComGoogle(GoogleSignInAccount account) {
        Log.d("", "Autenticando firebase com Google" + account.getId());

        AuthCredential credencial = GoogleAuthProvider.getCredential((account.getIdToken()), null);
        mAuth.signInWithCredential(credencial)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("Firebase Authentication", "signInWithCredential: success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            atualizarUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Firebase Authentication", "signInWithCredential:failure", task.getException());
                            // atualizarUI(null);

                        }
                    }
                });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask){
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
        } catch(ApiException e) {
            Log.w("Google Sign In", "Falha no Google Sign In; codigo: " + e.getStatusCode());
            // atualizarUI(null);
        }
    }

    public void sair(View view) {
    }

    /**
     * Utilizado na criação desta Activity.
     * Este método é chamado para direcionar o usuário para a MainActivity depois de autenticado
     */
    public void iniciarActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Método utilizado pelo botão de Sign In. Deve er corrigido
     * // TODO: Substituir o onClick do botão Sign In
     * @param view
     */
    public void iniciarActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Atualiza a interface com informações sobre o usuario
     * @param account
     */
    private void atualizarUI(GoogleSignInAccount account) {
        /*
        TextView nomeUsuario = new TextView(this);
        String nome = account.getDisplayName();
        nomeUsuario.setText(nome);
        */
    }

    /**
     * Atualiza a interface com informações sobre o usuário
     * @param firebaseUser
     */
    private void atualizarUI(FirebaseUser firebaseUser) {
        /*
        TextView nomeUsuario = findViewById(R.id.nomeUsuarioTextView);

        String nome = firebaseUser.getDisplayName();
        Log.i("Atualizar UI", "Usuário: " + nome);
        nomeUsuario.setText(nome);
        */
    }

    private void setUsuarioFirebase(FirebaseUser user) {
        this.usuarioFirebase = user;
    }

    /**
     * Verifica se o usuário fez login pelo Firebase
     * @return FirebaseUser, caso autenticado
     * @return null, caso contrário
     */
    public FirebaseUser getUsuarioFirebase() {
        // Verificamos se há um usuário autenticado
        Log.d("Firebase Auth", "getUsuarioFirebase()");
        FirebaseUser firebase = mAuth.getCurrentUser();
        Log.i("Firebase Auth", "Usuário Firebase: " + firebase);

        if (this.usuarioFirebase != null) {
            Log.d("Firebase Auth", "Usuário: " + usuarioFirebase.getDisplayName());
            return this.usuarioFirebase;
        } else {
            return null;
        }
    }

    public Usuario getUsuarioGoogle() {
        GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(this);

        if (googleAccount != null) {
            usuario.setNome(googleAccount.getDisplayName());
            usuario.setEmail(googleAccount.getEmail());

            return usuario;
        } else {
            return null;
        }
    }
}