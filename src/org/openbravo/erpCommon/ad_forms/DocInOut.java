/*
 ******************************************************************************
 * The contents of this file are subject to the   Compiere License  Version 1.1
 * ("License"); You may not use this file except in compliance with the License
 * You may obtain a copy of the License at http://www.compiere.org/license.html
 * Software distributed under the License is distributed on an  "AS IS"  basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * The Original Code is                  Compiere  ERP & CRM  Business Solution
 * The Initial Developer of the Original Code is Jorg Janke  and ComPiere, Inc.
 * Portions created by Jorg Janke are Copyright (C) 1999-2001 Jorg Janke, parts
 * created by ComPiere are Copyright (C) ComPiere, Inc.;   All Rights Reserved.
 * Contributor(s): Openbravo SLU
 * Contributions are Copyright (C) 2001-2013 Openbravo S.L.U.
 ******************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.costing.CostingAlgorithm.CostDimension;
import org.openbravo.costing.CostingStatus;
import org.openbravo.costing.CostingUtils;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.financial.FinancialUtils;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.currency.ConversionRateDoc;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;

public class DocInOut extends AcctServer {
  private static final long serialVersionUID = 1L;
  static Logger log4jDocInOut = Logger.getLogger(DocInOut.class);

  /** AD_Table_ID */
  private String SeqNo = "0";

  /**
   * Constructor
   * 
   * @param AD_Client_ID
   *          AD_Client_ID
   */
  public DocInOut(String AD_Client_ID, String AD_Org_ID, ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  public void loadObjectFieldProvider(ConnectionProvider conn, String stradClientId, String Id)
      throws ServletException {
    setObjectFieldProvider(DocInOutData.selectRegistro(conn, stradClientId, Id));
  }

  /**
   * Load Document Details
   * 
   * @return true if loadDocumentType was set
   */
  public boolean loadDocumentDetails(FieldProvider[] data, ConnectionProvider conn) {
    C_Currency_ID = NO_CURRENCY;
    log4jDocInOut.debug("loadDocumentDetails - C_Currency_ID : " + C_Currency_ID);
    DateDoc = data[0].getField("MovementDate");
    C_BPartner_Location_ID = data[0].getField("C_BPartner_Location_ID");

    loadDocumentType(); // lines require doc type
    // Contained Objects
    p_lines = loadLines(conn);
    log4jDocInOut.debug("Lines=" + p_lines.length);
    return true;
  } // loadDocumentDetails

  /**
   * Load Invoice Line
   * 
   * @return DocLine Array
   */
  public DocLine[] loadLines(ConnectionProvider conn) {
    ArrayList<Object> list = new ArrayList<Object>();
    DocLineInOutData[] data = null;
    try {
      data = DocLineInOutData.select(conn, Record_ID);
    } catch (ServletException e) {
      log4jDocInOut.warn(e);
    }
    //
    for (int i = 0; data != null && i < data.length; i++) {
      String Line_ID = data[i].getField("M_INOUTLINE_ID");
      DocLine_Material docLine = new DocLine_Material(DocumentType, Record_ID, Line_ID);
      docLine.loadAttributes(data[i], this);
      docLine.setQty(data[i].getField("MOVEMENTQTY"), conn); // sets Trx
      docLine.setBreakdownQty(data[i].getField("BREAKDOWNQTY"));
      // and
      // Storage
      // Qty
      docLine.m_M_Locator_ID = data[i].getField("M_LOCATOR_ID");
      OBContext.setAdminMode(false);
      try {
        // Get related M_Transaction_ID
        ShipmentInOutLine inOut = OBDal.getInstance().get(ShipmentInOutLine.class, Line_ID);
        if (inOut.getMaterialMgmtMaterialTransactionList().size() > 0) {
          docLine.setTransaction(inOut.getMaterialMgmtMaterialTransactionList().get(0));
        }
      } finally {
        OBContext.restorePreviousMode();
      }
      if (docLine.m_M_Product_ID.equals(""))
        log4jDocInOut.debug(" - No Product - ignored");
      else
        list.add(docLine);
    }
    // Return Array
    DocLine[] dl = new DocLine[list.size()];
    list.toArray(dl);
    return dl;
  } // loadLines

  /**
   * Get Balance
   * 
   * @return Zero (always balanced)
   */
  public BigDecimal getBalance() {
    BigDecimal retValue = ZERO;
    return retValue;
  } // getBalance

  /**
   * Create Facts (the accounting logic) for MMS, MMR.
   * 
   * <pre>
   *  Shipment
   *      CoGS            DR
   *      Inventory               CR
   *  Shipment of Project Issue
   *      CoGS            DR
   *      Project                 CR
   *  Receipt
   *      Inventory       DR
   *      NotInvoicedReceipt      CR
   * </pre>
   * 
   * @param as
   *          accounting schema
   * @return Fact
   */
  public Fact createFact(AcctSchema as, ConnectionProvider conn, Connection con,
      VariablesSecureApp vars) throws ServletException {

    // Select specific definition
    String strClassname = AcctServerData.selectTemplateDoc(conn, as.m_C_AcctSchema_ID,
        DocumentType);
    if (strClassname.equals(""))
      strClassname = AcctServerData.selectTemplate(conn, as.m_C_AcctSchema_ID, AD_Table_ID);
    if (!strClassname.equals("")) {
      try {
        DocInOutTemplate newTemplate = (DocInOutTemplate) Class.forName(strClassname).newInstance();
        return newTemplate.createFact(this, as, conn, con, vars);
      } catch (Exception e) {
        log4j.error("Error while creating new instance for DocInOutTemplate - " + e);
      }
    }

    // C_Currency_ID = as.getC_Currency_ID();
    // create Fact Header
    Fact fact = new Fact(this, as, Fact.POST_Actual);

    for (int i = 0; p_lines != null && i < p_lines.length; i++) {
      DocLine_Material line = (DocLine_Material) p_lines[i];

      ShipmentInOutLine shipmentInOutLine = OBDal.getInstance().get(ShipmentInOutLine.class,
          line.m_TrxLine_ID);
      OBCriteria<InvoiceLine> obCriteria = OBDal.getInstance().createCriteria(InvoiceLine.class);
      obCriteria.add(Restrictions.eq(InvoiceLine.PROPERTY_GOODSSHIPMENTLINE, shipmentInOutLine));
      List<InvoiceLine> lines = obCriteria.list();
    }

    ShipmentInOut inOut = OBDal.getInstance().get(ShipmentInOut.class, Record_ID);
    String ordertype_val = "";
    String ordertypecat_val = "";
    if (inOut.getSWAUpdateReason() != null) {
      ordertype_val = inOut.getSWAUpdateReason().getSearchKey();
      ordertypecat_val = inOut.getSWAUpdateReason().getComboCategory().getSearchKey();
    }

    String Fact_Acct_Group_ID = SequenceIdData.getUUID();
    // Line pointers
    FactLine dr = null;
    FactLine cr = null;
    // Sales or Return from Customer
    if (DocumentType.equals(AcctServer.DOCTYPE_MatShipment)) {

      Boolean matReturn = IsReturn.equals("Y");
      SimpleDateFormat df = new SimpleDateFormat("MMM-yy", new Locale("es", "ar"));
      String periodo = df.format(inOut.getMovementDate());

      for (int i = 0; p_lines != null && i < p_lines.length; i++) {
        DocLine_Material line = (DocLine_Material) p_lines[i];

        Organization legalEntity = OBContext.getOBContext()
            .getOrganizationStructureProvider(AD_Client_ID)
            .getLegalEntity(OBDal.getInstance().get(Organization.class, line.m_AD_Org_ID));
        Currency costCurrency = FinancialUtils.getLegalEntityCurrency(legalEntity);
        if (!CostingStatus.getInstance().isMigrated()) {
          costCurrency = OBDal.getInstance().get(Client.class, AD_Client_ID).getCurrency();
        } else if (line.transaction != null && line.transaction.getCurrency() != null) {
          costCurrency = line.transaction.getCurrency();
        }
        int standardPrecision = 2;
        OBContext.setAdminMode(false);
        try {
          standardPrecision = costCurrency.getStandardPrecision().intValue();
        } finally {
          OBContext.restorePreviousMode();
        }
        C_Currency_ID = costCurrency.getId();
        Account cogsAccount = null;

        ShipmentInOutLine shipmentInOutLine = OBDal.getInstance().get(ShipmentInOutLine.class,
            line.m_TrxLine_ID);
        OBCriteria<InvoiceLine> obCriteria = OBDal.getInstance().createCriteria(InvoiceLine.class);
        obCriteria.add(Restrictions.eq(InvoiceLine.PROPERTY_GOODSSHIPMENTLINE, shipmentInOutLine));
        List<InvoiceLine> lines = obCriteria.list();

        if (lines.size() == 0) {// search by order
          OrderLine orderLine = shipmentInOutLine.getSalesOrderLine();
          if (orderLine != null) {
            obCriteria = OBDal.getInstance().createCriteria(InvoiceLine.class);
            obCriteria.add(Restrictions.eq(InvoiceLine.PROPERTY_SALESORDERLINE, orderLine));
            lines = obCriteria.list();
          }
        }

        /*
         * if (lines.size() > 0 && lines.get(0).getInvoice() != null &&
         * lines.get(0).getInvoice().isScoIsforfree()) { DocLineInvoiceData[] dataGlitem = null;
         * dataGlitem = DocLineInvoiceData.selectGlitem(conn,
         * lines.get(0).getInvoice().getScoForfreeassetexGli().getId(), as.getC_AcctSchema_ID());
         * cogsAccount = Account.getAccount(conn, dataGlitem[0].glitemDebitAcct); } else
         */

        // For consumomatprimainmueble use Stock Variance acc
        boolean isconsumomatprimainmueble = false;
        if (ordertype_val.equals("consumomatprimainmueble")
            || ordertype_val.equals("anulacion_consumomatprimainmueble")) {
          cogsAccount = line.getAccount(ProductInfo.ACCTTYPE_P_StockVariance, as, conn);
          if (cogsAccount != null) {
            isconsumomatprimainmueble = true;
          }
        }

        if (cogsAccount == null) {
          if (matReturn) {
            cogsAccount = line.getAccount(ProductInfo.ACCTTYPE_P_CogsReturn, as, conn);
          } else if (inOut.getDocumentType().getScoSpecialdoctype().equals("SWAINTERNALSHIPMENT")) {

            if (inOut.getSCODiverseAccountGLItem() == null) {
              cogsAccount = line.getAccount(ProductInfo.ACCTTYPE_P_Asset, as, conn);
            } else {
              cogsAccount = getAccountGLItem(inOut.getSCODiverseAccountGLItem(), as, true, conn);
            }
          }
        }

        if (cogsAccount == null) {
          cogsAccount = line.getAccount(ProductInfo.ACCTTYPE_P_Cogs, as, conn);
        }

        Product product = OBDal.getInstance().get(Product.class, line.m_M_Product_ID);
        if (cogsAccount == null) {
          org.openbravo.model.financialmgmt.accounting.coa.AcctSchema schema = OBDal.getInstance()
              .get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
                  as.m_C_AcctSchema_ID);
          if (matReturn) {
            log4j.error("No Account COGS Return for product: " + product.getName()
                + " in accounting schema: " + schema.getName());
          } else {
            log4j.error("No Account COGS for product: " + product.getName()
                + " in accounting schema: " + schema.getName());
          }
        }

        Account assetAccount = null;
        if (inOut.getSCODiverseAccountGLItem() == null) {
          if (inOut.getSWAUpdateReason() != null && inOut.getSWAUpdateReason()
              .getScoOrdtypeRefaccGli() != null)/*
                                                 * Esto es tipo de orden
                                                 */
          {
            assetAccount = getAccountGLItem(inOut.getSWAUpdateReason().getScoOrdtypeRefaccGli(), as,
                true, conn);
          } else {
            assetAccount = line.getAccount(ProductInfo.ACCTTYPE_P_Asset, as, conn);
          }

        } else {
          assetAccount = getAccountGLItem(inOut.getSCODiverseAccountGLItem(), as, true, conn);
        }

        if (assetAccount == null) {
          org.openbravo.model.financialmgmt.accounting.coa.AcctSchema schema = OBDal.getInstance()
              .get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
                  as.m_C_AcctSchema_ID);
          log4j.error("No Account Asset for product: " + product.getName()
              + " in accounting schema: " + schema.getName());
        }
        /*
         * if (!isConvertible(as, conn)) { setMessageResult(conn, STATUS_NotConvertible, "error",
         * null); throw new IllegalStateException(); }
         */
        if (CostingStatus.getInstance().isMigrated() && line.transaction != null
            && !line.transaction.isCostCalculated()) {
          Map<String, String> parameters = getNotCalculatedCostParameters(line.transaction);
          setMessageResult(conn, STATUS_NotCalculatedCost, "error", parameters);
          throw new IllegalStateException();
        } else if (CostingStatus.getInstance().isMigrated() && line.transaction == null) {
          // Check default cost existence
          HashMap<CostDimension, BaseOBObject> costDimensions = CostingUtils.getEmptyDimensions();
          costDimensions.put(CostDimension.Warehouse, line.getWarehouse());
          if (!CostingUtils.hasStandardCostDefinition(product, legalEntity, dateAcct,
              costDimensions)) {
            Map<String, String> parameters = getInvalidCostParameters(product.getIdentifier(),
                DateAcct);
            setMessageResult(conn, STATUS_InvalidCost, "error", parameters);
            throw new IllegalStateException();
          }
        }
        String costs = line.getProductCosts(DateAcct, as, conn, con);

        if (matReturn) {
          log4jDocInOut.debug("(MatShipmentReturn) - DR account: "
              + line.getAccount(ProductInfo.ACCTTYPE_P_Cogs, as, conn));
          log4jDocInOut.debug("(MatShipmentReturn) - DR costs: " + costs);
        } else {
          log4jDocInOut.debug("(MatShipment) - DR account: "
              + line.getAccount(ProductInfo.ACCTTYPE_P_Cogs, as, conn));
          log4jDocInOut.debug("(MatShipment) - DR costs: " + costs);
        }
        BigDecimal b_Costs = new BigDecimal(costs).multiply(new BigDecimal(line.getBreakdownQty()))
            .divide(new BigDecimal(line.m_qty), standardPrecision, RoundingMode.HALF_UP);
        String strCosts = b_Costs.toString();
        if (b_Costs.compareTo(BigDecimal.ZERO) == 0 && !CostingStatus.getInstance().isMigrated()
            && DocInOutData.existsCost(conn, DateAcct, line.m_M_Product_ID).equals("0")) {
          Map<String, String> parameters = getInvalidCostParameters(product.getIdentifier(),
              DateAcct);
          setMessageResult(conn, STATUS_InvalidCost, "error", parameters);
          throw new IllegalStateException();
        }
        // CoGS DR

        dr = fact.createLine(line, cogsAccount, costCurrency.getId(), strCosts, "",
            Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);

        if (dr != null) {
          dr.setM_Locator_ID(line.m_M_Locator_ID);
          dr.setLocationFromLocator(line.m_M_Locator_ID, true, conn); // from
          // Loc
          dr.setLocationFromBPartner(C_BPartner_Location_ID, false, conn); // to
          dr.descripcion = "COSTO DE VENTAS " + periodo + "# ";

        }

        if (isconsumomatprimainmueble) {
          // PASS THE CoGS to Material Expense

          Account matExpenseAccount = line.getAccount(ProductInfo.ACCTTYPE_P_MatExpense, as, conn);
          Account matExpBridgeAccount = line.getAccount(ProductInfo.ACCTTYPE_P_MatExpBridge, as,
              conn);
          if (matExpenseAccount != null && matExpBridgeAccount != null) {
            DocLine docLine1 = new DocLine(line);
            docLine1.m_AmtAcctCr = line.m_AmtAcctCr;
            docLine1.m_AmtAcctDr = line.m_AmtAcctDr;

            FactLine fl1 = fact.createLine(docLine1, matExpenseAccount, costCurrency.getId(),
                strCosts, "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
            if (fl1 != null) {
              fl1.setM_Locator_ID(line.m_M_Locator_ID);
              fl1.setLocationFromLocator(line.m_M_Locator_ID, true, conn); // from
              // Loc
              fl1.setLocationFromBPartner(C_BPartner_Location_ID, false, conn); // to
              fl1.descripcion = "COSTO DE VENTAS " + periodo + "# ";
            }

            DocLine docLine2 = new DocLine(line);
            docLine2.m_AmtAcctCr = line.m_AmtAcctDr;
            docLine2.m_AmtAcctDr = line.m_AmtAcctCr;

            FactLine fl2 = fact.createLine(docLine2, matExpBridgeAccount, costCurrency.getId(), "",
                strCosts, Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
            if (fl2 != null) {
              fl2.setM_Locator_ID(line.m_M_Locator_ID);
              fl2.setLocationFromLocator(line.m_M_Locator_ID, true, conn); // from
              // Loc
              fl2.setLocationFromBPartner(C_BPartner_Location_ID, false, conn); // to
              fl2.descripcion = "COSTO DE VENTAS " + periodo + "# ";
            }
          }
        }

        // Loc
        if (matReturn) {
          log4jDocInOut.debug("(MatShipmentReturn) - CR account: "
              + line.getAccount(ProductInfo.ACCTTYPE_P_Asset, as, conn));
          log4jDocInOut.debug("(MatShipmentReturn) - CR costs: " + strCosts);
        } else {
          log4jDocInOut.debug("(MatShipment) - CR account: "
              + line.getAccount(ProductInfo.ACCTTYPE_P_Asset, as, conn));
          log4jDocInOut.debug("(MatShipment) - CR costs: " + strCosts);
        }
        // Inventory CR
        cr = fact.createLine(line, assetAccount, costCurrency.getId(), "", strCosts,
            Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
        if (cr != null) {
          cr.setM_Locator_ID(line.m_M_Locator_ID);
          cr.setLocationFromLocator(line.m_M_Locator_ID, true, conn); // from
          // Loc
          cr.setLocationFromBPartner(C_BPartner_Location_ID, false, conn); // to
          // Loc
          cr.descripcion = "COSTO DE VENTAS " + periodo + "# ";

        }

        if (lines.size() > 0 && lines.get(0).isDeferred()
            && lines.get(0).isScoIsdeferredonreceipt()) {

          InvoiceLine invoiceline = lines.get(0);

          DocLineInvoiceData dataline = null;
          try {
            DocLineInvoiceData[] data = DocLineInvoiceData.select(connectionProvider,
                invoiceline.getInvoice().getId());
            for (int j = 0; j < data.length; j++)
              if (data[j].cInvoicelineId.equals(invoiceline.getId()))
                dataline = data[j];
          } catch (ServletException e) {

          }

          DocLine_Invoice planDocLine = new DocLine_Invoice(AcctServer.DOCTYPE_ARInvoice,
              invoiceline.getInvoice().getId(), invoiceline.getId());
          planDocLine.loadAttributes(dataline, this);
          planDocLine.m_AD_Org_ID = invoiceline.getOrganization().getId();
          planDocLine.m_C_Currency_ID = invoiceline.getInvoice().getCurrency().getId();
          String strQty = dataline.qtyinvoiced;
          planDocLine.setQty(strQty);
          String LineNetAmt = dataline.linenetamt;
          String PriceList = dataline.pricelist;
          planDocLine.setAmount(LineNetAmt, PriceList, strQty);

          Account account = planDocLine.getAccount(ProductInfo.ACCTTYPE_P_Revenue, as, conn);
          Account accountDef = planDocLine.getAccount(ProductInfo.ACCTTYPE_P_DefRevenue, as, conn);

          // only the first //convert para ajuste

          BigDecimal amountline = invoiceline.getUnitPrice().multiply(new BigDecimal(line.m_qty));
          String amountConvertedInvLine = null;
          // a soles
          ConversionRateDoc conversionRateCurrentDoc = getConversionRateDoc(TABLEID_Invoice,
              invoiceline.getInvoice().getId(), invoiceline.getInvoice().getCurrency().getId(),
              as.m_C_Currency_ID);
          if (conversionRateCurrentDoc != null) {
            amountConvertedInvLine = applyRate(amountline, conversionRateCurrentDoc, true)
                .setScale(2, BigDecimal.ROUND_HALF_UP).toString();
          } else {
            amountConvertedInvLine = getConvertedAmt(amountline.toString(),
                invoiceline.getInvoice().getCurrency().getId(), as.m_C_Currency_ID,
                OBDateUtils.formatDate(getDateForInvoiceReference(invoiceline.getInvoice())), "",
                AD_Client_ID, AD_Org_ID, conn);
          }

          Vector<BigDecimal> vTcOut = new Vector<BigDecimal>();
          // convertAmount(amountline, false, DateAcct, TABLEID_Invoice,
          // invoiceline.getInvoice().getId(), C_Currency_ID, as.m_C_Currency_ID, line, as, fact,
          // Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn, true, true, true, vTcOut).toString();

          // Deferred Revenue Account
          FactLine factline = fact.createLine(planDocLine, accountDef, as.m_C_Currency_ID,
              amountConvertedInvLine.toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
              AcctServer.DOCTYPE_ARInvoice,
              OBDateUtils.formatDate(getDateForInvoiceReference(invoiceline.getInvoice())), null,
              conn);
          factline.setRecordId3(invoiceline.getInvoice().getId());
          factline.descripcion = "Reversion de Invoice: "
              + invoiceline.getInvoice().getScrPhysicalDocumentno() + " /"
              + invoiceline.getInvoice().getDocumentNo();
          // Revenue Account
          factline = fact.createLine(planDocLine, account, as.m_C_Currency_ID, "",
              amountConvertedInvLine.toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
              AcctServer.DOCTYPE_ARInvoice,
              OBDateUtils.formatDate(getDateForInvoiceReference(invoiceline.getInvoice())), null,
              conn);
          factline.setRecordId3(invoiceline.getInvoice().getId());
          factline.descripcion = "Reversion de Invoice: "
              + invoiceline.getInvoice().getScrPhysicalDocumentno() + " /"
              + invoiceline.getInvoice().getDocumentNo();

        }

      }
    }
    // Purchasing
    else if (DocumentType.equals(AcctServer.DOCTYPE_MatReceipt)) {

      // ACA SOLO LLEGA SI ES OTHER RECEIPT ( PRUEBA COAM LLEGA PARA INGRESOS)

      for (int i = 0; p_lines != null && i < p_lines.length; i++) {
        DocLine_Material line = (DocLine_Material) p_lines[i];
        Product product = OBDal.getInstance().get(Product.class, line.m_M_Product_ID);
        Organization legalEntity = OBContext.getOBContext()
            .getOrganizationStructureProvider(AD_Client_ID)
            .getLegalEntity(OBDal.getInstance().get(Organization.class, line.m_AD_Org_ID));
        Currency costCurrency = FinancialUtils.getLegalEntityCurrency(legalEntity);
        if (!CostingStatus.getInstance().isMigrated()) {
          costCurrency = OBDal.getInstance().get(Client.class, AD_Client_ID).getCurrency();
        } else if (line.transaction != null && line.transaction.getCurrency() != null) {
          costCurrency = line.transaction.getCurrency();
        }
        C_Currency_ID = costCurrency.getId();
        int standardPrecision = 2;
        OBContext.setAdminMode(false);
        try {
          standardPrecision = costCurrency.getStandardPrecision().intValue();
        } finally {
          OBContext.restorePreviousMode();
        }
        String costs = "0";
        String strCosts = "0";
        /*
         * if (!isConvertible(as, conn)) { setMessageResult(conn, STATUS_NotConvertible, "error",
         * null); throw new IllegalStateException(); }
         */
        if (product.isBookUsingPurchaseOrderPrice()) {
          // If the Product is checked as book using PO Price, the Price of the Purchase Order will
          // be used to create the FactAcct Line
          ShipmentInOutLine inOutLine = OBDal.getInstance().get(ShipmentInOutLine.class,
              line.m_TrxLine_ID);
          OrderLine ol = inOutLine.getSalesOrderLine();
          if (ol == null) {
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("product", inOutLine.getProduct().getIdentifier());
            parameters.put("line", inOutLine.getLineNo().toString());
            setMessageResult(conn, STATUS_NoRelatedPO, "error", parameters);
            throw new IllegalStateException();
          }
          costs = ol.getUnitPrice().multiply(new BigDecimal(line.getBreakdownQty())).toString();
          BigDecimal b_Costs = new BigDecimal(costs).setScale(standardPrecision,
              RoundingMode.HALF_UP);
          strCosts = b_Costs.toString();
        } else {
          // If the Product is not checked as book using PO Price, the Cost of the
          // Transaction will be used to create the FactAcct Line
          if (CostingStatus.getInstance().isMigrated() && line.transaction != null
              && !line.transaction.isCostCalculated()) {
            Map<String, String> parameters = getNotCalculatedCostParameters(line.transaction);
            setMessageResult(conn, STATUS_NotCalculatedCost, "error", parameters);
            throw new IllegalStateException();
          } else if (CostingStatus.getInstance().isMigrated() && line.transaction == null) {
            // Check default cost existence
            HashMap<CostDimension, BaseOBObject> costDimensions = CostingUtils.getEmptyDimensions();
            costDimensions.put(CostDimension.Warehouse, line.getWarehouse());
            if (!CostingUtils.hasStandardCostDefinition(product, legalEntity, dateAcct,
                costDimensions)) {
              Map<String, String> parameters = getInvalidCostParameters(product.getIdentifier(),
                  DateAcct);
              setMessageResult(conn, STATUS_InvalidCost, "error", parameters);
              throw new IllegalStateException();
            }
          }
          costs = line.getProductCosts(DateAcct, as, conn, con);

          BigDecimal b_Costs = new BigDecimal(costs)
              .multiply(new BigDecimal(line.getBreakdownQty()))
              .divide(new BigDecimal(line.m_qty), standardPrecision, RoundingMode.HALF_UP);

          if (b_Costs.compareTo(BigDecimal.ZERO) == 0 && !CostingStatus.getInstance().isMigrated()
              && DocInOutData.existsCost(conn, DateAcct, line.m_M_Product_ID).equals("0")) {
            Map<String, String> parameters = getInvalidCostParameters(product.getIdentifier(),
                DateAcct);
            setMessageResult(conn, STATUS_InvalidCost, "error", parameters);
            throw new IllegalStateException();
          }

          ShipmentInOutLine inOutLine = OBDal.getInstance().get(ShipmentInOutLine.class,
              line.m_TrxLine_ID);
          // si es por servicio restar el servicio
          if (inOutLine.getSreServiceInOrderLine() != null
              && inOutLine.getSreServiceInOrderLine().getInvoiceLineList().size() > 0) {
            List<InvoiceLine> lsLines = inOutLine.getSreServiceInOrderLine().getInvoiceLineList();
            BigDecimal bgAmt = new BigDecimal(0);
            for (int jk = 0; jk < lsLines.size(); jk++) {
              bgAmt = bgAmt.add(lsLines.get(jk).getLineNetAmount());
            }
            b_Costs = b_Costs.subtract(bgAmt);

          } else if (inOutLine.getSreServiceInOrderLine() != null
              && inOutLine.getSreServiceInOrderLine().getInvoiceLineList().size() == 0) {
            Map<String, String> parameters = new HashMap<String, String>();
            parameters.put("product", inOutLine.getProduct().getIdentifier());
            parameters.put("line", inOutLine.getLineNo().toString());

            setMessageResult(conn, STATUS_InvalidCost, "error", parameters);
            throw new IllegalStateException();
          }

          strCosts = b_Costs.toString();

        }
        Account notInvoicedReceiptsAccount = null;

        if (ordertypecat_val.equals("OtherOrdersforReceipt")) {
          notInvoicedReceiptsAccount = line.getAccount(ProductInfo.ACCTTYPE_P_OtherRevenue, as,
              conn);
        } else {
          notInvoicedReceiptsAccount = line.getAccount(ProductInfo.ACCTTYPE_P_StockVariance, as,
              conn);
        }

        if (notInvoicedReceiptsAccount == null) {
          org.openbravo.model.financialmgmt.accounting.coa.AcctSchema schema = OBDal.getInstance()
              .get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
                  as.m_C_AcctSchema_ID);
          log4j.error("No Account Not Invoiced Receipts for product: " + product.getName()
              + " in accounting schema: " + schema.getName());
        }

        Account assetAccount = null;
        if (inOut.getSCODiverseAccountGLItem() == null) {
          assetAccount = line.getAccount(ProductInfo.ACCTTYPE_P_Asset, as, conn);
        } else {
          assetAccount = getAccountGLItem(inOut.getSCODiverseAccountGLItem(), as, false, conn);
        }

        if (assetAccount == null) {
          org.openbravo.model.financialmgmt.accounting.coa.AcctSchema schema = OBDal.getInstance()
              .get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
                  as.m_C_AcctSchema_ID);
          log4j.error("No Account Asset for product: " + product.getName()
              + " in accounting schema: " + schema.getName());
        }
        // If there exists cost for the product, but it is equals to zero, then no line is added,
        // but no error is thrown. If this is the only line in the document, yes an error will be
        // thrown
        if (!costs.equals("0")
            || DocInOutData.existsCost(conn, DateAcct, line.m_M_Product_ID).equals("0")) {

          log4jDocInOut.debug("(matReceipt) - DR account: " + assetAccount);
          log4jDocInOut.debug("(matReceipt) - DR costs: " + strCosts);
          // Inventory DR
          dr = fact.createLine(line, assetAccount, costCurrency.getId(), strCosts, "",
              Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);

          if (!getStatus().equals("i")) {
            if (dr != null) {
              dr.setM_Locator_ID(line.m_M_Locator_ID);
              dr.setLocationFromBPartner(C_BPartner_Location_ID, true, conn); // from
              // Loc
              dr.setLocationFromLocator(line.m_M_Locator_ID, false, conn); // to
              // Loc
            }
            log4jDocInOut.debug("(matReceipt) - CR account: "
                + line.getAccount(AcctServer.ACCTTYPE_NotInvoicedReceipts, as, conn));
            log4jDocInOut.debug("(matReceipt) - CR costs: " + strCosts);
            // NotInvoicedReceipt CR

            cr = fact.createLine(line, notInvoicedReceiptsAccount, costCurrency.getId(), "",
                strCosts, Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);

            if (cr != null) {
              cr.setM_Locator_ID(line.m_M_Locator_ID);
              cr.setLocationFromBPartner(C_BPartner_Location_ID, true, conn); // from
              // Loc
              cr.setLocationFromLocator(line.m_M_Locator_ID, false, conn); // to
              // Loc
            }
          }
        }

        // there is defer accounts in invoice?
        /*
         * ShipmentInOutLine shipmentInOutLine = OBDal.getInstance().get(ShipmentInOutLine.class,
         * line.m_TrxLine_ID); OBCriteria<ReceiptInvoiceMatch> obCriteria =
         * OBDal.getInstance().createCriteria(ReceiptInvoiceMatch.class);
         * obCriteria.add(Restrictions.eq(ReceiptInvoiceMatch.PROPERTY_GOODSSHIPMENTLINE,
         * shipmentInOutLine)); List<ReceiptInvoiceMatch> matchlines = obCriteria.list();
         */

        /*
         * if (matchlines.size() > 0) { InvoiceLine invoiceline =
         * matchlines.get(0).getInvoiceLine();
         */

        ShipmentInOutLine shipmentInOutLine = OBDal.getInstance().get(ShipmentInOutLine.class,
            line.m_TrxLine_ID);
        OBCriteria<InvoiceLine> obCriteria = OBDal.getInstance().createCriteria(InvoiceLine.class);
        obCriteria.add(Restrictions.eq(InvoiceLine.PROPERTY_GOODSSHIPMENTLINE, shipmentInOutLine));
        List<InvoiceLine> lines = obCriteria.list();

        if (lines.size() == 0) {// search by order
          OrderLine orderLine = shipmentInOutLine.getSalesOrderLine();
          if (orderLine != null) {
            obCriteria = OBDal.getInstance().createCriteria(InvoiceLine.class);
            obCriteria.add(Restrictions.eq(InvoiceLine.PROPERTY_SALESORDERLINE, orderLine));
            lines = obCriteria.list();
          }
        }

        /*
         * if (lines.size() > 0) { InvoiceLine invoiceline = lines.get(0); if
         * (invoiceline.isDeferred() && invoiceline.isScoIsdeferredonreceipt()) {
         * 
         * DocLineInvoiceData dataline = null; try { DocLineInvoiceData[] data =
         * DocLineInvoiceData.select(connectionProvider, invoiceline.getInvoice().getId()); for (int
         * j = 0; j < data.length; j++) if (data[j].cInvoicelineId.equals(invoiceline.getId()))
         * dataline = data[j]; } catch (ServletException e) {
         * 
         * } DocLine_Invoice planDocLine = new DocLine_Invoice(AcctServer.DOCTYPE_APInvoice,
         * invoiceline.getInvoice().getId(), invoiceline.getId());
         * planDocLine.loadAttributes(dataline, this); planDocLine.m_AD_Org_ID =
         * invoiceline.getOrganization().getId(); planDocLine.m_C_Currency_ID =
         * invoiceline.getInvoice().getCurrency().getId(); String strQty = dataline.qtyinvoiced;
         * planDocLine.setQty(strQty); String LineNetAmt = dataline.linenetamt; String PriceList =
         * dataline.pricelist; planDocLine.setAmount(LineNetAmt, PriceList, strQty);
         * 
         * Account account = planDocLine.getAccount(ProductInfo.ACCTTYPE_P_Expense, as, conn);
         * Account accountDef = planDocLine.getAccount(ProductInfo.ACCTTYPE_P_DefExpense, as, conn);
         * 
         * BigDecimal amountline = invoiceline.getUnitPrice().multiply(new
         * BigDecimal(line.getBreakdownQty()));
         * 
         * Vector<BigDecimal> vTcOut = new Vector<BigDecimal>(); convertAmount(amountline, true,
         * DateAcct, TABLEID_Invoice, invoiceline.getInvoice().getId(), C_Currency_ID,
         * as.m_C_Currency_ID, line, as, fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn, true,
         * true, true, vTcOut).toString();
         * 
         * 
         * // Deferred Revenue Account FactLine factline = fact.createLine(planDocLine, accountDef,
         * planDocLine.m_C_Currency_ID, amountline.toString(), "", Fact_Acct_Group_ID,
         * nextSeqNo(SeqNo), AcctServer.DOCTYPE_APInvoice,
         * OBDateUtils.formatDate(invoiceline.getInvoice().getAccountingDate()), vTcOut.get(0),
         * conn); factline.descripcion = "Reversion de Invoice/Factura: " +
         * invoiceline.getInvoice().getScrPhysicalDocumentno() + " /*" +
         * invoiceline.getInvoice().getDocumentNo();
         * 
         * // Revenue Account factline = fact.createLine(planDocLine, account,
         * planDocLine.m_C_Currency_ID, "", amountline.toString(), Fact_Acct_Group_ID,
         * nextSeqNo(SeqNo), AcctServer.DOCTYPE_APInvoice,
         * OBDateUtils.formatDate(invoiceline.getInvoice().getAccountingDate()), vTcOut.get(0),
         * conn); factline.descripcion = "Reversion de Invoice/Factura: " +
         * invoiceline.getInvoice().getScrPhysicalDocumentno() + " /*" +
         * invoiceline.getInvoice().getDocumentNo();
         * 
         * } }
         */

      }
    } else {
      log4jDocInOut.warn("createFact - " + "DocumentType unknown: " + DocumentType);
      return null;
    }

    //
    SeqNo = "0";
    return fact;
  } // createFact

  /**
   * @return the log4jDocInOut
   */
  public static Logger getLog4jDocInOut() {
    return log4jDocInOut;
  }

  /**
   * @param log4jDocInOut
   *          the log4jDocInOut to set
   */
  public static void setLog4jDocInOut(Logger log4jDocInOut) {
    DocInOut.log4jDocInOut = log4jDocInOut;
  }

  /**
   * @return the seqNo
   */
  public String getSeqNo() {
    return SeqNo;
  }

  /**
   * @param seqNo
   *          the seqNo to set
   */
  public void setSeqNo(String seqNo) {
    SeqNo = seqNo;
  }

  /**
   * @return the serialVersionUID
   */
  public static long getSerialVersionUID() {
    return serialVersionUID;
  }

  public String nextSeqNo(String oldSeqNo) {
    log4jDocInOut.debug("DocInOut - oldSeqNo = " + oldSeqNo);
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    log4jDocInOut.debug("DocInOut - nextSeqNo = " + SeqNo);
    return SeqNo;
  }

  /**
   * Get Document Confirmation
   * 
   * not used
   */
  public boolean getDocumentConfirmation(ConnectionProvider conn, String strRecordId) {
    try {
      DocLineInOutData[] data = DocLineInOutData.select(conn, Record_ID);
      ShipmentInOut inOut = OBDal.getInstance().get(ShipmentInOut.class, strRecordId);
      String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("dateFormat.java");
      SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);
      String strDateAcct = outputFormat.format(inOut.getAccountingDate());

      if (inOut.getPosted().equals("sco_M")) {
        strMessage = "No se puede contabilizar Datos de Migracion";
        setStatus(STATUS_Migrated);
        return false;
      }

      /*
       * Tipos que no tienen contabilidad
       * 
       * ReposicionEntreAlmacenesOut ReposicionPorConsignacionOut SalidaProductosTransito
       * ReposicionPorMinaOut ReposicionEntreAlmacenes ReposicionPorConsignacion
       * IngresoProductosTransito ReposicionPorMina IngresoDevolucionConsignacion
       * 
       * SalidaPorMuestra DevolucionPorMuestra SalidaDevoluciónMuestras IngresoMuestras
       * 
       * Todos los order type con la categoria TipoTransferenciaDirecta
       */
      String ordertypecat = inOut.getSWAUpdateReason().getComboCategory().getSearchKey();
      if (ordertypecat.equals("TipoTransferenciaDirecta")
          || inOut.getSsaCmbitemValue().equals("ReposicionEntreAlmacenesOut")
          || inOut.getSsaCmbitemValue().equals("ReposicionPorConsignacionOut")
          || inOut.getSsaCmbitemValue().equals("SalidaProductosTransito")
          || inOut.getSsaCmbitemValue().equals("ReposicionPorMinaOut")
          || inOut.getSsaCmbitemValue().equals("ReposicionEntreAlmacenes")
          || inOut.getSsaCmbitemValue().equals("ReposicionPorConsignacion")
          || inOut.getSsaCmbitemValue().equals("IngresoProductosTransito")
          || inOut.getSsaCmbitemValue().equals("ReposicionPorMina")
          || inOut.getSsaCmbitemValue().equals("IngresoDevolucionConsignacion")
          || inOut.getSsaCmbitemValue().equals("SalidaPorMuestra")
          || inOut.getSsaCmbitemValue().equals("DevolucionPorMuestra")
          || inOut.getSsaCmbitemValue().equals("SalidaDevoluciónMuestras")
          || inOut.getSsaCmbitemValue().equals("IngresoMuestras")
      // se contabiliza con espejos en la factura
      // || inOut.getSsaCmbitemValue().equals("CompraNacional")
      // || inOut.getSsaCmbitemValue().equals("CompraImportacion")
      // || inOut.getSsaCmbitemValue().equals("DevolucionProveedor")
      ) {
        setStatus(STATUS_DocumentDisabled);
        return false;
      }

      int validLines = 0;
      for (int i = 0; i < data.length; i++) {
        BigDecimal trxCost = null;
        if (CostingStatus.getInstance().isMigrated()) {
          OBContext.setAdminMode(false);
          try {
            // Get related M_Transaction_ID
            ShipmentInOutLine inOutLine = OBDal.getInstance().get(ShipmentInOutLine.class,
                data[i].mInoutlineId);
            if (inOutLine.getProduct() == null) {
              continue;
            }
            MaterialTransaction trx = null;
            if (inOutLine.getMaterialMgmtMaterialTransactionList().size() > 0) {
              trx = inOutLine.getMaterialMgmtMaterialTransactionList().get(0);
              trxCost = trx.getTransactionCost();
            } else {
              if (inOutLine.getProduct().isBookUsingPurchaseOrderPrice()) {
                // Not stocked item type product.
                // If the Product is checked as book using Purchase Order Price, the Price of the PO
                // will be used to create the FactAcct Line, therefore a related PO must exist
                OrderLine ol = inOutLine.getSalesOrderLine();
                if (ol == null) {
                  Map<String, String> parameters = new HashMap<String, String>();
                  parameters.put("product", inOutLine.getProduct().getIdentifier());
                  parameters.put("line", inOutLine.getLineNo().toString());
                  setMessageResult(conn, STATUS_NoRelatedPO, "error", parameters);
                  throw new IllegalStateException();
                }
                trxCost = ol.getLineNetAmount();
              } else {
                // Not stocked item type product. Check standard cost existence.
                // If the Product is not checked as book using PO Price, the Cost of the
                // Transaction will be used to create the FactAcct Line, therefore the Cost of the
                // Transaction must have been calculated before.
                Organization legalEntity = OBContext.getOBContext()
                    .getOrganizationStructureProvider(AD_Client_ID)
                    .getLegalEntity(inOut.getOrganization());
                HashMap<CostDimension, BaseOBObject> costDimensions = CostingUtils
                    .getEmptyDimensions();
                if (inOutLine.getStorageBin() == null) {
                  costDimensions.put(CostDimension.Warehouse,
                      inOutLine.getShipmentReceipt().getWarehouse());
                } else {
                  costDimensions.put(CostDimension.Warehouse,
                      inOutLine.getStorageBin().getWarehouse());
                }
                if (!CostingUtils.hasStandardCostDefinition(inOutLine.getProduct(), legalEntity,
                    inOut.getAccountingDate(), costDimensions)) {
                  Map<String, String> parameters = getInvalidCostParameters(
                      inOutLine.getProduct().getIdentifier(), DateAcct);
                  setMessageResult(conn, STATUS_InvalidCost, "error", parameters);
                  throw new IllegalStateException();
                } else {
                  trxCost = CostingUtils
                      .getStandardCost(inOutLine.getProduct(), legalEntity,
                          inOut.getAccountingDate(), costDimensions, legalEntity.getCurrency())
                      .multiply(new BigDecimal(data[i].breakdownqty));
                }
              }
            }
            if (trxCost == null) {
              Map<String, String> parameters = getNotCalculatedCostParameters(trx);
              setMessageResult(conn, STATUS_NotCalculatedCost, "error", parameters);
              setStatus(STATUS_NotCalculatedCost);
              return false;
            }
          } finally {
            OBContext.restorePreviousMode();
          }
        } else {
          trxCost = new BigDecimal(ProductInfoData.selectProductAverageCost(conn,
              data[i].getField("mProductId"), strDateAcct));
          if (trxCost == null || trxCost.signum() == 0) {
            ShipmentInOutLine inOutLine = OBDal.getInstance().get(ShipmentInOutLine.class,
                data[i].mInoutlineId);
            if (inOutLine.getProduct() == null) {
              continue;
            }
            Map<String, String> parameters = getInvalidCostParameters(
                inOutLine.getProduct().getIdentifier(), strDateAcct);
            setMessageResult(conn, STATUS_InvalidCost, "error", parameters);
            throw new IllegalStateException();
          }
        }

        if (trxCost != null && trxCost.signum() != 0) {
          validLines++;
        }
      }
      if (validLines == 0) {
        setStatus(STATUS_DocumentDisabled);
        return false;
      }
    } catch (ServletException e) {
      log4j.error("Servlet Exception in document confirmation", e);
      return false;
    }
    return true;
  }

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method

}
