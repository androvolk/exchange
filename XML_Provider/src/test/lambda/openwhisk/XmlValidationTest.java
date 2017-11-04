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

public class XmlValidationTest
{

  public static void main ( String [] args )
  {
    try
    {
      SchemaFactory factory = SchemaFactory.newInstance ( XMLConstants.W3C_XML_SCHEMA_NS_URI );
      Schema schema = factory.newSchema ( new File ( args [ 0 ] ) );
      Validator validator = schema.newValidator ();
      XMLInputFactory xmlFactory = XMLInputFactory.newFactory ();
      XMLEventReader reader = xmlFactory.createXMLEventReader ( new StringReader ( args [ 1 ] ) );
      validator.validate ( new StAXSource ( reader ) );
    }
    catch ( SAXException | IOException | XMLStreamException e )
    {
      e.printStackTrace ();
    }
    
    System.out.println ( "XML is valid :)" );
  }

}
