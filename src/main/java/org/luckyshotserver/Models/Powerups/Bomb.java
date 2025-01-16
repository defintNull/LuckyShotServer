package org.luckyshotserver.Models.Powerups;

import org.luckyshotserver.Models.SinglePlayerGame;

import java.io.Serializable;

public class Bomb extends Powerup {
    private static Bomb instance;
    private static final int COST = 20;

    private Bomb() {
        super(COST);
    }

    public void use(SinglePlayerGame singlePlayerGame) {
        singlePlayerGame.getHumanPlayer().setLives(singlePlayerGame.getHumanPlayer().getLives() - 1);
        singlePlayerGame.getBot().setLives(singlePlayerGame.getBot().getLives() - 1);

    }

    public static Bomb getInstance() {
        if(instance == null) {
            instance = new Bomb();
        }
        return instance;
    }

    public String toString() {
        return "Bomb";
    }

    public String getEffect() {
        return "Every players lose a life!";
    }
}
