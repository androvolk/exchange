package test.lambda.openwhisk.providers.xml;

import static spark.Spark.after;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.put;
import static spark.Spark.stop;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import test.lambda.utils.CallRestService;
import test.lambda.utils.Convert;
import test.lambda.utils.CouchDB;
import test.lambda.utils.MissedParams;
import test.lambda.utils.Result;


public final class XmlProvider
{
  private static final Logger logger = LoggerFactory.getLogger ( XmlProvider.class );

//  private static final String STATUS_OK = "{\n\t\"status\": \"success\"\n\t\"success\": \"true\"\n}";
//  private static final String STATUS_ERROR = "{\n\t\"status\": \"failure\"\n\t\"success\": \"false\"\n}";
  private static final int HTTP_PORT = 8080;

  // Directory polling parameters
  private static String dirToMonitor = "/tmp/lambda_demo";
  private static String pollInterval = "5000";
  
  // CouchDB access parameters
  private static String dbHost = null; //"127.0.0.1";
  private static String dbPort = null; //5984;
  private static String dbName = null; //"feed_files";
  private static String dbLogin = null; //"lambda_demo";
  private static String dbPassword = "null; //123456";
  
//  private static String pollIntervalParam = null;  //???

  // Trigger parameters
  private static String triggerHost = "127.0.0.1";
  private static String triggerName = null;
  private static String triggerUrl = null;

  // Thread and provider control variables
  private volatile static boolean isOn = false;
  private volatile static boolean isPaused = false;
  private volatile static boolean isBusy = false;

