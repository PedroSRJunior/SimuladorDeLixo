import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Random; 

public class ColetaLixoFrame extends JFrame {
    JTextArea outputAreaLogs = new JTextArea(20, 40);
    JTextArea outputAreaAterro = new JTextArea(20, 40);

    JButton realizarViagemButton = new JButton("Realizar TODAS as Viagens");
    JButton relatorioButton = new JButton("Relatório Completo");
    JButton reiniciarSimulacaoButton = new JButton("Reiniciar Simulação");
    JButton exportarCsvButton = new JButton("Exportar Relatório CSV");

    static final int CAMINHOES_POR_TIPO = 10;
    static final int[] CAMINHOES_CAPACIDADES = {2, 4, 8, 10};
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
    private List<ViagemCaminhaoPequeno> viagensPequenos = new ArrayList<>();
    private List<ViagemCaminhaoGrande> viagensGrandes = new ArrayList<>();
    private Map<String, List<String>> zonasPorCaminhao = new HashMap<>();

    Random rand = new Random();

    public ColetaLixoFrame() {
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
        int contadorCaminhaoGrandeA = 1;
        int contadorCaminhaoGrandeB = 1;
        for (CaminhaoPequeno caminhao : caminhoes.values()) {
            int viagensRealizadas = 0;
            int cargaAtual = 0;
            int limiteViagens = 6 + rand.nextInt(5); // 6 a 10 viagens por dia
            List<String> zonasVisitadas = new ArrayList<>();
            while (viagensRealizadas < limiteViagens) {
                // SORTEIO DE ZONA E ESTAÇÃO DE ACORDO COM A NOVA REGRA:
                String nomeEstacao;
                String nomeZona;
                if (rand.nextBoolean()) {
                    nomeEstacao = "Estação A";
                    String[] zonasA = {"Zona Centro", "Zona Norte", "Zona Leste"};
                    nomeZona = zonasA[rand.nextInt(zonasA.length)];
                } else {
                    nomeEstacao = "Estação B";
                    String[] zonasB = {"Zona Sul", "Zona Sudeste"};
                    nomeZona = zonasB[rand.nextInt(zonasB.length)];
                }

                int horario = HORARIO_INICIO + rand.nextInt(HORARIO_FIM - HORARIO_INICIO + 1);
                boolean pico = SimulacaoUtils.ehHorarioPico(horario);

                int carga = rand.nextInt(caminhao.capacidade * 1000 - 499) + 500;
                cargaAtual += carga;
                viagensRealizadas++;
                caminhao.viagens++;

                zonasVisitadas.add(nomeZona);

                outputAreaLogs.append("🟩 Coleta na " + nomeZona + "\n");
                outputAreaLogs.append("🚛 " + caminhao.nome + " (" + caminhao.capacidade + "t) - Viagem "
                        + caminhao.viagens + ": coletou " + carga + " kg de lixo.\n");
                outputAreaLogs.append("🕒 Horário: " + SimulacaoUtils.formatarHora(horario) + (pico ? " (Pico)" : " (Fora Pico)") + "\n");
                outputAreaLogs.append("⏱️ Tempo de viagem: " + SimulacaoUtils.calcularTempoViagem(caminhao, pico) + " minutos.\n");
                outputAreaLogs.append("⏳ " + caminhao.nome + " entrou na fila da " + nomeEstacao + " (espera 45min do caminhão grande)\n\n");

                EstacaoTransferencia estacao = nomeEstacao.equals("Estação A") ? estacaoA : estacaoB;
                int tempoEspera = 45;
                if (estacao == estacaoA) {
                    tempoTotalEsperadoEstacaoA += tempoEspera;
                } else {
                    tempoTotalEsperadoEstacaoB += tempoEspera;
                }

                estacao.cargaAcumulada += carga;
                if (estacao.cargaAcumulada >= 20000) {
                    int horarioEnvio = horario + tempoEspera;
                    boolean envioPico = SimulacaoUtils.ehHorarioPico(horarioEnvio);
                    int tempoEnvioGrande = envioPico
                            ? (150 + rand.nextInt(31))    // 2:30h a 3:00h
                            : (90 + rand.nextInt(31));    // 1:30h a 2:00h
                    viagensGrandes.add(new ViagemCaminhaoGrande(
                        estacao.nome, SimulacaoUtils.formatarHora(horarioEnvio), envioPico, tempoEnvioGrande, estacao.cargaAcumulada
                    ));
                    if (estacao == estacaoA) {
                        outputAreaAterro.append(
                            "==============================\n" +
                            " 🚚 " + estacao.nome + ": caminhão GRANDE #" + contadorCaminhaoGrandeA + " enviado ao aterro\n" +
                            "==============================\n"
                        );
                        contadorCaminhaoGrandeA++;
                    } else {
                        outputAreaAterro.append(
                            "==============================\n" +
                            " 🚚 " + estacao.nome + ": caminhão GRANDE #" + contadorCaminhaoGrandeB + " enviado ao aterro\n" +
                            "==============================\n"
                        );
                        contadorCaminhaoGrandeB++;
                    }
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
                    caminhao.nome, nomeZona, nomeEstacao, SimulacaoUtils.formatarHora(horario), pico, carga, SimulacaoUtils.calcularTempoViagem(caminhao, pico)
                ));
            }

            outputAreaLogs.append("📝 Caminhão " + caminhao.nome + " finalizou após passar pelas zonas:\n");
            outputAreaLogs.append(String.join(" → ", zonasVisitadas) + "\n");
            outputAreaLogs.append("⚡ Carga total acumulada: " + cargaAtual + " kg\n");
            outputAreaLogs.append("==============================\n\n");

            zonasPorCaminhao.put(caminhao.nome, new ArrayList<>(zonasVisitadas));
        }
        relatorioButton.setEnabled(true);
        realizarViagemButton.setEnabled(false);
        exportarCsvButton.setEnabled(true);
        JOptionPane.showMessageDialog(this, "✅ Todas as viagens foram realizadas!");
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

