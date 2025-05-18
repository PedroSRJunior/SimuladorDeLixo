import javax.swing.*;
import java.awt.*;

public class TelaCarregamentoGrande extends JDialog {
    private int x = 500;
    private Timer timer;
    private final String estacao;

    public TelaCarregamentoGrande(JFrame parent, String estacao) {
        super(parent, "Enviando Caminhão Grande para o Aterro", true);
        this.estacao = estacao;

        setSize(600, 200);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel animacaoPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                setBackground(new Color(240, 240, 240));
                g.setColor(Color.DARK_GRAY);
                g.setFont(new Font("SansSerif", Font.BOLD, 20));

                g.drawString("Simulando envio ao aterro...", 150, 30);
                g.setFont(new Font("SansSerif", Font.PLAIN, 16));
                g.drawString("Origem: " + TelaCarregamentoGrande.this.estacao, 40, 60);
                g.drawString("Destino: Aterro Sanitário", 40, 85);

                g.setColor(Color.GRAY);
                g.drawLine(40, 130, 520, 130);

                g.setFont(new Font("SansSerif", Font.PLAIN, 26));
                g.drawString("🗑️", 20, 125);  // aterro
                g.drawString("🏭", 530, 125); // estação

                g.drawString("🚚", x, 125); // caminhão grande indo para a esquerda
            }
        };

        timer = new Timer(20, e -> {
            x -= 4;
            if (x <= 40) {
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