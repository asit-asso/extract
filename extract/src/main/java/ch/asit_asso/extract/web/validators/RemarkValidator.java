package ch.asit_asso.extract.web.validators;

import ch.asit_asso.extract.persistence.RemarkRepository;
import ch.asit_asso.extract.web.model.RemarkModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

/**
 * An object that ensure that a model representing a predefined remark contains valid information.
 *
 * @author Yves Grasset
 */
public class RemarkValidator extends BaseValidator {

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(RemarkValidator.class);

    /**
     * The object that links the predefined remark data objects with the data source.
     */
    private final RemarkRepository remarksRepository;



    /**
     * Creates a new instance of this validator.
     *
     * @param repository the object that links predefined remark data objects with the data source
     */
    public RemarkValidator(final RemarkRepository repository) {

        if (repository == null) {
            throw new IllegalArgumentException("The predefined remarks repository cannot be null.");
        }

        this.remarksRepository = repository;
    }



    /**
     * Determines if objects of a given type can be checked with this validator.
     *
     * @param type the class of the objects to validate
     * @return <code>true</code> if the type is supported by this validator
     */
    @Override
    public final boolean supports(final Class<?> type) {
        return RemarkModel.class.equals(type);
    }



    /**
     * Checks the conformity of the predefined remark model information.
     *
     * @param target the object to validate
     * @param errors an object that assembles the validation errors for the object
     */
    @Override
    @Transactional(readOnly = true)
    public void validate(final Object target, final Errors errors) {
        this.logger.debug("Validating the predefined remark model {}.", target);

        try {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "title", "remarkDetails.errors.title.required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "content", "remarkDetails.errors.content.required");

        } catch (Exception exception) {
            this.logger.error("An error occurred when the remark was validated.", exception);
        }
    }

}
