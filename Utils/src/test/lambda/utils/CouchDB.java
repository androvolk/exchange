package test.lambda.utils;

import java.util.List;
import java.util.Map;

public final class CouchDB
{
  private final static String HOST = "127.0.0.1";
  private final static int PORT = 5984;
  private final static String DB = "feed_files";
  private final static String LOGIN = "lambda_demo";
  private final static String PASSWORD = "123456";
  
  public static String getUuid ()
  {
    String result = null;
    
    @SuppressWarnings ( "rawtypes" )
    Map response = CallRestService.requestJsonAsMap ( "GET", HOST, PORT, "/_uuids", LOGIN, PASSWORD, null );
    if ( response == null ) return null;
    @SuppressWarnings ( "unchecked" )
    List < String > uuids = ( List < String > ) response.get ( "uuids" );
    if ( uuids == null )
    {
      System.err.println ( "CouchDB::getUuid failed to provide the UUID!" );
      return null;
    }
    result = uuids.get ( 0 );

    return result;
  }
 
  public static String createDocumentWithFile ( final String id, final String jsonDocument,  final String fullPathToFile )
  {
    String result = null;
    String uuid = null;
    Map response = null;
    
    if ( id == null )
    {
      uuid = getUuid ();
      if ( uuid == null ) return null;
    }
    
    response = CallRestService.requestJsonAsMap (
                "PUT", HOST, PORT, "/" + DB + "/" + ( id != null ? id : uuid ), LOGIN, PASSWORD, jsonDocument );
    if ( response == null )
    {
      System.err.println ( "CouchDB::createDocumentWithFile failed to create new document!" );
      return null;
    }

//    result = CallRestService.requestJsonAsString (
//                 "PUT", HOST, PORT, "/" + DB + "/" + ( id != null ? id : uuid ), LOGIN, PASSWORD, jsonDocument );
System.out.println ( response.toString () ); //!!!
    return result;
  }
 
  public static void main ( String [] args )
  {
    String uuid = getUuid ();
    
    if ( uuid != null ) System.out.println ( "UUID from the CouchDB" + uuid );
    else System.err.println ( "Unable to get UUID from CouchDB!" );
    
   /* String json =*/ createDocumentWithFile ( null, "{\"shit\":\"fuck\", \"more_shit\":\"more_fuck\"}",  null );
  }

}
