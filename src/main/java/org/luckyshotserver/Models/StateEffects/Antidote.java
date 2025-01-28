package org.luckyshotserver.Models.StateEffects;

public class Antidote extends StateEffect{
    private static Antidote instance;

    private Antidote() {

    }

    public String getActivation() {
        return "No potion will affect any player!";
    }

    public String getEffect() {
        return "Every potion loses its effects.";
    }

    public static Antidote getInstance() {
        if(instance == null) {
            instance = new Antidote();
        }
        return instance;
    }

    public String toString() {
        return "Antidote";
    }
}
