package ch.asit_asso.extract.orchestrator.runners;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import ch.asit_asso.extract.domain.SystemParameter;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.ldap.LdapPool;
import ch.asit_asso.extract.ldap.LdapSettings;
import ch.asit_asso.extract.ldap.LdapUser;
import ch.asit_asso.extract.ldap.LdapUsersCollection;
import ch.asit_asso.extract.ldap.LdapUtils;
import ch.asit_asso.extract.persistence.ApplicationRepositories;
import ch.asit_asso.extract.persistence.SystemParametersRepository;
import ch.asit_asso.extract.persistence.UsersRepository;
import ch.asit_asso.extract.utils.EmailUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapSynchronizationJobRunner implements Runnable {

    private static boolean running = false;

    private final LdapSettings ldapSettings;

    private final SystemParametersRepository parametersRepository;

    private final UsersRepository usersRepository;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(LdapSynchronizationJobRunner.class);



    /**
     * Creates a new instance of the runner.
     *
     * @param repositories an ensemble of objects linking the data objects with the database
     * @param ldapSettings an object that assembles the configuration about how to interact with the LDAP servers.
     */
    public LdapSynchronizationJobRunner(@NotNull final ApplicationRepositories repositories,
                                        @NotNull final LdapSettings ldapSettings) {
        this(repositories.getUsersRepository(), repositories.getParametersRepository(), ldapSettings);
    }

    public LdapSynchronizationJobRunner(@NotNull final UsersRepository usersRepository,
                                        @NotNull final SystemParametersRepository parametersRepository,
                                        @NotNull final LdapSettings ldapSettings) {

        this.ldapSettings = ldapSettings;
        this.parametersRepository = parametersRepository;
        this.usersRepository = usersRepository;
    }


    public static synchronized boolean isRunning() {
        return LdapSynchronizationJobRunner.running;
    }



    private static synchronized void setRunning(boolean isRunning) {
        LdapSynchronizationJobRunner.running = isRunning;
    }



    @Override
    public final void run() {
        this.logger.debug("Performing LDAP synchronization job.");

        if (LdapSynchronizationJobRunner.isRunning()) {
            this.logger.warn("The LDAP synchronization is already running. The present execution is aborted.");
            return;
        }

        this.ldapSettings.refresh();

        if (!this.ldapSettings.isEnabled() || !this.ldapSettings.isSynchronizationEnabled()) {
            //TODO Warn admins ?
            throw new IllegalStateException(
                    "The LDAP synchronization was started, but it is turned off in the settings.");
        }

        try {
            LdapSynchronizationJobRunner.setRunning(true);
            LdapPool pool = LdapPool.fromLdapSettings(this.ldapSettings);
            LdapUsersCollection ldapUsersFound = pool.getUsers();

            List<User> modifiedUsers = this.updateApplicationUsers(ldapUsersFound);

            if (modifiedUsers.isEmpty()) {
                this.logger.info("No user account was modified.");
                return;
            }

            this.logger.info("Saving {} updated user{}.", modifiedUsers.size(), (modifiedUsers.size() > 1) ? "s" : "");
            this.usersRepository.saveAll(modifiedUsers);

        } catch (Exception exception) {
            this.logger.error("An error occurred during the LDAP synchronization.", exception);

        } finally {
            this.updateLastSynchronizationDate();
            LdapSynchronizationJobRunner.setRunning(false);
        }
    }



    @NotNull
    private List<User> updateApplicationUsers(LdapUsersCollection ldapUsersFound) {

        List<User> modifiedUsers = new ArrayList<>(this.disableUsersNotInLdap(ldapUsersFound));

        for (LdapUser ldapUser : ldapUsersFound) {
            User domainUser = this.usersRepository.findByLoginIgnoreCase(ldapUser.getLogin());

            if (domainUser == null) {
                this.logger.info("No application user was found for {}.", ldapUser.getLogin());

                if (EmailUtils.isAddressInUse(ldapUser.getEmail(), this.usersRepository)) {
                    this.logger.info(
                            "There is already an application user with the e-mail {}. LDAP user {} is ignored.",
                            ldapUser.getEmail(), ldapUser.getLogin());
                    continue;
                }

                this.logger.info("Creating user {}.", ldapUser.getLogin());
                modifiedUsers.add(User.fromLdap(ldapUser));
                continue;
            }

            if (domainUser.getUserType() != User.UserType.LDAP) {
                this.logger.info("There is already a non-LDAP user with login {}. LDAP user is ignored.",
                                 ldapUser.getLogin());
                continue;
            }

            this.logger.info("Updating user {} properties if necessary.", ldapUser.getLogin());

            if (LdapUtils.updateFromLdap(domainUser, ldapUser, this.usersRepository)) {
                modifiedUsers.add(domainUser);
            }
        }

        return modifiedUsers;
    }



    private List<User> disableUsersNotInLdap(LdapUsersCollection ldapUsersFound) {

        User[] usersNotInLdap;
        List<User> disabledUsers = new ArrayList<>();

        if (ldapUsersFound.isEmpty()) {
            this.logger.info("No user matching the criteria found on any LDAP server.");
            usersNotInLdap = this.usersRepository.findAllActiveApplicationUsersByUserType(User.UserType.LDAP);

        } else {
            List<String> foundLdapLogins = ldapUsersFound.stream().map(LdapUser::getLogin).toList();
            this.logger.debug("Found the following LDAP users : {}",
                              String.join(", ", foundLdapLogins));
            usersNotInLdap = this.usersRepository.findByActiveTrueAndUserTypeIsAndLoginNotIn(User.UserType.LDAP,
                                                                                        foundLdapLogins);
        }

        for (User activeLdapUser : usersNotInLdap) {
            this.logger.info("User {} was not found in LDAP and will be disabled.", activeLdapUser.getLogin());
            activeLdapUser.setActive(false);
            disabledUsers.add(activeLdapUser);
        }

        return disabledUsers;
    }



    private void updateLastSynchronizationDate() {

        SystemParameter lastSynchronizationDate = new SystemParameter(
                SystemParametersRepository.LDAP_LAST_SYNCHRO_DATE_KEY, ZonedDateTime.now().toString());
        this.parametersRepository.save(lastSynchronizationDate);
    }
}
