package nevicelabs.pibiti;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.nearby.messages.Message;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Este service é um listener para transições entre geofences.
 * Ou seja, detecta quando o usuário entra ou sai de uma área delimitada.
 */
public class GeofenceTransitionsIntentService extends IntentService {
    private Date horarioEntrada;
    private Date horarioSaida;
    private long horarioFinal;
    private static final int NOTIFICATION_ID = 1;

    /**
     * Construtor vazio, necessário para o registro em AndroidManifest
     */
    public GeofenceTransitionsIntentService() {
        super("");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i("Intent Service", "onHandleIntent");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if(geofencingEvent.hasError()) {
            Log.i("Intent Service", "Erro Geofencing Event");
        } else {
            int tipoDeTransicao = geofencingEvent.getGeofenceTransition();

            // Quando o usuário entrar no local, registre o horário
            if(tipoDeTransicao == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.i("Intent Service", "Entrada no geofence");
                atualizarNotificacao("Você deu entrada no DCOMP às " + horarioEntrada);
                horarioEntrada = GregorianCalendar.getInstance().getTime();
                Log.i("Intent Service", "Horário: " + horarioEntrada);
            }
            // Também registre o horário quando o usuário sair do local
            else if (tipoDeTransicao == Geofence.GEOFENCE_TRANSITION_EXIT) {
                Log.i("Intent Service", "Saída no geofence");
                horarioSaida = GregorianCalendar.getInstance().getTime();
                Log.i("Intent Service", "Horário final: " + horarioSaida);

                horarioFinal = horarioSaida.getTime() - horarioEntrada.getTime();
                Log.i("", "Horário final: " + horarioFinal);
                atualizarNotificacao("Você saiu do DCOMP às "
                        + horarioSaida + "Teve uma permanência de " + horarioFinal + " minutos");
            }
        }
    }

    private void atualizarNotificacao(String mensagem) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent launchIntent = new Intent(getApplicationContext(), MainActivity.class);
        launchIntent.setAction(Intent.ACTION_MAIN);
        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        String titulo = "Bem-vindo ao DCOMP";
        String texto = mensagem;
        PendingIntent intent = PendingIntent.getActivity(getApplicationContext(),
                0, launchIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder notificacao = new NotificationCompat.Builder(this);
        notificacao.setSmallIcon(android.R.drawable.star_on);
        notificacao.setContentTitle(titulo);
        notificacao.setContentText(texto);
        notificacao.setContentIntent(intent);

        notificationManager.notify(NOTIFICATION_ID, notificacao.build());
    }
}
