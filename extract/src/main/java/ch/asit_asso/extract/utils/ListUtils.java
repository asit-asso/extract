package ch.asit_asso.extract.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ListUtils {

    private final static Logger logger = LoggerFactory.getLogger(ListUtils.class);

    public static <T> List<T> castList(Class<? extends T> clazz, Collection<?> rawCollection) {
        List<T> result = new ArrayList<>(rawCollection.size());

        for (Object object : rawCollection) {

            try {
                result.add(clazz.cast(object));

            } catch (ClassCastException castException) {
                ListUtils.logger.warn(String.format("Element %s could not be cast to %s and will not be added to list.",
                                                    object, clazz.getCanonicalName()), castException);
            }
        }
        return result;
    }
}
