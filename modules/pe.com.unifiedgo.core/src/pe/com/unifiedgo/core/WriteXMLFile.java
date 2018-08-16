package pe.com.unifiedgo.core;


import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import org.openbravo.base.exception.OBException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.service.db.DataExportService;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;





/**
 * The export client process is called from the ui. It exports all the data from one client using a
 * specific dataset. It again calls the {@link DataExportService} for the actual export.
 * 
 * @author mtaal
 */

public class WriteXMLFile{

    
  public static void main(String args[]) throws Exception {
  
  }
  
  public static String xml_Document(Object obj) throws OBException{
      String type ="";
	  if(obj instanceof Invoice){
		  if(((Invoice) obj).getDocumentType().getScoSpecialdoctype().equals("SCOARINVOICE")){
			 type="factura";
		  }
		  else if(((Invoice) obj).getDocumentType().getScoSpecialdoctype().equals("SCOARTICKET")){
			 type="boleta";
		  }
		  else{
			  throw new OBException("Facturación Electrónica no soporta este Documento");
		  }
		  
	  return  xml_Invoice((Invoice) obj,type);
	 }else{
		  throw new OBException("Facturación Electrónica no soporta esta Ventana");
	  }
	 
  }
  
  public static String xml_Invoice(Invoice invoice, String type) throws OBException {
	  String str_XML="";
	  try {
		    
		    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("documento");
			doc.appendChild(rootElement);
		
			
			// set attribute to rootElement element
			Attr attr_0 = doc.createAttribute("tipo");
			 attr_0.setValue(type);
			 rootElement.setAttributeNode(attr_0);
			
			Attr attr_1 = doc.createAttribute("uid");
			 attr_1.setValue(invoice.getId());
			 rootElement.setAttributeNode(attr_1);
		  
			// shorten way
			// staff.setAttribute("id", "1"); OTRA MANERA PARA INSERTAR LOS ATRIBUTOS A UN ELEMENT
			
			// firstname elements
			Element fechaEmision = doc.createElement("fechaEmision");
			 fechaEmision.appendChild(doc.createTextNode(new SimpleDateFormat("MM/dd/yyyy").format(invoice.getAccountingDate())));
			 rootElement.appendChild(fechaEmision);
			
			Element nroDocumento = doc.createElement("nroDocumento");
			nroDocumento.appendChild(doc.createTextNode(invoice.getScrPhysicalDocumentno().toString().trim()));
			rootElement.appendChild(nroDocumento);
			
			
			//Element sub_Client
			  Element sub_Client = doc.createElement("cliente");
			  rootElement.appendChild(sub_Client);
			
			  Element client_nroDocumentoClient = doc.createElement("nroDocumento");
			  client_nroDocumentoClient.appendChild(doc.createTextNode(invoice.getBusinessPartner().getTaxID().trim()));
			  sub_Client.appendChild(client_nroDocumentoClient);
			
			  Element client_tipoDocumento = doc.createElement("tipoDocumento");
			  client_tipoDocumento.appendChild(doc.createTextNode(invoice.getBusinessPartner().getScrComboItem().getCode().trim()));
			  sub_Client.appendChild(client_tipoDocumento);
			  
			  Element client_razonsocial = doc.createElement("razonsocial");
			  client_razonsocial.appendChild(doc.createTextNode(invoice.getBusinessPartner().getName()));
			  sub_Client.appendChild(client_razonsocial);
			//END Sub Client
			 
			 Element moneda = doc.createElement("moneda");
			 moneda.appendChild(doc.createTextNode(invoice.getCurrency().getISOCode().trim()));
			 rootElement.appendChild(moneda);
			
			 //Element Totales
			  Element sub_Totales = doc.createElement("totales");
			  rootElement.appendChild(sub_Totales);
                //Se agregarán todos los Tag de Totales	
			   HashMap<String, String> bd_map = new HashMap<String, String>();
			   bd_map=xml_invoice_tag_totales(doc,sub_Totales,invoice);
			   
			  Element totales_opGravadas = doc.createElement("opGravadas");
			  totales_opGravadas.appendChild(doc.createTextNode(bd_map.get("opGravadas").toString()));
			  sub_Totales.appendChild(totales_opGravadas);
			  
			  Element totales_opInafectas = doc.createElement("opInafectas");
			  totales_opInafectas.appendChild(doc.createTextNode(bd_map.get("opInafectas").toString()));
			  sub_Totales.appendChild(totales_opInafectas);
			  
			  Element totales_opExoneradas = doc.createElement("opExoneradas");
			  totales_opExoneradas.appendChild(doc.createTextNode(bd_map.get("opExoneradas").toString()));
			  sub_Totales.appendChild(totales_opExoneradas);
			  
			  Element totales_opIGV = doc.createElement("igv");
			  totales_opIGV.appendChild(doc.createTextNode(bd_map.get("opIGV").toString()));
			  sub_Totales.appendChild(totales_opIGV);
			  
			  Element totales_opISC = doc.createElement("isc");
			  totales_opISC.appendChild(doc.createTextNode(bd_map.get("opISC").toString()));
			  sub_Totales.appendChild(totales_opISC);
			  
			  Element totales_opOtrosTributos = doc.createElement("otrosTributos");
			  totales_opOtrosTributos.appendChild(doc.createTextNode(bd_map.get("opOtrosTributos").toString()));
			  sub_Totales.appendChild(totales_opOtrosTributos);
			  
			  Element totales_opOtrosCargos = doc.createElement("otrosCargos");
			  totales_opOtrosCargos.appendChild(doc.createTextNode(bd_map.get("opOtrosCargos").toString()));
			  sub_Totales.appendChild(totales_opOtrosCargos);
			  
			  Element totales_opDescuentos = doc.createElement("descuentos");
			  totales_opDescuentos.appendChild(doc.createTextNode(bd_map.get("opDescuentos").toString()));
			  sub_Totales.appendChild(totales_opDescuentos);
			  
			  Element totales_opVenta = doc.createElement("venta");
			  totales_opVenta.appendChild(doc.createTextNode(bd_map.get("opVenta").toString()));
			  sub_Totales.appendChild(totales_opVenta);
			  
			  Element totales_opBaseImponiblePercepcion = doc.createElement("baseImponiblePercepcion");
			  totales_opBaseImponiblePercepcion.appendChild(doc.createTextNode(bd_map.get("opBaseImponiblePercepcion").toString()));
			  sub_Totales.appendChild(totales_opBaseImponiblePercepcion);
			  
			  Element totales_opPercepcion = doc.createElement("percepcion");
			  totales_opPercepcion.appendChild(doc.createTextNode(bd_map.get("opPercepcion").toString()));
			  sub_Totales.appendChild(totales_opPercepcion);
			  
			  Element totales_opGratuitas = doc.createElement("opGratuitas");
			  totales_opGratuitas.appendChild(doc.createTextNode(bd_map.get("opGratuitas").toString()));
			  sub_Totales.appendChild(totales_opGratuitas);
			  
			  Element totales_opDescuentosGlobales = doc.createElement("descuentosGlobales");
			  totales_opDescuentosGlobales.appendChild(doc.createTextNode(bd_map.get("opDescuentosGlobales").toString()));
			  sub_Totales.appendChild(totales_opDescuentosGlobales);
			  
			 ///// END ELEMENT TOTALES
			  
			 
			
			
			xml_invoice_line(doc,rootElement );
			xml_invoice_line(doc,rootElement );
			
			///Read The format and return String
			 TransformerFactory transformerFactory = TransformerFactory.newInstance();
		     Transformer transformer = transformerFactory.newTransformer();
		     DOMSource source = new DOMSource(doc);
		     
		     
		     
		     StreamResult result = new StreamResult(new StringWriter());
      	     transformer.transform(source, result);
      	     
      	     str_XML = result.getWriter().toString();
      	     
      	     System.out.println("nuevo TMP: " + str_XML);
		
	  } catch (ParserConfigurationException pce) {
		pce.printStackTrace();
	  } catch (TransformerException tfe) {
		tfe.printStackTrace();
	  }
	  
	  return str_XML;
	  
  }
  
                         
  
