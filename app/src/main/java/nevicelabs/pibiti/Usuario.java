package nevicelabs.pibiti;

/**
 * Classe usuario. Contém os dados que serão armazenados e buscados no Firebase
 */
public class Usuario {

    private String nome;
    private String email;
    private String matricula;
    private long numDeHoras;

    // O construtor vazio é usado pelo Firebase Database
    public Usuario() {}

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public long getNumDeHoras() {
        return numDeHoras;
    }

    public void setNumDeHoras(long numDeHoras) {
        this.numDeHoras = numDeHoras;
    }
}
