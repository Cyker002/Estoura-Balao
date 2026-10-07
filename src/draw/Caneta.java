package draw;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import unistrokeRecognize.Point;

/**
 * Representa a caneta de desenho mágico do jogador.
 * Acumula os pontos enquanto o jogador arrasta o mouse e os desenha na tela.
 */
public class Caneta {
    private List<Point> pontos = new ArrayList<>();
    private Color corTraco = new Color(255, 215, 0); // Dourado mágico
    private Color corSombra = new Color(255, 140, 0, 120);

    public void adicionarPonto(int x, int y) {
        // Evita adicionar pontos duplicados seguidos
        if (!pontos.isEmpty()) {
            Point ultimo = pontos.get(pontos.size() - 1);
            if (Math.abs(ultimo.x - x) < 2 && Math.abs(ultimo.y - y) < 2) {
                return;
            }
        }
        pontos.add(new Point(x, y));
    }

    public void limpar() {
        pontos.clear();
    }

    public List<Point> getPontos() {
        return pontos;
    }

    public boolean temPontos() {
        return pontos.size() >= 2;
    }

    public void desenhar(EngineFrame e) {
        if (pontos.isEmpty()) {
            return;
        }

        if (pontos.size() == 1) {
            Point p = pontos.get(0);
            e.fillCircle(p.x, p.y, 4, corTraco);
            return;
        }

        for (int i = 0; i < pontos.size() - 1; i++) {
            Point p1 = pontos.get(i);
            Point p2 = pontos.get(i + 1);

            // Desenha efeito de brilho suave e linha principal
            e.drawLine(p1.x, p1.y, p2.x, p2.y, corSombra);
            e.drawLine(p1.x - 1, p1.y, p2.x - 1, p2.y, corSombra);
            e.drawLine(p1.x + 1, p1.y, p2.x + 1, p2.y, corSombra);
            e.drawLine(p1.x, p1.y - 1, p2.x, p2.y - 1, corSombra);
            e.drawLine(p1.x, p1.y + 1, p2.x, p2.y + 1, corSombra);

            e.drawLine(p1.x, p1.y, p2.x, p2.y, corTraco);
            e.fillCircle(p1.x, p1.y, 2.5, Color.WHITE);
        }

        Point ultimo = pontos.get(pontos.size() - 1);
        e.fillCircle(ultimo.x, ultimo.y, 4, Color.WHITE);
    }
}
