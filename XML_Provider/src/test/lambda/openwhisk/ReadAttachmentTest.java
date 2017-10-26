package test.lambda.openwhisk;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class ReadAttachmentTest
{

  public static void main ( String [] args ) throws IOException
  {
    String postBody = "{}";
    
    Request request = new Request.Builder ()
      .url ( "http://192.168.33.13:5984/feed_files/" + args [ 0 ] + "/xml?rev=" + args [ 1 ] )
      .get ()
      .build ();
 
    Call call = new OkHttpClient () .newCall ( request );
    Response response = call.execute ();
    System.out.println ( "Headers -> " + response.headers () .toString () );
    System.out.println ( "Code -> " + response.code () );
    System.out.println ( "Successful -> " + response.isSuccessful () );
    System.out.println ( "|" + response .body () .string () + "|");
  }

}
