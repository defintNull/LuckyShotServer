package org.luckyshotserver.Models;

import org.luckyshotserver.Models.Consumables.ConsumableInterface;

import java.util.Random;

public class BotPlayer extends Player{
    public String getConsumableInput() {
        return "use " + getRandomConsumableLetter();
    }

    public String getRandomConsumableLetter() {
        String options = "";
        String alphabet = "abcdefghijklmnopqrstuwxyz";
        for(int i = 0; i < ConsumableInterface.getConsumableClassList().size(); i++) {
            for(int j = 0; j < this.getConsumables().size(); j++) {
                if(this.getConsumables().get(j).getClass().equals(ConsumableInterface.getConsumableClassList().get(i))){
                    options += alphabet.charAt(i);
                }
            }
        }
        Random rand = new Random();
        return Character.toString(options.charAt(rand.nextInt(0, options.length())));
    }

    public String getShootingInput() {
        Random rand = new Random();
        return "shoot " + rand.nextInt(1, 3);
    }

    public String getInput() {
        Random rand = new Random();
        String output;
        if(rand.nextInt(2) == 0 && !getConsumables().isEmpty()) {
            output = getConsumableInput();
        }
        else {
            output = getShootingInput();
        }
        return output;
    }

    public boolean isShieldActive() {
        return false;
    }
}
