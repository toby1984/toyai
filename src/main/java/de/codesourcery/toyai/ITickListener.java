package de.codesourcery.toyai;

public interface ITickListener 
{
    public boolean tick(float deltaSeconds);
    
    public default void onTickListenerRemove() {
    }
}
