package ch.asit_asso.extract.configuration;

import ch.asit_asso.extract.ldap.LdapSettings;
import ch.asit_asso.extract.persistence.SystemParametersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LdapConfiguration {

    /**
     * The Spring Data object that links the application parameters with the data source.
     */
    @Autowired
    private SystemParametersRepository systemParametersRepository;

    @Value("${ldap.attributes.login}")
    private String loginAttribute;

    @Value("${ldap.attributes.mail}")
    private String mailAttribute;

    @Value("${ldap.attributes.fullname}")
    private String userNameAttribute;

    @Value("${ldap.user.objectclass}")
    private String userObjectClass;

    @Bean
    public LdapSettings ldapSettings() {
        LdapSettings settings = new LdapSettings(this.systemParametersRepository);
        settings.setLoginAttribute(this.loginAttribute);
        settings.setMailAttribute(this.mailAttribute);
        settings.setUserNameAttribute(this.userNameAttribute);
        settings.setUserObjectClass(this.userObjectClass);

        return settings;
    }
}
