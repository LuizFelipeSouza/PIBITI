package nevicelabs.pibiti;

import android.Manifest;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
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
import com.google.android.gms.nearby.messages.MessageFilter;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.MessagesOptions;
import com.google.android.gms.nearby.messages.NearbyMessagesStatusCodes;
import com.google.android.gms.nearby.messages.NearbyPermissions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeOptions;

import static android.content.ContentValues.TAG;
import static com.google.android.gms.nearby.messages.Strategy.BLE_ONLY;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<LocationSettingsResult>
{

    private static final int CODIGO_PERMISSOES = 0;
    private TextView mensagem;
    private MessageListener listener;
    private GoogleApiClient googleClient;
    private BluetoothAdapter bluetoothAdapter;
    private static final String CHAVE_INSCRITO = "inscrito";
    private static final int BLUETOOTH_ENABLE_REQUEST_ID = 1;
    private boolean inscrito = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mensagem = (TextView) findViewById(R.id.mensagemId);

        if(savedInstanceState != null) {
            inscrito = savedInstanceState.getBoolean(CHAVE_INSCRITO, false);
        }

        // Checa se as permissões necessárias foram concedidas e as requsita, caso não.
        if(!permissaoConcedida()) {
            Log.i(TAG, "Requisitando permissões");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requisitarPermissao();
            }
        }
        else {
            buildGoogleApiClient();
        }

        Intent bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(bluetoothIntent, BLUETOOTH_ENABLE_REQUEST_ID);
    }

    public void iniciarLoginAtivity(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void requisitarPermissao() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},
                CODIGO_PERMISSOES);
    }

    private boolean permissaoConcedida() {
        boolean permissaoBluetooth = ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED;

        boolean permissaoBluetoothAdmin = ContextCompat.checkSelfPermission(this,
                Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED;
        boolean temPermissao;

        if (permissaoBluetooth && permissaoBluetoothAdmin) {
            temPermissao = true;
        } else {
            temPermissao = false;
        }
        return temPermissao;
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

    public void subscribe() {
        /* SubscribeOptins permite filtrar quais mensagens serão utilizadas pelo app.
         * Neste caso, configurei para apenas BLE (Bluetooth Low Energy), com namespace e
         * tipo definidos no arquivo strings.xml */
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
                        } else {
                            Log.e(TAG, "Falha na operação. Erro: " +
                                    NearbyMessagesStatusCodes
                                            .getStatusCodeString(status.getStatusCode()));
                        }
                    }
                });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        subscribe();
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {}
}
