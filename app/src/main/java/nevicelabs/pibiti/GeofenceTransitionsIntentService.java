package nevicelabs.pibiti;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.nearby.messages.Message;
import com.google.firebase.auth.FirebaseUser;

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
    private Mensagem mensagem;

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
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if(geofencingEvent.hasError()) {
            Log.w("Intent Service", "Erro Geofencing Event");
        } else {
            int tipoDeTransicao = geofencingEvent.getGeofenceTransition();

            // Quando o usuário entrar no local, registre o horário
            if(tipoDeTransicao == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.i("Intent Service", "Entrada no geofence");
                mensagem = new Mensagem("Bem vindo ao DCOMP", "Você deu entrada às " + horarioEntrada);
                atualizarNotificacao(mensagem);
                horarioEntrada = GregorianCalendar.getInstance().getTime();
                // Log.i("Intent Service", "Horário: " + horarioEntrada);
                persistirNoFirebase(getDadosDoUsuario());
            }
            // Também registre o horário quando o usuário sair do local
            else if (tipoDeTransicao == Geofence.GEOFENCE_TRANSITION_EXIT) {
                Log.d("Intent Service", "Saída no geofence");
                horarioSaida = GregorianCalendar.getInstance().getTime();
                mensagem = new Mensagem("Até logo!", "Você saiu do DCOMP às " + horarioSaida);
                // Log.i("Intent Service", "Horário final: " + horarioSaida);

                horarioFinal = horarioSaida.getTime() - horarioEntrada.getTime();
                Log.i("", "Horário final: " + horarioFinal);
                atualizarNotificacao(mensagem);
                persistirNoFirebase(getDadosDoUsuario());
            }
        }
    }

    private void atualizarNotificacao(Mensagem mensagem) {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent launchIntent = new Intent(getApplicationContext(), MainActivity.class);
        launchIntent.setAction(Intent.ACTION_MAIN);
        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        String titulo = mensagem.getTitulo();
        String texto = mensagem.getConteudo();
        PendingIntent intent = PendingIntent.getActivity(getApplicationContext(),
                0, launchIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder notificacao = new NotificationCompat.Builder(this);
        notificacao.setSmallIcon(R.drawable.dcomp_icone);
        notificacao.setContentTitle(titulo);
        notificacao.setContentText(texto);
        notificacao.setContentIntent(intent);

        notificationManager.notify(NOTIFICATION_ID, notificacao.build());
    }

    private Usuario getDadosDoUsuario() {
        Log.d("Firebase Auth", "getDadosdoUsuario()");

        LoginActivity login = new LoginActivity();
        FirebaseUser usuarioFirebase = login.getUsuarioFirebase();

        if (usuarioFirebase != null) {
            // Log.i("Firebase Auth", "Montando usuário");
            // Log.i("Usuario Firebase", usuarioFirebase.getDisplayName());
            // Log.i("Usuario Firebase", usuarioFirebase.getEmail());
            Usuario usuario = new Usuario();
            usuario.setNome(usuarioFirebase.getDisplayName());
            usuario.setEmail(usuarioFirebase.getEmail());

            return usuario;
        } else {
            Log.d("Firebase Auth", "Usuário nulo!");
            return null;
        }
    }

    /**
     * Recebe o número de horas, consulta o usuário logado e o persiste no Firebase Database
     * @param usuario
     */
    private void persistirNoFirebase(Usuario usuario) {
        Log.d("Firebase Database", "Método persistirNoFirebase()");
        LoginActivity login = new LoginActivity();
        FirebaseUser usuarioFirebase = login.getUsuarioFirebase();

        if (usuarioFirebase != null) {
            Log.d("Firebase Database", "Persistindo usuário");
            UsuarioDAO dao = new UsuarioDAO();

            usuario.setNome(usuarioFirebase.getDisplayName());
            usuario.setEmail(usuarioFirebase.getEmail());
            usuario.setNumDeHoras(usuario.getNumDeHoras());
            // Log.i("Firebase Database", "Nome: " + usuario.getNome());
            // Log.i("Firebase Database", "E-mail: " + usuario.getEmail());
            // Log.i("Firebase Database", "Horas: " + usuario.getNumDeHoras());

            dao.adicionar(usuario);
            Log.i("Firebase Database", "Usuario persistido");
        } else {
            Log.d("Firebase Database", "Usuário nulo!");
        }
    }
}
