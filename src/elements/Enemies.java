/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package elements;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author raul
 */
public class Enemies {
    private List<Balloon> balloons;
    private float x;
    private float y;
    
    public Enemies(float x, float y, Symbol... symbol){
        this.x = x;
        this.y = y;
        balloons = new ArrayList<>();
        for(Symbol s : symbol){
            this.balloons.add(new Balloon(s));
        }
    }
}
