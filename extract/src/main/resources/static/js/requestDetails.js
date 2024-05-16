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

window.app = {};
var app = window.app;
var orderGeometryLayer = null;

/**
 * Permanently erases a file from the output folder of a request.
 *
 * @param {int} requestId The identifier of the request
 * @param {type} label The string that describes the request
 * @param {type} button The button that was clicked to trigger this action
 */
function deleteFile(requestId, label, button) {
    _executeFileAction(requestId, button, LANG_MESSAGES.requestDetails.deleteFileConfirm,
        [$(button).attr('data-file-path'), label]);
}


/**
 * Permanently erases a request.
 *
 * @param {int} id The identifier of the request to delete
 * @param {type} label The string that describes the request to delete
 * @param {type} button The button that was clicked to trigger this action
 */
function deleteRequest(id, label, button) {
    _executeRequestAction(id, label, button, LANG_MESSAGES.requestDetails.deleteConfirm);
}


/**
 * Abandons the processing of a request.
 *
 * @param {int}     id      The identifier of the request to reject
 * @param {string}  label   The string that describes the request to reject
 * @param {string}  remark  The string entered by the user inform the customer about the reasons why the request was
 *                           rejected.
 * @param {Object}  button  The button that was clicked to trigger this action
 */
function rejectRequest(id, label, remark, button) {
    _executeRequestAction(id, label, button, LANG_MESSAGES.requestDetails.rejectConfirm, remark);
}


/**
 * Restarts the processing of a request from the beginning.
 *
 * @param {int}     requestId      The identifier of the request whose processing must be restarted
 * @param {string}  label          The string that describes the request whose processing must be restarted
 * @param {Object}  button         The button that was clicked to trigger this action
 */
function relaunchProcess(requestId, label, button) {
    _executeRequestAction(requestId, label, button, LANG_MESSAGES.requestDetails.relaunchProcessConfirm);
}


/**
 * Reruns the active task of a request.
 *
 * @param {int}     requestId      The identifier of the request whose current task must be rerun
 * @param {string}  label          The string that describes the request whose current task must be rerun
 * @param {Object}  button         The button that was clicked to trigger this action
 */
function restartCurrentTask(requestId, label, button) {
    _executeRequestAction(requestId, label, button, LANG_MESSAGES.requestDetails.restartTaskConfirm);
}


/**
 * Reruns the active task of a request.
 *
 * @param {int}     requestId      The identifier of the request whose current task must be rerun
 * @param {string}  label          The string that describes the request whose current task must be rerun
 * @param {Object}  button         The button that was clicked to trigger this action
 */
function retryExport(requestId, label, button) {
    _executeRequestAction(requestId, label, button, LANG_MESSAGES.requestDetails.retryExportConfirm);
}


/**
 * Reexamines if a request can be associated with a process
 *
 * @param {int}     requestId      The identifier of the request to rematch with a process
 * @param {string}  label          The string that describes the request to rematch
 * @param {Object}  button         The button that was clicked to trigger this action
 */
function retryMatching(requestId, label, button) {
    _executeRequestAction(requestId, label, button, LANG_MESSAGES.requestDetails.retryMatchingConfirm);
}


/**
 * Abandons the active task of a request and proceeds with the next one.
 *
 * @param {int}     requestId      The identifier of the request whose current task must be skipped
 * @param {string}  label          The string that describes the request whose current task must be skipped
 * @param {Object}  button         The button that was clicked to trigger this action
 */
function skipCurrentTask(requestId, label, button) {
    _executeRequestAction(requestId, label, button, LANG_MESSAGES.requestDetails.skipTaskConfirm);
}


/**
 * Modifies a request that is currently in standby mode so that it can proceed.
 *
 * @param {int}     id      The identifier of the request to validate
 * @param {string}  label   The string that describes the request to validate
 * @param {string}  remark  The string entered by the user to give the customer further information about the
 *                           validation
 * @param {Object}  button  The button that was clicked to trigger this action
 */
function validateRequest(id, label, remark, button) {
    _executeRequestAction(id, label, button, LANG_MESSAGES.requestDetails.validateConfirm, remark);
}


/*************************** MAP **************************/

/**
 * Initializes the map and centers it on the perimeter of the current order.
 *
 * @param {String} orderWktGeometry a string representing the perimeter of the order as a WKT geometry
 * @param {Float}  geometryArea     the surface of the area for this order in square meters
 */
function loadOrderGeometryMap(orderWktGeometry, geometryArea) {

    initializeMap().then(function (map) {
        _addOrderGeometryToMap(orderWktGeometry, map);
        _initializeFullScreenControl(map);
        _initializeLayerSwitcher(map);
        _initializeExportButtons(map);
    });

    $('#orderAreaSize').text(_getAreaSizeText(geometryArea, 2));
}


/**
 * Creates a map to display the perimeter of the current order.
 *
 * @param {String} orderWktGeometry the string that contains the coordinates of the request extent in the
 *                                               WKT format
 * @param {ol.Map} map              the OpenLayers map to add the order geometry to
 */
function _addOrderGeometryToMap(orderWktGeometry, map) {
    orderGeometryLayer = _createOrderGeometryLayer(orderWktGeometry, map.getView().getProjection());
    map.addLayer(orderGeometryLayer);
    map.getView().fit(orderGeometryLayer.getSource().getExtent(), {
        padding: [10, 10, 10, 10]
    });
}


/**
 * Builds a vector layer to display the extent of the current request.
 *
 * @param {String}             orderWktGeometry the string that contains the coordinates of the request extent in the
 *                                               WKT format
 * @param {ol.proj.Projection} mapProjection    the OpenLayers projection object used by the map
 * @returns {ol.layer.Vector} the layer to add to the map to show the perimeter of the request
 */
function _createOrderGeometryLayer(orderWktGeometry, mapProjection) {
    var wktFormat = new ol.format.WKT();
    var feature = wktFormat.readFeature(orderWktGeometry, {
        dataProjection: 'EPSG:4326',
        featureProjection: mapProjection
    });

    var orderGeometryStyle = new ol.style.Style({
        fill: new ol.style.Fill({
            color: 'rgba(126,237,24,0.5)'
        }),
        stroke: new ol.style.Stroke({
            color: '#723A09',
            width: 1.25
        })
    });

    return new ol.layer.Vector({
        title: LANG_MESSAGES.requestDetails.mapLayers.polygon.title,
        source: new ol.source.Vector({
            features: [feature]
        }),
        style: orderGeometryStyle
    });
}


