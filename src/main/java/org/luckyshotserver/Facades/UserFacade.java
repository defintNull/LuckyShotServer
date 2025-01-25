package org.luckyshotserver.Facades;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.java_websocket.WebSocket;
import org.luckyshotserver.Facades.Services.Converters.ObjectConverter;
import org.luckyshotserver.Facades.Services.HibernateService;
import org.luckyshotserver.Facades.Services.Server;
import org.luckyshotserver.Models.User;

public class UserFacade {
    public boolean updateUser(User user, String userJSON) {
        ObjectConverter converter = new ObjectConverter();
        User userReceived = converter.jsonToUser(userJSON);

        if(!user.getUsername().equals(userReceived.getUsername())) {
            return false;
        }

        Session session = HibernateService.getInstance().getCurrentSession();
        Transaction transaction;
        try {
            transaction = session.beginTransaction();
            session.merge(userReceived);
            transaction.commit();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public boolean updateUser(User user) {
        Session session = HibernateService.getInstance().getCurrentSession();
        Transaction transaction;
        try {
            transaction = session.beginTransaction();
            session.merge(user);
            transaction.commit();
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
