import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.reject.RejectPlugin;

module ch.asit_asso.extract.plugins.reject {
    provides ITaskProcessor
            with RejectPlugin;

    requires ch.asit_asso.extract.commonInterface;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires org.slf4j;
    //requires ch.qos.logback.classic;
}
