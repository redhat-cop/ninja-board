# Quarkus Migration
## Goals
* Enhance by adopting a MicroService Architecture
* Allow single bundled deployment
* Enhance performance by using Red Hat Technology (Quarkus)
* Prioritising simplicity and maintainability

## Proposed implementation
Multi-Project gradle build where each sub-project ending with "-service" (except all-service) represents one of the
microservice. The sub-project "all-service" is combining all the micro-services into a single Quarkus deployment.

This project also includes a Maven Build to remove barrier to entry. After weighting pros and cons, one of the build
tool will be cut

## Create Environment variables before running
### Linux
```shell script
export USERS_LDAP_PROVIDER=i-wont-tell-you-the-real-value
export USERS_LDAP_BASEDN=i-really-wont-tell-you-the-real-value
./gradlew :all-service:build
java -jar all-service/build/all-service-2.0.0-SNAPSHOT-runner.jar
```

### Windows (PowerShell)
```shell script
[System.Environment]::SetEnvironmentVariable('USERS_LDAP_PROVIDER','i-wont-tell-you-the-real-value',
[System.EnvironmentVariableTarget]::User) # Need to set only once in user scope
[System.Environment]::SetEnvironmentVariable('USERS_LDAP_BASEDN','i-really-wont-tell-you-the-real-value',
[System.EnvironmentVariableTarget]::User) # Need to set only once in user scope
```

## Building and running
### Gradle
```shell script
./gradlew :all-service:build
java -jar all-service/build/all-service-2.0.0-SNAPSHOT-runner.jar
```

### Maven
```shell script
mvn install -f all-service
java -jar all-service/target/all-service-2.0.0-SNAPSHOT-runner.jar
```

## Testing
### Linux
```shell script
curl localhost:8080/user/some-user-id-that-to-exist
curl localhost:8080/user/some-user-id-that-dont-exist
curl localhost:8080/user/mail/some-mail-that-exist
```

### Windows
```shell script
Invoke-RestMethod -Uri http://localhost:8080/user/some-user-id-that-to-exist
Invoke-RestMethod -Uri http://localhost:8080/user/some-user-id-that-dont-exist
Invoke-RestMethod -Uri http://localhost:8080/user/mail/some-mail-that-exist
```

Or 
```shell script
curl http://localhost:8080/user/some-user-id-that-to-exist
curl http://localhost:8080/user/some-user-id-that-dont-exist
curl http://localhost:8080/user/mail/some-mail-that-exist
```