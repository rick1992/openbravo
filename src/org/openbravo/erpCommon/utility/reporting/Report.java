/*
 * ************************************************************************ The
 * contents of this file are subject to the Openbravo Public License Version 1.1
 * (the "License"), being the Mozilla Public License Version 1.1 with a
 * permitted attribution clause; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.openbravo.com/legal/license.html Software distributed under the
 * License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing rights and limitations under the License. The Original Code is
 * Openbravo ERP. The Initial Developer of the Original Code is Openbravo SLU All
 * portions are Copyright (C) 2001-2014 Openbravo SLU All Rights Reserved.
 * Contributor(s): ______________________________________.
 * ***********************************************************************
 */
package org.openbravo.erpCommon.utility.reporting;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.reporting.TemplateInfo.EmailDefinition;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.warehouse.pickinglist.PickingList;

public class Report {
  public String getOrgId() {
    return orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  public enum OutputTypeEnum {
    DEFAULT, PRINT, ARCHIVE, EMAIL
  }

  public OutputTypeEnum outputType = OutputTypeEnum.DEFAULT;

  private static Logger log4j = Logger.getLogger(Report.class);

  private DocumentType _DocumentType;
  private String _DocumentId; // Order Id, invoice id, etc.
  private String _DocumentStatus;
  private String _OurReference;
  private String _CusReference;
  private String _SalNam;
  private String _BPartnerId;
  private String _BPartnerName;
  private String _BPartnerLanguage;
  private String _DocDate; // Date Ordered, Date Invoices, etc.
  private String _MinDueDate; // Useful for Orders and Invoices
  private String _MaxDueDate; // Useful for Orders and Invoices
  private String _DocDescription;
  private String _ContactName;
  private String _Filename;
  private File _targetDirectory;
  private boolean _isAttached;
  private String docTypeId;
  private String orgId;
  private boolean deleteReport = false;
  private boolean multiReports = false;
  private String checkSalesOrder;
  private String _isebill;

  public String getDocTypeId() {
    return docTypeId;
  }

  public void setDocTypeId(String docTypeId) {
    this.docTypeId = docTypeId;
  }

  private TemplateInfo templateInfo;

  public Report(ConnectionProvider connectionProvider, DocumentType documentType, String documentId,
      String strLanguage, String templateId, boolean multiReport, OutputTypeEnum outputTypeString)
      throws ReportingException, ServletException {
    _DocumentType = documentType;
    _DocumentId = documentId;
    outputType = outputTypeString;
    ReportData[] reportData = null;

    System.out.println("DOCUMENT TTTTTYYYYPPPEEEEEE:" + _DocumentType);
    System.out.println("ENTER HERE 1");
    switch (_DocumentType) {
    case QUOTATION: // Retrieve quotation information
      reportData = ReportData.getOrderInfo(connectionProvider, documentId);
      if (!reportData[0].getField("docstatus").equals("UE")) {
        throw new ServletException("SCR_DocumentCannotPrinted");
      }
      break;

    case SALESORDER: // Retrieve order information
      reportData = ReportData.getOrderInfo(connectionProvider, documentId);
      if (reportData[0].getField("specialdoctype").equals("SCOSALESQUOTATION")) {
        if (!reportData[0].getField("docstatus").equals("UE")) {
          throw new ServletException("SCR_DocumentCannotPrinted");
        }
      }
      break;

    case SALESINVOICE: // Retrieve invoice information
      reportData = ReportData.getInvoiceInfo(connectionProvider, documentId);
      if (reportData[0].getField("specialdoctype").equals("SCOARINVOICE")) { // SALES
        // INVOICE
        if (reportData[0].getField("docstatus").equals("DR"))
          throw new ServletException("SCR_DocumentCannotPrinted");
      } else if (!reportData[0].getField("docstatus").equals("CO")) { // OTROS
        // INVOICES
        throw new ServletException("SCR_DocumentCannotPrinted");
      }
      break;

    case SHIPMENT: // Retrieve shipment information
      reportData = ReportData.getShipmentInfo(connectionProvider, documentId);
      if (!reportData[0].getField("docstatus").equals("CO")
          && !reportData[0].getField("param").equals("Y")) { // EM_Swa_Is_Wait_Picking
        throw new ServletException("SCR_DocumentCannotPrinted");
      }
      break;

    case PAYMENT: // Retrieve payment information
      reportData = ReportData.getPaymentInfo(connectionProvider, documentId);
      break;

    case WAREHOUSEPICKINGLISTS: // Retrieve picking list information

      reportData = ReportData.getPickingListInfo(connectionProvider, documentId);
      if (reportData[0].getField("docstatus").equals("CA")) { // Estado
        // cancelado
        throw new ServletException("SCR_DocumentCannotPrinted");
      }

      /*
       * if (reportData[0].getField("userpickeroid") == null ||
       * reportData[0].getField("userpickeroid").equals("")) { // Estado // cancelado throw new
       * ServletException("swa_cannotprint_notpicker"); }
       */

      PickingList picking = OBDal.getInstance().get(PickingList.class,
          reportData[0].getField("documentId"));
      List<ShipmentInOutLine> pickingListLine = picking
          .getMaterialMgmtShipmentInOutLineEMObwplPickinglistIDList();

      // for (int i = 0; i < pickingListLine.size(); i++) {
      for (int i = 0; i < pickingListLine.size(); i++) {
        if (!pickingListLine.get(i).getShipmentReceipt().getSCOSpecialDocType()
            .equals("SWAMMDISPATCH")) {// validación
          // salida
          if (pickingListLine.get(i).getShipmentReceipt().getSCRPhysicalDocumentNo() == null
              || pickingListLine.get(i).getShipmentReceipt().getSCRPhysicalDocumentNo().equals("")
              || pickingListLine.get(i).getShipmentReceipt().getSCRPhysicalDocumentNo()
                  .length() < 11
              || pickingListLine.get(i).getShipmentReceipt().getSCRPhysicalDocumentNo()
                  .equals("000-0000000")) {
            throw new ServletException("swa_print_invalidphysical");
          }
        }
      }

      // Validando si el picking fue por un pedido de venta
      // System.out.println("PRIMERO");
      if (picking.getSwaCOrder() != null) {
        if (!picking.getSWAUpdateReason().getSearchKey().equals("ventasujetacomprador")) {
          // --Validando la factura
          for (int i = 0; i < 1; i++) {
            if (pickingListLine.get(i).getSalesOrderLine() != null) {
              List<InvoiceLine> invoiceLineList = pickingListLine.get(i).getSalesOrderLine()
                  .getInvoiceLineList();
              if (invoiceLineList.size() == 0) {
                // throw new
                // ServletException("swa_print_picking_no_invoice");
              }
              for (int j = 0; j < invoiceLineList.size(); j++) {
                if (invoiceLineList.get(j).getInvoice().getScrPhysicalDocumentno() == null
                    || invoiceLineList.get(j).getInvoice().getScrPhysicalDocumentno().equals("")
                    || invoiceLineList.get(j).getInvoice().getScrPhysicalDocumentno().length() < 11
                    || invoiceLineList.get(j).getInvoice().getScrPhysicalDocumentno()
                        .equals("000-0000000")) {
                  // throw new
                  // ServletException("swa_print_invalidphysical_invoice");
                }
              }
            }
          }
        }
      }
      //
      // / System.out.println("SEGUNDO");
      // if (reportData[0].getField("isprinted").equals("Y")) {
      // throw new ServletException("SPL_DocumentPreviouslyPrinted");
      // }
      break;
    case PRELIQUIDATION: // Retrieve preliquidation information
      reportData = ReportData.getPreliquidationInfo(connectionProvider, documentId);
      break;

    case COSTINGIMPORT: // Retrieve payment out folio information
      reportData = ReportData.getCostingImportInfo(connectionProvider, documentId);
      break;

    case PURCHASEWITHHOLDINGRECEIPT: // Retrieve purchase withholding
      // receipt information
      reportData = ReportData.getPWithholdingReceiptInfo(connectionProvider, documentId);
      break;

    case SALESWITHHOLDINGRECEIPT: // Retrieve sales withholding receipt
      // information
      reportData = ReportData.getSWithholdingReceiptInfo(connectionProvider, documentId);
      break;

    case FINANCIALACCOUNT: // Retrieve sales withholding receipt information
      reportData = ReportData.getFinancialAccountInfo(connectionProvider, documentId);
      break;

    case PURCHASEORDERIMPORT: // Retrieve purchase order import information
      reportData = ReportData.getPurchaseOrderImportInfo(connectionProvider, documentId);

      if (!reportData[0].getField("docstatus").equals("CO")) {
        // throw new ServletException("SSA_SODocumentCannotPrinted");
      }

      break;

    case PAYMENTOUTCHECK: // Retrieve payment out check information
      reportData = ReportData.getPaymentOutCheckInfo(connectionProvider, documentId);
      break;

    case SALESPERCEPTIONRECEIPT: // Retrieve sales perception receipt
      // information
      reportData = ReportData.getSalesPerceptionReceiptInfo(connectionProvider, documentId);
      break;

    case REQUISITION: // Retrieve requisition information
      reportData = ReportData.getRequisitionInfo(connectionProvider, documentId);
      break;

    case ACCOUNTABILITY: // Retrieve accountability information
      reportData = ReportData.getAccountabilityInfo(connectionProvider, documentId);
      break;

    case BILLOFEXCHANGETOCOLLECTIONDOC: // Retrieve bill of exchange to
      // collection document
      // information
      reportData = ReportData.getBillOfExchangeToCollectionDocument(connectionProvider, documentId);
      break;

    case FOLIO:
      reportData = ReportData.getFolioImportInfo(connectionProvider, documentId);
      break;

    case GOODSTRANSFER:
      reportData = ReportData.getGoodsTransferInfo(connectionProvider, documentId);

      if (!reportData[0].getField("docstatus").equals("CO")) {
        throw new ServletException("SCR_DocumentCannotPrinted");
      }

      break;

    case GOODSMOVEMENT:
      reportData = ReportData.getGoodsMovementInfo(connectionProvider, documentId);
      break;

    case BOMPRODUCTION:
      reportData = ReportData.getBOMProductionInfo(connectionProvider, documentId);
      break;

    case PURCHASEINVOICE:
      reportData = ReportData.getPurchaseInvoiceInfo(connectionProvider, documentId);
      if (!reportData[0].getField("docstatus").equals("CO")) { // Estado
        // cancelado
        throw new ServletException("SCR_DocumentCannotPrinted");
      }
      break;

    case PURCHASEPREPAYMENT:
      reportData = ReportData.getPurchasePrepaymentInfo(connectionProvider, documentId);
      if (!reportData[0].getField("docstatus").equals("CO")
          && !reportData[0].getField("docstatus").equals("SCO_OP")) {
        throw new ServletException("SCR_DocumentCannotPrinted");
      }
      break;

    case MOVEMENTCODE: // Retrieve movement code information
      reportData = ReportData.getMovementCodeInfo(connectionProvider, documentId);
      break;
    case REQUERIMIENTOREPOSICION: // Retrieve requerimiento reposicion
      // information
      reportData = ReportData.getRequerimientoReposicionInfo(connectionProvider, documentId);
      if (!reportData[0].getField("docstatus").equals("CO")
          && !reportData[0].getField("docstatus").equals("PD")) { // Estado
        // cancelado
        throw new ServletException("SCR_DocumentCannotPrinted");
      }
      break;

    case SALESINVOICEORDER: // Retrieve invoice information
      reportData = ReportData.getInvoiceInfo(connectionProvider, documentId);

      if (reportData[0].getField("specialdoctype").equals("SCOARINVOICE")) { // SALES
        // INVOICE
        if (reportData[0].getField("docstatus").equals("DR"))
          throw new ServletException("SCR_DocumentCannotPrinted");
      } else if (!reportData[0].getField("docstatus").equals("CO")) { // OTROS
        // INVOICES
        throw new ServletException("SCR_DocumentCannotPrinted");
      }
      break;

    case SHIPMENTORDER: // Retrieve shipment information
      reportData = ReportData.getShipmentInfo(connectionProvider, documentId);
      if (!reportData[0].getField("docstatus").equals("CO")
          && !reportData[0].getField("param").equals("Y")) { // EM_Swa_Is_Wait_Picking
        throw new ServletException("SCR_DocumentCannotPrinted");
      }
      break;

    case SALESPROSPECT: // Retrieve sales prospect information
      reportData = ReportData.getSalesProspectInfo(connectionProvider, documentId);
      if (!reportData[0].getField("docstatus").equals("CO")) {
        throw new ServletException("SCR_DocumentCannotPrinted");
      }
      break;

    case SRECONTRACTPAYSCHEDULE: // Retrieve purchase contract payment schedule information
      reportData = ReportData.getSreContractPayScheduleInfo(connectionProvider, documentId);
      if (!reportData[0].getField("docstatus").equals("CO")) {
        throw new ServletException("SCR_DocumentCannotPrinted");
      }
      break;

    default:
      throw new ReportingException(
          Utility.messageBD(connectionProvider, "UnknownDocumentType", strLanguage)
              + _DocumentType);
    }

    multiReports = multiReport;
    if (reportData.length == 1) {
      checkSalesOrder = reportData[0].getField("isSalesOrderTransaction");
      orgId = reportData[0].getField("ad_Org_Id");
      docTypeId = reportData[0].getField("docTypeTargetId");

      _OurReference = reportData[0].getField("ourreference");
      _CusReference = reportData[0].getField("cusreference");
      _SalNam = reportData[0].getField("salnam");
      _BPartnerId = reportData[0].getField("bpartner_id");
      _BPartnerName = reportData[0].getField("bpartner_name");
      _BPartnerLanguage = reportData[0].getField("bpartner_language");
      _DocumentStatus = reportData[0].getField("docstatus");
      _DocDate = reportData[0].getField("docdate");
      _MinDueDate = reportData[0].getField("minduedate");
      _MaxDueDate = reportData[0].getField("maxduedate");
      _DocDescription = reportData[0].getField("docdesc");
      _ContactName = reportData[0].getField("contact_name");

      _isebill = "N";
      if (reportData[0].getField("isebill") != null
          && !reportData[0].getField("isebill").isEmpty()) {
        _isebill = reportData[0].getField("isebill");
      }

      templateInfo = new TemplateInfo(connectionProvider, docTypeId, orgId, strLanguage, templateId,
          _DocumentType, _DocumentStatus, _isebill);

      _Filename = generateReportFileName();
      _targetDirectory = null;
    } else
      throw new ReportingException(
          Utility.messageBD(connectionProvider, "NoDataReport", strLanguage) + documentId);

  }

  public void setTemplateInfo(TemplateInfo templateInfo) {
    this.templateInfo = templateInfo;
  }

  private String generateReportFileName() {
    // Generate the target report filename
    final String dateStamp = Utility.formatDate(new Date(), "yyyyMMdd-HHmmss");
    String reportFilename = templateInfo.getReportFilename();
    reportFilename = reportFilename.replaceAll("@our_ref@",
        Matcher.quoteReplacement(_OurReference));
    reportFilename = reportFilename.replaceAll("@cus_ref@",
        Matcher.quoteReplacement(_CusReference));
    reportFilename = reportFilename.replaceAll("@cus_nam@", Matcher.quoteReplacement(_ContactName));
    reportFilename = reportFilename.replaceAll("@bp_nam@", Matcher.quoteReplacement(_BPartnerName));
    reportFilename = reportFilename.replaceAll("@doc_date@", Matcher.quoteReplacement(_DocDate));
    reportFilename = reportFilename.replaceAll("@doc_nextduedate@",
        Matcher.quoteReplacement(_MinDueDate));
    reportFilename = reportFilename.replaceAll("@doc_lastduedate@",
        Matcher.quoteReplacement(_MaxDueDate));
    // reportFilename = reportFilename.replaceAll("@doc_desc@",
    // Matcher.quoteReplacement(_DocDescription)); // Too long
    if (checkSalesOrder.equalsIgnoreCase("y")
        && !_DocumentType.toString().equalsIgnoreCase("PAYMENT")) {
      reportFilename = reportFilename.replaceAll("@sal_nam@", Matcher.quoteReplacement(_SalNam));
    } else {
      reportFilename = reportFilename.replaceAll("@sal_nam@", "");
    }
    // only characters, numbers and "." are accepted. Others will be changed
    // for "_"
    reportFilename = reportFilename.replaceAll("[^A-Za-z0-9\\.]", "_");
    reportFilename = reportFilename + "." + dateStamp + ".pdf";
    if (log4j.isDebugEnabled())
      log4j.debug("target report filename: " + reportFilename);

    if (multiReports && outputType.equals(OutputTypeEnum.PRINT)) {
      reportFilename = UUID.randomUUID().toString() + "_" + reportFilename;
      setDeleteable(true);
    }

    return reportFilename;
  }

  public String getContextSubFolder() throws ServletException {
    return _DocumentType.getContextSubFolder();
  }

  public DocumentType getDocumentType() {
    return _DocumentType;
  }

  public String getDocumentId() {
    return _DocumentId;
  }

  public TemplateInfo getTemplateInfo() {
    return templateInfo;
  }

  public EmailDefinition getEmailDefinition() throws ReportingException {
    return templateInfo.getEmailDefinition(_BPartnerLanguage);
  }

  public EmailDefinition getDefaultEmailDefinition() throws ReportingException {
    return templateInfo.get_DefaultEmailDefinition();
  }

  public Map<String, EmailDefinition> getEmailDefinitions() throws ReportingException {
    return templateInfo.getEmailDefinitions();
  }

  public String getOurReference() {
    return _OurReference;
  }

  public String getCusReference() {
    return _CusReference;
  }

  public String getDocumentStatus() {
    return _DocumentStatus;
  }

  public String getBPartnerId() {
    return _BPartnerId;
  }

  public String getBPName() {
    return _BPartnerName;
  }

  public String getDocDate() {
    return _DocDate;
  }

  public String getMinDueDate() {
    return _MinDueDate;
  }

  public String getMaxDueDate() {
    return _MaxDueDate;
  }

  public String getDocDescription() {
    return _DocDescription;
  }

  public boolean isDraft() {
    return _DocumentStatus.equals("DR") || _DocumentStatus.equals("RPAP");
  }

  public String getFilename() {
    return _Filename;
  }

  public void setFilename(String newFileName) {
    _Filename = newFileName;
  }

  public File getTargetDirectory() {
    return _targetDirectory;
  }

  public void setTargetDirectory(File targetDirectory) {
    _targetDirectory = targetDirectory;
  }

  public String getTargetLocation() throws IOException {
    return _targetDirectory.getCanonicalPath() + "/" + _Filename;
  }

  public boolean isAttached() {
    return _isAttached;
  }

  public void setAttached(boolean attached) {
    _isAttached = attached;
  }

  public TemplateData[] getTemplate() {
    if (templateInfo.getTemplates() != null) {
      return templateInfo.getTemplates();
    }
    return null;
  }

  public boolean isDeleteable() {
    return deleteReport;
  }

  public void setDeleteable(boolean deleteable) {
    deleteReport = deleteable;
  }

  public String getCheckSalesOrder() {
    return checkSalesOrder;
  }

  public void setCheckSalesOrder(String checkSalesOrder) {
    this.checkSalesOrder = checkSalesOrder;
  }

}
