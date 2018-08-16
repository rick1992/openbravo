package it.extrasys.utility.invoice.biz;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.service.db.CallProcess;

public class PostInvoice {
  private static final Logger log = Logger.getLogger(PostInvoice.class);

  private static PostInvoice singleton;

  private PostInvoice() {
  }

  public static PostInvoice getSingleton() {
    if (singleton == null) {
      singleton = new PostInvoice();
    }
    return singleton;
  }

  /**
   * Call C_Order_Post
   */
  public ProcessInstance postInvoice(Invoice objInvoice) {
    ProcessInstance processInstance = null;
    try {
      // get an AD_Process instance, 104 is the C_Order_Post process
      final org.openbravo.model.ad.ui.Process process = OBDal.getInstance().get(
          org.openbravo.model.ad.ui.Process.class, "111");
      processInstance = CallProcess.getInstance().call(process, objInvoice.getId(),
          new HashMap<String, String>());
      // the processInstance now contains the result

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Inside Invoice post method ", e);
    }
    return processInstance;
  }
}
