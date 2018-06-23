package nevicelabs.pibiti;

public class Mensagem {
    private String titulo;
    private String conteudo;

    public Mensagem(String titulo, String texto) {
        this.titulo = titulo;
        this.conteudo = texto;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }
}
