package org.luckyshotserver.Models.Powerups;

import org.luckyshotserver.Models.SinglePlayerGame;

public class PoisonBullet extends Powerup{
    private static PoisonBullet instance;
    private static final int COST = 10;

    private PoisonBullet() {
        super(COST);
    }

    public void use(SinglePlayerGame singlePlayerGame) {
        singlePlayerGame.getRound().getTurn().setBulletPoisoned(true);
    }

    public static PoisonBullet getInstance() {
        if(instance == null) {
            instance = new PoisonBullet();
        }
        return instance;
    }

    public String toString() {
        return "Poison bullet";
    }

    public String getEffect() {
        return "Poison remove a life!";
    }
}
