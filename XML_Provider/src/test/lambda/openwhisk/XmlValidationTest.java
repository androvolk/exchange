package test.lambda.openwhisk;

import java.io.File;
import java.io.FileInputStream;
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
      Schema schema = factory.newSchema ( new File ( "D:\\dimos\\git\\exchange\\Build\\dist\\test.xsd" ) );
      Validator validator = schema.newValidator ();
      XMLInputFactory xmlFactory = XMLInputFactory.newFactory ();
      XMLEventReader reader = xmlFactory.createXMLEventReader ( new StringReader ( "<?xml version = \"1.0\"?>\n" + 
          "<class>  \n" + 
          "   <student rollno = \"393\">\n" + 
          "      <firstname>Dinkar</firstname>    \n" + 
          "      <lastname>Kad</lastname>\n" + 
          "      <nickname>Dinkar</nickname>\n" + 
          "      <marks>85</marks>  \n" + 
          "   </student>\n" + 
          "   <student rollno = \"493\">  \n" + 
          "      <firstname>Vaneet</firstname>\n" + 
          "      <lastname>Gupta</lastname>\n" + 
          "      <nickname>Vinni</nickname>\n" + 
          "      <marks>95</marks>\n" + 
          "   </student>\n" + 
          "   <student rollno = \"593\">    \n" + 
          "      <firstname>Jasvir</firstname>\n" + 
          "      <lastname>Singh</lastname>\n" + 
          "      <nickname>Jazz</nickname>\n" + 
          "      <marks>90</marks>\n" + 
          "   </student>\n" + 
          "</class>" ) );
      validator.validate ( new StAXSource ( reader ) );
    }
    catch ( SAXException | IOException | XMLStreamException e )
    {
      e.printStackTrace ();
    }
    
    System.out.println ( "XML is valid :)" );
  }

}
