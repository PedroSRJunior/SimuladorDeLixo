import javax.swing.*;

public class EstacaoTransferencia {
    String nome;
    int cargaAcumulada = 0;
    int caminhoesGrandesUsados = 0;
    int totalLixoEnviado = 0;  // Total de lixo enviado para o aterro
    JTextArea logNormal;
    JTextArea logAterro;

    EstacaoTransferencia(String nome, JTextArea logNormal, JTextArea logAterro) {
        this.nome = nome;
        this.logNormal = logNormal;
        this.logAterro = logAterro;
    }

    // Agora retorna o tempo de espera simulado
    int receberCarga(CaminhaoPequeno caminhao, int cargaKg) {
        int tempoEspera = (int)(Math.random() * 10) + 1; // Simula espera entre 1 e 10 minutos
        cargaAcumulada += cargaKg;
        logNormal.append("📥 " + nome + ": " + caminhao.nome + " entregou " + cargaKg +
                " kg. Tempo de espera: " + tempoEspera + " min. Total atual: " + cargaAcumulada + " kg\n");
        return tempoEspera;
    }

    void verificarEnvio() {
        if (cargaAcumulada >= 20000) {
            enviarCaminhaoGrande();
        }
    }

    void enviarCaminhaoGrande() {
        logAterro.append("🚚 " + nome + ": caminhão GRANDE enviado com para o aterro!\n");
        caminhoesGrandesUsados++;
        totalLixoEnviado += cargaAcumulada;
        cargaAcumulada = 0;
        new TelaCarregamentoGrande(null, nome).iniciarAnimacao();
    }

    int getTotalGrandes() {
        return caminhoesGrandesUsados;
    }

    int getTotalLixoEnviado() {
        return totalLixoEnviado;
    }
}