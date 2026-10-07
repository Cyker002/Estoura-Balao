package elements;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.image.Image;
import br.com.davidbuzatto.jsge.image.ImageUtils;
import java.awt.Color;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa um inimigo que desce pela tela sustentado por um ou mais balões.
 * Agora utiliza o sprite da caveira pixel art fornecido.
 */
public class Enemies implements Characters {
    private static Image spriteInimigo = null;
    private static boolean tentouCarregar = false;

    private List<Balloon> balloons;
    private float x;
    private float y;
    private float speed = 50.0f;       // Velocidade de descida suave
    private float fallVelocity = 0.0f; // Velocidade de queda após perder todos os balões
    private boolean falling = false;
    private boolean defeated = false;
    private double animTime = 0.0;
    private int width = 44;
    private int height = 84;
    private Color enemyColor = new Color(50, 50, 70);

    private static void carregarSprite() {
        if (tentouCarregar) return;
        tentouCarregar = true;
        try {
            URL url = Enemies.class.getResource("/resources/inimigo.png");
            if (url != null) {
                spriteInimigo = ImageUtils.loadImage(url);
            } else {
                File f = new File("src/resources/inimigo.png");
                if (f.exists()) {
                    spriteInimigo = ImageUtils.loadImage(f.getPath());
                } else {
                    f = new File("resources/inimigo.png");
                    if (f.exists()) {
                        spriteInimigo = ImageUtils.loadImage(f.getPath());
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Aviso: Falha ao carregar sprite do inimigo: " + ex.getMessage());
        }
    }

    public Enemies(float x, float y, Symbol... symbols) {
        this.x = x;
        this.y = y;
        balloons = new ArrayList<>();
        for (Symbol s : symbols) {
            this.balloons.add(new Balloon(s));
        }
        carregarSprite();
    }

    public Enemies(float x, float y, List<Symbol> symbols) {
        this.x = x;
        this.y = y;
        balloons = new ArrayList<>();
        for (Symbol s : symbols) {
            this.balloons.add(new Balloon(s));
        }
        carregarSprite();
    }

    @Override
    public void update(double delta) {
        animTime += delta;

        if (falling) {
            // Aceleração da gravidade quando perde os balões
            fallVelocity += 900.0f * (float) delta;
            y += fallVelocity * (float) delta;
            if (y > 600) {
                defeated = true;
            }
        } else {
            // Flutuando lentamente para baixo
            y += speed * (float) delta;

            // Se todos os balões estouraram, inicia a queda
            if (allBalloonsPopped()) {
                falling = true;
                fallVelocity = 100.0f;
            }
        }
    }

    /**
     * Estoura TODOS os balões ativos deste inimigo que correspondam ao símbolo desenhado.
     * @return lista de balões deste inimigo que foram estourados nesta ação
     */
    public List<Balloon> popMatchingBalloons(Symbol symbol) {
        List<Balloon> estourados = new ArrayList<>();
        if (falling || defeated) {
            return estourados;
        }
        for (Balloon b : balloons) {
            if (!b.isBurst() && b.getSymbol() == symbol) {
                b.pop();
                estourados.add(b);
            }
        }
        if (!estourados.isEmpty() && allBalloonsPopped()) {
            falling = true;
            fallVelocity = 100.0f;
        }
        return estourados;
    }

    public boolean popMatchingBalloon(Symbol symbol) {
        return !popMatchingBalloons(symbol).isEmpty();
    }

    public boolean allBalloonsPopped() {
        for (Balloon b : balloons) {
            if (!b.isBurst()) {
                return false;
            }
        }
        return true;
    }

    public boolean isFalling() {
        return falling;
    }

    public boolean isDefeated() {
        return defeated;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    public List<Balloon> getBalloons() {
        return balloons;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void aumentarVelocidade(float incremento) {
        this.speed += incremento;
    }

    /**
     * Desenha o inimigo, as cordas dos balões, os balões e os símbolos correspondentes.
     */
    @Override
    public void draw(EngineFrame e) {
        if (defeated) return;

        double sway = Math.sin(animTime * 3.0) * 4.0;
        double currentX = x + sway;
        double currentY = y;

        // 1. Desenha os balões ativos e suas cordas
        desenharBaloes(e, currentX, currentY);

        // 2. Desenha o corpo do inimigo usando o sprite da caveira
        desenharCorpoInimigo(e, currentX, currentY);
    }

    private void desenharBaloes(EngineFrame e, double currentX, double currentY) {
        int totalBalloons = balloons.size();
        double balloonRadius = 20.0;
        double spacing = 38.0;
        double startOffsetX = -((totalBalloons - 1) * spacing) / 2.0;

        // Conexão da corda no topo do sprite da caveira
        double conexaoTopo = currentY - (spriteInimigo != null ? 36.0 : height / 2.0);
        double distBalao = spriteInimigo != null ? 68.0 : 50.0;

        for (int i = 0; i < totalBalloons; i++) {
            Balloon b = balloons.get(i);
            if (b.isBurst()) continue;

            double bx = currentX + startOffsetX + (i * spacing);
            double by = currentY - distBalao - (Math.abs(startOffsetX + (i * spacing)) * 0.2);

            // Corda do balão até o topo da caveira
            e.drawLine(currentX, conexaoTopo, bx, by + balloonRadius, new Color(90, 90, 90));

            // Balão (círculo preenchido + borda)
            Color bColor = b.getColor();
            e.fillCircle(bx, by, balloonRadius, bColor);
            e.drawCircle(bx, by, balloonRadius, new Color(30, 30, 30, 150));

            // Nó do balão
            e.fillCircle(bx, by + balloonRadius, 3, bColor.darker());

            // Desenha o símbolo dentro do balão
            desenharSimbolo(e, b.getSymbol(), bx, by);
        }
    }

    /**
     * Desenha o corpo do inimigo usando o sprite da caveira carregado.
     */
    public void desenharCorpoInimigo(EngineFrame e, double currentX, double currentY) {
        carregarSprite();
        if (spriteInimigo != null) {
            double imgW = spriteInimigo.getWidth();
            double imgH = spriteInimigo.getHeight();
            double imgX = currentX - imgW / 2.0;
            double imgY = currentY - imgH / 2.0;

            if (falling) {
                // Efeito de queda: leve tremor de pânico
                double tremor = Math.sin(animTime * 25.0) * 3.0;
                e.drawImage(spriteInimigo, imgX + tremor, imgY);
            } else {
                e.drawImage(spriteInimigo, imgX, imgY);
            }
            return;
        }

        // Fallback visual caso a imagem não esteja acessível:
        e.fillCircle(currentX, currentY, width / 2.0, enemyColor);
        e.drawCircle(currentX, currentY, width / 2.0, Color.BLACK);

        if (falling) {
            e.drawLine(currentX - 8, currentY - 4, currentX - 2, currentY + 2, Color.WHITE);
            e.drawLine(currentX - 2, currentY - 4, currentX - 8, currentY + 2, Color.WHITE);
            e.drawLine(currentX + 2, currentY - 4, currentX + 8, currentY + 2, Color.WHITE);
            e.drawLine(currentX + 8, currentY - 4, currentX + 2, currentY + 2, Color.WHITE);
        } else {
            e.fillCircle(currentX - 5, currentY - 3, 3, Color.WHITE);
            e.fillCircle(currentX + 5, currentY - 3, 3, Color.WHITE);
            e.fillCircle(currentX - 5, currentY - 3, 1.5, Color.BLACK);
            e.fillCircle(currentX + 5, currentY - 3, 1.5, Color.BLACK);
        }
    }

    /**
     * Desenha o símbolo dentro do balão para o jogador saber qual gesto desenhar.
     */
    private void desenharSimbolo(EngineFrame e, Symbol s, double bx, double by) {
        if (s == null) return;
        Color sc = Color.WHITE;
        double sz = 9.0;

        switch (s) {
            case LINHA_HORIZONTAL:
                e.drawLine(bx - sz, by, bx + sz, by, sc);
                e.drawLine(bx - sz, by - 1, bx + sz, by - 1, sc);
                e.drawLine(bx - sz, by + 1, bx + sz, by + 1, sc);
                break;
            case LINHA_VERTICAL:
                e.drawLine(bx, by - sz, bx, by + sz, sc);
                e.drawLine(bx - 1, by - sz, bx - 1, by + sz, sc);
                e.drawLine(bx + 1, by - sz, bx + 1, by + sz, sc);
                break;
            case V_NORMAL:
                e.drawLine(bx - sz, by - sz + 2, bx, by + sz, sc);
                e.drawLine(bx, by + sz, bx + sz, by - sz + 2, sc);
                e.drawLine(bx - sz, by - sz + 3, bx, by + sz + 1, sc);
                e.drawLine(bx, by + sz + 1, bx + sz, by - sz + 3, sc);
                break;
            case V_INVERTIDO:
                e.drawLine(bx - sz, by + sz - 2, bx, by - sz, sc);
                e.drawLine(bx, by - sz, bx + sz, by + sz - 2, sc);
                e.drawLine(bx - sz, by + sz - 1, bx, by - sz + 1, sc);
                e.drawLine(bx, by - sz + 1, bx + sz, by + sz - 1, sc);
                break;
            case CIRCULO:
                e.drawCircle(bx, by, sz - 1, sc);
                e.drawCircle(bx, by, sz, sc);
                break;
            case Z:
                e.drawLine(bx - sz, by - sz, bx + sz, by - sz, sc);
                e.drawLine(bx + sz, by - sz, bx - sz, by + sz, sc);
                e.drawLine(bx - sz, by + sz, bx + sz, by + sz, sc);
                break;
        }
    }
}
