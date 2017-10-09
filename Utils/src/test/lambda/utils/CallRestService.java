package test.lambda.utils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import spark.utils.IOUtils;

public final class CallRestService
{
  private static MediaType JSON = null;
  private static Gson gson = null;
  private static OkHttpClient client = null;


  /**
   * Converts JSON to set of Key-Value pairs packed into Map
   * @param json JSON document
   * @return Map where key is JSON attribute name and value is a JSON value
   */
  @SuppressWarnings ( "rawtypes" )
  public static Map jsonToMap ( final String json ) 
  {
    if ( gson == null ) gson = new Gson ();
    return gson.fromJson ( json, HashMap.class );
  }
  

  /**
   * Calls REST GET command
   * @param url URL of resource
   * @return Result of the call in string format
   * @throws IOException
   */
  public static String get ( final String url ) throws IOException
  {
    Request request = new Request.Builder ()
        .url ( url )
        .build ();

    if ( client == null ) client = new OkHttpClient ();
    
    try ( Response response = client .newCall ( request ) .execute () )
    { return response .body () .string (); }
  }


  /**
   * Calls REST PUT command.
   * @param url URL of resource.
   * @param payload Data that should be transfered in request body.
   * @return Result of the call in string format.
   * @throws IOException
   */
  public static String put ( final String url, final String payload ) throws IOException
  {
    if ( JSON == null ) JSON = MediaType.parse ( "application/json; charset=utf-8" );
    
    RequestBody body = RequestBody.create ( JSON, payload );
    Request request = new Request.Builder ()
        .url ( url )
        .put ( body )
        .build ();

    if ( client == null ) client = new OkHttpClient ();
    
    try ( Response response = client .newCall ( request ) .execute () )
    { return response .body () .string (); }
  }


  // TODO: Test me gently!
  public static String putWithFile ( final String url, final String docRevision, final String fileResouceName,
                                               final String pathToFile, final String mimeType ) throws IOException
  {
    File attachment = new File ( pathToFile );
    RequestBody body = new MultipartBody.Builder ()
        .setType ( MultipartBody.FORM )
        .addFormDataPart ( fileResouceName, attachment.getName () ,
          RequestBody.create ( MediaType.parse ( mimeType ), new File ( pathToFile ) ) ) 
//          RequestBody.create ( MediaType.parse ( "application/octet-stream" ), 
        .build();

    Request request = new Request.Builder ()
//        .url ( url + "/" + fileResouceName )
        .url ( url + "/" + fileResouceName + "?rev=" + docRevision )
//        .addHeader ( "ETag", "\"" + docRevision  + "\"" )//???
        .put ( body )
        .build ();
System.out.println ( "Request: " + request.toString () );//!!!
System.out.println ( "Headers: "  + request.headers () .toString () );

    if ( client == null ) client = new OkHttpClient ();
    
    try ( Response response = client .newCall ( request ) .execute () )
    { return response .body () .string (); }
  }



  // TODO: Remove after debugging
  public static void main ( String [] args ) throws Exception
  {
    
    String json =  get ( "http://localhost:8080/post_test" );
    Map elements = jsonToMap ( json );

    System.out.println ( elements.get ( "Headers") );

  }

}
