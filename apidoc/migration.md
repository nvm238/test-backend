# Migration

We need to migrate data from Infosupport (old developers for Medicinfo) to our new infrastructure.

Mobile app flow:

- New app update is installed
- When initially started, it uses the `POST /v1/migrate` endpoint. This takes two uuids that only the app can know,
  which sort-of authenticates the user. As a response, the server creates an access key for that user.
- The app can now use the new backend with the API key.
