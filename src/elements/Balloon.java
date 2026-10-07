package elements;

import java.awt.Color;

/**
 * Representa um balão segurado por um inimigo.
 * Cada balão possui um símbolo que deve ser desenhado para estourá-lo.
 */
public class Balloon {
    private Symbol symbol;
    private boolean burst;
    private Color color;
    
    public Balloon(Symbol symbol){
        this.symbol = symbol;
        this.burst = false;
        this.color = definirCor(symbol);
    }

    private Color definirCor(Symbol s) {
        if (s == null) return new Color(230, 57, 70);
        switch (s) {
            case LINHA_HORIZONTAL: return new Color(230, 57, 70);   // Vermelho
            case LINHA_VERTICAL:   return new Color(69, 123, 157);  // Azul petróleo
            case V_NORMAL:         return new Color(42, 157, 143);  // Verde-azulado
            case V_INVERTIDO:      return new Color(244, 162, 97);  // Laranja
            case CIRCULO:          return new Color(155, 93, 229);  // Roxo
            case Z:                return new Color(241, 91, 181);  // Rosa
            default:               return new Color(230, 57, 70);
        }
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public boolean isBurst() {
        return burst;
    }
    
    public void pop(){
        this.burst = true;
    }

    public Color getColor() {
        return color;
    }
}
