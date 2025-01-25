package org.luckyshotserver.Models;

import java.util.ArrayList;
import java.util.HashMap;

public class MultiplayerGame {
    private ArrayList<HumanPlayer> players;
    private Round round;

    public MultiplayerGame(HumanPlayer[] players) {
        setHumanPlayers(players);
    }

    public HashMap<String, Object> getStateMap() {
        HashMap<String, Object> stateMap = new HashMap<>();

        for(int i = 0; i < players.size(); i++) {
            stateMap.put("player" + String.valueOf(i + 1), this.players.get(i));
        }
        stateMap.put("round", this.round);

        return stateMap;
    }

    public Round getRound() {
        return round;
    }

    public void setRound(Round round) {
        this.round = round;
    }

    public ArrayList<HumanPlayer> getHumanPlayers() {
        return new ArrayList<>(players);
    }

    public void setHumanPlayers(HumanPlayer[] players) {
        assert this.players != null;
        System.arraycopy(players, 0, this.players, 0, players.length);
    }
}
