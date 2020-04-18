package br.com.royalfarma.model;

public class Usuario {
    private String login, password;

    public Usuario(String user, String password) {
        this.login = user;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
