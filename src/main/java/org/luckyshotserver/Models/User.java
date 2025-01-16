package org.luckyshotserver.Models;

import jakarta.persistence.*;
import org.luckyshotserver.Facades.Services.Converters.PowerupConverter;
import org.luckyshotserver.Models.Powerups.Powerup;
import org.luckyshotserver.Models.Powerups.PowerupInterface;
import org.luckyshotserver.Views.View;

import java.lang.reflect.Method;
import java.util.HashMap;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "coins", nullable = false)
    private int coins;

    @Column(name = "powerups", nullable = false)
    @Convert(converter = PowerupConverter.class)
    private HashMap<Powerup, Integer> powerups = new HashMap<Powerup, Integer>();

    @Column(name = "level", nullable = false)
    private int level;

    @Column(name = "xp", nullable = false)
    private  int xp;

    @Column(name = "total_score", nullable = false)
    private long totalScore;

    @Column(name = "games_played", nullable = false)
    private int gamesPlayed;

    @Column(name = "games_won", nullable = false)
    private int gamesWon;

    @Column(name = "number_of_kills", nullable = false)
    private int numberOfKills;

    @Column(name = "number_of_self_shots", nullable = false)
    private int numberOfSelfShots;

    public User() {

    }

    public User(String username, String password, int coins, int level, int xp, long totalScore) {
        this.username = username;
        this.password = password;
        this.coins = coins;
        this.level = level;
        this.xp = xp;
        this.totalScore = totalScore;

        for(Class<? extends Powerup> powerup : PowerupInterface.getPowerupClassList()) {
            try {
                Method method = Class.forName(powerup.getName()).getMethod("getInstance");
                Object obj = method.invoke(null);
                this.powerups.put(((Powerup) obj), 0);
            } catch (Exception e) {
                // PER ORA HO MESSO QUESTA DI VIEW
                View view = new View();
                view.systemError(e.getMessage());
            }
        }
    }

    public User(String username, String password) {
        this(username, password, 0, 1, 0, 0);
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public void addCoins(int coins) {
        this.coins += coins;
    }

    public HashMap<Powerup, Integer> getPowerups() {
        return powerups;
    }

    public void setPowerups(HashMap<Powerup, Integer> powerups) {
        this.powerups = powerups;
    }

    public void removePowerup(Powerup powerup) {
        this.powerups.put(powerup ,this.powerups.get(powerup) - 1);
    }

    public void addPowerup(Powerup powerup) {
        this.powerups.put(powerup ,this.powerups.get(powerup) + 1);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(long totalScore) {
        this.totalScore = totalScore;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getGamesWon() {
        return gamesWon;
    }

    public void setGamesWon(int gamesWon) {
        this.gamesWon = gamesWon;
    }

    public int getNumberOfKills() {
        return numberOfKills;
    }

    public void setNumberOfKills(int numberOfKills) {
        this.numberOfKills = numberOfKills;
    }

    public int getNumberOfSelfShots() {
        return numberOfSelfShots;
    }

    public void setNumberOfSelfShots(int numberOfSelfShots) {
        this.numberOfSelfShots = numberOfSelfShots;
    }
}
