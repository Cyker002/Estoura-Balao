package unistrokeRecognize;

import java.util.ArrayList;
import java.util.List;
import static unistrokeRecognize.RecognizeUtils.*;

/**
 * Passos de pré-processamento do $1 Unistroke Recognizer:
 * 1. Resample (Reamostragem para N pontos equidistantes)
 * 2. Rotate (Opcional dependendo da sensibilidade à rotação)
 * 3. Scale (Escalonamento para uma caixa de tamanho padrão)
 * 4. Translate (Translação para a origem 0,0)
 */
public class PreProcessing {
    
    // Passo 1: Reamostragem para garantir N pontos uniformemente espaçados
    public static List<Point> resample(List<Point> points, int n) {
        if (points == null || points.isEmpty()) {
            return new ArrayList<>();
        }
        if (points.size() == 1) {
            List<Point> rep = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                rep.add(new Point(points.get(0).x, points.get(0).y));
            }
            return rep;
        }

        double interval = pathLength(points) / (n - 1);
        if (interval <= 0) {
            List<Point> rep = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                rep.add(new Point(points.get(0).x, points.get(0).y));
            }
            return rep;
        }

        double D = 0.0;
        List<Point> newPoints = new ArrayList<>();
        List<Point> srcPoints = new ArrayList<>(points);
        newPoints.add(new Point(srcPoints.get(0).x, srcPoints.get(0).y));
        
        for (int i = 1; i < srcPoints.size(); i++) {
            Point p1 = srcPoints.get(i - 1);
            Point p2 = srcPoints.get(i);
            double d = p1.distance(p2);
            
            if ((D + d) >= interval) {
                double qx = p1.x + ((interval - D) / d) * (p2.x - p1.x);
                double qy = p1.y + ((interval - D) / d) * (p2.y - p1.y); // Corrigido p2.y - p1.y
                Point q = new Point(qx, qy);
                newPoints.add(q);
                srcPoints.add(i, q);
                D = 0.0;
            } else {
                D += d;
            }
        }
        
        // Garante que o resultado tenha exatamente n pontos
        Point last = points.get(points.size() - 1);
        while (newPoints.size() < n) {
            newPoints.add(new Point(last.x, last.y));
        }
        if (newPoints.size() > n) {
            newPoints = new ArrayList<>(newPoints.subList(0, n));
        }
        
        return newPoints;
    }
    
    // Rotaciona pontos ao redor do centroide por um dado ângulo em radianos
    public static List<Point> rotateBy(List<Point> points, double radians) {
        Point c = centroid(points);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        
        List<Point> newPoints = new ArrayList<>();
        for (Point p : points) {
            double dx = p.x - c.x;
            double dy = p.y - c.y;
            
            double qx = dx * cos - dy * sin + c.x;
            double qy = dx * sin + dy * cos + c.y;
            newPoints.add(new Point(qx, qy));
        }
        
        return newPoints;
    }
    
    public static List<Point> rotateToZero(List<Point> points) {
        if (points == null || points.isEmpty()) {
            return new ArrayList<>();
        }
        Point c = centroid(points);
        double theta = Math.atan2(c.y - points.get(0).y, c.x - points.get(0).x);
        return rotateBy(points, -theta);
    }
    
    // Passo 3: Escalonar para uma caixa de dimensão 'size'
    // Se o traço for quase unidimensional (linha), escalona proporcionalmente
    public static List<Point> scaleTo(List<Point> points, double size) {
        if (points == null || points.isEmpty()) {
            return new ArrayList<>();
        }
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        
        for (Point p : points) {
            minX = Math.min(minX, p.x);
            minY = Math.min(minY, p.y);
            maxX = Math.max(maxX, p.x);
            maxY = Math.max(maxY, p.y);
        }
        
        double width = maxX - minX;
        double height = maxY - minY;
        double maxDim = Math.max(width, height);
        double minDim = Math.min(width, height);
        boolean is1D = maxDim > 0 && (minDim / maxDim) < 0.25;
        
        List<Point> newPoints = new ArrayList<>();
        for (Point p : points) {
            double qx;
            double qy;
            if (is1D) {
                qx = (maxDim > 0) ? p.x * (size / maxDim) : p.x;
                qy = (maxDim > 0) ? p.y * (size / maxDim) : p.y;
            } else {
                qx = (width > 0) ? p.x * (size / width) : p.x;
                qy = (height > 0) ? p.y * (size / height) : p.y;
            }
            newPoints.add(new Point(qx, qy));
        }
        
        return newPoints;
    }
    
    // Passo 4: Transladar para a origem (ou ponto pt)
    public static List<Point> transalateTo(List<Point> points, Point pt) {
        if (points == null || points.isEmpty()) {
            return new ArrayList<>();
        }
        Point c = centroid(points);
        List<Point> newPoints = new ArrayList<>();
        for (Point p : points) {
            newPoints.add(new Point(p.x + pt.x - c.x, p.y + pt.y - c.y));
        }
        return newPoints;
    }
}
