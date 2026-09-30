/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui.telas;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;

/**
 *
 * @author raul
 */
public class JanelaAtiva extends EngineFrame {
    
    private Tela JanelaAtiva;
    
    public JanelaAtiva (  ) {
        super(800, 450, "Janela Ativa" ,60 ,true);
    }
    
    @Override
    public void create () {
        JanelaAtiva = new MenuInicial();
        JanelaAtiva.create ();
        this.setTitle(JanelaAtiva.getTitulo());
    }

    @Override
    public void update ( double delta ) {
        JanelaAtiva.update ( delta , this );
        
    }

    @Override
    public void draw () {
       JanelaAtiva.draw ( this );
    }
    
    public void mudarTela(Tela telaMudar){
        
        this.JanelaAtiva = telaMudar;
        this.JanelaAtiva.create ();
        this.setTitle(JanelaAtiva.getTitulo());

    }
    
    public static void main ( String[] args ) {
        
        new JanelaAtiva();
        
    }
}
