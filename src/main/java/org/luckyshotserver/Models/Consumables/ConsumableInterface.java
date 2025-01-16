package org.luckyshotserver.Models.Consumables;

import org.luckyshotserver.Models.Enums.Probability;
import org.luckyshotserver.Views.View;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumMap;

public interface ConsumableInterface {

    EnumMap<Probability, Integer> probabilityRange = new EnumMap<>(Probability.class) {{
        put(Probability.LOW, 5);
        put(Probability.MEDIUM_LOW, 13);
        put(Probability.MEDIUM, 20);
        put(Probability.MEDIUM_HIGH, 27);
        put(Probability.HIGH, 35);
    }};

    static ArrayList<String> getConsumableStringList() {
        ArrayList<String> list = new ArrayList<String>();
        for(int i = 0; i < getConsumableClassList().size(); i++) {
            try {
                Method method = Class.forName(ConsumableInterface.getConsumableClassList().get(i).getName()).getMethod("getInstance");
                Object obj = method.invoke(null);
                String n = ((Consumable) obj).toString();
                list.add(n);
            } catch (Exception e) {
                View view = new View();
                view.systemError(e.getMessage());
            }
        }
        return list;
    }

    static ArrayList<Class<? extends Consumable>> getConsumableClassList() {
        ArrayList<Class<? extends Consumable>> list = new ArrayList<Class<? extends Consumable>>();
        list.add(CrystalBall.class);
        list.add(EnergyDrink.class);
        list.add(GhostGun.class);
        list.add(Glasses.class);
        list.add(Handcuffs.class);
        list.add(HealthPotion.class);
        list.add(Inverter.class);
        list.add(Magnet.class);
        list.add(MisteryPotion.class);
        return list;
    }
}
