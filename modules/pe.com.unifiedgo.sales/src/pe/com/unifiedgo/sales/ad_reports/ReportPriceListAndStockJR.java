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
package pe.com.unifiedgo.sales.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.Query;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.plm.Product;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportPriceListAndStockJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    // HttpSession session = request.getSession(true);
    // @SuppressWarnings("rawtypes")
    // Enumeration e = session.getAttributeNames();
    // while (e.hasMoreElements()) {
    // String name = (String) e.nextElement();
    // System.out.println("name: " + name + " - value: " +
    // session.getAttribute(name));
    // }

    // Get user Client's base currency
    String strUserCurrencyId = Utility.stringBaseCurrencyId(this, vars.getClient());
    if (vars.commandIn("DEFAULT")) {
      String strAD_Org_ID = vars.getGlobalVariable("inpOrgId", "ReportPriceListAndStockJR|OrgId",
          "");
      String strC_Tax_ID = vars.getGlobalVariable("inpcTaxId",
          "ReportPriceListAndStockJR|C_Tax_ID", "");
      String strmPENPriceListID = vars.getGlobalVariable("inpPENPriceList",
          "ReportPriceListAndStockJR|PENPriceList", "");
      String strmUSDPriceListID = vars.getGlobalVariable("inpUSDPriceList",
          "ReportPriceListAndStockJR|USDPriceList", "");
      String strSearchKey = vars.getGlobalVariable("inpSearchKey",
          "ReportPriceListAndStockJR|SearchKey", "");
      String strmBrandID = vars.getGlobalVariable("inpProductBrand",
          "ReportPriceListAndStockJR|ProductBrand", "");
      String strStockMin = vars.getNumericGlobalVariable("inpStockMin",
          "ReportPriceListAndStockJR|StockMin", "");
      String strStockMax = vars.getNumericGlobalVariable("inpStockMax",
          "ReportPriceListAndStockJR|StockMax", "");
      String mProductId = vars.getGlobalVariable("inpmProductId",
          "ReportPriceListAndStockJR|M_Product_Id", "");
      String productLineId = vars.getGlobalVariable("inpProductLine",
          "ReportPriceListAndStockJR|ProductLine_Id", "");

      vars.removeSessionValue("ReportPriceListAndStockJR|productstocklines");

      printPageDataSheet(request, response, vars, null, strAD_Org_ID, strmPENPriceListID,
          strmUSDPriceListID, strC_Tax_ID, strSearchKey, strmBrandID, strStockMin, strStockMax,
          mProductId, productLineId, true, null);

    } else if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      String strAD_Org_ID = vars.getStringParameter("inpOrgId");
      String strC_Tax_ID = vars.getGlobalVariable("inpcTaxId",
          "ReportPriceListAndStockJR|C_Tax_ID", "");
      String strmPENPriceListID = vars.getGlobalVariable("inpPENPriceList",
          "ReportPriceListAndStockJR|PENPriceList", "");
      String strmUSDPriceListID = vars.getGlobalVariable("inpUSDPriceList",
          "ReportPriceListAndStockJR|USDPriceList", "");
      String strSearchKey = vars.getStringParameter("inpSearchKey", "");
      String strmBrandID = vars.getStringParameter("inpProductBrand");
      String strStockMin = vars.getNumericParameter("inpStockMin");
      String strStockMax = vars.getNumericParameter("inpStockMax");
      String mProductId = vars.getStringParameter("inpmProductId");
      String productLineId = vars.getStringParameter("inpProductLine");

      ReportPriceListAndStockJRData[] data = ReportPriceListAndStockJRData.select(vars,
          strAD_Org_ID, vars.getSessionValue("#AD_CLIENT_ID"), strSearchKey, strmBrandID,
          strStockMin, strStockMax, strmPENPriceListID, strmUSDPriceListID);
      vars.setSessionObject("ReportPriceListAndStockJR|productstocklines", data);

      printPageDataSheet(request, response, vars, data, strAD_Org_ID, strmPENPriceListID,
          strmUSDPriceListID, strC_Tax_ID, strSearchKey, strmBrandID, strStockMin, strStockMax,
          mProductId, productLineId, false, null);

    } else if (vars.commandIn("GRIDLIST")) {
      String strAD_Org_ID = vars.getStringParameter("inpOrgId");
      String strC_Tax_ID = vars.getGlobalVariable("inpcTaxId",
          "ReportPriceListAndStockJR|C_Tax_ID", "");
      String strmPENPriceListID = vars.getGlobalVariable("inpPENPriceList",
          "ReportPriceListAndStockJR|PENPriceList", "");
      String strmUSDPriceListID = vars.getGlobalVariable("inpUSDPriceList",
          "ReportPriceListAndStockJR|USDPriceList", "");
      String strSearchKey = vars.getStringParameter("inpSearchKey", "");
      String strmBrandID = vars.getStringParameter("inpProductBrand");
      String strStockMin = vars.getNumericParameter("inpStockMin");
      String strStockMax = vars.getNumericParameter("inpStockMax");
      String mProductId = vars.getStringParameter("inpmProductId");
      String productLineId = vars.getStringParameter("inpProductLine");

      String strProductIdToShow = vars.getStringParameter("inpProductIdToShow");

      ReportPriceListAndStockJRData[] data = (ReportPriceListAndStockJRData[]) vars
          .getSessionObject("ReportPriceListAndStockJR|productstocklines");

      printGrid(request, response, vars, data, strAD_Org_ID, strmPENPriceListID,
          strmUSDPriceListID, strC_Tax_ID, strSearchKey, strmBrandID, strStockMin, strStockMax,
          mProductId, productLineId, false, strProductIdToShow);

    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, ReportPriceListAndStockJRData[] productStockData,
      String strAD_Org_ID, String strmPENPriceListID, String strmUSDPriceListID,
      String strC_Tax_ID, String strSearchKey, String strmBrandID, String strStockMin,
      String strStockMax, String mProductId, String productLineId, boolean isFirstLoad,
      String strProductIdToShow) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportPriceListAndStockJRData[] data = null;
    String strConvRateErrorMsg = "";

    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/sales/ad_reports/ReportPriceListAndStockFilterJR", discard)
        .createXmlDocument();

    // String searchKey = strSearchKey.trim();
    if (vars.commandIn("EDIT_HTML", "EDIT_PDF", "GETDETAIL")) {
      OBError myMessage = null;
      myMessage = new OBError();
      try {
        if (productStockData == null) {
          data = ReportPriceListAndStockJRData.select(vars, strAD_Org_ID,
              vars.getSessionValue("#AD_CLIENT_ID"), strSearchKey, strmBrandID, strStockMin,
              strStockMax, strmPENPriceListID, strmUSDPriceListID);
        } else {
          data = productStockData;
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
        if (data == null || data.length == 0) {
          discard[0] = "selEliminar";
          data = ReportPriceListAndStockJRData.set();
        }
      }
    }

    else {
      if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
        discard[0] = "selEliminar";
        data = ReportPriceListAndStockJRData.set();
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportPriceListAndStockFilterJR",
          false, "", "", "", false, "ad_reports", strReplaceWith, false, true);
      toolbar.prepareSimpleToolBarTemplate();
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.sales.ad_reports.ReportPriceListAndStockJR");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "ReportPriceListAndStockFilterJR.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "ReportPriceListAndStockFilterJR.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("ReportPriceListAndStockJR");
        vars.removeMessage("ReportPriceListAndStockJR");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("SearchKey", (strSearchKey != null) ? strSearchKey : "");

      xmlDocument.setParameter("mProduct", mProductId);
      xmlDocument.setParameter("productDescription",
          ReportPriceListAndStockJRData.selectMproduct(this, mProductId));

      xmlDocument.setParameter("ProductLine", productLineId);
      xmlDocument.setParameter("productLineDescription",
          ReportPriceListAndStockJRData.selectPrdcProductgroup(this, productLineId));

      Organization org;
      org = OBDal.getInstance().get(Organization.class, strAD_Org_ID);
      xmlDocument.setParameter("OrgId", strAD_Org_ID);
      xmlDocument.setParameter("OrgDescription", (org != null) ? org.getIdentifier() : "");

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "",
            "C86D7486B3E54743A598773205192262", "", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "PriceListAndStockJR"), Utility.getContext(this, vars,
                "#User_Client", "PriceListAndStockJR"), 0);

        Utility
            .fillSQLParameters(this, vars, null, comboTableData, "PriceListAndStockFilterJR", "");
        xmlDocument.setData("reportC_Tax_ID", "liststructure", comboTableData.select(false));
        comboTableData = null;

        if (isFirstLoad) {
          Query q2 = OBDal
              .getInstance()
              .getSession()
              .createSQLQuery(
                  "SELECT c_tax_id FROM c_tax WHERE em_sco_specialtax='SCOIGV' AND ad_client_id='"
                      + vars.getSessionValue("#AD_CLIENT_ID") + "' LIMIT 1");
          strC_Tax_ID = (String) q2.uniqueResult();

          xmlDocument.setParameter("FirstLoad", "FirstLoad = \"YES\"; ");
        } else {
          xmlDocument.setParameter("FirstLoad", "FirstLoad = \"NO\"; ");
        }

      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_Brand_ID",
            "", "", Utility.getContext(this, vars, "#AccessibleOrgTree", "PriceListAndStockJR"),
            Utility.getContext(this, vars, "#User_Client", "PriceListAndStockJR"), 0);
        Utility
            .fillSQLParameters(this, vars, null, comboTableData, "PriceListAndStockFilterJR", "");
        xmlDocument.setData("reportProductBrand", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      xmlDocument.setParameter(
          "ProductBrandArray",
          Utility.arrayDobleEntrada(
              "arrProductBrand",
              ReportPriceListAndStockJRData.selectProductBrandDouble(this,
                  Utility.getContext(this, vars, "#User_Client", "PriceListAndStockFilterJR"))));

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "",
            "ED958BF42BC6469BB1582FCF3550F5B7", "", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "PriceListAndStockJR"), Utility.getContext(this, vars,
                "#User_Client", "PriceListAndStockJR"), 0);
        Utility
            .fillSQLParameters(this, vars, null, comboTableData, "PriceListAndStockFilterJR", "");
        xmlDocument.setData("reportPENPriceList", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      xmlDocument.setParameter(
          "PENPriceListArray",
          Utility.arrayDobleEntrada(
              "arrPENPriceList",
              ReportPriceListAndStockJRData.selectPENPriceListDouble(this,
                  Utility.getContext(this, vars, "#User_Client", "PriceListAndStockFilterJR"))));

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "",
            "96DE7823F3024435ADE4810449695E82", "", Utility.getContext(this, vars,
                "#AccessibleOrgTree", "PriceListAndStockJR"), Utility.getContext(this, vars,
                "#User_Client", "PriceListAndStockJR"), 0);
        Utility
            .fillSQLParameters(this, vars, null, comboTableData, "PriceListAndStockFilterJR", "");
        xmlDocument.setData("reportUSDPriceList", "liststructure", comboTableData.select(false));
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      xmlDocument.setParameter(
          "USDPriceListArray",
          Utility.arrayDobleEntrada(
              "arrUSDPriceList",
              ReportPriceListAndStockJRData.selectUSDPriceListDouble(this,
                  Utility.getContext(this, vars, "#User_Client", "PriceListAndStockFilterJR"))));

      xmlDocument.setParameter("cTaxID", strC_Tax_ID);
      xmlDocument.setParameter("PENPriceList", strmPENPriceListID);
      xmlDocument.setParameter("USDPriceList", strmUSDPriceListID);
      xmlDocument.setParameter("ProductBrand", strmBrandID);
      xmlDocument.setParameter("StockMin", strStockMin);
      xmlDocument.setParameter("StockMax", strStockMax);

      xmlDocument.setData("structure1", data);
      // Print document in the output
      out.println(xmlDocument.print());
      out.close();
    }
  }

  private void printGrid(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, ReportPriceListAndStockJRData[] productStockData,
      String strAD_Org_ID, String strmPENPriceListID, String strmUSDPriceListID,
      String strC_Tax_ID, String strSearchKey, String strmBrandID, String strStockMin,
      String strStockMax, String mProductId, String productLineId, boolean isFirstLoad,
      String strProductIdToShow) throws IOException, ServletException {

    String[] discard = { "discard" };
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/sales/ad_reports/ReportPriceListAndStockGrid", discard)
        .createXmlDocument();
    ReportPriceListAndStockJRData[] detailData = null;

    if (strProductIdToShow != null) {
      detailData = ReportPriceListAndStockJRData.selectProductPrice(vars, strAD_Org_ID,
          vars.getSessionValue("#AD_CLIENT_ID"), strmPENPriceListID, strmUSDPriceListID,
          strC_Tax_ID, strProductIdToShow);
    }
    if (detailData == null || detailData.length == 0) {
      detailData = ReportPriceListAndStockJRData.set();
    }

    xmlDocument.setData("structure", detailData);

    Product product = null;
    if (strProductIdToShow != null && !"".equals(strProductIdToShow)) {
      product = OBDal.getInstance().get(Product.class, strProductIdToShow);
    }

    String productRefDescription = "";
    if (product != null) {
      productRefDescription = product.getIdentifier();
    }
    xmlDocument.setParameter("ProductRef", productRefDescription);

    String productImgHTML = "<img style=\"max-width:300px; max-height:180px;\" height=\"180px\" src=\"../web/images/noimage.png\">";
    if (product != null) {
      String imgBase64 = ReportPriceListAndStockJRData.selectBase64Image(this, strProductIdToShow);
      if (imgBase64 != null && !"".equals(imgBase64)) {
        productImgHTML = "<img style=\"max-width:300px; max-height:180px;\" height=\"180px\" src=\"data:image/png;base64,"
            + imgBase64 + "\">";
      }
    }
    xmlDocument.setParameter("ProductImg64", productImgHTML);

    JSONObject msg = new JSONObject();
    try {
      msg.put("detailBody", xmlDocument.print());
    } catch (JSONException e) {
      log4j.error("JSON object error" + msg.toString());
    }
    response.setContentType("application/json; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(msg.toString());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
