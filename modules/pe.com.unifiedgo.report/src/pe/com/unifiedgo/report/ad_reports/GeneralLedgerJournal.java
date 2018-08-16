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
package pe.com.unifiedgo.report.ad_reports;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesHistory;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.businessUtility.AccountingSchemaMiscData;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchema;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchemaTable;
import org.openbravo.model.financialmgmt.accounting.coa.ElementValue;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.StructPle;
import pe.com.unifiedgo.accounting.SunatUtil;

public class GeneralLedgerJournal extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  /**
   * Keeps a comma-separated list of the accounting entries that has been shown, from the newest one
   * to the oldest one. Used for navigation purposes
   */
  private static final String PREVIOUS_ACCTENTRIES = "GeneralLedgerJournal.previousAcctEntries";
  private static final String PREVIOUS_ACCTENTRIES_OLD = "GeneralLedgerJournal.previousAcctEntriesOld";

  /**
   * Keeps a comma-separated list of the line's range that has been shown, from the newest one to
   * the oldest one. Used for navigation purposes
   */
  private static final String PREVIOUS_RANGE = "GeneralLedgerJournal.previousRange";
  private static final String PREVIOUS_RANGE_OLD = "GeneralLedgerJournal.previousRangeOld";

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (log4j.isDebugEnabled())
      log4j.debug("Command: " + vars.getStringParameter("Command"));

    if (vars.commandIn("DEFAULT")) {
      String strcAcctSchemaId = vars.getGlobalVariable("inpcAcctSchemaId",
          "GeneralLedgerJournal|cAcctSchemaId", "");
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "GeneralLedgerJournal|DateFrom",
          "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "GeneralLedgerJournal|DateTo", "");
      String strDocument = vars.getGlobalVariable("inpDocument", "GeneralLedgerJournal|Document",
          "");
      String strOrg = vars.getGlobalVariable("inpOrg", "GeneralLedgerJournal|Org", "0");

      String strShowClosing = "Y";
      String strShowReg = "Y";
      String strShowOpening = "Y";
      String strShowRegular = "Y";
      String strShowDivideUp = "Y";

      String strRecord = vars.getGlobalVariable("inpRecord", "GeneralLedgerJournal|Record", "");
      String strTable = vars.getGlobalVariable("inpTable", "GeneralLedgerJournal|Table", "");
      log4j.debug("********DEFAULT***************  strShowClosing: " + strShowClosing);
      log4j.debug("********DEFAULT***************  strShowReg: " + strShowReg);
      log4j.debug("********DEFAULT***************  strShowOpening: " + strShowOpening);
      String initRecordNumberOld = vars.getSessionValue("GeneralLedgerJournal.initRecordNumberOld",
          "0");
      if (vars.getSessionValue("GeneralLedgerJournal.initRecordNumber", "0").equals("0")) {
        vars.setSessionValue("GeneralLedgerJournal.initRecordNumber", "0");
        vars.setSessionValue(PREVIOUS_ACCTENTRIES, "0");
        vars.setSessionValue(PREVIOUS_RANGE, "");
      } else if (!"-1".equals(initRecordNumberOld)) {
        vars.setSessionValue("GeneralLedgerJournal.initRecordNumber", initRecordNumberOld);
        vars.setSessionValue(PREVIOUS_ACCTENTRIES, vars.getSessionValue(PREVIOUS_ACCTENTRIES_OLD));
        vars.setSessionValue(PREVIOUS_RANGE, vars.getSessionValue(PREVIOUS_RANGE_OLD));
      }
      String strPageNo = vars.getGlobalVariable("inpPageNo", "GeneralLedgerJournal|PageNo", "1");
      String strEntryNo = vars.getGlobalVariable("inpEntryNo", "GeneralLedgerJournal|EntryNo", "1");
      String strShowDescription = vars.getGlobalVariable("inpShowDescription",
          "GeneralLedgerJournal|ShowDescription", "Y");
      String strDescLibroElectrMov = vars.getGlobalVariable("inpDescLibroElectrMov",
          "GeneralLedgerJournal|DescLibroElectrMov", "");
      String strDescLibroElectrPlan = vars.getGlobalVariable("inpDescLibroElectrPlan",
          "GeneralLedgerJournal|DescLibroElectrPlan", "");

      printPageDataSheet(response, vars, strDateFrom, strDateTo, strDocument, strOrg, strTable,
          strRecord, "", strcAcctSchemaId, strShowClosing, strShowReg, strShowOpening, strPageNo,
          strEntryNo, strShowDescription, strShowRegular, strShowDivideUp, "", "",
          strDescLibroElectrMov, strDescLibroElectrPlan, "", true);
    } else if (vars.commandIn("DIRECT")) {
      String strPeriodoId = vars.getStringParameter("inpPeriodo");
      String strTable = vars.getGlobalVariable("inpTable", "GeneralLedgerJournal|Table");
      String strRecord = vars.getGlobalVariable("inpRecord", "GeneralLedgerJournal|Record");
      String strAccSchemas = vars.getGlobalVariable("inpAccSchemas",
          "GeneralLedgerJournal|AccSchemas");
      String strDescLibroElectrMov = vars.getGlobalVariable("inpDescLibroElectrMov",
          "GeneralLedgerJournal|DescLibroElectrMov", "");
      String strDescLibroElectrPlan = vars.getGlobalVariable("inpDescLibroElectrPlan",
          "GeneralLedgerJournal|DescLibroElectrPlan", "");

      String paramschemas = vars.getStringParameter("inpParamschemas");
      String strPosted = vars.getStringParameter("posted");
      if (strPosted == "") {
        if (paramschemas != "") {
          strAccSchemas = paramschemas;
        }
      }

      String[] accSchemas = strAccSchemas.split(",");
      String strcAcctSchemaId = accSchemas[0];
      String schemas = "";
      for (int i = 1; i < accSchemas.length; i++) {
        if (i + 1 == accSchemas.length) {
          schemas = schemas + accSchemas[i];
        } else {
          schemas = schemas + accSchemas[i] + ",";
        }
      }
      setHistoryCommand(request, "DIRECT");
      vars.setSessionValue("GeneralLedgerJournal.initRecordNumber", "0");
      printPageDataSheet(response, vars, "", "", "", "", strTable, strRecord, "", strcAcctSchemaId,
          "", "", "", "1", "1", "", "Y", "", schemas, strPosted, strDescLibroElectrMov,
          strDescLibroElectrPlan, strPeriodoId, true);
    } else if (vars.commandIn("DIRECT2")) {
      String strPeriodoId = vars.getStringParameter("inpPeriodo");
      String strFactAcctGroupId = vars.getGlobalVariable("inpFactAcctGroupId",
          "GeneralLedgerJournal|FactAcctGroupId");
      String strDescLibroElectrMov = vars.getGlobalVariable("inpDescLibroElectrMov",
          "GeneralLedgerJournal|DescLibroElectrMov", "");
      String strDescLibroElectrPlan = vars.getGlobalVariable("inpDescLibroElectrPlan",
          "GeneralLedgerJournal|DescLibroElectrPlan", "");
      setHistoryCommand(request, "DIRECT2");
      vars.setSessionValue("GeneralLedgerJournal.initRecordNumber", "0");

      printPageDataSheet(response, vars, "", "", "", "", "", "", strFactAcctGroupId, "", "", "", "",
          "1", "1", "", "Y", "", "", "", strDescLibroElectrMov, strDescLibroElectrPlan,
          strPeriodoId, true);
    } else if (vars.commandIn("FIND")) {
      String strPeriodoId = vars.getStringParameter("inpPeriodo");
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId",
          "GeneralLedgerJournal|cAcctSchemaId");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "GeneralLedgerJournal|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "GeneralLedgerJournal|DateTo");
      String strDocument = vars.getRequestGlobalVariable("inpDocument",
          "GeneralLedgerJournal|Document");
      String strOrg = vars.getGlobalVariable("inpOrg", "GeneralLedgerJournal|Org", "0");

      String strShowClosing = "Y";
      String strShowReg = "Y";
      String strShowOpening = "Y";
      String strShowRegular = "Y";
      String strShowDivideUp = "Y";

      vars.setSessionValue("GeneralLedgerJournal.initRecordNumber", "0");
      vars.setSessionValue(PREVIOUS_ACCTENTRIES, "0");
      vars.setSessionValue(PREVIOUS_RANGE, "");
      setHistoryCommand(request, "DEFAULT");
      String strPageNo = vars.getRequestGlobalVariable("inpPageNo", "GeneralLedgerJournal|PageNo");
      String strEntryNo = vars.getRequestGlobalVariable("inpEntryNo",
          "GeneralLedgerJournal|EntryNo");
      String strShowDescription = vars.getRequestGlobalVariable("inpShowDescription",
          "GeneralLedgerJournal|ShowDescription");
      if (strShowDescription == null || "".equals(strShowDescription))
        vars.setSessionValue("GeneralLedgerJournal|ShowDescription", "N");
      String strDescLibroElectrMov = vars.getGlobalVariable("inpDescLibroElectrMov",
          "GeneralLedgerJournal|DescLibroElectrMov", "");
      String strDescLibroElectrPlan = vars.getGlobalVariable("inpDescLibroElectrPlan",
          "GeneralLedgerJournal|DescLibroElectrPlan", "");
      printPageDataSheet(response, vars, strDateFrom, strDateTo, strDocument, strOrg, "", "", "",
          strcAcctSchemaId, strShowClosing, strShowReg, strShowOpening, strPageNo, strEntryNo,
          strShowDescription, strShowRegular, strShowDivideUp, "", "", strDescLibroElectrMov,
          strDescLibroElectrPlan, strPeriodoId, false);
    } else if (vars.commandIn("PDF", "XLS")) {
      if (log4j.isDebugEnabled())
        log4j.debug("PDF");
      String strcAcctSchemaId = vars.getRequestGlobalVariable("inpcAcctSchemaId",
          "GeneralLedgerJournal|cAcctSchemaId");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "GeneralLedgerJournal|DateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "GeneralLedgerJournal|DateTo");
      String strDocument = vars.getRequestGlobalVariable("inpDocument",
          "GeneralLedgerJournal|Document");
      String strOrg = vars.getGlobalVariable("inpOrg", "GeneralLedgerJournal|Org", "0");

      String strShowClosing = "Y";
      String strShowReg = "Y";
      String strShowOpening = "Y";
      String strShowRegular = "Y";
      String strShowDivideUp = "Y";

      // String strRecord = vars.getGlobalVariable("inpRecord",
      // "GeneralLedgerJournal|Record");
      // String strTable = vars.getGlobalVariable("inpTable",
      // "GeneralLedgerJournal|Table");
      String strTable = vars.getStringParameter("inpTable");
      String strRecord = vars.getStringParameter("inpRecord");
      String strPageNo = vars.getGlobalVariable("inpPageNo", "GeneralLedgerJournal|PageNo", "1");
      String strEntryNo = vars.getGlobalVariable("inpEntryNo", "GeneralLedgerJournal|EntryNo", "1");
      String strShowDescription = vars.getRequestGlobalVariable("inpShowDescription",
          "GeneralLedgerJournal|ShowDescription");

      String strShowAll = vars.getStringParameter("inpShowAll");
      if (strShowDescription == null || "".equals(strShowDescription))
        vars.setSessionValue("GeneralLedgerJournal|ShowDescription", "N");

      String strFactAcctGroupId = "";
      if (strcAcctSchemaId.equals("") && strDateFrom.equals("") && strDocument.equals("")
          && strOrg.equals("0") && strShowClosing.equals("") && strShowReg.equals("")
          && strShowOpening.equals("") && strRecord.equals("")) {

        int currentHistoryIndex = new Integer(
            new VariablesHistory(request).getCurrentHistoryIndex()).intValue();
        String currentCommand = vars.getSessionValue("reqHistory.command" + currentHistoryIndex);
        if (currentCommand.equals("DIRECT2")) {
          strFactAcctGroupId = vars.getGlobalVariable("inpFactAcctGroupId",
              "GeneralLedgerJournal|FactAcctGroupId");
        }
      }
      // vars.setSessionValue("GeneralLedgerJournal.initRecordNumber",
      // "0");
      String strDescLibroElectrMov = vars.getStringParameter("inpDescLibroElectrMov");
      String strDescLibroElectrPlan = vars.getStringParameter("inpDescLibroElectrPlan");
      setHistoryCommand(request, "DEFAULT");

      printPagePDF(request, response, vars, strDateFrom, strDateTo, strDocument, strOrg, strTable,
          strRecord, strFactAcctGroupId, strcAcctSchemaId, strShowClosing, strShowReg,
          strShowOpening, strPageNo, strEntryNo, "Y".equals(strShowDescription) ? "Y" : "",
          strShowRegular, strShowDivideUp, strDescLibroElectrMov, strDescLibroElectrPlan,
          strShowAll);
    } else if (vars.commandIn("PREVIOUS_RELATION")) {
      String strInitRecord = vars.getSessionValue("GeneralLedgerJournal.initRecordNumber");
      String strPreviousRecordRange = vars.getSessionValue(PREVIOUS_RANGE);

      String[] previousRecord = strPreviousRecordRange.split(",");
      strPreviousRecordRange = previousRecord[0];
      int intRecordRange = strPreviousRecordRange.equals("") ? 0
          : Integer.parseInt(strPreviousRecordRange);
      strPreviousRecordRange = previousRecord[1];
      intRecordRange += strPreviousRecordRange.equals("") ? 0
          : Integer.parseInt(strPreviousRecordRange);

      // Remove parts of the previous range
      StringBuffer sb_previousRange = new StringBuffer();
      for (int i = 2; i < previousRecord.length; i++) {
        sb_previousRange.append(previousRecord[i] + ",");
      }
      vars.setSessionValue(PREVIOUS_RANGE, sb_previousRange.toString());

      // Remove parts of the previous accounting entries
      String[] previousAcctEntries = vars.getSessionValue(PREVIOUS_ACCTENTRIES).split(",");
      StringBuffer sb_previousAcctEntries = new StringBuffer();
      for (int i = 2; i < previousAcctEntries.length; i++) {
        sb_previousAcctEntries.append(previousAcctEntries[i] + ",");
      }

      if (strInitRecord.equals("") || strInitRecord.equals("0"))
        vars.setSessionValue("GeneralLedgerJournal.initRecordNumber", "0");
      else {
        int initRecord = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord < 0) ? "0" : Integer.toString(initRecord));
        vars.setSessionValue("GeneralLedgerJournal.initRecordNumber", strInitRecord);
        vars.setSessionValue(PREVIOUS_ACCTENTRIES, sb_previousAcctEntries.toString());
      }

      vars.setSessionValue("GeneralLedgerJournal.initRecordNumberOld", "-1");
      response.sendRedirect(getDireccion() + request.getServletPath());
    } else if (vars.commandIn("NEXT_RELATION")) {
      vars.setSessionValue("GeneralLedgerJournal.initRecordNumberOld", "-1");
      response.sendRedirect(getDireccion() + request.getServletPath());
    } else if (vars.commandIn("DOC")) {
      String org = vars.getStringParameter("inpOrg");
      String accSchema = vars.getStringParameter("inpcAcctSchemaId");
      String strDocument = vars.getRequestGlobalVariable("inpDocument",
          "GeneralLedgerJournal|Document");
      Set<String> docbasetypes = getDocuments(org, accSchema);
      String combobox = getJSONComboBox(docbasetypes, strDocument, false, vars);

      response.setContentType("text/html; charset=UTF-8");
      PrintWriter out = response.getWriter();
      out.println("objson = " + combobox);
      out.close();

    } else
      pageError(response);
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strDateFrom, String strDateTo, String strDocument, String strOrg, String strTable,
      String strRecord, String strFactAcctGroupId, String strcAcctSchemaId, String strShowClosing,
      String strShowReg, String strShowOpening, String strPageNo, String strEntryNo,
      String strShowDescription, String strShowRegular, String strShowDivideUp, String accShemas,
      String strPosted, String strDescLibroElectrMov, String strDescLibroElectrPlan,
      String strPeriodoId, boolean isFirstLoad) throws IOException, ServletException {
    String strRecordRange = Utility.getContext(this, vars, "#RecordRange", "GeneralLedgerJournal");
    int intRecordRangePredefined = (strRecordRange.equals("") ? 0
        : Integer.parseInt(strRecordRange));
    String strInitRecord = vars.getSessionValue("GeneralLedgerJournal.initRecordNumber");
    int initRecordNumber = (strInitRecord.equals("") ? 0 : Integer.parseInt(strInitRecord));
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = null;
    GeneralLedgerJournalData[] data = null;
    GeneralLedgerJournalData[] dataCountLines = null;
    String strPosition = "0";
    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "GeneralLedgerJournal", false, "", "",
        "imprimir();return false;", false, "ad_reports", strReplaceWith, false, true);
    // toolbar.setEmail(false);

    toolbar.prepareSimpleToolBarTemplate();

    toolbar.prepareSimpleToolBarTemplate();
    toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");

    int totalAcctEntries = 0;
    int lastRecordNumber = 0;
    if (vars.commandIn("FIND") || vars.commandIn("DEFAULT")
        && (!vars.getSessionValue("GeneralLedgerJournal.initRecordNumber").equals("0")
            || "0".equals(vars.getSessionValue("GeneralLedgerJournal.initRecordNumberOld", "")))) {
      String strCheck = buildCheck(strShowClosing, strShowReg, strShowOpening, strShowRegular,
          strShowDivideUp);
      String strTreeOrg = TreeData.getTreeOrg(this, vars.getClient());
      String strOrgFamily = getFamily(this, strTreeOrg, strOrg);
      if (strRecord.equals("")) {
        // Stores the number of lines per accounting entry
        dataCountLines = GeneralLedgerJournalData.selectCountGroupedLines(this,
            Utility.getContext(this, vars, "#User_Client", "GeneralLedgerJournal"),
            Utility.getContext(this, vars, "#AccessibleOrgTree", "GeneralLedgerJournal"),
            strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strDocument,
            strcAcctSchemaId, strOrgFamily, strCheck);
        String strInitAcctEntries = vars.getSessionValue(PREVIOUS_ACCTENTRIES);
        int acctEntries = (strInitAcctEntries.equals("") ? 0
            : Integer.parseInt(strInitAcctEntries.split(",")[0]));

        for (GeneralLedgerJournalData i : dataCountLines)
          totalAcctEntries += Integer.parseInt(i.groupedlines);

        int groupedLines[] = new int[intRecordRangePredefined + 1];
        int i = 1;
        while (groupedLines[i - 1] <= intRecordRangePredefined
            && dataCountLines.length >= acctEntries) {
          if (dataCountLines.length > acctEntries) {
            groupedLines[i] = groupedLines[i - 1]
                + Integer.parseInt(dataCountLines[acctEntries].groupedlines);
            i++;
          }
          acctEntries++;
        }

        int intRecordRangeUsed = 0;
        if (dataCountLines.length != acctEntries - 1) {
          if (i == 2) {
            // The first entry is bigger than the predefined range
            intRecordRangeUsed = groupedLines[i - 1];
            acctEntries++;
          } else if (i - 2 >= 0) {
            intRecordRangeUsed = groupedLines[i - 2];
          }
        } else {
          // Include also the last entry
          intRecordRangeUsed = groupedLines[i - 1];
        }

        // Hack for sqlC first record
        if (initRecordNumber == 0) {
          lastRecordNumber = initRecordNumber + intRecordRangeUsed + 1;
        } else {
          lastRecordNumber = initRecordNumber + intRecordRangeUsed;
        }
        vars.setSessionValue("GeneralLedgerJournal.initRecordNumber",
            String.valueOf(lastRecordNumber));
        vars.setSessionValue("GeneralLedgerJournal.initRecordNumberOld", strInitRecord);

        // Stores historical for navigation purposes
        vars.setSessionValue(PREVIOUS_ACCTENTRIES_OLD, vars.getSessionValue(PREVIOUS_ACCTENTRIES));
        vars.setSessionValue(PREVIOUS_ACCTENTRIES,
            String.valueOf(acctEntries - 1) + "," + vars.getSessionValue(PREVIOUS_ACCTENTRIES));
        vars.setSessionValue(PREVIOUS_RANGE_OLD, vars.getSessionValue(PREVIOUS_RANGE));
        vars.setSessionValue(PREVIOUS_RANGE,
            String.valueOf(intRecordRangeUsed) + "," + vars.getSessionValue(PREVIOUS_RANGE));
        data = GeneralLedgerJournalData.select(this, "'Y'",
            Utility.getContext(this, vars, "#User_Client", "GeneralLedgerJournal"),
            Utility.getContext(this, vars, "#AccessibleOrgTree", "GeneralLedgerJournal"),
            strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strDocument,
            strcAcctSchemaId, "S", strOrgFamily, strCheck, vars.getLanguage(), initRecordNumber,
            intRecordRangeUsed);

        if (data != null && data.length > 0)
          strPosition = GeneralLedgerJournalData.selectCount(this,
              Utility.getContext(this, vars, "#User_Client", "GeneralLedgerJournal"),
              Utility.getContext(this, vars, "#AccessibleOrgTree", "GeneralLedgerJournal"),
              strDateFrom, DateTimeData.nDaysAfter(this, strDateTo, "1"), strDocument,
              strcAcctSchemaId, strOrgFamily, strCheck, data[0].dateacct, data[0].identifier);
      } else {
        data = GeneralLedgerJournalData.selectDirect(this,
            Utility.getContext(this, vars, "#User_Client", "GeneralLedgerJournal"),
            Utility.getContext(this, vars, "#AccessibleOrgTree", "GeneralLedgerJournal"), strTable,
            strRecord, strcAcctSchemaId, vars.getLanguage(), initRecordNumber,
            intRecordRangePredefined);

        if (data != null && data.length > 0)
          strPosition = GeneralLedgerJournalData.selectCountDirect(this,
              Utility.getContext(this, vars, "#User_Client", "GeneralLedgerJournal"),
              Utility.getContext(this, vars, "#AccessibleOrgTree", "GeneralLedgerJournal"),
              strTable, strRecord, strFactAcctGroupId, data[0].dateacct, data[0].identifier);
      }
    } else if (vars.commandIn("DIRECT")) {
      data = GeneralLedgerJournalData.selectDirect(this,
          Utility.getContext(this, vars, "#User_Client", "GeneralLedgerJournal"),
          Utility.getContext(this, vars, "#AccessibleOrgTree", "GeneralLedgerJournal"), strTable,
          strRecord, strcAcctSchemaId, vars.getLanguage());
      if (data != null && data.length > 0)
        strPosition = GeneralLedgerJournalData.selectCountDirect(this,
            Utility.getContext(this, vars, "#User_Client", "GeneralLedgerJournal"),
            Utility.getContext(this, vars, "#AccessibleOrgTree", "GeneralLedgerJournal"), strTable,
            strRecord, strFactAcctGroupId, data[0].dateacct, data[0].identifier);
    } else if (vars.commandIn("DIRECT2")) {
      data = GeneralLedgerJournalData.selectDirect2(this,
          Utility.getContext(this, vars, "#User_Client", "GeneralLedgerJournal"),
          Utility.getContext(this, vars, "#AccessibleOrgTree", "GeneralLedgerJournal"),
          strFactAcctGroupId, vars.getLanguage());
      if (data != null && data.length > 0)
        strPosition = GeneralLedgerJournalData.selectCountDirect2(this,
            Utility.getContext(this, vars, "#User_Client", "GeneralLedgerJournal"),
            Utility.getContext(this, vars, "#AccessibleOrgTree", "GeneralLedgerJournal"),
            strFactAcctGroupId, data[0].dateacct, data[0].identifier);
    }
    if (data == null || data.length == 0) {
      String discard[] = { "secTable" };
      // toolbar
      // .prepareRelationBarTemplate(false, false,
      // "submitCommandForm('XLS', false, null, 'GeneralLedgerJournal.xls', 'EXCEL');return
      // false;");
      toolbar.prepareRelationBarTemplate(false, false, "imprimirXLS();return false;");

      xmlDocument = xmlEngine
          .readXmlTemplate("pe/com/unifiedgo/report/ad_reports/GeneralLedgerJournal", discard)
          .createXmlDocument();
      data = GeneralLedgerJournalData.set("0");
      data[0].rownum = "0";
    } else {
      data = notshow(data, vars);
      boolean hasPrevious = !(data == null || data.length == 0 || initRecordNumber <= 1);
      boolean hasNext = !(data == null || data.length == 0 || lastRecordNumber >= totalAcctEntries);
      // toolbar
      // .prepareRelationBarTemplate(true, true,
      // "submitCommandForm('XLS', false, null, 'GeneralLedgerJournal.xls', 'EXCEL');return
      // false;");
      toolbar.prepareRelationBarTemplate(true, true, "imprimirXLS();return false;");
      xmlDocument = xmlEngine
          .readXmlTemplate("pe/com/unifiedgo/report/ad_reports/GeneralLedgerJournal")
          .createXmlDocument();

      String jsDisablePreviousNext = "function checkPreviousNextButtons(){";
      if (!hasPrevious)
        jsDisablePreviousNext += "disableToolBarButton('linkButtonPrevious');";
      if (!hasNext)
        jsDisablePreviousNext += "disableToolBarButton('linkButtonNext');";
      jsDisablePreviousNext += "}";
      xmlDocument.setParameter("jsDisablePreviousNext", jsDisablePreviousNext);
    }
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "C_DocType DocBaseType", "",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "GeneralLedgerJournal"),
          Utility.getContext(this, vars, "#User_Client", "GeneralLedgerJournal"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "GeneralLedgerJournal",
          strDocument);
      xmlDocument.setData("reportDocument", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    xmlDocument.setParameter("toolbar", toolbar.toString());
    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_reports.GeneralLedgerJournal");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "GeneralLedgerJournal.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "GeneralLedgerJournal.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("GeneralLedgerJournal");
      vars.removeMessage("GeneralLedgerJournal");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("calendar", vars.getLanguage().substring(0, 2));
    xmlDocument.setParameter("document", strDocument);
    xmlDocument.setParameter("cAcctschemaId", strcAcctSchemaId);
    xmlDocument.setParameter("cAcctschemas", accShemas);
    xmlDocument.setParameter("posted", strPosted);
    xmlDocument.setParameter("paramDescLibroElectrMov",
        !strDescLibroElectrMov.equals("Y") ? "0" : "1");
    xmlDocument.setParameter("paramDescLibroElectrPlan",
        !strDescLibroElectrPlan.equals("Y") ? "0" : "1");

    try {
      // ComboTableData comboTableData = new ComboTableData(vars, this,
      // "TABLEDIR", "AD_ORG_ID", "",
      // "0C754881EAD94243A161111916E9B9C6", Utility.getContext(
      // this, vars, "#AccessibleOrgTree",
      // "GeneralLedgerJournal"), Utility.getContext(this,
      // vars, "#User_Client", "GeneralLedgerJournal"), '*');
      // comboTableData.fillParameters(null, "GeneralLedgerJournal", "");
      // xmlDocument.setData("reportAD_ORGID", "liststructure",
      // comboTableData.select(false));

      ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_Org_ID", "",
          "0C754881EAD94243A161111916E9B9C6",
          Utility.getContext(this, vars, "#AccessibleOrgTree", "GeneralLedgerJournal"),
          Utility.getContext(this, vars, "#User_Client", "GeneralLedgerJournal"), 0);

      Utility.fillSQLParameters(this, vars, null, comboTableData, "GeneralLedgerJournal", strOrg);
      xmlDocument.setData("reportAD_ORGID", "liststructure", comboTableData.select(false));
      comboTableData = null;

    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    // ////////////para periodos
    GeneralLedgerJournalData[] datax = GeneralLedgerJournalData.select_periodos(this);
    FieldProvider periodos[] = new FieldProvider[datax.length];
    Vector<Object> vector = new Vector<Object>(0);

    for (int i = 0; i < datax.length; i++) {
      SQLReturnObject sqlReturnObject = new SQLReturnObject();
      sqlReturnObject.setData("ID", datax[i].idperiodo);
      sqlReturnObject.setData("NAME", datax[i].periodo);
      vector.add(sqlReturnObject);
    }
    vector.copyInto(periodos);

    xmlDocument.setData("reportC_PERIODO", "liststructure", periodos);

    // ///////////// fin para periodos

    xmlDocument.setData("reportC_ACCTSCHEMA_ID", "liststructure",
        AccountingSchemaMiscData.selectC_ACCTSCHEMA_ID(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", "GeneralLedgerJournal"),
            Utility.getContext(this, vars, "#User_Client", "GeneralLedgerJournal"),
            strcAcctSchemaId));
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("paramLanguage", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("adOrgId", strOrg);
    xmlDocument.setParameter("groupId", strPosition);
    xmlDocument.setParameter("paramRecord", strRecord);
    xmlDocument.setParameter("paramTable", strTable);
    vars.setSessionValue("GeneralLedgerJournal|Record", strRecord);
    vars.setSessionValue("GeneralLedgerJournal|Table", strTable);
    xmlDocument.setParameter("inpPageNo", strPageNo);
    xmlDocument.setParameter("inpEntryNo", strEntryNo);
    // If none of the "show" flags is active, then regular is checked
    xmlDocument.setParameter("showRegular", ("".equals(strShowRegular)) ? "N" : strShowRegular);
    xmlDocument.setParameter("showClosing", ("".equals(strShowClosing)) ? "N" : strShowClosing);
    xmlDocument.setParameter("showReg", ("".equals(strShowReg)) ? "N" : strShowReg);
    xmlDocument.setParameter("showOpening", ("".equals(strShowOpening)) ? "N" : strShowOpening);
    xmlDocument.setParameter("showDivideUp", ("".equals(strShowDivideUp)) ? "N" : strShowDivideUp);
    xmlDocument.setParameter("showDescription",
        ("".equals(strShowDescription)) ? "N" : strShowDescription);

    xmlDocument.setParameter("paramPeriodosArray",
        Utility.arrayInfinitasEntradas("idperiodo;periodo;fechainicial;fechafinal;idorganizacion",
            "arrPeriodos", GeneralLedgerJournalData.select_periodos(this)));

    xmlDocument.setParameter("periodoId", strPeriodoId);

    xmlDocument.setParameter("paramLibrosArray",
        Utility.arrayInfinitasEntradas("idlibro;nombrelibro;idorganizacion", "arrLibroMayor",
            GeneralLedgerJournalData.select_libros(this)));

    if (isFirstLoad) {
      xmlDocument.setParameter("FirstLoad", "FirstLoad = \"YES\"; ");
    } else {
      xmlDocument.setParameter("FirstLoad", "FirstLoad = \"NO\"; ");
    }

    xmlDocument.setData("structure1", data);
    out.println(xmlDocument.print());
    out.close();
  }

  private GeneralLedgerJournalData[] notshow(GeneralLedgerJournalData[] data,
      VariablesSecureApp vars) {
    for (int i = 0; i < data.length - 1; i++) {
      if ((data[i].identifier.toString().equals(data[i + 1].identifier.toString()))
          && (data[i].dateacct.toString().equals(data[i + 1].dateacct.toString()))) {
        data[i + 1].newstyle = "visibility: hidden";
      }
    }
    return data;
  }

  public static StructPle getPLE(ConnectionProvider conn, VariablesSecureApp vars,
      String adUserClient, String adUserOrg, String strDateFrom, String strDateTo,
      String strDocument, String strOrg, String strTable, String strRecord,
      String strFactAcctGroupId, String strcAcctSchemaId, String strShowClosing, String strShowReg,
      String strShowOpening, String strPageNo, String strEntryNo, String strShowDescription,
      String strShowRegular, String strShowDivideUp, String strDescLibroElectrMov,
      String strDescLibroElectrPlan) throws Exception {
    GeneralLedgerJournalData[] data = getData(conn, vars, adUserClient, adUserOrg, strDateFrom,
        strDateTo, strDocument, strOrg, strTable, strRecord, strFactAcctGroupId, strcAcctSchemaId,
        strShowClosing, strShowReg, strShowOpening, strPageNo, strEntryNo, strShowDescription,
        strShowRegular, strShowDivideUp, "");
    StructPle sunatPle = getStringData(conn, vars, data, strDateFrom, strDateTo, strDocument,
        strOrg, strTable, strRecord, strFactAcctGroupId, strcAcctSchemaId, strShowClosing,
        strShowReg, strShowOpening, strPageNo, strEntryNo, strShowDescription, strShowRegular,
        strShowDivideUp, strDescLibroElectrMov, strDescLibroElectrPlan);
    return sunatPle;
  }

  private static GeneralLedgerJournalData[] getData(ConnectionProvider conn,
      VariablesSecureApp vars, String adUserClient, String adUserOrg, String strDateFrom,
      String strDateTo, String strDocument, String strOrg, String strTable, String strRecord,
      String strFactAcctGroupId, String strcAcctSchemaId, String strShowClosing, String strShowReg,
      String strShowOpening, String strPageNo, String strEntryNo, String strShowDescription,
      String strShowRegular, String strShowDivideUp, String strShowAll)
      throws IOException, ServletException {
    GeneralLedgerJournalData[] data = null;

    String[] splitFecha = strDateFrom.split("-");
    String ultimoDiaAnio = "31-12-" + splitFecha[2];
    String tipoPeriodo = "S";

    if (ultimoDiaAnio.equals(strDateFrom) && strDateFrom.equals(strDateTo)) {
      tipoPeriodo = "A";
    }

    String strTreeOrg = TreeData.getTreeOrg(conn, vars.getClient());
    String strOrgFamily = getFamily(conn, strTreeOrg, strOrg);
    if (!strFactAcctGroupId.equals("")) {
      data = GeneralLedgerJournalData.selectDirect2(conn, adUserClient, adUserOrg,
          strFactAcctGroupId, vars.getLanguage());

    } else if (strRecord.equals("")) {
      String strCheck = buildCheck(strShowClosing, strShowReg, strShowOpening, strShowRegular,
          strShowDivideUp);
      if (strShowAll.equalsIgnoreCase("Y")) {
        /* solo cuando hay check de mostrar todo */
        data = GeneralLedgerJournalData.selectAll(conn, "Y".equals("Y") ? "'Y'" : "'N'",
            adUserClient, adUserOrg, strDateFrom, DateTimeData.nDaysAfter(conn, strDateTo, "1"),
            strDocument, strcAcctSchemaId, tipoPeriodo, strOrgFamily, strCheck, vars.getLanguage());
      } else {
        /* para PLE */
        /*
         * data = GeneralLedgerJournalData.select(conn, "Y".equals("Y") ? "'Y'" : "'N'",
         * adUserClient, adUserOrg, strDateFrom, DateTimeData.nDaysAfter(conn, strDateTo, "1"),
         * strDocument, strcAcctSchemaId, tipoPeriodo, strOrgFamily, strCheck, vars.getLanguage());
         */
        data = GeneralLedgerJournalData.selectNew(conn, "Y".equals("Y") ? "'Y'" : "'N'",
            adUserClient, adUserOrg, strDateFrom, strDateTo, strDocument, strcAcctSchemaId,
            tipoPeriodo, strOrgFamily, strCheck, vars.getLanguage());
      }

    } else {
      data = GeneralLedgerJournalData.selectDirect(conn, adUserClient, adUserOrg, strTable,
          strRecord, strcAcctSchemaId, vars.getLanguage());
    }

    for (int i = 0; i < data.length; i++) {
      data[i].t8cod = getCode(data[i].tablename, data[i].issotrx);
    }

    return data;
  }

  private static StructPle getStringData(ConnectionProvider conn, VariablesSecureApp vars,
      GeneralLedgerJournalData[] data, String strDateFrom, String strDateTo, String strDocument,
      String strOrg, String strTable, String strRecord, String strFactAcctGroupId,
      String strcAcctSchemaId, String strShowClosing, String strShowReg, String strShowOpening,
      String strPageNo, String strEntryNo, String strShowDescription, String strShowRegular,
      String strShowDivideUp, String strDescLibroElectrMov, String strDescLibroElectrPlan)
      throws ServletException, IOException {

    StructPle sunatPle = new StructPle();
    sunatPle.numEntries = 0;

    boolean isLEmov = false;
    if (strDescLibroElectrMov.equals("Y")) {
      isLEmov = true;
      String strTreeOrg = TreeData.getTreeOrg(conn, vars.getClient());
      String strOrgFamily = getFamily(conn, strTreeOrg, strOrg);
      data = agregaAsientosRegularizados(data, conn, strOrgFamily, "'" + vars.getClient() + "'",
          strDateFrom, strDateTo);
    }
    boolean isLEplan = false;
    if (strDescLibroElectrPlan.equals("Y")) {
      isLEplan = true;
    }

    if (isLEmov) {

      // Libro electronico
      StringBuffer sb = new StringBuffer();
      // int correlativo = 0;
      // String prevRegNumber = "";
      Organization org = OBDal.getInstance().get(Organization.class, strOrg);
      String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11,
          '0');

      SimpleDateFormat formatterForm = new SimpleDateFormat("dd-MM-yyyy");
      Date dttFrom = null;
      try {
        dttFrom = formatterForm.parse(strDateFrom);
      } catch (Exception ex) {
        System.out.println("Exception: " + strDateFrom);
      }

      SimpleDateFormat dt = new SimpleDateFormat("yyyyMM");
      SimpleDateFormat dt2 = new SimpleDateFormat("dd/MM/yyyy");

      String filename = "LE" + rucAdq + dt.format(dttFrom) + "00050100001";// LERRRRRRRRRRRAAAAMM0006010000OIM1.TXT

      HashMap<String, Integer> hMapRegnumber = new HashMap<String, Integer>();

      for (int i = 0; i < data.length; i++) {

        GeneralLedgerJournalData led = data[i];

        Date dttAcct = null;
        try {
          dttAcct = formatterForm.parse(led.dateacct);
        } catch (Exception ex) {
        }
        String periodoTrib = dt.format(dttAcct) + "00";

        String regnumber = led.nreg;

        String tipoAsiento = "M";
        if (led.factaccttype2.equals("1"))
          tipoAsiento = "A";
        if (led.factaccttype2.equals("5"))
          tipoAsiento = "C";

        if (hMapRegnumber.get(regnumber) != null) {
          Integer correlativo = hMapRegnumber.get(regnumber);
          tipoAsiento = tipoAsiento + SunatUtil.LPad(String.valueOf(correlativo), 5, '0');
          hMapRegnumber.put(regnumber, correlativo + 1);
        } else {
          Integer correlativo = new Integer(0);
          tipoAsiento = tipoAsiento + SunatUtil.LPad(String.valueOf(correlativo), 5, '0');
          hMapRegnumber.put(regnumber, correlativo + 1);
        }
        // prevRegNumber = regnumber;

        // String plancuentas = "01";
        String cuentaContable = led.value;

        String cencos = led.costcenter;
        String monedaOrigen = SunatUtil.LPad(led.currencycode, 3, '0');

        // sunat de mela
        if (monedaOrigen.equals("001"))
          monedaOrigen = "PEN";
        else if (monedaOrigen.equals("002"))
          monedaOrigen = "USD";
        else
          monedaOrigen = "EUR";

        String adicionalinfo = "";
        String tdbp = led.tdbp;
        String rucbp = led.rucbp;
        String tdcomp = led.tdcomp;
        String physicalDocument = led.physicaldocument;

        String cinvoiceid = led.cInvoiceId;

        rucbp = rucbp.replace("-", "");
        if (rucbp.length() > 15) {
          adicionalinfo = adicionalinfo + rucbp;
          rucbp = rucbp.substring(0, 15);
        }

        String nroComprobante = physicalDocument;

        if (tdcomp.equals("50") || tdcomp.equals("52")) {// obtener dua si existiera
          Invoice inv = OBDal.getInstance().get(Invoice.class, cinvoiceid);
          if (inv.getScoDua() != null) {
            physicalDocument = inv.getScoDua().getSCODuanumber();
          } else {
            List<InvoiceLine> lsInvoicelines = inv.getInvoiceLineList();
            for (int jk = 0; jk < lsInvoicelines.size(); jk++) {
              if (lsInvoicelines.get(jk).getSimDua() != null) {
                physicalDocument = lsInvoicelines.get(jk).getSimDua().getSCODuanumber();
                break;
              }
            }
          }
        }

        String nroSerie = "";
        if (physicalDocument.contains("-")) {
          StringTokenizer st = new StringTokenizer(physicalDocument, "-");
          nroSerie = st.nextToken();
          nroComprobante = st.nextToken();
          if (tdcomp.equals("50") || tdcomp.equals("52")) {
            try {
              nroComprobante = st.nextToken();
              nroComprobante = st.nextToken();
            } catch (Exception ex) {
            }
          }
        }

        Date dttVenc = null;
        Date dttTrx = null;
        try {
          dttTrx = formatterForm.parse(led.fecemi);
          dttVenc = formatterForm.parse(led.fecvenc);
        } catch (Exception ex) {
        }

        if (tdcomp != null && !tdcomp.equals("")) {
          int tdcompInt = Integer.parseInt(tdcomp);
          if (tdcompInt == 7 || tdcompInt == 8 || tdcompInt == 87 || tdcompInt == 88
              || tdcompInt == 97 || tdcompInt == 98)
            dttVenc = null;
        }

        if (tdcomp.equals("99")) {// otros
          tdcomp = "00";
          nroComprobante = "00000000";
          nroSerie = "";
          dttVenc = null;
          dttTrx = dttAcct;
        }

        if (nroSerie.equals("") && tdcomp.equals("12"))
          nroSerie = "-";

        if (!nroSerie.equals("") && !tdcomp.equals("50") && !tdcomp.equals("52")
            && !tdcomp.equals("55") && !tdcomp.equals("05"))
          nroSerie = SunatUtil.LPad(nroSerie, 4, '0');
        if (nroSerie.length() > 4 && nroSerie.charAt(0) == '0') {
          int len = nroSerie.length();
          nroSerie = nroSerie.substring(len - 4, len);
        }

        if (tdcomp.equals(""))
          tdcomp = "00";
        if (nroComprobante.equals(""))
          nroComprobante = "00000000";

        nroComprobante = nroComprobante.replace("/", "");
        nroComprobante = nroComprobante.replace("-", "");

        if ((tdcomp.equalsIgnoreCase("01") || tdcomp.equalsIgnoreCase("03")
            || tdcomp.equalsIgnoreCase("04") || tdcomp.equalsIgnoreCase("06")
            || tdcomp.equalsIgnoreCase("07") || tdcomp.equalsIgnoreCase("08")
            || tdcomp.equalsIgnoreCase("09")) && nroComprobante.length() > 8)
          nroComprobante = nroComprobante.substring(nroComprobante.length() - 8,
              nroComprobante.length());

        if ((tdcomp.equalsIgnoreCase("02")) && nroComprobante.length() > 7)
          nroComprobante = nroComprobante.substring(nroComprobante.length() - 7,
              nroComprobante.length());

        if ((tdcomp.equalsIgnoreCase("05")) && nroComprobante.length() > 11)
          nroComprobante = nroComprobante.substring(nroComprobante.length() - 11,
              nroComprobante.length());

        if (nroComprobante.length() > 15)
          nroComprobante = nroComprobante.substring(0, 15);

        String fechaOp = dt2.format(dttTrx);
        String fechaContable = dt2.format(dttAcct);
        String fechaVenc = "";

        if (dttVenc != null)
          fechaVenc = dt2.format(dttVenc);

        String glosa = led.description;
        if (glosa.length() > 100)
          glosa = glosa.substring(0, 100);

        glosa = glosa.replace("\n", "");
        glosa = glosa.replace("|", "");

        NumberFormat formatter = new DecimalFormat("#0.00");

        String debe = formatter.format((led.amtacctdr != null && !led.amtacctdr.equals(""))
            ? Double.parseDouble(led.amtacctdr) : 0.00);
        String haber = formatter.format((led.amtacctcr != null && !led.amtacctcr.equals(""))
            ? Double.parseDouble(led.amtacctcr) : 0.00);

        String estadoOp = led.operationStatus == null ? "1" : led.operationStatus;
        SimpleDateFormat formatterYYMM = new SimpleDateFormat("yyyy/MM");
        /*
         * try { Date dateAcctf = formatterYYMM.parse( (dttAcct.getYear()+1900) + "/" +
         * dttAcct.getMonth()); Date dateTrxf = formatterYYMM.parse( (dttTrx.getYear()+1900) + "/" +
         * dttTrx.getMonth()); if (dateTrxf.compareTo(dateAcctf) < 0){ estadoOp = "8";
         * 
         * } } catch (Exception e) { }
         */

        tipoAsiento = led.correlativo;

        String linea = periodoTrib + "|" + regnumber + "|" + tipoAsiento + "|" + cuentaContable
            + "||" + cencos + "|" + monedaOrigen + "|" + tdbp + "|" + rucbp + "|" + tdcomp + "|"
            + nroSerie + "|" + nroComprobante + "|" + fechaContable + "|" + fechaVenc + "|"
            + fechaOp + "|" + glosa + "||" + debe + "|" + haber + "||" + estadoOp + "|"
            + adicionalinfo + "|";
        if (i > 0)
          sb.append("\n");
        sb.append(linea);
        sunatPle.numEntries++;

      }

      if (sunatPle.numEntries > 0) {
        filename = filename + "111.TXT";
      } else {
        filename = filename + "011.TXT";
      }

      sunatPle.filename = filename;
      sunatPle.data = sb.toString();
      return sunatPle;

    } else if (isLEplan) {

      // Libro electronico
      Organization org = OBDal.getInstance().get(Organization.class, strOrg);

      OBCriteria<ElementValue> evCriteria = OBDal.getInstance().createCriteria(ElementValue.class);
      evCriteria.add(Restrictions.eq(ElementValue.PROPERTY_ORGANIZATION, org));
      evCriteria.addOrderBy(ElementValue.PROPERTY_SEARCHKEY, true);
      List<ElementValue> elementValues = evCriteria.list();

      StringBuffer sb = new StringBuffer();
      String rucAdq = SunatUtil.LPad(org.getOrganizationInformationList().get(0).getTaxID(), 11,
          '0');

      SimpleDateFormat formatterForm = new SimpleDateFormat("dd-MM-yyyy");
      Date dttFrom = null;
      Date dttTo = null;

      try {
        dttFrom = formatterForm.parse(strDateFrom);
        dttTo = formatterForm.parse(strDateTo);
      } catch (Exception ex) {
        System.out.println("Exception: " + strDateFrom);
      }

      SimpleDateFormat dt = new SimpleDateFormat("yyyyMM");
      SimpleDateFormat dt2 = new SimpleDateFormat("dd/MM/yyyy");
      SimpleDateFormat dt3 = new SimpleDateFormat("yyyyMMdd");

      String filename = "LE" + rucAdq + dt.format(dttFrom) + "00050300001111.TXT";// LERRRRRRRRRRRAAAAMM0006010000OIM1.TXT

      String periodoTrib = dt3.format(dttFrom);
      int initial = 0;
      for (int i = 0; i < elementValues.size(); i++) {

        ElementValue led = elementValues.get(i);

        String numCuenta = led.getSearchKey();
        if (numCuenta.length() < 3)
          continue;
        if (!StringUtils.isNumeric(numCuenta))
          continue;

        String cuentaDescription = led.getName().replace('/', ' ');
        String plancuentas = "01";
        String plancuentasDescription = "-";
        String indOp = "1";

        String linea = periodoTrib + "|" + numCuenta + "|" + cuentaDescription + "|" + plancuentas
            + "|" + plancuentasDescription + "|||" + indOp + "|";
        if (initial > 0)
          sb.append("\n");
        initial = 1;
        sb.append(linea);
        sunatPle.numEntries++;
      }

      sunatPle.filename = filename;
      sunatPle.data = sb.toString();
      return sunatPle;
    }

    return null;
  }

  private static GeneralLedgerJournalData[] agregaAsientosRegularizados(
      GeneralLedgerJournalData[] data, ConnectionProvider conn, String organizacion, String cliente,
      String strDateFrom, String strDateTo) throws ServletException {

    HashMap<String, ArrayList<GeneralLedgerJournalData>> grupoFactsRegularizados = new HashMap<>();
    HashMap<String, ArrayList<GeneralLedgerJournalData>> grupoFactsOriginales = new HashMap<>();

    GeneralLedgerJournalData[] dataFactsRegularizados = GeneralLedgerJournalData
        .selectFactsARegularizar(conn, cliente, organizacion, strDateFrom, strDateTo);
    GeneralLedgerJournalData[] dataFactsOriginales = GeneralLedgerJournalData
        .selectFactsOriginales(conn, cliente, organizacion, strDateFrom, strDateTo);

    ArrayList<GeneralLedgerJournalData> newData = new ArrayList<>(Arrays.asList(data));

    for (int i = 0; i < dataFactsRegularizados.length; i++) {
      String key = dataFactsRegularizados[i].id;
      ArrayList<GeneralLedgerJournalData> facts = new ArrayList<>();
      if (grupoFactsRegularizados.containsKey(key)) {
        facts = grupoFactsRegularizados.get(key);
      }
      facts.add(dataFactsRegularizados[i]);
      grupoFactsRegularizados.put(dataFactsRegularizados[i].id, facts);
    }

    for (int i = 0; i < dataFactsOriginales.length; i++) {
      String key = dataFactsOriginales[i].id;
      ArrayList<GeneralLedgerJournalData> facts = new ArrayList<>();
      if (grupoFactsOriginales.containsKey(key)) {
        facts = grupoFactsOriginales.get(key);
      }
      facts.add(dataFactsOriginales[i]);
      grupoFactsOriginales.put(dataFactsOriginales[i].id, facts);
    }

    ArrayList<String> listRecordIds = new ArrayList<String>(grupoFactsOriginales.keySet());
    for (int i = 0; i < listRecordIds.size(); i++) {
      String key = listRecordIds.get(i);
      newData.addAll(procesaFactsRegularizados(grupoFactsRegularizados.get(key),
          grupoFactsOriginales.get(key)));
    }

    return newData.toArray(new GeneralLedgerJournalData[newData.size()]);
  }

  private static ArrayList<GeneralLedgerJournalData> procesaFactsRegularizados(
      ArrayList<GeneralLedgerJournalData> fRegularizados,
      ArrayList<GeneralLedgerJournalData> fOriginales) {

    HashMap<String, GeneralLedgerJournalData> grupoFactsPK = new HashMap<>();

    if (fRegularizados != null) {
      Iterator<GeneralLedgerJournalData> itr = fRegularizados.iterator();
      while (itr.hasNext()) {
        GeneralLedgerJournalData fr = itr.next();
        String key = fr.dateacct + "||" + fr.nreg + "||" + fr.correlativo + "||" + fr.value + "||"
            + fr.physicaldocument;
        fr.operationStatus = "8";
        grupoFactsPK.put(key, fr);
      }
    }

    if (fOriginales != null) {
      Iterator<GeneralLedgerJournalData> itr = fOriginales.iterator();
      while (itr.hasNext()) {
        GeneralLedgerJournalData fo = itr.next();
        String key = fo.dateacct + "||" + fo.nreg + "||" + fo.correlativo + "||" + fo.value + "||"
            + fo.physicaldocument;
        if (grupoFactsPK.containsKey(key)) {
          grupoFactsPK.get(key).operationStatus = "9";
        } else {
          fo.operationStatus = "9";
          fo.amtacctdr = "0.00";
          fo.amtacctcr = "0.00";
          grupoFactsPK.put(key, fo);
        }
      }
    }
    return new ArrayList<GeneralLedgerJournalData>(grupoFactsPK.values());
  }

  public void printPagePDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strDateFrom, String strDateTo, String strDocument,
      String strOrg, String strTable, String strRecord, String strFactAcctGroupId,
      String strcAcctSchemaId, String strShowClosing, String strShowReg, String strShowOpening,
      String strPageNo, String strEntryNo, String strShowDescription, String strShowRegular,
      String strShowDivideUp, String strDescLibroElectrMov, String strDescLibroElectrPlan,
      String strShowAll) throws IOException, ServletException {

    GeneralLedgerJournalData[] data = getData(this, vars,
        Utility.getContext(this, vars, "#User_Client", "GeneralLedgerJournal"),
        Utility.getContext(this, vars, "#AccessibleOrgTree", "GeneralLedgerJournal"), strDateFrom,
        strDateTo, strDocument, strOrg, strTable, strRecord, strFactAcctGroupId, strcAcctSchemaId,
        strShowClosing, strShowReg, strShowOpening, strPageNo, strEntryNo, strShowDescription,
        strShowRegular, strShowDivideUp, strShowAll);

    String strSubtitle = (Utility.messageBD(this, "LegalEntity", vars.getLanguage()) + ": ")
        + GeneralLedgerJournalData.selectCompany(this, vars.getClient()) + "\n";

    if (!("0".equals(strOrg)))
      strSubtitle += (Utility.messageBD(this, "OBUIAPP_Organization", vars.getLanguage()) + ": ")
          + GeneralLedgerJournalData.selectOrg(this, strOrg) + "\n";

    if (!"".equals(strDateFrom) || !"".equals(strDateTo))
      strSubtitle += (Utility.messageBD(this, "From", vars.getLanguage()) + ": ") + strDateFrom
          + "  " + (Utility.messageBD(this, "OBUIAPP_To", vars.getLanguage()) + ": ") + strDateTo
          + "\n";

    if (!"".equals(strcAcctSchemaId)) {
      AcctSchema financialMgmtAcctSchema = OBDal.getInstance().get(AcctSchema.class,
          strcAcctSchemaId);
      strSubtitle += Utility.messageBD(this, "generalLedger", vars.getLanguage()) + ": "
          + financialMgmtAcctSchema.getName();
    }

    boolean isLEmov = false;
    if (strDescLibroElectrMov.equals("Y")) {
      isLEmov = true;
    }
    boolean isLEplan = false;
    if (strDescLibroElectrPlan.equals("Y")) {
      isLEplan = true;
    }

    if (isLEmov || isLEplan) {
      StructPle sunatPle = getStringData(this, vars, data, strDateFrom, strDateTo, strDocument,
          strOrg, strTable, strRecord, strFactAcctGroupId, strcAcctSchemaId, strShowClosing,
          strShowReg, strShowOpening, strPageNo, strEntryNo, strShowDescription, strShowRegular,
          strShowDivideUp, strDescLibroElectrMov, strDescLibroElectrPlan);
      response.setContentType("text/plain");
      response.setHeader("Content-Disposition", "attachment;filename=" + sunatPle.filename);

      ServletOutputStream os = response.getOutputStream();

      os.write(sunatPle.data.getBytes());

      os.flush();
      os.close();
    } else {
      int correlativo = 0;
      String prevRegNumber = "";

      for (int i = 0; i < data.length; i++) {

        GeneralLedgerJournalData led = data[i];
        String regnumber = data[i].nreg;
        String tipoAsiento = "M";
        if (led.factaccttype2.equals("1"))
          tipoAsiento = "A";
        if (led.factaccttype2.equals("5"))
          tipoAsiento = "C";

        if (prevRegNumber.equals(regnumber)) {
          tipoAsiento = tipoAsiento + SunatUtil.LPad(String.valueOf(correlativo), 5, '0');
          correlativo++;
        } else {
          tipoAsiento = tipoAsiento + "00000";
          correlativo = 1;
        }
        prevRegNumber = regnumber;

        data[i].nombrelibro = led.seqno; // tipoAsiento;
      }

      /* PDF */
      String strOutput;
      String strReportName;
      if (vars.commandIn("PDF")) {
        strOutput = "pdf";
        strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/GeneralLedgerJournalCOAM.jrxml";
      } else {
        strOutput = "xls";
        strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/GeneralLedgerJournalExcel.jrxml";
      }

      HashMap<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("ShowDescription", strShowDescription);

      parameters.put("dateFrom", StringToDate(strDateFrom));
      parameters.put("dateTo", StringToDate(strDateTo));
      parameters.put("Subtitle", strSubtitle);
      parameters.put("PageNo", strPageNo);
      parameters.put("Ruc", GeneralLedgerJournalData.selectRucOrg(this, strOrg));
      parameters.put("Razon", GeneralLedgerJournalData.selectOrg(this, strOrg));
      parameters.put("InitialEntryNumber", strEntryNo);
      parameters.put("TaxID", GeneralLedgerJournalData.selectOrgTaxID(this, strOrg));

      if (data.length == 0) {
        advisePopUp(request, response, "WARNING",
            Utility.messageBD(this, "ProcessStatus-W", vars.getLanguage()),
            Utility.messageBD(this, "NoDataFound", vars.getLanguage()));
        return;
      }
      renderJR(vars, response, strReportName, "Libro Diario", strOutput, parameters, data, null);
    }

  }

  static private String getFamily(ConnectionProvider conn, String strTree, String strChild)
      throws IOException, ServletException {
    return Tree.getMembers(conn, strTree,
        (strChild == null || strChild.equals("")) ? "0" : strChild);
    /*
     * ReportGeneralLedgerData [] data = ReportGeneralLedgerData.selectChildren(this, strTree,
     * strChild); String strFamily = ""; if(data!=null && data.length>0) { for (int i =
     * 0;i<data.length;i++){ if (i>0) strFamily = strFamily + ","; strFamily = strFamily +
     * data[i].id; } return strFamily += ""; }else return "'1'";
     */
  }

  private Date StringToDate(String strDate) {
    SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

    Date date;
    try {
      if (!strDate.equals("")) {
        date = formatter.parse(strDate);
        return date;
      }
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  static private String buildCheck(String strShowClosing, String strShowReg, String strShowOpening,
      String strShowRegular, String strShowDivideUp) {
    String[] strElements = { strShowClosing.equals("Y") ? "'C'" : "",
        strShowReg.equals("Y") ? "'R'" : "", strShowOpening.equals("Y") ? "'O'" : "",
        strShowRegular.equals("Y") ? "'N'" : "", strShowDivideUp.equals("Y") ? "'D'" : "" };
    int no = 0;
    String strCheck = "";
    for (int i = 0; i < strElements.length; i++) {
      if (!strElements[i].equals("")) {
        if (no != 0)
          strCheck = strCheck + ", ";
        strCheck = strCheck + strElements[i];
        no++;
      }
    }
    return strCheck;
  }

  private <T extends BaseOBObject> String getJSONComboBox(Set<String> docbseTypes,
      String selectedValue, boolean isMandatory, VariablesSecureApp vars) {

    JSONObject json = new JSONObject();
    JSONArray select = new JSONArray();
    Map<String, String> attr = null;
    try {
      int i = 0;
      if (!isMandatory) {
        attr = new HashMap<String, String>();
        attr.put("value", "");
        attr.put("selected", "false");
        attr.put("text", "");
        select.put(i, attr);
        i++;
      }
      for (String dbt : docbseTypes) {
        attr = new HashMap<String, String>();
        attr.put("value", dbt);
        attr.put("selected", (dbt.equals(selectedValue)) ? "true" : "false");
        attr.put("text",
            Utility.getListValueName("C_DocType DocBaseType", dbt, vars.getLanguage()));
        select.put(i, attr);
        json.put("optionlist", select);
        i++;
      }
      json.put("ismandatory", String.valueOf(isMandatory));

    } catch (JSONException e) {
      log4j.error("Error creating JSON object for representing subaccount lines", e);
    }

    return json.toString();
  }

  public static Set<String> getDocuments(String org, String accSchema) {

    final StringBuilder whereClause = new StringBuilder();
    final List<Object> parameters = new ArrayList<Object>();
    OBContext.setAdminMode();
    try {
      // Set<String> orgStrct =
      // OBContext.getOBContext().getOrganizationStructureProvider()
      // .getChildTree(org, true);
      Set<String> orgStrct = OBContext.getOBContext().getOrganizationStructureProvider()
          .getNaturalTree(org);
      whereClause.append(" as cd ,");
      whereClause.append(AcctSchemaTable.ENTITY_NAME);
      whereClause.append(" as ca ");
      whereClause.append(" where cd.");
      whereClause.append(DocumentType.PROPERTY_TABLE + ".id");
      whereClause.append("= ca.");
      whereClause.append(AcctSchemaTable.PROPERTY_TABLE + ".id");
      whereClause.append(" and ca.");
      whereClause.append(AcctSchemaTable.PROPERTY_ACCOUNTINGSCHEMA + ".id");
      whereClause.append(" = ? ");
      parameters.add(accSchema);
      whereClause.append("and ca.");
      whereClause.append(AcctSchemaTable.PROPERTY_ACTIVE + "='Y'");
      whereClause.append(" and cd.");
      whereClause.append(DocumentType.PROPERTY_ORGANIZATION + ".id");
      whereClause.append(" in (" + Utility.getInStrSet(orgStrct) + ")");
      whereClause.append(" and ca." + AcctSchemaTable.PROPERTY_ORGANIZATION + ".id");
      whereClause.append(" in (" + Utility.getInStrSet(orgStrct) + ")");
      whereClause.append(" order by cd." + DocumentType.PROPERTY_DOCUMENTCATEGORY);
      final OBQuery<DocumentType> obqDt = OBDal.getInstance().createQuery(DocumentType.class,
          whereClause.toString());
      obqDt.setParameters(parameters);
      obqDt.setFilterOnReadableOrganization(false);
      TreeSet<String> docBaseTypes = new TreeSet<String>();
      for (DocumentType doc : obqDt.list()) {
        docBaseTypes.add(doc.getDocumentCategory());
      }
      return docBaseTypes;

    } finally {
      OBContext.restorePreviousMode();
    }

  }

  @Override
  public String getServletInfo() {
    return "Servlet GeneralLedgerJournal. This Servlet was made by Jose Valdez modified by everybody";
  } // end of getServletInfo() method

  static private String getCode(String tabla, String issotrx) {

    if (tabla.equalsIgnoreCase("c_invoice")) {
      if (issotrx.equalsIgnoreCase("N"))
        return "8";// Libro Compras
      else if (issotrx.equalsIgnoreCase("Y"))
        return "14";// Libro Ventas
    } else if (tabla.equalsIgnoreCase("m_inout") || tabla.equalsIgnoreCase("m_inventory")
        || tabla.equalsIgnoreCase("m_movement") || tabla.equalsIgnoreCase("m_production")
        || tabla.equalsIgnoreCase("swa_movementcode")) {
      return "3";// Libro Inventario
    } else if (tabla.equalsIgnoreCase("fin_finacc_transaction")
        || tabla.equalsIgnoreCase("fin_payment")) {
      return "1";// Libro Caja Bancos
    }
    return "5";

  }
}
