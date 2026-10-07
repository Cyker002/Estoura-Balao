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

        String tit = "JOGO PAUSADO";
        int titW = janela.measureText(tit, 34);
        janela.drawText(tit, (800 - titW) / 2, 150, 34, Color.WHITE);

        String op1 = "► Pressione ESPAÇO ou ESC para Continuar";
        int op1W = janela.measureText(op1, 18);
        janela.drawText(op1, (800 - op1W) / 2, 230, 18, Color.YELLOW);

        String op2 = "► Pressione M para Voltar ao Menu Inicial";
        int op2W = janela.measureText(op2, 16);
        janela.drawText(op2, (800 - op2W) / 2, 275, 16, Color.LIGHT_GRAY);
    }

    @Override
    public String getTitulo() {
        return "Estoura Balão - Pause";
    }
}
