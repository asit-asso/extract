import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.qgisprint.QGISPrintPlugin;

module ch.asit_asso.extract.plugins.qgisprint {
    provides ITaskProcessor
            with QGISPrintPlugin;

    requires ch.asit_asso.extract.commonInterface;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires org.apache.httpcomponents.httpmime;
    requires org.json;
    requires org.slf4j;
    requires java.xml;
    requires org.locationtech.jts;
    requires org.geotools.main;
    requires org.geotools.xml;
    requires org.jsoup;

    //requires ch.qos.logback.classic;
}
