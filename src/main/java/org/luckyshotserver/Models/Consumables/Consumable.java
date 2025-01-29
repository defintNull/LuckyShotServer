package org.luckyshotserver.Models.Consumables;

import org.luckyshotserver.Models.Enums.Probability;
import org.luckyshotserver.Models.MultiplayerGame;

public abstract class Consumable implements ConsumableInterface{
    private final int probability;

    protected Consumable(Probability probability) {
        this.probability = probabilityRange.get(probability);
    }

    public int getProbability() {
        return probability;
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }

    public String use(MultiplayerGame multiplayerGame) {
        return "Nothing happened!";
    }

    public String getEffect(String parameters) {
        return "No effect";
    }

    public boolean visibilityEffect() {
        return true;
    }
}
