package org.luckyshotserver.Models.Consumables;

import org.luckyshotserver.Models.Enums.Probability;
import org.luckyshotserver.Models.Gun;
import org.luckyshotserver.Models.SinglePlayerGame;

public class GhostGun extends Consumable{
    private static GhostGun instance;
    private GhostGun() {
        super(Probability.MEDIUM_HIGH);
    }

    public static GhostGun getInstance() {
        if(instance == null) {
            instance = new GhostGun();
        }
        return instance;
    }

    public String use(SinglePlayerGame singlePlayerGame) {
        Gun.getInstance().setDamage(2);
        return "";
    }

    public String getEffect(String effect) {
        return "Damage is doubled!";
    }

    public String toString() {
        return "Ghost gun";
    }
}
