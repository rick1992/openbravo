package it.extrasys.utility.shipmentinout.biz;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.openbravo.dal.service.OBDal;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.service.db.CallProcess;

public class PostShipment {
	
	 private static final Logger log = Logger.getLogger(PostShipment.class);

	  private static PostShipment singleton;

	  private PostShipment() {
	  }

	  public static PostShipment getSingleton() {
	    if (singleton == null) {
	      singleton = new PostShipment();
	    }
	    return singleton;
	  }

	  /**
	   * Call C_Order_Post
	   */
	  public ProcessInstance postShipment(ShipmentInOut objShip) {
	    ProcessInstance processInstance = null;
	    try {
	      // get an AD_Process instance, 109 is the M_InOut_Post process
	      final org.openbravo.model.ad.ui.Process process = OBDal.getInstance().get(
	          org.openbravo.model.ad.ui.Process.class, "109");
	      processInstance = CallProcess.getInstance().call(process, objShip.getId(),
	          new HashMap<String, String>());
	      // the processInstance now contains the result

	    } catch (Exception e) {
	      e.printStackTrace();
	      log.error("Inside Shipment In Out post method ", e);
	    }
	    return processInstance;
	  }

}
