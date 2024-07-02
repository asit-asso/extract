package ch.asit_asso.extract.domain;

import java.util.Calendar;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "RememberMe_Tokens", indexes = {
        @Index(columnList = "id_user", name = "IDX_REMEMBERME_USER")
})
@XmlRootElement
@NamedQueries({
        @NamedQuery(name = "RememberMeToken.getExpiredTokens",
                query = "SELECT t FROM RememberMeToken t WHERE t.user = :user"
                        + " AND t.tokenExpiration < current_timestamp"
        ),
        @NamedQuery(name = "RememberMeToken.getValidTokens",
                query = "SELECT t FROM RememberMeToken t WHERE t.user = :user"
                        + " AND t.tokenExpiration >= current_timestamp"
        )
})
public class RememberMeToken {

    /**
     * The identifier of the remember-me token entry.
     */
    @Id
    @GeneratedValue
    @Column(name = "id_code")
    //@NotNull
    private Integer id;

    @JoinColumn(name = "id_user", referencedColumnName = "id_user",
            foreignKey = @ForeignKey(name = "FK_REMEMBERME_USER")
    )
    @ManyToOne
    private User user;


    @Column(name = "token")
    @Size(max = 100)
    private String token;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "tokenexpire")
    private Calendar tokenExpiration;



    public RememberMeToken() { }

    public RememberMeToken(final Integer tokenId) { this.id = tokenId; }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer identifier) {
        this.id = identifier;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Calendar getTokenExpiration() {
        return this.tokenExpiration;
    }

    public void setTokenExpiration(Calendar tokenExpiration) {
        this.tokenExpiration = tokenExpiration;
    }

}
