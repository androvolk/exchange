package test.lambda.utils;

import java.io.IOException;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public final class Feed
{
  private static final String STATUS_OK = "{\n\t\"status\": \"success\"\n\t\"success\": \"true\"\n}";
  private static String xmlProvHost = "127.0.0.1";
  private static String xmlProvPort = "8080";
//  private static String dbHost = "127.0.0.1";
//  private static int dbPort = 5984;
//  private static String dbName = "feed_files";
//  private static String dbLogin = "lambda_demo";
//  private static String dbPassword = "123456";

  
   
  public static String create ( final JsonObject args ) throws IOException
  {
    Gson gson = null;
    String response = null;
    String jsonDocument = null;
    @SuppressWarnings ( "rawtypes" )
    Map entries = null;
    MissedParams missedParams = new MissedParams ();
 
    System.out.print ( "Creating feed..." );

    if ( args.has ( "xmlProvHost" ) ) xmlProvHost = args. get ( "xmlProvHost" ) .getAsString ();
    if ( args.has ( "xmlProvPort" ) ) xmlProvPort = args. get ( "xmlProvPort" ) .getAsString ();
    
    gson = new Gson ();
    jsonDocument = gson.toJson ( args );
    
System.out.println ( "JSON -> " + jsonDocument );//!!!
System.out.println ( "http://" + xmlProvHost + ":" +  xmlProvPort + "/config" );//!!!

    response = CallRestService.put ( "http://" + xmlProvHost + ":" +  xmlProvPort + "/config",  jsonDocument  );
    entries = CallRestService.jsonToMap ( response );
    
    if ( entries.get ( "success" ) .equals ( "failure" ) )
    {
      System.err.println ( "Feed::create failed to configure new feed!" );
      return null;
    }

System.out.println ( "response -> " + response );//!!!
    
    response = CallRestService.put ( "http://" + xmlProvHost + ":" +  xmlProvPort + "/register_trigger",  jsonDocument  );
    
    if ( response == null )
    {
      System.err.println ( "Feed::create failed to register trigger!" );
      return null;
    }

System.out.println ( "response -> " + response );//!!!

    return STATUS_OK;
  }

  public static String delete ( JsonObject args )
  {
    // TODO Implement feed deleting logic
    return STATUS_OK;
  }

}
