package test.lambda.openwhisk.providers.xml;

import static spark.Spark.port;
import static spark.Spark.get;
import static spark.Spark.put;
import static spark.Spark.post;
import static spark.Spark.stop;
import static spark.Spark.after;
import com.google.gson.Gson;
import java.io.File;
import java.util.HashMap;
import java.util.Map;



public final class XmlProvider
{
  private static final String STATUS_OK = "{\n\t\"status\": \"success\"\n\t\"success\": \"true\"\n}";
  
  private static String triggerName = null;
  private static String triggerUrl = null;
  private static String dirToMonitor = null;
  private static int pollInterval = 5000;
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
    get ( "/quit", ( req, res ) -> { stop (); return "Service Stopped!"; } );
    get ( "/service", ( req, res ) -> "{result: [ \"REST is cool!\", \"Well... Not that much!\" ] }" );
    post ( "/post_test", ( req, res ) -> "{\n\t\"Headers\": \"" + req.headers () + "\",\n\tBody: " + req.body ()  + "\"\n}" ); //!!!

    // Process trigger registering
    put ( "/register_trigger", ( req, res ) -> {
      //Gson gson = new Gson();
      //String body = req.body ();
      Map < String, String > params = null;
      try
      { 
//        @SuppressWarnings ( "unchecked" )
        params = ( new Gson () .fromJson ( req.body (), HashMap.class ) );
      }
      catch ( Exception e ) 
      {
        System.out.println ( e.getMessage () );
      }
      
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
System.out.print ( "===> 5" ); //!!!   
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
                    
                    // TODO: Process file here
                    System.out.println ( file.getName () ); 
                    
                    xml.delete ();
                    isBusy = false; 
                    break;
                  }
                }
              }
              Thread.sleep ( pollInterval );
            }
          } 
          catch ( InterruptedException e ) { System.err.println ( e.getMessage () ); }
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