        // Viagens dos Caminhões Grandes
        sb.append("\n📝 Viagens dos Caminhões Grandes:\n");
        sb.append("==========================================\n");
        int contadorCaminhaoGrande = 1;
        int totalGrandes = viagensGrandes.size();
        for (ViagemCaminhaoGrande v : viagensGrandes) {
            sb.append("Caminhão Grande #").append(contadorCaminhaoGrande)
              .append(" | Saiu da ").append(v.estacao)
              .append(" | Horário que saiu: ").append(v.horarioEnvio).append(" ").append(v.pico ? "(Pico)" : "(Fora Pico)")
              .append(" |  Tempo para chegar no Aterro:  ").append(v.tempoViagem).append(" min |\n");
            if (contadorCaminhaoGrande < totalGrandes) {
                sb.append("------------------------------------------------------------------------\n");
            }
            contadorCaminhaoGrande++;
        }

        // Caminhão Pequeno e as Zonas que percorreu (com separador entre caminhões de mesma capacidade)
        sb.append("\n==============================\n");
        sb.append("\n📍 Caminhão Pequeno e as Zonas que percorreu:\n");
        sb.append("==========================================\n");
        List<String> nomesOrdenados = new ArrayList<>(zonasPorCaminhao.keySet());
        Collections.sort(nomesOrdenados, (a, b) -> {
            int capaA = extrairCapacidade(a);
            int capaB = extrairCapacidade(b);
            int numA = extrairNumero(a);
            int numB = extrairNumero(b);
            if (capaA != capaB) return capaB - capaA;
            return numA - numB;
        });

        String lastCapacidade = "";
        for (int idx = 0; idx < nomesOrdenados.size(); idx++) {
            String nomeCaminhao = nomesOrdenados.get(idx);
            String capacidadeAtual = "";
            try {
                capacidadeAtual = nomeCaminhao.split(" ")[1];
            } catch (Exception e) {
                capacidadeAtual = "";
            }

            if (!lastCapacidade.isEmpty() && !capacidadeAtual.equals(lastCapacidade)) {
                sb.append("\n==========================================\n\n");
            }
            lastCapacidade = capacidadeAtual;

            List<String> zonas = zonasPorCaminhao.get(nomeCaminhao);
            sb.append("🚛 ").append(nomeCaminhao).append(": ");
            if (zonas != null && !zonas.isEmpty()) {
                sb.append(String.join(" → ", zonas));
            } else {
                sb.append("(nenhuma zona percorrida)");
            }
            sb.append("\n");

            boolean proximoEhMesmaCapacidade = (idx + 1 < nomesOrdenados.size())
                    && getCapacidade(nomesOrdenados.get(idx + 1)).equals(capacidadeAtual);
            if (proximoEhMesmaCapacidade) {
                sb.append("------------------------------------------------------------------------\n");
            }
        }
        sb.append("==========================================\n");

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(700, 500));

        JOptionPane.showMessageDialog(this, scrollPane, "Relatório Completo", JOptionPane.INFORMATION_MESSAGE);
    }

    // Auxiliares robustos para extração
    private int extrairCapacidade(String nome) {
        try {
            for (String parte : nome.split(" ")) {
                if (parte.endsWith("t")) {
                    return Integer.parseInt(parte.replace("t", ""));
                }
            }
        } catch (Exception e) {}
        return 0;
    }

    private int extrairNumero(String nome) {
        try {
            int idx = nome.indexOf('#');
            if (idx >= 0) {
                String num = nome.substring(idx + 1).trim();
                return Integer.parseInt(num);
            }
        } catch (Exception e) {}
        return 0;
    }

    private String getCapacidade(String nome) {
        try {
            return nome.split(" ")[1];
        } catch (Exception e) {
            return "";
        }
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
                writer.append("Caminhão Grande;Saiu da;Horário que saiu;Pico;Tempo para chegar no Aterro (min);Carga (kg)\n");
                int contadorCaminhaoGrande = 1;
                for (ViagemCaminhaoGrande v : viagensGrandes) {
                    writer.append("Caminhão Grande #").append(String.valueOf(contadorCaminhaoGrande++)).append(";")
                          .append(v.estacao).append(";")
                          .append(v.horarioEnvio).append(" ").append(v.pico ? "(Pico)" : "(Fora Pico)").append(";")
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
        zonasPorCaminhao.clear();

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
}