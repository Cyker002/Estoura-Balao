/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package unistrokeRecognize;

/**
 *
 * @author raul
 * 
 */
public class Point {
    public double x;
    public double y;
    
    public Point(double x, double y){
        this.x = x;
        this.y = y;
    }
    
//    diferença por eu nâo ter importado a classe point
//    preciso calcular a distancia entre dois pontos para fazer a reamostragem        
    public double distance(Point p){
        double dx = this.x - p.x;
        double dy = this.y - p.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
