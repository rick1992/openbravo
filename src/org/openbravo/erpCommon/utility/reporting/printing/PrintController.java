/*
 * The contents of this file are subject to the Openbravo Public License Version
 * 1.0 (the "License"), being the Mozilla Public License Version 1.1 with a
 * permitted attribution clause; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.openbravo.com/legal/license.html Software distributed under the
 * License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing rights and limitations under the License. The Original Code is
 * Openbravo ERP. The Initial Developer of the Original Code is Openbravo SLU All
 * portions are Copyright (C) 2008-2014 Openbravo SLU All Rights Reserved.
 * Contributor(s): ______________________________________.
 */
package org.openbravo.erpCommon.utility.reporting.printing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.regex.Matcher;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.email.EmailUtils;
import org.openbravo.erpCommon.utility.BasicUtility;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.poc.EmailManager;
import org.openbravo.erpCommon.utility.poc.EmailType;
import org.openbravo.erpCommon.utility.reporting.DocumentType;
import org.openbravo.erpCommon.utility.reporting.Report;
import org.openbravo.erpCommon.utility.reporting.Report.OutputTypeEnum;
import org.openbravo.erpCommon.utility.reporting.ReportManager;
import org.openbravo.erpCommon.utility.reporting.ReportingException;
import org.openbravo.erpCommon.utility.reporting.TemplateData;
import org.openbravo.erpCommon.utility.reporting.TemplateInfo;
import org.openbravo.erpCommon.utility.reporting.TemplateInfo.EmailDefinition;
import org.openbravo.exception.NoConnectionAvailableException;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.ad.system.Language;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.EmailServerConfiguration;
import org.openbravo.model.common.enterprise.EmailTemplate;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.enterprise.Warehouse;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import pe.com.unifiedgo.core.data.ScrUserwarehouseDocserie;
import pe.com.unifiedgo.ebilling.data.BILLPhyDocSequence;
import pe.com.unifiedgo.ebilling.data.BILLPhyDocType;

@SuppressWarnings("serial")
public class PrintController extends HttpSecureAppServlet {
  private final Map<String, TemplateData[]> differentDocTypes = new HashMap<String, TemplateData[]>();
  private boolean multiReports = false;
  private boolean archivedReports = false;

  final public static String defPhyDocNo = "000-0000000";
  final public static int phyDocRDigits = 7;
  final public static int phyDocLDigits = 3;

  final public static String eDefPhyDocNo = "F000-00000000";
  final public static int ePhyDocRDigits = 8;
  final public static int ePhyDocLDigits = 4;

  @Override
  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    final VariablesSecureApp vars = new VariablesSecureApp(request);

    DocumentType documentType = DocumentType.UNKNOWN;
    String sessionValuePrefix = null;
    String strDocumentId = null;

    // Determine which process called the print controller
    if (log4j.isDebugEnabled())
      log4j.debug("Servletpath: " + request.getServletPath());
    if (request.getServletPath().toLowerCase().indexOf("quotations") != -1) {
      documentType = DocumentType.QUOTATION;
      // The prefix PRINTORDERS is a fixed name based on the KEY of the
      // AD_PROCESS
      sessionValuePrefix = "PRINTQUOTATIONS";

      strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpcOrderId_R");
      if (strDocumentId.equals(""))
        strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpcOrderId");
    }
    if (request.getServletPath().toLowerCase().indexOf("orders") != -1) {
      documentType = DocumentType.SALESORDER;
      // The prefix PRINTORDERS is a fixed name based on the KEY of the
      // AD_PROCESS
      sessionValuePrefix = "PRINTORDERS";

      strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpcOrderId_R");
      if (strDocumentId.equals(""))
        strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpcOrderId");
    }
    if (request.getServletPath().toLowerCase().indexOf("invoices") != -1) {
      documentType = DocumentType.SALESINVOICE;
      // The prefix PRINTINVOICES is a fixed name based on the KEY of the
      // AD_PROCESS
      sessionValuePrefix = "PRINTINVOICES";

      strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpcInvoiceId_R");
      if (strDocumentId.equals(""))
        strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpcInvoiceId");
    }
    if (request.getServletPath().toLowerCase().indexOf("shipments") != -1) {
      documentType = DocumentType.SHIPMENT;
      // The prefix PRINTINVOICES is a fixed name based on the KEY of the
      // AD_PROCESS
      sessionValuePrefix = "PRINTSHIPMENTS";

      strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpmInoutId_R");
      if (strDocumentId.equals(""))
        strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpmInoutId");
    }
    if (request.getServletPath().toLowerCase().indexOf("payments") != -1) {
      documentType = DocumentType.PAYMENT;
      // The prefix PRINTPAYMENTS is a fixed name based on the KEY of the
      // AD_PROCESS
      sessionValuePrefix = "PRINTPAYMENT";

      strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpfinPaymentId_R");
      if (strDocumentId.equals(""))
        strDocumentId = vars.getSessionValue(sessionValuePrefix + ".inpfinPaymentId");
    }

