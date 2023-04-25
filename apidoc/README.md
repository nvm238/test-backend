# MedicInfo API documentation
The main documentation for this project is in [Swagger](https://api.app.dev-medicinfo.nl/api/swagger-ui.html).  
You don't have to worry about converting to `snake_case`:
The swagger-generated JSON structure is accurate, and all properties should be `camelCased`.

## Register customer endpoint
This endpoint requires a custom `Authorization` header, not for security purposes,
but to prevent people from spamming us with new user accounts.  
Its value should be `Digest <hash>` where `<hash>` is `sha256Hex("medicinfo-customer-registration;$labelId;$displayName")`.  
For example, when creating a user with display name `Luke Needham` for the label with id `210f6f05-1784-4638-bbb9-a36a633c5e88`,
 the Authorization header should be `Digest a27941367844695c3438a6cf5ba7f6044d71c2afaceac2fe10a5a3ba1a9f9543`
