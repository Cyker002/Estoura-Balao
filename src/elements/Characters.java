package elements;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;

/**
 * Interface base para personagens do jogo (Jogador, Inimigos, etc.).
 */
public interface Characters {
    void update(double delta);
    void draw(EngineFrame e);
    float getX();
    float getY();
}
