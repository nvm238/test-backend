# Stacks

See [Innovattic hosting docs](https://gitlab.innovattic.com/innovattic/hosting-docs) for basic instructions.

Use `create-change-set` to verify your changes.

## Elasticbeans app

```
aws cloudformation update-stack \
  --stack-name medicinfo-server-app \
  --template-body file://cf/app.yaml
```

## DB

```
aws cloudformation update-stack \
  --stack-name medicinfo-db-<env-name> \
  --template-body file://db-<env-name>.yaml \
  --parameters ParameterKey=Password,ParameterValue=<rds-password>
```


## Elasticbeans env

```
aws cloudformation update-stack \ 
  --stack-name medicinfo-server-<env-name> \ 
  --template-body file://cf/env.yaml \ 
  --parameters file://cf/<env-name>.json \
  --capabilities CAPABILITY_NAMED_IAM
```

## S3

```
aws cloudformation update-stack \
  --stack-name medicinfo-s3-<env-name> \
  --template-body file://s3.yaml \
  --parameters ParameterKey=EnvironmentName,ParameterValue=<env-name>
```
