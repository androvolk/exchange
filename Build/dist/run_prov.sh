#!/bin/bash

set -e
set -x

java -classpath ./xml2json.jar test.lambda.openwhisk.providers.xml.XmlProvider
