package org.luckyshotserver.Models.StateEffects;

public class Fog extends StateEffect{
    private static Fog instance;

    private Fog() {

    }

    public String getActivation() {
        return "There's some fog on the field!";
    }

    public String getEffect() {
        return "For this round your vision lacks of luck";
    }

    public static Fog getInstance() {
        if(instance == null) {
            instance = new Fog();
        }
        return instance;
    }

    public String toString() {
        return "Fog";
    }
}
