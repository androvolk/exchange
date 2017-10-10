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

import test.lambda.utils.CallRestService;
import test.lambda.utils.CouchDB;


public final class XmlProvider
{
  private static final String STATUS_OK = "{\n\t\"status\": \"success\"\n\t\"success\": \"true\"\n}";
  
  private static String triggerName = null;
  private static String triggerUrl = null;
  private static String dirToMonitor = null;
  private static int pollInterval = 5000;
  private static String pollIntervalParam = null;
  private volatile static boolean isOn = false;
  private volatile static boolean isPaused = false;
  private volatile static boolean isBusy = false;
  private static Thread poller = null;
  
  
  public XmlProvider ()
  {
    port ( 8080 );
  }
  
 
  public static void main ( String [] args ) throws RuntimeException
  {
    new XmlProvider ();
    
    get ( "/hello", ( req, res ) -> "Hello World" );
    get ( "/service", ( req, res ) -> "{result: [ \"REST is cool!\", \"Well... Not that much!\" ] }" );
    post ( "/quit", ( req, res ) -> { isOn = false; stop (); return "Service Stopped!"; } );

    // Process trigger registering
    put ( "/register_trigger", ( req, res ) -> {
      Map < String, String > params = null;
      try { params = ( new Gson () .fromJson ( req.body (), HashMap.class ) ); }
      catch ( Exception e ) { System.out.println ( e.getMessage () ); }
      
      // Validate important parameters
      if ( params == null || params.size () < 3 ||
         ( params.get ( "triggerName" ) == null ) ||
         ( params.get ( "triggerUrl" ) == null ) ||
         ( params.get ( "dirToMonitor" ) == null ) )
      {
        // Some parameters missed
        return "{\n\t\"status\": \"failure\"\n\t\"success\": \"false\"\n\t\"msg\": " +
               "\"Check /register_trigger parameters!\"\n\t\"params: \"" + params.toString () + "\"\n}";
      }

      triggerName = params.get ( "triggerName" );
      triggerUrl = params.get ( "triggerUrl" );
      dirToMonitor = params.get ( "dirToMonitor" );
      pollIntervalParam = params.get ( "pollInterval" );
      if ( pollIntervalParam != null ) pollInterval = Integer.parseInt ( pollIntervalParam );
      
System.out.println ("triggerName -> " + triggerName  ); //!!!    
System.out.println ("triggerUrl -> " + triggerUrl  ); //!!!    
System.out.println ("dirToMonitor -> " + dirToMonitor  ); //!!!    
System.out.println ("pollIntervalParam -> " + pollIntervalParam  ); //!!!

      try {  //TODO: Remove after debugging!
        
      // Startup directory polling
      poller = new Thread ( new Runnable () {
        File dir = new File ( dirToMonitor );
        File xml = null;
        @Override
        public void run ()
        {
          try
          {
System.out.println ( "Polling thread started" ); //!!!   
            while ( isOn == true )
            {
              System.out.print ( '.' ); //!!!
              if ( ! isPaused )
              {
                File [] fileList = dir.listFiles ();
                for ( File file : fileList )
                {
                  // Looking for XML file
                  if ( file.getName () .toLowerCase () .endsWith ( ".xml" ) )
                  {
                    isBusy = true;
                    xml = file;
                    
                    System.out.println ( "File found -> " + xml.getCanonicalPath () ); //!!!
                    // TODO: Move hard-coded params to trigger params
                    String doc = CouchDB.createDocument ( "127.0.0.1", 5984, "feed_files", "lambda_demo", "123456", null, "{\"current_processor\":\"XmlProvider\"}" );
                    Map docFields = CallRestService.jsonToMap ( doc );
                    System.out.println ("doc -> " + doc  ); //!!!
                    System.out.println ("Map keys -> " + docFields.keySet () .toString ()  ); //!!!
                    System.out.println ("Map values -> " + docFields.values () .toString ()  ); //!!!
                    String _id = ( String ) docFields.get ( "id" );
                    String _rev = ( String ) docFields.get ( "rev" );
                    System.out.println ("_id -> " + _id  ); //!!!
                    System.out.println ("_rev -> " + _rev  ); //!!!
                    String attachment = CouchDB.createDocumentAttachment ( "127.0.0.1", 5984, "feed_files", "lambda_demo",
                                "123456", _id, _rev, "xml", xml.getCanonicalPath (), "application/xml" );
                    System.out.println ("response -> " + attachment  ); //!!!
                    // TODO: Process file here
                    
                    
                    xml.delete ();
                    isBusy = false; 
                    break;
                  }
                }
              }
              Thread.sleep ( pollInterval );
            }
          } 
          catch ( InterruptedException | IOException | NoSuchAlgorithmException  e ) { System.err.println ( e.getMessage () ); }
        } }, "XML-Provider-Dir-Poller" );

      } catch (Exception e) { System.err.println ( e.getMessage () ); } //TODO: Remove after debugging!

      // Set busy flag and start the polling thread
      isOn = true;
      poller.start ();
      
      // Operation succeeded
      return STATUS_OK;
    } );
    
    after ( ( req, res ) -> { res.type ( "application/json" ); } );
  }
  
}



//FilenameFilter filter = new FilenameFilter() {
//@Override
//public boolean accept ( File dir, String name )
//{
//return ( ( name.toLowerCase () .endsWith ( ".xml" ) ) ? true : false );
//}
//};
