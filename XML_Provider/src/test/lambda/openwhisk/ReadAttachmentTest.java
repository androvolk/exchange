package test.lambda.openwhisk;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public final class ReadAttachmentTest
{

  public static void main ( String [] args ) throws IOException
  {
    Request request = new Request.Builder ()
      .url ( "http://192.168.33.13:5984/feed_files/" + args [ 0 ] + "/XML?rev=" + args [ 1 ] +"&attachments=true" )
      .get ()
      .build ();
 
    Call call = new OkHttpClient () .newCall ( request );
    Response response = call.execute ();
    
    File videoFile = new File ( "./out.xml" );
    try (BufferedSource bufferedSource = response.body().source()) {
      BufferedSink bufferedSink = Okio.buffer(Okio.sink( (videoFile) ) );
      bufferedSink.writeAll(bufferedSource);
      bufferedSink.close();
    }
  }
}
