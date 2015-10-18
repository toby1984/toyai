package de.codesourcery.toyai.entities;

import java.awt.Color;

import de.codesourcery.toyai.Entity;
import de.codesourcery.toyai.IBlackboard;

public class Player extends Entity {

    public final String name;
    public final Color color;
    
    public Player(String name,Color color,IBlackboard blackboard) 
    {
        super(EntityType.PLAYER,null,blackboard);
        this.name = name;
        this.color = color;
    }
    
    @Override
    public String toString() {
        return "Player "+name;
    }
}