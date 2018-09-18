package nevicelabs.pibiti;

import android.util.Log;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Esta classe é responsável por fazer a counicação com o Firebase Database
 */
public class UsuarioDAO {

    // Referência ao nó raiz do banco de dados. Permite a escrita e leitura de dados
    private FirebaseDatabase fireRef = FirebaseDatabase.getInstance();
    // Referência ao nó usuario do banco
    private DatabaseReference usuarioRef;
    private SimpleDateFormat formatoData = new SimpleDateFormat("yyyy/MMMM/dd");
    private SimpleDateFormat formatoHora = new SimpleDateFormat("HH:mm");

    /**
     * Construtor vazio, necessário para chamadas a Datasnapshot.getValue(Usuario.class)
     */
    public UsuarioDAO() {}

    public void atualizarHoras(Usuario usuario) {
        Log.i("Firebase", "Atualizando horas do usuário " + usuario.getNome());

        usuarioRef = fireRef.getReference(usuario.getId());
        usuarioRef.child("nome").setValue(usuario.getNome());

        usuarioRef.child(formatoData.format(usuario.getHorarioEntrada()))
                .child("horarios")
                .child("horario_entrada")
                .setValue(formatoHora.format(usuario.getHorarioEntrada()));

        usuarioRef.child(formatoData.format(usuario.getHorarioSaida()))
                .child("horarios")
                .child("horario_entrada")
                .setValue(formatoHora.format(usuario.getHorarioSaida()));

        long horarioFinal = 0;
        usuarioRef.child("total_horas").setValue(usuario.getNumDeHoras());
    }
}
