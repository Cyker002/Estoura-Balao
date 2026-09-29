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
public class UnistrokeRecognize {
    private static final int NUM_POINTS = 64;
    private static final double  SQUARE_SIZE = 250.0;
    private static final double ANGULAR_RANGE = Math.toRadians(45.0);
    private static final double ANGULAR_PRECISION = Math.toRadians(2.0);
    private static final double PHI = 0.5 * (-1.0 + Math.sqrt(5.0)); //Razão aurea
    
    private List<Template> templates = new ArrayList<>();
    
    public void addTemplate(String name, List<Point> points){
        templates.add(new Template(name, points, NUM_POINTS, SQUARE_SIZE));
    }
    
    public Result recognize(List<Point> rawPoints){
        if(rawPoints == null || rawPoints.size() < 2){
            return new Result("Desconhecido", 0.0);
        }
        
        //Normaliza o desenho/traço do Player
        List<Point> points = PreProcessing.resample(rawPoints, NUM_POINTS);
        points = PreProcessing.rotateToZero(points);
        points = PreProcessing.scaleTo(points, SQUARE_SIZE);
        points = PreProcessing.transalateTo(points, new Point(0, 0));
        
        double bestDistance = Double.MAX_VALUE;
        
        Template bestTemplate = null;
        
        //faz a comparação com todos os templates
        for(Template template : templates){
            double distance = distanceAtBestAngle(points, template, -ANGULAR_RANGE, ANGULAR_RANGE, ANGULAR_RANGE);
            if(distance < bestDistance){
                bestDistance =  distance;
                bestTemplate = template;
            }
        }
        
        if(bestTemplate == null){
            return new Result("Desconhecido", 0.0);
        }
            
        //converte a distancia euclidiana em um score de confiança entre 0.0 e 1.0
        double  halfDiagonal = 0.5* Math.sqrt(SQUARE_SIZE * SQUARE_SIZE + SQUARE_SIZE * SQUARE_SIZE);
        double score = 1.0 - (bestDistance / halfDiagonal);
            
        return new Result(bestTemplate.name, score);
    }
    
    private double distanceAtBestAngle(List<Point> points, Template template, double a, double b, double threshold){
        double x1 = PHI * a + (1.0 - PHI) * b;
        double f1 = distanceAtAngle(points, template, x1);
        double x2 = ( 1.0 - PHI) * a + PHI * b;
        double f2 = distanceAtAngle(points, template, x2);
        
        while(Math.abs(b -a) > threshold){
            if(f1 < f2){
               b = x2;
               x2 = x1;
               f2 = f1;
               x1 = PHI * a + (1.0 - PHI) * b;
               f1 = distanceAtAngle(points, template, x1);
            } else {
                a = x1;
                x1 = x2;
                f1  = f2;
                x2 = (1.0 - PHI) * a + PHI * b;
                f2 = distanceAtAngle(points, template, x2);
            }
        }
        return Math.min(f1, f2);
    }
    
    double distanceAtAngle(List<Point> points, Template template, double angle){
        List<Point> rotated = PreProcessing.rotateBy(points, angle);
        return RecognizeUtils.pathDistance(rotated, template.points);
    }
    
    //Classe DTO de resultado
    public static class Result{
        public String name;
        public double score;
        
        public Result(String name, double score){
            this.name = name;
            this.score = score;
        }
    }
}
