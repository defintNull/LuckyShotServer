package org.luckyshotserver.Facades;

import org.java_websocket.WebSocket;
import org.luckyshotserver.Facades.Services.Converters.ObjectConverter;
import org.luckyshotserver.Facades.Services.Server;
import org.luckyshotserver.Models.*;
import org.luckyshotserver.Models.StateEffects.StateEffect;
import org.luckyshotserver.Models.StateEffects.StateEffectInterface;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Random;

public class MultiplayerGameFacade {
    private final int N_MAX_PLAYERS;
    private ArrayList<User> users;
    private final int MAX_ROUNDS = 3;
    private ArrayList<WebSocket> webSockets;
    private MultiplayerGame game;
    private Server server = Server.getInstance();
    ObjectConverter converter = new ObjectConverter();

    public MultiplayerGameFacade(int MAX_ROOM_PLAYERS) {
        this.N_MAX_PLAYERS = MAX_ROOM_PLAYERS;
        server = Server.getInstance();
    }

    public void start(ArrayList<WebSocket> webSockets) {
        ArrayList<User> users = new ArrayList<>();
        ArrayList<HumanPlayer> players = new ArrayList<>();
        this.webSockets = new ArrayList<>(webSockets);

        for(int i = 0; i < N_MAX_PLAYERS; i++) {
            users.add(server.getUserFromWebSocket(webSockets.get(i)));
            players.add(new HumanPlayer(users.get(i).getUsername(), users.get(i).getPowerups()));
        }

        this.game = new MultiplayerGame(players);

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

            System.out.println(converter.objToJSON(game.getRound()));
            for(int i = 0; i < N_MAX_PLAYERS; i++) {
                server.sendOk(webSockets.get(i), converter.objToJSON(game.getRound()));
            }
            //singlePlayerGameView.showRoundStartingScreen(roundNumber);
            //singlePlayerGameView.showStateEffectActivation(stateEffect.getActivation());

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
