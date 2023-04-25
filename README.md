# MedicInfo backend

See: https://gitlab.innovattic.com/innovattic/server-guide

# Deployment

Follow hosting-docs to configure eb (via `eb init`). Afterwards, edit `.elasticbeans/config.yml` and add:

```
deploy:
  artifact: build/dist/medicinfo-eb.zip
```

## SLA work

Besides the [regular SLA work](https://docs.google.com/document/d/1J5-mvKxjs8xHMgm3UCh9rNm0-bQpvHzz0zJCag_nKt0/edit) for backend projects, additionally we need to do regular maintenance on Microsoft on-premises Data Gateway, which is installed on Amazon.

See [docs/powerbi.md](docs/powerbi.md) for more information, in particular the section "Maintaining the Windows machine".

### If you want to see integration test write files to your local computer
In `application-test.properties` add
```properties
medicinfo.files.folder=path/to/your/folder
```
NOTE: on Windows it will be `D:\\path\\to\\your\\folder`

### Note: OData tables
There are sql scripts that create views starting with '*odata_*'. Those views are not used anywhere in Kotlin code beside tests.
Mentioned views are used by MedicInfo PowerBI tool

## Viewing log files

```
EB_ENV=medicinfo-server-acc
eb logs $EB_ENV -g /aws/elasticbeanstalk/$EB_ENV/var/log/web.stdout.log
```

# BI setup

See [docs/powerbi](./docs/powerbi.md).

# Mirro certificates

To run the app it is required to provide RSA256 2048 bit PKCS8 private and public key.
Either use the properties from `application-dev.local.template` or define env variables

```properties
MIRRO_PUBLIC_KEY=<public_key>
MIRRO_PRIVATE_KEY=<private_key>
```

# Triage feature properties

### Choose to require birthdate or age
When starting triage and user birthdate is null in our system it will be requested from salesforce(this is the default behavior).
To disable birthdate requirement use property below, it will make age required to be present instead of birthdate, however there is 
no salesforce call to fetch the age.
```properties
feature.triage.require-birthdate.<label_code>=false

# example:
feature.triage.require-birthdate.CZdirect=false
```

### Salesforce checks on start triage
When users are registering for certain labels, ex. Medicoo(NONI), then they have to choose general practice center they are assigned to. When that general
practice center goes out of business or contract with insurer ends, we have to deny service to those users. We check with Salesforce if we're
still allowed to start a triage for that user.


### If you are on windows and integration tests are failing on `TriageImageService`
In `com.innovattic.medicinfo.logic.TriageImageService#cleanup` swap
```kotlin
val existingFiles = fileService.listFiles(storageKeyPrefixFor(labelCode, triageStatusId) + "/")
```
with
```kotlin
// TODO do not commit
val existingFiles = fileService.listFiles(storageKeyPrefixFor(labelCode, triageStatusId) + "/")
    .map { it.replace("\\", "/") }
```

## Local Database
If you have a local database you will need an admin user to start using the api. 
Use the following sql query to insert an admin.

```
INSERT INTO "user" ("public_id", "role", "name", "email", "created")
VALUES ('D1B1ABFE-D616-4A64-9CF9-50BB1957088B', 'admin', 'admin', 'admin@innovattic.com', CURRENT_TIMESTAMP)
RETURNING id
```

You will get an id number back use that to create an api key in the database with the following query
```
INSERT INTO "api_key" ("user_id", "api_key")
VALUES (<The id number you got from the sql query>, 'c299890c-3be4-4178-a352-60365ff91744');
```

You can then use the api_key: `c299890c-3be4-4178-a352-60365ff91744` 
and the admin uuid key `D1B1ABFE-D616-4A64-9CF9-50BB1957088B` to authenticate
