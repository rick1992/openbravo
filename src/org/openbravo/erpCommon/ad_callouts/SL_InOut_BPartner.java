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
package org.openbravo.erpCommon.ad_callouts;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.BpartnerMiscData;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.order.Order;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.utils.Replace;
import org.openbravo.xmlEngine.XmlDocument;

public class SL_InOut_BPartner extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strChanged = vars.getStringParameter("inpLastFieldChanged");
      if (log4j.isDebugEnabled())
        log4j.debug("CHANGED: " + strChanged);
      String strBPartner = vars.getStringParameter("inpcBpartnerId");
      String strLocation = vars.getStringParameter("inpcBpartnerId_LOC");
      String strContact = vars.getStringParameter("inpcBpartnerId_CON");
      String strWindowId = vars.getStringParameter("inpwindowId");
      String strProjectId = vars.getStringParameter("inpcProjectId");
      String strIsSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
      String strTabId = vars.getStringParameter("inpTabId");
      String strMWarehouseId = vars.getStringParameter("inpmWarehouseId");

      try {
        printPage(response, vars, strBPartner, strLocation, strContact, strWindowId, strProjectId,
            strIsSOTrx, strTabId, strMWarehouseId);
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strBPartner,
      String strLocation, String strContact, String strWindowId, String strProjectId,
      String strIsSOTrx, String strTabId, String strMWarehouseId)
      throws IOException, ServletException {

    /*
     * Enumeration<String> e = vars.getParameterNames(); while (e.hasMoreElements()) {
     * System.out.println(e.nextElement()); }
     */

    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine
        .readXmlTemplate("org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    BpartnerMiscData[] data = BpartnerMiscData.select(this, strBPartner);

    String strUserRep = "";
    if (data != null && data.length > 0) {
      // strUserRep = SEOrderBPartnerData.userIdSalesRep(this, data[0].salesrepId);
      strUserRep = data[0].salesrepId;
    }

    StringBuffer resultado = new StringBuffer();
    resultado.append("var calloutName='SL_InOut_BPartner';\n\n");
    resultado.append("var respuesta = new Array(");

    FieldProvider[] tdv = null;
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
          "C_BPartner_Location_ID", "", "C_BPartner Location - Ship To",
          Utility.getContext(this, vars, "#AccessibleOrgTree", strWindowId),
          Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, "");
      tdv = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    resultado.append("new Array(\"inpcBpartnerLocationId\", ");
    if (tdv != null && tdv.length > 0) {
      resultado.append("new Array(");

      if (strLocation.isEmpty()) {
        // If no location is provided, the first one is selected
        resultado.append("new Array(\"" + tdv[0].getField("id") + "\", \""
            + FormatUtilities.replaceJS(Replace.replace(tdv[0].getField("name"), "\"", "\\\""))
            + "\", \"" + "true" + "\")");
        if (tdv.length > 1) {
          resultado.append(",\n");
        }
        for (int i = 1; i < tdv.length; i++) {
          resultado.append("new Array(\"" + tdv[i].getField("id") + "\", \""
              + FormatUtilities.replaceJS(Replace.replace(tdv[i].getField("name"), "\"", "\\\""))
              + "\", \"" + "false" + "\")");
          if (i < tdv.length - 1) {
            resultado.append(",\n");
          }
        }
      } else {
        // If a location is provided, it is selected
        for (int i = 0; i < tdv.length; i++) {
          resultado.append("new Array(\"" + tdv[i].getField("id") + "\", \""
              + FormatUtilities.replaceJS(Replace.replace(tdv[i].getField("name"), "\"", "\\\""))
              + "\", \"" + (tdv[i].getField("id").equalsIgnoreCase(strLocation) ? "true" : "false")
              + "\")");
          if (i < tdv.length - 1) {
            resultado.append(",\n");
          }
        }
      }
      resultado.append("\n)");
    } else {
      // If not location search for nulllocation
      BpartnerMiscData[] dataloc = BpartnerMiscData.selectNullLocation(this, vars.getClient(),
          strBPartner);
      if (dataloc != null && dataloc.length != 0) {
        if (dataloc[0].cBpartnerLocationId != null) {
          resultado.append("new Array(");
          resultado.append("new Array(\"" + dataloc[0].cBpartnerLocationId + "\", \""
              + FormatUtilities.replaceJS(dataloc[0].locationname) + "\", \"" + "true" + "\")");
          resultado.append("\n)");

        } else {
          resultado.append("null");
        }
      } else {
        resultado.append("null");
      }
    }
    resultado.append("\n),");
    // resultado.append("new Array(\"inpsalesrepId\", ");
    // FieldProvider[] tld = null;
    // try {
    // ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "SalesRep_ID",
    // "AD_User SalesRep", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
    // "SEOrderBPartner"), Utility.getContext(this, vars, "#User_Client", "SEOrderBPartner"), 0);
    // Utility.fillSQLParameters(this, vars, null, comboTableData, "SEOrderBPartner", "");
    // tld = comboTableData.select(false);
    // comboTableData = null;
    // } catch (Exception ex) {
    // throw new ServletException(ex);
    // }

    // if (tld != null && tld.length > 0) {
    // resultado.append("new Array(");
    // for (int i = 0; i < tld.length; i++) {
    // resultado.append("new Array(\"" + tld[i].getField("id") + "\", \"" +
    // FormatUtilities.replaceJS(tld[i].getField("name")) + "\", \"" +
    // (tld[i].getField("id").equalsIgnoreCase(strUserRep) ? "true" : "false") + "\")");
    // if (i < tld.length - 1)
    // resultado.append(",\n");
    // }
    // resultado.append("\n)");
    // } else
    // resultado.append("null");
    // resultado.append("\n),");

    resultado.append("new Array(\"inpsalesrepId\", \"" + strUserRep + "\"),");

    resultado.append("new Array(\"inpcProjectId\", \"\"),");
    resultado.append("new Array(\"inpcProjectId_R\", \"\"),");
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_User_ID", "",
          "AD_User C_BPartner User/Contacts",
          Utility.getContext(this, vars, "#AccessibleOrgTree", strWindowId),
          Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, "");
      tdv = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    resultado.append("new Array(\"inpadUserId\", ");
    if (tdv != null && tdv.length > 0) {
      resultado.append("new Array(");

      if (strContact.isEmpty()) {
        resultado.append("new Array(\"" + tdv[0].getField("id") + "\", \""
            + FormatUtilities.replaceJS(Replace.replace(tdv[0].getField("name"), "\"", "\\\""))
            + "\", \"" + "true" + "\")");
        if (tdv.length > 1) {
          resultado.append(",\n");
        }
        for (int i = 1; i < tdv.length; i++) {
          resultado.append("new Array(\"" + tdv[i].getField("id") + "\", \""
              + FormatUtilities.replaceJS(Replace.replace(tdv[i].getField("name"), "\"", "\\\""))
              + "\", \"" + "false" + "\")");
          if (i < tdv.length - 1) {
            resultado.append(",\n");
          }
        }
      } else {
        for (int i = 0; i < tdv.length; i++) {
          resultado.append("new Array(\"" + tdv[i].getField("id") + "\", \""
              + FormatUtilities.replaceJS(Replace.replace(tdv[i].getField("name"), "\"", "\\\""))
              + "\", \"" + (tdv[i].getField("id").equalsIgnoreCase(strContact) ? "true" : "false")
              + "\")");
          if (i < tdv.length - 1) {
            resultado.append(",\n");
          }
        }
      }

      resultado.append("\n)");
    } else {
      resultado.append("null");
    }
    resultado.append("\n)");
    BusinessPartner bpartner = OBDal.getInstance().get(BusinessPartner.class, strBPartner);
    final String rtvendorship = "273673D2ED914C399A6C51DB758BE0F9";
    final String rMatReceipt = "123271B9AD60469BAE8A924841456B63";

    String strwindow = vars.getStringParameter("inpwindowId");
    String message = "";
    if ((!(strwindow.equals(rtvendorship) || strwindow.equals(rMatReceipt)))) {
      if ((!bpartner.equals(""))
          && FIN_Utility.isBlockedBusinessPartner(strBPartner, "Y".equals(strIsSOTrx), 2)) {
        // If the Business Partner is blocked for this document, show an information message.
        if (message.length() > 0) {
          message = message + "<br>";
        }

        String detail_msg = "";
        if (bpartner.getScrMsgBlocking() != null && !"".equals(bpartner.getScrMsgBlocking().trim()))
          detail_msg = bpartner.getScrMsgBlocking();
        else
          detail_msg = OBMessageUtils.messageBD("SSA_BusinessPartnerBlocked");
        message = message + OBMessageUtils.messageBD("ThebusinessPartner") + " "
            + bpartner.getIdentifier() + " " + detail_msg;
      }
    }

    final String goodsshipmentWindowId = "169";
    final String goodsreceiptWindowId = "184";
    if (strwindow.equals(goodsshipmentWindowId) || strwindow.equals(goodsreceiptWindowId)) {

      if (strMWarehouseId == null) {
        strMWarehouseId = "";
      }
      final String strSsaServiceOrderId = vars.getStringParameter("inpemSsaServiceorderId");
      if (strSsaServiceOrderId != null) {
        Order serviceorder = OBDal.getInstance().get(Order.class, strSsaServiceOrderId);
        if (serviceorder != null) {
          if (serviceorder.getDocumentStatus().equals("CO") && serviceorder.isSsaIsserviceorder()
              && serviceorder.getBusinessPartner().getId().equals(strBPartner)
              && serviceorder.getWarehouse().getId().equals(strMWarehouseId)) {
            // do nothing
          } else {
            resultado.append(",new Array(\"inpemSsaServiceorderId\", \"\")");
          }
        } else {
          resultado.append(",new Array(\"inpemSsaServiceorderId\", \"\")");
        }
      }
    }

    if (data != null && data.length > 0
        && new BigDecimal(data[0].creditavailable).compareTo(BigDecimal.ZERO) < 0
        && strIsSOTrx.equals("Y")) {
      String creditLimitExceed = "" + Double.parseDouble(data[0].creditavailable) * -1;
      if (message.length() > 0) {
        message = message + "<br>";
      }
      message = message + Utility.messageBD(this, "CreditLimitOver", vars.getLanguage())
          + creditLimitExceed;
    }
    resultado.append(", new Array('MESSAGE', \"" + message + "\")");

    resultado.append(");");
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }
}
