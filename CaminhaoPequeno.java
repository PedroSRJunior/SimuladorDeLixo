public class CaminhaoPequeno {
    String nome;
    int capacidade;
    int viagens = 0;
    int maxViagensPermitidas = 15; // alterado de 50 para 15

    CaminhaoPequeno(String nome, int capacidade) {
        this.nome = nome;
        this.capacidade = capacidade;
    }
}