/**
 * Computes a text representation of an area.
 *
 * @param {Number} rawAreaSize the area
 * @param {int} decimals the number of digits to round the area to
 * @returns {String} the area as a text
 */
function _getAreaSizeText(rawAreaSize, decimals) {
    var areaSizeUnit = "m²";
    var roundingFactor = Math.pow(10, decimals);
    var areaSize = rawAreaSize;

    if (areaSize >= 100000) {
        areaSize = rawAreaSize / 1000000;
        areaSizeUnit = "km²";
    }

    return (Math.round(areaSize * roundingFactor) / roundingFactor) + "\xA0" + areaSizeUnit;
}


/**
 * Adds a component that allows to switch layers on or off and change the base map.
 *
 * @param {ol.Map} map the OpenLayers map to add the layer switcher to
 */
function _initializeExportButtons(map) {
    map.addControl(new ExportToDxfControl({
        label: LANG_MESSAGES.requestDetails.exportToDxf.label,
        tooltip: LANG_MESSAGES.requestDetails.exportToDxf.tooltip
    }));
    map.addControl(new ExportToKmlControl({
        label: LANG_MESSAGES.requestDetails.exportToKml.label,
        tooltip: LANG_MESSAGES.requestDetails.exportToKml.tooltip
    }));
}


/**
 * Adds a component that allows to switch layers on or off and change the base map.
 *
 * @param {ol.Map} map the OpenLayers map to add the layer switcher to
 */
function _initializeLayerSwitcher(map) {
    var layerSwitcher = new ol.control.LayerSwitcher({
        tipLabel: LANG_MESSAGES.requestDetails.layerSwitcher.tooltip
    });

    map.addControl(layerSwitcher);
}


function _initializeFullScreenControl(map) {
    var fullScreenControl = new ol.control.FullScreen({
        tipLabel: LANG_MESSAGES.requestDetails.fullScreenControl.tooltip
    });

    map.addControl(fullScreenControl);
}


function _sendTextAsDownload(text, mimeType, fileName) {
    var data = new Blob([text], {
        type: mimeType
    });

    if (navigator.msSaveOrOpenBlob) {
        navigator.msSaveOrOpenBlob(data, fileName);
        return;
    }

    var link = document.createElement("a");
    link.style.visibility = "hidden";
    link.href = URL.createObjectURL(data);
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
}


/******************** CUSTOM CONTROLS *********************/

/**
 * @constructor
 * @extends {ol.control.Control}
 * @param {Object=} opt_options Control options.
 */
class ExportToKmlControl extends ol.control.Control {

    constructor(opt_options) {
        var options = opt_options || {};

        var button = document.createElement('button');
        button.innerHTML = options.label || "KML";
        button.title = options.tooltip;


        var element = document.createElement('div');
        element.className = 'export-kml export-button ol-unselectable ol-control';
        element.appendChild(button);

        super({
            element: element,
            target: options.target
        });

        button.addEventListener('click', this.handleExportToKml.bind(this), false);
        button.addEventListener('touchstart', this.handleExportToKml.bind(this), false);
    }

    handleExportToKml() {
        var mimeType = "application/vnd.google-earth.kml+xml";
        var format = new ol.format.KML({
            extractStyles: true
        });

        var sourceFeatures = orderGeometryLayer.getSource().getFeatures();
        var featuresToWrite = [];
        var kmlStyle = new ol.style.Style({
            fill: new ol.style.Fill({
                color: 'rgba(255,0,0,0.4)'
            }),
            stroke: new ol.style.Stroke({
                color: '#ff0000',
                width: 1.25
            })
        });

        sourceFeatures.forEach(
            function (f) {
                var featureClone = f.clone();
                featureClone.setStyle(kmlStyle);
                featuresToWrite.push(featureClone);
            }
        );

        var data = format.writeFeatures(featuresToWrite, {
            featureProjection: this.getMap().getView().getProjection(),
            dataProjection: new ol.proj.Projection({code: 'EPSG:4326'})
        });

        _sendTextAsDownload(data, mimeType, $('#requestId').val() + '.kml');
    }
}

/**
 * @constructor
 * @extends {ol.control.Control}
 * @param {Object=} opt_options Control options.
 */
class ExportToDxfControl extends ol.control.Control {
    constructor(opt_options) {
        var options = opt_options || {};

        var button = document.createElement('button');
        button.innerHTML = options.label || "DXF";
        button.title = options.tooltip;

        var element = document.createElement('div');
        element.className = 'export-dxf export-button ol-unselectable ol-control';
        element.appendChild(button);

        super({
            element: element,
            target: options.target
        });

        button.addEventListener('click', this.handleExportToDxf.bind(this), false);
        button.addEventListener('touchstart', this.handleExportToDxf.bind(this), false);
        this.DxfGlobalHandle = 2000;
    }

    buildDxfPolyline(ring) {
        console.log(ring);

        this.DxfGlobalHandle++;
        var pointsCount = ring.length;
        var dxfPolygon = '  0\n' +
            'POLYLINE\n' +
            '  5\n' +
            '' + this.decimalToHexadecimal(this.DxfGlobalHandle) + '\n' +
            '  8\n' +
            '0\n' +
            ' 66\n' +
            '     1\n' +
            ' 10\n' +
            '0.0\n' +
            ' 20\n' +
            '0.0\n' +
            ' 30\n' +
            '0.0\n' +
            ' 70\n' +
            '1\n';

        for (var i = 0; i < pointsCount; i++) {
            var vertex = ring[i];
            this.DxfGlobalHandle++;

            dxfPolygon += '  0\n' +
                'VERTEX\n' +
                '  5\n' +
                '' + this.decimalToHexadecimal(this.DxfGlobalHandle).toUpperCase() + '\n' +
                '  8\n' +
                '0\n' +
                ' 10\n' +
                '' + vertex[0] + '\n' +
                ' 20\n' +
                '' + vertex[1] + '\n' +
                ' 30\n' +
                '0.0\n' +
                '';
        }

        this.DxfGlobalHandle++;
        dxfPolygon += '  0\n' +
            'SEQEND\n' +
            '  5\n' +
            '' + this.decimalToHexadecimal(this.DxfGlobalHandle) + '\n' +
            '  8\n' +
            '0\n';

        return dxfPolygon;
    }

