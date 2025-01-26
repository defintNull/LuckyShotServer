package org.luckyshotserver.Facades;

import org.java_websocket.WebSocket;
import org.luckyshotserver.Facades.Services.Converters.ObjectConverter;
import org.luckyshotserver.Facades.Services.Server;
import org.luckyshotserver.Models.*;
import org.luckyshotserver.Models.Enums.MessageEnum;
import org.luckyshotserver.Models.Powerups.Powerup;
import org.luckyshotserver.Models.StateEffects.StateEffect;
import org.luckyshotserver.Models.StateEffects.StateEffectInterface;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MultiplayerGameFacade {
    private final int N_MAX_PLAYERS;
    private ArrayList<User> users = new ArrayList<>();
    private final int MAX_ROUNDS = 3;
    private ArrayList<WebSocket> webSockets;
    private MultiplayerGame game;
    private Server server = Server.getInstance();
    ObjectConverter converter = new ObjectConverter();
    ArrayList<HumanPlayer> players = new ArrayList<>();

    public MultiplayerGameFacade(ArrayList<WebSocket> webSockets) {
        this.N_MAX_PLAYERS = webSockets.size();
        server = Server.getInstance();
        this.webSockets = new ArrayList<>(webSockets);
        for(int i = 0; i < N_MAX_PLAYERS; i++) {
            users.add(server.getUserFromWebSocket(webSockets.get(i)));
            players.add(new HumanPlayer(users.get(i).getUsername(), users.get(i).getPowerups()));
        }
        System.out.println(users);

        this.game = new MultiplayerGame(players);
    }

    public void start() {
        boolean gameEnded = false;
        int roundNumber = 1;

        while(!gameEnded) {
            //Inizio di un round
            StateEffect stateEffect = getRandomStateEffect();
            Round round = new Round(roundNumber, stateEffect);
            Gun.getInstance().clearBullets();

            this.game.setRound(round);
            Random rnd = new Random();
            int randomLives = rnd.nextInt(2, 5);
            this.game.getRound().setMaxLives(randomLives);
            for(int i = 0; i < N_MAX_PLAYERS; i++) {
                this.game.getHumanPlayers().get(i).setResurrected(false);
                this.game.getHumanPlayers().get(i).setLives(randomLives);
                this.game.getHumanPlayers().get(i).setConsumables(new ArrayList<>());
            }

            boolean roundEnded = false;
            Random random = new Random();
            int turn = random.nextInt(N_MAX_PLAYERS);

            for(int i = 0; i < N_MAX_PLAYERS; i++) {
                server.sendCustom(webSockets.get(i), MessageEnum.SHOW_GAME_STATE, getGameMap());
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                server.sendCustom(webSockets.get(i), MessageEnum.ADD_ACTION, "ROUND_NUMBER," + String.valueOf(roundNumber));
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                server.sendCustom(webSockets.get(i), MessageEnum.ADD_ACTION, "STATE_EFFECT," + stateEffect.getClass().getSimpleName());
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                server.sendCustom(webSockets.get(i), MessageEnum.SHOW, "ACTIONS");
            }



            while(!roundEnded) {
                //Inizio di un turno
                Player currentPlayer = players.get(turn);
                Player otherPlayer = players.get((turn + 1) % 2);

                Turn currentTurn = new Turn(currentPlayer, otherPlayer);
                this.game.getRound().setTurn(currentTurn);

                processTurn();

                // Condizione di fine round
                int nDeaths = 0;
                int iPlayerAlive = 0;
                for(int i = 0; i < N_MAX_PLAYERS; i++) {
                    if(players.get(i).getLives() <= 0) {
                        nDeaths++;
                    } else {
                        iPlayerAlive = i;
                    }
                }

                if(nDeaths == N_MAX_PLAYERS - 1) {
                    roundEnded = true;
                    roundNumber += 1;

                    game.getHumanPlayers().get(iPlayerAlive).addXp(30);
                } else if(nDeaths == N_MAX_PLAYERS) { // Se c'è un pareggio (tipo una bomba) non si aggiunge xp
                    roundEnded = true;
                    roundNumber += 1;
                }

                turn = (turn + 1) % N_MAX_PLAYERS;
            }

            // Condizione di fine gioco
            if(game.getRound().getRoundNumber() >= MAX_ROUNDS) { // ho tolto la condizione sulle vite (vedere SinglePlayerGameFacade)
                gameEnded = true;
            }
        }
        // XP
        for(int i = 0; i < N_MAX_PLAYERS; i++) {
            if (players.get(i).getLives() != 0) {
                users.get(i).setGamesWon(users.get(i).getGamesWon() + 1);
                players.get(i).addXp(120);
            }
        }

        // Passing parameters to users and update in db
        UserFacade userFacade = new UserFacade();
        for(int i = 0; i < N_MAX_PLAYERS; i++) {
            users.get(i).setXp(users.get(i).getXp() + players.get(i).getXp());
            while (users.get(i).getLevel() * 1000 < users.get(i).getXp()) {
                users.get(i).setXp(users.get(i).getXp() - (users.get(i).getLevel() * 1000));
                users.get(i).setLevel(users.get(i).getLevel() + 1);
                users.get(i).addCoins(3);
            }
            users.get(i).setGamesPlayed(users.get(i).getGamesPlayed() + 1);
            userFacade.updateUser(users.get(i));
        }

//        String recv = null;
//        try {
//            recv = client.recv().getFirst();
//        } catch (Exception e) {
//
//        }
//        String status = recv.split(":")[0];
//
//        if(status.equals(MessageEnum.ERROR.getMessage())) {
//            client.send("QUIT:QUIT");
//            client.close();
//        }

//        singlePlayerGameView.showWinner(humanPlayer.getLives() != 0 ? "you" : "bot");
//        singlePlayerGameView.showFinalXp(humanPlayer.getXp());
//        singlePlayerGameView.showLevelAndXp(user.getUsername(), user.getLevel(), user.getXp());
//        singlePlayerGameView.showEndGameScreen();
    }

    /*
    [
        ["roundNumber"]: 1
        ["stateEffect"]: Fog.getSimpleName()

        ["player_1_username]: "prova"
        ["player_1_lives"]: 4
        ["player_1_<Consumable.getSimpleName()>"]: 2
        ["player_1_<Powerup.getSimpleName()"]: 3
        ["player_1_isShieldActive"]
        ["player_1_isPoisoned"]
        ["player_1_isHandcuffed"]
        ["player_1_isResurrected"]
        ["player_1_turn"]: 1

        ["player_2_username]: "prova"
        ["player_2_lives"]: 4
        ["player_2_<Consumable.getSimpleName()>"]: 2
        ["player_2_<Powerup.getSimpleName()"]: 3
        ["player_2_isShieldActive"]
        ["player_2_isPoisoned"]
        ["player_2_isHandcuffed"]
        ["player_2_isResurrected"]
        ["player_2_turn"]: 0

        ["punteggi per player"]


    ]
    */

    public String getGameMap() {
        HashMap<String, String> gameMap = new HashMap<>();

        gameMap.put("roundNumber", String.valueOf(game.getRound().getRoundNumber()));
        gameMap.put("stateEffect", String.valueOf(game.getRound().getStateEffect().getClass().getSimpleName()));

        for(int i = 0; i < N_MAX_PLAYERS; i++) {
            gameMap.put(players.get(i).getUsername(), String.valueOf(i + 1));
            gameMap.put("player_" + (i+1) + "_username", users.get(i).getUsername());
            gameMap.put("player_" + (i+1) + "_lives", String.valueOf(players.get(i).getLives()));
            HashMap<String, Integer> d = new HashMap<>();
            for(int j = 0; j < players.get(i).getConsumables().size(); j++) {
                if(!d.containsKey(players.get(i).getConsumables().get(j).getClass().getSimpleName())) {
                    d.put(players.get(i).getConsumables().get(j).getClass().getSimpleName(), 0);
                }
                d.put(players.get(i).getConsumables().get(j).getClass().getSimpleName(), d.get(players.get(i).getConsumables().get(j).getClass().getSimpleName()) + 1);
                gameMap.put("player_" + (i+1) + "_" + players.get(i).getConsumables().get(j).getClass().getSimpleName(), String.valueOf(d.get(players.get(i).getConsumables().get(j).getClass().getSimpleName())));
            }

            for(Map.Entry<Powerup, Integer> entry : players.get(i).getPowerups().entrySet()) {
                String powerupName = entry.getKey().getClass().getSimpleName();
                String value = String.valueOf(entry.getValue());
                gameMap.put("player_" + (i+1) + "_" + powerupName, value);
            }

            gameMap.put("player_" + (i+1) + "_isShieldActive", String.valueOf(players.get(i).isShieldActive()));
            gameMap.put("player_" + (i+1) + "_isPoisoned", String.valueOf(players.get(i).isPoisoned()));
            gameMap.put("player_" + (i+1) + "_isHandcuffed", String.valueOf(players.get(i).isHandcuffed()));
            gameMap.put("player_" + (i+1) + "_score", String.valueOf(players.get(i).getScore()));
            gameMap.put("player_" + (i+1) + "_comboCounter", String.valueOf(players.get(i).getComboCounter()));
            gameMap.put("player_" + (i+1) + "_xp", String.valueOf(players.get(i).getXp()));

            if(game.getRound().getTurn() != null) {
                if (((HumanPlayer) game.getRound().getTurn().getCurrentPlayer()).getUsername().equals(players.get(i).getUsername())) {
                    gameMap.put("player_" + (i + 1) + "_turn", String.valueOf(1));
                } else {
                    gameMap.put("player_" + (i + 1) + "_turn", String.valueOf(0));
                }
            }
        }

        ObjectConverter converter = new ObjectConverter();
        String map = converter.objToJSON(gameMap);

        System.out.println(map);
        return map;
    }

    private void processTurn() {

    }

    public StateEffect getRandomStateEffect() {
        ArrayList<Class<? extends StateEffect>> stateEffects = StateEffectInterface.getStateEffectClassList();
        Random rand = new Random();
        StateEffect stateEffect = null;
        try {
            Method method = Class.forName(stateEffects.get(rand.nextInt(0, stateEffects.size())).getName()).getMethod("getInstance");
            Object obj = method.invoke(null);
            stateEffect = (StateEffect) obj;
        } catch (Exception e) {
            for (WebSocket webSocket : webSockets) {
                server.sendError(webSocket, "FATAL");
            }
        }

        return stateEffect;
    }
}
