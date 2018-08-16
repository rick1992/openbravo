package pe.com.unifiedgo.imports.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.UtilSql;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.database.SessionInfo;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.model.pricing.pricelist.PriceList;
import org.openbravo.service.db.QueryTimeOutUtil;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.SCO_Utils;
import pe.com.unifiedgo.core.data.SCRComboCategory;
import pe.com.unifiedgo.core.data.SCRComboItem;
import pe.com.unifiedgo.imports.data.SimFolioImport;
import pe.com.unifiedgo.imports.data.simRelExpensesGlitem;

public class CreateInvoice extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static String strBDErrorMessage = "";

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strAD_Org_ID = vars.getGlobalVariable("inpadOrgId", "CreateInvoice|AD_Org_ID", "");
      String strcBpartnetID = vars.getGlobalVariable("inpCBPartner", "CreateInvoice|bpartner", "");
      String strDAMType = vars.getGlobalVariable("inpDAMType", "CreateInvoice|DAMType", "");
      String strExpenseType = vars.getGlobalVariable("inpExpenseType", "CreateInvoice|ExpenseType",
          "");
      String strExpenseMode = vars.getGlobalVariable("inpExpenseMode", "CreateInvoice|ExpenseMode",
          "");
      String strSimFolioImportId = vars.getGlobalVariable("inpsimFolioimportId",
          "CreateInvoice|SIMFolioImportID", "");

      String strChkCreateLine = "N";
      printPageDataSheet(response, vars, strAD_Org_ID, strcBpartnetID, strDAMType, strExpenseType,
          strExpenseMode, strChkCreateLine);

    } else if (vars.commandIn("GETDOCTYPE")) {
      String val = "N";
      String strDocTypeId = vars.getStringParameter("inpDocType");
      DocumentType doctype = OBDal.getInstance().get(DocumentType.class, strDocTypeId);
      if (doctype != null) {
        if (doctype.getScoSpecialdoctype().equals("SCOAPINVOICE"))
          val = "Y";
      }

      response.setCharacterEncoding("UTF-8");
      response.setContentType("application/json");
      PrintWriter out = response.getWriter();
      out.print(getJSON("", val, ""));
      out.close();

    } else if (vars.commandIn("GETINVOICE")) {

      String strAD_Client_ID = vars.getClient();
      String strAD_User_ID = vars.getUser();
      String strAD_Org_ID = vars.getGlobalVariable("inpadOrgId", "CreateInvoice|AD_Org_ID", "");
      String strcBpartnetID = vars.getStringParameter("inpCBPartner");
      String strDAMType = vars.getStringParameter("inpDAMType");
      String strDocTypeId = vars.getStringParameter("inpDocType");
      String strPriceListId = vars.getStringParameter("inpPriceList");
      String strExpenseType = vars.getStringParameter("inpExpenseType");
      String strExpenseMode = vars.getStringParameter("inpExpenseMode");
      String strSimFolioImportId = vars.getGlobalVariable("inpsimFolioimportId",
          "CreateInvoice|SIMFolioImportID", "");

      String strChkInsertLine = vars.getStringParameter("inpChkInsertLine");
      String strLineAmount = vars.getStringParameter("inpLineAmount");

      String invoiceTabId = "B2DE01505F1346A39E83743B7B8A10ED"; // Purchase Invoice Header tab
      if (strDocTypeId != null && !strDocTypeId.equals("")) {
        DocumentType doctype = OBDal.getInstance().get(DocumentType.class, strDocTypeId);
        if (doctype != null) {
          if (doctype.getScoSpecialdoctype().equals("SCOAPCREDITMEMO"))
            invoiceTabId = "96B59F682AE64A4EB2F8916EC128D46F"; // Credit Note Header tab
        }
      }
      Invoice invoice = null;
      try {
        invoice = createInvoiceFromImportFolio(strAD_Client_ID, strAD_Org_ID, strAD_User_ID,
            strcBpartnetID, strDAMType, strExpenseType, strExpenseMode, strSimFolioImportId,
            strDocTypeId, strPriceListId);

        if (strLineAmount == null || strLineAmount.equals("")) {
          strLineAmount = "0";
        }
        System.out.println(invoice.getDocumentNo());
        if (invoice != null)
          createInvoiceLineFromImportFolio(invoice, strLineAmount);

      } catch (Exception e) {
        invoice = null;
        System.out.println(e.getMessage());
        OBDal.getInstance().rollbackAndClose();
        e.printStackTrace();
      }

      response.setCharacterEncoding("UTF-8");
      response.setContentType("application/json");
      PrintWriter out = response.getWriter();
      out.print(getJSON(invoiceTabId, (invoice != null) ? invoice.getId() : null,
          CreateInvoice.strBDErrorMessage));
      out.close();
    }
  }

  private String getJSON(String tabId, String recordId, String message) throws ServletException {
    JSONObject json = null;

    try {
      OBContext.setAdminMode();

      json = new JSONObject();
      json.put("tabId", tabId);
      if (recordId != null) {
        System.out.println("PRIMERO");
        json.put("recordId", recordId);
        json.put("msg", "");
      } else {
        System.out.println("SEGUNDO");
        json.put("recordId", "");
        json.put("msg", message);
      }

    } catch (Exception e) {
      try {
        json.put("error", e.getMessage());
      } catch (JSONException jex) {
        log4j.error("Error trying to generate message: " + jex.getMessage(), jex);
      }
    } finally {
      OBContext.restorePreviousMode();
    }

    return json.toString();
  }

  private void createInvoiceLineFromImportFolio(Invoice invoice, String strLineAmount)
      throws Exception {

    System.out.println("VICTOR");

    // Get TAX
    OBCriteria<TaxRate> taxs = OBDal.getInstance().createCriteria(TaxRate.class);
    taxs.add(Restrictions.eq(TaxRate.PROPERTY_CLIENT, invoice.getClient()));
    taxs.add(Restrictions.eq(TaxRate.PROPERTY_ACTIVE, true));
    taxs.add(Restrictions.eq(TaxRate.PROPERTY_SCOSPECIALTAX, "SCOIGV"));
    System.out.println("VICTOR 1");
    TaxRate tax = null;

    tax = (TaxRate) taxs.uniqueResult();
    if (tax == null) {
      CreateInvoice.strBDErrorMessage = "swa_nofound_comboitem_transfer";
      // return null;
    }

    // ///////////////
    System.out.println("VICTOR 2: " + invoice.getScoDuaexpensetype());

    // GetAccount GlItem
    OBCriteria<simRelExpensesGlitem> glItems = OBDal.getInstance().createCriteria(
        simRelExpensesGlitem.class);
    glItems.add(Restrictions.eq(simRelExpensesGlitem.PROPERTY_CLIENT, invoice.getClient()));
    glItems.add(Restrictions.eq(simRelExpensesGlitem.PROPERTY_ACTIVE, true));
    glItems.add(Restrictions.eq(simRelExpensesGlitem.PROPERTY_DUAEXPENSETYPE,
        invoice.getScoDuaexpensetype()));
    simRelExpensesGlitem glItem = null;
    System.out.println("VICTOR 3");
    List<simRelExpensesGlitem> relationEx = glItems.list();

    if (relationEx.size() == 0) {
      CreateInvoice.strBDErrorMessage = "sim_noaccountwithexpensestype";
      throw new Exception("Error");
    }
    System.out.println("....: " + relationEx.get(0).getGLItem());
    /*
     * glItem = (simRelExpensesGlitem) glItems.uniqueResult(); if (glItem == null) {
     * CreateInvoice.strBDErrorMessage = "swa_nofound_comboitem_transfer"; //return null; }
     */

    System.out.println("VICTOR 4 ");
    //
    InvoiceLine invoicel = OBProvider.getInstance().get(InvoiceLine.class);
    invoicel.setOrganization(invoice.getOrganization());
    invoicel.setClient(invoice.getClient());
    invoicel.setInvoice(invoice);
    invoicel.setLineNo(new Long(10));
    invoicel.setFinancialInvoiceLine(true);
    invoicel.setAccount(relationEx.get(0).getGLItem());// glitem
    invoicel.setInvoicedQuantity(new BigDecimal(1));
    invoicel.setUnitPrice(new BigDecimal(strLineAmount));
    invoicel.setLineNetAmount(new BigDecimal(strLineAmount));
    invoicel.setTax(tax);
    invoicel.setStandardPrice(new BigDecimal(strLineAmount));
    invoicel.setTaxableAmount(new BigDecimal(strLineAmount));
    invoicel.setBusinessPartner(invoice.getBusinessPartner());
    System.out.println("VICTOR 5 ");
    OBDal.getInstance().save(invoicel);

  }

  private Invoice createInvoiceFromImportFolio(String strAD_Client_ID, String strAD_Org_ID,
      String strAD_User_ID, String strcBpartnetID, String strDAMType, String strExpenseType,
      String strExpenseMode, String strSimFolioImportId, String strDocTypeId, String strPriceListId)
      throws Exception {

    // IMPORT FOLIO
    SimFolioImport importFolio = OBDal.getInstance().get(SimFolioImport.class, strSimFolioImportId);
    if (importFolio.getSCODua() == null) {
      CreateInvoice.strBDErrorMessage = "sim_therearentdua";
      return null;
    }

    // DATA SETUP
    Client client = OBDal.getInstance().get(Client.class, strAD_Client_ID);
    Organization org = OBDal.getInstance().get(Organization.class, strAD_Org_ID);
    User user = OBDal.getInstance().get(User.class, strAD_User_ID);

    DocumentType doctype = OBDal.getInstance().get(DocumentType.class, strDocTypeId);

    if (doctype == null) {
      doctype = SCO_Utils.getDocTypeFromSpecial(org, "SCOAPINVOICE");
      if (doctype == null) {
        CreateInvoice.strBDErrorMessage = "NoDocumentTypeFound";
        return null;
      }
    }
    String DocumentNo = SCO_Utils.getDocumentNo(doctype, "C_Invoice");

    BusinessPartner bpartner = OBDal.getInstance().get(BusinessPartner.class, strcBpartnetID);
    if (bpartner == null) {
      CreateInvoice.strBDErrorMessage = "BPartnerNotFound";
      return null;
    } else if (!bpartner.isVendor()) {
      CreateInvoice.strBDErrorMessage = "FIN_NoVendor";
      return null;
    } else if ((bpartner.getBusinessPartnerLocationList().size() <= 0)
        || (bpartner.getPurchasePricelist() == null) || (bpartner.getPOPaymentTerms() == null)
        || (bpartner.getPOPaymentMethod() == null)) {
      CreateInvoice.strBDErrorMessage = "MissingPLPTPL";
      return null;
    } else if (bpartner.getPurchasePricelist().getCurrency() == null) {
      CreateInvoice.strBDErrorMessage = "APRM_CreditNoPricelistCurrency";
      return null;
    }

    OBCriteria<SCRComboCategory> cmbcategories = OBDal.getInstance().createCriteria(
        SCRComboCategory.class);
    cmbcategories.add(Restrictions.eq(SCRComboCategory.PROPERTY_CLIENT, client));
    if (doctype.getScoSpecialdoctype().equals("SCOAPINVOICE"))
      cmbcategories.add(Restrictions.eq(SCRComboCategory.PROPERTY_SEARCHKEY, "tablasunat10"));
    else
      cmbcategories.add(Restrictions.eq(SCRComboCategory.PROPERTY_SEARCHKEY,
          "tablasunat10_NotaCredito"));
    SCRComboCategory cmbcategory = null;
    try {
      cmbcategory = (SCRComboCategory) cmbcategories.uniqueResult();
      if (cmbcategory == null) {
        CreateInvoice.strBDErrorMessage = "swa_nofound_comboitem_transfer";
        return null;
      }
    } catch (Exception e) {
      CreateInvoice.strBDErrorMessage = "swa_nofound_comboitem_transfer";
      return null;
    }

    OBCriteria<SCRComboItem> cmbitems = OBDal.getInstance().createCriteria(SCRComboItem.class);
    cmbitems.add(Restrictions.eq(SCRComboItem.PROPERTY_CLIENT, client));
    cmbitems.add(Restrictions.eq(SCRComboItem.PROPERTY_COMBOCATEGORY, cmbcategory));
    System.out.println("ESPECIAL: " + doctype.getScoSpecialdoctype());
    if (doctype.getScoSpecialdoctype().equals("SCOAPINVOICE"))
      cmbitems.add(Restrictions.eq(SCRComboItem.PROPERTY_SEARCHKEY, "tabla10_01")); // Factura
    else
      cmbitems.add(Restrictions.eq(SCRComboItem.PROPERTY_SEARCHKEY, "tabla10_07")); // Crédito

    SCRComboItem cmbitem = null;
    try {
      cmbitem = (SCRComboItem) cmbitems.uniqueResult();
      if (cmbitem == null) {
        CreateInvoice.strBDErrorMessage = "swa_nofound_comboitem_transfer";
        return null;
      }
    } catch (Exception e) {
      CreateInvoice.strBDErrorMessage = "swa_nofound_comboitem_transfer";
      return null;
    }

    PriceList priceList = OBDal.getInstance().get(PriceList.class, strPriceListId);
    if (priceList == null) {
      priceList = bpartner.getPurchasePricelist();
    }

    // Creating Invoice header
    Invoice invoice = OBProvider.getInstance().get(Invoice.class);
    invoice.setOrganization(org);
    invoice.setClient(client);
    invoice.setDocumentType(doctype);
    invoice.setTransactionDocument(doctype);
    invoice.setDocumentNo(DocumentNo);
    invoice.setAccountingDate(new Date());
    invoice.setInvoiceDate(new Date());
    invoice.setTaxDate(new Date());
    invoice.setBusinessPartner(bpartner);
    invoice.setPartnerAddress(bpartner.getBusinessPartnerLocationList().get(0));
    invoice.setPriceList(priceList);
    invoice.setCurrency(priceList.getCurrency());
    invoice.setSummedLineAmount(BigDecimal.ZERO);
    invoice.setGrandTotalAmount(BigDecimal.ZERO);
    invoice.setWithholdingamount(BigDecimal.ZERO);
    invoice.setSalesTransaction(false);
    invoice.setPaymentMethod(bpartner.getPOPaymentMethod());
    invoice.setPaymentTerms(bpartner.getPOPaymentTerms());
    invoice.setScoPodoctypeComboitem(cmbitem);
    invoice.setScoPurchaseinvoicetype("SCO_PURIMP");
    invoice.setScoIsimportation(true);
    invoice.setScoDua(importFolio.getSCODua());
    invoice.setScoDuatype(strDAMType);
    if ("SO".compareTo(strDAMType) != 0) {
      invoice.setScoDuaexpensetype(strExpenseType);
      invoice.setScoDuaexpensemode(strExpenseMode);
    }
    invoice.setCreatedBy(user);
    invoice.setUpdatedBy(user);

    OBDal.getInstance().save(invoice);

    return invoice;
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strAD_Org_ID, String strcBpartnetID, String strDAMType, String strExpenseType,
      String strExpenseMode, String strChkCreateLine) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;

    String discard[] = { "secTable" };
    xmlDocument = xmlEngine.readXmlTemplate("pe/com/unifiedgo/imports/ad_reports/CreateInvoice",
        discard).createXmlDocument();

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.imports.ad_reports.CreateInvoice");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      xmlDocument.setParameter("paramAdOrgId", strAD_Org_ID);

      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "CreateInvoice.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "CreateInvoice.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("CreateInvoice");
      vars.removeMessage("CreateInvoice");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    String stPriceList = "";
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "C_DOCTYPE_ID",
          "", "272DE2F13BA24B18940C2860288184E6", Utility.getContext(this, vars,
              "#AccessibleOrgTree", "CreateInvoice"), Utility.getContext(this, vars,
              "#User_Client", "CreateInvoice"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "CreateInvoice", "");
      xmlDocument.setData("reportDocType", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    String strDocType = "";
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "M_PRICELIST_ID",
          "", "26ABE9BE38BF417C9B6EE158180850AC", Utility.getContext(this, vars,
              "#AccessibleOrgTree", "CreateInvoice"), Utility.getContext(this, vars,
              "#User_Client", "CreateInvoice"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "CreateInvoice", "");
      xmlDocument.setData("reportPriceList", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "79911DAE2D784E718F597E219C7A649D", "", Utility.getContext(this, vars,
              "#AccessibleOrgTree", "CreateInvoice"), Utility.getContext(this, vars,
              "#User_Client", "CreateInvoice"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "CreateInvoice", "");
      xmlDocument.setData("reportDAMType", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "BC21D0049719443DBF8DAE4F29FAEFAB", "", Utility.getContext(this, vars,
              "#AccessibleOrgTree", "CreateInvoice"), Utility.getContext(this, vars,
              "#User_Client", "CreateInvoice"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "CreateInvoice", "");
      xmlDocument.setData("reportExpenseType", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "09D69FA68F70462197AB5D43C83DE185", "", Utility.getContext(this, vars,
              "#AccessibleOrgTree", "CreateInvoice"), Utility.getContext(this, vars,
              "#User_Client", "CreateInvoice"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "CreateInvoice", "");
      xmlDocument.setData("reportExpenseMode", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    String strcBpartnetId = "";
    xmlDocument.setParameter("paramBPartnerId", strcBpartnetId);
    xmlDocument.setParameter("paramBPartnerDescription",
        CreateInvoiceData.selectBpartner(this, strcBpartnetId));

    String strCollectionId = "";

    xmlDocument.setParameter("paramCollectionId", strCollectionId);
    xmlDocument.setParameter("paramCollectionDescripcion", "");

    xmlDocument.setParameter("OrgFolio", "AD_Org_ID=\"" + strAD_Org_ID + "\"; ");
    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    // xmlDocument.setParameter("cBpartnerId", strcBpartnetID);
    xmlDocument.setParameter("BPartnerDescription", selectBPartner(this, strcBpartnetID));
    xmlDocument.setParameter("DocType", strDocType);
    xmlDocument.setParameter("PriceList", stPriceList);

    strDAMType = "EXP";
    xmlDocument.setParameter("DAMType", strDAMType);
    xmlDocument.setParameter("ExpenseType", strExpenseType);
    xmlDocument.setParameter("ExpenseMode", strExpenseMode);

    // String strChkLocatorOut="Y";
    xmlDocument.setParameter("paramChkInsertLine", strChkCreateLine);

    out.println(xmlDocument.print());
    out.close();
  }

  private String selectBPartner(ConnectionProvider connectionProvider, String cBpartnerId)
      throws ServletException {
    String strSql = "";
    strSql = strSql + "      SELECT NAME FROM C_BPARTNER WHERE C_BPARTNER_ID=?";

    ResultSet result;
    String strReturn = "";
    PreparedStatement st = null;

    int iParameter = 0;
    try {
      st = connectionProvider.getPreparedStatement(strSql);
      QueryTimeOutUtil.getInstance().setQueryTimeOut(st, SessionInfo.getQueryProfile());
      iParameter++;
      UtilSql.setValue(st, iParameter, 12, null, cBpartnerId);

      result = st.executeQuery();
      if (result.next()) {
        strReturn = UtilSql.getValue(result, "name");
      }
      result.close();
    } catch (SQLException e) {
      log4j.error("SQL error in query: " + strSql + "Exception:" + e);
      throw new ServletException("@CODE=" + Integer.toString(e.getErrorCode()) + "@"
          + e.getMessage());
    } catch (Exception ex) {
      log4j.error("Exception in query: " + strSql + "Exception:" + ex);
      throw new ServletException("@CODE=@" + ex.getMessage());
    } finally {
      try {
        connectionProvider.releasePreparedStatement(st);
      } catch (Exception ignore) {
        ignore.printStackTrace();
      }
    }
    return (strReturn);
  }

}
