public class SimulacaoUtils {
    // Horário de pico: 06:00–08:00, 12:00–14:00, 17:00–19:00
    public static boolean ehHorarioPico(int minutos) {
        if (minutos >= 6*60 && minutos < 8*60) return true;
        if (minutos >= 12*60 && minutos < 14*60) return true;
        if (minutos >= 17*60 && minutos < 19*60) return true;
        return false;
    }

    public static String formatarHora(int minutos) {
        return String.format("%02d:%02d", minutos/60, minutos%60);
    }

    public static int calcularTempoViagem(CaminhaoPequeno caminhao, boolean pico) {
        // Você pode customizar o cálculo com base nas regras do seu projeto.
        return pico
            ? (20 + caminhao.capacidade * 2 + new java.util.Random().nextInt(15)) // Exemplo: aumenta tempo no pico
            : (10 + caminhao.capacidade * 2 + new java.util.Random().nextInt(10));
    }
}