package pe.com.unifiedgo.accounting;

import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

import pe.com.unifiedgo.accounting.data.SCOPercepSales;
import pe.com.unifiedgo.accounting.data.SerieSunat;

public class AutoGeneratePerceptionNumber extends DalBaseProcess {
  public void doExecute(ProcessBundle bundle) throws Exception {
    try {

      /*
       * Set<String> params = bundle.getParams().keySet(); for (int i = 0; i < params.size(); i++) {
       * System.out.println(params.toArray()[i]); }
       */

      final String SCO_Percep_Sales_ID = (String) bundle.getParams().get("SCO_Percep_Sales_ID");
      final String scoSerieSunatId = (String) bundle.getParams().get("scoSerieSunatId");

      SerieSunat serieSunat = OBDal.getInstance().get(SerieSunat.class, scoSerieSunatId);
      SCOPercepSales purchase_perception_receipt = OBDal.getInstance().get(SCOPercepSales.class, SCO_Percep_Sales_ID);
      if (serieSunat == null || purchase_perception_receipt == null) {
        throw new Exception("Internal Error Null");
      }

      long nextNumber = serieSunat.getCurrentNumber();
      nextNumber++;
      purchase_perception_receipt.setPerceptionNumber(serieSunat.getSerieName() + "-" + SCO_Utils.getFormatedNumber(nextNumber, 6));
      purchase_perception_receipt.setPnumberautogen(true);
      purchase_perception_receipt.setSUNATSerialForAutoGeneration(serieSunat);

      serieSunat.setCurrentNumber(nextNumber);

      OBDal.getInstance().save(purchase_perception_receipt);
      OBDal.getInstance().save(serieSunat);

      final OBError msg = new OBError();
      msg.setType("Success");
      msg.setTitle(OBMessageUtils.getI18NMessage("OBUIAPP_Success", null));
      msg.setMessage(OBMessageUtils.getI18NMessage("SCO_AutoGeneratePerceptionNum_Success", null));
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
}