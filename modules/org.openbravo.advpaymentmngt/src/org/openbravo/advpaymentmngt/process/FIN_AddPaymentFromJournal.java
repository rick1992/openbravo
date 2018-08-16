/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.0  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License.
 * The Original Code is Openbravo ERP.
 * The Initial Developer of the Original Code is Openbravo SLU
 * All portions are Copyright (C) 2012-2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package org.openbravo.advpaymentmngt.process;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.gl.GLItemAccounts;
import org.openbravo.model.financialmgmt.gl.GLJournal;
import org.openbravo.model.financialmgmt.gl.GLJournalLine;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.CallProcess;
import org.openbravo.service.db.CallStoredProcedure;
import org.openbravo.service.db.DalBaseProcess;

import pe.com.unifiedgo.accounting.SCO_Utils;
import pe.com.unifiedgo.accounting.data.SCOPrepayment;

public class FIN_AddPaymentFromJournal extends DalBaseProcess {

  @Override
  protected void doExecute(ProcessBundle bundle) throws Exception {
    int cont = 0;

    String dateFormatString = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatString);

    // Recover context and variables
    ConnectionProvider conn = bundle.getConnection();
    VariablesSecureApp varsAux = bundle.getContext().toVars();
    HttpServletRequest request = RequestContext.get().getRequest();
    User user = OBDal.getInstance().get(User.class, varsAux.getUser());
    OBContext.setOBContext(varsAux.getUser(), varsAux.getRole(), varsAux.getClient(),
        varsAux.getOrg());
    VariablesSecureApp vars = new VariablesSecureApp(request);

