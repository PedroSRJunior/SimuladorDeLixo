import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.io.FileWriter;
import java.io.IOException;

class ColetaLixoFrame extends JFrame {
    JTextArea outputAreaLogs = new JTextArea(20, 40);
    JTextArea outputAreaAterro = new JTextArea(20, 40);

    JButton realizarViagemButton = new JButton("Realizar TODAS as Viagens");
    JButton relatorioButton = new JButton("Relatório Completo");
    JButton reiniciarSimulacaoButton = new JButton("Reiniciar Simulação");
    JButton exportarCsvButton = new JButton("Exportar Relatório CSV");

    static final int CAMINHOES_POR_TIPO = 10;
    static final int[] CAMINHOES_CAPACIDADES = {2, 4, 8, 10};
    static final String[] NOMES_ZONAS = {"Zona Sul", "Zona Norte", "Zona Centro", "Zona Leste", "Zona Sudeste"};
    static final String[] NOMES_ESTACOES = {"Estação A", "Estação B"};
    static final int HORARIO_INICIO = 5 * 60;   // 05:00 em minutos
    static final int HORARIO_FIM = 23 * 60;     // 23:00 em minutos

    Map<String, Zona> zonas = new HashMap<>();
    Map<String, CaminhaoPequeno> caminhoes = new HashMap<>();
    EstacaoTransferencia estacaoA;
    EstacaoTransferencia estacaoB;
    int totalLixoColetado = 0;
    int totalViagensRealizadas = 0;
    int tempoTotalEsperadoEstacaoA = 0;
    int tempoTotalEsperadoEstacaoB = 0;

    Map<String, Integer> lixoPorZona = new HashMap<>();
    Map<String, Integer> caminhaoPorZona = new HashMap<>();
    List<ViagemCaminhaoPequeno> viagensPequenos = new ArrayList<>();
    List<ViagemCaminhaoGrande> viagensGrandes = new ArrayList<>();

    Random rand = new Random();

