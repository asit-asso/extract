package ch.asit_asso.extract.plugins.qgisprint.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class QgisUtils {

    private static final Logger logger = LoggerFactory.getLogger(QgisUtils.class);



    public static String getIdFromGmlIdString(String gmlIdText) {
        QgisUtils.logger.debug("Item id attribute text is \"{}\"", gmlIdText);
        int lastDotIndex = gmlIdText.lastIndexOf(".");
        QgisUtils.logger.debug("Last dot is at index {}", lastDotIndex);

        if (lastDotIndex == gmlIdText.length() - 1) {
            QgisUtils.logger.debug("The dot is the last character of the string. Returning an empty string");
            return "";
        }

        return gmlIdText.substring(lastDotIndex + 1);
    }

}
