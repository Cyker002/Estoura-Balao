package gui.telas;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import draw.Caneta;
import elements.Balloon;
import elements.Enemies;
import elements.Player;
import elements.Symbol;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import unistrokeRecognize.UnistrokeRecognize;

/**
 * Tela principal de gameplay com o layout de desenho:
 * - Botão de Pause (dois palitos) no canto superior esquerdo
 * - Pontuação no canto superior direito
 * - Chãozinho na base com o Personagem (Mago)
 * - Inimigos com balões descendo do céu
 */
public class TelaJogo implements Tela {

    // Entidades do jogo
    private Player player;
    private List<Enemies> inimigos;
    private List<Particula> particulas;
    private Caneta caneta;
    private UnistrokeRecognize recognizer;
    
    // Estado do jogo
    private int pontuacao;
    private int vidas;
    private boolean gameOver;
    private boolean pausado;
    
    // Geração de inimigos e dificuldade dinâmica
    private double spawnTimer;
    private double spawnInterval;
    private double tempoDeJogo;
    private int nivelVelocidade;
    private static final double INTERVALO_AUMENTO_VELOCIDADE = 15.0; // Segundos para cada aceleração (15s)
    private static final float INCREMENTO_VELOCIDADE = 12.0f;       // Aumento de velocidade a cada 15s
    
    // Feedback visual
    private String feedbackMensagem = "";
    private double feedbackTimer = 0.0;
    private Color corFeedback = Color.WHITE;

    // Estrutura de Dados em Tempo Real: ArrayList de Balões visível na tela
    private List<Balloon> listaBaloes;
    private String logOperacaoLista = "new ArrayList<>()";
    private Color corLogOperacao = new Color(130, 200, 255);
    private int ultimoIndiceModificado = -1;
    private double timerDestaqueSlot = 0.0;
    
    // Dimensões da tela e layout
    private static final int LARGURA_TELA = 800;
    private static final int ALTURA_TELA = 450;
    private static final int Y_CHAO = 390;
    
    // Área do botão de Pause (canto superior esquerdo)
    private static final int PAUSE_X = 20;
    private static final int PAUSE_Y = 15;
    private static final int PAUSE_W = 38;
    private static final int PAUSE_H = 38;
    
    private Random random = new Random();
    private static final Symbol[] TODOS_SIMBOLOS = Symbol.values();

    @Override
    public void create() {
        // Inicializa o personagem no centro da área útil de jogo (à esquerda do painel lateral)
        player = new Player(315.0f, Y_CHAO - 5.0f);
        
        inimigos = new ArrayList<>();
        listaBaloes = new ArrayList<>();
        particulas = new ArrayList<>();
        caneta = new Caneta();
        recognizer = new UnistrokeRecognize();
        
        pontuacao = 0;
        vidas = 3;
        gameOver = false;
        pausado = false;
        
        spawnTimer = 0.0;
        spawnInterval = 2.5;
        tempoDeJogo = 0.0;
        nivelVelocidade = 0;
        
        logOperacaoLista = "new ArrayList<>()";
        corLogOperacao = new Color(130, 200, 255);
        ultimoIndiceModificado = -1;
        timerDestaqueSlot = 0.0;

        feedbackMensagem = "Desenhe os símbolos dos balões para estourá-los!";
        feedbackTimer = 3.5;
        corFeedback = new Color(255, 230, 120);
    }

