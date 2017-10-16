package test.lambda.utils;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

public final class Convert
{
  private static Gson gson = null;

  /**
   * Converts JSON to set of Key-Value pairs packed into Map
   * @param json JSON document
   * @return Map where key is JSON attribute name and value is a JSON value
   */
  @SuppressWarnings ( "rawtypes" )
  public static Map jsonToMap ( final String json ) 
  {
    if ( gson == null ) gson = new Gson ();
    return gson.fromJson ( json, HashMap.class );
  }
  
}
