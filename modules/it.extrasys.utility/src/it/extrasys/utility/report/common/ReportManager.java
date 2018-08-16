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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.businessUtility.TabAttachmentsData;
import org.openbravo.erpCommon.utility.JRFormatFactory;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.reporting.ReportingException;
import org.openbravo.model.ad.utility.Attachment;
import org.openbravo.utils.Replace;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;

public class ReportManager {
  protected static Logger log4j = Logger.getLogger(ReportManager.class);
  protected static final String TEMP_REPORT_DIR = "tmp";

  protected ConnectionProvider connectionProvider;
  protected String strBaseDesignPath;
  protected String strDefaultDesignPath;
  protected String language;
  protected String strBaseWeb; // BASE WEB!!!!!!
  protected String prefix;
  protected String strAttachmentPath;
  protected String fileNameNoResult;
  protected boolean multiReports = false;

  public ReportManager(ConnectionProvider connectionProvider, String ftpDirectory,
      String replaceWithFull, String baseDesignPath, String defaultDesignPath, String prefix,
      boolean multiReport) {
    this.connectionProvider = connectionProvider;
    this.strBaseWeb = replaceWithFull;
    this.strBaseDesignPath = baseDesignPath;
    this.strDefaultDesignPath = defaultDesignPath;
    this.strAttachmentPath = ftpDirectory;
    this.prefix = prefix;
    multiReports = multiReport;

    // Strip of ending slash character
    if (this.strBaseDesignPath.endsWith("/"))
      this.strBaseDesignPath = this.strBaseDesignPath.substring(0,
          this.strBaseDesignPath.length() - 1);
    if (this.strDefaultDesignPath.endsWith("/"))
      this.strDefaultDesignPath = this.strDefaultDesignPath.substring(0,
          this.strDefaultDesignPath.length() - 1);
  }

  public JasperPrint processReport(Report report, VariablesSecureApp variables)
      throws ReportingException {
    HashMap<String, Object> designParameters = processReportPrepareParameter(report, variables);
    return processReportPrint(report, variables, designParameters);
  }

  public HashMap<String, Object> processReportPrepareParameter(Report report,
      VariablesSecureApp variables) {
    setTargetDirectory(report);
    HashMap<String, Object> designParameters = populateDesignParameters(variables, report);
    return designParameters;
  }

  public JasperPrint processReportPrint(Report report, VariablesSecureApp variables,
      HashMap<String, Object> designParameters) throws ReportingException {

    if (report.bPartnerLanguage != null && !"".equals(report.bPartnerLanguage)) {
      language = report.bPartnerLanguage;
    } else {
      language = (variables.getLanguage() == null || variables.getLanguage().equals("")) ? "en_US"
          : variables.getLanguage();
    }
    final String baseDesignPath = this.prefix + "/" + this.strBaseDesignPath + "/"
        + this.strDefaultDesignPath;
    final Locale locale = new Locale(language.substring(0, 2), language.substring(3, 5));

    String templateLocation = report.getTemplateInfo().getTemplateLocation();
    templateLocation = Replace.replace(
        Replace.replace(templateLocation, "@basedesign@", baseDesignPath), "@baseattach@",
        this.strAttachmentPath);
    templateLocation = Replace.replace(templateLocation, "//", "/");

    final String templateFile = templateLocation + report.getTemplateInfo().getTemplateFilename();

    designParameters.put("TEMPLATE_LOCATION", templateLocation);
    JasperPrint jasperPrint = null;

    try {
      JasperDesign jasperDesign = JRXmlLoader.load(templateFile);

      Object[] parameters = jasperDesign.getParametersList().toArray();
      String parameterName = "";
      String subReportName = "";
      Collection<String> subreportList = new ArrayList<String>();

      /*
       * TODO: At present this process assumes the subreport is a .jrxml file. Need to handle the
       * possibility that this subreport file could be a .jasper file.
       */
      for (int i = 0; i < parameters.length; i++) {
        final JRDesignParameter parameter = (JRDesignParameter) parameters[i];
        if (parameter.getName().startsWith("SUBREP_")) {
          parameterName = parameter.getName();
          subreportList.add(parameterName);
          subReportName = Replace.replace(parameterName, "SUBREP_", "") + ".jrxml";
          JasperReport jasperReportLines = createSubReport(templateLocation, subReportName,
              baseDesignPath);
          designParameters.put(parameterName, jasperReportLines);
        }
      }

      JasperReport jasperReport = Utility.getTranslatedJasperReport(this.connectionProvider,
          templateFile, language, baseDesignPath);

      if (log4j.isDebugEnabled())
        log4j.debug("creating the format factory: " + variables.getJavaDateFormat());
      JRFormatFactory jrFormatFactory = new JRFormatFactory();
      jrFormatFactory.setDatePattern(variables.getJavaDateFormat());
      designParameters.put(JRParameter.REPORT_FORMAT_FACTORY, jrFormatFactory);
      String salesOrder = report.getCheckSalesOrder();
      if (salesOrder != null && salesOrder.equals("Y")) {
        designParameters.put(
            "DOCUMENT_NAME",
            Utility.messageBD(this.connectionProvider, "Sales", language) + " "
                + Utility.messageBD(this.connectionProvider, "Invoice", language));
      } else {
        designParameters.put(
            "DOCUMENT_NAME",
            Utility.messageBD(this.connectionProvider, "Purchase", language) + " "
                + Utility.messageBD(this.connectionProvider, "Invoice", language));
      }
      jasperPrint = fillReport(designParameters, jasperReport);

    } catch (final JRException exception) {
      log4j.error(exception.getMessage());
      exception.printStackTrace();
      throw new ReportingException(exception);
    } catch (final Exception exception) {
      log4j.error(exception.getMessage());
      exception.getStackTrace();
      throw new ReportingException(exception);
    }

    return jasperPrint;
  }

