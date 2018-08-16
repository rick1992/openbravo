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
import org.openbravo.erpCommon.utility.ComboTableData;
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

public class CreateNewCustomer extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static final BigDecimal ZERO = BigDecimal.ZERO;
  static Logger log4j = Logger.getLogger(CreateNewCustomer.class);

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strOrgId = vars.getGlobalVariable("inpOrgId", "CreateNewCustomer|OrgId", "");
      String strIsNotCompany = vars.getGlobalVariable("inpIsNotCompany",
          "CreateNewCustomer|IsNotCompany", "Y");
      String strComercialName = vars.getGlobalVariable("inpComercialName",
          "CreateNewCustomer|ComercialName", "");
      String strNames = vars.getGlobalVariable("inpNames", "CreateNewCustomer|Names", "");
      String strFirstLastname = vars.getGlobalVariable("inpFirstLastname",
          "CreateNewCustomer|FirstLastname", "");
      String strSecondLastname = vars.getGlobalVariable("inpSecondLastname",
          "CreateNewCustomer|SecondLastname", "");
      String strBPDocTypeId = vars.getGlobalVariable("inpBPDocTypeId",
          "CreateNewCustomer|BPDocTypeId", "");
      String strTaxID = ""; // vars.getGlobalVariable("inpTaxID", "CreateNewCustomer|TaxID", "");
      String strcLocationId = vars.getGlobalVariable("inpcLocationId",
          "CreateNewCustomer|CLocation_ID", "");
      String strTelf = vars.getGlobalVariable("inpTelf", "CreateNewCustomer|Telf", "");
      String strCel = vars.getGlobalVariable("inpCel", "CreateNewCustomer|Cel", "");
      String strCorreo = vars.getGlobalVariable("inpCorreo", "CreateNewCustomer|Correo", "");
      String strSalesRepId = vars.getGlobalVariable("inpSalesRepId",
          "CreateNewCustomer|SalesRep_ID", "");

      // Informacion del Conyuge
      String strCivilState = vars.getGlobalVariable("inpCivilState", "CreateNewCustomer|CivilState",
          "");
      String strBPDocTypeConyugeId = vars.getGlobalVariable("inpBPDocTypeConyugeId",
          "CreateNewCustomer|BPDocTypeConyugeId", "");
      String strTaxConyugeID = "";
      String strIsNotCompanyConyuge = vars.getGlobalVariable("inpIsNotCompanyConyuge",
          "CreateNewCustomer|IsNotCompanyConyuge", "Y");
      String strComercialNameConyuge = vars.getGlobalVariable("inpComercialNameConyuge",
          "CreateNewCustomer|ComercialNameConyuge", "");
      String strNamesConyuge = vars.getGlobalVariable("inpNamesConyuge",
          "CreateNewCustomer|NamesConyuge", "");
      String strFirstLastnameConyuge = vars.getGlobalVariable("inpFirstLastnameConyuge",
          "CreateNewCustomer|FirstLastnameConyuge", "");
      String strSecondLastnameConyuge = vars.getGlobalVariable("inpSecondLastnameConyuge",
          "CreateNewCustomer|SecondLastnameConyuge", "");
      String strTelfConyuge = vars.getGlobalVariable("inpTelfConyuge",
          "CreateNewCustomer|TelfConyuge", "");
      String strCelConyuge = vars.getGlobalVariable("inpCelConyuge", "CreateNewCustomer|CelConyuge",
          "");
      String strCorreoConyuge = vars.getGlobalVariable("inpCorreoConyuge",
          "CreateNewCustomer|CorreoConyuge", "");
      String strcLocationConyugeId = vars.getGlobalVariable("inpcLocationConyugeId",
          "CreateNewCustomer|CLocationConyuge_ID", "");

      printPage(response, vars, strOrgId, strIsNotCompany, strComercialName, strNames,
          strFirstLastname, strSecondLastname, strBPDocTypeId, strTaxID, strcLocationId,
          strSalesRepId, strTelf, strCel, strCorreo, strCivilState, strBPDocTypeConyugeId,
          strTaxConyugeID, strIsNotCompanyConyuge, strComercialNameConyuge, strNamesConyuge,
          strFirstLastnameConyuge, strSecondLastnameConyuge, strTelfConyuge, strCelConyuge,
          strCorreoConyuge, strcLocationConyugeId);

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
      String strSalesRepId = vars.getStringParameter("inpSalesRepId", "");

      String strCivilState = vars.getStringParameter("inpCivilState", "");
      String strBPDocTypeConyugeId = vars.getStringParameter("inpBPDocTypeConyugeId", "");
      String strTaxConyugeID = vars.getStringParameter("inpTaxConyugeID", "");
      String strIsNotCompanyConyuge = vars.getStringParameter("inpIsNotCompanyConyuge");
      String strComercialNameConyuge = vars.getStringParameter("inpComercialNameConyuge", "");
      String strNamesConyuge = vars.getStringParameter("inpNamesConyuge", "");
      String strFirstLastnameConyuge = vars.getStringParameter("inpFirstLastnameConyuge", "");
      String strSecondLastnameConyuge = vars.getStringParameter("inpSecondLastnameConyuge", "");
      String strTelfConyuge = vars.getStringParameter("inpTelfConyuge", "");
      String strCelConyuge = vars.getStringParameter("inpCelConyuge", "");
      String strCorreoConyuge = vars.getStringParameter("inpCorreoConyuge", "");
      String strcLocationConyugeId = vars.getStringParameter("inpcLocationConyugeId");

      OBError myMessage = new OBError();
      try {
        // Tercero
        String cBPartnerId = processBusinessPartner(vars, strOrgId, strIsNotCompany,
            strComercialName, strNames, strFirstLastname, strSecondLastname, strBPDocTypeId,
            strTaxID, strcLocationId, strSalesRepId, strTelf, strCel, strCorreo);

        // Conyuge
        if (strTaxConyugeID != null && !"".equals(strTaxConyugeID.trim())) {
          String strRelatedCBPartnerId = processBusinessPartner(vars, strOrgId,
              strIsNotCompanyConyuge, strComercialNameConyuge, strNamesConyuge,
              strFirstLastnameConyuge, strSecondLastnameConyuge, strBPDocTypeConyugeId,
              strTaxConyugeID, strcLocationConyugeId, strSalesRepId, strTelfConyuge, strCelConyuge,
              strCorreoConyuge);

          processRelationShip(vars, strOrgId, cBPartnerId, strRelatedCBPartnerId);
        }

        myMessage.setType("Success");
        myMessage.setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));
        if ("Y".equals(strIsNotCompany))
          myMessage.setMessage(Utility.messageBD(this, "SCR_BPCustomerCreatorProcessCompleted2",
              vars.getLanguage()));
        else
          myMessage.setMessage(
              Utility.messageBD(this, "SCR_BPCustomerCreatorProcessCompleted", vars.getLanguage()));
        vars.setMessage("CreateNewCustomer", myMessage);

        printPage(response, vars, "", strIsNotCompany, "", "", "", "", "", "", "", "", "", "", "",
            "", "", "", strIsNotCompanyConyuge, "", "", "", "", "", "", "", "");

      } catch (Exception e) {
        System.out.println("error throwed:" + e.getMessage());
        myMessage.setType("Error");
        myMessage.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
        myMessage.setMessage(e.getMessage());
        vars.setMessage("CreateNewCustomer", myMessage);

        printPage(response, vars, strOrgId, strIsNotCompany, strComercialName, strNames,
            strFirstLastname, strSecondLastname, strBPDocTypeId, strTaxID, strcLocationId,
            strSalesRepId, strTelf, strCel, strCorreo, strCivilState, strBPDocTypeConyugeId,
            strTaxConyugeID, strIsNotCompanyConyuge, strComercialNameConyuge, strNamesConyuge,
            strFirstLastnameConyuge, strSecondLastnameConyuge, strTelfConyuge, strCelConyuge,
            strCorreoConyuge, strcLocationConyugeId);
      }

    } else if (vars.commandIn("GETBPINFO")) {
      BPInfoActionHandler bpinfohdlr = new BPInfoActionHandler();
      String strTaxID = vars.getGlobalVariable("taxid", "CreateNewCustomer|TAXID", "");
      String strIsNotCompany = vars.getGlobalVariable("isnotcompany", "");

      response.setCharacterEncoding("UTF-8");
      response.setContentType("application/json");
      PrintWriter out = response.getWriter();
      out.print(bpinfohdlr.getbpinfo(strTaxID, strIsNotCompany).toString());
      out.close();

    } else if (vars.commandIn("GETBPCONYUGEINFO")) {
      BPInfoActionHandler bpinfohdlr = new BPInfoActionHandler();
      String strTaxConyugeID = vars.getGlobalVariable("taxconyugeid",
          "CreateNewCustomer|TAXCONYUGEID", "");

      response.setCharacterEncoding("UTF-8");
      response.setContentType("application/json");
      PrintWriter out = response.getWriter();
      out.print(bpinfohdlr.getbpinfo(strTaxConyugeID).toString());
      out.close();

    } else
      pageErrorPopUp(response);
  }

  private String processBusinessPartner(VariablesSecureApp vars, String strOrgId,
      String strIsNotCompany, String strComercialName, String strNames, String strFirstLastname,
      String strSecondLastname, String strBPDocTypeId, String strTaxID, String strcLocationId,
      String strSalesRepId, String strTelf, String strCel, String strCorreo) throws Exception {
    if (log4j.isDebugEnabled())
      log4j.debug("Save: Business Partner");

    String isCompany = "", comercialName = "", names = null, lastname1 = null, lastname2 = null,
        bpTemplateId, strBPartnerID;

    String TPL_NAME = "%TPL-BPCustomer";

    OBError myMessage = new OBError();

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
      strBPartnerID = SequenceIdData.getUUID();

      bpTemplateId = CreateNewCustomerData.selectBPTemplateId_NoOrgValAccess(this,
          Utility.getContext(this, vars, "#User_Client", null), TPL_NAME, strOrgId);
      if (bpTemplateId == null || "".equals(bpTemplateId))
        throw new Exception("SCR_NotTPLBP_Customer");

      CreateNewCustomerData.insertBPbasedOnTemplate(conn, this, strBPartnerID, vars.getClient(),
          strOrgId, vars.getUser(), comercialName, strTaxID, strSalesRepId, strBPDocTypeId,
          isCompany, names, lastname1, lastname2, bpTemplateId);

      if (strcLocationId != null && !"".equals(strcLocationId)) {
        CreateNewCustomerData.insertBPLocation(conn, this, vars.getClient(), strOrgId,
            vars.getUser(), strBPartnerID, strcLocationId);
      }

      if ((strTelf != null && !"".equals(strTelf)) || (strCel != null && !"".equals(strCel))
          || (strCorreo != null && !"".equals(strCorreo))) {
        CreateNewCustomerData.insertContactInformation(conn, this, vars.getClient(), strOrgId,
            vars.getUser(), strCorreo, strTelf, strCel, strBPartnerID);
      }

      releaseCommitConnection(conn);
      // success
      return strBPartnerID;

    } catch (Exception e) {
      System.out.println("acaa error:" + e.getMessage());

      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }

      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      if (e.getMessage().equals("SCR_NotTPLBP_Customer")) {
        myMessage.setMessage(Utility.messageBD(this, "SCR_NotTPLBP_Customer", vars.getLanguage()));
      } else if (e.getMessage().equals("SCR_UserNotHaveBPAssigned")) {
        myMessage
            .setMessage(Utility.messageBD(this, "SCR_UserNotHaveBPAssigned", vars.getLanguage()));
      } else {
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), e.getMessage());
      }
      throw new Exception(myMessage.getMessage());
    }

  }

  private void processRelationShip(VariablesSecureApp vars, String strOrgId,
      String strMainCBPartnerId, String strRelatedCBPartnerId) throws Exception {
    if (log4j.isDebugEnabled())
      log4j.debug("Save: Business Partner Relationship");

    OBError myMessage = new OBError();

    Connection conn = null;
    try {
      conn = new DalConnectionProvider(false).getTransactionConnection();

      // Catch database error message
      CreateNewCustomerData.insertBPartnerRelationship(conn, this, vars.getClient(), strOrgId,
          vars.getUser(), strMainCBPartnerId, strRelatedCBPartnerId, "SPOUSE");

      releaseCommitConnection(conn);

    } catch (Exception e) {
      System.out.println("acaa error:" + e.getMessage());
      try {
        releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      e.printStackTrace();
      log4j.warn("Rollback in transaction");
      if (e.getMessage().equals("SCR_NotTPLBP_Customer")) {
        myMessage.setMessage(Utility.messageBD(this, "SCR_NotTPLBP_Customer", vars.getLanguage()));
      } else if (e.getMessage().equals("SCR_UserNotHaveBPAssigned")) {
        myMessage
            .setMessage(Utility.messageBD(this, "SCR_UserNotHaveBPAssigned", vars.getLanguage()));
      } else {
        myMessage.setMessage(Utility.messageBD(this, "ProcessRunError", vars.getLanguage()));
      }
      throw new Exception(myMessage.getMessage());
    }

  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strOrgId,
      String strIsNotCompany, String strComercialName, String strNames, String strFirstLastname,
      String strSecondLastname, String strBPDocTypeId, String strTaxID, String strcLocationId,
      String strSalesRepId, String strTelf, String strCel, String strCorreo, String strCivilState,
      String strBPDocTypeConyugeId, String strTaxConyugeID, String strIsNotCompanyConyuge,
      String strComercialNameConyuge, String strNamesConyuge, String strFirstLastnameConyuge,
      String strSecondLastnameConyuge, String strTelfConyuge, String strCelConyuge,
      String strCorreoConyuge, String strcLocationConyugeId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: process CreateNewCustomer");

    ActionButtonDefaultData[] data = null;
    String strHelp = "", strDescription = "", strProcessId = "27939DC1970641E4B5F315AA7767DF5E",
        cLocationDescription = "";
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
        .readXmlTemplate("pe/com/unifiedgo/core/ad_actionbutton/CreateNewCustomer")
        .createXmlDocument();

    // ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "CreateNewCustomer", false, "", "",
    // "",
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
      String def_orgid = CreateNewCustomerData.selectLegalEntityNoZeroOrg(this,
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
      String def_bpdoctype = CreateNewCustomerData.selectDocumentType(this);
      cmb_bpdoctype = OBDal.getInstance().get(SCRComboItem.class, def_bpdoctype);
    }
    xmlDocument.setParameter("BPDocTypeId", (cmb_bpdoctype != null) ? cmb_bpdoctype.getId() : "");
    xmlDocument.setParameter("BPDocTypeDescription",
        (cmb_bpdoctype != null) ? cmb_bpdoctype.getName() : "");

    xmlDocument.setParameter("TaxID", (strTaxID != null) ? strTaxID : "");

    cLocationDescription = "";
    if (strcLocationId != null && !"".equals(strcLocationId)) {
      cLocationDescription = CreateNewCustomerData.selectLocationDescription(this, strcLocationId);
    }
    xmlDocument.setParameter("paramLocationId", strcLocationId);
    xmlDocument.setParameter("paramLocationDescription", cLocationDescription);

    xmlDocument.setParameter("Telf", (strTelf != null) ? strTelf : "");
    xmlDocument.setParameter("Cel", (strCel != null) ? strCel : "");
    xmlDocument.setParameter("Correo", (strCorreo != null) ? strCorreo : "");

    if (strSalesRepId != null && !strSalesRepId.isEmpty()) {
      usr = OBDal.getInstance().get(User.class, strSalesRepId);
    } else { // default
      User def_usr = OBDal.getInstance().get(User.class, vars.getUser());
      if (def_usr != null && def_usr.isSsaChkIssalesrep() != null && def_usr.isSsaChkIssalesrep()) {
        usr = def_usr;
      }
    }
    xmlDocument.setParameter("paramSalesRepId", (usr != null) ? usr.getId() : "");
    xmlDocument.setParameter("paramSalesRepDescription", (usr != null) ? usr.getName() : "");

    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateInvdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateInvsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));

    // Datos del Conyuge
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "BCB52BBAA8F1419BB402AF53B2183866", "",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "CreateNewCustomer"),
          Utility.getContext(this, vars, "#User_Client", "CreateNewCustomer"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "CreateNewCustomer", "");
      xmlDocument.setData("reportCivilState", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    xmlDocument.setParameter("CivilState", strCivilState);

    if (strBPDocTypeConyugeId != null && !strBPDocTypeConyugeId.isEmpty()) {
      cmb_bpdoctype = OBDal.getInstance().get(SCRComboItem.class, strBPDocTypeConyugeId);
    } else { // default
      String def_bpdoctype = CreateNewCustomerData.selectDocumentType(this);
      cmb_bpdoctype = OBDal.getInstance().get(SCRComboItem.class, def_bpdoctype);
    }
    xmlDocument.setParameter("BPDocTypeConyugeId",
        (cmb_bpdoctype != null) ? cmb_bpdoctype.getId() : "");
    xmlDocument.setParameter("BPDocTypeConyugeDescription",
        (cmb_bpdoctype != null) ? cmb_bpdoctype.getName() : "");

    xmlDocument.setParameter("TaxConyugeID", (strTaxConyugeID != null) ? strTaxConyugeID : "");

    xmlDocument.setParameter("isNotCompanyConyuge", strIsNotCompanyConyuge);

    xmlDocument.setParameter("ComercialNameConyuge",
        (strComercialNameConyuge != null) ? strComercialNameConyuge : "");
    xmlDocument.setParameter("NamesConyuge", (strNamesConyuge != null) ? strNamesConyuge : "");
    xmlDocument.setParameter("FirstLastnameConyuge",
        (strFirstLastnameConyuge != null) ? strFirstLastnameConyuge : "");
    xmlDocument.setParameter("SecondLastnameConyuge",
        (strSecondLastnameConyuge != null) ? strSecondLastnameConyuge : "");

    xmlDocument.setParameter("TelfConyuge", (strTelfConyuge != null) ? strTelfConyuge : "");
    xmlDocument.setParameter("CelConyuge", (strCelConyuge != null) ? strCelConyuge : "");
    xmlDocument.setParameter("CorreoConyuge", (strCorreoConyuge != null) ? strCorreoConyuge : "");

    cLocationDescription = "";
    if (strcLocationConyugeId != null && !"".equals(strcLocationConyugeId)) {
      cLocationDescription = CreateNewCustomerData.selectLocationDescription(this,
          strcLocationConyugeId);
    }
    xmlDocument.setParameter("paramLocationConyugeId", strcLocationConyugeId);
    xmlDocument.setParameter("paramLocationConyugeDescription", cLocationDescription);

    // New interface parameters
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.core.ad_actionbutton.CreateNewCustomer");

      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "CreateNewCustomer.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "CreateNewCustomer.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("CreateNewCustomer");
      vars.removeMessage("CreateNewCustomer");
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
    return "Servlet CreateNewCustomer";
  } // end of getServletInfo() method
}
