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
 * All portions are Copyright (C) 2010-2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.CashVATUtil;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.CustomerAccounts;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.AcctSchemaTableDocType;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.accounting.FIN_FinancialAccountAccounting;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchemaTable;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.gl.GLJournalLine;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentScheduleDetail;
import org.openbravo.model.financialmgmt.payment.FIN_Payment_Credit;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;

import pe.com.unifiedgo.accounting.data.SCOBillofexchange;
import pe.com.unifiedgo.accounting.data.SCOBoetodiscLine;
import pe.com.unifiedgo.accounting.data.SCOFactinvLine;
import pe.com.unifiedgo.accounting.data.SCOInternalDoc;
import pe.com.unifiedgo.accounting.data.SCOPrepayment;
import pe.com.unifiedgo.accounting.data.ScoRendicioncuentas;

public class DocFINPayment extends AcctServer {
  private static final long serialVersionUID = 1L;
  static Logger log4j = Logger.getLogger(DocFINPayment.class);

  String SeqNo = "0";
  String generatedAmount = "";
  String usedAmount = "";

  public DocFINPayment() {
  }

  public DocFINPayment(String AD_Client_ID, String AD_Org_ID,
      ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  @Override
  public boolean loadDocumentDetails(FieldProvider[] data, ConnectionProvider conn) {
    DateDoc = data[0].getField("PaymentDate");
    Amounts[AMTTYPE_Gross] = data[0].getField("Amount");
    generatedAmount = data[0].getField("GeneratedCredit");
    usedAmount = data[0].getField("UsedCredit");
    loadDocumentType();
    p_lines = loadLines();
    return true;
  }

  public FieldProviderFactory[] loadLinesFieldProvider(String Id) {
    FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, Id);
    List<FIN_PaymentDetail> paymentDetails = FIN_Utility.getOrderedPaymentDetailList(payment);
    if (paymentDetails == null)
      return null;

    FieldProviderFactory[] data = new FieldProviderFactory[paymentDetails.size()];
    FIN_PaymentSchedule ps = null;
    FIN_PaymentDetail pd = null;

    String isDetraccPayment = "N";
    if (payment.isScoDetractionpayment() != null && payment.isScoDetractionpayment() == true) {
      isDetraccPayment = "Y";
    }

    OBContext.setAdminMode();
    try {
      for (int i = 0; i < data.length; i++) {
        // Details refunded used credit are excluded as the entry will
        // be created using the credit
        // used
        if (paymentDetails.get(i).isRefund() && paymentDetails.get(i).isPrepayment()) {
          continue;
        }

        // If the Payment Detail has already been processed, skip it
        if (paymentDetails.get(i).equals(pd)) {
          continue;
        }
        pd = paymentDetails.get(i);

        data[i] = new FieldProviderFactory(null);

        FIN_PaymentSchedule psi = null;
        FIN_PaymentSchedule pso = null;
        SCOPrepayment prepayment = null;// prepayment de compra
        ScoRendicioncuentas rendcuentas = null; // entrega a rendir

        if (paymentDetails.get(i).getSCOPrepayment() != null)
          prepayment = paymentDetails.get(i).getSCOPrepayment();
        else if (paymentDetails.get(i).getScoRendcuentas() != null)
          rendcuentas = paymentDetails.get(i).getScoRendcuentas();
        else {
          psi = paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
              .getInvoicePaymentSchedule();
          pso = paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
              .getOrderPaymentSchedule();
        }

        // Related to Issue Issue 19567. Some Payment Detail's amount
        // and writeoff amount are merged
        // into one.
        // https://issues.openbravo.com/view.php?id=19567
        HashMap<String, BigDecimal> amountAndWriteOff = getPaymentDetailWriteOffAndAmount(
            paymentDetails, ps, psi, pso, i);
        BigDecimal amount = amountAndWriteOff.get("amount");
        BigDecimal payamount = amountAndWriteOff.get("amountpay");
        BigDecimal writeOff = amountAndWriteOff.get("writeoff");

        FieldProviderFactory.setField(data[i], "C_Currency_ID", pd.getScoDocCurrency().getId());

        if (prepayment != null) {

          FieldProviderFactory.setField(data[i], "Amount", amount.toString());
          FieldProviderFactory.setField(data[i], "PaymentAmount", payamount.toString());
          FieldProviderFactory.setField(data[i], "AD_Client_ID",
              paymentDetails.get(i).getClient().getId());
          FieldProviderFactory.setField(data[i], "AD_Org_ID",
              paymentDetails.get(i).getOrganization().getId());
          FieldProviderFactory.setField(data[i], "FIN_Payment_Detail_ID",
              paymentDetails.get(i).getId());
          FieldProviderFactory.setField(data[i], "cBpartnerId",
              prepayment.getBusinessPartner().getId());
          FieldProviderFactory.setField(data[i], "DoubtFulDebtAmount", "0.00");
          FieldProviderFactory.setField(data[i], "WriteOffAmt", writeOff.toString());
          FieldProviderFactory.setField(data[i], "C_GLItem_ID",
              prepayment.getPaymentglitem() != null ? prepayment.getPaymentglitem().getId() : "");
          FieldProviderFactory.setField(data[i], "Refund",
              paymentDetails.get(i).isRefund() ? "Y" : "N");
          FieldProviderFactory.setField(data[i], "isprepayment", "N");
          FieldProviderFactory.setField(data[i], "isoverpayment", "N");
          FieldProviderFactory.setField(data[i], "isPaymentDatePriorToInvoiceDate", "N");
          FieldProviderFactory.setField(data[i], "cProjectId",
              prepayment.getProject() != null ? prepayment.getProject().getId() : "");
          FieldProviderFactory.setField(data[i], "cCampaignId", "");
          FieldProviderFactory.setField(data[i], "cActivityId", "");
          FieldProviderFactory.setField(data[i], "mProductId", "");
          FieldProviderFactory.setField(data[i], "cSalesregionId", "");
          FieldProviderFactory.setField(data[i], "cCostcenterId", "");
          FieldProviderFactory.setField(data[i], "user1Id", "");
          FieldProviderFactory.setField(data[i], "user2Id", "");
          FieldProviderFactory.setField(data[i], "internalDoc", "");
          FieldProviderFactory.setField(data[i], "invoiceRef", "");
          FieldProviderFactory.setField(data[i], "scoEeffCashflowId", "");
          FieldProviderFactory.setField(data[i], "isDetraccPayment", isDetraccPayment);
          FieldProviderFactory.setField(data[i], "ConvertRate",
              paymentDetails.get(i).getScoConvertRate() != null
                  ? paymentDetails.get(i).getScoConvertRate().toString()
                  : "");

        } else if (rendcuentas != null) {

          FieldProviderFactory.setField(data[i], "Amount", amount.toString());
          FieldProviderFactory.setField(data[i], "PaymentAmount", payamount.toString());
          FieldProviderFactory.setField(data[i], "AD_Client_ID",
              paymentDetails.get(i).getClient().getId());
          FieldProviderFactory.setField(data[i], "AD_Org_ID",
              paymentDetails.get(i).getOrganization().getId());
          FieldProviderFactory.setField(data[i], "FIN_Payment_Detail_ID",
              paymentDetails.get(i).getId());
          FieldProviderFactory.setField(data[i], "cBpartnerId",
              rendcuentas.getBusinessPartner().getId());
          FieldProviderFactory.setField(data[i], "DoubtFulDebtAmount", "0.00");

          FieldProviderFactory.setField(data[i], "WriteOffAmt", writeOff.toString());

          String strGlitemId = rendcuentas.getOrganization().getScoRendcuentasGlitem().getId();
          FieldProviderFactory.setField(data[i], "C_GLItem_ID", strGlitemId);
          FieldProviderFactory.setField(data[i], "Refund",
              paymentDetails.get(i).isRefund() ? "Y" : "N");
          FieldProviderFactory.setField(data[i], "isprepayment", "N");
          FieldProviderFactory.setField(data[i], "isoverpayment", "N");
          FieldProviderFactory.setField(data[i], "isPaymentDatePriorToInvoiceDate", "N");
          FieldProviderFactory.setField(data[i], "cProjectId",
              rendcuentas.getProject() != null ? rendcuentas.getProject().getId() : "");
          FieldProviderFactory.setField(data[i], "cCampaignId", "");
          FieldProviderFactory.setField(data[i], "cActivityId", "");
          FieldProviderFactory.setField(data[i], "mProductId", "");
          FieldProviderFactory.setField(data[i], "cSalesregionId", "");
          FieldProviderFactory.setField(data[i], "cCostcenterId", "");
          FieldProviderFactory.setField(data[i], "user1Id", "");
          FieldProviderFactory.setField(data[i], "user2Id", "");
          FieldProviderFactory.setField(data[i], "internalDoc", "");
          FieldProviderFactory.setField(data[i], "invoiceRef", "");
          FieldProviderFactory.setField(data[i], "scoEeffCashflowId", "");
          FieldProviderFactory.setField(data[i], "isDetraccPayment", isDetraccPayment);
          FieldProviderFactory.setField(data[i], "ConvertRate",
              paymentDetails.get(i).getScoConvertRate() != null
                  ? paymentDetails.get(i).getScoConvertRate().toString()
                  : "");
        } else {

          if (amount == null) {
            data[i] = null;
            ps = psi;
            continue;
          } else {
            FieldProviderFactory.setField(data[i], "Amount", amount.toString());
            FieldProviderFactory.setField(data[i], "PaymentAmount", payamount.toString());
          }
          ps = psi;

          FieldProviderFactory.setField(data[i], "AD_Client_ID",
              paymentDetails.get(i).getClient().getId());
          FieldProviderFactory.setField(data[i], "AD_Org_ID",
              paymentDetails.get(i).getOrganization().getId());
          FieldProviderFactory.setField(data[i], "FIN_Payment_Detail_ID",
              paymentDetails.get(i).getId());
          // Calculate Business Partner from payment header or from
          // details if header is null or
          // from
          // the PSD in case of GL Item
          BusinessPartner bPartner = payment.getBusinessPartner() != null
              ? payment.getBusinessPartner()
              : (paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                  .getInvoicePaymentSchedule() != null
                      ? paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                          .getInvoicePaymentSchedule().getInvoice().getBusinessPartner()
                      : (paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                          .getOrderPaymentSchedule() != null
                              ? paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                                  .getOrderPaymentSchedule().getOrder().getBusinessPartner()
                              : (paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                                  .getBusinessPartner())));
          FieldProviderFactory.setField(data[i], "cBpartnerId",
              bPartner != null ? bPartner.getId() : "");
          FieldProviderFactory.setField(data[i], "DoubtFulDebtAmount", paymentDetails.get(i)
              .getFINPaymentScheduleDetailList().get(0).getDoubtfulDebtAmount().toString());
          FieldProviderFactory.setField(data[i], "WriteOffAmt", writeOff.toString());
          FieldProviderFactory.setField(data[i], "C_GLItem_ID",
              paymentDetails.get(i).getGLItem() != null ? paymentDetails.get(i).getGLItem().getId()
                  : "");
          // PARA BOE
          FieldProviderFactory.setField(data[i], "Sco_Discboe_ID",
              paymentDetails.get(i).getScoDiscboe() != null
                  ? paymentDetails.get(i).getScoDiscboe().getId()
                  : "");

          // PARA FACTORING INVOICE
          FieldProviderFactory.setField(data[i], "Sco_Factoredinv_ID",
              paymentDetails.get(i).getScoFactoredinv() != null
                  ? paymentDetails.get(i).getScoFactoredinv().getId()
                  : "");
          // PARA LOAN DOCUMENT
          FieldProviderFactory.setField(data[i], "Sco_Loandocinv_ID",
              paymentDetails.get(i).getScoLoandocliInv() != null
                  ? paymentDetails.get(i).getScoLoandocliInv().getId()
                  : "");

          FieldProviderFactory.setField(data[i], "Sco_Prepayment_ID",
              paymentDetails.get(i).getScoPayoutprepayment() != null
                  ? paymentDetails.get(i).getScoPayoutprepayment()
                  : "");

          FieldProviderFactory.setField(data[i], "Sco_Rendcuentas_ID",
              paymentDetails.get(i).getScoPayoutrendcuentas() != null
                  ? paymentDetails.get(i).getScoPayoutrendcuentas()
                  : "");

          FieldProviderFactory.setField(data[i], "Refund",
              paymentDetails.get(i).isRefund() ? "Y" : "N");

          FieldProviderFactory.setField(data[i], "Refund",
              paymentDetails.get(i).isRefund() ? "Y" : "N");
          // Check if payment against invoice is in a previous date
          // than invoice accounting date
          boolean isPaymentDatePriorToInvoiceDate = isPaymentDatePriorToInvoiceDate(
              paymentDetails.get(i));
          FieldProviderFactory.setField(data[i], "isprepayment",
              paymentDetails.get(i).isPrepayment() ? "Y" : "N");// (isPaymentDatePriorToInvoiceDate
                                                                // ?
                                                                // "Y"
                                                                // :
                                                                // "N"));
          FieldProviderFactory.setField(data[i], "isoverpayment",
              paymentDetails.get(i).isScoIsoverpayment() ? "Y" : "N");

          FieldProviderFactory.setField(data[i], "internalDoc",
              paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                  .getScoInternalDoc() != null
                      ? paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                          .getScoInternalDoc().getId()
                      : "");

          FieldProviderFactory.setField(data[i], "invoiceRef",
              paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                  .getScoInvoiceGlref() != null
                      ? paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                          .getScoInvoiceGlref().getId()
                      : "");

          FieldProviderFactory.setField(data[i], "scoEeffCashflowId",
              paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                  .getScoEeffCashflow() != null
                      ? paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                          .getScoEeffCashflow().getId()
                      : "");

          FieldProviderFactory.setField(data[i], "isDetraccPayment", isDetraccPayment);
          FieldProviderFactory.setField(data[i], "ConvertRate",
              paymentDetails.get(i).getScoConvertRate() != null
                  ? paymentDetails.get(i).getScoConvertRate().toString()
                  : "");

          FieldProviderFactory.setField(data[i], "isPaymentDatePriorToInvoiceDate",
              isPaymentDatePriorToInvoiceDate && !paymentDetails.get(i).isPrepayment() ? "Y" : "N");
          FieldProviderFactory.setField(data[i], "cProjectId", paymentDetails.get(i)
              .getFINPaymentScheduleDetailList().get(0).getInvoicePaymentSchedule() != null
              && paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                  .getInvoicePaymentSchedule().getInvoice().getProject() != null
                      ? paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                          .getInvoicePaymentSchedule().getInvoice().getProject().getId()
                      : (paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                          .getOrderPaymentSchedule() != null
                          && paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                              .getOrderPaymentSchedule().getOrder().getProject() != null
                                  ? paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                                      .getOrderPaymentSchedule().getOrder().getProject().getId()
                                  : (paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                                      .getProject() != null
                                          ? paymentDetails.get(i).getFINPaymentScheduleDetailList()
                                              .get(0).getProject().getId()
                                          : "")));
          FieldProviderFactory.setField(data[i], "cCampaignId", paymentDetails.get(i)
              .getFINPaymentScheduleDetailList().get(0).getInvoicePaymentSchedule() != null
              && paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                  .getInvoicePaymentSchedule().getInvoice().getSalesCampaign() != null
                      ? paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                          .getInvoicePaymentSchedule().getInvoice().getSalesCampaign().getId()
                      : (paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                          .getOrderPaymentSchedule() != null
                          && paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                              .getOrderPaymentSchedule().getOrder().getSalesCampaign() != null
                                  ? paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                                      .getOrderPaymentSchedule().getOrder().getSalesCampaign()
                                      .getId()
                                  : (paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                                      .getSalesCampaign() != null
                                          ? paymentDetails.get(i).getFINPaymentScheduleDetailList()
                                              .get(0).getSalesCampaign().getId()
                                          : "")));
          FieldProviderFactory.setField(data[i], "cActivityId", paymentDetails.get(i)
              .getFINPaymentScheduleDetailList().get(0).getInvoicePaymentSchedule() != null
              && paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                  .getInvoicePaymentSchedule().getInvoice().getActivity() != null
                      ? paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                          .getInvoicePaymentSchedule().getInvoice().getActivity().getId()
                      : (paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                          .getOrderPaymentSchedule() != null
                          && paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                              .getOrderPaymentSchedule().getOrder().getActivity() != null
                                  ? paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                                      .getOrderPaymentSchedule().getOrder().getActivity().getId()
                                  : (paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                                      .getActivity() != null
                                          ? paymentDetails.get(i).getFINPaymentScheduleDetailList()
                                              .get(0).getActivity().getId()
                                          : "")));
          FieldProviderFactory.setField(data[i], "mProductId",
              paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0).getProduct() != null
                  ? paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0).getProduct()
                      .getId()
                  : "");
          FieldProviderFactory.setField(data[i], "cSalesregionId",
              paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                  .getSalesRegion() != null
                      ? paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                          .getSalesRegion().getId()
                      : "");
          FieldProviderFactory.setField(data[i], "cCostcenterId", paymentDetails.get(i)
              .getFINPaymentScheduleDetailList().get(0).getInvoicePaymentSchedule() != null
              && paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                  .getInvoicePaymentSchedule().getInvoice().getCostcenter() != null
                      ? paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                          .getInvoicePaymentSchedule().getInvoice().getCostcenter().getId()
                      : (paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                          .getOrderPaymentSchedule() != null
                          && paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                              .getOrderPaymentSchedule().getOrder().getCostcenter() != null
                                  ? paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                                      .getOrderPaymentSchedule().getOrder().getCostcenter().getId()
                                  : (paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                                      .getCostCenter() != null
                                          ? paymentDetails.get(i).getFINPaymentScheduleDetailList()
                                              .get(0).getCostCenter().getId()
                                          : "")));

          FieldProviderFactory.setField(data[i], "user1Id", paymentDetails.get(i)
              .getFINPaymentScheduleDetailList().get(0).getInvoicePaymentSchedule() != null
              && paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                  .getInvoicePaymentSchedule().getInvoice().getStDimension() != null
                      ? paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                          .getInvoicePaymentSchedule().getInvoice().getStDimension().getId()
                      : (paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                          .getOrderPaymentSchedule() != null
                          && paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                              .getOrderPaymentSchedule().getOrder().getStDimension() != null
                                  ? paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                                      .getOrderPaymentSchedule().getOrder().getStDimension().getId()
                                  : (paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                                      .getStDimension() != null
                                          ? paymentDetails.get(i).getFINPaymentScheduleDetailList()
                                              .get(0).getStDimension().getId()
                                          : "")));
          FieldProviderFactory.setField(data[i], "user2Id", paymentDetails.get(i)
              .getFINPaymentScheduleDetailList().get(0).getInvoicePaymentSchedule() != null
              && paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                  .getInvoicePaymentSchedule().getInvoice().getNdDimension() != null
                      ? paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                          .getInvoicePaymentSchedule().getInvoice().getNdDimension().getId()
                      : (paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                          .getOrderPaymentSchedule() != null
                          && paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                              .getOrderPaymentSchedule().getOrder().getNdDimension() != null
                                  ? paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                                      .getOrderPaymentSchedule().getOrder().getNdDimension().getId()
                                  : (paymentDetails.get(i).getFINPaymentScheduleDetailList().get(0)
                                      .getNdDimension() != null
                                          ? paymentDetails.get(i).getFINPaymentScheduleDetailList()
                                              .get(0).getNdDimension().getId()
                                          : "")));
        }

      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return data;
  }

