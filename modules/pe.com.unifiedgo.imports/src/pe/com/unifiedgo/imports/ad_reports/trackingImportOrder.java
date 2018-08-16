/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2001-2010 Openbravo SLU 
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package pe.com.unifiedgo.imports.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.WindowTreeData;
import org.openbravo.erpCommon.utility.WindowTreeUtility;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class trackingImportOrder extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";
  private static String LocalOrg = null;
  private static String RemoteOrg = null;
  private static String OrderID = null;

  private static final String CHILD_SHEETS = "frameWindowTreeF3"; // tree

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    OBError myMessage = null;
    myMessage = new OBError();
    myMessage.setTitle("");

    if (vars.commandIn("DEFAULT")) {
      String strOrg = vars.getGlobalVariable("inpOrg", "trackingImportOrder|Org", "0");
      String strDocDate = vars.getGlobalVariable("inpDocDate", "trackingImportOrder|docDate",
          SREDateTimeData.FirstDayOfMonth(this));
      String strDateto = vars.getGlobalVariable("inpDateTo", "trackingImportOrder|inpDateTo",
          SREDateTimeData.today(this));
      String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
          "trackingImportOrder|CB_PARTNER_ID", "");

      String mProductId = vars.getGlobalVariable("inpmProductId",
          "trackingImportOrder|M_Product_Id", "");

      String strNumOrder = vars.getStringParameter("paramProductOrder");
      
      String strShowPending = vars.getGlobalVariable("inpShowPending", "trackingImportOrder|ShowPending", "N");


      printPageDataSheet(request, response, vars, strOrg, strDocDate, strDateto, strcBpartnetId,
          mProductId, strNumOrder,strShowPending);

    } else if (vars.commandIn("LISTAR")) {
      String strOrg = vars.getStringParameter("inpadOrgId");
      String strDocDate = vars.getStringParameter("inpDocDate");
      String strDateTo = vars.getStringParameter("inpDateTo");
      String strcBpartnetId = vars.getStringParameter("inpcBPartnerId");
      String mProductId = vars.getStringParameter("inpmProductId");
      String strNumOrder = vars.getStringParameter("paramProductOrder");
      String strPending = vars.getStringParameter("inpShowPending");

      printPageDataSheet(request, response, vars, strOrg, strDocDate, strDateTo, strcBpartnetId,
          mProductId, strNumOrder,strPending);
    } else if (vars.commandIn("XLS")) {
      String strOrg = vars.getStringParameter("inpadOrgId");
      String strDocDate = vars.getStringParameter("inpDocDate");
      String strDateTo = vars.getStringParameter("inpDateTo");
      String strcBpartnetId = vars.getStringParameter("inpcBPartnerId");
      String mProductId = vars.getStringParameter("inpmProductId");
      String strNumOrder = vars.getStringParameter("paramProductOrder");

      setHistoryCommand(request, "DEFAULT");

      if (vars.commandIn("XLS")) {
        printPageXLS(request, response, vars, strOrg, strDocDate, strDateTo, strcBpartnetId,
            mProductId, strNumOrder);
      }

    } else
      pageError(response);
  }

  private void printPageXLS(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strOrg, String strDocDate, String strDateto,
      String strcBpartnetId, String mProductId, String strNumOrder) throws IOException,
      ServletException {

    trackingImportOrderExcelData[] data = null;

    OBError myMessage = null;
    myMessage = new OBError();

    try {
      data = trackingImportOrderExcelData.selectData(this, strOrg,
          vars.getSessionValue("#AD_CLIENT_ID"), strDocDate, strDateto, strcBpartnetId, mProductId,
          strNumOrder);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }
    
    if (data.length==0) {
        advisePopUp(request, response, "WARNING", Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()), Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
        return;
      }

    Date fecha = null;

    String strOutput;
    String strReportName;

    strOutput = "xls";
    strReportName = "@basedesign@/pe/com/unifiedgo/imports/ad_reports/TrackingImportOrderExcelPlane.jrxml";

    HashMap<String, Object> parameters = new HashMap<String, Object>();
    // parameters.put("ORGANIZACION", dataOrganizacion.length == 0 ? ""
    // : dataOrganizacion[0].organizacion);
    // parameters.put("ORGANIZACION_RUC", dataOrganizacion.length == 0 ? "" :
    // dataOrganizacion[0].ruc);
    //
    // parameters.put("PRODUCTO", dataProducto.length == 0 ? "" : dataProducto[0].producto);

    // data.length;
    // validar posibles null al convertir string a BigDecimal

    renderJR(vars, response, strReportName, "Seguimiento_Ordenes_Importacion", strOutput,
        parameters, data, null);

  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strOrg, String strDocDate, String strDateTo,
      String strBpartnetId, String mProductId, String mStrNumOrder, String strShowPending) throws IOException,
      ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    trackingImportOrderData[] getData = null;
    trackingImportOrderData[] getDataSub1 = null;
    trackingImportOrderData[] getDataPartialFolio = null;

    String strConvRateErrorMsg = "";
    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/imports/ad_reports/trackingImportOrder", discard).createXmlDocument();

    if (vars.commandIn("DEFAULT")) {
      xmlDocument.setParameter("menu", loadNodes(null, null, null, "0"));
    }

    if (vars.commandIn("LISTAR")) {
      OBError myMessage = null;
      myMessage = new OBError();

      // Map<trackingImportOrderData, trackingImportOrderData[]> resMap = new
      // HashMap<trackingImportOrderData, trackingImportOrderData[]>();
      Map<String, trackingImportOrderData[]> resMap = new HashMap<String, trackingImportOrderData[]>();
      Map<String, trackingImportOrderData[]> resMapSub = new HashMap<String, trackingImportOrderData[]>();

      try {
        String ad_org_id = strOrg;
        
        if(strShowPending!=null && strShowPending.equals("Y"))
          getData = trackingImportOrderData.selectOrdertoReviewPending(this, ad_org_id,
            vars.getSessionValue("#AD_CLIENT_ID"), strDocDate, strDateTo, strBpartnetId,
            mProductId, mStrNumOrder);
         else
        	getData = trackingImportOrderData.selectOrdertoReview(this, ad_org_id,
        	            vars.getSessionValue("#AD_CLIENT_ID"), strDocDate, strDateTo, strBpartnetId,
        	            mProductId, mStrNumOrder);

        for (int i = 0; i < getData.length; i++) {
          
            if(getData[i].partnername.length()> 35)
           	  getData[i].partnername = getData[i].partnername.substring(0, 30) + "..." ;
         
        	
          getDataSub1 = trackingImportOrderData.selectOrderImportLine(this, getData[i].orderid,
              mProductId);
          resMap.put(getData[i].orderid, getDataSub1);
          for (int j = 0; j < getDataSub1.length; j++) {
        	  
           if(getDataSub1[j].productname.length()> 50)
              	  getDataSub1[j].productname = getDataSub1[j].productname.substring(0, 50) + "..." ;  
        
            getDataPartialFolio = trackingImportOrderData.selectPartialFolioImport(this,
                getDataSub1[j].orderimportlineid);
            resMapSub.put(getDataSub1[j].orderimportlineid, getDataPartialFolio);
          }
        }

      } catch (ServletException ex) {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
      }
      strConvRateErrorMsg = myMessage.getMessage();
      if (!strConvRateErrorMsg.equals("") && strConvRateErrorMsg != null) {
        advise(request, response, "ERROR",
            Utility.messageBD(this, "NoConversionRateHeader", vars.getLanguage()),
            strConvRateErrorMsg);
      } else { // Otherwise, the report is launched
        if ((getData == null || getData.length == 0)) {
          discard[0] = "selEliminar";
          getData = trackingImportOrderData.set();
          xmlDocument.setParameter("menu", loadNodes(null, null, null, "0"));
        } else {
          String strLevel = "0"; // Level Tree Initial
          xmlDocument.setParameter("menu", loadNodes(getData, resMap, resMapSub, strLevel));

          // xmlDocument.setData("structure5", getData);
        }
      }

    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {

      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "trackingImportOrder", false, "", "",
          "", false, "ad_reports", strReplaceWith, false, true);
       toolbar.setEmail(false);
       toolbar.prepareSimpleToolBarTemplate();
      toolbar.prepareRelationBarTemplate(false, false, "printXLS();return false;");
      // toolbar.prepareRelationBarTemplate(false, false, "printXLS();return false;");
      xmlDocument.setParameter("toolbar", toolbar.toString());

      // ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "trackingImportOrder", false, "",
      // "",
      // "", false, "ad_reports", strReplaceWith, false, true);
      // toolbar.setEmail(false);
      // toolbar.prepareSimpleToolBarTemplate();
      // // toolbar.prepareRelationBarTemplate(false, false, "printXLS();return false;");
      // xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.report.ad_reports.trackingImportOrder");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "trackingImportOrder.html",
            classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "trackingImportOrder.html",
            strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("trackingImportOrder");
        vars.removeMessage("trackingImportOrder");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID", "",
            "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "trackingImportOrder"), Utility.getContext(this, vars,
                "#User_Client", "trackingImportOrder"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "trackingImportOrder", strOrg);
        xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      /*
       * try { ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
       * "AD_ORG_ID", "", "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this, vars,
       * "#AccessibleOrgTree", "trackingImportOrder"), Utility.getContext(this, vars,
       * "#User_Client", "trackingImportOrder"), 0); Utility.fillSQLParameters(this, vars, null,
       * comboTableData, "trackingImportOrder", strOrg); xmlDocument.setData("reportTOAD_Org_ID",
       * "liststructure", comboTableData.select(false)); } catch (Exception ex) { throw new
       * ServletException(ex); }
       */

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("docDatedisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
      xmlDocument.setParameter("docDatesaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

      xmlDocument.setParameter("docDate", strDocDate);
      xmlDocument.setParameter("docDateto", strDateTo);
      xmlDocument.setParameter("adOrgId", strOrg);

      xmlDocument.setParameter("paramBPartnerId", strBpartnetId);
      xmlDocument.setParameter("paramBPartnerDescription",
          trackingImportOrderData.selectBpartner(this, strBpartnetId));
      // xmlDocument.setParameter("adToOrgId", strToOrg);

      xmlDocument.setParameter("mProduct", mProductId);
      xmlDocument.setParameter("productDescription",
          trackingImportOrderData.selectMproduct(this, mProductId));

      xmlDocument.setParameter("paramProductOrder", mStrNumOrder);
      xmlDocument.setParameter("paramShowPending", strShowPending);

      // xmlDocument.setParameter("menu", loadNodes(vars, "test"));
      // xmlDocument.setParameter("menu", "TEST");

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
            "9A4C02741FAD4E5DAE696E0CBE279F9E", "", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "trackingImportOrder"), Utility.getContext(this, vars,
                "#User_Client", "trackingImportOrder"), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, "trackingImportOrder", "");
        xmlDocument.setData("reportNumMonths", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      out.println(xmlDocument.print());
      out.close();
    }
  }

  private String strLevel_1(trackingImportOrderData data) {
    StringBuffer element = new StringBuffer();

    element.append("<table class=\"Main_Client_TableEdition\">");
    element.append("<tr>");
    element.append("<td colspan=\"6\">");
    element.append("<div style=\"height:25px;\"/>");
    element
        .append("<table cellspacing=\"0\" cellpadding=\"1\" width=\"1200px\" class=\"DataGrid_Header_Table DataGrid_Body_Table\" style=\"table-layout: auto;\" id=\"selSection5\">");
    // Cabecera BEGIN
    element.append("<tr class=\"Popup_Client_Selector_DataGrid_HeaderRow\">");

    element.append("<th width=\"270\" class=\"DataGrid_Header_Cell\">Organización</th>");
    element.append("<th width=\"130\" class=\"DataGrid_Header_Cell\">Nro. Orden</th>");
    element.append("<th width=\"100\" class=\"DataGrid_Header_Cell\">Fec. Aprox</th>");
    element.append("<th width=\"400\" class=\"DataGrid_Header_Cell\">Proveedor</th>");
    element.append("<th width=\"50\" class=\"DataGrid_Header_Cell\">Cant. Pedido</th>");
    element.append("<th width=\"50\" class=\"DataGrid_Header_Cell\">Cant. Recibido</th>");
    element.append("<th width=\"50\" class=\"DataGrid_Header_Cell\">Cant. Saldo</th>");
    element.append("<th width=\"50\" class=\"DataGrid_Header_Cell\">Moneda</th>");
    element.append("<th width=\"50\" class=\"DataGrid_Header_Cell\">Total</th>");
    element.append("<th width=\"50\" class=\"DataGrid_Header_Cell\">Cant. Cierre</th>");

    element.append("</tr>");
    // CABECERA FIN
    //
    // CUERPO BEGIN
    element.append("<tr>");

    element.append(" <td class=\"DataGrid_Body_Cell\"> " + data.orgname + "</td>");
    element.append(" <td class=\"DataGrid_Body_Cell\"> " + data.documentno + "</td>");
    element.append(" <td class=\"DataGrid_Body_Cell\"> " + data.datepromised + "</td>");
    element.append(" <td class=\"DataGrid_Body_Cell\"> " + data.partnername + "</td>");
    element.append(" <td class=\"DataGrid_Body_Cell\"> " + data.qtyordered + "</td>");
    element.append(" <td class=\"DataGrid_Body_Cell\"> " + data.qtyreceived + "</td>");
    element.append(" <td class=\"DataGrid_Body_Cell\"> " + data.qtydifference + "</td>");
    element.append(" <td class=\"DataGrid_Body_Cell\"> " + data.scurrency + "</td>");
    element.append(" <td class=\"DataGrid_Body_Cell\"> " + data.ordergrandtotal + "</td>");
    element.append(" <td  class=\"DataGrid_Body_Cell\"> " + data.cierramanual + "</td>");

    element.append("</tr>");
    // CUERPO END

    element.append("</table>");
    element.append("<div style=\"height:25px;\" />");
    element.append("</td>");
    element.append("</tr>");
    element.append("</table>");
    return element.toString();
  }

  private String strLevel_2(trackingImportOrderData data) {
    StringBuffer element = new StringBuffer();

    element.append("<table class=\"Main_Client_TableEdition\">");
    element.append("<tr>");
    element.append("<td colspan=\"6\">");
    element.append("<div style=\"height:25px;\"/>");
    element
        .append("<table cellspacing=\"0\" cellpadding=\"1\" width=\"900px\" class=\"DataGrid_Header_Table DataGrid_Body_Table\" style=\"table-layout: auto;\" id=\"selSection5\">");
    // Cabecera BEGIN
    element.append("<tr class=\"Popup_Client_Selector_DataGrid_HeaderRow\">");

    element.append("<th width=\"15%\" class=\"DataGrid_Header_Cell\">Código</th>");
    element.append("<th width=\"10%\" class=\"DataGrid_Header_Cell\">Nro Partida</th>");
    element.append("<th width=\"10%\" class=\"DataGrid_Header_Cell\">Producto</th>");
    element.append("<th width=\"10%\" class=\"DataGrid_Header_Cell\">U.M</th>");
    element.append("<th width=\"15%\" class=\"DataGrid_Header_Cell\">Pedido</th>");
    element.append("<th width=\"10%\" class=\"DataGrid_Header_Cell\">Recibido</th>");
    element.append("<th width=\"10%\" class=\"DataGrid_Header_Cell\">Saldo</th>");
    element.append("<th width=\"5%\" class=\"DataGrid_Header_Cell\">P. Unitario</th>");

    element.append("</tr>");
    // CABECERA FIN
    //
    // CUERPO BEGIN
    element.append("<tr>");

    element.append(" <td  class=\"DataGrid_Body_Cell\"> " + data.productkey + "</td>");
    element.append(" <td  class=\"DataGrid_Body_Cell\"> " + data.partidaarancel + "</td>");
    element.append(" <td  class=\"DataGrid_Body_Cell\"> " + data.productname + "</td>");
    element.append(" <td  class=\"DataGrid_Body_Cell\"> " + data.uomname + "</td>");
    element.append(" <td  class=\"DataGrid_Body_Cell\"> " + data.productqty + "</td>");
    element.append(" <td  class=\"DataGrid_Body_Cell\"> " + data.productreceived + "</td>");
    element.append(" <td  class=\"DataGrid_Body_Cell\"> " + data.productdiff + "</td>");
    element.append(" <td  class=\"DataGrid_Body_Cell\"> " + data.productprice + "</td>");

    element.append("</tr>");
    // CUERPO END

    element.append("</table>");
    element.append("<div style=\"height:25px;\" />");
    element.append("</td>");
    element.append("</tr>");
    element.append("</table>");
    return element.toString();
  }

  private String Cabecera1() {
    StringBuffer element = new StringBuffer();

    element.append("<table class=\"Main_Client_TableEdition\">");
    element.append("<tr>");
    element.append("<td colspan=\"6\">");
    element.append("<div style=\"height:25px;\"/>");
    element
        .append("<table cellspacing=\"0\" cellpadding=\"1\" width=\"60%\" class=\"DataGrid_Header_Table \" style=\"table-layout: auto;\" id=\"selSection5\">");
    // Cabecera BEGIN
    element.append("<tr class=\"Popup_Client_Selector_DataGrid_HeaderRow\">");

    element.append("<th width=\"50%\" class=\"DataGrid_Header_Cell\">Organization</th>");
    element.append("<th width=\"50%\" class=\"DataGrid_Header_Cell\">Product</th>");

    element.append("</tr>");
    // CABECERA FIN
    // CUERPO BEGIN
    element.append("<tr>");

    element.append(" <td  class=\"DataGrid_Body_Cell\">Grupo Coam - Regrión Lima</td>");
    element.append(" <td  class=\"DataGrid_Body_Cell\">SK-20010001 - Lentes</td>");

    element.append("</tr>");
    // CUERPO END

    element.append("</table>");
    element.append("<div style=\"height:25px;\" />");
    element.append("</td>");
    element.append("</tr>");
    element.append("</table>");
    return element.toString();
  }

  private String strHeaderGridLevel_1() {
    StringBuffer element = new StringBuffer();

    element.append("<table class=\"Main_Client_TableEdition\">");
    element.append("<tr>");
    element.append("<td colspan=\"6\">");
    element.append("<div style=\"height:25px;\"/>");
    element
        .append("<table cellspacing=\"0\" cellpadding=\"1\" width=\"1205px\" class=\"DataGrid_Header_Table DataGrid_Body_Table\" style=\"table-layout: auto;\" id=\"selSection5\">");
    // Cabecera BEGIN
    element.append("<tr class=\"Popup_Client_Selector_DataGrid_HeaderRow\">");

    element.append("<th  class=\"DataGrid_Header_Cell\">Organización</th>");
    element.append("<th width=\"100\" class=\"DataGrid_Header_Cell\">Nro. Orden</th>");
    element.append("<th width=\"100\" class=\"DataGrid_Header_Cell\">Fec. Aprox</th>");
    element.append("<th width=\"310\" class=\"DataGrid_Header_Cell\">Proveedor</th>");
    element.append("<th width=\"80\" class=\"DataGrid_Header_Cell\">Cant. Pedido</th>");
    element.append("<th width=\"85\" class=\"DataGrid_Header_Cell\">Cant. Recibido</th>");
    element.append("<th width=\"70\" class=\"DataGrid_Header_Cell\">Cant. Saldo</th>");
    element.append("<th width=\"50\" class=\"DataGrid_Header_Cell\">Moneda</th>");
    element.append("<th width=\"90\" class=\"DataGrid_Header_Cell\">Total</th>");
    element.append("<th width=\"90\" class=\"DataGrid_Header_Cell\">Cant. Al Cierre</th>");

    element.append("</tr>");
    // CABECERA FIN
    // CUERPO BEGIN
    element.append("</table>");
    element.append("<div style=\"height:25px;\" />");
    element.append("</td>");
    element.append("</tr>");
    element.append("</table>");
    return element.toString();
  }

  private String strBodyGridLevel_1(trackingImportOrderData data) {
    StringBuffer element = new StringBuffer();

    element
        .append("<table cellspacing=\"0\" cellpadding=\"1\"  width=\"1200px\" class=\"DataGrid_Header_Table DataGrid_Body_Table\" style=\"table-layout: auto;\" id=\"selSection5\">");
    // CUERPO BEGIN
    element.append("<tr>");
    element.append(" <td  class=\"DataGrid_Body_Cell\"> " + data.orgname + "</td>");
    element.append(" <td width=\"100\" class=\"DataGrid_Body_Cell\"> " + data.documentno + "</td>");
    element.append(" <td width=\"100\" class=\"DataGrid_Body_Cell\"> " + data.datepromised
        + "</td>");
    element
        .append(" <td width=\"310\" class=\"DataGrid_Body_Cell\"> " + data.partnername + "</td>");
    element.append(" <td width=\"80\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.qtyordered
        + "</td>");
    element.append(" <td width=\"85\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.qtyreceived
        + "</td>");
    element.append(" <td width=\"70\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.qtydifference
        + "</td>");
    element.append(" <td width=\"50\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.scurrency
        + "</td>");
    element.append(" <td width=\"90\" class=\"DataGrid_Body_Cell_toCoam\"> "
        + data.ordergrandtotal + "</td>");
    element.append(" <td width=\"90\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.cierramanual
        + "</td>");
    element.append("</tr>");
    // CUERPO END
    element.append("</table>");
    return element.toString();
  }

  private String strHeaderGridLevel_2() {
    StringBuffer element = new StringBuffer();

    element.append("<table class=\"Main_Client_TableEdition\">");
    element.append("<tr>");
    element.append("<td colspan=\"6\">");
    element.append("<div style=\"height:25px;\"/>");
    element
        .append("<table cellspacing=\"0\" cellpadding=\"1\" width=\"1255px\" class=\"DataGrid_Header_Table_toCoam_color_x DataGrid_Body_Table_toCoam_color_x\" style=\"table-layout: auto;\" id=\"selSection5\">");
    // Cabecera BEGIN
    element.append("<tr class=\"Popup_Client_Selector_DataGrid_HeaderRow\">");

    element.append("<th  class=\"DataGrid_Header_Cell\">Código</th>");
    element.append("<th width=\"130\" class=\"DataGrid_Header_Cell\">Nro. Parte</th>");
    element.append("<th width=\"120\" class=\"DataGrid_Header_Cell\">Nro Partida</th>");
    element.append("<th width=\"480\" class=\"DataGrid_Header_Cell\">Producto</th>");
    element.append("<th width=\"70\" class=\"DataGrid_Header_Cell\">U.M</th>");
    element.append("<th width=\"60\" class=\"DataGrid_Header_Cell\">Pedido</th>");
    element.append("<th width=\"60\" class=\"DataGrid_Header_Cell\">Recibido</th>");
    element.append("<th width=\"60\" class=\"DataGrid_Header_Cell\">Saldo</th>");
    element.append("<th width=\"50\" class=\"DataGrid_Header_Cell\">P. Unitario</th>");
    element.append("<th width=\"90\" class=\"DataGrid_Header_Cell\">Nro. Requer</th>");

    element.append("</tr>");
    // CABECERA FIN
    // CUERPO BEGIN
    element.append("</table>");
    element.append("<div style=\"height:25px;\" />");
    element.append("</td>");
    element.append("</tr>");
    element.append("</table>");
    return element.toString();
  }

  private String strHeaderGridLevel_3() {
    StringBuffer element = new StringBuffer();

    element.append("<table class=\"Main_Client_TableEdition\">");
    element.append("<tr>");
    element.append("<td colspan=\"6\">");
    element.append("<div style=\"height:25px;\"/>");
    element
        .append("<table cellspacing=\"0\" cellpadding=\"1\" width=\"1505px\" class=\"DataGrid_Header_Table_toCoam_color_y DataGrid_Body_Table_toCoam_color_y\" style=\"table-layout: auto;\" id=\"selSection5\">");
    // Cabecera BEGIN
    element.append("<tr class=\"Popup_Client_Selector_DataGrid_HeaderRow\">");

    element.append("<th  class=\"DataGrid_Header_Cell\">Nro Parcial</th>");
    element.append("<th width=\"180\" class=\"DataGrid_Header_Cell\">Importación</th>");
    element.append("<th width=\"85\" class=\"DataGrid_Header_Cell\">F. Desp. Prv.</th>");
    element.append("<th width=\"85\" class=\"DataGrid_Header_Cell\">F. Ing Fwd</th>");
    element.append("<th width=\"85\" class=\"DataGrid_Header_Cell\">F. Despacho</th>");
    element.append("<th width=\"85\" class=\"DataGrid_Header_Cell\">F. Embarque</th>");
    element.append("<th width=\"85\" class=\"DataGrid_Header_Cell\">F.Lleg. a Lima</th>");
    element.append("<th width=\"85\" class=\"DataGrid_Header_Cell\">F. Aprox Alm.</th>");
    element.append("<th width=\"85\" class=\"DataGrid_Header_Cell\">F. Lleg Alm.</th>");
    element.append("<th width=\"60\" class=\"DataGrid_Header_Cell\">Cant. Parcial</th>");
    element.append("<th width=\"60\" class=\"DataGrid_Header_Cell\">%Adv</th>");
    element.append("<th width=\"60\" class=\"DataGrid_Header_Cell\">Tlc Adv</th>");
    element.append("<th width=\"70\" class=\"DataGrid_Header_Cell\">Nro Factura</th>");

    element.append("<th width=\"80\" class=\"DataGrid_Header_Cell\">F. Emi. Fact.</th>");
    element.append("<th width=\"80\" class=\"DataGrid_Header_Cell\">F. Vco. Fact.</th>");
    element.append("<th width=\"75\" class=\"DataGrid_Header_Cell\">Total Fact.</th>");
    element.append("<th width=\"70\" class=\"DataGrid_Header_Cell\">Pago. Fact.</th>");

    element.append("</tr>");
    // CABECERA FIN
    // CUERPO BEGIN
    element.append("</table>");
    element.append("<div style=\"height:25px;\" />");
    element.append("</td>");
    element.append("</tr>");
    element.append("</table>");
    return element.toString();
  }

  private String strBodyGridLevel_2(trackingImportOrderData data) {
    StringBuffer element = new StringBuffer();

    element
        .append("<table cellspacing=\"0\" cellpadding=\"1\"  width=\"1250px\" class=\"DataGrid_Header_Table_toCoam_color_x DataGrid_Body_Table_toCoam_color_x\" style=\"table-layout: auto;\" id=\"selSection5\">");
    // CUERPO BEGIN
    element.append("<tr>");
    element.append(" <td  class=\"DataGrid_Body_Cell\"> " + data.productkey + "</td>");
    element.append(" <td width=\"130\" class=\"DataGrid_Body_Cell\"> " + data.internalcode
        + "</td>");
    element.append(" <td width=\"120\" class=\"DataGrid_Body_Cell\"> " + data.partidaarancel
        + "</td>");
    element
        .append(" <td width=\"480\" class=\"DataGrid_Body_Cell\"> " + data.productname + "</td>");
    element.append(" <td width=\"70\" class=\"DataGrid_Body_Cell\"> " + data.uomname + "</td>");
    element.append(" <td width=\"60\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.productqty
        + "</td>");
    element.append(" <td width=\"60\" class=\"DataGrid_Body_Cell_toCoam\"> "
        + data.productreceived + "</td>");
    element.append(" <td width=\"62\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.productdiff
        + "</td>");
    element.append(" <td width=\"60\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.productprice
        + "</td>");
    element.append(" <td width=\"90\" class=\"DataGrid_Body_Cell_toCoam\"> "
        + data.requisitiondocno + "</td>");
    element.append("</tr>");
    // CUERPO END
    element.append("</table>");
    return element.toString();
  }

  private String strBodyGridLevel_3(trackingImportOrderData data) {
    StringBuffer element = new StringBuffer();

    element
        .append("<table cellspacing=\"0\" cellpadding=\"1\"  width=\"1500px\" class=\"DataGrid_Header_Table_toCoam_color_y DataGrid_Body_Table_toCoam_color_y\" style=\"table-layout: auto;\" id=\"selSection5\">");
    // CUERPO BEGIN
    element.append("<tr>");
    element.append(" <td  class=\"DataGrid_Body_Cell\"> " + data.partialdocno + "</td>");
    element.append(" <td width=\"180\" class=\"DataGrid_Body_Cell\"> " + data.importname + "</td>");
    element.append(" <td width=\"85\" class=\"DataGrid_Body_Cell\"> " + data.datedespprv + "</td>");
    element.append(" <td width=\"85\" class=\"DataGrid_Body_Cell_toCoam\"> "
        + data.datetoforwarder + "</td>");
    element.append(" <td width=\"85\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.datedespacho
        + "</td>");
    element.append(" <td width=\"85\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.dateshipment
        + "</td>");
    element.append(" <td width=\"85\" class=\"DataGrid_Body_Cell_toCoam\"> "
        + data.datearrivalcapital + "</td>");
    element.append(" <td width=\"85\" class=\"DataGrid_Body_Cell_toCoam\"> "
        + data.dateaproxarrivalware + "</td>");
    element.append(" <td width=\"85\" class=\"DataGrid_Body_Cell_toCoam\"> "
        + data.datearrivalwarehouse + "</td>");
    element.append(" <td width=\"75\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.partialqty
        + "</td>");
    element.append(" <td width=\"60\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.adv + "</td>");
    element.append(" <td width=\"60\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.tlcadv
        + "</td>");
    element.append(" <td width=\"72\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.invoicenro
        + "</td>");
    element.append(" <td width=\"80\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.dateinvoice
        + "</td>");
    element.append(" <td width=\"78\" class=\"DataGrid_Body_Cell_toCoam\"> " + data.datedue
        + "</td>");
    element.append(" <td width=\"77\" class=\"DataGrid_Body_Cell_toCoam\"> "
        + data.invoicegrandtotal + "</td>");
    element.append(" <td width=\"70\" class=\"DataGrid_Body_Cell_toCoam\"> "
        + data.invoicetotalpaid + "</td>");
    element.append("</tr>");
    // CUERPO END
    element.append("</table>");
    return element.toString();
  }

  // private String loadNodes(VariablesSecureApp vars, String key, trackingImportOrderData[]
  // getData)
  private String loadNodes(trackingImportOrderData[] dataPut,
      Map<String, trackingImportOrderData[]> resMapAsociate,
      Map<String, trackingImportOrderData[]> resMapSub, String strLevel) throws ServletException {
    String TreeType = "00";
    String TreeID = "ID_REF";
    // String TreeName = "PRUEBA";
    String TreeDescription = "";
    StringBuffer menu = new StringBuffer();

    menu.append("\n<ul class=\"dhtmlgoodies_tree_to_coam\">\n");

    if (dataPut != null) {
      if (strLevel.equals("0"))
        menu.append(WindowTreeUtility.addNodeTest(strHeaderGridLevel_1(), null, null, true, TreeID));
      else if (strLevel.equals("1"))
        menu.append(WindowTreeUtility.addNodeTest(strHeaderGridLevel_2(), null, null, true, TreeID));
      else if (strLevel.equals("2"))
        menu.append(WindowTreeUtility.addNodeTest(strHeaderGridLevel_3(), null, null, true, TreeID));
    }

    if (dataPut != null)
      menu.append(generateTree(dataPut, resMapAsociate, resMapSub, true, strLevel));

    menu.append("\n</ul>\n");

    /*
     * if(strLevel.equals("0")){ menu.append("\n<ul class=\"dhtmlgoodies_tree_to_coam\">\n");
     * menu.append(WindowTreeUtility.addNodeTest(strHeaderGridLevel_1(), null, null, true, TreeID));
     * if(dataPut!=null) menu.append(generateTree(dataPut, resMapAsociate,resMapSub, true,"0"));
     * menu.append("\n</ul>\n"); } else if(strLevel.equals("1")){
     * menu.append("\n<ul class=\"dhtmlgoodies_tree_to_coam\">\n");
     * menu.append(WindowTreeUtility.addNodeTest(strHeaderGridLevel_2(), null, null, true, TreeID));
     * if(dataPut!=null) menu.append(generateTree(dataPut, resMapAsociate,null,true,"1"));
     * menu.append("\n</ul>\n"); } else if(strLevel.equals("2")){
     * menu.append("\n<ul class=\"dhtmlgoodies_tree_to_coam\">\n");
     * menu.append(WindowTreeUtility.addNodeTest(strHeaderGridLevel_3(), null, null, true, TreeID));
     * if(dataPut!=null) menu.append(generateTree(dataPut, null,null,true,"2"));
     * menu.append("\n</ul>\n"); }
     */

    // nodeIdList = null;
    return menu.toString();
  }

  private static Map<String, List<WindowTreeData>> buildTree(WindowTreeData[] input) {
    Map<String, List<WindowTreeData>> resMap = new HashMap<String, List<WindowTreeData>>();

    for (WindowTreeData elem : input) {
      List<WindowTreeData> list = resMap.get(elem.parentId);
      if (list == null) {
        list = new ArrayList<WindowTreeData>();
      }
      list.add(elem);
      resMap.put(elem.parentId, list);
    }

    return resMap;
  }

  private String generateTree(trackingImportOrderData[] data,
      Map<String, trackingImportOrderData[]> resMap,
      Map<String, trackingImportOrderData[]> resMapSub, boolean haschild, String strlevel)
      throws ServletException {

    boolean hayDatos = false;
    StringBuffer strResultado = new StringBuffer();
    strResultado.append("<ul style=\"display:block\">");

    String tosetTree = null;

    if (strlevel.equals("0")) {

      for (int i = 0; i < data.length; i++) {
        // tosetTree = strLevel_1(data[i]);

        tosetTree = strBodyGridLevel_1(data[i]);
        strResultado.append(WindowTreeUtility.addNodeTest(tosetTree, null, null, true, "01"));
        trackingImportOrderData[] dataline = resMap.get(data[i].orderid);

        if (dataline != null)
          strResultado.append(loadNodes(dataline, resMapSub, null, "1"));

      }

    } else if (strlevel.equals("1")) {
      for (int i = 0; i < data.length; i++) {
        // tosetTree = strLevel_2(data[i]);
        tosetTree = strBodyGridLevel_2(data[i]);
        strResultado.append(WindowTreeUtility.addNodeTest(tosetTree, null, null, true, "01"));
        // trackingImportOrderData[] dataline = resMapSub.get(data[i].orderimportlineid); //este es
        trackingImportOrderData[] dataline = resMap.get(data[i].orderimportlineid);
        if (dataline != null) {
          strResultado.append(loadNodes(dataline, null, null, "2"));
        }

      }

    } else if (strlevel.equals("2")) {
      for (int i = 0; i < data.length; i++) {

        tosetTree = strBodyGridLevel_3(data[i]);
        strResultado.append(WindowTreeUtility.addNodeTest(tosetTree, null, null, true, "01"));

      }

    }

    strResultado.append("</li></ul>");
    // return (hayDatos ? strResultado.toString() : "");
    return (strResultado.toString());
  }

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
