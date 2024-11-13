package ch.asit_asso.extract.connectors.easysdiv4.utils;

import ch.asit_asso.extract.connectors.easysdiv4.Easysdiv4;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.VersionInfo;

import java.util.Collection;
import java.util.List;

public class UserAgentProvider {

    private final String appVersion;

    private UserAgentProvider(String appVersion)
    {
        this.appVersion = appVersion;
    }

    public static UserAgentProvider withVersion(String version)
    {
        return new UserAgentProvider(version);
    }

    public Collection<? extends Header> getDefaultHeaders() {
        return List.of(new BasicHeader("X-Extract-Version", appVersion));
    }

    public String getUserAgent() {
        String userAgent = VersionInfo.getUserAgent("Apache-HttpClient", "org.apache.http.client", UserAgentProvider.class);

        if (!StringUtils.isBlank(appVersion)) {
            userAgent += " Extract/" + appVersion;
        }

        return userAgent;
    }
}