  private DocLine[] loadLines() {
    ArrayList<Object> list = new ArrayList<Object>();
    FieldProviderFactory[] data = loadLinesFieldProvider(Record_ID);
    if (data == null || data.length == 0)
      return null;
    for (int i = 0; i < data.length; i++) {
      if (data[i] == null)
        continue;
      String Line_ID = data[i].getField("FIN_Payment_Detail_ID");
      OBContext.setAdminMode();
      try {
        FIN_PaymentDetail detail = OBDal.getInstance().get(FIN_PaymentDetail.class, Line_ID);
        DocLine_FINPayment docLine = new DocLine_FINPayment(DocumentType, Record_ID, Line_ID);
        docLine.loadAttributes(data[i], this);

        docLine.m_description = detail.getScoDescription();
        docLine.setAmount(data[i].getField("Amount"));
        docLine.setPaymentAmount(data[i].getField("PaymentAmount"));
        docLine.setIsPrepayment(data[i].getField("isprepayment"));
        docLine.setScoIsOverpayment(data[i].getField("isoverpayment"));
        docLine.setWriteOffAmt(data[i].getField("WriteOffAmt"));
        docLine.setDoubtFulDebtAmount(new BigDecimal(data[i].getField("DoubtFulDebtAmount")));
        docLine.setC_GLItem_ID(data[i].getField("C_GLItem_ID"));
        docLine.Sco_Discboe_ID = data[i].getField("Sco_Discboe_ID");
        docLine.Sco_Factoredinv_ID = data[i].getField("Sco_Factoredinv_ID");
        docLine.Sco_Loandocinv_ID = data[i].getField("Sco_Loandocinv_ID");
        docLine.Sco_Eeff_Cashflow_ID = data[i].getField("scoEeffCashflowId");
        docLine.Sco_Prepayment_ID = data[i].getField("Sco_Prepayment_ID");
        docLine.Sco_Rendcuentas_ID = data[i].getField("Sco_Rendcuentas_ID");
        docLine.setPrepaymentAgainstInvoice(false);// "Y".equals(data[i].getField("isPaymentDatePriorToInvoiceDate"))
                                                   // ? true : false);
        docLine.setCurrency(data[i].getField("C_Currency_ID"));

        docLine.setIsDetraccPayment(data[i].getField("isDetraccPayment"));
        docLine.setConverRate(data[i].getField("ConvertRate"));
        docLine.setIsReceipt(detail.getFinPayment().isReceipt() ? "Y" : "N");

        if (detail.getSCOPrepayment() != null) {
          docLine.setInvoice(null);
          docLine.setSCOPrepayment(detail.getSCOPrepayment());
        } else if (detail.getScoRendcuentas() != null) {

          docLine.setInvoice(null);
          docLine.setSCORendcuentas(detail.getScoRendcuentas());

        } else {

          docLine.setInvoice(detail.getFINPaymentScheduleDetailList() != null
              && detail.getFINPaymentScheduleDetailList().get(0).getInvoicePaymentSchedule() != null
                  ? detail.getFINPaymentScheduleDetailList().get(0).getInvoicePaymentSchedule()
                      .getInvoice()
                  : null);
        }

        docLine.setInvoiceTaxCashVAT_V(Line_ID);

        Invoice invoice = docLine.getInvoice();
        if (invoice != null) {
          docLine.m_Record_Id3 = invoice.getId();
          docLine.m_AD_Table3_Id = "318";
        } else {
          String internalDoc = data[i].getField("internalDoc");
          if (!internalDoc.equals("")) {
            docLine.m_Record_Id3 = internalDoc;
            docLine.m_AD_Table3_Id = "A64BF5FB928C4EC1BACC023D6DC87F3C";
          }

          String invoiceRef = data[i].getField("invoiceRef");
          if (!invoiceRef.equals("")) {
            docLine.m_Record_Id3 = invoiceRef;
            docLine.m_AD_Table3_Id = "318";
          }

          if (data[i].getField("Sco_Prepayment_ID") != null
              && !data[i].getField("Sco_Prepayment_ID").equals("")) {
            docLine.m_Record_Id3 = data[i].getField("Sco_Prepayment_ID");
            docLine.m_AD_Table3_Id = "135FAE2571DB4028A90D5CAA6FAC154C";
          }

          if (data[i].getField("Sco_Rendcuentas_ID") != null
              && !data[i].getField("Sco_Rendcuentas_ID").equals("")) {
            docLine.m_Record_Id3 = data[i].getField("Sco_Rendcuentas_ID");
            docLine.m_AD_Table3_Id = "F90F4E012DF74D2B92BACC79473FF588";
          }
        }

        docLine.m_Record_Id2 = data[i].getField("FIN_Payment_Detail_ID");

        list.add(docLine);
      } finally {
        OBContext.restorePreviousMode();
      }
    }
    // Return Array
    DocLine_FINPayment[] dl = new DocLine_FINPayment[list.size()];
    list.toArray(dl);
    return dl;
  } // loadLines

