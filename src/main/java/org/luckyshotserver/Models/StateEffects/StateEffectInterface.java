package org.luckyshotserver.Models.StateEffects;

import org.luckyshotserver.Models.Powerups.Powerup;
import org.luckyshotserver.Views.SinglePlayerGameView;
import org.luckyshotserver.Views.View;

import java.lang.reflect.Method;
import java.util.ArrayList;

public interface StateEffectInterface {
    static ArrayList<String> getStateEffectStringList() {
        ArrayList<String> list = new ArrayList<String>();
        for(int i = 0; i < getStateEffectClassList().size(); i++) {
            try {
                Method method = Class.forName(StateEffectInterface.getStateEffectClassList().get(i).getName()).getMethod("getInstance");
                Object obj = method.invoke(null);
                String n = ((Powerup) obj).toString();
                list.add(n);
            } catch (Exception e) {
                View view = new View();
                view.systemError(e.getMessage());
            }
        }
        return list;
    }

    static ArrayList<Class<? extends StateEffect>> getStateEffectClassList() {
        ArrayList<Class<? extends StateEffect>> list = new ArrayList<>();
        list.add(Antidote.class);
        list.add(DoubleScore.class);
        list.add(Fog.class);
        list.add(GuardianAngel.class);
        list.add(NoneEffect.class);
        return list;
    }
}
