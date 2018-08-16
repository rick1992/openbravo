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

package pe.com.unifiedgo.sales.ad_actionButton;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.RequestContext;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.reference.PInstanceProcessData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.CallProcess;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.service.db.DalConnectionProvider;

import pe.com.unifiedgo.accounting.data.SCOBillofexchange;

public class BOEManagementComplete extends DalBaseProcess {
  private final AdvPaymentMngtDao dao = new AdvPaymentMngtDao();

  @Override
  protected void doExecute(ProcessBundle bundle) throws Exception {
    OBError errorMsg = null;

    DalConnectionProvider conn = new DalConnectionProvider();
    HttpServletRequest request = RequestContext.get().getRequest();
    VariablesSecureApp vars = new VariablesSecureApp(request);

    try {
      // for (Map.Entry<String, Object> entry : bundle.getParams().entrySet()) {
      // System.out.println(entry.getKey() + "/" + entry.getValue());
      // }

      String BOEManagementId = (String) bundle.getParams().get("SCO_Billofexchange_ID");
      String docaction = "CO";

      SCOBillofexchange objBOEManagement = OBDal.getInstance().get(SCOBillofexchange.class,
          BOEManagementId);

      for (Invoice boe : objBOEManagement.getInvoiceEMScoBoeIDList()) {
        if (!boe.isProcessed() && docaction.equals(boe.getDocumentAction())) {
          // Processing Bill Of Exchange (Invoice)
          OBContext.setAdminMode(true);
          Process process = null;
          try {
            process = dao.getObject(Process.class, "111");
          } finally {
            OBContext.restorePreviousMode();
          }

          Map<String, String> parameters = null;
          ProcessInstance pinstance = CallProcess.getInstance().call(process, boe.getId(),
              parameters);

          boe.setAPRMProcessinvoice(boe.getDocumentAction());
          OBDal.getInstance().save(boe);
          OBDal.getInstance().flush();

          OBContext.setAdminMode();
          try {
            // on error close popup
            if (pinstance.getResult() == 0L) {
              PInstanceProcessData[] pinstanceData = PInstanceProcessData.select(conn,
                  pinstance.getId());
              errorMsg = Utility.getProcessInstanceMessage(conn, vars, pinstanceData);
              throw new OBException(errorMsg.getMessage());
            }
          } finally {
            OBContext.restorePreviousMode();
          }
          // End processing
        }
      }

      final OBError msg = new OBError();
      msg.setType("Success");
      msg.setTitle("@Success@");
      OBDal.getInstance().commitAndClose();
      bundle.setResult(msg);

    } catch (final OBException e) {
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(e.getMessage());
      msg.setTitle("@Error@");
      OBDal.getInstance().rollbackAndClose();
      bundle.setResult(msg);
    }
  }
}
