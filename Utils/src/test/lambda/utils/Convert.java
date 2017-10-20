package test.lambda.utils;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public final class Convert
{
  private static Gson gson = null;
  private static JsonParser parser = null;

  /**
   * Converts JSON to set of Key-Value pairs packed into Map
   * @param json - JSON document
   * @return Map where key is JSON attribute name and value is a JSON value
   */
  @SuppressWarnings ( "rawtypes" )
  public static Map jsonToMap ( final String json ) 
  {
    if ( gson == null ) gson = new Gson ();
    return gson.fromJson ( json, HashMap.class );
  }
  
  
  /**
   * Converts JsonObjec instance to plain textual JSON
   * @param object - JsonObject describing JSON
   * @return Textual JSON reflecting JsonObject data
   */
  public static String jsonObjectToJson ( final JsonObject object )
  {
    if ( gson == null ) gson = new Gson ();
    return gson.toJson ( object );    
  }
  
  
  /**
   * Converts JSON to Gson JsonObject
   * @param json - JSON document
   * @return Gson JsonObject reflecting the JSON doc
   */
  public static JsonObject jsonToJsonObject ( final String json )
  {

System.out.println ( "Convert::jsonToJsonObject - " + json );//!!!

    if ( parser == null ) parser = new JsonParser ();
    return parser .parse ( json ) .getAsJsonObject ();
  }
  
}
