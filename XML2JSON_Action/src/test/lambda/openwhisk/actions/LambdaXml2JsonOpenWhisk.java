package test.lambda.openwhisk.actions;

import com.google.gson.JsonObject;

import test.lambda.utils.Convert;
import test.lambda.utils.MissedParams;
import test.lambda.utils.Result;
import test.lambda.utils.Xml2Json;

public final class LambdaXml2JsonOpenWhisk
{

  public static JsonObject main ( JsonObject args )
  {
    String id = null;
    String rev = null;
    String xml = null;
    String json = null;
    JsonObject result = null;
    MissedParams missedParams = new MissedParams (); 

    if ( args .has ( "id" )) id = args .get ( "id" ) .getAsString ();
    else missedParams.add ( "id" );
    if ( args .has ( "rev" )) rev = args .get ( "rev" ) .getAsString ();
    else missedParams.add ( "rev" );
    
    if ( missedParams.hasMissedParameters () )
         return  Result.failure ( "LambdaXml2JsonOpenWhisk - some parameters are missing: ( " + missedParams + " )" );

System.out.println ( "Xml2Json id -> " + id);//!!
System.out.println ( "Xml2Json rev -> " + rev);

    // Getting XML from CouchDB by it's id and revision



//    // Getting 'xml' parameter out of the call's input
//    if ( ! args.has ( "xml" ) ) System.err.println ( "The 'xml' parameter missing!" );
//    else
//    {
//      xml = args .get ( "xml" ) .getAsString ();
//      if ( xml == null ) 
//      { 
//        System.err.println ( "Unable to extract 'xml' parameter or it is null!" );
//        return null; 
//      }
//    }
    
    // Passing the xml to the global converter
    json = Xml2Json.convertXml2Json ( xml );
    if ( json == null ) System.err.println ( "Converter returned null!" );
    
    // Forming resulting JSON object with 'result' element having JSON-ified XML as its value 
    result = new JsonObject ();
    result.addProperty ( "result", json );
//    result.addProperty ( "# of elements", Integer.valueOf ( args.entrySet ().size () ) ); //!!!
    
    return args /*result*/;

  }

//  public static void main ( String [] args )
//  {
//    JsonObject in = new JsonObject ();
//    in. addProperty ( "xml", "<MyXml><String sarg = \"SARG\">Vava</String><ArrayOfInts><Int>123</Int><Int>456</Int><Int>789</Int></ArrayOfInts></MyXml>" );
//    
//    JsonObject out = LambdaXml2JsonOpenWhisk.main ( in );
//    System.out.println ( out );
//  }

}
