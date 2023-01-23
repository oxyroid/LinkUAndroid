## Custom your own local.properties

1. Rebuild this project if you cannot find there is no `local.properties` in the root dir.
2. Open [`local.properties`](../local.properties) file, add some properties:

```properties
base_url=https://api.test.com/
ws_url=https://ws.test.com
```
> The ws_url shouldn't end with /

3. Replace values with your own API links, you can visit these files to design your backend.
   1.  [HTTP_API files](../domain/src/main/java/com/linku/domain/service/api) => "base_url".
   2.  [WebSocket service file](../domain/src/main/java/com/linku/domain/service/api/SessionService.kt) => "ws_url".
