/*
 * Copyright (C) 2017 arx iT
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
package ch.asit_asso.extract.web.model;

import ch.asit_asso.extract.utils.EmailUtils;
import ch.asit_asso.extract.utils.Secrets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;



/**
 * A non-standard parameter of a plugin instance.
 *
 * @author Yves Grasset
 */
public class PluginItemModelParameter {

    /**
     * The writer to the application logs.
     */
    private final Logger logger = LoggerFactory.getLogger(PluginItemModelParameter.class);

    /**
     * The description of this parameter.
     */
    private String label;

    /**
     * The maximum size of the value for this parameter.
     */
    private int maxLength;

    /**
     * The number that the value of a numeric parameter must not exceed, or <code>null</code> if there is
     * no limit.
     */
    private Integer maxValue;

    /**
     * The number that the value of a numeric parameter must equal or be greater than, or <code>null</code>
     * if there is no limit.
     */
    private Integer minValue;

    /**
     * The string that identifies this parameter.
     */
    private String name;

    /**
     * Whether a value must be provided for this parameter.
     */
    private boolean required;

    /**
     * The span between two possible values of a numeric parameter, or <code>null</code> if all values are
     * acceptable.
     */
    private Integer step;

    /**
     * The type of value that this parameter needs.
     */
    private String type;

    /**
     * The value of this parameter.
     */
    private Object value;



    /**
     * Creates a new parameter instance.
     */
    public PluginItemModelParameter() {

    }



    /**
     * Creates a new parameter instance.
     *
     * @param parameterName the name of the parameter (must be unique in the instance parameter collection)
     * @param description   the user-friendly label of this parameter
     * @param valueType     the type of value expected by this parameter
     * @param isRequired    <code>true</code> if the value of this parameter must be defined
     * @param maximumSize   the maximum length of the value
     */
    public PluginItemModelParameter(final String parameterName, final String description, final String valueType,
            final boolean isRequired, final int maximumSize) {

        if (parameterName == null || parameterName.trim().isEmpty()) {
            throw new IllegalArgumentException("The parameter name cannot be null or empty.");
        }

        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("The parameter label cannot be null or empty.");
        }

        if (valueType == null || valueType.trim().isEmpty()) {
            throw new IllegalArgumentException("The parameter type cannot be null or empty.");
        }

