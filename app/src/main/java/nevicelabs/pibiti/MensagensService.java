package nevicelabs.pibiti;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

/**
 * Esta classe service escuta as mensagens enviadas por dispositivos bluetooth no backgorund do app.
 * Quando uma mensagem é encontrada, o método onHandleIntent é chamado para lidar com seu conteúdo.
 */
public class MensagensService extends IntentService {

    private static final int MESSAGES_NOTIFICATION_ID = 1;
    private Notification notificacao;
    private String mensagem;

    // Construtor sem argumentos para a definição do Service em AndroidManifest
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

    /**
     * Método executado quando startSevice() é chamado em MainActivity
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.i("", "Serviço iniciado");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("","Serviço interrompido");
    }

    /**
     * Este método é chamado quando o aplicativo encontra uma mensagem enviada por um dispositivo.
     * @param intent
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // startForeground(MESSAGES_NOTIFICATION_ID, notificacao);
        MessageListener listener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                Log.i("", "Mensagem encontrada: " + message);
                mensagem = message.toString();
                atualizarNotificacao(message);
            }

            @Override
            public void onLost(Message message) {
                super.onLost(message);
                Log.i("", "Fora do alcance da mensagem: " + message);
                mensagem = message.toString();
                atualizarNotificacao(message);
            }
        };
    }

    private void criarNotificacao() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.star_big_on)
                .setContentTitle("PIBITI")
                .setContentText("Buscando mensagens...");

        // Cria um intent para que quando a notificação seja tocada, a MainAcitivity seja aberta
        Intent resultIntent = new Intent(this, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificacao = mBuilder.build();

        notificationManager.notify(MESSAGES_NOTIFICATION_ID, notificacao);
    }

    private void atualizarNotificacao(Message messages) {
       NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("Nova mensagem")
                .setContentText(messages.toString())
                .setSmallIcon(android.R.drawable.stat_notify_more);

        manager.notify(MESSAGES_NOTIFICATION_ID, mNotifyBuilder.build());
    }

    public String getMensagem() {
        return this.mensagem;
    }
}