    @Override
    public void update(double delta, JanelaAtiva janela) {
        int mouseX = janela.getMouseX();
        int mouseY = janela.getMouseY();

        // 1. Controle do Botão de Pause (clique no ícone ou teclas P / ESC)
        boolean clicouNoBotaoPause = janela.isMouseButtonPressed(EngineFrame.MOUSE_BUTTON_LEFT) &&
                                     estaDentro(mouseX, mouseY, PAUSE_X, PAUSE_Y, PAUSE_W, PAUSE_H);
        
        if (clicouNoBotaoPause || janela.isKeyPressed(EngineFrame.KEY_P) || janela.isKeyPressed(EngineFrame.KEY_ESCAPE)) {
            pausado = !pausado;
            caneta.limpar();
            player.setDesenhando(false);
            return;
        }

        // Se o jogo estiver pausado, processa apenas as opções do menu de pause
        if (pausado) {
            atualizarMenuPause(janela);
            return;
        }

        // Se estiver em Game Over
        if (gameOver) {
            if (janela.isKeyPressed(EngineFrame.KEY_SPACE) || 
                (janela.isMouseButtonPressed(EngineFrame.MOUSE_BUTTON_LEFT) && !clicouNoBotaoPause)) {
                create();
            }
            return;
        }

        tempoDeJogo += delta;
        player.update(delta);

        // A cada 15 segundos, aumenta a velocidade de todos os inimigos
        int novoNivel = (int) (tempoDeJogo / INTERVALO_AUMENTO_VELOCIDADE);
        if (novoNivel > nivelVelocidade) {
            int niveisSubidos = novoNivel - nivelVelocidade;
            nivelVelocidade = novoNivel;

            // Acelera imediatamente todos os inimigos que já estão descendo na tela
            for (Enemies inimigo : inimigos) {
                if (!inimigo.isFalling()) {
                    inimigo.aumentarVelocidade(INCREMENTO_VELOCIDADE * niveisSubidos);
                }
            }

            // Alerta visual de aceleração para o jogador
            feedbackMensagem = "⚡ PERIGO! Inimigos mais rápidos (Nível " + (nivelVelocidade + 1) + ")! ⚡";
            corFeedback = new Color(255, 140, 20);
            feedbackTimer = 2.8;
        }

        // Aumenta a frequência de spawn progressivamente com o tempo
        spawnInterval = Math.max(0.9, 2.5 - (tempoDeJogo / 60.0) * 0.8);

        // 2. Spawn de Inimigos
        spawnTimer += delta;
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0.0;
            spawnarInimigo();
        }

        // Timer de destaque do slot no painel ArrayList
        if (timerDestaqueSlot > 0) {
            timerDestaqueSlot -= delta;
            if (timerDestaqueSlot <= 0) {
                ultimoIndiceModificado = -1;
            }
        }

        // 3. Atualização dos Inimigos
        Iterator<Enemies> itInimigos = inimigos.iterator();
        while (itInimigos.hasNext()) {
            Enemies inimigo = itInimigos.next();
            inimigo.update(delta);

            // Inimigo ainda com balões atingiu o chãozinho
            if (!inimigo.isFalling() && inimigo.getY() >= Y_CHAO) {
                vidas--;
                criarExplosao(inimigo.getX(), Y_CHAO, Color.RED, 25);
                
                // Remove balões sobreviventes deste inimigo da lista
                for (Balloon b : inimigo.getBalloons()) {
                    if (!b.isBurst()) {
                        int idxRem = listaBaloes.indexOf(b);
                        if (idxRem != -1) {
                            listaBaloes.remove(idxRem);
                            logOperacaoLista = "remove(" + idxRem + ") [colisão chão]";
                            corLogOperacao = new Color(255, 90, 90);
                            ultimoIndiceModificado = idxRem;
                            timerDestaqueSlot = 1.0;
                        }
                    }
                }

                itInimigos.remove();
                if (vidas <= 0) {
                    gameOver = true;
                }
                continue;
            }

            // Inimigo derrotado que caiu fora da tela
            if (inimigo.isDefeated()) {
                for (Balloon b : inimigo.getBalloons()) {
                    listaBaloes.remove(b);
                }
                itInimigos.remove();
            }
        }

        // 4. Atualização das Partículas
        Iterator<Particula> itParticulas = particulas.iterator();
        while (itParticulas.hasNext()) {
            Particula p = itParticulas.next();
            p.update(delta);
            if (p.isMorta()) {
                itParticulas.remove();
            }
        }

        // 5. Temporizador de Feedback
        if (feedbackTimer > 0) {
            feedbackTimer -= delta;
            if (feedbackTimer <= 0) {
                feedbackMensagem = "";
            }
        }

