#!/bin/bash

awslocal sns create-topic --name test-hxinsight-topic
awslocal sqs create-queue --queue-name test-hxinsight-topic-consumer
awslocal sns subscribe --topic-arn arn:aws:sns:us-east-1:000000000000:test-hxinsight-topic --protocol sqs --notification-endpoint arn:aws:sqs:us-east-1:000000000000:test-hxinsight-topic-consumer
