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
 * All portions are Copyright (C) 2014 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */

package org.openbravo.module.cashvat.modulescript; 

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import org.apache.log4j.Logger;
import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.openbravo.database.ConnectionProvider;
import org.openbravo.modulescript.ModuleScript;

public class AddMissingCashVATPayments extends ModuleScript {
  private static final Logger log4j = Logger.getLogger(AddMissingCashVATPayments.class);

  @Override
  public void execute() {
    try {
      ConnectionProvider cp = getConnectionProvider();
      deleteCashVATInfoWithoutPaymentDetail(cp);
      createCashVATInfo(cp);
      } catch (Exception e) {
      handleError(e);
    }
  }

    private void deleteCashVATInfoWithoutPaymentDetail(ConnectionProvider cp) throws Exception {
	int deleted = AddMissingCashVATPaymentsData.deleteCashVATInfoWithoutPaymentDetail(cp);
	if (deleted > 0) {
	    log4j.info("Deleted Cash VAT Info lines without payment detail info: " + deleted);
	}
    }

    private void createCashVATInfo(ConnectionProvider cp) throws Exception {
	int totalMatched = 0;
	
	AddMissingCashVATPaymentsData[] missedFPDs = AddMissingCashVATPaymentsData.selectMissedPaymentDetails(cp);

	for (int i = 0; i < missedFPDs.length; i++) {
	    final String invoiceId = missedFPDs[i].cInvoiceId;
	    final String isPaid = missedFPDs[i].ispaid;
	    final String invoiceTaxId = missedFPDs[i].cInvoicetaxId;
	    final String finPaymentDetailId = missedFPDs[i].finPaymentDetailId;
	    final String stdPrecision = missedFPDs[i].stdprecision;
	    final String percentage = missedFPDs[i].percentage;

	    boolean isFinalAdjustment = false;
	    if ("Y".equals(isPaid)) {
		final String lastPaymentDetailId = AddMissingCashVATPaymentsData.selectLastPaymentDetail(cp, invoiceId)[0].finPaymentDetailId;
		if (finPaymentDetailId.equals(lastPaymentDetailId)) {
		    isFinalAdjustment = true;
		}
	    }

	    if (isFinalAdjustment) {
		totalMatched = totalMatched + AddMissingCashVATPaymentsData.insertCashVATInfoLastPayment(cp, invoiceTaxId, finPaymentDetailId);
	    } else {
		totalMatched = totalMatched + AddMissingCashVATPaymentsData.insertCashVATInfoNonLastPayment(cp, invoiceTaxId, percentage, stdPrecision, finPaymentDetailId);
	    }

	}

	if (totalMatched > 0) {
	    log4j.info("Fixed Invoice Tax Cash VAT lines: " + totalMatched);
	}
    }
}