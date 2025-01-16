package org.luckyshotserver.Models;

import org.luckyshotserver.Models.Consumables.Consumable;

import java.util.ArrayList;

public abstract class Player {
    private int lives;
    private ArrayList<Consumable> consumables;
    private boolean isShieldActive = false;
    private boolean isPoisoned = false;
    private boolean isHandcuffed = false;
    private boolean isResurrected = false;

    public Player() {
        consumables = new ArrayList<>();
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public ArrayList<Consumable> getConsumables() {
        return consumables;
    }

    public void setConsumables(ArrayList<Consumable> consumables) {
        this.consumables = consumables;
    }

    public void removeConsumable(Consumable consumable) {
        this.consumables.remove(consumable);
    }

    public int getConsumablesNumber() {
        return consumables.size();
    }

    public boolean isShieldActive() {
        return isShieldActive;
    }

    public void setShieldActive(boolean shieldActive) {
        isShieldActive = shieldActive;
    }

    public boolean isPoisoned() {
        return isPoisoned;
    }

    public void setPoisoned(boolean poisoned) {
        isPoisoned = poisoned;
    }

    public boolean isHandcuffed() {
        return isHandcuffed;
    }

    public void setHandcuffed(boolean handcuffed) {
        isHandcuffed = handcuffed;
    }

    public boolean isResurrected() {
        return isResurrected;
    }

    public void setResurrected(boolean resurrected) {
        isResurrected = resurrected;
    }
}
