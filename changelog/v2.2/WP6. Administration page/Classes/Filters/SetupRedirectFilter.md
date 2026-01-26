## Description

The `SetupRedirectFilter` is a custom filter that ensures users are redirected to the setup page if the application has not yet been configured. It extends `OncePerRequestFilter`, meaning it will only be executed once per request.

The filter has two main responsibilities:

1. **Allow requests to proceed if certain conditions are met** (the application is configured, the requested resource is whitelisted, or the request is for the `/setup` page).
2. **Redirect to the `/setup` page** if the above conditions are not satisfied.

#### Key Components

- **`AppInitializationService`**: A service that determines whether the application has been configured. It is injected into the filter to provide this information during the filtering process.
    
- **Whitelisted Resources**: The filter bypasses specific static resources (e.g., JavaScript, CSS, image files) defined by the `WHITELISTED_EXTENSIONS` set. These resources are not redirected to the setup page, even if the application is not configured.
    

## Constructor
```java
@Override
public SetupRedirectFilter(AppInitializationService appInitializationService)
```

- **Parameters**:
	- `AppInitializationService appInitializationService`: Injected service to check whether the application setup is complete.

## Methods

### `addInterceptors`

```java
void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
```

- **Purpose**: Processes each request and determines if it should be forwarded or redirected to the `/setup` page.
    
- **Parameters**:
    - `HttpServletRequest request`: The incoming HTTP request.
    - `HttpServletResponse response`: The outgoing HTTP response.
    - `FilterChain filterChain`: The chain of filters in the request processing pipeline.
- **Behavior**:
    - If the application is configured (`appInitializationService.isConfigured()`), or the request is for a whitelisted resource, or the URI starts with `/setup`, the request proceeds normally.
    - Otherwise, the user is redirected to the setup page.

### `requestCanBeForwarded`

```java
String requestCanBeForwarded(HttpServletRequest request)
```

- **Purpose**: Determines if the current request can proceed without redirection.
    
- **Returns**: `true` if the application is configured, the request is for a whitelisted resource, or the request URI starts with `/setup`. Otherwise, `false`.
    
- **Logic**:
    - Uses the `AppInitializationService` to check if the app is already configured.
    - Checks if the request is for a static resource (e.g., `.js`, `.css`).
    - Verifies if the request path starts with `/setup`.

### `getRelativeSetupPath`

```java
String getRelativeSetupPath(HttpServletRequest request)
```

- **Purpose**: Constructs the setup page path based on the request's context path.
    
- **Returns**: The relative URL for the setup page (`{contextPath}/setup`).

### `isWhitelistedResource`

```java
bool isWhitelistedResource(String uri)
```

- **Purpose**: Checks if the requested resource is a whitelisted file type (e.g., `.js`, `.css`, images).
    
- **Returns**: `true` if the request is for a whitelisted resource; otherwise, `false`.

> [!warning]
> The method whitelists the javascript files that are used in the controllers. This is necessary in order to forbid the access to protected resources, since the controller is public

## Usage

This filter should be registered as a component in a Spring Boot application and will automatically redirect users to the setup page if they try to access protected resources before the application setup is complete. It helps ensure that the application can only be used once the initial configuration has been completed.

The filter is inserted in the configuration before the form login filter.
