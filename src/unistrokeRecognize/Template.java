/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package unistrokeRecognize;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author raul
 */
public class Template {
    public String name;
    public List<Point> points;
    
    public Template(String name, List<Point> rawPoints, int n, double size){
        this.name = name;
        
        List<Point> pts = new ArrayList<Point>();
        pts = PreProcessing.rotateToZero(pts);
        pts = PreProcessing.scaleTo(pts, size);
        this.points = PreProcessing.transalateTo(pts, new Point(0,0));
    }
}
