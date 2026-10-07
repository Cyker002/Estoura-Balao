package unistrokeRecognize;

import java.util.List;

/**
 * Representa um template de gesto pré-processado para reconhecimento.
 */
public class Template {
    public String name;
    public List<Point> points;
    
    public Template(String name, List<Point> rawPoints, int n, double size) {
        this.name = name;
        List<Point> pts = PreProcessing.resample(rawPoints, n);
        pts = PreProcessing.scaleTo(pts, size);
        this.points = PreProcessing.transalateTo(pts, new Point(0, 0));
    }
}
