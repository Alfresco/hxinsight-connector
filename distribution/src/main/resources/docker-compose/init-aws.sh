#!/bin/bash

awslocal s3api create-bucket --bucket "$BUCKET_NAME"
awslocal sqs create-queue --queue-name "$SQS_QUEUE_NAME"
