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
    get ( "/hello", ( req, res ) -> 
    {
      log.info ( "'/hello' called" );
      log.debug ( "Payload - " + req.body () );
      return "Hello from the web server's belly!";
    } );

    
// ---------------------- /quit - Shutting down the provider ---------------------------------------------------------
    
    // Shut down the provider
    post ( "/quit", ( req, res ) -> 
    {
      log.info ( "'/quit' called" );
      log.debug ( "Payload - " + req.body () );
      isOn = false;
      stop ();
      return "Service Stopped!"; 
    } );

    
// ---------------------- /config - Configuring provider -------------------------------------------------------
    
    // Configure provider
    put ( "/config", ( req, res ) ->
    {
      MissedParams missedParams = new MissedParams ();

      log.info ( "'/config' called" );
      log.debug ( "Payload - " + req.body () );

      params = Convert.jsonToJsonObject ( req.body () );

      // Validate important parameters
      if ( params == null )
      {
        String msg = "'/config' call failed due to incorrect parameters!";
        log.error ( msg );
        log.debug ( "Payload - " + req.body () );
        return ( Convert.jsonObjectToJson ( Result.failure ( msg ) ) );
      }

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
      
      if ( missedParams.hasMissedParameters () )
      {
        String msg = "'/config' - some parameters are missing: ( " + missedParams + " )";
        res.status ( 500 );
        log.error ( msg );
        return Convert.jsonObjectToJson ( Result.failure ( msg ) );
      }

      log.debug ( String.format ( "Passed Parameters: {\n\tdirToMonitor : %s\n\tpollInterval : %d" +
              "\n\tdbHost : %s\n\tdbPort : %d\n\tdbName : %s\n\tdbLogin : %s\n\tdbPassword : %s\n\txmlSchemaFile : %s\n}",
                               dirToMonitor, pollInterval, dbHost, dbPort, dbName, dbLogin, dbPassword, xmlSchemaFile ) );

      // Check up if directory exist
      if ( ! ( new File ( dirToMonitor ) ) .exists () )
      {
        String msg = "Directory '" + dirToMonitor + "' doesn't exist! Configuration failed!";
        log.error ( msg );

        // Set HTTP return code 500 for marking REST operation as failed
        res.status ( 500 );
        return Convert.jsonObjectToJson ( Result.failure ( msg ) );
      }

      log.info ( "'/config' - Configuration completed" );
      return Convert.jsonObjectToJson ( Result.success ( "'/config' - Configuration succeeded" ) );
    } );

 
// ---------------------- /register_trigger - Process trigger registering ----------------------------------
    
    put ( "/register_trigger", ( req, res ) -> 
    {
      MissedParams missedParams = new MissedParams ();

      log.info ( "'/register_trigger' called" );
      log.debug ( "Payload - " + req.body () );

      try { params = Convert.jsonToJsonObject ( req.body () ); }//???
      catch ( Exception e ) 
      { 
        String msg = "'/register_trigger' - Unable to parse incoming request:\n" + req.body () + "\n" + e.getMessage ();
        log.error ( msg );

        // Set HTTP return code 500 for marking REST operation as failed
        res.status ( 500 );//???
        return Convert.jsonObjectToJson ( Result.failure ( msg ) );
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
        log.debug ( String.format ( "authKeyLogin = %s, authKeyPassword = %s", authKeyLogin, authKeyPassword ) );
      }
      else missedParams.add ( "authKey" );

      if ( missedParams.hasMissedParameters () )
      {
        String msg = "'/register_trigger' - some parameters are missing: { " + missedParams + " }";
        log.error ( msg );

        // Set HTTP return code 500 for marking REST operation as failed
        res.status ( 500 );
        return Convert.jsonObjectToJson ( Result.failure ( msg  ) );
      }

      // Construct valid trigger URL of request parameters
      triggerUrl = "https://" + triggerHost + "/api/v1/namespaces/guest/triggers/" + triggerName;
     
      // Startup directory polling
      poller = new Thread ( new DirPoller (), "XML-Provider-Dir-Poller" );

      // Set busy flag and start the polling thread
      isOn = true;
      log.debug ( "Polling thread 'poller' is ready to start - " + poller );
      poller.start ();
      
      // Operation succeeded
      return Convert.jsonObjectToJson ( Result.success ( "'/register_trigger' - Trigger established" ) );
    } );
    

