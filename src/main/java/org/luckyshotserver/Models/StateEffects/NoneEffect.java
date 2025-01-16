package org.luckyshotserver.Models.StateEffects;

public class NoneEffect extends StateEffect{
    private static NoneEffect instance;

    private NoneEffect() {

    }

    public String getEffect() {
        return "No state effect for this round";
    }

    public static NoneEffect getInstance() {
        if(instance == null) {
            instance = new NoneEffect();
        }
        return instance;
    }

    public String toString() {
        return "No Effect";
    }
}
