package org.luckyshotserver.Models;

import java.util.HashMap;

public class SinglePlayerGame {
    private HumanPlayer player;
    private BotPlayer bot; // DA VEDERE SE USARLI COME ATTRIBUTO O COME VARIABILE LOCALE
    private Round round;

    public SinglePlayerGame(HumanPlayer player, BotPlayer bot) {
        setHumanPlayer(player);
        setBot(bot);
    }

    public HashMap<String, Object> getStateMap() {
        HashMap<String, Object> stateMap = new HashMap<>();

        stateMap.put("humanPlayer", this.player);
        stateMap.put("bot", this.bot);
        stateMap.put("round", this.round);

        return stateMap;
    }

    public Round getRound() {
        return round;
    }

    public void setRound(Round round) {
        this.round = round;
    }

    public HumanPlayer getHumanPlayer() {
        return player;
    }

    public void setHumanPlayer(HumanPlayer player) {
        this.player = player;
    }

    public BotPlayer getBot() {
        return bot;
    }

    public void setBot(BotPlayer bot) {
        this.bot = bot;
    }
}