        // 6. Entrada do Jogador (Desenho com a Caneta)
        // Não desenha se o clique inicial foi sobre o botão de pause
        if (janela.isMouseButtonPressed(EngineFrame.MOUSE_BUTTON_LEFT)) {
            if (!clicouNoBotaoPause) {
                caneta.limpar();
                caneta.adicionarPonto(mouseX, mouseY);
                player.setDesenhando(true);
            }
        } else if (janela.isMouseButtonDown(EngineFrame.MOUSE_BUTTON_LEFT)) {
            if (player.isDesenhando()) {
                caneta.adicionarPonto(mouseX, mouseY);
            }
        } else if (janela.isMouseButtonReleased(EngineFrame.MOUSE_BUTTON_LEFT)) {
            if (player.isDesenhando()) {
                if (caneta.temPontos()) {
                    processarDesenho();
                }
                caneta.limpar();
                player.setDesenhando(false);
            }
        }
    }

    private void atualizarMenuPause(JanelaAtiva janela) {
        // Retomar jogo
        if (janela.isKeyPressed(EngineFrame.KEY_SPACE) || janela.isKeyPressed(EngineFrame.KEY_ENTER)) {
            pausado = false;
        }
        // Reiniciar partida
        if (janela.isKeyPressed(EngineFrame.KEY_R)) {
            create();
        }
        // Voltar ao Menu Inicial
        if (janela.isKeyPressed(EngineFrame.KEY_M)) {
            janela.mudarTela(new MenuInicial());
        }
    }

    /**
     * Compara o traço desenhado com os símbolos conhecidos e estoura os balões.
     */
    private void processarDesenho() {
        UnistrokeRecognize.Result resultado = recognizer.recognize(caneta.getPontos());

        if (resultado.score >= 0.70 && !resultado.name.equals("Desconhecido")) {
            try {
                Symbol simboloDesenhado = Symbol.valueOf(resultado.name);
                int baloesEstourados = 0;

                for (Enemies inimigo : inimigos) {
                    List<Balloon> estourados = inimigo.popMatchingBalloons(simboloDesenhado);
                    for (Balloon b : estourados) {
                        baloesEstourados++;
                        criarExplosao(inimigo.getX(), inimigo.getY() - 68, b.getColor(), 18);

                        // Remove da estrutura de dados ArrayList e atualiza o painel
                        int idxRem = listaBaloes.indexOf(b);
                        if (idxRem != -1) {
                            listaBaloes.remove(idxRem);
                            ultimoIndiceModificado = idxRem;
                            timerDestaqueSlot = 1.4;
                            logOperacaoLista = "remove(" + idxRem + ") -> shift";
                            corLogOperacao = new Color(255, 140, 80);
                        }
                    }
                }

                if (baloesEstourados > 0) {
                    pontuacao += baloesEstourados * 10;
                    feedbackMensagem = "Acertou: " + nomeAmigavel(simboloDesenhado) + " (+" + (baloesEstourados * 10) + " pts)";
                    corFeedback = new Color(80, 240, 130);
                    feedbackTimer = 1.2;
                } else {
                    feedbackMensagem = "Desenhou: " + nomeAmigavel(simboloDesenhado) + " (sem alvo na tela)";
                    corFeedback = new Color(230, 235, 245);
                    feedbackTimer = 0.8;
                }

            } catch (IllegalArgumentException ex) {
                // Símbolo não mapeado
            }
        } else {
            feedbackMensagem = "Gesto não reconhecido! Tente novamente.";
            corFeedback = new Color(255, 110, 110);
            feedbackTimer = 0.8;
        }
    }

    private String nomeAmigavel(Symbol s) {
        switch (s) {
            case LINHA_HORIZONTAL: return "Linha Horizontal (—)";
            case LINHA_VERTICAL:   return "Linha Vertical (|)";
            case V_NORMAL:         return "V";
            case V_INVERTIDO:      return "V Invertido (^)";
            case CIRCULO:          return "Círculo (O)";
            case Z:                return "Z";
            default:               return s.name();
        }
    }

    private void spawnarInimigo() {
        // Gera inimigos na área de jogo à esquerda do painel da estrutura de dados
        float x = 70 + random.nextInt(480);
        float y = -45;

        int qtdBaloes = 1;
        if (pontuacao >= 100 && random.nextDouble() < 0.45) {
            qtdBaloes = 2;
        } else if (pontuacao >= 250 && random.nextDouble() < 0.65) {
            qtdBaloes = 2;
            if (random.nextDouble() < 0.35) {
                qtdBaloes = 3;
            }
        }

        List<Symbol> simbolos = new ArrayList<>();
        for (int i = 0; i < qtdBaloes; i++) {
            simbolos.add(TODOS_SIMBOLOS[random.nextInt(TODOS_SIMBOLOS.length)]);
        }

        Enemies novoInimigo = new Enemies(x, y, simbolos);
        // Velocidade base que aumenta progressivamente a cada 30 segundos
        float velocidadeBase = 35.0f + (nivelVelocidade * INCREMENTO_VELOCIDADE);
        novoInimigo.setSpeed(velocidadeBase + random.nextFloat() * 20.0f);
        inimigos.add(novoInimigo);

        // Adiciona os novos balões ao ArrayList monitorado na tela
        for (Balloon b : novoInimigo.getBalloons()) {
            listaBaloes.add(b);
        }
        int ultimoIdx = listaBaloes.size() - 1;
        ultimoIndiceModificado = ultimoIdx;
        timerDestaqueSlot = 1.2;
        logOperacaoLista = "add(b) no índice [" + ultimoIdx + "]";
        corLogOperacao = new Color(80, 240, 130);
    }

    private void criarExplosao(double x, double y, Color cor, int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            particulas.add(new Particula(x, y, cor, random));
        }
    }

    private boolean estaDentro(int px, int py, int x, int y, int w, int h) {
        return px >= x && px <= (x + w) && py >= y && py <= (y + h);
    }

    @Override
    public void draw(JanelaAtiva janela) {
        // 1. Cenário de Fundo
        desenharCenario(janela);

        // 2. Chãozinho da base
        desenharChao(janela);

        // 3. Personagem (Mago) de pé na base
        player.draw(janela);

        // 4. Inimigos e seus Balões
        for (Enemies inimigo : inimigos) {
            inimigo.draw(janela);
        }

        // 5. Partículas de estouro
        for (Particula p : particulas) {
            p.draw(janela);
        }

        // 6. Traço da Caneta mágica desenhado pelo jogador
        caneta.desenhar(janela);

        // 7. Interface HUD da Base:
        // - Botão de Pause (dois palitos) no canto superior esquerdo
        desenharBotaoPause(janela);

        // - Pontuação no canto superior direito
        desenharPontuacao(janela);

        // - Painel lateral da estrutura de dados ArrayList de balões funcionando
        desenharPainelArrayList(janela);

        // - Feedback do gesto e vidas restantes
        desenharStatus(janela);

        // 8. Menus de Pause e Game Over
        if (pausado) {
            desenharMenuPause(janela);
        } else if (gameOver) {
            desenharGameOver(janela);
        }
    }

    private void desenharCenario(JanelaAtiva janela) {
        // Céu diurno na cor solicitada: (179, 235, 242)
        janela.fillRectangle(0, 0, LARGURA_TELA, ALTURA_TELA, new Color(179, 235, 242));

        // Nuvens estilizadas brancas ao fundo
        janela.fillCircle(110, 85, 45, new Color(255, 255, 255, 190));
        janela.fillCircle(155, 78, 55, new Color(255, 255, 255, 220));
        janela.fillCircle(200, 90, 42, new Color(255, 255, 255, 190));

        janela.fillCircle(630, 115, 48, new Color(255, 255, 255, 190));
        janela.fillCircle(680, 105, 58, new Color(255, 255, 255, 220));
    }

    /**
     * Desenha o chãozinho na base da tela onde o personagem fica.
     */
    private void desenharChao(JanelaAtiva janela) {
        // 1. Camada de terra/rocha profunda
        janela.fillRectangle(0, Y_CHAO + 10, LARGURA_TELA, ALTURA_TELA - (Y_CHAO + 10), new Color(42, 32, 26));

        // 2. Camada de terra intermediária
        janela.fillRectangle(0, Y_CHAO + 4, LARGURA_TELA, 10, new Color(74, 52, 38));

        // 3. Faixa de grama no topo do chãozinho
        janela.fillRectangle(0, Y_CHAO, LARGURA_TELA, 6, new Color(46, 139, 87));
        janela.fillRectangle(0, Y_CHAO - 2, LARGURA_TELA, 3, new Color(60, 179, 113));

        // 4. Detalhes decorativos de grama (pequenos tufinhos)
        for (int gx = 10; gx < LARGURA_TELA; gx += 35) {
            janela.drawLine(gx, Y_CHAO - 2, gx - 2, Y_CHAO - 5, new Color(70, 200, 120));
            janela.drawLine(gx, Y_CHAO - 2, gx + 2, Y_CHAO - 6, new Color(70, 200, 120));
            janela.drawLine(gx, Y_CHAO - 2, gx + 4, Y_CHAO - 4, new Color(70, 200, 120));
        }

        // 5. Linha de pedra de sustentação do castelo na base
        janela.fillRectangle(0, Y_CHAO + 22, LARGURA_TELA, 4, new Color(30, 22, 18));
        for (int bx = 20; bx < LARGURA_TELA; bx += 80) {
            janela.fillRectangle(bx, Y_CHAO + 22, 2, 25, new Color(30, 22, 18));
        }
    }

    /**
     * Botão de Pause no canto superior esquerdo com os dois palitos (||).
     */
    private void desenharBotaoPause(JanelaAtiva janela) {
        int mx = janela.getMouseX();
        int my = janela.getMouseY();
        boolean hover = estaDentro(mx, my, PAUSE_X, PAUSE_Y, PAUSE_W, PAUSE_H);

        // Fundo do botão
        Color corFundo = hover ? new Color(55, 65, 100, 220) : new Color(30, 35, 60, 190);
        Color corBorda = hover ? new Color(130, 150, 210) : new Color(80, 95, 140);
        janela.fillRectangle(PAUSE_X, PAUSE_Y, PAUSE_W, PAUSE_H, corFundo);
        janela.drawRectangle(PAUSE_X, PAUSE_Y, PAUSE_W, PAUSE_H, corBorda);

        // Dois palitos verticais (||)
        Color corPalitos = hover ? Color.WHITE : new Color(220, 230, 255);
        int palitoLargura = 5;
        int palitoAltura = 18;
        int palitoY = PAUSE_Y + 10;

        // Palito da esquerda
        janela.fillRectangle(PAUSE_X + 11, palitoY, palitoLargura, palitoAltura, corPalitos);
        // Palito da direita
        janela.fillRectangle(PAUSE_X + 22, palitoY, palitoLargura, palitoAltura, corPalitos);
    }

    /**
     * Pontuação exibida no canto superior direito.
     */
    private void desenharPontuacao(JanelaAtiva janela) {
        int w = 180;
        int h = 38;
        int x = LARGURA_TELA - w - 20; // 600
        int y = 15;

        // Caixa da pontuação
        janela.fillRectangle(x, y, w, h, new Color(25, 30, 52, 200));
        janela.drawRectangle(x, y, w, h, new Color(80, 95, 140));

        // Texto da Pontuação
        janela.drawText("PONTOS: " + pontuacao, x + 18, y + 12, 18, new Color(255, 215, 0));
    }

    /**
     * Desenha o painel lateral que representa visualmente a estrutura de dados
     * ArrayList<Balloon> em tempo real durante a partida.
     */
    private void desenharPainelArrayList(JanelaAtiva janela) {
        int panelX = 600;
        int panelY = 60;
        int panelW = 180;
        int panelH = 320;

        // 1. Fundo do painel lateral com estilo semi-transparente elegante
        janela.fillRectangle(panelX, panelY, panelW, panelH, new Color(22, 26, 44, 215));
        janela.drawRectangle(panelX, panelY, panelW, panelH, new Color(80, 95, 140));
        janela.drawRectangle(panelX + 1, panelY + 1, panelW - 2, panelH - 2, new Color(40, 50, 75));

        // 2. Cabeçalho da estrutura de dados
        janela.drawText("ESTRUTURA DE DADOS", panelX + 10, panelY + 10, 10, new Color(130, 185, 245));
        janela.drawText("ArrayList<Balloon>", panelX + 10, panelY + 23, 14, Color.YELLOW);

        int tam = (listaBaloes != null) ? listaBaloes.size() : 0;
        String infoCapacidade = "size: " + tam + " | cap. visual: 6";
        janela.drawText(infoCapacidade, panelX + 10, panelY + 40, 11, new Color(200, 215, 235));

        // Linha divisória
        janela.drawLine(panelX + 8, panelY + 54, panelX + panelW - 8, panelY + 54, new Color(60, 75, 110));

        // 3. Slots de memória indexados do ArrayList (índices [0] a [5])
        int maxSlots = 6;
        int slotH = 34;
        int startSlotY = panelY + 60;

        for (int i = 0; i < maxSlots; i++) {
            int slotY = startSlotY + (i * (slotH + 4));
            int boxX = panelX + 34;
            int boxW = panelW - 44; // 136 px

            // Rótulo do índice [i]
            Color corIndice = (i < tam) ? new Color(120, 200, 255) : new Color(120, 130, 150);
            janela.drawText("[" + i + "]", panelX + 8, slotY + 11, 11, corIndice);

            boolean slotOcupado = (i < tam);
            boolean slotDestacado = (i == ultimoIndiceModificado && timerDestaqueSlot > 0);

            if (slotOcupado) {
                Balloon b = listaBaloes.get(i);

                // Fundo do slot ocupado com destaque visual para alterações recentes
                Color corFundoSlot = slotDestacado ? new Color(45, 65, 105, 240) : new Color(30, 36, 60, 210);
                Color corBordaSlot = slotDestacado ? corLogOperacao : new Color(65, 80, 120);

                janela.fillRectangle(boxX, slotY, boxW, slotH, corFundoSlot);
                janela.drawRectangle(boxX, slotY, boxW, slotH, corBordaSlot);

                // Mini balãozinho
                double bx = boxX + 16.0;
                double by = slotY + 17.0;
                double br = 8.5;

                janela.fillCircle(bx, by, br, b.getColor());
                janela.drawCircle(bx, by, br, new Color(20, 20, 20, 160));
                janela.fillCircle(bx, by + br, 1.8, b.getColor().darker());

                // Desenho do símbolo dentro do mini balão
                desenharMiniSimbolo(janela, b.getSymbol(), bx, by);

                // Nome do símbolo no slot
                String nomeSimb = nomeCurto(b.getSymbol());
                janela.drawText(nomeSimb, boxX + 32, slotY + 11, 11, Color.WHITE);

            } else {
                // Slot vazio / livre (null)
                janela.fillRectangle(boxX, slotY, boxW, slotH, new Color(18, 22, 36, 140));
                janela.drawRectangle(boxX, slotY, boxW, slotH, new Color(45, 52, 75));
                janela.drawText("null (livre)", boxX + 28, slotY + 11, 10, new Color(100, 112, 135));
            }
        }

        // Indicador caso haja mais balões que o limite visual dos 6 slots
        if (tam > maxSlots) {
            String excedente = "+ " + (tam - maxSlots) + " no array...";
            janela.drawText(excedente, panelX + 38, startSlotY + (5 * (slotH + 4)) + 24, 9, Color.YELLOW);
        }

        // 4. Rodapé do painel com a última operação executada (add ou remove com shift)
        int footerY = panelY + panelH - 24;
        janela.drawLine(panelX + 8, footerY - 5, panelX + panelW - 8, footerY - 5, new Color(60, 75, 110));
        janela.drawText("Op: " + logOperacaoLista, panelX + 10, footerY + 4, 10, corLogOperacao);
    }

    private void desenharMiniSimbolo(JanelaAtiva janela, Symbol s, double bx, double by) {
        if (s == null) return;
        Color sc = Color.WHITE;
        double sz = 4.0;

        switch (s) {
            case LINHA_HORIZONTAL:
                janela.drawLine(bx - sz, by, bx + sz, by, sc);
                break;
            case LINHA_VERTICAL:
                janela.drawLine(bx, by - sz, bx, by + sz, sc);
                break;
            case V_NORMAL:
                janela.drawLine(bx - sz, by - sz + 1, bx, by + sz, sc);
                janela.drawLine(bx, by + sz, bx + sz, by - sz + 1, sc);
                break;
            case V_INVERTIDO:
                janela.drawLine(bx - sz, by + sz - 1, bx, by - sz, sc);
                janela.drawLine(bx, by - sz, bx + sz, by + sz - 1, sc);
                break;
            case CIRCULO:
                janela.drawCircle(bx, by, sz - 1, sc);
                break;
            case Z:
                janela.drawLine(bx - sz, by - sz, bx + sz, by - sz, sc);
                janela.drawLine(bx + sz, by - sz, bx - sz, by + sz, sc);
                janela.drawLine(bx - sz, by + sz, bx + sz, by + sz, sc);
                break;
        }
    }

    private String nomeCurto(Symbol s) {
        if (s == null) return "Balão";
        switch (s) {
            case LINHA_HORIZONTAL: return "Linha (—)";
            case LINHA_VERTICAL:   return "Vertical (|)";
            case V_NORMAL:         return "V";
            case V_INVERTIDO:      return "V Invert. (^)";
            case CIRCULO:          return "Círculo (O)";
            case Z:                return "Z";
            default:               return s.name();
        }
    }

    /**
     * Mostra vidas e a mensagem de feedback do último gesto desenhado.
     */
    private void desenharStatus(JanelaAtiva janela) {
        // Vidas ao lado do botão de pause
        String vidasTexto = "VIDAS: ";
        for (int i = 0; i < vidas; i++) {
            vidasTexto += "♥ ";
        }
        janela.drawText(vidasTexto, PAUSE_X + PAUSE_W + 15, 27, 16, new Color(220, 40, 50));

        // Cronômetro e nível de velocidade atual
        int minutos = (int) (tempoDeJogo / 60);
        int segundos = (int) (tempoDeJogo % 60);
        int segRestantes = (int) (INTERVALO_AUMENTO_VELOCIDADE - (tempoDeJogo % INTERVALO_AUMENTO_VELOCIDADE));
        String infoDificuldade = String.format("TEMPO: %02d:%02d | VEL: Nív. %d (+%02ds)", 
                minutos, segundos, nivelVelocidade + 1, segRestantes);
        janela.drawText(infoDificuldade, PAUSE_X + PAUSE_W + 150, 27, 14, new Color(35, 50, 75));

        // Mensagem de feedback do traço / estouro de balão com fundo transparente
        if (!feedbackMensagem.isEmpty()) {
            int fontSize = 14;
            int textWidth = janela.measureText(feedbackMensagem, fontSize);
            int badgeW = textWidth + 24;
            int badgeH = 28;
            int badgeX = PAUSE_X + 5;
            int badgeY = 60;

            // Fundo transparente neutro com borda sutil
            janela.fillRectangle(badgeX, badgeY, badgeW, badgeH, new Color(0, 0, 0, 90));
            janela.drawRectangle(badgeX, badgeY, badgeW, badgeH, new Color(255, 255, 255, 60));

            // Texto do log centralizado no meio do retângulo (horizontal e verticalmente)
            int textX = badgeX + (badgeW - textWidth) / 2;
            int textY = badgeY + (badgeH - fontSize) / 2;
            janela.drawText(feedbackMensagem, textX, textY, fontSize, corFeedback);
        }
    }

    /**
     * Tela de Jogo Pausado com opções de controle.
     */
    private void desenharMenuPause(JanelaAtiva janela) {
        // Overlay translúcido
        janela.fillRectangle(0, 0, LARGURA_TELA, ALTURA_TELA, new Color(0, 0, 0, 185));

        // Painel central de Pause
        int pw = 430;
        int ph = 210;
        int px = (LARGURA_TELA - pw) / 2;
        int py = (ALTURA_TELA - ph) / 2;

        janela.fillRectangle(px, py, pw, ph, new Color(28, 32, 52));
        janela.drawRectangle(px, py, pw, ph, new Color(90, 110, 170));
        janela.drawRectangle(px + 2, py + 2, pw - 4, ph - 4, new Color(90, 110, 170));

        // Título centralizado
        String titPause = "PAUSADO";
        int titW = janela.measureText(titPause, 26);
        janela.drawText(titPause, px + (pw - titW) / 2, py + 32, 26, Color.WHITE);

        // Opções de controle alinhadas com margem interna confortável
        int opX = px + 40;
        janela.drawText("► Pressione ESPAÇO ou P para Continuar", opX, py + 90, 14, Color.YELLOW);
        janela.drawText("► Pressione R para Reiniciar", opX, py + 125, 14, new Color(200, 215, 240));
        janela.drawText("► Pressione M para Menu Principal", opX, py + 160, 14, new Color(200, 215, 240));
    }

    private void desenharGameOver(JanelaAtiva janela) {
        janela.fillRectangle(0, 0, LARGURA_TELA, ALTURA_TELA, new Color(0, 0, 0, 200));

        int pw = 460;
        int ph = 230;
        int px = (LARGURA_TELA - pw) / 2;
        int py = (ALTURA_TELA - ph) / 2;

        janela.fillRectangle(px, py, pw, ph, new Color(25, 28, 42));
        janela.drawRectangle(px, py, pw, ph, new Color(230, 57, 70));
        janela.drawRectangle(px + 2, py + 2, pw - 4, ph - 4, new Color(230, 57, 70));

        // Título GAME OVER centralizado
        String titulo = "GAME OVER";
        int tituloW = janela.measureText(titulo, 30);
        janela.drawText(titulo, px + (pw - tituloW) / 2, py + 35, 30, new Color(230, 57, 70));

        // Pontuação Final centralizada
        String scoreTexto = "Pontuação Final: " + pontuacao;
        int scoreW = janela.measureText(scoreTexto, 18);
        janela.drawText(scoreTexto, px + (pw - scoreW) / 2, py + 95, 18, Color.WHITE);

        // Texto amarelo centralizado com ampla folga dentro da caixa
        String reiniciarTexto = "Pressione ESPAÇO para jogar novamente";
        int reiniciarW = janela.measureText(reiniciarTexto, 16);
        janela.drawText(reiniciarTexto, px + (pw - reiniciarW) / 2, py + 155, 16, Color.YELLOW);
    }

    @Override
    public String getTitulo() {
        return "Estoura Balão - Magic Touch";
    }

    /**
     * Partícula simples para efeitos de estouro de balão.
     */
    private static class Particula {
        double x, y;
        double vx, vy;
        double vida;
        double vidaMaxima;
        Color cor;

        public Particula(double x, double y, Color cor, Random r) {
            this.x = x;
            this.y = y;
            double angulo = r.nextDouble() * 2 * Math.PI;
            double vel = 60 + r.nextDouble() * 140;
            this.vx = Math.cos(angulo) * vel;
            this.vy = Math.sin(angulo) * vel;
            this.vidaMaxima = 0.3 + r.nextDouble() * 0.4;
            this.vida = this.vidaMaxima;
            this.cor = cor;
        }

        public void update(double delta) {
            x += vx * delta;
            y += vy * delta;
            vy += 220 * delta; // Gravidade na partícula
            vida -= delta;
        }

        public boolean isMorta() {
            return vida <= 0;
        }

        public void draw(EngineFrame e) {
            double alpha = Math.max(0.0, vida / vidaMaxima);
            int a = (int) (alpha * 255);
            Color c = new Color(cor.getRed(), cor.getGreen(), cor.getBlue(), a);
            e.fillCircle(x, y, 3, c);
        }
    }
}
