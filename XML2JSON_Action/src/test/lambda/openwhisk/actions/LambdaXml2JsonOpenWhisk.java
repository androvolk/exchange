package test.lambda.openwhisk.actions;

import com.google.gson.JsonObject;

import test.lambda.utils.Xml2Json;

public final class LambdaXml2JsonOpenWhisk
{

  public static JsonObject main ( JsonObject args )
  {

    String xml = null;
    String json = null;
    JsonObject result = null;
    
    // Getting 'xml' parameter out of the call's input
    if ( ! args.has ( "xml" ) ) System.err.println ( "The 'xml' parameter missing!" );
    else
    {
      xml = args .get ( "xml" ) .getAsString ();
      if ( xml == null ) 
      { 
        System.err.println ( "Unable to extract 'xml' parameter or it is null!" );
        return null; 
      }
    }
    
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
