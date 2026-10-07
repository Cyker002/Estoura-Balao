package gui.telas;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import java.awt.Color;

/**
 * Tela de Pause independente (caso prefira alternar para ela via janela.mudarTela).
 */
public class MenuPause implements Tela {

    @Override
    public void create() {
    }

    @Override
    public void update(double delta, JanelaAtiva janela) {
        if (janela.isKeyPressed(EngineFrame.KEY_SPACE) || janela.isKeyPressed(EngineFrame.KEY_ESCAPE)) {
            janela.mudarTela(new TelaJogo());
        }
        if (janela.isKeyPressed(EngineFrame.KEY_M)) {
            janela.mudarTela(new MenuInicial());
        }
    }

    @Override
    public void draw(JanelaAtiva janela) {
        janela.fillRectangle(0, 0, 800, 450, new Color(18, 22, 38));

        janela.drawText("JOGO PAUSADO", 270, 150, 34, Color.WHITE);
        janela.drawText("► Pressione ESPAÇO ou ESC para Continuar", 215, 230, 18, Color.YELLOW);
        janela.drawText("► Pressione M para Voltar ao Menu Inicial", 230, 275, 16, Color.LIGHT_GRAY);
    }

    @Override
    public String getTitulo() {
        return "Estoura Balão - Pause";
    }
}
