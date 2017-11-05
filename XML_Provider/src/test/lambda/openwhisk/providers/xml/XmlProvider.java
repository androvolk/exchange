package test.lambda.openwhisk.providers.xml;

import static spark.Spark.port;
import static spark.Spark.get;
import static spark.Spark.put;
import static spark.Spark.post;
import static spark.Spark.stop;
import static spark.Spark.after;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
//import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.google.gson.Gson;
import com.google.gson.JsonObject;

import test.lambda.utils.CallRestService;
import test.lambda.utils.Convert;
import test.lambda.utils.CouchDB;
import test.lambda.utils.MissedParams;
import test.lambda.utils.Result;


public final class XmlProvider
{
  private static final Logger log = LoggerFactory.getLogger ( XmlProvider.class );

  private static final int HTTP_PORT = 8080;

  // Directory polling parameters
  private static String dirToMonitor ="/tmp/lambda_demo";
  private static int pollInterval = 5000;
  
  // Authorization key parameters
  private static String authKeyLogin = null;
  private static String authKeyPassword = null;
  
  // CouchDB access parameters
  private static String dbHost = null;
  private static int dbPort;
  private static String dbName = null;
  private static String dbLogin = null;
  private static String dbPassword = null;
  private static String xmlSchemaFile = null;
  
  // Trigger parameters
  private static String triggerHost = null;
  private static String triggerName = null;
  private static String triggerUrl = null;

  // Thread and provider control variables
  private volatile static boolean isOn = false;
  private volatile static boolean isPaused = false;
  private volatile static boolean isBusy = false;

  // Worker thread monitoring directory
  private static Thread poller = null;
  
