package org.luckyshotserver.Models.Consumables;

import org.luckyshotserver.Models.Enums.Probability;
import org.luckyshotserver.Models.Gun;
import org.luckyshotserver.Models.MultiplayerGame;

public class Inverter extends Consumable{
    private static Inverter instance;
    private Inverter() {
        super(Probability.HIGH);
    }

    public static Inverter getInstance() {
        if(instance == null) {
            instance = new Inverter();
        }
        return instance;
    }

    public String use(MultiplayerGame multiplayerGame) {
        int type = Gun.getInstance().getBullet(Gun.getInstance().getBullets().size() - 1).getType();
        Gun.getInstance().getBullet(Gun.getInstance().getBullets().size() - 1).setType((type + 1) % 2);
        return "NULL";
    }

    public String getEffect(String effect) {
        return "Bullet inverted!";
    }

    public String toString() {
        return "Inverter";
    }
}