    ColetaLixoFrame() {
        setTitle("Simulador de Coleta de Lixo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        estacaoA = new EstacaoTransferencia("Estação A", outputAreaLogs, outputAreaAterro);
        estacaoB = new EstacaoTransferencia("Estação B", outputAreaLogs, outputAreaAterro);

        for (int capacidade : CAMINHOES_CAPACIDADES) {
            for (int i = 1; i <= CAMINHOES_POR_TIPO; i++) {
                String nome = "Caminhão " + capacidade + "t #" + i;
                caminhoes.put(nome, new CaminhaoPequeno(nome, capacidade));
            }
        }

        JPanel inputPanel = new JPanel();
        inputPanel.add(realizarViagemButton);
        inputPanel.add(relatorioButton);
        inputPanel.add(reiniciarSimulacaoButton);
        inputPanel.add(exportarCsvButton);

        // Ajuste dos JTextAreas e JScrollPane
        outputAreaLogs.setEditable(false);
        outputAreaAterro.setEditable(false);
        outputAreaLogs.setFont(new Font("Monospaced", Font.PLAIN, 14));
        outputAreaAterro.setFont(new Font("Monospaced", Font.PLAIN, 14));
        outputAreaLogs.setLineWrap(true);
        outputAreaAterro.setLineWrap(true);
        outputAreaLogs.setWrapStyleWord(true);
        outputAreaAterro.setWrapStyleWord(true);

        JScrollPane scrollLogs = new JScrollPane(outputAreaLogs);
        JScrollPane scrollAterro = new JScrollPane(outputAreaAterro);
        scrollLogs.setPreferredSize(new Dimension(500, 500));
        scrollAterro.setPreferredSize(new Dimension(500, 500));

        JPanel outputPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        outputPanel.setPreferredSize(new Dimension(1020, 520));
        outputPanel.add(scrollLogs);
        outputPanel.add(scrollAterro);

        add(inputPanel, BorderLayout.NORTH);
        add(outputPanel, BorderLayout.CENTER);

        realizarViagemButton.setEnabled(true);
        relatorioButton.setEnabled(false);
        exportarCsvButton.setEnabled(false);

        realizarViagemButton.addActionListener(e -> {
            mostrarAnimacaoSimples();
            realizarTodasViagens();
        });
        relatorioButton.addActionListener(e -> mostrarRelatorio());
        reiniciarSimulacaoButton.addActionListener(e -> reiniciarSimulacao());
        exportarCsvButton.addActionListener(e -> exportarRelatorioCSV());

        setResizable(true);
        setPreferredSize(new Dimension(1050, 650));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    void mostrarAnimacaoSimples() {
        JDialog dialog = new JDialog(this, "Simulando...", true);
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        dialog.setLayout(new BorderLayout());
        dialog.add(new JLabel("Aguarde, realizando simulação..."), BorderLayout.NORTH);
        dialog.add(progressBar, BorderLayout.CENTER);
        dialog.setSize(350, 100);
        dialog.setLocationRelativeTo(this);

        new Thread(() -> {
            for (int i = 0; i <= 100; i += 5) {
                progressBar.setValue(i);
                try {
                    Thread.sleep(30);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            dialog.dispose();
        }).start();

        dialog.setVisible(true);
    }

    void realizarTodasViagens() {
        for (CaminhaoPequeno caminhao : caminhoes.values()) {
            int viagensRealizadas = 0;
            int cargaAtual = 0;
            List<String> zonasVisitadas = new ArrayList<>();
            while (viagensRealizadas < caminhao.maxViagensPermitidas && cargaAtual < caminhao.capacidade * 1000) {
                String nomeZona = NOMES_ZONAS[rand.nextInt(NOMES_ZONAS.length)];
                String nomeEstacao = NOMES_ESTACOES[rand.nextInt(NOMES_ESTACOES.length)];

                int horario = HORARIO_INICIO + rand.nextInt(HORARIO_FIM - HORARIO_INICIO + 1);
                boolean pico = ehHorarioPico(horario);

                int cargaRestante = caminhao.capacidade * 1000 - cargaAtual;
                int carga = rand.nextInt(Math.max(1, Math.min(caminhao.capacidade * 1000 - 500, cargaRestante))) + 500;
                if (carga > cargaRestante) carga = cargaRestante;

                cargaAtual += carga;
                viagensRealizadas++;
                caminhao.viagens++;

                zonasVisitadas.add(nomeZona);

                outputAreaLogs.append("\n🟩 Coleta na " + nomeZona + "\n");
                outputAreaLogs.append("🚛 " + caminhao.nome + " (" + caminhao.capacidade + "t) - Viagem "
                        + caminhao.viagens + ": coletou " + carga + " kg de lixo.\n");

                outputAreaLogs.append("🕒 Horário: " + formatarHora(horario) + (pico ? " (Pico)" : " (Fora Pico)") + "\n");

                int tempoViagem = calcularTempoViagem(caminhao, pico);
                outputAreaLogs.append("⏱️ Tempo de viagem: " + tempoViagem + " minutos.\n");

                EstacaoTransferencia estacao = nomeEstacao.equals("Estação A") ? estacaoA : estacaoB;
                int tempoEspera = 45;
                outputAreaLogs.append("⏳ " + caminhao.nome + " entrou na fila da " + nomeEstacao + " (espera 45min do caminhão grande)\n");
                if (estacao == estacaoA) {
                    tempoTotalEsperadoEstacaoA += tempoEspera;
                } else {
                    tempoTotalEsperadoEstacaoB += tempoEspera;
                }

                estacao.cargaAcumulada += carga;
                if (estacao.cargaAcumulada >= 20000) {
                    int horarioEnvio = horario + tempoEspera;
                    boolean envioPico = ehHorarioPico(horarioEnvio);
                    int tempoEnvioGrande = envioPico ? (150 + rand.nextInt(31)) : (90 + rand.nextInt(31));
                    viagensGrandes.add(new ViagemCaminhaoGrande(
                        estacao.nome, formatarHora(horarioEnvio), envioPico, tempoEnvioGrande, estacao.cargaAcumulada
                    ));
                    outputAreaAterro.append(
                        "\n==============================\n" +
                        " 🚚 " + estacao.nome + ": caminhão GRANDE enviado ao aterro às " +
                        formatarHora(horarioEnvio) + (envioPico ? " (Pico)" : " (Fora Pico)") +
                        ", tempo viagem: " + tempoEnvioGrande + " min, carga: " + estacao.cargaAcumulada + " kg\n" +
                        "==============================\n"
                    );
                    estacao.totalLixoEnviado += estacao.cargaAcumulada;
                    estacao.cargaAcumulada = 0;
                    estacao.caminhoesGrandesUsados++;
                }

                Zona zona = zonas.computeIfAbsent(nomeZona, Zona::new);
                zona.lixoTotal += carga;

                lixoPorZona.put(nomeZona, lixoPorZona.getOrDefault(nomeZona, 0) + carga);
                caminhaoPorZona.put(nomeZona, caminhaoPorZona.getOrDefault(nomeZona, 0) + 1);
                totalLixoColetado += carga;
                totalViagensRealizadas++;

                viagensPequenos.add(new ViagemCaminhaoPequeno(
                    caminhao.nome, nomeZona, nomeEstacao, formatarHora(horario), pico, carga, tempoViagem
                ));
            }

            outputAreaLogs.append("\n📝 Caminhão " + caminhao.nome + " ficou cheio após passar pelas zonas:\n");
            outputAreaLogs.append(String.join(" → ", zonasVisitadas) + "\n");
            outputAreaLogs.append("⚡ Carga final: " + cargaAtual + " kg\n");
            outputAreaLogs.append("-----------------------------------------------------------\n");
        }
        relatorioButton.setEnabled(true);
        realizarViagemButton.setEnabled(false);
        exportarCsvButton.setEnabled(true);
        JOptionPane.showMessageDialog(this, "✅ Todas as viagens foram realizadas!");
    }

    boolean ehHorarioPico(int minutos) {
        int[][] intervalos = {
            {6*60, 8*60},
            {12*60, 14*60},
            {17*60, 19*60}
        };
        for (int[] intervalo : intervalos) {
            if (minutos >= intervalo[0] && minutos < intervalo[1]) return true;
        }
        return false;
    }

    String formatarHora(int minutos) {
        int h = minutos / 60;
        int m = minutos % 60;
        return String.format("%02d:%02d", h, m);
    }

    int calcularTempoViagem(CaminhaoPequeno caminhao, boolean pico) {
        if (pico) {
            switch (caminhao.capacidade) {
                case 2: return rand.nextInt(14) + 16;
                case 4: return rand.nextInt(14) + 46;
                case 8: return rand.nextInt(14) + 76;
                case 10: return rand.nextInt(14) + 106;
            }
        } else {
            switch (caminhao.capacidade) {
                case 2: return rand.nextInt(16) + 10;
                case 4: return rand.nextInt(16) + 30;
                case 8: return rand.nextInt(16) + 60;
                case 10: return rand.nextInt(16) + 90;
            }
        }
        return 0;
    }

    void mostrarRelatorio() {
        StringBuilder sb = new StringBuilder();
        sb.append("📈 Relatório Completo\n");

        sb.append("\n⏱️ Tempo Médio de Espera nas Estações\n");
        int viagensPorEstacao = Math.max(1, totalViagensRealizadas / 2);
        sb.append("🟩 Estação A: ").append(tempoTotalEsperadoEstacaoA / viagensPorEstacao).append(" min\n");
        sb.append("🟩 Estação B: ").append(tempoTotalEsperadoEstacaoB / viagensPorEstacao).append(" min\n");

        sb.append("\n⚡ Lixo Gerado por Hora: ").append(totalLixoColetado / totalViagensRealizadas).append(" kg\n");

        sb.append("\n🔋 Quantidade de Lixo por Zona:\n");
        for (Map.Entry<String, Integer> entry : lixoPorZona.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(" kg\n");
        }

        sb.append("\n🔋 Quantidade de Caminhões Pequenos Enviados por Zona:\n");
        for (Map.Entry<String, Integer> entry : caminhaoPorZona.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(" caminhões\n");
        }

        int totalLixoEnviadoAterro = estacaoA.totalLixoEnviado + estacaoB.totalLixoEnviado;
        sb.append("\n🚛 Lixo Enviado para o Aterro: ").append(totalLixoEnviadoAterro).append(" kg\n");

        sb.append("\n🔋 Total de Lixo Coletado Em todas as Zonas: ")
            .append(lixoPorZona.values().stream().mapToInt(Integer::intValue).sum()).append(" kg\n");

        int totalLixoGerado = lixoPorZona.values().stream().mapToInt(Integer::intValue).sum();
        int caminhãoGrandeCapacidade = 20000;
        int caminhõesGrandesNecessarios = (int) Math.ceil((double) totalLixoGerado / caminhãoGrandeCapacidade);

        sb.append("\n📦 Quantos caminhões de 20 toneladas o município precisa?\n");
        sb.append("O município precisaria de Teresina ").append(caminhõesGrandesNecessarios)
                .append(" caminhões de 20 toneladas para atender à demanda total de lixo gerado.\n");

        sb.append("\n📝 Viagens dos Caminhões Pequenos (exemplo):\n");
        for (int i = 0; i < Math.min(10, viagensPequenos.size()); i++) {
            ViagemCaminhaoPequeno v = viagensPequenos.get(i);
            sb.append(v.caminhao).append(" | ")
              .append(v.zona).append(" | ")
              .append(v.estacao).append(" | ")
              .append(v.horario).append(" ")
              .append(v.pico ? "(Pico)" : "(Fora Pico)").append(" | ")
              .append(v.carga).append(" kg | ")
              .append(v.tempoViagem).append(" min\n");
        }

        sb.append("\n📝 Viagens dos Caminhões Grandes:\n");
        for (ViagemCaminhaoGrande v : viagensGrandes) {
            sb.append(v.estacao).append(" | ")
              .append(v.horarioEnvio).append(" ")
              .append(v.pico ? "(Pico)" : "(Fora Pico)").append(" | ")
              .append(v.tempoViagem).append(" min | ")
              .append(v.carga).append(" kg\n");
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(700, 500));

        JOptionPane.showMessageDialog(this, scrollPane, "Relatório Completo", JOptionPane.INFORMATION_MESSAGE);
    }

    void exportarRelatorioCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Relatório CSV");
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(fileToSave + ".csv")) {
                writer.append("Caminhão Pequeno;Zona;Estação;Horário;Pico;Carga (kg);Tempo Viagem (min)\n");
                for (ViagemCaminhaoPequeno v : viagensPequenos) {
                    writer.append(v.caminhao).append(";")
                          .append(v.zona).append(";")
                          .append(v.estacao).append(";")
                          .append(v.horario).append(";")
                          .append(v.pico ? "Pico" : "Fora Pico").append(";")
                          .append(String.valueOf(v.carga)).append(";")
                          .append(String.valueOf(v.tempoViagem)).append("\n");
                }
                writer.append("\n");
                writer.append("Caminhão Grande;Estação;Horário Envio;Pico;Tempo Viagem (min);Carga (kg)\n");
                for (ViagemCaminhaoGrande v : viagensGrandes) {
                    writer.append("Caminhão Grande").append(";")
                          .append(v.estacao).append(";")
                          .append(v.horarioEnvio).append(";")
                          .append(v.pico ? "Pico" : "Fora Pico").append(";")
                          .append(String.valueOf(v.tempoViagem)).append(";")
                          .append(String.valueOf(v.carga)).append("\n");
                }
                writer.append("\n");
                writer.append("Zona;Lixo Coletado (kg);Caminhões Enviados\n");
                for (String zona : lixoPorZona.keySet()) {
                    writer.append(zona).append(";")
                          .append(String.valueOf(lixoPorZona.get(zona))).append(";")
                          .append(String.valueOf(caminhaoPorZona.getOrDefault(zona, 0))).append("\n");
                }
                writer.append("\n");
                writer.append("Resumo Geral\n");
                writer.append("Total Lixo Coletado;").append(String.valueOf(totalLixoColetado)).append("\n");
                writer.append("Total Viagens Caminhões Pequenos;").append(String.valueOf(totalViagensRealizadas)).append("\n");
                writer.append("Total Lixo Enviado ao Aterro;").append(String.valueOf(estacaoA.totalLixoEnviado + estacaoB.totalLixoEnviado)).append("\n");

                JOptionPane.showMessageDialog(this, "Relatório exportado com sucesso!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao salvar arquivo: " + ex.getMessage());
            }
        }
    }

    void reiniciarSimulacao() {
        caminhoes.clear();
        zonas.clear();
        lixoPorZona.clear();
        caminhaoPorZona.clear();
        viagensPequenos.clear();
        viagensGrandes.clear();

        for (int capacidade : CAMINHOES_CAPACIDADES) {
            for (int i = 1; i <= CAMINHOES_POR_TIPO; i++) {
                String nome = "Caminhão " + capacidade + "t #" + i;
                caminhoes.put(nome, new CaminhaoPequeno(nome, capacidade));
            }
        }

        totalLixoColetado = 0;
        totalViagensRealizadas = 0;
        tempoTotalEsperadoEstacaoA = 0;
        tempoTotalEsperadoEstacaoB = 0;
        estacaoA.cargaAcumulada = 0;
        estacaoA.totalLixoEnviado = 0;
        estacaoA.caminhoesGrandesUsados = 0;
        estacaoB.cargaAcumulada = 0;
        estacaoB.totalLixoEnviado = 0;
        estacaoB.caminhoesGrandesUsados = 0;

        realizarViagemButton.setEnabled(true);
        relatorioButton.setEnabled(false);
        exportarCsvButton.setEnabled(false);

        outputAreaLogs.setText("");
        outputAreaAterro.setText("");

        JOptionPane.showMessageDialog(this, "✅ A simulação foi reiniciada!");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ColetaLixoFrame());
    }

    // Classes auxiliares para guardar informações das viagens
    static class ViagemCaminhaoPequeno {
        String caminhao, zona, estacao, horario;
        boolean pico;
        int carga, tempoViagem;
        ViagemCaminhaoPequeno(String caminhao, String zona, String estacao, String horario, boolean pico, int carga, int tempoViagem) {
            this.caminhao = caminhao;
            this.zona = zona;
            this.estacao = estacao;
            this.horario = horario;
            this.pico = pico;
            this.carga = carga;
            this.tempoViagem = tempoViagem;
        }
    }

    static class ViagemCaminhaoGrande {
        String estacao, horarioEnvio;
        boolean pico;
        int tempoViagem, carga;
        ViagemCaminhaoGrande(String estacao, String horarioEnvio, boolean pico, int tempoViagem, int carga) {
            this.estacao = estacao;
            this.horarioEnvio = horarioEnvio;
            this.pico = pico;
            this.tempoViagem = tempoViagem;
            this.carga = carga;
        }
    }
}

// Você ainda precisa das classes Zona, CaminhaoPequeno e EstacaoTransferencia! 
// Se quiser, posso gerar exemplos mínimos dessas classes.