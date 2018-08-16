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
 * All portions are Copyright (C) 2001-2013 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_actionButton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Tax;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.financial.FinancialUtils;
import org.openbravo.model.common.order.Order;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.SCO_Utils;

public class CopyFromPOOrder extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static final BigDecimal ZERO = BigDecimal.ZERO;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strProcessId = vars.getStringParameter("inpProcessId");
      String strWindow = vars.getStringParameter("inpwindowId");
      String strTab = vars.getStringParameter("inpTabId");
      String strKey = vars.getGlobalVariable("inpcOrderId", strWindow + "|C_Order_ID");
      printPage(response, vars, strKey, strWindow, strTab, strProcessId);

      vars.setSessionValue("CopyFromPOOrder|C_Order_ID", strKey);

    } else if (vars.commandIn("SAVE")) {
      String strWindow = vars.getStringParameter("inpwindowId");
      String strOrder = vars.getStringParameter("inpcOrderId");
      String strKey = vars.getRequestGlobalVariable("inpKey", strWindow + "|C_Order_ID");
      String strTab = vars.getStringParameter("inpTabId");

      String strWindowPath = Utility.getTabURL(strTab, "R", true, getDireccion());
      if (strWindowPath.equals("")) {
        strWindowPath = strDefaultServlet;
      }

      OBError myError = processButton(vars, strKey, strOrder);
      log4j.debug(myError.getMessage());
      vars.setMessage(strTab, myError);

      vars.removeSessionValue("CopyFromPOOrder|C_Order_ID");

      printPageClosePopUp(response, vars, strWindowPath);
    } else
      pageErrorPopUp(response);
  }

  private OBError processButton(VariablesSecureApp vars, String strKey, String strOrder) {
    OBError myError = null;
    int i = 0;
    String strPriceActual = "";
    String strPriceList = "";
    String strPriceLimit = "";
    String strDiscount = "";
    String strGrossUnitPrice = "0";
    String strGrossBaseUnitPrice = "0";
    String strGrossAmount = "0";
    String strNetPriceList = "0";
    String strGrossPriceList = "0";
    Connection conn = null;
    try {
      conn = getTransactionConnection();
      CopyFromPOOrderData[] data = CopyFromPOOrderData.selectLines(this, strOrder);
      CopyFromPOOrderData[] orderData = CopyFromPOOrderData.select(this, strKey);
      Order order = OBDal.getInstance().get(Order.class, strKey);
      int stdPrecision = 0;
      int pricePrecision = 0;
      OBContext.setAdminMode(true);
      try {
        stdPrecision = order.getCurrency().getStandardPrecision().intValue();
        pricePrecision = order.getCurrency().getPricePrecision().intValue();
      } finally {
        OBContext.restorePreviousMode();
      }

      int count = 0;
      for (i = 0; data != null && i < data.length; i++) {
        if (data[i].cOrderDiscountId != null && !"".equals(data[i].cOrderDiscountId)) {
          continue;
        }

        // Get actual pricelist and pricelimit
        Date now = new Date();
        String[] product_data = SCO_Utils.getProductPricesByDate(new DalConnectionProvider(),
            orderData[0].adOrgId, data[i].mProductId, orderData[0].mPricelistId, now);
        BigDecimal newPriceList = new BigDecimal(product_data[0]);

        BigDecimal discount = newPriceList.subtract(new BigDecimal(data[i].priceactual))
            .multiply(new BigDecimal("100")).divide(newPriceList, 2, BigDecimal.ROUND_HALF_EVEN);

        strPriceActual = data[i].priceactual;
        strPriceList = newPriceList.toString();
        strPriceLimit = product_data[2];

        strDiscount = discount.toString();

        String strCTaxID = Tax.get(this, data[i].mProductId, orderData[0].datepromised,
            orderData[0].adOrgId, orderData[0].mWarehouseId.equals("") ? vars.getWarehouse()
                : orderData[0].mWarehouseId, CopyFromPOOrderData.cBPartnerLocationId(this,
                orderData[0].cBpartnerId), CopyFromPOOrderData.cBPartnerLocationId(this,
                orderData[0].cBpartnerId), orderData[0].cProjectId, orderData[0].issotrx
                .equals("Y") ? true : false);
        if (strCTaxID.equals("")) {
          myError = Utility.translateError(this, vars, vars.getLanguage(),
              Utility.messageBD(this, "TaxNotFound", vars.getLanguage()));
          return myError;
        }
        // Processing for taxincluded
        if (order.getPriceList().isPriceIncludesTax()) {
          BigDecimal grossAmount, grossUnitPrice, qtyOrdered, priceActual;

          strGrossUnitPrice = strPriceActual;
          strGrossBaseUnitPrice = strGrossUnitPrice;
          grossUnitPrice = (strGrossUnitPrice.equals("") ? ZERO
              : (new BigDecimal(strGrossUnitPrice))).setScale(pricePrecision,
              BigDecimal.ROUND_HALF_UP);
          qtyOrdered = (data[i].qtyordered.equals("") ? ZERO : new BigDecimal(data[i].qtyordered));
          grossAmount = qtyOrdered.multiply(grossUnitPrice).setScale(stdPrecision,
              BigDecimal.ROUND_HALF_UP);
          priceActual = FinancialUtils.calculateNetFromGross(strCTaxID, grossAmount,
              pricePrecision, grossAmount, qtyOrdered);

          strGrossPriceList = strPriceList;
          strPriceActual = priceActual.toString();
          strNetPriceList = priceActual.toString();
          strPriceLimit = priceActual.toString();
          strGrossAmount = grossAmount.toString();
        } else {
          strNetPriceList = strPriceList;
        }

        int line = Integer.valueOf(orderData[0].line.equals("") ? "0" : orderData[0].line)
            .intValue() + ((count + 1));
        String strCOrderlineID = SequenceIdData.getUUID();
        try {
          String isInstance = CopyFromPOOrderData.getIsInstanceValue(conn, this,
              data[i].mAttributesetinstanceId);
          if (isInstance != null && isInstance.equalsIgnoreCase("Y")) {
            String strMAttributesetinstanceID = SequenceIdData.getUUID();
            CopyFromPOOrderData.copyAttributes(conn, this, strMAttributesetinstanceID,
                vars.getUser(), vars.getUser(), data[i].mAttributesetinstanceId);
            CopyFromPOOrderData.copyInstances(conn, this, strMAttributesetinstanceID,
                vars.getUser(), vars.getUser(), data[i].mAttributesetinstanceId);
            data[i].mAttributesetinstanceId = strMAttributesetinstanceID;
          }
          CopyFromPOOrderData.insertCOrderline(
              conn,
              this,
              strCOrderlineID,
              orderData[0].adClientId,
              orderData[0].adOrgId,
              vars.getUser(),
              strKey,
              Integer.toString(line),
              orderData[0].cBpartnerId,
              orderData[0].cBpartnerLocationId.equals("") ? ExpenseSOrderData.cBPartnerLocationId(
                  this, orderData[0].cBpartnerId) : orderData[0].cBpartnerLocationId,
              orderData[0].dateordered, orderData[0].datepromised, data[i].description,
              data[i].mProductId, orderData[0].mWarehouseId.equals("") ? vars.getWarehouse()
                  : orderData[0].mWarehouseId, data[i].cUomId, data[i].qtyordered,
              data[i].quantityorder, data[i].cCurrencyId, strNetPriceList, strPriceActual,
              strPriceLimit, strCTaxID, strDiscount, data[i].mProductUomId, data[i].orderline,
              data[i].mAttributesetinstanceId, strGrossPriceList, strGrossUnitPrice,
              strGrossAmount, strGrossBaseUnitPrice, data[i].cProjectId, data[i].user1Id,
              data[i].user2Id, data[i].cCostcenterId, data[i].aAssetId);
          count++;
        } catch (ServletException ex) {
          myError = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
          releaseRollbackConnection(conn);
          return myError;
        }

      }

      releaseCommitConnection(conn);
      myError = new OBError();
      myError.setType("Success");
      myError.setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));
      myError.setMessage(Utility.messageBD(this, "RecordsCopied", vars.getLanguage()) + count);
    } catch (Exception e) {
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      myError = Utility.translateError(this, vars, vars.getLanguage(), "ProcessRunError");
    }
    return myError;
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strKey,
      String windowId, String strTab, String strProcessId) throws IOException, ServletException {
    log4j.debug("Output: Button process Copy lines");
    ActionButtonDefaultData[] data = null;
    String strHelp = "", strDescription = "";
    if (vars.getLanguage().equals("en_US")) {
      data = ActionButtonDefaultData.select(this, strProcessId);
    } else {
      data = ActionButtonDefaultData.selectLanguage(this, vars.getLanguage(), strProcessId);
    }
    System.out.println("strProcessId:" + strProcessId);
    if (data != null && data.length != 0) {
      strDescription = data[0].description;
      strHelp = data[0].help;
    }
    String[] discard = { "" };
    if (strHelp.equals("")) {
      discard[0] = "helpDiscard";
    }
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/CopyFromPOOrder", discard).createXmlDocument();
    xmlDocument.setParameter("key", strKey);
    xmlDocument.setParameter("window", windowId);
    xmlDocument.setParameter("tab", strTab);
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("question",
        Utility.messageBD(this, "StartProcess?", vars.getLanguage()));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("description", strDescription);
    xmlDocument.setParameter("help", strHelp);
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet Copy from order";
  } // end of the getServletInfo() method
}