  @Override
  public Fact createFact(AcctSchema as, ConnectionProvider conn, Connection con,
      VariablesSecureApp vars) throws ServletException {
    // Select specific definition

    String strClassname = "";
    final StringBuilder whereClause = new StringBuilder();
    Fact fact = new Fact(this, as, Fact.POST_Actual);
    String Fact_Acct_Group_ID = SequenceIdData.getUUID();
    String Fact_Acct_Group_ID2 = SequenceIdData.getUUID();
    String Fact_Acct_Group_ID3 = SequenceIdData.getUUID();

    FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, this.Record_ID);

    isReceiptForConversion = false;

    boolean bookDifferences = true;// !payment.getAccount().getCurrency().getId().equals(as.m_C_Currency_ID);

    boolean tcPago = false;
    if (payment.isScoIsapppayment() && !payment.isReceipt()
        && payment.getAmount().compareTo(BigDecimal.ZERO) == 0)
      tcPago = true; // no aplica si hay mas de 1 documento

    OBContext.setAdminMode();
    try {
      whereClause.append(" as astdt ");
      whereClause.append(
          " where astdt.acctschemaTable.accountingSchema.id = '" + as.m_C_AcctSchema_ID + "'");
      whereClause.append(" and astdt.acctschemaTable.table.id = '" + AD_Table_ID + "'");
      whereClause.append(" and astdt.documentCategory = '" + DocumentType + "'");

      final OBQuery<AcctSchemaTableDocType> obqParameters = OBDal.getInstance()
          .createQuery(AcctSchemaTableDocType.class, whereClause.toString());
      final List<AcctSchemaTableDocType> acctSchemaTableDocTypes = obqParameters.list();

      if (acctSchemaTableDocTypes != null && acctSchemaTableDocTypes.size() > 0)
        strClassname = acctSchemaTableDocTypes.get(0).getCreatefactTemplate().getClassname();

      if (strClassname.equals("")) {
        final StringBuilder whereClause2 = new StringBuilder();

        whereClause2.append(" as ast ");
        whereClause2.append(" where ast.accountingSchema.id = '" + as.m_C_AcctSchema_ID + "'");
        whereClause2.append(" and ast.table.id = '" + AD_Table_ID + "'");

        final OBQuery<AcctSchemaTable> obqParameters2 = OBDal.getInstance()
            .createQuery(AcctSchemaTable.class, whereClause2.toString());
        final List<AcctSchemaTable> acctSchemaTables = obqParameters2.list();
        if (acctSchemaTables != null && acctSchemaTables.size() > 0
            && acctSchemaTables.get(0).getCreatefactTemplate() != null)
          strClassname = acctSchemaTables.get(0).getCreatefactTemplate().getClassname();
      }
      if (!strClassname.equals("")) {
        try {
          DocFINPaymentTemplate newTemplate = (DocFINPaymentTemplate) Class.forName(strClassname)
              .newInstance();
          return newTemplate.createFact(this, as, conn, con, vars);
        } catch (Exception e) {
          log4j.error("Error while creating new instance for DocFINPaymentTemplate - ", e);
        }
      }

      boolean isReceipt = DocumentType.equals("ARR");
      boolean isLetraDsctoPuente = false;
      String acctPuenteDscto = "";
      BigDecimal sobranteDscto = new BigDecimal(Amounts[AMTTYPE_Gross]);

      BigDecimal amtInDscto = new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);

      boolean isFactoringInvPuente = false;
      String acctPuenteFactoringInv = "";

