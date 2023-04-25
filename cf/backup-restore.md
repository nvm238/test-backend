## Steps

- Go to AWS Backup, select the last RDS backup and restore to new instance. Use same properties as current production version (properties can also be found in `cf/db-prod.yaml` in the source code)
- Go to AWS Backup, select the last S3 backup and restore to new bucket. Use same properties as current production version.

- Create `backup-restore-server-app` stack to create the ElasticBeanstalk application

```
aws cloudformation create-stack \
  --stack-name backup-restore-server-app \
  --template-body file://cf/backup-restore-app.yaml
```


- Create `medicinfo-server-backuprestore` stack to create the EC2 instance
	- Create a new certificate for `api-backup-restore.app.acc-medicinfo.nl` if it doesn't exist yet
	- Update certificate in cf/backuprestore.json
	
```
aws cloudformation create-stack \ 
  --stack-name medicinfo-server-backuprestore \ 
  --template-body file://cf/env.yaml \ 
  --parameters file://cf/backuprestore.json \
  --capabilities CAPABILITY_NAMED_IAM
```

- Update the Configuration for `medicinfo-server-backuprestore` in Elastic Beanstalk. Set the following values:
	- CALENDLY_API_KEY (LastPass)
	- CALENDLY_SIGNATURE (LastPass)
	- MIRRO_PRIVATE_KEY (LastPass)
	- MIRRO_PUBLIC_KEY (LastPass)
	- RDS_PASSWORD (LastPass, current password on production if restored from production backup)
	- SALESFORCE_API_KEY (LastPass)
- Deploy the backend to the new environment. *Please note: The following changes are done in the production properties file and shouldn't be committed. Make sure to deploy to the backup-restore server and not to production.*
	- In `application-prod.properties` update the `spring.datasource.url` property to the newly created database url
	- In the same file, update the s3 bucket name to the newly created bucket
	- Build and deploy using `./gradlew clean eb` && `eb deploy backup-restore-server-backuprestore`


## Checks

### Database

- Check if the database can be opened with an external client. Verify that the oldest _user_ is from 2015-08-17 12:18:09. Also verify that the newest _message_ is just a few minutes before the creation date of the backup.
- Check if the amount of messages in the backup-restore database = the amount of messages in the production database minus the amount of messages that are created after the last message of the backup-restore database

Execute on production:
```
SELECT COUNT(*) as total
FROM message
WHERE created > '2022-07-05 08:00:00'; -- This should be the time of the last message backed up
```

On both production and backup restore run:
```
SELECT COUNT(*)
FROM message
```

Then subtract the total count on production with the total count on backup-restore. The result should be the same as the first query.

- Manually SELECT the 10 newest and oldest messages and confirm that they are human-readable.

```
SELECT *
FROM message
ORDER BY created
LIMIT 10
```

SELECT *
FROM message
ORDER BY created desc
LIMIT 10

### Server

- Check if labels and users can be fetched through the corresponding REST endpoints on the deployed backup-restore server
- Get a random message id from the `message_attachment` table and fetch the image from the backup restore server (which gets the image from the restored S3 bucket)

```
SELECT ma.public_id as "Message attachment ID", c.public_id as "Conversation ID"
FROM message_attachment ma JOIN message m ON ma.message_id = m.id JOIN conversation c on m.conversation_id = c.id
ORDER BY ma.created desc
LIMIT 100
```