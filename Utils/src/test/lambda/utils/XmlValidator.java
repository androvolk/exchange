package test.lambda.utils;

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


public final class XmlValidator
{
  private static SchemaFactory factory = null;
  private static XMLInputFactory xmlFactory = null;
  
  /**
   * Fast XML validator.
   * @param xml - XML as a string.
   * @param schemaFileName - XML schema file name to validate the xml against.
   * @return <i>true</i> if XML is valid. <i>false</i> otherwise.
   */
  public static boolean isXmlValid ( final String xml, final String schemaFileName )
  {
    try
    {
      if ( factory == null ) factory = SchemaFactory.newInstance ( XMLConstants.W3C_XML_SCHEMA_NS_URI );
      Schema schema = factory.newSchema ( new File ( schemaFileName ) );
      Validator validator = schema.newValidator ();
      if ( xmlFactory == null ) xmlFactory = XMLInputFactory.newFactory ();
      XMLEventReader reader = xmlFactory.createXMLEventReader ( new StringReader ( xml ) );
      validator.validate ( new StAXSource ( reader ) );
    }
    catch ( SAXException | IOException | XMLStreamException e )
    {
      System.err.println ( "XmlValidator::isXmlValid found the problem in XML file: " + e.getMessage () );
      return false;
    }
    
    return true;
  }
  
}