      for (int i = 0; p_lines != null && i < p_lines.length; i++) {
        DocLine_FINPayment line = (DocLine_FINPayment) p_lines[i];

        System.out.println("inside5:" + line.Line_ID + " " + line.getAmount());

        BigDecimal taxCashVatAmount = new BigDecimal(0);

        if (line.getSCOPrepayment() == null && line.getSCORendcuentas() == null) {
          FIN_PaymentScheduleDetail detail = OBDal.getInstance()
              .get(FIN_PaymentDetail.class, line.Line_ID).getFINPaymentScheduleDetailList().get(0);
          if (detail != null && detail.getInvoicePaymentSchedule() != null)
            C_Invoice_ID_Ref = detail.getInvoicePaymentSchedule().getInvoice().getId();
        }

        boolean isPrepayment = line.getIsPrepayment().equals("Y");
        boolean isOverpayment = line.getScoIsOverpayment().equals("Y");
        String bpartnerId = (line.m_C_BPartner_ID == null || line.m_C_BPartner_ID.equals(""))
            ? this.C_BPartner_ID
            : line.m_C_BPartner_ID;

        String strcCurrencyId = line.getCurrency();
        String bpAmount = line.getAmount();
        if (line.WriteOffAmt != null && !line.WriteOffAmt.equals("")
            && new BigDecimal(line.WriteOffAmt).compareTo(ZERO) != 0) {

          String lossaccount = AcctServer.ACCTTYPE_WriteOff;
          boolean isloss = false;

          if (isReceipt && new BigDecimal(line.WriteOffAmt).compareTo(ZERO) > 0) {
            isloss = true;
          } else if (isReceipt && new BigDecimal(line.WriteOffAmt).compareTo(ZERO) < 0) {
            isloss = false;
          } else if (!isReceipt && new BigDecimal(line.WriteOffAmt).compareTo(ZERO) > 0) {
            isloss = false;
          } else if (!isReceipt && new BigDecimal(line.WriteOffAmt).compareTo(ZERO) < 0) {
            isloss = true;
          }

          Account account = isloss ? getAccount(AcctServer.ACCTTYPE_WriteOff, as, conn)
              : getAccount(AcctServer.ACCTTYPE_WriteOff_Revenue, as, conn);
          if (account == null) {
            lossaccount = AcctServer.ACCTTYPE_WriteOffDefault;
            account = isloss ? getAccount(AcctServer.ACCTTYPE_WriteOffDefault, as, conn)
                : getAccount(AcctServer.ACCTTYPE_WriteOffDefault_Revenue, as, conn);
          }
          FactLine fl = fact.createLine(line, account, strcCurrencyId,
              (isReceipt ? line.WriteOffAmt : ""), (isReceipt ? "" : line.WriteOffAmt),
              Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, conn);// ///////////////////
          if (isloss) {
            fl.setCostcenter(getCostcenter(lossaccount, as, conn));
            fl.getM_docLine().m_C_Costcenter_ID = getCostcenter(lossaccount, as, conn);
          }

          bpAmount = new BigDecimal(bpAmount).add(new BigDecimal(line.WriteOffAmt)).toString();
        }
        System.out.println("inside4:" + line.Line_ID + " " + line.getAmount());
        if ("".equals(line.getC_GLItem_ID())) {

          boolean docissotrx = isReceipt;
          if (line.getInvoice() != null) {
            docissotrx = line.getInvoice().isSalesTransaction();
          }

          String bpAmountConverted = bpAmount;
          Invoice invoice = line.getInvoice();
          Invoice invref = null;
          if (invoice != null) {
            invref = AcctServer.getInvoicerefForMapping(invoice);
          }
          DocumentType docType = null;
          if (invoice != null)
            docType = invoice.getDocumentType();

          String m_typeDoc = "00";
          if (docType != null) {
            String specialDocType = docType.getScoSpecialdoctype();
            if (specialDocType == null) {
              m_typeDoc = "00";
            } else if (specialDocType.equals("SCOAPINVOICE")
                || specialDocType.equals("SCOAPSIMPLEPROVISIONINVOICE")
                || specialDocType.equals("SCOAPBOEINVOICE")
                || specialDocType.equals("SCOAPLOANINVOICE")) {
              m_typeDoc = invoice.getScoPodoctypeComboitem().getCode();
            } else if (specialDocType.equals("SCOARINVOICE")
                || specialDocType.equals("SCOARBOEINVOICE")) {
              m_typeDoc = "01";
            } else if (specialDocType.equals("SCOARCREDITMEMO")
                || specialDocType.equals("SCOAPCREDITMEMO")) {
              m_typeDoc = "07";
            } else if (specialDocType.equals("SCOARDEBITMEMO")) {
              m_typeDoc = "08";
            } else if (specialDocType.equals("SCOARTICKET")) {
              m_typeDoc = "03";
            }
          }

          String description = "";
          if (invoice != null)
            description = invoice.getScrPhysicalDocumentno() + " TDoc(" + m_typeDoc + ") "
                + (invoice.getBusinessPartner().getName().length() <= 25
                    ? invoice.getBusinessPartner().getName()
                    : invoice.getBusinessPartner().getName().substring(0, 22) + "...");

          FactLine fl = null;

          GLItem diverse = null;
          if (invoice != null)
            diverse = invoice.getScoDiverseAccGlitem();
          Account diverseAccount = null;
          if (diverse != null)
            diverseAccount = getAccountGLItem(diverse, as, isReceipt, conn);

          Vector<BigDecimal> vTcOut = new Vector<BigDecimal>();
          System.out.println("inside3:" + line.Line_ID + " " + line.getAmount());
          if (!isOrderPrepayment(line.getLine_ID()) && invoice != null) {
            // To force opposite posting isReceipt is opposite as
            // well. this is required when
            // looking backwards

            boolean tc = true;
            if (isReceipt)
              tc = AcctServer.getTCenVigencia(invoice, DateAcct);

            bpAmountConverted = convertAmount(new BigDecimal(bpAmount), !isReceipt, DateAcct,
                TABLEID_Invoice, invoice.getId(), strcCurrencyId, as.m_C_Currency_ID, line, as,
                fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn, bookDifferences, tc, tcPago,
                vTcOut).toString();
            bpAmountConverted = (new BigDecimal(bpAmount)).toString();

            System.out.println("inside2:" + line.Line_ID + " " + line.getAmount());
            if (!isPrepayment) {
              if (line.getDoubtFulDebtAmount().signum() != 0) {

                Vector<BigDecimal> vDebtTcOut = new Vector<BigDecimal>();
                BigDecimal doubtFulDebtAmount = convertAmount(line.getDoubtFulDebtAmount(),
                    isReceipt, DateAcct, TABLEID_Invoice, invoice.getId(), strcCurrencyId,
                    as.m_C_Currency_ID, line, as, fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn,
                    false, tc, tcPago, vDebtTcOut);
                doubtFulDebtAmount = line.getDoubtFulDebtAmount();
                fl = fact.createLine(line,
                    diverseAccount == null
                        ? getAccountBPartner(bpartnerId, as, true, false, true, conn)
                        : diverseAccount,
                    strcCurrencyId, "", doubtFulDebtAmount.toString(), Fact_Acct_Group_ID,
                    nextSeqNo(SeqNo), DocumentType, DateAcct, vDebtTcOut.get(0), conn);
                fl.descripcion = description;
                if (invref != null) {
                  if (fl != null) {
                    fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                    fl.doc_C_Currency_ID = invref.getCurrency().getId();
                    fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                  }
                }

                bpAmountConverted = new BigDecimal(bpAmountConverted).subtract(doubtFulDebtAmount)
                    .toString();
                fl = fact.createLine(line,
                    getAccountBPartnerAllowanceForDoubtfulDebt(bpartnerId, as, conn),
                    strcCurrencyId, doubtFulDebtAmount.toString(), "", Fact_Acct_Group_ID2,
                    nextSeqNo(SeqNo), DocumentType, DateAcct, vDebtTcOut.get(0), conn);
                fl.descripcion = description;
                if (invref != null) {
                  if (fl != null) {
                    fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                    fl.doc_C_Currency_ID = invref.getCurrency().getId();
                    fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                  }
                }

                // Assign expense to the dimensions of the
                // invoice lines
                BigDecimal assignedAmount = BigDecimal.ZERO;
                DocDoubtfulDebtData[] data = DocDoubtfulDebtData.select(conn, invoice.getId());
                Currency currency = OBDal.getInstance().get(Currency.class, strcCurrencyId);
                for (int j = 0; j < data.length; j++) {
                  BigDecimal lineAmount = doubtFulDebtAmount
                      .multiply(new BigDecimal(data[j].percentage)).setScale(
                          currency.getStandardPrecision().intValue(), BigDecimal.ROUND_HALF_UP);
                  if (j == data.length - 1) {
                    lineAmount = doubtFulDebtAmount.subtract(assignedAmount);
                  }
                  DocLine lineDD = new DocLine(DocumentType, Record_ID, "");
                  lineDD.m_A_Asset_ID = data[j].aAssetId;
                  lineDD.m_M_Product_ID = data[j].mProductId;
                  lineDD.m_C_Project_ID = data[j].cProjectId;
                  lineDD.m_C_BPartner_ID = data[j].cBpartnerId;
                  lineDD.m_C_Costcenter_ID = data[j].cCostcenterId;
                  lineDD.m_C_Campaign_ID = data[j].cCampaignId;
                  lineDD.m_C_Activity_ID = data[j].cActivityId;
                  lineDD.m_C_Glitem_ID = data[j].mCGlitemId;
                  lineDD.m_User1_ID = data[j].user1id;
                  lineDD.m_User2_ID = data[j].user2id;
                  lineDD.m_AD_Org_ID = data[j].adOrgId;
                  fl = fact.createLine(lineDD,
                      getAccountBPartnerBadDebt(
                          (lineDD.m_C_BPartner_ID == null || lineDD.m_C_BPartner_ID.equals(""))
                              ? this.C_BPartner_ID
                              : lineDD.m_C_BPartner_ID,
                          false, as, conn),
                      strcCurrencyId, "", lineAmount.toString(), Fact_Acct_Group_ID2,
                      nextSeqNo(SeqNo), DocumentType, DateAcct, vDebtTcOut.get(0), conn);
                  if (invref != null) {
                    if (fl != null) {
                      fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                      fl.doc_C_Currency_ID = invref.getCurrency().getId();
                      fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                    }
                  }
                  fl.descripcion = description;
                  assignedAmount = assignedAmount.add(lineAmount);
                }
              }

              // Cash VAT
              List<String> parameters = new ArrayList<String>();

              boolean tmpRecForConv = isReceiptForConversion;
              isReceiptForConversion = isReceipt;
              System.out.println("inside1:" + line.Line_ID + " " + line.getAmount());
              Vector<BigDecimal> vCashvatTc = new Vector<BigDecimal>();

              convertAmount(new BigDecimal(100), isReceipt, DateAcct, TABLEID_Invoice,
                  invoice.getId(), strcCurrencyId, as.m_C_Currency_ID, line, as, fact,
                  Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn, false, isReceipt, isReceipt,
                  vCashvatTc);

              SeqNo = CashVATUtil.createFactCashVAT(as, conn, fact, Fact_Acct_Group_ID, line,
                  invoice, DocumentType, strcCurrencyId, SeqNo, parameters, vCashvatTc.get(0));
              isReceiptForConversion = tmpRecForConv;
              taxCashVatAmount = new BigDecimal(parameters.get(0));
              /*
               * taxCashVatAmount = convertAmount(new BigDecimal(parameters.get(0)), !isReceipt,
               * DateAcct, TABLEID_Invoice, invoice.getId(), C_Currency_ID, as.m_C_Currency_ID,
               * line, as, fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn, true, tc, !isReceipt);
               */

            }

            acctPuenteDscto = "";
            acctPuenteFactoringInv = "";
            if (invoice != null && invoice.getScoBoeType() != null
                && invoice.getScoBoeType().equals("SCO_DIS")) {

              isLetraDsctoPuente = true;

              sobranteDscto = sobranteDscto.subtract(new BigDecimal(bpAmount));

              // CASO DE LETRAS MIGRADAS // HACER ASIENTO DE
              // MIGRACION Y NO TOMAR EN CUENTA NADA MAS
              List<SCOBoetodiscLine> boetodislines = invoice.getSCOBoetodiscLineInvoicerefIDList();
              /*
               * if(boetodislines.size() == 0){ //DE CARTERA A DSCTO
               * 
               * 
               * String strValidCombinationCartera = invoice.getBusinessPartner
               * ().getCustomerAccountsList ().get(0).getSCOBillOfExchangeAcc().getId();
               * 
               * fl=fact.createLine( line, Account.getAccount(conn, strValidCombinationCartera),
               * strcCurrencyId, (isReceipt ? "" : bpAmountConverted), (isReceipt ?
               * bpAmountConverted : ""), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,
               * DateAcct, vTcOut.get(0), conn); fl.descripcion=description;
               * 
               * fl=fact.createLine( line, ( (diverseAccount == null || isPrepayment) ?
               * getAccountBPartner(bpartnerId, as, isReceipt, isPrepayment, conn) :
               * diverseAccount), strcCurrencyId, (!isReceipt ? "" : bpAmountConverted), (!isReceipt
               * ? bpAmountConverted : ""), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType,
               * DateAcct, vTcOut.get(0), conn); fl.descripcion=description; continue; }
               */

              // cuenta a usar es la puente en lugar de la normal
              // de la factura

              final StringBuilder whereClausePuente = new StringBuilder();
              whereClausePuente.append(" as cusa ");
              whereClausePuente.append(" where cusa.businessPartner.id = '"
                  + invoice.getBusinessPartner().getId() + "'");

              final OBQuery<CustomerAccounts> obqParametersPuente = OBDal.getInstance()
                  .createQuery(CustomerAccounts.class, whereClausePuente.toString());
              obqParametersPuente.setFilterOnReadableClients(false);
              obqParametersPuente.setFilterOnReadableOrganization(false);
              final List<CustomerAccounts> customerAccounts = obqParametersPuente.list();

              acctPuenteDscto = customerAccounts.get(0).getScoPuenteletraAcct().getId();

              FIN_Payment discpayment = null;
              for (int ii = 0; ii < boetodislines.size(); ii++) {
                discpayment = boetodislines.get(ii).getSCOBoeToDiscount().getPayment();
                if (discpayment != null)
                  break;
              }

              if (discpayment == null) {
                discpayment = payment; // ONLY FOR MIGRATION
              }

              Vector<BigDecimal> vTcOutDscto = new Vector<BigDecimal>();
              String bpAmountConvertedDscto = convertAmount(new BigDecimal(bpAmount), isReceipt,
                  DateAcct, TABLEID_Payment, discpayment.getId(), strcCurrencyId,
                  as.m_C_Currency_ID, line, as, fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn,
                  bookDifferences, tc, tcPago, vTcOutDscto).toString();
              bpAmountConvertedDscto = (new BigDecimal(bpAmount)).toString();

              fl = fact.createLine(line, Account.getAccount(conn, acctPuenteDscto),
                  payment.getAccount().getCurrency().getId(),
                  (!isReceipt ? "" : bpAmountConvertedDscto),
                  (!isReceipt ? bpAmountConvertedDscto : ""), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
                  DocumentType, DateAcct, vTcOutDscto.get(0), conn);
              if (invref != null) {
                if (fl != null) {
                  fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                  fl.doc_C_Currency_ID = invref.getCurrency().getId();
                  fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                }
              }
              fl.descripcion = description;

            } else if (invoice != null && invoice.getScoFactinvType() != null
                && invoice.getScoFactinvType().equals("SCO_FACT")) {

              isFactoringInvPuente = true;

              // CASO DE FACTORING INVOICE MIGRADAS // HACER ASIENTO DE
              // MIGRACION Y NO TOMAR EN CUENTA NADA MAS
              List<SCOFactinvLine> factinvlines = invoice.getSCOFactinvLineInvoicerefIDList();

              // cuenta a usar es la puente en lugar de la normal
              // de la factura

              final StringBuilder whereClausePuente = new StringBuilder();
              whereClausePuente.append(" as cusa ");
              whereClausePuente.append(" where cusa.businessPartner.id = '"
                  + invoice.getBusinessPartner().getId() + "'");

              final OBQuery<CustomerAccounts> obqParametersPuente = OBDal.getInstance()
                  .createQuery(CustomerAccounts.class, whereClausePuente.toString());
              obqParametersPuente.setFilterOnReadableClients(false);
              obqParametersPuente.setFilterOnReadableOrganization(false);
              final List<CustomerAccounts> customerAccounts = obqParametersPuente.list();

              acctPuenteFactoringInv = customerAccounts.get(0).getScoPuentefactinvAcct().getId();

              FIN_Payment factinvpayment = null;
              for (int ii = 0; ii < factinvlines.size(); ii++) {
                factinvpayment = factinvlines.get(ii).getSCOFactoringinvoice().getPayment();
                if (factinvpayment != null)
                  break;
              }

              if (factinvpayment == null) {
                factinvpayment = payment; // ONLY FOR MIGRATION
              }

              Vector<BigDecimal> vTcOutFactoringInv = new Vector<BigDecimal>();
              // String bpAmountConvertedFactoringInv = convertAmount(new BigDecimal(bpAmount),
              // isReceipt, DateAcct, TABLEID_Payment, payment.getId(), strcCurrencyId,
              // as.m_C_Currency_ID, line, as, fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn,
              // bookDifferences, tc, tcPago, vTcOutFactoringInv).toString();
              String bpAmountConvertedFactoringInv = convertAmount(new BigDecimal(bpAmount),
                  isReceipt, DateAcct, TABLEID_Invoice, invoice.getId(), strcCurrencyId,
                  as.m_C_Currency_ID, line, as, fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn,
                  bookDifferences, tc, tcPago, vTcOutFactoringInv).toString();

              bpAmountConvertedFactoringInv = (new BigDecimal(bpAmount)).toString();

              fl = fact.createLine(line, Account.getAccount(conn, acctPuenteFactoringInv),
                  payment.getAccount().getCurrency().getId(),
                  (!isReceipt ? "" : bpAmountConvertedFactoringInv),
                  (!isReceipt ? bpAmountConvertedFactoringInv : ""), Fact_Acct_Group_ID,
                  nextSeqNo(SeqNo), DocumentType, DateAcct, vTcOutFactoringInv.get(0), conn);
              if (invref != null) {
                if (fl != null) {
                  fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                  fl.doc_C_Currency_ID = invref.getCurrency().getId();
                  fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                }
              }
              fl.descripcion = description;

            }

          }

          if (vTcOut.size() == 0)
            vTcOut.add(null);

          if (taxCashVatAmount.doubleValue() == 0) {

            if (isOverpayment) {// payment only for overpayment
              // apart from the real payment
              boolean isReceiptForAccount = isReceipt;
              BigDecimal bigAmount = new BigDecimal(bpAmountConverted);
              if (bigAmount.compareTo(BigDecimal.ZERO) < 0)
                isReceiptForAccount = !isReceipt;

              Account account = isReceiptForAccount
                  ? getAccount(AcctServer.ACCTTYPE_WriteOff_Revenue, as, conn)
                  : getAccount(AcctServer.ACCTTYPE_WriteOff, as, conn);
              if (account == null) {
                account = isReceiptForAccount
                    ? getAccount(AcctServer.ACCTTYPE_WriteOffDefault_Revenue, as, conn)
                    : getAccount(AcctServer.ACCTTYPE_WriteOffDefault, as, conn);
              }

              fl = fact.createLine(line, account, strcCurrencyId,
                  (isReceipt ? "" : bpAmountConverted), (isReceipt ? bpAmountConverted : ""),
                  Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, DateAcct, vTcOut.get(0),
                  conn);
              fl.descripcion = description;
            } else {

              fl = fact.createLine(line,
                  ((diverseAccount == null || isPrepayment)
                      ? getAccountBPartner(bpartnerId, as, docissotrx, isPrepayment, conn)
                      : diverseAccount),
                  strcCurrencyId, (isReceipt ? "" : bpAmountConverted),
                  (isReceipt ? bpAmountConverted : ""), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
                  DocumentType, DateAcct, vTcOut.get(0), conn);
              if (invref != null) {
                if (fl != null) {
                  fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                  fl.doc_C_Currency_ID = invref.getCurrency().getId();
                  fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                }
              }
              fl.descripcion = description;
            }

          } else {

            BigDecimal partialAmount = new BigDecimal(bpAmountConverted);
            partialAmount = partialAmount.subtract(taxCashVatAmount);

            fl = fact.createLine(line,
                ((diverseAccount == null || isPrepayment)
                    ? getAccountBPartner(bpartnerId, as, docissotrx, isPrepayment, conn)
                    : diverseAccount),
                strcCurrencyId, (isReceipt ? "" : partialAmount.toString()),
                (isReceipt ? partialAmount.toString() : ""), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
                DocumentType, DateAcct, vTcOut.get(0), conn);
            if (invref != null) {
              if (fl != null) {
                fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                fl.doc_C_Currency_ID = invref.getCurrency().getId();
                fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
              }
            }
            fl.descripcion = description;

            fl = fact.createLine(line,
                ((diverseAccount == null || isPrepayment)
                    ? getAccountBPartner(bpartnerId, as, docissotrx, isPrepayment, conn)
                    : diverseAccount),
                strcCurrencyId, (isReceipt ? "" : taxCashVatAmount.toString()),
                (isReceipt ? taxCashVatAmount.toString() : ""), Fact_Acct_Group_ID,
                nextSeqNo(SeqNo), DocumentType, DateAcct, vTcOut.get(0), conn);
            if (invref != null) {
              if (fl != null) {
                fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                fl.doc_C_Currency_ID = invref.getCurrency().getId();
                fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
              }
            }
            fl.descripcion = description;
          }
          // If payment date is prior to invoice date book invoice as
          // a pre-payment not as a regular
          // Receivable/Payable
          if (line.isPrepaymentAgainstInvoice()) {
            DocLine line2 = new DocLine(DocumentType, Record_ID, line.m_TrxLine_ID);
            line2.copyInfo(line);
            line2.m_DateAcct = OBDateUtils.formatDate(invoice.getAccountingDate());
            // checking if the prepayment account and ReceivablesNo
            // account in the Business Partner
            // is the same.In this case we do not need to create
            // more accounting lines
            if (!getAccountBPartner(bpartnerId, as, docissotrx, true, conn).Account_ID
                .equals(getAccountBPartner(bpartnerId, as, docissotrx, false, conn).Account_ID)) {
              fl = fact.createLine(line2,
                  (diverseAccount == null)
                      ? getAccountBPartner(bpartnerId, as, docissotrx, false, conn)
                      : diverseAccount,
                  strcCurrencyId, (isReceipt ? "" : bpAmountConverted),
                  (isReceipt ? bpAmountConverted : ""), Fact_Acct_Group_ID3, nextSeqNo(SeqNo),
                  DocumentType, DateAcct, vTcOut.get(0), conn);
              if (invref != null) {
                if (fl != null) {
                  fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                  fl.doc_C_Currency_ID = invref.getCurrency().getId();
                  fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                }
              }
              fl.descripcion = description;

              fl = fact.createLine(line2,
                  getAccountBPartner(bpartnerId, as, docissotrx, true, conn), strcCurrencyId,
                  (!isReceipt ? "" : bpAmountConverted), (!isReceipt ? bpAmountConverted : ""),
                  Fact_Acct_Group_ID3, nextSeqNo(SeqNo), DocumentType, DateAcct, vTcOut.get(0),
                  conn);
              if (invref != null) {
                if (fl != null) {
                  fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                  fl.doc_C_Currency_ID = invref.getCurrency().getId();
                  fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                }
              }
              fl.descripcion = description;

            }
          }
        } else {

          System.out.println("Anticipo o entrega " + line.getAmount() + " ");

          if (line.getSCOPrepayment() != null) {// anticipo de compra
            SCOPrepayment prep = line.getSCOPrepayment();
            String strAmount = line.getAmount();
            FIN_Payment pmt = prep.getPayment();
            SCOBillofexchange boe = prep.getSCOBillofexchange();
            GLJournalLine jline = prep.getJournalLine();
            Vector<BigDecimal> vTcOutPrep = new Vector<BigDecimal>();
            String bpAmountConverted = "";

            if (pmt != null) {
              bpAmountConverted = convertAmount(new BigDecimal(strAmount), !isReceipt, DateAcct,
                  TABLEID_Payment, pmt.getId(), strcCurrencyId, as.m_C_Currency_ID, line, as, fact,
                  Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn, bookDifferences, true, tcPago,
                  vTcOutPrep).toString();
            } else if (boe != null) {
              bpAmountConverted = convertAmount(new BigDecimal(strAmount), !isReceipt, DateAcct,
                  TABLEID_Billofexchange, boe.getId(), strcCurrencyId, as.m_C_Currency_ID, line, as,
                  fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn, bookDifferences, true, tcPago,
                  vTcOutPrep).toString();
            } else if (jline != null) {
              bpAmountConverted = convertAmount(new BigDecimal(strAmount), !isReceipt, DateAcct,
                  TABLEID_GLJournalline, jline.getId(), strcCurrencyId, as.m_C_Currency_ID, line,
                  as, fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn, bookDifferences, true,
                  tcPago, vTcOutPrep).toString();
            } else {
              pmt = new FIN_Payment();
              pmt.setId(line.getSCOPrepayment().getId());

              bpAmountConverted = convertAmount(new BigDecimal(strAmount), !isReceipt, DateAcct,
                  TABLEID_Payment, pmt.getId(), strcCurrencyId, as.m_C_Currency_ID, line, as, fact,
                  Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn, bookDifferences, true, tcPago,
                  vTcOutPrep).toString();
            }

            bpAmountConverted = (new BigDecimal(strAmount)).toString();

            boolean isReceiptForGlItem = isReceipt;
            if (new BigDecimal(strAmount).compareTo(BigDecimal.ZERO) < 0) {
              isReceiptForGlItem = !isReceipt;
            }

            FactLine fl = fact.createLine(line,
                getAccountGLItem(OBDal.getInstance().get(GLItem.class, line.getC_GLItem_ID()), as,
                    isReceiptForGlItem, conn),
                strcCurrencyId, (isReceipt ? "" : bpAmountConverted),
                (isReceipt ? bpAmountConverted : ""), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
                DocumentType, DateAcct, vTcOutPrep.get(0), conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = line.getSCOPrepayment().getOrganization().getId();
              fl.doc_C_Currency_ID = line.getSCOPrepayment().getCurrency().getId();
              fl.doc_C_Doctype_ID = line.getSCOPrepayment().getTransactionDocument().getId();
            }
            fl.setRecordId3(line.getSCOPrepayment().getId());
            fl.setTable3Id("135FAE2571DB4028A90D5CAA6FAC154C");

            fl.descripcion = line.getSCOPrepayment().getDocumentNo() + " - "
                + line.getSCOPrepayment().getBusinessPartner().getName() + " - "
                + line.getSCOPrepayment().getDescription();

          } else if (line.getSCORendcuentas() != null) {// rendicion
            // de
            // cuentas
            ScoRendicioncuentas prep = line.getSCORendcuentas();
            String strAmount = line.getAmount();
            FIN_Payment pmt = prep.getFINPaymentOpen();

            if (pmt == null) {
              pmt = new FIN_Payment();
              pmt.setId(line.getSCORendcuentas().getId());
            }

            Vector<BigDecimal> vTcOutPrep = new Vector<BigDecimal>();
            String bpAmountConverted = convertAmount(new BigDecimal(strAmount), !isReceipt,
                DateAcct, TABLEID_Payment, pmt.getId(), strcCurrencyId, as.m_C_Currency_ID, line,
                as, fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn, bookDifferences, true, tcPago,
                vTcOutPrep).toString();
            bpAmountConverted = (new BigDecimal(strAmount)).toString();

            boolean isReceiptForGlItem = isReceipt;
            if (new BigDecimal(strAmount).compareTo(BigDecimal.ZERO) < 0) {
              isReceiptForGlItem = !isReceipt;
            }

            FactLine fl = fact.createLine(line,
                getAccountGLItem(OBDal.getInstance().get(GLItem.class, line.getC_GLItem_ID()), as,
                    isReceiptForGlItem, conn),
                strcCurrencyId, (isReceipt ? "" : bpAmountConverted),
                (isReceipt ? bpAmountConverted : ""), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
                DocumentType, DateAcct, vTcOutPrep.get(0), conn);
            if (fl != null) {
              fl.doc_Ad_Org_ID = line.getSCORendcuentas().getOrganization().getId();
              fl.doc_C_Currency_ID = line.getSCORendcuentas().getCurrency().getId();
              fl.doc_C_Doctype_ID = line.getSCORendcuentas().getTransactionDocument().getId();
            }
            fl.setRecordId3(line.getSCORendcuentas().getId());
            fl.setTable3Id("F90F4E012DF74D2B92BACC79473FF588");

            fl.descripcion = line.getSCORendcuentas().getDocumentNo() + " - "
                + line.getSCORendcuentas().getBusinessPartner().getName() + " - "
                + line.getSCORendcuentas().getDescription();

          } else {

            if (!line.Sco_Discboe_ID.equals("")) {

              Vector<BigDecimal> vTcOut = new Vector<BigDecimal>();
              Invoice invoice = OBDal.getInstance().get(Invoice.class, line.Sco_Discboe_ID);
              Invoice invref = null;
              if (invoice != null) {
                invref = AcctServer.getInvoicerefForMapping(invoice);
              }

              boolean tc = true;
              if (isReceipt)
                tc = AcctServer.getTCenVigencia(invoice, DateAcct);
              String tmpBoeType = invoice.getScoBoeType();
              invoice.setScoBoeType("SCO_POR");// Solo para que el
              // DateReference
              // salga de la
              // letra y
              // no del canje
              // a dscto
              String bpAmountConverted = convertAmount(new BigDecimal(bpAmount), !isReceipt,
                  DateAcct, TABLEID_Invoice, invoice.getId(), strcCurrencyId, as.m_C_Currency_ID,
                  line, as, fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn, bookDifferences, tc,
                  tcPago, vTcOut).toString();
              bpAmountConverted = (new BigDecimal(bpAmount)).toString();

              invoice.setScoBoeType(tmpBoeType);

              boolean isReceiptForGlItem = isReceipt;
              if (new BigDecimal(bpAmountConverted).compareTo(BigDecimal.ZERO) < 0) {
                isReceiptForGlItem = !isReceipt;
              }

              FactLine fl = fact.createLine(line,
                  getAccountGLItem(OBDal.getInstance().get(GLItem.class, line.getC_GLItem_ID()), as,
                      isReceiptForGlItem, conn),
                  strcCurrencyId, (isReceipt ? "" : bpAmountConverted),
                  (isReceipt ? bpAmountConverted : ""), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
                  DocumentType, DateAcct, vTcOut.get(0), conn);
              if (invref != null) {
                if (fl != null) {
                  fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                  fl.doc_C_Currency_ID = invref.getCurrency().getId();
                  fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                }
              }
              fl.setRecordId3(line.Sco_Discboe_ID);
              fl.setTable3Id("318");
              fl.descripcion = invoice.getScrPhysicalDocumentno() + " - "
                  + invoice.getBusinessPartner().getName();

            } else if (!line.Sco_Factoredinv_ID.equals("")) {

              Vector<BigDecimal> vTcOut = new Vector<BigDecimal>();
              Invoice invoice = OBDal.getInstance().get(Invoice.class, line.Sco_Factoredinv_ID);
              Invoice invref = null;
              if (invoice != null) {
                invref = AcctServer.getInvoicerefForMapping(invoice);
              }

              boolean tc = true;
              if (isReceipt)
                tc = AcctServer.getTCenVigencia(invoice, DateAcct);
              String tmpFactoringType = invoice.getScoFactinvType();
              invoice.setScoFactinvType("SCO_POR");// Solo para que el
              // DateReference
              // salga de la factura y
              // no del canje
              // a factoring
              String bpAmountConverted = convertAmount(new BigDecimal(bpAmount), !isReceipt,
                  DateAcct, TABLEID_Invoice, invoice.getId(), strcCurrencyId, as.m_C_Currency_ID,
                  line, as, fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn, bookDifferences, tc,
                  tcPago, vTcOut).toString();
              bpAmountConverted = (new BigDecimal(bpAmount)).toString();

              invoice.setScoFactinvType(tmpFactoringType);

              boolean isReceiptForGlItem = isReceipt;
              if (new BigDecimal(bpAmountConverted).compareTo(BigDecimal.ZERO) < 0) {
                isReceiptForGlItem = !isReceipt;
              }

              FactLine fl = fact.createLine(line,
                  getAccountGLItem(OBDal.getInstance().get(GLItem.class, line.getC_GLItem_ID()), as,
                      isReceiptForGlItem, conn),
                  strcCurrencyId, (isReceipt ? "" : bpAmountConverted),
                  (isReceipt ? bpAmountConverted : ""), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
                  DocumentType, DateAcct, vTcOut.get(0), conn);
              if (invref != null) {
                if (fl != null) {
                  fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                  fl.doc_C_Currency_ID = invref.getCurrency().getId();
                  fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                }
              }
              fl.setRecordId3(line.Sco_Factoredinv_ID);
              fl.setTable3Id("318");
              fl.descripcion = invoice.getScrPhysicalDocumentno() + " - "
                  + invoice.getBusinessPartner().getName();

            } else if (!line.Sco_Loandocinv_ID.equals("")) {

              Vector<BigDecimal> vTcOut = new Vector<BigDecimal>();
              Invoice invoice = OBDal.getInstance().get(Invoice.class, line.Sco_Loandocinv_ID);
              Invoice invref = null;
              if (invoice != null) {
                invref = AcctServer.getInvoicerefForMapping(invoice);
              }

              boolean tc = true;
              if (isReceipt)
                tc = AcctServer.getTCenVigencia(invoice, DateAcct);
              String bpAmountConverted = convertAmount(new BigDecimal(bpAmount), !isReceipt,
                  DateAcct, TABLEID_Invoice, invoice.getId(), strcCurrencyId, as.m_C_Currency_ID,
                  line, as, fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn, bookDifferences, tc,
                  tcPago, vTcOut).toString();
              bpAmountConverted = (new BigDecimal(bpAmount)).toString();

              boolean isReceiptForGlItem = isReceipt;
              if (new BigDecimal(bpAmountConverted).compareTo(BigDecimal.ZERO) < 0) {
                isReceiptForGlItem = !isReceipt;
              }

              FactLine fl = fact.createLine(line,
                  getAccountGLItem(OBDal.getInstance().get(GLItem.class, line.getC_GLItem_ID()), as,
                      isReceiptForGlItem, conn),
                  strcCurrencyId, (isReceipt ? "" : bpAmountConverted),
                  (isReceipt ? bpAmountConverted : ""), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
                  DocumentType, DateAcct, vTcOut.get(0), conn);
              if (invref != null) {
                if (fl != null) {
                  fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                  fl.doc_C_Currency_ID = invref.getCurrency().getId();
                  fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                }
              }
              fl.setRecordId3(line.Sco_Loandocinv_ID);
              fl.setTable3Id("318");
              fl.descripcion = invoice.getScrPhysicalDocumentno() + " - "
                  + invoice.getBusinessPartner().getName();

            } else {

              Vector<BigDecimal> vTcOut = new Vector<BigDecimal>();
              boolean tc = true;
              if (isReceipt)
                tc = false;

              String bpAmountConverted = "";
              boolean bHasRef = false;

              if (!bHasRef) {

                bpAmountConverted = convertAmount(new BigDecimal(bpAmount), !isReceipt, DateAcct,
                    TABLEID_Payment, payment.getId(), strcCurrencyId, as.m_C_Currency_ID, line, as,
                    fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn, bookDifferences, tc, tcPago,
                    vTcOut).toString();

              }

              bpAmountConverted = (new BigDecimal(bpAmount)).toString();

              boolean isReceiptForGlItem = isReceipt;
              if (new BigDecimal(bpAmountConverted).compareTo(BigDecimal.ZERO) < 0) {
                isReceiptForGlItem = !isReceipt;
              }

              FactLine fl = fact.createLine(line,
                  getAccountGLItem(OBDal.getInstance().get(GLItem.class, line.getC_GLItem_ID()), as,
                      isReceiptForGlItem, conn),
                  strcCurrencyId, (isReceipt ? "" : bpAmount), (isReceipt ? bpAmount : ""),
                  Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, DateAcct, vTcOut.get(0),
                  conn);
              fl.setEEFF_Cashflow(line.Sco_Eeff_Cashflow_ID);

              String description = line.getM_description();
              fl.descripcion = description;

              // ACA PREPAYMENT Y RENDCUENTAS APERTURA
              if (!line.Sco_Prepayment_ID.equals("")) {

                fl.setRecordId3(line.Sco_Prepayment_ID);
                fl.setTable3Id("135FAE2571DB4028A90D5CAA6FAC154C");
                SCOPrepayment prep = OBDal.getInstance().get(SCOPrepayment.class,
                    line.Sco_Prepayment_ID);
                if (fl != null) {
                  fl.doc_Ad_Org_ID = prep.getOrganization().getId();
                  fl.doc_C_Currency_ID = prep.getCurrency().getId();
                  fl.doc_C_Doctype_ID = prep.getTransactionDocument().getId();
                }
              } else if (!line.Sco_Rendcuentas_ID.equals("")) {

                fl.setRecordId3(line.Sco_Rendcuentas_ID);
                fl.setTable3Id("F90F4E012DF74D2B92BACC79473FF588");
                ScoRendicioncuentas prep = OBDal.getInstance().get(ScoRendicioncuentas.class,
                    line.Sco_Rendcuentas_ID);
                if (fl != null) {
                  fl.doc_Ad_Org_ID = prep.getOrganization().getId();
                  fl.doc_C_Currency_ID = prep.getCurrency().getId();
                  fl.doc_C_Doctype_ID = prep.getTransactionDocument().getId();
                }
              } else {

                String record3 = line.m_Record_Id3;
                if (!record3.equals("")) {
                  SCOInternalDoc doc = OBDal.getInstance().get(SCOInternalDoc.class, record3);
                  if (doc != null) {
                    if (fl != null) {
                      fl.doc_Ad_Org_ID = doc.getOrganization().getId();
                      if (doc.getCurrency() != null)
                        fl.doc_C_Currency_ID = doc.getCurrency().getId();
                      fl.doc_C_Doctype_ID = doc.getTransactionDocument().getId();
                    }
                  } else {
                    Invoice docInv = OBDal.getInstance().get(Invoice.class, record3);
                    Invoice invref = null;
                    if (docInv != null) {
                      invref = AcctServer.getInvoicerefForMapping(docInv);
                    }
                    if (invref != null) {
                      if (fl != null) {
                        fl.doc_Ad_Org_ID = invref.getOrganization().getId();
                        fl.doc_C_Currency_ID = invref.getCurrency().getId();
                        fl.doc_C_Doctype_ID = invref.getTransactionDocument().getId();
                      }
                    }
                  }
                }
              }
            }
          }

        }
      }
      C_Invoice_ID_Ref = "";

