import javax.swing.*;
import java.awt.*;
import java.util.*;

class ColetaLixoFrame extends JFrame {
    // Componentes da interface
    JComboBox<String> zonaCombo = new JComboBox<>(new String[]{
            "Zona Sul", "Zona Norte", "Zona Centro", "Zona Leste", "Zona Sudeste"
    });

    JComboBox<String> caminhaoCombo = new JComboBox<>(new String[]{
            "Caminhão 1 (2t)", "Caminhão 2 (4t)", "Caminhão 3 (8t)", "Caminhão 4 (10t)"
    });

    JComboBox<String> estacaoCombo = new JComboBox<>(new String[]{"Estação A", "Estação B"});

    JComboBox<String> horarioCombo = new JComboBox<>(new String[]{"Fora de Pico", "Horário de Pico"});

    JTextArea outputAreaLogs = new JTextArea(20, 40);
    JTextArea outputAreaAterro = new JTextArea(20, 40);

    JButton realizarViagemButton = new JButton("Realizar Viagem");
    JButton relatorioButton = new JButton("Relatório Completo");
    JButton reiniciarSimulacaoButton = new JButton("Reiniciar Simulação");

    JButton botaoCadastrarCaminhao1 = new JButton("Cadastrar viagens Caminhão 1");
    JButton botaoCadastrarCaminhao2 = new JButton("Cadastrar viagens Caminhão 2");
    JButton botaoCadastrarCaminhao3 = new JButton("Cadastrar viagens Caminhão 3");
    JButton botaoCadastrarCaminhao4 = new JButton("Cadastrar viagens Caminhão 4");

    static final Stack<SimulacaoRegistro> historicoSimulacoes = new Stack<>();
    Map<String, Zona> zonas = new HashMap<>();
    Map<String, CaminhaoPequeno> caminhoes = new HashMap<>();

    EstacaoTransferencia estacaoA;
    EstacaoTransferencia estacaoB;

    int caminhoesCadastrados = 0;
    int tempoTotalEsperadoEstacaoA = 0;
    int tempoTotalEsperadoEstacaoB = 0;
    int totalLixoColetado = 0;
    int totalViagensRealizadas = 0;

    Map<String, Integer> lixoPorZona = new HashMap<>(); // Quantidade de lixo por zona
    Map<String, Integer> caminhaoPorZona = new HashMap<>(); // Quantidade de caminhões por zona