  private static JsonObject params = null;
  
  
  public static void main ( String [] args ) throws RuntimeException
  {
    
    // Starting provider itself
    new XmlProvider ();

    // Setting HTTP listening port 
    port ( HTTP_PORT );
    
    // Let's show that we're alive
    log.info ( "\n\n  *******************************************************************************************" );
    log.info ( "  *                                                                                         *" );
    log.info ( "  *             Lambda Demo OpenWhisk XML Documents Provider    (c) 2017                    *" );
    log.info ( "  *                                                                                         *" );
    log.info ( "  *******************************************************************************************\n\n" );

// ---------------------- /hello - Simple ping to be sure that provider is alive  ----------------------------------
    
    // Simple Ping method to see that provider is Ok
    get ( "/hello", ( req, res ) -> "Hello World" );

    
// ---------------------- /quit - Shutting down the provider ---------------------------------------------------------
    
    // Shut down the provider
    post ( "/quit", ( req, res ) -> { isOn = false; stop (); return "Service Stopped!"; } );

    
// ---------------------- /config - Configuring provider -------------------------------------------------------
    
    // Configure provider
    put ( "/config", ( req, res ) ->
    {
      MissedParams missedParams = new MissedParams ();

      log.info ( "/config PUT payload - " + req.body () );

      params = Convert.jsonToJsonObject ( req.body () ); //???

System.out.println ( "#1 ");//!!!

      // Validate important parameters
      if ( params == null )
        return ( Convert.jsonObjectToJson ( Result.failure ( "/config call failed due to parameters!" ) ) );
System.out.println ( "#2 ");//!!!

      if ( params.has ( "dirToMonitor" ) )  dirToMonitor = params .get ( "dirToMonitor" ) .getAsString ();
      if ( params.has ( "pollInterval" ) )  pollInterval = params .get ( "pollInterval" ) .getAsInt ();
      
      if ( params.has ( "dbHost" ) ) dbHost = params .get ( "dbHost" ) .getAsString ();
      else missedParams.add ( "dbHost" );
      if ( params.has ( "dbPort" ) ) dbPort = params .get ( "dbPort" ) .getAsInt ();
      else missedParams.add ( "dbPort" );
      if ( params.has ( "dbName" ) ) dbName = params .get ( "dbName" ) .getAsString ();
      else missedParams.add ( "dbName" );
      if ( params.has ( "dbLogin" ) ) dbLogin = params .get ( "dbLogin" ) .getAsString ();
      else missedParams.add ( "dbLogin" );
      if ( params.has ( "dbPassword" ) ) dbPassword = params .get ( "dbPassword" ) .getAsString ();
      else missedParams.add ( "dbPassword" );
      if ( params.has ( "xmlSchemaFile" ) ) xmlSchemaFile = params .get ( "xmlSchemaFile" ) .getAsString (); 
      else missedParams.add ( "xmlSchemaFile" );
      
System.out.println ( "#3 ");//!!!

      if ( missedParams.hasMissedParameters () )
      {
        res.status ( 500 );
        return Convert.jsonObjectToJson ( 
                  Result.failure ( "/config - some parameters are missing: ( " + missedParams + " )" ) );
      }
System.out.println ( "#4 ");//!!!

System.out.println ("dirToMonitor -> " + dirToMonitor  ); //!!!    
System.out.println ("pollInterval -> " + pollInterval  ); //!!!    
System.out.println ("dbHost -> " + dbHost  ); //!!!    
System.out.println ("dbPort -> " + dbPort  ); //!!!    
System.out.println ("dbName -> " + dbName  ); //!!!    
System.out.println ("dbLogin -> " + dbLogin  ); //!!!    
System.out.println ("dbPassword -> " + dbPassword  ); //!!!    
System.out.println ("xmlSchemaFile -> " + xmlSchemaFile  ); //!!!    

System.out.println ( "#5 ");//!!!

      // Check up if directory exist
      if ( ! ( new File ( dirToMonitor ) ) .exists () )
      {
        System.err.println ( "Directory '" + dirToMonitor + "' doesn't exist! Configuration failed!" );
        res.status ( 500 );//???
        return Convert.jsonObjectToJson ( 
                  Result.failure ( "/config - Directory '" + dirToMonitor + 
                                        "' doesn't exist! Configuration failed! " ) );
      }

      return Convert.jsonObjectToJson ( Result.success ( "/config - Confuguration succeeded" ) );
    } );

 
// ---------------------- /register_trigger - Process trigger registering ----------------------------------
    
    put ( "/register_trigger", ( req, res ) -> 
    {
      MissedParams missedParams = new MissedParams ();

  System.out.println ( "/register_trigger PUT payload  -> " + req.body () );//!!!

      try {
System.out.println ( "#6 ");//!!!
System.out.println ( "req.body() -> " + req.body () );//!!!

            params = Convert.jsonToJsonObject ( req.body () );
System.out.println ( "#7 ");//!!!
      }//???
      catch ( Exception e ) 
      { 
System.out.println ( "#8 ");//!!!
        res.status ( 500 );//???
        String msg = e.getMessage ();
        System.err.println ( "/register_trigger - Unable to parse incoming request:\n" + req.body () + "\n" + msg );
        return Convert.jsonObjectToJson (
            Result.failure ( "/register_trigger - Unable to parse incoming request:\n" + req.body () + "\n" + msg ) );
      }
      
      // Validate important parameters
      if ( params. has ( "triggerHost" ) )  triggerHost = params. get ( "triggerHost" ) .getAsString ();
      else missedParams.add ( "triggerHost" );
      if ( params. has( "triggerName" ) )
      {
        String fullTrigName = params .get ( "triggerName" ) .getAsString ();
        int pos = fullTrigName.lastIndexOf ( '/' ); // OpenWhisk sends trigger name with namespace. Bad API design!
        triggerName = fullTrigName.substring ( ++ pos );
      }
      else missedParams.add ( "triggerName" );
      if ( params. has( "authKey" ) )
      {  
        String authKey = params .get ( "authKey" ) .getAsString ();
        int offset = authKey.indexOf ( ':' );
        authKeyLogin = authKey.substring ( 0, offset );
        authKeyPassword = authKey.substring ( ++ offset );
System.out.println ( "authKeyLogin -> " + authKeyLogin );//!!!
System.out.println ( "authKeyPassword -> " + authKeyPassword );//!!!
      }
      else missedParams.add ( "authKey" );

      if ( missedParams.hasMissedParameters () )
      {
        res.status ( 500 );
        return Convert.jsonObjectToJson (
                  Result.failure ( "/register_trigge - some parameters are missing: ( " + missedParams + " )" ) );
      }
      // Construct valid trigger URL of request parameters
      triggerUrl = "https://" + triggerHost + "/api/v1/namespaces/guest/triggers/" + triggerName;
     
System.out.println ("triggerHost -> " + triggerHost  ); //!!!    
System.out.println ("triggerName -> " + triggerName  ); //!!!    
System.out.println ("triggerUrl -> " + triggerUrl  ); //!!!    

        // Startup directory polling
        poller = new Thread ( new DirPoller (), "XML-Provider-Dir-Poller" );

      // Set busy flag and start the polling thread
      isOn = true;
      poller.start ();
      
      // Operation succeeded
      return Convert.jsonObjectToJson ( Result.success ( "/register_trigger - Trigger established" ) );
    } );
    

// ---------------------- /unregister_trigger - Process trigger disconnection  ----------------------------------
    
    put ( "/unregister_trigger", ( req, res ) -> 
    {
      
  System.out.println ( "/unregister_trigger PUT payload  -> " + req.body () );//!!!

      try { params = Convert.jsonToJsonObject ( req.body () ); }//???
      catch ( Exception e ) 
      { 
System.out.println ( "Exception - > " + e.getMessage () );//!!!

        String msg = e.getMessage ();
        System.err.println ( "/unregister_trigger - Unable to parse incoming request:\n" + req.body () + "\n" + msg );
        return Convert.jsonObjectToJson (
            Result.failure ( "/unregister_trigger - Unable to parse incoming request:\n" + req.body () + "\n" + msg ) );
      }

System.out.println ( "jsonToJsonObject is OK" );//!!!
System.out.println ( "params -> " + params ); //!!!

      
      if ( params. has ( "triggerName" ) )
      {
System.out.println ( "params. has ( \"triggerName\" ) " );//!!!
System.out.println ( "triggerName -> " + triggerName );//!!!
System.out.println ( triggerName != null ? triggerName : "null????" );
        if ( triggerName != null && params .get ( "triggerName" ) .getAsString () .equals ( triggerName ) )
        {
System.out.println ( "triggerName matched" );//!!!

          isOn = false;
          triggerHost = null;
          triggerName = null;
          triggerUrl = null;
          authKeyLogin = null;
          authKeyPassword = null;
        }
        else { String json =  Convert.jsonObjectToJson ( Result.ignored ( "/unregister_trigger - Trigger unknown. Ignored." ) ) ; System.out.println ( "json -> " + json ); return json;  };
System.out.println ( "params. has ( \"triggerName\" ) - out" );//!!!

      }
      else return Convert.jsonObjectToJson ( Result.failure ( "/unregister_trigger - Parameter 'triggerName' is missed!" ) );
System.out.println ( "Before return Result.toJson ( Result.success ( \"/unregister_trigger - Trigger disconnected\" )" );//!!!
      
      // Operation succeeded
      return Convert.jsonObjectToJson ( Result.success ( "/unregister_trigger - Trigger disconnected" ) );
    } );
    
    // Add MIME type header to every call's header
    after ( ( req, res ) -> { res.type ( "application/json" ); } );
    
System.out.println ( "end of main ()" );//!!!

  }

  
//---------------------------------------- Directory polling thread ------------------------------------------------

