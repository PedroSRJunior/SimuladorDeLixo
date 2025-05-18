import javax.swing.*;
import java.awt.*;

public class TelaCarregamento extends JDialog {
    private int x = 40;
    private Timer timer;
    private final String zona;
    private final String estacao;

    public TelaCarregamento(JFrame parent, String zona, String estacao) {
        super(parent, "Simulando Viagem de Caminhão Pequeno", true);
        this.zona = zona;
        this.estacao = estacao;

        setSize(600, 250);
        setLocationRelativeTo(parent);
        setUndecorated(false); // Pode colocar true para sem borda
        setResizable(false);

        JPanel animacaoPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Fundo
                setBackground(new Color(245, 245, 245));
                g.setColor(Color.DARK_GRAY);
                g.setFont(new Font("SansSerif", Font.BOLD, 20));

                // Título e Informações
                g.drawString("Simulando coleta de lixo...", 160, 30);
                g.setFont(new Font("SansSerif", Font.PLAIN, 16));
                g.drawString("Zona de origem: " + TelaCarregamento.this.zona, 40, 60);
                g.drawString("Destino: " + TelaCarregamento.this.estacao, 40, 85);

                // Linha de trajeto
                g.setColor(Color.GRAY);
                g.drawLine(40, 140, 520, 140);

                // Ícones fixos nas pontas
                g.setFont(new Font("SansSerif", Font.PLAIN, 26));
                g.drawString("🏘️", 20, 135);  // zona
                g.drawString("🏭", 530, 135); // estação

                // Caminhão em movimento
                g.drawString("🚛", x, 135);
            }
        };

        timer = new Timer(20, e -> {
            x += 4;
            if (x >= 500) {
                timer.stop();
                dispose();
            }
            animacaoPanel.repaint();
        });

        add(animacaoPanel);
    }

    public void iniciarAnimacao() {
        timer.start();
        setVisible(true);
    }
}
