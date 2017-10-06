package test.lambda.utils;

//import org.eclipse.jetty.client.HttpClient;
//import org.eclipse.jetty.client.api.ContentResponse;
//import org.eclipse.jetty.client.api.Request;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class TestHTTP
{
//  private static HttpClient httpClient = null;
  
  
  public TestHTTP ()
  {
    // TODO Auto-generated constructor stub
  }

//  public static String call () throws Exception
//  {
//    Request req = null;
//    ContentResponse resp = null;
//    String result = null;
//    
//    if ( httpClient == null ) httpClient = new HttpClient();
//    if ( ! httpClient.isRunning () )  httpClient.start ();
//
//    
//    req = httpClient.newRequest ( "http://localhost:8080/hello" );
//    req.getHeaders () .add ( "Content-Type", "application/json" );
//System.out.println ( resp.getStatus () );
////System.out.println ( resp.getEncoding () );
//System.out.println ( result );
//
//    return result;
//  }

  public static String call () throws Exception
  {
    final MediaType JSON = MediaType.parse ( "application/json; charset=utf-8" );

    OkHttpClient client = new OkHttpClient();

//    Request request = new Request.Builder () 
//                            .url ( "http://localhost:8080/" )
//                            .get ()
//                            .build ();        
    RequestBody body = RequestBody.create ( JSON, "{\"post\":\"test\"}" );
    Request request = new Request.Builder ()
      .url ( "http://localhost:8080/post_test" )
      .post ( body )
      .build ();
    Response response = client .newCall ( request ) .execute ();
    return response .body () .string();
  }
  
  // TODO: Remove after debugging!
  public static void main ( String [] args ) throws Exception
  {
    System.out.println (  call() );
//    httpClient.stop ();
  }

}
