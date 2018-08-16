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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.AccountTree;
import org.openbravo.erpCommon.businessUtility.AccountTreeData;
import org.openbravo.erpCommon.businessUtility.AccountingSchemaMiscData;
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DateTimeData;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.JRFieldProviderDataSource;
import org.openbravo.erpCommon.utility.LeftTabsBar;
import org.openbravo.erpCommon.utility.NavigationBar;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.ToolBar;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.erpCommon.utility.WindowTreeData;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.accounting.OrganizationClosing;
import org.openbravo.model.financialmgmt.calendar.Calendar;
import org.openbravo.model.financialmgmt.calendar.Year;
import org.openbravo.xmlEngine.XmlDocument;

public class GeneralAccounting extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  private static final String C_ELEMENT_VALUE_TABLE_ID = "188";

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);

    if (vars.commandIn("DEFAULT")) {
      String strDateFrom = vars.getGlobalVariable("inpDateFrom", "GeneralAccounting|dateFrom", "");
      String strDateTo = vars.getGlobalVariable("inpDateTo", "GeneralAccounting|dateTo", "");
      String strDateFromRef = vars.getGlobalVariable("inpDateFromRef",
          "GeneralAccounting|dateFromRef", "");
      String strDateToRef = vars.getGlobalVariable("inpDateToRef", "GeneralAccounting|dateToRef",
          "");
      String strAsDateTo = vars.getGlobalVariable("inpAsDateTo", "GeneralAccounting|asDateTo", "");
      String strAsDateToRef = vars.getGlobalVariable("inpAsDateToRef",
          "GeneralAccounting|asDateToRef", "");
      String strPageNo = vars.getGlobalVariable("inpPageNo", "GeneralAccounting|PageNo", "1");
      String strElementValue = vars.getGlobalVariable("inpcElementvalueId",
          "GeneralAccounting|C_ElementValue_ID", "");
      String strConImporte = vars.getGlobalVariable("inpConImporte",
          "GeneralAccounting|conImporte", "N");
      String strConCodigo = vars.getGlobalVariable("inpConCodigo", "GeneralAccounting|conCodigo",
          "N");
      String strLevel = vars.getGlobalVariable("inpLevel", "GeneralAccounting|level", "");
      printPageDataSheet(response, vars, "", "", strDateFrom, strDateTo, strPageNo, strDateFromRef,
          strDateToRef, strAsDateTo, strAsDateToRef, strElementValue, strConImporte, "", strLevel,
          strConCodigo, "");
    } else if (vars.commandIn("FIND")) {
      String strcAcctSchemaId = vars.getStringParameter("inpcAcctSchemaId", "");
      String strAgno = vars.getRequiredGlobalVariable("inpAgno", "GeneralAccounting|agno");
      String strAgnoRef = vars.getRequiredGlobalVariable("inpAgnoRef", "GeneralAccounting|agnoRef");
      String strDateFrom = vars.getRequestGlobalVariable("inpDateFrom",
          "GeneralAccounting|dateFrom");
      String strDateTo = vars.getRequestGlobalVariable("inpDateTo", "GeneralAccounting|dateTo");
      String strDateFromRef = vars.getRequestGlobalVariable("inpDateFromRef",
          "GeneralAccounting|dateFromRef");
      String strDateToRef = vars.getRequestGlobalVariable("inpDateToRef",
          "GeneralAccounting|dateToRef");
      String strPageNo = vars.getRequestGlobalVariable("inpPageNo", "GeneralAccounting|PageNo");
      String strAsDateTo = vars.getRequestGlobalVariable("inpAsDateTo",
          "GeneralAccounting|asDateTo");
      String strAsDateToRef = vars.getRequestGlobalVariable("inpAsDateToRef",
          "GeneralAccounting|asDateToRef");
      String strElementValue = vars.getRequiredGlobalVariable("inpcElementvalueId",
          "GeneralAccounting|C_ElementValue_ID");
      String strConImporte = vars.getRequestGlobalVariable("inpConImporte",
          "GeneralAccounting|conImporte");
      String strConCodigo = vars.getRequestGlobalVariable("inpConCodigo",
          "GeneralAccounting|conCodigo");
      String strOrg = vars.getRequestGlobalVariable("inpOrganizacion",
          "GeneralAccounting|organizacion");
      String strLevel = vars.getRequestGlobalVariable("inpLevel", "GeneralAccounting|level");
      printPagePDFBalanceGeneral(request, response, vars, strAgno, strAgnoRef, strDateFrom,
          strDateTo, strDateFromRef, strDateToRef, strAsDateTo, strAsDateToRef, strElementValue,
          strConImporte, strOrg, strLevel, strConCodigo, strcAcctSchemaId, strPageNo);

    } else
      pageError(response);
  }

  private void printPagePDF(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strYearId, String strYearRefId, String strDateFrom,
      String strDateTo, String strDateFromRef, String strDateToRef, String strAsDateTo,
      String strAsDateToRef, String strElementValue, String strConImporte, String strOrg,
      String strLevel, String strConCodigo, String strcAcctSchemaId, String strPageNo)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: pdf");

    String strCalculateOpening = strElementValue.substring(0, 1);
    strElementValue = strElementValue.substring(1, strElementValue.length());
    GeneralAccountingData[] strGroups = GeneralAccountingData.selectGroups(this, strElementValue);

    try {
      strGroups[strGroups.length - 1].pagebreak = "";

      String[][] strElementValueDes = new String[strGroups.length][];
      if (log4j.isDebugEnabled())
        log4j.debug("strElementValue:" + strElementValue + " - strGroups.length:"
            + strGroups.length);

      // System.out.println("strElementValue:" + strElementValue + " - strGroups.length:"
      // + strGroups.length);

      for (int i = 0; i < strGroups.length; i++) {
        GeneralAccountingData[] strElements = GeneralAccountingData.selectElements(this,
            strGroups[i].id);
        strElementValueDes[i] = new String[strElements.length];

        if (log4j.isDebugEnabled())
          log4j.debug("strElements.length:" + strElements.length);

        for (int j = 0; j < strElements.length; j++) {
          strElementValueDes[i][j] = strElements[j].id;
        }
      }

      String strTreeOrg = GeneralAccountingData.treeOrg(this, vars.getClient());
      AccountTree[] acct = new AccountTree[strGroups.length];

      AccountTreeData[][] elements = new AccountTreeData[strGroups.length][];

      WindowTreeData[] dataTree = WindowTreeData.selectTreeIDWithTableId(this,
          Utility.stringList(vars.getClient()), C_ELEMENT_VALUE_TABLE_ID);
      String TreeID = "";
      if (dataTree != null && dataTree.length != 0)
        TreeID = dataTree[0].id;
      OBContext.setAdminMode(false);
      try {
        String openingEntryOwner = "";
        String openingEntryOwnerRef = "";
        // For each year, the initial and closing date is obtained
        Year year = OBDal.getInstance().get(Year.class, strYearId);
        Year yearRef = OBDal.getInstance().get(Year.class, strYearRefId);
        HashMap<String, Date> startingEndingDate = getStartingEndingDate(year);
        HashMap<String, Date> startingEndingDateRef = getStartingEndingDate(yearRef);
        // Years to be included as no closing is present
        String strYearsToClose = "";
        String strYearsToCloseRef = "";
        if (strCalculateOpening.equals("Y")) {
          strCalculateOpening = "N";
          strDateTo = strAsDateTo;
          strDateToRef = strAsDateToRef;
          strDateFrom = "";
          strDateFromRef = "";
          String[] yearsInfo = getYearsToClose(startingEndingDate.get("startingDate"), strOrg,
              year.getCalendar(), strcAcctSchemaId, false);
          strYearsToClose = yearsInfo[0];
          openingEntryOwner = yearsInfo[1];
          if (strYearsToClose.length() > 0) {
            strCalculateOpening = "Y";
            strYearsToClose = "," + strYearsToClose;
          }
          yearsInfo = getYearsToClose(startingEndingDateRef.get("startingDate"), strOrg,
              yearRef.getCalendar(), strcAcctSchemaId, true);
          strYearsToCloseRef = yearsInfo[0];
          openingEntryOwnerRef = yearsInfo[1];
          if (strYearsToCloseRef.length() > 0) {
            strCalculateOpening = "Y";
            strYearsToCloseRef = "," + strYearsToCloseRef;
          }
        }
        // Income summary amount is calculated and included in the balance sheet data
        String strIncomeSummaryAccount = GeneralAccountingData
            .incomesummary(this, strcAcctSchemaId);

        for (int i = 0; i < strGroups.length; i++) {
          // All account tree is obtained
          if (vars.getLanguage().equals("en_US")) {
            elements[i] = AccountTreeData.select(this, strConCodigo, TreeID);
          } else {
            elements[i] = AccountTreeData.selectTrl(this, strConCodigo, vars.getLanguage(), TreeID);
          }
          // For each account with movements in the year, debit and credit total amounts are
          // calculated according to fact_acct movements.
          AccountTreeData[] accounts = AccountTreeData.selectFactAcct(this,
              Utility.getContext(this, vars, "#AccessibleOrgTree", "GeneralAccounting"),
              Utility.getContext(this, vars, "#User_Client", "GeneralAccounting"), strDateFrom,
              DateTimeData.nDaysAfter(this, strDateTo, "1"), strcAcctSchemaId,
              Tree.getMembers(this, strTreeOrg, strOrg), "'" + year.getFiscalYear() + "'"
                  + strYearsToClose, openingEntryOwner, strDateFromRef,
              DateTimeData.nDaysAfter(this, strDateToRef, "1"), "'" + yearRef.getFiscalYear() + "'"
                  + strYearsToCloseRef, openingEntryOwnerRef);
          {
            if (log4j.isDebugEnabled())
              log4j.debug("*********** strIncomeSummaryAccount: " + strIncomeSummaryAccount);
            String strISyear = processIncomeSummary(strDateFrom,
                DateTimeData.nDaysAfter(this, strDateTo, "1"), "'" + year.getFiscalYear() + "'"
                    + strYearsToClose, strTreeOrg, strOrg, strcAcctSchemaId);
            if (log4j.isDebugEnabled())
              log4j.debug("*********** strISyear: " + strISyear);
            String strISyearRef = processIncomeSummary(strDateFromRef,
                DateTimeData.nDaysAfter(this, strDateToRef, "1"), "'" + yearRef.getFiscalYear()
                    + "'" + strYearsToCloseRef, strTreeOrg, strOrg, strcAcctSchemaId);
            if (log4j.isDebugEnabled())
              log4j.debug("*********** strISyearRef: " + strISyearRef);
            accounts = appendRecords(accounts, strIncomeSummaryAccount, strISyear, strISyearRef);

          }
          // Report tree is built with given the account tree, and the amounts obtained from
          // fact_acct
          acct[i] = new AccountTree(vars, this, elements[i], accounts, strElementValueDes[i]);
          if (acct[i] != null) {
            acct[i].filterSVC();
            acct[i].filter(strConImporte.equals("Y"), strLevel, false);
          } else if (log4j.isDebugEnabled())
            log4j.debug("acct null!!!");

          // System.out.println("Elemento" + i + ": " + acct[i]);// lo puse yoooo
        }

        String strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/GeneralAccountingPDF.jrxml";
        HashMap<String, Object> parameters = new HashMap<String, Object>();

        parameters.put("group", strGroups);
        parameters.put("agno", year.getFiscalYear());

        parameters.put("agno2", yearRef.getFiscalYear());
        parameters.put("column", year.getFiscalYear());
        parameters.put("columnRef", yearRef.getFiscalYear());
        parameters.put("org", OrganizationData.selectOrgName(this, strOrg));
        parameters.put("column1", year.getFiscalYear());
        parameters.put("columnRef1", yearRef.getFiscalYear());
        parameters.put("companyName", GeneralAccountingData.companyName(this, vars.getClient()));
        parameters.put("date", DateTimeData.today(this));
        if (strDateFrom.equals(""))
          strDateFrom = OBDateUtils.formatDate(startingEndingDate.get("startingDate"));
        if (strDateTo.equals(""))
          strDateTo = OBDateUtils.formatDate(startingEndingDate.get("endingDate"));
        if (strDateFromRef.equals(""))
          strDateFromRef = OBDateUtils.formatDate(startingEndingDateRef.get("startingDate"));
        if (strDateToRef.equals(""))
          strDateToRef = OBDateUtils.formatDate(startingEndingDateRef.get("endingDate"));
        parameters.put("period", strDateFrom + " - " + strDateTo);
        parameters.put("periodRef", strDateFromRef + " - " + strDateToRef);
        parameters.put("agnoInitial", year.getFiscalYear());
        parameters.put("agnoRef", yearRef.getFiscalYear());

        parameters.put("principalTitle",
            strCalculateOpening.equals("Y") ? GeneralAccountingData.rptTitle(this, strElementValue)
                + " (Provisional)" : GeneralAccountingData.rptTitle(this, strElementValue));

        parameters.put("pageNo", strPageNo);

        AccountTreeData[][] trees = new AccountTreeData[strGroups.length][];
        for (int i = 0; i < strGroups.length; i++)
          trees[i] = acct[i].getAccounts();

        List<HashMap<String, String>> hashMapList = new ArrayList<HashMap<String, String>>();

        for (int i = 0; i < trees.length; i++) {
          for (int j = 0; j < trees[i].length; j++) {
            HashMap<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("elementLevel", trees[i][j].elementLevel);
            hashMap.put("name", trees[i][j].name);
            hashMap.put("qty", trees[i][j].qty);
            hashMap.put("qtyRef", trees[i][j].qtyRef);
            hashMap.put("groupname", strGroups[i].name);
            hashMap.put("pagebreak", strGroups[i].pagebreak);

            hashMapList.add(hashMap);
          }
        }

        FieldProvider[] data = FieldProviderFactory.getFieldProviderArray(hashMapList);
        renderJR(vars, response, strReportName, "pdf", parameters, data, null);

      } finally {
        OBContext.restorePreviousMode();
      }

    } catch (ArrayIndexOutOfBoundsException e) {
      advisePopUp(request, response, "ERROR",
          Utility.messageBD(this, "ReportWithoutNodes", vars.getLanguage()));
    }
  }

  private void printPagePDFBalanceGeneral(HttpServletRequest request, HttpServletResponse response,
      VariablesSecureApp vars, String strYearId, String strYearRefId, String strDateFrom,
      String strDateTo, String strDateFromRef, String strDateToRef, String strAsDateTo,
      String strAsDateToRef, String strElementValue, String strConImporte, String strOrg,
      String strLevel, String strConCodigo, String strcAcctSchemaId, String strPageNo)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: pdf");

    String strCalculateOpening = strElementValue.substring(0, 1);
    strElementValue = strElementValue.substring(1, strElementValue.length());
    GeneralAccountingData[] strGroups = GeneralAccountingData.selectGroups(this, strElementValue);

    try {
      strGroups[strGroups.length - 1].pagebreak = "";

      String[][] strElementValueDes = new String[strGroups.length][];
      if (log4j.isDebugEnabled())
        log4j.debug("strElementValue:" + strElementValue + " - strGroups.length:"
            + strGroups.length);
      // System.out.println("strElementValue:" + strElementValue + " - strGroups.length:"
      // + strGroups.length);
      for (int i = 0; i < strGroups.length; i++) {
        GeneralAccountingData[] strElements = GeneralAccountingData.selectElements(this,
            strGroups[i].id);
        strElementValueDes[i] = new String[strElements.length];

        if (log4j.isDebugEnabled())
          log4j.debug("strElements.length:" + strElements.length);
        // System.out.println("strElements.length:" + strElements.length);
        for (int j = 0; j < strElements.length; j++) {
          strElementValueDes[i][j] = strElements[j].id;
        }
      }

      String strTreeOrg = GeneralAccountingData.treeOrg(this, vars.getClient());
      AccountTree[] acct = new AccountTree[strGroups.length];

      AccountTreeData[][] elements = new AccountTreeData[strGroups.length][];

      WindowTreeData[] dataTree = WindowTreeData.selectTreeIDWithTableId(this,
          Utility.stringList(vars.getClient()), C_ELEMENT_VALUE_TABLE_ID);
      String TreeID = "";
      if (dataTree != null && dataTree.length != 0)
        TreeID = dataTree[0].id;
      OBContext.setAdminMode(false);
      try {
        String openingEntryOwner = "";
        String openingEntryOwnerRef = "";
        // For each year, the initial and closing date is obtained
        Year year = OBDal.getInstance().get(Year.class, strYearId);
        Year yearRef = OBDal.getInstance().get(Year.class, strYearRefId);
        HashMap<String, Date> startingEndingDate = getStartingEndingDate(year);
        HashMap<String, Date> startingEndingDateRef = getStartingEndingDate(yearRef);
        // Years to be included as no closing is present
        String strYearsToClose = "";
        String strYearsToCloseRef = "";
        if (strCalculateOpening.equals("Y")) {
          strCalculateOpening = "N";
          strDateTo = strAsDateTo;
          strDateToRef = strAsDateToRef;
          strDateFrom = "";
          strDateFromRef = "";
          String[] yearsInfo = getYearsToClose(startingEndingDate.get("startingDate"), strOrg,
              year.getCalendar(), strcAcctSchemaId, false);
          strYearsToClose = yearsInfo[0];
          openingEntryOwner = yearsInfo[1];
          if (strYearsToClose.length() > 0) {
            strCalculateOpening = "Y";
            strYearsToClose = "," + strYearsToClose;
          }
          yearsInfo = getYearsToClose(startingEndingDateRef.get("startingDate"), strOrg,
              yearRef.getCalendar(), strcAcctSchemaId, true);
          strYearsToCloseRef = yearsInfo[0];
          openingEntryOwnerRef = yearsInfo[1];
          if (strYearsToCloseRef.length() > 0) {
            strCalculateOpening = "Y";
            strYearsToCloseRef = "," + strYearsToCloseRef;
          }
        }
        // Income summary amount is calculated and included in the balance sheet data
        String strIncomeSummaryAccount = GeneralAccountingData
            .incomesummary(this, strcAcctSchemaId);

        for (int i = 0; i < strGroups.length; i++) {
          // All account tree is obtained
          if (vars.getLanguage().equals("en_US")) {
            // System.out.println("Hace Select");
            elements[i] = AccountTreeData.select(this, strConCodigo, TreeID);
            // System.out.println("elementos:" + elements[i].length);
          } else {
            // System.out.println("Hace SelectTRL");
            elements[i] = AccountTreeData.selectTrl(this, strConCodigo, vars.getLanguage(), TreeID);
            // System.out.println("elementos:" + elements[i].length);
          }
          // For each account with movements in the year, debit and credit total amounts are
          // calculated according to fact_acct movements.
          AccountTreeData[] accounts = AccountTreeData.selectFactAcct(this,
              Utility.getContext(this, vars, "#AccessibleOrgTree", "GeneralAccounting"),
              Utility.getContext(this, vars, "#User_Client", "GeneralAccounting"), strDateFrom,
              DateTimeData.nDaysAfter(this, strDateTo, "1"), strcAcctSchemaId,
              Tree.getMembers(this, strTreeOrg, strOrg), "'" + year.getFiscalYear() + "'"
                  + strYearsToClose, openingEntryOwner, strDateFromRef,
              DateTimeData.nDaysAfter(this, strDateToRef, "1"), "'" + yearRef.getFiscalYear() + "'"
                  + strYearsToCloseRef, openingEntryOwnerRef);

          {
            if (log4j.isDebugEnabled())
              log4j.debug("*********** strIncomeSummaryAccount: " + strIncomeSummaryAccount);
            String strISyear = processIncomeSummary(strDateFrom,
                DateTimeData.nDaysAfter(this, strDateTo, "1"), "'" + year.getFiscalYear() + "'"
                    + strYearsToClose, strTreeOrg, strOrg, strcAcctSchemaId);
            if (log4j.isDebugEnabled())
              log4j.debug("*********** strISyear: " + strISyear);
            String strISyearRef = processIncomeSummary(strDateFromRef,
                DateTimeData.nDaysAfter(this, strDateToRef, "1"), "'" + yearRef.getFiscalYear()
                    + "'" + strYearsToCloseRef, strTreeOrg, strOrg, strcAcctSchemaId);
            if (log4j.isDebugEnabled())
              log4j.debug("*********** strISyearRef: " + strISyearRef);
            accounts = appendRecords(accounts, strIncomeSummaryAccount, strISyear, strISyearRef);

          }
          // Report tree is built with given the account tree, and the amounts obtained from
          // fact_acct
          // System.out.println("accounts tree elements[i]:" + elements[i].length);
          // System.out.println("accounts tree accounts:" + accounts.length);
          // System.out.println("accounts tree strElementValueDes[i]:" +
          // strElementValueDes[i].length);
          acct[i] = new AccountTree(vars, this, elements[i], accounts, strElementValueDes[i]);
          if (acct[i] != null) {
            // System.out.println("acct[i] no  es  null y tiene : " + acct[i].getAccounts().length);
            acct[i].filterSVC();
            acct[i].filter(strConImporte.equals("Y"), strLevel, false);
            // System.out.println("acct[i] no  es  null y tiene : " + acct[i].getAccounts().length);
          } else if (log4j.isDebugEnabled())
            log4j.debug("acct null!!!");

          // System.out.println("Elemento" + i + ": " + acct[i]);// lo puse yoooo
        }

        String strReportName = "@basedesign@/pe/com/unifiedgo/report/ad_reports/GeneralAccountingPDF.jrxml";
        HashMap<String, Object> parameters = new HashMap<String, Object>();

        parameters.put("group", strGroups);
        parameters.put("agno", year.getFiscalYear());

        parameters.put("agno2", yearRef.getFiscalYear());
        parameters.put("column", year.getFiscalYear());
        parameters.put("columnRef", yearRef.getFiscalYear());
        parameters.put("org", OrganizationData.selectOrgName(this, strOrg));
        parameters.put("column1", year.getFiscalYear());
        parameters.put("columnRef1", yearRef.getFiscalYear());
        parameters.put("companyName", GeneralAccountingData.companyName(this, vars.getClient()));
        parameters.put("date", DateTimeData.today(this));
        if (strDateFrom.equals(""))
          strDateFrom = OBDateUtils.formatDate(startingEndingDate.get("startingDate"));
        if (strDateTo.equals(""))
          strDateTo = OBDateUtils.formatDate(startingEndingDate.get("endingDate"));
        if (strDateFromRef.equals(""))
          strDateFromRef = OBDateUtils.formatDate(startingEndingDateRef.get("startingDate"));
        if (strDateToRef.equals(""))
          strDateToRef = OBDateUtils.formatDate(startingEndingDateRef.get("endingDate"));
        parameters.put("period", strDateFrom + " - " + strDateTo);
        parameters.put("periodRef", strDateFromRef + " - " + strDateToRef);
        parameters.put("agnoInitial", year.getFiscalYear());
        parameters.put("agnoRef", yearRef.getFiscalYear());

        parameters.put("principalTitle",
            strCalculateOpening.equals("Y") ? GeneralAccountingData.rptTitle(this, strElementValue)
                + " (Provisional)" : GeneralAccountingData.rptTitle(this, strElementValue));

        parameters.put("pageNo", strPageNo);

        AccountTreeData[][] trees = new AccountTreeData[strGroups.length][];
        // System.out.println("strGroups.length:" + strGroups.length);
        for (int i = 0; i < strGroups.length; i++) {
          trees[i] = acct[i].getAccounts();
          // System.out.println("trees[" + i + "] = acct[i].getAccounts()"
          // + acct[i].getAccounts().length);
        }

        List<HashMap<String, String>> hashMapList = new ArrayList<HashMap<String, String>>();
        List<HashMap<String, String>> hashMapListSR1 = new ArrayList<HashMap<String, String>>();
        List<HashMap<String, String>> hashMapListSR2 = new ArrayList<HashMap<String, String>>();
        // System.out.println("trees.length:" + trees.length);
        for (int i = 0; i < trees.length; i++) {
          // System.out.println("trees[" + i + "].length:" + trees[i].length);
          for (int j = 0; j < trees[i].length; j++) {
            HashMap<String, String> hashMap = new HashMap<String, String>();
            hashMap.put("elementLevel", trees[i][j].elementLevel);
            // System.out.println("elementLevel " + trees[i][j].elementLevel);
            hashMap.put("name", trees[i][j].name);
            // System.out.println("name" + trees[i][j].name);
            hashMap.put("qty", trees[i][j].qty);
            // System.out.println("qty" + trees[i][j].qty);
            hashMap.put("qtyRef", trees[i][j].qtyRef);
            // System.out.println("qtyRef" + trees[i][j].qtyRef);
            hashMap.put("groupname", strGroups[i].name);
            // System.out.println("groupname" + strGroups[i].name);
            hashMap.put("pagebreak", strGroups[i].pagebreak);
            // System.out.println("pagebreak" + strGroups[i].pagebreak);

            hashMapList.add(hashMap);

            if (i == 0)
              hashMapListSR1.add(hashMap);
            if (i == 1)
              hashMapListSR2.add(hashMap);
          }
        }
        /************* Agregado por yo :) *****************/

        FieldProvider[] dataSR1 = FieldProviderFactory.getFieldProviderArray(hashMapListSR1);
        FieldProvider[] dataSR2 = FieldProviderFactory.getFieldProviderArray(hashMapListSR2);
        JRFieldProviderDataSource dsSR1 = new JRFieldProviderDataSource(dataSR1,
            vars.getJavaDateFormat());
        JRFieldProviderDataSource dsSR2 = new JRFieldProviderDataSource(dataSR2,
            vars.getJavaDateFormat());
        parameters.put("dsSR1", dsSR1);
        parameters.put("dsSR2", dsSR2);

        // Obteniendo el path del template
        String strLanguage = vars.getLanguage();
        String strBaseDesign = this.getBaseDesignPath(strLanguage);
        JasperReport jasperReportLinesSR1;
        JasperReport jasperReportLinesSR2;

        // Setting path and compiling report
        try {
          JasperDesign jasperDesignLinesSR1 = JRXmlLoader.load(strBaseDesign
              + "/pe/com/unifiedgo/report/ad_reports/GeneralAccountingPDF_subreport1.jrxml");
          jasperReportLinesSR1 = JasperCompileManager.compileReport(jasperDesignLinesSR1);

          JasperDesign jasperDesignLinesSR2 = JRXmlLoader.load(strBaseDesign
              + "/pe/com/unifiedgo/report/ad_reports/GeneralAccountingPDF_subreport2.jrxml");
          jasperReportLinesSR2 = JasperCompileManager.compileReport(jasperDesignLinesSR2);
        } catch (JRException e) {
          e.printStackTrace();
          // System.out.println("error creando el subreporte");
          throw new ServletException(e.getMessage());
        }
        parameters.put("SUBREP_GeneralAccountingPDF_subreport1", jasperReportLinesSR1);
        parameters.put("SUBREP_GeneralAccountingPDF_subreport2", jasperReportLinesSR2);

        /*
         * System.out.println("-----------parameters----------"); Iterator it =
         * parameters.entrySet().iterator(); while (it.hasNext()) { Map.Entry e = (Map.Entry)
         * it.next(); System.out.println(e.getKey() + " " + e.getValue()); }
         * /***************************************************************************************
         */
        FieldProvider[] data = FieldProviderFactory.getFieldProviderArray(hashMapList);
        renderJR(vars, response, strReportName, "xls", parameters, data, null);

      } finally {
        OBContext.restorePreviousMode();
      }

    } catch (ArrayIndexOutOfBoundsException e) {
      advisePopUp(request, response, "ERROR",
          Utility.messageBD(this, "ReportWithoutNodes", vars.getLanguage()));

    }
  }

  private String[] getYearsToClose(Date startingDate, String strOrg, Calendar calendar,
      String strcAcctSchemaId, boolean isYearRef) {
    String openingEntryOwner = "";
    ArrayList<Year> previousYears = getOrderedPreviousYears(startingDate, calendar);
    Set<String> notClosedYears = new HashSet<String>();
    for (Year previousYear : previousYears) {
      for (Organization org : getCalendarOwnerOrgs(strOrg)) {
        if (isNotClosed(previousYear, org, strcAcctSchemaId)) {
          notClosedYears.add(previousYear.getFiscalYear());
        } else {
          openingEntryOwner = previousYear.getFiscalYear();
        }
      }
    }
    String[] result = { Utility.getInStrSet(notClosedYears), openingEntryOwner };
    return result;
  }

  private Set<Organization> getCalendarOwnerOrgs(String strOrg) {
    Set<Organization> calendarOwnerOrgs = new HashSet<Organization>();
    Organization organization = OBDal.getInstance().get(Organization.class, strOrg);
    if (organization.isAllowPeriodControl()) {
      calendarOwnerOrgs.add(organization);
    }
    for (String child : new OrganizationStructureProvider().getChildTree(strOrg, false)) {
      calendarOwnerOrgs.addAll(getCalendarOwnerOrgs(child));
    }
    return calendarOwnerOrgs;
  }

  private boolean isNotClosed(Year year, Organization org, String strcAcctSchemaId) {
    OBContext.setAdminMode(false);
    try {
      OBCriteria<OrganizationClosing> obc = OBDal.getInstance().createCriteria(
          OrganizationClosing.class);
      obc.createAlias(OrganizationClosing.PROPERTY_ORGACCTSCHEMA, "oa");
      obc.add(Restrictions.eq("organization", org));
      obc.add(Restrictions.eq(OrganizationClosing.PROPERTY_YEAR, year));
      obc.add(Restrictions.eq("oa.accountingSchema.id", strcAcctSchemaId));
      obc.add(Restrictions.isNotNull(OrganizationClosing.PROPERTY_CLOSINGFACTACCTGROUP));
      obc.setMaxResults(1);
      return obc.uniqueResult() == null ? true : false;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private ArrayList<Year> getOrderedPreviousYears(Date startingDate, Calendar calendar) {
    final StringBuilder hqlString = new StringBuilder();
    ArrayList<Year> result = new ArrayList<Year>();
    hqlString.append("select y");
    hqlString.append(" from FinancialMgmtYear y, FinancialMgmtPeriod as p");
    hqlString
        .append(" where p.year = y  and p.endingDate < :date and y.calendar = :calendar order by p.startingDate");
    final Session session = OBDal.getInstance().getSession();
    final Query query = session.createQuery(hqlString.toString());
    query.setParameter("date", startingDate);
    query.setParameter("calendar", calendar);
    for (Object resultObject : query.list()) {
      final Year previousYear = (Year) resultObject;
      if (!(result.contains(previousYear))) {
        result.add(previousYear);
      }
    }
    return result;
  }

  private HashMap<String, Date> getStartingEndingDate(Year year) {
    final StringBuilder hqlString = new StringBuilder();
    HashMap<String, Date> result = new HashMap<String, Date>();
    result.put("startingDate", null);
    result.put("endingDate", null);
    hqlString.append("select min(p.startingDate) as startingDate, max(p.endingDate) as endingDate");
    hqlString.append(" from FinancialMgmtPeriod as p");
    hqlString.append(" where p.year = :year");

    final Session session = OBDal.getInstance().getSession();
    final Query query = session.createQuery(hqlString.toString());
    query.setParameter("year", year);
    for (Object resultObject : query.list()) {
      if (resultObject.getClass().isArray()) {
        final Object[] values = (Object[]) resultObject;
        result.put("startingDate", (Date) values[0]);
        result.put("endingDate", (Date) values[1]);
      }
    }
    return result;
  }

  private AccountTreeData[] appendRecords(AccountTreeData[] data, String strIncomeSummary,
      String strISyear, String strISyearRef) throws ServletException {
    if (data == null || strIncomeSummary == null || strIncomeSummary.equals("")
        || strISyear == null || strISyear.equals("") || strISyearRef == null
        || strISyearRef.equals(""))
      return data;
    AccountTreeData[] data2 = new AccountTreeData[data.length + 1];
    boolean found = false;
    for (int i = 0; i < data.length; i++) {
      if (data[i].id.equals(strIncomeSummary)) {
        found = true;
        BigDecimal isYear = new BigDecimal(strISyear);
        BigDecimal isYearRef = new BigDecimal(strISyearRef);
        data[i].qty = (new BigDecimal(data[i].qty).subtract(isYear)).toPlainString();
        data[i].qtycredit = (new BigDecimal(data[i].qtycredit).add(isYear)).toPlainString();
        data[i].qtyRef = (new BigDecimal(data[i].qtyRef).subtract(isYearRef)).toPlainString();
        data[i].qtycreditRef = (new BigDecimal(data[i].qtycreditRef).add(isYearRef))
            .toPlainString();
      }
      data2[i] = data[i];
    }
    if (!found) {
      data2[data2.length - 1] = new AccountTreeData();
      data2[data2.length - 1].id = strIncomeSummary;
      data2[data2.length - 1].qty = new BigDecimal(strISyear).negate().toPlainString();
      data2[data2.length - 1].qtycredit = strISyear;
      data2[data2.length - 1].qtyRef = new BigDecimal(strISyearRef).negate().toPlainString();
      data2[data2.length - 1].qtycreditRef = strISyearRef;
    } else
      return data;
    return data2;
  }

  private String processIncomeSummary(String strDateFrom, String strDateTo, String strAgno,
      String strTreeOrg, String strOrg, String strcAcctSchemaId) throws ServletException,
      IOException {
    String strISRevenue = GeneralAccountingData.selectPyG(this, "R", strDateFrom, strDateTo,
        strcAcctSchemaId, strAgno, Tree.getMembers(this, strTreeOrg, strOrg));
    String strISExpense = GeneralAccountingData.selectPyG(this, "E", strDateFrom, strDateTo,
        strcAcctSchemaId, strAgno, Tree.getMembers(this, strTreeOrg, strOrg));
    BigDecimal totalRevenue = new BigDecimal(strISRevenue);
    BigDecimal totalExpense = new BigDecimal(strISExpense);
    BigDecimal total = totalRevenue.add(totalExpense);
    if (log4j.isDebugEnabled())
      log4j.debug(total.toString());
    return total.toString();
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strAgno, String strAgnoRef, String strDateFrom, String strDateTo, String strPageNo,
      String strDateFromRef, String strDateToRef, String strAsDateTo, String strAsDateToRef,
      String strElementValue, String strConImporte, String strOrg, String strLevel,
      String strConCodigo, String strcAcctSchemaId) throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/report/ad_reports/GeneralAccounting").createXmlDocument();

    ToolBar toolbar = new ToolBar(this, vars.getLanguage(), "GeneralAccounting", false, "", "", "",
        false, "ad_reports", strReplaceWith, false, true);
    toolbar.prepareSimpleToolBarTemplate();
    xmlDocument.setParameter("toolbar", toolbar.toString());

    try {
      WindowTabs tabs = new WindowTabs(this, vars,
          "pe.com.unifiedgo.report.ad_reports.GeneralAccounting");
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      xmlDocument.setParameter("theme", vars.getTheme());
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "GeneralAccounting.html",
          classInfo.id, classInfo.type, strReplaceWith, tabs.breadcrumb());
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "GeneralAccounting.html",
          strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.manualTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage("GeneralAccounting");
      vars.removeMessage("GeneralAccounting");
      if (myMessage != null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("agno", strAgno);
    xmlDocument.setParameter("agnoRef", strAgnoRef);
    xmlDocument.setParameter("dateFrom", strDateFrom);
    xmlDocument.setParameter("dateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTo", strDateTo);
    xmlDocument.setParameter("dateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromRef", strDateFromRef);
    xmlDocument.setParameter("dateFromRefdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateFromRefsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateToRef", strDateToRef);
    xmlDocument.setParameter("PageNo", strPageNo);
    xmlDocument.setParameter("dateToRefdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("dateToRefsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("asDateTo", strAsDateTo);
    xmlDocument.setParameter("asDateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("asDateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("asDateToRef", strAsDateToRef);
    xmlDocument.setParameter("asDateToRefdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("asDateToRefsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
    xmlDocument.setParameter("conImporte", strConImporte);
    xmlDocument.setParameter("conCodigo", strConCodigo);
    xmlDocument.setParameter("C_Org_ID", strOrg);
    xmlDocument.setParameter("C_ElementValue_ID", strElementValue);
    xmlDocument.setParameter("level", strLevel);
    xmlDocument.setParameter("cAcctschemaId", strcAcctSchemaId);
    xmlDocument.setData(
        "reportC_ACCTSCHEMA_ID",
        "liststructure",
        AccountingSchemaMiscData.selectC_ACCTSCHEMA_ID(this,
            Utility.getContext(this, vars, "#AccessibleOrgTree", "GeneralAccounting"),
            Utility.getContext(this, vars, "#User_Client", "GeneralAccounting"), strcAcctSchemaId));
    try {
      ComboTableData comboTableData = new ComboTableData(vars, this, "LIST", "",
          "C_ElementValue level", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
              "GeneralAccounting"), Utility.getContext(this, vars, "#User_Client",
              "GeneralAccounting"), 0);
      Utility.fillSQLParameters(this, vars, null, comboTableData, "GeneralAccounting", "");
      xmlDocument.setData("reportLevel", "liststructure", comboTableData.select(false));
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    String strOrgList = "";
    String[] orgList = OBContext.getOBContext().getReadableOrganizations();
    int i = 0;
    for (String org : orgList) {
      if (i == 0) {
        strOrgList += "'" + org + "'";
      } else {
        strOrgList += ",'" + org + "'";
      }
      i++;
    }

    xmlDocument.setParameter(
        "orgs",
        Utility.arrayDobleEntrada("arrOrgs",
            GeneralAccountingData.selectOrgsDouble(this, vars.getClient(), strOrgList)));
    xmlDocument.setParameter(
        "accountingReports",
        Utility.arrayDobleEntrada("arrAccountingReports",
            GeneralAccountingData.selectRptDouble(this)));
    xmlDocument.setParameter(
        "years",
        Utility.arrayDobleEntrada("arrYears",
            GeneralAccountingData.selectYearsDouble(this, vars.getUserClient())));
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  public String getServletInfo() {
    return "Servlet GeneralAccountingData";
  } // end of getServletInfo() method
}
