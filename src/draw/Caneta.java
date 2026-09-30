package draw;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import java.awt.Color;
import java.awt.Graphics;
import unistrokeRecognize.Point;
import java.util.ArrayList;
import java.util.List;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author raul
 */
public class Caneta {
    protected int iniX;
    protected int iniY;
    protected int fimX;
    protected int fimY;
    
    private List<Point> pontos = new ArrayList<>();

    public void adicionarPonto(int x, int y) {
        pontos.add(new Point(x, y));
    }

    public void desenhar( EngineFrame e ){
         if (pontos.size() < 2) {
            // Se tiver só um clique rápido sem arrasto, desenha um ponto
            if (!pontos.isEmpty()) {
                Point p = pontos.get(0);
                e.drawLine(p.x, p.y, p.x, p.y, EngineFrame.BLACK);
            }
            return;
        }
        for (int i = 0; i < pontos.size() - 1; i++) {
            Point p1 = pontos.get(i);
            Point p2 = pontos.get(i + 1);
            e.drawLine(p1.x, p1.y, p2.x, p2.y, EngineFrame.BLACK);
        }
    }
   
    
}