    ColetaLixoFrame() {
        setTitle("Simulador de Coleta de Lixo");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        estacaoA = new EstacaoTransferencia("Estação A", outputAreaLogs, outputAreaAterro);
        estacaoB = new EstacaoTransferencia("Estação B", outputAreaLogs, outputAreaAterro);

        // Painel para entrada de dados
        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Zona:"));
        inputPanel.add(zonaCombo);
        inputPanel.add(new JLabel("Caminhão:"));
        inputPanel.add(caminhaoCombo);
        inputPanel.add(new JLabel("Estação:"));
        inputPanel.add(estacaoCombo);
        inputPanel.add(new JLabel("Horário:"));
        inputPanel.add(horarioCombo);
        inputPanel.add(realizarViagemButton);
        inputPanel.add(relatorioButton);
        inputPanel.add(reiniciarSimulacaoButton); // Botão de reiniciar a simulação

        // Painel para cadastro de caminhões pequenos
        JPanel panelCadastro = new JPanel(new FlowLayout());
        panelCadastro.add(botaoCadastrarCaminhao1);
        panelCadastro.add(botaoCadastrarCaminhao2);
        panelCadastro.add(botaoCadastrarCaminhao3);
        panelCadastro.add(botaoCadastrarCaminhao4);

        // Painel para saída de dados
        JPanel outputPanel = new JPanel(new GridLayout(1, 2));
        outputAreaLogs.setEditable(false);
        outputAreaAterro.setEditable(false);
        outputPanel.add(new JScrollPane(outputAreaLogs));
        outputPanel.add(new JScrollPane(outputAreaAterro));

        // Adiciona os painéis na interface
        add(panelCadastro, BorderLayout.SOUTH);
        add(inputPanel, BorderLayout.NORTH);
        add(outputPanel, BorderLayout.CENTER);

        realizarViagemButton.setEnabled(false);
        relatorioButton.setEnabled(false);

        // Cadastra os caminhões pequenos
        caminhoes.put("Caminhão 1", new CaminhaoPequeno("Caminhão 1", 2));
        caminhoes.put("Caminhão 2", new CaminhaoPequeno("Caminhão 2", 4));
        caminhoes.put("Caminhão 3", new CaminhaoPequeno("Caminhão 3", 8));
        caminhoes.put("Caminhão 4", new CaminhaoPequeno("Caminhão 4", 10));

        // Ações dos botões
        botaoCadastrarCaminhao1.addActionListener(e -> cadastrarViagens("Caminhão 1", botaoCadastrarCaminhao1));
        botaoCadastrarCaminhao2.addActionListener(e -> cadastrarViagens("Caminhão 2", botaoCadastrarCaminhao2));
        botaoCadastrarCaminhao3.addActionListener(e -> cadastrarViagens("Caminhão 3", botaoCadastrarCaminhao3));
        botaoCadastrarCaminhao4.addActionListener(e -> cadastrarViagens("Caminhão 4", botaoCadastrarCaminhao4));

        realizarViagemButton.addActionListener(e -> realizarViagem());
        relatorioButton.addActionListener(e -> mostrarRelatorio());
        reiniciarSimulacaoButton.addActionListener(e -> reiniciarSimulacao()); // Ação para reiniciar a simulação

        // Impedir maximização da janela
        setResizable(false);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    void cadastrarViagens(String nomeCaminhao, JButton botao) {
        String input = JOptionPane.showInputDialog(this,
                "Quantas viagens diárias o " + nomeCaminhao + " pode fazer? (máx 50)", "Cadastro de Viagens",
                JOptionPane.QUESTION_MESSAGE);
        try {
            int maxViagens = Integer.parseInt(input);
            if (maxViagens < 1 || maxViagens > 50) throw new NumberFormatException();
            caminhoes.get(nomeCaminhao).maxViagensPermitidas = maxViagens;
            botao.setEnabled(false);
            caminhoesCadastrados++;
            if (caminhoesCadastrados == 4) {
                realizarViagemButton.setEnabled(true);
                JOptionPane.showMessageDialog(this, "✅ Todos os caminhões foram cadastrados com sucesso!");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Entrada inválida! Digite um número entre 1 e 50.");
        }
    }

    void realizarViagem() {
// Mostrar animação da viagem

        // Não limpar o histórico de viagens
        String nomeZona = (String) zonaCombo.getSelectedItem();
        String selecaoCaminhao = (String) caminhaoCombo.getSelectedItem();
        String nomeEstacao = (String) estacaoCombo.getSelectedItem();
        String horarioSelecionado = (String) horarioCombo.getSelectedItem();
        String nomeCaminhao = selecaoCaminhao.split(" ")[0] + " " + selecaoCaminhao.split(" ")[1];
        CaminhaoPequeno caminhao = caminhoes.get(nomeCaminhao);
        if (caminhao.viagens >= caminhao.maxViagensPermitidas) {
            outputAreaLogs.append("\n🚫 " + nomeCaminhao + " já atingiu o limite de " + caminhao.maxViagensPermitidas + " viagens diárias.\n");
            JOptionPane.showMessageDialog(this,
                nomeCaminhao + " já atingiu o limite de viagens diárias!",
                "Limite Atingido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TelaCarregamento tela = new TelaCarregamento(this, nomeZona, nomeEstacao);
        tela.iniciarAnimacao();

        // Define o tempo de viagem com base no horário
        int tempoViagem = calcularTempoViagem(caminhao, horarioSelecionado);

        int carga = new Random().nextInt(caminhao.capacidade * 1000 - 500 + 1) + 500;
        caminhao.viagens++;

        outputAreaLogs.append("\n🟩 Iniciando coleta na " + nomeZona + "\n");
        outputAreaLogs.append("🚛 " + nomeCaminhao + " (" + caminhao.capacidade + "t) - Viagem "
                + caminhao.viagens + ": coletou " + carga + " kg de lixo.\n");
        outputAreaLogs.append("⏱️ Tempo de viagem: " + tempoViagem + " minutos.\n");

        EstacaoTransferencia estacao = nomeEstacao.equals("Estação A") ? estacaoA : estacaoB;
        int tempoEspera = estacao.receberCarga(caminhao, carga);
        if (estacao == estacaoA) {
            tempoTotalEsperadoEstacaoA += tempoEspera;
        } else {
            tempoTotalEsperadoEstacaoB += tempoEspera;
        }
        estacao.verificarEnvio();

        Zona zona = zonas.computeIfAbsent(nomeZona, Zona::new);
        zona.lixoTotal += carga;

        // Atualiza os dados para o relatório
        lixoPorZona.put(nomeZona, lixoPorZona.getOrDefault(nomeZona, 0) + carga);
        caminhaoPorZona.put(nomeZona, caminhaoPorZona.getOrDefault(nomeZona, 0) + 1);

        totalLixoColetado += carga;
        totalViagensRealizadas++;
        if (!relatorioButton.isEnabled()) {
        relatorioButton.setEnabled(true);
    }

        // Se todas as viagens foram completadas, aciona o relatório
        if (totalViagensRealizadas == caminhoes.values().stream().mapToInt(c -> c.maxViagensPermitidas).sum()) {
            relatorioButton.doClick();
        }
    }

    // Cálculo do tempo de viagem
    int calcularTempoViagem(CaminhaoPequeno caminhao, String horarioSelecionado) {
        Random rand = new Random();
        int tempo = 0;

        switch (caminhao.capacidade) {
            case 2:
                tempo = horarioSelecionado.equals("Horário de Pico") ? rand.nextInt(14) + 16 : rand.nextInt(16) + 10;
                break;
            case 4:
                tempo = horarioSelecionado.equals("Horário de Pico") ? rand.nextInt(14) + 46 : rand.nextInt(16) + 30;
                break;
            case 8:
                tempo = horarioSelecionado.equals("Horário de Pico") ? rand.nextInt(14) + 76 : rand.nextInt(16) + 60;
                break;
            case 10:
                tempo = horarioSelecionado.equals("Horário de Pico") ? rand.nextInt(14) + 106 : rand.nextInt(16) + 90;
                break;
        }
        return tempo;
    }

    // Relatório de todos os dados
void mostrarRelatorio() {
    StringBuilder sb = new StringBuilder();
    sb.append("📈 Relatório Completo\n");

    // Tempo Médio de Espera nas Estações
    sb.append("\n⏱️ Tempo Médio de Espera nas Estações\n");
    int viagensPorEstacao = Math.max(1, totalViagensRealizadas / 2); // Evita divisão por zero
    int tempoMedioEsperadoEstacaoA = tempoTotalEsperadoEstacaoA / viagensPorEstacao;
    int tempoMedioEsperadoEstacaoB = tempoTotalEsperadoEstacaoB / viagensPorEstacao;
    sb.append("🟩 Estação A: ").append(tempoMedioEsperadoEstacaoA).append(" min\n");
    sb.append("🟩 Estação B: ").append(tempoMedioEsperadoEstacaoB).append(" min\n");

    sb.append("\n⚡ Lixo Gerado por Hora: ").append(totalLixoColetado / totalViagensRealizadas).append(" kg\n");

    // Quantidade de Lixo por Zona
    sb.append("\n🔋 Quantidade de Lixo por Zona:\n");
    for (Map.Entry<String, Integer> entry : lixoPorZona.entrySet()) {
        sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(" kg\n");
    }

    // Quantidade de Caminhões Grandes Enviados por Zona
    sb.append("\n🔋 Quantidade de Caminhões Pequenos Enviados por Zona:\n");
    for (Map.Entry<String, Integer> entry : caminhaoPorZona.entrySet()) {
        sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(" caminhões\n");
    }

    // Agora, vamos adicionar a quantidade de lixo enviado para o aterro
    int totalLixoEnviadoAterro = 0;
    for (EstacaoTransferencia estacao : Arrays.asList(estacaoA, estacaoB)) {
        totalLixoEnviadoAterro += estacao.getTotalLixoEnviado(); // Supondo que a EstacaoTransferencia tenha um método que retorna o lixo enviado.
    }

    sb.append("\n🚛 Lixo Enviado para o Aterro: ").append(totalLixoEnviadoAterro).append(" kg\n");

    // Quantidade total de lixo coletado nas zonas
    int totalLixoColetadoZonas = lixoPorZona.values().stream().mapToInt(Integer::intValue).sum();
    sb.append("\n🔋 Total de Lixo Coletado Em todas as Zonas: ").append(totalLixoColetadoZonas).append(" kg\n");

    // Lixo total gerado e caminhões grandes necessários
    int totalLixoGerado = lixoPorZona.values().stream().mapToInt(Integer::intValue).sum();
    int caminhãoGrandeCapacidade = 20000; // 20 toneladas
    int caminhõesGrandesNecessarios = (int) Math.ceil((double) totalLixoGerado / caminhãoGrandeCapacidade);

    sb.append("\n📦 Quantos caminhões de 20 toneladas o município precisa?\n");
    sb.append("O município precisaria de Teresina ").append(caminhõesGrandesNecessarios)
            .append(" caminhões de 20 toneladas para atender à demanda total de lixo gerado.\n    ");


    JOptionPane.showMessageDialog(this, sb.toString());
    }

    // Reinicia a simulação
    void reiniciarSimulacao() {
        // Limpar os dados
        caminhoes.clear();
        zonas.clear();
        lixoPorZona.clear();
        caminhaoPorZona.clear();

        caminhoes.put("Caminhão 1", new CaminhaoPequeno("Caminhão 1", 2));
        caminhoes.put("Caminhão 2", new CaminhaoPequeno("Caminhão 2", 4));
        caminhoes.put("Caminhão 3", new CaminhaoPequeno("Caminhão 3", 8));
        caminhoes.put("Caminhão 4", new CaminhaoPequeno("Caminhão 4", 10));

        caminhoesCadastrados = 0;
        totalLixoColetado = 0;
        totalViagensRealizadas = 0;
        tempoTotalEsperadoEstacaoA = 0;
        tempoTotalEsperadoEstacaoB = 0;

        // Reabilitar botões e campos
        realizarViagemButton.setEnabled(false);
        relatorioButton.setEnabled(false);
        botaoCadastrarCaminhao1.setEnabled(true);
        botaoCadastrarCaminhao2.setEnabled(true);
        botaoCadastrarCaminhao3.setEnabled(true);
        botaoCadastrarCaminhao4.setEnabled(true);

        outputAreaLogs.setText("");
        outputAreaAterro.setText("");
        
        JOptionPane.showMessageDialog(this, "✅ A simulação foi reiniciada!");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ColetaLixoFrame());
    }
}
