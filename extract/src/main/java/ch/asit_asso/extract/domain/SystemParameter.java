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
package ch.asit_asso.extract.domain;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;



/**
 * A global setting for the application.
 *
 * @author Florent Krin
 */
@Entity
@Table(name = "System")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "SystemParameter.getBasePath",
            query = "SELECT s.value FROM SystemParameter s where s.key = 'base_path'"),
    @NamedQuery(name = "SystemParameter.getSchedulerFrequency",
            query = "SELECT s.value FROM SystemParameter s WHERE s.key = 'freq_scheduler_sec'"),
    @NamedQuery(name = "SystemParameter.getSchedulerMode",
            query = "SELECT s.value FROM SystemParameter s WHERE s.key = 'op_mode'"),
    @NamedQuery(name = "SystemParameter.getSchedulerRanges",
            query = "SELECT s.value FROM SystemParameter s WHERE s.key = 'op_ranges'"),
    @NamedQuery(name = "SystemParameter.getSmtpServer",
            query = "SELECT s.value FROM SystemParameter s WHERE s.key = 'smtp_server'"),
    @NamedQuery(name = "SystemParameter.getSmtpPort",
            query = "SELECT s.value FROM SystemParameter s WHERE s.key = 'smtp_port'"),
    @NamedQuery(name = "SystemParameter.getSmtpFromName",
            query = "SELECT s.value FROM SystemParameter s WHERE s.key = 'smtp_from_name'"),
    @NamedQuery(name = "SystemParameter.getSmtpFromMail",
            query = "SELECT s.value FROM SystemParameter s WHERE s.key = 'smtp_from_mail'"),
    @NamedQuery(name = "SystemParameter.getSmtpPassword",
            query = "SELECT s.value FROM SystemParameter s WHERE s.key = 'smtp_pass'"),
    @NamedQuery(name = "SystemParameter.getSmtpUser",
            query = "SELECT s.value FROM SystemParameter s WHERE s.key = 'smtp_user'"),
    @NamedQuery(name = "SystemParameter.getSmtpSSL",
            query = "SELECT s.value FROM SystemParameter s WHERE s.key = 'smtp_ssl'"),
    @NamedQuery(name = "SystemParameter.getDashboardRefreshInterval",
            query = "SELECT s.value FROM SystemParameter s WHERE s.key = 'dashboard_interval'"),
    @NamedQuery(name = "SystemParameter.isEmailNotificationEnabled",
            query = "SELECT s.value FROM SystemParameter s WHERE s.key = 'mails_enable'")
})
public class SystemParameter implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The string that uniquely identifies this setting in the application.
     */
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "key")
    private String key;

    /**
     * The content of this setting.
     */
    @Size(max = 65000)
    @Column(name = "value")
    private String value;



    /**
     * Creates a new setting instance.
     */
    public SystemParameter() {
    }



    /**
     * Creates a new setting instance.
     *
     * @param parameterKey the string that uniquely identifies this setting in the application
     */
    public SystemParameter(final String parameterKey) {
        this.key = parameterKey;
    }



    /**
     * Creates a new setting instance.
     *
     * @param parameterKey the string that uniquely identifies this setting in the application
     * @param valueString  the string with the content of this setting
     */
    public SystemParameter(final String parameterKey, final String valueString) {
        this.key = parameterKey;
        this.value = valueString;
    }



    /**
     * Obtains the string that uniquely identifies this setting in the application.
     *
     * @return the key to this setting
     */
    public String getKey() {
        return this.key;
    }



    /**
     * Defines the string that uniquely identifies this setting in the application.
     *
     * @param parameterKey the key to this setting
     */
    public void setKey(final String parameterKey) {
        this.key = parameterKey;
    }



    /**
     * Obtains the content of this setting.
     *
     * @return the setting value as a string
     */
    public String getValue() {
        return this.value;
    }



    /**
     * Defines the content of this setting.
     *
     * @param valueString the setting value as a string
     */
    public void setValue(final String valueString) {
        this.value = valueString;
    }



    @Override
    public final int hashCode() {
        int hash = 0;
        hash += this.key.hashCode();

        return hash;
    }



    @Override
    public final boolean equals(final Object object) {

        if (object == null || !(object instanceof SystemParameter)) {
            return false;
        }

        SystemParameter other = (SystemParameter) object;

        return this.key.equals(other.key);
    }



    @Override
    public final String toString() {
        return String.format("ch.asit_asso.extract.SystemParameter[ key=%s ]", this.key);
    }

}
