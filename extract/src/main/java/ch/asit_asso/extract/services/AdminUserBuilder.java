package ch.asit_asso.extract.services;

import ch.asit_asso.extract.domain.User;

public class AdminUserBuilder {
    private String login;
    private String password;
    private String name;
    private String email;

    public static AdminUserBuilder create() {
        return new AdminUserBuilder();
    }

    public AdminUserBuilder withLogin(String login) {
        this.login = login;
        return this;
    }

    public AdminUserBuilder password(String password) {
        this.password = password;
        return this;
    }

    public AdminUserBuilder name(String name) {
        this.name = name;
        return this;
    }

    public AdminUserBuilder email(String email) {
        this.email = email;
        return this;
    }

    public User build() {
        User user = new User();
        user.setActive(true);
        user.setName(name);
        user.setLogin(login);
        user.setPassword(password);
        user.setEmail(email);
        user.setProfile(User.Profile.ADMIN);
        user.setMailActive(false);
        user.setUserType(User.UserType.LOCAL);
        user.setTwoFactorStatus(User.TwoFactorStatus.INACTIVE);
        user.setTwoFactorForced(false);
        return user;
    }
}
