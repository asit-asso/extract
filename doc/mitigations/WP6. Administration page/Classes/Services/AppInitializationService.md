## Description

Class `AppInitializationService` is a central component of the new functionality that checks whether an admin user exists or not.

## Attributes

- **`repository`** : Repository used to query that database of users.    

## Constructor

- **`AppInitializationService(UsersRepository repository)`** : Constructor method that takes the user repository as an argument

## Methods

### `isConfigured`

```java
public boolean isConfigured()
```

- **Description** : This method returns `true`if an administrator exists in the database off users. It seeks records by Profile `existsByProfile`. la requête.
- Returns `true`if at least an administrator exits
