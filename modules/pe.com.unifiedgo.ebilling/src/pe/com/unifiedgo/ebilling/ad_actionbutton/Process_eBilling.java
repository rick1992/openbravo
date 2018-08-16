package pe.com.unifiedgo.ebilling.ad_actionbutton;

import java.util.Iterator;

import org.apache.axis.message.SOAPBodyElement;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.w3c.dom.NodeList;

import pe.com.unifiedgo.core.SOAPClientSAAJ;
import pe.com.unifiedgo.core.WriteXMLFile;

import javax.xml.soap.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;



public class Process_eBilling extends DalBaseProcess {
  public void doExecute(ProcessBundle bundle) throws Exception {
    try {

      /*
       * Set<String> params = bundle.getParams().keySet(); for (int i = 0; i < params.size(); i++) {
       * System.out.println(params.toArray()[i]); }
       */

      final String C_Invoice_ID = (String) bundle.getParams().get("C_Invoice_ID");
      final VariablesSecureApp vars = bundle.getContext().toVars();
      OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
      final ConnectionProvider conProvider = bundle.getConnection();
      final String language = bundle.getContext().getLanguage();
      User user = OBDal.getInstance().get(User.class, vars.getUser());

      Invoice invoice = OBDal.getInstance().get(Invoice.class, C_Invoice_ID);
      if (invoice == null) {
        throw new Exception("Internal Error Null");
      }
      
      
      
      
      
      
      
      
      
      
      
      

      
      //Logic for XML_INVOICE
      
      String parameter_XML = WriteXMLFile.xml_Document(invoice);

      //String parameter_XML = WriteXMLFile.xml_Invoice();
        //System.out.println("parameter_XML: " + parameter_XML);
      
      //New Logic Import SOAPClientSAAJ.java
            //SOAPClientSAAJ.createSOAP_eBillingWS(parameter_XML);
            SOAPClientSAAJ.createSOAP_Test();
           // SOAPClientSAAJ.createSOAP_TestEstados();
      
      // Logic
      /*System.out.println("invoice:" + invoice.getIdentifier());
      
      
      SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
      SOAPConnection soapConnection = soapConnectionFactory.createConnection();
      // Send SOAP Message to SOAP Server
      String url = "http://ws.cdyne.com/emailverify/Emailvernotestemail.asmx";
      SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(), url);
      // print SOAP Response
      
      printSOAPResponse(soapResponse);
      
     // System.out.print("Response SOAP Message:");
     // soapResponse.writeTo(System.out);
      soapConnection.close();*/
      

      final OBError msg = new OBError();
      msg.setType("Success");
      msg.setTitle("@Success@");

      bundle.setResult(msg);
      OBDal.getInstance().commitAndClose();
    } catch (final OBException e) {
        
        System.out.println("ERRRRROROROROROROR");
        
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(e.getMessage());
      System.out.println("MENSAJE: " + e.getMessage());
      
      msg.setTitle("@Error@");
      OBDal.getInstance().rollbackAndClose();
      bundle.setResult(msg);
    }
  }
  
  private static SOAPMessage createSOAPRequestChino() throws Exception {
      MessageFactory messageFactory = MessageFactory.newInstance();
      SOAPMessage soapMessage = messageFactory.createMessage();
      SOAPPart soapPart = soapMessage.getSOAPPart();

      String serverURI = "http://ws.cdyne.com/";
     // String serverURI2 = "http://www.SoapClient.com/xml/";

      // SOAP Envelope
      SOAPEnvelope envelope = soapPart.getEnvelope();
      envelope.addNamespaceDeclaration("example", serverURI);
      //envelope.addNamespaceDeclaration("SoapResponder", serverURI2);

      /*
      Constructed SOAP Request Message:
      <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:example="http://ws.cdyne.com/">
          <SOAP-ENV:Header/>
          <SOAP-ENV:Body>
              <example:VerifyEmail>
                  <example:email>mutantninja@gmail.com</example:email>
                  <example:LicenseKey>123</example:LicenseKey>
              </example:VerifyEmail>
          </SOAP-ENV:Body>
      </SOAP-ENV:Envelope>
       */

      // SOAP Body
      SOAPBody soapBody = envelope.getBody();
      SOAPElement soapBodyElem = soapBody.addChildElement("VerifyEmail", "example");
      SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("email", "example");
      soapBodyElem1.addTextNode("mutantninja@gmail.com");
      SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("LicenseKey", "example");
      soapBodyElem2.addTextNode("123");

      MimeHeaders headers = soapMessage.getMimeHeaders();
      headers.addHeader("SOAPAction", serverURI  + "VerifyEmail");

      soapMessage.saveChanges();

      /* Print the request message */
      System.out.print("Request SOAP Message:");
      soapMessage.writeTo(System.out);
      System.out.println();

      return soapMessage;
  }
  
