package pe.com.unifiedgo.accounting.process;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_forms.Account;
import org.openbravo.erpCommon.ad_forms.AcctSchema;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.ad_forms.DocGLJournal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.accounting.Costcenter;
import org.openbravo.model.financialmgmt.accounting.coa.AccountingCombination;
import org.openbravo.model.financialmgmt.gl.GLJournal;
import org.openbravo.model.financialmgmt.gl.GLJournalLine;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.service.db.DbUtility;

import pe.com.unifiedgo.accounting.data.SCOEEFFCashflow;
import pe.com.unifiedgo.accounting.data.SCOInternalDoc;
import pe.com.unifiedgo.accounting.data.ScoRendicioncuentas;

public class GenerateClosingReevalGLJournalLines extends DalBaseProcess {
  public void doExecute(ProcessBundle bundle) throws Exception {
    try {

      /*
       * Set<String> params = bundle.getParams().keySet(); for (int i = 0; i < params.size(); i++) {
       * System.out.println(params.toArray()[i]); }
       */

      final String GL_Journal_ID = (String) bundle.getParams().get("GL_Journal_ID");
      final VariablesSecureApp vars = bundle.getContext().toVars();
      OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
      final ConnectionProvider conProvider = bundle.getConnection();
      final String language = bundle.getContext().getLanguage();
      User user = OBDal.getInstance().get(User.class, vars.getUser());

      int count = 0;

      GLJournal gljournal = OBDal.getInstance().get(GLJournal.class, GL_Journal_ID);
      if (gljournal == null) {
        throw new OBException("@SCO_InternalError@");
      }

      String stmDelete = "DELETE FROM gl_journalline WHERE GL_JOURNAL_ID='" + gljournal.getId()
          + "'";
      PreparedStatement stdelete = bundle.getConnection().getPreparedStatement(stmDelete);
      stdelete.executeUpdate();

      AcctSchema schema = new AcctSchema(bundle.getConnection(),
          gljournal.getAccountingSchema().getId());

      boolean reevalCxC = false;
      boolean reevalCxP = false;
      boolean reevalCxF = false;

      reevalCxC = gljournal.isScoReevalReceivableChk();
      reevalCxP = gljournal.isScoReevalPayableChk();
      reevalCxF = gljournal.isScoReevalFinaccChk();

      Date endingDate = gljournal.getAccountingDate();

      String strDateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("dateFormat.java");
      final SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);

      Calendar c = Calendar.getInstance();
      c.setTime(endingDate);
      c.add(Calendar.DATE, 1);
      Date nextFirstDay = c.getTime();

      Calendar c2 = Calendar.getInstance();
      c2.set(Calendar.YEAR, endingDate.getYear() + 1900);
      c2.set(Calendar.DAY_OF_YEAR, 1);
      c2.set(Calendar.HOUR, 0);
      c2.set(Calendar.MINUTE, 0);
      c2.set(Calendar.SECOND, 0);
      Date startingDate = c2.getTime();

      String DateAcct = dateFormat.format(gljournal.getAccountingDate());
      int line = 1;

      if (reevalCxC) {
        // String stm =
        // "SELECT c_invoice_id FROM C_INVOICE WHERE DATEACCT<=? AND ISSOTRX = 'Y' AND (ISPAID='N'
        // OR finalsettlement > ?) AND C_CURRENCY_ID<>'308' AND DOCSTATUS='CO' AND
        // ad_isorgincluded(ad_org_id,
        // '"+gljournal.getOrganization().getId()+"','"+gljournal.getClient().getId()+"')<>-1 ";

        String stm = "select debescr, haberscr, debesol, habersol, curr, invoice_id, acctvalue, bpid, account_id, dateacc from ( "
            + "select "
            + " coalesce(  sum(case when c_currency_id<>'308' then amtsourcedr else 0 end), 0) as debescr, "
            + "	coalesce( sum (case when c_currency_id<>'308' then amtsourcecr else 0 end), 0) as haberscr, "
            + "sum(amtacctdr) as debesol, sum(amtacctcr) as habersol, min(c_currency_id) as curr, invoice_id, acctvalue, account_id, max(c_bpartner_id) as bpid, min(dateacct) as dateacc from ( "
            + "select *, case when em_sco_record3_id is not null then em_sco_record3_id else record_id end as invoice_id "
            + "from fact_acct fa where (acctvalue like '12%' or acctvalue like '13%' or acctvalue like '14%' or acctvalue like '16%' or acctvalue like '17%') "
            + "and (acctvalue not like '122%' and acctvalue not like '132%' and acctvalue not like '12124%') "
            + "and trunc(dateacct) between ? and ? " + "and AD_ISORGINCLUDED(fa.AD_ORG_ID, '"
            + gljournal.getOrganization().getId() + "', fa.AD_CLIENT_ID)<>-1) as a "
            + " group by invoice_id, acctvalue, account_id "
            + ")as b where curr<>'308' and (debescr<>haberscr  or  debesol<>habersol) and (trunc(dateacc)>='2017-01-01' OR debesol<>habersol)";

        ResultSet result;
        PreparedStatement st = null;

        st = bundle.getConnection().getPreparedStatement(stm);
        st.setTimestamp(1, new Timestamp(startingDate.getTime()));
        st.setTimestamp(2, new Timestamp(endingDate.getTime()));

        Currency currSoles = OBDal.getInstance().get(Currency.class, "308");// soles
        int counter = 0;

        result = st.executeQuery();

        while (result.next()) {

          BigDecimal debescr = result.getBigDecimal("debescr");
          BigDecimal haberscr = result.getBigDecimal("haberscr");
          BigDecimal debesol = result.getBigDecimal("debesol");
          BigDecimal habersol = result.getBigDecimal("habersol");
          String curr = result.getString("curr");
          String invoiceId = result.getString("invoice_id");
          String acctValue = result.getString("acctvalue");
          String bpid = result.getString("bpid");
          String accountId = result.getString("account_id");

          BigDecimal montoScr = debescr.subtract(haberscr);
          BigDecimal montoSol = debesol.subtract(habersol);

          // obtener el monto en soles al tipo de cambio
          String amountConvertedTo = AcctServer.getConvertedAmtGeneral(montoScr.toString(), curr,
              "308", DateAcct, "", gljournal.getClient().getId(),
              gljournal.getOrganization().getId(), bundle.getConnection(), false, true);

          BigDecimal saldoDebe = montoSol.subtract(new BigDecimal(amountConvertedTo)).setScale(2,
              RoundingMode.HALF_UP);

          if (saldoDebe.compareTo(BigDecimal.ZERO) != 0) {

            // HALLAR COMBINACION CONTABLE DESDE ELEMENTVALUE
            Account acc = new Account(conProvider, schema.getC_AcctSchema_ID(), accountId);

            Invoice inv = OBDal.getInstance().get(Invoice.class, invoiceId);
            SCOInternalDoc internalDoc = null;
            String physical = "NO DEFINIDO";

            BusinessPartner bPartner = null;

            if (inv == null) {
              // continue;//SI NO ES INVOICE YA NO REEVALUAR

              internalDoc = OBDal.getInstance().get(SCOInternalDoc.class, invoiceId);
              if (internalDoc != null) {

                boolean skipreeval = internalDoc.isSkipreeval() != null ? internalDoc.isSkipreeval()
                    : false;
                if (skipreeval)
                  continue;

                physical = internalDoc.getPhysicalDocumentNo();
                bPartner = internalDoc.getBusinessPartner();

                if (internalDoc.getCurrency() == null) {
                  continue;
                }
                if (internalDoc.getCurrency().getId().equals("308"))// soles no tomar en cuenta
                  continue;
              } else {
                continue;
              }

            } else {

              boolean skipreeval = inv.isScoSkipreeval() != null ? inv.isScoSkipreeval() : false;
              if (skipreeval)
                continue;

              physical = inv.getScrPhysicalDocumentno();
              bPartner = inv.getBusinessPartner();

              if (inv.getCurrency().getId().equals("308"))// soles no tomar en cuenta
                continue;
            }

            BigDecimal montoDebe = BigDecimal.ZERO;
            BigDecimal montoHaber = saldoDebe.negate();

            Account account = null;
            DocGLJournal dummy = new DocGLJournal(gljournal.getClient().getId(),
                gljournal.getOrganization().getId(), bundle.getConnection());

            if (montoHaber.compareTo(BigDecimal.ZERO) < 0) {
              montoHaber = BigDecimal.ZERO;
              montoDebe = saldoDebe;
              account = dummy.getAccount(AcctServer.ACCTTYPE_ConvertChargeDefaultAmt, schema,
                  bundle.getConnection());
            } else {
              account = dummy.getAccount(AcctServer.ACCTTYPE_ConvertGainDefaultAmt, schema,
                  bundle.getConnection());
            }

            GLJournalLine jline = OBProvider.getInstance().get(GLJournalLine.class);
            jline.setAccountingDate(gljournal.getAccountingDate());
            jline.setOrganization(gljournal.getOrganization());
            jline.setClient(gljournal.getClient());
            jline.setCredit(montoHaber);
            jline.setDebit(montoDebe);
            jline.setDescription("Asiento de reevaluacion T/C CxCobrar documento " + physical);
            jline.setBusinessPartner(bPartner);
            jline.setCurrency(currSoles);
            jline.setRate(BigDecimal.ONE);
            jline.setForeignCurrencyCredit(montoHaber);
            jline.setForeignCurrencyDebit(montoDebe);
            jline.setJournalEntry(gljournal);
            jline.setLineNo((long) line++);
            if (inv != null)
              jline.setScoCInvoice(inv);
            else if (internalDoc != null)
              jline.setScoInternalDoc(internalDoc);

            AccountingCombination ac = OBDal.getInstance().get(AccountingCombination.class,
                account.C_ValidCombination_ID);

            jline.setAccountingCombination(ac);
            OBDal.getInstance().save(jline);

            GLJournalLine jline2 = OBProvider.getInstance().get(GLJournalLine.class);
            jline2.setAccountingDate(gljournal.getAccountingDate());
            jline2.setOrganization(gljournal.getOrganization());
            jline2.setClient(gljournal.getClient());
            jline2.setCredit(montoDebe);
            jline2.setDebit(montoHaber);
            jline2.setDescription("Asiento de reevaluacion T/C CxCobrar documento " + physical);
            jline2.setBusinessPartner(bPartner);
            jline2.setCurrency(currSoles);
            jline2.setRate(BigDecimal.ONE);
            jline2.setForeignCurrencyCredit(montoDebe);
            jline2.setForeignCurrencyDebit(montoHaber);
            jline2.setJournalEntry(gljournal);
            jline2.setLineNo((long) line++);

            if (inv != null)
              jline2.setScoCInvoice(inv);
            else if (internalDoc != null)
              jline2.setScoInternalDoc(internalDoc);

            AccountingCombination ac2 = OBDal.getInstance().get(AccountingCombination.class,
                acc.C_ValidCombination_ID);
            jline2.setAccountingCombination(ac2);
            jline2.setScoReevalCurrency(OBDal.getInstance().get(Currency.class, curr));
            OBDal.getInstance().save(jline2);

            counter++;

          }

        }
        result.close();
        OBDal.getInstance().flush();

      }

      if (reevalCxP) {

        String stm = "select debescr, haberscr, debesol, habersol, curr, invoice_id, acctvalue, bpid, account_id, dateacc from ( "
            + "select "
            + " coalesce(  sum(case when c_currency_id<>'308' then amtsourcedr else 0 end), 0) as debescr, "
            + "	coalesce( sum (case when c_currency_id<>'308' then amtsourcecr else 0 end), 0) as haberscr, "
            + "sum(amtacctdr) as debesol, sum(amtacctcr) as habersol, min(c_currency_id) as curr, invoice_id, acctvalue, account_id, max(c_bpartner_id) as bpid, min(dateacct) as dateacc from ( "
            + "select *, case when em_sco_record3_id is not null then em_sco_record3_id else record_id end as invoice_id "
            + "from fact_acct fa where (acctvalue like '42%' or acctvalue like '43%' or acctvalue like '44%' or acctvalue like '45%' or acctvalue like '46%' or acctvalue like '47%') "
            + "and (acctvalue not like '422%' and acctvalue not like '432%' and acctvalue not like '473%') "
            + "and trunc(dateacct) between ? and ? " + "and AD_ISORGINCLUDED(fa.AD_ORG_ID, '"
            + gljournal.getOrganization().getId() + "', fa.AD_CLIENT_ID)<>-1) as a "
            + " group by invoice_id, acctvalue, account_id "
            + ")as b where curr<>'308' and (debescr<>haberscr  or  debesol<>habersol) and (trunc(dateacc)>='2017-01-01' OR debesol<>habersol) ";

        ResultSet result;
        PreparedStatement st = null;

        st = bundle.getConnection().getPreparedStatement(stm);
        st.setTimestamp(1, new Timestamp(startingDate.getTime()));
        st.setTimestamp(2, new Timestamp(endingDate.getTime()));

        Currency currSoles = OBDal.getInstance().get(Currency.class, "308");// soles
        int counter = 0;

        result = st.executeQuery();
        while (result.next()) {

          BigDecimal debescr = result.getBigDecimal("debescr");
          BigDecimal haberscr = result.getBigDecimal("haberscr");
          BigDecimal debesol = result.getBigDecimal("debesol");
          BigDecimal habersol = result.getBigDecimal("habersol");
          String curr = result.getString("curr");
          String invoiceId = result.getString("invoice_id");
          String acctValue = result.getString("acctvalue");
          String bpid = result.getString("bpid");
          String accountId = result.getString("account_id");

          BigDecimal montoScr = debescr.subtract(haberscr);
          BigDecimal montoSol = debesol.subtract(habersol);

          // obtener el monto en soles al tipo de cambio
          String amountConvertedTo = AcctServer.getConvertedAmtGeneral(montoScr.toString(), curr,
              "308", DateAcct, "", gljournal.getClient().getId(),
              gljournal.getOrganization().getId(), bundle.getConnection(), true, true);

          BigDecimal saldoDebe = (montoSol).subtract(new BigDecimal(amountConvertedTo)).setScale(2,
              RoundingMode.HALF_UP);

          if (saldoDebe.compareTo(BigDecimal.ZERO) != 0) {

            // HALLAR COMBINACION CONTABLE DESDE ELEMENTVALUE
            Account acc = new Account(conProvider, schema.getC_AcctSchema_ID(), accountId);
            BusinessPartner bPartner = null;

            Invoice inv = OBDal.getInstance().get(Invoice.class, invoiceId);
            SCOInternalDoc internalDoc = null;
            ScoRendicioncuentas rendCuentas = null;
            String physical = "NO DEFINIDO";

            if (inv == null) {

              internalDoc = OBDal.getInstance().get(SCOInternalDoc.class, invoiceId);
              if (internalDoc != null) {

                boolean skipreeval = internalDoc.isSkipreeval() != null ? internalDoc.isSkipreeval()
                    : false;
                if (skipreeval)
                  continue;

                physical = internalDoc.getPhysicalDocumentNo();
                bPartner = internalDoc.getBusinessPartner();

                if (internalDoc.getCurrency() == null) {
                  continue;
                }
                if (internalDoc.getCurrency().getId().equals("308"))// soles no tomar en cuenta
                  continue;
              } else {
                continue;
              }

            } else {

              boolean skipreeval = inv.isScoSkipreeval() != null ? inv.isScoSkipreeval() : false;
              if (skipreeval)
                continue;

              physical = inv.getScrPhysicalDocumentno();
              bPartner = inv.getBusinessPartner();

              if (inv.getCurrency().getId().equals("308"))// soles no tomar en cuenta
                continue;
            }

            BigDecimal montoDebe = BigDecimal.ZERO;
            BigDecimal montoHaber = saldoDebe.negate();

            Account account = null;
            DocGLJournal dummy = new DocGLJournal(gljournal.getClient().getId(),
                gljournal.getOrganization().getId(), bundle.getConnection());

            if (montoHaber.compareTo(BigDecimal.ZERO) < 0) {
              montoHaber = BigDecimal.ZERO;
              montoDebe = saldoDebe;
              account = dummy.getAccount(AcctServer.ACCTTYPE_ConvertChargeDefaultAmt, schema,
                  bundle.getConnection());
            } else {
              account = dummy.getAccount(AcctServer.ACCTTYPE_ConvertGainDefaultAmt, schema,
                  bundle.getConnection());
            }

            GLJournalLine jline = OBProvider.getInstance().get(GLJournalLine.class);
            jline.setAccountingDate(gljournal.getAccountingDate());
            jline.setOrganization(gljournal.getOrganization());
            jline.setClient(gljournal.getClient());
            jline.setCredit(montoHaber);
            jline.setDebit(montoDebe);
            jline.setDescription("Asiento de reevaluacion T/C CxPagar documento " + physical);
            jline.setBusinessPartner(bPartner);
            jline.setCurrency(currSoles);
            jline.setRate(BigDecimal.ONE);
            jline.setForeignCurrencyCredit(montoHaber);
            jline.setForeignCurrencyDebit(montoDebe);
            jline.setJournalEntry(gljournal);
            jline.setLineNo((long) line++);
            if (inv != null)
              jline.setScoCInvoice(inv);
            else if (internalDoc != null)
              jline.setScoInternalDoc(internalDoc);
            // FIX-ME FALTA ENTREGA A RENDIR

            AccountingCombination ac = OBDal.getInstance().get(AccountingCombination.class,
                account.C_ValidCombination_ID);

            jline.setAccountingCombination(ac);
            OBDal.getInstance().save(jline);

            GLJournalLine jline2 = OBProvider.getInstance().get(GLJournalLine.class);
            jline2.setAccountingDate(gljournal.getAccountingDate());
            jline2.setOrganization(gljournal.getOrganization());
            jline2.setClient(gljournal.getClient());
            jline2.setCredit(montoDebe);
            jline2.setDebit(montoHaber);
            jline2.setDescription("Asiento de reevaluacion T/C CxPagar documento " + physical);
            jline2.setBusinessPartner(bPartner);
            jline2.setCurrency(currSoles);
            jline2.setRate(BigDecimal.ONE);
            jline2.setForeignCurrencyCredit(montoDebe);
            jline2.setForeignCurrencyDebit(montoHaber);
            jline2.setJournalEntry(gljournal);
            jline2.setLineNo((long) line++);

            if (inv != null)
              jline2.setScoCInvoice(inv);
            else if (internalDoc != null)
              jline2.setScoInternalDoc(internalDoc);

            AccountingCombination ac2 = OBDal.getInstance().get(AccountingCombination.class,
                acc.C_ValidCombination_ID);
            jline2.setAccountingCombination(ac2);
            jline2.setScoReevalCurrency(OBDal.getInstance().get(Currency.class, curr));

            OBDal.getInstance().save(jline2);

            counter++;

          }

        }
        result.close();
        OBDal.getInstance().flush();

      }

      if (reevalCxF) {
        String stm = "select distinct account from ("
            + " select fin_deposit_acct as account, c_acctschema_id, fa.ad_org_id,c_currency_id from fin_financial_account fa INNER JOIN fin_financial_account_acct faa ON fa.fin_financial_account_id=faa.fin_financial_account_id where c_currency_id<>'308' "
            + " union all "
            + " select fin_withdrawal_acct as account, c_acctschema_id, fa.ad_org_id,c_currency_id from fin_financial_account fa INNER JOIN fin_financial_account_acct faa ON fa.fin_financial_account_id=faa.fin_financial_account_id where c_currency_id<>'308' "
            + " union all "
            + " select fin_out_intransit_acct as account, c_acctschema_id, fa.ad_org_id,c_currency_id from fin_financial_account fa INNER JOIN fin_financial_account_acct faa ON fa.fin_financial_account_id=faa.fin_financial_account_id where c_currency_id<>'308' "
            + " union all"
            + " select fin_out_clear_acct as account, c_acctschema_id, fa.ad_org_id,c_currency_id from fin_financial_account fa INNER JOIN fin_financial_account_acct faa ON fa.fin_financial_account_id=faa.fin_financial_account_id where c_currency_id<>'308' "
            + " union all"
            + " select fin_in_intransit_acct as account, c_acctschema_id, fa.ad_org_id,c_currency_id from fin_financial_account fa INNER JOIN fin_financial_account_acct faa ON fa.fin_financial_account_id=faa.fin_financial_account_id where c_currency_id<>'308' "
            + " union all"
            + " select fin_in_clear_acct as account, c_acctschema_id, fa.ad_org_id,c_currency_id from fin_financial_account fa INNER JOIN fin_financial_account_acct faa ON fa.fin_financial_account_id=faa.fin_financial_account_id where c_currency_id<>'308' "
            + " ) as alias where " + " alias.c_acctschema_id=? and alias.c_currency_id<>'308' and"
            + " alias.account is not null and ad_isorgincluded(alias.ad_org_id, '"
            + gljournal.getOrganization().getId() + "','" + gljournal.getClient().getId()
            + "')<>-1 ";

        ResultSet result;
        PreparedStatement st = null;

        // get default eeff_cashflow values
        SCOEEFFCashflow eeffcashflow_defin = null;
        OBCriteria<SCOEEFFCashflow> oc = OBDal.getInstance().createCriteria(SCOEEFFCashflow.class);
        oc.add(Restrictions.eq(SCOEEFFCashflow.PROPERTY_CLIENT, gljournal.getClient()));
        oc.add(
            Restrictions.sqlRestriction("AD_ISORGINCLUDED('" + gljournal.getOrganization().getId()
                + "', ad_org_id, '" + gljournal.getClient().getId() + "') > -1"));
        oc.add(Restrictions.eq(SCOEEFFCashflow.PROPERTY_ISPAYINDEFAULT, true));
        oc.setMaxResults(1);
        List<SCOEEFFCashflow> ocr = oc.list();
        if (ocr.size() > 0) {
          eeffcashflow_defin = ocr.get(0);
        }

        SCOEEFFCashflow eeffcashflow_defout = null;
        OBCriteria<SCOEEFFCashflow> oc2 = OBDal.getInstance().createCriteria(SCOEEFFCashflow.class);
        oc2.add(Restrictions.eq(SCOEEFFCashflow.PROPERTY_CLIENT, gljournal.getClient()));
        oc2.add(
            Restrictions.sqlRestriction("AD_ISORGINCLUDED('" + gljournal.getOrganization().getId()
                + "', ad_org_id, '" + gljournal.getClient().getId() + "') > -1"));
        oc2.add(Restrictions.eq(SCOEEFFCashflow.PROPERTY_ISPAYOUTDEFAULT, true));
        oc2.setMaxResults(1);
        List<SCOEEFFCashflow> ocr2 = oc2.list();
        if (ocr2.size() > 0) {
          eeffcashflow_defout = ocr2.get(0);
        }

        st = bundle.getConnection().getPreparedStatement(stm);
        st.setString(1, gljournal.getAccountingSchema().getId());

        Currency currSoles = OBDal.getInstance().get(Currency.class, "308");// soles
        int counter = 0;

        result = st.executeQuery();
        // System.out.println("accc:" + gljournal.getAccountingSchema().getId());
        while (result.next()) {

          String validcombination = result.getString("account");

          AccountingCombination vac = OBDal.getInstance().get(AccountingCombination.class,
              validcombination);

          if (!vac.getAccount().getSearchKey().startsWith("10")) // solo bancos reales
            continue;

          String stmC = "select sum(case when c_currency_id<>'308' then amtsourcedr-amtsourcecr else 0 end) as source, sum(amtacctdr-amtacctcr) as soles, min(c_currency_id) as currency_id from fact_acct where account_id=? AND dateacct>=? AND dateacct<=?";

          Calendar cal = Calendar.getInstance();
          cal.setTime(endingDate);
          cal.set(Calendar.DAY_OF_YEAR, 1);
          Date iniYear = cal.getTime();

          ResultSet result2;
          PreparedStatement st2 = null;
          st2 = bundle.getConnection().getPreparedStatement(stmC);
          st2.setString(1, vac.getAccount().getId());
          st2.setTimestamp(2, new Timestamp(iniYear.getTime()));
          st2.setTimestamp(3, new Timestamp(endingDate.getTime()));

          // System.out.println("vac:" + vac.getAccount().getId());

          result2 = st2.executeQuery();

          // PARA TRAER LOS IDS DEL FLUJOS Y COSTO ASOCIADOS A LA CUENTA FINANCIERA
          stmC = "select coalesce(ffaa.em_sco_eeff_cashflow_id,'') as flujo_id,coalesce(ffaa.em_sco_costcenter_id,'') as costo_id from FIN_Financial_Account_Acct ffaa "
              + " inner join fin_financial_account ffa on ffaa.fin_financial_account_id=ffa.fin_financial_account_id "
              + " where to_char(?) in (fin_deposit_acct, fin_withdrawal_acct)  order by 1,2 limit 1 ;";

          ResultSet result3;
          PreparedStatement st3 = null;
          st3 = bundle.getConnection().getPreparedStatement(stmC);
          st3.setString(1, vac.getId());
          result3 = st3.executeQuery();

          String centroCostosId = "", flujoEfectivoId = "";
          SCOEEFFCashflow flujoEfectivo = null;
          Costcenter centroCostos = null;

          if (result3.next()) {
            centroCostosId = result3.getString("costo_id");
            flujoEfectivoId = result3.getString("flujo_id");
          }

          //

          if (result2.next()) {

            BigDecimal source = result2.getBigDecimal("source");
            BigDecimal soles = result2.getBigDecimal("soles");
            String currencyId = result2.getString("currency_id");

            if (source == null)
              continue;

            // al TC compra
            // boolean isReceipt = false;

            // al TC venta para coam
            boolean isReceipt = true;

            // String amountConvertedTo = AcctServer.getConvertedAmtGeneral(source.toString(),
            // currencyId, "308", DateAcct, "", gljournal.getClient().getId(),
            // gljournal.getOrganization().getId(), bundle.getConnection(), isReceipt, true);

            String amountConvertedTo = AcctServer.getConvertedAmtGeneral(source.toString(),
                currencyId, "308", DateAcct, "", gljournal.getClient().getId(),
                gljournal.getOrganization().getId(), bundle.getConnection(), isReceipt, false);

            BigDecimal saldo = new BigDecimal(amountConvertedTo).subtract(soles);

            if (saldo.compareTo(BigDecimal.ZERO) == 0)
              continue;

            BigDecimal montoDebe = saldo;
            BigDecimal montoHaber = BigDecimal.ZERO;

            Account account = null;
            DocGLJournal dummy = new DocGLJournal(gljournal.getClient().getId(),
                gljournal.getOrganization().getId(), bundle.getConnection());

            if (saldo.compareTo(BigDecimal.ZERO) < 0) {
              montoDebe = montoHaber;
              montoHaber = saldo.negate();
              account = dummy.getAccount(AcctServer.ACCTTYPE_ConvertChargeDefaultAmt, schema,
                  bundle.getConnection());
              centroCostos = OBDal.getInstance().get(Costcenter.class, centroCostosId);
            } else {

              account = dummy.getAccount(AcctServer.ACCTTYPE_ConvertGainDefaultAmt, schema,
                  bundle.getConnection());
            }

            System.out.println("CUENTA SOBRANTE: " + account.combination);
            System.out.println("CUENTA BANCARIA: " + vac.getCombination());

            GLJournalLine jline = OBProvider.getInstance().get(GLJournalLine.class);
            jline.setAccountingDate(gljournal.getAccountingDate());
            jline.setOrganization(gljournal.getOrganization());
            jline.setClient(gljournal.getClient());
            jline.setCredit(montoDebe);
            jline.setDebit(montoHaber);
            jline.setDescription(
                "Asiento de reevaluacion Caja y Bancos T/C cuenta " + vac.getCombination());
            jline.setBusinessPartner(null);
            jline.setCurrency(currSoles);
            jline.setRate(BigDecimal.ONE);
            jline.setForeignCurrencyCredit(montoDebe);
            jline.setForeignCurrencyDebit(montoHaber);
            jline.setJournalEntry(gljournal);
            jline.setLineNo((long) line++);
            jline.setScoCInvoice(null);

            AccountingCombination ac = OBDal.getInstance().get(AccountingCombination.class,
                account.C_ValidCombination_ID);

            jline.setAccountingCombination(ac);
            jline.setCostCenter(centroCostos);// ADD CENTRO COSTOS AUTOMATICO
            OBDal.getInstance().save(jline);

            // System.out.println("PRIMERA LINEA :");
            // System.out.println(jline.getAccountingCombination().getIdentifier());

            GLJournalLine jline2 = OBProvider.getInstance().get(GLJournalLine.class);
            jline2.setAccountingDate(gljournal.getAccountingDate());
            jline2.setOrganization(gljournal.getOrganization());
            jline2.setClient(gljournal.getClient());
            jline2.setCredit(montoHaber);
            jline2.setDebit(montoDebe);
            jline.setDescription(
                "Asiento de reevaluacion Caja y Bancos T/C cuenta " + vac.getCombination());
            jline2.setBusinessPartner(null);
            jline2.setCurrency(currSoles);
            jline2.setRate(BigDecimal.ONE);
            jline2.setForeignCurrencyCredit(montoHaber);
            jline2.setForeignCurrencyDebit(montoDebe);
            jline2.setJournalEntry(gljournal);
            jline2.setLineNo((long) line++);
            jline2.setScoCInvoice(null);

            AccountingCombination ac2 = OBDal.getInstance().get(AccountingCombination.class,
                vac.getId());

            jline2.setAccountingCombination(ac2);
            jline2.setScoReevalCurrency(OBDal.getInstance().get(Currency.class, currencyId));

            // check eeffcashflow req
            boolean req_cashflow = vac.getAccount().isScoRequiresEeffcashflow() != null
                ? vac.getAccount().isScoRequiresEeffcashflow() : false;
            if (req_cashflow) {
              if (eeffcashflow_defin == null || eeffcashflow_defout == null) {
                throw new OBException("@SCO_AcctNeedsEEFFCashflow@");
              } else {
                if (montoHaber.compareTo(montoDebe) >= 0) {
                  jline2.setScoEeffCashflow(eeffcashflow_defin);
                } else {
                  jline2.setScoEeffCashflow(eeffcashflow_defout);
                }
              }
            } else { // AGREGANDO FLUJO DE EFECTIVO AUTOMATICO
              flujoEfectivo = OBDal.getInstance().get(SCOEEFFCashflow.class, flujoEfectivoId);
              jline2.setScoEeffCashflow(flujoEfectivo);
            }

            OBDal.getInstance().save(jline2);

            // System.out.println("SEGUNDA LINEA :");
            // System.out.println(jline2.toString());

            counter++;

          }

          result2.close();

        }
        result.close();
        OBDal.getInstance().flush();

      }

      final OBError msg = new OBError();
      msg.setType("Success");
      msg.setTitle("@Success@");

      bundle.setResult(msg);
      OBDal.getInstance().commitAndClose();

    } catch (final OBException e) {
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(e.getMessage());
      msg.setTitle("@Error@");
      OBDal.getInstance().rollbackAndClose();
      bundle.setResult(msg);
    } catch (final Exception e) {
      OBDal.getInstance().rollbackAndClose();

      String message = DbUtility.getUnderlyingSQLException(e).getMessage();
      if (message.contains("@"))
        message = OBMessageUtils.parseTranslation(message);

      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(message);
      msg.setTitle("@Error@");
      bundle.setResult(msg);
    }
  }
}