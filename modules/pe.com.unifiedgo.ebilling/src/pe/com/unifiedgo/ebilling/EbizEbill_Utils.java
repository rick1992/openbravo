package pe.com.unifiedgo.ebilling;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.Node;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.service.db.DalConnectionProvider;

import pe.com.unifiedgo.core.SOAPClientSAAJ;
import pe.com.unifiedgo.sales.SSA_Utils;

public class EbizEbill_Utils {

  final public static String ERROR_TIMEOUT = "0";
  final public static String ERROR_CANNOTGETINVOICE = "1";
  final public static String ERROR_CANNOTGETINVLINES = "1";

  static Logger log4j = Logger.getLogger(Utility.class);

  final public static boolean donotsend = true;

  /*
   * final public static String defPhyDocNo = "000-0000000"; final public static int phyDocRDigits =
   * 7; final public static int phyDocLDigits = 3;
   */

  public static String encodeBase64(String str, String charsetname) {
    // encode data on your side using BASE64
    byte[] bytesEncoded = Base64.encodeBase64(str.getBytes(Charset.forName(charsetname)));
    return new String(bytesEncoded);
  }

  public static String encodeBase64(String str) {
    // encode data on your side using BASE64
    byte[] bytesEncoded = Base64.encodeBase64(str.getBytes());
    return new String(bytesEncoded);
  }

  public static String decodeBase64(byte[] bytesEncoded) {
    // Decode data on other side, by processing encoded data
    byte[] valueDecoded = Base64.decodeBase64(bytesEncoded);
    return new String(valueDecoded);
  }

  public static void saveToAttachmentDirectory(String path, String filename, String content)
      throws Exception {
    String attachPath = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("attach.path");
    File attachDirectory = new File(attachPath + "/" + path);
    attachDirectory.mkdirs();

    BufferedWriter writer = null;
    String filepath = attachPath + "/" + path + "/" + filename;

    byte[] b = content.getBytes("windows-1252");
    FileOutputStream fileOuputStream = null;
    try {
      fileOuputStream = new FileOutputStream(filepath);
      fileOuputStream.write(b);
    } finally {
      fileOuputStream.close();
    }
  }

