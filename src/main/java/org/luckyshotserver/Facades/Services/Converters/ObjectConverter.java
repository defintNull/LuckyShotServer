package org.luckyshotserver.Facades.Services.Converters;

import com.fasterxml.jackson.databind.ObjectMapper;

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
}
