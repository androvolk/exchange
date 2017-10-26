#!/bin/bash

#set -e
set -x

CLI="wsk"

echo "   Attention! You are going to delete XML Provider Feed Infrastructure!"
read -p "Press Ctrl-C to cancel or enter to continue..."


echo -e "\n============  Deleting rule ======================================================\n"
$CLI rule delete /guest/new_xml_file_ruler
 
echo -e "\n============  Deleting trigger ===================================================\n"
$CLI trigger delete /guest/new_xml_file_trigger -p xmlProvHost 192.168.33.13
 
echo -e "\n============  Deleting feed action ===============================================\n"
$CLI action delete /guest/lambda_demo/xml_feed
 
echo -e "\n============  Deleting Java actions ==============================================\n"
$CLI action delete /guest/lambda_demo/xml2json
 
echo -e "\n============  Deleting shared package for feeds and actions ======================\n"
$CLI package delete /guest/lambda_demo

echo -e "\n============  Check result =======================================================\n"
$CLI list