    try {

      // retrieve the parameters from the bundle
      final String journalId = (String) bundle.getParams().get("GL_Journal_ID");

      // in case it was called from other process
      String docAction = (String) bundle.getParams().get("inpdocaction");
      if (docAction == null)
        docAction = vars.getStringParameter("inpdocaction");

      if ("".equals(docAction)) {
        docAction = "CO";
      }

      // Set the docAction of the Journal (Complete, Reactivate, Close...)
      GLJournal journal = OBDal.getInstance().get(GLJournal.class, journalId);
      journal.setDocumentAction(docAction);
      OBDal.getInstance().flush();
      OBDal.getInstance().refresh(journal);

      // If complete check for account number: if 33XXXX or 34XXXX
      if (docAction.equals("CO")) {
        boolean needs_asset = false;
        for (GLJournalLine journalLine : journal.getFinancialMgmtGLJournalLineList()) {
          String account_number = journalLine.getAccountingCombination().getAccount()
              .getSearchKey();
          if (account_number.substring(0, 2).equals("33")
              || account_number.substring(0, 2).equals("34")) {
            needs_asset = true;
            break;
          }
        }

        /*
         * if (needs_asset) { Asset asset = journal.getAsset(); if (asset == null) { throw new
         * OBException("@SCO_JournalCONeedsAsset@"); } }
         */

      }

      // Check if the Lines of the Journal have related Payments. In that case
      // the Payments must be deleted before Closing or Reactivating the line.
      String relatedPayments = "";
      if (!"CO".equals(docAction)) {
        for (GLJournalLine journalLine : journal.getFinancialMgmtGLJournalLineList()) {
          if (journalLine.getRelatedPayment() != null) {
            relatedPayments = relatedPayments + journalLine.getLineNo() + ", ";
          }
        }
      }
      if (!"".equals(relatedPayments)) {
        relatedPayments = relatedPayments.substring(0, relatedPayments.length() - 2);
        throw new OBException("@FIN_JournalLineRelatedPayments@: " + relatedPayments);
      }

      try {
        // Call GL_Journal_Post method from the database.
        final List<Object> parameters = new ArrayList<Object>();
        parameters.add(null);
        parameters.add(journalId);
        final String procedureName = "gl_journal_post";
        CallStoredProcedure mm = CallStoredProcedure.getInstance();
        mm.call(procedureName, parameters, null, false, false);
      } catch (Exception e) {
        OBDal.getInstance().rollbackAndClose();
        OBError error = OBMessageUtils.translateError(conn, vars, vars.getLanguage(),
            e.getCause().getMessage());
        throw new OBException(error.getMessage());
      }

      OBDal.getInstance().refresh(journal);
      Date date = journal.getDocumentDate();

      // Complete the Journal
      if ("CO".equals(docAction)) {
        for (GLJournalLine journalLine : journal.getFinancialMgmtGLJournalLineList()) {
          // Recover again the object to avoid problems with Dal
          journalLine = OBDal.getInstance().get(GLJournalLine.class, journalLine.getId());
          if (journalLine.isOpenItems() && journalLine.getRelatedPayment() == null) {
            // Create bundle
            vars = new VariablesSecureApp(varsAux.getUser(), varsAux.getClient(), varsAux.getOrg(),
                varsAux.getRole(), varsAux.getLanguage());
            ProcessBundle pb = new ProcessBundle("DE1B382FDD2540199D223586F6E216D0", vars)
                .init(conn);
            HashMap<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("GL_JournalLine_ID", journalLine.getId());
            pb.setParams(parameters);
            OBError myMessage = null;
            // Create a Payment for the Journal line
            FIN_AddPaymentFromJournalLine myProcess = new FIN_AddPaymentFromJournalLine();
            myProcess.setDoCommit(false);
            myProcess.execute(pb);
            myMessage = (OBError) pb.getResult();

            if (myMessage.getType().equals("Error")) {
              throw new OBException("@FIN_PaymentFromJournalError@ " + journalLine.getLineNo()
                  + " - " + myMessage.getMessage());
            }
            cont++;
          }
        }
      }

      // SCO_CREATEPREP IMPL
      if ("CO".equals(docAction)) {
        for (GLJournalLine journalLine : journal.getFinancialMgmtGLJournalLineList()) {

          if (journalLine.isScoCreateprep() && !journalLine.isScoLocked()) {
            OBCriteria<GLItemAccounts> aoc = OBDal.getInstance()
                .createCriteria(GLItemAccounts.class);
            aoc.add(Restrictions.eq(GLItemAccounts.PROPERTY_CLIENT, journal.getClient()));
            aoc.add(Restrictions.eq(GLItemAccounts.PROPERTY_ACCOUNTINGSCHEMA,
                journal.getAccountingSchema()));
            aoc.add(Restrictions.eq(GLItemAccounts.PROPERTY_GLITEMCREDITACCT,
                journalLine.getAccountingCombination()));
            List<GLItemAccounts> glitemaccs = aoc.list();
            if (glitemaccs.size() == 0) {
              throw new OBException("@SCO_JournalCreatePrepNoAccts@: "
                  + journalLine.getAccountingCombination().getIdentifier());
            }

            GLItem glitem = glitemaccs.get(0).getGLItem();
            SCOPrepayment prepayment = createSCOPrepayment(conn, journal, journalLine, glitem,
                user);
            OBDal.getInstance().save(prepayment);
            OBDal.getInstance().flush();

            OBContext.setAdminMode(true);
            Process process = null;
            try {
              process = OBDal.getInstance().get(Process.class, "87F637F610B542FE8A7432214DC09321");
            } finally {
              OBContext.restorePreviousMode();
            }

            Map<String, String> parameters = new HashMap<String, String>();
            ;
            final ProcessInstance pinstance = CallProcess.getInstance().call(process,
                prepayment.getId(), parameters);

            if (pinstance.getResult() == 0L) {
              final PInstanceProcessData[] pinstanceData = PInstanceProcessData.select(conn,
                  pinstance.getId());
              OBError myMessage = Utility.getProcessInstanceMessage(conn, vars, pinstanceData);
              throw new OBException(myMessage.getMessage());
            }

            OBDal.getInstance().save(prepayment);
            journalLine.setScoPrepayment(prepayment);
            OBDal.getInstance().save(journalLine);
            OBDal.getInstance().flush();
          }
        }

        // SCO_PREPAYMENT CO LOGIC
        for (GLJournalLine journalline : journal.getFinancialMgmtGLJournalLineList()) {
          SCOPrepayment prepayment = journalline.getScoPrepayment();
          if (prepayment != null && !journalline.isScoLocked()) {

            BigDecimal amount;
            if (journalline.getDebit().compareTo(new BigDecimal(0)) > 0) {
              if (journalline.getScoOtherCurrency() != null && journalline.getScoOtherCurrency()
                  .getId().equals(prepayment.getCurrency().getId()))
                amount = journalline.getDebit();
              else
                amount = journalline.getForeignCurrencyDebit();

              if (prepayment.isSalesTransaction())
                amount = amount.negate();

            } else {
              if (journalline.getScoOtherCurrency() != null && journalline.getScoOtherCurrency()
                  .getId().equals(prepayment.getCurrency().getId()))
                amount = journalline.getCredit();
              else
                amount = journalline.getForeignCurrencyCredit();

              if (!prepayment.isSalesTransaction())
                amount = amount.negate();
            }

            if ((amount.compareTo(new BigDecimal(0)) >= 0
                && prepayment.getAmount().compareTo(new BigDecimal(0)) >= 0)
                || (amount.compareTo(new BigDecimal(0)) <= 0
                    && prepayment.getAmount().compareTo(new BigDecimal(0)) <= 0)) {
              // Es pago de apertura
              if (prepayment.isPaymentComplete() || amount.compareTo(prepayment.getAmount()) != 0) {
                throw new OBException("@FIN_PaymentFromJournalError@ " + journalline.getLineNo()
                    + " -- " + "@SCO_JournalPrepaymentOPBadAmount@");
              }

              if ((prepayment.getAmount().compareTo(new BigDecimal(0)) >= 0
                  && prepayment.getTotalPaid().add(amount).compareTo(prepayment.getAmount()) > 0)
                  || (prepayment.getAmount().compareTo(new BigDecimal(0)) < 0 && prepayment
                      .getTotalPaid().add(amount).compareTo(prepayment.getAmount()) < 0)) {
                throw new OBException("@FIN_PaymentFromJournalError@ " + journalline.getLineNo()
                    + " - " + "@SCO_JournalPrepaymentOPBadAmount@");
              }

              prepayment.setTotalPaid(prepayment.getTotalPaid().add(amount));
              if (prepayment.getAmount().compareTo(new BigDecimal(0)) >= 0) {
                if (prepayment.getTotalPaid().compareTo(prepayment.getAmount()) >= 0) {
                  prepayment.setPaymentComplete(true);
                } else {
                  prepayment.setPaymentComplete(false);
                }
              } else {
                if (prepayment.getTotalPaid().compareTo(prepayment.getAmount()) <= 0) {
                  prepayment.setPaymentComplete(true);
                } else {
                  prepayment.setPaymentComplete(false);
                }
              }
              prepayment.setJournalLine(journalline);
              OBDal.getInstance().save(prepayment);
              OBDal.getInstance().flush();

            } else {
              // Es aplicación del documento vario
              if (!prepayment.isPaymentComplete()) {
                throw new OBException("@FIN_PaymentFromJournalError@ " + journalline.getLineNo()
                    + " - " + "@SCO_JournalPrepaymentAppNotOP@");
              }

              BigDecimal appamount = amount.negate();
              if ((prepayment.getAmount().compareTo(new BigDecimal(0)) >= 0 && prepayment
                  .getUsedamount().add(appamount).compareTo(prepayment.getAmount()) > 0)
                  || (prepayment.getAmount().compareTo(new BigDecimal(0)) < 0 && prepayment
                      .getUsedamount().add(appamount).compareTo(prepayment.getAmount()) < 0)) {
                throw new OBException("@FIN_PaymentFromJournalError@ " + journalline.getLineNo()
                    + " - " + "@SCO_JournalPrepaymentAppBadAmount@");
              }

              prepayment.setUsedamount(prepayment.getUsedamount().add(appamount));
              OBDal.getInstance().save(prepayment);
              OBDal.getInstance().flush();
            }
          }
        }

        // unlock all
        List<GLJournalLine> gllines = journal.getFinancialMgmtGLJournalLineList();
        for (int i = 0; i < gllines.size(); i++) {
          GLJournalLine journalLine = gllines.get(i);
          journalLine.setScoLocked(false);
          OBDal.getInstance().save(journalLine);
          OBDal.getInstance().flush();
        }

      } else if ("RE".equals(docAction)) {

        // lock some
        List<GLJournalLine> gllines = journal.getFinancialMgmtGLJournalLineList();
        for (int i = 0; i < gllines.size(); i++) {
          GLJournalLine journalLine = gllines.get(i);
          if (journalLine.isScoCreateprep()) {
            SCOPrepayment prepayment = journalLine.getScoPrepayment();
            if (prepayment != null) {
              if (prepayment.getUsedamount().compareTo(new BigDecimal(0)) != 0) {
                // has payments. Allow to reactivate but lock the line
                journalLine.setScoLocked(true);
                OBDal.getInstance().save(journalLine);
                OBDal.getInstance().flush();
              }
            }
          }
        }

        // SCO_PREPAYMENT RE LOGIC
        for (GLJournalLine journalline : journal.getFinancialMgmtGLJournalLineList()) {
          SCOPrepayment prepayment = journalline.getScoPrepayment();
          if (prepayment != null && !journalline.isScoLocked()) {
            BigDecimal amount;
            if (journalline.getDebit().compareTo(new BigDecimal(0)) > 0) {
              if (journalline.getScoOtherCurrency() != null && journalline.getScoOtherCurrency()
                  .getId().equals(prepayment.getCurrency().getId()))
                amount = journalline.getDebit();
              else
                amount = journalline.getForeignCurrencyDebit();

              if (prepayment.isSalesTransaction())
                amount = amount.negate();

            } else {
              if (journalline.getScoOtherCurrency() != null && journalline.getScoOtherCurrency()
                  .getId().equals(prepayment.getCurrency().getId()))
                amount = journalline.getCredit();
              else
                amount = journalline.getForeignCurrencyCredit();

              if (!prepayment.isSalesTransaction())
                amount = amount.negate();
            }

            if ((amount.compareTo(new BigDecimal(0)) >= 0
                && prepayment.getAmount().compareTo(new BigDecimal(0)) >= 0)
                || (amount.compareTo(new BigDecimal(0)) <= 0
                    && prepayment.getAmount().compareTo(new BigDecimal(0)) <= 0)) {
              // Es pago de apertura
              prepayment.setTotalPaid(prepayment.getTotalPaid().subtract(amount));

              if (prepayment.getAmount().compareTo(new BigDecimal(0)) >= 0) {
                if (prepayment.getTotalPaid().compareTo(prepayment.getAmount()) >= 0) {
                  prepayment.setPaymentComplete(true);
                } else {
                  prepayment.setPaymentComplete(false);
                }
              } else {
                if (prepayment.getTotalPaid().compareTo(prepayment.getAmount()) <= 0) {
                  prepayment.setPaymentComplete(true);
                } else {
                  prepayment.setPaymentComplete(false);
                }
              }
              prepayment.setJournalLine(null);
              OBDal.getInstance().save(prepayment);
              OBDal.getInstance().flush();

            } else {
              // Es aplicación del documento vario
              BigDecimal appamount = amount.negate();

              prepayment.setUsedamount(prepayment.getUsedamount().subtract(appamount));
              OBDal.getInstance().save(prepayment);
              OBDal.getInstance().flush();
            }
          }
        }

        for (GLJournalLine journalLine : journal.getFinancialMgmtGLJournalLineList()) {
          if (journalLine.isScoCreateprep() && !journalLine.isScoLocked()) {
            SCOPrepayment prepayment = journalLine.getScoPrepayment();

            if (prepayment != null) {
              prepayment.setTotalPaid(new BigDecimal(0));
              prepayment.setDocumentAction("RE");
              prepayment.setPaymentComplete(false);
              prepayment.setJournalLine(null);

              journalLine.setScoPrepayment(null);
              OBDal.getInstance().save(journalLine);
              OBDal.getInstance().flush();

              OBDal.getInstance().save(prepayment);
              OBDal.getInstance().flush();

              OBContext.setAdminMode(true);
              Process process = null;
              try {
                process = OBDal.getInstance().get(Process.class,
                    "87F637F610B542FE8A7432214DC09321");
              } finally {
                OBContext.restorePreviousMode();
              }

              Map<String, String> parameters = new HashMap<String, String>();
              ;
              final ProcessInstance pinstance = CallProcess.getInstance().call(process,
                  prepayment.getId(), parameters);

              if (pinstance.getResult() == 0L) {
                final PInstanceProcessData[] pinstanceData = PInstanceProcessData.select(conn,
                    pinstance.getId());
                OBError myMessage = Utility.getProcessInstanceMessage(conn, vars, pinstanceData);
                throw new OBException(myMessage.getMessage());
              }

              OBDal.getInstance().remove(prepayment);
              OBDal.getInstance().flush();
            }

          }
        }
      }

      // OBError is also used for successful results
      final OBError msg = new OBError();
      msg.setType("Success");
      msg.setTitle("@Success@");
      if (cont > 0) {
        msg.setMessage(" @FIN_NumberOfPayments@: " + cont);
      }
      bundle.setResult(msg);
      OBDal.getInstance().commitAndClose();
    } catch (final OBException e) {
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(e.getMessage());
      msg.setTitle("@Error@");
      OBDal.getInstance().rollbackAndClose();
      bundle.setResult(msg);
    }

  }

  SCOPrepayment createSCOPrepayment(ConnectionProvider conProvider, GLJournal journal,
      GLJournalLine journalline, GLItem glitem, User user) throws OBException, ServletException {

    SCOPrepayment prepayment = OBProvider.getInstance().get(SCOPrepayment.class);
    prepayment.setClient(journal.getClient());
    prepayment.setOrganization(journal.getOrganization());
    prepayment.setCreatedBy(user);
    prepayment.setUpdatedBy(user);
    prepayment.setCreationDate(new Date());
    prepayment.setUpdated(new Date());
    prepayment.setActive(true);

    prepayment.setBusinessPartner(journalline.getBusinessPartner());
    if (journalline.getScoOtherCurrency() != null)
      prepayment.setCurrency(journalline.getScoOtherCurrency());
    else
      prepayment.setCurrency(journalline.getCurrency());

    prepayment.setDocumentNo(journalline.getScoPrepDocumentno());
    prepayment.setDocumentStatus("DR");
    prepayment.setDocumentAction("CO");
    prepayment.setProcessed(false);
    prepayment.setProcessNow(false);
    prepayment.setGeneratedDate(journalline.getScoPrepDategen());
    prepayment.setDocumentType(OBDal.getInstance().get(DocumentType.class, "0"));
    prepayment.setTransactionDocument(
        SCO_Utils.getDocTypeFromSpecial(prepayment.getOrganization(), "SCOPREPAYMENT"));
    prepayment.setPaymentglitem(glitem);

    BigDecimal amount;
    if (journalline.getDebit().compareTo(new BigDecimal(0)) > 0) {
      if (journalline.getScoOtherCurrency() != null)
        amount = journalline.getDebit();
      else
        amount = journalline.getForeignCurrencyDebit();

      if (journalline.isScoPrepIssotrx())
        amount = amount.negate();

    } else {
      if (journalline.getScoOtherCurrency() != null)
        amount = journalline.getCredit();
      else
        amount = journalline.getForeignCurrencyCredit();

      if (!journalline.isScoPrepIssotrx())
        amount = amount.negate();

    }
    prepayment.setAmount(amount);
    prepayment.setDescription(journalline.getDescription());
    prepayment.setDuedate(journalline.getScoPrepDuedate());
    prepayment.setDoctype("OTHER");
    prepayment.setSalesTransaction(journalline.isScoPrepIssotrx());

    return prepayment;
  }
}
