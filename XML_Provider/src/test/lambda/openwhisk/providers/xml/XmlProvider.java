package test.lambda.openwhisk.providers.xml;

import static spark.Spark.port;
import static spark.Spark.get;
import static spark.Spark.put;
import static spark.Spark.post;
import static spark.Spark.stop;
import static spark.Spark.after;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.management.RuntimeErrorException;

import test.lambda.utils.CallRestService;
import test.lambda.utils.CouchDB;


public final class XmlProvider
{
  private static final String STATUS_OK = "{\n\t\"status\": \"success\"\n\t\"success\": \"true\"\n}";
  private static final int HTTP_PORT = 8080;

  // Directory polling parameters
  private static String dirToMonitor = "/tmp/lambda_demo";
  private static int pollInterval = 5000;
  
  // CouchDB access parameters
  private static String dbHost = "127.0.0.1";
  private static int dbPort = 5984;
  private static String dbName = "feed_files";
  private static String dbLogin = "lambda_demo";
  private static String dbPassword = "123456";
  
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
  
  
//  public XmlProvider ()
//  {
//    port ( 8080 );
//  }
  
 
  public static void main ( String [] args ) throws RuntimeException
  {
    new XmlProvider ();

    // Setting HTTP listening port 
    port ( HTTP_PORT ); //???
    
    // Simple Ping method to see that provider is Ok
    get ( "/hello", ( req, res ) -> "Hello World" );
    
    // Shut down the provider
    post ( "/quit", ( req, res ) -> { isOn = false; stop (); return "Service Stopped!"; } );

    // Configure provider
    post ( "/config", ( req, res ) ->
    {
      Map < String, String > params = null;
      try { params = ( new Gson () .fromJson ( req.body (), HashMap.class ) ); }
      catch ( Exception e ) { System.out.println ( e.getMessage () ); }

      // Validate important parameters
      if ( params == null )
        return "{\n\t\"status\": \"failure\",\n\t\"success\": \"false\",\n\t" + 
                                  "\"msg\": \"/config call failed due to parameters!\"\n}";

      // Set new parameters values arrived with call instead of default ones
      if ( params.containsKey ( "dirToMonitor" ) )  dirToMonitor = params.get ( "dirToMonitor" );
      if ( params.containsKey ( "pollInterval" ) )  pollInterval = Integer.parseInt ( params.get ( "pollInterval" ) );
      if ( params.containsKey ( "dbHost" ) )  dbHost = params.get ( "dbHost" );
      if ( params.containsKey ( "dbPort" ) )  dbPort = Integer.parseInt ( params.get ( "dbPort" ) );
      if ( params.containsKey ( "dbName" ) )  dbName = params.get ( "dbName" );
      if ( params.containsKey ( "dbLogin" ) )  dbLogin = params.get ( "dbLogin" );
      if ( params.containsKey ( "dbPassword" ) )  dbPassword = params.get ( "dbPassword" );
      
System.out.println ("dirToMonitor -> " + dirToMonitor  ); //!!!    
System.out.println ("pollInterval -> " + pollInterval  ); //!!!    
System.out.println ("dbHost -> " + dbHost  ); //!!!    
System.out.println ("dbPort -> " + dbPort  ); //!!!    
System.out.println ("dbName -> " + dbName  ); //!!!    
System.out.println ("dbLogin -> " + dbLogin  ); //!!!    
System.out.println ("dbPassword -> " + dbPassword  ); //!!!    

      return STATUS_OK;
    } );

    // Process trigger registering
    put ( "/register_trigger", ( req, res ) -> 
    {
      Map < String, String > params = null;
      try { params = ( new Gson () .fromJson ( req.body (), HashMap.class ) ); }
      catch ( Exception e ) { System.out.println ( e.getMessage () ); }
      
      // Validate important parameters
      if ( params == null || params.isEmpty () )
        return "{\n\t\"status\": \"failure\",\n\t\"success\": \"false\",\n\t" + 
                                  "\"msg\": \"/register_trigger call failed due to parameters!\"\n}";

      if ( params.containsKey ( "triggerHost" ) )  triggerHost = params.get ( "triggerHost" );
      if ( params.containsKey ( "triggerName" ) )  triggerName = params.get ( "triggerName" );
      else  return "{\n\t\"status\": \"failure\",\n\t\"success\": \"false\",\n\t" + 
                          "\"msg\": \"/register_trigger call failed due to missing trigger name!\"\n}";

      triggerUrl = "https://" + triggerHost + "/api/v1/namespaces/guest/triggers/" + triggerName;
     
System.out.println ("triggerHost -> " + triggerHost  ); //!!!    
System.out.println ("triggerName -> " + triggerName  ); //!!!    
System.out.println ("triggerUrl -> " + triggerUrl  ); //!!!    

      try   //TODO: Remove after debugging! 
      {
        // Startup directory polling
        poller = new Thread ( new DirPoller (), "XML-Provider-Dir-Poller" );

      } catch (Exception e) { System.err.println ( e.getMessage () ); } //TODO: Remove after debugging!

      // Set busy flag and start the polling thread
      isOn = true;
      poller.start ();
      
      // Operation succeeded
      return STATUS_OK;
    } );
    
    // Add MIME type header to every call
    after ( ( req, res ) -> { res.type ( "application/json" ); } );
  }

  
//---------------------------------------- Directory polling thread ------------------------------------------------

