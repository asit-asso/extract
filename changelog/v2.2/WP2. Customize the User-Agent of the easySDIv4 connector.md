## Context

In its current state, the Easysdiv4 connector doesn't communicate its version. The present workpackage intends to implement this behaviour.  

## How it works
There are three steps involved in the process:
- Enable filtering in the `pom.xml` file
- Add a configuration value in the `properties` file
- Update the `getHttpClient` method to add the user agent
## Configuration file
In the configuration file, we have the following line:
```properties
app.version=@project.version@
```

The value `@project.version@` is replaced by the contents of `<version>` in the `pom.xml` file, but requires that the resource filtering is enabled:
```xml
<resources>  
    <resource>        
	    <directory>src/main/resources</directory>  
        <filtering>true</filtering>  
    </resource>
</resources>
```
## Implementation
The code of the `Easysdiv4` class was modified as shown below:
```diff
private CloseableHttpClient getHttpClient(final HttpHost targetHost, final String targetLogin,  
        final String targetPassword) {  
    assert targetHost != null : "The target host cannot be null";  
    final CredentialsProvider credentials = this.getCredentialsProvider(targetHost, targetLogin, targetPassword);  
+    final UserAgentProvider provider =  UserAgentProvider.withVersion(config.getProperty("app.version"));  
    return HttpClients.custom()  
+            .setDefaultCredentialsProvider(credentials)  
+            .setDefaultHeaders(provider.getDefaultHeaders())  
            .setUserAgent(provider.getUserAgent())  
            .build();  
}
```

This snippet contains two additional pieces of information:
1. The user agent 
2. The custom `X-Extract-Version` header

Both lines are included in every request:
```
16:36:31.406 32072 [pool-3-thread-1] DEBUG org.apache.http.wire - http-outgoing-0 >> "X-Extract-Version: 2.1.3-RELEASE[\r][\n]"
16:36:31.531 32197 [pool-3-thread-1] DEBUG org.apache.http.wire - http-outgoing-0 >> "User-Agent: Apache-HttpClient/4.5.14 (Java/17.0.11) Extract/2.1.3-RELEASE[\r][\n]"
```

## Classes
### `UserAgentProvider`

#### Description
In order to get the user agent value and the custom header, we've created a new class `UserAgentProvider` that externalizes this behaviour. This allows easily testing it.
#### Constructor
The class `UserAgentProvider` takes the Extract version as a parameter and keeps it as it is.
#### Methods
##### `withVersion`
Factory method that allows creating the class given the Extract version:
```java
public static UserAgentProvider withVersion(String version)
```

**Arguments**:
- **`version`**: The version of the Extract platform 

## Unit tests

Unit tests are executed in the `UserAgentProviderTest`file. This class tests that the User-Agent matches the structure: `<Apache-HttpClient Version> Extract/<AppVersion>` and that the `X-Extract-Version` header matches the correct version number.