      System.out.println("Paso lineas");
      /*
       * for(int i=0; i<fact.getLines().length; i++){ System.out.println("combF:"
       * +fact.getLines()[i].getM_acct().combination); }
       */

      if (BigDecimal.ZERO.compareTo(new BigDecimal(Amounts[AMTTYPE_Gross])) != 0) {

        if (isLetraDsctoPuente) {
          // YA CONTABILIZADO
          /*
           * String saldoSobrante = sobranteDscto.toString(); FactLine fl = fact.createLine(null,
           * getAccount(conn, payment.getPaymentMethod(), payment.getAccount(), as,
           * payment.isReceipt()), payment.getAccount().getCurrency().getId(), (payment.isReceipt()
           * ? saldoSobrante : ""), (payment.isReceipt() ? "" : saldoSobrante), Fact_Acct_Group_ID,
           * "999999", DocumentType, conn);
           * 
           * if (payment.getAccount().getScoInternalDoc() != null)
           * fl.setRecordId3(payment.getAccount().getScoInternalDoc().getId());
           */

        } else if (isFactoringInvPuente) {
          // YA CONTABILIZADO
        } else {
          FactLine fl = fact.createLine(null,
              getAccount(conn, payment.getPaymentMethod(), payment.getAccount(), as,
                  payment.isReceipt()),
              payment.getAccount().getCurrency().getId(),
              (payment.isReceipt() ? Amounts[AMTTYPE_Gross] : ""),
              (payment.isReceipt() ? "" : Amounts[AMTTYPE_Gross]), Fact_Acct_Group_ID, "999999",
              DocumentType, conn);

          if (payment.getSsaInternalDoc() != null) {
            fl.setRecordId3(payment.getSsaInternalDoc().getId());
            fl.setTable3Id("A64BF5FB928C4EC1BACC023D6DC87F3C");
          } else if (payment.getAccount().getScoInternalDoc() != null) {
            fl.setRecordId3(payment.getAccount().getScoInternalDoc().getId());
            fl.setTable3Id("A64BF5FB928C4EC1BACC023D6DC87F3C");
          }

          if (payment.getScoEeffCashflow() != null) {
            fl.setEEFF_Cashflow(payment.getScoEeffCashflow().getId());
          }
        }
      }

