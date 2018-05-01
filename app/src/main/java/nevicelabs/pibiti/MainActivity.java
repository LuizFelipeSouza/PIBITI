package nevicelabs.pibiti;

import android.Manifest;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.*;

import static android.content.ContentValues.TAG;
import static com.google.android.gms.nearby.messages.Strategy.BLE_ONLY;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<LocationSettingsResult>
{

    private static final int CODIGO_PERMISSOES = 1111;
    private static final String CHAVE_INSCRITO = "inscrito";
    private GoogleApiClient googleClient;
    private TextView mensagem;
    private MessageListener listener;
    private BluetoothAdapter bluetoothAdapter;
    private static final int BLUETOOTH_ENABLE_REQUEST_ID = 1;
    private boolean inscrito = false;
    private String[] permissoes = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.INTERNET };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mensagem = (TextView) findViewById(R.id.mensagemId);

        // TODO: O que significa esse savedInstance?
        if(savedInstanceState != null) {
            inscrito = savedInstanceState.getBoolean(CHAVE_INSCRITO, false);
        }

        // Verificamos se as permissões necessárias foram concedidas e as requisitamos, caso não
        verificarPermissoes();

        // Requsita a ativação do Bluetooth ao iniciar a Activity
        Intent bluetoothIntent = new Intent(bluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(bluetoothIntent, BLUETOOTH_ENABLE_REQUEST_ID);
    }

    public void iniciarLoginAtivity(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private boolean temPermissoes(Context context, String[] permissoes) {
        if (context != null && permissoes != null) {
            for (String permissao : permissoes) {
                if(ActivityCompat.checkSelfPermission(context, permissao) !=
                        PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    private void verificarPermissoes() {
        if (!temPermissoes(this, permissoes)) {
            ActivityCompat.requestPermissions(this, permissoes, CODIGO_PERMISSOES);
        }
        else {
            buildGoogleApiClient();
        }
    }

    /**
     * Fornece um cliente para as APIs do Google, o que possibilita o uso da Nearby Messages API
     */
    private synchronized void buildGoogleApiClient() {
        if(googleClient == null) {
            googleClient = new GoogleApiClient.Builder(this)

                    .addApi(Nearby.MESSAGES_API, new MessagesOptions.Builder()
                            .setPermissions(NearbyPermissions.BLE).build())
                    .addConnectionCallbacks(this)
                    .enableAutoManage(this,this).build();

            googleClient.connect();
        }
    }

    /**
     * Retorna um intent a partir da classe Service. No caso, MensagensService
     * @return
     */
    @NonNull
    private Intent getBackgroundSubscribeServiceIntent() {
        return new Intent(this, MensagensService.class);
    }

    private PendingIntent getPendingIntent() {
        return PendingIntent.getService(this, 0,
                getBackgroundSubscribeServiceIntent(), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Método chamado a partir do pressionamento do botão inscrever-se
     * @param view
     */
    public void inscrever(View view) {
        Log.i("", "Botão inscrição pressionado");
        verificarPermissoes();
        subscribe();
    }

    public void subscribe() {
        /* SubscribeOptins permite filtrar quais mensagens serão utilizadas pelo app.
         * Neste caso, configurei para apenas BLE (Bluetooth Low Energy), com namespace e
         * tipo definidos no arquivo strings.xml
         */
        SubscribeOptions options = new SubscribeOptions.Builder()
                .setStrategy(Strategy.BLE_ONLY)
                .setFilter(new MessageFilter.Builder()
                        .includeNamespacedType(getString(R.string.Namespace),
                                getString(R.string.Type))
                        .build()).build();

        Nearby.Messages.subscribe(googleClient, getPendingIntent(), options)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if(status.isSuccess()) {
                            Log.i(TAG, "Inscrito com sucesso");
                            startService(getBackgroundSubscribeServiceIntent());
                            inscrito = true;
                            mensagem.setText("Inscrito");
                        } else {
                            Log.e(TAG, "Falha na operação. Erro: " +
                                    NearbyMessagesStatusCodes
                                            .getStatusCodeString(status.getStatusCode()));
                        }
                    }
                });
    }

    /**
     * Método chamado a partir do pressionamento do botão de transmissão
     * @param view
     */
    public void transmitir(View view) {
        Log.i("", "Botão transmissão pressionado");
        verificarPermissoes();
        String textoMensagem = "Olá Mundo!";
        publicar(textoMensagem);
    }

    // TODO: Certificar-se de que a publicação está sendo feita com o mesmo nome e namespace da inscrição
    private void publicar(String message) {
        PublishOptions options = new PublishOptions.Builder()
                .setStrategy(BLE_ONLY)
                .setCallback(new PublishCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                        Log.i(TAG, "No longer publishing");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                            }
                        });
                    }
                }).build();
        Message mensagemAtiva = new Message(message.getBytes());
        Nearby.getMessagesClient(this).publish(mensagemAtiva);
        // Nearby.Messages.publish(googleClient, mensagemAtiva, options);

        Log.i("", "Transmitindo " + message);
        mensagem.setText("Transmitindo");
    }

    @Override
    protected void onStop() {
        // Nearby.getMessagesClient(this).unpublish(mMessage);
        // Nearby.getMessagesClient(this).unsubscribe(mMessageListener);
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // subscribe();
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {}
}
