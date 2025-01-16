package org.luckyshotserver.Facades.Services.Converters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jetbrains.annotations.NotNull;
import org.luckyshotserver.Models.Powerups.Powerup;
import org.luckyshotserver.Views.View;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Converter
public class PowerupConverter implements AttributeConverter<HashMap<Powerup, Integer>, String>{

    @Override
    public String convertToDatabaseColumn(@NotNull HashMap<Powerup, Integer> powerupIntegerHashMap) {
        ArrayList<ArrayList<String>> matrix = new ArrayList<>();
        for(Map.Entry<Powerup, Integer> entry : powerupIntegerHashMap.entrySet()) {
            ArrayList<String> row = new ArrayList<>();
            // Name
            row.add(entry.getKey().getClass().getSimpleName());
            // Occurrences
            row.add(Integer.toString(entry.getValue()));
            matrix.add(row);
        }

        String json = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            json = mapper.writeValueAsString(matrix);
        } catch (Exception e) {
            View view = new View();
            view.systemError(e.getMessage());
        }

        return json;
    }

    @Override
    public HashMap<Powerup, Integer> convertToEntityAttribute(String hashMapConvertedHashMap) {
        HashMap<Powerup, Integer> hashMap = new HashMap<>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            ArrayList<ArrayList<String>> convertList = mapper.readValue(
                    hashMapConvertedHashMap,
                    new TypeReference<>() {}
            );

            for(ArrayList<String> convert : convertList) {
                String[] path = Powerup.class.getName().split("\\.");
                path[path.length - 1] = convert.get(0);
                String path2 = String.join(".", path);
                try {
                    Method method = Class.forName(path2).getMethod("getInstance");
                    Object obj = method.invoke(null);
                    hashMap.put((Powerup) obj, Integer.valueOf(convert.get(1)));
                } catch (Exception e) {
                    View view = new View();
                    view.systemError(e.getMessage());
                }
            }
        } catch (Exception e) {
            View view = new View();
            view.systemError(e.getMessage());
        }

        return hashMap;
    }
}