  // Worker thread monitoring directory
  private static Thread poller = null;
  
  
  public static void main ( String [] args ) throws RuntimeException
  {

    // Configuring SLF4J logging facility
    logger.trace("TRACE");
    logger.info("INFO");
    logger.debug("DEBUG");
    logger.warn("WARN");
    logger.error("ERROR");
    
    // Starting provider itself
    new XmlProvider ();

    // Setting HTTP listening port 
    port ( HTTP_PORT );
    
    // Let's show that we're alive
    System.out.println ( "\n\n*******************************************************************************************" );
    System.out.println ( "*                                                                                         *" );
    System.out.println ( "*             Lambda Demo OpenWhisk XML Documents Provider    (c) 2017                    *" );
    System.out.println ( "*                                                                                         *" );
    System.out.println ( "*******************************************************************************************\n\n" );
    
    // Simple Ping method to see that provider is Ok
    get ( "/hello", ( req, res ) -> "Hello World" );
    
    // Shut down the provider
    post ( "/quit", ( req, res ) -> { isOn = false; stop (); return "Service Stopped!"; } );

    // Configure provider
    put ( "/config", ( req, res ) ->
    {
      MissedParams missedParams = new MissedParams ();

System.out.println ( "/config request -> " + req.params () );//!!!
System.out.println ( "/config PUT payload  -> " + req.body () );//!!!

      Map < String, String > params = null;
      try { params = ( new Gson () .fromJson ( req.body (), HashMap.class ) ); }
      catch ( Exception e ) { System.out.println ( e.getMessage () ); }

      // Validate important parameters
      if ( params == null )
//        return "{\n\t\"status\": \"failure\",\n\t\"success\": \"false\",\n\t" + 
//                                  "\"msg\": \"/config call failed due to parameters!\"\n}";
            return Result.toJson ( Result.failure ( "/config call failed due to parameters!" ) );
        
      // Set new parameters values arrived with call instead of default ones
      if ( params.containsKey ( "dirToMonitor" ) )  dirToMonitor = params.get ( "dirToMonitor" );
      if ( params.containsKey ( "pollInterval" ) )  pollInterval = params.get ( "pollInterval" );

      if ( params.containsKey ( "dbHost" ) ) dbHost = params.get ( "dbHost" );
      else missedParams.add ( "dbHost" );
      if ( params.containsKey ( "dbPort" ) ) dbPort = params.get ( "dbPort" );
      else missedParams.add ( "dbPort" );
      if ( params.containsKey ( "dbName" ) ) dbName = params.get ( "dbName" );
      else missedParams.add ( "dbName" );
      if ( params.containsKey ( "dbLogin" ) )  dbLogin = params.get ( "dbLogin" );
      else missedParams.add ( "dbLogin" );
      if ( params.containsKey ( "dbPassword" ) )  dbPassword = params.get ( "dbPassword" );
      else missedParams.add ( "dbPassword" );

      if ( missedParams.hasMissedParameters () )
        return Result.toJson ( 
                  Result.failure ( "/config - some parameters are missing: ( " + missedParams + " )" ) );

System.out.println ("dirToMonitor -> " + dirToMonitor  ); //!!!    
System.out.println ("pollInterval -> " + pollInterval  ); //!!!    
System.out.println ("dbHost -> " + dbHost  ); //!!!    
System.out.println ("dbPort -> " + dbPort  ); //!!!    
System.out.println ("dbName -> " + dbName  ); //!!!    
System.out.println ("dbLogin -> " + dbLogin  ); //!!!    
System.out.println ("dbPassword -> " + dbPassword  ); //!!!    

      // Check up if directory exist
      if ( !  ( new File ( dirToMonitor ) ) .exists () )
      {
        System.err.println ( "Directory '" + dirToMonitor + "' doesn't exist! Configuration failed!" );
        res.status ( 500 );
//        return STATUS_ERROR;
        return Result.toJson ( 
                  Result.failure ( "/config - Directory '" + dirToMonitor + 
                        "' doesn't exist! Configuration failed! " ) );
      }

//      return STATUS_OK;
      return Result.toJson ( Result.success ( "/config - Confuguration succeeded" ) );
    } );

    // Process trigger registering
    put ( "/register_trigger", ( req, res ) -> 
    {
      MissedParams missedParams = new MissedParams ();

  System.out.println ( "/register_triggerrequest -> " + req.params () );//!!!
  System.out.println ( "/register_trigger PUT payload  -> " + req.body () );//!!!

      Map < String, String > params = null;
      try { params = ( new Gson () .fromJson ( req.body (), HashMap.class ) ); }
      catch ( Exception e ) 
      { 
        String msg = e.getMessage ();
        System.err.println ( "/register_trigger - Unable to parse incoming request:\n" + req.body () + "\n" + msg );
        return Result.toJson ( 
            Result.failure ( "/register_trigger - Unable to parse incoming request:\n" + req.body () + "\n" + msg ) );
      }
      
      // Validate important parameters
      if ( params.containsKey ( "triggerHost" ) )  triggerHost = params.get ( "triggerHost" );
      else missedParams.add ( "triggerHost" );
      if ( params.containsKey ( "triggerName" ) )  triggerName = params.get ( "triggerName" );
      else missedParams.add ( "triggerName" );

      if ( missedParams.hasMissedParameters () )
        return Result.toJson ( 
                  Result.failure ( "/register_trigge - some parameters are missing: ( " + missedParams + " )" ) );

      // Construct valid trigger URL of request parameters
      triggerUrl = "https://" + triggerHost + "/api/v1/namespaces/guest/triggers" + triggerName;
     
System.out.println ("triggerHost -> " + triggerHost  ); //!!!    
System.out.println ("triggerName -> " + triggerName  ); //!!!    
System.out.println ("triggerUrl -> " + triggerUrl  ); //!!!    

//      try   //TODO: Remove after debugging! 
//      {
        // Startup directory polling
        poller = new Thread ( new DirPoller (), "XML-Provider-Dir-Poller" );

//      } catch (Exception e) { System.err.println ( e.getMessage () ); } //TODO: Remove after debugging!

      // Set busy flag and start the polling thread
      isOn = true;
      poller.start ();
      
      // Operation succeeded
//      return STATUS_OK;
      return Result.toJson ( Result.success ( "/register_trigger - Trigger established" ) );
    } );
    
    
    // Process trigger registering
    put ( "/unregister_trigger", ( req, res ) -> 
    {
  System.out.println ( "/register_triggerrequest -> " + req.params () );//!!!
  System.out.println ( "/register_trigger PUT payload  -> " + req.body () );//!!!

      Map < String, String > params = null;
      try { params = ( new Gson () .fromJson ( req.body (), HashMap.class ) ); }
      catch ( Exception e ) 
      { 
        String msg = e.getMessage ();
        System.err.println ( "/unregister_trigger - Unable to parse incoming request:\n" + req.body () + "\n" + msg );
        return Result.toJson ( 
            Result.failure ( "/unregister_trigger - Unable to parse incoming request:\n" + req.body () + "\n" + msg ) );
      }
      
      if ( params.containsKey ( "triggerName" ) )
      {
        if ( params .get ( "triggerName" ) .equals ( triggerName ) )
        {
          isOn = false;
          triggerHost = null;
          triggerName = null;
          triggerUrl = null;
        }
        else return Result.toJson ( Result.ignored ( "/unregister_trigger - Trigger '" + "' unknown. Ignored." ) );

      }
      else return Result.toJson ( Result.failure ( "/unregister_trigger - Parameter 'triggerName' is missed!" ) );
      
      // Operation succeeded
      return Result.toJson ( Result.success ( "/unregister_trigger - Trigger disconnected" ) );
    } );
    
    // Add MIME type header to every call's header
    after ( ( req, res ) -> { res.type ( "application/json" ); } );
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
                Map entries = uploadXmlToCouchDB ( file );
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
          Thread.sleep ( Integer.parseInt ( pollInterval ) );
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
    private Map uploadXmlToCouchDB ( final File xmlToUpload) throws IOException, NoSuchAlgorithmException
    {
      Map jsonFields = null;
      String response = null;
      
//      isBusy = true; //???
      
System.out.println ( "File found -> " + xmlToUpload.getCanonicalPath () ); //!!!
      
      response = CouchDB.createDocument ( 
                dbHost, dbPort, dbName, dbLogin, dbPassword, null, "{\"current_processor\":\"XmlProvider\"}" );
 //     if ( response == null ) return Result.failure ( "DirPoller::uploadXmlToCouchDB - uploadXmlToCouchDB() returned null!" );
      jsonFields = Convert.jsonToMap ( response );
      
System.out.println ("CouchDB.createDocument response -> " + response  ); //!!!
System.out.println ("Map keys -> " + jsonFields.keySet () .toString ()  ); //!!!
System.out.println ("Map values -> " + jsonFields.values () .toString ()  ); //!!!

      String id = ( String ) jsonFields.get ( "id" );
      String rev = ( String ) jsonFields.get ( "rev" );
      
System.out.println ("_id -> " + id  ); //!!!
System.out.println ("_rev -> " + rev  ); //!!!
      
      response = CouchDB.uploadAttachment ( dbHost, dbPort, dbName, dbLogin, dbPassword,
                                      id, rev, "xml", xmlToUpload.getCanonicalPath (), "application/xml" );
//      jsonFields = CallRestService.jsonToMap ( response );

System.out.println ("CouchDB.createDocumentAttachment response -> " + response  ); //!!!

      xmlToUpload.delete ();
//      isBusy = false; //???
      
      return  Convert.jsonToMap ( response );
    }
  }


  /**
   * Fires the trigger passing id and revision of document stored in CouchDB
   * @param params
   * @throws IOException 
   */
  private static Map fireTrigger ( final Map params ) throws IOException
  {
    if ( params == null || ! params.containsKey ( "id" ) || ! params.containsKey ( "rev" ) )
    {
System.err.println ( params ); //!!!
      System.err.println ( "Feed::fireTrigger received bad 'params'!" );
      return null;
    }
   
    String response = CallRestService.put ( triggerUrl, "{\n\t\"id\":\"" + 
                                              ( String ) params.get ( "id" ) + 
                                                  "\",\n\t\"rev\":\"" + ( String ) params.get ( "rev" ) + "\"\n}" );
System.out.println ("CallRestService.put response -> " + response  ); //!!!

    return Convert.jsonToMap ( response );
  }

}
