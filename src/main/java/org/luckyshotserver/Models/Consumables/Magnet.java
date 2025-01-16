package org.luckyshotserver.Models.Consumables;

import org.luckyshotserver.Models.Enums.Probability;
import org.luckyshotserver.Models.Gun;
import org.luckyshotserver.Models.SinglePlayerGame;

public class Magnet extends Consumable{
    private static Magnet instance;
    private Magnet() {
        super(Probability.HIGH);
    }

    public static Magnet getInstance() {
        if(instance == null) {
            instance = new Magnet();
        }
        return instance;
    }

    public String use(SinglePlayerGame singlePlayerGame) {
        int type = Gun.getInstance().popBullet().getType();
        return String.valueOf(type);
    }

    public String getEffect(String effect) {
        return "Bullet removed! It was " + (effect.equals("1") ? "live" : "fake");
    }

    public String toString() {
        return "Magnet";
    }
}