      // Pre-payment is consumed when Used Credit Amount not equals Zero.
      // When consuming Credit no
      // credit is generated
      if (new BigDecimal(usedAmount).compareTo(ZERO) != 0
          && new BigDecimal(generatedAmount).compareTo(ZERO) == 0) {
        List<FIN_Payment_Credit> creditPayments = payment.getFINPaymentCreditList();
        BigDecimal amtDiff = BigDecimal.ZERO;
        for (FIN_Payment_Credit creditPayment : creditPayments) {

          Vector<BigDecimal> vTcCreditOut = new Vector<BigDecimal>();

          String creditAmountConverted = convertAmount(creditPayment.getAmount(),
              creditPayment.getCreditPaymentUsed().isReceipt(), DateAcct, TABLEID_Payment,
              creditPayment.getCreditPaymentUsed().getId(),
              creditPayment.getCreditPaymentUsed().getCurrency().getId(), as.m_C_Currency_ID, null,
              as, fact, Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn, bookDifferences, false, tcPago,
              vTcCreditOut).toString();

          creditAmountConverted = creditPayment.getAmount().toString();
          fact.createLine(null,
              getAccountBPartner(C_BPartner_ID, as,
                  creditPayment.getCreditPaymentUsed().isReceipt(), true, conn),
              creditPayment.getCreditPaymentUsed().getCurrency().getId(),
              (creditPayment.getCreditPaymentUsed().isReceipt() ? creditAmountConverted : ""),
              (creditPayment.getCreditPaymentUsed().isReceipt() ? "" : creditAmountConverted),
              Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, DateAcct, vTcCreditOut.get(0),
              conn);
          // amtDiff =
          // amtDiff.add(creditPayment.getAmount()).subtract(
          // new BigDecimal(creditAmountConverted));

        }

        /*
         * for(int i=0; i<fact.getLines().length; i++){ System.out.println
         * ("combI:"+fact.getLines()[i].getM_acct().combination); }
         */

        /*
         * if (!payment.isReceipt() && amtDiff.compareTo(BigDecimal.ZERO) == 1 ||
         * payment.isReceipt() && amtDiff.compareTo(BigDecimal.ZERO) == -1) { fact.createLine(null,
         * getAccount(AcctServer.ACCTTYPE_ConvertGainDefaultAmt, as, conn),
         * payment.getCurrency().getId(), "", amtDiff.abs().toString(), Fact_Acct_Group_ID,
         * nextSeqNo(SeqNo), DocumentType, conn); } else { fact.createLine(null,
         * getAccount(AcctServer.ACCTTYPE_ConvertChargeDefaultAmt, as, conn),
         * payment.getCurrency().getId(), amtDiff.abs().toString(), "", Fact_Acct_Group_ID,
         * nextSeqNo(SeqNo), DocumentType, conn); }
         */

        /*
         * for(int i=0; i<fact.getLines().length; i++){ System.out.println
         * ("combJ:"+fact.getLines()[i].getM_acct().combination); }
         */

        if (creditPayments.isEmpty()) {

          Vector<BigDecimal> vTcOut = new Vector<BigDecimal>();

          String amountConverted = convertAmount(new BigDecimal(usedAmount), payment.isReceipt(),
              DateAcct, TABLEID_Payment, payment.getId(),
              payment.getAccount().getCurrency().getId(), as.m_C_Currency_ID, null, as, fact,
              Fact_Acct_Group_ID, nextSeqNo(SeqNo), conn, bookDifferences, false, tcPago, vTcOut)
                  .toString();

          amountConverted = usedAmount;
          fact.createLine(null,
              getAccountBPartner(C_BPartner_ID, as, payment.isReceipt(), true, conn),
              payment.getAccount().getCurrency().getId(),
              (payment.isReceipt() ? amountConverted : ""),
              (payment.isReceipt() ? "" : amountConverted), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
              DocumentType, DateAcct, vTcOut.get(0), conn);

          /*
           * fact.createLine(null, getAccountBPartner(C_BPartner_ID, as, payment.isReceipt(), true,
           * conn), payment .getAccount().getCurrency().getId(), (payment.isReceipt() ? usedAmount :
           * ""), (payment.isReceipt() ? "" : usedAmount), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
           * DocumentType, conn);
           */
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }

    System.out.println("Terminando fact");
    FactLine[] factlines = fact.getLines();
    for (int i = 0; i < factlines.length; i++) {
      System.out.println(
          "Debit: " + factlines[i].getM_AmtAcctDr() + " Credit: " + factlines[i].getM_AmtAcctCr());
    }

    SeqNo = "0";
    return fact;
  }

