import ch.asit_asso.extract.plugins.archive.ArchivePlugin;
import ch.asit_asso.extract.plugins.common.ITaskProcessor;

module ch.asit_asso.extract.plugins.archive {
    provides ITaskProcessor
            with ArchivePlugin;

    requires ch.asit_asso.extract.commonInterface;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.lang3;
    requires org.apache.commons.io;
    requires org.slf4j;
    requires ch.qos.logback.classic;
}
