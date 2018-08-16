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
 * All portions are Copyright (C) 2001-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package pe.com.unifiedgo.accounting.ad_actionbutton;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.financialmgmt.accounting.AccountingFact;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.SCO_Utils;

public class RedirectSourceDocument extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {

      String factAcctId = vars.getStringParameter("inpfactAcctId");

      String sco_doc_for_regple_v_id = vars.getStringParameter("inpscoDocForRegpleVId");

      String strAD_Org_ID = vars.getGlobalVariable("inpadOrgId", "RedirectSourceDocument|AD_Org_ID",
          "");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "RedirectSourceDocument|DateFrom",
          "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "RedirectSourceDocument|DateTo", "");

      printPageDataSheet(request, response, vars, strDateFrom, strDateTo, strAD_Org_ID, factAcctId,
          sco_doc_for_regple_v_id);

    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strAD_Org_ID,
      String strFactAcctId, String sco_doc_for_regple_v_id) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    String discard[] = { "sectionDocType" };
    XmlDocument xmlDocument = null;
    RedirectSourceDocumentData[] data = null;

    xmlDocument = xmlEngine
        .readXmlTemplate("pe/com/unifiedgo/accounting/ad_actionbutton/RedirectSourceDocument")
        .createXmlDocument();

    // ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "RedirectSourceDocument", false, "",
    // "",
    // "", false, "ad_reports", strReplaceWith, false, true);
    // toolbar.prepareSimpleToolBarTemplate();
    //
    // xmlDocument.setParameter("toolbar", toolbar.toString());

    // try {
    // WindowTabs tabs = new WindowTabs(this, vars,
    // "org.openbravo.erpCommon.ad_reports.RedirectSourceDocument");
    // xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
    // xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
    // xmlDocument.setParameter("childTabContainer", tabs.childTabs());
    // xmlDocument.setParameter("theme", vars.getTheme());
    // NavigationBar nav = new NavigationBar(this, vars.getLanguage(),
    // "RedirectSourceDocument.html",
    // classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
    // xmlDocument.setParameter("navigationBar", nav.toString());
    // LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "RedirectSourceDocument.html",
    // strReplaceWith);
    // xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    // } catch (Exception ex) {
    // throw new ServletException(ex);
    // }
    {
      OBError myMessage = vars.getMessage("RedirectSourceDocument");
      vars.removeMessage("RedirectSourceDocument");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    if (vars.commandIn("FIND") && data.length == 0) {
      // No data has been found. Show warning message.
      xmlDocument.setParameter("messageType", "WARNING");
      xmlDocument.setParameter("messageTitle",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()));
      xmlDocument.setParameter("messageMessage",
          Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    xmlDocument.setParameter("adOrg", strAD_Org_ID);

    // try {
    // ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
    // "0C754881EAD94243A161111916E9B9C6",
    // Utility.getContext(this, vars, "#AccessibleOrgTree", "NationalPurchasePlanningJR"),
    // Utility.getContext(this, vars, "#User_Client", "NationalPurchasePlanningJR"), 0);
    // Utility.fillSQLParameters(this, vars, null, comboTableData,
    // "NationalPurchasePlanningFilterJR", strAD_Org_ID);
    // xmlDocument.setData("reportAD_Org_ID", "liststructure", comboTableData.select(false));
    // comboTableData = null;
    // } catch (Exception ex) {
    // throw new ServletException(ex);
    // }

    AccountingFact factAcct = OBDal.getInstance().get(AccountingFact.class, strFactAcctId);
    if (factAcct == null) {
      factAcct = OBDal.getInstance().get(AccountingFact.class, sco_doc_for_regple_v_id);
    }
    String ad_table_id = "";
    String record_id = "";
    if (factAcct.isScoIsmigrated()) {
      ad_table_id = factAcct.getScoRecord3Table() != null ? factAcct.getScoRecord3Table().getId()
          : null;
      record_id = factAcct.getScoRecord3();
    } else {
      ad_table_id = factAcct.getTable() != null ? factAcct.getTable().getId() : null;
      record_id = factAcct.getRecordID();
    }

    String tabId = ad_table_id != null ? SCO_Utils.getTabId(this, ad_table_id, record_id, false)
        : null;
    if (tabId == null) {
      advisePopUp(request, response, "WARNING",
          Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
          "LA LINEA DE ASIENTO NO TIENE UN DOCUMENTO ASOCIADO, POSIBLEMENTE SEA UNA ASIENTO DE APERTURA O MIGRACIÓN");
      return;
    }

    xmlDocument.setParameter("RecordId", "RecordId = \"" + record_id + "\"; ");
    xmlDocument.setParameter("TabId", "TabId = \"" + tabId + "\"; ");

    // xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet RedirectSourceDocument. This Servlet was made by Juan Pablo Calvente";
  } // end of the getServletInfo() method
}
