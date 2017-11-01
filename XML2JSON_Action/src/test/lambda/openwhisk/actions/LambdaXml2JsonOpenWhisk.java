package test.lambda.openwhisk.actions;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import com.google.gson.JsonObject;

import test.lambda.utils.Convert;
import test.lambda.utils.CouchDB;
import test.lambda.utils.MissedParams;
import test.lambda.utils.Result;
import test.lambda.utils.Xml2Json;

public final class LambdaXml2JsonOpenWhisk
{

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
    String json = null;

    JsonObject response = null;

    MissedParams missedParams = new MissedParams (); 

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
    
    if ( missedParams.hasMissedParameters () )
         return  Result.failure ( "LambdaXml2JsonOpenWhisk - some parameters are missing: ( " + missedParams + " )" );

System.out.println ( "Xml2Json id -> " + id);//!!
System.out.println ( "Xml2Json rev -> " + rev);

    // Getting XML from CouchDB by it's id and revision
    try
    {
      response = CouchDB.downloadXmlAttachment ( dbHost, dbPort, dbName, dbLogin, dbPassword, id, rev, "XML" );
    }
    catch ( NoSuchAlgorithmException | IOException e )
    {
      String msg = "CouchDB::downloadXmlAttachment generated exception! Reason: " + e.getMessage ();
      System.err.println ( msg );
      return Result.failure ( msg );
    }

    if ( Result.isFailed ( response ) )
    {
      String msg = "CouchDB::downloadXmlAttachment failed to download XML attachment! Reason: " + 
                                                    Convert.jsonObjectToJson ( Result .getValue ( response ) ); 
      System.err.println ( msg );
      return Result.failure ( msg );
    }

    // Getting XML out of reply
    xml = Result.getValue ( response ) .get ( "value" ) .getAsString ();
//    xml = Convert.jsonObjectToJson ( Result.getValue ( response ) );
System.out.println ( "Downloaded XML -> " + xml ); //!!!   
    
    // XML should not be null
    if ( xml == null )
    {
      String msg = "CouchDB::downloadXmlAttachment returned response with null as value!"; 
      System.err.println ( msg );
      return Result.failure ( msg );
    }

    // Passing the xml to the global converter
    json = Xml2Json.convertXml2Json ( xml );
System.out.println ( "Converted JSON -> " + json ); //!!!   

    // JSON should not be null if converter did his job well
    if ( json == null )
    {
      String msg = "Xml2Json::convertXml2Json failed to convert XML into JSON and returned null!"; 
      System.err.println ( msg );
      return Result.failure ( msg );
    }
    
    // Store the JSON as attachment to the same document
    try
    {
      response = CouchDB.uploadJsonAttachment ( dbHost, dbPort, dbName, dbLogin, dbPassword, id, rev, "JSON", json );
    }
    catch ( NoSuchAlgorithmException | IOException e )
    {
      String msg = "CouchDB::uploadJsonAttachment generated exception! Reason: " + e.getMessage ();
      System.err.println ( msg );
      return Result.failure ( msg );
    }
    
    
    // Forming resulting JSON object with 'result' element having JSON-ified XML as its value 
    if ( Result.isFailed ( response ) )
    {
      String msg = "CouchDB::uploadJsonAttachment failed to upload JSON attachment! Reason: " + 
                                                    Convert.jsonObjectToJson ( Result .getValue ( response ) ); 
      System.err.println ( msg );
      return Result.failure ( msg );
    }
    else rev = Result.getValue ( response ) .get ( "rev" ) .getAsString ();

System.out.println ("_id -> " + id  ); //!!!
System.out.println ("_rev -> " + rev  ); //!!!

    return  response;
  }

//  public static void main ( String [] args )
//  {
//    JsonObject in = new JsonObject ();
//    in. addProperty ( "xml", "<MyXml><String sarg = \"SARG\">Vava</String><ArrayOfInts><Int>123</Int><Int>456</Int><Int>789</Int></ArrayOfInts></MyXml>" );
//    
//    JsonObject out = LambdaXml2JsonOpenWhisk.main ( in );
//    System.out.println ( out );
//  }

}
