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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;


public final class XmlValidator
{
  private static final Logger log = LoggerFactory.getLogger ( XmlValidator.class );

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
    log.debug ( String.format ( 
          "Entering XmlValidator::isXmlValid()  Params: { xml = %s, schemaFileName = %s }", xml, schemaFileName ) );
    try
    {
      if ( factory == null ) factory = SchemaFactory.newInstance ( XMLConstants.W3C_XML_SCHEMA_NS_URI );
      log.debug ( String.format ( "factory = %s", factory ) );
      Schema schema = factory.newSchema ( new File ( schemaFileName ) );
      log.debug ( String.format ( "schema = %s", schema ) );
      Validator validator = schema.newValidator ();
      log.debug ( String.format ( "validator = %s", validator ) );
      if ( xmlFactory == null ) xmlFactory = XMLInputFactory.newFactory ();
      log.debug ( String.format ( "xmlFactory = %s", xmlFactory ) );
      XMLEventReader reader = xmlFactory.createXMLEventReader ( new StringReader ( xml ) );
      log.debug ( String.format ( "reader = %s", reader ) );
      validator.validate ( new StAXSource ( reader ) );
    }
    catch ( SAXException | IOException | XMLStreamException e )
    {
      log.error ( "XmlValidator::isXmlValid found the problem in XML file: " + e.getMessage () );
      e.printStackTrace ();
      log.error ( "Leaving XmlValidator::isXmlValid() after failure" );
      return false;
    }

    log.debug ( "Successfully leaving XmlValidator::isXmlValid()" );
    
    return true;
  }
  
}