    post(request, response, vars, documentType, sessionValuePrefix, strDocumentId);

  }

  protected void post(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, DocumentType documentType, String sessionValuePrefix,
      String strDocumentId) throws IOException, ServletException {
    post(request, response, vars, documentType, sessionValuePrefix, strDocumentId, "",
        "org/openbravo/erpCommon/utility/reporting/printing/PrintOptions");
  }

  @SuppressWarnings("unchecked")
  protected void post(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, DocumentType documentType, String sessionValuePrefix,
      String strDocumentId, String printfile_suffix, String printxmlfile_path)
      throws IOException, ServletException {
    OBError myMessage = null;
    String newPhyDocNo = null;
    String newAdUserPickerId = null;
    System.out.println("PRINTCONTROLLER1 TABID:" + vars.getSessionValue("inpTabId"));
    try {
      System.out.println("postcontroller 1");
      String fullDocumentIdentifier = strDocumentId + documentType.getTableName();
      System.out.println("postcontroller 2 fullDocumentIdentifier:" + fullDocumentIdentifier);
      Map<String, Report> reports;
      // Checks are maintained in this way for mulithread safety
      HashMap<String, Boolean> checks = new HashMap<String, Boolean>();
      checks.put("moreThanOneCustomer", Boolean.FALSE);
      checks.put("moreThanOnesalesRep", Boolean.FALSE);
      String documentIds[] = null;
      if (log4j.isDebugEnabled())
        log4j.debug("strDocumentId: " + strDocumentId);
      // normalize the string of ids to a comma separated list
      strDocumentId = strDocumentId.replaceAll("\\(|\\)|'", "");
      System.out.println("postcontroller 3 strDocumentId:" + strDocumentId);
      if (strDocumentId.length() == 0)
        throw new ServletException(Utility.messageBD(this, "NoDocument", vars.getLanguage()));
      documentIds = strDocumentId.split(",");
      if (log4j.isDebugEnabled())
        log4j.debug("Number of documents selected: " + documentIds.length);

      String tabId = vars.getSessionValue("inpTabId");
      System.out.println("PRINTCONTROLLER2 TABID:" + vars.getSessionValue("inpTabId"));
      // System.out.println("tabId: " + tabId);
      // Physical Document Number
      boolean isInvoiceWithPhyDocNo = isInvoiceWithPhyDocNo_ToCheck(tabId);
      boolean isInOutWithPhyDocNo = isInOutWithPhyDocNo_ToCheck(tabId);
      boolean isPickingList = isPickingList_ToCheck(tabId);
      System.out.println("postcontroller 4:" + vars.getCommand());
      if (vars.commandIn("PRINTDIRECT") || vars.commandIn("ARCHIVE") || vars.commandIn("PRINT")) {
        // Physical Document Number
        // System.out.println("PRIMERO: ");
        if (isInvoiceWithPhyDocNo || isInOutWithPhyDocNo) {
          System.out.println("postcontroller 5: estab invoice/shpment");
          newPhyDocNo = vars.getStringParameter("inpPhysicalDocNo");
          myMessage = validatePhyDocNo(vars, tabId, documentIds[0], newPhyDocNo);
          if (myMessage.getType() == "Error") {
            tabId = vars.getSessionValue("inpTabId");
            vars.getStringParameter("tab");
            vars.setMessage(tabId, myMessage);
            vars.getRequestGlobalVariable("inpTabId", "AttributeSetInstance.tabId");
            printPageClosePopUpAndRefreshParent(response, vars);
            throw new Exception(myMessage.getMessage());
          }
        }

        if (isPickingList) {// Valdiation For Picking
          newAdUserPickerId = vars.getStringParameter("inpPickerId");
          myMessage = validateAdUSerPickier(vars, tabId, strDocumentId, newAdUserPickerId);
          if (myMessage.getType() == "Error") {
            tabId = vars.getSessionValue("inpTabId");
            vars.getStringParameter("tab");
            vars.setMessage(tabId, myMessage);
            vars.getRequestGlobalVariable("inpTabId", "AttributeSetInstance.tabId");
            printPageClosePopUpAndRefreshParent(response, vars);
            throw new Exception(myMessage.getMessage());
          }
        }
      }

      if (vars.commandIn("PRINTDIRECT") || vars.commandIn("ARCHIVE") || vars.commandIn("PRINT")) {
        if (isPickingList) {// Valdiation For Picking
          newAdUserPickerId = vars.getStringParameter("inpPickerId");
          myMessage = validateAdUSerPickier(vars, tabId, strDocumentId, newAdUserPickerId);
          if (myMessage.getType() == "Error") {
            tabId = vars.getSessionValue("inpTabId");
            vars.getStringParameter("tab");
            vars.setMessage(tabId, myMessage);
            vars.getRequestGlobalVariable("inpTabId", "AttributeSetInstance.tabId");
            printPageClosePopUpAndRefreshParent(response, vars);
            throw new Exception(myMessage.getMessage());
          }
        }

      }

      multiReports = (documentIds.length > 1);
      reports = (Map<String, Report>) vars.getSessionObject(sessionValuePrefix + ".Documents");
      System.out
          .println("postcontroller 6:" + vars.getSessionObject(sessionValuePrefix + ".Documents"));
      final ReportManager reportManager = new ReportManager(this, globalParameters.strFTPDirectory,
          strReplaceWithFull, globalParameters.strBaseDesignPath,
          globalParameters.strDefaultDesignPath, globalParameters.prefix, multiReports);
      if (vars.commandIn("PRINTDIRECT")) {
        System.out.println("PRINTCONTROLLER3 TABID:" + vars.getSessionValue("inpTabId"));
        // System.out.println("segundo: ");
        System.out.println("postcontroller 7:" + "PRINTDIRECT");
        archivedReports = false;
        // Order documents by Document No.
        if (multiReports)
          documentIds = orderByDocumentNo(documentType, documentIds);
        /*
         * PRINT option will print directly to the UI for a single report. For multiple reports the
         * documents will each be saved individually and the concatenated in the same manner as the
         * saved reports. After concatenating the reports they will be deleted.
         */
        System.out.println("postcontroller 8:" + documentIds);
        Report report = null;
        JasperPrint jasperPrint = null;
        Collection<JasperPrint> jrPrintReports = new ArrayList<JasperPrint>();
        final Collection<Report> savedReports = new ArrayList<Report>();
        for (int i = 0; i < documentIds.length; i++) {
          String documentId = documentIds[i];

          // Physical Document Number
          updateDatePrinted(this, documentId, documentType);
          if (isInvoiceWithPhyDocNo || isInOutWithPhyDocNo) {
            if (isInvoiceWithPhyDocNo)
              PrintControllerData.updateInvoice_PhysicalDocNo(this, newPhyDocNo, documentId);
            if (isInOutWithPhyDocNo)
              PrintControllerData.updateInOut_PhysicalDocNo(this, newPhyDocNo, documentId);
            updateNextPhyDocSequence(vars.getSessionValue("scr_userwhs_docserie_id_selected"));
          }

          if (isPickingList) {
            PrintControllerData.updatePicking_UserPicker(this, newAdUserPickerId, documentId);

            // Al imprimir el Picking se completa todo.
            /*
             * String json = "{ \"pickings\": [" + documentId + "]}"; JSONObject jsnobject = new
             * JSONObject(json); JSONArray jsonArray = jsnobject.getJSONArray("pickings");
             * PickingListActionHandler picking = new PickingListActionHandler(); JSONObject
             * jsonResponse = picking.doProcess(jsonArray);
             * 
             * if (!jsonResponse.getJSONObject("message").get("severity").equals("success")) throw
             * new Exception("Error al completar el picking");
             */

            // PickingListActionHandler
          }
          System.out.println("postcontroller 9:" + "acaa buildreport");
          report = buildReport(response, vars, documentId, reportManager, documentType,
              Report.OutputTypeEnum.PRINT);
          try {
            jasperPrint = reportManager.processReport(report, vars);
            jrPrintReports.add(jasperPrint);
          } catch (final ReportingException e) {
            advisePopUp(request, response, "Report processing failed",
                "Unable to process report selection");
            log4j.error(e.getMessage());
            e.getStackTrace();
          }
          savedReports.add(report);
          if (multiReports) {
            reportManager.saveTempReport(report, vars);
          }
        }
        System.out.println("postcontroller 10" + "printreports");
        printReports(response, jrPrintReports, savedReports, true);
        System.out.println("postcontroller 11" + "docserie");
        vars.removeSessionValue("scr_userwhs_docserie_id_selected");
        System.out.println("postcontroller 12" + "fin");
      } else if (vars.commandIn("PRINT")) {
        System.out.println("PRINTCONTROLLER4 TABID:" + vars.getSessionValue("inpTabId"));
        // System.out.println("tercero: ");
        System.out.println("postcontroller 13" + "PRINT");
        archivedReports = false;
        // Order documents by Document No.
        if (multiReports)
          documentIds = orderByDocumentNo(documentType, documentIds);
        /*
         * PRINT option will print directly to the UI for a single report. For multiple reports the
         * documents will each be saved individually and the concatenated in the same manner as the
         * saved reports. After concatenating the reports they will be deleted.
         */
        System.out.println("postcontroller 14" + documentIds);
        Report report = null;
        JasperPrint jasperPrint = null;
        Collection<JasperPrint> jrPrintReports = new ArrayList<JasperPrint>();
        final Collection<Report> savedReports = new ArrayList<Report>();

        // System.out.println("TERCERO");
        // System.out.println("TERCERO O1");
        for (int i = 0; i < documentIds.length; i++) {
          String documentId = documentIds[i];
          // System.out.println("TERCERO O2");
          // Physical Document Number
          updateDatePrinted(this, documentId, documentType);
          if (isInvoiceWithPhyDocNo || isInOutWithPhyDocNo) {
            if (isInvoiceWithPhyDocNo)
              PrintControllerData.updateInvoice_PhysicalDocNo(this, newPhyDocNo, documentId);
            if (isInOutWithPhyDocNo)
              PrintControllerData.updateInOut_PhysicalDocNo(this, newPhyDocNo, documentId);
            updateNextPhyDocSequence(vars.getSessionValue("scr_userwhs_docserie_id_selected"));
          }

          // System.out.println("TERCERO O3");
          if (isPickingList) {
            // System.out.println("TERCERO yes: " +
            // newAdUserPickerId + " - " + documentId);
            PrintControllerData.updatePicking_UserPicker(this, newAdUserPickerId, documentId);
          }
          // System.out.println("TERCERO O4");
          System.out.println("postcontroller 15" + "buildreport");
          report = buildReport(response, vars, documentId, reportManager, documentType,
              Report.OutputTypeEnum.PRINT);
          try {
            jasperPrint = reportManager.processReport(report, vars);
            jrPrintReports.add(jasperPrint);
          } catch (final ReportingException e) {
            advisePopUp(request, response, "Report processing failed",
                "Unable to process report selection");
            log4j.error(e.getMessage());
            e.getStackTrace();
          }
          savedReports.add(report);
          if (multiReports) {
            reportManager.saveTempReport(report, vars);
          }
        }
        System.out.println("postcontroller 16" + "printreports");
        printReports(response, jrPrintReports, savedReports, false);
        System.out.println("postcontroller 17" + "docserie");
        vars.removeSessionValue("scr_userwhs_docserie_id_selected");
        System.out.println("postcontroller 18" + "fin");
      } else if (vars.commandIn("ARCHIVE")) {
        System.out.println("PRINTCONTROLLER5 TABID:" + vars.getSessionValue("inpTabId"));
        // Order documents by Document No.
        System.out.println("postcontroller 19" + "ARCHIVE");
        if (multiReports)
          documentIds = orderByDocumentNo(documentType, documentIds);

        // System.out.println("cuarto: ");

        /*
         * ARCHIVE will save each report individually and then print the reports in a single
         * printable (concatenated) format.
         */
        System.out.println("postcontroller 20" + documentIds);
        archivedReports = true;
        Report report = null;
        final Collection<Report> savedReports = new ArrayList<Report>();
        for (int index = 0; index < documentIds.length; index++) {
          String documentId = documentIds[index];

          // Physical Document Number
          updateDatePrinted(this, documentId, documentType);
          if (isInvoiceWithPhyDocNo || isInOutWithPhyDocNo) {
            if (isInvoiceWithPhyDocNo)
              PrintControllerData.updateInvoice_PhysicalDocNo(this, newPhyDocNo, documentId);
            if (isInOutWithPhyDocNo)
              PrintControllerData.updateInOut_PhysicalDocNo(this, newPhyDocNo, documentId);
            updateNextPhyDocSequence(vars.getSessionValue("scr_userwhs_docserie_id_selected"));
          }

          if (isPickingList) {
            PrintControllerData.updatePicking_UserPicker(this, newAdUserPickerId, documentId);

            // Al imprimir el Picking se completa todo.
            /*
             * String json = "{ \"pickings\": [" + documentId + "]}"; JSONObject jsnobject = new
             * JSONObject(json); JSONArray jsonArray = jsnobject.getJSONArray("pickings");
             * PickingListActionHandler picking = new PickingListActionHandler(); JSONObject
             * jsonResponse = picking.doProcess(jsonArray);
             * 
             * if (!jsonResponse.getJSONObject("message").get("severity").equals("success")) throw
             * new Exception("Error al completar el picking");
             */

          }
          System.out.println("postcontroller 21" + "buildreport");
          report = buildReport(response, vars, documentId, reportManager, documentType,
              OutputTypeEnum.ARCHIVE);
          buildReport(response, vars, documentId, reports, reportManager);
          try {
            reportManager.processReport(report, vars);
          } catch (final ReportingException e) {
            log4j.error(e);
          }
          reportManager.saveTempReport(report, vars);
          savedReports.add(report);
        }
        System.out.println("postcontroller 22" + "printreports");
        printReports(response, null, savedReports, false);
        System.out.println("postcontroller 23" + "docserie");
        vars.removeSessionValue("scr_userwhs_docserie_id_selected");
        System.out.println("postcontroller 24" + "fin");
      } else {
        System.out.println("PRINTCONTROLLER6 INICIO TABID:" + vars.getSessionValue("inpTabId"));
        System.out.println("postcontroller 25" + "INICIO");
        if (vars.commandIn("DEFAULT")) {
          // System.out.println("quinto: ");
          System.out.println("postcontroller 26" + "DEFAULT");
          differentDocTypes.clear();
          reports = new HashMap<String, Report>();

          for (int index = 0; index < documentIds.length; index++) {
            final String documentId = documentIds[index];
            System.out.println("postcontroller 27" + documentId);
            if (log4j.isDebugEnabled())
              log4j.debug("Processing document with id: " + documentId);
            try {
              System.out.println("postcontroller 28" + "Report");
              final Report report = new Report(this, documentType, documentId, vars.getLanguage(),
                  "default", multiReports, OutputTypeEnum.DEFAULT);
              reports.put(documentId, report);
              final String senderAddress = EmailData.getSenderAddress(this, vars.getClient(),
                  report.getOrgId());
              boolean moreThanOnesalesRep = checks.get("moreThanOnesalesRep").booleanValue();
              System.out.println(
                  "postcontroller 29" + "request:" + request.getServletPath().toLowerCase());
              if (request.getServletPath().toLowerCase()
                  .indexOf("print" + printfile_suffix + ".html") == -1) {
                System.out.println(
                    "postcontroller 30" + "no index:" + "print" + printfile_suffix + ".html");
                if ("".equals(senderAddress) || senderAddress == null) {
                  final OBError on = new OBError();
                  on.setMessage(Utility.messageBD(this, "NoSender", vars.getLanguage()));
                  on.setTitle(Utility.messageBD(this, "EmailConfigError", vars.getLanguage()));
                  on.setType("Error");
                  // tabId = vars.getSessionValue("inpTabId");
                  vars.getStringParameter("tab");
                  vars.setMessage(tabId, on);
                  vars.getRequestGlobalVariable("inpTabId", "AttributeSetInstance.tabId");
                  printPageClosePopUpAndRefreshParent(response, vars);
                  throw new ServletException("Configuration Error no sender defined");
                }
              }

              // check the different doc typeId's if all the
              // selected
              // doc's
              // has the same doc typeId the template selector
              // should
              // appear
              if (!differentDocTypes.containsKey(report.getDocTypeId())) {
                differentDocTypes.put(report.getDocTypeId(), report.getTemplate());
              }
            } catch (ServletException exception) {
              System.out.println("postcontroller 31" + exception.getMessage());
              throw new ServletException(
                  Utility.messageBD(this, exception.getMessage(), vars.getLanguage()));
            } catch (final ReportingException exception) {
              throw new ServletException(exception);
            }
          }
          System.out.println("postcontroller 32" + "sessionvalueprefix:" + sessionValuePrefix);
          System.out.println("PRINTCONTROLLER6 TABID:" + vars.getSessionValue("inpTabId"));
          vars.setSessionObject(sessionValuePrefix + ".Documents", reports);
          if (request.getServletPath().toLowerCase()
              .indexOf("print" + printfile_suffix + ".html") != -1) {
            System.out.println("postcontroller 33" + "createprintoptionspage");
            createPrintOptionsPage(request, response, vars, documentType,
                getComaSeparatedString(documentIds), reports, printxmlfile_path);
          } else {
            System.out.println("postcontroller 34" + "createemailoptionspage");
            createEmailOptionsPage(request, response, vars, documentType,
                getComaSeparatedString(documentIds), reports, checks, fullDocumentIdentifier);
          }

        } else if (vars.commandIn("ADD")) {
          if (request.getServletPath().toLowerCase()
              .indexOf("print" + printfile_suffix + ".html") != -1)
            createPrintOptionsPage(request, response, vars, documentType,
                getComaSeparatedString(documentIds), reports, printxmlfile_path);
          else {
            final boolean showList = true;
            createEmailOptionsPage(request, response, vars, documentType,
                getComaSeparatedString(documentIds), reports, checks, fullDocumentIdentifier);
          }

        } else if (vars.commandIn("DEL")) {
          final String documentToDelete = vars.getStringParameter("idToDelete");
          final Vector<Object> vector = (Vector<Object>) request.getSession().getAttribute("files");
          request.getSession().setAttribute("files", vector);

          seekAndDestroy(vector, documentToDelete);
          createEmailOptionsPage(request, response, vars, documentType,
              getComaSeparatedString(documentIds), reports, checks, fullDocumentIdentifier);

        } else if (vars.commandIn("EMAIL")) {
          PocData[] pocData = (PocData[]) vars.getSessionObject("pocData" + fullDocumentIdentifier);
          int nrOfEmailsSend = 0;
          for (final PocData documentData : pocData) {
            getEnvironentInformation(pocData, checks);
            final String documentId = documentData.documentId;
            if (log4j.isDebugEnabled())
              log4j.debug("Processing document with id: " + documentId);

            String templateInUse = "default";
            if (differentDocTypes.size() == 1) {
              templateInUse = vars.getRequestGlobalVariable("templates", "templates");
            }

            final Report report = buildReport(response, vars, documentId, reportManager,
                documentType, OutputTypeEnum.EMAIL, templateInUse);

            // if there is only one document type id the user should
            // be
            // able to choose between different templates
            if (differentDocTypes.size() == 1) {
              final String templateId = vars.getRequestGlobalVariable("templates", "templates");
              try {
                final TemplateInfo usedTemplateInfo = new TemplateInfo(this, report.getDocTypeId(),
                    report.getOrgId(), vars.getLanguage(), templateId, "N");
                report.setTemplateInfo(usedTemplateInfo);
              } catch (final ReportingException e) {
                throw new ServletException("Error trying to get template information", e);
              }
            }

            if (report == null)
              throw new ServletException(
                  Utility.messageBD(this, "NoDataReport", vars.getLanguage()) + documentId);
            // Check if the document is not in status 'draft'
            if (!report.isDraft()) {
              // Check if the report is already attached
              if (!report.isAttached()) {
                // get the Id of the entities table, this is
                // used to
                // store the file as an OB attachment
                final String tableId = ToolsData.getTableId(this,
                    report.getDocumentType().getTableName());

                // If the user wants to archive the document
                if (vars.getStringParameter("inpArchive").equals("Y")) {
                  // Save the report as a attachment because
                  // it is
                  // being transferred to the user
                  try {
                    reportManager.createAttachmentForReport(this, report, tableId, vars);
                  } catch (final ReportingException exception) {
                    throw new ServletException(exception);
                  }
                } else {
                  reportManager.saveTempReport(report, vars);
                }
              } else {
                if (log4j.isDebugEnabled())
                  log4j.debug("Document is not attached.");
              }
              final String senderAddress = vars.getStringParameter("fromEmail");
              sendDocumentEmail(report, vars,
                  (Vector<Object>) request.getSession().getAttribute("files"), documentData,
                  senderAddress, checks, documentType);
              nrOfEmailsSend++;
            }
          }
          request.getSession().removeAttribute("files");
          vars.removeSessionValue("pocData" + fullDocumentIdentifier);
          createPrintStatusPage(response, vars, nrOfEmailsSend);
        } else if (vars.commandIn("UPDATE_TEMPLATE")) {
          JSONObject o = new JSONObject();
          try {
            PocData[] pocData = (PocData[]) vars
                .getSessionObject("pocData" + fullDocumentIdentifier);
            final String templateId = vars.getRequestGlobalVariable("templates", "templates");
            final String documentId = pocData[0].documentId;
            for (final PocData documentData : pocData) {
              final Report report = new Report(this, documentType, documentId, vars.getLanguage(),
                  templateId, multiReports, OutputTypeEnum.DEFAULT);
              o.put("templateId", templateId);
              o.put("subject", report.getEmailDefinition().getSubject());
              o.put("body", report.getEmailDefinition().getBody());
              if (!multiReports) {
                o.put("filename", report.getFilename());
              }
              reports = new HashMap<String, Report>();
              reports.put(documentId, report);
            }
            vars.setSessionObject(sessionValuePrefix + ".Documents", reports);

          } catch (Exception e) {
            log4j.error("Error in change template ajax", e);
            o = new JSONObject();
            try {
              o.put("error", true);
            } catch (JSONException e1) {
              log4j.error("Error in change template ajax", e1);
            }
          }

          response.setContentType("application/json");
          final PrintWriter out = response.getWriter();
          out.println(o.toString());
          out.close();
        } else if (vars.commandIn("UPDATE_EMAILCONFIG")) {
          JSONObject o = new JSONObject();
          try {
            String currentEmailConfigId = vars.getStringParameter("emailConfigList");
            EmailTemplate emailTemplate = OBDal.getInstance().get(EmailTemplate.class,
                currentEmailConfigId);
            o.put("subject", emailTemplate.getSubject());
            o.put("body", emailTemplate.getBody());

          } catch (Exception e) {
            log4j.error("Error in change template ajax", e);
            o = new JSONObject();
            try {
              o.put("error", true);
            } catch (JSONException e1) {
              log4j.error("Error in change template ajax", e1);
            }
          }

          response.setContentType("application/json");
          final PrintWriter out = response.getWriter();
          out.println(o.toString());
          out.close();
        }

        pageError(response);
      }
    } catch (Exception e) {
      System.out.println("postcontroller 35" + e.getMessage());
      // Catching the exception here instead of throwing it to HSAS
      // because this is used in multi
      // part request making the mechanism to detect popup not to work.
      log4j.error("Error captured: ", e);
      bdErrorGeneralPopUp(request, response, "Error",
          Utility.translateError(this, vars, vars.getLanguage(), e.getMessage()).getMessage());
    }
  }

  public void printReports(HttpServletResponse response, Collection<JasperPrint> jrPrintReports,
      Collection<Report> reports, boolean isDirect) {
    ServletOutputStream os = null;
    String filename = "";
    try {
      os = response.getOutputStream();
      response.setContentType("application/pdf");

      if (!multiReports && !archivedReports) {
        for (Iterator<Report> iterator = reports.iterator(); iterator.hasNext();) {
          Report report = iterator.next();
          filename = report.getFilename();
        }
        if (isDirect) {
          response.setHeader("Content-disposition", "inline" + "; filename=" + filename);
        } else
          response.setHeader("Content-disposition", "attachment" + "; filename=" + filename);
        for (Iterator<JasperPrint> iterator = jrPrintReports.iterator(); iterator.hasNext();) {
          JasperPrint jasperPrint = (JasperPrint) iterator.next();
          JasperExportManager.exportReportToPdfStream(jasperPrint, os);
        }
      } else {
        response.setContentType("application/pdf");
        concatReport(reports.toArray(new Report[] {}), response, isDirect);
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
  private void concatReport(Report[] reports, HttpServletResponse response, boolean isDirect) {
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
        if (isDirect)
          response.setHeader("Content-disposition", "inline" + "; filename=" + filename);
        else
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

  public Report buildReport(HttpServletResponse response, VariablesSecureApp vars,
      String strDocumentId, final ReportManager reportManager, DocumentType documentType,
      OutputTypeEnum outputType) {
    return buildReport(response, vars, strDocumentId, reportManager, documentType, outputType,
        "default");
  }

  public Report buildReport(HttpServletResponse response, VariablesSecureApp vars,
      String strDocumentId, final ReportManager reportManager, DocumentType documentType,
      OutputTypeEnum outputType, String templateId) {
    Report report = null;
    if (strDocumentId != null) {
      strDocumentId = strDocumentId.replaceAll("\\(|\\)|'", "");
    }
    try {
      report = new Report(this, documentType, strDocumentId, vars.getLanguage(), templateId,
          multiReports, outputType);
    } catch (final ReportingException e) {
      log4j.error(e);
    } catch (final ServletException e) {
      log4j.error(e);
    }

    reportManager.setTargetDirectory(report);
    return report;
  }

  public void buildReport(HttpServletResponse response, VariablesSecureApp vars,
      String strDocumentId, Map<String, Report> reports, final ReportManager reportManager)
      throws ServletException, IOException {
    final String documentId = vars.getStringParameter("inpDocumentId");
    if (strDocumentId != null)
      strDocumentId = strDocumentId.replaceAll("\\(|\\)|'", "");
    final Report report = reports.get(strDocumentId);
    if (report == null)
      throw new ServletException(
          Utility.messageBD(this, "NoDataReport", vars.getLanguage()) + documentId);
    // Check if the document is not in status 'draft'
    if (!report.isDraft() && !report.isAttached() && vars.commandIn("ARCHIVE")) {
      // TODO: Move the table Id retrieval into the DocumentType
      // getTableId method!
      // get the Id of the entities table, this is used to store the
      // file as an OB attachment
      final String tableId = ToolsData.getTableId(this, report.getDocumentType().getTableName());

      if (log4j.isDebugEnabled())
        log4j.debug(
            "Table " + report.getDocumentType().getTableName() + " has table id: " + tableId);
      // Save the report as a attachment because it is being
      // transferred to the user
      try {
        reportManager.createAttachmentForReport(this, report, tableId, vars);
      } catch (final ReportingException exception) {
        throw new ServletException(exception);
      }
    } else {
      if (log4j.isDebugEnabled())
        log4j.debug("Document is not attached.");
    }
  }

  /**
   * 
   * @param vector
   * @param documentToDelete
   */
  private void seekAndDestroy(Vector<Object> vector, String documentToDelete) {
    for (int i = 0; i < vector.size(); i++) {
      final AttachContent content = (AttachContent) vector.get(i);
      if (content.id.equals(documentToDelete)) {
        vector.remove(i);
        break;
      }
    }

  }

  PocData[] getContactDetails(DocumentType documentType, String strDocumentId)
      throws ServletException {
    switch (documentType) {
    case QUOTATION:
      return PocData.getContactDetailsForOrders(this, strDocumentId);
    case SALESORDER:
      return PocData.getContactDetailsForOrders(this, strDocumentId);
    case SALESINVOICE:
      return PocData.getContactDetailsForInvoices(this, strDocumentId);
    case SHIPMENT:
      return PocData.getContactDetailsForShipments(this, strDocumentId);
    case PURCHASEORDER:
      return PocData.getContactDetailsForOrders(this, strDocumentId);
    case PAYMENT:
      return PocData.getContactDetailsForPayments(this, strDocumentId);
    case SALESINVOICEORDER:
      return PocData.getContactDetailsForInvoices(this, strDocumentId);
    case SHIPMENTORDER:
      return PocData.getContactDetailsForShipments(this, strDocumentId);
    }
    return null;
  }

  void sendDocumentEmail(Report report, VariablesSecureApp vars, Vector<Object> object,
      PocData documentData, String senderAddress, HashMap<String, Boolean> checks,
      DocumentType documentType) throws IOException, ServletException {
    final String attachmentFileLocation = report.getTargetLocation();
    String emailSubject = null, emailBody = null;
    final String ourReference = report.getOurReference();
    final String cusReference = report.getCusReference();
    if (log4j.isDebugEnabled())
      log4j.debug("our document ref: " + ourReference);
    if (log4j.isDebugEnabled())
      log4j.debug("cus document ref: " + cusReference);

    final String toName = documentData.contactName;
    String toEmail = null;
    final String replyToName = documentData.salesrepName;
    String replyToEmail = null;

    boolean moreThanOneCustomer = checks.get("moreThanOneCustomer").booleanValue();
    boolean moreThanOnesalesRep = checks.get("moreThanOnesalesRep").booleanValue();
    if (moreThanOneCustomer) {
      toEmail = documentData.contactEmail;
    } else {
      toEmail = vars.getStringParameter("toEmail");
    }

    if (moreThanOnesalesRep) {
      replyToEmail = documentData.salesrepEmail;
    } else {
      replyToEmail = vars.getStringParameter("replyToEmail");
    }
    if (differentDocTypes.size() > 1) {
      try {
        EmailDefinition emailDefinition = report.getDefaultEmailDefinition();
        emailSubject = emailDefinition.getSubject();
        emailBody = emailDefinition.getBody();
      } catch (ReportingException e) {
        log4j.error(e.getMessage(), e);
      }
    } else {
      emailSubject = vars.getStringParameter("emailSubject");
      emailBody = vars.getStringParameter("emailBody");
    }

    // TODO: Move this to the beginning of the print handling and do nothing
    // if these conditions fail!!!)

    if ((replyToEmail == null || replyToEmail.length() == 0)) {
      throw new ServletException(Utility.messageBD(this, "NoSalesRepEmail", vars.getLanguage()));
    }

    if ((toEmail == null || toEmail.length() == 0)) {
      throw new ServletException(Utility.messageBD(this, "NoCustomerEmail", vars.getLanguage()));
    }

    // Replace special tags

    emailSubject = emailSubject.replaceAll("@cus_ref@", Matcher.quoteReplacement(cusReference));
    emailSubject = emailSubject.replaceAll("@our_ref@", Matcher.quoteReplacement(ourReference));
    emailSubject = emailSubject.replaceAll("@cus_nam@", Matcher.quoteReplacement(toName));
    emailSubject = emailSubject.replaceAll("@sal_nam@", Matcher.quoteReplacement(replyToName));
    emailSubject = emailSubject.replaceAll("@bp_nam@",
        Matcher.quoteReplacement(report.getBPName()));
    emailSubject = emailSubject.replaceAll("@doc_date@",
        Matcher.quoteReplacement(report.getDocDate()));
    emailSubject = emailSubject.replaceAll("@doc_nextduedate@",
        Matcher.quoteReplacement(report.getMinDueDate()));
    emailSubject = emailSubject.replaceAll("@doc_lastduedate@",
        Matcher.quoteReplacement(report.getMaxDueDate()));
    emailSubject = emailSubject.replaceAll("@doc_desc@",
        Matcher.quoteReplacement(report.getDocDescription()));

    emailBody = emailBody.replaceAll("@cus_ref@", Matcher.quoteReplacement(cusReference));
    emailBody = emailBody.replaceAll("@our_ref@", Matcher.quoteReplacement(ourReference));
    emailBody = emailBody.replaceAll("@cus_nam@", Matcher.quoteReplacement(toName));
    emailBody = emailBody.replaceAll("@sal_nam@", Matcher.quoteReplacement(replyToName));
    emailBody = emailBody.replaceAll("@bp_nam@", Matcher.quoteReplacement(report.getBPName()));
    emailBody = emailBody.replaceAll("@doc_date@", Matcher.quoteReplacement(report.getDocDate()));
    emailBody = emailBody.replaceAll("@doc_nextduedate@",
        Matcher.quoteReplacement(report.getMinDueDate()));
    emailBody = emailBody.replaceAll("@doc_lastduedate@",
        Matcher.quoteReplacement(report.getMaxDueDate()));
    emailBody = emailBody.replaceAll("@doc_desc@",
        Matcher.quoteReplacement(report.getDocDescription()));

    String host = null;
    boolean auth = true;
    String username = null;
    String password = null;
    String connSecurity = null;
    int port = 25;

    OBContext.setAdminMode(true);
    try {
      final EmailServerConfiguration mailConfig = OBDal.getInstance()
          .get(EmailServerConfiguration.class, vars.getStringParameter("fromEmailId"));

      host = mailConfig.getSmtpServer();

      if (!mailConfig.isSMTPAuthentification()) {
        auth = false;
      }
      username = mailConfig.getSmtpServerAccount();
      password = FormatUtilities.encryptDecrypt(mailConfig.getSmtpServerPassword(), false);
      connSecurity = mailConfig.getSmtpConnectionSecurity();
      port = mailConfig.getSmtpPort().intValue();
    } finally {
      OBContext.restorePreviousMode();
    }

    final String recipientTO = toEmail;
    final String recipientCC = vars.getStringParameter("ccEmail");
    final String recipientBCC = vars.getStringParameter("bccEmail");
    final String replyTo = replyToEmail;
    final String contentType = "text/plain; charset=utf-8";

    if (log4j.isDebugEnabled()) {
      log4j.debug("From: " + senderAddress);
      log4j.debug("Recipient TO (contact email): " + recipientTO);
      log4j.debug("Recipient CC: " + recipientCC);
      log4j.debug("Recipient BCC (user email): " + recipientBCC);
      log4j.debug("Reply-to (sales rep email): " + replyTo);
    }

    List<File> attachments = new ArrayList<File>();
    attachments.add(new File(attachmentFileLocation));

    if (object != null) {
      final Vector<Object> vector = (Vector<Object>) object;
      for (int i = 0; i < vector.size(); i++) {
        final AttachContent objContent = (AttachContent) vector.get(i);
        final File file = prepareFile(objContent, ourReference);
        attachments.add(file);
      }
    }

    try {
      EmailManager.sendEmail(host, auth, username, password, connSecurity, port, senderAddress,
          recipientTO, recipientCC, recipientBCC, replyTo, emailSubject, emailBody, contentType,
          attachments, null, null);
    } catch (Exception exception) {
      log4j.error(exception);
      final String exceptionClass = exception.getClass().toString().replace("class ", "");
      String exceptionString = "Problems while sending the email" + exception;
      exceptionString = exceptionString.replace(exceptionClass, "");
      throw new ServletException(exceptionString);
    }

    // Store the email in the database
    Connection conn = null;
    try {
      conn = this.getTransactionConnection();

      // First store the email message
      final String newEmailId = SequenceIdData.getUUID();
      if (log4j.isDebugEnabled())
        log4j.debug("New email id: " + newEmailId);

      EmailData.insertEmail(conn, this, newEmailId, vars.getClient(), report.getOrgId(),
          vars.getUser(), EmailType.OUTGOING.getStringValue(), replyTo, recipientTO, recipientCC,
          recipientBCC, Utility.formatDate(new Date(), "yyyyMMddHHmmss"), emailSubject, emailBody,
          report.getBPartnerId(),
          ToolsData.getTableId(this, report.getDocumentType().getTableName()),
          documentData.documentId);

      releaseCommitConnection(conn);
    } catch (final NoConnectionAvailableException exception) {
      log4j.error(exception);
      throw new ServletException(exception);
    } catch (final SQLException exception) {
      log4j.error(exception);
      try {
        releaseRollbackConnection(conn);
      } catch (final Exception ignored) {
      }

      throw new ServletException(exception);
    }

  }

  void createPrintOptionsPage(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, DocumentType documentType, String strDocumentId,
      Map<String, Report> reports) throws IOException, ServletException {
    createPrintOptionsPage(request, response, vars, documentType, strDocumentId, reports,
        "org/openbravo/erpCommon/utility/reporting/printing/PrintOptions");
  }

  void createPrintOptionsPage(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, DocumentType documentType, String strDocumentId,
      Map<String, Report> reports, String printxmlfile_path) throws IOException, ServletException {
    System.out.println("createPrintOptionsPage1 TABID:" + vars.getSessionValue("inpTabId"));
    System.out.println("printoptionspage 1:" + strDocumentId);
    XmlDocument xmlDocument = null;
    xmlDocument = xmlEngine.readXmlTemplate(printxmlfile_path).createXmlDocument();
    xmlDocument.setParameter("strDocumentId", strDocumentId);
    System.out.println("createPrintOptionsPage2 TABID:" + vars.getSessionValue("inpTabId"));
    Client client = OBDal.getInstance().get(Client.class, vars.getSessionValue("#AD_CLIENT_ID"));
    User user = OBDal.getInstance().get(User.class, vars.getSessionValue("#AD_USER_ID"));

    // Get additional document information
    final String draftDocumentIds = "";
    String strIsDirectPDF = vars.getStringParameter("isDirectPDF");
    System.out.println("printoptionspage 2:" + "isdirectpdf:" + strIsDirectPDF);
    if (strIsDirectPDF == null || "".equals(strIsDirectPDF)) {
      System.out.println("printoptionspage 3:" + "isdirectpdf false");
      strIsDirectPDF = "false";
    }
    String strIsDirectAttach = vars.getStringParameter("isDirectAttach");
    System.out.println("printoptionspage 4:" + "isdirectattach" + strIsDirectAttach);
    if (strIsDirectAttach == null || "".equals(strIsDirectAttach)) {
      System.out.println("printoptionspage 5:" + "isdirectatach false");
      strIsDirectAttach = "false";
    }
    String strTabId = vars.getSessionValue("inpTabId");
    System.out.println("printoptionspage 6:" + "tabid:" + strTabId);
    if (strTabId == null || "".equals(strTabId)) {
      System.out.println("printoptionspage 7:" + "strtabid vacio");
      strTabId = "";
    }

    strDocumentId = strDocumentId.replaceAll("\\(|\\)|'", "");

    String strPhyDocNo = "";
    String strSpecialDocType = "";
    String strIsebill = "N";
    boolean isPhyDocNoSetted = false;
    BILLPhyDocSequence phyDocSeq = null;

    // invoices
    if (isInvoiceWithPhyDocNo_ToCheck(strTabId)) {
      System.out.println("printoptionspage 8:" + "es tab de invoice");
      Invoice inv = OBDal.getInstance().get(Invoice.class, strDocumentId);
      if (inv != null) {
        strSpecialDocType = inv.getSCOSpecialDocType();
        if (inv.isBillIsebill() != null && inv.isBillIsebill()) {
          strIsebill = "Y";
        }
        strPhyDocNo = inv.getScrPhysicalDocumentno();

        if (defPhyDocNo.equals(strPhyDocNo)) {
          if (strSpecialDocType.compareTo("SCOARDEBITMEMO") == 0
              || strSpecialDocType.compareTo("SCOARINVOICE") == 0
              || strSpecialDocType.compareTo("SCOARCREDITMEMO") == 0
              || strSpecialDocType.compareTo("SCOARTICKET") == 0
              || strSpecialDocType.compareTo("SCOARINVOICERETURNMAT") == 0) {
            OBCriteria<ScrUserwarehouseDocserie> usrwhs_serie = OBDal.getInstance()
                .createCriteria(ScrUserwarehouseDocserie.class);
            usrwhs_serie.add(Restrictions.eq(ScrUserwarehouseDocserie.PROPERTY_CLIENT, client));
            usrwhs_serie
                .add(Restrictions.eq(ScrUserwarehouseDocserie.PROPERTY_USER, inv.getCreatedBy()));
            if (strSpecialDocType.compareTo("SCOARINVOICE") == 0 && inv.getSalesOrder() != null) {
              usrwhs_serie.add(Restrictions.eq(ScrUserwarehouseDocserie.PROPERTY_WAREHOUSE, OBDal
                  .getInstance().get(Warehouse.class, inv.getSalesOrder().getWarehouse().getId())));
            }
            usrwhs_serie.add(Restrictions.sqlRestriction("AD_ISORGINCLUDED( '"
                + inv.getOrganization().getId() + "', orgwarehouse_id, ad_client_id ) > -1"));
            usrwhs_serie.addOrderBy(ScrUserwarehouseDocserie.PROPERTY_CREATIONDATE, false);
            List<ScrUserwarehouseDocserie> serie_array = usrwhs_serie.list();

            for (int i = 0; !isPhyDocNoSetted && i < serie_array.size(); i++) {
              phyDocSeq = serie_array.get(i).getBillPhyDocSequence();
              for (int j = 0; j < phyDocSeq.getBILLPhyDocTypeList().size(); j++) {
                BILLPhyDocType phyDocType = phyDocSeq.getBILLPhyDocTypeList().get(j);
                if (phyDocType.getDocumentType().getScoSpecialdoctype()
                    .compareTo(strSpecialDocType) == 0) {
                  isPhyDocNoSetted = true;
                  strPhyDocNo = getAutoPhyDocNo(phyDocSeq.getPrefix(),
                      phyDocSeq.getNextAssignedNumber());
                  break;
                }
              }
            }

          } else {
            OBCriteria<Invoice> invoices = OBDal.getInstance().createCriteria(Invoice.class);
            invoices.add(Restrictions.eq(Invoice.PROPERTY_CLIENT, client));
            invoices.add(Restrictions.eq(Invoice.PROPERTY_CREATEDBY, inv.getCreatedBy()));
            invoices.add(Restrictions.isNotNull(Invoice.PROPERTY_SCRPHYSICALDOCUMENTNO));
            invoices.add(Restrictions.ne(Invoice.PROPERTY_SCRPHYSICALDOCUMENTNO, defPhyDocNo));
            invoices.add(Restrictions.eq(Invoice.PROPERTY_SCOSPECIALDOCTYPE, strSpecialDocType));
            invoices.add(Restrictions.ne(Invoice.PROPERTY_ID, strDocumentId));
            invoices.add(Restrictions.sqlRestriction(" em_scr_physical_documentno ~ '[0-9]+$' "));
            invoices.addOrderBy(Invoice.PROPERTY_CREATIONDATE, false);
            if (invoices.list().size() > 0) {
              Invoice prev_invoice = invoices.list().get(0);
              strPhyDocNo = getAutoPhyDocNo(prev_invoice.getScrPhysicalDocumentno());
            }
          }

          if (!defPhyDocNo.equals(strPhyDocNo)) {
            vars.setSessionValue("strPrintedPhyDocNo", strPhyDocNo);
            vars.setSessionValue("strPrintedInvoiceId", inv.getId());
          }
        }
      }

      // goods shipment
    } else if (isInOutWithPhyDocNo_ToCheck(strTabId)) {
      System.out.println("printoptionspage 9:" + "es tab de guia");
      ShipmentInOut shipmt = OBDal.getInstance().get(ShipmentInOut.class, strDocumentId);
      if (shipmt != null) {
        strSpecialDocType = shipmt.getSCOSpecialDocType();
        strIsebill = "N";
        strPhyDocNo = shipmt.getSCRPhysicalDocumentNo();
        if (defPhyDocNo.equals(strPhyDocNo)) {
          if (strSpecialDocType.compareTo("SCOMMSHIPMENT") == 0
              || strSpecialDocType.compareTo("SWAINTERNALSHIPMENT") == 0
              || strSpecialDocType.compareTo("SWARTVRECEIPT") == 0) {
            OBCriteria<ScrUserwarehouseDocserie> usrwhs_serie = OBDal.getInstance()
                .createCriteria(ScrUserwarehouseDocserie.class);
            usrwhs_serie.add(Restrictions.eq(ScrUserwarehouseDocserie.PROPERTY_CLIENT, client));
            usrwhs_serie.add(
                Restrictions.eq(ScrUserwarehouseDocserie.PROPERTY_USER, shipmt.getCreatedBy()));
            usrwhs_serie.add(Restrictions.eq(ScrUserwarehouseDocserie.PROPERTY_WAREHOUSE,
                shipmt.getWarehouse()));
            usrwhs_serie.add(Restrictions.sqlRestriction("AD_ISORGINCLUDED( '"
                + shipmt.getOrganization().getId() + "', orgwarehouse_id, ad_client_id ) > -1"));
            usrwhs_serie.addOrderBy(ScrUserwarehouseDocserie.PROPERTY_CREATIONDATE, false);
            List<ScrUserwarehouseDocserie> serie_array = usrwhs_serie.list();

            for (int i = 0; !isPhyDocNoSetted && i < serie_array.size(); i++) {
              phyDocSeq = serie_array.get(i).getBillPhyDocSequence();
              if (phyDocSeq.isEbill()) {
                continue;
              }

              for (int j = 0; j < phyDocSeq.getBILLPhyDocTypeList().size(); j++) {
                BILLPhyDocType phyDocType = phyDocSeq.getBILLPhyDocTypeList().get(j);
                if (phyDocType.getDocumentType().getScoSpecialdoctype()
                    .compareTo(strSpecialDocType) == 0) {
                  isPhyDocNoSetted = true;
                  strPhyDocNo = getAutoPhyDocNo(phyDocSeq.getPrefix(),
                      phyDocSeq.getNextAssignedNumber());
                  break;
                }
              }
            }

          } else {
            OBCriteria<ShipmentInOut> shipmts = OBDal.getInstance()
                .createCriteria(ShipmentInOut.class);
            shipmts.add(Restrictions.eq(ShipmentInOut.PROPERTY_CLIENT, client));
            shipmts.add(Restrictions.eq(ShipmentInOut.PROPERTY_CREATEDBY, shipmt.getCreatedBy()));
            shipmts.add(Restrictions.isNotNull(ShipmentInOut.PROPERTY_SCRPHYSICALDOCUMENTNO));
            shipmts.add(Restrictions.ne(ShipmentInOut.PROPERTY_SCRPHYSICALDOCUMENTNO, defPhyDocNo));
            shipmts
                .add(Restrictions.eq(ShipmentInOut.PROPERTY_SCOSPECIALDOCTYPE, strSpecialDocType));
            shipmts.add(Restrictions.ne(ShipmentInOut.PROPERTY_ID, strDocumentId));
            shipmts.add(Restrictions.sqlRestriction(" em_scr_physical_documentno ~ '[0-9]+$' "));
            shipmts.addOrderBy(ShipmentInOut.PROPERTY_CREATIONDATE, false);
            if (shipmts.list().size() > 0) {
              ShipmentInOut prev_shipment = shipmts.list().get(0);
              strPhyDocNo = getAutoPhyDocNo(prev_shipment.getSCRPhysicalDocumentNo());
            }
          }
        }

      }

    }
    if (phyDocSeq != null) {
      vars.setSessionValue("scr_userwhs_docserie_id_selected", phyDocSeq.getId());
    }
    System.out.println("printoptionspage 10:" + "specialdoctype:" + strSpecialDocType);
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\r\n");
    xmlDocument.setParameter("language", vars.getLanguage());
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("description", "");
    xmlDocument.setParameter("help", "");
    xmlDocument.setParameter("isDirectPDF", "isDirectPDF = " + strIsDirectPDF + ";\r\n");
    xmlDocument.setParameter("isDirectAttach", "isDirectAttach = " + strIsDirectAttach + ";\r\n");
    xmlDocument.setParameter("strTabId", "strTabId = \"" + strTabId + "\";\r\n");
    xmlDocument.setParameter("strSpecialDocType",
        "strSpecialDocType = \"" + strSpecialDocType + "\";\r\n");
    xmlDocument.setParameter("strIsebill", "strIsebill = \"" + strIsebill + "\";\r\n");
    xmlDocument.setParameter("physicalDocNo", strPhyDocNo);

    String adOrgId = vars.getSessionValue("strTmpAdOrgId");
    xmlDocument.setParameter("strAdOrgTmpId", adOrgId);

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  void createEmailOptionsPage(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, DocumentType documentType, String strDocumentId,
      Map<String, Report> reports, HashMap<String, Boolean> checks) {
    createEmailOptionsPage(request, response, vars, documentType, strDocumentId, reports, checks);
  }

  void createEmailOptionsPage(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, DocumentType documentType, String strDocumentId,
      Map<String, Report> reports, HashMap<String, Boolean> checks, String fullDocumentIdentifier)
      throws IOException, ServletException, ReportingException {
    boolean hasMultipleEmailConfigurations = false;
    XmlDocument xmlDocument = null;
    PocData[] pocData = getContactDetails(documentType, strDocumentId);
    @SuppressWarnings("unchecked")
    Vector<java.lang.Object> vector = (Vector<java.lang.Object>) request.getSession()
        .getAttribute("files");

    final String[] hiddenTags = getHiddenTags(pocData, vector, vars, checks);
    if (hiddenTags != null) {
      xmlDocument = xmlEngine
          .readXmlTemplate("org/openbravo/erpCommon/utility/reporting/printing/EmailOptions",
              hiddenTags)
          .createXmlDocument();
    } else {
      xmlDocument = xmlEngine
          .readXmlTemplate("org/openbravo/erpCommon/utility/reporting/printing/EmailOptions")
          .createXmlDocument();
    }

    xmlDocument.setParameter("strDocumentId", strDocumentId);

    boolean isTheFirstEntry = false;
    if (vector == null) {
      vector = new Vector<java.lang.Object>(0);
      isTheFirstEntry = new Boolean(true);
    }

    if (vars.getMultiFile("inpFile") != null
        && !vars.getMultiFile("inpFile").getName().equals("")) {
      final AttachContent content = new AttachContent();
      final FileItem file1 = vars.getMultiFile("inpFile");
      content.setFileName(pocData[0].ourreference.replace('/', '_') + '-'
          + Utility.formatDate(new Date(), "yyyyMMdd-HHmmss") + '.' + file1.getName());
      content.setFileItem(file1);
      content.setId(Utility.formatDate(new Date(), "yyyyMMdd-HHmmss") + '.' + file1.getName());
      content.visible = "hidden";
      if (vars.getStringParameter("inpArchive") == "Y") {
        content.setSelected("true");
      }
      vector.addElement(content);
      request.getSession().setAttribute("files", vector);

    }

    if ("yes".equals(vars.getStringParameter("closed"))) {
      xmlDocument.setParameter("closed", "yes");
      request.getSession().removeAttribute("files");
    }

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\r\n");
    xmlDocument.setParameter("language", vars.getLanguage());
    xmlDocument.setParameter("theme", vars.getTheme());

    EmailDefinition emailDefinition = null;
    try {
      if (moreThanOneLanguageDefined(reports) && hasDifferentBpLanguages(reports)) {
        // set multiple email configurations
        List<EmailDefinition> emailDef = new ArrayList<EmailDefinition>();
        Map<String, EmailDefinition> emailDefinitions = reports.values().iterator().next()
            .getEmailDefinitions();
        Iterator<Entry<String, EmailDefinition>> entries = emailDefinitions.entrySet().iterator();
        while (entries.hasNext()) {
          Map.Entry<String, EmailDefinition> entry = entries.next();
          emailDef.add(entry.getValue());
        }
        emailDefinition = reports.values().iterator().next().getTemplateInfo()
            .get_DefaultEmailDefinition();
        String emailDefinitionsComboHtml = getOptionsList(emailDef, emailDefinition.getId(), false);
        xmlDocument.setParameter("reportEmailConfig", emailDefinitionsComboHtml);
        hasMultipleEmailConfigurations = true;
      } else {
        emailDefinition = reports.values().iterator().next().getEmailDefinition();
        hasMultipleEmailConfigurations = false;
      }
    } catch (final OBException exception) {
      final OBError on = new OBError();
      on.setMessage(Utility.messageBD(this, "EmailConfiguration", vars.getLanguage()));
      on.setTitle(Utility.messageBD(this, "Info", vars.getLanguage()));
      on.setType("info");
      final String tabId = vars.getSessionValue("inpTabId");
      vars.getStringParameter("tab");
      vars.setMessage(tabId, on);
      vars.getRequestGlobalVariable("inpTabId", "AttributeSetInstance.tabId");
      printPageClosePopUpAndRefreshParent(response, vars);
    } catch (ReportingException e) {
      log4j.error(e);
    }

    String fromEmail = null;
    String fromEmailId = null;

    OBContext.setAdminMode(true);
    try {
      OBCriteria<EmailServerConfiguration> mailConfigCriteria = OBDal.getInstance()
          .createCriteria(EmailServerConfiguration.class);
      mailConfigCriteria.addOrderBy("client.id", false);
      final List<EmailServerConfiguration> mailConfigList = mailConfigCriteria.list();

      if (mailConfigList.size() == 0) {
        throw new ServletException("No Poc configuration found for this client.");
      }

      EmailServerConfiguration mailConfig = EmailUtils
          .getEmailConfiguration(OBDal.getInstance().get(Organization.class, vars.getOrg()));

      if (mailConfig == null) {
        throw new ServletException(
            "No sender defined: Please go to client configuration to complete the email configuration.");
      }

      fromEmail = mailConfig.getSmtpServerSenderAddress();
      fromEmailId = mailConfig.getId();
    } finally {
      OBContext.restorePreviousMode();
    }

    // Get additional document information
    String draftDocumentIds = "";
    final AttachContent attachedContent = new AttachContent();
    final boolean onlyOneAttachedDoc = onlyOneAttachedDocs(reports);
    final Map<String, PocData> customerMap = new HashMap<String, PocData>();
    final Map<String, PocData> salesRepMap = new HashMap<String, PocData>();
    final Vector<Object> cloneVector = new Vector<Object>();
    boolean allTheDocsCompleted = true;
    for (final PocData documentData : pocData) {
      // Map used to count the different users

      final String customer = documentData.contactEmail;
      getEnvironentInformation(pocData, checks);
      if (checks.get("moreThanOneDoc")) {
        if (customer == null || customer.length() == 0) {
          final OBError on = new OBError();
          on.setMessage(Utility.messageBD(this, "NoContact", vars.getLanguage()).replace("@docNum@",
              documentData.ourreference));

          on.setTitle(Utility.messageBD(this, "Info", vars.getLanguage()));
          on.setType("info");
          final String tabId = vars.getSessionValue("inpTabId");
          vars.getStringParameter("tab");
          vars.setMessage(tabId, on);
          vars.getRequestGlobalVariable("inpTabId", "AttributeSetInstance.tabId");
          printPageClosePopUpAndRefreshParent(response, vars);
        } else if (documentData.contactEmail == null || documentData.contactEmail.equals("")) {
          final OBError on = new OBError();
          on.setMessage(Utility.messageBD(this, "NoEmail", vars.getLanguage()).replace("@customer@",
              documentData.contactName));
          on.setTitle(Utility.messageBD(this, "Info", vars.getLanguage()));
          on.setType("info");
          final String tabId = vars.getSessionValue("inpTabId");
          vars.getStringParameter("tab");
          vars.setMessage(tabId, on);
          vars.getRequestGlobalVariable("inpTabId", "AttributeSetInstance.tabId");
          printPageClosePopUpAndRefreshParent(response, vars);
        }
      }

      if (!customerMap.containsKey(customer)) {
        customerMap.put(customer, documentData);
      }

      final String salesRep = documentData.salesrepEmail;

      boolean moreThanOnesalesRep = checks.get("moreThanOnesalesRep").booleanValue();
      if (moreThanOnesalesRep) {
        if (salesRep == null || salesRep.length() == 0) {
          final OBError on = new OBError();
          on.setMessage(Utility.messageBD(this, "NoSenderDocument", vars.getLanguage()));
          on.setTitle(Utility.messageBD(this, "Info", vars.getLanguage()));
          on.setType("info");
          final String tabId = vars.getSessionValue("inpTabId");
          vars.getStringParameter("tab");
          vars.setMessage(tabId, on);
          vars.getRequestGlobalVariable("inpTabId", "AttributeSetInstance.tabId");
          printPageClosePopUpAndRefreshParent(response, vars);
        } else if (documentData.salesrepEmail == null || documentData.salesrepEmail.equals("")) {
          final OBError on = new OBError();
          on.setMessage(Utility.messageBD(this, "NoEmailSender", vars.getLanguage())
              .replace("@salesRep@", documentData.salesrepName));
          on.setTitle(Utility.messageBD(this, "Info", vars.getLanguage()));
          on.setType("info");
          final String tabId = vars.getSessionValue("inpTabId");
          vars.getStringParameter("tab");
          vars.setMessage(tabId, on);
          vars.getRequestGlobalVariable("inpTabId", "AttributeSetInstance.tabId");
          printPageClosePopUpAndRefreshParent(response, vars);
        }
      }

      if (!salesRepMap.containsKey(salesRep)) {
        salesRepMap.put(salesRep, documentData);
      }

      final Report report = reports.get(documentData.documentId);
      // All ids of documents in draft are passed to the web client
      if (report.isDraft()) {
        if (draftDocumentIds.length() > 0)
          draftDocumentIds += ",";
        draftDocumentIds += report.getDocumentId();
        allTheDocsCompleted = false;
      }

      // Fill the report location
      final String reportFilename = report.getContextSubFolder() + report.getFilename();
      documentData.reportLocation = request.getContextPath() + "/" + reportFilename + "?documentId="
          + documentData.documentId;
      if (log4j.isDebugEnabled())
        log4j.debug(" Filling report location with: " + documentData.reportLocation);

      if (onlyOneAttachedDoc) {
        attachedContent.setDocName(report.getFilename());
        attachedContent.setVisible("checkbox");
        cloneVector.add(attachedContent);
      }

    }
    if (!allTheDocsCompleted) {
      final OBError on = new OBError();
      on.setMessage(Utility.messageBD(this, "ErrorIncompleteDocuments", vars.getLanguage()));
      on.setTitle(Utility.messageBD(this, "ErrorSendingEmail", vars.getLanguage()));
      on.setType("Error");
      final String tabId = vars.getSessionValue("inpTabId");
      vars.getStringParameter("tab");
      vars.setMessage(tabId, on);
      vars.getRequestGlobalVariable("inpTabId", "AttributeSetInstance.tabId");
      printPageClosePopUpAndRefreshParent(response, vars);
    }

    final int numberOfCustomers = customerMap.size();
    final int numberOfSalesReps = salesRepMap.size();

    if (!onlyOneAttachedDoc && isTheFirstEntry) {
      if (numberOfCustomers > 1) {
        attachedContent.setDocName(String.valueOf(
            reports.size() + " Documents to " + String.valueOf(numberOfCustomers) + " Customers"));
        attachedContent.setVisible("checkbox");

      } else {
        attachedContent.setDocName(String.valueOf(reports.size() + " Documents"));
        attachedContent.setVisible("checkbox");

      }
      cloneVector.add(attachedContent);
    }

    final AttachContent[] data = new AttachContent[vector.size()];
    final AttachContent[] data2 = new AttachContent[cloneVector.size()];
    if (cloneVector.size() >= 1) { // Has more than 1 element
      vector.copyInto(data);
      cloneVector.copyInto(data2);
      xmlDocument.setData("structure2", data2);
      xmlDocument.setData("structure1", data);
    }
    if (pocData.length >= 1) {
      xmlDocument.setData("reportEmail", "liststructure",
          reports.get((pocData[0].documentId)).getTemplate());
    }

    if (log4j.isDebugEnabled())
      log4j.debug("Documents still in draft: " + draftDocumentIds);
    xmlDocument.setParameter("draftDocumentIds", draftDocumentIds);

    final PocData[] currentUserInfo = PocData.getContactDetailsForUser(this, vars.getUser());
    final String userName = currentUserInfo[0].userName;
    final String userEmail = currentUserInfo[0].userEmail;
    String bccEmail = "";
    String bccName = "";
    if (userEmail != null && userEmail.length() > 0) {
      bccEmail = userEmail;
      bccName = userName;
    }

    if (vars.commandIn("ADD") || vars.commandIn("DEL")) {
      xmlDocument.setParameter("fromEmailId", vars.getStringParameter("fromEmailId"));
      xmlDocument.setParameter("fromEmail", vars.getStringParameter("fromEmail"));
      xmlDocument.setParameter("toEmail", vars.getStringParameter("toEmail"));
      xmlDocument.setParameter("toEmailOrig", vars.getStringParameter("toEmailOrig"));
      xmlDocument.setParameter("ccEmail", vars.getStringParameter("ccEmail"));
      xmlDocument.setParameter("ccEmailOrig", vars.getStringParameter("ccEmailOrig"));
      xmlDocument.setParameter("bccEmail", vars.getStringParameter("bccEmail"));
      xmlDocument.setParameter("bccEmailOrig", vars.getStringParameter("bccEmailOrig"));
      xmlDocument.setParameter("replyToEmail", vars.getStringParameter("replyToEmail"));
      xmlDocument.setParameter("replyToEmailOrig", vars.getStringParameter("replyToEmailOrig"));
      xmlDocument.setParameter("emailSubject", vars.getStringParameter("emailSubject"));
      xmlDocument.setParameter("emailBody", vars.getStringParameter("emailBody"));
    } else {
      xmlDocument.setParameter("fromEmailId", fromEmailId);
      xmlDocument.setParameter("fromEmail", fromEmail);
      xmlDocument.setParameter("toEmail", pocData[0].contactEmail);
      xmlDocument.setParameter("toEmailOrig", pocData[0].contactEmail);
      xmlDocument.setParameter("ccEmail", "");
      xmlDocument.setParameter("ccEmailOrig", "");
      xmlDocument.setParameter("bccEmail", bccEmail);
      xmlDocument.setParameter("bccEmailOrig", bccEmail);
      xmlDocument.setParameter("replyToEmail", pocData[0].salesrepEmail);
      xmlDocument.setParameter("replyToEmailOrig", pocData[0].salesrepEmail);
      xmlDocument.setParameter("emailSubject", emailDefinition.getSubject());
      xmlDocument.setParameter("emailBody", emailDefinition.getBody());
    }
    xmlDocument.setParameter("inpArchive", vars.getStringParameter("inpArchive"));
    xmlDocument.setParameter("fromName", "");
    xmlDocument.setParameter("toName", pocData[0].contactName);
    xmlDocument.setParameter("ccName", "");
    xmlDocument.setParameter("bccName", bccName);
    xmlDocument.setParameter("replyToName", pocData[0].salesrepName);
    xmlDocument.setParameter("inpArchive", vars.getStringParameter("inpArchive"));
    xmlDocument.setParameter("multCusCount", String.valueOf(numberOfCustomers));
    xmlDocument.setParameter("multSalesRepCount", String.valueOf(numberOfSalesReps));
    if (!hasMultipleEmailConfigurations) {
      xmlDocument.setParameter("useDefault", "Y");
    }
    if (differentDocTypes.size() > 1) {
      xmlDocument.setParameter("multiDocType", "Y");
    }

    vars.setSessionObject("pocData" + fullDocumentIdentifier, pocData);
    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private String getOptionsList(List<EmailDefinition> emailDef, String selectedValue,
      boolean isMandatory) {
    StringBuilder strOptions = new StringBuilder();
    if (!isMandatory)
      strOptions.append("<option value=\"\"></option>");
    for (EmailDefinition obObject : emailDef) {
      strOptions.append("<option value=\"").append(obObject.getId()).append("\"");
      if (obObject.getId().equals(selectedValue))
        strOptions.append(" selected=\"selected\"");
      strOptions.append(">");
      strOptions.append(BasicUtility
          .formatMessageBDToHtml(obObject.getSubject() + " - " + obObject.getLanguage()));
      strOptions.append("</option>");
    }
    return strOptions.toString();
  }

  private boolean moreThanOneLanguageDefined(Map<String, Report> reports)
      throws ReportingException {
    boolean hasMoreThanOneLanguage = false;
    @SuppressWarnings("rawtypes")
    Iterator itRep = reports.values().iterator();
    while (itRep.hasNext()) {
      Report report = (Report) itRep.next();
      if (report.getEmailDefinitions().size() > 1) {
        hasMoreThanOneLanguage = true;
        break;
      }
    }
    return hasMoreThanOneLanguage;
  }

  private boolean hasDifferentBpLanguages(Map<String, Report> reports) throws ReportingException {
    boolean hasMoreThanOneLanguage = false;
    @SuppressWarnings("rawtypes")
    Iterator itRep = reports.values().iterator();
    Language currentLanguage = null;
    while (itRep.hasNext()) {
      Report report = (Report) itRep.next();
      String bPartnerId = report.getBPartnerId();
      BusinessPartner businessPartner = OBDal.getInstance().get(BusinessPartner.class, bPartnerId);
      Language language = businessPartner.getLanguage();
      if (currentLanguage == null) {
        currentLanguage = language;
      } else if (currentLanguage != language) {
        hasMoreThanOneLanguage = true;
      }
    }
    return hasMoreThanOneLanguage;
  }

  private void getEnvironentInformation(PocData[] pocData, HashMap<String, Boolean> checks) {
    final Map<String, PocData> customerMap = new HashMap<String, PocData>();
    final Map<String, PocData> salesRepMap = new HashMap<String, PocData>();
    int docCounter = 0;
    checks.put("moreThanOneDoc", false);
    for (final PocData documentData : pocData) {
      // Map used to count the different users
      docCounter++;
      final String customer = documentData.contactEmail;
      final String salesRep = documentData.salesrepEmail;
      if (!customerMap.containsKey(customer)) {
        customerMap.put(customer, documentData);
      }
      if (!salesRepMap.containsKey(salesRep)) {
        salesRepMap.put(salesRep, documentData);
      }
    }
    if (docCounter > 1) {
      checks.put("moreThanOneDoc", true);
    }
    boolean moreThanOneCustomer = (customerMap.size() > 1);
    boolean moreThanOnesalesRep = (salesRepMap.size() > 1);
    checks.put("moreThanOneCustomer", new Boolean(moreThanOneCustomer));
    checks.put("moreThanOnesalesRep", new Boolean(moreThanOnesalesRep));
  }

  /**
   * @author gmauleon
   * @param pocData
   * @param vars
   * @param vector
   * @return
   */
  private String[] getHiddenTags(PocData[] pocData, Vector<Object> vector, VariablesSecureApp vars,
      HashMap<String, Boolean> checks) {
    String[] discard;
    final Map<String, PocData> customerMap = new HashMap<String, PocData>();
    final Map<String, PocData> salesRepMap = new HashMap<String, PocData>();
    for (final PocData documentData : pocData) {
      // Map used to count the different users

      final String customer = documentData.contactEmail;
      final String salesRep = documentData.salesrepEmail;
      if (!customerMap.containsKey(customer)) {
        customerMap.put(customer, documentData);
      }
      if (!salesRepMap.containsKey(salesRep)) {
        salesRepMap.put(salesRep, documentData);
      }
    }
    boolean moreThanOneCustomer = (customerMap.size() > 1);
    boolean moreThanOnesalesRep = (salesRepMap.size() > 1);
    checks.put("moreThanOneCustomer", new Boolean(moreThanOneCustomer));
    checks.put("moreThanOnesalesRep", new Boolean(moreThanOnesalesRep));

    // check the number of customer and the number of
    // sales Rep. to choose one of the 3 possibilities
    // 1.- n customer n sales rep (hide "To" and "Reply-to" inputs)
    // 2.- n customers 1 sales rep (hide "To" input)
    // 3.- 1 customer n sales rep (hide Reply-to inputs)
    // 4.- Otherwise show both
    if (moreThanOneCustomer && moreThanOnesalesRep) {
      discard = new String[] { "to", "to_bottomMargin", "replyTo", "replyTo_bottomMargin" };
    } else if (moreThanOneCustomer) {
      discard = new String[] { "to", "to_bottomMargin", "multSalesRep", "multSalesRepCount" };
    } else if (moreThanOnesalesRep) {
      discard = new String[] { "replyTo", "replyTo_bottomMargin" };
    } else {
      discard = new String[] { "multipleCustomer", "multipleCustomer_bottomMargin" };
    }

    // check the templates
    if (differentDocTypes.size() > 1) { // the templates selector shouldn't
      // appear
      if (discard == null) { // Its the only think to hide
        discard = new String[] { "discardSelect" };
      } else {
        final String[] discardAux = new String[discard.length + 1];
        for (int i = 0; i < discard.length; i++) {
          discardAux[i] = discard[i];
        }
        discardAux[discard.length] = "discardSelect";
        return discardAux;
      }
    }
    if (vector == null && vars.getMultiFile("inpFile") == null) {
      if (discard == null) {
        discard = new String[] { "view" };
      } else {
        final String[] discardAux = new String[discard.length + 1];
        for (int i = 0; i < discard.length; i++) {
          discardAux[i] = discard[i];
        }
        discardAux[discard.length] = "view";
        return discardAux;
      }
    }
    return discard;
  }

  private boolean onlyOneAttachedDocs(Map<String, Report> reports) {
    if (reports.size() == 1) {
      return true;
    } else {
      return false;
    }

  }

  void createPrintStatusPage(HttpServletResponse response, VariablesSecureApp vars,
      int nrOfEmailsSend) throws IOException, ServletException {
    XmlDocument xmlDocument = null;
    xmlDocument = xmlEngine
        .readXmlTemplate("org/openbravo/erpCommon/utility/reporting/printing/PrintStatus")
        .createXmlDocument();
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\r\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("language", vars.getLanguage());
    xmlDocument.setParameter("nrOfEmailsSend", "" + nrOfEmailsSend);

    response.setContentType("text/html; charset=UTF-8");
    final PrintWriter out = response.getWriter();

    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * 
   * @param documentIds
   * @return returns a comma separated and quoted string of documents id's. useful to sql querys
   */
  private String getComaSeparatedString(String[] documentIds) {
    String result = new String("(");
    for (int index = 0; index < documentIds.length; index++) {
      final String documentId = documentIds[index];
      if (index + 1 == documentIds.length) {
        result = result + "'" + documentId + "')";
      } else {
        result = result + "'" + documentId + "',";
      }

    }
    return result;
  }

  /**
   * @author gmauleon
   * @param content
   * @return
   * @throws ServletException
   */
  private File prepareFile(AttachContent content, String documentId) throws ServletException {
    try {
      final String attachPath = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("attach.path") + "/tmp";
      final File f = new File(attachPath, content.getFileName());
      final InputStream inputStream = content.getFileItem().getInputStream();
      final OutputStream out = new FileOutputStream(f);
      final byte buf[] = new byte[1024];
      int len;
      while ((len = inputStream.read(buf)) > 0)
        out.write(buf, 0, len);
      out.close();
      inputStream.close();
      return f;
    } catch (final Exception e) {
      throw new ServletException("Error trying to get the attached file", e);
    }

  }

  /**
   * Returns an array of document's ID ordered by Document No ASC
   * 
   * @param documentType
   * @param documentIds
   *          array of document's ID without order
   * @return List of ordered IDs
   * @throws ServletException
   */
  private String[] orderByDocumentNo(DocumentType documentType, String[] documentIds)
      throws ServletException {
    String strTable = documentType.getTableName();

    StringBuffer strIds = new StringBuffer();
    strIds.append("'");
    for (int i = 0; i < documentIds.length; i++) {
      if (i > 0) {
        strIds.append("', '");
      }
      strIds.append(documentIds[i]);
    }
    strIds.append("'");

    PrintControllerData[] printControllerData;
    String documentIdsOrdered[] = new String[documentIds.length];
    int i = 0;
    if (strTable.equals("C_INVOICE")) {
      printControllerData = PrintControllerData.selectInvoices(this, strIds.toString());
      for (PrintControllerData docID : printControllerData) {
        documentIdsOrdered[i++] = docID.getField("Id");
      }
    } else if (strTable.equals("C_ORDER")) {
      printControllerData = PrintControllerData.selectOrders(this, strIds.toString());
      for (PrintControllerData docID : printControllerData) {
        documentIdsOrdered[i++] = docID.getField("Id");
      }
    } else if (strTable.equals("FIN_PAYMENT")) {
      printControllerData = PrintControllerData.selectPayments(this, strIds.toString());
      for (PrintControllerData docID : printControllerData) {
        documentIdsOrdered[i++] = docID.getField("Id");
      }
    } else
      return documentIds;

    return documentIdsOrdered;
  }

  protected void updateDatePrinted(ConnectionProvider connectionProvider, String strDocumentId,
      DocumentType documentType) throws Exception {
    switch (documentType) {
    case QUOTATION:
    case SALESORDER:
    case PURCHASEORDER:
      PrintControllerData.updateOrderPrintInfo(connectionProvider, strDocumentId);
      break;
    case SALESINVOICE:
      PrintControllerData.updateInvoicePrintInfo(connectionProvider, strDocumentId);
      break;
    case SHIPMENT:
      PrintControllerData.updateInOutPrintInfo(connectionProvider, strDocumentId);
      break;
    case WAREHOUSEPICKINGLISTS:
      PrintControllerData.updatePickingListPrintInfo(connectionProvider, strDocumentId);
      break;
    case PURCHASEORDERIMPORT:
      PrintControllerData.updateOrderImportPrintInfo(connectionProvider, strDocumentId);
      break;
    case GOODSTRANSFER:
      PrintControllerData.updateTransferInOutPrintInfo(connectionProvider, strDocumentId);
      break;
    case PURCHASEINVOICE:
      PrintControllerData.updateInvoicePrintInfo(connectionProvider, strDocumentId);
      break;
    case SALESINVOICEORDER:
      PrintControllerData.updateInvoicePrintInfo(connectionProvider, strDocumentId);
      break;
    case SHIPMENTORDER:
      PrintControllerData.updateInOutPrintInfo(connectionProvider, strDocumentId);
      break;
    case PAYMENT:
    case PURCHASEPREPAYMENT:
    case PRELIQUIDATION:
    case COSTINGIMPORT:
    case PURCHASEWITHHOLDINGRECEIPT:
    case SALESWITHHOLDINGRECEIPT:
    case FINANCIALACCOUNT:
    case PAYMENTOUTCHECK:
    case SALESPERCEPTIONRECEIPT:
    case REQUISITION:
    case ACCOUNTABILITY:
    case BILLOFEXCHANGETOCOLLECTIONDOC:
    case FOLIO:
    case WAYBILL:
    case GOODSMOVEMENT:
    case BOMPRODUCTION:
    case MOVEMENTCODE:
    case REQUERIMIENTOREPOSICION:
    case SALESPROSPECT:
    case SRECONTRACTPAYSCHEDULE:
      break;
    }
  }

  protected boolean isInvoiceWithPhyDocNo_ToCheck(String strTabId) {
    if (strTabId.equals("192D51223A4D4919880A08622D9C56D7") // N/Débito(Clt)"AR Debit Memo"
        || strTabId.equals("91EDF2EEB72E4D49BB050574B34E5A15") // Factura(Clt)"Sales Invoice"
        || strTabId.equals("3460A4AF36EE457EB5531F2BF6155D91") // N/Crédito(Clt)"AR Credit Memo"
        || strTabId.equals("9AFBDDABD4D04F03B84C6FA44232E40D") // Boleta(Clt)"Sales Ticket"
        || strTabId.equals("77FC9EE9608941CB9939E528F5E8839C") // N/Cred.Devol(Clt)"Return Material
                                                               // Sales Inv"
        || strTabId.equals("9FB63E7E3D8C49989EE16DBD724E5DD0") // Letra"AR Bill Of Exchange Invoice"
        || strTabId.equals("7BCF30AEED0D4693B0848E97DC2A5EA6") // Letra"Renovación de Letras"
        || strTabId.equals("2A33566839434530AA02750DCEE58553") // Letra"Canje de Letras"
        || strTabId.equals("2359FAB94F09415EBE97402337235CBE") // Letra"Canje de Letras"
    ) {
      return true;
    }
    return false;
  }

  protected boolean isPickingList_ToCheck(String strTabId) {
    if (strTabId.equals("7D68FFCA597C4F84BC385DBCA7A8308C")) // Documento de
      // Picking
      return true;
    return false;
  }

  protected boolean isInOutWithPhyDocNo_ToCheck(String strTabId) {
    if (strTabId.equals("257") // Documento de Salida"Goods Shipment"
        || strTabId.equals("728DBD16A1F14A4D82335E37BA433E33") // Devol
    // a g/r
    // de
    // proveedor
    // //
    // "Return to Vendor Shipment"
    ) {
      return true;
    }
    return false;
  }

  protected OBError validateAdUSerPickier(VariablesSecureApp vars, String strTabId,
      String strDocumentId, String newAdUserPickerId) throws Exception {
    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));

    try {
      // PICKEING LIST
      if (newAdUserPickerId == null || "".equals(newAdUserPickerId)) {
        msg.setType("Error");
        msg.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
        msg.setMessage(
            Utility.parseTranslation(this, vars, vars.getLanguage(), "@swa_PickerNoSelected@"));
        return msg;
      }
    } catch (Exception e) {
      // System.out.println("e.getMessage():" + e.getMessage());
      e.printStackTrace(System.err);
      msg.setType("Error");
      msg.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
      msg.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(),
          FIN_Utility.getExceptionMessage(e)));
      OBDal.getInstance().rollbackAndClose();
    }

    return msg;
  }

  protected OBError validatePhyDocNo(VariablesSecureApp vars, String strTabId, String strDocumentId,
      String strNewPhyDocNo) throws Exception {
    OBError msg = new OBError();
    msg.setType("Success");
    msg.setTitle(Utility.messageBD(this, "Success", vars.getLanguage()));

    try {
      Client client = OBDal.getInstance().get(Client.class, vars.getSessionValue("#AD_CLIENT_ID"));
      strDocumentId = strDocumentId.replaceAll("\\(|\\)|'", "");

      // INVOICES
      if (isInvoiceWithPhyDocNo_ToCheck(strTabId)) {
        Invoice invoice = OBDal.getInstance().get(Invoice.class, strDocumentId);
        if (invoice == null) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
          msg.setMessage(
              Utility.parseTranslation(this, vars, vars.getLanguage(), "@SCR_NoInvoiceData@"));
          return msg;
        }

        if (strNewPhyDocNo == null || "".equals(strNewPhyDocNo)
            || defPhyDocNo.equals(strNewPhyDocNo)) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
          msg.setMessage(
              Utility.parseTranslation(this, vars, vars.getLanguage(), "@SCR_NoPhysicalDocNo@"));
          return msg;
        }
        if (existsInvoicePhyDocNo(client, strDocumentId, strNewPhyDocNo)) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
          msg.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(),
              "@SCO_SalesInvUniqPhysicalNumber@"));
          return msg;
        }

        // GOODS SHIPMENTS
      } else if (isInOutWithPhyDocNo_ToCheck(strTabId)) {
        ShipmentInOut shipment = OBDal.getInstance().get(ShipmentInOut.class, strDocumentId);
        if (shipment == null) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
          msg.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(),
              "@SCR_NoGoodsShipmentData@"));
          return msg;
        }
        if ("SWAMMDISPATCH".equals(shipment.getSCOSpecialDocType())) {
          return msg;
        }

        if (strNewPhyDocNo == null || "".equals(strNewPhyDocNo)
            || defPhyDocNo.equals(strNewPhyDocNo)) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
          msg.setMessage(
              Utility.parseTranslation(this, vars, vars.getLanguage(), "@SCR_NoPhysicalDocNo@"));
          return msg;
        }
        if (existsInOutPhyDocNo(client, strDocumentId, strNewPhyDocNo)) {
          msg.setType("Error");
          msg.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
          msg.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(),
              "@SCO_InoutSalesInvUniqPhysicalNumber@"));
          return msg;
        }
      }

    } catch (Exception e) {
      // System.out.println("e.getMessage():" + e.getMessage());
      e.printStackTrace(System.err);
      msg.setType("Error");
      msg.setTitle(Utility.messageBD(this, "Error", vars.getLanguage()));
      msg.setMessage(Utility.parseTranslation(this, vars, vars.getLanguage(),
          FIN_Utility.getExceptionMessage(e)));
      OBDal.getInstance().rollbackAndClose();
    }

    return msg;
  }

  protected boolean existsInvoicePhyDocNo(Client client, String specialDocTypeDocument,
      String physicaldocno) {
    OBCriteria<Invoice> invoices = OBDal.getInstance().createCriteria(Invoice.class);
    invoices.add(Restrictions.eq(Invoice.PROPERTY_CLIENT, client));
    invoices.add(Restrictions.eq(Invoice.PROPERTY_SCOSPECIALDOCTYPE, specialDocTypeDocument));
    invoices.add(Restrictions.ne(Invoice.PROPERTY_DOCUMENTSTATUS, "VO"));
    invoices.add(Restrictions.ne(Invoice.PROPERTY_DOCUMENTSTATUS, "DR"));
    invoices.add(Restrictions.eq(Invoice.PROPERTY_SCRPHYSICALDOCUMENTNO, physicaldocno));
    if (invoices.list().size() >= 1)
      return true;
    return false;
  }

  protected boolean existsInOutPhyDocNo(Client client, String specialDocTypeDocument,
      String physicaldocno) {
    OBCriteria<ShipmentInOut> shipments = OBDal.getInstance().createCriteria(ShipmentInOut.class);
    shipments.add(Restrictions.eq(ShipmentInOut.PROPERTY_CLIENT, client));
    shipments
        .add(Restrictions.eq(ShipmentInOut.PROPERTY_SCOSPECIALDOCTYPE, specialDocTypeDocument));
    shipments.add(Restrictions.ne(Invoice.PROPERTY_DOCUMENTSTATUS, "VO"));
    shipments.add(Restrictions.ne(Invoice.PROPERTY_DOCUMENTSTATUS, "DR"));
    shipments.add(Restrictions.eq(ShipmentInOut.PROPERTY_SCRPHYSICALDOCUMENTNO, physicaldocno));
    if (shipments.list().size() > 0)
      return true;
    return false;
  }

  protected void updateNextPhyDocSequence(String strPhyDocSeqId) {
    // Updating Next Prefix in Serial Physical Document Number
    if (!"".equals(strPhyDocSeqId.trim()) && strPhyDocSeqId != null) {

      OBContext.setAdminMode(true);
      try {
        BILLPhyDocSequence phyDocSeq = OBDal.getInstance().get(BILLPhyDocSequence.class,
            strPhyDocSeqId);
        phyDocSeq
            .setNextAssignedNumber(phyDocSeq.getNextAssignedNumber() + phyDocSeq.getIncrementBy());

        OBDal.getInstance().save(phyDocSeq);
        OBDal.getInstance().flush();

      } finally {
        OBContext.restorePreviousMode();
      }

    }

  }

  private String getAutoPhyDocNo(String prevPhyDocNo) {
    String docNoPart1 = "", docNoPart2 = "";

    if (prevPhyDocNo.split("[0-9]+$").length > 0) {
      docNoPart1 = prevPhyDocNo.split("[0-9]+$")[0];
    }
    docNoPart2 = prevPhyDocNo.substring(docNoPart1.length());

    if ("".equals(docNoPart2) || docNoPart2 == null) {
      log4j.error("Validating getAutoPhyDocNo(prevPhyDocNo): prevPhyDocNo incorrect format");
      return PrintController.defPhyDocNo;
    }

    if (pe.com.unifiedgo.report.common.Utility.isNumeric(docNoPart2)) {
      docNoPart2 = String.format("%0" + phyDocRDigits + "d", (Integer.parseInt(docNoPart2) + 1));
    }
    return docNoPart1 + docNoPart2;
  }

  private String getAutoPhyDocNo(String prefix, Long nextDocNo) {
    if ("".equals(prefix) || prefix == null) {
      log4j.error("Validating getAutoPhyDocNo(prefix,nextDocNo): prefix empty or null");
      return PrintController.defPhyDocNo;
    }
    if (nextDocNo == null) {
      log4j.error("Validating getAutoPhyDocNo(prefix,nextDocNo): nextDocNo null");
      return PrintController.defPhyDocNo;
    }

    String nextPhyDocNo = String.format("%0" + phyDocRDigits + "d", nextDocNo);
    return prefix + nextPhyDocNo;
  }

  @Override
  public String getServletInfo() {
    return "Servlet that processes the print action";
  } // End of getServletInfo() method
}