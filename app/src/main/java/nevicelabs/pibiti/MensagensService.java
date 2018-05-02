package nevicelabs.pibiti;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.TextView;

import android.widget.Toast;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Esta classe service excuta as mensagens enviadas por dispositivos bluetooth no backgorund do app.
 * Quando uma mensagem é encontrada, o método onHandleIntent é chamado para lidar com seu conteúdo.
 */
public class MensagensService extends IntentService {

    private static final int MESSAGES_NOTIFICATION_ID = 1;
    private Date horarioIncial;
    private Date horarioFinal;
    Usuario usuario = LoginActivity.getUsuario();

    // Construtuor sem argumentos apra a definição do Service em AndroidManifest.xml
    public MensagensService() {
        super("name");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public MensagensService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        criarNotificacao();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.i("", "Serviço iniciado onStartComand()");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("", "Serviço interrompido onDestroy()");
    }

    /**
     * Este método é chamado quando o aplicativo encontra uma mensagem enviada por um dispositivo.
     *
     * @param intent
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i("", "Entrando no método de execução onHandleIntent()");
        horarioIncial = GregorianCalendar.getInstance().getTime();
        Log.i("", "Horário: " + horarioIncial);

        MessageListener listener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                Log.i("", "Mensagem encontrada: " + message);
                horarioIncial = GregorianCalendar.getInstance().getTime();
                atualizarNotificacao(message);
            }

            @Override
            public void onLost(Message message) {
                super.onLost(message);
                Log.i("", "Fora do alcance da mensagem: " + message);
                horarioFinal = GregorianCalendar.getInstance().getTime();
            }
        };
    }

    private void criarNotificacao() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.star_on)
                .setContentTitle("PIBITI")
                .setContentText("Buscando mensagens...");

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(MESSAGES_NOTIFICATION_ID, mBuilder.build());
    }

    private void atualizarNotificacao(Message mensagem) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent launchIntent = new Intent(getApplicationContext(), MainActivity.class);
        launchIntent.setAction(Intent.ACTION_MAIN);
        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        String titulo = mensagem.getType();
        String texto = new String(mensagem.getContent());
        PendingIntent intent = PendingIntent.getActivity(getApplicationContext(),
                0, launchIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder notificacao = new NotificationCompat.Builder(this);
        notificacao.setSmallIcon(android.R.drawable.star_on);
        notificacao.setContentTitle(titulo);
        notificacao.setContentText(texto);
        notificacao.setContentIntent(intent);

        notificationManager.notify(MESSAGES_NOTIFICATION_ID, notificacao.build());
    }
}
