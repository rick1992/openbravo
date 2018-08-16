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
 * All portions are Copyright (C) 2011-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package pe.com.unifiedgo.accounting.actionhandler;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.application.process.BaseProcessActionHandler;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.gl.GLJournalLine;
import org.openbravo.service.db.DbUtility;

import pe.com.unifiedgo.accounting.data.SCOInternalDoc;
import pe.com.unifiedgo.accounting.data.SCOPrepayment;

/**
 * 
 * @author gorkaion
 * 
 */
public class SCOFactAcctCurBalancePickEdit extends BaseProcessActionHandler {
  private static Logger log = Logger.getLogger(SCOFactAcctCurBalancePickEdit.class);

  @Override
  protected JSONObject doExecute(Map<String, Object> parameters, String content) {
    JSONObject jsonRequest = null;
    JSONObject errorMessage = new JSONObject();
    OBContext.setAdminMode();
    try {
      jsonRequest = new JSONObject(content);
      log.debug(jsonRequest);
      errorMessage.put("severity", "success");
      errorMessage.put("text", OBMessageUtils.messageBD("Success"));
      jsonRequest = new JSONObject(content);

      // When the focus is NOT in the tab of the button (i.e. any child tab) and the tab does not
      // contain any record, the inpglJournallineId parameter contains "null" string. Use
      // GL_JournalLine_ID
      // instead because it always contains the id of the selected lines.
      // Issue 20585: https://issues.openbravo.com/view.php?id=20585
      final String strGLJournalLineId = jsonRequest.getString("GL_JournalLine_ID");

      GLJournalLine gljournalli = OBDal.getInstance().get(GLJournalLine.class, strGLJournalLineId);
      if (gljournalli != null) {
        if ("DR".compareTo(gljournalli.getJournalEntry().getDocumentStatus()) != 0) {
          throw new Exception("@SCO_CannotModifiedCompletedDoc@");
        }

        updateGLJournalLines(gljournalli, jsonRequest);
      }

      jsonRequest.put("message", errorMessage);

    } catch (Exception e) {
      log.error(e.getMessage(), e);

      try {
        jsonRequest = new JSONObject();
        Throwable ex = DbUtility.getUnderlyingSQLException(e);
        String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();
        errorMessage.put("severity", "error");
        errorMessage.put("text", message);
        jsonRequest.put("message", errorMessage);

      } catch (Exception e2) {
        log.error(e.getMessage(), e2);
        // do nothing, give up
      }
    } finally {
      OBContext.restorePreviousMode();
    }
    return jsonRequest;
  }

  private void updateGLJournalLines(GLJournalLine gljournalli, JSONObject jsonRequest)
      throws JSONException {
    JSONArray selectedLines = jsonRequest.getJSONArray("_selection");
    // if no lines selected don't do anything.
    if (selectedLines.length() == 0) {
      return;
    }
    if (selectedLines.length() > 1) {
      throw new JSONException("@SCO_SelectOnlyOneLine@");
    }

    // Processing line
    JSONObject selectedLine = selectedLines.getJSONObject((int) 0);

    BigDecimal foreignCurrencyDebit = new BigDecimal(
        selectedLine.getString("foreignCurrencyDebit"));
    BigDecimal foreignCurrencyCredit = new BigDecimal(
        selectedLine.getString("foreignCurrencyCredit"));
    BigDecimal debit = new BigDecimal(selectedLine.getString("debit"));
    BigDecimal credit = new BigDecimal(selectedLine.getString("credit"));
    BigDecimal rate = new BigDecimal(selectedLine.getString("rate"));

    gljournalli.setForeignCurrencyDebit(foreignCurrencyCredit);
    gljournalli.setForeignCurrencyCredit(foreignCurrencyDebit);
    gljournalli.setDebit(credit);
    gljournalli.setCredit(debit);
    gljournalli.setRate(rate);

    String bpartnerId = selectedLine.getString("businessPartner");
    if (bpartnerId == null || bpartnerId == "null") {
      gljournalli.setBusinessPartner(null);
    } else {
      gljournalli.setBusinessPartner(OBDal.getInstance().get(BusinessPartner.class, bpartnerId));
    }

    String cInvoiceId = selectedLine.getString("invoice");
    if (cInvoiceId == null || cInvoiceId == "null") {
      gljournalli.setScoCInvoice(null);
    } else {
      gljournalli.setScoCInvoice(OBDal.getInstance().get(Invoice.class, cInvoiceId));
    }

    String scoInternalDocId = selectedLine.getString("documentoInterno");
    if (scoInternalDocId == null || scoInternalDocId == "null") {
      gljournalli.setScoInternalDoc(null);
    } else {
      gljournalli
          .setScoInternalDoc(OBDal.getInstance().get(SCOInternalDoc.class, scoInternalDocId));
    }

    String scoPrepaymentId = selectedLine.getString("documentosVarios");
    if (scoPrepaymentId == null || scoPrepaymentId == "null") {
      gljournalli.setScoPrepayment(null);
    } else {
      gljournalli.setScoPrepayment(OBDal.getInstance().get(SCOPrepayment.class, scoPrepaymentId));
    }

    OBDal.getInstance().save(gljournalli);
    OBDal.getInstance().save(gljournalli.getJournalEntry());
    OBDal.getInstance().refresh(gljournalli.getJournalEntry());
    OBDal.getInstance().flush();
  }

}