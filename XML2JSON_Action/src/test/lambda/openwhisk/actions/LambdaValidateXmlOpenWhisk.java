package test.lambda.openwhisk.actions;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import test.lambda.utils.Convert;
import test.lambda.utils.CouchDB;
import test.lambda.utils.MissedParams;
import test.lambda.utils.Result;
import test.lambda.utils.XmlValidator;

public class LambdaValidateXmlOpenWhisk
{
  private static final Logger log = LoggerFactory.getLogger ( LambdaValidateXmlOpenWhisk.class );

  public static JsonObject main ( JsonObject args )
  {
    String id = null;
    String rev = null;
    String dbHost = null;
    int dbPort = 0;  // Quick fix to shut up the compiler :)
    String dbName = null;
    String dbLogin = null;
    String dbPassword = null;
    String xml = null;
    String xmlSchemaFile = null;
    
    JsonObject response = null;
    JsonObject result = null;

    MissedParams missedParams = new MissedParams (); 

    log.info ( "XML validation started" );
    log.debug ( "args = " + args );
    
    if ( args .has ( "id" )) id = args .get ( "id" ) .getAsString ();
    else missedParams.add ( "id" );
    if ( args .has ( "rev" )) rev = args .get ( "rev" ) .getAsString ();
    else missedParams.add ( "rev" );

    if ( args.has ( "dbHost" ) ) dbHost = args .get ( "dbHost" ) .getAsString ();
    else missedParams.add ( "dbHost" );
    if ( args.has ( "dbPort" ) ) dbPort = args .get ( "dbPort" ) .getAsInt ();
    else missedParams.add ( "dbPort" );
    if ( args.has ( "dbName" ) ) dbName = args .get ( "dbName" ) .getAsString ();
    else missedParams.add ( "dbName" );
    if ( args.has ( "dbLogin" ) )  dbLogin = args .get ( "dbLogin" ) .getAsString ();
    else missedParams.add ( "dbLogin" );
    if ( args.has ( "dbPassword" ) )  dbPassword = args .get ( "dbPassword" ) .getAsString ();
    else missedParams.add ( "dbPassword" );

    if ( args.has ( "xmlSchemaFile" ) ) xmlSchemaFile = args .get ( "xmlSchemaFile" ) .getAsString ();
    else missedParams.add ( "xmlSchemaFile" );
    
    
    if ( missedParams.hasMissedParameters () )
    {
      String msg = "LambdaValidateXmlOpenWhisk - some parameters are missing: ( " + missedParams + " )"; 
      log.info ( "XML validation failed" );
      log.error ( "args = " + args );
      return  Result.failure ( msg );
    }
    
    log.debug ( String.format ( "LambdaValidateXmlOpenWhisk parameters: { id = %s, rev = %s, dbHost = %s, " + 
                                "dbPort = %d, dbName = %s, dbLogin = %s, dbPassword = %s, xmlSchemaFile = %s }",
                                id, rev, dbHost, dbPort, dbName, dbLogin, dbPassword, xmlSchemaFile ) );

    // Getting XML from CouchDB by it's id and revision
    try
    {
      response = CouchDB.downloadXmlAttachment ( dbHost, dbPort, dbName, dbLogin, dbPassword, id, rev, "XML" );
      log.debug ( "response = " + response );
    }
    catch ( NoSuchAlgorithmException | IOException e )
    {
      String msg = "CouchDB::downloadXmlAttachment generated exception! Reason: " + e.getMessage ();
      log.info ( "XML validation failed" );
      log.error ( msg );
      return Result.failure ( msg );
    }

    if ( Result.isFailed ( response ) )
    {
      String msg = "CouchDB::downloadXmlAttachment failed to download XML attachment! Reason: " + 
                                                    Convert.jsonObjectToJson ( Result .getValue ( response ) ); 
      log.info ( "XML validation failed" );
      log.error ( msg );
      return Result.failure ( msg );
    }

    // Getting XML out of reply
    xml = Result.getValue ( response ) .get ( "value" ) .getAsString ();
    log.debug ( "xml = " + xml );
    
    // XML should not be null
    if ( xml == null )
    {
      String msg = "CouchDB::downloadXmlAttachment returned response with null as value!"; 
      log.info ( "XML validation failed" );
      log.error ( msg );
      return Result.failure ( msg );
    }

    // Time to validate the XML
    if ( ! XmlValidator.isXmlValid ( xml, xmlSchemaFile ) )
    {
      String msg = "XmlValidator::isXmlValid failed to validate XML!"; 
      log.info ( "XML validation failed" );
      log.error ( msg );
      return Result.failure ( msg );
    }
    
    log.info ( "XML validation was successful" );
    return Convert.jsonToJsonObject ( "{\"id\":\"" + id + ",\"rev\":\"" + rev + "\"}" );
  }

}
