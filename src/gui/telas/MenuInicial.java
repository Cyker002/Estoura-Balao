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

        // Título estilizado perfeitamente centralizado
        double oscilacao = Math.sin(tempoAnimacao * 3.0) * 5.0;
        String titJogo = "ESTOURA BALÃO";
        int titW = janela.measureText(titJogo, 42);
        janela.drawText(titJogo, (800 - titW) / 2, 85 + oscilacao, 42, new Color(35, 42, 65));

        String subTit = "Projeto Estrutura De Dados";
        int subW = janela.measureText(subTit, 16);
        janela.drawText(subTit, (800 - subW) / 2, 125 + oscilacao, 16, new Color(70, 80, 105));

        // Painel de Tutorial / Símbolos alargado para acomodar o texto com folga
        int panelW = 590;
        int panelH = 175;
        int panelX = (800 - panelW) / 2; // 105
        int panelY = 165;

        janela.fillRectangle(panelX, panelY, panelW, panelH, new Color(30, 35, 55, 205));
        janela.drawRectangle(panelX, panelY, panelW, panelH, new Color(70, 80, 120));
        janela.drawRectangle(panelX + 1, panelY + 1, panelW - 2, panelH - 2, new Color(45, 52, 80));

        int margemX = panelX + 25;
        janela.drawText("COMO JOGAR:", margemX, panelY + 22, 18, Color.YELLOW);
        janela.drawText("Desenhe os símbolos dos balões na tela para estourá-los!", margemX, panelY + 48, 14, Color.WHITE);
        janela.drawText("Evite que os monstros toquem a base do seu castelo.", margemX, panelY + 68, 14, Color.LIGHT_GRAY);

        // Guia visual de símbolos distribuído em 3 colunas confortáveis
        janela.drawText("Símbolos disponíveis:", margemX, panelY + 98, 14, Color.CYAN);

        int col1X = panelX + 25;
        int col2X = panelX + 215;
        int col3X = panelX + 405;

        janela.drawText("— Linha Horizontal", col1X, panelY + 122, 14, new Color(230, 80, 80));
        janela.drawText("| Linha Vertical", col2X, panelY + 122, 14, new Color(80, 160, 240));
        janela.drawText("V Normal", col3X, panelY + 122, 14, new Color(60, 210, 120));

        janela.drawText("Ʌ Invertido", col1X, panelY + 146, 14, new Color(240, 170, 60));
        janela.drawText("O Círculo", col2X, panelY + 146, 14, new Color(170, 100, 240));
        janela.drawText("Z Zigue-zague", col3X, panelY + 146, 14, new Color(240, 100, 190));

        // Botão / Instrução de Início piscando suavemente e centralizado
        int piscar = (int) ((Math.sin(tempoAnimacao * 5.0) + 1.0) * 127);
        Color corPiscar = new Color(35, 45, 70, Math.max(90, piscar));
        String promptTexto = "► PRESSIONE ESPAÇO OU CLIQUE PARA JOGAR ◄";
        int promptW = janela.measureText(promptTexto, 18);
        janela.drawText(promptTexto, (800 - promptW) / 2, 385, 18, corPiscar);
    }

    @Override
    public String getTitulo() {
        return "Estoura Balão - Menu Inicial";
    }
}
