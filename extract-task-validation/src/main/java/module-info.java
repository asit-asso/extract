import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.validation.ValidationPlugin;

module ch.asit_asso.extract.plugins.validation {
    provides ITaskProcessor
            with ValidationPlugin;

    requires ch.asit_asso.extract.commonInterface;

    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires com.fasterxml.jackson.databind;
    //requires ch.qos.logback.classic;
}
