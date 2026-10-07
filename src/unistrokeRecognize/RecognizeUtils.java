package unistrokeRecognize;

import java.util.List;

/**
 * Utilitários de cálculo para o Unistroke Recognizer.
 */
public class RecognizeUtils {
    
    // Calcula a distância total do traço
    public static double pathLength(List<Point> p) {
        if (p == null || p.size() < 2) {
            return 0.0;
        }
        double d = 0;
        for (int i = 1; i < p.size(); i++) {
            d += p.get(i - 1).distance(p.get(i));
        }
        return d;
    }
    
    // Calcula o centro de massa (centroide) do traço
    public static Point centroid(List<Point> points) {
        if (points == null || points.isEmpty()) {
            return new Point(0, 0);
        }
        double x = 0;
        double y = 0;
        for (Point p : points) {
            x += p.x;
            y += p.y;
        }
        return new Point(x / points.size(), y / points.size());
    }
    
    // Calcula a distância média entre dois traços com o mesmo tamanho N
    public static double pathDistance(List<Point> pts1, List<Point> pts2) {
        if (pts1 == null || pts2 == null || pts1.isEmpty() || pts2.isEmpty()) {
            return Double.MAX_VALUE;
        }
        double d = 0;
        int n = Math.min(pts1.size(), pts2.size());
        for (int i = 0; i < n; i++) {
            d += pts1.get(i).distance(pts2.get(i));
        }
        return d / n;
    }
}
