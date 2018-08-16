package pe.com.unifiedgo.core;

import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.util.Iterator;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Name;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.soap.Text;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.openbravo.service.db.DataExportService;

/**
 * The export client process is called from the ui. It exports all the data from one client using a
 * specific dataset. It again calls the {@link DataExportService} for the actual export.
 * 
 * @author mtaal
 */

public class SOAPClientSAAJ {

  /** The filename of the export file with client data. */
  public static final String CLIENT_DATA_PREFIX = "client_data_";

  /** The directory within WEB-INF in which the export file is placed. */
  public static final String EXPORT_DIR_NAME = "referencedata";

  public static void main(String args[]) throws Exception {
    // Create SOAP Connection

    // Send SOAP Message to SOAP Server
    // String url = "http://ws.cdyne.com/emailverify/Emailvernotestemail.asmx";
    // String url2 = "http://www.SoapClient.com/xml/SoapResponder.xsd";

    // SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(), url);

    /*
     * SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
     * SOAPConnection soapConnection = soapConnectionFactory.createConnection();
     * 
     * 
     * // print SOAP Response System.out.print("Response SOAP Message:");
     * soapResponse.writeTo(System.out);
     * 
     * soapConnection.close();
     */
  }

  public static SOAPMessage createSOAPConnection(SOAPMessage request, String strURLAddress) throws Exception {

    SOAPMessage soapResponse = null;
    try {
      SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
      SOAPConnection soapConnection = soapConnectionFactory.createConnection();

      // Send SOAP Message to SOAP Server
      String url = strURLAddress;
      soapResponse = soapConnection.call(request, url);
      printSOAPResponse(soapResponse);
      soapConnection.close();

    } catch (SOAPException e1) {
      e1.printStackTrace();
      throw e1;
    } catch (Exception e2) {
      e2.printStackTrace();
      throw e2;
    }

    return soapResponse;

    /*
     * if (soapResponse == null) System.out.println("Null Returned");
     */
    // else
    // printSOAPResponse(soapResponse);

  }

  public static void createSOAP_eBillingWS(String parameter_XML) throws Exception {
    // Create SOAP Connection
    SOAPMessage soapResponse = null;
    try {

      SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
      SOAPConnection soapConnection = soapConnectionFactory.createConnection();

      // Send SOAP Message to SOAP Server
      String url = "http://192.168.0.33:18585/WebSite1/Convert.asmx";
      soapResponse = soapConnection.call(createSOAPRequest_eBillingWS(parameter_XML), url);

      // print SOAP Response
      // printSOAPResponse(soapResponse);

      // System.out.print("Response SOAP Message:");
      // soapResponse.writeTo(System.out);

      soapConnection.close();

    } catch (MalformedURLException e1) {
      e1.printStackTrace();
    } catch (SOAPException e1) {
      e1.printStackTrace();
    } catch (Exception e2) {
      e2.printStackTrace();
    }
    if (soapResponse == null)
      System.out.println("Null Returned");
    else
      printSOAPResponse(soapResponse);

  }

  public static void createSOAP_Test() throws Exception {
    // Create SOAP Connection
    SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
    SOAPConnection soapConnection = soapConnectionFactory.createConnection();
    System.out.println("PRIMERO");
    // Send SOAP Message to SOAP Server
    String url = "http://devretenciones.invoicec.com.pe/rets/retService?wsdl";
    System.out.println("SEGUNDO");
    // String url2 = "http://www.SoapClient.com/xml/SoapResponder.xsd";
    SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(), url);

    // print SOAP Response
    printSOAPResponse(soapResponse);

    // System.out.print("Response SOAP Message:");
    // soapResponse.writeTo(System.out);