  public boolean isOrderPrepayment(String paymentDetailID) {
    FIN_PaymentDetail pd = OBDal.getInstance().get(FIN_PaymentDetail.class, paymentDetailID);
    if (pd != null) {
      return pd.isPrepayment();
    }
    return false;
  }

  public String nextSeqNo(String oldSeqNo) {
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    return SeqNo;
  }

  /**
   * Get Source Currency Balance - subtracts line amounts from total - no rounding
   * 
   * @return positive amount, if total is bigger than lines
   */
  @Override
  public BigDecimal getBalance() {
    BigDecimal retValue = ZERO;
    StringBuffer sb = new StringBuffer(" [");
    // Total
    retValue = retValue.add(new BigDecimal(getAmount(AcctServer.AMTTYPE_Gross)));
    if ((new BigDecimal(generatedAmount)).signum() == 0) {
      retValue = retValue.add(new BigDecimal(usedAmount));
    }
    sb.append(getAmount(AcctServer.AMTTYPE_Gross));
    // - Lines
    for (int i = 0; i < p_lines.length; i++) {
      BigDecimal lineBalance = new BigDecimal(((DocLine_FINPayment) p_lines[i]).PaymentAmount);
      retValue = retValue.subtract(lineBalance);
      sb.append("-").append(lineBalance);
    }
    sb.append("]");
    //
    log4j.debug(" Balance=" + retValue + sb.toString());
    return retValue;
  } // getBalance

