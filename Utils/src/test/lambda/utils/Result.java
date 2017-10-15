package test.lambda.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Class-factory generating <i>success</i>, <i>failure</i>, and <i>ignored</i> statuses.
 * Also, allows to check the status quickly or get attached message.
 */
public final class Result
{

  /**
   * Generates JsonObject with <i>success</i> status.
   * @param message - Message to add or <i>null</i> to skip message addition
   * @return Full-filed JsonObject 
   */
  public static JsonObject success ( final String message )
  {
    JsonObject result = new JsonObject ();
    
    result.addProperty ( "status", "success" );
    result.addProperty ( "success", "true" );
    if ( message != null ) result.addProperty ( "msg", message );

    return result;
  }
  
  
  /**
   * Generates JsonObject with <i>failure</i> status.
   * @param message - Message to add or <i>null</i> to skip message addition
   * @return Full-filed JsonObject 
   */
  public static JsonObject failure ( final String message )
  {
    JsonObject result = new JsonObject ();
    
    result.addProperty ( "status", "failure" );
    result.addProperty ( "success", "false" );
    if ( message != null ) result.addProperty ( "msg", message );

    return result;
  }


  /**
   * Generates JsonObject with <i>ignored</i> status.
   * @param message - Message to add or <i>null</i> to skip message addition
   * @return Full-filed JsonObject 
   */
  public static JsonObject ignored ( final String message )
  {
    JsonObject result = new JsonObject ();
    
    result.addProperty ( "status", "ignored" );
    result.addProperty ( "success", "true" );
    if ( message != null ) result.addProperty ( "msg", message );

    return result;
  }
  
  
  /**
   * Checks the status of result for sign of success.
   * @param result - JsonObject to check
   * @return <i>true</i> if result's status is 'success', <i>false</i> otherwise
   */
  public static boolean isSuccess ( final JsonObject result )
  {
    return result. get ( "status" ) .getAsString () .equals ( "success" );
  }


  /**
   * Checks the status of result for sign of failure.
   * @param result - JsonObject to check
   * @return <i>true</i> if result's status is 'failure', <i>false</i> otherwise
   */
  public static boolean isFailure ( final JsonObject result )
  {
    return result. get ( "status" ) .getAsString () .equals ( "failure" );
  }


  /**
   * Checks the status of result for sign of ignored operation.
   * @param result - JsonObject to check
   * @return <i>true</i> if result's status is 'ignored', <i>false</i> otherwise
   */
  public static boolean isIgnored ( final JsonObject result )
  {
    return result. get ( "status" ) .getAsString () .equals ( "ignored" );
  }


  /**
   * Checks if result marked as succeeded.
   * @param result - JsonObject to check
   * @return <i>true</i> if result indicates success <i>false</i> otherwise
   */
  public static boolean isSucceeded ( final JsonObject result )
  {
    return result. get ( "success" ) .getAsString () .equals ( "true" );
  }
  
  
  /**
   * Checks if result marked as failed.
   * @param result - JsonObject to check
   * @return <i>true</i> if result indicates failure <i>false</i> otherwise
   */
  public static boolean isFailed ( final JsonObject result )
  {
    return ( ! isSucceeded ( result ) );
  }
  
  
  /**
   * Returns message attached to result object.
   * @param result - JsonObject to get message from
   * @return Message attached to the result object, empty string otherwise
   */
  public static String getMessage ( final JsonObject result )
  {
    if ( result.has ( "msg" ) ) 
      return result. get ( "msg" ) .getAsString ();
    else
      return "";
  }


  /**
   * Converts JsonObject result object to JSON document
   * @param result - JsonObject result object
   * @return JSON representation of result
   */
  public static String toJson ( final JsonObject result )
  {
    Gson gson = new Gson ();
    return gson.toJson ( result );
  }
  
}
