package ch.asit_asso.extract.web.model;

import ch.asit_asso.extract.domain.Remark;
import ch.asit_asso.extract.domain.Task;
import ch.asit_asso.extract.persistence.TasksRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Model object representing a predefined remark instance.
 *
 * @author Yves Grasset
 */
public class RemarkModel {

    private static final String VALIDATION_TASK_CODE = "VALIDATION";

    private static final String[] VALIDATION_PARAMETERS_KEYS = { "valid_msgs", "reject_msgs" };

    /**
     * The identifier of this predefined remark.
     */
    private Integer id;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The string to display as the title of this predefined remark.
     */
    private String title;

    /**
     * The text of this predefined remark.
     */
    private String content;


    /**
     * Whether this predefined remark can be removed from the data source.
     */
    private boolean deletable;



    /**
     * Creates an instance of this model for a new predefined remark.
     */
    public RemarkModel() {
        this.logger.debug("Instantiating a model for a new predefined remark.");
    }



    /**
     * Creates an instance of this model for an existing predefined remark.
     *
     * @param domainRemark the data object about the predefined remark to represent.
     */
    public RemarkModel(final Remark domainRemark, TasksRepository tasksRepository) {

        if (domainRemark == null) {
            throw new IllegalArgumentException("The domain object for the user cannot be null.");
        }

        if (tasksRepository == null) {
            throw new IllegalArgumentException("The domain object for the user cannot be null.");
        }

        this.logger.debug("Instantiating a model for existing predefined remark {}.", domainRemark.getTitle());
        this.setPropertiesFromDomainObject(domainRemark, tasksRepository);
    }



    /**
     * Obtains the number that uniquely identifies this predefined remark.
     *
     * @return the identifier
     */
    public final Integer getId() {
        return id;
    }



    /**
     * Defines the number that uniquely identifies this predefined remark.
     *
     * @param remarkId the identifier
     */
    public final void setId(final Integer remarkId) {
        this.id = remarkId;
    }



    /**
     * Obtains the title of this predefined remark.
     *
     * @return the title
     */
    public final String getTitle() {
        return this.title;
    }



    /**
     * Defines the title of this predefined remark.
     *
     * @param title the title
     */
    public final void setTitle(final String title) {
        this.title = title;
    }



    /**
     * Obtains the text of this predefined remark.
     *
     * @return the content
     */
    public final String getContent() {
        return this.content;
    }



    /**
     * Defines the text of this predefined remark.
     *
     * @param content the content
     */
    public final void setContent(final String content) {
        this.content = content;
    }



    /**
     * Obtains whether this predefined remark can be removed from the data source.
     *
     * @return <code>true</code> if this remark can be deleted
     */
    public final boolean isDeletable() {
        return this.deletable;
    }



    /**
     * Defines whether this predefined remark can be removed from the data source.
     *
     * @param canBeDeleted <code>true</code> if this remark can be deleted
     */
    public final void setDeletable(final boolean canBeDeleted) {
        this.deletable = canBeDeleted;
    }





    /**
     * Makes a new data object for this predefined remark.
     *
     * @return the created predefined remark data object
     */
    public final Remark createDomainObject() {
        Remark domainRemark = new Remark();

        return this.updateDomainObject(domainRemark);
    }



    /**
     * Reports the modifications to this model to the predefined remark data object.
     *
     * @param domainRemark      the data object for this predefined remark
     * @return the updated predefined remark data object
     */
    public final Remark updateDomainObject(final Remark domainRemark) {

        if (domainRemark == null) {
            throw new IllegalArgumentException("The predefined remark domain object to update cannot be null.");
        }

        domainRemark.setTitle(this.getTitle());
        domainRemark.setContent(this.getContent());

        return domainRemark;
    }



    /**
     * Defines the data in this model based on a predefined remark data object.
     *
     * @param domainRemark the data object that contains the predefined remark to copy to this model.
     */
    private void setPropertiesFromDomainObject(final Remark domainRemark, final TasksRepository tasksRepository) {
        assert domainRemark != null : "The domain predefined remark object must be set.";

        this.setId(domainRemark.getId());
        this.setTitle(domainRemark.getTitle());
        this.setContent(domainRemark.getContent());
        this.setDeletable(!this.isAssociatedToTasks(tasksRepository));
    }



    /**
     * Creates models to represent a collection of process data objects.
     *
     * @param domainObjectsCollection the remark data objects to represent
     * @param tasksRepository         the link between the task data objects and the data source
     * @return a collection that contains the models representing the data objects
     */
    public static Collection<RemarkModel> fromDomainObjectsCollection(
            final Iterable<Remark> domainObjectsCollection,
            final TasksRepository tasksRepository) {

        if (domainObjectsCollection == null) {
            throw new IllegalArgumentException("The collection of remark data objects cannot be null.");
        }

        List<RemarkModel> modelsList = new ArrayList<>();

        for (Remark domainRemark : domainObjectsCollection) {
            modelsList.add(new RemarkModel(domainRemark, tasksRepository));
        }

        return modelsList;
    }



    private boolean isAssociatedToTasks(final TasksRepository tasksRepository) {
        Task[] validationTasks = tasksRepository.findAllByCode(RemarkModel.VALIDATION_TASK_CODE);
        String stringId = this.getId().toString();

        for (Task task : validationTasks) {
            Map<String, String> parametersValues = task.getParametersValues();

            if (parametersValues == null || parametersValues.size() == 0) {
                continue;
            }

            List<String> messageIds;

            for (String parameterKey : RemarkModel.VALIDATION_PARAMETERS_KEYS) {

                if (parametersValues.containsKey(parameterKey)) {
                    messageIds = Arrays.stream(parametersValues.get(parameterKey).split(",")).toList();

                    if (messageIds.contains(stringId)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
