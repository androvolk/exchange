package test.lambda.utils;

import java.io.IOException;
//import java.nio.file.Files;
//import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
//import java.util.Base64;
//import java.util.List;
//import java.util.Map;

//import javax.xml.bind.DatatypeConverter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class CouchDB
{
//  private final static String HOST = "127.0.0.1";
//  private final static String PORT = "5984";
//  private final static String DB = "feed_files";
//  private final static String LOGIN = "lambda_demo";
//  private final static String PASSWORD = "123456";
  
  /**
   * Generate URL for calling CouchDB REST API
   * @param host
   * @param port
   * @param dataBase
   * @param resource
   * @param login
   * @param password
   * @return Valid URL to call the API
   */
  public static String makeUrl ( final String host, final int port, final String dataBase, 
//  public static String makeUrl ( final String host, final String port, final String dataBase, 
                                            final String resource, final String login, final String password )
  {
//    String result = null;
    String prefix = "";
    String infix = "";
    
    if ( login != null && password != null ) prefix = login + ':' + password + '@';
    if ( dataBase != null) infix = "/" + dataBase;
    
    return "http://" + prefix + host + ":" + port + infix + resource; 

//    return result;
  }


  /**
   * Asks CouchDB for next available UUID. May be used as <b>_id</b> for documents 
   * @param host
   * @param port
   * @param login
   * @param password
   * @return
   * @throws IOException
   */
  public static String getUuid ( final String host, final int port, final String login, final String password )
//  public static String getUuid ( final String host, final String port, final String login, final String password )
                                                                                                      throws IOException
  {
    JsonObject response = null;
//    @SuppressWarnings ( "rawtypes" )
//    Map response = null;
    
    response = CallRestService.get (  makeUrl ( host, port, null, "/_uuids", login, password ) );
//    response = Convert.jsonToMap ( CallRestService.get (  makeUrl ( host, port, null, "/_uuids", login, password ) ) );
    if ( Result.isFailed ( response ) ) return null;
System.out.println ( "uuids response -> " + response );//!!!
    JsonArray uuids = Result.getValue ( response ) .get ( "uuids" ) .getAsJsonArray ();
System.out.println ( "uuids = " + uuids );//!!!
//    @SuppressWarnings ( "unchecked" )
//    List < String > uuids = ( List < String > ) response.get ( "uuids" );
    if ( uuids == null )
    {
      System.err.println ( "CouchDB::getUuid failed to provide the UUID! REST returned: " + Result.getValue ( response ) );
      return null;
    }

    return uuids. get ( 0 ) .getAsString ();
//    return uuids.get ( 0 );
  }


  /**
   * Asks CouchDB to create new document. 
   * @param host
   * @param port
   * @param dataBase
   * @param login
   * @param password
   * @param id If not null then used as unique <b>id</b> for document. Otherwise UUID will be generated an used instead. 
   * @param jsonDocument JSON document in string form to store in database.
   * @return
   * @throws IOException
   */
  public static JsonObject createDocument (  final String host, final int port, final String dataBase,
//  public static String createDocument (  final String host, final String port, final String dataBase,
                                         final String login, final String password, final String id, final String jsonDocument )
                                                                                                             throws IOException
  {
//    JsonObject result = null;
//    String result = null;
    JsonObject response = null;
//    String response = null;
    String uuid = null;
    
    if ( id == null )
    {
      uuid = getUuid ( host, port, login, password );
      if ( uuid == null ) return Result.failure ( 
                    "CouchDB::createDocument - getUuide() was unable go generate ID for document!" );
    }
    
    
    response = CallRestService.put ( makeUrl ( 
//      return CallRestService.put ( makeUrl ( 
                    host, port, dataBase, "/" + ( id != null ? id : uuid ) , login, password ), jsonDocument );

//    result = Convert.jsonToJsonObject ( response );
    
    if ( Result.isFailed ( response ) )
//    if ( Result.isFailed ( result ) )
    {
      String msg = "CouchDB::createDocument failed to create new document! REST returned: " + 
                                                    Convert.jsonObjectToJson ( Result .getValue ( response ) ); 
      System.err.println ( msg );
      return Result.failure ( msg );
    }
    else return response;
  }

  
  /**
   * Asks CouchDB to attach file to existing document with particular ID and revision. 
   * @param host
   * @param port
   * @param dataBase
   * @param login
   * @param password
   * @param id
   * @param docRevision
   * @param fileResouceName
   * @param fullPathToFile
   * @param mimeType
   * @return
   * @throws IOException
   * @throws NoSuchAlgorithmException
   */
  public static JsonObject uploadAttachment ( final String host, final int port, final String dataBase,
//  public static String uploadAttachment ( final String host, final String port, final String dataBase,
                                         final String login, final String password, final String id, final String docRevision,
                                         final String fileResouceName, final String fullPathToFile, final String mimeType )
                                                                                   throws IOException, NoSuchAlgorithmException
  {
//    String result = null;
    JsonObject response = null;

    response = CallRestService.putWithFile ( makeUrl ( host, port, dataBase, "/" + id , login, password ),
//    return CallRestService.putWithFile ( makeUrl ( host, port, dataBase, "/" + id , login, password ),
                                                          docRevision, fileResouceName, fullPathToFile, mimeType );
    if ( Result.isFailed ( response ) )
//    if ( result == null )
    {
      String msg = "CouchDB::uploadAttachment failed to add attachment! REST returned: " + 
                                                    Convert.jsonObjectToJson ( Result .getValue ( response ) ); 
      System.err.println ( msg );
      return Result.failure ( msg );
    }
    else return response;

//    return result;
  }
 
  
  /**
   * 
   * @param host
   * @param port
   * @param dataBase
   * @param login
   * @param password
   * @param id
   * @param docRevision
   * @param fileResouceName
   * @param fullPathToFile
   * @param mimeType
   * @return
   * @throws IOException
   * @throws NoSuchAlgorithmException
   */
  public static JsonObject downloadAttachment ( final String host, final int port, final String dataBase,
                                           final String login, final String password, final String id, final String docRevision,
                                           final String fileResouceName, final String fullPathToFile, final String mimeType )
                                                                                     throws IOException, NoSuchAlgorithmException
    {
      JsonObject response = null;

      response = CallRestService.putWithFile ( makeUrl ( host, port, dataBase, "/" + id , login, password ),
                                                            docRevision, fileResouceName, fullPathToFile, mimeType );
      if ( Result.isFailed ( response ) )
      {
        String msg = "CouchDB::downloadAttachment failed to get attachment! REST returned: " + 
                                                      Convert.jsonObjectToJson ( Result .getValue ( response ) ); 
        System.err.println ( msg );
        return Result.failure ( msg );
      }
      else return response;
    }
   
    
//  public static void main ( String [] args ) throws Exception
//  {
//    String uuid = getUuid ( HOST, PORT, LOGIN, PASSWORD );
//    
//    if ( uuid != null ) System.out.println ( "UUID from the CouchDB: " + uuid );
//    else System.err.println ( "Unable to get UUID from CouchDB!" );
//
////    String result = createDocument ( HOST, PORT, DB, resource, login, password, id, jsonDocument );
//
//    String result = createDocumentAttachment ( HOST, PORT, DB, LOGIN, PASSWORD, "300fce2c6360d9c33f71e2b0eb015113",
//        "2-5949068b7035e4bfc358c2cb0074cfbf", "xml", "/home/vagrant/demos/DJ/exchange/Build/dist/test.xml", "application/xml");
//    System.out.println ( "Result ->  " + result);
//    
//  }

}
