package org.luckyshotserver.Models.Consumables;

import org.luckyshotserver.Models.Enums.Probability;
import org.luckyshotserver.Models.MultiplayerGame;
import org.luckyshotserver.Models.SinglePlayerGame;

import java.util.Random;

public class MisteryPotion extends Consumable{
    private static MisteryPotion instance;
    private MisteryPotion() {
        super(Probability.MEDIUM);
    }

    public static MisteryPotion getInstance() {
        if(instance == null) {
            instance = new MisteryPotion();
        }
        return instance;
    }

    public String use(MultiplayerGame multiplayerGame) {
        Random rand = new Random();
        int lives = multiplayerGame.getRound().getTurn().getCurrentPlayer().getLives();
        if(rand.nextInt(0, 2) == 0) {
            if(lives < multiplayerGame.getRound().getMaxLives()) {
                multiplayerGame.getRound().getTurn().getCurrentPlayer().setLives(lives + 1);
            }
        } else {
            multiplayerGame.getRound().getTurn().getCurrentPlayer().setLives(lives - 2);
        }

        return "NULL";
    }

    public String getEffect(String effect) {
        return "Hope to stay alive...";
    }

    public String toString() {
        return "Mistery potion";
    }
}