    buildDxfTemplate(bbox, rings) {
        console.log(rings);

        var polygons = '';

        for (var i = 0; i < rings.length; i++) {
            polygons += this.buildDxfPolyline(rings[i]);
        }

        return '  0\n' +
            'SECTION\n' +
            '  2\n' +
            'HEADER\n' +
            '  9\n' +
            '$ACADVER\n' +
            '  1\n' +
            'AC1009\n' +
            '  9\n' +
            '$INSBASE\n' +
            ' 10\n' +
            '0.0\n' +
            ' 20\n' +
            '0.0\n' +
            ' 30\n' +
            '0.0\n' +
            '  9\n' +
            '$EXTMIN\n' +
            ' 10\n' +
            '' + bbox[0] + '\n' +
            ' 20\n' +
            '' + bbox[1] + '\n' +
            ' 30\n' +
            '0.0\n' +
            '  9\n' +
            '$EXTMAX\n' +
            ' 10\n' +
            '' + bbox[2] + '\n' +
            ' 20\n' +
            '' + bbox[3] + '\n' +
            ' 30\n' +
            '0.0\n' +
            '  9\n' +
            '$LIMMIN\n' +
            ' 10\n' +
            '0.0\n' +
            ' 20\n' +
            '0.0\n' +
            '  9\n' +
            '$LIMMAX\n' +
            ' 10\n' +
            '420.0\n' +
            ' 20\n' +
            '297.0\n' +
            '  9\n' +
            '$ORTHOMODE\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$REGENMODE\n' +
            ' 70\n' +
            '     1\n' +
            '  9\n' +
            '$FILLMODE\n' +
            ' 70\n' +
            '     1\n' +
            '  9\n' +
            '$QTEXTMODE\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$MIRRTEXT\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$DRAGMODE\n' +
            ' 70\n' +
            '     2\n' +
            '  9\n' +
            '$LTSCALE\n' +
            ' 40\n' +
            '1.0\n' +
            '  9\n' +
            '$OSMODE\n' +
            ' 70\n' +
            '    37\n' +
            '  9\n' +
            '$ATTMODE\n' +
            ' 70\n' +
            '     1\n' +
            '  9\n' +
            '$TEXTSIZE\n' +
            ' 40\n' +
            '2.5\n' +
            '  9\n' +
            '$TRACEWID\n' +
            ' 40\n' +
            '1.0\n' +
            '  9\n' +
            '$TEXTSTYLE\n' +
            '  7\n' +
            'STANDARD\n' +
            '  9\n' +
            '$CLAYER\n' +
            '  8\n' +
            '0\n' +
            '  9\n' +
            '$CELTYPE\n' +
            '  6\n' +
            'BYLAYER\n' +
            '  9\n' +
            '$CECOLOR\n' +
            ' 62\n' +
            '   256\n' +
            '  9\n' +
            '$DIMSCALE\n' +
            ' 40\n' +
            '1.0\n' +
            '  9\n' +
            '$DIMASZ\n' +
            ' 40\n' +
            '2.5\n' +
            '  9\n' +
            '$DIMEXO\n' +
            ' 40\n' +
            '0.625\n' +
            '  9\n' +
            '$DIMDLI\n' +
            ' 40\n' +
            '3.75\n' +
            '  9\n' +
            '$DIMRND\n' +
            ' 40\n' +
            '0.0\n' +
            '  9\n' +
            '$DIMDLE\n' +
            ' 40\n' +
            '0.0\n' +
            '  9\n' +
            '$DIMEXE\n' +
            ' 40\n' +
            '1.25\n' +
            '  9\n' +
            '$DIMTP\n' +
            ' 40\n' +
            '0.0\n' +
            '  9\n' +
            '$DIMTM\n' +
            ' 40\n' +
            '0.0\n' +
            '  9\n' +
            '$DIMTXT\n' +
            ' 40\n' +
            '2.5\n' +
            '  9\n' +
            '$DIMCEN\n' +
            ' 40\n' +
            '2.5\n' +
            '  9\n' +
            '$DIMTSZ\n' +
            ' 40\n' +
            '0.0\n' +
            '  9\n' +
            '$DIMTOL\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$DIMLIM\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$DIMTIH\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$DIMTOH\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$DIMSE1\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$DIMSE2\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$DIMTAD\n' +
            ' 70\n' +
            '     1\n' +
            '  9\n' +
            '$DIMZIN\n' +
            ' 70\n' +
            '     8\n' +
            '  9\n' +
            '$DIMBLK\n' +
            '  1\n' +
            '\n' +
            '  9\n' +
            '$DIMASO\n' +
            ' 70\n' +
            '     1\n' +
            '  9\n' +
            '$DIMSHO\n' +
            ' 70\n' +
            '     1\n' +
            '  9\n' +
            '$DIMPOST\n' +
            '  1\n' +
            '\n' +
            '  9\n' +
            '$DIMAPOST\n' +
            '  1\n' +
            '\n' +
            '  9\n' +
            '$DIMALT\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$DIMALTD\n' +
            ' 70\n' +
            '     3\n' +
            '  9\n' +
            '$DIMALTF\n' +
            ' 40\n' +
            '0.03937007874016\n' +
            '  9\n' +
            '$DIMLFAC\n' +
            ' 40\n' +
            '1.0\n' +
            '  9\n' +
            '$DIMTOFL\n' +
            ' 70\n' +
            '     1\n' +
            '  9\n' +
            '$DIMTVP\n' +
            ' 40\n' +
            '0.0\n' +
            '  9\n' +
            '$DIMTIX\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$DIMSOXD\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$DIMSAH\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$DIMBLK1\n' +
            '  1\n' +
            '\n' +
            '  9\n' +
            '$DIMBLK2\n' +
            '  1\n' +
            '\n' +
            '  9\n' +
            '$DIMSTYLE\n' +
            '  2\n' +
            'ISO-25\n' +
            '  9\n' +
            '$DIMCLRD\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$DIMCLRE\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$DIMCLRT\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$DIMTFAC\n' +
            ' 40\n' +
            '1.0\n' +
            '  9\n' +
            '$DIMGAP\n' +
            ' 40\n' +
            '0.625\n' +
            '  9\n' +
            '$LUNITS\n' +
            ' 70\n' +
            '     2\n' +
            '  9\n' +
            '$LUPREC\n' +
            ' 70\n' +
            '     4\n' +
            '  9\n' +
            '$SKETCHINC\n' +
            ' 40\n' +
            '1.0\n' +
            '  9\n' +
            '$FILLETRAD\n' +
            ' 40\n' +
            '0.0\n' +
            '  9\n' +
            '$AUNITS\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$AUPREC\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$MENU\n' +
            '  1\n' +
            '.\n' +
            '  9\n' +
            '$ELEVATION\n' +
            ' 40\n' +
            '0.0\n' +
            '  9\n' +
            '$PELEVATION\n' +
            ' 40\n' +
            '0.0\n' +
            '  9\n' +
            '$THICKNESS\n' +
            ' 40\n' +
            '0.0\n' +
            '  9\n' +
            '$LIMCHECK\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$BLIPMODE\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$CHAMFERA\n' +
            ' 40\n' +
            '0.0\n' +
            '  9\n' +
            '$CHAMFERB\n' +
            ' 40\n' +
            '0.0\n' +
            '  9\n' +
            '$SKPOLY\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$TDCREATE\n' +
            ' 40\n' +
            '2456337.6735687042\n' +
            '  9\n' +
            '$TDUPDATE\n' +
            ' 40\n' +
            '2456337.6787776388\n' +
            '  9\n' +
            '$TDINDWG\n' +
            ' 40\n' +
            '0.0052466782\n' +
            '  9\n' +
            '$TDUSRTIMER\n' +
            ' 40\n' +
            '0.0052157986\n' +
            '  9\n' +
            '$USRTIMER\n' +
            ' 70\n' +
            '     1\n' +
            '  9\n' +
            '$ANGBASE\n' +
            ' 50\n' +
            '0.0\n' +
            '  9\n' +
            '$ANGDIR\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$PDMODE\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$PDSIZE\n' +
            ' 40\n' +
            '0.0\n' +
            '  9\n' +
            '$PLINEWID\n' +
            ' 40\n' +
            '0.0\n' +
            '  9\n' +
            '$COORDS\n' +
            ' 70\n' +
            '     1\n' +
            '  9\n' +
            '$SPLFRAME\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$SPLINETYPE\n' +
            ' 70\n' +
            '     6\n' +
            '  9\n' +
            '$SPLINESEGS\n' +
            ' 70\n' +
            '     8\n' +
            '  9\n' +
            '$ATTDIA\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$ATTREQ\n' +
            ' 70\n' +
            '     1\n' +
            '  9\n' +
            '$HANDLING\n' +
            ' 70\n' +
            '     1\n' +
            '  9\n' +
            '$HANDSEED\n' +
            '  5\n' +
            '381\n' +
            '  9\n' +
            '$SURFTAB1\n' +
            ' 70\n' +
            '     6\n' +
            '  9\n' +
            '$SURFTAB2\n' +
            ' 70\n' +
            '     6\n' +
            '  9\n' +
            '$SURFTYPE\n' +
            ' 70\n' +
            '     6\n' +
            '  9\n' +
            '$SURFU\n' +
            ' 70\n' +
            '     6\n' +
            '  9\n' +
            '$SURFV\n' +
            ' 70\n' +
            '     6\n' +
            '  9\n' +
            '$UCSNAME\n' +
            '  2\n' +
            '\n' +
            '  9\n' +
            '$UCSORG\n' +
            ' 10\n' +
            '0.0\n' +
            ' 20\n' +
            '0.0\n' +
            ' 30\n' +
            '0.0\n' +
            '  9\n' +
            '$UCSXDIR\n' +
            ' 10\n' +
            '1.0\n' +
            ' 20\n' +
            '0.0\n' +
            ' 30\n' +
            '0.0\n' +
            '  9\n' +
            '$UCSYDIR\n' +
            ' 10\n' +
            '0.0\n' +
            ' 20\n' +
            '1.0\n' +
            ' 30\n' +
            '0.0\n' +
            '  9\n' +
            '$PUCSNAME\n' +
            '  2\n' +
            '\n' +
            '  9\n' +
            '$PUCSORG\n' +
            ' 10\n' +
            '0.0\n' +
            ' 20\n' +
            '0.0\n' +
            ' 30\n' +
            '0.0\n' +
            '  9\n' +
            '$PUCSXDIR\n' +
            ' 10\n' +
            '1.0\n' +
            ' 20\n' +
            '0.0\n' +
            ' 30\n' +
            '0.0\n' +
            '  9\n' +
            '$PUCSYDIR\n' +
            ' 10\n' +
            '0.0\n' +
            ' 20\n' +
            '1.0\n' +
            ' 30\n' +
            '0.0\n' +
            '  9\n' +
            '$USERI1\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$USERI2\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$USERI3\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$USERI4\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$USERI5\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$USERR1\n' +
            ' 40\n' +
            '0.0\n' +
            '  9\n' +
            '$USERR2\n' +
            ' 40\n' +
            '0.0\n' +
            '  9\n' +
            '$USERR3\n' +
            ' 40\n' +
            '0.0\n' +
            '  9\n' +
            '$USERR4\n' +
            ' 40\n' +
            '0.0\n' +
            '  9\n' +
            '$USERR5\n' +
            ' 40\n' +
            '0.0\n' +
            '  9\n' +
            '$WORLDVIEW\n' +
            ' 70\n' +
            '     1\n' +
            '  9\n' +
            '$SHADEDGE\n' +
            ' 70\n' +
            '     3\n' +
            '  9\n' +
            '$SHADEDIF\n' +
            ' 70\n' +
            '    70\n' +
            '  9\n' +
            '$TILEMODE\n' +
            ' 70\n' +
            '     1\n' +
            '  9\n' +
            '$MAXACTVP\n' +
            ' 70\n' +
            '    64\n' +
            '  9\n' +
            '$PLIMCHECK\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$PEXTMIN\n' +
            ' 10\n' +
            '0.0\n' +
            ' 20\n' +
            '0.0\n' +
            ' 30\n' +
            '0.0\n' +
            '  9\n' +
            '$PEXTMAX\n' +
            ' 10\n' +
            '0.0\n' +
            ' 20\n' +
            '0.0\n' +
            ' 30\n' +
            '0.0\n' +
            '  9\n' +
            '$PLIMMIN\n' +
            ' 10\n' +
            '0.0\n' +
            ' 20\n' +
            '0.0\n' +
            '  9\n' +
            '$PLIMMAX\n' +
            ' 10\n' +
            '12.0\n' +
            ' 20\n' +
            '9.0\n' +
            '  9\n' +
            '$UNITMODE\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$VISRETAIN\n' +
            ' 70\n' +
            '     1\n' +
            '  9\n' +
            '$PLINEGEN\n' +
            ' 70\n' +
            '     0\n' +
            '  9\n' +
            '$PSLTSCALE\n' +
            ' 70\n' +
            '     1\n' +
            '  0\n' +
            'ENDSEC\n' +
            '  0\n' +
            'SECTION\n' +
            '  2\n' +
            'TABLES\n' +
            '  0\n' +
            'TABLE\n' +
            '  2\n' +
            'VPORT\n' +
            ' 70\n' +
            '     1\n' +
            '  0\n' +
            'VPORT\n' +
            '  2\n' +
            '*ACTIVE\n' +
            ' 70\n' +
            '     0\n' +
            ' 10\n' +
            '0.0\n' +
            ' 20\n' +
            '0.0\n' +
            ' 11\n' +
            '1.0\n' +
            ' 21\n' +
            '1.0\n' +
            ' 12\n' +
            '' + (bbox[0] + bbox[2]) / 2 + '\n' +
            ' 22\n' +
            '' + (bbox[1] + bbox[3]) / 2 + '\n' +
            ' 13\n' +
            '0.0\n' +
            ' 23\n' +
            '0.0\n' +
            ' 14\n' +
            '10.0\n' +
            ' 24\n' +
            '10.0\n' +
            ' 15\n' +
            '10.0\n' +
            ' 25\n' +
            '10.0\n' +
            ' 16\n' +
            '0.0\n' +
            ' 26\n' +
            '0.0\n' +
            ' 36\n' +
            '1.0\n' +
            ' 17\n' +
            '0.0\n' +
            ' 27\n' +
            '0.0\n' +
            ' 37\n' +
            '0.0\n' +
            ' 40\n' +
            '1012.684627994895\n' +
            ' 41\n' +
            '2.303519061583577\n' +
            ' 42\n' +
            '50.0\n' +
            ' 43\n' +
            '0.0\n' +
            ' 44\n' +
            '0.0\n' +
            ' 50\n' +
            '0.0\n' +
            ' 51\n' +
            '0.0\n' +
            ' 71\n' +
            '     0\n' +
            ' 72\n' +
            '  1000\n' +
            ' 73\n' +
            '     1\n' +
            ' 74\n' +
            '     3\n' +
            ' 75\n' +
            '     0\n' +
            ' 76\n' +
            '     0\n' +
            ' 77\n' +
            '     0\n' +
            ' 78\n' +
            '     0\n' +
            '  0\n' +
            'ENDTAB\n' +
            '  0\n' +
            'TABLE\n' +
            '  2\n' +
            'LTYPE\n' +
            ' 70\n' +
            '     2\n' +
            '  0\n' +
            'LTYPE\n' +
            '  2\n' +
            'CONTINUOUS\n' +
            ' 70\n' +
            '     0\n' +
            '  3\n' +
            'Solid line\n' +
            ' 72\n' +
            '    65\n' +
            ' 73\n' +
            '     0\n' +
            ' 40\n' +
            '0.0\n' +
            '  0\n' +
            'ENDTAB\n' +
            '  0\n' +
            'TABLE\n' +
            '  2\n' +
            'LAYER\n' +
            ' 70\n' +
            '     1\n' +
            '  0\n' +
            'LAYER\n' +
            '  2\n' +
            '0\n' +
            ' 70\n' +
            '     0\n' +
            ' 62\n' +
            '     7\n' +
            '  6\n' +
            'CONTINUOUS\n' +
            '  0\n' +
            'ENDTAB\n' +
            '  0\n' +
            'TABLE\n' +
            '  2\n' +
            'STYLE\n' +
            ' 70\n' +
            '     3\n' +
            '  0\n' +
            'STYLE\n' +
            '  2\n' +
            'STANDARD\n' +
            ' 70\n' +
            '     0\n' +
            ' 40\n' +
            '0.0\n' +
            ' 41\n' +
            '1.0\n' +
            ' 50\n' +
            '0.0\n' +
            ' 71\n' +
            '     0\n' +
            ' 42\n' +
            '2.5\n' +
            '  3\n' +
            'txt\n' +
            '  4\n' +
            '\n' +
            '  0\n' +
            'STYLE\n' +
            '  2\n' +
            'ANNOTATIF\n' +
            ' 70\n' +
            '     0\n' +
            ' 40\n' +
            '0.0\n' +
            ' 41\n' +
            '1.0\n' +
            ' 50\n' +
            '0.0\n' +
            ' 71\n' +
            '     0\n' +
            ' 42\n' +
            '2.5\n' +
            '  3\n' +
            'txt\n' +
            '  4\n' +
            '\n' +
            '  0\n' +
            'STYLE\n' +
            '  2\n' +
            'LEGEND\n' +
            ' 70\n' +
            '     0\n' +
            ' 40\n' +
            '0.0\n' +
            ' 41\n' +
            '1.0\n' +
            ' 50\n' +
            '0.0\n' +
            ' 71\n' +
            '     0\n' +
            ' 42\n' +
            '2.5\n' +
            '  3\n' +
            'txt\n' +
            '  4\n' +
            '\n' +
            '  0\n' +
            'ENDTAB\n' +
            '  0\n' +
            'TABLE\n' +
            '  2\n' +
            'VIEW\n' +
            ' 70\n' +
            '     1\n' +
            '  0\n' +
            'ENDTAB\n' +
            '  0\n' +
            'TABLE\n' +
            '  2\n' +
            'UCS\n' +
            ' 70\n' +
            '     0\n' +
            '  0\n' +
            'ENDTAB\n' +
            '  0\n' +
            'TABLE\n' +
            '  2\n' +
            'APPID\n' +
            ' 70\n' +
            '    15\n' +
            '  0\n' +
            'APPID\n' +
            '  2\n' +
            'ACAD\n' +
            ' 70\n' +
            '     0\n' +
            '  0\n' +
            'APPID\n' +
            '  2\n' +
            'ACAD_PSEXT\n' +
            ' 70\n' +
            '     0\n' +
            '  0\n' +
            'APPID\n' +
            '  2\n' +
            'ACADANNOPO\n' +
            ' 70\n' +
            '     0\n' +
            '  0\n' +
            'APPID\n' +
            '  2\n' +
            'ACADANNOTATIVE\n' +
            ' 70\n' +
            '     0\n' +
            '  0\n' +
            'APPID\n' +
            '  2\n' +
            'ACAD_DSTYLE_DIMJAG\n' +
            ' 70\n' +
            '     0\n' +
            '  0\n' +
            'APPID\n' +
            '  2\n' +
            'ACAD_DSTYLE_DIMTALN\n' +
            ' 70\n' +
            '     0\n' +
            '  0\n' +
            'APPID\n' +
            '  2\n' +
            'ACAD_MLEADERVER\n' +
            ' 70\n' +
            '     0\n' +
            '  0\n' +
            'APPID\n' +
            '  2\n' +
            'ACAD_NAV_VCDISPLAY\n' +
            ' 70\n' +
            '     0\n' +
            '  0\n' +
            'APPID\n' +
            '  2\n' +
            'ACMAPDMDISPLAYSTYLEREGAPP\n' +
            ' 70\n' +
            '     0\n' +
            '  0\n' +
            'APPID\n' +
            '  2\n' +
            'ADE\n' +
            ' 70\n' +
            '     0\n' +
            '  0\n' +
            'APPID\n' +
            '  2\n' +
            'DCO15\n' +
            ' 70\n' +
            '     0\n' +
            '  0\n' +
            'APPID\n' +
            '  2\n' +
            'ADE_PROJECTION\n' +
            ' 70\n' +
            '     0\n' +
            '  0\n' +
            'APPID\n' +
            '  2\n' +
            'MAPGWS\n' +
            ' 70\n' +
            '     0\n' +
            '  0\n' +
            'APPID\n' +
            '  2\n' +
            'ACAD_EXEMPT_FROM_CAD_STANDARDS\n' +
            ' 70\n' +
            '     0\n' +
            '  0\n' +
            'APPID\n' +
            '  2\n' +
            'MapManagementAppName\n' +
            ' 70\n' +
            '     0\n' +
            '  0\n' +
            'ENDTAB\n' +
            '  0\n' +
            'TABLE\n' +
            '  2\n' +
            'DIMSTYLE\n' +
            ' 70\n' +
            '     4\n' +
            '  0\n' +
            'DIMSTYLE\n' +
            '  2\n' +
            'STANDARD\n' +
            ' 70\n' +
            '     0\n' +
            '  3\n' +
            '\n' +
            '  4\n' +
            '\n' +
            '  5\n' +
            '\n' +
            '  6\n' +
            '\n' +
            '  7\n' +
            '\n' +
            ' 40\n' +
            '1.0\n' +
            ' 41\n' +
            '0.18\n' +
            ' 42\n' +
            '0.0625\n' +
            ' 43\n' +
            '0.38\n' +
            ' 44\n' +
            '0.18\n' +
            ' 45\n' +
            '0.0\n' +
            ' 46\n' +
            '0.0\n' +
            ' 47\n' +
            '0.0\n' +
            ' 48\n' +
            '0.0\n' +
            '140\n' +
            '0.18\n' +
            '141\n' +
            '0.09\n' +
            '142\n' +
            '0.0\n' +
            '143\n' +
            '25.399999999999999\n' +
            '144\n' +
            '1.0\n' +
            '145\n' +
            '0.0\n' +
            '146\n' +
            '1.0\n' +
            '147\n' +
            '0.09\n' +
            ' 71\n' +
            '     0\n' +
            ' 72\n' +
            '     0\n' +
            ' 73\n' +
            '     1\n' +
            ' 74\n' +
            '     1\n' +
            ' 75\n' +
            '     0\n' +
            ' 76\n' +
            '     0\n' +
            ' 77\n' +
            '     0\n' +
            ' 78\n' +
            '     0\n' +
            '170\n' +
            '     0\n' +
            '171\n' +
            '     2\n' +
            '172\n' +
            '     0\n' +
            '173\n' +
            '     0\n' +
            '174\n' +
            '     0\n' +
            '175\n' +
            '     0\n' +
            '176\n' +
            '     0\n' +
            '177\n' +
            '     0\n' +
            '178\n' +
            '     0\n' +
            '  0\n' +
            'DIMSTYLE\n' +
            '  2\n' +
            'ANNOTATIF\n' +
            ' 70\n' +
            '     0\n' +
            '  3\n' +
            '\n' +
            '  4\n' +
            '\n' +
            '  5\n' +
            '\n' +
            '  6\n' +
            '\n' +
            '  7\n' +
            '\n' +
            ' 40\n' +
            '0.0\n' +
            ' 41\n' +
            '2.5\n' +
            ' 42\n' +
            '0.625\n' +
            ' 43\n' +
            '3.75\n' +
            ' 44\n' +
            '1.25\n' +
            ' 45\n' +
            '0.0\n' +
            ' 46\n' +
            '0.0\n' +
            ' 47\n' +
            '0.0\n' +
            ' 48\n' +
            '0.0\n' +
            '140\n' +
            '2.5\n' +
            '141\n' +
            '2.5\n' +
            '142\n' +
            '0.0\n' +
            '143\n' +
            '0.03937007874016\n' +
            '144\n' +
            '1.0\n' +
            '145\n' +
            '0.0\n' +
            '146\n' +
            '1.0\n' +
            '147\n' +
            '0.625\n' +
            ' 71\n' +
            '     0\n' +
            ' 72\n' +
            '     0\n' +
            ' 73\n' +
            '     0\n' +
            ' 74\n' +
            '     0\n' +
            ' 75\n' +
            '     0\n' +
            ' 76\n' +
            '     0\n' +
            ' 77\n' +
            '     1\n' +
            ' 78\n' +
            '     8\n' +
            '170\n' +
            '     0\n' +
            '171\n' +
            '     3\n' +
            '172\n' +
            '     1\n' +
            '173\n' +
            '     0\n' +
            '174\n' +
            '     0\n' +
            '175\n' +
            '     0\n' +
            '176\n' +
            '     0\n' +
            '177\n' +
            '     0\n' +
            '178\n' +
            '     0\n' +
            '  0\n' +
            'DIMSTYLE\n' +
            '  2\n' +
            'ISO-25\n' +
            ' 70\n' +
            '     0\n' +
            '  3\n' +
            '\n' +
            '  4\n' +
            '\n' +
            '  5\n' +
            '\n' +
            '  6\n' +
            '\n' +
            '  7\n' +
            '\n' +
            ' 40\n' +
            '1.0\n' +
            ' 41\n' +
            '2.5\n' +
            ' 42\n' +
            '0.625\n' +
            ' 43\n' +
            '3.75\n' +
            ' 44\n' +
            '1.25\n' +
            ' 45\n' +
            '0.0\n' +
            ' 46\n' +
            '0.0\n' +
            ' 47\n' +
            '0.0\n' +
            ' 48\n' +
            '0.0\n' +
            '140\n' +
            '2.5\n' +
            '141\n' +
            '2.5\n' +
            '142\n' +
            '0.0\n' +
            '143\n' +
            '0.03937007874016\n' +
            '144\n' +
            '1.0\n' +
            '145\n' +
            '0.0\n' +
            '146\n' +
            '1.0\n' +
            '147\n' +
            '0.625\n' +
            ' 71\n' +
            '     0\n' +
            ' 72\n' +
            '     0\n' +
            ' 73\n' +
            '     0\n' +
            ' 74\n' +
            '     0\n' +
            ' 75\n' +
            '     0\n' +
            ' 76\n' +
            '     0\n' +
            ' 77\n' +
            '     1\n' +
            ' 78\n' +
            '     8\n' +
            '170\n' +
            '     0\n' +
            '171\n' +
            '     3\n' +
            '172\n' +
            '     1\n' +
            '173\n' +
            '     0\n' +
            '174\n' +
            '     0\n' +
            '175\n' +
            '     0\n' +
            '176\n' +
            '     0\n' +
            '177\n' +
            '     0\n' +
            '178\n' +
            '     0\n' +
            '  0\n' +
            'ENDTAB\n' +
            '  0\n' +
            'ENDSEC\n' +
            '  0\n' +
            'SECTION\n' +
            '  2\n' +
            'BLOCKS\n' +
            '  0\n' +
            'BLOCK\n' +
            '  8\n' +
            '0\n' +
            '  2\n' +
            '$MODEL_SPACE\n' +
            ' 70\n' +
            '     0\n' +
            ' 10\n' +
            '0.0\n' +
            ' 20\n' +
            '0.0\n' +
            ' 30\n' +
            '0.0\n' +
            '  3\n' +
            '$MODEL_SPACE\n' +
            '  1\n' +
            '\n' +
            '  0\n' +
            'ENDBLK\n' +
            '  5\n' +
            '21\n' +
            '  8\n' +
            '0\n' +
            '  0\n' +
            'BLOCK\n' +
            ' 67\n' +
            '     1\n' +
            '  8\n' +
            '0\n' +
            '  2\n' +
            '$PAPER_SPACE\n' +
            ' 70\n' +
            '     0\n' +
            ' 10\n' +
            '0.0\n' +
            ' 20\n' +
            '0.0\n' +
            ' 30\n' +
            '0.0\n' +
            '  3\n' +
            '$PAPER_SPACE\n' +
            '  1\n' +
            '\n' +
            '  0\n' +
            'ENDBLK\n' +
            '  5\n' +
            'D5\n' +
            ' 67\n' +
            '     1\n' +
            '  8\n' +
            '0\n' +
            '  0\n' +
            'ENDSEC\n' +
            '  0\n' +
            'SECTION\n' +
            '  2\n' +
            'ENTITIES\n' +
            polygons +
            '  0\n' +
            'ENDSEC\n' +
            '  0\n' +
            'EOF\n' +
            '\n' +
            '      \n' +
            '      ';
    }

