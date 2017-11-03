#!/bin/bash

#set -e
#set -x

CLI="wsk"

echo "   Attention! Make sure that XML Provider process is already running!"
echo "   If not, run './run_prov.sh' script, first"
read -p "Press enter to continue..."

echo -e "\n============ Creating shared package for actions and feeds ======================\n"
$CLI package update lambda_demo --shared yes -p dbHost "192.168.33.13" -p dbPort 5984 -p dbName feed_files -p dbLogin lambda_demo -p dbPassword "~123456" -a description "OpenWhisk Demo Showing How To Implement Lambdas" -a parameters '[{"name":"dbHost","required":"false", "description":"CouchDB host name or IP address"}, {"name":"dbPort","required":"false", "description":"Port number which is listened to by CouchDB"}, {"name":"dbName","required":"false", "description":"Database inside the CouchDB where processed XML are stored"}, {"name":"dbLogin","required":"false", "description":"Login to access the database"}, {"name":"dbPassword","required":"false", "description":"Password to access the database"}]'
 
echo -e "\n============ Creating Java actions ==============================================\n"
$CLI action update lambda_demo/validate_xml xml2json.jar  --main test.lambda.openwhisk.actions.LambdaValidateXmlOpenWhisk --web true -p xmlSchemaFile /home/vagrant/demos/DJ/exchange/Build/dist/test.xsd
$CLI action update lambda_demo/xml2json xml2json.jar  --main test.lambda.openwhisk.actions.LambdaXml2JsonOpenWhisk --web true
 
echo -e "\n============ Create Java action sequence ========================================\n"
$CLI action create lambda_demo/process_xml --sequence lambda_demo/validate_xml,lambda_demo/xml2json

echo -e "\n============ Registering feed action ============================================\n"
$CLI action update lambda_demo/xml_feed xml2json.jar  --main test.lambda.openwhisk.feeds.LambdaXmlFeedOpenWhisk -p xmlProvHost "127.0.0.1" -p xmlProvPort 8080 -p triggerHost "192.168.33.13"  -a feed true -a description "Feed Action Controlling XML Provider's Live Cycle" -a parameters '[{"name":"xmlProvHost","required":"false", "description":"XML feed provider host name or IP address"}, {"name":"xmlProvPort","required":"false", "description":"Port number which is listened to by XML feed provider"}, {"name":"triggerHost","required":"false", "description":"OpenWhisk environment where the trigger is hosted"}]'
 
echo -e "\n============ Creating trigger ===================================================\n"
$CLI trigger create new_xml_file_trigger --feed  lambda_demo/xml_feed -p xmlProvHost 192.168.33.13
 
echo -e "\n============ Creating rule =======================================================\n"
$CLI rule rule create new_xml_file_ruler new_xml_file_trigger lambda_demo/process_xml

echo -e "\n============ Check result =======================================================\n"
$CLI list
