package test.lambda.utils;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;

public final class TestHTTP
{
  private static HttpClient httpClient = null;
  
  
  public TestHTTP ()
  {
    // TODO Auto-generated constructor stub
  }

  public static String call () throws Exception
  {
    Request req = null;
    ContentResponse resp = null;
    String result = null;
    
    if ( httpClient == null ) httpClient = new HttpClient();
    if ( ! httpClient.isRunning () )  httpClient.start ();

    
    req = httpClient.newRequest ( "http://localhost:8080/hello" );
    req.getHeaders () .add ( "Content-Type", "application/json" );
    req.s
    resp = req.send ();
    result = new String ( resp.getContent () );
System.out.println ( resp.getStatus () );
//System.out.println ( resp.getEncoding () );
System.out.println ( result );

    return result;
  }

  
  // TODO: Remove after debugging!
  public static void main ( String [] args ) throws Exception
  {
    call();
    httpClient.stop ();
  }

}
