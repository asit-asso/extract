package ch.asit_asso.extract.domain;

import jakarta.xml.bind.annotation.XmlRootElement;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * A predefined remark to be used during the validation stage.
 *
 * @author Yves Grasset
 */
@Entity
@Table(name = "Remarks", indexes = {
        @Index(columnList = "title", name = "IDX_REMARK_TITLE")
})
@XmlRootElement
public class Remark implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The number that uniquely identifies this predefined remark in the application.
     */
    @Id
    @Basic(optional = false)
    @GeneratedValue
    @NotNull
    @Column(name = "id_remark")
    private Integer id;

    /**
     * The title of the remark.
     */
    @Size(max = 255)
    @NotNull
    @Column(name = "title")
    private String title;

    /**
     * The text of the remark.
     */
    @NotNull
    @Column(name = "content", columnDefinition = "text")
    private String content;




    /**
     * Creates a new rule instance.
     */
    public Remark() {
    }



    /**
     * Creates a new remark instance.
     *
     * @param identifier the number that identifies this predefined remark in the application
     */
    public Remark(final Integer identifier) {
        this.id = identifier;
    }



    /**
     * Obtains the number that identifies this predefined remark in the application.
     *
     * @return the predefined remark identifier
     */
    public Integer getId() {
        return this.id;
    }



    /**
     * Defines the number that identifies this predefined remark in the application.
     *
     * @param identifier the predefined remark identifier
     */
    public void setId(final Integer identifier) {
        this.id = identifier;
    }



    /**
     * Obtains the title of this predefined remark.
     *
     * @return the predefined remark title
     */
    public String getTitle() {
        return this.title;
    }



    /**
     * Defines the title of this predefined remark in the application.
     *
     * @param title the predefined remark title
     */
    public void setTitle(final String title) {
        this.title = title;
    }



    /**
     * Obtains the text of this predefined remark.
     *
     * @return the predefined remark content
     */
    public String getContent() {
        return this.content;
    }



    /**
     * Defines the text of this predefined remark in the application.
     *
     * @param content the predefined remark content
     */
    public void setContent(final String content) {
        this.content = content;
    }
}
