/*
 *************************************************************************
 * The contents of conn file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use conn
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.businessUtility;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchema;
import org.openbravo.model.financialmgmt.accounting.coa.ElementValue;
import org.openbravo.model.financialmgmt.calendar.Year;

import pe.com.unifiedgo.accounting.data.SCOEndyearclosemapping;

public class EndYearCloseUtility {

  private Organization organization;
  private Year year;
  private ConnectionProvider conn;
  private Connection con;
  private VariablesSecureApp vars;
  private BigDecimal ExpenseAmtDr = new BigDecimal("0");
  private BigDecimal ExpenseAmtCr = new BigDecimal("0");
  private BigDecimal RevenueAmtDr = new BigDecimal("0");
  private BigDecimal RevenueAmtCr = new BigDecimal("0");
  protected Logger log4j = Logger.getLogger(EndYearCloseUtility.class);

  private String anioRegnumber = "002017";
  private String anioRegnumberPlus = "002018";

  public EndYearCloseUtility(Organization _organization, Year _year, ConnectionProvider _conn,
      Connection _con, VariablesSecureApp _vars) {
    organization = _organization;
    year = _year;
    conn = _conn;
    vars = _vars;
    con = _con;
  }

  public synchronized OBError processYearClose() {
    String strYearId = year.getId();
    String strOrgId = organization.getId();

    String strYear = year.getFiscalYear();
    String strNextYear = String.valueOf(Integer.parseInt(year.getFiscalYear()) + 1);
    String yearstartdate = "01-01-" + strYear;
    String nextyearstardate = "01-01-" + strNextYear;

    OBError myError = new OBError();
    try {
      boolean isYearNotClose = EndYearCloseUtilityData.selectYearNotClosed(conn, strYearId);
      if (isYearNotClose) {
        myError.setType("Error");
        myError.setTitle("");
        myError.setMessage(Utility.messageBD(conn, "YearNotClose", vars.getLanguage()));
        return myError;
      }

      String countNotPosted = EndYearCloseUtilityData.countNotPosted(con, conn, vars.getClient(),
          strOrgId, yearstartdate, nextyearstardate);

      if (Integer.parseInt(countNotPosted) > 0) {
        myError.setType("Error");
        myError.setTitle("");
        myError
            .setMessage(Utility.messageBD(conn, "SCO_CloseYearNotAllPosted", vars.getLanguage()));
        return myError;
      }

      // EndYearCloseUtilityData[] dataOrgs = EndYearCloseUtilityData.treeOrg(conn,
      // vars.getClient(), strOrgId);
      EndYearCloseUtilityData[] dataOrgAcctSchemas = EndYearCloseUtilityData
          .treeOrgAcctSchemas(conn, vars.getClient(), strOrgId);
      EndYearCloseUtilityData[] acctSchema = EndYearCloseUtilityData.treeAcctSchema(conn,
          vars.getClient(), strOrgId);
      String strPediodId = EndYearCloseUtilityData.getLastPeriod(conn, strYearId);
      Set<String> closedOrganizations = new HashSet<String>();

      Year year = OBDal.getInstance().get(Year.class, strYearId);
      anioRegnumber = "12" + strYear;
      anioRegnumberPlus = "00" + strNextYear;

      for (int j = 0; j < acctSchema.length; j++) {
        String balanceAmount = EndYearCloseUtilityData.balanceAmount(conn, strYearId,
            acctSchema[j].id,
            Utility.getInStrSet(new OrganizationStructureProvider().getChildTree(strOrgId, true)));
        if (BigDecimal.ZERO.compareTo(new BigDecimal(balanceAmount)) != 0) {
          conn.releaseRollbackConnection(con);
          myError.setType("Error");
          myError.setTitle("");
          Map<String, String> parameters = new HashMap<String, String>();
          try {
            OBContext.setAdminMode();
            AcctSchema schema = OBDal.getInstance().get(AcctSchema.class, acctSchema[j].id);
            parameters.put("AcctSchema", schema.getName());
          } finally {
            OBContext.restorePreviousMode();
          }

          myError.setMessage(Utility.parseTranslation(conn, vars, parameters, vars.getLanguage(),
              Utility.messageBD(conn, "BalanceIsNotBalanced", vars.getLanguage())));
          return myError;
        }
        String strRegId = SequenceIdData.getUUID();
        String strCloseId = SequenceIdData.getUUID();
        String strOpenId = SequenceIdData.getUUID();
        String strDivideUpId = SequenceIdData.getUUID();
        boolean createClosing = OBDal.getInstance().get(AcctSchema.class, acctSchema[j].id)
            .getFinancialMgmtAcctSchemaGLList().size() > 0
                ? OBDal.getInstance().get(AcctSchema.class, acctSchema[j].id)
                    .getFinancialMgmtAcctSchemaGLList().get(0).isCreateClosing()
                : true;
        EndYearCloseUtilityData[] retainedEarningAccount = EndYearCloseUtilityData
            .retainedearning(conn, acctSchema[j].id);

        if (retainedEarningAccount == null || retainedEarningAccount.length == 0) {
          strDivideUpId = "";
        }
        // for (int i = 0; i < dataOrgs.length; i++) {
        if (log4j.isDebugEnabled())
          log4j.debug("Output: Before buttonReg");
        String regCount = EndYearCloseUtilityData.getRegCount(conn, vars.getClient(),
            strOrgId/*
                     * dataOrgs [ i]. org
                     */, acctSchema[j].id, strPediodId);
        if (new Integer(regCount).intValue() > 0) {
          conn.releaseRollbackConnection(con);
          myError.setType("Error");
          myError.setTitle("");
          myError
              .setMessage(Utility.messageBD(conn, "RegularizationDoneAlready", vars.getLanguage()));
          return myError;
        }
        String strRegOut = processButtonReg(strYearId, strOrgId /* dataOrgs[i].org */, strRegId,
            acctSchema[j].id, strDivideUpId, retainedEarningAccount);

        String strCloseOut = createClosing ? processButtonClose(strYearId,
            strOrgId /*
                      * dataOrgs[i].org
                      */, strCloseId, strOpenId, acctSchema[j].id, strDivideUpId) : "Success";

        if (!createClosing) {
          strCloseId = "";
          strOpenId = "";
        }
        if (!strRegOut.equals("Success")) {
          conn.releaseRollbackConnection(con);
          return Utility.translateError(conn, vars, vars.getLanguage(), "ProcessRunError");
        } else if (!strCloseOut.equals("Success")) {
          return Utility.translateError(conn, vars, vars.getLanguage(),
              Utility.messageBD(conn, "ProcessRunError_CreateNextPeriod", vars.getLanguage()));
        }
        ExpenseAmtDr = BigDecimal.ZERO;
        ExpenseAmtCr = BigDecimal.ZERO;
        RevenueAmtDr = BigDecimal.ZERO;
        RevenueAmtCr = BigDecimal.ZERO;
        // }

        for (int i = 0; i < dataOrgAcctSchemas.length; i++) {
          String strOrgSchemaId = EndYearCloseUtilityData.orgAcctschema(conn,
              dataOrgAcctSchemas[i].org, acctSchema[j].id);
          if (strOrgSchemaId != null && !strOrgSchemaId.equals("")) {
            if (EndYearCloseUtilityData.insertOrgClosing(con, conn, vars.getClient(), strOrgId,
                vars.getUser(), strYearId, strOrgSchemaId, strRegId, strCloseId, strDivideUpId,
                strOpenId) == 0)
              return Utility.translateError(conn, vars, vars.getLanguage(), "ProcessRunError");
          }
        }
        if (!closedOrganizations.contains(strOrgId)) {
          if (EndYearCloseUtilityData.updateClose(con, conn, vars.getUser(), strYearId,
              strOrgId) == 0) {
            String strAllPeriodsErr = Utility.messageBD(conn, "AllPeriodsPermanentClosed",
                vars.getLanguage());
            return Utility.translateError(conn, vars, vars.getLanguage(), strAllPeriodsErr);
          }
        }

        // set the em_sco_regnumber to the created fact_accts
        EndYearCloseUtilityData regFactaccts[] = EndYearCloseUtilityData
            .selectOrderedFactAcctbyGroup(con, conn, vars.getClient(), strRegId);
        if (regFactaccts != null) {
          for (int i = 0; i < regFactaccts.length; i++) {
            String factAcctId = regFactaccts[i].factAcctId;
            EndYearCloseUtilityData.updateFactAcctScoSeqno(con, conn, String.valueOf(i + 1),
                vars.getClient(), factAcctId);
          }
        }

        EndYearCloseUtilityData closeFactaccts[] = EndYearCloseUtilityData
            .selectOrderedFactAcctbyGroup(con, conn, vars.getClient(), strCloseId);
        if (closeFactaccts != null) {
          for (int i = 0; i < closeFactaccts.length; i++) {
            String factAcctId = closeFactaccts[i].factAcctId;
            EndYearCloseUtilityData.updateFactAcctScoSeqno(con, conn, String.valueOf(i + 1),
                vars.getClient(), factAcctId);
          }
        }

        EndYearCloseUtilityData openFactaccts[] = EndYearCloseUtilityData
            .selectOrderedFactAcctbyGroup(con, conn, vars.getClient(), strOpenId);
        if (openFactaccts != null) {
          for (int i = 0; i < openFactaccts.length; i++) {
            String factAcctId = openFactaccts[i].factAcctId;
            EndYearCloseUtilityData.updateFactAcctScoSeqno(con, conn, String.valueOf(i + 1),
                vars.getClient(), factAcctId);
          }
        }

        EndYearCloseUtilityData divideUpFactaccts[] = EndYearCloseUtilityData
            .selectOrderedFactAcctbyGroup(con, conn, vars.getClient(), strDivideUpId);
        if (divideUpFactaccts != null) {
          for (int i = 0; i < divideUpFactaccts.length; i++) {
            String factAcctId = divideUpFactaccts[i].factAcctId;
            EndYearCloseUtilityData.updateFactAcctScoSeqno(con, conn, String.valueOf(i + 1),
                vars.getClient(), factAcctId);
          }
        }

        closedOrganizations.add(strOrgId);
      }

      // Create sco_yearop_stock_balance entries for the new year
      EndYearCloseUtilityData.deleteYearopStockBalanceEntries(con, conn, vars.getClient(), strOrgId,
          nextyearstardate);
      EndYearCloseUtilityData.insertSelectYearopStockBalance(con, conn, vars.getClient(), strOrgId,
          vars.getUser(), nextyearstardate, yearstartdate);

      // Create sco_yearop_finacc_balance entries for the new year
      EndYearCloseUtilityData.deleteYearopFinaccBalanceEntries(con, conn, vars.getClient(),
          strOrgId, nextyearstardate);
      EndYearCloseUtilityData.insertSelectYearopFinaccBalance(con, conn, vars.getClient(), strOrgId,
          vars.getUser(), nextyearstardate, yearstartdate);

      conn.releaseCommitConnection(con);
      myError.setType("Success");
      myError.setTitle("");
      myError.setMessage(Utility.messageBD(conn, "Success", vars.getLanguage()));
    } catch (Exception e) {
      e.printStackTrace();
      log4j.warn(e);
      try {
        conn.releaseRollbackConnection(con);
      } catch (Exception ignored) {
      }
      myError = Utility.translateError(conn, vars, vars.getLanguage(), e.getMessage());
    }
    return myError;
  }

  private synchronized String processButtonReg(String strYearId, String stradOrgId,
      String strFact_Acct_Group_ID, String strAcctSchema, String strDivideUpId,
      EndYearCloseUtilityData[] account2) throws ServletException {

    String Fact_Acct_ID = "";
    String Fact_Acct_Group_ID = strFact_Acct_Group_ID;
    String strPediodId = EndYearCloseUtilityData.getLastPeriod(conn, strYearId);
    String strRegEntry = Utility.messageBD(conn, "RegularizationEntry", vars.getLanguage());
    String currency = EndYearCloseUtilityData.cCurrencyId(conn, strAcctSchema);

    OBCriteria<SCOEndyearclosemapping> obc = OBDal.getInstance()
        .createCriteria(SCOEndyearclosemapping.class);
    obc.setFilterOnReadableOrganization(false);
    obc.add(Restrictions.eq(SCOEndyearclosemapping.PROPERTY_ACTIVE, true));
    obc.addOrderBy(SCOEndyearclosemapping.PROPERTY_SEQUENCENUMBER, true);

    List<SCOEndyearclosemapping> endyearclosemapping_array = obc.list();

    if (endyearclosemapping_array.size() == 0) {

      EndYearCloseUtilityData[] totalAmountsExpense = EndYearCloseUtilityData.getTotalAmounts(con,
          conn, strYearId, "E", stradOrgId, strAcctSchema);

      EndYearCloseUtilityData[] totalAmountsMemo = EndYearCloseUtilityData.getTotalAmounts(con,
          conn, strYearId, "M", stradOrgId, strAcctSchema);

      ExpenseAmtDr = ExpenseAmtDr.add(new BigDecimal(totalAmountsExpense[0].totalamtdr)
          .add(new BigDecimal(totalAmountsMemo[0].totalamtdr)));
      ExpenseAmtCr = ExpenseAmtCr.add(new BigDecimal(totalAmountsExpense[0].totalamtcr)
          .add(new BigDecimal(totalAmountsMemo[0].totalamtcr)));

      EndYearCloseUtilityData[] totalAmountsRevenue = EndYearCloseUtilityData.getTotalAmounts(con,
          conn, strYearId, "R", stradOrgId, strAcctSchema);

      RevenueAmtDr = RevenueAmtDr.add(new BigDecimal(totalAmountsRevenue[0].totalamtdr));
      RevenueAmtCr = RevenueAmtCr.add(new BigDecimal(totalAmountsRevenue[0].totalamtcr));

      // Inserts income summary statement
      EndYearCloseUtilityData.insertSelect(con, conn, vars.getClient(), stradOrgId, vars.getUser(),
          EndYearCloseUtilityData.getEndDate(conn, strPediodId), strPediodId, currency,
          Fact_Acct_Group_ID, "10", "R", strRegEntry, "00-CIERRE-" + anioRegnumber, strYearId,
          "'E','M'", strAcctSchema, "");

      EndYearCloseUtilityData.insertSelect(con, conn, vars.getClient(), stradOrgId, vars.getUser(),
          EndYearCloseUtilityData.getEndDate(conn, strPediodId), strPediodId, currency,
          Fact_Acct_Group_ID, "20", "R", strRegEntry, "00-CIERRE-" + anioRegnumber, strYearId,
          "'R'", strAcctSchema, "");

      EndYearCloseUtilityData[] account = EndYearCloseUtilityData.incomesummary(conn,
          strAcctSchema);

      if (ExpenseAmtDr.add(RevenueAmtDr).subtract(RevenueAmtCr).subtract(ExpenseAmtCr)
          .signum() > 0) {
        Fact_Acct_ID = SequenceIdData.getUUID();
        EndYearCloseUtilityData.insert(con, conn, Fact_Acct_ID, vars.getClient(), stradOrgId,
            vars.getUser(), strAcctSchema, account[0].accountId,
            EndYearCloseUtilityData.getEndDate(conn, strPediodId), strPediodId,
            EndYearCloseUtilityData.adTableId(conn), "A",
            EndYearCloseUtilityData.cCurrencyId(conn, strAcctSchema), "0",
            ExpenseAmtDr.add(RevenueAmtDr).subtract(RevenueAmtCr).subtract(ExpenseAmtCr).toString(),
            "0",
            ExpenseAmtDr.add(RevenueAmtDr).subtract(RevenueAmtCr).subtract(ExpenseAmtCr).toString(),
            Fact_Acct_Group_ID, "10", account[0].name, account[0].value, account[0].cBpartnerId,
            account[0].recordId2, account[0].mProductId, account[0].aAssetId, strRegEntry,
            account[0].cTaxId, account[0].cProjectId, account[0].cActivityId, account[0].user1Id,
            account[0].user2Id, account[0].cCampaignId, account[0].cSalesregionId,
            "00-CIERRE-" + anioRegnumber);
      } else if (ExpenseAmtDr.add(RevenueAmtDr).subtract(RevenueAmtCr).subtract(ExpenseAmtCr)
          .signum() < 0) {
        Fact_Acct_ID = SequenceIdData.getUUID();

        EndYearCloseUtilityData.insert(con, conn, Fact_Acct_ID, vars.getClient(), stradOrgId,
            vars.getUser(), strAcctSchema, account[0].accountId,
            EndYearCloseUtilityData.getEndDate(conn, strPediodId), strPediodId,
            EndYearCloseUtilityData.adTableId(conn), "A",
            EndYearCloseUtilityData.cCurrencyId(conn, strAcctSchema),
            ExpenseAmtCr.add(RevenueAmtCr).subtract(RevenueAmtDr).subtract(ExpenseAmtDr).toString(),
            "0",
            ExpenseAmtCr.add(RevenueAmtCr).subtract(RevenueAmtDr).subtract(ExpenseAmtDr).toString(),
            "0", Fact_Acct_Group_ID, "10", account[0].name, account[0].value,
            account[0].cBpartnerId, account[0].recordId2, account[0].mProductId,
            account[0].aAssetId, strRegEntry, account[0].cTaxId, account[0].cProjectId,
            account[0].cActivityId, account[0].user1Id, account[0].user2Id, account[0].cCampaignId,
            account[0].cSalesregionId, "00-CIERRE-" + anioRegnumber);
      }
      // Inserts retained earning statement
      String strClosingEntry = Utility.messageBD(conn, "ClosingEntry", vars.getLanguage());
      if (account2 != null && account2.length > 0) {

        if (ExpenseAmtDr.add(RevenueAmtDr).subtract(RevenueAmtCr).subtract(ExpenseAmtCr)
            .signum() > 0) {

          Fact_Acct_ID = SequenceIdData.getUUID();
          EndYearCloseUtilityData.insertClose(con, conn, Fact_Acct_ID, vars.getClient(), stradOrgId,
              vars.getUser(), strAcctSchema, account[0].accountId,
              EndYearCloseUtilityData.getEndDate(conn, strPediodId), strPediodId,
              EndYearCloseUtilityData.adTableId(conn), "A",
              EndYearCloseUtilityData.cCurrencyId(conn, strAcctSchema),
              ExpenseAmtDr.add(RevenueAmtDr).subtract(RevenueAmtCr).subtract(ExpenseAmtCr)
                  .toString(),
              "0",
              ExpenseAmtDr.add(RevenueAmtDr).subtract(RevenueAmtCr).subtract(ExpenseAmtCr)
                  .toString(),
              "0", strDivideUpId, "10", "D", account[0].name, account[0].value,
              account[0].cBpartnerId, account[0].recordId2, account[0].mProductId,
              account[0].aAssetId, strClosingEntry, account[0].cTaxId, account[0].cProjectId,
              account[0].cActivityId, account[0].user1Id, account[0].user2Id,
              account[0].cCampaignId, account[0].cSalesregionId, "00-CIERRE-" + anioRegnumber);
          Fact_Acct_ID = SequenceIdData.getUUID();
          EndYearCloseUtilityData.insertClose(con, conn, Fact_Acct_ID, vars.getClient(), stradOrgId,
              vars.getUser(), strAcctSchema, account2[0].accountId,
              EndYearCloseUtilityData.getEndDate(conn, strPediodId), strPediodId,
              EndYearCloseUtilityData.adTableId(conn), "A",
              EndYearCloseUtilityData.cCurrencyId(conn, strAcctSchema), "0",
              ExpenseAmtDr.add(RevenueAmtDr).subtract(RevenueAmtCr).subtract(ExpenseAmtCr)
                  .toString(),
              "0",
              ExpenseAmtDr.add(RevenueAmtDr).subtract(RevenueAmtCr).subtract(ExpenseAmtCr)
                  .toString(),
              strDivideUpId, "10", "D", account2[0].name, account2[0].value,
              account2[0].cBpartnerId, account2[0].recordId2, account2[0].mProductId,
              account2[0].aAssetId, strClosingEntry, account2[0].cTaxId, account2[0].cProjectId,
              account2[0].cActivityId, account2[0].user1Id, account2[0].user2Id,
              account2[0].cCampaignId, account2[0].cSalesregionId, "00-CIERRE-" + anioRegnumber);
        } else if (ExpenseAmtDr.add(RevenueAmtDr).subtract(RevenueAmtCr).subtract(ExpenseAmtCr)
            .signum() < 0) {

          Fact_Acct_ID = SequenceIdData.getUUID();
          EndYearCloseUtilityData.insertClose(con, conn, Fact_Acct_ID, vars.getClient(), stradOrgId,
              vars.getUser(), strAcctSchema, account[0].accountId,
              EndYearCloseUtilityData.getEndDate(conn, strPediodId), strPediodId,
              EndYearCloseUtilityData.adTableId(conn), "A",
              EndYearCloseUtilityData.cCurrencyId(conn, strAcctSchema), "0",
              ExpenseAmtCr.add(RevenueAmtCr).subtract(RevenueAmtDr).subtract(ExpenseAmtDr)
                  .toString(),
              "0",
              ExpenseAmtCr.add(RevenueAmtCr).subtract(RevenueAmtDr).subtract(ExpenseAmtDr)
                  .toString(),
              strDivideUpId, "10", "D", account[0].name, account[0].value, account[0].cBpartnerId,
              account[0].recordId2, account[0].mProductId, account[0].aAssetId, strClosingEntry,
              account[0].cTaxId, account[0].cProjectId, account[0].cActivityId, account[0].user1Id,
              account[0].user2Id, account[0].cCampaignId, account[0].cSalesregionId,
              "00-CIERRE-" + anioRegnumber);
          Fact_Acct_ID = SequenceIdData.getUUID();
          EndYearCloseUtilityData.insertClose(con, conn, Fact_Acct_ID, vars.getClient(), stradOrgId,
              vars.getUser(), strAcctSchema, account2[0].accountId,
              EndYearCloseUtilityData.getEndDate(conn, strPediodId), strPediodId,
              EndYearCloseUtilityData.adTableId(conn), "A",
              EndYearCloseUtilityData.cCurrencyId(conn, strAcctSchema),
              ExpenseAmtCr.add(RevenueAmtCr).subtract(RevenueAmtDr).subtract(ExpenseAmtDr)
                  .toString(),
              "0",
              ExpenseAmtCr.add(RevenueAmtCr).subtract(RevenueAmtDr).subtract(ExpenseAmtDr)
                  .toString(),
              "0", strDivideUpId, "10", "D", account2[0].name, account2[0].value,
              account2[0].cBpartnerId, account2[0].recordId2, account2[0].mProductId,
              account2[0].aAssetId, strClosingEntry, account2[0].cTaxId, account2[0].cProjectId,
              account2[0].cActivityId, account2[0].user1Id, account2[0].user2Id,
              account2[0].cCampaignId, account2[0].cSalesregionId, "00-CIERRE-" + anioRegnumber);
        }
      }

    } else {

      for (int i = 0; i < endyearclosemapping_array.size(); i++) {
        SCOEndyearclosemapping endyearclosemapping = endyearclosemapping_array.get(i);

        EndYearCloseUtilityData[] totalAmountsExpense = EndYearCloseUtilityData.getTotalAmountsLike(
            con, conn, strYearId, endyearclosemapping.getSourceAccounts(), stradOrgId,
            strAcctSchema);

        ExpenseAmtDr = new BigDecimal(0);
        ExpenseAmtCr = new BigDecimal(0);

        String acct = "";
        for (int j = 0; j < totalAmountsExpense.length; j++) {// por cada fuente

          ExpenseAmtDr = ExpenseAmtDr.add(new BigDecimal(totalAmountsExpense[j].totalamtdr));
          ExpenseAmtCr = ExpenseAmtCr.add(new BigDecimal(totalAmountsExpense[j].totalamtcr));

          ElementValue ev = OBDal.getInstance().get(ElementValue.class,
              totalAmountsExpense[j].accountId);
          String evValue = ev.getSearchKey();
          String evDescription = ev.getName();

          BigDecimal dr = new BigDecimal(totalAmountsExpense[j].totalamtdr);
          BigDecimal cr = new BigDecimal(totalAmountsExpense[j].totalamtcr);
          if (ExpenseAmtDr.signum() != 0 || ExpenseAmtCr.signum() != 0) {
            Fact_Acct_ID = SequenceIdData.getUUID();
            EndYearCloseUtilityData.insert(con, conn, Fact_Acct_ID, vars.getClient(), stradOrgId,
                vars.getUser(), strAcctSchema, totalAmountsExpense[j].accountId,
                EndYearCloseUtilityData.getEndDate(conn, strPediodId), strPediodId,
                EndYearCloseUtilityData.adTableId(conn), "A",
                EndYearCloseUtilityData.cCurrencyId(conn, strAcctSchema), dr.toString(),
                cr.toString(), dr.toString(), cr.toString(), Fact_Acct_Group_ID, "10",
                evDescription/* name */, evValue/* value */, null, null, null, null, strRegEntry,
                null, null, null, null, null, null, null, "00-CIERRE-" + anioRegnumber);
          }
        }

        BigDecimal debit = new BigDecimal(0);
        BigDecimal credit = ExpenseAmtCr.subtract(ExpenseAmtDr);

        if (credit.signum() == -1) {
          debit = credit.abs();
          credit = new BigDecimal(0);
        }

        EndYearCloseUtilityData[] account = EndYearCloseUtilityData.elementvalueByValue(conn,
            strAcctSchema, endyearclosemapping.getDestinationAccount());

        if (credit.signum() != 0 || debit.signum() != 0) {
          Fact_Acct_ID = SequenceIdData.getUUID();
          EndYearCloseUtilityData.insert(con, conn, Fact_Acct_ID, vars.getClient(), stradOrgId,
              vars.getUser(), strAcctSchema, account[0].accountId,
              EndYearCloseUtilityData.getEndDate(conn, strPediodId), strPediodId,
              EndYearCloseUtilityData.adTableId(conn), "A",
              EndYearCloseUtilityData.cCurrencyId(conn, strAcctSchema), credit.toString(),
              debit.toString(), credit.toString(), debit.toString(), Fact_Acct_Group_ID, "10",
              account[0].name, account[0].value, account[0].cBpartnerId, account[0].recordId2,
              account[0].mProductId, account[0].aAssetId, strRegEntry, account[0].cTaxId,
              account[0].cProjectId, account[0].cActivityId, account[0].user1Id, account[0].user2Id,
              account[0].cCampaignId, account[0].cSalesregionId, "00-CIERRE-" + anioRegnumber);

        }
      }
    }
    return "Success";
  }

  private synchronized String processButtonClose(String strYearId, String stradOrgId,
      String strCloseID, String strOpenID, String strAcctSchema, String strDivideUpId)
      throws ServletException {

    String Fact_Acct_Group_ID = strCloseID;
    String strPediodId = EndYearCloseUtilityData.getLastPeriod(conn, strYearId);
    String newPeriod = EndYearCloseUtilityData.getNextPeriod(conn, strPediodId);
    String strOpeningEntry = Utility.messageBD(conn, "OpeningEntry", vars.getLanguage());
    String strClosingEntry = Utility.messageBD(conn, "ClosingEntry", vars.getLanguage());
    if (newPeriod.equals("")) {
      try {
        conn.releaseRollbackConnection(con);
      } catch (SQLException e) {
        log4j.error("Next Period does not exist", e);
      }
      return "ProcessRunError";
    }

    String currency = EndYearCloseUtilityData.cCurrencyId(conn, strAcctSchema);

    EndYearCloseUtilityData.insertSelect(con, conn, vars.getClient(), stradOrgId, vars.getUser(),
        EndYearCloseUtilityData.getEndDate(conn, strPediodId), strPediodId, currency,
        Fact_Acct_Group_ID, "20", "C", strClosingEntry, "00-CIERRE-" + anioRegnumber, strYearId,
        "'A'", strAcctSchema, strDivideUpId);
    EndYearCloseUtilityData.insertSelect(con, conn, vars.getClient(), stradOrgId, vars.getUser(),
        EndYearCloseUtilityData.getEndDate(conn, strPediodId), strPediodId, currency,
        Fact_Acct_Group_ID, "10", "C", strClosingEntry, "00-CIERRE-" + anioRegnumber, strYearId,
        "'L','O'", strAcctSchema, strDivideUpId);

    String Fact_Acct_Group_ID2 = strOpenID;
    EndYearCloseUtilityData.insertSelectOpeningWithoutAnalitycs(con, conn, vars.getClient(),
        stradOrgId, vars.getUser(), EndYearCloseUtilityData.getStartDate(conn, newPeriod),
        newPeriod, "308", Fact_Acct_Group_ID2, "20", "O", strOpeningEntry,
        "00-APERTURA-" + anioRegnumberPlus, strYearId, "'A','L','O'", strAcctSchema, strDivideUpId);
    EndYearCloseUtilityData.insertSelectOpeningWithoutAnalitycs(con, conn, vars.getClient(),
        stradOrgId, vars.getUser(), EndYearCloseUtilityData.getStartDate(conn, newPeriod),
        newPeriod, "100", Fact_Acct_Group_ID2, "20", "O", strOpeningEntry,
        "00-APERTURA-" + anioRegnumberPlus, strYearId, "'A','L','O'", strAcctSchema, strDivideUpId);
    EndYearCloseUtilityData.insertSelectOpeningWithoutAnalitycs(con, conn, vars.getClient(),
        stradOrgId, vars.getUser(), EndYearCloseUtilityData.getStartDate(conn, newPeriod),
        newPeriod, "102", Fact_Acct_Group_ID2, "20", "O", strOpeningEntry,
        "00-APERTURA-" + anioRegnumberPlus, strYearId, "'A','L','O'", strAcctSchema, strDivideUpId);

    EndYearCloseUtilityData.insertSelectOpeningWithAnalitycsNotInvoice(con, conn, vars.getClient(),
        stradOrgId, vars.getUser(), EndYearCloseUtilityData.getStartDate(conn, newPeriod),
        newPeriod, "308", Fact_Acct_Group_ID2, "20", "O", strOpeningEntry,
        "00-APERTURA-" + anioRegnumberPlus, strYearId, "'A','L','O'", strAcctSchema, strDivideUpId);
    EndYearCloseUtilityData.insertSelectOpeningWithAnalitycsNotInvoice(con, conn, vars.getClient(),
        stradOrgId, vars.getUser(), EndYearCloseUtilityData.getStartDate(conn, newPeriod),
        newPeriod, "100", Fact_Acct_Group_ID2, "20", "O", strOpeningEntry,
        "00-APERTURA-" + anioRegnumberPlus, strYearId, "'A','L','O'", strAcctSchema, strDivideUpId);
    EndYearCloseUtilityData.insertSelectOpeningWithAnalitycsNotInvoice(con, conn, vars.getClient(),
        stradOrgId, vars.getUser(), EndYearCloseUtilityData.getStartDate(conn, newPeriod),
        newPeriod, "102", Fact_Acct_Group_ID2, "20", "O", strOpeningEntry,
        "00-APERTURA-" + anioRegnumberPlus, strYearId, "'A','L','O'", strAcctSchema, strDivideUpId);

    EndYearCloseUtilityData[] dataAnalytics = EndYearCloseUtilityData.getAllAnalitycs(con, conn,
        strYearId, stradOrgId, vars.getClient());

    StringBuffer sb = new StringBuffer("");
    for (int i = 0; i < dataAnalytics.length; i++) {
      if (i == 0) {
        sb.append("'" + dataAnalytics[i].accountId + "-" + dataAnalytics[i].recordAnalitycs + "'");
      } else {
        sb.append(",'" + dataAnalytics[i].accountId + "-" + dataAnalytics[i].recordAnalitycs + "'");
      }
    }
    String allAnalitycs = sb.toString();
    if (allAnalitycs.equals(""))
      allAnalitycs = "'--'";

    EndYearCloseUtilityData.insertSelectOpeningWithAnalitycs(con, conn, vars.getClient(),
        stradOrgId, vars.getUser(), EndYearCloseUtilityData.getStartDate(conn, newPeriod),
        newPeriod, "308", Fact_Acct_Group_ID2, "20", "O", strOpeningEntry,
        "00-APERTURA-" + anioRegnumberPlus, strYearId, "'A','L','O'", strAcctSchema, strDivideUpId,
        allAnalitycs);
    EndYearCloseUtilityData.insertSelectOpeningWithAnalitycs(con, conn, vars.getClient(),
        stradOrgId, vars.getUser(), EndYearCloseUtilityData.getStartDate(conn, newPeriod),
        newPeriod, "100", Fact_Acct_Group_ID2, "20", "O", strOpeningEntry,
        "00-APERTURA-" + anioRegnumberPlus, strYearId, "'A','L','O'", strAcctSchema, strDivideUpId,
        allAnalitycs);
    EndYearCloseUtilityData.insertSelectOpeningWithAnalitycs(con, conn, vars.getClient(),
        stradOrgId, vars.getUser(), EndYearCloseUtilityData.getStartDate(conn, newPeriod),
        newPeriod, "102", Fact_Acct_Group_ID2, "20", "O", strOpeningEntry,
        "00-APERTURA-" + anioRegnumberPlus, strYearId, "'A','L','O'", strAcctSchema, strDivideUpId,
        allAnalitycs);

    EndYearCloseUtilityData[] opentries = EndYearCloseUtilityData.selectOPEntries(con, conn,
        vars.getClient(), stradOrgId, EndYearCloseUtilityData.getStartDate(conn, newPeriod));

    // AHORA UNIR LAS CUENTAS QUE SE PUEDAN
    EndYearCloseUtilityData[] dataBicurrency = EndYearCloseUtilityData.selectBicurrency(con, conn,
        EndYearCloseUtilityData.getStartDate(conn, newPeriod), stradOrgId);

    for (int i = 0; i < dataBicurrency.length; i++) {

      String[] currs = dataBicurrency[i].arrcurrency.split(",");

      // If there is more than 2 currencies for the same record dont merge since we do not know the
      // record real currency
      if (currs.length > 2) {
        System.out.println("NOT MERGING: dataBicurrency[i].recordAnalitycs:"
            + dataBicurrency[i].recordAnalitycs + " - dataBicurrency[i].accountId:"
            + dataBicurrency[i].accountId + " - dataBicurrency[i].arrcurrency:"
            + dataBicurrency[i].arrcurrency + " - currs.length:" + currs.length);
        continue;
      }
      EndYearCloseUtilityData.updateBicurrency(con, conn, dataBicurrency[i].recordAnalitycs,
          dataBicurrency[i].accountId, stradOrgId,
          EndYearCloseUtilityData.getStartDate(conn, newPeriod));
      EndYearCloseUtilityData.updateBicurrencyAmt(con, conn, dataBicurrency[i].recordAnalitycs,
          dataBicurrency[i].accountId, stradOrgId,
          EndYearCloseUtilityData.getStartDate(conn, newPeriod));
      EndYearCloseUtilityData.deleteBicurrency(con, conn, dataBicurrency[i].recordAnalitycs,
          dataBicurrency[i].accountId, stradOrgId,
          EndYearCloseUtilityData.getStartDate(conn, newPeriod));
    }

    return "Success";
  }

  public OBError processUndoYearClose() {
    String stradOrgId = organization.getId();
    String strYearId = year.getId();
    OBError myError = null;

    String strYear = year.getFiscalYear();
    String strNextYear = String.valueOf(Integer.parseInt(year.getFiscalYear()) + 1);
    String yearstartdate = "01-01-" + strYear;
    String nextyearstardate = "01-01-" + strNextYear;

    try {
      String strRegFactAcctGroupId = "";
      String strCloseFactAcctGroupId = "";
      String strDivideUpFactAcctGroupId = "";
      String strOpenUpFactAcctGroupId = "";
      String strOrgClosingId = "";
      try {
        EndYearCloseUtilityData[] data = EndYearCloseUtilityData.selectFactAcctGroupId(conn,
            stradOrgId, strYearId);
        if (data != null && data.length != 0) {
          for (int i = 0; i < data.length; i++) {
            strRegFactAcctGroupId = data[i].regFactAcctGroupId;
            strCloseFactAcctGroupId = data[i].closeFactAcctGroupId;
            strDivideUpFactAcctGroupId = data[i].divideupFactAcctGroupId;
            strOpenUpFactAcctGroupId = data[i].openFactAcctGroupId;
            strOrgClosingId = data[i].adOrgClosingId;
            String strResult = processUndoYearClose(strYearId, stradOrgId, strRegFactAcctGroupId,
                strCloseFactAcctGroupId, strDivideUpFactAcctGroupId, strOpenUpFactAcctGroupId,
                strOrgClosingId);
            if (!"ProcessOK".equals(strResult)) {
              myError = new OBError();
              myError.setType("Error");
              myError.setTitle("Error");
              myError.setMessage(Utility.messageBD(conn, strResult, vars.getLanguage()));
              conn.releaseRollbackConnection(con);
              return myError;
            }
          }
          EndYearCloseUtilityData.updatePeriodsOpen(con, conn, vars.getUser(), strYearId,
              stradOrgId);
        }

        // Delete sco_yearop_stock_balance entries for the year
        EndYearCloseUtilityData.deleteYearopStockBalanceEntries(con, conn, vars.getClient(),
            stradOrgId, nextyearstardate);
        // Delete sco_yearop_finacc_balance entries for the year
        EndYearCloseUtilityData.deleteYearopFinaccBalanceEntries(con, conn, vars.getClient(),
            stradOrgId, nextyearstardate);

      } catch (ServletException ex) {
        myError = Utility.translateError(conn, vars, vars.getLanguage(), ex.getMessage());
        conn.releaseRollbackConnection(con);
        return myError;
      }

      conn.releaseCommitConnection(con);
      myError = new OBError();
      myError.setType("Success");
      myError.setTitle("");
      myError.setMessage(Utility.messageBD(conn, "Success", vars.getLanguage()));
    } catch (Exception e) {
      e.printStackTrace();
      log4j.warn(e);
      try {
        conn.releaseRollbackConnection(con);
      } catch (Exception ignored) {
      }
      myError = Utility.translateError(conn, vars, vars.getLanguage(), "ProcessRunError");
    }
    return myError;
  }

  private String processUndoYearClose(String strYearId, String stradOrgId,
      String strRegFactAcctGroupId, String strCloseFactAcctGroupId,
      String strDivideUpFactAcctGroupId, String strOpenUpFactAcctGroupId, String strOrgClosingId)
      throws ServletException {
    boolean isYearNotAllowed = EndYearCloseUtilityData.selectUndoAllowed(conn, strYearId);
    if (isYearNotAllowed) {
      return "UndoNotAllowedForYear";
    }
    EndYearCloseUtilityData.deleteOrgClosing(con, conn, strOrgClosingId);
    EndYearCloseUtilityData.deleteFactAcctClose(con, conn, strRegFactAcctGroupId,
        strCloseFactAcctGroupId, strDivideUpFactAcctGroupId, strOpenUpFactAcctGroupId, stradOrgId);
    return "ProcessOK";
  }
}