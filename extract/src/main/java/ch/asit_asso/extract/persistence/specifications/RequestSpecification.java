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
package ch.asit_asso.extract.persistence.specifications;

import java.util.Calendar;
import java.util.Collection;
import java.util.Locale;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import ch.asit_asso.extract.domain.Connector;
import ch.asit_asso.extract.domain.Process;
import ch.asit_asso.extract.domain.Request;
import ch.asit_asso.extract.domain.Request_;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.util.StringUtils;



/**
 * A set of criteria to filter requests.
 *
 * @author Yves Grasset
 */
public final class RequestSpecification {

    /**
     * The writer to the application logs.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestSpecification.class);



    /**
     * Creates a new specification instance.
     */
    private RequestSpecification() {
    }



    /**
     * Obtains the criteria to apply to a request to determine whether it must be returned.
     *
     * @param searchText a string that contains the text must be contained in one of the textual fields, or an empty
     *                   string if no textual filter must be applied
     * @param connector  the connector whose requests must be returned, or <code>null</code> to ignore the connector
     *                   requests are associated with
     * @param process    the process whose requests must be returned, or <code>null</code> to ignore the process
     *                   requests are associated with
     * @param startDate  the date from which the request can have been received, or <code>null</code> if there must
     *                   not be a lower limit for the reception date
     * @param endDate    the date until which the request can have been received, or <code>null</code> if there must
     *                   not be an upper limit for the reception date
     * @return the set of criteria to apply the desired filters
     */
    public static Specification<Request> getFilterSpecification(final String searchText,
                                                                final Connector connector, final Process process,
                                                                final Calendar startDate, final Calendar endDate) {

        return Specification.where(RequestSpecification.containsText(searchText)).and(RequestSpecification.isBoundToProcess(process))
                .and(RequestSpecification.isBoundToConnector(connector)).and(RequestSpecification.startsFrom(startDate))
                .and(RequestSpecification.endsUntil(endDate));
    }



    /**
     * Obtains the criteria to apply a textual filter to a request.
     *
     * @param searchText a string that contains the text must be contained in one of the textual fields, or an empty
     *                   string if no textual filter must be applied
     * @return the set of criteria to apply the textual filter
     */
    public static Specification<Request> containsText(final String searchText) {

        return new Specification<Request>() {

            @Override
            public Predicate toPredicate(final Root<Request> root, final CriteriaQuery<?> query,
                    final CriteriaBuilder builder) {

                if (StringUtils.isEmpty(searchText)) {
                    RequestSpecification.LOGGER.debug("The search text is empty. No filtering.");
                    return builder.conjunction();
                }

                final String searchPattern = String.format("%%%s%%", searchText.toLowerCase(Locale.getDefault()));
                RequestSpecification.LOGGER.debug("The text filter pattern is \"{}\"", searchPattern);
                final Predicate customerPredicate = builder.like(builder.lower(root.get(Request_.client)),
                        searchPattern);
                final Predicate thirdPartyPredicate = builder.like(builder.lower(root.get(Request_.tiers)),
                        searchPattern);
                final Predicate orderLabelPredicate = builder.like(builder.lower(root.get(Request_.orderLabel)),
                        searchPattern);
                final Predicate productLabelPredicate = builder.like(builder.lower(root.get(Request_.productLabel)),
                        searchPattern);

                return builder.or(customerPredicate, thirdPartyPredicate, orderLabelPredicate, productLabelPredicate);
            }

        };
    }



    /**
     * Obtains the criteria to add an upper limit to the date a request has been received at.
     *
     * @param endDate the date until which the request can have been received, or <code>null</code> if there must
     *                not be an upper limit for the reception date
     * @return the set of criteria to apply the reception date upper limit
     */
    public static Specification<Request> endsUntil(final Calendar endDate) {

        return new Specification<Request>() {

            @Override
            public Predicate toPredicate(final Root<Request> root, final CriteriaQuery<?> query,
                    final CriteriaBuilder builder) {

                if (endDate == null) {
                    RequestSpecification.LOGGER.debug("No date specified as the end of the filter period.");
                    return builder.conjunction();
                }

                RequestSpecification.LOGGER.debug("Only the requests received before {} will be returned.",
                        endDate.getTime());
                return builder.lessThan(root.get(Request_.startDate), endDate);
            }

        };

    }



