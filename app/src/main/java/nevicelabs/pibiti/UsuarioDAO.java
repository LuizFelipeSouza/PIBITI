package nevicelabs.pibiti;

import android.util.Log;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

/**
 * Esta classe é responsável por fazer a counicação com o Firebase Database
 */
public class UsuarioDAO {

    // Referência ao nó raiz do banco de dados. Permite a escrita e leitura de dados
    private FirebaseDatabase fireRef = FirebaseDatabase.getInstance();
    // Referência ao nó usuario do banco
    private DatabaseReference usuarioRef = fireRef.getReference("usuario");

    /**
     * Construtor vazio, necessário para chamadas a Datasnapshot.getValue(Usuario.class)
     */
    public UsuarioDAO() {}

    public void adicionar(Usuario usuario) {
        Log.i("Firebase", "Armazendando " + usuario.getNome() + " no Firebase");
        usuarioRef.child("horario_entrada").setValue(usuario.getHorarioEntrada());
    }

    public boolean usuarioExiste(Usuario usuario) {
        return true;
    }

    public void atualizarHoras(Usuario usuario, Date horario) {
        // id = usuario.getMatricula();
        Log.i("Firebase", "Atualizando horas do usuário " + usuario.getNome()
                + "\n" + "Horario: " + horario);
        // usuarioRef.child("users").child(id).child("horas").setValue(horario);
        usuarioRef.child("horario_saida").setValue(horario);
        long horarioFinal = horario.getTime();
        usuarioRef.child("total_horas").setValue(horarioFinal);
    }
}
