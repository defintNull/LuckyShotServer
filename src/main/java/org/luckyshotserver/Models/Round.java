package org.luckyshotserver.Models;

import org.luckyshotserver.Models.StateEffects.StateEffect;

public class Round {
    private int roundNumber;
    private StateEffect stateEffect;
    private Turn turn;
    private int maxLives;

    public Round(int n, StateEffect stateEffect) {
        this.roundNumber = n;
        this.stateEffect = stateEffect;
    }

    public Turn getTurn() {
        return turn;
    }

    public void setTurn(Turn turn) {
        this.turn = turn;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public StateEffect getStateEffect() {
        return stateEffect;
    }

    public void setStateEffect(StateEffect stateEffect) {
        this.stateEffect = stateEffect;
    }

    public int getMaxLives() {
        return maxLives;
    }

    public void setMaxLives(int maxLives) {
        this.maxLives = maxLives;
    }
}
