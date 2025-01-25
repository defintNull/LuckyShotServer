package org.luckyshotserver.Facades;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.java_websocket.WebSocket;
import org.luckyshotserver.Facades.Services.Converters.ObjectConverter;
import org.luckyshotserver.Facades.Services.HibernateService;
import org.luckyshotserver.Facades.Services.Server;
import org.luckyshotserver.Models.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class LoginFacade {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    public LoginFacade() {

    }

    public User login(WebSocket webSocket, String username, String password) {
        Session session = HibernateService.getInstance().getCurrentSession();
        User user = null;

        try {
            user = session.createQuery("from User where username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (Exception e) {
            user = null;

        }
        if(user != null) {
            if (!encoder.matches(password, user.getPassword())) {
                user = null;
            }
        }

        return user;
    }

    public boolean register(WebSocket webSocket, String username, String password) {
        Session session = HibernateService.getInstance().getCurrentSession();

        boolean check = false;
        User user = null;
        try {
            user = session.createQuery("from User where username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (Exception e) {
            user = null;
        }
        if(user != null) {
            Server server = Server.getInstance();
            server.sendError(webSocket, "ALREADY_EXISTS");
            return false;
        } else {
            check = true;
        }

        Transaction transaction = null;
        user = null;
        try {
            transaction = session.beginTransaction();
            user = new User(username, password);
            session.persist(user);
            transaction.commit();
        } catch (Exception e) {
            if(transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }

        Server server = Server.getInstance();
        server.sendOk(webSocket, "REGISTERED");

        return true;
    }
}
