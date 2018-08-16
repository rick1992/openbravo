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
package pe.com.unifiedgo.core.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.xmlEngine.XmlDocument;

public class ReportBPCreditInfoJR extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strOrgId = vars.getGlobalVariable("inpOrgId", "ReportBPCreditInfoJR|OrgId", "");
      String strcBpartnetId = vars.getGlobalVariable("inpcBPartnerId",
          "ReportBPCreditInfoJR|CB_PARTNER_ID", "");
      
      printPageDataSheet(request, response, vars, strOrgId, strcBpartnetId);

    } else if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      String strOrgId = vars.getStringParameter("inpOrgId");
      String strcBpartnetId = vars.getStringParameter("inpcBPartnerId");

      printPageDataSheet(request, response, vars, strOrgId, strcBpartnetId);

    } else
      pageError(response);
  }

  private ReportBPCreditInfoJRData[] getDataWithTotals(ReportBPCreditInfoJRData[] data) {
    if (data.length == 0)
      return data;

    DecimalFormat df = new DecimalFormat("0.00");
    df.setRoundingMode(RoundingMode.HALF_UP);

    double pentotalcreditlimit = 0, pentotalcreditused = 0, pentotalcreditfree = 0, usdtotalcreditlimit = 0, usdtotalcreditused = 0, usdtotalcreditfree = 0;
    List<ReportBPCreditInfoJRData> dataList = new ArrayList<ReportBPCreditInfoJRData>();
    String lasttaxid = "";
    for (int i = 0; i < data.length; i++) {
      if (lasttaxid.compareTo(data[i].bptaxid) == 0 || lasttaxid == "") {
        dataList.add(data[i]);

        if (lasttaxid == "") {
          lasttaxid = data[i].bptaxid;
        }

      } else {
        ReportBPCreditInfoJRData objRptBPCredInfoDt = new ReportBPCreditInfoJRData();
        objRptBPCredInfoDt.orgname = "<b> TOTAL GENERAL </b>";
        objRptBPCredInfoDt.pencreditlimit = "<b> " + df.format(pentotalcreditlimit) + " </b>";
        objRptBPCredInfoDt.pencreditused = "<b> " + df.format(pentotalcreditused) + " </b>";
        objRptBPCredInfoDt.pencreditfree = "<b> " + df.format(pentotalcreditfree) + " </b>";
        objRptBPCredInfoDt.usdcreditlimit = "<b> " + df.format(usdtotalcreditlimit) + " </b>";
        objRptBPCredInfoDt.usdcreditused = "<b> " + df.format(usdtotalcreditused) + " </b>";
        objRptBPCredInfoDt.usdcreditfree = "<b> " + df.format(usdtotalcreditfree) + " </b>";
        objRptBPCredInfoDt.bptaxid = data[i - 1].bptaxid;
        objRptBPCredInfoDt.bpid = data[i - 1].bpid;
        objRptBPCredInfoDt.bpname = data[i - 1].bpname;

        dataList.add(objRptBPCredInfoDt);

        dataList.add(data[i]);

        pentotalcreditlimit = 0.0;
        pentotalcreditused = 0.0;
        pentotalcreditfree = 0.0;
        usdtotalcreditlimit = 0.0;
        usdtotalcreditused = 0.0;
        usdtotalcreditfree = 0.0;

        lasttaxid = data[i].bptaxid;
      }

      pentotalcreditlimit += Double.parseDouble(data[i].pencreditlimit);
      pentotalcreditused += Double.parseDouble(data[i].pencreditused);
      pentotalcreditfree += Double.parseDouble(data[i].pencreditfree);
      usdtotalcreditlimit += Double.parseDouble(data[i].usdcreditlimit);
      usdtotalcreditused += Double.parseDouble(data[i].usdcreditused);
      usdtotalcreditfree += Double.parseDouble(data[i].usdcreditfree);
    }

    ReportBPCreditInfoJRData objRptBPCredInfoDt = new ReportBPCreditInfoJRData();
    objRptBPCredInfoDt.orgname = "<b> TOTAL GENERAL </b>";
    objRptBPCredInfoDt.pencreditlimit = "<b> " + df.format(pentotalcreditlimit) + " </b>";
    objRptBPCredInfoDt.pencreditused = "<b> " + df.format(pentotalcreditused) + " </b>";
    objRptBPCredInfoDt.pencreditfree = "<b> " + df.format(pentotalcreditfree) + " </b>";
    objRptBPCredInfoDt.usdcreditlimit = "<b> " + df.format(usdtotalcreditlimit) + " </b>";
    objRptBPCredInfoDt.usdcreditused = "<b> " + df.format(usdtotalcreditused) + " </b>";
    objRptBPCredInfoDt.usdcreditfree = "<b> " + df.format(usdtotalcreditfree) + " </b>";
    objRptBPCredInfoDt.bptaxid = data[data.length - 1].bptaxid;
    objRptBPCredInfoDt.bpid = data[data.length - 1].bpid;
    objRptBPCredInfoDt.bpname = data[data.length - 1].bpname;
    dataList.add(objRptBPCredInfoDt);

    ReportBPCreditInfoJRData[] finalData = dataList.toArray(new ReportBPCreditInfoJRData[dataList
        .size()]);
    return finalData;
  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strOrgId, String strcBpartnetId)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    ReportBPCreditInfoJRData[] data = null;
    String strConvRateErrorMsg = "";
    BusinessPartner bpartner = null;

    String discard[] = { "discard" };

    xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/core/ad_reports/ReportBPCreditInfoFilterJR", discard).createXmlDocument();

    if (vars.commandIn("EDIT_HTML", "EDIT_PDF")) {
      OBError myMessage = null;
      myMessage = new OBError();

      bpartner = OBDal.getInstance().get(BusinessPartner.class, strcBpartnetId);
      if (bpartner == null) {
        throw new ServletException("BPartnerNotFound");
      }

      try {
        // Getting bp credit in different org's by taxid
        data = ReportBPCreditInfoJRData.select(this, Utility.getContext(this, vars, "#User_Client",
            null), Utility.getContext(this, vars, "#User_Org", null), bpartner.getTaxID(), strOrgId);
        data = getDataWithTotals(data);

        System.out.println("#User_Client" + Utility.getContext(this, vars, "#User_Client", null));
        System.out.println("#User_Org" + Utility.getContext(this, vars, "#User_Org", null));
        System.out.println("strOrgId:" + strOrgId);


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
          data = ReportBPCreditInfoJRData.set();
        } else {
          xmlDocument.setData("structure1", data);
        }
      }
    }

    else {
      if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
        discard[0] = "selEliminar";
        data = ReportBPCreditInfoJRData.set();
      }
    }

    if (strConvRateErrorMsg.equals("") || strConvRateErrorMsg == null) {
      ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "ReportBPCreditInfoFilterJR", false,
          "", "", "", false, "ad_reports", strReplaceWith, false, true);
      toolbar.prepareSimpleToolBarTemplate();
      xmlDocument.setParameter("toolbar", toolbar.toString());

      try {
        WindowTabs tabs = new WindowTabs(this, vars,
            "pe.com.unifiedgo.core.ad_reports.ReportBPCreditInfoJR");
        xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
        xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
        xmlDocument.setParameter("childTabContainer", tabs.childTabs());
        xmlDocument.setParameter("theme", vars.getTheme());
        NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
            "ReportBPCreditInfoFilterJR.html", classInfo.id, classInfo.type, strReplaceWith,
            tabs.breadcrumb());
        xmlDocument.setParameter("navigationBar", nav.toString());
        LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(),
            "ReportBPCreditInfoFilterJR.html", strReplaceWith);
        xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
      } catch (Exception ex) {
        throw new ServletException(ex);
      }
      {
        OBError myMessage = vars.getMessage("ReportBPCreditInfoJR");
        vars.removeMessage("ReportBPCreditInfoJR");
        if (myMessage != null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

      Organization org;
      org = OBDal.getInstance().get(Organization.class, strOrgId);
      xmlDocument.setParameter("OrgId", strOrgId);
      xmlDocument.setParameter("OrgDescription", (org != null) ? org.getIdentifier() : "");

      xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("paramBPartnerId", strcBpartnetId);

      xmlDocument.setParameter("paramBPartnerDescription",
          ReportBPCreditInfoJRData.selectBpartner(this, strcBpartnetId));

      // Print document in the output
      out.println(xmlDocument.print());
      out.close();
    }
  }

  public String getServletInfo() {
    return "Servlet PurchaseOrderFilter. This Servlet was made by Jon Alegría";
  } // end of getServletInfo() method

}
