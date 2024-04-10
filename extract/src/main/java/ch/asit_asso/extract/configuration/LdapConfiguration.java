package ch.asit_asso.extract.configuration;

import ch.asit_asso.extract.ldap.LdapSettings;
import ch.asit_asso.extract.persistence.SystemParametersRepository;
import ch.asit_asso.extract.utils.Secrets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class LdapConfiguration {

    /**
     * The Spring Data object that links the application parameters with the data source.
     */
    private final Secrets secrets;

    private final SystemParametersRepository systemParametersRepository;

    @Value("${ldap.attributes.login}")
    private String loginAttribute;

    @Value("${ldap.attributes.mail}")
    private String mailAttribute;

    @Value("${ldap.attributes.fullname}")
    private String userNameAttribute;

    @Value("${ldap.user.objectclass}")
    private String userObjectClass;

    public LdapConfiguration(Secrets secrets, SystemParametersRepository repository) {
        this.secrets = secrets;
        this.systemParametersRepository = repository;
    }

    @Bean
    @DependsOn("applicationInitializer")
    public LdapSettings ldapSettings() {
        LdapSettings settings = new LdapSettings(this.systemParametersRepository, this.secrets);
        settings.setLoginAttribute(this.loginAttribute);
        settings.setMailAttribute(this.mailAttribute);
        settings.setUserNameAttribute(this.userNameAttribute);
        settings.setUserObjectClass(this.userObjectClass);

        return settings;
    }
}
