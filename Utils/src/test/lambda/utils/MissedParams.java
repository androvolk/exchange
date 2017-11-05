package test.lambda.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for storing missed parameters' names. 
 * Use <i>add()</i> to save name of missed parameter.
 * Use <i>thereAreMissedParameters()</i> to check if there are missed parameters
 * and <i>toString()</i> to obtain the whole list of them. 
 */
public final class MissedParams
{

  private List < String > params = null;
 

  public MissedParams () { params = new ArrayList <> (); }


  /**
   * Stores missed parameter's name.
   * @param paramName - name to save
   * @return <i>true</i> if operation succeeded otherwise <i>false</i>
   */
  public boolean add ( String paramName ) 
  { 
    if ( hasMissedParameters () )
      return params.add ( ", " + paramName );
    else 
      return params.add ( paramName );
  }


  /**
   * Checks up if there are missed parameters.
   * @return <i>true</i> if some parameters are missed <i>false</i>
   */
  public boolean hasMissedParameters () { return ( ! params.isEmpty () ); }


  /**
   * Returns how many parameters are missed.
   * @return Number of missed parameters
   */
  public int size () { return params.size ();  }


  @Override
  public String toString ()
  {
    if ( ! hasMissedParameters () ) return "No missed parameters! :)";
    
    StringBuilder result = new StringBuilder ();
    params .stream () .forEach ( s -> result.append ( s ) ); 

    return result.toString ();
  }

}

