package it.extrasys.utility.order.biz;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.common.order.Order;
import org.openbravo.service.db.CallProcess;

public class PostOrder {

  private static final Logger log = Logger.getLogger(PostOrder.class);

  private static PostOrder singleton;

  private PostOrder() {
  }

  public static PostOrder getSingleton() {
    if (singleton == null) {
      singleton = new PostOrder();
    }
    return singleton;
  }

  /**
   * Call C_Order_Post
   */
  public ProcessInstance postOrder(Order objOrder) {
    ProcessInstance processInstance = null;
    try {
      // get an AD_Process instance, 104 is the C_Order_Post process
      final org.openbravo.model.ad.ui.Process process = OBDal.getInstance().get(
          org.openbravo.model.ad.ui.Process.class, "104");
      processInstance = CallProcess.getInstance().call(process, objOrder.getId(),
          new HashMap<String, String>());
      // the processInstance now contains the result

    } catch (Exception e) {
      e.printStackTrace();
      log.error("Inside Order post method ", e);
    }
    return processInstance;
  }
}