// ---------------------- /unregister_trigger - Process trigger disconnection  ----------------------------------
    
    put ( "/unregister_trigger", ( req, res ) -> 
    {
      
      log.info ( "'/unregister_trigger' called" );
      log.debug ( "Payload - " + req.body () );

      try { params = Convert.jsonToJsonObject ( req.body () ); }//???
      catch ( Exception e ) 
      { 
        String msg = "/unregister_trigger - Unable to parse incoming request:\n" + req.body () + "\n" + e.getMessage ();
        log.error ( msg );
        return Convert.jsonObjectToJson ( Result.failure ( msg ) );
      }

      if ( params. has ( "triggerName" ) )
      {
        if ( triggerName != null && params .get ( "triggerName" ) .getAsString () .equals ( triggerName ) )
        {
          log.info ( "Unregistering existing trigger. triggerName : " + triggerName );

          isOn = false;
          triggerHost = null;
          triggerName = null;
          triggerUrl = null;
          authKeyLogin = null;
          authKeyPassword = null;
        }
        else 
        { 
          String msg = "/unregister_trigger - Trigger unknown. Ignored.";
          String json =  Convert.jsonObjectToJson ( Result.ignored ( msg ) ) ;
          log.info ( msg );
          log.debug ( json );
          return json;
        };
      }
      else
      {
        String msg = "/unregister_trigger - Parameter 'triggerName' is missed!";
        log.error ( msg );
        return Convert.jsonObjectToJson ( Result.failure ( msg ) );
      }
      
      // Operation succeeded
      String msg = "/unregister_trigger - Trigger disconnected";
      log.info ( msg );
      return Convert.jsonObjectToJson ( Result.success ( msg ) );
    } );
    
    // Add MIME type header to every call's header
    after ( ( req, res ) -> { res.type ( "application/json" ); } );
    
    log.debug ( "End of main() reached" );
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
      log.debug ( "DirPoller::run() called. Polling thread started." );

      if ( ! ( new File ( dirToMonitor ) ) .exists () )
      {
        log.error ( "Directory '" + dirToMonitor + 
                  "' doesn't exist! Call '/config' once again with correct 'dirToMonitor' parameter." );
        return; 
      }
      
      log.info ( "Directory to poll is '" + dirToMonitor + "'" );
      
      try
      {
        log.info ( "Directory monitoring for XML files started." );
        
        // Polling keeps going until external signal to stop arrived
        while ( isOn == true )
        {
          
          log.debug ( "DirPoller is waiting for XML file..." );

          // If files processing is not suspended, proceed. Otherwise just sleep for some time 
          if ( ! isPaused )
          {
            isBusy = true;
            
            // Looking for files in monitored directory and choosing XMLs only
            File [] fileList = dir.listFiles ();
            for ( File file : fileList )
            {
              if ( file.getName () .toLowerCase () .endsWith ( ".xml" ) )
              {
                // Store discovered XML in CouchDB, first...
                JsonObject entries = uploadXmlToCouchDB ( file );
                if ( entries == null )
                {
                  log.error ( "DirPoller::run - uploadXmlToCouchDB() returned null unexpectedly!" );
                  isBusy = false; isOn = false;
                  stop ();
                }
                
                // ... then fire the registered OpenWhisk trigger
                entries = fireTrigger ( entries );
                log.info ( "Trigger '" + triggerName + "' fired" );
                log.debug ( "DirPoller::fireTrigger result - " + entries );
                break;
              }
            }

            isBusy = false;
          }

          // Short pause before next attempt to find the file
          Thread.sleep ( pollInterval );
        }
      } 
      catch ( InterruptedException | IOException | NoSuchAlgorithmException  e )
      { 
        log.error ( "Exception in DirPoller::run - " + e.getMessage () ); 
        e.printStackTrace ();
      }
      
      log.info ( "Directory monitoring for XML files stopped." );
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
      
      log.info ( "File found - " + xmlToUpload.getCanonicalPath () );
      
      response = CouchDB.createDocument ( 
                dbHost, dbPort, dbName, dbLogin, dbPassword, null, "{\"last_processor\":\"XmlProvider\"}" );

      log.debug ( "CouchDB.createDocument response - " + response );
      
      if ( Result.isFailed ( response ) )
      {
        String msg = "DirPoller::uploadXmlToCouchDB failed to create new document! REST returned: " + 
                                                      Convert.jsonObjectToJson ( Result .getValue ( response ) ); 
        log.error ( msg );
        return Result.failure ( msg );
      }
      else value = Result.getValue ( response );

      // Need to know document ID and revision to upload the attachment
      String id = value .get ( "id" ) .getAsString ();
      String rev = value .get ( "rev" ) .getAsString ();

      log.info ( String.format ( 
                  "Document created in database for file. Document ID = %s, revision = %s", id, rev ) );
      
      response = CouchDB.uploadXmlAttachment ( dbHost, dbPort, dbName, dbLogin, dbPassword,
                                                        id, rev, "XML", xmlToUpload.getCanonicalPath () );

      log.debug ( "CouchDB::uploadXmlAttachment result - " + response );

      if ( Result.isFailed ( response ) )
      {
        String msg = "DirPoller::uploadXmlToCouchDB failed to upload attachment! Reason: " + 
                                                      Convert.jsonObjectToJson ( Result .getValue ( response ) ); 
        log.error ( msg );
        return Result.failure ( msg );
      }
      else
      {
        log.info ( String.format ( 
              "File uploadeded to database and attached to document. Document ID = %s, new revision = %s",
                                                                  id, response .get ( "rev" ) .getAsString () ) );
        // Delete processed file from the monitored directory
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

    log.debug ( "CallRestService::post result - " + response );

    if ( Result.isFailed ( response ) )
    {
      String msg = "DirPoller::fireTrigger failed to fire trigger! REST returned: " + 
                                                    Convert.jsonObjectToJson ( Result .getValue ( response ) ); 
      log.error ( msg );
      return Result.failure ( msg );
    }
    else return response;
  }

}
