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
package ch.asit_asso.extract.domain.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Transforms a JSON string to a map of keys and values and vice versa.
 *
 * @author Yves Grasset
 */
@Converter
public class JsonToParametersValuesConverter implements AttributeConverter<HashMap<String, String>, String> {

    /**
     * The writer to the application logs.
     */
    private final Logger logger;

    /**
     * The object-to-JSON-to-object transformer.
     */
    private final ObjectMapper objectMapper;



    /**
     * Creates a new instance of this converter.
     */
    public JsonToParametersValuesConverter() {
        this.logger = LoggerFactory.getLogger(JsonToParametersValuesConverter.class);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.getFactory().configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), true);
    }



    /**
     * Transforms a map of keys and values to a JSON string.
     *
     * @param attribute the data to transform
     * @return the JSON string
     */
    @Override
    public final String convertToDatabaseColumn(final HashMap<String, String> attribute) {

        try {
            return this.objectMapper.writeValueAsString(attribute);

        } catch (JsonProcessingException exception) {
            this.logger.error("Could not convert the JSON string to a parameters values map.", exception);
            return null;
        }
    }



    /**
     * Parses a JSON string into a map of keys and values.
     *
     * @param dbData a JSON string that contains only keys and values
     * @return the map
     */
    @Override
    public final HashMap<String, String> convertToEntityAttribute(final String dbData) {

        try {
            return this.objectMapper.readValue(dbData, new TypeReference<>() {});

        } catch (IOException exception) {
            this.logger.error("Could not convert the parameters values map to JSON.", exception);
            return null;
        }
    }

}
