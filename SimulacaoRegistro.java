public class SimulacaoRegistro {
    String zona;
    String caminhao;
    int carga;
    int viagensFeitas;
    int totalGrandes;

    SimulacaoRegistro(String zona, String caminhao, int carga, int viagens, int grandes) {
        this.zona = zona;
        this.caminhao = caminhao;
        this.carga = carga;
        this.viagensFeitas = viagens;
        this.totalGrandes = grandes;
    }

    public String toString() {
        return "🗺️ Zona: " + zona + " | 🚛 " + caminhao + " → " + carga + " kg | Viagens: "
                + viagensFeitas + " | 🚚 Grandes: " + totalGrandes;
    }
}
