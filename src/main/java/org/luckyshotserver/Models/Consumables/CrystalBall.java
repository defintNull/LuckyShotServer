package org.luckyshotserver.Models.Consumables;

import org.luckyshotserver.Models.Enums.Probability;
import org.luckyshotserver.Models.Gun;
import org.luckyshotserver.Models.MultiplayerGame;

import java.util.Random;

public class CrystalBall extends Consumable {
    private static CrystalBall instance;
    private CrystalBall() {
        super(Probability.MEDIUM_LOW);
    }

    public static CrystalBall getInstance() {
        if(instance == null) {
            instance = new CrystalBall();
        }
        return instance;
    }

    public String use(MultiplayerGame multiplayerGame) {
        Gun gun = Gun.getInstance();
        Random rand = new Random();
        int r = rand.nextInt(0, gun.getBullets().size());
        int type = gun.getBullet(r).getType();

        return String.valueOf(gun.getBullets().size() - r) + type;
    }

    public String getEffect(String parameters) {
        char position = parameters.charAt(0);
        char type = parameters.charAt(1);
        return "The bullet in position " + position + " is " + (type == '1' ? "live" : "fake");
    }

    public String toString() {
        return "Crystal ball";
    }

    public boolean visibilityEffect() {
        return false;
    }
}
