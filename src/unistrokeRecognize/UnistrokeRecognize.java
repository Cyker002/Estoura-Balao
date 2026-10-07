package unistrokeRecognize;

import java.util.ArrayList;
import java.util.List;

/**
 * Reconhecedor de gestos unistroke baseado no algoritmo $1.
 * Adaptado para reconhecimento sensível à orientação (essencial para Magic Touch,
 * onde Linha Horizontal, Vertical, V e V Invertido têm significados diferentes).
 */
public class UnistrokeRecognize {
    private static final int NUM_POINTS = 64;
    private static final double SQUARE_SIZE = 250.0;
    private static final double ANGULAR_RANGE = Math.toRadians(35.0); // Tolerância para pequenos desvios de ângulo
    private static final double ANGULAR_PRECISION = Math.toRadians(2.0);
    private static final double PHI = 0.5 * (-1.0 + Math.sqrt(5.0)); // Razão áurea
    
    private List<Template> templates = new ArrayList<>();
    
    public UnistrokeRecognize() {
        carregarTemplatesPadrao();
    }
    
    public void addTemplate(String name, List<Point> points) {
        if (points != null && points.size() >= 2) {
            templates.add(new Template(name, points, NUM_POINTS, SQUARE_SIZE));
        }
    }
    
    public Result recognize(List<Point> rawPoints) {
        if (rawPoints == null || rawPoints.size() < 2) {
            return new Result("Desconhecido", 0.0);
        }
        
        // Normaliza o traço do jogador (sem rotacionar para zero para preservar orientação)
        List<Point> points = PreProcessing.resample(rawPoints, NUM_POINTS);
        points = PreProcessing.scaleTo(points, SQUARE_SIZE);
        points = PreProcessing.transalateTo(points, new Point(0, 0));
        
        double bestDistance = Double.MAX_VALUE;
        Template bestTemplate = null;
        
        // Compara com todos os templates usando busca de ângulo na faixa de tolerância
        for (Template template : templates) {
            double distance = distanceAtBestAngle(points, template, -ANGULAR_RANGE, ANGULAR_RANGE, ANGULAR_PRECISION);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestTemplate = template;
            }
        }
        
        if (bestTemplate == null) {
            return new Result("Desconhecido", 0.0);
        }
            
        // Converte a distância euclidiana média em um score de confiança entre 0.0 e 1.0
        double halfDiagonal = 0.5 * Math.sqrt(SQUARE_SIZE * SQUARE_SIZE + SQUARE_SIZE * SQUARE_SIZE);
        double score = 1.0 - (bestDistance / halfDiagonal);
        if (score < 0.0) {
            score = 0.0;
        }
            
        return new Result(bestTemplate.name, score);
    }
    
    private double distanceAtBestAngle(List<Point> points, Template template, double a, double b, double threshold) {
        double x1 = PHI * a + (1.0 - PHI) * b;
        double f1 = distanceAtAngle(points, template, x1);
        double x2 = (1.0 - PHI) * a + PHI * b;
        double f2 = distanceAtAngle(points, template, x2);
        
        while (Math.abs(b - a) > threshold) {
            if (f1 < f2) {
                b = x2;
                x2 = x1;
                f2 = f1;
                x1 = PHI * a + (1.0 - PHI) * b;
                f1 = distanceAtAngle(points, template, x1);
            } else {
                a = x1;
                x1 = x2;
                f1 = f2;
                x2 = (1.0 - PHI) * a + PHI * b;
                f2 = distanceAtAngle(points, template, x2);
            }
        }
        return Math.min(f1, f2);
    }
    
    private double distanceAtAngle(List<Point> points, Template template, double angle) {
        List<Point> rotated = PreProcessing.rotateBy(points, angle);
        return RecognizeUtils.pathDistance(rotated, template.points);
    }

    /**
     * Carrega os templates padrão para os símbolos do jogo:
     * LINHA_HORIZONTAL, LINHA_VERTICAL, V_NORMAL, V_INVERTIDO, CIRCULO, Z.
     * Inclui variações comuns de desenho (ex: desenhar linha da esquerda pra direita ou vice-versa).
     */
    public void carregarTemplatesPadrao() {
        templates.clear();

        // 1. LINHA_HORIZONTAL (esquerda -> direita e direita -> esquerda)
        addTemplate("LINHA_HORIZONTAL", List.of(
            new Point(0, 50), new Point(100, 50)
        ));
        addTemplate("LINHA_HORIZONTAL", List.of(
            new Point(100, 50), new Point(0, 50)
        ));

        // 2. LINHA_VERTICAL (cima -> baixo e baixo -> cima)
        addTemplate("LINHA_VERTICAL", List.of(
            new Point(50, 0), new Point(50, 100)
        ));
        addTemplate("LINHA_VERTICAL", List.of(
            new Point(50, 100), new Point(50, 0)
        ));

        // 3. V_NORMAL (desce até o bico e sobe)
        addTemplate("V_NORMAL", List.of(
            new Point(0, 0), new Point(50, 100), new Point(100, 0)
        ));

        // 4. V_INVERTIDO (sobe até a ponta e desce)
        addTemplate("V_INVERTIDO", List.of(
            new Point(0, 100), new Point(50, 0), new Point(100, 100)
        ));

        // 5. CIRCULO (suporta diferentes pontos de partida e sentidos)
        int[] angulosIniciais = { -90, 0, 90, 180 }; // Topo, Direita, Base, Esquerda
        for (int anguloInicio : angulosIniciais) {
            // Sentido horário
            List<Point> circuloHorario = new ArrayList<>();
            for (int i = 0; i <= 360; i += 15) {
                double rad = Math.toRadians(anguloInicio + i);
                circuloHorario.add(new Point(50 + 45 * Math.cos(rad), 50 + 45 * Math.sin(rad)));
            }
            addTemplate("CIRCULO", circuloHorario);

            // Sentido anti-horário
            List<Point> circuloAntiHorario = new ArrayList<>();
            for (int i = 360; i >= 0; i -= 15) {
                double rad = Math.toRadians(anguloInicio + i);
                circuloAntiHorario.add(new Point(50 + 45 * Math.cos(rad), 50 + 45 * Math.sin(rad)));
            }
            addTemplate("CIRCULO", circuloAntiHorario);
        }

        // 6. Z (traço superior horizontal, diagonal para esquerda inferior, base horizontal)
        addTemplate("Z", List.of(
            new Point(0, 0), new Point(100, 0), new Point(0, 100), new Point(100, 100)
        ));
    }
    
    // Classe DTO de resultado
    public static class Result {
        public String name;
        public double score;
        
        public Result(String name, double score) {
            this.name = name;
            this.score = score;
        }

        @Override
        public String toString() {
            return String.format("%s (%.2f%%)", name, score * 100);
        }
    }
}
