package test.lambda.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import okhttp3.MediaType;
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


  @SuppressWarnings ( "rawtypes" )
  public static Map jsonToMap ( final String json ) 
  {
    if ( gson == null ) gson = new Gson ();
    return gson.fromJson ( json, HashMap.class );
  }
  

  public static String get ( final String url ) throws IOException
  {
    Request request = new Request.Builder()
        .url(url)
        .build();

    if ( client == null ) client = new OkHttpClient ();
    
    try ( Response response = client .newCall ( request ) .execute () )
    { return response .body () .string (); }
  }

  public static String put ( final String url, final String json) throws IOException
  {
    if ( JSON == null ) JSON = MediaType.parse ( "application/json; charset=utf-8" );
    
    RequestBody body = RequestBody.create(JSON, json);
    Request request = new Request.Builder()
        .url(url).put ( body )
        .post(body)
        .build();
    if ( client == null ) client = new OkHttpClient ();
    
    try ( Response response = client .newCall ( request ) .execute () )
    { return response .body () .string (); }
  }

//  private static Response request ( final String method, final String host, final int port, 
//                            final String path, final String login, final String password, final String payload )
//  {
////    URL url = null;
////   HttpURLConnection con = null;
////    OutputStream os = null;
//    String body = null;
//    String prefix = "";
//    
//    if ( login != null && password != null ) prefix = login + ':' + password + '@';
//    
//    try
//    {
//      url = new URL ( "http://" + prefix + host + ":" + Integer.toString ( port ) + path );
//System.out.println ( url.toString () );      //!!!
//      con = ( HttpURLConnection ) url.openConnection ();
//      con.setRequestMethod ( method );
//      con.setRequestProperty ( "Content-Type", "application/json" );
//      con.setDoOutput ( true );
//      if ( payload != null )
//      {
////        con.setDoOutput ( true );
//        os = con.getOutputStream ();
//        os.write ( payload.getBytes ( "UTF-8" ) );
//        os.flush ();
//      }
//      con.connect (); //???
//      body = IOUtils.toString ( con.getInputStream () );
//      
//      return new Response ( con.getResponseCode (), body );
//    }
//    catch ( IOException e )
//    {
//      System.err.println ( "Request failed: " + e.getMessage () );
//      e.printStackTrace ();
//      return null;
//    }
//  }


//  public static Map requestJsonAsMap ( final String method, final String host, final int port, 
//                             final String path, final String login, final String password, final String payload )
//  {
//    Response resp = null;
//    
//    resp = request ( method, host, port, path, login, password, payload );
//    if ( resp == null ) return null;
//    return resp.json ();
//  }


//  public static String requestJsonAsString ( final String method, final String host, final int port, 
//                             final String path, final String login, final String password, final String payload )
//  {
//    Response resp = null;
//    
//    resp = request ( method, host, port, path, login, password, payload );
//    if ( resp == null ) return null;
//    return String.format ( "{\n\t\"status\": \"%d\",\n\t\"body\": \"%s\"\n}", Integer.valueOf ( resp.status ), resp.body );
//  }


//  private static class Response
//  {
//    public final String body;
//    public final int status;
//
//    public Response ( int status, String body )
//    {
//      this.status = status;
//      this.body = body;
//    }
//
//    @SuppressWarnings ( "unchecked" )
//    public Map < String, String > json ()
//    {
//      if ( gson == null ) gson = new Gson ();
//      return ( Map < String, String > ) ( gson.fromJson ( body, HashMap.class ) );
//    }
//  }


  public static void main ( String [] args ) throws Exception
  {
    
//    System.out.println ( CallRestService.request ( "GET", "localhost", 8080, "/service", null, null, null ) .status );
//    System.out.println ( CallRestService.request ( "GET", "localhost", 8080, "/service", null, null, null ) .body );
    
    String json =  get ( "http://localhost:8080/post_test" );
    Map elements = jsonToMap ( json );

    System.out.println ( elements.get ( "Headers") );

//    Map response = CallRestService.requestJsonAsMap ( "GET", "localhost", 8080, "/service", null, null, null );

 //   System.out.println ( response.get ( "status" ) );
//    @SuppressWarnings ( "unchecked" )
//    List < String > result = ( List < String > ) ( elements.get ( "result" ) );
//    for ( String string : result ) { System.out.println ( string ); }
  }

}
