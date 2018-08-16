package pe.com.unifiedgo.ebilling;

public class EbizEbill_Catalogo {

  // CATALOGO NO. 01: Código de Tipo de documento
  public static String getCatalogo01(String value) { // specialdoctype
    String type = "";
    if (value.compareTo("SCOARINVOICE") == 0 || value.compareTo("SCOAPINVOICE") == 0
        || value.compareTo("SCOAPSIMPLEPROVISIONINVOICE") == 0)
      type = "01";
    else if (value.compareTo("SCOARTICKET") == 0 || value.compareTo("SCOARINVOICERETURNMAT") == 0)
      type = "03";
    else if (value.compareTo("SCOARCREDITMEMO") == 0 || value.compareTo("SCOAPCREDITMEMO") == 0)
      type = "07";
    else if (value.compareTo("SCOARDEBITMEMO") == 0)
      type = "08";
    return type;
  }

  // CATALOGO NO. 06: Códigos de Tipos de Documentos de Identidad
  public static String getCatalogo06(String value) { // bpdocumentypeid-cmb
    String type = "";
    if (value.compareTo("OTROS TIPOS DE DOCUMENTOS") == 0)
      type = "0";
    else if (value.compareTo("DNI") == 0)
      type = "1";
    else if (value.compareTo("CARNET DE EXTRANJERIA") == 0)
      type = "4";
    else if (value.compareTo("REGISTRO UNICO DE CONTRIBUYENTES") == 0)
      type = "6";
    else if (value.compareTo("PASAPORTE") == 0)
      type = "7";
    else if (value.compareTo("CEDULA DIPLOMATICA DE IDENTIDAD") == 0)
      type = "A";
    return type;
  }

  // CATALOGO NO. 07: Códigos de Tipo de Afectación del IGV
  public static String getCatalogo07(String value) { // specialtax
    String type = "30";
    if (value.compareTo("SCOIGV") == 0)
      type = "10";
    else if (value.compareTo("SCOEXEMPT") == 0)
      type = "30";
    else if (value.compareTo("15") == 0)
      type = "15";
    return type;
  }

  // CATALOGO NO. 15: Códigos - Elementos adicionales en la Factura Electrónica y/o Boleta de Venta
  // Electrónica
  public static String getCatalogo15(boolean tituloGratuito) { // specialtax
    String type = "1000";
    if (!tituloGratuito)
      type = "1000";
    else if (tituloGratuito)
      type = "1002";
    return type;
  }

}