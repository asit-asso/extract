package ch.asit_asso.extract.authentication.twofactor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class TwoFactorAuthentication  extends AbstractAuthenticationToken {

    private final Authentication first;

    private final Logger logger = LoggerFactory.getLogger(TwoFactorAuthentication.class);

    public TwoFactorAuthentication(Authentication first) {
        super(first.getAuthorities().stream().filter(authority -> authority instanceof SimpleGrantedAuthority).toList());
        this.logger.debug("Authorities list: {}",
                          String.join(", ", this.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList()));
        this.first = first;
    }

    @Override
    public Object getPrincipal() {
        return this.first.getPrincipal();
    }

    @Override
    public Object getCredentials() {
        return this.first.getCredentials();
    }

    @Override
    public void eraseCredentials() {
        if (this.first instanceof CredentialsContainer) {
            ((CredentialsContainer) this.first).eraseCredentials();
        }
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    public Authentication getFirst() {
        return this.first;
    }

}
