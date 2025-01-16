package org.luckyshotserver.Models.Consumables;

import org.luckyshotserver.Models.Enums.Probability;
import org.luckyshotserver.Models.SinglePlayerGame;

public class HealthPotion extends Consumable{

    private static HealthPotion instance;
    private HealthPotion() {
        super(Probability.HIGH);
    }

    public static HealthPotion getInstance() {
        if(instance == null) {
            instance = new HealthPotion();
        }
        return instance;
    }

    public String use(SinglePlayerGame singlePlayerGame) {
        singlePlayerGame.getRound().getTurn().getCurrentPlayer().setLives(singlePlayerGame.getRound().getTurn().getCurrentPlayer().getLives() + 1);
        return "";
    }

    public String getEffect(String effect) {
        return "Life restored!";
    }

    public String toString() {
        return "Health potion";
    }
}
