package test.lambda.openwhisk.feeds;

import java.io.IOException;

import com.google.gson.JsonObject;

import test.lambda.utils.Feed;
import test.lambda.utils.Result;

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
    
    System.out.println ( "LambdaXmlFeedOpenWhisk feed action called" ); //!!!
    
    lifecycleEvent = args .get ( "lifecycleEvent" ) .getAsString ();
    triggerName = args .get ( "triggerName" ) .getAsString (); //!!!
    
System.out.println ( "args -> " + args  ); //!!!
System.out.println ( "Feed lifecycle event -> " + lifecycleEvent ); //!!!
System.out.println ( "Trigger name -> " + triggerName ); //!!!
    

    try
    {
      if ( lifecycleEvent.equals ( "CREATE" ) )
      {
        result = Feed.create ( args );
System.out.println ( "LambdaXmlFeedOpenWhisk::main::lifecycleEvent.equals ( \"CREATE\" )  -> " + result ); //!!!
//        output = Feed.create ( args );
//        if ( output == null ) result = failureWithMessage ( "Feed::create returned null!" );
        if ( Result.isFailed ( result )) 
          System.out.println ( 
              "LambdaXmlFeedOpenWhisk::main - Unable to create feed! Reason: " + Result.getMessage ( result ) );
      }
      else if ( lifecycleEvent.equals ( "DELETE" ) )
      {
        result = Feed.delete ( args );
//        if ( output == null ) result = failureWithMessage ( "Feed::create returned null!" );
//        else result = success ( output );
        if ( Result.isFailed ( result )) 
          System.out.println ( 
              "LambdaXmlFeedOpenWhisk::main - Unable to delete feed! Reason: " + Result.getMessage ( result ) );
        
      }
      else result = Result.ignored ( "Unsupported life cycle event: '" + lifecycleEvent + "'");
      
    }
    catch ( IOException e )
    {
      System.err.println ( "Feed generated IOException! Reason: " + e.getMessage () );
//      result = failureWithMessage ( e.getMessage () );
      result = Result.failure (
                  "LambdaXmlFeedOpenWhisk::main - Feed generated IOException! Reason: " + e.getMessage () );
    }

    return result;
  }

  
//  /**
//   * Produces 'failure' reply
//   * @param message - message to insert in resulting JSON object
//   * @return failure JsonObject
//   */
//  private static JsonObject failureWithMessage ( final String message )
//  {
//    JsonObject result = new JsonObject ();
//    result.addProperty ( "status", "failure" );
//    result.addProperty ( "success", "false" );
//    result.addProperty ( "msg", message );
//    return result;
//  }
//
//  
//  /**
//   * Produces 'success' reply
//   * @param output - some output to insert in resulting JSON object
//   * @return success JsonObject
//   */
//  private static JsonObject success ( final String output )
//  {
//    JsonObject result = new JsonObject ();
//    result.addProperty ( "status", "success" );
//    result.addProperty ( "success", "true" );
//    result.addProperty ( "output", output );
//    return result;
//  }
//
//  
//  /**
//   * Produces 'ingored' reply
//   * Produces ignore JsonObject
//   * @return
//   */
//  private static JsonObject ignored ()
//  {
//    JsonObject result = new JsonObject ();
//    result.addProperty ( "status", "ignored" );
//    result.addProperty ( "success", "true" );
//    return result;
//  }

 

//public static void main ( String [] args )
//{
//  JsonObject in = new JsonObject ();
//  in. addProperty ( "xml", "<MyXml><String sarg = \"SARG\">Vava</String><ArrayOfInts><Int>123</Int><Int>456</Int><Int>789</Int></ArrayOfInts></MyXml>" );
//  
//  JsonObject out = LambdaXml2JsonOpenWhisk.main ( in );
//  System.out.println ( out );
//}

}
