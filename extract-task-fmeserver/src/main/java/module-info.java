import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.fmeserver.FmeServerPlugin;

module ch.asit_asso.extract.plugins.fmeserver {
    provides ITaskProcessor
            with FmeServerPlugin;

    requires ch.asit_asso.extract.commonInterface;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires org.json;
    requires org.slf4j;
    //requires ch.qos.logback.classic;
}
