#!/bin/bash

awslocal sns create-topic --name test-hxinsight-topic

awslocal s3api create-bucket --bucket "$BUCKET_NAME"
