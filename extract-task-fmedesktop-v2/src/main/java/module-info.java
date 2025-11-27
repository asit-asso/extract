import ch.asit_asso.extract.plugins.common.ITaskProcessor;
import ch.asit_asso.extract.plugins.fmedesktopv2.FmeDesktopV2Plugin;

module ch.asit_asso.extract.plugins.fmedesktopv2 {
    provides ITaskProcessor
            with FmeDesktopV2Plugin;

    requires ch.asit_asso.extract.commonInterface;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.apache.commons.io;
    requires org.apache.commons.lang3;
    requires org.slf4j;
    requires org.locationtech.jts;
    requires org.locationtech.jts.io;
    //requires ch.qos.logback.classic;
}
