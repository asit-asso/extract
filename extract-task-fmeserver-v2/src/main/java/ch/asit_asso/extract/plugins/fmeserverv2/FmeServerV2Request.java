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
package ch.asit_asso.extract.plugins.fmeserverv2;

import ch.asit_asso.extract.plugins.common.ITaskProcessorRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Objects;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for FME Server V2 request handling with GeoJSON conversion and validation.
 *
 * @author Extract Team
 */
public class FmeServerV2Request {

    /**
     * Logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(FmeServerV2Request.class);

    /**
     * The original request data.
     */
    private final ITaskProcessorRequest request;

    /**
     * Plugin configuration.
     */
    private final PluginConfiguration config;

    /**
     * ObjectMapper for JSON processing.
     */
    private final ObjectMapper mapper;

    /**
     * Creates a new FME Server V2 request handler.
     *
     * @param request the request to handle
     * @param config the plugin configuration
     */
    public FmeServerV2Request(ITaskProcessorRequest request, PluginConfiguration config) {
        this.request = Objects.requireNonNull(request, "Request cannot be null");
        this.config = Objects.requireNonNull(config, "Configuration cannot be null");
        this.mapper = new ObjectMapper();
    }

    /**
     * Creates the GeoJSON feature for the FME Server request.
     *
     * @return GeoJSON feature as string
     * @throws Exception if conversion fails
     */
    public String createGeoJsonFeature() throws Exception {
        ObjectNode root = mapper.createObjectNode();
        root.put("type", "Feature");

        // Add geometry if available
        addGeometry(root);

        // Add properties
        ObjectNode properties = createProperties();
        root.set("properties", properties);

        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
    }

    /**
     * Adds geometry to the GeoJSON feature.
     *
     * @param root the root JSON node
     */
    private void addGeometry(ObjectNode root) {
        String perimeter = request.getPerimeter();

        if (perimeter == null || perimeter.trim().isEmpty()) {
            logger.debug("No perimeter defined, using null geometry");
            root.putNull("geometry");
            return;
        }

        try {
            ObjectNode geometryNode = convertWKTToGeoJSON(perimeter);
            root.set("geometry", geometryNode);
            logger.debug("Successfully converted WKT to GeoJSON geometry");

        } catch (ParseException e) {
            logger.warn("Failed to parse WKT geometry: {}", e.getMessage());
            root.putNull("geometry");

        } catch (Exception e) {
            logger.error("Unexpected error converting geometry", e);
            root.putNull("geometry");
        }
    }

    /**
     * Creates the properties object for the GeoJSON feature.
     *
     * @return properties as ObjectNode
     */
    private ObjectNode createProperties() {
        ObjectNode properties = mapper.createObjectNode();

        // Add basic request information
        addBasicProperties(properties);

        // Add client and organization information
        addClientProperties(properties);

        // Add product information
        addProductProperties(properties);

        // Add surface calculation if possible
        addSurfaceProperty(properties);

        // Add custom parameters
        addCustomParameters(properties);

        return properties;
    }

    /**
     * Adds basic request properties.
     *
     * @param properties the properties node
     */
    private void addBasicProperties(ObjectNode properties) {
        // Use configuration keys if available, otherwise use defaults
        String requestIdKey = config.getProperty("paramRequestInternalId", "Request");
        String folderOutKey = config.getProperty("paramRequestFolderOut", "FolderOut");
        String orderGuidKey = config.getProperty("paramRequestOrderGuid", "OrderGuid");
        String orderLabelKey = config.getProperty("paramRequestOrderLabel", "OrderLabel");

        addPropertySafe(properties, requestIdKey, String.valueOf(request.getId()));
        addPropertySafe(properties, folderOutKey, request.getFolderOut());
        addPropertySafe(properties, orderGuidKey, request.getOrderGuid());
        addPropertySafe(properties, orderLabelKey, request.getOrderLabel());

        logger.trace("Added basic properties to request");
    }

    /**
     * Adds client and organization properties.
     *
     * @param properties the properties node
     */
    private void addClientProperties(ObjectNode properties) {
        String clientGuidKey = config.getProperty("paramRequestClientGuid", "ClientGuid");
        String clientNameKey = config.getProperty("paramRequestClientName", "ClientName");
        String organismGuidKey = config.getProperty("paramRequestOrganismGuid", "OrganismGuid");
        String organismNameKey = config.getProperty("paramRequestOrganismName", "OrganismName");

        addPropertySafe(properties, clientGuidKey, request.getClientGuid());
        addPropertySafe(properties, clientNameKey, request.getClient());
        addPropertySafe(properties, organismGuidKey, request.getOrganismGuid());
        addPropertySafe(properties, organismNameKey, request.getOrganism());

        logger.trace("Added client properties to request");
    }

