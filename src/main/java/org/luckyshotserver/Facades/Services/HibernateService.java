package org.luckyshotserver.Facades.Services;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import org.luckyshotserver.Models.User;

public class HibernateService {
    private static HibernateService instance;
    private SessionFactory sessionFactory;

    private HibernateService() {
        this.sessionFactory = new Configuration()
                .configure("hibernate.cfg.xml")
                //Annotated classes
                .addAnnotatedClass(User.class)
                //Auto table generator
                //.setProperty(AvailableSettings.JAKARTA_HBM2DDL_DATABASE_ACTION, Action.SPEC_ACTION_DROP_AND_CREATE)
                //Build
                .buildSessionFactory();
    }

    public static HibernateService getInstance() {
        if(instance == null) {
            instance = new HibernateService();
        }
        return instance;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
