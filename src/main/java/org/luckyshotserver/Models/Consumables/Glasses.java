package org.luckyshotserver.Models.Consumables;

import org.luckyshotserver.Models.Enums.Probability;
import org.luckyshotserver.Models.Gun;
import org.luckyshotserver.Models.MultiplayerGame;
import org.luckyshotserver.Models.SinglePlayerGame;

public class Glasses extends Consumable{
    private static Glasses instance;
    private Glasses() {
        super(Probability.LOW);
    }

    public static Glasses getInstance() {
        if(instance == null) {
            instance = new Glasses();
        }
        return instance;
    }

    public String use(MultiplayerGame multiplayerGame) {
        int type = Gun.getInstance().getBullets().getLast().getType();
        return String.valueOf(type);
    }

    public String getEffect(String effect) {
        return "The next bullet is " + (effect.equals("1") ? "live" : "fake");
    }

    public String toString() {
        return "Glasses";
    }
}