    decimalToHexadecimal(number) {
        //  discuss at: http://phpjs.org/functions/dechex/
        // original by: Philippe Baumann
        // bugfixed by: Onno Marsman
        // improved by: http://stackoverflow.com/questions/57803/how-to-convert-decimal-to-hex-in-javascript
        //    input by: pilus
        //   example 1: dechex(10);
        //   returns 1: 'a'
        //   example 2: dechex(47);
        //   returns 2: '2f'
        //   example 3: dechex(-1415723993);
        //   returns 3: 'ab9dc427'

        if (number < 0) {
            number = 0xFFFFFFFF + number + 1;
        }

        return parseInt(number, 10).toString(16);
    }

    getLinearRingsFromFeatures(features) {
        console.log(features);

        var rings = [];

        for (var i = 0; i < features.length; i++) {
            var feature = features[i];
            var geometry = feature.getGeometry();

            if (geometry instanceof ol.geom.MultiPolygon) {
                var polygons = geometry.getPolygons();

                for (var j = 0; j < polygons.length; j++) {
                    rings = rings.concat(polygons[j].getCoordinates());
                }

            } else if (geometry instanceof ol.geom.Polygon) {
                rings = rings.concat(geometry.getCoordinates());
            }
        }

        return (rings);
    }

    handleExportToDxf() {
        var mimeType = "image/vnd.dxf";
        var featuresSource = orderGeometryLayer.getSource();
        var bbox = featuresSource.getExtent();
        var rings = this.getLinearRingsFromFeatures(featuresSource.getFeatures());
        _sendTextAsDownload(this.buildDxfTemplate(bbox, rings), mimeType, $("#requestId").val() + ".dxf");
    }

};


