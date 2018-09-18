package nevicelabs.pibiti;

import java.util.Date;

/**
 * Classe usuario. Contém os dados que serão armazenados e buscados no Firebase
 */
public class Usuario {

    private String id;
    private String imagemPerfil;
    private String nome;
    private String email;
    private String matricula;
    private Date horarioEntrada;
    private Date horarioSaida;
    private long numDeHoras;

    // O construtor vazio é usado pelo Firebase Database
    public Usuario() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImagemPerfil() {
        return this.imagemPerfil;
    }

    public void setImagemPerfil(String imagemPerfil) {
        this.imagemPerfil = imagemPerfil;
    }

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

    public void setHorarioEntrada(Date horarioEntrada) {
        this.horarioEntrada = horarioEntrada;
    }

    public Date getHorarioEntrada() {
        return this.horarioEntrada;
    }

    public void setHorarioSaida(Date horarioSaida) {
        if (horarioSaida == null) {
            this.horarioSaida = this.horarioEntrada;
        } else {
            this.horarioSaida = horarioSaida;
        }
    }

    public Date getHorarioSaida() {
        return this.horarioSaida;
    }

    public long getNumDeHoras() {
        return numDeHoras;
    }

    public void setNumDeHoras(long numDeHoras) {
        this.numDeHoras = numDeHoras;
    }
}
