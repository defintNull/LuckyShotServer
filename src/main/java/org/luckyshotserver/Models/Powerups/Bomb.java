package org.luckyshotserver.Models.Powerups;

import org.luckyshotserver.Models.MultiplayerGame;

public class Bomb extends Powerup {
    private static Bomb instance;
    private static final int COST = 20;

    private Bomb() {
        super(COST);
    }

    public void use(MultiplayerGame multiplayerGame) {
        for(int i = 0; i < multiplayerGame.getHumanPlayers().size(); i++) {
            multiplayerGame.getHumanPlayers().get(i).setLives(multiplayerGame.getHumanPlayers().get(i).getLives() - 1);
        }
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
        return "Every players loses a life!";
    }
}
