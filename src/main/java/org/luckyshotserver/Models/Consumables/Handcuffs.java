package org.luckyshotserver.Models.Consumables;

import org.luckyshotserver.Models.Enums.Probability;
import org.luckyshotserver.Models.MultiplayerGame;

public class Handcuffs extends Consumable{

    private static Handcuffs instance;
    private Handcuffs() {
        super(Probability.LOW);
    }

    public static Handcuffs getInstance() {
        if(instance == null) {
            instance = new Handcuffs();
        }
        return instance;
    }

    public String use(MultiplayerGame multiplayerGame) {
        multiplayerGame.getRound().getTurn().getOtherPlayer().setHandcuffed(true);
        return "NULL";
    }

    public String getEffect(String effect) {
        return "Your opponent is handcuffed...";
    }

    public String toString() {
        return "Handcuffs";
    }

    public boolean visibilityEffect() {
        return false;
    }
}
