package ch.asit_asso.extract.connectors.easysdiv4;

public class ApplicationInfo {
    private final String applicationVersion;
    private static final String CONFIG_FILE_PATH = "connectors/easysdiv4/properties/config.properties";
    private ConnectorConfig config;

    public ApplicationInfo() {
        this.config = new ConnectorConfig(CONFIG_FILE_PATH);

        this.applicationVersion = this.config.getProperty("application.version");
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }
}