    /**
     * Obtains the criteria to filter the request based on the connector it is associated with.
     *
     * @param connector the connector that the request must be associated with
     * @return the set of criteria to apply the connector filter
     */
    public static Specification<Request> isBoundToConnector(final Connector connector) {

        return new Specification<Request>() {

            @Override
            public Predicate toPredicate(final Root<Request> root, final CriteriaQuery<?> query,
                    final CriteriaBuilder builder) {

                if (connector == null) {
                    RequestSpecification.LOGGER.debug("No connector specified for filtering.");
                    return builder.conjunction();
                }

                RequestSpecification.LOGGER.debug("Only the requests associated with the connector \"{}\" will be"
                        + " returned.", connector.getName());
                return builder.equal(root.get(Request_.connector), connector);
            }

        };
    }



    /**
     * Obtains the criteria to filter the request based on the process it is associated with.
     *
     * @param process the process that the request must be associated with
     * @return the set of criteria to apply the process filter
     */
    public static Specification<Request> isBoundToProcess(final Process process) {

        return new Specification<Request>() {

            @Override
            public Predicate toPredicate(final Root<Request> root, final CriteriaQuery<?> query,
                    final CriteriaBuilder builder) {

                if (process == null) {
                    RequestSpecification.LOGGER.debug("No process specified for filtering.");
                    return builder.conjunction();
                }

                RequestSpecification.LOGGER.debug("Only the requests associated with the process \"{}\" will be"
                        + " returned.", process.getName());
                return builder.equal(root.get(Request_.process), process);
            }

        };
    }



    /**
     * Obtains the criteria to filter the request based on the process it is associated with.
     *
     * @param processesList a list that contains the process that the request can be associated with
     * @return the set of criteria to apply the process filter
     */
    public static Specification<Request> isProcessInList(
            final Collection<Process> processesList
    ) {

        return new Specification<Request>() {

            @Override
            public Predicate toPredicate(final Root<Request> root, final CriteriaQuery<?> query,
                    final CriteriaBuilder builder) {

                if (CollectionUtils.isEmpty(processesList)) {
                    return builder.disjunction();
                }

                return root.get(Request_.process).in(processesList);
            }

        };
    }



    /**
     * Obtains the criteria to return only the request whose process has completed.
     *
     * @return the criteria to filter out the unfinished requests
     */
    public static Specification<Request> isFinished() {

        return new Specification<Request>() {

            @Override
            public Predicate toPredicate(final Root<Request> root, final CriteriaQuery<?> query,
                    final CriteriaBuilder builder) {
                return builder.equal(root.get(Request_.status), Request.Status.FINISHED);
            }

        };
    }



    /**
     * Obtains the criteria to add a lower limit to the date a request has been received at.
     *
     * @param startDate the date from which the request can have been received, or <code>null</code> if there must
     *                  not be a lower limit for the reception date
     * @return the set of criteria to apply the reception date lower limit
     */
    public static Specification<Request> startsFrom(final Calendar startDate) {

        return new Specification<Request>() {

            @Override
            public Predicate toPredicate(final Root<Request> root, final CriteriaQuery<?> query,
                    final CriteriaBuilder builder) {

                if (startDate == null) {
                    RequestSpecification.LOGGER.debug("No date specified as the start of the filtering period.");
                    return builder.conjunction();
                }

                RequestSpecification.LOGGER.debug("Only the requests received after or at {} will be returned.",
                        startDate.getTime());
                return builder.greaterThanOrEqualTo(root.get(Request_.startDate), startDate);
            }

        };
    }

}
