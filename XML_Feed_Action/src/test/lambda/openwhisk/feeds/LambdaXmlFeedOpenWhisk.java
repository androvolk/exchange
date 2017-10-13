package test.lambda.openwhisk.feeds;

import java.io.IOException;

import com.google.gson.JsonObject;

import test.lambda.utils.Feed;

public final class LambdaXmlFeedOpenWhisk
{
  
  /**
   * Feed action entry point
   * @param args - set of parameters in Gson JsonObject form. 
   * @return Set of properties in Gson JsonObject form.
   */
  public static JsonObject main ( JsonObject args )
  {

    String xml = null;
    String output = null;
    String lifecycleEvent = null;
    String triggerName = null;
    JsonObject result = null;
    
    System.out.println ( "LambdaXmlFeedOpenWhisk feed acction called" ); //!!!
    
    lifecycleEvent = args .get ( "lifecycleEvent" ) .getAsString ();
    triggerName = args .get ( "triggerName" ) .getAsString (); //!!!
    
System.out.println ( "args -> " + args  ); //!!!
System.out.println ( "Feed lifecycle event -> " + lifecycleEvent ); //!!!
System.out.println ( "Trigger name -> " + triggerName ); //!!!
    

    try
    {
      // Getting 'xml' parameter out of the call's input
      if ( lifecycleEvent.equals ( "CREATE" ) )
      {
        output = Feed.create ( args );
        if ( output == null ) result = failureWithMessage ( "Feed::create returned null!" );
        else result = success ( output );
      }
      else if ( lifecycleEvent.equals ( "DELETE" ) )
      {
        output = Feed.delete ( args );
        if ( output == null ) result = failureWithMessage ( "Feed::create returned null!" );
        else result = success ( output );
      }
      else result = ignored ();
      
    }
    catch ( IOException e )
    {
      System.err.println ( "Feed::create generated IOException" );
      result = failureWithMessage ( e.getMessage () );
    }

    return result;
  }

  
  /**
   * Produces 'failure' reply
   * @param message - message to insert in resulting JSON object
   * @return failure JsonObject
   */
  private static JsonObject failureWithMessage ( final String message )
  {
    JsonObject result = new JsonObject ();
    result.addProperty ( "status", "failure" );
    result.addProperty ( "success", "false" );
    result.addProperty ( "msg", message );
    return result;
  }

  
  /**
   * Produces 'success' reply
   * @param output - some output to insert in resulting JSON object
   * @return success JsonObject
   */
  private static JsonObject success ( final String output )
  {
    JsonObject result = new JsonObject ();
    result.addProperty ( "status", "success" );
    result.addProperty ( "success", "true" );
    result.addProperty ( "output", output );
    return result;
  }

  
  /**
   * Produces 'ingored' reply
   * Produces ignore JsonObject
   * @return
   */
  private static JsonObject ignored ()
  {
    JsonObject result = new JsonObject ();
    result.addProperty ( "status", "ignored" );
    result.addProperty ( "success", "true" );
    return result;
  }

 

//public static void main ( String [] args )
//{
//  JsonObject in = new JsonObject ();
//  in. addProperty ( "xml", "<MyXml><String sarg = \"SARG\">Vava</String><ArrayOfInts><Int>123</Int><Int>456</Int><Int>789</Int></ArrayOfInts></MyXml>" );
//  
//  JsonObject out = LambdaXml2JsonOpenWhisk.main ( in );
//  System.out.println ( out );
//}

}
