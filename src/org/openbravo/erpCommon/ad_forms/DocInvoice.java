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
 * Contributions are Copyright (C) 2001-2014 Openbravo S.L.U.
 ******************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.AccDefUtility;
import org.openbravo.erpCommon.utility.CashVATUtil;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.currency.ConversionRateDoc;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.financialmgmt.calendar.Period;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.tax.TaxRate;

import pe.com.unifiedgo.core.data.SCRComboItem;

public class DocInvoice extends AcctServer {
  private static final long serialVersionUID = 1L;
  static Logger log4jDocInvoice = Logger.getLogger(DocInvoice.class);

  DocTax[] m_taxes = null;
  DocLine_FinPaymentSchedule[] m_payments = null;

  public DocLine_FinPaymentSchedule[] getM_payments() {
    return m_payments;
  }

  DocLine[] p_lines_taxes = null;

  boolean isCashVAT = false;

  String SeqNo = "0";

  /**
   * Constructor
   * 
   * @param AD_Client_ID
   *          AD_Client_ID
   */
  public DocInvoice(String AD_Client_ID, String AD_Org_ID, ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  public void loadObjectFieldProvider(ConnectionProvider conn, String stradClientId, String Id)
      throws ServletException {
    setObjectFieldProvider(DocInvoiceData.selectRegistro(conn, stradClientId, Id));
  }

  public boolean loadDocumentDetails(FieldProvider[] data, ConnectionProvider conn) {
    DateDoc = data[0].getField("DateNewInvoiced");
    TaxIncluded = data[0].getField("IsTaxIncluded");
    C_BPartner_Location_ID = data[0].getField("C_BPartner_Location_ID");
    // Amounts
    Amounts[AMTTYPE_Gross] = data[0].getField("GrandTotal");
    if (Amounts[AMTTYPE_Gross] == null)
      Amounts[AMTTYPE_Gross] = "0";
    Amounts[AMTTYPE_Net] = data[0].getField("TotalLines");
    if (Amounts[AMTTYPE_Net] == null)
      Amounts[AMTTYPE_Net] = "0";
    Amounts[AMTTYPE_Charge] = data[0].getField("ChargeAmt");
    if (Amounts[AMTTYPE_Charge] == null)
      Amounts[AMTTYPE_Charge] = "0";

    loadDocumentType(); // lines require doc type
    // Contained Objects
    p_lines = loadLines();
    m_taxes = loadTaxes();
    m_payments = loadPayments();
    m_debt_payments = loadDebtPayments();
    isCashVAT = true; // Allows cashvat //StringUtils.equals("Y",
    // data[0].getField("iscashvat"));
    return true;

  }

  private DocLine[] loadLines() {
    ArrayList<Object> list = new ArrayList<Object>();
    DocLineInvoiceData[] data = null;
    try {
      log4jDocInvoice.debug("############### groupLines = " + groupLines);
      if (groupLines.equals("Y"))
        data = DocLineInvoiceData.selectTotal(connectionProvider, Record_ID);
      else
        data = DocLineInvoiceData.select(connectionProvider, Record_ID);
    } catch (ServletException e) {
      log4jDocInvoice.warn(e);
    }
    if (data == null || data.length == 0)
      return null;
    for (int i = 0; i < data.length; i++) {
      String Line_ID = data[i].cInvoicelineId;
      DocLine_Invoice docLine = new DocLine_Invoice(DocumentType, Record_ID, Line_ID);
      docLine.loadAttributes(data[i], this);
      docLine.setCustomGlDeferId(data[i].emScoAutoAccGlitemId);
      String strQty = data[i].qtyinvoiced;
      docLine.setQty(strQty);

      String isnoemitido = data[i].isnoemitido;
      docLine.setIsNoEmitido(isnoemitido);

      String LineNetAmt = data[i].linenetamt;
      String PriceList = data[i].pricelist;
      docLine.setAmount(LineNetAmt, PriceList, strQty);
      // Accruals & Deferrals for revenue products
      docLine.setIsDeferred("Y".equals(data[i].isdeferred));
      docLine.setDefPlanType(data[i].defplantype);
      docLine.setPeriodNumber(
          !"".equals(data[i].periodnumber) ? new Integer(data[i].periodnumber) : 0);
      docLine.setStartingPeriodId(data[i].cPeriodId);
      docLine.setIsDeferredOnReceipt("Y".equals(data[i].isdeferredonreceipt));
      list.add(docLine);
    }
    // Return Array
    DocLine[] dl = new DocLine[list.size()];
    list.toArray(dl);
    return dl;
  } // loadLines

  private DocTax[] loadTaxes() {
    ArrayList<Object> list = new ArrayList<Object>();
    DocInvoiceData[] data = null;
    try {
      data = DocInvoiceData.select(connectionProvider, Record_ID);
    } catch (ServletException e) {
      log4jDocInvoice.warn(e);
    }
    log4jDocInvoice.debug("############### Taxes.length = " + data.length);
    for (int i = 0; i < data.length; i++) {
      String C_Tax_ID = data[i].cTaxId;
      String name = data[i].name;
      String rate = data[i].rate;

      String taxBaseAmt = data[i].taxbaseamt;
      String amount = data[i].taxamt;
      boolean isTaxDeductable = false;
      boolean isTaxUndeductable = ("Y".equals(data[i].ratetaxundeductable))
          || ("Y".equals(data[i].orgtaxundeductable));
      if ("Y".equals(data[i].orgtaxundeductable)) {
        /*
         * If any tax line level has tax deductable flag then override isTaxUndeductable flag for
         * intracommunity non tax deductible organization
         */
        if ("Y".equals(data[i].istaxdeductable)) {
          isTaxUndeductable = false;
          isTaxDeductable = true;
        }
      } else {
        // configured for intracommunity with tax liability
        if ("Y".equals(data[i].istaxdeductable)) {
          isTaxDeductable = true;
        }
      }

      boolean taxIsCashVAT = StringUtils.equals(data[i].iscashvat, "Y");

      DocTax taxLine = new DocTax(C_Tax_ID, name, rate, taxBaseAmt, amount, isTaxUndeductable,
          isTaxDeductable, taxIsCashVAT);
      list.add(taxLine);
    }
    // Return Array
    DocTax[] tl = new DocTax[list.size()];
    list.toArray(tl);
    return tl;
  } // loadTaxes

  private DocLine_Payment[] loadDebtPayments() {
    ArrayList<Object> list = new ArrayList<Object>();
    DocInvoiceData[] data = null;
    try {
      data = DocInvoiceData.selectDebtPayments(connectionProvider, Record_ID);
      log4jDocInvoice.debug("############### DebtPayments.length = " + data.length);
      for (int i = 0; i < data.length; i++) {
        //
        String Line_ID = data[i].cDebtPaymentId;
        DocLine_Payment dpLine = new DocLine_Payment(DocumentType, Record_ID, Line_ID);
        log4jDocInvoice.debug(" dpLine.m_Record_Id2 = " + data[i].cDebtPaymentId);
        dpLine.m_Record_Id2 = data[i].cDebtPaymentId;
        dpLine.C_Currency_ID_From = data[i].cCurrencyId;
        dpLine.dpStatus = data[i].status;
        dpLine.isReceipt = data[i].isreceipt;
        dpLine.isPaid = data[i].ispaid;
        dpLine.isManual = data[i].ismanual;
        dpLine.WriteOffAmt = data[i].writeoffamt;
        dpLine.Amount = data[i].amount;
        list.add(dpLine);
      }
    } catch (ServletException e) {
      log4jDocInvoice.warn(e);
    }

    // Return Array
    DocLine_Payment[] tl = new DocLine_Payment[list.size()];
    list.toArray(tl);
    return tl;
  } // loadDebtPayments

  private DocLine_FinPaymentSchedule[] loadPayments() {

    ArrayList<Object> list = new ArrayList<Object>();
    DocInvoiceData[] data = null;
    try {
      data = DocInvoiceData.selectPayments(connectionProvider, Record_ID);
      log4jDocInvoice.debug("############### DebtPayments.length = " + data.length);
      for (int i = 0; i < data.length; i++) {
        //
        String Line_ID = data[i].finPaymentScheduleId;
        DocLine_FinPaymentSchedule dpLine = new DocLine_FinPaymentSchedule(DocumentType, Record_ID,
            Line_ID);
        log4jDocInvoice.debug(" dpLine.m_Record_Id2 = " + data[i].finPaymentScheduleId);
        // dpLine.m_Record_Id2 = data[i].finPaymentScheduleId;
        dpLine.C_Currency_ID_From = data[i].cCurrencyId;
        dpLine.isPaid = data[i].ispaid;
        dpLine.Amount = data[i].amount;
        dpLine.PrepaidAmount = data[i].prepaidamt;

        list.add(dpLine);
      }
    } catch (ServletException e) {
      log4jDocInvoice.warn(e);
    }

    // Return Array
    DocLine_FinPaymentSchedule[] tl = new DocLine_FinPaymentSchedule[list.size()];
    list.toArray(tl);
    return tl;

  } // loadPayments

  /**
   * Create Facts (the accounting logic) for ARI, ARC, ARF, API, APC.
   * 
   * <pre>
   *  ARI, ARF
   *      Receivables     DR
   *      Charge                  CR
   *      TaxDue                  CR
   *      Revenue                 CR
   *  ARC
   *      Receivables             CR
   *      Charge          DR
   *      TaxDue          DR
   *      Revenue         RR
   *  API
   *      Payables                CR
   *      Charge          DR
   *      TaxCredit       DR
   *      Expense         DR
   *  APC
   *      Payables        DR
   *      Charge                  CR
   *      TaxCredit               CR
   *      Expense                 CR
   * </pre>
   * 
   * @param as
   *          accounting schema
   * @return Fact
   */
  public Fact createFact(AcctSchema as, ConnectionProvider conn, Connection con,
      VariablesSecureApp vars) throws ServletException {

    isReceiptForConversion = false;// T/C Compra por defecto
    FactLine fl = null;
    // Select specific definition
    String strClassname = AcctServerData.selectTemplateDoc(conn, as.m_C_AcctSchema_ID,
        DocumentType);
    if (strClassname.equals(""))
      strClassname = AcctServerData.selectTemplate(conn, as.m_C_AcctSchema_ID, AD_Table_ID);
    if (!strClassname.equals("")) {
      try {
        DocInvoiceTemplate newTemplate = (DocInvoiceTemplate) Class.forName(strClassname)
            .newInstance();
        return newTemplate.createFact(this, as, conn, con, vars);
      } catch (Exception e) {
        log4j.error("Error while creating new instance for DocInvoiceTemplate - " + e);
      }
    }
    log4jDocInvoice.debug("Starting create fact");
    // create Fact Header
    Fact fact = new Fact(this, as, Fact.POST_Actual);

    Invoice invoice = OBDal.getInstance().get(Invoice.class, this.Record_ID);

    if (invoice.getDocumentStatus().equals("VO"))
      fact.allowZero = true;

    GLItem itemDiverse = invoice.getScoDiverseAccGlitem();

    Date dttInvRef = null;
    String dateAcctTemp = DateAcct;
    if (!invoice.getTransactionDocument().getScoSpecialdoctype().equals("SCOARBOEINVOICE")
        && !invoice.getTransactionDocument().getScoSpecialdoctype().equals("SCOAPBOEINVOICE")
        && !invoice.getTransactionDocument().getScoSpecialdoctype().equals("SCOAPLOANINVOICE")) {

      dttInvRef = AcctServer.getDateForInvoiceReference(invoice);
      DateAcct = OBDateUtils.formatDate(dttInvRef);
    }

    /*
     * for (int i = 0; i < costcenters.size(); i++) {
     * fact.vCostCenters.add(costcenters.get(i).getCostCenter().getId());
     * fact.vCostCentersPercentage.add(costcenters.get(i).getPercentage()); }
     */

    String Fact_Acct_Group_ID = SequenceIdData.getUUID();
    // Cash based accounting
    if (!as.isAccrual())
      return null;

    // Change DocType
    if (DocumentType.equals(AcctServer.DOCTYPE_APInvoice)) {
      SCRComboItem comboItem = OBDal.getInstance().get(SCRComboItem.class,
          invoice.getScoPodoctypeComboitem().getId());
      typeDoc = comboItem.getCode();
    }

    /** @todo Assumes TaxIncluded = N */

    Invoice invref = AcctServer.getInvoicerefForMapping(invoice);

    // ARI, ARF, ARI_RM
    if (DocumentType.equals(AcctServer.DOCTYPE_ARInvoice)
        || DocumentType.equals(AcctServer.DOCTYPE_ARProForma)
        || DocumentType.equals(AcctServer.DOCTYPE_RMSalesInvoice)) {
      log4jDocInvoice.debug("Point 1");

      System.out.println("ES 'AR'");
      if (DocumentType.equals(AcctServer.DOCTYPE_ARInvoice) && invoice.isScoIsforfree()
          && !invoice.getDocumentStatus().equals("VO")) {

        isReceiptForConversion = true;// T/C Venta
        // cobrar IGV
        Vector<TaxRate> vTaxRates = new Vector<TaxRate>();
        Vector<BigDecimal> vTaxAmount = new Vector<BigDecimal>();
        GLItem transferenciaGratuita = null;
        DocLine datalineTransferenciaGratuita = null;
        for (InvoiceLine invoiceline : invoice.getInvoiceLineList()) {

          if (invoiceline.isFinancialInvoiceLine()
              && invoiceline.getAccount().getScoSpecialglitem() != null
              && invoiceline.getAccount().getScoSpecialglitem().equals("SCOFREEIGV")) {
            transferenciaGratuita = invoiceline.getAccount();

            for (int k = 0; k < p_lines.length; k++) {
              if (p_lines[k].m_TrxLine_ID.equals(invoiceline.getId())) {
                datalineTransferenciaGratuita = p_lines[k];
                break;
              }
            }

            continue;
          }

          BigDecimal igvamount = invoiceline.getTax().getRate().divide(new BigDecimal(100))
              .multiply(invoiceline.getUnitPrice().multiply(invoiceline.getInvoicedQuantity()))
              .setScale(2, RoundingMode.HALF_UP);
          boolean found = false;
          for (int j = 0; j < vTaxRates.size(); j++) {
            if (vTaxRates.get(j).getId().equals(invoiceline.getTax().getId())) {
              found = true;
              vTaxAmount.set(j, vTaxAmount.get(j).add(igvamount));
              break;
            }
          }
          if (!found) {
            vTaxRates.add(invoiceline.getTax());
            vTaxAmount.add(igvamount);
          }
        }

        for (int j = 0; j < vTaxRates.size(); j++) {

          DocLineInvoiceData[] dataGlitem = null;
          dataGlitem = DocLineInvoiceData.selectGlitem(conn, transferenciaGratuita.getId(),
              as.getC_AcctSchema_ID());

          DocTaxData[] data = DocTaxData.select(conn, vTaxRates.get(j).getId(),
              as.m_C_AcctSchema_ID);
          fl = fact.createLine(datalineTransferenciaGratuita,
              Account.getAccount(conn, data[0].tDueAcct), C_Currency_ID, "",
              vTaxAmount.get(j).toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,
              conn);
          if (fl != null) {
            fl.doc_Ad_Org_ID = invref.getOrganization().getId();
            fl.doc_C_Currency_ID = invref.getCurrency().getId();
            fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
          }

          fl = fact.createLine(datalineTransferenciaGratuita,
              Account.getAccount(conn, dataGlitem[0].glitemDebitAcct), C_Currency_ID,
              vTaxAmount.get(j).toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,
              conn);
          if (fl != null) {
            fl.doc_Ad_Org_ID = invref.getOrganization().getId();
            fl.doc_C_Currency_ID = invref.getCurrency().getId();
            fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
          }
        }
      } else {
        System.out.println("ES 'AR' 2");
        // Receivables DR
        // Charge CR
        log4jDocInvoice.debug("The first create line");
        if (new BigDecimal(getAmount(AcctServer.AMTTYPE_Charge)).compareTo(BigDecimal.ZERO) != 0) {
          fl = fact.createLine(null, getAccount(AcctServer.ACCTTYPE_Charge, as, conn),
              C_Currency_ID, "", getAmount(AcctServer.AMTTYPE_Charge), Fact_Acct_Group_ID,
              nextSeqNo(SeqNo), DocumentType, conn);
          if (fl != null) {
            fl.doc_Ad_Org_ID = invref.getOrganization().getId();
            fl.doc_C_Currency_ID = invref.getCurrency().getId();
            fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
          }
        }
        // TaxDue CR
        log4jDocInvoice.debug("m_taxes.length: " + m_taxes);

        System.out.println("taxes len:" + m_taxes.length);
        for (int i = 0; m_taxes != null && i < m_taxes.length; i++) {
          // New docLine created to assign C_Tax_ID value to the entry
          DocLine docLine = new DocLine(DocumentType, Record_ID, "");
          docLine.m_C_Tax_ID = m_taxes[i].m_C_Tax_ID;

          BigDecimal percentageFinalAccount = CashVATUtil._100;
          final BigDecimal taxesAmountTotal = new BigDecimal(
              StringUtils.isBlank(m_taxes[i].m_amount) ? "0" : m_taxes[i].m_amount);

          Vector<BigDecimal> vTcOutTax = new Vector<BigDecimal>();
          boolean isReceipt = true;
          convertAmount(taxesAmountTotal, isReceipt, DateAcct, TABLEID_Invoice, invoice.getId(),
              this.C_Currency_ID, as.m_C_Currency_ID, null, as, fact, Fact_Acct_Group_ID,
              nextSeqNo(SeqNo), conn, true, true, false, vTcOutTax, true, false).toString();

          BigDecimal taxToTransAccount = BigDecimal.ZERO;
          if (IsReversal.equals("Y")) {
            if (invoice.isScoIsmanualTranstax()) {
              final BigDecimal taxToFinalAccount = taxesAmountTotal.subtract(taxToTransAccount);
              fl = fact.createLine(docLine,
                  m_taxes[i].getAccount(DocTax.ACCTTYPE_Man_TaxDue_Trans, as, conn), C_Currency_ID,
                  taxToFinalAccount.toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
                  DocumentType, OBDateUtils.formatDate(getDateForInvoiceReference(invoice)),
                  vTcOutTax.get(0), conn);
              if (fl != null) {
                fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                fl.doc_C_Currency_ID = invref.getCurrency().getId();
                fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
              }
            } else {
              if (isCashVAT && m_taxes[i].m_isCashVAT) {
                percentageFinalAccount = CashVATUtil
                    .calculatePrepaidPercentageForCashVATTax(m_taxes[i].m_C_Tax_ID, Record_ID);
                taxToTransAccount = CashVATUtil.calculatePercentageAmount(
                    CashVATUtil._100.subtract(percentageFinalAccount), taxesAmountTotal,
                    C_Currency_ID);
                fl = fact.createLine(docLine,
                    m_taxes[i].getAccount(DocTax.ACCTTYPE_TaxDue_Trans, as, conn), C_Currency_ID,
                    taxToTransAccount.toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
                    DocumentType, OBDateUtils.formatDate(getDateForInvoiceReference(invoice)),
                    vTcOutTax.get(0), conn);
                if (fl != null) {
                  fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                  fl.doc_C_Currency_ID = invref.getCurrency().getId();
                  fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                }
              }
              final BigDecimal taxToFinalAccount = taxesAmountTotal.subtract(taxToTransAccount);
              fl = fact.createLine(docLine, m_taxes[i].getAccount(DocTax.ACCTTYPE_TaxDue, as, conn),
                  C_Currency_ID, taxToFinalAccount.toString(), "", Fact_Acct_Group_ID,
                  nextSeqNo(SeqNo), DocumentType,
                  OBDateUtils.formatDate(getDateForInvoiceReference(invoice)), vTcOutTax.get(0),
                  conn);
              if (fl != null) {
                fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                fl.doc_C_Currency_ID = invref.getCurrency().getId();
                fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
              }
            }
          } else {
            if (invoice.isScoIsmanualTranstax()) {
              final BigDecimal taxToFinalAccount = taxesAmountTotal.subtract(taxToTransAccount);
              fl = fact.createLine(docLine,
                  m_taxes[i].getAccount(DocTax.ACCTTYPE_Man_TaxDue_Trans, as, conn), C_Currency_ID,
                  "", taxToFinalAccount.toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
                  DocumentType, OBDateUtils.formatDate(getDateForInvoiceReference(invoice)),
                  vTcOutTax.get(0), conn);
              if (fl != null) {
                fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                fl.doc_C_Currency_ID = invref.getCurrency().getId();
                fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
              }
            } else {
              if (isCashVAT && m_taxes[i].m_isCashVAT) {
                percentageFinalAccount = CashVATUtil
                    .calculatePrepaidPercentageForCashVATTax(m_taxes[i].m_C_Tax_ID, Record_ID);
                taxToTransAccount = CashVATUtil.calculatePercentageAmount(
                    CashVATUtil._100.subtract(percentageFinalAccount), taxesAmountTotal,
                    C_Currency_ID);
                fl = fact.createLine(docLine,
                    m_taxes[i].getAccount(DocTax.ACCTTYPE_TaxDue_Trans, as, conn), C_Currency_ID,
                    "", taxToTransAccount.toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
                    DocumentType, OBDateUtils.formatDate(getDateForInvoiceReference(invoice)),
                    vTcOutTax.get(0), conn);
                if (fl != null) {
                  fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                  fl.doc_C_Currency_ID = invref.getCurrency().getId();
                  fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                }
              }
              final BigDecimal taxToFinalAccount = taxesAmountTotal.subtract(taxToTransAccount);
              fl = fact.createLine(docLine, m_taxes[i].getAccount(DocTax.ACCTTYPE_TaxDue, as, conn),
                  C_Currency_ID, "", taxToFinalAccount.toString(), Fact_Acct_Group_ID,
                  nextSeqNo(SeqNo), DocumentType,
                  OBDateUtils.formatDate(getDateForInvoiceReference(invoice)), vTcOutTax.get(0),
                  conn);
              if (fl != null) {
                fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                fl.doc_C_Currency_ID = invref.getCurrency().getId();
                fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
              }
            }
          }
        }
        // Revenue CR
        BigDecimal montoPrepay = new BigDecimal(0);
        Vector<BigDecimal> vTcOutPrepay = new Vector<BigDecimal>();
        String currencyPrepay = "";

        Vector<BigDecimal> montoPrepays = new Vector<BigDecimal>();
        Vector<BigDecimal> vTcOutPrepays = new Vector<BigDecimal>();
        Vector<String> currencyPrepays = new Vector<String>();

        if (p_lines != null && p_lines.length > 0) {
          for (int i = 0; i < p_lines.length; i++) {
            Account account = ((DocLine_Invoice) p_lines[i])
                .getAccount(ProductInfo.ACCTTYPE_P_Revenue, as, conn);
            System.out.println("p_line:" + ((DocLine_Invoice) p_lines[i]).getAmount()
                + " - DocumentType:" + DocumentType);
            if (DocumentType.equals(AcctServer.DOCTYPE_RMSalesInvoice)) {
              Account accountReturnMaterial = ((DocLine_Invoice) p_lines[i])
                  .getAccount(ProductInfo.ACCTTYPE_P_RevenueReturn, as, conn);
              if (accountReturnMaterial != null) {
                account = accountReturnMaterial;
              }
            }
            String amount = p_lines[i].getAmount();
            String amountConverted = "";

            ConversionRateDoc conversionRateCurrentDoc = getConversionRateDoc(TABLEID_Invoice,
                Record_ID, C_Currency_ID, as.m_C_Currency_ID);
            if (conversionRateCurrentDoc != null) {
              System.out.println("whattttt conversionratecurrentdoccccc");
              amountConverted = applyRate(new BigDecimal(p_lines[i].getAmount()),
                  conversionRateCurrentDoc, true).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            } else {
              System.out.println("enter normal getconvertedamt amount:" + p_lines[i].getAmount()
                  + " - getDateForInvoiceReference(invoice):"
                  + getDateForInvoiceReference(invoice));

              amountConverted = getConvertedAmt(p_lines[i].getAmount(), C_Currency_ID,
                  as.m_C_Currency_ID, OBDateUtils.formatDate(getDateForInvoiceReference(invoice)),
                  "", AD_Client_ID, AD_Org_ID, conn, isReceiptForConversion);
            }

            Account deferAcct = null;

            if (p_lines[i].m_M_Product_ID.equals("") && !p_lines[i].m_C_Glitem_ID.equals("")) {
              if (((DocLine_Invoice) p_lines[i]).getCustomGlDeferId() != null
                  && !((DocLine_Invoice) p_lines[i]).getCustomGlDeferId().equals(""))
                deferAcct = getAccountGLItem(OBDal.getInstance().get(GLItem.class,
                    ((DocLine_Invoice) p_lines[i]).getCustomGlDeferId()), as, true, conn);

            } else {
              InvoiceLine invLine = OBDal.getInstance().get(InvoiceLine.class,
                  p_lines[i].m_TrxLine_ID);
              GLItem linediv_acc_glitem = invLine.getScoLinedivAccGlitem();
              if (linediv_acc_glitem != null) {
                deferAcct = getAccountGLItem(linediv_acc_glitem, as, true, conn);
                account = deferAcct;
              } else {
                deferAcct = ((DocLine_Invoice) p_lines[i])
                    .getAccount(ProductInfo.ACCTTYPE_P_DefRevenue, as, conn);
              }
            }

            Account acctExtorn = account;

            if (((DocLine_Invoice) p_lines[i]).isDeferred()) {

              amount = createAccDefRevenueFact(invoice, invref, fact, (DocLine_Invoice) p_lines[i],
                  acctExtorn, deferAcct, amountConverted, as.m_C_Currency_ID, conn);
              if (IsReversal.equals("Y")) {
                fl = fact.createLine(p_lines[i], acctExtorn, as.m_C_Currency_ID, amount, "",
                    Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
                if (fl != null) {
                  fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                  fl.doc_C_Currency_ID = invref.getCurrency().getId();
                  fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                }
              } else {
                fl = fact.createLine(p_lines[i], acctExtorn, as.m_C_Currency_ID, "", amount,
                    Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
                if (fl != null) {
                  fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                  fl.doc_C_Currency_ID = invref.getCurrency().getId();
                  fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                }
              }
            } else {

              InvoiceLine invLine = OBDal.getInstance().get(InvoiceLine.class,
                  p_lines[i].m_TrxLine_ID);
              System.out.println("NOT DEFERRED LINE invLine:" + invLine.getIdentifier());
              if (!invLine.getInvoice().isSsaIsprepayment() && invLine.getAccount() != null
                  && invLine.getAccount().getScoSpecialglitem() != null
                  && invLine.getAccount().getScoSpecialglitem().equals("SCOPREPAYMENT")) {
                System.out.println("ENTER LINE SPECIAL GLITEM PREPAYMENT");
                String datePrepayment = OBDateUtils
                    .formatDate(invLine.getScoInvoicelinePrepay().getInvoice().getAccountingDate());
                Invoice invPrepay = invLine.getScoInvoicelinePrepay().getInvoice();
                Vector<BigDecimal> vTcOutPrep = new Vector<BigDecimal>();
                System.out.println("datePrepayment:" + datePrepayment + " - invPrepay:"
                    + invPrepay.getIdentifier());
                String amountConvertedPrepay = null;

                boolean bookDifferences = false;
                /*
                 * if (!this.C_Currency_ID.equals(as.m_C_Currency_ID)) { bookDifferences = true; }
                 */

                String amountConvertedPrepayPEN = convertAmount(
                    invLine.getSsaPrepaymentAmtApplied(), !(IsReversal.equals("Y")), datePrepayment,
                    TABLEID_Invoice, invPrepay.getId(), invPrepay.getCurrency().getId(),
                    as.m_C_Currency_ID, p_lines[i], as, fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
                    conn, bookDifferences, false, false, vTcOutPrep, false, true).toString();
                System.out.println("amountConvertedPrepayPEN:" + amountConvertedPrepayPEN);

                amountConvertedPrepay = (invLine.getSsaPrepaymentAmtApplied()).toString();

                System.out.println("CREATING FACT LINE account:" + account.combination + "  - CURR:"
                    + invPrepay.getCurrency().getId() + " - amount:" + amountConvertedPrepay
                    + " -tc: " + vTcOutPrep.get(0));

                DocLine docline = new DocLine(p_lines[i]);
                docline.m_C_Project_ID = invPrepay.getProject() != null
                    ? invPrepay.getProject().getId() : "";
                fl = fact.createLine(docline, account, invPrepay.getCurrency().getId(),
                    amountConvertedPrepay, "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,
                    null, vTcOutPrep.get(0), conn);

                if (fl != null) {
                  fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                  fl.doc_C_Currency_ID = invref.getCurrency().getId();
                  fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                }
                // CREAR 70 con el mismo tipo de cambio
                // Add prepayment line
                if (invPrepay.getCurrency().getId().equals(invoice.getCurrency().getId())) {
                  montoPrepays.add(new BigDecimal(amountConvertedPrepay));
                  vTcOutPrepays.add(vTcOutPrep.get(0));
                  currencyPrepays.add(invPrepay.getCurrency().getId());
                  System.out.println("new 2 montoPrepay:" + montoPrepay + " - vTcOutPrepay:"
                      + vTcOutPrepay + " - currencyPrepay:" + currencyPrepay);
                } else if (as.m_C_Currency_ID.equals(invoice.getCurrency().getId())) {
                  montoPrepays.add(new BigDecimal(amountConvertedPrepayPEN));
                  vTcOutPrepays.add(new BigDecimal(0));

                }
                // }

                fl.setRecordId3(invLine.getScoInvoicelinePrepay().getInvoice().getId());

              } else {
                System.out.println("NORMAL INVLINE");

                BigDecimal bigAmt = new BigDecimal(amount);
                System.out.println("START  bigAmt:" + bigAmt);
                for (int ii = 0; ii < montoPrepays.size(); ii++) {
                  BigDecimal _montoPrepay = montoPrepays.get(ii);
                  String _currencyPrepay = currencyPrepays.get(ii);
                  BigDecimal _vTcOutPrepay = vTcOutPrepays.get(ii);

                  System.out
                      .println("got prepay: _montoPrepay:" + _montoPrepay + " - _currencyPrepay:"
                          + _currencyPrepay + " - _vTcOutPrepay:" + _vTcOutPrepay);
                  if (_montoPrepay.compareTo(new BigDecimal(0)) <= 0) {
                    System.out.println("prepay 0 continue");
                    continue;
                  }
                  if (bigAmt.compareTo(new BigDecimal(0)) <= 0) {
                    System.out.println("bigamt 0 break");
                    break;
                  }

                  if (bigAmt.compareTo(_montoPrepay) <= 0) {
                    System.out.println("enter here _montoprepay>=bigamt");
                    _montoPrepay = _montoPrepay.subtract(bigAmt);
                    montoPrepays.set(ii, _montoPrepay);
                    System.out.println("new montoprepay:" + _montoPrepay);

                    if (_currencyPrepay.equals(invoice.getCurrency().getId())) {
                      System.out.println("_create line 1 amount:" + bigAmt
                          + " - vTcOutPrepay.get(0):" + _vTcOutPrepay);
                      fl = fact.createLine(p_lines[i], account, this.C_Currency_ID, "",
                          bigAmt.toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,
                          null, _vTcOutPrepay, conn);
                      if (fl != null) {
                        fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                        fl.doc_C_Currency_ID = invref.getCurrency().getId();
                        fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                      }
                    } else {
                      System.out.println("create line 2 amount:" + bigAmt);

                      fl = fact.createLine(p_lines[i], account, this.C_Currency_ID, "",
                          bigAmt.toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,
                          conn);
                      if (fl != null) {
                        fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                        fl.doc_C_Currency_ID = invref.getCurrency().getId();
                        fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                      }
                    }

                    bigAmt = BigDecimal.ZERO;
                    System.out.println("new bigamt:" + bigAmt);

                  } else {
                    System.out.println("_enter here montoprepay<bigamt");

                    bigAmt = bigAmt.subtract(_montoPrepay);
                    BigDecimal highAmt = _montoPrepay;
                    System.out.println("new bigAmt:" + bigAmt + " - highAmt:" + _montoPrepay);

                    if (_currencyPrepay.equals(invoice.getCurrency().getId())) {
                      System.out.println("_create line 3 amount:" + highAmt
                          + " - vTcOutPrepay.get(0):" + _vTcOutPrepay);

                      fl = fact.createLine(p_lines[i], account, this.C_Currency_ID, "",
                          highAmt.toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,
                          null, _vTcOutPrepay, conn);
                      if (fl != null) {
                        fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                        fl.doc_C_Currency_ID = invref.getCurrency().getId();
                        fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                      }
                    } else {
                      System.out.println("_create line 4 amount:" + highAmt);

                      fl = fact.createLine(p_lines[i], account, this.C_Currency_ID, "",
                          highAmt.toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,
                          conn);
                      if (fl != null) {
                        fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                        fl.doc_C_Currency_ID = invref.getCurrency().getId();
                        fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                      }
                    }
                    _montoPrepay = BigDecimal.ZERO;
                    montoPrepays.set(ii, _montoPrepay);
                    System.out.println("new montoprepay:" + _montoPrepay);
                  }
                }
                System.out.println("END  bigAmt:" + bigAmt);

                if (bigAmt.compareTo(BigDecimal.ZERO) > 0) {
                  System.out.println("CREATE NORMAL LINE  bigAmt:" + bigAmt);
                  fl = fact.createLine(p_lines[i], account, this.C_Currency_ID, "",
                      bigAmt.toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
                  if (fl != null) {
                    fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                    fl.doc_C_Currency_ID = invref.getCurrency().getId();
                    fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                  }
                } else if (bigAmt.compareTo(BigDecimal.ZERO) < 0) {
                  System.out.println("CREATE NORMAL LINE neg  bigAmt:" + bigAmt);
                  fl = fact.createLine(p_lines[i], account, this.C_Currency_ID,
                      bigAmt.negate().toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
                      DocumentType, conn);
                  if (fl != null) {
                    fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                    fl.doc_C_Currency_ID = invref.getCurrency().getId();
                    fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                  }
                }
                // }
              }

            }
            // If revenue has been deferred
            if (((DocLine_Invoice) p_lines[i]).isDeferred() && !amount.equals(amountConverted)) {
              amount = new BigDecimal(amountConverted).subtract(new BigDecimal(amount)).toString();
              if (IsReversal.equals("Y")) {
                fl = fact.createLine(p_lines[i], deferAcct, as.m_C_Currency_ID, amount, "",
                    Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
                if (fl != null) {
                  fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                  fl.doc_C_Currency_ID = invref.getCurrency().getId();
                  fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                }
              } else {
                fl = fact.createLine(p_lines[i], deferAcct, as.m_C_Currency_ID, "", amount,
                    Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
                if (fl != null) {
                  fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                  fl.doc_C_Currency_ID = invref.getCurrency().getId();
                  fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                }
              }
            }
          }
        }

        for (int i = 0; m_payments != null && i < m_payments.length; i++) {

          fl = fact.createLine(m_payments[i],
              (itemDiverse == null) ? getAccountBPartner(C_BPartner_ID, as, true, false, conn)
                  : getAccountGLItem(itemDiverse, as, true, conn),
              this.C_Currency_ID, m_payments[i].Amount, "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
              DocumentType, conn);
          if (fl != null) {
            fl.doc_Ad_Org_ID = invref.getOrganization().getId();
            fl.doc_C_Currency_ID = invref.getCurrency().getId();
            fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
            fl.isInvoicePayRec = true;
          }

        }

        if ((m_payments == null || m_payments.length == 0)
            && (m_debt_payments == null || m_debt_payments.length == 0)) {

          fl = fact.createLine(null,
              (itemDiverse == null) ? getAccountBPartner(C_BPartner_ID, as, true, false, conn)
                  : getAccountGLItem(itemDiverse, as, true, conn),
              this.C_Currency_ID, Amounts[AMTTYPE_Gross], "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
              DocumentType, conn);
          if (fl != null) {
            fl.doc_Ad_Org_ID = invref.getOrganization().getId();
            fl.doc_C_Currency_ID = invref.getCurrency().getId();
            fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
            fl.isInvoicePayRec = true;
          }
        }

        // Set Locations
        FactLine[] fLines = fact.getLines();
        for (int i = 0; i < fLines.length; i++) {
          if (fLines[i] != null) {
            fLines[i].setLocationFromOrg(fLines[i].getAD_Org_ID(conn), true, conn); // from
            // Loc
            fLines[i].setLocationFromBPartner(C_BPartner_Location_ID, false, conn); // to
            // Loc
          }
        }

      }
    }
    // ARC
    else if (this.DocumentType.equals(AcctServer.DOCTYPE_ARCredit)) {

      log4jDocInvoice.debug("Point 2");
      // Receivables CR
      if (m_payments == null || m_payments.length == 0)
        for (int i = 0; m_debt_payments != null && i < m_debt_payments.length; i++) {
          BigDecimal amount = new BigDecimal(m_debt_payments[i].Amount);
          // BigDecimal ZERO = BigDecimal.ZERO;
          fl = fact.createLine(m_debt_payments[i],
              (itemDiverse == null)
                  ? getAccountBPartner(C_BPartner_ID, as, true, m_debt_payments[i].dpStatus, conn)
                  : getAccountGLItem(itemDiverse, as, true, conn),
              this.C_Currency_ID, "",
              getConvertedAmt(((amount.negate())).toPlainString(),
                  m_debt_payments[i].C_Currency_ID_From, this.C_Currency_ID,
                  OBDateUtils.formatDate(getDateForInvoiceReference(invoice)), "", AD_Client_ID,
                  AD_Org_ID, conn),
              Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
          if (fl != null) {
            fl.doc_Ad_Org_ID = invref.getOrganization().getId();
            fl.doc_C_Currency_ID = invref.getCurrency().getId();
            fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
            fl.isInvoicePayRec = true;
          }
        }
      else
        for (int i = 0; m_payments != null && i < m_payments.length; i++) {
          BigDecimal amount = new BigDecimal(m_payments[i].Amount);
          BigDecimal prepaidAmount = new BigDecimal(m_payments[i].PrepaidAmount);

          fl = fact.createLine(m_payments[i],
              (itemDiverse == null) ? getAccountBPartner(C_BPartner_ID, as, true, false, conn)
                  : getAccountGLItem(itemDiverse, as, true, conn),
              this.C_Currency_ID, "", amount.negate().toString(), Fact_Acct_Group_ID,
              nextSeqNo(SeqNo), DocumentType, conn);
          if (fl != null) {
            fl.doc_Ad_Org_ID = invref.getOrganization().getId();
            fl.doc_C_Currency_ID = invref.getCurrency().getId();
            fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
            fl.isInvoicePayRec = true;
          }

          // OJO Asiento solo modificado del pago, no prepayment
          // porque no lo usamos
          // Pre-payment: Probably not needed as at this point we can
          // not generate pre-payments
          // against ARC. Amount is negated
          if (m_payments[i].C_Currency_ID_From.equals(as.m_C_Currency_ID)
              && prepaidAmount.compareTo(ZERO) != 0) {
            fl = fact.createLine(m_payments[i],
                (itemDiverse == null) ? getAccountBPartner(C_BPartner_ID, as, true, true, conn)
                    : getAccountGLItem(itemDiverse, as, true, conn),
                this.C_Currency_ID, "", prepaidAmount.negate().toString(), Fact_Acct_Group_ID,
                nextSeqNo(SeqNo), DocumentType, conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = invref.getOrganization().getId();
              fl.doc_C_Currency_ID = invref.getCurrency().getId();
              fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
              fl.isInvoicePayRec = true;
            }
          } else {
            try {
              DocInvoiceData[] prepayments = DocInvoiceData.selectPrepayments(connectionProvider,
                  m_payments[i].Line_ID);
              for (int j = 0; j < prepayments.length; j++) {
                Vector<BigDecimal> vTcPreOut = new Vector<BigDecimal>();
                BigDecimal prePaymentAmt = convertAmount(
                    new BigDecimal(prepayments[j].prepaidamt).negate(), true, DateAcct,
                    TABLEID_Payment, prepayments[j].finPaymentId, m_payments[i].C_Currency_ID_From,
                    as.m_C_Currency_ID, m_payments[i], as, fact, Fact_Acct_Group_ID,
                    nextSeqNo(SeqNo), conn, vTcPreOut);
                prePaymentAmt = new BigDecimal(prepayments[j].prepaidamt);
                fl = fact.createLine(m_payments[i],
                    (itemDiverse == null) ? getAccountBPartner(C_BPartner_ID, as, true, true, conn)
                        : getAccountGLItem(itemDiverse, as, true, conn),
                    m_payments[i].C_Currency_ID_From, "", prePaymentAmt.toString(),
                    Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, null, vTcPreOut.get(0),
                    conn);
                if (fl != null) {
                  fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                  fl.doc_C_Currency_ID = invref.getCurrency().getId();
                  fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                  fl.isInvoicePayRec = true;
                }
              }
            } catch (ServletException e) {
              log4jDocInvoice.warn(e);
            }
          }
        }
      if ((m_payments == null || m_payments.length == 0)
          && (m_debt_payments == null || m_debt_payments.length == 0)) {

        fl = fact.createLine(null,
            (itemDiverse == null) ? getAccountBPartner(C_BPartner_ID, as, true, false, conn)
                : getAccountGLItem(itemDiverse, as, true, conn),
            this.C_Currency_ID, "", Amounts[AMTTYPE_Gross], Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, conn);
        if (fl != null) {
          fl.doc_Ad_Org_ID = invref.getOrganization().getId();
          fl.doc_C_Currency_ID = invref.getCurrency().getId();
          fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
          fl.isInvoicePayRec = true;
        }

      }
      // Charge DR
      if (new BigDecimal(getAmount(AcctServer.AMTTYPE_Charge)).compareTo(BigDecimal.ZERO) != 0) {
        fl = fact.createLine(null, getAccount(AcctServer.ACCTTYPE_Charge, as, conn),
            this.C_Currency_ID, getAmount(AcctServer.AMTTYPE_Charge), "", Fact_Acct_Group_ID,
            nextSeqNo(SeqNo), DocumentType, conn);
        if (fl != null) {
          fl.doc_Ad_Org_ID = invref.getOrganization().getId();
          fl.doc_C_Currency_ID = invref.getCurrency().getId();
          fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
        }
      }
      // TaxDue DR

      for (int i = 0; m_taxes != null && i < m_taxes.length; i++) {
        // New docLine created to assign C_Tax_ID value to the entry
        DocLine docLine = new DocLine(DocumentType, Record_ID, "");
        docLine.m_C_Tax_ID = m_taxes[i].m_C_Tax_ID;

        BigDecimal percentageFinalAccount = CashVATUtil._100;
        final BigDecimal taxesAmountTotal = new BigDecimal(
            StringUtils.isBlank(m_taxes[i].getAmount()) ? "0" : m_taxes[i].getAmount());

        Vector<BigDecimal> vTcOutTax = new Vector<BigDecimal>();
        boolean isReceipt = true;
        convertAmount(taxesAmountTotal.negate(), isReceipt, DateAcct, TABLEID_Invoice,
            invoice.getId(), this.C_Currency_ID, as.m_C_Currency_ID, null, as, fact,
            Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn, true, true, false, vTcOutTax, true, false)
                .toString();

        BigDecimal taxToTransAccount = BigDecimal.ZERO;
        if (invoice.isScoIsmanualTranstax()) {
          final BigDecimal taxToFinalAccount = taxesAmountTotal.subtract(taxToTransAccount);
          fl = fact.createLine(docLine,
              m_taxes[i].getAccount(DocTax.ACCTTYPE_Man_TaxDue_Trans, as, conn), this.C_Currency_ID,
              taxToFinalAccount.toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,
              OBDateUtils.formatDate(getDateForInvoiceReference(invoice)), vTcOutTax.get(0), conn);
          if (fl != null) {
            fl.doc_Ad_Org_ID = invref.getOrganization().getId();
            fl.doc_C_Currency_ID = invref.getCurrency().getId();
            fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
          }
        } else {
          if (isCashVAT && m_taxes[i].m_isCashVAT) {
            percentageFinalAccount = CashVATUtil
                .calculatePrepaidPercentageForCashVATTax(m_taxes[i].m_C_Tax_ID, Record_ID);
            taxToTransAccount = CashVATUtil.calculatePercentageAmount(
                CashVATUtil._100.subtract(percentageFinalAccount), taxesAmountTotal, C_Currency_ID);
            fl = fact.createLine(docLine,
                m_taxes[i].getAccount(DocTax.ACCTTYPE_TaxDue_Trans, as, conn), this.C_Currency_ID,
                taxToTransAccount.toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
                DocumentType, OBDateUtils.formatDate(getDateForInvoiceReference(invoice)),
                vTcOutTax.get(0), conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = invref.getOrganization().getId();
              fl.doc_C_Currency_ID = invref.getCurrency().getId();
              fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
            }
          }
          final BigDecimal taxToFinalAccount = taxesAmountTotal.subtract(taxToTransAccount);
          fl = fact.createLine(docLine, m_taxes[i].getAccount(DocTax.ACCTTYPE_TaxDue, as, conn),
              this.C_Currency_ID, taxToFinalAccount.toString(), "", Fact_Acct_Group_ID,
              nextSeqNo(SeqNo), DocumentType,
              OBDateUtils.formatDate(getDateForInvoiceReference(invoice)), vTcOutTax.get(0), conn);
          if (fl != null) {
            fl.doc_Ad_Org_ID = invref.getOrganization().getId();
            fl.doc_C_Currency_ID = invref.getCurrency().getId();
            fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
          }
        }
      }
      // Revenue CR
      for (int i = 0; p_lines != null && i < p_lines.length; i++) {
        String amount = p_lines[i].getAmount();
        String amountCoverted = "";

        ConversionRateDoc conversionRateCurrentDoc = null;
        if (invoice.getScoInvoiceref() != null)
          conversionRateCurrentDoc = getConversionRateDoc(TABLEID_Invoice,
              invoice.getScoInvoiceref().getId(), C_Currency_ID, as.m_C_Currency_ID);

        if (conversionRateCurrentDoc != null) {
          amountCoverted = applyRate(new BigDecimal(p_lines[i].getAmount()),
              conversionRateCurrentDoc, true).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        } else {
          amountCoverted = getConvertedAmt(p_lines[i].getAmount(), C_Currency_ID,
              as.m_C_Currency_ID, OBDateUtils.formatDate(getDateForInvoiceReference(invoice)), "",
              AD_Client_ID, AD_Org_ID, conn, isReceiptForConversion);
        }

        Account account = ((DocLine_Invoice) p_lines[i]).getAccount(ProductInfo.ACCTTYPE_P_Revenue,
            as, conn);

        Account deferAcct = null;

        if (p_lines[i].m_M_Product_ID.equals("") && !p_lines[i].m_C_Glitem_ID.equals("")) {
          if (((DocLine_Invoice) p_lines[i]).getCustomGlDeferId() != null
              && !((DocLine_Invoice) p_lines[i]).getCustomGlDeferId().equals(""))
            deferAcct = getAccountGLItem(OBDal.getInstance().get(GLItem.class,
                ((DocLine_Invoice) p_lines[i]).getCustomGlDeferId()), as, true, conn);

        } else {
          InvoiceLine invLine = OBDal.getInstance().get(InvoiceLine.class, p_lines[i].m_TrxLine_ID);
          GLItem linediv_acc_glitem = invLine.getScoLinedivAccGlitem();
          if (linediv_acc_glitem != null) {
            deferAcct = getAccountGLItem(linediv_acc_glitem, as, true, conn);
            account = deferAcct;
          } else {
            deferAcct = ((DocLine_Invoice) p_lines[i]).getAccount(ProductInfo.ACCTTYPE_P_DefRevenue,
                as, conn);
          }
        }

        if (((DocLine_Invoice) p_lines[i]).isDeferred()) {
          amount = createAccDefRevenueFact(invoice, invref, fact, (DocLine_Invoice) p_lines[i],
              account, deferAcct, amountCoverted, as.m_C_Currency_ID, conn);
          fl = fact.createLine(p_lines[i], account, as.m_C_Currency_ID, amount, "",
              Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
          if (fl != null) {
            fl.doc_Ad_Org_ID = invref.getOrganization().getId();
            fl.doc_C_Currency_ID = invref.getCurrency().getId();
            fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
          }
        } else {
          fl = fact.createLine(p_lines[i], account, this.C_Currency_ID, amount, "",
              Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
          if (fl != null) {
            fl.doc_Ad_Org_ID = invref.getOrganization().getId();
            fl.doc_C_Currency_ID = invref.getCurrency().getId();
            fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
          }
        }
        // If revenue has been deferred
        if (((DocLine_Invoice) p_lines[i]).isDeferred() && !amount.equals(amountCoverted)) {
          amount = new BigDecimal(amountCoverted).subtract(new BigDecimal(amount)).toString();
          fl = fact.createLine(p_lines[i], deferAcct, as.m_C_Currency_ID, amount, "",
              Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
          if (fl != null) {
            fl.doc_Ad_Org_ID = invref.getOrganization().getId();
            fl.doc_C_Currency_ID = invref.getCurrency().getId();
            fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
          }
        }
      }
      // Set Locations
      FactLine[] fLines = fact.getLines();
      for (int i = 0; fLines != null && i < fLines.length; i++) {
        if (fLines[i] != null) {
          fLines[i].setLocationFromOrg(fLines[i].getAD_Org_ID(conn), true, conn); // from
          // Loc
          fLines[i].setLocationFromBPartner(C_BPartner_Location_ID, false, conn); // to
          // Loc
        }
      }
    }
    // API
    else if (this.DocumentType.equals(AcctServer.DOCTYPE_APInvoice)) {

      isReceiptForConversion = true;

      log4jDocInvoice.debug("Point 3");
      // Liability CR
      if (m_payments == null || m_payments.length == 0)
        for (int i = 0; m_debt_payments != null && i < m_debt_payments.length; i++) {
          if (m_debt_payments[i].isReceipt.equals("Y")) {
            fl = fact.createLine(m_debt_payments[i],
                (itemDiverse == null)
                    ? getAccountBPartner(C_BPartner_ID, as, true, m_debt_payments[i].dpStatus, conn)
                    : getAccountGLItem(itemDiverse, as, true, conn),
                this.C_Currency_ID,
                getConvertedAmt(m_debt_payments[i].Amount, m_debt_payments[i].C_Currency_ID_From,
                    this.C_Currency_ID, OBDateUtils.formatDate(getDateForInvoiceReference(invoice)),
                    "", AD_Client_ID, AD_Org_ID, conn),
                "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = invref.getOrganization().getId();
              fl.doc_C_Currency_ID = invref.getCurrency().getId();
              fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
              fl.isInvoicePayRec = true;
            }
          } else {
            fl = fact.createLine(m_debt_payments[i],
                (itemDiverse == null) ? getAccountBPartner(C_BPartner_ID, as, false,
                    m_debt_payments[i].dpStatus, conn)
                    : getAccountGLItem(itemDiverse, as, false, conn),
                this.C_Currency_ID, "",
                getConvertedAmt(m_debt_payments[i].Amount, m_debt_payments[i].C_Currency_ID_From,
                    this.C_Currency_ID, OBDateUtils.formatDate(getDateForInvoiceReference(invoice)),
                    "", AD_Client_ID, AD_Org_ID, conn),
                Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = invref.getOrganization().getId();
              fl.doc_C_Currency_ID = invref.getCurrency().getId();
              fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
              fl.isInvoicePayRec = true;
            }
          }
        }
      else
        for (int i = 0; m_payments != null && i < m_payments.length; i++) {

          fl = fact.createLine(m_payments[i],
              (itemDiverse == null) ? getAccountBPartner(C_BPartner_ID, as, false, false, conn)
                  : getAccountGLItem(itemDiverse, as, false, conn),
              this.C_Currency_ID, "", m_payments[i].Amount, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
              DocumentType, conn);
          if (fl != null) {
            fl.doc_Ad_Org_ID = invref.getOrganization().getId();
            fl.doc_C_Currency_ID = invref.getCurrency().getId();
            fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
            fl.isInvoicePayRec = true;
          }

          if (m_payments[i].C_Currency_ID_From.equals(as.m_C_Currency_ID)
              && new BigDecimal(m_payments[i].PrepaidAmount).compareTo(ZERO) != 0) {
            fl = fact.createLine(m_payments[i],
                (itemDiverse == null) ? getAccountBPartner(C_BPartner_ID, as, false, true, conn)
                    : getAccountGLItem(itemDiverse, as, false, conn),
                this.C_Currency_ID, "", m_payments[i].PrepaidAmount, Fact_Acct_Group_ID,
                nextSeqNo(SeqNo), DocumentType, conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = invref.getOrganization().getId();
              fl.doc_C_Currency_ID = invref.getCurrency().getId();
              fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
              fl.isInvoicePayRec = true;
            }
          } else {
            try {
              DocInvoiceData[] prepayments = DocInvoiceData.selectPrepayments(connectionProvider,
                  m_payments[i].Line_ID);
              for (int j = 0; j < prepayments.length; j++) {
                Vector<BigDecimal> vTcPreOut = new Vector<BigDecimal>();
                BigDecimal prePaymentAmt = convertAmount(new BigDecimal(prepayments[j].prepaidamt),
                    false, DateAcct, TABLEID_Payment, prepayments[j].finPaymentId,
                    m_payments[i].C_Currency_ID_From, as.m_C_Currency_ID, m_payments[i], as, fact,
                    Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn, vTcPreOut);
                prePaymentAmt = new BigDecimal(prepayments[j].prepaidamt);
                fl = fact.createLine(m_payments[i],
                    (itemDiverse == null) ? getAccountBPartner(C_BPartner_ID, as, false, true, conn)
                        : getAccountGLItem(itemDiverse, as, false, conn),
                    m_payments[i].C_Currency_ID_From, "", prePaymentAmt.toString(),
                    Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, null, vTcPreOut.get(0),
                    conn);
                if (fl != null) {
                  fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                  fl.doc_C_Currency_ID = invref.getCurrency().getId();
                  fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                  fl.isInvoicePayRec = true;
                }
              }
            } catch (ServletException e) {
              log4jDocInvoice.warn(e);
            }
          }
        }
      if ((m_payments == null || m_payments.length == 0)
          && (m_debt_payments == null || m_debt_payments.length == 0)) {

        fl = fact.createLine(null,
            (itemDiverse == null) ? getAccountBPartner(C_BPartner_ID, as, false, false, conn)
                : getAccountGLItem(itemDiverse, as, false, conn),
            this.C_Currency_ID, "", Amounts[AMTTYPE_Gross], Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, conn);
        if (fl != null) {
          fl.doc_Ad_Org_ID = invref.getOrganization().getId();
          fl.doc_C_Currency_ID = invref.getCurrency().getId();
          fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
          fl.isInvoicePayRec = true;
        }

      }

      // Charge DR
      fl = fact.createLine(null, getAccount(AcctServer.ACCTTYPE_Charge, as, conn),
          this.C_Currency_ID, getAmount(AcctServer.AMTTYPE_Charge), "", Fact_Acct_Group_ID,
          nextSeqNo(SeqNo), DocumentType, conn);
      if (fl != null) {
        fl.doc_Ad_Org_ID = invref.getOrganization().getId();
        fl.doc_C_Currency_ID = invref.getCurrency().getId();
        fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
      }

      // TaxCredit DR
      for (int i = 0; m_taxes != null && i < m_taxes.length; i++) {
        // New docLine created to assign C_Tax_ID value to the entry
        DocLine docLine = new DocLine(DocumentType, Record_ID, "");
        docLine.m_C_Tax_ID = m_taxes[i].m_C_Tax_ID;

        if (!m_taxes[i].m_isTaxUndeductable) {
          BigDecimal percentageFinalAccount = CashVATUtil._100;
          final BigDecimal taxesAmountTotal = new BigDecimal(
              StringUtils.isBlank(m_taxes[i].getAmount()) ? "0" : m_taxes[i].getAmount());
          BigDecimal taxToTransAccount = BigDecimal.ZERO;
          if (IsReversal.equals("Y")) {
            if (invoice.isScoIsmanualTranstax()) {
              final BigDecimal taxToFinalAccount = taxesAmountTotal.subtract(taxToTransAccount);
              fl = fact.createLine(docLine,
                  m_taxes[i].getAccount(DocTax.ACCTTYPE_Man_TaxCredit_Trans, as, conn),
                  this.C_Currency_ID, "", taxToFinalAccount.toString(), Fact_Acct_Group_ID,
                  nextSeqNo(SeqNo), DocumentType, conn);
              if (fl != null) {
                fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                fl.doc_C_Currency_ID = invref.getCurrency().getId();
                fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
              }
            } else {
              if (isCashVAT && m_taxes[i].m_isCashVAT) {
                percentageFinalAccount = CashVATUtil
                    .calculatePrepaidPercentageForCashVATTax(m_taxes[i].m_C_Tax_ID, Record_ID);
                taxToTransAccount = CashVATUtil.calculatePercentageAmount(
                    CashVATUtil._100.subtract(percentageFinalAccount), taxesAmountTotal,
                    C_Currency_ID);
                fl = fact.createLine(docLine,
                    m_taxes[i].getAccount(DocTax.ACCTTYPE_TaxCredit_Trans, as, conn),
                    this.C_Currency_ID, "", taxToTransAccount.toString(), Fact_Acct_Group_ID,
                    nextSeqNo(SeqNo), DocumentType, conn);
                if (fl != null) {
                  fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                  fl.doc_C_Currency_ID = invref.getCurrency().getId();
                  fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                }
              }
              final BigDecimal taxToFinalAccount = taxesAmountTotal.subtract(taxToTransAccount);
              fl = fact.createLine(docLine,
                  m_taxes[i].getAccount(DocTax.ACCTTYPE_TaxCredit, as, conn), this.C_Currency_ID,
                  "", taxToFinalAccount.toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
                  DocumentType, conn);
              if (fl != null) {
                fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                fl.doc_C_Currency_ID = invref.getCurrency().getId();
                fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
              }
            }

          } else {
            if (invoice.isScoIsmanualTranstax()) {
              final BigDecimal taxToFinalAccount = taxesAmountTotal.subtract(taxToTransAccount);
              fl = fact.createLine(docLine,
                  m_taxes[i].getAccount(DocTax.ACCTTYPE_Man_TaxCredit_Trans, as, conn),
                  this.C_Currency_ID, taxToFinalAccount.toString(), "", Fact_Acct_Group_ID,
                  nextSeqNo(SeqNo), DocumentType, conn);
              if (fl != null) {
                fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                fl.doc_C_Currency_ID = invref.getCurrency().getId();
                fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
              }
            } else {
              if (isCashVAT && m_taxes[i].m_isCashVAT) {
                percentageFinalAccount = CashVATUtil
                    .calculatePrepaidPercentageForCashVATTax(m_taxes[i].m_C_Tax_ID, Record_ID);
                taxToTransAccount = CashVATUtil.calculatePercentageAmount(
                    CashVATUtil._100.subtract(percentageFinalAccount), taxesAmountTotal,
                    C_Currency_ID);
                fl = fact.createLine(docLine,
                    m_taxes[i].getAccount(DocTax.ACCTTYPE_TaxCredit_Trans, as, conn),
                    this.C_Currency_ID, taxToTransAccount.toString(), "", Fact_Acct_Group_ID,
                    nextSeqNo(SeqNo), DocumentType, conn);
                if (fl != null) {
                  fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                  fl.doc_C_Currency_ID = invref.getCurrency().getId();
                  fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                }
              }
              final BigDecimal taxToFinalAccount = taxesAmountTotal.subtract(taxToTransAccount);
              fl = fact.createLine(docLine,
                  m_taxes[i].getAccount(DocTax.ACCTTYPE_TaxCredit, as, conn), this.C_Currency_ID,
                  taxToFinalAccount.toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
                  DocumentType, conn);
              if (fl != null) {
                fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                fl.doc_C_Currency_ID = invref.getCurrency().getId();
                fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
              }
            }

          }

        } else {
          DocLineInvoiceData[] data = null;
          try {
            data = DocLineInvoiceData.selectUndeductable(connectionProvider, Record_ID);
          } catch (ServletException e) {
            log4jDocInvoice.warn(e);
          }

          BigDecimal cumulativeTaxLineAmount = new BigDecimal(0);
          BigDecimal taxAmount = new BigDecimal(0);
          for (int j = 0; data != null && j < data.length; j++) {
            DocLine docLine1 = new DocLine(DocumentType, Record_ID, "");
            docLine1.m_C_Tax_ID = data[j].cTaxId;
            docLine1.m_C_BPartner_ID = data[j].cBpartnerId;
            docLine1.m_M_Product_ID = data[j].mProductId;
            docLine1.m_C_Costcenter_ID = data[j].cCostcenterId;
            docLine1.m_C_Project_ID = data[j].cProjectId;
            docLine1.m_User1_ID = data[j].user1id;
            docLine1.m_User2_ID = data[j].user2id;
            docLine1.m_C_Activity_ID = data[j].cActivityId;
            docLine1.m_C_Campaign_ID = data[j].cCampaignId;
            docLine1.m_A_Asset_ID = data[j].aAssetId;
            String strtaxAmount = null;

            try {

              DocInvoiceData[] dataEx = DocInvoiceData.selectProductAcct(conn,
                  as.getC_AcctSchema_ID(), m_taxes[i].m_C_Tax_ID, Record_ID);
              if (dataEx.length == 0) {
                dataEx = DocInvoiceData.selectGLItemAcctForTaxLine(conn, as.getC_AcctSchema_ID(),
                    m_taxes[i].m_C_Tax_ID, Record_ID);
              }
              strtaxAmount = m_taxes[i].getAmount();
              taxAmount = new BigDecimal(strtaxAmount.equals("") ? "0.00" : strtaxAmount);
              if (j == data.length - 1) {
                data[j].taxamt = taxAmount.subtract(cumulativeTaxLineAmount).toPlainString();
              }
              try {

                if (this.DocumentType.equals(AcctServer.DOCTYPE_APInvoice)) {
                  if (IsReversal.equals("Y")) {
                    fl = fact.createLine(docLine1, Account.getAccount(conn, dataEx[0].pExpenseAcct),
                        this.C_Currency_ID, "", data[j].taxamt, Fact_Acct_Group_ID,
                        nextSeqNo(SeqNo), DocumentType, conn);
                    if (fl != null) {
                      fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                      fl.doc_C_Currency_ID = invref.getCurrency().getId();
                      fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                    }

                  } else {
                    fl = fact.createLine(docLine1, Account.getAccount(conn, dataEx[0].pExpenseAcct),
                        this.C_Currency_ID, data[j].taxamt, "", Fact_Acct_Group_ID,
                        nextSeqNo(SeqNo), DocumentType, conn);
                    if (fl != null) {
                      fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                      fl.doc_C_Currency_ID = invref.getCurrency().getId();
                      fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                    }
                  }
                } else if (this.DocumentType.equals(AcctServer.DOCTYPE_APCredit)) {
                  fl = fact.createLine(docLine1, Account.getAccount(conn, dataEx[0].pExpenseAcct),
                      this.C_Currency_ID, "", data[j].taxamt, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
                      DocumentType, conn);
                  if (fl != null) {
                    fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                    fl.doc_C_Currency_ID = invref.getCurrency().getId();
                    fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                  }
                }
                cumulativeTaxLineAmount = cumulativeTaxLineAmount
                    .add(new BigDecimal(data[j].taxamt));
              } catch (ServletException e) {
                log4jDocInvoice.error("Exception in createLineForTaxUndeductable method: " + e);
              }
            } catch (ServletException e) {
              log4jDocInvoice.warn(e);
            }
          }
        }
      }

      // Expense DR
      for (int i = 0; p_lines != null && i < p_lines.length; i++) {
        String amount = p_lines[i].getAmount();
        String amountConverted = "";
        ConversionRateDoc conversionRateCurrentDoc = getConversionRateDoc(TABLEID_Invoice,
            Record_ID, C_Currency_ID, as.m_C_Currency_ID);
        if (conversionRateCurrentDoc != null) {
          amountConverted = applyRate(new BigDecimal(p_lines[i].getAmount()),
              conversionRateCurrentDoc, true).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        } else {
          amountConverted = getConvertedAmt(p_lines[i].getAmount(), C_Currency_ID,
              as.m_C_Currency_ID, OBDateUtils.formatDate(getDateForInvoiceReference(invoice)), "",
              AD_Client_ID, AD_Org_ID, conn);
        }

        Account deferAcct = null;
        Account acctExtorn = null;

        if (!((DocLine_Invoice) p_lines[i]).m_C_Glitem_ID.equals("")) {

          if (p_lines[i].m_M_Product_ID.equals("") && !p_lines[i].m_C_Glitem_ID.equals("")) {
            if (((DocLine_Invoice) p_lines[i]).getCustomGlDeferId() != null
                && !((DocLine_Invoice) p_lines[i]).getCustomGlDeferId().equals(""))
              deferAcct = getAccountGLItem(OBDal.getInstance().get(GLItem.class,
                  ((DocLine_Invoice) p_lines[i]).getCustomGlDeferId()), as, true, conn);

          } else {
            deferAcct = ((DocLine_Invoice) p_lines[i]).getAccount(ProductInfo.ACCTTYPE_P_DefExpense,
                as, conn);
          }

          acctExtorn = ((DocLine_Invoice) p_lines[i]).getAccount(ProductInfo.ACCTTYPE_P_Expense, as,
              conn);
        } else {
          InvoiceLine invLine = OBDal.getInstance().get(InvoiceLine.class, p_lines[i].m_TrxLine_ID);
          GLItem linediv_acc_glitem = invLine.getScoLinedivAccGlitem();
          if (linediv_acc_glitem != null) {
            deferAcct = getAccountGLItem(linediv_acc_glitem, as, true, conn);
            acctExtorn = deferAcct;
          } else {
            deferAcct = ((DocLine_Invoice) p_lines[i]).getAccount(ProductInfo.ACCTTYPE_P_DefExpense,
                as, conn);
            acctExtorn = ((DocLine_Invoice) p_lines[i]).getAccount(ProductInfo.ACCTTYPE_P_Expense,
                as, conn);
          }
        }

        DocLine_Invoice docinv = (DocLine_Invoice) p_lines[i];
        if (docinv.isNoEmitido()) {

          Organization org = OBDal.getInstance().get(Organization.class,
              invoice.getOrganization().getId());
          String strGlitemNoEmitidas = org.getScoNotissuedGli().getId();
          Account accNoEmitidas = getAccountGLItem(
              OBDal.getInstance().get(GLItem.class, strGlitemNoEmitidas), as, true, conn);
          if (IsReversal.equals("Y")) {
            fl = fact.createLine(p_lines[i], accNoEmitidas, this.C_Currency_ID, amount, "",
                Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = invref.getOrganization().getId();
              fl.doc_C_Currency_ID = invref.getCurrency().getId();
              fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
            }
          } else {
            fl = fact.createLine(p_lines[i], accNoEmitidas, this.C_Currency_ID, "", amount,
                Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = invref.getOrganization().getId();
              fl.doc_C_Currency_ID = invref.getCurrency().getId();
              fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
            }
          }
        }

        if (((DocLine_Invoice) p_lines[i]).isDeferred()) {

          amount = createAccDefExpenseFact(fact, (DocLine_Invoice) p_lines[i], acctExtorn,
              deferAcct, amountConverted, as.m_C_Currency_ID, conn, invoice, invref);
          if (IsReversal.equals("Y")) {
            fl = fact.createLine(p_lines[i], acctExtorn, as.m_C_Currency_ID, "", amount,
                Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = invref.getOrganization().getId();
              fl.doc_C_Currency_ID = invref.getCurrency().getId();
              fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
            }

          } else {
            fl = fact.createLine(p_lines[i], acctExtorn, as.m_C_Currency_ID, amount, "",
                Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = invref.getOrganization().getId();
              fl.doc_C_Currency_ID = invref.getCurrency().getId();
              fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
            }

          }

        } else {
          if (IsReversal.equals("Y")) {
            fl = fact.createLine(p_lines[i], acctExtorn, this.C_Currency_ID, "", amount,
                Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = invref.getOrganization().getId();
              fl.doc_C_Currency_ID = invref.getCurrency().getId();
              fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
            }

          } else {

            Account expense = acctExtorn;
            fl = fact.createLine(p_lines[i], expense, this.C_Currency_ID, amount, "",
                Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = invref.getOrganization().getId();
              fl.doc_C_Currency_ID = invref.getCurrency().getId();
              fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
            }
          }
        }

        // If expense has been deferred
        if (((DocLine_Invoice) p_lines[i]).isDeferred() && !amount.equals(amountConverted)) {
          amount = new BigDecimal(amountConverted).subtract(new BigDecimal(amount)).toString();
          if (IsReversal.equals("Y")) {
            fl = fact.createLine(p_lines[i], deferAcct, as.m_C_Currency_ID, "", amount,
                Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = invref.getOrganization().getId();
              fl.doc_C_Currency_ID = invref.getCurrency().getId();
              fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
            }

          } else {
            fl = fact.createLine(p_lines[i], deferAcct, as.m_C_Currency_ID, amount, "",
                Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = invref.getOrganization().getId();
              fl.doc_C_Currency_ID = invref.getCurrency().getId();
              fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
            }

          }
        }
      }

      // Set Locations
      FactLine[] fLines = fact.getLines();
      for (int i = 0; fLines != null && i < fLines.length; i++) {
        if (fLines[i] != null) {
          fLines[i].setLocationFromBPartner(C_BPartner_Location_ID, true, conn); // from
          // Loc
          fLines[i].setLocationFromOrg(fLines[i].getAD_Org_ID(conn), false, conn); // to
          // Loc
        }
      }
      updateProductInfo(as.getC_AcctSchema_ID(), conn, con); // only API

    }
    // APC
    else if (this.DocumentType.equals(AcctServer.DOCTYPE_APCredit)) {
      log4jDocInvoice.debug("Point 4");
      isReceiptForConversion = true;

      // Liability DR
      if (m_payments == null || m_payments.length == 0)
        for (int i = 0; m_debt_payments != null && i < m_debt_payments.length; i++) {
          BigDecimal amount = new BigDecimal(m_debt_payments[i].Amount);
          // BigDecimal ZERO = BigDecimal.ZERO;
          fl = fact.createLine(m_debt_payments[i],
              (itemDiverse == null)
                  ? getAccountBPartner(C_BPartner_ID, as, false, m_debt_payments[i].dpStatus, conn)
                  : getAccountGLItem(itemDiverse, as, false, conn),
              this.C_Currency_ID,
              getConvertedAmt(((amount.negate())).toPlainString(),
                  m_debt_payments[i].C_Currency_ID_From, this.C_Currency_ID,
                  OBDateUtils.formatDate(getDateForInvoiceReference(invoice)), "", AD_Client_ID,
                  AD_Org_ID, conn),
              "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
          if (fl != null) {
            fl.doc_Ad_Org_ID = invref.getOrganization().getId();
            fl.doc_C_Currency_ID = invref.getCurrency().getId();
            fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
            fl.isInvoicePayRec = true;
          }
        }
      else
        for (int i = 0; m_payments != null && i < m_payments.length; i++) {
          BigDecimal amount = new BigDecimal(m_payments[i].Amount);
          BigDecimal prepaidAmount = new BigDecimal(m_payments[i].PrepaidAmount);

          fl = fact.createLine(m_payments[i],
              (itemDiverse == null) ? getAccountBPartner(C_BPartner_ID, as, false, false, conn)
                  : getAccountGLItem(itemDiverse, as, false, conn),
              this.C_Currency_ID, amount.negate().toString(), "", Fact_Acct_Group_ID,
              nextSeqNo(SeqNo), DocumentType, conn);
          if (fl != null) {
            fl.doc_Ad_Org_ID = invref.getOrganization().getId();
            fl.doc_C_Currency_ID = invref.getCurrency().getId();
            fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
            fl.isInvoicePayRec = true;
          }

          // Pre-payment: Probably not needed as at this point we can
          // not generate pre-payments
          // against APC. Amount is negated
          if (m_payments[i].C_Currency_ID_From.equals(as.m_C_Currency_ID)
              && prepaidAmount.compareTo(ZERO) != 0) {
            fl = fact.createLine(m_payments[i],
                (itemDiverse == null) ? getAccountBPartner(C_BPartner_ID, as, false, true, conn)
                    : getAccountGLItem(itemDiverse, as, false, conn),
                this.C_Currency_ID, prepaidAmount.negate().toString(), "", Fact_Acct_Group_ID,
                nextSeqNo(SeqNo), DocumentType, conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = invref.getOrganization().getId();
              fl.doc_C_Currency_ID = invref.getCurrency().getId();
              fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
              fl.isInvoicePayRec = true;
            }
          } else {
            try {
              DocInvoiceData[] prepayments = DocInvoiceData.selectPrepayments(connectionProvider,
                  m_payments[i].Line_ID);
              for (int j = 0; j < prepayments.length; j++) {
                Vector<BigDecimal> vTcPreOut = new Vector<BigDecimal>();
                BigDecimal prePaymentAmt = convertAmount(
                    new BigDecimal(prepayments[j].prepaidamt).negate(), false, DateAcct,
                    TABLEID_Payment, prepayments[j].finPaymentId, m_payments[i].C_Currency_ID_From,
                    as.m_C_Currency_ID, m_payments[i], as, fact, Fact_Acct_Group_ID,
                    nextSeqNo(SeqNo), conn, vTcPreOut);
                prePaymentAmt = new BigDecimal(prepayments[j].prepaidamt);
                fl = fact.createLine(m_payments[i],
                    (itemDiverse == null) ? getAccountBPartner(C_BPartner_ID, as, false, true, conn)
                        : getAccountGLItem(itemDiverse, as, false, conn),
                    m_payments[i].C_Currency_ID_From, prePaymentAmt.toString(), "",
                    Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, null, vTcPreOut.get(0),
                    conn);
                if (fl != null) {
                  fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                  fl.doc_C_Currency_ID = invref.getCurrency().getId();
                  fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                  fl.isInvoicePayRec = true;
                }
              }
            } catch (ServletException e) {
              log4jDocInvoice.warn(e);
            }
          }
        }
      if ((m_payments == null || m_payments.length == 0)
          && (m_debt_payments == null || m_debt_payments.length == 0)) {

        fl = fact.createLine(null,
            (itemDiverse == null) ? getAccountBPartner(C_BPartner_ID, as, false, false, conn)
                : getAccountGLItem(itemDiverse, as, false, conn),
            this.C_Currency_ID, Amounts[AMTTYPE_Gross], "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
            DocumentType, conn);
        if (fl != null) {
          fl.doc_Ad_Org_ID = invref.getOrganization().getId();
          fl.doc_C_Currency_ID = invref.getCurrency().getId();
          fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
          fl.isInvoicePayRec = true;
        }

      }
      // Charge CR
      fl = fact.createLine(null, getAccount(AcctServer.ACCTTYPE_Charge, as, conn),
          this.C_Currency_ID, "", getAmount(AcctServer.AMTTYPE_Charge), Fact_Acct_Group_ID,
          nextSeqNo(SeqNo), DocumentType, conn);
      if (fl != null) {
        fl.doc_Ad_Org_ID = invref.getOrganization().getId();
        fl.doc_C_Currency_ID = invref.getCurrency().getId();
        fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
      }

      // TaxCredit CR
      for (int i = 0; m_taxes != null && i < m_taxes.length; i++) {
        // New docLine created to assign C_Tax_ID value to the entry
        DocLine docLine = new DocLine(DocumentType, Record_ID, "");
        docLine.m_C_Tax_ID = m_taxes[i].m_C_Tax_ID;
        if (m_taxes[i].m_isTaxUndeductable) {
          computeTaxUndeductableLine(conn, as, fact, docLine, Fact_Acct_Group_ID,
              m_taxes[i].m_C_Tax_ID, m_taxes[i].getAmount(), invoice, invref);

        } else {
          BigDecimal percentageFinalAccount = CashVATUtil._100;
          final BigDecimal taxesAmountTotal = new BigDecimal(
              StringUtils.isBlank(m_taxes[i].getAmount()) ? "0" : m_taxes[i].getAmount());
          BigDecimal taxToTransAccount = BigDecimal.ZERO;
          if (invoice.isScoIsmanualTranstax()) {
            final BigDecimal taxToFinalAccount = taxesAmountTotal.subtract(taxToTransAccount);
            fl = fact.createLine(docLine,
                m_taxes[i].getAccount(DocTax.ACCTTYPE_Man_TaxCredit_Trans, as, conn),
                this.C_Currency_ID, "", taxToFinalAccount.toString(), Fact_Acct_Group_ID,
                nextSeqNo(SeqNo), DocumentType, conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = invref.getOrganization().getId();
              fl.doc_C_Currency_ID = invref.getCurrency().getId();
              fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
            }
          } else {
            if (isCashVAT && m_taxes[i].m_isCashVAT) {
              percentageFinalAccount = CashVATUtil
                  .calculatePrepaidPercentageForCashVATTax(m_taxes[i].m_C_Tax_ID, Record_ID);
              taxToTransAccount = CashVATUtil.calculatePercentageAmount(
                  CashVATUtil._100.subtract(percentageFinalAccount), taxesAmountTotal,
                  C_Currency_ID);
              fl = fact.createLine(docLine,
                  m_taxes[i].getAccount(DocTax.ACCTTYPE_TaxCredit_Trans, as, conn),
                  this.C_Currency_ID, "", taxToTransAccount.toString(), Fact_Acct_Group_ID,
                  nextSeqNo(SeqNo), DocumentType, conn);
              if (fl != null) {
                fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                fl.doc_C_Currency_ID = invref.getCurrency().getId();
                fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
              }
            }
            final BigDecimal taxToFinalAccount = taxesAmountTotal.subtract(taxToTransAccount);
            fl = fact.createLine(docLine,
                m_taxes[i].getAccount(DocTax.ACCTTYPE_TaxCredit, as, conn), this.C_Currency_ID, "",
                taxToFinalAccount.toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,
                conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = invref.getOrganization().getId();
              fl.doc_C_Currency_ID = invref.getCurrency().getId();
              fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
            }
          }
        }
      }

      // Expense CR
      for (int i = 0; p_lines != null && i < p_lines.length; i++) {
        String amount = p_lines[i].getAmount();
        String amountConverted = "";

        ConversionRateDoc conversionRateCurrentDoc = null;

        if (invoice.getScoInvoiceref() != null)
          conversionRateCurrentDoc = getConversionRateDoc(TABLEID_Invoice,
              invoice.getScoInvoiceref().getId(), C_Currency_ID, as.m_C_Currency_ID);
        if (conversionRateCurrentDoc != null) {
          amountConverted = applyRate(new BigDecimal(p_lines[i].getAmount()),
              conversionRateCurrentDoc, true).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        } else {
          amountConverted = getConvertedAmt(p_lines[i].getAmount(), C_Currency_ID,
              as.m_C_Currency_ID, OBDateUtils.formatDate(getDateForInvoiceReference(invoice)), "",
              AD_Client_ID, AD_Org_ID, conn);
        }

        Account deferAcct = null;
        Account acctExtorn = null;

        if (!((DocLine_Invoice) p_lines[i]).m_C_Glitem_ID.equals("")) {

          if (p_lines[i].m_M_Product_ID.equals("") && !p_lines[i].m_C_Glitem_ID.equals("")) {
            if (((DocLine_Invoice) p_lines[i]).getCustomGlDeferId() != null
                && !((DocLine_Invoice) p_lines[i]).getCustomGlDeferId().equals(""))
              deferAcct = getAccountGLItem(OBDal.getInstance().get(GLItem.class,
                  ((DocLine_Invoice) p_lines[i]).getCustomGlDeferId()), as, true, conn);

          } else {
            deferAcct = ((DocLine_Invoice) p_lines[i]).getAccount(ProductInfo.ACCTTYPE_P_DefExpense,
                as, conn);
          }

          acctExtorn = ((DocLine_Invoice) p_lines[i]).getAccount(ProductInfo.ACCTTYPE_P_Expense, as,
              conn);
        } else {
          InvoiceLine invLine = OBDal.getInstance().get(InvoiceLine.class, p_lines[i].m_TrxLine_ID);
          GLItem linediv_acc_glitem = invLine.getScoLinedivAccGlitem();
          if (linediv_acc_glitem != null) {
            deferAcct = getAccountGLItem(linediv_acc_glitem, as, true, conn);
            acctExtorn = deferAcct;
          } else {
            deferAcct = ((DocLine_Invoice) p_lines[i]).getAccount(ProductInfo.ACCTTYPE_P_DefExpense,
                as, conn);
            acctExtorn = ((DocLine_Invoice) p_lines[i]).getAccount(ProductInfo.ACCTTYPE_P_Expense,
                as, conn);
          }
        }

        if (((DocLine_Invoice) p_lines[i]).isDeferred()) {
          amount = createAccDefExpenseFact(fact, (DocLine_Invoice) p_lines[i], acctExtorn,
              deferAcct, amountConverted, as.m_C_Currency_ID, conn, invoice, invref);
          fl = fact.createLine(p_lines[i], acctExtorn, as.m_C_Currency_ID, "", amount,
              Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
          if (fl != null) {
            fl.doc_Ad_Org_ID = invref.getOrganization().getId();
            fl.doc_C_Currency_ID = invref.getCurrency().getId();
            fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
          }

        } else {
          fl = fact.createLine(p_lines[i], acctExtorn, this.C_Currency_ID, "", amount,
              Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
          if (fl != null) {
            fl.doc_Ad_Org_ID = invref.getOrganization().getId();
            fl.doc_C_Currency_ID = invref.getCurrency().getId();
            fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
          }

        }
        // If expense has been deferred
        if (((DocLine_Invoice) p_lines[i]).isDeferred() && !amount.equals(amountConverted)) {
          amount = new BigDecimal(amountConverted).subtract(new BigDecimal(amount)).toString();
          fl = fact.createLine(p_lines[i], deferAcct, as.m_C_Currency_ID, "", amount,
              Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
          if (fl != null) {
            fl.doc_Ad_Org_ID = invref.getOrganization().getId();
            fl.doc_C_Currency_ID = invref.getCurrency().getId();
            fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
          }
        }

      }
      // Set Locations
      FactLine[] fLines = fact.getLines();
      for (int i = 0; fLines != null && i < fLines.length; i++) {
        if (fLines[i] != null) {
          fLines[i].setLocationFromBPartner(C_BPartner_Location_ID, true, conn); // from
          // Loc
          fLines[i].setLocationFromOrg(fLines[i].getAD_Org_ID(conn), false, conn); // to
          // Loc
        }
      }
    } else {

      fl = fact.createLine(null,
          getAccountBPartner(C_BPartner_ID, as, invoice.isSalesTransaction(), false, conn),
          this.C_Currency_ID, "0.00", "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
      if (fl != null) {
        fl.doc_Ad_Org_ID = invref.getOrganization().getId();
        fl.doc_C_Currency_ID = invref.getCurrency().getId();
        fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
      }

    }

    DateAcct = dateAcctTemp;
    SeqNo = "0";

    fact.showFact();

    return fact;
  }// createFact

  String createAccDefRevenueFact(Invoice invoice, Invoice invref, Fact fact, DocLine_Invoice line,
      Account prodRevAccount, Account prodDefRevAccount, String lineAmount, String strCurrencyId,
      ConnectionProvider conn) {
    BigDecimal amount = new BigDecimal(lineAmount);

    if (line.isDeferredOnReceipt()) {
      // No Plan, only when products arrives to the warehouse
      return "0.00";
    }

    String Fact_Acct_Group_ID = SequenceIdData.getUUID();
    ArrayList<HashMap<String, String>> plan = new ArrayList<HashMap<String, String>>();
    Period startingPeriod = OBDal.getInstance().get(Period.class, line.getStartingPeriodId());
    plan = calculateAccDefPlan(startingPeriod, line.getPeriodNumber(), amount, strCurrencyId);
    for (HashMap<String, String> planLine : plan) {
      DocLine planDocLine = new DocLine(DocumentType, Record_ID, line.m_TrxLine_ID);
      planDocLine.copyInfo(line);
      planDocLine.m_DateAcct = planLine.get("date");
      if (IsReversal.equals("Y")) {
        // Revenue Account
        FactLine factline = fact.createLine(planDocLine, prodRevAccount, strCurrencyId,
            planLine.get("amount"), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
        factline.descripcion = "Reversion de Invoice/Factura: " + invoice.getDocumentNo();
        factline.doc_Ad_Org_ID = invref.getOrganization().getId();
        factline.doc_C_Currency_ID = invref.getCurrency().getId();
        factline.doc_C_Doctype_ID = invref.getTransactionDocument().getId();

        // Deferred Revenue Account
        factline = fact.createLine(planDocLine, prodDefRevAccount, strCurrencyId, "",
            planLine.get("amount"), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
        factline.descripcion = "Reversion de Invoice/Factura: " + invoice.getDocumentNo();
        factline.doc_Ad_Org_ID = invref.getOrganization().getId();
        factline.doc_C_Currency_ID = invref.getCurrency().getId();
        factline.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
      } else {
        // Deferred Revenue Account
        FactLine factline = fact.createLine(planDocLine, prodDefRevAccount, strCurrencyId,
            planLine.get("amount"), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
        factline.descripcion = "Reversion de Invoice/Factura: " + invoice.getDocumentNo();
        factline.doc_Ad_Org_ID = invref.getOrganization().getId();
        factline.doc_C_Currency_ID = invref.getCurrency().getId();
        factline.doc_C_Doctype_ID = invref.getTransactionDocument().getId();

        // Revenue Account
        factline = fact.createLine(planDocLine, prodRevAccount, strCurrencyId, "",
            planLine.get("amount"), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
        factline.descripcion = "Reversion de Invoice/Factura: " + invoice.getDocumentNo();
        factline.doc_Ad_Org_ID = invref.getOrganization().getId();
        factline.doc_C_Currency_ID = invref.getCurrency().getId();
        factline.doc_C_Doctype_ID = invref.getTransactionDocument().getId();

      }
      amount = amount.subtract(new BigDecimal(planLine.get("amount")));
      Fact_Acct_Group_ID = SequenceIdData.getUUID();
    }

    return amount.toString();
  }

  private ArrayList<HashMap<String, String>> calculateAccDefPlan(Period startingPeriod,
      int periodNumber, BigDecimal amount, String strCurrencyId) {
    Period period = startingPeriod;
    Date date = period.getEndingDate();
    ArrayList<HashMap<String, String>> plan = new ArrayList<HashMap<String, String>>();
    int i = 1;
    BigDecimal total = BigDecimal.ZERO;
    int stdPrecision = 0;
    OBContext.setAdminMode(true);
    try {
      stdPrecision = OBDal.getInstance().get(Currency.class, this.C_Currency_ID)
          .getStandardPrecision().intValue();
    } finally {
      OBContext.restorePreviousMode();
    }
    BigDecimal periodAmount = amount
        .divide(new BigDecimal(periodNumber), new MathContext(32, RoundingMode.HALF_UP))
        .setScale(stdPrecision, BigDecimal.ROUND_HALF_UP);

    while (i <= periodNumber) {
      if (!OBDateUtils.formatDate(date).equals(DateAcct)) {
        HashMap<String, String> hm = new HashMap<String, String>();
        hm.put("date", OBDateUtils.formatDate(date));
        hm.put("amount",
            i == periodNumber ? amount.subtract(total).toString() : periodAmount.toString());
        plan.add(hm);
      }
      try {
        AcctServerData[] data = AcctServerData.periodOpen(connectionProvider, AD_Client_ID,
            DocumentType, AD_Org_ID, OBDateUtils.formatDate(period.getEndingDate()));
        if ("".equals(data[0].period)) {
          setStatus(STATUS_PeriodClosed);
          throw new IllegalStateException("DocInvoice - Error getting next year period");
        }
      } catch (ServletException e) {
        log4j.warn("DocInvoice - Error checking period open.", e);
        e.printStackTrace();
      }
      if (i < periodNumber) {
        period = AccDefUtility.getNextPeriod(period);
        date = period.getEndingDate();
      }
      total = total.add(periodAmount);
      i++;
    }
    return plan;
  }

  public String createAccDefExpenseFact(Fact fact, DocLine_Invoice line, Account prodExpAccount,
      Account prodDefExpAccount, String lineAmount, String strCurrencyId, ConnectionProvider conn,
      Invoice invoice, Invoice invref) {
    FactLine fl = null;
    BigDecimal amount = new BigDecimal(lineAmount);

    if (line.isDeferredOnReceipt()) {
      // No Plan, only when products arrives to the warehouse
      return "0.00";
    }

    String Fact_Acct_Group_ID = SequenceIdData.getUUID();
    ArrayList<HashMap<String, String>> plan = new ArrayList<HashMap<String, String>>();
    Period startingPeriod = OBDal.getInstance().get(Period.class, line.getStartingPeriodId());
    plan = calculateAccDefPlan(startingPeriod, line.getPeriodNumber(), amount, strCurrencyId);
    for (HashMap<String, String> planLine : plan) {
      DocLine planDocLine = new DocLine(DocumentType, Record_ID, line.m_TrxLine_ID);
      planDocLine.copyInfo(line);
      planDocLine.m_DateAcct = planLine.get("date");
      if (IsReversal.equals("Y")) {
        // Expense Account
        fl = fact.createLine(planDocLine, prodExpAccount, strCurrencyId, "", planLine.get("amount"),
            Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
        if (fl != null) {
          fl.doc_Ad_Org_ID = invref.getOrganization().getId();
          fl.doc_C_Currency_ID = invref.getCurrency().getId();
          fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
        }
        // Deferred Expense Account
        fl = fact.createLine(planDocLine, prodDefExpAccount, strCurrencyId, planLine.get("amount"),
            "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
        if (fl != null) {
          fl.doc_Ad_Org_ID = invref.getOrganization().getId();
          fl.doc_C_Currency_ID = invref.getCurrency().getId();
          fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
        }
      } else {
        // Deferred Expense Account
        fl = fact.createLine(planDocLine, prodDefExpAccount, strCurrencyId, "",
            planLine.get("amount"), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
        if (fl != null) {
          fl.doc_Ad_Org_ID = invref.getOrganization().getId();
          fl.doc_C_Currency_ID = invref.getCurrency().getId();
          fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
        }
        // Expense Account
        fl = fact.createLine(planDocLine, prodExpAccount, strCurrencyId, planLine.get("amount"), "",
            Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);
        if (fl != null) {
          fl.doc_Ad_Org_ID = invref.getOrganization().getId();
          fl.doc_C_Currency_ID = invref.getCurrency().getId();
          fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
        }
      }
      amount = amount.subtract(new BigDecimal(planLine.get("amount")));
      Fact_Acct_Group_ID = SequenceIdData.getUUID();
    }
    return amount.toString();
  }

  /**
   * Update Product Info. - Costing (PriceLastInv) - PO (PriceLastInv)
   * 
   * @param C_AcctSchema_ID
   *          accounting schema
   */
  public void updateProductInfo(String C_AcctSchema_ID, ConnectionProvider conn, Connection con) {
    log4jDocInvoice.debug("updateProductInfo - C_Invoice_ID=" + this.Record_ID);

    /**
     * @todo Last.. would need to compare document/last updated date would need to maintain
     *       LastPriceUpdateDate on _PO and _Costing
     */

    // update Product PO info
    // should only be once, but here for every AcctSchema
    // ignores multiple lines with same product - just uses first
    int no = 0;
    try {
      no = DocInvoiceData.updateProductPO(con, conn, Record_ID);
      log4jDocInvoice.debug("M_Product_PO - Updated=" + no);

    } catch (ServletException e) {
      log4jDocInvoice.warn(e);
      if (e.getMessage().contains("@NoConversionRate@")) {
        setMessageResult(Utility.translateError(conn, RequestContext.get().getVariablesSecureApp(),
            OBContext.getOBContext().getLanguage().getId(), e.getMessage()));
        throw new IllegalStateException();
      }
    }
  } // updateProductInfo

  /**
   * Get Source Currency Balance - subtracts line and tax amounts from total - no rounding
   * 
   * @return positive amount, if total invoice is bigger than lines
   */
  public BigDecimal getBalance() {
    // BigDecimal ZERO = new BigDecimal("0");
    BigDecimal retValue = ZERO;
    StringBuffer sb = new StringBuffer(" [");
    // Total
    retValue = retValue.add(new BigDecimal(getAmount(AcctServer.AMTTYPE_Gross)));
    sb.append(getAmount(AcctServer.AMTTYPE_Gross));
    // - Charge
    retValue = retValue.subtract(new BigDecimal(getAmount(AcctServer.AMTTYPE_Charge)));
    sb.append("-").append(getAmount(AcctServer.AMTTYPE_Charge));
    // - Tax
    for (int i = 0; i < m_taxes.length; i++) {
      retValue = retValue.subtract(new BigDecimal(m_taxes[i].getAmount()));
      sb.append("-").append(m_taxes[i].getAmount());
    }
    // - Lines
    for (int i = 0; p_lines != null && i < p_lines.length; i++) {

      DocLine_Invoice invline = (DocLine_Invoice) p_lines[i];
      if (invline.isNoEmitido())
        continue;

      retValue = retValue.subtract(new BigDecimal(p_lines[i].getAmount()));
      sb.append("-").append(p_lines[i].getAmount());
    }
    sb.append("]");
    //
    log4jDocInvoice.debug("Balance=" + retValue + sb.toString());
    return retValue.setScale(12, RoundingMode.HALF_UP);
  } // getBalance

  public String nextSeqNo(String oldSeqNo) {
    log4jDocInvoice.debug("DocInvoice - oldSeqNo = " + oldSeqNo);
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    log4jDocInvoice.debug("DocInvoice - nextSeqNo = " + SeqNo);
    return SeqNo;
  }

  /**
   * Get the account for Accounting Schema
   * 
   * @param cBPartnerId
   *          business partner id
   * @param as
   *          accounting schema
   * @return Account
   */
  public final Account getAccountBPartner(String cBPartnerId, AcctSchema as, boolean isReceipt,
      String dpStatus, ConnectionProvider conn) {
    DocPaymentData[] data = null;

    try {
      if (log4j.isDebugEnabled())
        log4j.debug("DocInvoice - getAccountBPartner - DocumentType = " + DocumentType);

      String FIN_ScoSpecialMethod = OBDal.getInstance().get(Invoice.class, Record_ID)
          .getPaymentMethod().getScoSpecialmethod();
      String C_ScoSpecialDocType = OBDal.getInstance().get(DocumentType.class, C_DocType_ID)
          .getScoSpecialdoctype();

      if (FIN_ScoSpecialMethod != null && FIN_ScoSpecialMethod.compareTo("SCOBILLOFEXCHANGE") == 0)
        data = DocPaymentData.selectBPartnerLetraAcct(conn, cBPartnerId, as.getC_AcctSchema_ID(),
            dpStatus);
      else if (C_ScoSpecialDocType != null && C_ScoSpecialDocType.compareTo("SCOARDEBITMEMO") == 0)
        data = DocPaymentData.selectBPartnerNotaCargoAcct(conn, cBPartnerId,
            as.getC_AcctSchema_ID(), dpStatus);
      else if (isReceipt) {
        data = DocPaymentData.selectBPartnerCustomerAcct(conn, cBPartnerId, as.getC_AcctSchema_ID(),
            dpStatus);
      } else {
        data = DocPaymentData.selectBPartnerVendorAcct(conn, cBPartnerId, as.getC_AcctSchema_ID(),
            dpStatus);
      }
    } catch (ServletException e) {
      log4j.warn(e);
    }
    // Get Acct
    String Account_ID = "";
    if (data != null && data.length != 0) {
      Account_ID = data[0].accountId;
    } else
      return null;
    // No account
    if (Account_ID.equals("")) {
      log4j.warn("DocInvoice - getAccountBPartner - NO account BPartner=" + cBPartnerId
          + ", Record=" + Record_ID + ", status " + dpStatus);
      return null;
    }
    // Return Account
    Account acct = null;
    try {
      acct = Account.getAccount(conn, Account_ID);
    } catch (ServletException e) {
      log4j.warn(e);
    }
    return acct;
  } // getAccount

  /**
   * @return the log4jDocInvoice
   */
  public static Logger getLog4jDocInvoice() {
    return log4jDocInvoice;
  }

  /**
   * @param log4jDocInvoice
   *          the log4jDocInvoice to set
   */
  public static void setLog4jDocInvoice(Logger log4jDocInvoice) {
    DocInvoice.log4jDocInvoice = log4jDocInvoice;
  }

  /**
   * @return the m_taxes
   */
  public DocTax[] getM_taxes() {
    return m_taxes;
  }

  /**
   * @param m_taxes
   *          the m_taxes to set
   */
  public void setM_taxes(DocTax[] m_taxes) {
    this.m_taxes = m_taxes;
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

  /**
   * Get Document Confirmation
   * 
   */
  public boolean getDocumentConfirmation(ConnectionProvider conn, String strRecordId) {
    DocInvoiceData[] data = null;
    FieldProvider dataFP[] = getObjectFieldProvider();

    Invoice invoice = OBDal.getInstance().get(Invoice.class, strRecordId);
    if (invoice.getPosted().equals("sco_M")) {
      strMessage = "No se puede contabilizar Datos de Migracion";
      setStatus(STATUS_Migrated);
      return false;
    }

    String specialdoctype = invoice.getTransactionDocument().getScoSpecialdoctype() != null
        ? invoice.getTransactionDocument().getScoSpecialdoctype() : "";

    // Deshabilitado para posting porque ya se hace en Manejo de
    // Letras
    if (specialdoctype.equals("SCOARBOEINVOICE") || specialdoctype.equals("SCOAPBOEINVOICE")
        || specialdoctype.equals("SCOAPLOANINVOICE")) {
      setStatus(STATUS_DocumentDisabled);
      return false;
    }

    if (ZERO.compareTo(new BigDecimal(dataFP[0].getField("GrandTotal"))) == 0) {

      boolean zero = true;
      for (InvoiceLine invoiceline : invoice.getInvoiceLineList()) {
        if (ZERO.compareTo(invoiceline.getLineNetAmount()) != 0) {
          zero = false;
        }
        if (ZERO.compareTo(invoiceline.getScoUnissuedlinenetamt()) != 0) {
          zero = false;
        }
      }

      if (zero && !invoice.isSalesTransaction()) {
        strMessage = "@TotalGrossIsZero@";
        setStatus(STATUS_DocumentDisabled);
        return false;
      }

    }
    try {
      data = DocInvoiceData.selectRegistro(conn, AD_Client_ID, strRecordId);
      AcctSchema[] m_acctSchemas = reloadLocalAcctSchemaArray(data[0].adOrgId);

      AcctSchema acct = null;
      for (int i = 0; i < m_acctSchemas.length; i++) {
        acct = m_acctSchemas[i];
        data = DocInvoiceData.selectFinInvCount(conn, strRecordId, acct.m_C_AcctSchema_ID);
        int countFinInv = Integer.parseInt(data[0].fininvcount);
        int countGLItemAcct = Integer.parseInt(data[0].finacctcount);
        // For any GL Item used in financial invoice lines debit/credit
        // accounts must be defined
        if (countFinInv != 0 && (countFinInv != countGLItemAcct)) {
          log4jDocInvoice.debug("DocInvoice - getDocumentConfirmation - GL Item used in financial "
              + "invoice lines debit/credit accounts must be defined.");
          setStatus(STATUS_InvalidAccount);
          return false;
        }
      }
    } catch (Exception e) {
      log4jDocInvoice.error("Exception in getDocumentConfirmation method.", e);
    }

    return true;
  }

  private AcctSchema[] reloadLocalAcctSchemaArray(String adOrgId) throws ServletException {
    AcctSchema acct = null;
    ArrayList<Object> new_as = new ArrayList<Object>();
    // We reload again all the acct schemas of the client
    AcctSchema[] m_aslocal = AcctSchema.getAcctSchemaArray(connectionProvider, AD_Client_ID,
        adOrgId);
    // Filter the right acct schemas for the organization
    for (int i = 0; i < m_aslocal.length; i++) {
      acct = m_aslocal[i];
      if (AcctSchemaData.selectAcctSchemaTable(connectionProvider, acct.m_C_AcctSchema_ID,
          AD_Table_ID)) {
        new_as.add(new AcctSchema(connectionProvider, acct.m_C_AcctSchema_ID));
      }
    }
    AcctSchema[] retValue = new AcctSchema[new_as.size()];
    new_as.toArray(retValue);
    m_aslocal = retValue;
    return m_aslocal;
  }

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method

  public void computeTaxUndeductableLine(ConnectionProvider conn, AcctSchema as, Fact fact,
      DocLine docLine, String Fact_Acct_Group_ID, String taxId, String strTaxAmount,
      Invoice invoice, Invoice invref) {
    int invoiceLineTaxCount = 0;
    int totalInvoiceLineTax = getTaxLineCount(conn, taxId);
    BigDecimal cumulativeTaxLineAmount = new BigDecimal(0);
    BigDecimal taxAmount = new BigDecimal(strTaxAmount.equals("") ? "0.00" : strTaxAmount);
    DocInvoiceData[] data = null;
    try {

      // We can have some lines from product or some lines from general
      // ledger
      data = DocInvoiceData.selectProductAcct(conn, as.getC_AcctSchema_ID(), taxId, Record_ID);
      cumulativeTaxLineAmount = createLineForTaxUndeductable(invoiceLineTaxCount,
          totalInvoiceLineTax, cumulativeTaxLineAmount, taxAmount, data, conn, fact, docLine,
          Fact_Acct_Group_ID, invoice, invref);
      invoiceLineTaxCount = data.length;
      // check whether gl item is selected instead of product in invoice
      // line
      data = DocInvoiceData.selectGLItemAcctForTaxLine(conn, as.getC_AcctSchema_ID(), taxId,
          Record_ID);
      createLineForTaxUndeductable(invoiceLineTaxCount, totalInvoiceLineTax,
          cumulativeTaxLineAmount, taxAmount, data, conn, fact, docLine, Fact_Acct_Group_ID,
          invoice, invref);
    } catch (ServletException e) {
      log4jDocInvoice.error("Exception in computeTaxUndeductableLine method: " + e);
    }
  }

  private int getTaxLineCount(ConnectionProvider conn, String taxId) {
    DocInvoiceData[] data = null;
    try {
      data = DocInvoiceData.getTaxLineCount(conn, taxId, Record_ID);
    } catch (ServletException e) {
      log4jDocInvoice.error("Exception in getTaxLineCount method: " + e);
    }
    if (data.length > 0) {
      return Integer.parseInt(data[0].totallines);
    }
    return 0;
  }

  private BigDecimal createLineForTaxUndeductable(int invoiceLineTaxCount, int totalInvoiceLineTax,
      BigDecimal cumulativeTaxLineAmount, BigDecimal taxAmount, DocInvoiceData[] data,
      ConnectionProvider conn, Fact fact, DocLine docLine, String Fact_Acct_Group_ID,
      Invoice invoice, Invoice invref) {
    FactLine fl = null;
    for (int j = 0; j < data.length; j++) {
      invoiceLineTaxCount++;
      // We have to adjust the amount in last line of tax
      if (invoiceLineTaxCount == totalInvoiceLineTax) {
        data[j].taxamt = taxAmount.subtract(cumulativeTaxLineAmount).toPlainString();
      }
      try {
        // currently applicable for API and APC
        if (this.DocumentType.equals(AcctServer.DOCTYPE_APInvoice)) {
          if (IsReversal.equals("Y")) {
            fl = fact.createLine(docLine, Account.getAccount(conn, data[j].pExpenseAcct),
                this.C_Currency_ID, "", data[j].taxamt, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
                DocumentType, conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = invref.getOrganization().getId();
              fl.doc_C_Currency_ID = invref.getCurrency().getId();
              fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
            }

          } else {
            fl = fact.createLine(docLine, Account.getAccount(conn, data[j].pExpenseAcct),
                this.C_Currency_ID, data[j].taxamt, "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
                DocumentType, conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = invref.getOrganization().getId();
              fl.doc_C_Currency_ID = invref.getCurrency().getId();
              fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
            }
          }
        } else if (this.DocumentType.equals(AcctServer.DOCTYPE_APCredit)) {
          fl = fact.createLine(docLine, Account.getAccount(conn, data[j].pExpenseAcct),
              this.C_Currency_ID, "", data[j].taxamt, Fact_Acct_Group_ID, nextSeqNo(SeqNo),
              DocumentType, conn);
          if (fl != null) {
            fl.doc_Ad_Org_ID = invref.getOrganization().getId();
            fl.doc_C_Currency_ID = invref.getCurrency().getId();
            fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
          }
        }
        cumulativeTaxLineAmount = cumulativeTaxLineAmount.add(new BigDecimal(data[j].taxamt));
      } catch (ServletException e) {
        log4jDocInvoice.error("Exception in createLineForTaxUndeductable method: " + e);
      }

    }

    return cumulativeTaxLineAmount;
  }
}
