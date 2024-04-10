package ch.asit_asso.extract.ldap;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.directory.DirContext;
import javax.validation.constraints.NotNull;
import ch.asit_asso.extract.ldap.LdapSettings.EncryptionType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.DefaultDirObjectFactory;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.query.SearchScope;
import org.springframework.security.core.Authentication;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import static org.springframework.ldap.query.LdapQueryBuilder.query;

public abstract class LdapServer {

    protected final String base;

    protected final String password;

    protected final String url;

    protected final String username;

    protected final LdapSettings settings;

    private final Logger logger = LoggerFactory.getLogger(LdapServer.class);


    protected LdapServer(@NotNull String serverInfo, @NotNull String base, LdapSettings.EncryptionType encryption,
                         String username, String password, LdapSettings settings) {
        this.base = base;
        this.username = username;
        this.password = StringUtils.isNotEmpty(password) ? password : null;
        this.settings = settings;
        this.url = this.buildUrl(serverInfo, encryption).orElseThrow();
    }

    private static final Pattern SERVER_EXTRACTION_PATTERN
            = Pattern.compile("^(?:ldaps?://)?(?<host>[A-Z0-9_\\-.]+)(?::(?<port>\\d+))?(?<file>/.*)?$",
                              Pattern.CASE_INSENSITIVE);


    public abstract Authentication authenticate(Authentication authentication,
                                                UserDetailsContextMapper userDetailsMapper);



    public String getUrl() {
        return this.url;
    }


    public LdapUsersCollection getUsers() {

        try {
            LdapTemplate sourceLdapTemplate = this.getLdapTemplate();
            sourceLdapTemplate.setIgnorePartialResultException(true);
            sourceLdapTemplate.afterPropertiesSet();
            LdapUserAttributesMapper mapper = new LdapUserAttributesMapper(this.settings);
            List<LdapUser> usersFound = sourceLdapTemplate.search(query().attributes(this.settings.getMailAttribute(),
                                                                                     this.settings.getLoginAttribute(),
                                                                                     this.settings.getUserNameAttribute(),
                                                                                     "memberOf", "userAccountControl")
                                                                         .searchScope(SearchScope.SUBTREE)
                                                                         .where("objectClass")
                                                                         .is(this.settings.getUserObjectClass())
                                                                         .and(query().where("memberOf")
                                                                                     .is(this.settings.getAdminsGroup())
                                                                                     .or("memberOf")
                                                                                     .is(this.settings.getOperatorsGroup())),
                                                                  mapper);
            return LdapUsersCollection.of(usersFound);

        } catch (Exception connectionException) {
            this.logger.error(String.format("An error occurred when the users were fetched from server %s", this.url),
                              connectionException);
            return null;
        }
    }



    public Optional<String> testConnection() {

        try {

            if (this.getContext() == null) {
                return Optional.of(String.format("%s/%s - Connection test returned no context", this.url, this.base));
            }

        } catch (Exception connectionException) {
            return Optional.of(String.format("%s/%s - %s",
                                             this.url, this.base, connectionException.getMessage()));
        }

        return Optional.empty();
    }



    public static LdapServer build(@NotNull String serverInfo, @NotNull String base, EncryptionType encryption,
                                         String username, String password, LdapSettings settings) {

        Logger logger = LoggerFactory.getLogger(LdapServer.class);

        if (ActiveDirectoryServer.isDomain(base)) {
            logger.debug("Base {} is interpreted as an Active Directory domain.", base);
            return new ActiveDirectoryServer(serverInfo, base, encryption, username, password, settings);
        }

        return new OpenLdapServer(serverInfo, base, encryption, username, password, settings);
    }



    @NotNull
    protected LdapTemplate getLdapTemplate() {

        LdapContextSource sourceLdapCtx = new LdapContextSource();
        sourceLdapCtx.setUrl(this.url + "/");
        sourceLdapCtx.setBase(this.base);

        if (!this.isAnonymous()) {
            sourceLdapCtx.setUserDn(this.username);
            sourceLdapCtx.setPassword(this.password);
        }
        sourceLdapCtx.setDirObjectFactory(DefaultDirObjectFactory.class);
        sourceLdapCtx.afterPropertiesSet();

        return new LdapTemplate(sourceLdapCtx);
    }



    protected Optional<String> buildUrl(String serverInfo, LdapSettings.EncryptionType encryption) {
        Matcher matcher = LdapServer.SERVER_EXTRACTION_PATTERN.matcher(serverInfo);
        String host = null;
        String port = null;
        String file = null;
        String schema = (encryption == LdapSettings.EncryptionType.LDAPS) ? "ldaps" : "ldap";

        if (matcher.find()) {
            host = matcher.group("host");
            port = matcher.group("port");
            file = matcher.group("file");
        }

        if (host == null) {
            this.logger.warn("The server name {} is invalid. Server ignored.", serverInfo);
            return Optional.empty();
        }

        if (port == null) {
            port = (encryption == LdapSettings.EncryptionType.STARTTLS) ? LdapSettings.DEFAULT_STARTTLS_PORT
                                                                        : LdapSettings.DEFAULT_LDAPS_PORT;
        }

        return Optional.of(String.format("%s://%s:%s%s", schema, host, port,
                                         Objects.requireNonNullElse(file, "")));

    }



    protected DirContext getContext() {

        LdapTemplate sourceLdapTemplate = this.getLdapTemplate();
        ContextSource contextSource = sourceLdapTemplate.getContextSource();

        if (this.isAnonymous()) {
            return contextSource.getReadOnlyContext();
        }

        return contextSource.getContext(this.username, this.password);
    }



    protected boolean isAnonymous() {
        return StringUtils.isEmpty(this.username) || StringUtils.isEmpty(this.password);
    }
}
