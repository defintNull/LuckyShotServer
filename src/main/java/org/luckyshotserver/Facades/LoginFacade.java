package org.luckyshotserver.Facades;

public class LoginFacade {
    private HibernateService hibernateService;
    
    public LoginFacade() {

    }

    public void login(String username, String password) {
        Session session = hibernateService.getSessionFactory().openSession();
        User user = null;
        while (user == null) {
            String[] credentials = loginView.getLoginUserInput();
            try {
                user = session.createQuery("from User where username = :username", User.class)
                        .setParameter("username", credentials[0])
                        .getSingleResult();
            } catch (Exception e) {
                user = null;
                loginView.displayLoginRetry();
            }
            if(user != null) {
                if(!encoder.matches(credentials[1], user.getPassword())) {
                    user = null;
                    loginView.displayLoginRetry();
                }
            }
        }
        session.close();
    }
}
