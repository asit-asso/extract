package ch.asit_asso.extract.ldap;

import java.util.Collections;
import java.util.List;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import ch.asit_asso.extract.domain.User;
import ch.asit_asso.extract.utils.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.AttributesMapper;

public class LdapUserAttributesMapper implements AttributesMapper<LdapUser> {

    private final LdapSettings settings;

    private final Logger logger = LoggerFactory.getLogger(LdapUserAttributesMapper.class);

    public LdapUserAttributesMapper(LdapSettings ldapSettings) {
        this.settings = ldapSettings;
    }

    @Override
    public LdapUser mapFromAttributes(Attributes attributes) throws NamingException {
        List<Attribute> attributesList = ListUtils.castList(Attribute.class, Collections.list(attributes.getAll()));
        this.logger.debug("Attributes found : {}",
                          String.join(", ", attributesList.stream().map(Attribute::getID).toList()));
        String login = attributes.get(this.settings.getLoginAttribute()).get().toString();
        String name = attributes.get(this.settings.getUserNameAttribute()).get().toString();
        String email = attributes.get(this.settings.getMailAttribute()).get().toString();
        boolean active = true;
        Attribute disabledAttribute = attributes.get("userAccountControl");

        if (disabledAttribute != null) {
            active = ((Integer.parseInt((String) disabledAttribute.get()) & 2) == 0);
        }

        List<String> memberships = ListUtils.castList(String.class, Collections.list(attributes.get("memberOf").getAll()));

        if (memberships.contains(this.settings.getAdminsGroup())) {
            return new LdapUser(login, name, email, User.Profile.ADMIN, active);
        }

        if (memberships.contains(this.settings.getOperatorsGroup())) {
            return new LdapUser(login, name, email, User.Profile.OPERATOR, active);
        }

        return null;
    }
}
