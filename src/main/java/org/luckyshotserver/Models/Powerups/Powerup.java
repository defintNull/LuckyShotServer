package org.luckyshotserver.Models.Powerups;

import org.luckyshotserver.Models.SinglePlayerGame;

import java.io.Serializable;

public abstract class Powerup implements PowerupInterface, Serializable {
    private int cost;

    protected  Powerup(int cost) {
        this.cost = cost;
    }

    public void use(SinglePlayerGame singlePlayerGame) {

    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public String toString() {
        return this.getClass().getSimpleName();
    }

    public String getEffect() {
        return "no effect";
    }
}