/******************* BACKGROUND METHODS *******************/

/**
 * Carries an action on a request based on a button click, if the user confirms it.
 *
 * @param {Integer} requestId          the number that identifies the request to execute the action on
 * @param {String}  label              the string that describes the request to execute the action on
 * @param {Object}  button             the button that triggers the action to execute
 * @param {Object}  confirmationTexts  the object that contains the localized strings to ask the user for a
 *                                      confirmation of the action
 * @param {string}  remark             the string entered by the user to give the customer further information about
 *                                      the operation. Can be <code>null</code> if the action allows it.
 */
function _executeRequestAction(requestId, label, button, confirmationTexts, remark) {

    if (!requestId || isNaN(requestId) || !button || !confirmationTexts) {
        return;
    }

    var alertButtonsTexts = LANG_MESSAGES.generic.alertButtons;
    var message = confirmationTexts.message.replace('\{0\}', label);
    var confirmedCallback = function () {

        if (remark) {
            $('#remark').val(remark);
        }

        $('#actionForm').attr('action', $(button).attr('data-action'));
        $('#actionForm').submit();
    };

    showConfirm(confirmationTexts.title, message, confirmedCallback, null, alertButtonsTexts.yes,
        alertButtonsTexts.no);
}


/**
 * Carries an action on a request output file based on a button click, if the user confirms it.
 *
 * @param {Integer} requestId          the number that identifies the request to execute the action on
 * @param {Object}  button             the button that triggers the action to execute
 * @param {Object}  confirmationTexts  the object that contains the localized strings to ask the user for a
 *                                      confirmation of the action
 * @param {Array}  confirmationValues  the array that contains the values to insert instead of the placeholders in the
 *                                      confirmation message. Can be <code>null</code> if the message contains no
 *                                      placeholder.
 */
