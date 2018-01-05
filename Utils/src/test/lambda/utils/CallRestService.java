package test.lambda.utils;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class CallRestService
{
  private static final Logger log = LoggerFactory.getLogger ( CallRestService.class );

  private static MediaType JSON = null;
  private static OkHttpClient client = null;


  /**
   * Calls REST GET command
   * @param url - URL of resource.
   * @return Result of the call.
   * @throws IOException
   */
  public static JsonObject get ( final String url ) throws IOException
  {
    Request request = new Request.Builder ()
        .url ( url )
        .build ();

    if ( client == null ) client = new OkHttpClient ();
   
    log.debug ( "GET request: " + request );
    
    try ( Response response = client .newCall ( request ) .execute () )
    { 
      log.info ( "Called: GET " + url );
      return decodeResponse ( response, false ); 
    }
  }


  /**
   * Calls REST POST command.
   * @param url - URL of resource.
   * @param login - payload Data that should be transfered in request body.
   * @param password - Login for authentication at the resource.
   * @param payload - Password for authentication at the resource.
   * @returnResult of the call.
   * @throws IOException
   */
  public static JsonObject post ( final String url, final String login, final String password, final String payload ) 
                                                                                                      throws IOException
  {
    if ( JSON == null ) JSON = MediaType.parse ( "application/json; charset=utf-8" );

    RequestBody body = RequestBody.create ( JSON, payload );
    Request request = new Request.Builder ()
        .url ( url )
        .addHeader ( "Authorization", Credentials.basic ( login, password ) )
        .post ( body )
        .build ();

    if ( client == null ) client = new OkHttpClient ();

    log.debug ( "POST request: " + request );

    try ( Response response = client .newCall ( request ) .execute () )
    { 
      log.info ( "Called: POST " + url );
      return decodeResponse ( response, false ); }
  }


  /**
   * Calls REST PUT command.
   * @param url - URL of resource.
   * @param - payload Data that should be transfered in request body.
   * @return Result of the call.
   * @throws IOException
   */
  public static JsonObject put ( final String url, final String payload ) throws IOException
  {
    if ( JSON == null ) JSON = MediaType.parse ( "application/json; charset=utf-8" );

    RequestBody body = RequestBody.create ( JSON, payload );
    Request request = new Request.Builder ()
        .url ( url )
        .put ( body )
        .build ();

    if ( client == null ) client = new OkHttpClient ();

    log.debug ( "PUT request: " + request );

    try ( Response response = client .newCall ( request ) .execute () )
    { 
      log.info ( "Called: PUT " + url );
      return decodeResponse ( response, false ); 
    }
  }


  /**
   * Calls REST GET command to download the attachment.
   * @param url
   * @param docRevision
   * @param attachmentResouceName
   * @return
   * @throws IOException
   */
  public static JsonObject getWithAttachment ( final String url, final String docRevision,
                                                      final String attachmentResouceName ) throws IOException
  {
    Request request = new Request.Builder ()
        .url ( url + "/" + attachmentResouceName + "?rev=" + docRevision +"&attachments=true" )
        .build ();

    if ( client == null ) client = new OkHttpClient ();

    log.debug ( "GET request: " + request );

    try ( Response response = client .newCall ( request ) .execute () )
    { 
      log.info ( "Called: GET " + url );
      return decodeResponse ( response, true );
    }
  }


 /**
   * Calls REST PUT command to upload the XML file.
   * @param url - URL of document resource.
   * @param docRevision - Updated document's revision
   * @param fileResouceName - Name of resource that will be assigned to uploaded file
   * @param pathToFile - Full path to the XML file being uploaded
   * @return Result of the call.
   * @throws IOException
   */
  public static JsonObject putWithXmlFile ( final String url, final String docRevision,
                                                final String fileResouceName, final String pathToFile ) throws IOException
  {
    RequestBody body = RequestBody.create ( 
                            MediaType.parse ( "application/xml; charset=utf-8" ), new File ( pathToFile ) );
    
    Request request = new Request.Builder ()
        .url ( url + "/" + fileResouceName + "?rev=" + docRevision )
        .put ( body )
        .build ();
    
    log.debug ( "PUT request: " + request );

    if ( client == null ) client = new OkHttpClient ();
    
    try ( Response response = client .newCall ( request ) .execute () )
    { 
      log.info ( "Called: PUT " + url );
      return decodeResponse ( response, false );
    }
  }

  
  /**
   * Calls REST PUT command to upload the JSON attachment.
   * @param url - URL of document resource.
   * @param docRevision - Updated document's revision
   * @param fileResouceName - Name of resource that will be assigned to uploaded file
   * @param jsonAttachment - JSON document as a string being uploaded
   * @return Result of the call.
   * @throws IOException
   */
  public static JsonObject putWithJsonAttachment ( final String url, final String docRevision,
                                                final String jsonResouceName, final String jsonAttachment ) throws IOException
  {
    RequestBody body = RequestBody.create( MediaType.parse ( "application/json; charset=utf-8" ), jsonAttachment );
    
    Request request = new Request.Builder ()
        .url ( url + "/" + jsonResouceName + "?rev=" + docRevision )
        .put ( body )
        .build ();
    
    log.debug ( "PUT request: " + request );

    if ( client == null ) client = new OkHttpClient ();
    
    try ( Response response = client .newCall ( request ) .execute () )
    {
      log.info ( "Called: PUT " + url );
      return decodeResponse ( response, false );
    }
  }

  
//  /**
//   * Calls REST PUT command to upload the file.
//   * @param url - URL of document resource.
//   * @param docRevision - Updated document's revision
//   * @param fileResouceName - Name of resource that will be assigned to uploaded file
//   * @param pathToFile - Full path to the file being uploaded
//   * @param mimeType - MIME type of file being uploaded
//   * @return Result of the call.
//   * @throws IOException
//   */
//  public static JsonObject putWithFile ( final String url, final String docRevision, final String fileResouceName,
//                                                   final String pathToFile, final String mimeType ) throws IOException
//  {
//    File attachment = new File ( pathToFile );
//
//    RequestBody body = new MultipartBody.Builder ()
//        .setType ( MultipartBody.FORM )
//        .addFormDataPart ( fileResouceName, attachment.getName () ,
//          RequestBody.create ( MediaType.parse ( mimeType ), new File ( pathToFile ) ) ) 
//        .build();
//
//    Request request = new Request.Builder ()
//        .url ( url + "/" + fileResouceName + "?rev=" + docRevision )
//        .put ( body )
//        .build ();
//    
//System.out.println ( "Request: " + request.toString () );  //!!!
//System.out.println ( "Headers: "  + request.headers () .toString () ); //!!!
//
//    if ( client == null ) client = new OkHttpClient ();
//    
//    try ( Response response = client .newCall ( request ) .execute () )
//    { return decodeResponse ( response ); }
//
//  }

  
  /**
   * Analyzes OkHttp Response object and returns appropriate Gson JsonObject result.
   * @param response - JSON reply from the REST service as a string.
   * @param isPlain - Should be <i>true</i> if response is plain text, not JSON
   * @return Success or failure JsonObject with with REST response body packed into 'value' field.
   * @throws IOException 
   */
  private static JsonObject decodeResponse ( final Response response, boolean isPlain ) throws IOException
  {
    String body = null;
    JsonObject result = null;
    JsonObject value = null;
    
    log.debug ( "CallRestService::decodeResponse() called" );
    
    // Result processing depends on whether it is plain text or JSON
    if ( isPlain == true )
    {
      log.debug ( "Plain text value asked" );
      
      value = new JsonObject ();
      String xml = new String ( response .body () .bytes () ); //???
      
      log.debug ( "XML out of body bytes: " + xml );

      value.addProperty ( "value",  xml );
      //        value.addProperty ( "value",  response .body () .string () );
    }
    else
    {
      log.debug ( "JSON value asked" );
      
      body = response .body () .string ();

      log.debug ( "JSON out of body: " + body );

      value = Convert.jsonToJsonObject ( body );
    }
    
    // Code result depending on success or failure
    if ( response.isSuccessful () )
    {
      log.debug ( "CallRestService::decodeResponse - success" );
      log.debug ( "CallRestService::decodeResponse - value:" + value );

      result = Result.success ( value );
    }
    else
    {
      log.debug ( "CallRestService::decodeResponse - failure" );
      log.debug ( "CallRestService::decodeResponse - value:" + value );
      
      result = Result.failure ( value );
    }

    log.debug ( "Leaving the CallRestService::decodeResponse" );

    return result;
  }


//  private static JsonObject decodeResponse ( final Response response, boolean isPlain ) throws IOException
//  {
//    String body = null; //???
//    JsonObject result = null;
//    JsonObject value = null;
//    
//System.out.println ( "Inside the CallRestService::decodeResponse" );//!!!
//System.out.println ( "response code -> " + response.code () );//!!!
//     
//    if ( response.isSuccessful () )
//    {
//System.out.println ( "CallRestService::decodeResponse - success" );//!!!
//      if ( isPlain == true )
//      {
//        value = new JsonObject ();
//        String xml = new String ( response .body () .bytes () ); //???
//System.out.println ( "XML out of bytes -> "  + xml ); //!!!
//        value.addProperty ( "value",  xml );
//        //        value.addProperty ( "value",  response .body () .string () );
//      }
//      else
//      {
//        body = response .body () .string ();
//        value = Convert.jsonToJsonObject ( body );
//      }
//
//System.out.println ( "CallRestService::decodeResponse - value:" + value );//!!!
//      result = Result.success ( value );
//    }
//    else
//    {
//System.err.println ( "CallRestService::decodeResponse - failure" );//!!!
//      if ( isPlain == true )
//      {
//        value = new JsonObject ();
//        String xml = new String ( response .body () .bytes () ); //???
//System.out.println ( "XML out of bytes -> "  + xml ); //!!!
//        value.addProperty ( "value",  xml );
//        //        value.addProperty ( "value",  response .body () .string () );
//      }
//      else
//      {
//        body = response .body () .string ();
//        value = Convert.jsonToJsonObject ( body );
//      }
//System.out.println ( "CallRestService::decodeResponse - value:" + value );//!!!
//      result = Result.failure ( value );
//    }
//
//System.out.println ( "Leaving the CallRestService::decodeResponse" );//!!!
//    
//    return result;
//  }


}
