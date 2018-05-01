package nevicelabs.pibiti;

/**
 * Classe usuario. Contém os dados que serão armazenados e buscados no Firebase
 */
public class Usuario {

    private String nome;
    private String email;
    private long matricula;

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

    public long getMatricula() {
        return matricula;
    }

    public void setMatricula(long matricula) {
        this.matricula = matricula;
    }
}
