package test.lambda.utils;

import java.io.File;
import java.io.IOException;

import com.google.gson.JsonObject;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class CallRestService
{
  private static MediaType JSON = null;
  private static OkHttpClient client = null;


  /**
   * Calls REST GET command
   * @param url - URL of resource.
   * @return Result of the call.
   * @throws IOException
   */
  public static JsonObject get ( final String url ) throws IOException
//  public static String get ( final String url ) throws IOException
  {
    Request request = new Request.Builder ()
        .url ( url )
        .build ();

    if ( client == null ) client = new OkHttpClient ();
    
    try ( Response response = client .newCall ( request ) .execute () )
//    { return response .body () .string (); }
    { return decodeResponse ( response ); }
    
    
  }


  /**
   * Calls REST PUT command.
   * @param url - URL of resource.
   * @param - payload Data that should be transfered in request body.
   * @return Result of the call.
   * @throws IOException
   */
  public static JsonObject put ( final String url, final String payload ) throws IOException
//  public static String put ( final String url, final String payload ) throws IOException
  {
    if ( JSON == null ) JSON = MediaType.parse ( "application/json; charset=utf-8" );

System.out.println ( "PUT URL -> " + url );//!!!
System.out.println ( "PUT payload  -> " + payload );//!!!    

    RequestBody body = RequestBody.create ( JSON, payload );
    Request request = new Request.Builder ()
        .url ( url )
        .put ( body )
        .build ();

    if ( client == null ) client = new OkHttpClient ();

System.out.println ( "PUT request  -> " + request );//!!!    

Call call = client .newCall ( request ); //!!!
System.out.println ( "call  -> " + call );//!!!    
Response response =  call.execute ();
System.out.println ( "call::execute response  -> " + response );//!!!    


/*    try ( Response response = client .newCall ( request ) .execute () )*/
//    { return response .body () .string (); }
    { return decodeResponse ( response ); }
  }


  /**
   * Calls REST PUT command to upload the file.
   * @param url - URL of document resource.
   * @param docRevision - Updated document's revision
   * @param fileResouceName - Name of resource that will be assigned to uploaded file
   * @param pathToFile - Full path to the file being uploaded
   * @param mimeType - MIME type of file being uploaded
   * @return Result of the call.
   * @throws IOException
   */
  public static JsonObject putWithFile ( final String url, final String docRevision, final String fileResouceName,
//  public static String putWithFile ( final String url, final String docRevision, final String fileResouceName,
                                               final String pathToFile, final String mimeType ) throws IOException
  {
    File attachment = new File ( pathToFile );

    RequestBody body = new MultipartBody.Builder ()
        .setType ( MultipartBody.FORM )
        .addFormDataPart ( fileResouceName, attachment.getName () ,
          RequestBody.create ( MediaType.parse ( mimeType ), new File ( pathToFile ) ) ) 
        .build();

    Request request = new Request.Builder ()
        .url ( url + "/" + fileResouceName + "?rev=" + docRevision )
        .put ( body )
        .build ();
    
System.out.println ( "Request: " + request.toString () );  //!!!
System.out.println ( "Headers: "  + request.headers () .toString () ); //!!!

    if ( client == null ) client = new OkHttpClient ();
    
    try ( Response response = client .newCall ( request ) .execute () )
//    { return response .body () .string (); }
    { return decodeResponse ( response ); }

  }

  
  /**
   * Analyzes OkHttp Response object and returns appropriate Gson JsonObject result.
   * @param response - JSON reply from the REST service as a string.
   * @return Success or failure JsonObject with with REST response body packed into 'value' field.
   * @throws IOException 
   */
  private static JsonObject decodeResponse ( final Response response ) throws IOException
  {
    JsonObject result = null;
    JsonObject value = null;
    
System.out.println ( "Inside the CallRestService::decodeResponse" );//!!!
System.out.println ( "response code -> " + response.code () );//!!!
    
    if ( response.isSuccessful () )
    {
System.out.println ( "CallRestService::decodeResponse - success" );//!!!

      value = Convert.jsonToJsonObject ( response .body () .string () );
System.out.println ( "CallRestService::decodeResponse - value:" + value );//!!!
      result = Result.success ( value );
    }
    else
    {
System.err.println ( "CallRestService::decodeResponse - failure" );//!!!
      value = Convert.jsonToJsonObject ( response .body () .string () );
System.out.println ( "CallRestService::decodeResponse - value:" + value );//!!!
      result = Result.failure ( value );
    }

System.out.println ( "Leaving the CallRestService::decodeResponse" );//!!!
    
    return result;
  }

}
