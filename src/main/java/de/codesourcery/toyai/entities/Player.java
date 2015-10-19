package de.codesourcery.toyai.entities;

import java.awt.Color;

import de.codesourcery.toyai.Entity;
import de.codesourcery.toyai.IBlackboard;

public class Player extends Entity {

    public final String name;
    public final Color color;
    private int entityCount;

    public Player(String name,Color color,IBlackboard blackboard)
    {
        super(EntityType.PLAYER,null,blackboard);
        this.name = name;
        this.color = color;
    }

    public void incOwnedEntityCount() {
    	entityCount++;
    }

    private void decOwnedEntityCount()
    {
    	if ( entityCount == 0 ) {
    		throw new IllegalStateException();
    	}
    	entityCount--;
    }

    @Override
    public void childRemovedFromWorld(Entity child)
    {
    	if ( child.is( EntityType.TANK ) ) {
    		decOwnedEntityCount();
    	}
    }

    public int getEntityCount() {
		return entityCount;
	}

    public String getName() {
		return name;
	}

    @Override
    public String toString() {
        return "Player "+name;
    }

	@Override
	public boolean isAlive() {
		return true;
	}
}