  protected String getAttachmentPath() {
    return this.strAttachmentPath;
  }

  protected String getTempReportDir() {
    return TEMP_REPORT_DIR;
  }

  public void setTargetDirectory(Report report) {
    final File targetDirectory = new File(getAttachmentPath() + "/" + getTempReportDir());
    if (!targetDirectory.exists())
      targetDirectory.mkdirs();
    report.setTargetDirectory(targetDirectory);
  }

  public void saveTempReport(Report report, VariablesSecureApp vars) {
    JasperPrint jasperPrint = null;
    try {
      jasperPrint = processReport(report, vars);
      saveReport(report, jasperPrint);
    } catch (final ReportingException e) {
      log4j.error(e.getMessage());
      e.printStackTrace();
    }
  }

  public void saveReport(Report report, JasperPrint jasperPrint) {
    String separator = "";
    if (!report.getTargetDirectory().toString().endsWith("/")) {
      separator = "/";
    }
    final String target = report.getTargetDirectory() + separator + report.getFilename();

    try {
      JasperExportManager.exportReportToPdfFile(jasperPrint, target);
    } catch (final JRException e) {
      e.printStackTrace();
    }
  }

  protected JasperPrint fillReport(HashMap<String, Object> designParameters,
      JasperReport jasperReport) throws ReportingException, SQLException {
    JasperPrint jasperPrint = null;

    Connection con = null;
    try {
      con = this.connectionProvider.getTransactionConnection();
      jasperPrint = JasperFillManager.fillReport(jasperReport, designParameters, con);
    } catch (final Exception e) {
      log4j.error(e.getMessage());
      e.printStackTrace();
      throw new ReportingException(e.getMessage());
    } finally {
      this.connectionProvider.releaseRollbackConnection(con);
    }
    return jasperPrint;
  }

  protected JasperReport createSubReport(String templateLocation, String subReportFileName,
      String baseDesignPath) {
    JasperReport jasperReportLines = null;
    try {
      jasperReportLines = Utility.getTranslatedJasperReport(this.connectionProvider,
          templateLocation + subReportFileName, language, baseDesignPath);
    } catch (final JRException e1) {
      log4j.error(e1.getMessage());
      e1.printStackTrace();
    }
    return jasperReportLines;
  }

  public File createAttachmentForReport(ConnectionProvider connectionProvider, Report report,
      String tableId, VariablesSecureApp vars) throws ReportingException, IOException {
    if (report.isAttached())
      throw new ReportingException(Utility.messageBD(connectionProvider, "AttachmentExists", (vars
          .getLanguage() == null || vars.getLanguage().equals("")) ? "en_US" : vars.getLanguage()));

    final String destination = tableId + "-" + report.getDocumentId();

    // First move the file to the correct destination
    final File destinationFolder = new File(this.strAttachmentPath + "/" + destination);
    if (!destinationFolder.exists()) {
      destinationFolder.mkdirs();
    }
    report.setTargetDirectory(destinationFolder);

    final JasperPrint jasperPrint = processReport(report, vars);
    saveReport(report, jasperPrint);

    attachFile(report);
    final File sourceFile = new File(report.getTargetLocation());
    final File destinationFile = new File(destinationFolder, sourceFile.getName());
    log4j.debug("Destination file before renaming: " + destinationFile);
    if (!sourceFile.renameTo(destinationFile))
      throw new ReportingException(Utility.messageBD(
          connectionProvider,
          "UnreachableDestination",
          (vars.getLanguage() == null || vars.getLanguage().equals("")) ? "en_US" : vars
              .getLanguage())
          + destinationFolder);

    report.setTargetDirectory(destinationFolder);
    // Attach them to the order in OB
    Connection conn = null;
    try {
      conn = this.connectionProvider.getTransactionConnection();

      final String newFileId = SequenceIdData.getUUID();
      log4j.debug("New file id: " + newFileId);
      // The 103 in the following insert specifies the document type: in
      // this case PDF
      TabAttachmentsData.insert(conn, this.connectionProvider, newFileId, vars.getClient(),
          vars.getOrg(), vars.getUser(), tableId, report.getDocumentId(), "103",
          "Generated by printing ", destinationFile.getName());

      this.connectionProvider.releaseCommitConnection(conn);
    } catch (final Exception exception) {
      try {
        this.connectionProvider.releaseRollbackConnection(conn);
      } catch (final Exception ignored) {
      }

      throw new ReportingException(exception);
    }

    report.setAttached(true);

    return destinationFile;
  }

