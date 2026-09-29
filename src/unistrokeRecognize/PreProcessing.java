/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package unistrokeRecognize;

import java.util.ArrayList;
import java.util.List;
import static unistrokeRecognize.RecognizeUtils.*;

/**
 *
 * @author raul
 * 
 * separei nessa classe os quatro passos de pre processamento necessario para 
 * se utilizar o Unistroke Recognize 
 */
public class PreProcessing {
    
    //Reamostragem = reorganiza o traço = constroi um traço novo apartir de um traço feito pelo Mouse reorganizando os seus pontos 
    // Passo 1: garante que todo traço tenha o mesmo tamanho N
    // utilizarei N = 64
    public static List<Point> resample(List<Point> points , int n){
        double interval = pathLength(points) / (n - 1);
        double D = 0;// 
        
        List<Point> newPoints = new ArrayList<Point>();//traço que recebera os pontos remodelados
        List<Point> srcPoints = new ArrayList<Point>(points);//copia do traço original
        newPoints.add(srcPoints.get(0));//começa com o ponto inicial do traço antigo
        
        for(int i = 1; i <= srcPoints.size(); i++){
            // calculo a distancia de dois pontos e guardo em d
            Point p1 = srcPoints.get(i - 1); 
            Point p2 = srcPoints.get(i);
            double d = p1.distance(p2);// é o comprimento total entre p1 e p2
            
            if((D + d) >= interval){// quando as distancias acumuladas sao maiores que o intervalo interpolam um novo ponto
                
                //aqui ocorre uma interpolação linear, (interval - D) é quanto ainda falta para completar o intervalo
                //d = tamanho do seguimento atual
                //quando dividimos o que ainda falta para completar um intervalo pelo tamanho do seguimento, temos uma fração
                //essa fração representa o ponto exato em que esta 'interval' do ultimo ponto adicionado
                double qx = p1.x + ((interval - D) / d) * (p2.x - p1.x);
                double qy = p1.y + ((interval - D) / d) * (p2.x - p1.x);
                Point q = new Point(qx, qy);
                newPoints.add(q);// adiciona o ponto em newPoints
                srcPoints.add(i,q);// adiciona o q para ser o proximo p1
                D = 0;
            }else{//Percorre os seguimentos originais acumulando a distancia => 'd'
                D += d;
                
            }
        }
        while (newPoints.size() < n){
            newPoints.add(srcPoints.get(n - 1));
        }
        
        return newPoints;
    }
    
    
    //serve para rotacionar todos os pontos por -theta
    public static List<Point> rotateBy(List<Point> points, double radians){
        Point c = centroid(points);
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        
        List<Point> newPoints = new ArrayList<Point>();
        for(Point p : points){
            double dx = p.x - c.x;
            double dy = p.y - c.y;
            
            double qx = dx * cos - dy * sin + c.x;
            double qy = dx * cos + dy * sin + c.y;
            newPoints.add(new Point(qx, qy));
        }
        
        return newPoints;
    }
    
    public static List<Point> rotateToZero(List<Point> points){
        Point c = centroid(points);
        double theta = Math.atan2(c.y - points.get(0).y, c.x - points.get(0).x);
        return rotateBy(points, theta);
    }
    
    
    //Passo 3 : Escalonar e Traslandar
    //redimenciona para uma caixa, e move para (0,0)
    
    
    //Redimenciona um traço, trocando sua escala
    public static List<Point> scaleTo(List<Point> points, double size){
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        
        for(Point p : points){
            minX = Math.min(minX, p.x);
            minY = Math.min(minY, p.y);
            maxX = Math.max(maxX, p.x);
            maxY = Math.max(maxY, p.y);
        }
        
        double width = maxX - minX;
        double heigth = maxY - minY;
        
        List<Point> newPoints = new ArrayList<Point>();
        
        for(Point p : points){
            //garante que se a linha for reta, nao seja deformada e confudida com outro gesto
            double qx = (width != 0) ? p.x * (size / width) : p.x;
            double qy = (heigth != 0) ? p.y * (size / heigth) : p.y;
            newPoints.add(new Point(qx, qy));
        }
        
        return newPoints;
    }
    
    //Traslandar(mover)
    public static List<Point> transalateTo(List<Point> points, Point pt){
        Point c = centroid(points);
        List<Point> newPoints = new ArrayList<Point>();
        
        for(Point p : points){
            newPoints.add(new Point(p.x + pt.x - c.x, p.y + pt.y - c.y));
        }
        
        return newPoints;
    }
    
    
    
}