  /**
   * Inner static class which polls dedicated directory for incoming XML files, stores the  
   * @author dimos
   */
  private static final class DirPoller implements Runnable
  {
    File dir = new File ( dirToMonitor );
//    File xml = null;

    @Override
    public void run ()
    {
      try
      {
System.out.println ( "Polling thread started" ); //!!!   
        while ( isOn == true )
        {
          System.out.println ( "Polling directory '" + dirToMonitor + "'" );
          System.out.print ( '.' ); //!!!
          if ( ! isPaused )
          {
            File [] fileList = dir.listFiles ();
            for ( File file : fileList )
            {
              // Looking for XML file
              if ( file.getName () .toLowerCase () .endsWith ( ".xml" ) )
              {
 //               xml = file;
                
                // Store discovered XML in CouchDB, first...
                Map docParams = uploadXmlToCouchDB ( file );
                if ( docParams == null )
                {
                  System.err.println ( "DirPoller::uploadXmlToCouchDB returned null unexpectedly!" );
                  isBusy = false; isOn = false;
                  stop ();
                }
                
                // ... then fire the registered OpenWhisk trigger
                fireTrigger ( docParams );
                
                break;
              }
            }
          }
          Thread.sleep ( pollInterval );
        }
      } 
      catch ( InterruptedException | IOException | NoSuchAlgorithmException  e )
      { System.err.println ( e.getMessage () ); }
    }
    
    /**
     * Fires the trigger passing id and revision of document stored in CouchDB
     * @param params
     * @throws IOException 
     */
    private Map fireTrigger ( final Map params ) throws IOException
    {
      if ( params == null || params.containsKey ( "id" ) || params.containsKey ( "rev" ) )
      {
System.err.println ( params ); //!!!
        System.err.println ( "Feed::fireTrigger received bad 'params'!" );
        return null;
      }
     
      String response = CallRestService.put ( triggerUrl, "{\n\t\"id\":\"" + 
                                                ( String ) params.get ( "id" ) + 
                                                    "\",\n\t\"rev\":\"" + ( String ) params.get ( "rev" ) + "\"\n}" );
System.out.println ("CallRestService.put response -> " + response  ); //!!!

      return CallRestService.jsonToMap ( response );
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
      
      isBusy = true;
      
System.out.println ( "File found -> " + xmlToUpload.getCanonicalPath () ); //!!!
      
      response = CouchDB.createDocument ( dbHost, dbPort, dbName, dbLogin, dbPassword, null, "{\"current_processor\":\"XmlProvider\"}" );
      if ( response == null ) return null;
      jsonFields = CallRestService.jsonToMap ( response );
      
System.out.println ("CouchDB.createDocument response -> " + response  ); //!!!
System.out.println ("Map keys -> " + jsonFields.keySet () .toString ()  ); //!!!
System.out.println ("Map values -> " + jsonFields.values () .toString ()  ); //!!!

      String _id = ( String ) jsonFields.get ( "id" );
      String _rev = ( String ) jsonFields.get ( "rev" );
      
System.out.println ("_id -> " + _id  ); //!!!
System.out.println ("_rev -> " + _rev  ); //!!!
      
      response = CouchDB.createDocumentAttachment ( "127.0.0.1", 5984, "feed_files", "lambda_demo",
                                        "123456", _id, _rev, "xml", xmlToUpload.getCanonicalPath (), "application/xml" );
      jsonFields = CallRestService.jsonToMap ( response );

System.out.println ("CouchDB.createDocumentAttachment response -> " + response  ); //!!!

      xmlToUpload.delete ();
      isBusy = false;
      
      return jsonFields;
    }
  }


  
}
