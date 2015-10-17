package de.codesourcery.toyai;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Player extends Entity {

    public final String name;
    public final Color color;
    
    public final List<Entity> ownedEntities = new ArrayList<>();
    
    public Player(String name,Color color) 
    {
        super(EntityType.PLAYER,null);
        this.name = name;
        this.color = color;
    }
    
    public void add(Entity entity) {
        ownedEntities.add(entity);
    }
    
    @Override
    public String toString() {
        return "Player "+name;
    }
}