    /**
     * Adds product properties.
     *
     * @param properties the properties node
     */
    private void addProductProperties(ObjectNode properties) {
        String productGuidKey = config.getProperty("paramRequestProductGuid", "ProductGuid");
        String productLabelKey = config.getProperty("paramRequestProductLabel", "ProductLabel");

        addPropertySafe(properties, productGuidKey, request.getProductGuid());
        addPropertySafe(properties, productLabelKey, request.getProductLabel());

        logger.trace("Added product properties to request");
    }

    /**
     * Safely adds a property to the JSON node.
     *
     * @param node the JSON node
     * @param key the property key
     * @param value the property value
     */
    private void addPropertySafe(ObjectNode node, String key, String value) {
        if (key != null && !key.trim().isEmpty()) {
            if (value != null) {
                node.put(key, value);
            } else {
                node.putNull(key);
            }
        }
    }

    /**
     * Adds surface calculation if perimeter is available.
     *
     * @param properties the properties node
     */
    private void addSurfaceProperty(ObjectNode properties) {
        if (request.getPerimeter() == null || request.getPerimeter().trim().isEmpty()) {
            return;
        }

        try {
            double surface = calculateSurface(request.getPerimeter());
            String surfaceKey = config.getProperty("paramRequestSurface", "Surface");
            properties.put(surfaceKey, surface);
            logger.debug("Calculated surface: {} m²", surface);

        } catch (Exception e) {
            logger.debug("Could not calculate surface: {}", e.getMessage());
        }
    }

    /**
     * Adds custom parameters from the request.
     *
     * @param properties the properties node
     */
    private void addCustomParameters(ObjectNode properties) {
        String parametersKey = config.getProperty("paramRequestParameters", "Parameters");
        String parametersJson = request.getParameters();

        if (parametersJson == null || parametersJson.trim().isEmpty()) {
            properties.set(parametersKey, mapper.createObjectNode());
            return;
        }

        try {
            // Try to parse as JSON
            ObjectNode parametersNode = (ObjectNode) mapper.readTree(parametersJson);
            properties.set(parametersKey, parametersNode);
            logger.debug("Added {} custom parameters", parametersNode.size());

        } catch (Exception e) {
            logger.warn("Could not parse custom parameters as JSON, adding as string: {}", e.getMessage());
            properties.put(parametersKey, parametersJson);
        }
    }

    /**
     * Calculates the surface area from a WKT perimeter string.
     *
     * @param wktPerimeter the WKT string
     * @return approximate surface area in square meters
     * @throws ParseException if WKT parsing fails
     */
    private double calculateSurface(String wktPerimeter) throws ParseException {
        WKTReader reader = new WKTReader();
        Geometry geometry = reader.read(wktPerimeter);

        // This is a rough approximation assuming WGS84 coordinates
        // For accurate area calculation, projection to a local coordinate system would be needed
        double area = geometry.getArea();

        // Very rough conversion to square meters (assuming degrees)
        // This should be improved with proper projection
        double metersPerDegree = 111000; // Approximate at equator
        return area * metersPerDegree * metersPerDegree;
    }

    /**
     * Converts WKT geometry string to GeoJSON geometry object.
     *
     * @param wkt the WKT string
     * @return GeoJSON geometry as ObjectNode
     * @throws ParseException if WKT parsing fails
     */
    private ObjectNode convertWKTToGeoJSON(String wkt) throws ParseException {
        if (wkt == null || wkt.trim().isEmpty()) {
            throw new IllegalArgumentException("WKT string cannot be null or empty");
        }

        WKTReader reader = new WKTReader();
        Geometry geometry = reader.read(wkt);

        ObjectNode geoJsonGeometry = mapper.createObjectNode();

        if (geometry instanceof org.locationtech.jts.geom.Point) {
            convertPoint(geometry, geoJsonGeometry);

        } else if (geometry instanceof Polygon) {
            convertPolygon((Polygon) geometry, geoJsonGeometry);

        } else if (geometry instanceof MultiPolygon) {
            convertMultiPolygon((MultiPolygon) geometry, geoJsonGeometry);

        } else if (geometry instanceof org.locationtech.jts.geom.LineString) {
            convertLineString(geometry, geoJsonGeometry);

        } else if (geometry instanceof org.locationtech.jts.geom.MultiLineString) {
            convertMultiLineString(geometry, geoJsonGeometry);

        } else {
            logger.warn("Unsupported geometry type: {}", geometry.getGeometryType());
            throw new IllegalArgumentException("Unsupported geometry type: " + geometry.getGeometryType());
        }

        return geoJsonGeometry;
    }