  private void attachFile(Report report) {
    try {
      PdfReader readerTemp = new PdfReader(report.getTargetLocation());
      Document documentTemp = new Document(readerTemp.getPageSizeWithRotation(1));
      String[] tempName = report.getTargetLocation().split(".pdf");
      FileOutputStream osTemp = new FileOutputStream(tempName[0] + "TEMP.pdf");
      PdfCopy writerTemp = new PdfCopy(documentTemp, osTemp);
      documentTemp.open();
      PdfImportedPage page;
      int n = readerTemp.getNumberOfPages();

      for (int i = 0; i < n;) {
        ++i;
        page = writerTemp.getImportedPage(readerTemp, i);
        writerTemp.addPage(page);
      }
      documentTemp.close();
      writerTemp.close();

      PdfReader reader = new PdfReader(report.getTargetLocation());
      Document document = new Document(reader.getPageSizeWithRotation(1));
      FileOutputStream os = new FileOutputStream(report.getTargetLocation());
      PdfCopy writer = new PdfCopy(document, os);
      document.open();
      PdfReader readerTempFile = new PdfReader(tempName[0] + "TEMP.pdf");
      int nTemp = readerTempFile.getNumberOfPages();
      for (int i = 0; i < nTemp;) {
        ++i;
        page = writer.getImportedPage(readerTempFile, i);
        writer.addPage(page);
      }

      attachFiles(report, writer);
      document.close();
      File file = new File(tempName[0] + "TEMP.pdf");
      if (file.exists() && !file.isDirectory()) {
        file.delete();
      }
    } catch (Exception e) {
      log4j.error(e);
    }
  }

  public void attachFiles(Report report, PdfCopy writer) {
    PdfImportedPage page;
    // ADD ATTACH FILE

    String attachpath = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("attach.path");
    if (!attachpath.endsWith("/")) {
      attachpath = attachpath + "/";
    }
    attachpath = attachpath + "217-" + report.getDocTypeId() + "/";
    List<Attachment> atts = report.getAttachements();
    if (atts != null) {
      for (Attachment att : atts) {
        try {
          if (att.getName().endsWith(".pdf")) {
            PdfReader readerAtt = new PdfReader(attachpath + att.getName());
            int nAtt = readerAtt.getNumberOfPages();
            for (int i = 0; i < nAtt;) {
              ++i;
              page = writer.getImportedPage(readerAtt, i);
              writer.addPage(page);
            }
          }
        } catch (Exception e) {
          log4j.error(e);
        }
      }
    }

    // END ADD ATTACH FILE
  }

  protected HashMap<String, Object> populateDesignParameters(VariablesSecureApp variables,
      Report report) {
    final String baseDesignPath = this.prefix + "/" + this.strBaseDesignPath + "/"
        + this.strDefaultDesignPath;
    final HashMap<String, Object> designParameters = new HashMap<String, Object>();

    designParameters.put("DOCUMENT_ID", report.getDocumentId());

    designParameters.put("BASE_ATTACH", this.strAttachmentPath);
    designParameters.put("BASE_WEB", this.strBaseWeb);
    designParameters.put("BASE_DESIGN", baseDesignPath);
    designParameters.put("IS_IGNORE_PAGINATION", false);
    designParameters.put("USER_CLIENT",
        Utility.getContext(this.connectionProvider, variables, "#User_Client", ""));
    designParameters.put("USER_ORG",
        Utility.getContext(this.connectionProvider, variables, "#User_Org", ""));

    final String language = (report.getbPartnerLanguage() != null && !"".equals(report
        .getbPartnerLanguage())) ? report.getbPartnerLanguage()
        : (variables.getLanguage() == null || variables.getLanguage().equals("")) ? "en_US"
            : variables.getLanguage();
    designParameters.put("LANGUAGE", language);

    final Locale locale = new Locale(language.substring(0, 2), language.substring(3, 5));
    designParameters.put("REPORT_LOCALE", locale);

    final DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    dfs.setDecimalSeparator(variables.getSessionValue("#AD_ReportDecimalSeparator").charAt(0));
    dfs.setGroupingSeparator(variables.getSessionValue("#AD_ReportGroupingSeparator").charAt(0));
    final DecimalFormat NumberFormat = new DecimalFormat(
        variables.getSessionValue("#AD_ReportNumberFormat"), dfs);
    designParameters.put("NUMBERFORMAT", NumberFormat);

    return designParameters;
  }

