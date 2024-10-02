## Context

In the `sendImportRequest` function of the `Easysdiv4` class, an uncontrolled error may occur if the function takes too long to execute or the resource is not available. The aim of this work package is therefore to add a default timeout to the Http client making the requests.

## How it works

Three steps are required to implement the function:
- Define the timeout value in the config file
- Define a function to extract the value
- Configure the `HttpGet` object

## Configuration file

In the configuration file, we have the following line:
```properties
getOrders.timeoutInMilliseconds=5000
```

The default timeout is 5s.

## Implementation

To extract the value from the configuration file and make it usable, the `TimeoutUtils` utility class approach was chosen. This allows the logic to be separated and tested more easily.

```java
public static int parseTimeout(final String timeoutStr)  
{  
    int timeoutInMilliseconds;  
  
    try {  
        if (timeoutStr == null) {  
            LOGGER.warn("Timeout configuration key is missing, using default timeout of {} ms.", DEFAULT_TIMEOUT_MILLIS);  
            timeoutInMilliseconds = DEFAULT_TIMEOUT_MILLIS;  
        } else {  
            timeoutInMilliseconds = Integer.parseInt(timeoutStr);  
  
            if (timeoutInMilliseconds <= MIN_TIMEOUT_MILLIS || timeoutInMilliseconds > MAX_TIMEOUT_MILLIS) {  
                LOGGER.warn("Timeout value {} ms is out of bounds, using default timeout of {} ms.", timeoutInMilliseconds, DEFAULT_TIMEOUT_MILLIS);  
                timeoutInMilliseconds = DEFAULT_TIMEOUT_MILLIS;  
            }  
        }  
    } catch (Exception e) {  
        LOGGER.warn("Failed to parse timeout value, using default using default timeout of {} ms.", DEFAULT_TIMEOUT_MILLIS, e);  
        timeoutInMilliseconds = DEFAULT_TIMEOUT_MILLIS;  
    }  
  
    return timeoutInMilliseconds;  
}
```

The function tests all edge conditions, before returning a parsed value or the default value in the event of an exception.

### Configuring the `HttpGet` object

The `createGetRequest` method has been modified to receive a timeout configuration:

```java
private HttpGet createGetRequest(final URI url) {  
    assert url != null : "The target url cannot be null.";  
  
    this.logger.debug("Creating HTTP GET request for URL {}.", url);  
  
    HttpGet request = new HttpGet(url);  
    request.setConfig(createRequestConfigWithTimeout());  
  
    return (HttpGet) this.addProxyInfoToRequest(request);  
}
```
The timeout is configured in the `createRequestConfigWithTimeout` method:

```java
protected RequestConfig createRequestConfigWithTimeout()  
{  
    String timeoutStr = config.getProperty("getOrders.timeoutInMilliseconds");  
    int timeoutInMilliseconds = TimeoutUtils.parseTimeout(timeoutStr);  
  
    return RequestConfig.custom()  
            .setConnectTimeout(timeoutInMilliseconds)  
            .setConnectionRequestTimeout(timeoutInMilliseconds)  
            .setSocketTimeout(timeoutInMilliseconds)  
            .build();  
}
```

## Unit tests

Unit tests were run on the `TimeoutUtils` and `Easydiv4` classes to check that all edge conditions were triggered.