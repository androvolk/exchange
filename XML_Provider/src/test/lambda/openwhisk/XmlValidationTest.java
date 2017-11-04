package test.lambda.openwhisk;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stax.StAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class XmlValidationTest
{

  public static void main ( String [] args )
  {
    try
    {
      SchemaFactory factory = SchemaFactory.newInstance ( XMLConstants.W3C_XML_SCHEMA_NS_URI );
System.out.println ( "#1" );
      Schema schema = factory.newSchema ( new File ( args [ 0 ] ) );
System.out.println ( "#2" );
      Validator validator = schema.newValidator ();
System.out.println ( "#3" );
      XMLInputFactory xmlFactory = XMLInputFactory.newFactory ();
System.out.println ( "#4" );
      //new File (args [1]) .
      
      BufferedSource bufferedSource = Okio.buffer ( Okio.source ( new File ( args [1] ) ) );
      
      XMLEventReader reader = xmlFactory.createXMLEventReader ( new StringReader ( bufferedSource.readUtf8 () ) );
System.out.println ( "#5" );
      validator.validate ( new StAXSource ( reader ) );
System.out.println ( "#6" );
    }
    catch ( SAXException | IOException | XMLStreamException e )
    {
System.err.println ( e.getMessage () );
      e.printStackTrace ();
    }
    
    System.out.println ( "XML is valid :)" );
  }

}
