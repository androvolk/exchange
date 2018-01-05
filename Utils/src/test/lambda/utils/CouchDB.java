package test.lambda.utils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class CouchDB
{
  private static final Logger log = LoggerFactory.getLogger ( CouchDB.class );

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
                                            final String resource, final String login, final String password )
  {
    String prefix = "";
    String infix = "";
    
    if ( login != null && password != null ) prefix = login + ':' + password + '@';
    if ( dataBase != null) infix = "/" + dataBase;
    
    return "http://" + prefix + host + ":" + port + infix + resource; 
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
                                                                                                      throws IOException
  {
    JsonObject response = null;
    
    response = CallRestService.get (  makeUrl ( host, port, null, "/_uuids", login, password ) );
    if ( Result.isFailed ( response ) ) return null;
    
    log.debug ( "uuids response: " + response );

    JsonArray uuids = Result.getValue ( response ) .get ( "uuids" ) .getAsJsonArray ();
    
    if ( uuids == null )
    {
      log.error ( "CouchDB::getUuid failed to provide the UUID! REST returned: " + Result.getValue ( response ) );
      return null;
    }

    return uuids. get ( 0 ) .getAsString ();
  }


  public static String getLastDocRevision ( final String host, final int port, final String dataBase,
                                                          final String login, final String password, final String id )
                                                                                                          throws IOException
  {
    JsonObject response = null;
    String uuid = null;
    
    response = CallRestService.get ( makeUrl ( 
                    host, port, dataBase, "/" + ( id != null ? id : uuid ) , login, password ) );

    if ( Result.isFailed ( response ) )
    {
      String msg = "CouchDB::getLastDocRevision failed to retrieve document information! REST returned: " + 
                                                    Convert.jsonObjectToJson ( Result .getValue ( response ) ); 
      log.error ( msg );
      return null;
    }
    else
    {
      log.debug ( "CouchDB::getLastDocRevision response: " + response );
      //TODO: Start here!!!
return "";//      return response.;
    }
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
  public static JsonObject createDocument ( final String host, final int port, final String dataBase,
                                         final String login, final String password, final String id, final String jsonDocument )
                                                                                                             throws IOException
  {
    JsonObject response = null;
    String uuid = null;
    
    if ( id == null )
    {
      uuid = getUuid ( host, port, login, password );
      if ( uuid == null )
      {
        String msg = "CouchDB::createDocument - getUuide() was unable go generate ID for document!";
        log.error ( msg );
        return Result.failure ( msg );
      }
    }
    
    response = CallRestService.put ( makeUrl ( 
                    host, port, dataBase, "/" + ( id != null ? id : uuid ) , login, password ), jsonDocument );

    if ( Result.isFailed ( response ) )
    {
      String msg = "CouchDB::createDocument failed to create new document! REST returned: " + 
                                                    Convert.jsonObjectToJson ( Result .getValue ( response ) ); 
      log.error ( msg );
      return Result.failure ( msg );
    }
    else
    {
      log.debug ( "CouchDB::createDocument response: " + response );
      return response;
    }
  }

  
  /**
   * Asks CouchDB to attach XML file to existing document with particular ID and revision. 
   * @param host
   * @param port
   * @param dataBase
   * @param login
   * @param password
   * @param id
   * @param docRevision
   * @param fileResouceName
   * @param fullPathToFile
   * @return
   * @throws IOException
   * @throws NoSuchAlgorithmException
   */
  public static JsonObject uploadXmlAttachment ( final String host, final int port, final String dataBase,
                                         final String login, final String password, final String id, final String docRevision,
                                         final String fileResouceName, final String fullPathToFile )
                                                                                   throws IOException, NoSuchAlgorithmException
  {
    JsonObject response = null;

    response = CallRestService.putWithXmlFile ( makeUrl ( 
                                                      host, port, dataBase, "/" + id , login, password ),
                                                                docRevision, fileResouceName, fullPathToFile );
    if ( Result.isFailed ( response ) )
    {
      String msg = "CouchDB::uploadXmlAttachment failed to add attachment! REST returned: " + 
                                                    Convert.jsonObjectToJson ( Result .getValue ( response ) ); 
      log.error ( msg );
      return Result.failure ( msg );
    }
    else
    {
      log.debug ( "CouchDB::uploadXmlAttachment response: " + response );
      return response;
    }
  }


  /**
   * Asks CouchDB to attach JSON to existing document with particular ID and revision. 
   * @param host
   * @param port
   * @param dataBase
   * @param login
   * @param password
   * @param id
   * @param docRevision
   * @param jsonResouceName
   * @param jsonAttachment
   * @return
   * @throws IOException
   * @throws NoSuchAlgorithmException
   */
  public static JsonObject uploadJsonAttachment ( final String host, final int port, final String dataBase,
                                         final String login, final String password, final String id, final String docRevision,
                                                     final String jsonResouceName, final String jsonAttachment )
                                                                                   throws IOException, NoSuchAlgorithmException
  {
    JsonObject response = null;

    response = CallRestService.putWithJsonAttachment ( makeUrl ( 
                                                      host, port, dataBase, "/" + id , login, password ),
                                                                docRevision, jsonResouceName, jsonAttachment );
    if ( Result.isFailed ( response ) )
    {
      String msg = "CouchDB::uploadJsonAttachment failed to add attachment! REST returned: " + 
                                                    Convert.jsonObjectToJson ( Result .getValue ( response ) ); 
      log.error ( msg );
      return Result.failure ( msg );
    }
    else
    {
      log.debug ( "CouchDB::uploadJsonAttachment response: " + response );
      return response;
    }
  }


  /**
   * Asks CouchDB to download attachment to existing document with particular ID and revision.
   * @param host
   * @param port
   * @param dataBase
   * @param login
   * @param password
   * @param id
   * @param docRevision
   * @param attachmentResouceName
   * @return
   * @throws IOException
   * @throws NoSuchAlgorithmException
   */
  public static JsonObject downloadXmlAttachment ( final String host, final int port, final String dataBase,
                                                     final String login, final String password, final String id,
                                                     final String docRevision, final String attachmentResouceName )
                                                                                  throws IOException, NoSuchAlgorithmException
    {
      JsonObject response = null;

      response = CallRestService.getWithAttachment ( makeUrl ( 
                      host, port, dataBase, "/" + id , login, password ), docRevision, attachmentResouceName );
      
      if ( Result.isFailed ( response ) )
      {
        String msg = "CouchDB::downloadXmlAttachment failed to get attachment! REST returned: " + 
                                                      Convert.jsonObjectToJson ( Result .getValue ( response ) ); 
        log.error ( msg );
        return Result.failure ( msg );
      }
      else
      {
        log.debug ( "CouchDB::downloadXmlAttachment response: " + response );
        return response;
      }
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