  public static HashMap<String, String> xml_invoice_tag_totales(Document doc, Element staff,Invoice invoice)  throws OBException{
	  List<InvoiceLine> invoiceLineList = invoice.getInvoiceLineList();
	  
	  DecimalFormat df = new DecimalFormat("#0.00");
	  df.setRoundingMode(RoundingMode.CEILING);
	  
	  BigDecimal opGravadas = BigDecimal.ZERO;
	  BigDecimal opInafectas = BigDecimal.ZERO;
	  BigDecimal opExoneradas = BigDecimal.ZERO;
	  BigDecimal opIGV = BigDecimal.ZERO;
	  BigDecimal opISC = BigDecimal.ZERO;
	  BigDecimal opOtrosTributos = BigDecimal.ZERO;
	  BigDecimal opOtrosCargos = BigDecimal.ZERO;
	  BigDecimal opDescuentos = BigDecimal.ZERO;
	  BigDecimal opVenta = BigDecimal.ZERO;
	  BigDecimal opBaseImponiblePercepcion = BigDecimal.ZERO;
	  BigDecimal opPercepcion = BigDecimal.ZERO;
	  BigDecimal opGratuitas = BigDecimal.ZERO;
	  BigDecimal opDescuentosGlobales = BigDecimal.ZERO;
	  
	 HashMap<String, String> bd_map = new HashMap<String, String>();
	  
	  for(int i=0;i<invoiceLineList.size();i++){
		  BigDecimal linetnetAmt = invoiceLineList.get(i).getLineNetAmount();
		  if(invoiceLineList.get(i).getTax().getScoSpecialtax().equals("SCOEXEMPT")){
              if(invoiceLineList.get(i).getScrComboItem().getSearchKey().equals("PVTE-Inafecto-porDefecto")){
				//Operaciones Inafectas  
            	  opInafectas = opInafectas.add(linetnetAmt);
			  }
			  else if(invoiceLineList.get(i).getScrComboItem().getSearchKey().equals("PVTE-OtrosTributos-Cargos")){
				//Operaciones Otros Cargos
				  opOtrosCargos = opOtrosCargos.add(linetnetAmt);
			  }
			  else if(invoiceLineList.get(i).getScrComboItem().getSearchKey().equals("PVTE-Exonerado")){
				//Operaciones Exoneradas
				  opExoneradas = opExoneradas.add(linetnetAmt);
			  }
			  else{
				  throw new OBException("Linea de Factura Excenta no sujeta a facturacion electronica");
			  }
		  }
		  else if(invoiceLineList.get(i).getTax().getScoSpecialtax().equals("SCOIGV")){
			  //Operaciones Gravadas
			  opGravadas = opGravadas.add(linetnetAmt);
		  }else{
			  throw new OBException("Linea de Factura Categoria de Impuesto no sujeta a facturacion electronica");
		  }
	  }
	  
	  opVenta = invoice.getGrandTotalAmount();
	  opIGV = invoice.getSsaTotallinestax();
	  
	  bd_map.put("opGravadas", df.format(opGravadas));
	  bd_map.put("opInafectas", df.format(opInafectas));
	  bd_map.put("opExoneradas", df.format(opExoneradas));
	  bd_map.put("opOtrosCargos", df.format(opOtrosCargos));
	  bd_map.put("opOtrosTributos", df.format(opOtrosTributos));
	  bd_map.put("opIGV", df.format(opIGV));
	  bd_map.put("opISC", df.format(opISC));
	  bd_map.put("opDescuentos", df.format(opDescuentos));
	  bd_map.put("opVenta", df.format(opVenta));
	  bd_map.put("opBaseImponiblePercepcion", df.format(opBaseImponiblePercepcion));
	  bd_map.put("opPercepcion", df.format(opPercepcion));
	  bd_map.put("opGratuitas", df.format(opGratuitas));
	  bd_map.put("opDescuentosGlobales", df.format(opDescuentosGlobales));
	  
	  //Agregando etiquetas de Totales
	 return bd_map; 
	  
  }
  
  
  public static void xml_invoice_line(Document doc, Element staff){
	  Element line = doc.createElement("linea");
	  staff.appendChild(line);
	  
	  //Add Child Element
	  Element numItem = doc.createElement("numeroItem");
	  numItem.appendChild(doc.createTextNode("numero"));
	  line.appendChild(numItem);
	  
	  Element uom = doc.createElement("uom");
	  uom.appendChild(doc.createTextNode("Pieza"));
	  line.appendChild(uom);
	  
	  
	  
  }
  
  
  /* public static String xml_Invoice_Original() throws Exception {
	  String str_XML="";
	  try {
		    
		    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			
			// root elements
			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("face");
			doc.appendChild(rootElement);
			
			Element staff = doc.createElement("documento");
			rootElement.appendChild(staff);
			
			// set attribute to staff element
			Attr attr_0 = doc.createAttribute("id");
			 attr_0.setValue("1");
			Attr attr_1 = doc.createAttribute("tipo");
			 attr_1.setValue("factura");
			staff.setAttributeNode(attr_0);
			staff.setAttributeNode(attr_1);
		  
			// shorten way
			// staff.setAttribute("id", "1"); PROBAR DE ESTA OTRA MANERA PARA INSERTAR LOS ATRIBUTOS A UN ELEMENT
			
			// firstname elements
			Element fechaEmision = doc.createElement("fechaEmision");
			fechaEmision.appendChild(doc.createTextNode("18/05/1955"));
			staff.appendChild(fechaEmision);
			
			
			///Read The format and return String
			 TransformerFactory transformerFactory = TransformerFactory.newInstance();
		     Transformer transformer = transformerFactory.newTransformer();
		     DOMSource source = new DOMSource(doc);
		     
		     
		     
		     StreamResult result = new StreamResult(new StringWriter());
      	     transformer.transform(source, result);
      	     
      	     str_XML = result.getWriter().toString();
      	     
      	     System.out.println("tmp: " + str_XML);
		
	  } catch (ParserConfigurationException pce) {
		pce.printStackTrace();
	  } catch (TransformerException tfe) {
		tfe.printStackTrace();
	  }
	  
	  return str_XML;
	  
  }
  */

  
}