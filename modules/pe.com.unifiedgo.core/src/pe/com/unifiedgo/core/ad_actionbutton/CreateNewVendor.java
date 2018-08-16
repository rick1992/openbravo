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
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.core.BPInfoActionHandler;
import pe.com.unifiedgo.core.data.SCRComboItem;

public class CreateNewVendor extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static final BigDecimal ZERO = BigDecimal.ZERO;
  static Logger log4j = Logger.getLogger(CreateNewVendor.class);

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strOrgId = vars.getGlobalVariable("inpOrgId", "CreateNewVendor|OrgId", "");
      String strIsNotCompany = vars.getGlobalVariable("inpIsNotCompany",
          "CreateNewVendor|IsNotCompany", "");
      String strComercialName = vars.getGlobalVariable("inpComercialName",
          "CreateNewVendor|ComercialName", "");
      String strNames = vars.getGlobalVariable("inpNames", "CreateNewVendor|Names", "");
      String strFirstLastname = vars.getGlobalVariable("inpFirstLastname",
          "CreateNewVendor|FirstLastname", "");
      String strSecondLastname = vars.getGlobalVariable("inpSecondLastname",
          "CreateNewVendor|SecondLastname", "");
      String strBPDocTypeId = vars.getGlobalVariable("inpBPDocTypeId",
          "CreateNewVendor|BPDocTypeId", "");
      String strTaxID = ""; // vars.getGlobalVariable("inpTaxID", "CreateNewVendor|TaxID", "");
      String strcLocationId = vars.getGlobalVariable("inpcLocationId",
          "CreateNewVendor|CLocation_ID", "");
      String strTelf = vars.getGlobalVariable("inpTelf", "CreateNewVendor|Telf", "");
      String strCel = vars.getGlobalVariable("inpCel", "CreateNewVendor|Cel", "");
      String strCorreo = vars.getGlobalVariable("inpCorreo", "CreateNewVendor|Correo", "");

      printPage(response, vars, strOrgId, strIsNotCompany, strComercialName, strNames,
          strFirstLastname, strSecondLastname, strBPDocTypeId, strTaxID, strcLocationId, strTelf,
          strCel, strCorreo);

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

      OBError myMessage = processBusinessPartner(vars, strOrgId, strIsNotCompany, strComercialName,
          strNames, strFirstLastname, strSecondLastname, strBPDocTypeId, strTaxID, strcLocationId,
          strTelf, strCel, strCorreo);
      vars.setMessage("CreateNewVendor", myMessage);

      if ("Error".equals(myMessage.getType())) {
        printPage(response, vars, strOrgId, strIsNotCompany, strComercialName, strNames,
            strFirstLastname, strSecondLastname, strBPDocTypeId, strTaxID, strcLocationId, strTelf,
            strCel, strCorreo);
      } else {
        printPage(response, vars, "", "", "", "", "", "", "", "", "", "", "", "");
      }

    } else if (vars.commandIn("GETBPINFO")) {
      BPInfoActionHandler bpinfohdlr = new BPInfoActionHandler();
      String strTaxID = vars.getGlobalVariable("taxid", "CreateNewVendor|TAXID", "");
      String strIsNotCompany = vars.getGlobalVariable("isnotcompany", "");

      response.setCharacterEncoding("UTF-8");
      response.setContentType("application/json");
      PrintWriter out = response.getWriter();
      out.print(bpinfohdlr.getbpinfo(strTaxID, strIsNotCompany).toString());
      out.close();

    } else
      pageErrorPopUp(response);
  }

  private OBError processBusinessPartner(VariablesSecureApp vars, String strOrgId,
      String strIsNotCompany, String strComercialName, String strNames, String strFirstLastname,
      String strSecondLastname, String strBPDocTypeId, String strTaxID, String strcLocationId,
      String strTelf, String strCel, String strCorreo) {
    if (log4j.isDebugEnabled())
      log4j.debug("Save: Business Partner");

    String isCompany = "", comercialName = "", names = null, lastname1 = null, lastname2 = null;
    String bpTemplateId;
    String success_msg = "";

    String TPL_NAME = "%TPL-BPVendor";

    OBError myMessage = null;
    myMessage = new OBError();
    myMessage.setTitle("");

    if ("Y".equals(strIsNotCompany)) {
      isCompany = "N";
      comercialName = strNames + " " + strFirstLastname + " " + strSecondLastname;
      names = strNames;
      lastname1 = strFirstLastname;
      lastname2 = strSecondLastname;

    } else {
      isCompany = "Y";
      comercialName = strComercialName;
    }

    Connection conn = null;
    try {
      conn = new DalConnectionProvider(false).getTransactionConnection();
      String strBPartnerID = SequenceIdData.getUUID();

      bpTemplateId = CreateNewVendorData.selectBPTemplateId_NoOrgValAccess(this,
          Utility.getContext(this, vars, "#User_Client", null),
          // Utility.getContext(this, vars, "#User_Org", null),
          TPL_NAME, strOrgId);
      if (bpTemplateId == null || "".equals(bpTemplateId))
        throw new Exception("SCR_NotTPLBP_Vendor");

      CreateNewVendorData.insertBPbasedOnTemplate(conn, this, strBPartnerID, vars.getClient(),
          strOrgId, vars.getUser(), comercialName, strTaxID, strBPDocTypeId, isCompany, names,
          lastname1, lastname2, bpTemplateId);

      if (strcLocationId != null && !"".equals(strcLocationId)) {
        CreateNewVendorData.insertBPLocation(conn, this, vars.getClient(), strOrgId, vars.getUser(),
            strBPartnerID, strcLocationId);
      }

      if ((strTelf != null && !"".equals(strTelf)) || (strCel != null && !"".equals(strCel))
          || (strCorreo != null && !"".equals(strCorreo))) {
        CreateNewVendorData.insertContactInformation(conn, this, vars.getClient(), strOrgId,
            vars.getUser(), strCorreo, strTelf, strCel, strBPartnerID);
      }

      releaseCommitConnection(conn);

      if ("Y".equals(isCompany))
        success_msg = "SCR_BPVendorCreatorProcessCompleted";
      else
        success_msg = "SCR_BPVendorCreatorProcessCompleted2";

      myMessage.setType("Success");
      myMessage.setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));
      myMessage.setMessage(Utility.messageBD(this, success_msg, vars.getLanguage()));
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
      if (e.getMessage().equals("SCR_NotTPLBP_Customer")) {
        myMessage.setMessage(Utility.messageBD(this, "SCR_NotTPLBP_Vendor", vars.getLanguage()));
      } else {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), e.getMessage());
      }
      return myMessage;
    }

  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strOrgId,
      String strIsNotCompany, String strComercialName, String strNames, String strFirstLastname,
      String strSecondLastname, String strBPDocTypeId, String strTaxID, String strcLocationId,
      String strTelf, String strCel, String strCorreo) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: process CreateNewVendor");

    ActionButtonDefaultData[] data = null;
    String strHelp = "", strDescription = "", strProcessId = "727ACCCC80D640B99C20D1C399F1CBB0";
    String[] discard = { "" };
    Organization org = null;
    User usr = null;
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
        .readXmlTemplate("pe/com/unifiedgo/core/ad_actionbutton/CreateNewVendor")
        .createXmlDocument();

    // ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "CreateNewVendor", false, "", "", "",
    // false, "ad_process", strReplaceWith, false, true);
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
      String def_orgid = CreateNewVendorData.selectLegalEntityNoZeroOrg(this,
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
      String def_bpdoctype = CreateNewVendorData.selectDocumentType(this);
      cmb_bpdoctype = OBDal.getInstance().get(SCRComboItem.class, def_bpdoctype);
    }
    xmlDocument.setParameter("BPDocTypeId", (cmb_bpdoctype != null) ? cmb_bpdoctype.getId() : "");
    xmlDocument.setParameter("BPDocTypeDescription",
        (cmb_bpdoctype != null) ? cmb_bpdoctype.getName() : "");

    xmlDocument.setParameter("TaxID", (strTaxID != null) ? strTaxID : "");

    String cLocationDescription = "";
    if (strcLocationId != null && !"".equals(strcLocationId)) {
      cLocationDescription = CreateNewVendorData.selectLocationDescription(this, strcLocationId);
    }
    xmlDocument.setParameter("paramLocationId", strcLocationId);
    xmlDocument.setParameter("paramLocationDescription", cLocationDescription);

    xmlDocument.setParameter("Telf", (strTelf != null) ? strTelf : "");
    xmlDocument.setParameter("Cel", (strCel != null) ? strCel : "");
    xmlDocument.setParameter("Correo", (strCorreo != null) ? strCorreo : "");

    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateInvdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateInvsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    // New interface parameters
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.core.ad_actionbutton.CreateNewVendor");

      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "CreateNewVendor.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "CreateNewVendor.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("CreateNewVendor");
      vars.removeMessage("CreateNewVendor");
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
    return "Servlet CreateNewVendor";
  } // end of getServletInfo() method
}
