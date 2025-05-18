public class ViagemCaminhaoGrande {
    public String estacao, horarioEnvio;
    public boolean pico;
    public int tempoViagem, carga;
    public ViagemCaminhaoGrande(String estacao, String horarioEnvio, boolean pico, int tempoViagem, int carga) {
        this.estacao = estacao;
        this.horarioEnvio = horarioEnvio;
        this.pico = pico;
        this.tempoViagem = tempoViagem;
        this.carga = carga;
    }
}