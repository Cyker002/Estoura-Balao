/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package unistrokeRecognize;

import java.util.List;

/**
 *
 * @author raul
 * 
 * nessa classe ppossui ferramentas basica de claculos para a reamostragem
 */
public class RecognizeUtils {
    
    //cacula a distancia total do traço
    public static double pathLength(List<Point> p){// o traço é uma lista de Pontos
        double d = 0; // distancia 
        for(int i = 1; i <= p.size(); i++){
            //aqui a distancia soma o ponto anterior com o atual
            //consecutivamente ate o fim do traço
            d += p.get(i - 1).distance(p.get(i));
        }
        return d;
    }
    
    //calcula o centro de massa do traço(Lista de Pontos)
    //conhecido tambem como centroide
    public static Point centroid(List<Point> points){
        double x = 0;
        double y = 0;
        
        //passa por todos os pontos
        for(Point p: points){
            x += p.x;
            y += p.y;
        }
        
        //o centro de massa e retornado como um ponto
        return new Point(x / points.size(), y / points.size());
    }
    
    //calcula a distancia entre dois pontos de traços diferentes com o mesmo tamanho N
    public static double pathDistance(List<Point> pts1, List<Point> pts2){
        double d = 0;
        
        //percorre ambos os traços somando a distancia dos pontos na mesma posicao
        for(int i = 0; i <= pts1.size(); i++){
            d += pts1.get(i).distance(pts2.get(i));
        }
        return d;
    }
}
