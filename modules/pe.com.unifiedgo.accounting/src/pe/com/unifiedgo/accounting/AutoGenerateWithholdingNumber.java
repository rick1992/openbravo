package pe.com.unifiedgo.accounting;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

import pe.com.unifiedgo.accounting.data.SCOPwithholdingReceipt;
import pe.com.unifiedgo.accounting.data.SerieSunat;

public class AutoGenerateWithholdingNumber extends DalBaseProcess {
  public void doExecute(ProcessBundle bundle) throws Exception {
    try {

      /*
       * Set<String> params = bundle.getParams().keySet(); for (int i = 0; i < params.size(); i++) {
       * System.out.println(params.toArray()[i]); }
       */

      final String SCO_Pwithholding_Receipt_ID = (String) bundle.getParams().get("SCO_Pwithholding_Receipt_ID");
      final String scoSerieSunatId = (String) bundle.getParams().get("scoSerieSunatId");

      SerieSunat serieSunat = OBDal.getInstance().get(SerieSunat.class, scoSerieSunatId);
      SCOPwithholdingReceipt pwithholding_receipt = OBDal.getInstance().get(SCOPwithholdingReceipt.class, SCO_Pwithholding_Receipt_ID);
      if (serieSunat == null || pwithholding_receipt == null) {
        throw new Exception("Internal Error Null");
      }
      boolean isebill = serieSunat.isBillIsebill() != null ? serieSunat.isBillIsebill() : false;

      long nextNumber = serieSunat.getCurrentNumber();
      nextNumber++;
      if (isebill) {
        pwithholding_receipt.setWithholdingnumber(serieSunat.getSerieName() + "-" + SCO_Utils.getFormatedNumber(nextNumber, 8));
        pwithholding_receipt.setBillIsebill(true);
        pwithholding_receipt.setBillEbillingStatus("NE");
      } else {
        pwithholding_receipt.setWithholdingnumber(serieSunat.getSerieName() + "-" + SCO_Utils.getFormatedNumber(nextNumber, 6));

      }
      pwithholding_receipt.setWnumberautogen(true);
      pwithholding_receipt.setSUNATSerialForAutoGeneration(serieSunat);

      serieSunat.setCurrentNumber(nextNumber);

      OBDal.getInstance().save(pwithholding_receipt);
      OBDal.getInstance().save(serieSunat);

      final OBError msg = new OBError();
      msg.setType("Success");
      msg.setTitle(OBMessageUtils.getI18NMessage("OBUIAPP_Success", null));
      msg.setMessage(OBMessageUtils.getI18NMessage("SCO_AutoGenerateWithholdingNum_Success", null));
      bundle.setResult(msg);

    } catch (final Exception e) {
      e.printStackTrace(System.err);
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(e.getMessage());
      msg.setTitle("Error occurred");
      bundle.setResult(msg);
    }
  }

  public String getFormatedNumber(long number, int numdig) {
    String fnumber = Long.toString(number);
    int newlen = (numdig - fnumber.length());
    for (int i = 0; i < newlen; i++) {
      fnumber = "0" + fnumber;
    }

    return fnumber;
  }
}