  private static SOAPMessage createSOAPRequest() throws Exception {
      MessageFactory messageFactory = MessageFactory.newInstance();
      SOAPMessage soapMessage = messageFactory.createMessage();
      SOAPPart soapPart = soapMessage.getSOAPPart();

      String serverURI = "http://ws.cdyne.com/";
     // String serverURI2 = "http://www.SoapClient.com/xml/";

      // SOAP Envelope
      SOAPEnvelope envelope = soapPart.getEnvelope();
      envelope.addNamespaceDeclaration("example", serverURI);
      //envelope.addNamespaceDeclaration("SoapResponder", serverURI2);

      /*
      Constructed SOAP Request Message:
      <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:example="http://ws.cdyne.com/">
          <SOAP-ENV:Header/>
          <SOAP-ENV:Body>
              <example:VerifyEmail>
                  <example:email>mutantninja@gmail.com</example:email>
                  <example:LicenseKey>123</example:LicenseKey>
              </example:VerifyEmail>
          </SOAP-ENV:Body>
      </SOAP-ENV:Envelope>
       */

      // SOAP Body
      SOAPBody soapBody = envelope.getBody();
      SOAPElement soapBodyElem = soapBody.addChildElement("VerifyEmail", "example");
      SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("email", "example");
      soapBodyElem1.addTextNode("mutantninja@gmail.com");
      SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("LicenseKey", "example");
      soapBodyElem2.addTextNode("123");

      MimeHeaders headers = soapMessage.getMimeHeaders();
      headers.addHeader("SOAPAction", serverURI  + "VerifyEmail");

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
      String operationName=null;
      SOAPEnvelope env= soapResponse.getSOAPPart().getEnvelope();
      SOAPBody sb = env.getBody();
      Name ElName = env.createName("VerifyEmailResponse");
      Iterator it = sb.getChildElements();
      

      while (it.hasNext()) {
          /*System.out.println("ORTIZ");
            el=(SOAPElement)it.next();
              
            operationName=el.getElementName().getLocalName();
            System.out.println("MyValue: "+operationName);*/
          Node node=(Node)it.next();
          if (node instanceof SOAPElement) {
                  el=(SOAPElement)node;
                  System.out.println(el.getElementName().getLocalName());
                  if(el.getElementName().getLocalName().equals("VerifyEmailResponse")){
                          NodeList statusNodeList = el.getChildNodes();
                          for(int i=0; i< statusNodeList.getLength();i++){
                                  SOAPElement statusElement = (SOAPElement) statusNodeList.item(i);
                                  System.out.println(statusElement.getElementName().getLocalName());
                                  NodeList statusNodeList2 = statusElement.getChildNodes();
                                  for(int j=0; j< statusNodeList2.getLength();j++){
                                          SOAPElement statusElement2 = (SOAPElement) statusNodeList2.item(j);
                                          System.out.println(statusElement2.getElementName().getLocalName());
                                          System.out.println(statusElement2.getValue());
                                  }
                                  
                          }
                          
                  }
          }
      }
      
      
      /*it = sb.getChildElements();
      while (it.hasNext()) {
          System.out.println("ORTIZ 3333");
          Node node = (Node)it.next();
          SOAPElement element = null;
          Text text = null;
          
          if (node instanceof SOAPElement) {
              element = (SOAPElement)node;
              System.out.println(element.getElementName().getLocalName());
              
              Iterator It3 = element.getChildElements();
              while (It3.hasNext()) {
                  Node node2 = (Node)It3.next();
                  SOAPElement element2 = null;
                  if (node instanceof SOAPElement) {
                          element = (SOAPElement)node;
                      System.out.println(element.getElementName().getLocalName());
                  }
                  
              }
              
          }
          
      }*/
      
      //----
      /*ElName = env.createName("ResponseText");                  
      Iterator iteratorTmp = sb.getChildElements(ElName);
      SOAPBodyElement bodyElement = (SOAPBodyElement)iteratorTmp.next();
      String lastPrice = bodyElement.getValue();
      System.out.print("PORFIN");
      System.out.println(lastPrice);
      System.out.print("PORFIN22222");*/
      //---
      
      
      
  //    SOAPBodyElement sbe = (SOAPBodyElement) it.next();
      
    //  String MyValue =   sbe.getValue();
      
     // System.out.println("MyValue: "+MyValue);
      
      
      System.out.print("\nResponse SOAP Message = ");
      StreamResult result = new StreamResult(System.out);
      transformer.transform(sourceContent, result);
      System.out.println("RESULT");
      System.out.println(result);
  }
  
  private static void printSOAPResponseVafaster(SOAPMessage soapResponse) throws Exception{
          
  }
  
}