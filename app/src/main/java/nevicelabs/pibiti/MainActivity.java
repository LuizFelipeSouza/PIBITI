package nevicelabs.pibiti;

import android.Manifest;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.location.*;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.*;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import org.w3c.dom.Text;

import static android.content.ContentValues.TAG;
import static com.google.android.gms.nearby.messages.Strategy.BLE_ONLY;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<LocationSettingsResult> {

    // Constantes
    private static final int CODIGO_PERMISSOES = 1111;
    private static final float GEOFENCE_RADIUS_IN_METERS = 100;
    // Variáveis relativas à Location API
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private Location localizacaoAtual;
    // Variáveis relativas aos geofences
    private GeofencingClient mGeofencingClient;
    private Geofence geofence;
    private PendingIntent mGeofencePendingIntent;
    // Coordenadas do DCOMP
    private double[] coordenadas = {-10.922625,-37.103885};
    // Coordenadas do nevicelab
    // private double[] coordenadas = {-11.150023,-37.616538};
    // Coordenadas do nevicelab II
    // private double[] coordenadas ={-10.927088,-37.105261};
    // Cliente para as APIs Google
    private GoogleApiClient googleClient;
    // Lista de permissões
    private String[] permissoes = {Manifest.permission.ACCESS_FINE_LOCATION};
    // TextViews
    private ImageView fotoDePerfil;
    private TextView nomeUsuarioTextView;
    private TextView horarioEntrada;
    private TextView horarioSaida;
    private TextView totalHoras;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            atualizarUI(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializando as TextViews
        fotoDePerfil = findViewById(R.id.fotoDePerfil);
        nomeUsuarioTextView = findViewById(R.id.nomeUsuarioTextView);
        horarioEntrada = findViewById(R.id.setHorarioEntradaTextView);
        horarioSaida = findViewById(R.id.setHorarioSaidaTextView);
        totalHoras = findViewById(R.id.setHorarioTotalTextView);

        // Verificamos se as permissões necessárias foram concedidas e as requisitamos, caso não
        verificarPermissoes();

        // Cria um cliente das APIs Google
        buildGoogleApiClient();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mGeofencingClient = LocationServices.getGeofencingClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null) {
                    // Log.i("Localização", "Localização nula");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // Log.i("Localização", "Localização: " + location);
                }
            }
        };

        receberLocalizacao();
        verificarConfiguracoesAtuaisDeLocalizacao();
        adicionarGeofences();
    }

    public void iniciarLoginActivity(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private boolean temPermissoes(Context context, String[] permissoes) {
        if (context != null && permissoes != null) {
            for (String permissao : permissoes) {
                if (ActivityCompat.checkSelfPermission(context, permissao) !=
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
        } else {
            buildGoogleApiClient();
        }
    }

    private void receberLocalizacao() {
        if (!temPermissoes(this, permissoes)) {
            ActivityCompat.requestPermissions(this, permissoes, CODIGO_PERMISSOES);
        }
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            localizacaoAtual = location;
                            // Log.i("Localização", "Localização atual: " + localizacaoAtual);
                        }
                    });
        } catch (SecurityException e) {
            Log.w("Localização", "SecurityException: " + e);
        }
    }

    private void criarLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void verificarConfiguracoesAtuaisDeLocalizacao() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
    }

    private void atualizarLocalizacao() {
        criarLocationRequest();
        callbackDeAtualizacaoDaLocalizacao();

        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null);
        } catch (SecurityException e) {
            Log.w("Localização", "Security Exception: " + e);
        }
    }

    private void callbackDeAtualizacaoDaLocalizacao() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.w("Localização", "Localização nula");
                }
                for (Location location : locationResult.getLocations()) {
                    localizacaoAtual = location;
                    // Log.i("Localização", "Localização atual: " + localizacaoAtual);
                }
            }
        };
    }

    /**
     * Método para criar a geofence.
     * Setamos a latitude, longitude e o raio.
     * Configuramos também os métodos de transição,
     * assim determinamos que a interação do aplicativo
     * com o geofence acontecerá tanto na entrada
     * quanto na saída.
     **/
    private Geofence criarGeofences() {
        geofence = new Geofence.Builder()
                .setRequestId("dcomp")
                .setCircularRegion(
                        coordenadas[0],
                        coordenadas[1],
                        GEOFENCE_RADIUS_IN_METERS
                )
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();

        Log.i("Geofence", "Geofence criado");

        return geofence;
    }

    private void adicionarGeofences() {
        Log.i("Geofence", "Método adicionarGeofences()");
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Log.i("Geofence", "Geofence adicionado com sucesso");
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Geofence", "Falha ao adicionar geofence" + e);
                    }
                });
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(criarGeofences());

        return builder.build();
    }

    /**
     * Este é o PendingIntent que faz a comunicação com a classe IntentService
     **/
    private PendingIntent getGeofencePendingIntent() {
        // Se o PendingIntent já existe, reuse-o
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }

        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        mGeofencePendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return mGeofencePendingIntent;
    }

    /**
     * Fornece um cliente para as APIs do Google, o que possibilita o uso da Nearby Messages API
     */
    private synchronized void buildGoogleApiClient() {
        if(googleClient == null) {
            googleClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            googleClient.connect();
        }
    }

    public void atualizarUI(Intent intent) {
        String imagemUrl = intent.getStringExtra("imagemPerfil");
        Glide.with(this).load(imagemUrl).into(fotoDePerfil);
        nomeUsuarioTextView.setText(intent.getStringExtra("nome"));
        horarioEntrada.setText(intent.getStringExtra("horarioEntrada"));
        horarioSaida.setText(intent.getStringExtra("horarioSaida"));
        // totalHoras.setText(intent.getStringExtra("totalHoras"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizarLocalizacao();
        registerReceiver(broadcastReceiver, new IntentFilter(
                GeofenceTransitionsIntentService.BROADCAST_ACTION
        ));
    }

    @Override
    protected void onStop() { super.onStop(); }

    @Override
    public void onConnected(@Nullable Bundle bundle) {}

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {}
}
