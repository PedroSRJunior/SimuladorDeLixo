public class ViagemCaminhaoPequeno {
    public String caminhao, zona, estacao, horario;
    public boolean pico;
    public int carga, tempoViagem;
    public ViagemCaminhaoPequeno(String caminhao, String zona, String estacao, String horario, boolean pico, int carga, int tempoViagem) {
        this.caminhao = caminhao;
        this.zona = zona;
        this.estacao = estacao;
        this.horario = horario;
        this.pico = pico;
        this.carga = carga;
        this.tempoViagem = tempoViagem;
    }
}