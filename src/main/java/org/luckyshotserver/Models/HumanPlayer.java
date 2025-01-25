package org.luckyshotserver.Models;

import org.luckyshotserver.Models.Powerups.Powerup;

import java.util.ArrayList;
import java.util.HashMap;

public class HumanPlayer extends Player{
    private String username;
    private int score;
    private double multiplier;
    private int comboCounter;
    private HashMap<Powerup, Integer> powerups;
    private int xp;

    public HumanPlayer(String username, HashMap<Powerup, Integer> powerups) {
        this.username = username;
        this.powerups = powerups;
        this.xp = 0;
        this.multiplier = 1;
        this.comboCounter = 0;
        this.score = 0;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int score) {
        this.score += score;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public void incrementMultiplier() {
        this.multiplier += 0.5;
    }

    public void incrementMultiplier(double multiplier) {
        this.multiplier += multiplier;
    }

    public int getComboCounter() {
        return comboCounter;
    }

    public void setComboCounter(int comboCounter) {
        this.comboCounter = comboCounter;
    }

    public void incrementComboCounter() {
        this.comboCounter += 1;
    }

    public void incrementComboCounter(int comboCounter) {
        this.comboCounter += comboCounter;
    }

    public HashMap<Powerup, Integer> getPowerups() {
        return powerups;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public void addXp(int xp) {
        this.xp += xp;
    }
}
