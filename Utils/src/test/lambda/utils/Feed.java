package test.lambda.utils;

import java.io.IOException;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public final class Feed
{
//  private static final String STATUS_OK = "{\n\t\"status\": \"success\"\n\t\"success\": \"true\"\n}";
  private static String xmlProvHost = null; //"127.0.0.1";
  private static String xmlProvPort = null; //"8080";
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
    Gson gson = null;
    String response = null;
    String jsonDocument = null;
    @SuppressWarnings ( "rawtypes" )
    Map entries = null;
    MissedParams missedParams = new MissedParams ();
 
    System.out.print ( "Creating feed..." );

    // Obtain parameters from environment
    if ( args.has ( "xmlProvHost" ) )  xmlProvHost = args. get ( "xmlProvHost" ) .getAsString ();
    else missedParams.add ( "xmlProvHost" );
    if ( args.has ( "xmlProvPort" ) )  xmlProvPort = args. get ( "xmlProvPort" ) .getAsString ();
    else missedParams.add ( "xmlProvPort" );
    
    if ( missedParams.hasMissedParameters () )
      return Result.failure ( "Feed::create - some parameters are missing: ( " + missedParams + " )");
    
    gson = new Gson ();
    jsonDocument = gson.toJson ( args );
    
System.out.println ( "JSON -> " + jsonDocument );//!!!
System.out.println ( "http://" + xmlProvHost + ":" +  xmlProvPort + "/config" );//!!!

    response = CallRestService.put ( "http://" + xmlProvHost + ":" +  xmlProvPort + "/config",  jsonDocument  );

System.out.println ( "Call to /config response - > " + response );//!!!

    entries = Convert.jsonToMap ( response );
    
    if ( entries.get ( "success" ) .equals ( "failure" ) )
    {
      System.err.println ( "Feed::create failed to configure new feed!" );
      return null;
    }

    response = CallRestService.put ( "http://" + xmlProvHost + ":" +  xmlProvPort + "/register_trigger",  jsonDocument  );
    
System.out.println ( "Call to /register_trigger response - > " + response );//!!!

    if ( response == null )
    {
      System.err.println ( "Feed::create failed to register trigger!" );
      return null;
    }


    return Result.success ( "XML files feed successfully created" );
//    return STATUS_OK;
  }


  /**
   * Deletes the feed.
   * @param args - JsonObject containing all required parameters
   * @return Result as JsonObject with status, message, etc. 
   * @throws IOException
   */
  public static JsonObject delete ( JsonObject args ) throws IOException
  {
    String response = null;
    Gson gson = null;
    String jsonDocument = null;

    gson = new Gson ();
    jsonDocument = gson.toJson ( args );
    
System.out.println ( "JSON -> " + jsonDocument );//!!!
System.out.println ( "http://" + xmlProvHost + ":" +  xmlProvPort + "/config" );//!!!
    
    response = CallRestService.put ( "http://" + xmlProvHost + ":" +  xmlProvPort + "/unregister_trigger",  jsonDocument  );
    
System.out.println ( "Call to /unregister_trigger - > " + response );//!!!

    if ( response == null )
    {
      System.err.println ( "Feed::create failed to register trigger!" );
      return null;
    }

    return Result.success ( "XML files feed successfully deleted" );
//    return STATUS_OK;
  }

}
