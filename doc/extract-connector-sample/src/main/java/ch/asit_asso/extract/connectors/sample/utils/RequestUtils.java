/*
 * Copyright (C) 2021 arx iT
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
package ch.asit_asso.extract.connectors.sample.utils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import ch.asit_asso.extract.connectors.common.IProduct;
import ch.asit_asso.extract.connectors.sample.ConnectorConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 *
 * @author Yves Grasset
 */
public abstract class RequestUtils {

    /**
     * The writer to the application logs.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(RequestUtils.class);



    public static final String interpolateVariables(String pattern, IProduct product, ConnectorConfig config) {
        final String[] authorizedFields = config.getProperty("url.properties.authorized").split(",");
        String formattedPath = pattern;

        for (String fieldName : authorizedFields) {
            String fieldValue = RequestUtils.getProductFieldValue(product, fieldName);

            if (fieldValue == null) {
                continue;
            }

            final String fieldPlaceholder = String.format("{%s}", fieldName.toUpperCase());
            int searchFieldIndex = formattedPath.toUpperCase().indexOf(fieldPlaceholder);

            while (searchFieldIndex >= 0) {
                formattedPath = formattedPath.substring(0, searchFieldIndex) + fieldValue
                        + formattedPath.substring(searchFieldIndex + fieldPlaceholder.length());
                searchFieldIndex = formattedPath.toUpperCase().indexOf(fieldPlaceholder);
            }
        }

        return formattedPath;
    }



    /**
     * Obtains the value of a property in the request that is currently being archived.
     *
     * @param request   the current request
     * @param fieldName the name of the property
     * @return the value of the property, or <code>null</code> if the value could not be obtained
     */
    private static String getProductFieldValue(final IProduct request, final String fieldName) {

        try {
            final Field field = request.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            final Object fieldInstance = field.get(request);
            field.setAccessible(false);

            if (fieldInstance == null) {
                return "";
            }

            String fieldValue = fieldInstance.toString();

            if (field.getType().isAssignableFrom(Calendar.class)) {
                Calendar calendarFieldInstance = (Calendar) fieldInstance;
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

                if (calendarFieldInstance.getTime() != null) {
                    fieldValue = formatter.format(calendarFieldInstance.getTime());
                }
            }

            return URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString());

        } catch (NoSuchFieldException noFieldException) {
            RequestUtils.LOGGER.error(
                    String.format("Could not find the field \"%s\" in the request object.", fieldName),
                    noFieldException);

        } catch (IllegalAccessException permissionException) {
            RequestUtils.LOGGER.error(
                    String.format("Could not access to the field value for \"%s\".", fieldName),
                    permissionException);
        } catch (UnsupportedEncodingException encodingException) {
            RequestUtils.LOGGER.error(
                    String.format("The field value for \"%s\" contains characters that cannot be URL encoded.",
                            fieldName),
                    encodingException);
        }

        return null;
    }

}
