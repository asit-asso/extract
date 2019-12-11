/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.easysdi.extract.orchestrator.schedulers;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;



/**
 * The base for an object that will start batch processes at a given interval or time.
 *
 * @author Yves Grasset
 */
public abstract class JobScheduler {

    /**
     * The number of milliseconds in a second.
     */
    protected static final int MILLISECONDS_FACTOR = 1000;

    /**
     * The object that allows to repeatedly execute tasks at a given interval.
     */
    private final ScheduledTaskRegistrar taskRegistrar;

    /**
     * The number of seconds to wait before executing a job again after its completion.
     */
    private int schedulingStep;



    /**
     * Creates a new instance of this scheduler.
     *
     * @param registrar the object that allows to repeatedly execute tasks at a given interval
     */
    public JobScheduler(final ScheduledTaskRegistrar registrar) {

        if (registrar == null) {
            throw new IllegalArgumentException("The scheduled task registrar cannot be null.");
        }

        this.taskRegistrar = registrar;
        this.schedulingStep = 1;
    }



    /**
     * Starts the batch processes managed by this scheduler.
     */
    public abstract void scheduleJobs();



    /**
     * Stops the recurrence of the batch processes managed by this scheduler.
     */
    public abstract void unscheduleJobs();



    /**
     * Obtains the interval between two occurrences of the scheduled base jobs.
     *
     * @return the interval in seconds
     */
    public final int getSchedulingStep() {
        return this.schedulingStep;
    }



    /**
     * Obtains the interval between two occurrences of the scheduled base jobs.
     *
     * @return the interval in milliseconds
     */
    public final long getSchedulingStepInMilliseconds() {
        return (long) this.schedulingStep * JobScheduler.MILLISECONDS_FACTOR;
    }



    /**
     * Defines the interval between two occurrences of the scheduled base jobs.
     *
     * @param step the number of seconds to wait between two occurrences of a given base job.
     */
    public final void setSchedulingStep(final int step) {
        this.schedulingStep = step;
    }



    /**
     * Obtains the object that keep traces of scheduled jobs.
     *
     * @return the job task registrar
     */
    protected final ScheduledTaskRegistrar getTaskRegistrar() {
        return this.taskRegistrar;
    }



    /**
     * Obtains the object uses to start a batch job at a given interval or time.
     *
     * @return the task scheduler
     */
    protected final TaskScheduler getTaskScheduler() {
        return this.taskRegistrar.getScheduler();
    }

}
