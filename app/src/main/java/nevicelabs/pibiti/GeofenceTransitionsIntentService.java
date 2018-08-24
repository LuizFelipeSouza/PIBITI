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

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Este service é um listener para transições entre geofences.
 * Ou seja, detecta quando o usuário entra ou sai de uma área delimitada.
 */
public class GeofenceTransitionsIntentService extends IntentService {
    private static Date horarioEntrada;
    private static Date horarioSaida;
    private float horarioFinal;
    private SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm");
    private SimpleDateFormat formatoData = new SimpleDateFormat("dd.MM.yyyy 'às' HH:mm");
    private static final int NOTIFICATION_ID = 1;
    private Mensagem mensagem;
    public static final String BROADCAST_ACTION = "nevicelabs.pibiti";

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
                horarioEntrada = Calendar.getInstance().getTime();
                mensagem = new Mensagem("Bem vindo ao DCOMP", "Você deu entrada às "
                        + formatoHora.format(horarioEntrada));

                atualizarNotificacao(mensagem);
                atualizarUI(getDadosDoUsuario());
                persistirNoFirebase(getDadosDoUsuario());

            }
            // Também registre o horário quando o usuário sair do local
            if (tipoDeTransicao == Geofence.GEOFENCE_TRANSITION_EXIT) {
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

    private Usuario getDadosDoUsuario() {
        // Log.d("Firebase Auth", "getDadosdoUsuario()");

        FirebaseUser usuarioFirebase = LoginActivity.getUsuarioFirebase();

        if (usuarioFirebase != null) {
            // Log.i("Firebase Auth", "Montando usuário");
            Usuario usuario = new Usuario();
            usuario.setId(usuarioFirebase.getUid());
            usuario.setImagemPerfil(usuarioFirebase.getPhotoUrl().toString());
            usuario.setNome(usuarioFirebase.getDisplayName());
            usuario.setEmail(usuarioFirebase.getEmail());
            // TODO: Verificar se o horário de entrada está ficando como null
            usuario.setHorarioEntrada(horarioEntrada);
            usuario.setHorarioSaida(horarioSaida);
            // usuario.setNumDeHoras();

            return usuario;
        } else {
            Log.w("Firebase Auth", "Usuário nulo!");
            return null;
        }
    }

    /**
     * Recebe o número de horas, consulta o usuário logado e o persiste no Firebase Database
     * @param usuario
     */
    private void persistirNoFirebase(Usuario usuario) {
        LoginActivity login = new LoginActivity();
        FirebaseUser usuarioFirebase = login.getUsuarioFirebase();

        // Verificamos se o usuario não é nulo para prosseguirmos com a operação
        if (usuarioFirebase != null) {
            // Log.d("Firebase Database", "Persistindo usuário");
            UsuarioDAO dao = new UsuarioDAO();

            /* Verificamos se o usuário já existe. Caso sim, apenas a tualizamos o hrário de saída
             Do contrário, criamos um usuário com nome, e-mail e horário de entrada. */
            if(dao.usuarioExiste(usuario)) {
                dao.atualizarHoras(usuario, horarioEntrada);
            } else {
                dao.adicionar(usuario);
                Log.i("Firebase Database", "Usuario adicionado");
            }
        } else {
            Log.d("Firebase Database", "Usuário nulo!");
        }
    }

    private void atualizarUI(Usuario usuario) {
        Intent intent = new Intent(BROADCAST_ACTION);

        intent.putExtra("nome", usuario.getNome());
        intent.putExtra("imagemPerfil", usuario.getImagemPerfil());
        intent.putExtra("horarioEntrada", formatoData.format(horarioEntrada));
        // intent.putExtra("horarioSaida", formatoData.format(horarioSaida));
        intent.putExtra("totalHoras", usuario.getNumDeHoras());

        sendBroadcast(intent);
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
}
