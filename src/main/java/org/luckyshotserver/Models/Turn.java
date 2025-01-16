package org.luckyshotserver.Models;

public class Turn {
    private Gun gun;
    private Player currentPlayer;
    private Player otherPlayer;
    private boolean isBulletPoisoned;

    public Turn(Player currentPlayer, Player otherPlayer) {
        this.gun = Gun.getInstance();
        this.currentPlayer = currentPlayer;
        this.otherPlayer = otherPlayer;
        isBulletPoisoned = false;
    }

    public boolean isBulletPoisoned() {
        return isBulletPoisoned;
    }

    public void setBulletPoisoned(boolean bulletPoisoned) {
        isBulletPoisoned = bulletPoisoned;
    }

    public Player getOtherPlayer() {
        return otherPlayer;
    }

    public void setOtherPlayer(Player otherPlayer) {
        this.otherPlayer = otherPlayer;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }
}