  public static String eBizzSend_Factura(String strURLAddress, String strURLEndPoint,
      String cInvoiceId) throws Exception {
    System.out.println("processing cInvoiceId:" + cInvoiceId);
    ConnectionProvider conn = new DalConnectionProvider();

    EbizEbillUtilsData[] factura, facturaLineas;
    EbizEbillUtilsData f, linea;
    factura = EbizEbillUtilsData.selectFactura(conn, cInvoiceId);
    if (factura.length == 0) {
      throw new Exception(ERROR_CANNOTGETINVOICE);
    }
    f = factura[0];

    facturaLineas = EbizEbillUtilsData.selectFacturaLines(conn, cInvoiceId);
    if (facturaLineas.length == 0) {
      throw new Exception(ERROR_CANNOTGETINVLINES);
    }

    String formato = "", formatoLinea = "", formatoTotalLinea = "", formatoEncoded64, nombreArchivo;
    String clienteTipoDocumento, facturaLeyenda, itemAfectacionIGV;
    DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");

    // llenado formato para cabecera de factura
    clienteTipoDocumento = EbizEbill_Catalogo.getCatalogo06(f.clientetipodocumento);
    facturaLeyenda = (f.estitulogratuito.compareTo("Y") == 0) ? "TRANSFERENCIA GRATUITA DE UN BIEN Y/O SERVICIO PRESTADO GRATUITAMENTE"
        : SSA_Utils.getAmountDescription(f.facturaimportetotal);

    formato = "CF|" + df2.format(df.parse(f.fechaemision)) + "|" + f.emisorruc + "|" + "01" + "|"
        + f.correlativo + "|" + f.clientenrodocumento + "|" + clienteTipoDocumento + "|"
        + f.clienterazonsocial + "|" + f.moneda + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopgrabadas) + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopinafectas) + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopexoneradas) + "|"
        + SSA_Utils.formatNumber(f.facturatotaligv) + "|"
        + SSA_Utils.formatNumber(f.facturatotalisc) + "|"
        + SSA_Utils.formatNumber(f.facturatotaltributos) + "|"
        + SSA_Utils.formatNumber(f.facturaotroscargos) + "|"
        + SSA_Utils.formatNumber(f.facturatotalimportedscto) + "|"
        + SSA_Utils.formatNumber(f.facturaimportetotal) + "|" + facturaLeyenda + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopgratuitas) + "|"
        + SSA_Utils.formatNumber(f.facturadsctoglobales) + "|" + f.clientedireccion + "|"
        + f.clientetelefono + "|" + f.clientecorreo + "|" + f.adicional1 + "|" + f.adicional2 + "|"
        + f.adicional3 + "|" + f.adicional4 + "|" + f.codigoLeyenda + "|" + f.control + "|"
        + f.documentorelacionado + "|" + "99" + "|";
    System.out.println("formato de cabecera:" + formato);

    // llenado formato para las lineas de la factura
    for (int i = 0; i < facturaLineas.length; i++) {
      linea = facturaLineas[i];

      itemAfectacionIGV = EbizEbill_Catalogo.getCatalogo07(linea.specialtax);

      formatoLinea = "\nDF" + "|" + linea.numeroitem + "|" + linea.productocodigo + "|"
          + linea.itemunidadmedida + "|" + linea.itemcantidad + "|" + linea.description + "|"
          + SSA_Utils.formatNumber(linea.itemvalorunitario) + "|"
          + SSA_Utils.formatNumber(linea.itempreciovtaunitario) + "|"
          + SSA_Utils.formatNumber(linea.itemigv) + "|" + itemAfectacionIGV + "|"
          + SSA_Utils.formatNumber(linea.itemmontoisc) + "|" + "" + "|"
          + SSA_Utils.formatNumber(linea.itemtotalvalorvta) + "|"
          + SSA_Utils.formatNumber(linea.valorrefopnoonerosas) + "|" + linea.itemindicadordsctos
          + "|" + SSA_Utils.formatNumber(linea.itemmontodsctos) + "|" + linea.adicional1 + "|"
          + linea.adicional2 + "|";

      formatoTotalLinea = formatoTotalLinea + formatoLinea;
    }
    System.out.println("formato de Lineas:" + formatoTotalLinea);
    formato = formato + formatoTotalLinea;
    System.out.println("formato Total de factura:" + formato);

    // enconding data
    formatoEncoded64 = encodeBase64(formato, "ISO-8859-1");

    nombreArchivo = f.emisorruc + "-" + "01" + "-" + f.correlativo + ".txt";

    try {
      saveToAttachmentDirectory("ebill", nombreArchivo, formato);
    } catch (Exception e) {
      System.out.println("Couldnt write: " + nombreArchivo);
    }

    //
    // sending data and getting response
    SOAPMessage SOAPrequest = null, SOAPresponse = null;
    String responseStatus = "AR";

    if (!donotsend) {
      SOAPrequest = createSOAPRequestSendInvoice(strURLEndPoint, nombreArchivo, formatoEncoded64);

      SOAPresponse = SOAPClientSAAJ.createSOAPConnection(SOAPrequest, strURLAddress);
      responseStatus = eBizz_getResponseStatus(SOAPresponse);

      responseStatus = "AR";

      try {
        saveToAttachmentDirectory("ebill", "out_" + nombreArchivo + ".out",
            SOAPClientSAAJ.getSOAPResponseStringResult(SOAPresponse));
      } catch (Exception e) {
        System.out.println("Couldnt write: " + nombreArchivo);
      }
    }

    return responseStatus;
  }

  public static String eBizzSend_FacturaGuia(String strURLAddress, String strURLEndPoint,
      String cInvoiceId) throws Exception {
    System.out.println("processing cInvoiceId:" + cInvoiceId);
    ConnectionProvider conn = new DalConnectionProvider();

    EbizEbillUtilsData[] factura, facturaLineas;
    EbizEbillUtilsData f, linea;
    factura = EbizEbillUtilsData.selectFacturaGuia(conn, cInvoiceId);
    if (factura.length == 0) {
      throw new Exception(ERROR_CANNOTGETINVOICE);
    }
    f = factura[0];

    facturaLineas = EbizEbillUtilsData.selectFacturaLines(conn, cInvoiceId);
    if (facturaLineas.length == 0) {
      throw new Exception(ERROR_CANNOTGETINVLINES);
    }

    String formato = "", formatoLinea = "", formatoTotalLinea = "", formatoEncoded64, nombreArchivo;
    String clienteTipoDocumento, facturaLeyenda, itemAfectacionIGV;
    DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");

    // llenado formato para cabecera de factura
    clienteTipoDocumento = EbizEbill_Catalogo.getCatalogo06(f.clientetipodocumento);
    facturaLeyenda = (f.estitulogratuito.compareTo("Y") == 0) ? "TRANSFERENCIA GRATUITA DE UN BIEN Y/O SERVICIO PRESTADO GRATUITAMENTE"
        : SSA_Utils.getAmountDescription(f.facturaimportetotal);

    formato = "CF|" + df2.format(df.parse(f.fechaemision)) + "|" + f.emisorruc + "|" + "01" + "|"
        + f.correlativo + "|" + f.clientenrodocumento + "|" + clienteTipoDocumento + "|"
        + f.clienterazonsocial + "|" + f.moneda + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopgrabadas) + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopinafectas) + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopexoneradas) + "|"
        + SSA_Utils.formatNumber(f.facturatotaligv) + "|"
        + SSA_Utils.formatNumber(f.facturatotalisc) + "|"
        + SSA_Utils.formatNumber(f.facturatotaltributos) + "|"
        + SSA_Utils.formatNumber(f.facturaotroscargos) + "|"
        + SSA_Utils.formatNumber(f.facturatotalimportedscto) + "|"
        + SSA_Utils.formatNumber(f.facturaimportetotal) + "|" + facturaLeyenda + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopgratuitas) + "|"
        + SSA_Utils.formatNumber(f.facturadsctoglobales) + "|" + f.clientedireccion + "|"
        + f.clientetelefono + "|" + f.clientecorreo + "|" + f.adicional1 + "|" + f.adicional2 + "|"
        + f.adicional3 + "|" + f.adicional4 + "|" + f.codigoLeyenda + "|" + f.control + "|"
        + f.ppartidaUbigeo + "|" + f.ppartidaDireccion + "|" + f.ppartidaUrbanizacion + "|"
        + f.ppartidaProvincia + "|" + f.ppartidaDepartamento + "|" + f.ppartidaDistrito + "|"
        + f.pllegadaUbigeo + "|" + f.pllegadaDireccion + "|" + f.pllegadaUrbanizacion + "|"
        + f.pllegadaProvincia + "|" + f.pllegadaDepartamento + "|" + f.pllegadaDistrito
        + "||||||||||";
    System.out.println("formato de cabecera:" + formato);

    // llenado formato para las lineas de la factura
    for (int i = 0; i < facturaLineas.length; i++) {
      linea = facturaLineas[i];

      itemAfectacionIGV = EbizEbill_Catalogo.getCatalogo07(linea.specialtax);

      formatoLinea = "\nDF" + "|" + linea.numeroitem + "|" + linea.productocodigo + "|"
          + linea.itemunidadmedida + "|" + linea.itemcantidad + "|" + linea.description + "|"
          + SSA_Utils.formatNumber(linea.itemvalorunitario) + "|"
          + SSA_Utils.formatNumber(linea.itempreciovtaunitario) + "|"
          + SSA_Utils.formatNumber(linea.itemigv) + "|" + itemAfectacionIGV + "|"
          + SSA_Utils.formatNumber(linea.itemmontoisc) + "|" + "" + "|"
          + SSA_Utils.formatNumber(linea.itemtotalvalorvta) + "|"
          + SSA_Utils.formatNumber(linea.valorrefopnoonerosas) + "|" + linea.itemindicadordsctos
          + "|" + SSA_Utils.formatNumber(linea.itemmontodsctos) + "|" + linea.adicional1 + "|"
          + linea.adicional2 + "|";

      formatoTotalLinea = formatoTotalLinea + formatoLinea;
    }
    System.out.println("formato de Lineas:" + formatoTotalLinea);
    formato = formato + formatoTotalLinea;
    System.out.println("formato Total de factura:" + formato);

    // enconding data
    formatoEncoded64 = encodeBase64(formato, "ISO-8859-1");

    nombreArchivo = f.emisorruc + "-" + "01" + "-" + f.correlativo + ".txt";

    try {
      saveToAttachmentDirectory("ebill", nombreArchivo, formato);
    } catch (Exception e) {
      System.out.println("Couldnt write: " + nombreArchivo);
    }

    //
    // sending data and getting response
    SOAPMessage SOAPrequest = null, SOAPresponse = null;
    String responseStatus = "AR";

    if (!donotsend) {

      SOAPrequest = createSOAPRequestSendInvoice(strURLEndPoint, nombreArchivo, formatoEncoded64);

      SOAPresponse = SOAPClientSAAJ.createSOAPConnection(SOAPrequest, strURLAddress);
      responseStatus = eBizz_getResponseStatus(SOAPresponse);

      responseStatus = "AR";

      try {
        saveToAttachmentDirectory("ebill", "out_" + nombreArchivo + ".out",
            SOAPClientSAAJ.getSOAPResponseStringResult(SOAPresponse));
      } catch (Exception e) {
        System.out.println("Couldnt write: " + nombreArchivo);
      }
    }
    return responseStatus;
  }

  public static String eBizzSend_FacturaDetraccion(String strURLAddress, String strURLEndPoint,
      String cInvoiceId) throws Exception {
    ConnectionProvider conn = new DalConnectionProvider();

    EbizEbillUtilsData[] factura, facturaLineas;
    EbizEbillUtilsData f, linea;
    factura = EbizEbillUtilsData.selectFacturaDetraccion(conn, cInvoiceId);
    if (factura.length == 0) {
      throw new Exception(ERROR_CANNOTGETINVOICE);
    }
    f = factura[0];

    facturaLineas = EbizEbillUtilsData.selectFacturaDetraccionLines(conn, cInvoiceId);
    if (facturaLineas.length == 0) {
      throw new Exception(ERROR_CANNOTGETINVLINES);
    }

    String formato = "", formatoLinea = "", formatoTotalLinea = "", formatoEncoded64, nombreArchivo;
    String clienteTipoDocumento, facturaLeyenda, itemAfectacionIGV;
    DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");

    // llenado formato para cabecera de factura con detraccion
    clienteTipoDocumento = EbizEbill_Catalogo.getCatalogo06(f.clientetipodocumento);
    facturaLeyenda = (f.estitulogratuito.compareTo("Y") == 0) ? "TRANSFERENCIA GRATUITA DE UN BIEN Y/O SERVICIO PRESTADO GRATUITAMENTE"
        : SSA_Utils.getAmountDescription(f.facturaimportetotal);

    formato = "CF|" + df2.format(df.parse(f.fechaemision)) + "|" + f.emisorruc + "|" + "01" + "|"
        + f.correlativo + "|" + f.clientenrodocumento + "|" + clienteTipoDocumento + "|"
        + f.clienterazonsocial + "|" + f.moneda + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopgrabadas) + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopinafectas) + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopexoneradas) + "|"
        + SSA_Utils.formatNumber(f.facturatotaligv) + "|"
        + SSA_Utils.formatNumber(f.facturatotalisc) + "|"
        + SSA_Utils.formatNumber(f.facturatotaltributos) + "|"
        + SSA_Utils.formatNumber(f.facturaotroscargos) + "|"
        + SSA_Utils.formatNumber(f.facturatotalimportedscto) + "|"
        + SSA_Utils.formatNumber(f.facturaimportetotal) + "|" + facturaLeyenda + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopgratuitas) + "|"
        + SSA_Utils.formatNumber(f.facturadsctoglobales) + "|" + f.clientedireccion + "|"
        + f.clientetelefono + "|" + f.clientecorreo + "|" + f.adicional1 + "|" + f.adicional2 + "|"
        + f.adicional3 + "|" + f.adicional4 + "|" + f.codigoLeyenda + "|" + f.control + "|"
        + f.dtbienserviciocodigo + "|" + f.dtbienserviciovalor + "|" + f.dtcuentacorrientecodigo
        + "|" + f.dtcuentacorrientevalor + "|" + f.dtdetraccionescodigo + "|"
        + f.dtdetraccionporcentaje + "|" + f.dtdetraccionmontovalor + "|" + f.dtnombrematriculacode
        + "|" + f.dtnombreembarcacion + "|" + f.dtmatriculaembarcacion + "|"
        + f.dttipocantidadespeciecodigo + "|" + f.dttipoespecie + "|" + f.dtcantidadespecie + "|"
        + f.dtlugardescargacodigo + "|" + f.dtlugardescargavalor + "|" + f.dtfechadescargacodigo
        + "|" + f.dtfechadescargavalor + "|";
    System.out.println("formato de cabecera:" + formato);

    // llenado formato para las lineas de la factura con detraccion
    for (int i = 0; i < facturaLineas.length; i++) {
      linea = facturaLineas[i];

      itemAfectacionIGV = EbizEbill_Catalogo.getCatalogo07(linea.specialtax);

      formatoLinea = "\nDF" + "|" + linea.numeroitem + "|" + linea.productocodigo + "|"
          + linea.itemunidadmedida + "|" + linea.itemcantidad + "|" + linea.description + "|"
          + SSA_Utils.formatNumber(linea.itemvalorunitario) + "|"
          + SSA_Utils.formatNumber(linea.itempreciovtaunitario) + "|"
          + SSA_Utils.formatNumber(linea.itemigv) + "|" + itemAfectacionIGV + "|"
          + SSA_Utils.formatNumber(linea.itemmontoisc) + "|" + "" + "|"
          + SSA_Utils.formatNumber(linea.itemtotalvalorvta) + "|"
          + SSA_Utils.formatNumber(linea.valorrefopnoonerosas) + "|" + linea.itemindicadordsctos
          + "|" + SSA_Utils.formatNumber(linea.itemmontodsctos) + "|" + linea.adicional1 + "|"
          + linea.adicional2 + "|";

      formatoTotalLinea = formatoTotalLinea + formatoLinea;
    }
    System.out.println("formato de Lineas:" + formatoTotalLinea);
    formato = formato + formatoTotalLinea;
    System.out.println("formato Total de factura:" + formato);

    // enconding data
    formatoEncoded64 = encodeBase64(formato, "ISO-8859-1");

    nombreArchivo = f.emisorruc + "-" + "01" + "-" + f.correlativo + ".txt";

    try {
      saveToAttachmentDirectory("ebill", nombreArchivo, formato);
    } catch (Exception e) {
      System.out.println("Couldnt write: " + nombreArchivo);
    }

    //
    // sending data and getting response
    SOAPMessage SOAPrequest = null, SOAPresponse = null;
    String responseStatus = "AR";

    if (!donotsend) {
      SOAPrequest = createSOAPRequestSendInvoice(strURLEndPoint, nombreArchivo, formatoEncoded64);

      SOAPresponse = SOAPClientSAAJ.createSOAPConnection(SOAPrequest, strURLAddress);
      responseStatus = eBizz_getResponseStatus(SOAPresponse);

      responseStatus = "AR";

      try {
        saveToAttachmentDirectory("ebill", "out_" + nombreArchivo + ".out",
            SOAPClientSAAJ.getSOAPResponseStringResult(SOAPresponse));
      } catch (Exception e) {
        System.out.println("Couldnt write: " + nombreArchivo);
      }
    }
    return responseStatus;
  }

  public static String eBizzSend_Anticipo(String strURLAddress, String strURLEndPoint,
      String cInvoiceId) throws Exception {
    ConnectionProvider conn = new DalConnectionProvider();

    EbizEbillUtilsData[] factura, facturaLineas;
    EbizEbillUtilsData f, linea;
    factura = EbizEbillUtilsData.selectAnticipo(conn, cInvoiceId);
    if (factura.length == 0) {
      throw new Exception(ERROR_CANNOTGETINVOICE);
    }
    f = factura[0];

    facturaLineas = EbizEbillUtilsData.selectAnticipoLines(conn, cInvoiceId);
    if (facturaLineas.length == 0) {
      throw new Exception(ERROR_CANNOTGETINVLINES);
    }

    String formato = "", formatoLinea = "", formatoTotalLinea = "", formatoEncoded64, nombreArchivo;
    String clienteTipoDocumento, facturaLeyenda, itemAfectacionIGV;
    DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");

    // llenado formato para cabecera de anticipo
    clienteTipoDocumento = EbizEbill_Catalogo.getCatalogo06(f.clientetipodocumento);
    facturaLeyenda = SSA_Utils.getAmountDescription(f.facturaimportetotal);

    formato = "CF|" + df2.format(df.parse(f.fechaemision)) + "|" + f.emisorruc + "|" + "01" + "|"
        + f.correlativo + "|" + f.clientenrodocumento + "|" + clienteTipoDocumento + "|"
        + f.clienterazonsocial + "|" + f.moneda + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopgrabadas) + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopinafectas) + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopexoneradas) + "|"
        + SSA_Utils.formatNumber(f.facturatotaligv) + "|"
        + SSA_Utils.formatNumber(f.facturatotalisc) + "|"
        + SSA_Utils.formatNumber(f.facturatotaltributos) + "|"
        + SSA_Utils.formatNumber(f.facturaotroscargos) + "|"
        + SSA_Utils.formatNumber(f.facturatotalimportedscto) + "|"
        + SSA_Utils.formatNumber(f.facturaimportetotal) + "|" + facturaLeyenda + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopgratuitas) + "|"
        + SSA_Utils.formatNumber(f.facturadsctoglobales) + "|" + f.clientedireccion + "|"
        + f.clientetelefono + "|" + f.clientecorreo + "|" + f.adicional1 + "|" + f.adicional2 + "|"
        + f.adicional3 + "|" + f.adicional4 + "|" + f.codigoLeyenda + "|" + f.control + "|"
        + f.tipooperacionfactura + "|" + f.documentoanticipo + "|" + f.documentorelacionado + "|"
        + f.nrodocumentooriginal + "|";
    System.out.println("formato de cabecera:" + formato);

    // llenado formato para las lineas de la anticipo
    for (int i = 0; i < facturaLineas.length; i++) {
      linea = facturaLineas[i];

      itemAfectacionIGV = EbizEbill_Catalogo.getCatalogo07(linea.specialtax);

      formatoLinea = "\nDF" + "|" + linea.numeroitem + "|" + linea.productocodigo + "|"
          + linea.itemunidadmedida + "|" + linea.itemcantidad + "|" + linea.description + "|"
          + SSA_Utils.formatNumber(linea.itemvalorunitario) + "|"
          + SSA_Utils.formatNumber(linea.itempreciovtaunitario) + "|"
          + SSA_Utils.formatNumber(linea.itemigv) + "|" + itemAfectacionIGV + "|"
          + SSA_Utils.formatNumber(linea.itemmontoisc) + "|" + "" + "|"
          + SSA_Utils.formatNumber(linea.itemtotalvalorvta) + "|"
          + SSA_Utils.formatNumber(linea.valorrefopnoonerosas) + "|" + linea.itemindicadordsctos
          + "|" + SSA_Utils.formatNumber(linea.itemmontodsctos) + "|" + linea.adicional1 + "|"
          + linea.adicional2 + "|";

      formatoTotalLinea = formatoTotalLinea + formatoLinea;
    }
    System.out.println("formato de Lineas:" + formatoTotalLinea);
    formato = formato + formatoTotalLinea;
    System.out.println("formato Total de factura:" + formato);

    // enconding data
    formatoEncoded64 = encodeBase64(formato, "ISO-8859-1");

    nombreArchivo = f.emisorruc + "-" + "01" + "-" + f.correlativo + ".txt";

    try {
      saveToAttachmentDirectory("ebill", nombreArchivo, formato);
    } catch (Exception e) {
      System.out.println("Couldnt write: " + nombreArchivo);
    }

    //
    // sending data and getting response
    SOAPMessage SOAPrequest = null, SOAPresponse = null;
    String responseStatus = "AR";

    if (!donotsend) {
      SOAPrequest = createSOAPRequestSendInvoice(strURLEndPoint, nombreArchivo, formatoEncoded64);

      SOAPresponse = SOAPClientSAAJ.createSOAPConnection(SOAPrequest, strURLAddress);
      responseStatus = eBizz_getResponseStatus(SOAPresponse);

      responseStatus = "AR";

      try {
        saveToAttachmentDirectory("ebill", "out_" + nombreArchivo + ".out",
            SOAPClientSAAJ.getSOAPResponseStringResult(SOAPresponse));
      } catch (Exception e) {
        System.out.println("Couldnt write: " + nombreArchivo);
      }
    }
    return responseStatus;
  }

  public static String eBizzSend_FacturaConAnticipo(String strURLAddress, String strURLEndPoint,
      String cInvoiceId) throws Exception {
    ConnectionProvider conn = new DalConnectionProvider();

    EbizEbillUtilsData[] factura, facturaLineas;
    EbizEbillUtilsData f, linea;
    factura = EbizEbillUtilsData.selectFacturaConAnticipo(conn, cInvoiceId);
    if (factura.length == 0) {
      throw new Exception(ERROR_CANNOTGETINVOICE);
    }
    f = factura[0];

    facturaLineas = EbizEbillUtilsData.selectFacturaConAnticipoLines(conn, cInvoiceId);
    if (facturaLineas.length == 0) {
      throw new Exception(ERROR_CANNOTGETINVLINES);
    }

    String formato = "", formatoLinea = "", formatoTotalLinea = "", formatoEncoded64, nombreArchivo;
    String clienteTipoDocumento, facturaLeyenda, itemAfectacionIGV;
    DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");

    // llenado formato para cabecera de anticipo
    clienteTipoDocumento = EbizEbill_Catalogo.getCatalogo06(f.clientetipodocumento);
    facturaLeyenda = SSA_Utils.getAmountDescription(f.facturaimportetotal);

    formato = "CF|" + df2.format(df.parse(f.fechaemision)) + "|" + f.emisorruc + "|" + "01" + "|"
        + f.correlativo + "|" + f.clientenrodocumento + "|" + clienteTipoDocumento + "|"
        + f.clienterazonsocial + "|" + f.moneda + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopgrabadas) + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopinafectas) + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopexoneradas) + "|"
        + SSA_Utils.formatNumber(f.facturatotaligv) + "|"
        + SSA_Utils.formatNumber(f.facturatotalisc) + "|"
        + SSA_Utils.formatNumber(f.facturatotaltributos) + "|"
        + SSA_Utils.formatNumber(f.facturaotroscargos) + "|"
        + SSA_Utils.formatNumber(f.facturatotalimportedscto) + "|"
        + SSA_Utils.formatNumber(f.facturaimportetotal) + "|" + facturaLeyenda + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopgratuitas) + "|"
        + SSA_Utils.formatNumber(f.facturadsctoglobales) + "|" + f.clientedireccion + "|"
        + f.clientetelefono + "|" + f.clientecorreo + "|" + f.adicional1 + "|" + f.adicional2 + "|"
        + f.adicional3 + "|" + f.adicional4 + "|" + f.codigoLeyenda + "|" + f.control + "|"
        + f.tipooperacionfactura + "|" + f.documentoanticipo + "|" + f.documentorelacionado + "|"
        + f.nrodocumentooriginal + "|";
    System.out.println("formato de cabecera:" + formato);

    // llenado formato para las lineas de la anticipo
    for (int i = 0; i < facturaLineas.length; i++) {
      linea = facturaLineas[i];

      itemAfectacionIGV = EbizEbill_Catalogo.getCatalogo07(linea.specialtax);

      formatoLinea = "\nDF" + "|" + linea.numeroitem + "|" + linea.productocodigo + "|"
          + linea.itemunidadmedida + "|" + linea.itemcantidad + "|" + linea.description + "|"
          + SSA_Utils.formatNumber(linea.itemvalorunitario) + "|"
          + SSA_Utils.formatNumber(linea.itempreciovtaunitario) + "|"
          + SSA_Utils.formatNumber(linea.itemigv) + "|" + itemAfectacionIGV + "|"
          + SSA_Utils.formatNumber(linea.itemmontoisc) + "|" + "" + "|"
          + SSA_Utils.formatNumber(linea.itemtotalvalorvta) + "|"
          + SSA_Utils.formatNumber(linea.valorrefopnoonerosas) + "|" + linea.itemindicadordsctos
          + "|" + SSA_Utils.formatNumber(linea.itemmontodsctos) + "|" + linea.adicional1 + "|"
          + linea.adicional2 + "|";

      formatoTotalLinea = formatoTotalLinea + formatoLinea;
    }
    System.out.println("formato de Lineas:" + formatoTotalLinea);
    formato = formato + formatoTotalLinea;
    System.out.println("formato Total de factura:" + formato);

    // enconding data
    formatoEncoded64 = encodeBase64(formato, "ISO-8859-1");

    nombreArchivo = f.emisorruc + "-" + "01" + "-" + f.correlativo + ".txt";

    try {
      saveToAttachmentDirectory("ebill", nombreArchivo, formato);
    } catch (Exception e) {
      System.out.println("Couldnt write: " + nombreArchivo);
    }

    //
    // sending data and getting response
    SOAPMessage SOAPrequest = null, SOAPresponse = null;
    String responseStatus = "AR";

    if (!donotsend) {
      SOAPrequest = createSOAPRequestSendInvoice(strURLEndPoint, nombreArchivo, formatoEncoded64);

      SOAPresponse = SOAPClientSAAJ.createSOAPConnection(SOAPrequest, strURLAddress);
      responseStatus = eBizz_getResponseStatus(SOAPresponse);

      responseStatus = "AR";

      try {
        saveToAttachmentDirectory("ebill", "out_" + nombreArchivo + ".out",
            SOAPClientSAAJ.getSOAPResponseStringResult(SOAPresponse));
      } catch (Exception e) {
        System.out.println("Couldnt write: " + nombreArchivo);
      }
    }
    return responseStatus;
  }

  public static String eBizzSend_FacturaPercepcion(String strURLAddress, String strURLEndPoint,
      String cInvoiceId) throws Exception {
    ConnectionProvider conn = new DalConnectionProvider();

    EbizEbillUtilsData[] factura, facturaLineas;
    EbizEbillUtilsData f, linea;
    factura = EbizEbillUtilsData.selectFacturaPercepcion(conn, cInvoiceId);
    if (factura.length == 0) {
      throw new Exception(ERROR_CANNOTGETINVOICE);
    }
    f = factura[0];

    facturaLineas = EbizEbillUtilsData.selectFacturaPercepcionLines(conn, cInvoiceId);
    if (facturaLineas.length == 0) {
      throw new Exception(ERROR_CANNOTGETINVLINES);
    }

    String formato = "", formatoLinea = "", formatoTotalLinea = "", formatoEncoded64, nombreArchivo;
    String clienteTipoDocumento, facturaLeyenda, itemAfectacionIGV;
    DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");

    // llenado formato para cabecera de factura con percepcion
    clienteTipoDocumento = EbizEbill_Catalogo.getCatalogo06(f.clientetipodocumento);
    facturaLeyenda = (f.estitulogratuito.compareTo("Y") == 0) ? "TRANSFERENCIA GRATUITA DE UN BIEN Y/O SERVICIO PRESTADO GRATUITAMENTE"
        : SSA_Utils.getAmountDescription(f.facturaimportetotal);

    formato = "CF|" + df2.format(df.parse(f.fechaemision)) + "|" + f.emisorruc + "|" + "01" + "|"
        + f.correlativo + "|" + f.clientenrodocumento + "|" + clienteTipoDocumento + "|"
        + f.clienterazonsocial + "|" + f.moneda + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopgrabadas) + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopinafectas) + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopexoneradas) + "|"
        + SSA_Utils.formatNumber(f.facturatotaligv) + "|"
        + SSA_Utils.formatNumber(f.facturatotalisc) + "|"
        + SSA_Utils.formatNumber(f.facturatotaltributos) + "|"
        + SSA_Utils.formatNumber(f.facturaotroscargos) + "|"
        + SSA_Utils.formatNumber(f.facturatotalimportedscto) + "|"
        + SSA_Utils.formatNumber(f.facturaimportetotal) + "|" + facturaLeyenda + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopgratuitas) + "|"
        + SSA_Utils.formatNumber(f.facturadsctoglobales) + "|" + f.clientedireccion + "|"
        + f.clientetelefono + "|" + f.clientecorreo + "|" + f.adicional1 + "|" + f.adicional2 + "|"
        + f.adicional3 + "|" + f.adicional4 + "|" + f.codigoLeyenda + "|" + f.control + "|"
        + f.percepcionbase + "|" + f.percepcionmonto + "|" + f.percepciontotalmonto + "|";
    System.out.println("formato de cabecera:" + formato);

    // llenado formato para las lineas de la factura con percepcion
    for (int i = 0; i < facturaLineas.length; i++) {
      linea = facturaLineas[i];

      itemAfectacionIGV = EbizEbill_Catalogo.getCatalogo07(linea.specialtax);

      formatoLinea = "\nDF" + "|" + linea.numeroitem + "|" + linea.productocodigo + "|"
          + linea.itemunidadmedida + "|" + linea.itemcantidad + "|" + linea.description + "|"
          + SSA_Utils.formatNumber(linea.itemvalorunitario) + "|"
          + SSA_Utils.formatNumber(linea.itempreciovtaunitario) + "|"
          + SSA_Utils.formatNumber(linea.itemigv) + "|" + itemAfectacionIGV + "|"
          + SSA_Utils.formatNumber(linea.itemmontoisc) + "|" + "" + "|"
          + SSA_Utils.formatNumber(linea.itemtotalvalorvta) + "|"
          + SSA_Utils.formatNumber(linea.valorrefopnoonerosas) + "|" + linea.itemindicadordsctos
          + "|" + SSA_Utils.formatNumber(linea.itemmontodsctos) + "|" + linea.adicional1 + "|"
          + linea.adicional2 + "|";

      formatoTotalLinea = formatoTotalLinea + formatoLinea;
    }
    System.out.println("formato de Lineas:" + formatoTotalLinea);
    formato = formato + formatoTotalLinea;
    System.out.println("formato Total de factura:" + formato);

    // enconding data
    formatoEncoded64 = encodeBase64(formato, "ISO-8859-1");

    nombreArchivo = f.emisorruc + "-" + "01" + "-" + f.correlativo + ".txt";

    try {
      saveToAttachmentDirectory("ebill", nombreArchivo, formato);
    } catch (Exception e) {
      System.out.println("Couldnt write: " + nombreArchivo);
    }

    //
    // sending data and getting response
    SOAPMessage SOAPrequest = null, SOAPresponse = null;
    String responseStatus = "AR";

    if (!donotsend) {
      SOAPrequest = createSOAPRequestSendInvoice(strURLEndPoint, nombreArchivo, formatoEncoded64);

      SOAPresponse = SOAPClientSAAJ.createSOAPConnection(SOAPrequest, strURLAddress);
      responseStatus = eBizz_getResponseStatus(SOAPresponse);

      responseStatus = "AR";

      try {
        saveToAttachmentDirectory("ebill", "out_" + nombreArchivo + ".out",
            SOAPClientSAAJ.getSOAPResponseStringResult(SOAPresponse));
      } catch (Exception e) {
        System.out.println("Couldnt write: " + nombreArchivo);
      }
    }
    return responseStatus;
  }

  public static String eBizzSend_Boleta(String strURLAddress, String strURLEndPoint,
      String cInvoiceId) throws Exception {
    ConnectionProvider conn = new DalConnectionProvider();

    EbizEbillUtilsData[] boleta, boletaLineas;
    EbizEbillUtilsData b, linea;
    boleta = EbizEbillUtilsData.selectBoleta(conn, cInvoiceId);
    if (boleta.length == 0) {
      throw new Exception(ERROR_CANNOTGETINVOICE);
    }
    b = boleta[0];

    boletaLineas = EbizEbillUtilsData.selectBoletaLines(conn, cInvoiceId);
    if (boleta.length == 0) {
      throw new Exception(ERROR_CANNOTGETINVLINES);
    }

    String formato = "", formatoLinea = "", formatoTotalLinea = "", formatoEncoded64, nombreArchivo;
    String clienteTipoDocumento, facturaLeyenda, itemAfectacionIGV;
    DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");

    // llenado formato para cabecera de boleta
    clienteTipoDocumento = EbizEbill_Catalogo.getCatalogo06(b.clientetipodocumento);
    facturaLeyenda = (b.estitulogratuito.compareTo("Y") == 0) ? "TRANSFERENCIA GRATUITA DE UN BIEN Y/O SERVICIO PRESTADO GRATUITAMENTE"
        : SSA_Utils.getAmountDescription(b.facturaimportetotal);

    formato = "CB|" + df2.format(df.parse(b.fechaemision)) + "|" + b.emisorruc + "|" + "03" + "|"
        + b.correlativo + "|" + b.clientenrodocumento + "|" + clienteTipoDocumento + "|"
        + b.clienterazonsocial + "|" + b.moneda + "|"
        + SSA_Utils.formatNumber(b.facturatotalvtaopgrabadas) + "|"
        + SSA_Utils.formatNumber(b.facturatotalvtaopinafectas) + "|"
        + SSA_Utils.formatNumber(b.facturatotalvtaopexoneradas) + "|"
        + SSA_Utils.formatNumber(b.facturatotaligv) + "|"
        + SSA_Utils.formatNumber(b.facturatotalisc) + "|"
        + SSA_Utils.formatNumber(b.facturatotaltributos) + "|"
        + SSA_Utils.formatNumber(b.facturaotroscargos) + "|"
        + SSA_Utils.formatNumber(b.facturatotalimportedscto) + "|"
        + SSA_Utils.formatNumber(b.facturaimportetotal) + "|"
        + SSA_Utils.formatNumber(b.facturatotalvtaopgratuitas) + "|"
        + SSA_Utils.formatNumber(b.facturadsctoglobales) + "|" + facturaLeyenda + "|"
        + b.clientedireccion + "|" + b.clientetelefono + "|" + b.clientecorreo + "|" + b.adicional1
        + "|" + b.adicional2 + "|" + b.adicional3 + "|" + b.codigoLeyenda + "|"
        + b.guiaremisionnumero + "|" + "09"
        + "|" // guiaremisiontipo catalogo1
        + b.tipooperacionfactura + "|" + b.percepcionmnbase + "|" + b.percepcionmnmonto + "|"
        + b.percepcionmntotalmonto + "|" + b.documentoanticipo + "|";

    System.out.println("formato de cabecera:" + formato);

    // llenado formato para las lineas de la boleta
    for (int i = 0; i < boletaLineas.length; i++) {
      linea = boletaLineas[i];

      itemAfectacionIGV = EbizEbill_Catalogo.getCatalogo07(linea.specialtax);

      formatoLinea = "\nDB" + "|" + linea.numeroitem + "|" + linea.productocodigo + "|"
          + linea.itemunidadmedida + "|" + linea.itemcantidad + "|" + linea.description + "|"
          + SSA_Utils.formatNumber(linea.itemvalorunitario) + "|"
          + SSA_Utils.formatNumber(linea.itempreciovtaunitario) + "|"
          + SSA_Utils.formatNumber(linea.itemigv) + "|" + itemAfectacionIGV + "|"
          + SSA_Utils.formatNumber(linea.itemmontoisc) + "|" + "" + "|"
          + SSA_Utils.formatNumber(linea.itemtotalvalorvta) + "|"
          + SSA_Utils.formatNumber(linea.valorrefopnoonerosas) + "|" + linea.itemindicadordsctos
          + "|" + SSA_Utils.formatNumber(linea.itemmontodsctos) + "|" + linea.adicional1 + "|";

      formatoTotalLinea = formatoTotalLinea + formatoLinea;
    }

    System.out.println("formato de Lineas:" + formatoTotalLinea);
    formato = formato + formatoTotalLinea;
    System.out.println("formato Total de factura:" + formato);

    // enconding data
    formatoEncoded64 = encodeBase64(formato, "ISO-8859-1");
    nombreArchivo = b.emisorruc + "-" + "03" + "-" + b.correlativo + ".txt";

    try {
      saveToAttachmentDirectory("ebill", nombreArchivo, formato);
    } catch (Exception e) {
      System.out.println("Couldnt write: " + nombreArchivo);
    }

    // sending data and getting response
    SOAPMessage SOAPrequest = null, SOAPresponse = null;
    String responseStatus = "AR";

    if (!donotsend) {
      SOAPrequest = createSOAPRequestSendInvoice(strURLEndPoint, nombreArchivo, formatoEncoded64);

      SOAPresponse = SOAPClientSAAJ.createSOAPConnection(SOAPrequest, strURLAddress);
      responseStatus = eBizz_getResponseStatus(SOAPresponse);

      responseStatus = "AR";

      try {
        saveToAttachmentDirectory("ebill", "out_" + nombreArchivo + ".out",
            SOAPClientSAAJ.getSOAPResponseStringResult(SOAPresponse));
      } catch (Exception e) {
        System.out.println("Couldnt write: " + nombreArchivo);
      }
    }
    return responseStatus;
  }

  public static String eBizzSend_NotaCredito(String strURLAddress, String strURLEndPoint,
      String cInvoiceId) throws Exception {
    ConnectionProvider conn = new DalConnectionProvider();

    EbizEbillUtilsData[] factura, facturaLineas;
    EbizEbillUtilsData f, linea;
    factura = EbizEbillUtilsData.selectNotaCredito(conn, cInvoiceId);
    if (factura.length == 0) {
      throw new Exception(ERROR_CANNOTGETINVOICE);
    }
    f = factura[0];

    facturaLineas = EbizEbillUtilsData.selectNotaCreditoLines(conn, cInvoiceId);
    if (facturaLineas.length == 0) {
      throw new Exception(ERROR_CANNOTGETINVLINES);
    }

    String formato = "", formatoLinea = "", formatoTotalLinea = "", formatoEncoded64, nombreArchivo;
    String clienteTipoDocumento, facturaLeyenda, itemAfectacionIGV, tipoDocumentoRelacionado;
    DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");

    // llenado formato para cabecera de nota de credito
    clienteTipoDocumento = EbizEbill_Catalogo.getCatalogo06(f.clientetipodocumento);
    tipoDocumentoRelacionado = EbizEbill_Catalogo
        .getCatalogo01(f.identificadordocumentorelacionado);
    facturaLeyenda = SSA_Utils.getAmountDescription(f.facturaimportetotal);

    formato = "CC|" + df2.format(df.parse(f.fechaemision)) + "|" + f.emisorruc + "|" + "07" + "|"
        + f.correlativo + "|" + f.clientenrodocumento + "|" + clienteTipoDocumento + "|"
        + f.clienterazonsocial + "|" + f.moneda + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopgrabadas) + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopinafectas) + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopexoneradas) + "|"
        + SSA_Utils.formatNumber(f.facturatotaligv) + "|"
        + SSA_Utils.formatNumber(f.facturatotalisc) + "|"
        + SSA_Utils.formatNumber(f.facturatotaltributos) + "|"
        + SSA_Utils.formatNumber(f.facturaotroscargos) + "|"
        + SSA_Utils.formatNumber(f.facturatotalimportedscto) + "|"
        + SSA_Utils.formatNumber(f.facturaimportetotal) + "|" + f.motivo + "|"
        + f.documentorelacionado + "|" + f.codigomotivo + "|" + tipoDocumentoRelacionado + "|"
        + facturaLeyenda + "|" + f.adicional1 + "|" + f.clientedireccion + "|" + f.clientetelefono
        + "|" + f.clientecorreo + "|" + f.nroordenservicio + "|" + f.nrodocumentooriginal + "|"
        + f.adicional2 + "|" + f.adicional3 + "|" + f.adicional4 + "|";
    System.out.println("formato de cabecera:" + formato);

    // llenado formato para las lineas de la nota de credito
    for (int i = 0; i < facturaLineas.length; i++) {
      linea = facturaLineas[i];

      itemAfectacionIGV = EbizEbill_Catalogo.getCatalogo07(linea.specialtax);

      formatoLinea = "\nDC" + "|"
          + linea.numeroitem
          + "|"
          + linea.productocodigo
          + "|"
          + linea.itemunidadmedida
          + "|"
          + linea.itemcantidad
          + "|"
          + (linea.description.length() > 250 ? linea.description.substring(0, 250)
              : linea.description) + "|" + SSA_Utils.formatNumber(linea.itemvalorunitario) + "|"
          + SSA_Utils.formatNumber(linea.itempreciovtaunitario) + "|"
          + SSA_Utils.formatNumber(linea.itemigv) + "|" + itemAfectacionIGV + "|"
          + SSA_Utils.formatNumber(linea.itemmontoisc) + "|" + "" + "|"
          + SSA_Utils.formatNumber(linea.itemtotalvalorvta) + "|"
          + SSA_Utils.formatNumber(linea.valorrefopnoonerosas) + "|" + linea.adicional1 + "|";

      formatoTotalLinea = formatoTotalLinea + formatoLinea;
    }
    System.out.println("formato de Lineas:" + formatoTotalLinea);
    formato = formato + formatoTotalLinea;
    System.out.println("formato Total de factura:" + formato);

    // enconding data
    formatoEncoded64 = encodeBase64(formato, "ISO-8859-1");

    nombreArchivo = f.emisorruc + "-" + "07" + "-" + f.correlativo + ".txt";

    try {
      saveToAttachmentDirectory("ebill", nombreArchivo, formato);
    } catch (Exception e) {
      System.out.println("Couldnt write: " + nombreArchivo);
    }

    //
    // sending data and getting response
    SOAPMessage SOAPrequest = null, SOAPresponse = null;
    String responseStatus = "AR";

    if (!donotsend) {
      SOAPrequest = createSOAPRequestSendInvoice(strURLEndPoint, nombreArchivo, formatoEncoded64);

      SOAPresponse = SOAPClientSAAJ.createSOAPConnection(SOAPrequest, strURLAddress);
      responseStatus = eBizz_getResponseStatus(SOAPresponse);

      responseStatus = "AR";

      try {
        saveToAttachmentDirectory("ebill", "out_" + nombreArchivo + ".out",
            SOAPClientSAAJ.getSOAPResponseStringResult(SOAPresponse));
      } catch (Exception e) {
        System.out.println("Couldnt write: " + nombreArchivo);
      }
    }
    return responseStatus;
  }

  public static String eBizzSend_NotaCreditoDevolucion(String strURLAddress, String strURLEndPoint,
      String cInvoiceId) throws Exception {
    ConnectionProvider conn = new DalConnectionProvider();

    EbizEbillUtilsData[] factura, facturaLineas;
    EbizEbillUtilsData f, linea;
    factura = EbizEbillUtilsData.selectNotaCreditoDevolucion(conn, cInvoiceId);
    if (factura.length == 0) {
      throw new Exception(ERROR_CANNOTGETINVOICE);
    }
    f = factura[0];

    facturaLineas = EbizEbillUtilsData.selectNotaCreditoDevolucionLines(conn, cInvoiceId);
    if (facturaLineas.length == 0) {
      throw new Exception(ERROR_CANNOTGETINVLINES);
    }

    String formato = "", formatoLinea = "", formatoTotalLinea = "", formatoEncoded64, nombreArchivo;
    String clienteTipoDocumento, facturaLeyenda, itemAfectacionIGV, tipoDocumentoRelacionado;
    DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");

    // llenado formato para cabecera de nota de credito
    clienteTipoDocumento = EbizEbill_Catalogo.getCatalogo06(f.clientetipodocumento);
    tipoDocumentoRelacionado = EbizEbill_Catalogo
        .getCatalogo01(f.identificadordocumentorelacionado);
    facturaLeyenda = SSA_Utils.getAmountDescription(f.facturaimportetotal);

    formato = "CC|" + df2.format(df.parse(f.fechaemision)) + "|" + f.emisorruc + "|" + "07" + "|"
        + f.correlativo + "|" + f.clientenrodocumento + "|" + clienteTipoDocumento + "|"
        + f.clienterazonsocial + "|" + f.moneda + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopgrabadas) + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopinafectas) + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopexoneradas) + "|"
        + SSA_Utils.formatNumber(f.facturatotaligv) + "|"
        + SSA_Utils.formatNumber(f.facturatotalisc) + "|"
        + SSA_Utils.formatNumber(f.facturatotaltributos) + "|"
        + SSA_Utils.formatNumber(f.facturaotroscargos) + "|"
        + SSA_Utils.formatNumber(f.facturatotalimportedscto) + "|"
        + SSA_Utils.formatNumber(f.facturaimportetotal) + "|" + f.motivo + "|"
        + f.documentorelacionado + "|" + f.codigomotivo + "|" + tipoDocumentoRelacionado + "|"
        + facturaLeyenda + "|" + f.adicional1 + "|" + f.clientedireccion + "|" + f.clientetelefono
        + "|" + f.clientecorreo + "|" + f.nroordenservicio + "|" + f.nrodocumentooriginal + "|"
        + f.adicional2 + "|" + f.adicional3 + "|" + f.adicional4 + "|";
    System.out.println("formato de cabecera:" + formato);

    // llenado formato para las lineas de la nota de credito
    for (int i = 0; i < facturaLineas.length; i++) {
      linea = facturaLineas[i];

      itemAfectacionIGV = EbizEbill_Catalogo.getCatalogo07(linea.specialtax);

      formatoLinea = "\nDC" + "|"
          + linea.numeroitem
          + "|"
          + linea.productocodigo
          + "|"
          + linea.itemunidadmedida
          + "|"
          + linea.itemcantidad
          + "|"
          + (linea.description.length() > 250 ? linea.description.substring(0, 250)
              : linea.description) + "|" + SSA_Utils.formatNumber(linea.itemvalorunitario) + "|"
          + SSA_Utils.formatNumber(linea.itempreciovtaunitario) + "|"
          + SSA_Utils.formatNumber(linea.itemigv) + "|" + itemAfectacionIGV + "|"
          + SSA_Utils.formatNumber(linea.itemmontoisc) + "|" + "" + "|"
          + SSA_Utils.formatNumber(linea.itemtotalvalorvta) + "|"
          + SSA_Utils.formatNumber(linea.valorrefopnoonerosas) + "|" + linea.adicional1 + "|";
      System.out.println("NC-DEV:linea.adicional1:" + linea.adicional1);
      formatoTotalLinea = formatoTotalLinea + formatoLinea;
    }
    System.out.println("formato de Lineas:" + formatoTotalLinea);
    formato = formato + formatoTotalLinea;
    System.out.println("formato Total de factura:" + formato);

    // enconding data
    formatoEncoded64 = encodeBase64(formato, "ISO-8859-1");

    nombreArchivo = f.emisorruc + "-" + "07" + "-" + f.correlativo + ".txt";

    try {
      saveToAttachmentDirectory("ebill", nombreArchivo, formato);
    } catch (Exception e) {
      System.out.println("Couldnt write: " + nombreArchivo);
    }

    //
    // sending data and getting response
    SOAPMessage SOAPrequest = null, SOAPresponse = null;
    String responseStatus = "AR";

    if (!donotsend) {
      SOAPrequest = createSOAPRequestSendInvoice(strURLEndPoint, nombreArchivo, formatoEncoded64);

      SOAPresponse = SOAPClientSAAJ.createSOAPConnection(SOAPrequest, strURLAddress);
      responseStatus = eBizz_getResponseStatus(SOAPresponse);

      responseStatus = "AR";

      try {
        saveToAttachmentDirectory("ebill", "out_" + nombreArchivo + ".out",
            SOAPClientSAAJ.getSOAPResponseStringResult(SOAPresponse));
      } catch (Exception e) {
        System.out.println("Couldnt write: " + nombreArchivo);
      }
    }
    return responseStatus;
  }

  public static String eBizzSend_NotaDebito(String strURLAddress, String strURLEndPoint,
      String cInvoiceId) throws Exception {
    ConnectionProvider conn = new DalConnectionProvider();

    EbizEbillUtilsData[] factura, facturaLineas;
    EbizEbillUtilsData f, linea;
    factura = EbizEbillUtilsData.selectNotaDebito(conn, cInvoiceId);
    if (factura.length == 0) {
      throw new Exception(ERROR_CANNOTGETINVOICE);
    }
    f = factura[0];

    facturaLineas = EbizEbillUtilsData.selectNotaDebitosLines(conn, cInvoiceId);
    if (facturaLineas.length == 0) {
      throw new Exception(ERROR_CANNOTGETINVLINES);
    }

    String formato = "", formatoLinea = "", formatoTotalLinea = "", formatoEncoded64, nombreArchivo;
    String clienteTipoDocumento, facturaLeyenda, itemAfectacionIGV, tipoDocumentoRelacionado, codigoMotivo;
    DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");

    // llenado formato para cabecera de nota de debito
    clienteTipoDocumento = EbizEbill_Catalogo.getCatalogo06(f.clientetipodocumento);
    tipoDocumentoRelacionado = EbizEbill_Catalogo
        .getCatalogo01(f.identificadordocumentorelacionado);
    facturaLeyenda = SSA_Utils.getAmountDescription(f.facturaimportetotal);

    formato = "CD|" + df2.format(df.parse(f.fechaemision)) + "|" + f.emisorruc + "|" + "08" + "|"
        + f.correlativo + "|" + f.clientenrodocumento + "|" + clienteTipoDocumento + "|"
        + f.clienterazonsocial + "|" + f.moneda + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopgrabadas) + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopinafectas) + "|"
        + SSA_Utils.formatNumber(f.facturatotalvtaopexoneradas) + "|"
        + SSA_Utils.formatNumber(f.facturatotaligv) + "|"
        + SSA_Utils.formatNumber(f.facturatotalisc) + "|"
        + SSA_Utils.formatNumber(f.facturatotaltributos) + "|"
        + SSA_Utils.formatNumber(f.facturaotroscargos) + "|"
        + SSA_Utils.formatNumber(f.facturatotalimportedscto) + "|"
        + SSA_Utils.formatNumber(f.facturaimportetotal) + "|" + f.motivo + "|"
        + f.documentorelacionado + "|" + f.codigomotivo + "|" + tipoDocumentoRelacionado + "|"
        + facturaLeyenda + "|" + f.adicional1 + "|" + f.clientedireccion + "|" + f.clientetelefono
        + "|" + f.clientecorreo + "|" + f.nroordenservicio + "|" + f.nrodocumentooriginal + "|"
        + f.adicional2 + "|" + f.adicional3 + "|" + f.adicional4 + "|";
    System.out.println("formato de cabecera:" + formato);

    // llenado formato para las lineas de la nota de debito
    for (int i = 0; i < facturaLineas.length; i++) {
      linea = facturaLineas[i];

      itemAfectacionIGV = EbizEbill_Catalogo.getCatalogo07(linea.specialtax);

      formatoLinea = "\nDD" + "|"
          + linea.numeroitem
          + "|"
          + linea.productocodigo
          + "|"
          + linea.itemunidadmedida
          + "|"
          + linea.itemcantidad
          + "|"
          + (linea.description.length() > 250 ? linea.description.substring(0, 250)
              : linea.description) + "|" + SSA_Utils.formatNumber(linea.itemvalorunitario) + "|"
          + SSA_Utils.formatNumber(linea.itempreciovtaunitario) + "|"
          + SSA_Utils.formatNumber(linea.itemigv) + "|" + itemAfectacionIGV + "|"
          + SSA_Utils.formatNumber(linea.itemmontoisc) + "|" + "" + "|"
          + SSA_Utils.formatNumber(linea.itemtotalvalorvta) + "|"
          + SSA_Utils.formatNumber(linea.valorrefopnoonerosas) + "|" + linea.adicional1 + "|";

      formatoTotalLinea = formatoTotalLinea + formatoLinea;
    }
    System.out.println("formato de Lineas:" + formatoTotalLinea);
    formato = formato + formatoTotalLinea;
    System.out.println("formato Total de factura:" + formato);

    // enconding data
    formatoEncoded64 = encodeBase64(formato, "ISO-8859-1");

    nombreArchivo = f.emisorruc + "-" + "08" + "-" + f.correlativo + ".txt";

    try {
      saveToAttachmentDirectory("ebill", nombreArchivo, formato);
    } catch (Exception e) {
      System.out.println("Couldnt write: " + nombreArchivo);
    }

    //
    // sending data and getting response
    SOAPMessage SOAPrequest = null, SOAPresponse = null;
    String responseStatus = "AR";

    if (!donotsend) {
      SOAPrequest = createSOAPRequestSendInvoice(strURLEndPoint, nombreArchivo, formatoEncoded64);

      SOAPresponse = SOAPClientSAAJ.createSOAPConnection(SOAPrequest, strURLAddress);
      responseStatus = eBizz_getResponseStatus(SOAPresponse);

      responseStatus = "AR";

      try {
        saveToAttachmentDirectory("ebill", "out_" + nombreArchivo + ".out",
            SOAPClientSAAJ.getSOAPResponseStringResult(SOAPresponse));
      } catch (Exception e) {
        System.out.println("Couldnt write: " + nombreArchivo);
      }
    }
    return responseStatus;
  }

  public static String eBizzSend_Baja(String strURLAddress, String strURLEndPoint, String cInvoiceId)
      throws Exception {
    ConnectionProvider conn = new DalConnectionProvider();

    EbizEbillUtilsData[] factura;
    EbizEbillUtilsData f;
    factura = EbizEbillUtilsData.selectDocumentoBaja(conn, cInvoiceId);
    if (factura.length == 0) {
      throw new Exception(ERROR_CANNOTGETINVOICE);
    }
    f = factura[0];

    String formato = "", formatoLinea = "", formatoTotalLinea = "", formatoEncoded64, nombreArchivo;
    String tipoDocumento;
    DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
    DateFormat df3 = new SimpleDateFormat("yyyyMMdd");

    // llenado formato para cabecera de documentos dados de baja
    String tipoDocumentoRelacionado = EbizEbill_Catalogo
        .getCatalogo01(f.identificadordocumentorelacionado);

    formato = "VD|" + f.emisorruc + "|" + tipoDocumentoRelacionado + "|" + f.correlativo + "|"
        + f.motivoanulacion + "|";

    System.out.println("formato de cabecera:" + formato);
    System.out.println("formato Total de factura:" + formato);

    // enconding data
    formatoEncoded64 = encodeBase64(formato, "ISO-8859-1");

    nombreArchivo = "CB" + "-" + f.emisorruc + "-" + df3.format(df.parse(f.fechaemision)) + ".txt";

    try {
      saveToAttachmentDirectory("ebill", nombreArchivo + "_" + f.correlativo + ".txt", formato);
    } catch (Exception e) {
      System.out.println("Couldnt write: " + nombreArchivo);
    }

    //
    // sending data and getting response
    SOAPMessage SOAPrequest = null, SOAPresponse = null;
    String responseStatus = "AR";

    if (!donotsend) {
      SOAPrequest = createSOAPRequestSendInvoice(strURLEndPoint, nombreArchivo, formatoEncoded64);

      SOAPresponse = SOAPClientSAAJ.createSOAPConnection(SOAPrequest, strURLAddress);
      responseStatus = eBizz_getResponseStatus(SOAPresponse);

      responseStatus = "AR";

      try {
        saveToAttachmentDirectory("ebill", "out_" + nombreArchivo + ".out",
            SOAPClientSAAJ.getSOAPResponseStringResult(SOAPresponse));
      } catch (Exception e) {
        System.out.println("Couldnt write: " + nombreArchivo);
      }
    }
    return responseStatus;
  }

  public static String eBizzSend_ComprobanteRetencion(String strURLAddress, String strURLEndPoint,
      String scoPwithholdingReceiptId) throws Exception {
    ConnectionProvider conn = new DalConnectionProvider();

    EbizEbillUtilsData[] compRetencion, compRetencionLineas;
    EbizEbillUtilsData r, linea;

    compRetencion = EbizEbillUtilsData.selectComprobanteRetencion(conn, scoPwithholdingReceiptId);
    if (compRetencion.length == 0) {
      throw new Exception(ERROR_CANNOTGETINVOICE);
    }
    r = compRetencion[0];

    compRetencionLineas = EbizEbillUtilsData.selectComprobanteRetencionLines(conn,
        scoPwithholdingReceiptId);
    if (compRetencionLineas.length == 0) {
      throw new Exception(ERROR_CANNOTGETINVLINES);
    }

    String formato = "", formatoLinea = "", formatoTotalLinea = "", formatoEncoded64, nombreArchivo;
    String proveedorTipoDocumento;
    DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");

    proveedorTipoDocumento = EbizEbill_Catalogo.getCatalogo06(r.proveedortipodocumento);

    BigDecimal totalSolesRet = new BigDecimal(0);
    BigDecimal totalSolesPag = new BigDecimal(0);
    // System.out.println("Processing totalsolesret and pag:");
    for (int h = 0; h < compRetencionLineas.length; h++) {
      BigDecimal solesRet = new BigDecimal(compRetencionLineas[h].retinvtotalretenido);
      BigDecimal solesPag = new BigDecimal(compRetencionLineas[h].retinvtotalpagado);

      // System.out.println("before solesRet:" + solesRet + " - solesPag:" + solesPag);
      if (!compRetencionLineas[h].retinvmoneda.equals("PEN")) {
        solesRet = solesRet.multiply(new BigDecimal(compRetencionLineas[h].tc)).setScale(2,
            BigDecimal.ROUND_HALF_UP);
        solesPag = solesPag.multiply(new BigDecimal(compRetencionLineas[h].tc)).setScale(2,
            BigDecimal.ROUND_HALF_UP);
      }
      // System.out.println("after:" + solesRet + " - solesPag:" + solesPag);

      totalSolesRet = totalSolesRet.add(solesRet);
      totalSolesPag = totalSolesPag.add(solesPag);
    }

    formato = "CR|" + r.correlativo + "|" + df2.format(df.parse(r.fechaemision)) + "|20|"
        + r.emisorruc + "|6||" + r.emisorubigeo + "|" + r.emisorDireccion + "|"
        + r.emisorUrbanizacion + "|" + r.emisorProvincia + "|" + r.emisorDepartamento + "|"
        + r.emisorDistrito + "|" + r.emisorPais + "|" + r.emisorrazonsocial + "|"
        + r.proveedornrodocumento + "|" + proveedorTipoDocumento + "||" + r.proveedorubigeo + "|"
        + r.proveedorDireccion + "|" + r.proveedorUrbanizacion + "|" + r.proveedorProvincia + "|"
        + r.proveedorDepartamento + "|" + r.proveedorDistrito + "|" + r.proveedorPais + "|"
        + r.proveedorrazonsocial + "|" + r.regimenretencion + "|"
        + SSA_Utils.formatNumber(r.tasaretencion) + "||"
        + String.format("%.2f", Double.valueOf(totalSolesRet.toString())) + "|" + r.monedaretenido
        + "|" + String.format("%.2f", Double.valueOf(totalSolesPag.toString())) + "|"
        + r.monedapagado + "|" + r.proveedorcorreo + "|||";

    System.out.println("formato de cabecera:" + formato);

    // llenado formato para las lineas del comprobante de retención
    for (int i = 0; i < compRetencionLineas.length; i++) {
      String tipoDocumentoRelacionado = EbizEbill_Catalogo
          .getCatalogo01(compRetencionLineas[i].identificadordocumentorelacionado);

      String importeTotalRetenido = compRetencionLineas[i].retinvtotalretenido;
      String importeTotalPagado = compRetencionLineas[i].retinvtotalpagado;
      double tC = Double.valueOf(compRetencionLineas[i].tc);
      if (!compRetencionLineas[i].retinvmoneda.equals("PEN")) {// !soles
        importeTotalRetenido = String.valueOf(Double.valueOf(importeTotalRetenido) * tC);
        importeTotalPagado = String.valueOf(Double.valueOf(importeTotalPagado) * tC);
      }

      formatoLinea = "\nDR|" + tipoDocumentoRelacionado + "|"
          + compRetencionLineas[i].retinvcorrelativo + "|"
          + df2.format(df.parse(compRetencionLineas[i].retinvfechaemision)) + "|"
          + SSA_Utils.formatNumber(compRetencionLineas[i].retinvfacturaimportetotal) + "|"
          + compRetencionLineas[i].retinvmoneda + "|"
          + df2.format(df.parse(compRetencionLineas[i].retinvfechapago)) + "|"
          + compRetencionLineas[i].numpago + "|"
          + SSA_Utils.formatNumber(compRetencionLineas[i].retinvtotalpagadonoret) + "|"
          + compRetencionLineas[i].retinvmonedapago + "|"
          + SSA_Utils.formatNumber(importeTotalRetenido) + "|"
          + compRetencionLineas[i].retinvmonedaimpretenido + "|"
          + df2.format(df.parse(compRetencionLineas[i].retinvfecharetencion)) + "|"
          + SSA_Utils.formatNumber(importeTotalPagado) + "|"
          + compRetencionLineas[i].retinvmonedaimpnetopago + "|"
          + compRetencionLineas[i].retinvmonedaref + "|"
          + compRetencionLineas[i].retinvmonedaobjetivo + "|" + compRetencionLineas[i].tc + "|"
          + df2.format(df.parse(compRetencionLineas[i].fechatc)) + "|";

      formatoTotalLinea = formatoTotalLinea + formatoLinea;
    }
    System.out.println("formato de Lineas:" + formatoTotalLinea);
    formato = formato + formatoTotalLinea;
    System.out.println("formato Total de comp retencion:" + formato);

    // enconding data
    formatoEncoded64 = encodeBase64(formato, "ISO-8859-1");

    nombreArchivo = r.emisorruc + "-" + "20" + "-" + r.correlativo + ".txt";

    try {
      saveToAttachmentDirectory("ebill", nombreArchivo, formato);
    } catch (Exception e) {
      System.out.println("Couldnt write: " + nombreArchivo);
    }
    //
    // sending data and getting response
    SOAPMessage SOAPrequest = null, SOAPresponse = null;
    String responseStatus = "AR";

    if (!donotsend) {

      SOAPrequest = createSOAPRequestSendRetention(strURLEndPoint, nombreArchivo, formatoEncoded64);

      SOAPresponse = SOAPClientSAAJ.createSOAPConnection(SOAPrequest, strURLAddress);
      responseStatus = eBizz_getResponseStatus(SOAPresponse);

      responseStatus = "AR";

      try {
        saveToAttachmentDirectory("ebill", "out_" + nombreArchivo + ".out",
            SOAPClientSAAJ.getSOAPResponseStringResult(SOAPresponse));
      } catch (Exception e) {
        System.out.println("Couldnt write: " + nombreArchivo);
      }
    }

    return responseStatus;
  }

  public static String eBizzSend_ComprobanteRetencionBaja(String strURLAddress,
      String strURLEndPoint, String scoPwithholdingReceiptId) throws Exception {
    ConnectionProvider conn = new DalConnectionProvider();

    EbizEbillUtilsData[] compRetencion;
    EbizEbillUtilsData r;
    compRetencion = EbizEbillUtilsData.selectComprobanteRetencionBaja(conn,
        scoPwithholdingReceiptId);
    if (compRetencion.length == 0) {
      throw new Exception(ERROR_CANNOTGETINVOICE);
    }
    r = compRetencion[0];

    String formato = "", formatoLinea = "", formatoTotalLinea = "", formatoEncoded64, nombreArchivo;
    String tipoDocumento;
    DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
    DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
    DateFormat df3 = new SimpleDateFormat("yyyyMMdd");

    // llenado formato para cabecera de documentos dados de baja

    formato = "VD|" + r.emisorruc + "|20|" + r.correlativo + "|" + r.motivoanulacion + "|";
    System.out.println("formato de cabecera:" + formato);
    System.out.println("formato Total de factura:" + formato);

    // enconding data
    formatoEncoded64 = encodeBase64(formato, "ISO-8859-1");

    nombreArchivo = "PB-" + r.emisorruc + "-20-" + df3.format(df.parse(r.fechaemision)) + ".txt";

    try {
      saveToAttachmentDirectory("ebill", nombreArchivo, formato);
    } catch (Exception e) {
      System.out.println("Couldnt write: " + nombreArchivo);
    }
    //
    // sending data and getting response
    SOAPMessage SOAPrequest = null, SOAPresponse = null;
    String responseStatus = "AR";

    if (!donotsend) {

      SOAPrequest = createSOAPRequestSendRetention(strURLEndPoint, nombreArchivo, formatoEncoded64);

      SOAPresponse = SOAPClientSAAJ.createSOAPConnection(SOAPrequest, strURLAddress);
      responseStatus = eBizz_getResponseStatus(SOAPresponse);

      responseStatus = "AR";

      try {
        saveToAttachmentDirectory("ebill", "out_" + nombreArchivo + ".out",
            SOAPClientSAAJ.getSOAPResponseStringResult(SOAPresponse));
      } catch (Exception e) {
        System.out.println("Couldnt write: " + nombreArchivo);
      }
    }

    return responseStatus;
  }

  public static String eBizzGet_Estado(String strURLAddress, String strURLEndPoint,
      String cInvoiceId) throws Exception {
    // SOAPMessage SOAPrequest = createSOAPRequestSendInvoice(strURLEndPoint, strFileName,
    // strInfoB64);
    SOAPMessage soapResponse = null;
    // soapResponse = SOAPClientSAAJ.createSOAPConnection(SOAPrequest, strURLAddress);
    return "";
  }

  public static String eBizz_getResponseStatus(SOAPMessage soapResponse) throws Exception {
    String responseStatus = "";

    Source sourceContent = soapResponse.getSOAPPart().getContent();
    Iterator it = soapResponse.getSOAPBody().getChildElements();
    while (it.hasNext()) {
      Node node = (Node) it.next();
      if (node instanceof SOAPElement) {
        SOAPElement element = (SOAPElement) node; // sendRetentionResponse
        Iterator It2 = element.getChildElements();
        while (It2.hasNext()) {
          Node nodereturn = (Node) It2.next();
          if (nodereturn instanceof SOAPElement) {
            SOAPElement elementreturn = (SOAPElement) nodereturn; // return
            Iterator It3 = elementreturn.getChildElements();
            while (It3.hasNext()) {
              Node nodefinal = (Node) It3.next();
              if (nodefinal instanceof SOAPElement) {
                SOAPElement elementfinal = (SOAPElement) nodefinal;
                if (elementfinal.getElementName().getLocalName().equals("statusCode")) {
                  responseStatus = elementfinal.getValue();
                }
              }
            }
          }
        }
      }
    }

    return responseStatus;
  }

  public static SOAPMessage createSOAPRequestSendInvoice(String strURLEndPoint, String strFileName,
      String strInfoB64) throws Exception {

    MessageFactory messageFactory = MessageFactory.newInstance();
    SOAPMessage soapMessage = messageFactory.createMessage();
    SOAPPart soapPart = soapMessage.getSOAPPart();
    String serverURI = strURLEndPoint;

    // SOAP Envelope
    SOAPEnvelope envelope = soapPart.getEnvelope();
    envelope.addNamespaceDeclaration("ws", serverURI);

    // SOAP Body
    SOAPBody soapBody = envelope.getBody();
    SOAPElement soapBodyElem = soapBody.addChildElement("sendInvoice", "ws");
    SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("nombreArchivo");
    soapBodyElem1.addTextNode(strFileName);
    SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("contenido");
    soapBodyElem2.addTextNode(strInfoB64);

    MimeHeaders headers = soapMessage.getMimeHeaders();
    headers.addHeader("SOAPAction", serverURI + "sendInvoice");

    soapMessage.saveChanges();

    /* Print the request message */
    System.out.print("Request SOAP Message:");
    soapMessage.writeTo(System.out);
    System.out.println();

    return soapMessage;
  }

  public static SOAPMessage createSOAPRequestSendRetention(String strURLEndPoint,
      String strFileName, String strInfoB64) throws Exception {

    MessageFactory messageFactory = MessageFactory.newInstance();
    SOAPMessage soapMessage = messageFactory.createMessage();
    SOAPPart soapPart = soapMessage.getSOAPPart();
    String serverURI = strURLEndPoint;

    // SOAP Envelope
    SOAPEnvelope envelope = soapPart.getEnvelope();
    envelope.addNamespaceDeclaration("ws", serverURI);

    // SOAP Body
    SOAPBody soapBody = envelope.getBody();
    SOAPElement soapBodyElem = soapBody.addChildElement("sendRetention", "ws");
    SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("nombreArchivo");
    soapBodyElem1.addTextNode(strFileName);
    SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("contenido");
    soapBodyElem2.addTextNode(strInfoB64);

    MimeHeaders headers = soapMessage.getMimeHeaders();
    headers.addHeader("SOAPAction", serverURI + "sendRetention");

    soapMessage.saveChanges();

    /* Print the request message */
    System.out.print("Request SOAP Message:");
    soapMessage.writeTo(System.out);
    System.out.println();

    return soapMessage;
  }

}