package org.luckyshotserver.Facades.Services.Converters;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.luckyshotserver.Models.Powerups.Powerup;
import org.luckyshotserver.Models.User;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ObjectConverter {
    public String objToJSON(Object obj) {
        String json = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            json = mapper.writeValueAsString(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }

    public Object jsonToObj(String json, Class c) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public User jsonToUser(String json) {
        Map<String, Object> map = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            map = objectMapper.readValue(json, HashMap.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        User user = new User();
        user.setId(Long.parseLong(String.valueOf(map.get("id"))));
        user.setUsername((String)map.get("username"));
        user.setPassword((String)map.get("password"));
        user.setCoins(Integer.parseInt((String)map.get("coins")));
        user.setLevel(Integer.parseInt((String)map.get("level")));
        user.setXp(Integer.parseInt((String)map.get("xp")));
        user.setTotalScore(Integer.parseInt((String)map.get("totalScore")));
        user.setGamesPlayed(Integer.parseInt((String)map.get("gamesPlayed")));
        user.setGamesWon(Integer.parseInt((String)map.get("gamesWon")));
        user.setNumberOfKills(Integer.parseInt((String)map.get("numberOfKills")));
        user.setNumberOfSelfShots(Integer.parseInt((String)map.get("numberOfSelfShots")));

        Map<String, String> powerupMap = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            powerupMap = (HashMap)map.get("powerups");
        } catch (Exception e) {
            e.printStackTrace();
        }

        HashMap<Powerup, Integer> userPowerups = new HashMap<>();

        for(Map.Entry convert : powerupMap.entrySet()) {
            String name = (String)convert.getKey();
            int quantity = Integer.parseInt((String)(convert.getValue()));

            String[] path = Powerup.class.getName().split("\\.");
            path[path.length - 1] = name;
            String path2 = String.join(".", path);
            try {
                Method method = Class.forName(path2).getMethod("getInstance");
                Object obj = method.invoke(null);
                userPowerups.put((Powerup) obj, quantity);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        user.setPowerups(userPowerups);

        return user;
    }

    public String userToJson(User user) {
        Map<String, Object> map = new HashMap<>();

        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("password", user.getPassword());
        map.put("coins", String.valueOf(user.getCoins()));
        map.put("level", String.valueOf(user.getLevel()));
        map.put("xp", String.valueOf(user.getXp()));
        map.put("totalScore", String.valueOf(user.getTotalScore()));
        map.put("gamesPlayed", String.valueOf(user.getGamesPlayed()));
        map.put("gamesWon", String.valueOf(user.getGamesWon()));
        map.put("numberOfKills", String.valueOf(user.getNumberOfKills()));
        map.put("numberOfSelfShots", String.valueOf(user.getNumberOfSelfShots()));

        HashMap<String, String> powerupMap = new HashMap<>();

        for(Map.Entry<Powerup, Integer> entry: user.getPowerups().entrySet()) {
            String name = entry.getKey().getClass().getSimpleName();
            powerupMap.put(name, String.valueOf(entry.getValue()));
        }

        ObjectMapper objectMapper = new ObjectMapper();

        map.put("powerups", powerupMap);

        String userJSON = null;
        try {
            userJSON = objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return userJSON;
    }
}