function _executeFileAction(requestId, button, confirmationTexts, confirmationValues) {

    if (!requestId || isNaN(requestId) || !button || !confirmationTexts) {
        return;
    }

    var alertButtonsTexts = LANG_MESSAGES.generic.alertButtons;
    var message = confirmationTexts.message;

    if (confirmationValues) {

        for (var valueIndex = 0; valueIndex < confirmationValues.length; valueIndex++) {
            message = message.replace('\{' + valueIndex + '\}', confirmationValues[valueIndex]);
        }
    }

    var confirmedCallback = function () {
        $('#actionForm').attr('action', $(button).attr('data-action'));
        $('#targetFile').val($(button).attr('data-file-path'));
        $('#actionForm').submit();
    };

    showConfirm(confirmationTexts.title, message, confirmedCallback, null, alertButtonsTexts.yes,
        alertButtonsTexts.no);
}


/**
 * Carries the appropriate action after a button click.
 *
 * @param {Object}   button         the button that was clicked to trigger this method
 * @param {Function} actionFunction the function to call to carry the action for the button
 * @param {String}   remarkFieldId  the string that identifies the item that contains the remark to pass to the action
 *                                  function, or <code>null</code> if it does not take a remark
 */
function _handleButtonClick(button, actionFunction, remarkFieldId) {
    var id = parseInt($('#requestId').val());
    var label = $('#requestLabel').val();

    if (!button || isNaN(id) || !label) {
        console.error("ERROR - Could not fetch the necessary info to process a click on "
            + ((button) ? button.id : "an action button"));
        return;
    }

    if (remarkFieldId) {
        actionFunction(id, label, $('#' + remarkFieldId).val(), button);
    } else {
        actionFunction(id, label, button);
    }
}