    soapConnection.close();
  }

  public static void createSOAP_TestOriginal() throws Exception {
    // Create SOAP Connection
    SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
    SOAPConnection soapConnection = soapConnectionFactory.createConnection();

    // Send SOAP Message to SOAP Server
    String url = "http://ws.cdyne.com/emailverify/Emailvernotestemail.asmx";
    // String url2 = "http://www.SoapClient.com/xml/SoapResponder.xsd";
    SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(), url);

    // print SOAP Response
    System.out.println("CABALLERO");
    printSOAPResponse(soapResponse);

    // System.out.print("Response SOAP Message:");
    // soapResponse.writeTo(System.out);

    soapConnection.close();
  }

  private static SOAPMessage createSOAPRequest() throws Exception {
    MessageFactory messageFactory = MessageFactory.newInstance();
    SOAPMessage soapMessage = messageFactory.createMessage();
    SOAPPart soapPart = soapMessage.getSOAPPart();
    System.out.println("TERCERO");
    String serverURI = "http://ws.retenciones.invoicec.cimait.com/";
    System.out.println("CUARTO");
    // SOAP Envelope
    SOAPEnvelope envelope = soapPart.getEnvelope();
    envelope.addNamespaceDeclaration("ws", serverURI);
    System.out.println("QUINTO");

    // SOAP Body
    SOAPBody soapBody = envelope.getBody();
    System.out.println("SEXTO");
    SOAPElement soapBodyElem = soapBody.addChildElement("sendRetention", "ws");
    SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("nombreArchivo");
    System.out.println("SEPTIMO");
    soapBodyElem1.addTextNode("20131529008-08-B014-00000001");
    SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("contenido");
    soapBodyElem2.addTextNode("Q1J8UjAwMS0wMDAwMDAyOXwyMDE3LTA1LTI0fDIwfDEwNzQyNzc2MTM3fDZ8fDEyMzIzMnxWaWxsYSBlbCBTYWx2YWRvcnxMSU1BfExJTUF8TElNQXxMSU1BfFBFfEppbW15IFNhbGF6YXJ8MTA3NDI3NzYxMzd8Nnx8fHx8fHx8UEV8SmltbXkgU2FsYXphcnwwMXwzLjAwfHwxMS43MHxQRU58MTAuMDB8UEVOfHx8CkRSfDAxfEYwMDEtMDAwMDAwMzF8MjAxNy0wNC0wOHwzODkuODd8UEVOfDIwMTctMDUtMjR8MXwxMC4wMHxQRU58MTEuNzB8UEVOfDIwMTctMDUtMjR8Mzc4LjE3fFBFTnxQRU58UEVOfDEuMDAwfDIwMTctMDUtMjR8");
    System.out.println("OCTAVO");

    MimeHeaders headers = soapMessage.getMimeHeaders();
    headers.addHeader("SOAPAction", serverURI + "sendRetention");
    System.out.println("NOVENO");

    soapMessage.saveChanges();

    /* Print the request message */
    System.out.print("Request SOAP Message:");
    soapMessage.writeTo(System.out);
    System.out.println();

    return soapMessage;
  }

  private static SOAPMessage createSOAPRequestOriginal() throws Exception {
    MessageFactory messageFactory = MessageFactory.newInstance();
    SOAPMessage soapMessage = messageFactory.createMessage();
    SOAPPart soapPart = soapMessage.getSOAPPart();

    String serverURI = "http://ws.cdyne.com/";

    // SOAP Envelope
    SOAPEnvelope envelope = soapPart.getEnvelope();
    envelope.addNamespaceDeclaration("example", serverURI);
    // envelope.addNamespaceDeclaration("SoapResponder", serverURI2);

    /*
     * Constructed SOAP Request Message: <SOAP-ENV:Envelope
     * xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"
     * xmlns:example="http://ws.cdyne.com/"> <SOAP-ENV:Header/> <SOAP-ENV:Body>
     * <example:VerifyEmail> <example:email>mutantninja@gmail.com</example:email>
     * <example:LicenseKey>123</example:LicenseKey> </example:VerifyEmail> </SOAP-ENV:Body>
     * </SOAP-ENV:Envelope>
     */

    // SOAP Body
    SOAPBody soapBody = envelope.getBody();
    SOAPElement soapBodyElem = soapBody.addChildElement("VerifyEmail", "example");
    SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("email", "example");
    soapBodyElem1.addTextNode("mutantninja@gmail.com");
    SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("LicenseKey", "example");
    soapBodyElem2.addTextNode("123");

    MimeHeaders headers = soapMessage.getMimeHeaders();
    headers.addHeader("SOAPAction", serverURI + "VerifyEmail");

    soapMessage.saveChanges();

    /* Print the request message */
    System.out.print("Request SOAP Message:");
    soapMessage.writeTo(System.out);
    System.out.println();

    return soapMessage;
  }

  private static SOAPMessage createSOAPRequest_eBillingWS(String parameter_XML) throws Exception {
    MessageFactory messageFactory = MessageFactory.newInstance();
    SOAPMessage soapMessage = messageFactory.createMessage();
    SOAPPart soapPart = soapMessage.getSOAPPart();

    String serverURI = "http://ws.test.com/";

    // SOAP Envelope
    SOAPEnvelope envelope = soapPart.getEnvelope();
    envelope.addNamespaceDeclaration("example", serverURI);
    // envelope.addNamespaceDeclaration("SoapResponder", serverURI2);

    /*
     * Constructed SOAP Request Message: <SOAP-ENV:Envelope
     * xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"
     * xmlns:example="http://ws.cdyne.com/"> <SOAP-ENV:Header/> <SOAP-ENV:Body>
     * <example:VerifyEmail> <example:email>mutantninja@gmail.com</example:email>
     * <example:LicenseKey>123</example:LicenseKey> </example:VerifyEmail> </SOAP-ENV:Body>
     * </SOAP-ENV:Envelope>
     */

    // SOAP Body Estructure
    SOAPBody soapBody = envelope.getBody();
    SOAPElement soapBodyElem = soapBody.addChildElement("loadFacturaXML", "example");

    SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("xml", "example");
    soapBodyElem1.addTextNode(parameter_XML);

    SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("ruc", "example");
    soapBodyElem2.addTextNode("01345678965");

    SOAPElement soapBodyElem3 = soapBodyElem.addChildElement("token", "example");
    soapBodyElem3.addTextNode("Altoken");

    SOAPElement soapBodyElem4 = soapBodyElem.addChildElement("usuario", "example");
    soapBodyElem4.addTextNode("Openbravo");

    SOAPElement soapBodyElem5 = soapBodyElem.addChildElement("subid", "example");
    soapBodyElem5.addTextNode("SubidforORG");

    MimeHeaders headers = soapMessage.getMimeHeaders();
    headers.addHeader("SOAPAction", serverURI + "loadFacturaXML");

    soapMessage.saveChanges();

    /* Print the request message */
    System.out.print("Request SOAP Message:");
    soapMessage.writeTo(System.out);
    System.out.println();

    return soapMessage;
  }

  private static void printSOAPResponse(SOAPMessage soapResponse) throws Exception {
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    Source sourceContent = soapResponse.getSOAPPart().getContent();
    SOAPElement el;
    String operationName = null;
    SOAPEnvelope env = soapResponse.getSOAPPart().getEnvelope();
    SOAPBody sb = env.getBody();
    Name ElName = env.createName("VerifyEmailResponse");
    Iterator it = sb.getChildElements();

    /*
     * while (it.hasNext()) { System.out.println("JEJEJ"); el=(SOAPElement)it.next();
     * operationName=el.getElementName().getLocalName();
     * System.out.println("MyValue: "+operationName);
     * 
     * 
     * 
     * 
     * }
     */

    /*
     * while (it.hasNext()) {
     * 
     * System.out.println("JEJEJ");
     */
    /*
     * System.out.println("ORTIZ"); el=(SOAPElement)it.next();
     * 
     * operationName=el.getElementName().getLocalName();
     * System.out.println("MyValue: "+operationName);
     */
    /*
     * Node node=(Node)it.next(); if (node instanceof SOAPElement) { el=(SOAPElement)node;
     * System.out.println(el.getElementName().getLocalName());
     * if(el.getElementName().getLocalName().equals("VerifyEmailResponse")){ NodeList statusNodeList
     * = el.getChildNodes(); for(int i=0; i< statusNodeList.getLength();i++){ SOAPElement
     * statusElement = (SOAPElement) statusNodeList.item(i);
     * System.out.println(statusElement.getElementName().getLocalName()); NodeList statusNodeList2 =
     * statusElement.getChildNodes(); for(int j=0; j< statusNodeList2.getLength();j++){ SOAPElement
     * statusElement2 = (SOAPElement) statusNodeList2.item(j);
     * System.out.println(statusElement2.getElementName().getLocalName());
     * System.out.println(statusElement2.getValue()); }
     * 
     * }
     * 
     * } } }
     */

    it = sb.getChildElements();
    while (it.hasNext()) {
      System.out.println("ORTIZ 3333");
      Node node = (Node) it.next();
      SOAPElement element = null;
      Text text = null;

      if (node instanceof SOAPElement) {
        element = (SOAPElement) node;
        System.out.println(element.getElementName().getLocalName());

        Iterator It3 = element.getChildElements();
        while (It3.hasNext()) {
          Node node2 = (Node) It3.next();
          SOAPElement element2 = null;
          if (node instanceof SOAPElement) {
            element = (SOAPElement) node;
            System.out.println(element.getElementName().getLocalName());
          }

        }

      }

    }

    // ----
    /*
     * ElName = env.createName("ResponseText"); Iterator iteratorTmp = sb.getChildElements(ElName);
     * SOAPBodyElement bodyElement = (SOAPBodyElement)iteratorTmp.next(); String lastPrice =
     * bodyElement.getValue(); System.out.print("PORFIN"); System.out.println(lastPrice);
     * System.out.print("PORFIN22222");
     */
    // ---

    // SOAPBodyElement sbe = (SOAPBodyElement) it.next();

    // String MyValue = sbe.getValue();

    // System.out.println("MyValue: "+MyValue);

    System.out.print("\nResponse SOAP Message = ");
    StreamResult result = new StreamResult(System.out);
    transformer.transform(sourceContent, result);
    System.out.println(" - RESULT 1 - ");
    System.out.println(result);
    System.out.println(" - RESULT 2 - ");

  }

  public static String getSOAPResponseStringResult(SOAPMessage soapResponse) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    soapResponse.writeTo(out);
    String strMsg = new String(out.toByteArray());

    return strMsg;
  }

}