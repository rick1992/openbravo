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
 * All portions are Copyright (C) 2001-2014 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package pe.com.unifiedgo.project.ad_actionbutton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.geography.Country;
import org.openbravo.model.common.order.Order;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.financialmgmt.payment.Incoterms;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.imports.data.SimOrderImport;
import pe.com.unifiedgo.imports.data.SimOrderImportLine;

public class GenerateBudgetRequest extends HttpSecureAppServlet {
  final private Date now = DateUtils.truncate(new Date(), Calendar.DATE);
  private static final long serialVersionUID = 1L;
  private static final BigDecimal ZERO = BigDecimal.ZERO;
  private String strOrderImportID = "";
  private String strBpartnerId = "";
  private String strBpartnerLocationId = "";
  private String strIncotermsId = "";
  private String strtypepurchaseorder = "";
  private String strCurrencyId = "";
  private String strCountryId = "";
  private String strClientId = "";
  private String strOrgId = "";
  private String strWarehouseId = "";
  private String strPaymenTerm = "";
  private String strFinPaymenTerm = "";
  private String strTabId = "";
  private String strWindowId = "";
  private Order order = null;
  long lineNo;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    // Order Order = null;
    OBError myMessage = null;
    myMessage = new OBError();
    myMessage.setTitle("");
    if (vars.commandIn("DEFAULT")) {

      /*
       * Enumeration<String> params = vars.getParameterNames(); while (params.hasMoreElements()) {
       * System.out.println(params.nextElement()); }
       */

      try {
        strWindowId = vars.getStringParameter("inpwindowId");
        strTabId = vars.getStringParameter("inpTabId");
        strClientId = vars.getStringParameter("inpadClientId");

        String strRequisitionId = vars.getStringParameter("inpmRequisitionId");

        printPageDataSheet(response, vars, strWindowId, strTabId, strOrderImportID, strBpartnerId,
            strBpartnerLocationId, strCurrencyId, strRequisitionId);

      } catch (Exception e) {
        OBError myError;
        myError = new OBError();
        myError.setType("Warning");
        myError.setTitle(OBMessageUtils.messageBD("Warning"));
        myError.setMessage(OBMessageUtils.messageBD("Sim_pressButtonOk"));

        String strWindowPath = Utility.getTabURL(strTabId, "R", true);
        if (strWindowPath.equals(""))
          strWindowPath = strDefaultServlet;
        vars.setMessage(strTabId, myError);
        printPageClosePopUp(response, vars, strWindowPath);
      }
    } else if (vars.commandIn("SAVE")) {

      String[] selectedLines = null;
      String strRownum = null;
      String strRownum2 = null;
      String strLine = null;
      String strProductID = null;
      String strotro = null;
      String orderQty = null;
      String v_orderQty = null;
      // Order order = null;
      OrderLine orderLine = null;
      int qty = 0;
      int temporal = 0;
      int temporal2 = 0;

      String strWindowPath = Utility.getTabURL(strTabId, "R", true);
      if (strWindowPath.equals(""))
        strWindowPath = strDefaultServlet;
      // vars.setMessage(strTabId, myError);
      printPageClosePopUp(response, vars, strWindowPath);
    }
  }

  private OrderLine createOrderLine(VariablesSecureApp vars, SimOrderImport simOrder,
      SimOrderImportLine simOrderLines, Order order, String orderQty) {

    BusinessPartner bPartner = OBDal.getInstance().get(BusinessPartner.class, strBpartnerId);
    Product product = OBDal.getInstance().get(Product.class, simOrderLines.getProduct().getId());

    try {

      OrderLine orderLine = OBProvider.getInstance().get(OrderLine.class);
      orderLine.setClient(simOrder.getClient());
      orderLine.setOrganization(simOrder.getOrganization());
      orderLine.setUpdated(now);
      orderLine.setSalesOrder(order);
      orderLine.setLineNo(lineNo);
      lineNo += 10L;
      orderLine.setBusinessPartner(bPartner);
      orderLine.setPartnerAddress(simOrderLines.getPartnerAddress());
      orderLine.setOrderDate(simOrder.getOrderDate());
      orderLine.setScheduledDeliveryDate(simOrder.getScheduledDeliveryDate());
      orderLine.setProduct(simOrderLines.getProduct());
      orderLine.setWarehouse(simOrder.getWarehouse());
      orderLine.setUOM(simOrderLines.getUOM());
      // BigDecimal Quantity = new BigDecimal(simOrderLines.getOrderedQuantity());
      BigDecimal Quantity = new BigDecimal(orderQty);
      orderLine.setOrderedQuantity(Quantity);
      orderLine.setCurrency(simOrderLines.getCurrency());
      orderLine.setListPrice(simOrderLines.getListPrice());

      orderLine.setUnitPrice(simOrderLines.getUnitPrice());

      // El descuento ahora serà agregando una linea de descuento a la factura
      /*
       * if(simOrder.getScrDiscount()!=null){ BigDecimal Discount =
       * simOrderLines.getUnitPrice().multiply(simOrder.getScrDiscount().divide(new
       * BigDecimal(100))); orderLine.setUnitPrice(simOrderLines.getUnitPrice().subtract(Discount));
       * } orderLine.setLineNetAmount(simOrderLines.getLineNetAmount());
       * orderLine.setDiscount(simOrder.getScrDiscount());
       */
      orderLine.setTax(simOrderLines.getTax());

      BigDecimal standarPrice = new BigDecimal(simOrderLines.getStandardPrice());
      orderLine.setStandardPrice(simOrderLines.getUnitPrice());

      orderLine.setTaxableAmount(simOrderLines.getLineNetAmount());
      // orderLine.setSIMTariffHeading(orderQty);; setSimPartidaArancelaria(null);
      // setSimPartidaArancelaria(product.getSimPartidaArancelaria());
      orderLine.setSimPartidaArancelaria(product.getSimPartidaArancelaria());
      orderLine.setSimTlcDiscAdvalorem(product.getSIMTLCDiscountADdvalorem());
      orderLine.setSimAdvalorem(product.getSIMAdValorem());
      orderLine.setSimOrderimportline(simOrderLines);
      OBDal.getInstance().save(orderLine);
      return orderLine;
    } catch (Exception e) {
      // TODO: handle exception
      return null;
    }

  }

  // private Order createOrder(VariablesSecureApp vars){
  private int createOrder(VariablesSecureApp vars) {
    OBError myMessage = null;
    Client cliente = OBDal.getInstance().get(Client.class, strClientId);
    Organization org = OBDal.getInstance().get(Organization.class, strOrgId);
    Warehouse warehouse = OBDal.getInstance().get(Warehouse.class, strWarehouseId);
    BusinessPartner bPartner = OBDal.getInstance().get(BusinessPartner.class, strBpartnerId);
    Location location = OBDal.getInstance().get(Location.class, strBpartnerLocationId);
    Currency currency = OBDal.getInstance().get(Currency.class, strCurrencyId);
    Country country = OBDal.getInstance().get(Country.class, strCountryId);
    Incoterms incoterms = OBDal.getInstance().get(Incoterms.class, strIncotermsId);
    SimOrderImport sim_import = OBDal.getInstance().get(SimOrderImport.class, strOrderImportID);

    OrganizationStructureProvider osp = OBContext.getOBContext()
        .getOrganizationStructureProvider(cliente.getId());

    OBCriteria<DocumentType> P_doctype_c = OBDal.getInstance().createCriteria(DocumentType.class);
    P_doctype_c.add(Restrictions.eq(DocumentType.PROPERTY_SCOSPECIALDOCTYPE, "SIMPARTIAL"));
    // P_doctype_c.add(Restrictions.eq(DocumentType.PROPERTY_ORGANIZATION, org));

    P_doctype_c.add(Restrictions.in("organization.id", osp.getParentTree(org.getId(), true)));

    DocumentType P_doc_type_c = null;
    try {
      // DocumentType P_doc_type_c;
      P_doc_type_c = (DocumentType) P_doctype_c.uniqueResult();
      if (P_doc_type_c == null) {
        return 1;// no hay doc type
      }
    } catch (Exception e) {
      return 4;// There are some DocType whit the same information
    }

    try {

      Long contador = sim_import.getPartialcount() + 1;
      String documentno = sim_import.getDocumentNo() + "-" + contador.toString();

      order = OBProvider.getInstance().get(Order.class);
      order.setClient(cliente);
      order.setOrganization(org);
      order.setUpdated(now);
      order.setSalesTransaction(false);
      order.setDocumentStatus("DR");
      order.setDocumentAction("CO");
      order.setTransactionDocument(P_doc_type_c);
      order.setDocumentType(P_doc_type_c);
      order.setDocumentNo(documentno);

      order.setOrderDate(sim_import.getOrderDate());
      order.setDatePrinted(sim_import.getDatePrinted());
      order.setScheduledDeliveryDate(sim_import.getScheduledDeliveryDate());
      order.setBusinessPartner(bPartner);
      order.setPartnerAddress(location);
      order.setInvoiceAddress(location);
      order.setPrintDiscount(false);
      order.setCurrency(currency);

      order.setDeliveryMethod("D");
      //// System.out.println(incoterms);
      order.setIncoterms(sim_import.getIncoterms());
      order.setSimOrderimport(sim_import);
      order.setAccountingDate(now);
      order.setPaymentTerms(sim_import.getPaymentTerms());
      order.setPaymentMethod(sim_import.getPaymentMethod());
      order.setWarehouse(sim_import.getWarehouse());
      order.setPriceList(sim_import.getPriceList());
      order.setInvoiceTerms("I");
      order.setSimIsImport(true);
      order.setSimCCountry(country);
      order.setSimForwarderBpartner(sim_import.getForwarderBpartner());

      sim_import.setPartialcount(contador);
      OBDal.getInstance().save(order);
      return 0;

    } catch (Exception e) {
      // TODO: handle exception
      return 1;
    }

  }

  private OBError copyLines(VariablesSecureApp vars, String strRownums, String strKey)
      throws IOException, ServletException {
    OBError myError = null;
    return myError;
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strWindowId, String strTabId, String strOrderImportID, String strBpartner,
      String strBpartnerLocation, String strCurrencyId, String strRequisitionId)
      throws IOException, ServletException {

    // GeneratePartialLinesData[] data = null;
    // GeneratePartialLinesData[] dataLines = null;

    GenerateBudgetRequestData[] data = null;

    XmlDocument xmlDocument = xmlEngine
        .readXmlTemplate("pe/com/unifiedgo/project/ad_actionbutton/GenerateBudgetRequest")
        .createXmlDocument();

    OBError myMessage = null;
    myMessage = new OBError();
    try {
      data = GenerateBudgetRequestData.selectRequisitionLineForBudgetRequest(this,
          strRequisitionId);
      // data = GeneratePartialLinesData.select1(this, strOrderImportID);
      // dataLines = GeneratePartialLinesData.select2(this, strOrderImportID);
    } catch (ServletException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    // data = GeneratePartialLinesData.set();
    // System.out.println(data[0].DOCNO);
    /*
     * String Docnum = data[0].DOCNO; String DateOr = data[0].DATEORDER; String BPartn =
     * data[0].PARTNER;
     */

    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabId", strTabId);

    xmlDocument.setData("structure4", data);
    // xmlDocument.setData("structure4", dataLines);

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet Copy from order";
  } // end of getServletInfo() method
}