  protected String getBaseDesignPath(String language) {
    String designPath = this.strDefaultDesignPath;
    designPath = this.prefix + "/" + this.strBaseDesignPath + "/" + designPath;

    return designPath;
  }

  public void printReports(HttpServletResponse response, Collection<JasperPrint> jrPrintReports,
      Collection<Report> reports) {
    ServletOutputStream os = null;
    String filename = "";
    try {
      if (reports != null && !reports.isEmpty()) {
        os = response.getOutputStream();
        response.setContentType("application/pdf");

        if (!multiReports) {
          for (Iterator<Report> iterator = reports.iterator(); iterator.hasNext();) {
            Report report = iterator.next();
            filename = report.getFilename();
          }
          response.setHeader("Content-disposition", "attachment" + "; filename=" + filename);
          for (Iterator<JasperPrint> iterator = jrPrintReports.iterator(); iterator.hasNext();) {
            JasperPrint jasperPrint = (JasperPrint) iterator.next();
            JasperExportManager.exportReportToPdfStream(jasperPrint, os);
          }
        } else {
          response.setContentType("application/pdf");
          concatReport(reports.toArray(new Report[] {}), response);
        }
      } else {
        filename = fileNameNoResult + ".pdf";
        os = response.getOutputStream();
        response.setContentType("application/pdf");
        response.setHeader("Content-disposition", "attachment" + "; filename=" + filename);
        try {
          JasperPrint jasperPrint = new JasperPrint();
          JasperExportManager.exportReportToPdfStream(jasperPrint, os);
        } catch (Exception e) {
        }
      }
    } catch (IOException e) {
      log4j.error(e.getMessage());
    } catch (JRException e) {
      e.printStackTrace();
    } finally {
      try {
        os.close();
        response.flushBuffer();
      } catch (IOException e) {
        log4j.error(e.getMessage(), e);
      }
    }
  }

  /*
   * This method is base on code originally created by Mark Thompson (Concatenate.java) and
   * distributed under the following conditions.
   * 
   * $Id: Concatenate.java 3373 2008-05-12 16:21:24Z xlv $
   * 
   * This code is free software. It may only be copied or modified if you include the following
   * copyright notice:
   * 
   * This class by Mark Thompson. Copyright (c) 2002 Mark Thompson.
   * 
   * This code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
   * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   */
  private void concatReport(Report[] reports, HttpServletResponse response) {
    try {
      int pageOffset = 0;
      // ArrayList master = new ArrayList();
      int f = 0;
      String filename = "";
      Report outFile = null;
      if (reports.length == 1)
        filename = reports[0].getFilename();
      Document document = null;
      PdfCopy writer = null;
      while (f < reports.length) {
        if (filename == null || filename.equals("")) {
          outFile = reports[f];
          if (multiReports) {
            filename = outFile.getTemplateInfo().getReportFilename();
            filename = filename.replaceAll("@our_ref@", "");
            filename = filename.replaceAll("@cus_ref@", "");
            filename = filename.replaceAll(" ", "_");
            filename = filename.replaceAll("-", "");
            filename = filename + ".pdf";
          } else {
            filename = outFile.getFilename();
          }
        }
        response.setHeader("Content-disposition", "attachment" + "; filename=" + filename);
        // we create a reader for a certain document
        PdfReader reader = new PdfReader(reports[f].getTargetLocation());
        reader.consolidateNamedDestinations();
        // we retrieve the total number of pages
        int n = reader.getNumberOfPages();
        pageOffset += n;

        if (f == 0) {
          // step 1: creation of a document-object
          document = new Document(reader.getPageSizeWithRotation(1));
          // step 2: we create a writer that listens to the document
          writer = new PdfCopy(document, response.getOutputStream());
          // step 3: we open the document
          document.open();
        }
        // step 4: we add content
        PdfImportedPage page;
        for (int i = 0; i < n;) {
          ++i;
          page = writer.getImportedPage(reader, i);
          writer.addPage(page);
        }
        if (reports[f].isDeleteable()) {
          File file = new File(reports[f].getTargetLocation());
          if (file.exists() && !file.isDirectory()) {
            file.delete();
          }
        }
        f++;
      }
      document.close();
    } catch (Exception e) {
      log4j.error(e);
    }
  }

  public String getFileNameNoResult() {
    return fileNameNoResult;
  }

  public void setFileNameNoResult(String fileNameNoResult) {
    this.fileNameNoResult = fileNameNoResult;
  }

}
