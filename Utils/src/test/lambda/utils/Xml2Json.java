package test.lambda.utils;

import org.json.JSONObject;
import org.json.XML;

public final class Xml2Json
{

  public static String convertXml2Json ( final String xml )
  {
    // Converting XML to JSON
    JSONObject obj = XML.toJSONObject ( xml );
    
    return obj.toString ();
  }

}