  /**
   * Inner static class which polls dedicated directory for incoming XML files, stores the  
   */
  private static final class DirPoller implements Runnable
  {
    File dir = new File ( dirToMonitor );

    @Override
    public void run ()
    {
      if ( ! ( new File ( dirToMonitor ) ) .exists () )
      {
        System.err.println ( "Directory '" + dirToMonitor + 
                  "' doesn't exist! Call /config once again with correct 'dirToMonitor' parameter." );
        return; 
      }
      
      System.out.println ( "Polling directory '" + dirToMonitor + "'" );
      
      try
      {
        // Polling keeps going until external signal to stop arrived
        while ( isOn == true )
        {
          
System.out.print ( '.' ); //!!!

          if ( ! isPaused )
          {
            isBusy = true; //???
            
            File [] fileList = dir.listFiles ();
            for ( File file : fileList )
            {
              // Looking for XML file
              if ( file.getName () .toLowerCase () .endsWith ( ".xml" ) )
              {
                // Store discovered XML in CouchDB, first...
                JsonObject entries = uploadXmlToCouchDB ( file );
                if ( entries == null )
                {
                  System.err.println ( "DirPoller::run - uploadXmlToCouchDB() returned null unexpectedly!" );
                  isBusy = false; isOn = false;
                  stop ();
                }
                
                // ... then fire the registered OpenWhisk trigger
                entries = fireTrigger ( entries );

System.out.println ( "fireTrigger returned -> " + entries ); //!!!                
                
                break;
              }
            }

            isBusy = false; //???
          }

          // Short pause before next attempt to find the file
          Thread.sleep ( pollInterval );
        }
      } 
      catch ( InterruptedException | IOException | NoSuchAlgorithmException  e )
      { System.err.println ( e.getMessage () ); }
    }

    
    /**
     * Creates the document in CouchDB and attach the XML file to it
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    private JsonObject uploadXmlToCouchDB ( final File xmlToUpload) throws IOException, NoSuchAlgorithmException
    {
      JsonObject value = null;
      JsonObject response = null;
      
System.out.println ( "File found -> " + xmlToUpload.getCanonicalPath () ); //!!!
      
      response = CouchDB.createDocument ( 
                dbHost, dbPort, dbName, dbLogin, dbPassword, null, "{\"last_processor\":\"XmlProvider\"}" );
      
System.out.println ("CouchDB.createDocument response -> " + response  ); //!!!
      
      if ( Result.isFailed ( response ) )
      //if ( Result.isFailed ( result ) )
      {
        String msg = "DirPoller::uploadXmlToCouchDB failed to create new document! REST returned: " + 
                                                      Convert.jsonObjectToJson ( Result .getValue ( response ) ); 
        System.err.println ( msg );
        return Result.failure ( msg );
      }
      else value = Result.getValue ( response );

      String id = value .get ( "id" ) .getAsString ();
      String rev = value .get ( "rev" ) .getAsString ();
      
System.out.println ("_id -> " + id  ); //!!!
System.out.println ("_rev -> " + rev  ); //!!!
      
      response = CouchDB.uploadXmlAttachment ( dbHost, dbPort, dbName, dbLogin, dbPassword,
                                                        id, rev, "XML", xmlToUpload.getCanonicalPath () );

System.out.println ("CouchDB.createDocumentAttachment response -> " + response  ); //!!!

      if ( Result.isFailed ( response ) )
      {
        String msg = "DirPoller::uploadXmlToCouchDB failed to upload attachment! REST returned: " + 
                                                      Convert.jsonObjectToJson ( Result .getValue ( response ) ); 
        System.err.println ( msg );
        return Result.failure ( msg );
      }
      else
      {
        xmlToUpload.delete ();
        return response;
      }
    }
  }


  /**
   * Fires the trigger passing id and revision of document stored in CouchDB
   * @param params
   * @throws IOException 
   */
  private static JsonObject fireTrigger ( final JsonObject params ) throws IOException
  {
    JsonObject response = CallRestService.post ( triggerUrl, authKeyLogin, authKeyPassword,
                     "{\"id\":\"" + Result.getValue ( params ) .get ( "id" ) .getAsString () + 
                           "\",\"rev\":\"" + Result.getValue ( params ) .get ( "rev" ) .getAsString () + "\"}" );

System.out.println ("CallRestService.put response -> " + response  ); //!!!

    if ( Result.isFailed ( response ) )
    {
      String msg = "DirPoller::fireTrigger failed to fire trigger! REST returned: " + 
                                                    Convert.jsonObjectToJson ( Result .getValue ( response ) ); 
      System.err.println ( msg );
      return Result.failure ( msg );
    }
    else return response;
  }

}
