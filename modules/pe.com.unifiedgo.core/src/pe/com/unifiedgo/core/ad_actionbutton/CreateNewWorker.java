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
 * All portions are Copyright (C) 2001-2012 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package pe.com.unifiedgo.core.ad_actionbutton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_actionButton.ActionButtonDefaultData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.core.BPInfoActionHandler;
import pe.com.unifiedgo.core.data.SCRComboItem;

public class CreateNewWorker extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static final BigDecimal ZERO = BigDecimal.ZERO;
  static Logger log4j = Logger.getLogger(CreateNewWorker.class);

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strOrgId = vars.getGlobalVariable("inpOrgId", "CreateNewWorker|OrgId", "");
      String strIsNotCompany = vars.getGlobalVariable("inpIsNotCompany",
          "CreateNewWorker|IsNotCompany", "Y");
      String strComercialName = vars.getGlobalVariable("inpComercialName",
          "CreateNewWorker|ComercialName", "");
      String strNames = vars.getGlobalVariable("inpNames", "CreateNewCustomer|Names", "");
      String strFirstLastname = vars.getGlobalVariable("inpFirstLastname",
          "CreateNewWorker|FirstLastname", "");
      String strSecondLastname = vars.getGlobalVariable("inpSecondLastname",
          "CreateNewWorker|SecondLastname", "");
      String strBPDocTypeId = vars.getGlobalVariable("inpBPDocTypeId",
          "CreateNewWorker|BPDocTypeId", "");
      String strTaxID = ""; // vars.getGlobalVariable("inpTaxID", "CreateNewCustomer|TaxID", "");
      String strcLocationId = vars.getGlobalVariable("inpcLocationId",
          "CreateNewWorker|CLocation_ID", "");
      String strTelf = vars.getGlobalVariable("inpTelf", "CreateNewWorker|Telf", "");
      String strCel = vars.getGlobalVariable("inpCel", "CreateNewWorker|Cel", "");
      String strCorreo = vars.getGlobalVariable("inpCorreo", "CreateNewWorker|Correo", "");
      // String strUsername = vars.getGlobalVariable("inpUsername",
      // "CreateNewWorker|Username", "");

      printPage(response, vars, strOrgId, strIsNotCompany, strComercialName, strNames,
          strFirstLastname, strSecondLastname, strBPDocTypeId, strTaxID, strcLocationId, null,
          strTelf, strCel, strCorreo);

    } else if (vars.commandIn("SAVE")) {
      String strOrgId = vars.getStringParameter("inpOrgId", "");
      String strIsNotCompany = vars.getStringParameter("inpIsNotCompany");
      String strComercialName = vars.getStringParameter("inpComercialName", "");
      String strNames = vars.getStringParameter("inpNames", "");
      String strFirstLastname = vars.getStringParameter("inpFirstLastname", "");
      String strSecondLastname = vars.getStringParameter("inpSecondLastname", "");
      String strBPDocTypeId = vars.getStringParameter("inpBPDocTypeId", "");
      String strTaxID = vars.getStringParameter("inpTaxID", "");
      String strcLocationId = vars.getStringParameter("inpcLocationId");
      String strTelf = vars.getStringParameter("inpTelf", "");
      String strCel = vars.getStringParameter("inpCel", "");
      String strCorreo = vars.getStringParameter("inpCorreo", "");
      // String strUsername = vars.getStringParameter("inpUsername", "");

      OBError myMessage = processBusinessPartner(vars, strOrgId, strIsNotCompany, strComercialName,
          strNames, strFirstLastname, strSecondLastname, strBPDocTypeId, strTaxID, strcLocationId,
          null, strTelf, strCel, strCorreo);
      vars.setMessage("CreateNewWorker", myMessage);

      if ("Error".equals(myMessage.getType())) {
        printPage(response, vars, strOrgId, strIsNotCompany, strComercialName, strNames,
            strFirstLastname, strSecondLastname, strBPDocTypeId, strTaxID, strcLocationId, null,
            strTelf, strCel, strCorreo);
      } else {
        printPage(response, vars, "", strIsNotCompany, "", "", "", "", "", "", "", "", "", "", "");
      }

    } else if (vars.commandIn("GETBPINFO")) {
      BPInfoActionHandler bpinfohdlr = new BPInfoActionHandler();
      String strTaxID = vars.getGlobalVariable("taxid", "CreateNewWorker|TAXID", "");

      response.setCharacterEncoding("UTF-8");
      response.setContentType("application/json");
      PrintWriter out = response.getWriter();
      out.print(bpinfohdlr.getbpinfo(strTaxID).toString());
      out.close();

    } else
      pageErrorPopUp(response);
  }

  private OBError processBusinessPartner(VariablesSecureApp vars, String strOrgId,
      String strIsNotCompany, String strComercialName, String strNames, String strFirstLastname,
      String strSecondLastname, String strBPDocTypeId, String strTaxID, String strcLocationId,
      String strUsername, String strTelf, String strCel, String strCorreo) {
    if (log4j.isDebugEnabled())
      log4j.debug("Save: Business Partner");

    String isCompany = "", comercialName = "", names = null, lastname1 = null, lastname2 = null;
    // String username = "";
    String bpTemplateId;
    Organization org = OBDal.getInstance().get(Organization.class, strOrgId);

    String TPL_NAME = "%TPL-BPSalesRepresentant";

    OBError myMessage = null;
    myMessage = new OBError();
    myMessage.setTitle("");

    if ("Y".equals(strIsNotCompany)) {
      isCompany = "N";
      comercialName = strNames + " " + strFirstLastname + " " + strSecondLastname;
      names = strNames;
      lastname1 = strFirstLastname;
      lastname2 = strSecondLastname;
      // username = strUsername;

    } else {
      isCompany = "Y";
      comercialName = strComercialName;
      // username = strUsername;
    }

    Connection conn = null;
    try {
      conn = new DalConnectionProvider(false).getTransactionConnection();
      String strBPartnerID = SequenceIdData.getUUID();

      bpTemplateId = CreateNewWorkerData.selectBPTemplateId(this,
          Utility.getContext(this, vars, "#User_Client", null),
          Utility.getContext(this, vars, "#User_Org", null), TPL_NAME, strOrgId);
      if (bpTemplateId == null || "".equals(bpTemplateId))
        throw new Exception("SCR_NotTPLBP_SalesRepresentant");

      // Catch database error message
      try {
        CreateNewWorkerData.insertBPbasedOnTemplate(conn, this, strBPartnerID, vars.getClient(),
            strOrgId, vars.getUser(), comercialName, strTaxID, strBPDocTypeId, isCompany, names,
            lastname1, lastname2, bpTemplateId);

        if (strcLocationId != null && !"".equals(strcLocationId)) {
          CreateNewWorkerData.insertBPLocation(conn, this, vars.getClient(), strOrgId,
              vars.getUser(), strBPartnerID, strcLocationId);
        }

        if ((strTelf != null && !"".equals(strTelf)) || (strCel != null && !"".equals(strCel))
            || (strCorreo != null && !"".equals(strCorreo))) {
          CreateNewWorkerData.insertContactInformation(conn, this, vars.getClient(), strOrgId,
              vars.getUser(), strCorreo, strTelf, strCel, strBPartnerID);
        }

        // CreateNewWorkerData.insertUser(conn, this, vars.getClient(), strOrgId,
        // vars.getUser(), comercialName, strBPartnerID, username);

      } catch (ServletException ex) {
        System.out.println(" ex.getMessage():" + ex.getMessage());
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
        releaseRollbackConnection(conn);
        return myMessage;
      }

      releaseCommitConnection(conn);

      myMessage.setType("Success");
      myMessage.setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));
      myMessage.setMessage(Utility.messageBD(this,
          "SCR_BPSalesRepresentativeCreatorProcessCompleted", vars.getLanguage()));
      return myMessage;

    } catch (Exception e) {
      System.out.println("acaa error:" + e.getMessage());
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      myMessage.setType("Error");
      myMessage.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
      if (e.getMessage().equals("SCR_NotTPLBP_SalesRepresentant")) {
        myMessage.setMessage(
            Utility.messageBD(this, "SCR_NotTPLBP_SalesRepresentant", vars.getLanguage()));
      } else {
        myMessage.setMessage(Utility.messageBD(this, "ProcessRunError", vars.getLanguage()));
      }
      return myMessage;
    }

  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strOrgId,
      String strIsNotCompany, String strComercialName, String strNames, String strFirstLastname,
      String strSecondLastname, String strBPDocTypeId, String strTaxID, String strcLocationId,
      String strUsername, String strTelf, String strCel, String strCorreo)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: process CreateNewWorker");

    ActionButtonDefaultData[] data = null;
    String strHelp = "", strDescription = "", strProcessId = "67ADBA9482224C1C93C4D15EFB1B0E80";
    String[] discard = { "" };
    Organization org = null;
    SCRComboItem cmb_bpdoctype = null;

    if (vars.getLanguage().equals("en_US"))
      data = ActionButtonDefaultData.select(this, strProcessId);
    else
      data = ActionButtonDefaultData.selectLanguage(this, vars.getLanguage(), strProcessId);
    if (data != null && data.length != 0) {
      strDescription = data[0].description;
      strHelp = data[0].help;
    }
    if (strHelp.equals(""))
      discard[0] = new String("helpDiscard");

    XmlDocument xmlDocument = xmlEngine
        .readXmlTemplate("pe/com/unifiedgo/core/ad_actionbutton/CreateNewWorker")
        .createXmlDocument();

    // ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "CreateNewWorker",
    // false,
    // "", "", "", false, "ad_process", strReplaceWith, false, true);
    // toolbar.prepareSimpleToolBarTemplate();
    // xmlDocument.setParameter("toolbar", toolbar.toString());

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("description", strDescription);
    xmlDocument.setParameter("help", strHelp);

    if (strOrgId != null && !strOrgId.isEmpty()) {
      org = OBDal.getInstance().get(Organization.class, strOrgId);
    } else { // default
      String def_orgid = CreateNewWorkerData.selectLegalEntityNoZeroOrg(this,
          Utility.getContext(this, vars, "#User_Client", null));
      org = OBDal.getInstance().get(Organization.class, def_orgid);
    }
    xmlDocument.setParameter("OrgId", (org != null) ? org.getId() : "");
    xmlDocument.setParameter("OrgDescription", (org != null) ? org.getIdentifier() : "");

    xmlDocument.setParameter("isNotCompany", strIsNotCompany);

    xmlDocument.setParameter("ComercialName", (strComercialName != null) ? strComercialName : "");
    xmlDocument.setParameter("Names", (strNames != null) ? strNames : "");
    xmlDocument.setParameter("FirstLastname", (strFirstLastname != null) ? strFirstLastname : "");
    xmlDocument.setParameter("SecondLastname",
        (strSecondLastname != null) ? strSecondLastname : "");

    if (strBPDocTypeId != null && !strBPDocTypeId.isEmpty()) {
      cmb_bpdoctype = OBDal.getInstance().get(SCRComboItem.class, strBPDocTypeId);
    } else { // default
      String def_bpdoctype = CreateNewWorkerData.selectDocumentType(this);
      cmb_bpdoctype = OBDal.getInstance().get(SCRComboItem.class, def_bpdoctype);
    }
    xmlDocument.setParameter("BPDocTypeId", (cmb_bpdoctype != null) ? cmb_bpdoctype.getId() : "");
    xmlDocument.setParameter("BPDocTypeDescription",
        (cmb_bpdoctype != null) ? cmb_bpdoctype.getName() : "");

    xmlDocument.setParameter("TaxID", (strTaxID != null) ? strTaxID : "");

    String cLocationDescription = "";
    if (strcLocationId != null && !"".equals(strcLocationId)) {
      cLocationDescription = CreateNewWorkerData.selectLocationDescription(this, strcLocationId);
    }
    xmlDocument.setParameter("paramLocationId", strcLocationId);
    xmlDocument.setParameter("paramLocationDescription", cLocationDescription);

    xmlDocument.setParameter("Telf", (strTelf != null) ? strTelf : "");
    xmlDocument.setParameter("Cel", (strCel != null) ? strCel : "");
    xmlDocument.setParameter("Correo", (strCorreo != null) ? strCorreo : "");

    // xmlDocument.setParameter("Username", (strUsername != null) ? strUsername : "");

    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateInvdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateInvsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    // New interface parameters
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.core.ad_actionbutton.CreateNewWorker");

      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "CreateNewWorker.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "CreateNewWorker.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("CreateNewWorker");
      vars.removeMessage("CreateNewWorker");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet CreateNewWorker";
  } // end of getServletInfo() method
}
