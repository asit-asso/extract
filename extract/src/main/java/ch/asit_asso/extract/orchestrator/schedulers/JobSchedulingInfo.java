/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ch.asit_asso.extract.orchestrator.schedulers;

import java.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * A set of data about a recurring batch process.
 *
 * @author Yves Grasset
 */
public class JobSchedulingInfo {

    /**
     * The number of milliseconds between job execution below which a warning will be logged.
     */
    private static final int MINIMUM_DELAY_WARNING_THRESHOLD = 1000;

    /**
     * The number of milliseconds to wait before starting a new instance of the process.
     */
    private long delay;

    /**
     * The object that allows to cancel the batch process.
     */
    private ScheduledFuture future;

    /**
     * The number that identifies the scheduled job.
     */
    private final int jobId;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(JobSchedulingInfo.class);



    /**
     * Creates a new instance of batch process data.
     *
     * @param jobIdentifier the identifier for the recurring process
     * @param jobDelay      the number of milliseconds to wait before starting a new instance of the process
     * @param jobFuture     the object that allows to cancel the batch process
     */
    public JobSchedulingInfo(final int jobIdentifier, final long jobDelay, final ScheduledFuture jobFuture) {

        if (jobIdentifier < 1) {
            throw new IllegalArgumentException("The job identifier must be greater than 0.");
        }

        this.jobId = jobIdentifier;
        this.setDelay(jobDelay);
        this.setFuture(jobFuture);
    }



    /**
     * Obtains the identifier of the batch process.
     *
     * @return the job identifier
     */
    public final int getJobId() {
        return this.jobId;
    }



    /**
     * Obtains the number of milliseconds to wait before starting a new instance of the process.
     *
     * @return the delay in milliseconds
     */
    public final long getDelay() {
        return this.delay;
    }



    /**
     * Defines the number of milliseconds to wait before starting a new instance of the process.
     *
     * @param jobDelay the delay in milliseconds
     */
    public final void setDelay(final long jobDelay) {

        if (jobDelay < 1) {
            throw new IllegalArgumentException("The delay in milliseconds should be greater than 0.");
        }

        if (jobDelay < JobSchedulingInfo.MINIMUM_DELAY_WARNING_THRESHOLD) {
            this.logger.warn("The job {} has been scheduled with a delay inferior to 1 second.", this.getJobId());
        }

        this.delay = jobDelay;
    }



    /**
     * Obtains the object used to cancel the execution of the batch process.
     *
     * @return the object returned by the scheduler
     */
    public final ScheduledFuture getFuture() {
        return this.future;
    }



    /**
     * Defines the object used to cancel the execution of the batch process.
     *
     * @param jobFuture the object returned by the scheduler
     */
    public final void setFuture(final ScheduledFuture jobFuture) {

        if (jobFuture == null) {
            throw new IllegalArgumentException("The scheduled future object cannot be null.");
        }

        this.future = jobFuture;
    }



    /**
     * Prevents further execution of the batch process.
     *
     * @param interrupt <code>true</code> to stop the execution even if the job is running.
     *                  <code>false</code> to let it finish before canceling it.
     */
    public final void cancelJob(final boolean interrupt) {

        if (this.getFuture() == null) {
            return;
        }

        this.getFuture().cancel(interrupt);
    }

}