        this.name = parameterName;
        this.label = description;
        this.type = valueType;
        this.required = isRequired;
        this.maxLength = (maximumSize > 0) ? maximumSize : -1;
    }



    /**
     * Creates a new numeric parameter instance.
     *
     * @param parameterName the name of the parameter (must be unique in the instance parameter collection)
     * @param description   the user-friendly label of this parameter
     * @param valueType     the type of value expected by this parameter
     * @param isRequired    <code>true</code> if the value of this parameter must be defined
     * @param minimumValue  the number that the value must not exceed, or <code>null</code> if there is no limit
     * @param maximumValue  the number that the value must be greater than or equal to, or <code>null</code> if there
     *                      is no limit
     * @param stepValue     the span between two acceptable values, or <code>null</code> if all values are acceptable
     */
    public PluginItemModelParameter(final String parameterName, final String description, final String valueType,
            final boolean isRequired, final Integer minimumValue, final Integer maximumValue, final Integer stepValue) {

        if (parameterName == null || parameterName.trim().isEmpty()) {
            throw new IllegalArgumentException("The parameter name cannot be null or empty.");
        }

        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("The parameter label cannot be null or empty.");
        }

        if (valueType == null || valueType.trim().isEmpty()) {
            throw new IllegalArgumentException("The parameter type cannot be null or empty.");
        }

        if (minimumValue != null && maximumValue != null && minimumValue >= maximumValue) {
            throw new IllegalArgumentException("The parameter minimum value must be less than its maximum value.");
        }

        this.name = parameterName;
        this.label = description;
        this.type = valueType;
        this.required = isRequired;
        this.maxValue = maximumValue;
        this.minValue = minimumValue;
        this.step = stepValue;
    }



    /**
     * Creates a new parameter instance.
     *
     * @param parameterName  the name of the parameter (must be unique in the instance parameter collection)
     * @param description    the user-friendly label of this parameter
     * @param valueType      the type of value expected by this parameter
     * @param isRequired     <code>true</code> if the value of this parameter must be defined
     * @param maximumSize    the maximum length of the value
     * @param parameterValue the value of this parameter
     */
    public PluginItemModelParameter(final String parameterName, final String description, final String valueType,
            final boolean isRequired, final int maximumSize, final Object parameterValue) {
        this(parameterName, description, valueType, isRequired, maximumSize);
        this.setValue(parameterValue);
    }



    /**
     * Creates a new numeric parameter instance.
     *
     * @param parameterName  the name of the parameter (must be unique in the instance parameter collection)
     * @param description    the user-friendly label of this parameter
     * @param valueType      the type of value expected by this parameter
     * @param isRequired     <code>true</code> if the value of this parameter must be defined
     * @param minimumValue   the number that the value must not exceed, or <code>null</code> if there is no limit
     * @param maximumValue   the number that the value must be greater than or equal to, or <code>null</code> if there
     *                       is no limit
     * @param stepValue      the span between two acceptable values, or <code>null</code> if all values are acceptable
     * @param parameterValue the value of this parameter
     */
    public PluginItemModelParameter(final String parameterName, final String description, final String valueType,
            final boolean isRequired, final Integer minimumValue, final Integer maximumValue, final Integer stepValue,
            final Object parameterValue) {
        this(parameterName, description, valueType, isRequired, minimumValue, maximumValue, stepValue);
        this.setValue(parameterValue);
    }



    /**
     * Obtains the user-friendly description of this parameter.
     *
     * @return the description
     */
    public final String getLabel() {
        return this.label;
    }



    /**
     * Obtains the maximum size of the value for this parameter.
     *
     * @return the maximum size
     */
    public final int getMaxLength() {
        return this.maxLength;
    }



    /**
     * Obtains the number that the value of a numeric parameter must not exceed, or <code>null</code> if
     * there is no limit.
     *
     * @return the maximum value
     */
    public final Integer getMaxValue() {
        return this.maxValue;
    }



    /**
     * Obtains the number that the value of a numeric parameter must equal or be greater than, or <code>null</code>
     * if there is no limit.
     *
     * @return the minimum value
     */
    public final Integer getMinValue() {
        return this.minValue;
    }



    /**
     * Obtains the identifier of this parameter.
     *
     * @return the parameter name
     */
    public final String getName() {
        return this.name;
    }



    /**
     * Obtains whether a value must be set for this parameter.
     *
     * @return <code>true</code> if this parameter is mandatory
     */
    public final boolean isRequired() {
        return this.required;
    }



    /**
     * Obtains the span between two possible values of a numeric parameter, or <code>null</code> if all
     * values are acceptable.
     *
     * @return the value step
     */
    public final Integer getStep() {
        return this.step;
    }



    /**
     * Obtains the type of value expected by this parameter.
     *
     * @return the type of value
     */
    public final String getType() {
        return this.type;
    }



    /**
     * Obtains the defined value for this parameter.
     *
     * @return the parameter value
     */
    public final Object getValue() {
        return this.value;
    }



    public final String getPrintValue() {

        if (this.getValue() == null) {
            return null;
        }

        if (this.isPasswordType()) {
            return "[hidden]";
        }

        return this.getValue().toString();
    }



    /**
     * Defines the value for this parameter.
     *
     * @param parameterValue the parameter value
     */
    public final void setValue(final Object parameterValue) {
        this.value = parameterValue;
    }



    /**
     * Determines whether a value has been set for this parameter.
     *
     * @return <code>true</code> if a value has been set
     */
    public final boolean isDefined() {
        Object value = this.getValue();

        return switch (this.getType()) {
            case "pass" -> (StringUtils.hasLength((String) value) || !this.isRequired());
            case "email", "text", "multitext", "numeric", "list_msgs" -> (value != null);
            case "boolean" -> true;
            default -> false;
        };
    }



    /**
     * Checks if the proposed value for this parameter is appropriate.
     *
     * @param updatedValue the new value for this parameter
     * @return <code>true</code> of the given value is OK
     */
    public final boolean validateUpdatedValue(final String updatedValue) {
        this.logger.debug("Validating value {} for parameter {}", (this.isPasswordType()) ? "[hidden]" : updatedValue,
                          this.getName());

        if (!StringUtils.hasLength(updatedValue)) {

            if (this.isRequired()) {
                throw new IllegalArgumentException("The updated data object cannot be null.");
            }

            return true;
        }

        switch (this.getType()) {

            case "text":
            case "pass":
            case "multitext":
                return (updatedValue.length() <= this.getMaxLength());

            case "email":
                return (updatedValue.length() <= this.getMaxLength()
                        && this.checkEmailString(updatedValue, this.isRequired()));

            case "list_msgs":

                if (!StringUtils.hasLength(updatedValue)) {
                    return this.isRequired();
                }

                String[] values = updatedValue.split(",");

                for (String idString : values) {

                    try {
                        Integer.parseInt(idString);

                    } catch (NumberFormatException exception) {
                        this.logger.error(String.format("Could not convert string %s to integer.", idString), exception);
                        return false;
                    }
                }

                return true;

            case "boolean":
                return true;

            case "numeric":

                try {
                    int intValue = Integer.parseInt(updatedValue);
                    int relativeValue = (this.minValue != null) ? intValue - this.minValue : intValue;

                    return ((this.minValue == null || intValue >= this.minValue)
                            && (this.maxValue == null || intValue <= this.maxValue)
                            && (this.step == null || relativeValue % this.step == 0));

                } catch (NumberFormatException exception) {
                    this.logger.error("The value {} for numeric parameter \"{}\" is invalid", this.getName(),
                            updatedValue);
                    return false;
                }

            default:
                this.logger.error("Trying to validate unsupported parameter type \"{}\"", this.getType());
                throw new UnsupportedOperationException("This type of parameter is not supported.");
        }
    }



    /**
     * Modifies the value of this parameter.
     *
     * @param updatedParameter an instance of a plugin parameter containing the modified value
     */
    public final void updateValue(final PluginItemModelParameter updatedParameter) {
        this.updateValue(updatedParameter.getValue());
    }



    /**
     * Modifies the value of this parameter.
     *
     * @param rawUpdatedValue the new value
     */
    public final void updateValue(final Object rawUpdatedValue) {
        this.logger.debug("Updating the value of parameter {} : {} -> {}", this.getName(), this.getPrintValue(),
                          (this.isPasswordType()) ? "[hidden]" : rawUpdatedValue);
        String updatedValue = (String) rawUpdatedValue;

        if (!this.validateUpdatedValue(updatedValue)) {
            this.logger.info("The value {} is not valid for parameter {}",
                             (this.isPasswordType()) ? "[hidden]" : updatedValue, this.getName());
            throw new IllegalArgumentException("The value is not valid.");
        }

        switch (this.getType()) {

            case "text":
            case "multitext":
            case "boolean":
            case "email":
            case "numeric":
            case "list_msgs":
                this.setValue(updatedValue);
                break;

            case "pass":
                this.logger.debug("Updating the password-typed parameter.");

                if (!Secrets.isGenericPasswordString(updatedValue)) {
                    this.setValue(updatedValue);
                    this.logger.debug("The password-typed parameter has been updated.");
                } else {
                    this.logger.debug("The password-typed parameter was not changed.");
                }
                break;

            default:
                this.logger.error("Trying to update a value of an unsupported parameter type \"{}\"", this.getType());
                throw new UnsupportedOperationException("This type of parameter is not supported.");

        }
    }



    /**
     * Defines the user-friendly description for this parameter.
     *
     * @param description the label
     */
    public final void setLabel(final String description) {
        this.label = description;
    }



    /**
     * Defines the maximum size of the value for this parameter.
     *
     * @param maximumSize the maximum length of the value
     */
    public final void setMaxLength(final int maximumSize) {
        this.maxLength = maximumSize;
    }



    /**
     * Defines the number that the value of a numeric parameter must not exceed, or <code>null</code> if
     * there is no limit.
     *
     * @param maximum the maximum value
     */
    public final void setMaxValue(final Integer maximum) {
        this.maxValue = maximum;
    }



    /**
     * Defines the number that the value of a numeric parameter must equal or be greater than,
     * or <code>null</code> if there is no limit.
     *
     * @param minimum the minimum value
     */
    public final void setMinValue(final Integer minimum) {
        this.minValue = minimum;
    }



    /**
     * Defines the span between two possible values of a numeric parameter, or <code>null</code> if all
     * values are acceptable.
     *
     * @param stepValue the step value
     */
    public final void setStep(final Integer stepValue) {
        this.step = stepValue;
    }



    /**
     * The unique identifier for this parameter.
     *
     * @param parameterName the parameter identifying name
     */
    public final void setName(final String parameterName) {
        this.name = parameterName;
    }



    /**
     * Defines whether a value must be set for this parameter.
     *
     * @param isRequired <code>true</code> if this parameter is mandatory
     */
    public final void setRequired(final boolean isRequired) {
        this.required = isRequired;
    }



    /**
     * Defines the type of value expected by this parameter.
     *
     * @param valueType a string identifying the value type
     */
    public final void setType(final String valueType) {
        this.type = valueType;
    }



    private boolean isPasswordType() {
        return "pass".equals(this.getType());
    }



    /**
     * Validates that all the e-mail addresses in a string are valid. The addresses can be separated by commas
     * or semicolons.
     *
     * @param emailString the string that contains the addresses to validate
     * @param isMandatory <code>true</code> if at least one address must be defined
     * @return <code>true</code> if all the addresses are valid, or if the string is empty and the field is not
     *         mandatory. Note that this method will return <code>false</code> if at least one address is invalid,
     *         even if the field is not mandatory,
     */
    private boolean checkEmailString(final String emailString, final boolean isMandatory) {

        if (!StringUtils.hasLength(emailString)) {
            return !isMandatory;
        }

        for (String address : emailString.split("[;,]")) {
            address = address.trim();

            if (!EmailUtils.isAddressValid(address)) {
                this.logger.error("The e-mail address {} did not validate.", address);
                return false;
            }
        }

        return true;
    }

}
