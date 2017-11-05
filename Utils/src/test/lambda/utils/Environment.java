package test.lambda.utils;

import java.util.Map.Entry;
import java.util.Set;

public final class Environment
{

  private Environment () {}

  /**
   * Reads current environment and dumps it in readable form.
   * @return Environment line by line as a String.
   */
  public static String dump ()
  {
    Set < Entry < String, String > > env = System.getenv () .entrySet ();
    
    StringBuilder result = new StringBuilder ();
    env .stream () .forEach ( s -> result.append ( String.format ( "\t%s : %s\n", s.getKey (), s.getValue () ) ) ); 
    
    return "{\n" + result.toString () + "}";
  }

}
