package test.lambda.openwhisk;

import java.io.File;
import java.io.IOException;

import com.google.gson.JsonObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import test.lambda.utils.Convert;
import test.lambda.utils.Result;

public final class WriteAttachmentTest
{
  private static MediaType XML = null;
  private static OkHttpClient client = null;

  public static void main ( String [] args ) throws IOException
  {
    if ( XML == null ) XML = MediaType.parse ( "application/xml; charset=utf-8" );
    
System.out.println ( "MediaType -> " + XML );//!!!

    RequestBody body = RequestBody.create ( XML, new File ( args [ 2 ] ) );
    Request request = new Request.Builder ()
        .url ( "http://192.168.33.13:5984/feed_files/" + args [ 0 ] + "/XML?rev=" +  args [ 1 ] )
        .put ( body )
        .build ();

    if ( client == null ) client = new OkHttpClient ();

System.out.println ( "PUT request  -> " + request );//!!!    

    try ( Response response = client .newCall ( request ) .execute () )
    { System.out.println ( decodeResponse ( response ) ); }
  }

  
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
