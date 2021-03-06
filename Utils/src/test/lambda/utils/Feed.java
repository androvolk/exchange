package test.lambda.utils;

import java.io.IOException;
//import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.google.gson.Gson;
import com.google.gson.JsonObject;


public final class Feed
{
  private static final Logger log = LoggerFactory.getLogger ( Feed.class );

//  private static final String STATUS_OK = "{\n\t\"status\": \"success\"\n\t\"success\": \"true\"\n}";
  private static String xmlProvHost = null; //"127.0.0.1";
//  private static String xmlProvPort = null; //"8080";
  private static int xmlProvPort; //"8080";
//  private static String dbHost = "127.0.0.1";
//  private static int dbPort = 5984;
//  private static String dbName = "feed_files";
//  private static String dbLogin = "lambda_demo";
//  private static String dbPassword = "123456";

  
  /**
   * Creates the feed.
   * @param args - JsonObject containing all required parameters
   * @return Result of operation as JsonObject with status, message, etc. 
   * @throws IOException
   */
  public static JsonObject create ( final JsonObject args ) throws IOException
  {
//    Gson gson = null;
    JsonObject response = null;
//    String response = null;
    String jsonDocument = null;
//    JsonObject result = null;
//    @SuppressWarnings ( "rawtypes" )
//    Map entries = null;
    MissedParams missedParams = new MissedParams ();
 
    log.info ( "Creating feed" );

    // Obtain parameters from environment
    if ( args.has ( "xmlProvHost" ) )  xmlProvHost = args. get ( "xmlProvHost" ) .getAsString ();
    else missedParams.add ( "xmlProvHost" );
//    if ( args.has ( "xmlProvPort" ) )  xmlProvPort = args. get ( "xmlProvPort" ) .getAsString ();
    if ( args.has ( "xmlProvPort" ) )  xmlProvPort = args. get ( "xmlProvPort" ) .getAsInt ();
    else missedParams.add ( "xmlProvPort" );
    
    if ( missedParams.hasMissedParameters () )
    {
      String msg = "Feed::create - some parameters are missing: ( " + missedParams + " )"; 
      log.error ( msg );
      return  Result.failure ( msg );
    }
    
//    gson = new Gson ();
//    jsonDocument = gson.toJson ( args );
    jsonDocument = Convert.jsonObjectToJson ( args );
    
    // Configure XML provider first - set important parameters
    response = CallRestService.put ( "http://" + xmlProvHost + ":" +  xmlProvPort + "/config",  jsonDocument  );
    
    log.debug ( "Call to /config response: " + response );

//    result = Convert.jsonToJsonObject ( response );
//    entries = Convert.jsonToMap ( response );
    
//    if ( params.get ( "success" ) .equals ( "failure" ) )
//    {
//      System.err.println ( "Feed::create failed to configure new feed!" );
//      return null;
//    }

//    if ( Result.isFailed ( result ) ) return Result.failure ( 
//                  "Feed::create failed to configure new feed! Reason: " + Result.getMessage ( result ) );

    if ( Result.isFailed ( response ) )
    {
      String msg = "Feed::create failed to configure new feed! REST returned: " + 
                                                    Convert.jsonObjectToJson ( Result .getValue ( response ) ); 
      log.error ( msg );
      return Result.failure ( msg );
    }

    // Register trigger now that will be called if new XML file found in dedicated directory
    response = CallRestService.put ( "http://" + xmlProvHost + ":" +  xmlProvPort + "/register_trigger",  jsonDocument  );

    log.debug ( "Call to /register_trigger response: " + response );

//    result = Convert.jsonToJsonObject ( response );

//    if ( response == null )
//    {
//      System.err.println ( "Feed::create failed to register trigger!" );
//      return null;
//    }

    if ( Result.isFailed ( response ) )
    {
      String msg = "Feed::create failed to register trigger! REST returned: " + 
                                                    Convert.jsonObjectToJson ( Result .getValue ( response ) ); 
      log.error ( msg );
      return Result.failure ( msg );
    }
    else return response;
//    if ( Result.isFailed ( result ) ) return Result.failure ( 
//              "Feed::create failed to register trigger! Reason: " + Result.getMessage ( result ) );
//
//
//    return Result.success ( "XML files feed successfully created" );
  }


  /**
   * Deletes the feed.
   * @param args - JsonObject containing all required parameters
   * @return Result as JsonObject with status, message, etc. 
   * @throws IOException
   */
  public static JsonObject delete ( JsonObject args ) throws IOException
  {
    JsonObject response = null;
//    String response = null;
//    Gson gson = null;
    String jsonDocument = null;
//    JsonObject result = null;
    MissedParams missedParams = new MissedParams ();

    log.info ( "Deleting feed" );
    
    // Obtain parameters from environment //???
    if ( args.has ( "xmlProvHost" ) )  xmlProvHost = args. get ( "xmlProvHost" ) .getAsString ();
    else missedParams.add ( "xmlProvHost" );
    if ( args.has ( "xmlProvPort" ) )  xmlProvPort = args. get ( "xmlProvPort" ) .getAsInt ();
    else missedParams.add ( "xmlProvPort" );
    
    if ( missedParams.hasMissedParameters () )
    {
      String msg = "Feed::delete - some parameters are missing: ( " + missedParams + " )"; 
      log.error ( msg );
      return  Result.failure ( msg );
    }
    
//    gson = new Gson ();
//    jsonDocument = gson.toJson ( args );
    jsonDocument = Convert.jsonObjectToJson ( args );
    
    response = CallRestService.put ( "http://" + xmlProvHost + ":" +  xmlProvPort + "/unregister_trigger",  jsonDocument  );
   
    log.debug ( "Call to /unregister_trigger response: " + response );

//    result = Convert.jsonToJsonObject ( response );

//    if ( response == null )
//    {
//      System.err.println ( "Feed::create failed to unregister trigger!" );
//      return null;
//    }

    if ( Result.isFailed ( response ) )
    {
      String msg = "Feed::delete failed to unregister trigger! REST returned: " + 
                                                    Convert.jsonObjectToJson ( Result .getValue ( response ) ); 
      log.error ( msg );
      return Result.failure ( msg );
    }
    else return response;


//    if ( Result.isFailed ( result ) ) return Result.failure ( 
//              "Feed::create failed to unregister trigger! Reason: " + Result.getMessage ( result ) );

//    return Result.success ( "XML files feed successfully deleted" );
  }

}
