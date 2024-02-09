# Workbench: AWS EventBridge Pipe with Lambda Enrichment to Calculate Signature and call public  API Endpoint

This project is a demonstration on how to use AWS EventBridge Pipes to enrich an event with additional data, calculate
a signature and then call a public API endpoint with original message body as request body and the calculated signature
as HTTP header.

## Architecture

The architecture of the project is as follows:

![architecture.svg](architecture.svg)

- Messages are received by the source queue.
- A Lambda Function will then enrich the incoming message with additional data and calculate a signature. In this example
a simple SHA1 hash is used as signature.
- The body of the original body is then sent to a public API endpoint with the calculated signature as HTTP header.

## How to deploy

The project can be deployed using the AWS CDK. The following steps are required to deploy the project:
```shell
cd infra
cdk -c endpointUrl=https://example.com/my-api deploy
```