  @Override
  public boolean getDocumentConfirmation(ConnectionProvider conn, String strRecordId) {
    // Checks if this step is configured to generate accounting for the
    // selected financial account
    boolean confirmation = false;
    final String PAYMENT_RECEIVED = "RPR";
    final String PAYMENT_MADE = "PPM";
    final String DEPOSITED_NOT_CLEARED = "RDNC";
    final String WITHDRAWN_NOT_CLEARED = "PWNC";
    final String PAYMENT_CLEARED = "RPPC";
    OBContext.setAdminMode();
    try {
      FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, strRecordId);

      if (payment.getPosted().equals("sco_M")) {
        strMessage = "No se puede contabilizar Datos de Migracion";
        setStatus(STATUS_Migrated);
        return false;
      }

      // Posting can just happen if payment is in the right status
      if (payment.getStatus().equals(PAYMENT_RECEIVED) || payment.getStatus().equals(PAYMENT_MADE)
          || payment.getStatus().equals(DEPOSITED_NOT_CLEARED)
          || payment.getStatus().equals(WITHDRAWN_NOT_CLEARED)
          || payment.getStatus().equals(PAYMENT_CLEARED)) {
        OBCriteria<FinAccPaymentMethod> obCriteria = OBDal.getInstance()
            .createCriteria(FinAccPaymentMethod.class);
        obCriteria.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT, payment.getAccount()));
        obCriteria.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD,
            payment.getPaymentMethod()));
        obCriteria.setFilterOnReadableClients(false);
        obCriteria.setFilterOnReadableOrganization(false);
        List<FinAccPaymentMethod> lines = obCriteria.list();
        List<FIN_FinancialAccountAccounting> accounts = payment.getAccount()
            .getFINFinancialAccountAcctList();
        for (FIN_FinancialAccountAccounting account : accounts) {
          if (confirmation)
            return confirmation;
          if (payment.isReceipt()) {
            if (("INT").equals(lines.get(0).getUponReceiptUse())
                && account.getInTransitPaymentAccountIN() != null)
              confirmation = true;
            else if (("DEP").equals(lines.get(0).getUponReceiptUse())
                && account.getDepositAccount() != null)
              confirmation = true;
            else if (("CLE").equals(lines.get(0).getUponReceiptUse())
                && account.getClearedPaymentAccount() != null)
              confirmation = true;
          } else {
            if (("INT").equals(lines.get(0).getUponPaymentUse())
                && account.getFINOutIntransitAcct() != null)
              confirmation = true;
            else if (("WIT").equals(lines.get(0).getUponPaymentUse())
                && account.getWithdrawalAccount() != null)
              confirmation = true;
            else if (("CLE").equals(lines.get(0).getUponPaymentUse())
                && account.getClearedPaymentAccountOUT() != null)
              confirmation = true;
          }
          // For payments with Amount ZERO always create an entry as
          // no transaction will be created
          if (payment.getAmount().compareTo(ZERO) == 0) {
            confirmation = true;
          }
        }
      }
    } catch (Exception e) {
      setStatus(STATUS_DocumentDisabled);
      return confirmation;
    } finally {
      OBContext.restorePreviousMode();
    }
    if (!confirmation)
      setStatus(STATUS_DocumentDisabled);
    return confirmation;
  }

  @Override
  public void loadObjectFieldProvider(ConnectionProvider conn, String strAD_Client_ID, String Id)
      throws ServletException {
    FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, Id);
    FieldProviderFactory[] data = new FieldProviderFactory[1];
    data[0] = new FieldProviderFactory(null);
    FieldProviderFactory.setField(data[0], "AD_Client_ID", payment.getClient().getId());
    FieldProviderFactory.setField(data[0], "AD_Org_ID", payment.getOrganization().getId());
    FieldProviderFactory.setField(data[0], "C_BPartner_ID",
        payment.getBusinessPartner() != null ? payment.getBusinessPartner().getId() : "");
    FieldProviderFactory.setField(data[0], "DocumentNo", payment.getDocumentNo());
    String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);
    FieldProviderFactory.setField(data[0], "PaymentDate",
        outputFormat.format(payment.getPaymentDate()));
    FieldProviderFactory.setField(data[0], "C_DocType_ID", payment.getDocumentType().getId());
    FieldProviderFactory.setField(data[0], "C_Currency_ID",
        payment.getAccount().getCurrency().getId());
    FieldProviderFactory.setField(data[0], "Amount", payment.getAmount().toString());
    FieldProviderFactory.setField(data[0], "GeneratedCredit",
        payment.getGeneratedCredit().toString());
    FieldProviderFactory.setField(data[0], "UsedCredit", payment.getUsedCredit().toString());
    FieldProviderFactory.setField(data[0], "WriteOffAmt", payment.getWriteoffAmount().toString());
    FieldProviderFactory.setField(data[0], "Description",
        payment.getDescription() != null ? payment.getDescription() : "");
    FieldProviderFactory.setField(data[0], "Posted", payment.getPosted());
    FieldProviderFactory.setField(data[0], "Processed", payment.isProcessed() ? "Y" : "N");
    FieldProviderFactory.setField(data[0], "Processing", payment.isProcessNow() ? "Y" : "N");
    FieldProviderFactory.setField(data[0], "C_Project_ID",
        payment.getProject() != null ? payment.getProject().getId() : "");
    FieldProviderFactory.setField(data[0], "C_Campaign_ID",
        payment.getSalesCampaign() != null ? payment.getSalesCampaign().getId() : "");
    FieldProviderFactory.setField(data[0], "C_Activity_ID",
        payment.getActivity() != null ? payment.getActivity().getId() : "");
    // User1_ID and User2_ID
    DocFINPaymentData[] paymentInfo = DocFINPaymentData.select(conn, payment.getId());
    if (paymentInfo.length > 0) {
      FieldProviderFactory.setField(data[0], "User1_ID", paymentInfo[0].user1Id);
      FieldProviderFactory.setField(data[0], "User2_ID", paymentInfo[0].user2Id);
      FieldProviderFactory.setField(data[0], "C_Costcenter_ID", paymentInfo[0].cCostcenterId);
    }
    FieldProviderFactory.setField(data[0], "SCO_Area_ID",
        payment.getScoArea() != null ? payment.getScoArea().getId() : "");

    setObjectFieldProvider(data);
  }

  /*
   * Retrieves Account for receipt / Payment for the given payment method + Financial Account
   */
  public Account getAccount(ConnectionProvider conn, FIN_PaymentMethod paymentMethod,
      FIN_FinancialAccount finAccount, AcctSchema as, boolean bIsReceipt) throws ServletException {
    OBContext.setAdminMode();
    Account account = null;
    try {
      OBCriteria<FIN_FinancialAccountAccounting> accounts = OBDal.getInstance()
          .createCriteria(FIN_FinancialAccountAccounting.class);
      accounts.add(Restrictions.eq(FIN_FinancialAccountAccounting.PROPERTY_ACCOUNT, finAccount));
      accounts.add(Restrictions.eq(FIN_FinancialAccountAccounting.PROPERTY_ACCOUNTINGSCHEMA,
          OBDal.getInstance().get(org.openbravo.model.financialmgmt.accounting.coa.AcctSchema.class,
              as.m_C_AcctSchema_ID)));
      accounts.add(Restrictions.eq(FIN_FinancialAccountAccounting.PROPERTY_ACTIVE, true));
      accounts.setFilterOnReadableClients(false);
      accounts.setFilterOnReadableOrganization(false);
      List<FIN_FinancialAccountAccounting> accountList = accounts.list();
      if (accountList == null || accountList.size() == 0)
        return null;
      OBCriteria<FinAccPaymentMethod> accPaymentMethod = OBDal.getInstance()
          .createCriteria(FinAccPaymentMethod.class);
      accPaymentMethod.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT, finAccount));
      accPaymentMethod
          .add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD, paymentMethod));
      accPaymentMethod.setFilterOnReadableClients(false);
      accPaymentMethod.setFilterOnReadableOrganization(false);
      List<FinAccPaymentMethod> lines = accPaymentMethod.list();
      if (bIsReceipt) {
        account = getAccount(conn, lines.get(0).getUponReceiptUse(), accountList.get(0),
            bIsReceipt);
      } else {
        account = getAccount(conn, lines.get(0).getUponPaymentUse(), accountList.get(0),
            bIsReceipt);
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return account;
  }

  @Deprecated
  public String convertAmount(String Amount, boolean isReceipt, String mDateAcct,
      String conversionDate, String C_Currency_ID_From, String C_Currency_ID_To, DocLine line,
      AcctSchema as, Fact fact, String Fact_Acct_Group_ID, ConnectionProvider conn)
      throws ServletException {
    if (Amount == null || Amount.equals(""))
      return "0";
    if (C_Currency_ID_From.equals(C_Currency_ID_To))
      return Amount;
    else
      MultiCurrency = true;
    String Amt = getConvertedAmt(Amount, C_Currency_ID_From, C_Currency_ID_To, conversionDate, "",
        AD_Client_ID, AD_Org_ID, conn);
    if (log4j.isDebugEnabled())
      log4j.debug("Amt:" + Amt);

    String AmtTo = getConvertedAmt(Amount, C_Currency_ID_From, C_Currency_ID_To, mDateAcct, "",
        AD_Client_ID, AD_Org_ID, conn);
    if (log4j.isDebugEnabled())
      log4j.debug("AmtTo:" + AmtTo);

    BigDecimal AmtDiff = (new BigDecimal(AmtTo)).subtract(new BigDecimal(Amt));
    if (log4j.isDebugEnabled())
      log4j.debug("AmtDiff:" + AmtDiff);

    if (log4j.isDebugEnabled()) {
      log4j.debug("curr from:" + C_Currency_ID_From + " Curr to:" + C_Currency_ID_To + " convDate:"
          + conversionDate + " DateAcct:" + mDateAcct);
      log4j.debug("Amt:" + Amt + " AmtTo:" + AmtTo + " Diff:" + AmtDiff.toString());
    }

    if ((isReceipt && AmtDiff.compareTo(new BigDecimal("0.00")) == 1)
        || (!isReceipt && AmtDiff.compareTo(new BigDecimal("0.00")) == -1)) {
      fact.createLine(line, getAccount(AcctServer.ACCTTYPE_ConvertGainDefaultAmt, as, conn),
          C_Currency_ID_To, "", AmtDiff.abs().toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo),
          DocumentType, conn);
    } else {
      fact.createLine(line, getAccount(AcctServer.ACCTTYPE_ConvertChargeDefaultAmt, as, conn),
          C_Currency_ID_To, AmtDiff.abs().toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo),
          DocumentType, conn);
    }

    return Amt;
  }

  public String getSeqNo() {
    return SeqNo;
  }

  public void setSeqNo(String seqNo) {
    SeqNo = seqNo;
  }

  public String getGeneratedAmount() {
    return generatedAmount;
  }

  public void setGeneratedAmount(String generatedAmount) {
    this.generatedAmount = generatedAmount;
  }

  public String getUsedAmount() {
    return usedAmount;
  }

  public void setUsedAmount(String usedAmount) {
    this.usedAmount = usedAmount;
  }

  boolean isPaymentDatePriorToInvoiceDate(FIN_PaymentDetail paymentDetail) {
    List<FIN_PaymentScheduleDetail> schedDetails = paymentDetail.getFINPaymentScheduleDetailList();
    if (schedDetails.size() == 0) {
      return false;
    } else {
      if (schedDetails.get(0).getInvoicePaymentSchedule() != null
          && schedDetails.get(0).getInvoicePaymentSchedule().getInvoice().getAccountingDate()
              .after(paymentDetail.getFinPayment().getPaymentDate())) {
        return true;
      } else {
        return false;
      }
    }
  }
}
