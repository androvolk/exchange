package test.lambda.openwhisk;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public final class ReadAttachmentTest
{

  public static void main ( String [] args ) throws IOException
  {
    List < byte [] > chunks = new ArrayList <> ();
    
    Request request = new Request.Builder ()
      .url ( "http://192.168.33.13:5984/feed_files/" + args [ 0 ] + "/XML?rev=" + args [ 1 ] +"&attachments=true" )
//     .addHeader ( "Accept", "multipart/mixed" )
      .get ()
      .build ();
 
    Call call = new OkHttpClient () .newCall ( request );
    Response response = call.execute ();
//    System.out.println ( "Headers -> " + response.headers () .toString () );
//    System.out.println ( "Code -> " + response.code () );
//    System.out.println ( "Successful -> " + response.isSuccessful () );
//    System.out.println ( "|" + response .body () .string () + "|");

//    InputStream is = response.body ().byteStream ();
//    int counter = is.available ();
//    System.out.println ( "Bytes available: " + counter );
//    ByteBuffer buff = ByteBuffer.allocate ( counter );
//    new BufferedInputStream ( is ) .
    
    File videoFile = new File ( "./out.xml" );
    try (BufferedSource bufferedSource = response.body().source()) {
      BufferedSink bufferedSink = Okio.buffer(Okio.sink( (videoFile) ) );
      bufferedSink.writeAll(bufferedSource);
      bufferedSink.close();
    }
  }
}
