package org.luckyshotserver.Models.Consumables;

import org.luckyshotserver.Models.Enums.Probability;
import org.luckyshotserver.Models.MultiplayerGame;

public class EnergyDrink extends Consumable{
    private static EnergyDrink instance;
    private EnergyDrink() {
        super(Probability.MEDIUM);
    }

    public static EnergyDrink getInstance() {
        if(instance == null) {
            instance = new EnergyDrink();
        }
        return instance;
    }

    public String use(MultiplayerGame multiplayerGame) {

        return "NULL";
    }

    public String getEffect(String effect) {
        return "Now you can steal a consumable...";
    }

    public String toString() {
        return "Energy drink";
    }

    public boolean visibilityEffect() {
        return false;
    }
}
