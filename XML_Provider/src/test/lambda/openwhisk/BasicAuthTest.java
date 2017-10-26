package test.lambda.openwhisk;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public final class BasicAuthTest
{

  public static void main ( String [] args ) throws IOException
  {
    String postBody = "{}";
    
    Request request = new Request.Builder()
      .url("https://192.168.33.13/api/v1/namespaces/guest/activations")
      .addHeader("Authorization", Credentials.basic ("23bc46b1-71f6-4ed5-8c54-816aa4f8c502", "123zO3xZCLrMN6v2BKK1dXYFpXlPkccOFqm12CdAsMgRU4VrNZ9lyGVCGuMDGIwP"))
      .get()
      .build();
 
    Call call = new OkHttpClient ().newCall(request);
    Response response = call.execute();
    System.out.println ( "Headers -> " + response.headers ().toString () );
    System.out.println ( "Code -> " + response.code () );
    System.out.println ( "Successful -> " + response.isSuccessful ());
    System.out.println ( response.body().string () );
  }

}
