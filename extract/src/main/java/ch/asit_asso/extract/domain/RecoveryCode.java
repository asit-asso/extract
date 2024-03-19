package ch.asit_asso.extract.domain;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(name = "Recovery_Codes", indexes = {
        @Index(columnList = "id_user", name = "IDX_RECOVERY_CODES_USER")
})
public class RecoveryCode implements Serializable {

    /**
     * The identifier of the recovery code entry.
     */
    @Id
    @GeneratedValue
    @Column(name = "id_code")
    //@NotNull
    private Integer id;

    @JoinColumn(name = "id_user", referencedColumnName = "id_user",
            foreignKey = @ForeignKey(name = "FK_RECOVERY_CODES_USER")
    )
    @ManyToOne
    private User user;


    @Column(name = "token")
    @Size(max = 100)
    private String token;


    public RecoveryCode() { }


    public RecoveryCode(Integer identifier) { this.id = identifier; }


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
}