    /**
     * Converts a Point to GeoJSON.
     */
    private void convertPoint(Geometry geometry, ObjectNode geoJsonGeometry) {
        geoJsonGeometry.put("type", "Point");
        Coordinate coord = geometry.getCoordinate();
        ArrayNode coordinates = mapper.createArrayNode();
        coordinates.add(coord.x);
        coordinates.add(coord.y);
        if (!Double.isNaN(coord.z)) {
            coordinates.add(coord.z);
        }
        geoJsonGeometry.set("coordinates", coordinates);
    }

    /**
     * Converts a LineString to GeoJSON.
     */
    private void convertLineString(Geometry geometry, ObjectNode geoJsonGeometry) {
        geoJsonGeometry.put("type", "LineString");
        ArrayNode coordinates = coordinatesToArray(geometry.getCoordinates());
        geoJsonGeometry.set("coordinates", coordinates);
    }

    /**
     * Converts a MultiLineString to GeoJSON.
     */
    private void convertMultiLineString(Geometry geometry, ObjectNode geoJsonGeometry) {
        geoJsonGeometry.put("type", "MultiLineString");
        ArrayNode multiCoordinates = mapper.createArrayNode();

        for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Geometry lineString = geometry.getGeometryN(i);
            ArrayNode coordinates = coordinatesToArray(lineString.getCoordinates());
            multiCoordinates.add(coordinates);
        }

        geoJsonGeometry.set("coordinates", multiCoordinates);
    }

    /**
     * Converts a Polygon to GeoJSON.
     */
    private void convertPolygon(Polygon polygon, ObjectNode geoJsonGeometry) {
        geoJsonGeometry.put("type", "Polygon");
        ArrayNode coordinates = polygonToCoordinates(polygon);
        geoJsonGeometry.set("coordinates", coordinates);
    }

    /**
     * Converts a MultiPolygon to GeoJSON.
     */
    private void convertMultiPolygon(MultiPolygon multiPolygon, ObjectNode geoJsonGeometry) {
        geoJsonGeometry.put("type", "MultiPolygon");
        ArrayNode multiCoordinates = mapper.createArrayNode();

        for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
            Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
            ArrayNode coordinates = polygonToCoordinates(polygon);
            multiCoordinates.add(coordinates);
        }

        geoJsonGeometry.set("coordinates", multiCoordinates);
    }

    /**
     * Converts a JTS Polygon to GeoJSON coordinates array.
     */
    private ArrayNode polygonToCoordinates(Polygon polygon) {
        ArrayNode rings = mapper.createArrayNode();

        // Add exterior ring
        ArrayNode exteriorRing = coordinatesToArray(polygon.getExteriorRing().getCoordinates());
        rings.add(exteriorRing);

        // Add interior rings (holes)
        for (int i = 0; i < polygon.getNumInteriorRing(); i++) {
            ArrayNode interiorRing = coordinatesToArray(polygon.getInteriorRingN(i).getCoordinates());
            rings.add(interiorRing);
        }

        return rings;
    }

    /**
     * Converts an array of Coordinates to a JSON array.
     */
    private ArrayNode coordinatesToArray(Coordinate[] coords) {
        ArrayNode coordinates = mapper.createArrayNode();

        for (Coordinate coord : coords) {
            ArrayNode point = mapper.createArrayNode();
            point.add(coord.x);
            point.add(coord.y);
            if (!Double.isNaN(coord.z)) {
                point.add(coord.z);
            }
            coordinates.add(point);
        }

        return coordinates;
    }

    /**
     * Validates the request has required fields.
     *
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return request.getFolderOut() != null && !request.getFolderOut().trim().isEmpty();
    }

    /**
     * Gets the original request.
     *
     * @return the request
     */
    public ITaskProcessorRequest getRequest() {
        return request;
    }
}