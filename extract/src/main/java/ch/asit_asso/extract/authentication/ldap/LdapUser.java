package ch.asit_asso.extract.authentication.ldap;

import ch.asit_asso.extract.ldap.LdapSettings;
import org.springframework.ldap.core.DirContextOperations;

public class LdapUser { //extends ApplicationUser {

    public LdapUser(DirContextOperations ldapUserData, LdapSettings ldapSettings)
    {
        //this.rolesList = this.buildRolesList(ldapUserData, ldapSettings);
    }

//    private List<GrantedAuthority> buildRolesList(final DirContextOperations ldapUserData, LdapSettings ldapSettings) {
//
//    }
}
