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
 * All portions are Copyright (C) 2009-2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

package it.extrasys.utility.report.common;

import it.extrasys.utility.report.common.TemplateInfo.EmailDefinition;
import it.extrasys.utility.report.common.dao.ReportDAO;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.reporting.ReportingException;
import org.openbravo.erpCommon.utility.reporting.TemplateData;
import org.openbravo.model.ad.utility.Attachment;

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

  protected static Logger log4j = Logger.getLogger(Report.class);

  protected DocumentType documentType;
  protected String documentId; // Order Id, invoice id, etc.
  protected String documentStatus;
  protected String ourReference;
  protected String cusReference;
  protected String salnam;
  protected String bPartnerId;
  protected String bPartnerName;
  protected String bPartnerLanguage;
  protected String filename;
  protected String isebill;
  protected File targetDirectory;
  protected boolean isAttached;
  protected String docTypeId;
  protected String orgId;
  protected boolean deleteReport = false;
  protected boolean multiReports = false;
  protected String checkSalesOrder;
  protected TemplateInfo templateInfo;
  protected ReportData[] reportData;
  protected String dateStamp;
  protected List<Attachment> attachements;

  public String getDateStamp() {
    return dateStamp;
  }

  public void setDateStamp(String dateStamp) {
    this.dateStamp = dateStamp;
  }

  public ReportData[] getReportData() {
    return reportData;
  }

  public void setReportData(ReportData[] reportData) {
    this.reportData = reportData;
  }

  public String getDocTypeId() {
    return docTypeId;
  }

  public void setDocTypeId(String docTypeId) {
    this.docTypeId = docTypeId;
  }

  public Report() {

  }

  public String getbPartnerLanguage() {
    return bPartnerLanguage;
  }

  public void setbPartnerLanguage(String bPartnerLanguage) {
    this.bPartnerLanguage = bPartnerLanguage;
  }

  public List<Attachment> getAttachements() {
    return attachements;
  }

  public void setAttachements(List<Attachment> attachements) {
    this.attachements = attachements;
  }

  public Report(ConnectionProvider connectionProvider, DocumentType documentType,
      String documentId, String strLanguage, String templateId, boolean multiReport, String prefix,
      String strBaseDesignPath, String strDefaultDesignPath, OutputTypeEnum outputTypeString)
      throws ReportingException, ServletException {
    this.documentType = documentType;
    this.documentId = documentId;
    ReportData[] reportData = null;

    switch (this.documentType) {
    case QUOTATION: // Retrieve quotation information
      reportData = ReportData.getOrderInfo(connectionProvider, documentId);
      break;
    case SALESORDER: // Retrieve order information
      reportData = ReportData.getOrderInfo(connectionProvider, documentId);
      break;

    case SALESINVOICE: // Retrieve invoice information
      reportData = ReportData.getInvoiceInfo(connectionProvider, documentId);
      break;

    case SHIPMENT: // Retrieve shipment information
      reportData = ReportData.getShipmentInfo(connectionProvider, documentId);
      break;

    default:
      throw new ReportingException(Utility.messageBD(connectionProvider, "UnknownDocumentType",
          strLanguage) + this.documentType);
    }
    multiReports = multiReport;
    if (reportData.length == 1) {
      checkSalesOrder = reportData[0].getField("isSalesOrderTransaction");
      orgId = reportData[0].getField("ad_Org_Id");
      docTypeId = reportData[0].getField("docTypeTargetId");

      this.ourReference = reportData[0].getField("ourreference");
      this.cusReference = reportData[0].getField("cusreference");
      this.salnam = reportData[0].getField("salnam");
      this.bPartnerId = reportData[0].getField("bpartner_id");
      this.bPartnerName = reportData[0].getField("bpartner_name");
      this.bPartnerLanguage = reportData[0].getField("bpartner_language");
      this.documentStatus = reportData[0].getField("docstatus");

      this.isebill = "N";
      if (reportData[0].getField("isebill") != null && !reportData[0].getField("isebill").isEmpty()) {
        this.isebill = reportData[0].getField("isebill");
      }
      // ---------------------------------------
      // Multilanguage management
      templateInfo = new TemplateInfo(connectionProvider, docTypeId, orgId, strLanguage,
          templateId, this.bPartnerLanguage, prefix, strBaseDesignPath, strDefaultDesignPath, isebill);
      // ---------------------------------------

      // ADDED TO ATTACH PDF FILE TO REPORT OUTPUT
      attachements = ReportDAO.getSingleton().findAttachment("217", docTypeId, strLanguage);

      this.filename = generateReportFileName();
      this.targetDirectory = null;
    }
    // else
    // throw new ReportingException(Utility.messageBD(connectionProvider, "NoDataReport",
    // strLanguage) + documentId);
  }

  public void loadReportData(ConnectionProvider connectionProvider, String strLanguage,
      String templateId, String prefix, String strBaseDesignPath, String strDefaultDesignPath)
      throws ServletException, ReportingException {
    if (reportData.length == 1) {
      checkSalesOrder = reportData[0].getField("isSalesOrderTransaction");
      orgId = reportData[0].getField("ad_Org_Id");
      docTypeId = reportData[0].getField("docTypeTargetId");

      ourReference = reportData[0].getField("ourreference");
      cusReference = reportData[0].getField("cusreference");
      bPartnerId = reportData[0].getField("bpartner_id");
      bPartnerLanguage = reportData[0].getField("bpartner_language");
      documentStatus = reportData[0].getField("docstatus");
      if (templateInfo == null)
        // ---------------------------------------
        // R. Gervasi
        templateInfo = new TemplateInfo(connectionProvider, docTypeId, orgId, strLanguage,
            templateId, this.bPartnerLanguage, prefix, strBaseDesignPath, strDefaultDesignPath,"N");
      // ---------------------------------------

      filename = generateReportFileName();
      targetDirectory = null;

      // ADDED TO ATTACH PDF FILE TO REPORT OUTPUT
      attachements = ReportDAO.getSingleton().findAttachment("217", docTypeId, strLanguage);

    } else
      throw new ReportingException(Utility.messageBD(connectionProvider, "NoDataReport",
          strLanguage) + documentId);
  }

  public void setTemplateInfo(TemplateInfo templateInfo) {
    this.templateInfo = templateInfo;
  }

  protected String generateReportFileName() {
    // Generate the target report filename
    final String dateStamp = Utility.formatDate(new Date(), "yyyyMMdd-HHmmss");
    String reportFilename = templateInfo.getReportFilename();
    reportFilename = reportFilename.replaceAll("@our_ref@", ourReference);
    reportFilename = reportFilename.replaceAll("@cus_ref@", cusReference);
    reportFilename = reportFilename.replaceAll("@cus_nam@", bPartnerName);
    if (checkSalesOrder.equalsIgnoreCase("y")
        && !this.documentType.toString().equalsIgnoreCase("PAYMENT")) {
      reportFilename = reportFilename.replaceAll("@sal_nam@", salnam);
    } else {
      reportFilename = reportFilename.replaceAll("@sal_nam@", "");
    }
    // only characters, numbers and "." are accepted. Others will be changed for "_"
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
    return documentType.getContextSubFolder();
  }

  public DocumentType getDocumentType() {
    return documentType;
  }

  public String getDocumentId() {
    return documentId;
  }

  public TemplateInfo getTemplateInfo() {
    return templateInfo;
  }

  public EmailDefinition getEmailDefinition() throws ReportingException {
    return templateInfo.getEmailDefinition(bPartnerLanguage);
  }

  public String getOurReference() {
    return ourReference;
  }

  public String getCusReference() {
    return cusReference;
  }

  public String getDocumentStatus() {
    return documentStatus;
  }

  public String getBPartnerId() {
    return bPartnerId;
  }

  public boolean isDraft() {
    return documentStatus.equals("DR");
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String newFileName) {
    filename = newFileName;
  }

  public File getTargetDirectory() {
    return targetDirectory;
  }

  public void setTargetDirectory(File targetDirectory) {
    this.targetDirectory = targetDirectory;
  }

  public String getTargetLocation() throws IOException {
    return targetDirectory.getCanonicalPath() + "/" + filename;
  }

  public boolean isAttached() {
    return isAttached;
  }

  public void setAttached(boolean attached) {
    isAttached = attached;
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