function getRemarkText(remarkId, remarkType, targetControlId) {
    $.ajax({
        url: getRemarkTextUrl,
        method: 'GET',
        data: {id: remarkId, requestId, remarkType}
    }).done(function (data) {

        if (!data) {
            console.warn("Returned remark text was empty.");
            return;
        }

        var targetControl = document.getElementById(targetControlId);

        if (!targetControl) {
            console.warn(`The target text area ${targetControlId} could not be found.`);
            return;
        }

        $(targetControl).val(data);

    }).fail(function (data, textStatus) {
        console.error('Fetching remark text failed: ' + textStatus);
        return null;
    });
}


/********************* EVENT HANDLERS *********************/

$(function () {
    $('#standbyValidateButton').on('click', function () {
        _handleButtonClick(this, validateRequest, 'standbyValidateRemark');
    });

    $('#standbyCancelButton').on('click', function () {
        _handleButtonClick(this, rejectRequest, 'standbyCancelRemark');
    });

    $('#errorCancelButton').on('click', function () {
        _handleButtonClick(this, rejectRequest, 'errorCancelRemark');
    });

    $('#errorRelaunchButton').on('click', function () {
        _handleButtonClick(this, relaunchProcess);
    });

    $('#standbyRestartButton').on('click', function () {
        _handleButtonClick(this, relaunchProcess);
    });

    $('#errorRestartButton').on('click', function () {
        _handleButtonClick(this, restartCurrentTask);
    });


    $('#errorRetryMatchingButton').on('click', function () {
        _handleButtonClick(this, retryMatching);
    });


    $('#errorRetryExportButton').on('click', function () {
        _handleButtonClick(this, retryExport);
    });

    $('#errorSkipButton').on('click', function () {
        _handleButtonClick(this, skipCurrentTask);
    });

    $('#requestDeleteButton').on('click', function () {
        _handleButtonClick(this, deleteRequest);
    });

    $('.file-delete-button').on('click', function () {
        _handleButtonClick(this, deleteFile);
    });

    $('#file-download-button').on('click', function () {
        window.open($('#file-download-button').attr('data-action'), "_blank");
    });

    $('#file-upload-button').on('click', function () {
        $('#filesToAdd').click();
    });

    $('#filesToAdd').on('change', function () {
        $('#file-upload-button').prop('disabled', true);
        $('#actionForm').attr('enctype', 'multipart/form-data');
        $('#actionForm').attr('action', $('#file-upload-button').attr('data-action'));
        $('#actionForm').submit();
    });

    $('#validationMessagesList').on('change', function () {
        var remarkId = parseInt(this.options[this.selectedIndex].value);
        var remarkText = getRemarkText(remarkId, 'validation', 'standbyValidateRemark');
    });

    $('#rejectionMessagesList').on('change', function () {
        var remarkId = parseInt(this.options[this.selectedIndex].value);
        var remarkText = getRemarkText(remarkId, 'rejection', 'standbyCancelRemark');
    });
});
