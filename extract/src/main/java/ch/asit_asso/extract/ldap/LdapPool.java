package ch.asit_asso.extract.ldap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import ch.asit_asso.extract.ldap.LdapSettings.EncryptionType;
import ch.asit_asso.extract.web.model.SystemParameterModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

public class LdapPool {

    private final List<LdapServer> serversList;

    private final Logger logger = LoggerFactory.getLogger(LdapPool.class);


    public LdapPool(@NotNull String[] serversInfo, @NotNull String[] bases, EncryptionType encryptionType, String user,
                    String password, LdapSettings settings) {
        this.serversList = this.buildServersList(serversInfo, bases, encryptionType, user, password, settings);
    }


    public LdapPool(@NotNull String serversInfo, @NotNull String bases, EncryptionType encryptionType, String user,
                    String password, LdapSettings settings) {
        this.serversList = this.buildServersList(serversInfo.split(";"), bases.split(";"), encryptionType,
                                                 user, password, settings);
    }



    public Authentication authenticate(Authentication authentication, UserDetailsContextMapper userDetailsMapper) {
        Authentication ldapAuthentication;

        for (LdapServer ldapServer : this.serversList) {
            ldapAuthentication = ldapServer.authenticate(authentication, userDetailsMapper);

            if (ldapAuthentication == null || !ldapAuthentication.isAuthenticated()) {
                this.logger.debug("Authentication failed.");
                continue;
            }

            this.logger.debug("LDAP authentication successful.");
            return ldapAuthentication;
        }

        return null;
    }



    public Optional<String> testConnections() {
        this.logger.debug("Testing pool connections");

        for (LdapServer server : this.serversList) {
            Optional<String> result = server.testConnection();

            if (result.isPresent()) {
                this.logger.debug("One server from the pool returned an error. Test failed.");
                return result;
            }
        }

        this.logger.debug("No server returned an error. Test successful.");
        return Optional.empty();
    }


    @NotNull
    public static LdapPool fromModel(SystemParameterModel parameterModel) {
        return LdapPool.fromModel(parameterModel, parameterModel.getLdapSynchronizationPassword());
    }



    public static LdapPool fromModel(SystemParameterModel parameterModel, String synchroPassword) {

            String username = (parameterModel.isLdapSynchronizationEnabled())
                              ? parameterModel.getLdapSynchronizationUser()
                              : null;
            String password = (parameterModel.isLdapSynchronizationEnabled()) ? synchroPassword : null;

            return new LdapPool(parameterModel.getLdapServers(), parameterModel.getLdapBaseDn(),
                                parameterModel.getLdapEncryption(), username, password, null);
    }


    @NotNull
    public static LdapPool fromLdapSettings(LdapSettings settings) {
        settings.refresh();
        String username = (settings.isSynchronizationEnabled())
                          ? settings.getSynchronizationUserName()
                          : null;
        String password = (settings.isSynchronizationEnabled())
                          ? settings.getSynchronizationPassword()
                          : null;

        return new LdapPool(settings.getServers(), settings.getBaseDn(), settings.getEncryptionType(), username,
                            password, settings);
    }


    public LdapUsersCollection getUsers() {

        LdapUsersCollection users = new LdapUsersCollection();

        for (LdapServer server : this.serversList) {
            users.addAll(server.getUsers());
        }

        return users;
    }



    private List<LdapServer> buildServersList(@NotNull String[] serversInfo, @NotNull String[] bases,
                                              EncryptionType encryption,  String user, String password, LdapSettings settings) {

        this.logger.debug("Creating the list of servers for the pool.");
        List<LdapServer> list = new ArrayList<>();

        for (String server : serversInfo) {

            for (String base : bases) {
                this.logger.debug("Adding a server with info {} and base {} to the pool.", server, base);
                list.add(LdapServer.build(server, base, encryption, user, password, settings));
            }
        }

        this.logger.debug("The pool contains {} combinations of URL and bases.", list.size());
        return list;
    }
}
