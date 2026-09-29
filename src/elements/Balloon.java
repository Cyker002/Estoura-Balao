/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package elements;

/**
 *
 * @author raul
 */
public class Balloon {
    private Symbol symbol;
    private boolean burst;
    
    public Balloon(Symbol symbol){
        this.symbol = symbol;
        this.burst= false;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public boolean isBurst() {
        return burst;
    }
    
    public void pop(){
        this.burst =  burst;
    }    
}
