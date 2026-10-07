package gui.telas;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import java.awt.Color;

/**
 * Menu inicial do jogo com instruções e botão para iniciar a partida.
 */
public class MenuInicial implements Tela {

    private double tempoAnimacao = 0.0;

    @Override
    public void create() {
        tempoAnimacao = 0.0;
    }

    @Override
    public void update(double delta, JanelaAtiva janela) {
        tempoAnimacao += delta;

        // Inicia o jogo ao pressionar ESPAÇO ou clicar com o botão esquerdo
        if (janela.isKeyPressed(EngineFrame.KEY_SPACE) || 
            janela.isMouseButtonPressed(EngineFrame.MOUSE_BUTTON_LEFT)) {
            janela.mudarTela(new TelaJogo());
        }
    }

    @Override
    public void draw(JanelaAtiva janela) {
        // Fundo na cor solicitada: (211, 211, 211)
        janela.fillRectangle(0, 0, 800, 450, new Color(211, 211, 211));

        // Título estilizado
        double oscilacao = Math.sin(tempoAnimacao * 3.0) * 5.0;
        janela.drawText("ESTOURA BALÃO", 240, 90 + oscilacao, 42, new Color(35, 42, 65));
        janela.drawText("Projeto Estrutura De Dados", 270, 130 + oscilacao, 16, new Color(70, 80, 105));

        // Painel de Tutorial / Símbolos
        janela.fillRectangle(150, 170, 500, 160, new Color(30, 35, 55, 200));
        janela.drawRectangle(150, 170, 500, 160, new Color(70, 80, 120));

        janela.drawText("COMO JOGAR:", 170, 190, 18, Color.YELLOW);
        janela.drawText("Desenhe os símbolos dos balões na tela para estourá-los!", 170, 215, 14, Color.WHITE);
        janela.drawText("Evite que os monstros toquem a base do seu castelo.", 170, 235, 14, Color.LIGHT_GRAY);

        // Guia visual de símbolos
        janela.drawText("Símbolos disponíveis:", 170, 265, 14, Color.CYAN);
        janela.drawText("— Linha Horizontal ", 180, 290, 14, new Color(230, 80, 80));
        janela.drawText("| Linha Vertical", 340, 290, 14, new Color(80, 160, 240));
        janela.drawText("V Normal", 490, 290, 14, new Color(60, 210, 120));
        janela.drawText("Ʌ Invertido", 180, 312, 14, new Color(240, 170, 60));
        janela.drawText("O Círculo", 340, 312, 14, new Color(170, 100, 240));
        janela.drawText("Z Zigue-zague", 490, 312, 14, new Color(240, 100, 190));

        // Botão / Instrução de Início piscando suavemente
        int piscar = (int) ((Math.sin(tempoAnimacao * 5.0) + 1.0) * 127);
        Color corPiscar = new Color(35, 45, 70, Math.max(90, piscar));
        janela.drawText("► PRESSIONE ESPAÇO OU CLIQUE PARA JOGAR ◄", 195, 385, 18, corPiscar);
    }

    @Override
    public String getTitulo() {
        return "Estoura Balão - Menu Inicial";
    }
}
