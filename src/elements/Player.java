package elements;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import java.awt.Color;

/**
 * Representa o Personagem principal (o Feiticeiro / Mago) posicionado na base.
 * Conspira magias com seu cajado conforme o jogador desenha os símbolos na tela.
 */
public class Player implements Characters {

    private float x;
    private float y;
    private boolean desenhando;
    private double animTime;
    
    // Cores temáticas do mago (estilo clássico Magic Touch)
    private Color corTunica = new Color(58, 45, 110);
    private Color corChapeu = new Color(45, 33, 92);
    private Color corFitaChapeu = new Color(245, 197, 66);
    private Color corPele = new Color(255, 220, 185);
    private Color corBarba = new Color(240, 240, 245);
    private Color corCajado = new Color(139, 69, 19);
    private Color corCristal = new Color(0, 230, 255);

    public Player(float x, float y) {
        this.x = x;
        this.y = y;
        this.desenhando = false;
        this.animTime = 0.0;
    }

    @Override
    public void update(double delta) {
        animTime += delta;
    }

    public void setDesenhando(boolean desenhando) {
        this.desenhando = desenhando;
    }

    public boolean isDesenhando() {
        return desenhando;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    public void setPosicao(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void draw(EngineFrame e) {
        // Leve respiração / animação ociosa
        double respiracao = Math.sin(animTime * 3.5) * 1.5;
        double px = x;
        double py = y + respiracao;

        // =====================================================================
        // NOTA: Para substituir por uma imagem/sprite próprio no futuro, basta
        // descomentar a linha abaixo e carregar sua BufferedImage/Image do JSGE:
        // e.drawImage(suaImagem, px - largura/2, py - altura, null);
        // =====================================================================

        desenharSombra(e, x, y);
        desenharTunica(e, px, py);
        desenharCabecaEBarba(e, px, py);
        desenharChapeu(e, px, py);
        desenharCajado(e, px, py);
    }

    private void desenharSombra(EngineFrame e, double px, double py) {
        // Sombra oval no chão
        e.fillCircle(px, py + 12, 16, new Color(0, 0, 0, 70));
    }

    private void desenharTunica(EngineFrame e, double px, double py) {
        // Corpo / Túnica do mago
        e.fillRectangle(px - 10, py - 6, 20, 18, corTunica);
        e.drawRectangle(px - 10, py - 6, 20, 18, corTunica.darker());
        
        // Cinto dourado
        e.fillRectangle(px - 10, py + 3, 20, 3, corFitaChapeu);
    }

    private void desenharCabecaEBarba(EngineFrame e, double px, double py) {
        // Rosto
        e.fillCircle(px, py - 13, 8, corPele);

        // Olhos
        if (desenhando) {
            // Olhos brilhando de magia quando desenha
            e.fillCircle(px - 3, py - 14, 2, corCristal);
            e.fillCircle(px + 3, py - 14, 2, corCristal);
        } else {
            // Olhos normais
            e.fillCircle(px - 3, py - 14, 1.5, Color.BLACK);
            e.fillCircle(px + 3, py - 14, 1.5, Color.BLACK);
        }

        // Barba branca clássica
        e.fillCircle(px, py - 7, 6, corBarba);
        e.fillCircle(px - 4, py - 8, 4, corBarba);
        e.fillCircle(px + 4, py - 8, 4, corBarba);
        e.fillCircle(px, py - 4, 3.5, corBarba);
    }

    private void desenharChapeu(EngineFrame e, double px, double py) {
        // Aba do chapéu pontudo
        e.fillRectangle(px - 15, py - 20, 30, 4, corChapeu);
        e.drawRectangle(px - 15, py - 20, 30, 4, corChapeu.darker());

        // Fita dourada no chapéu
        e.fillRectangle(px - 10, py - 23, 20, 3, corFitaChapeu);

        // Cone do chapéu (triângulo subindo)
        double topoX = px + 2 + Math.sin(animTime * 2.0) * 1.5;
        double topoY = py - 40;
        
        // Desenha o formato do chapéu por camadas
        for (int i = 0; i <= 16; i++) {
            double frac = i / 16.0;
            double curY = (py - 23) - (frac * ( (py - 23) - topoY ));
            double curLargura = (1.0 - frac) * 9.0;
            e.drawLine(px - curLargura, curY, px + curLargura, curY, corChapeu);
        }

        // Pompom / Estrelinha na ponta do chapéu
        e.fillCircle(topoX, topoY, 2.5, corFitaChapeu);
    }

    private void desenharCajado(EngineFrame e, double px, double py) {
        double cajadoX = px + 15;
        double cajadoBaseY = py + 12;
        double cajadoTopoY = py - 24;

        // Se estiver conjurando magia, o cajado ergue ligeiramente
        if (desenhando) {
            cajadoTopoY -= 4;
        }

        // Haste de madeira do cajado
        e.drawLine(cajadoX, cajadoBaseY, cajadoX, cajadoTopoY, corCajado);
        e.drawLine(cajadoX + 1, cajadoBaseY, cajadoX + 1, cajadoTopoY, corCajado);

        // Suporte do cristal
        e.fillCircle(cajadoX, cajadoTopoY, 3, corFitaChapeu);

        // Cristal mágico na ponta
        double pulso = (Math.sin(animTime * 6.0) + 1.0) / 2.0;
        double raioCristal = 4.0 + (desenhando ? pulso * 2.5 : 0.0);

        // Efeito de aura brilhante quando o jogador está desenhando na tela
        if (desenhando) {
            Color aura = new Color(0, 230, 255, 60 + (int)(pulso * 70));
            e.fillCircle(cajadoX, cajadoTopoY - 3, raioCristal + 5, aura);
        }

        e.fillCircle(cajadoX, cajadoTopoY - 3, raioCristal, corCristal);
        e.fillCircle(cajadoX - 1, cajadoTopoY - 4, 1.5, Color.WHITE); // Brilho interno
    }
}
