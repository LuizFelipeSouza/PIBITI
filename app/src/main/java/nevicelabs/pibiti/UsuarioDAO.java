package nevicelabs.pibiti;

import android.util.Log;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Esta classe é responsável por fazer a counicação com o Firebase Database
 */
public class UsuarioDAO {

    // Referência ao nó raiz do banco de dados
    private DatabaseReference fireRef = FirebaseDatabase.getInstance().getReference();
    // Referência ao nó usuário do banco
    private DatabaseReference usuarioRef = fireRef.child("Usuario");
    // Utilizamos o número de matrícula como id. Este será um nó pai dos outros valores
    private String id;

    public void adicionar(Usuario usuario) {
        // Utilizamos o número de matrícula como id
        id = usuario.getMatricula();
        Log.i("", "Armazendando " + usuario.getNome() + " no Firebase");
        // Armazenamos os dados no banco como nós filhos de id
        usuarioRef.child(id).setValue(usuario);
    }

    public void atualizarHoras(Usuario usuario) {

    }
}
