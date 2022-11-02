/*
 * Copyright (C) 2019 arx iT
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.asit_asso.extract.orchestrator;

import ch.asit_asso.extract.persistence.SystemParametersRepository;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;


/**
 *
 * @author Yves Grasset
 */
public class OrchestratorSettings {

    public enum SchedulerMode {
        ON,
        RANGES,
        OFF
    }

    private static final int DEFAULT_FREQUENCY = 20;

    private int frequency;

    private SchedulerMode mode;

    private OrchestratorTimeRangeCollection ranges;

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(OrchestratorSettings.class);


    public final int getFrequency() {
        return this.frequency;
    }



    public final void setFrequency(final int newFrequency) {

        if (newFrequency <= 0) {
            throw new IllegalArgumentException("The orchestrator frequency must strictly positive.");
        }

        this.logger.info("Orchestrator frequency set to {} seconds.", newFrequency);
        this.frequency = newFrequency;
    }



    public final SchedulerMode getMode() {
        return this.mode;
    }



    public final void setMode(final SchedulerMode newMode) {

        if (newMode == null) {
            throw new IllegalArgumentException("The working mode of the orchestrator cannot be null.");
        }

        this.mode = newMode;
    }



    public final OrchestratorTimeRangeCollection getRanges() {
        return this.ranges;
    }



    public final void setRanges(final OrchestratorTimeRangeCollection newRanges) {

        if (newRanges == null) {
            throw new IllegalArgumentException("The working time ranges collection for the orchestrator cannot be null.");
        }

        this.ranges = newRanges;
    }



    public final void setRanges(final List<OrchestratorTimeRange> rangesList) {

        if (rangesList == null) {
            throw new IllegalArgumentException("The working time ranges list for the orchestrator cannot be null.");
        }

        this.ranges = new OrchestratorTimeRangeCollection(rangesList);
    }



    public OrchestratorSettings() {
        this.frequency = OrchestratorSettings.DEFAULT_FREQUENCY;
        this.mode = SchedulerMode.ON;
        this.ranges = new OrchestratorTimeRangeCollection();
    }



    public OrchestratorSettings(final SystemParametersRepository parametersRepository) {
        this.setValuesFromRepository(parametersRepository);
    }



    public OrchestratorSettings(final int frequency, final SchedulerMode mode,
            final List<OrchestratorTimeRange> rangesList) {
        this.setFrequency(frequency);
        this.setMode(mode);
        this.setRanges(rangesList);
    }



    @Override
    public boolean equals(Object object) {

        if (object == null || !(object instanceof OrchestratorSettings)) {
            return false;
        }

        final OrchestratorSettings other = (OrchestratorSettings) object;

        return this.frequency == other.frequency && this.mode == other.mode && this.ranges.equals(other.ranges);
    }



    public String getStateString() {
        this.logger.debug("Current mode is {}. There are {} ranges configured.", this.getMode().toString(), this.ranges.getRanges().length);

        if (this.isWorking()) {
            return "RUNNING";
        }

        if (this.getMode() == OrchestratorSettings.SchedulerMode.OFF) {
            return "STOPPED";
        }

        if (this.getMode() == OrchestratorSettings.SchedulerMode.RANGES && this.ranges.getRanges().length == 0) {
            return "SCHEDULE_CONFIG_ERROR";
        }

        return "SCHEDULED_STOP";
    }



    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + this.frequency;
        hash = 59 * hash + Objects.hashCode(this.mode);
        hash = 59 * hash + Objects.hashCode(this.ranges);
        return hash;
    }



    public boolean isNowInRanges() {
        return this.ranges.isInRanges(DateTime.now());
    }



    public boolean isValid() {

        if (this.getFrequency() < 1) {
            return false;
        }

        if (this.getMode() == null) {
            return false;
        }

        if (this.getMode() != SchedulerMode.RANGES) {
            return true;
        }

        return this.getRanges().isValid();
    }



    public boolean isWorking() {

        switch (this.mode) {

            case OFF:
                return false;

            case ON:
                return true;

            case RANGES:
                return this.isNowInRanges();

            default:
                throw new IllegalStateException(String.format("Invalid orchestrator working mode : %s",
                        this.mode.name()));
        }
    }



    public final void setValuesFromRepository(SystemParametersRepository parametersRepository) {

        if (parametersRepository == null) {
            throw new IllegalArgumentException("The system parameters repository cannot be null.");
        }

        this.setFrequency(Integer.valueOf(parametersRepository.getSchedulerFrequency()));
        this.setMode(SchedulerMode.valueOf(parametersRepository.getSchedulerMode()));
        final String rangesString = parametersRepository.getSchedulerRanges();
        this.setRanges(OrchestratorTimeRangeCollection.fromJson(rangesString));
    }

}
