# GivebackNinja.DefaultApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createUser**](DefaultApi.md#createUser) | **POST** /user | Registers a new user in the system.
[**levelGet**](DefaultApi.md#levelGet) | **GET** /level | Obtain all available Levels
[**scorecardGet**](DefaultApi.md#scorecardGet) | **GET** /scorecard | Get all user scorecards.
[**scorecardUsernameGet**](DefaultApi.md#scorecardUsernameGet) | **GET** /scorecard/{username} | Get UserScorecard info for a specific user.
[**scorecardUsernamePoolPost**](DefaultApi.md#scorecardUsernamePoolPost) | **POST** /scorecard/{username}/{pool} | 



## createUser

> User createUser(user)

Registers a new user in the system.

### Example

```javascript
import GivebackNinja from 'giveback_ninja';

let apiInstance = new GivebackNinja.DefaultApi();
let user = {"username":"new_ninja","displayName":"New Ninja","githubUsername":"new_ninja"}; // User | 
apiInstance.createUser(user, (error, data, response) => {
  if (error) {
    console.error(error);
  } else {
    console.log('API called successfully. Returned data: ' + data);
  }
});
```

### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **user** | [**User**](User.md)|  | 

### Return type

[**User**](User.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: application/json
- **Accept**: application/json


## levelGet

> [Level] levelGet()

Obtain all available Levels

### Example

```javascript
import GivebackNinja from 'giveback_ninja';

let apiInstance = new GivebackNinja.DefaultApi();
apiInstance.levelGet((error, data, response) => {
  if (error) {
    console.error(error);
  } else {
    console.log('API called successfully. Returned data: ' + data);
  }
});
```

### Parameters

This endpoint does not need any parameter.

### Return type

[**[Level]**](Level.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


## scorecardGet

> [Scorecard] scorecardGet()

Get all user scorecards.

### Example

```javascript
import GivebackNinja from 'giveback_ninja';

let apiInstance = new GivebackNinja.DefaultApi();
apiInstance.scorecardGet((error, data, response) => {
  if (error) {
    console.error(error);
  } else {
    console.log('API called successfully. Returned data: ' + data);
  }
});
```

### Parameters

This endpoint does not need any parameter.

### Return type

[**[Scorecard]**](Scorecard.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


## scorecardUsernameGet

> Scorecard scorecardUsernameGet(username)

Get UserScorecard info for a specific user.

### Example

```javascript
import GivebackNinja from 'giveback_ninja';

let apiInstance = new GivebackNinja.DefaultApi();
let username = "username_example"; // String | The unique name of a specific user.
apiInstance.scorecardUsernameGet(username, (error, data, response) => {
  if (error) {
    console.error(error);
  } else {
    console.log('API called successfully. Returned data: ' + data);
  }
});
```

### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **username** | **String**| The unique name of a specific user. | 

### Return type

[**Scorecard**](Scorecard.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json


## scorecardUsernamePoolPost

> Scorecard scorecardUsernamePoolPost(username, pool, body)



### Example

```javascript
import GivebackNinja from 'giveback_ninja';

let apiInstance = new GivebackNinja.DefaultApi();
let username = "username_example"; // String | The unique name of the user.
let pool = "pool_example"; // String | The pool to increment. The pool will be created if not already defined.
let body = 56; // Number | 
apiInstance.scorecardUsernamePoolPost(username, pool, body, (error, data, response) => {
  if (error) {
    console.error(error);
  } else {
    console.log('API called successfully. Returned data: ' + data);
  }
});
```

### Parameters


Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **username** | **String**| The unique name of the user. | 
 **pool** | **String**| The pool to increment. The pool will be created if not already defined. | 
 **body** | **Number**|  | 

### Return type

[**Scorecard**](Scorecard.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: text/plain
- **Accept**: application/json

