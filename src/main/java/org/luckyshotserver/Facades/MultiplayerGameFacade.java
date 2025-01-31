package org.luckyshotserver.Facades;

import org.checkerframework.checker.units.qual.A;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.java_websocket.WebSocket;
import org.luckyshotserver.Facades.Services.Converters.ObjectConverter;
import org.luckyshotserver.Facades.Services.HibernateService;
import org.luckyshotserver.Facades.Services.Server;
import org.luckyshotserver.Models.*;
import org.luckyshotserver.Models.Consumables.*;
import org.luckyshotserver.Models.Enums.MessageEnum;
import org.luckyshotserver.Models.Powerups.Bomb;
import org.luckyshotserver.Models.Powerups.Powerup;
import org.luckyshotserver.Models.Powerups.PowerupInterface;
import org.luckyshotserver.Models.Powerups.Shield;
import org.luckyshotserver.Models.StateEffects.*;

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
    private ArrayList<String> inputBuffer = new ArrayList<>();
    private int turn = 0;
    private ArrayList<Boolean> ack = new ArrayList<>();
    private ArrayList<Integer> wins = new ArrayList<>();

    public ArrayList<WebSocket> getWebSockets() {
        return new ArrayList<>(webSockets);
    }

    public void setInputBuffer(ArrayList<String> inputBuffer) {
        this.inputBuffer = new ArrayList<>(inputBuffer);
    }

    public MultiplayerGameFacade(ArrayList<WebSocket> webSockets) {
        this.N_MAX_PLAYERS = webSockets.size();
        server = Server.getInstance();
        this.webSockets = new ArrayList<>(webSockets);
        for(int i = 0; i < N_MAX_PLAYERS; i++) {
            users.add(server.getUserFromWebSocket(webSockets.get(i)));
            players.add(new HumanPlayer(users.get(i).getUsername(), users.get(i).getPowerups()));
        }

        this.game = new MultiplayerGame(players);

        for(int i = 0; i < N_MAX_PLAYERS; i++) {
            ack.add(false);
            wins.add(0);
        }
    }

    public void setAck(Boolean b, int i) {
        ack.set(i, b);
    }

    public void waitAck(int t, int n) {
        int nAcks = 0;
        while(nAcks < n) {
            nAcks = 0;
            for(int i = 0; i < N_MAX_PLAYERS; i++) {
                if(ack.get(i)) {
                    nAcks += 1;
                }
            }
            try {
                Thread.sleep(t);
            } catch (InterruptedException e) {

            }
        }

        for(int i = 0; i < N_MAX_PLAYERS; i++) {
            ack.set(i, false);
        }
    }

    public void start() {
        boolean gameEnded = false;
        int roundNumber = 1;

        int iPlayerAlive = -1;

        while(!gameEnded) {
            //Problema di sincrono
            try {
                Thread.sleep(1000);
            } catch (Exception e) {

            }
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
            turn = random.nextInt(N_MAX_PLAYERS);

            for(int i = 0; i < N_MAX_PLAYERS; i++) {
                server.sendCustom(webSockets.get(i), MessageEnum.SHOW_GAME_STATE, getGameMap());
            }
            waitAck(50, 2);
            for(int i = 0; i < N_MAX_PLAYERS; i++) {
                server.sendCustom(webSockets.get(i), MessageEnum.ADD_ACTION, "ROUND_NUMBER," + String.valueOf(roundNumber));
            }
            waitAck(50, 2);
            for(int i = 0; i < N_MAX_PLAYERS; i++) {
                server.sendCustom(webSockets.get(i), MessageEnum.ADD_ACTION, "STATE_EFFECT,ACTIVATION," + stateEffect.getClass().getSimpleName());
            }
            waitAck(50, 2);
            for(int i = 0; i < N_MAX_PLAYERS; i++) {
                server.sendCustom(webSockets.get(i), MessageEnum.SHOW, "ACTIONS");
            }
            waitAck(50, 2);

            while(!roundEnded) {
                //Inizio di un turno
                Player currentPlayer = players.get(turn);
                Player otherPlayer = players.get((turn + 1) % N_MAX_PLAYERS);

                Turn currentTurn = new Turn(currentPlayer, otherPlayer);
                this.game.getRound().setTurn(currentTurn);

                processTurn(turn);

                // Condizione di fine round
                int nDeaths = 0;
                iPlayerAlive = -1;
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
                    wins.set(iPlayerAlive, wins.get(iPlayerAlive) + 1);
                    game.getHumanPlayers().get(iPlayerAlive).addXp(30);
                } else if(nDeaths == N_MAX_PLAYERS) { // Se c'è un pareggio (tipo una bomba) non si aggiunge xp
                    roundEnded = true;
                    roundNumber += 1;
                }

                turn = (turn + 1) % N_MAX_PLAYERS;
            }

            for(int i = 0; i < N_MAX_PLAYERS; i++) {
                server.sendCustom(webSockets.get(i), MessageEnum.SHOW_GAME_STATE, getGameMap());
            }
            waitAck(50, 2);

            for(int i = 0; i < N_MAX_PLAYERS; i++) {
                server.sendCustom(webSockets.get(i), MessageEnum.SHOW, "ACTIONS");
            }
            waitAck(50, 2);

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

        for(int i = 0; i < N_MAX_PLAYERS; i++) {
            server.sendCustom(webSockets.get(i), MessageEnum.SHOW_GAME_STATE, getGameMap());
        }
        waitAck(50, 2);

        for(int i = 0; i < N_MAX_PLAYERS; i++) {
            server.sendCustom(webSockets.get(i), MessageEnum.SHOW, "ACTIONS");
        }
        waitAck(50, 2);

        for(int i = 0; i < N_MAX_PLAYERS; i++) {
            server.sendCustom(webSockets.get(i), MessageEnum.SHOW_GAME_STATE, getGameMap());
        }
        waitAck(50, 2);

        for(int i = 0; i < N_MAX_PLAYERS; i++) {
            if (wins.get(0).equals(wins.get(1))) {
                server.sendCustom(webSockets.get(i), MessageEnum.ADD_ACTION, "WIN,-1");
            } else if (wins.get(i) > wins.get((i + 1) % N_MAX_PLAYERS)) {
                server.sendCustom(webSockets.get(i), MessageEnum.ADD_ACTION, "WIN,1");
            } else {
                server.sendCustom(webSockets.get(i), MessageEnum.ADD_ACTION, "WIN,0");
            }
        }
        waitAck(50, 2);
        for (int i = 0; i < N_MAX_PLAYERS; i++) {
            server.sendCustom(webSockets.get(i), MessageEnum.ADD_ACTION, "XP," + players.get(i).getXp());
        }
        waitAck(50, 2);
        for(int i = 0; i < N_MAX_PLAYERS; i++) {
            server.sendCustom(webSockets.get(i), MessageEnum.ADD_ACTION, "SUMMARY," + users.get(i).getUsername() + "," + users.get(i).getLevel() + "," + users.get(i).getXp());
        }
        waitAck(50, 2);
        for (int i = 0; i < N_MAX_PLAYERS; i++) {
            server.sendCustom(webSockets.get(i), MessageEnum.SHOW, "ACTIONS");
        }
        waitAck(50,2);
        for (int i = 0; i < N_MAX_PLAYERS; i++) {
            server.sendCustom(webSockets.get(i), MessageEnum.END, "OK");
        }
        waitAck(50, 2);
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

        String map = converter.objToJSON(gameMap);

        System.out.println(map);
        return map;
    }

    private void processTurn(int turn) {
        Gun gun = Gun.getInstance();
        boolean changeTurn = false;

        // Rimuovo lo scudo al giocatore corrente
        game.getRound().getTurn().getCurrentPlayer().setShieldActive(false);

        // Rimuovo manette
        if(game.getRound().getTurn().getOtherPlayer().isHandcuffed()) {
            game.getRound().getTurn().getOtherPlayer().setHandcuffed(false);
            for(int i = 0; i < N_MAX_PLAYERS; i++) {
                server.sendCustom(webSockets.get(i), MessageEnum.ADD_ACTION, "HANDCUFFS," + ((HumanPlayer)game.getRound().getTurn().getOtherPlayer()).getUsername() + "," + 0 + "," + (i == turn));
            }
            waitAck(50, 2);
        }

        while(!changeTurn) {
            for(int i = 0; i < N_MAX_PLAYERS; i++) {
                server.sendCustom(webSockets.get(i), MessageEnum.SHOW_GAME_STATE, getGameMap());
            }
            waitAck(50, 2);

            for(int i = 0; i < N_MAX_PLAYERS; i++) {
                server.sendCustom(webSockets.get(i), MessageEnum.SHOW, "ACTIONS");
            }
            waitAck(50, 2);

            // Se la pistola è vuota, assegno i consumabili e la ricarico
            if(gun.isEmpty()) {
                drawConsumables();
                for(int i = 0; i < N_MAX_PLAYERS; i++) {
                    server.sendCustom(webSockets.get(i), MessageEnum.SHOW_GAME_STATE, getGameMap());
                }
                waitAck(50, 2);
                for(int i = 0; i < N_MAX_PLAYERS; i++) {
                    server.sendCustom(webSockets.get(i), MessageEnum.SHOW, "ACTIONS");
                }
                waitAck(50, 2);

                loadGun();
                for(int i = 0; i < N_MAX_PLAYERS; i++) {
                    server.sendCustom(webSockets.get(i), MessageEnum.SHOW_GAME_STATE, getGameMap());
                }
                waitAck(50,2);
                for(int i = 0; i < N_MAX_PLAYERS; i++) {
                    server.sendCustom(webSockets.get(i), MessageEnum.SHOW, "ACTIONS");
                }
                waitAck(50, 2);
            }

            if(!game.getRound().getTurn().getCurrentPlayer().isHandcuffed()) {
                boolean validInput = false;
                server.sendCustom(webSockets.get(turn), MessageEnum.INPUT, "ACTION");


                while(inputBuffer.isEmpty()) {
                    try {
                        Thread.sleep(50);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                waitAck(50, 1);

                String target = inputBuffer.get(1);

                if(inputBuffer.get(0).equals("USE")) {
                    validInput = useCommand(target);
                    for(int i = 0; i < N_MAX_PLAYERS; i++) {
                        if(players.get(i).getLives() <= 0) {
                            changeTurn = true;
                        }
                    }
                }
                else if(inputBuffer.get(0).equals("SHOOT")) {
                    if(target.equals("1") || target.equals("2")) {
                        changeTurn = shootingPhase(target);
                        validInput = true;
                    }
                }

                if(!validInput) {
                    server.sendCustom(webSockets.get(turn), MessageEnum.ADD_ERROR, "COMMAND");
                    waitAck(50, 1);
                    server.sendCustom(webSockets.get(turn), MessageEnum.REFRESH, "ALL");
                    waitAck(50, 1);
                    server.sendCustom(webSockets.get(turn), MessageEnum.SHOW_ERROR, "COMMAND");
                    waitAck(50, 1);
                }

                inputBuffer.clear();
            } else {
                for(int i = 0; i < N_MAX_PLAYERS; i++) {
                    server.sendCustom(webSockets.get(i), MessageEnum.ADD_ACTION, "HANDCUFFS," + ((HumanPlayer)game.getRound().getTurn().getOtherPlayer()).getUsername() + "," + 1 + "," + (i == turn));
                }
                waitAck(50, 2);
                for(int i = 0; i < N_MAX_PLAYERS; i++) {
                    server.sendCustom(webSockets.get(i), MessageEnum.REFRESH, "ALL");
                }
                waitAck(50, 2);
                for(int i = 0; i < N_MAX_PLAYERS; i++) {
                    server.sendCustom(webSockets.get(i), MessageEnum.SHOW, "ACTIONS");
                }
                waitAck(50, 2);
                changeTurn = true;
            }
        }
        //Poison effect
        if(game.getRound().getTurn().getCurrentPlayer().isPoisoned()) {
            game.getRound().getTurn().getCurrentPlayer().setLives(game.getRound().getTurn().getCurrentPlayer().getLives() - 1);
            game.getRound().getTurn().getCurrentPlayer().setPoisoned(false);
            for(int i = 0; i < N_MAX_PLAYERS; i++) {
                server.sendCustom(webSockets.get(i), MessageEnum.ADD_ACTION, "POISONED," + ((HumanPlayer)game.getRound().getTurn().getOtherPlayer()).getUsername() + "," + 0);
            }
            waitAck(50, 2);
        }

        //Guardian angel
        if(game.getRound().getStateEffect().getClass() == GuardianAngel.class) {
            boolean active = true;
            for (int i = 0; i < N_MAX_PLAYERS; i++) {
                if(game.getHumanPlayers().get(i).isResurrected()) {
                    active = false;
                }
            }
            ArrayList<Integer> r = new ArrayList<>();
            for (int i = 0; i < N_MAX_PLAYERS; i++) {
                if(game.getHumanPlayers().get(i).getLives() <= 0) {
                    r.add(i);
                }
            }
            if(active && !r.isEmpty()) {
                Random rand = new Random();
                int index = rand.nextInt(r.size());
                game.getHumanPlayers().get(r.get(index)).setLives(1);
                game.getHumanPlayers().get(r.get(index)).setResurrected(true);
                for(int i = 0; i < N_MAX_PLAYERS; i++) {
                    server.sendCustom(webSockets.get(i), MessageEnum.ADD_ACTION, "RESURRECTED," + (game.getHumanPlayers().get(r.get(index))).getUsername());
                }
                waitAck(50, 2);
            }
        }

//        for(int i = 0; i < N_MAX_PLAYERS; i++) {
//            server.sendCustom(webSockets.get(i), MessageEnum.SHOW_GAME_STATE, getGameMap());
//        }
//        waitAck(50, 2);
    }

    public void drawConsumables() {
        for(int i = 0; i < N_MAX_PLAYERS; i++) {
            server.sendCustom(webSockets.get(i), MessageEnum.ADD_ACTION, "CONSUMABLES_DRAWN");
        }
        waitAck(50, 2);

        int maxConsumablesNumber = 8;
        Random rand = new Random();
        int r = rand.nextInt(2, 6);
        for(int i = 0; i < N_MAX_PLAYERS; i++) {
            int numberOfConsumablesHumanPlayer = Math.min(r, maxConsumablesNumber - game.getHumanPlayers().get(i).getConsumablesNumber());
            ArrayList<Consumable> consumables = game.getHumanPlayers().get(i).getConsumables();
            for(int j = 0; j < numberOfConsumablesHumanPlayer; j++) {
                Consumable randomConsumable = getRandomConsumable();
                consumables.add(randomConsumable);
            }
            game.getHumanPlayers().get(i).setConsumables(consumables);
        }
    }

    private Consumable getRandomConsumable() {
        HashMap<Consumable, Integer> consumableProb = new HashMap<>();

        for(Class<? extends Consumable> c : ConsumableInterface.getConsumableClassList()) {
            try {
                Class<?> cls = Class.forName(c.getName());
                Method m = cls.getMethod("getInstance");
                Object consumable = m.invoke(null);
                consumableProb.put((Consumable) consumable, ((Consumable) consumable).getProbability());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Random rand = new Random();
        Consumable consumable = null;
        int tries = 0;
        int maxTries = 100;
        boolean found = false;
        while(!found && tries < maxTries) {
            ArrayList<Consumable> consumableList = new ArrayList<>(consumableProb.keySet());
            Consumable randomConsumable = consumableList.get(rand.nextInt(consumableList.size()));
            int r = rand.nextInt(100);
            if (r < consumableProb.get(randomConsumable) || tries == maxTries - 1) {
                found = true;
                consumable = randomConsumable;
            }
            tries++;
        }

        return consumable;
    }

    public void loadGun() {
        ArrayList<Bullet> b = Gun.getInstance().generateBulletSequence();
        showBullets(b);
        Gun.getInstance().setBullets(b);
    }

    public void showBullets(ArrayList<Bullet> bullets) {
        ArrayList<String> b = new ArrayList<>();
        for (Bullet bullet : bullets) {
            b.add(Integer.toString(bullet.getType()));
        }
        for(int i = 0; i < N_MAX_PLAYERS; i++) {
            server.sendCustom(webSockets.get(i), MessageEnum.SHOW_BULLETS, String.join("", b));
        }
        waitAck(50, 2);
    }

    public boolean usePowerup(int target) {
        if(target < 1 || target > PowerupInterface.getPowerupClassList().size()) {
            server.sendCustom(webSockets.get(turn), MessageEnum.ADD_ERROR, "POWERUP_NOT_FOUND");
            waitAck(50, 1);
            return false;
        }
        String powerupName = PowerupInterface.getPowerupClassList().get(target - 1).getName();
        try {
            Method method = Class.forName(powerupName).getMethod("getInstance");
            Object obj = method.invoke(null);

            if (game.getHumanPlayers().get(turn).getPowerups().get((Powerup) obj) <= 0) {
                server.sendCustom(webSockets.get(turn), MessageEnum.ADD_ERROR, "POWERUP_NOT_FOUND");
                waitAck(50, 1);
                return false;
            }

            ((Powerup) obj).use(game);
            game.getHumanPlayers().get(turn).getPowerups().put((Powerup) obj, game.getHumanPlayers().get(turn).getPowerups().get(obj) - 1);
            users.get(turn).removePowerup((Powerup) obj);

            //User updated on db
            UserFacade userFacade = new UserFacade();
            userFacade.updateUser(users.get(turn));

            for(int i = 0; i < N_MAX_PLAYERS; i++) {
                server.sendCustom(webSockets.get(i), MessageEnum.ADD_ACTION, "POWERUP,ACTIVATION," + ((HumanPlayer) game.getRound().getTurn().getCurrentPlayer()).getUsername() + "," + obj.getClass().getSimpleName());
            }
            waitAck(50, 2);
            if (obj.getClass() == Bomb.class) {
                for(int i = 0; i < N_MAX_PLAYERS; i++) {
                    server.sendCustom(webSockets.get(i), MessageEnum.ADD_ACTION, "POWERUP,EFFECT," + ((HumanPlayer) game.getRound().getTurn().getCurrentPlayer()).getUsername() + "," + obj.getClass().getSimpleName());
                }
                waitAck(50, 2);
            }
        } catch (Exception e) {
            server.sendCustom(webSockets.get(turn), MessageEnum.ADD_ERROR, "POWERUP_NOT_FOUND");
            waitAck(50, 1);
            return false;
        }
        return true;
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException | NullPointerException e) {
            return false;
        }
        return true;
    }

    private boolean useCommand(String target) {
        if(isInteger(target)) {
            return usePowerup(Integer.parseInt(target)); //Aggiungere controllo numero
        }
        return useConsumable(target);
    }

    public boolean useConsumable(String target) {
        HashMap<String, Class<? extends Consumable>> map = new HashMap<>();
        String alphabet = "abcdefghijklmnopqrstuwxyz";
        for(int i = 0; i< ConsumableInterface.getConsumableClassList().size(); i++) {
            map.put(Character.toString(alphabet.charAt(i)), ConsumableInterface.getConsumableClassList().get(i));
        }

        if(!map.containsKey(target)) {
            server.sendCustom(webSockets.get(turn), MessageEnum.ADD_ERROR, "CONSUMABLE_NOT_FOUND");
            waitAck(50, 1);
            return false;
        }

        Class<? extends Consumable> consumableClass = map.get(target);

        boolean check = false;
        for(int i=0; i<game.getRound().getTurn().getCurrentPlayer().getConsumablesNumber(); i++) {
            if(game.getRound().getTurn().getCurrentPlayer().getConsumables().get(i).getClass().getSimpleName().equals(consumableClass.getSimpleName())) {
                check = true;
                break;
            }
        }

        if(!check) {
            server.sendCustom(webSockets.get(turn), MessageEnum.ADD_ERROR, "CONSUMABLE_NOT_FOUND");
            waitAck(50, 1);
            return false;
        }
        try {
            boolean used = false;
            Method method = Class.forName(consumableClass.getName()).getMethod("getInstance");
            Object obj = method.invoke(null);

            // Fog state effect
            boolean checkFog = game.getRound().getStateEffect().getClass() == Fog.class && (obj.getClass() == CrystalBall.class || obj.getClass() == Glasses.class);

            //Antidote state effect
            boolean checkAntidote = game.getRound().getStateEffect().getClass() == Antidote.class && (obj.getClass() == HealthPotion.class || obj.getClass() == MisteryPotion.class);

            if(checkFog) {
                server.sendCustom(webSockets.get(turn), MessageEnum.ADD_ACTION, "STATE_EFFECT,EFFECT," + Fog.class.getSimpleName());
                waitAck(50, 1);
                return true;
            } else if(checkAntidote) {
                server.sendCustom(webSockets.get(turn), MessageEnum.ADD_ACTION, "STATE_EFFECT,EFFECT," + Antidote.class.getSimpleName());
                waitAck(50, 1);
                return true;
            }

            for(int i=0; i<N_MAX_PLAYERS; i++) {
                server.sendCustom(webSockets.get(i), MessageEnum.ADD_ACTION, "CONSUMABLE,ACTIVATION," + obj.getClass().getSimpleName() + "," + ((HumanPlayer)game.getRound().getTurn().getCurrentPlayer()).getUsername());
            }
            waitAck(50, 2);
            String effect = ((Consumable) obj).use(game);
            if(((Consumable) obj).visibilityEffect()) {
                for (int i = 0; i < N_MAX_PLAYERS; i++) {
                    server.sendCustom(webSockets.get(i), MessageEnum.ADD_ACTION, "CONSUMABLE,EFFECT," + obj.getClass().getSimpleName() + "," + effect);
                }
                waitAck(50, 2);
            } else {
                server.sendCustom(webSockets.get(turn), MessageEnum.ADD_ACTION, "CONSUMABLE,EFFECT," + obj.getClass().getSimpleName() + "," + effect);
                waitAck(50, 1);
            }
            // Energy drink actions
            if(obj.getClass() == EnergyDrink.class) {
                if(game.getRound().getTurn().getOtherPlayer().getConsumables().isEmpty()){
                    return false;
                }
                for(int i=0; i<N_MAX_PLAYERS; i++) {
                    server.sendCustom(webSockets.get(i), MessageEnum.REFRESH, "ALL");
                }
                waitAck(50, 2);
                for (int i = 0; i < N_MAX_PLAYERS; i++) {
                    server.sendCustom(webSockets.get(i), MessageEnum.SHOW, "ACTIONS");
                }
                waitAck(50, 2);
                Character consumableToSteal;
                ArrayList<Consumable> otherPlayerConsumables = game.getRound().getTurn().getOtherPlayer().getConsumables();
                ArrayList<Character> charList = new ArrayList<>();
                for (Consumable otherPlayerConsumable : otherPlayerConsumables) {
                    for (Map.Entry<String, Class<? extends Consumable>> entry : map.entrySet()) {
                        if (otherPlayerConsumable.getClass().equals(entry.getValue())) {
                            charList.add(entry.getKey().charAt(0));
                        }
                    }
                }

                boolean validInput = false;
                server.sendCustom(webSockets.get(turn), MessageEnum.INPUT, "ENERGY_DRINK");

                while(inputBuffer.isEmpty()) {
                    try {
                        Thread.sleep(50);
                    } catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                waitAck(50, 1);
                consumableToSteal = inputBuffer.getFirst().charAt(0);

                boolean ok = false;
                for(int i = 0; i < alphabet.length(); i++) {
                    if(consumableToSteal.equals(alphabet.charAt(i))) {
                        ok = true;
                        break;
                    }
                }

                boolean exists = false;
                for (Character character : charList) {
                    if (character == consumableToSteal) {
                        exists = true;
                        break;
                    }
                }

                if(ok && exists) {
                    Class<? extends Consumable> stolenConsumableClass = map.get(consumableToSteal.toString());
                    if(stolenConsumableClass == EnergyDrink.class) {
                        for(int i=0; i<N_MAX_PLAYERS; i++) {
                            server.sendCustom(webSockets.get(i), MessageEnum.ADD_ERROR, "CONSUMABLE,FORBIDDEN," + obj.getClass().getSimpleName());
                        }
                        waitAck(50, 2);
                    } else {
                        Method method2 = Class.forName(stolenConsumableClass.getName()).getMethod("getInstance");
                        Object obj2 = method2.invoke(null);

                        //Fog effect
                        boolean checkFog2 = false;
                        if(game.getRound().getStateEffect().getClass() == Fog.class) {
                            if(obj2.getClass() == CrystalBall.class || obj2.getClass() == Glasses.class) {
                                checkFog2 = true;
                            }
                        }

                        if(!checkFog2) {
                            for(int i=0; i<N_MAX_PLAYERS; i++) {
                                server.sendCustom(webSockets.get(i), MessageEnum.ADD_ACTION, "CONSUMABLE,ACTIVATION," + obj2.getClass().getSimpleName() + "," + ((HumanPlayer) game.getRound().getTurn().getCurrentPlayer()).getUsername());
                            }
                            waitAck(50, 2);
                            String effect2 = ((Consumable)obj2).use(game);
                            if(((Consumable) obj2).visibilityEffect()) {
                                for (int i = 0; i < N_MAX_PLAYERS; i++) {
                                    server.sendCustom(webSockets.get(i), MessageEnum.ADD_ACTION, "CONSUMABLE,EFFECT," + obj2.getClass().getSimpleName() + "," + effect2);
                                }
                                waitAck(50, 2);
                            }
                            else {
                                server.sendCustom(webSockets.get(turn), MessageEnum.ADD_ACTION, "CONSUMABLE,EFFECT," + obj.getClass().getSimpleName() + "," + effect);
                                waitAck(50, 1);
                            }
                            game.getRound().getTurn().getOtherPlayer().removeConsumable((Consumable) obj2);
                            used = true;
                        } else {
                            server.sendCustom(webSockets.get(turn), MessageEnum.ADD_ACTION, "STATE_EFFECT,EFFECT," + Fog.class.getSimpleName());
                            waitAck(50, 1);
                        }
                    }
                }
            } else {
                used = true;
            }

            if(used) {
                game.getRound().getTurn().getCurrentPlayer().removeConsumable((Consumable) obj);

                //XP
                ((HumanPlayer) game.getRound().getTurn().getCurrentPlayer()).addXp(10);
            } else {
                server.sendCustom(webSockets.get(turn), MessageEnum.ADD_ACTION, "CONSUMABLE_NOT_USED");
                waitAck(50, 1);
                return false;
            }
        } catch (Exception e) {
            server.sendCustom(webSockets.get(turn), MessageEnum.ADD_ERROR, "CONSUMABLE_NOT_FOUND");
            waitAck(50, 1);
            return false;
        }
        return true;
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

    public boolean shootingPhase(String target) {
        boolean changeTurn = false;
        int score = 0;

        Player currentPlayer = game.getRound().getTurn().getCurrentPlayer();
        Bullet currentBullet = null;
        if(target.equals("1") || target.equals("2")) {
            currentBullet = Gun.getInstance().popBullet();
        }

        // 1 = self
        // 2 = other
        if(target.equals("1")) {
            if(currentBullet.getType() == 1) {
                changeTurn = true;
                if(!currentPlayer.isShieldActive()) {
                    currentPlayer.setLives(currentPlayer.getLives() - Gun.getInstance().getDamage());
                    users.get(turn).setNumberOfSelfShots(users.get(turn).getNumberOfSelfShots() + 1);
                    if(Gun.getInstance().getDamage() != 1) {
                        for(int i=0; i<N_MAX_PLAYERS; i++) {
                            server.sendCustom(webSockets.get(i), MessageEnum.ADD_ACTION, "GHOST_GUN_DAMAGE");
                        }
                        waitAck(50, 2);
                    }
                    if(game.getRound().getTurn().isBulletPoisoned()) {
                        currentPlayer.setPoisoned(true);
                    }

                    //Score system reset
                    ((HumanPlayer) game.getRound().getTurn().getCurrentPlayer()).setComboCounter(0);
                    ((HumanPlayer) game.getRound().getTurn().getCurrentPlayer()).setMultiplier(1);
                } else {
                    for(int i=0; i<N_MAX_PLAYERS; i++) {
                        server.sendCustom(webSockets.get(i), MessageEnum.ADD_ACTION, "POWERUP,EFFECT," + ((HumanPlayer) game.getRound().getTurn().getCurrentPlayer()).getUsername() + "," + Shield.class.getSimpleName());
                    }
                    waitAck(50, 2);
                    currentPlayer.setShieldActive(false);
                }
            } else {
                //Score system & XP
                score = (int)Math.round(((HumanPlayer) game.getRound().getTurn().getCurrentPlayer()).getMultiplier() * 80);
                if(game.getRound().getStateEffect().getClass() == DoubleScore.class) {
                    score *= 2;
                }
                ((HumanPlayer) game.getRound().getTurn().getCurrentPlayer()).addScore(score);
                ((HumanPlayer) game.getRound().getTurn().getCurrentPlayer()).incrementComboCounter();
                if((((HumanPlayer) game.getRound().getTurn().getCurrentPlayer()).getComboCounter() % 5) == 0) {
                    ((HumanPlayer) game.getRound().getTurn().getCurrentPlayer()).incrementMultiplier();
                }

                // XP
                ((HumanPlayer) game.getRound().getTurn().getCurrentPlayer()).addXp(20);
            }
        } else if(target.equals("2")) {
            if(currentBullet.getType() == 1) {
                Player otherPlayer = game.getRound().getTurn().getOtherPlayer();

                if(!otherPlayer.isShieldActive()) {
                    otherPlayer.setLives(otherPlayer.getLives() - Gun.getInstance().getDamage());
                    users.get(turn).setNumberOfKills(users.get(turn).getNumberOfKills() + 1);
                    if(Gun.getInstance().getDamage() != 1) {
                        for(int i=0; i<N_MAX_PLAYERS; i++) {
                            server.sendCustom(webSockets.get(i), MessageEnum.ADD_ACTION, "GHOST_GUN_DAMAGE");
                        }
                        waitAck(50, 2);
                    }
                    if(game.getRound().getTurn().isBulletPoisoned()) {
                        otherPlayer.setPoisoned(true);
                    }

                    //Score system & XP
                    score = (int)Math.round(((HumanPlayer) game.getRound().getTurn().getCurrentPlayer()).getMultiplier() * 100);
                    if(game.getRound().getStateEffect().getClass() == DoubleScore.class) {
                        score *= 2;
                    }
                    ((HumanPlayer) game.getRound().getTurn().getCurrentPlayer()).addScore(score);
                    ((HumanPlayer) game.getRound().getTurn().getCurrentPlayer()).incrementComboCounter();
                    if((((HumanPlayer) game.getRound().getTurn().getCurrentPlayer()).getComboCounter() % 5) == 0) {
                        ((HumanPlayer) game.getRound().getTurn().getCurrentPlayer()).incrementMultiplier();
                    }

                    // XP
                    ((HumanPlayer) game.getRound().getTurn().getCurrentPlayer()).addXp(25);

                } else {
                    for(int i=0; i<N_MAX_PLAYERS; i++) {
                        server.sendCustom(webSockets.get(i), MessageEnum.ADD_ACTION, "POWERUP,EFFECT," + ((HumanPlayer) game.getRound().getTurn().getCurrentPlayer()).getUsername() + "," + Shield.class.getSimpleName() );
                    }
                    waitAck(50, 2);
                    otherPlayer.setShieldActive(false);
                }
            } else {
                //Score system reset
                ((HumanPlayer) game.getRound().getTurn().getCurrentPlayer()).setComboCounter(0);
                ((HumanPlayer) game.getRound().getTurn().getCurrentPlayer()).setMultiplier(1);
            }
            changeTurn = true;
        } else {
            Gun.getInstance().setDamage(1);
            return false;
        }

        Gun.getInstance().setDamage(1);
        game.getRound().getTurn().setBulletPoisoned(false);
        for(int i=0; i<N_MAX_PLAYERS; i++) {
            server.sendCustom(webSockets.get(i), MessageEnum.ADD_ACTION, "SHOOT,RESULT," + currentBullet.getType() + "," +  players.get(i).getUsername());
        }
        waitAck(50, 2);
        for(int i=0; i<N_MAX_PLAYERS; i++) {
            if(i != turn) {
                server.sendCustom(webSockets.get(i), MessageEnum.ADD_ACTION, "SHOOT,TARGET," + target + "," + players.get(i).getUsername());
            }
        }
        waitAck(50, 1);

        users.get(turn).setTotalScore(users.get(turn).getTotalScore() + score);

        return changeTurn;
    }
}
