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
 * All portions are Copyright (C) 2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.materialmgmt;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.structure.BaseOBObject;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.materialmgmt.onhandquantity.Reservation;
import org.openbravo.model.materialmgmt.onhandquantity.ReservationStock;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.service.db.CallProcess;
import org.openbravo.service.db.DalConnectionProvider;

import pe.com.unifiedgo.requirement.data.SREServiceorderline;
import pe.com.unifiedgo.warehouse.data.SwaRequerimientoReposicionDetail;
import pe.com.unifiedgo.warehouse.data.swaProductWarehouse;

public class ReservationUtils {
  String returnValue;
  String exito;

  public static Reservation createReserveFromSalesOrderLine(OrderLine soLine, boolean doProcess)
      throws OBException {
    if (!soLine.getSalesOrder().isSalesTransaction()) {
      throw new OBException(OBMessageUtils.messageBD("cannotReservePurchaseOrder"));
    }
    if (soLine.getOrderedQuantity().subtract(soLine.getDeliveredQuantity())
        .compareTo(BigDecimal.ZERO) == 0) {
      throw new OBException(OBMessageUtils.messageBD("cannotReserveDeliveredSalesOrderLine"));
    }

    OBDal.getInstance().flush();
    CSResponse cs = null;
    try {
      cs = ReservationUtilsData.createReserveFromSalesOrderLine(
          OBDal.getInstance().getConnection(false), new DalConnectionProvider(false),
          soLine.getId(), doProcess ? "Y" : "N",
          (String) DalUtil.getId(OBContext.getOBContext().getUser()));
    } catch (ServletException e) {
    }

    if (cs != null && cs.returnValue != null) {
      return OBDal.getInstance().get(Reservation.class, cs.returnValue);
    }

    return null;
  }

  public static OBError reserveStockAuto(Reservation reservation) throws OBException {

    OBDal.getInstance().flush();
    CSResponse cs = null;
    try {
      cs = ReservationUtilsData.reserveStockAutoSd(OBDal.getInstance().getConnection(false),
          new DalConnectionProvider(false), reservation.getId(),
          (String) DalUtil.getId(OBContext.getOBContext().getUser()));
    } catch (ServletException e) {
      String message = OBMessageUtils.translateError(e.getMessage()).getMessage();
      throw new OBException(message, e);
    }

    String message = "";
    if (cs != null && cs.returnValue != null) {
      message = cs.returnValue;
    }

    OBError obmessage = new OBError();
    obmessage.setType("SUCCESS");
    obmessage.setMessage(message);
    return obmessage;
  }
  
  
  public static OBError reserveStockAutoMaxPermit(Reservation reservation, String maxPermit) throws OBException {

	    OBDal.getInstance().flush();
	    CSResponse cs = null;
	    try {
	      cs = ReservationUtilsData.reserveStockAutoMaxPermit(OBDal.getInstance().getConnection(false),
	          new DalConnectionProvider(false), reservation.getId(),
	          (String) DalUtil.getId(OBContext.getOBContext().getUser()),maxPermit);
	    } catch (ServletException e) {
	      String message = OBMessageUtils.translateError(e.getMessage()).getMessage();
	      throw new OBException(message, e);
	    }

	    String message = "";
	    if (cs != null && cs.returnValue != null) {
	      message = cs.returnValue;
	    }

	    OBError obmessage = new OBError();
	    obmessage.setType("SUCCESS");
	    obmessage.setMessage(message);
	    return obmessage;
	  }

  /**
   * Function to reserve in allocated or not allocated given stock or purchase order line. Available
   * OBObject are:<br>
   * - StorageDetail: reserves stock in the warehouse.<br>
   * - OrderLine: reserves stock pending to receipt purchase order line.
   */

  public static ReservationStock reserveStockManual(Reservation reservation, BaseOBObject obObject,
      BigDecimal quantity, String allocated) throws OBException {

    String strType = "";

    if (obObject instanceof OrderLine) {
      strType = "PO";
    } else if (obObject instanceof StorageDetail) {
      strType = "SD";
    } else {
      throw new OBException("notValidReservationType");
    }

    OBDal.getInstance().flush();
    CSResponse cs = null;
    try {
      cs = ReservationUtilsData.reserveStockManual(OBDal.getInstance().getConnection(false),
          new DalConnectionProvider(false), reservation.getId(), strType, obObject.getId()
              .toString(), quantity.toString(), (String) DalUtil.getId(OBContext.getOBContext()
              .getUser()), allocated);
    } catch (ServletException e) {
      String message = OBMessageUtils.translateError(e.getMessage()).getMessage();
      throw new OBException(message, e);
    }

    if (cs != null && cs.returnValue != null) {
    	//System.out.println(cs.returnValue.get);
      return OBDal.getInstance().get(ReservationStock.class, cs.returnValue);
    }

    return null;
  }

  /**
   * Allowed actions:
   * <ul>
   * <li>PR Process</li>
   * <li>RE Reactivate</li>
   * <li>HO Put on Hold</li>
   * <li>UNHO Unhold</li>
   * <li>CL Close</li>
   * </ul>
   */
  public static OBError processReserve(Reservation reservation, String action) throws OBException {

    OBContext.setAdminMode(true);
    Process process = null;
    try {
      process = OBDal.getInstance().get(Process.class, "5A2A0AF88AF54BB085DCC52FCC9B17B7");
    } finally {
      OBContext.restorePreviousMode();
    }

    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("RES_Action", action);

    final ProcessInstance pinstance = CallProcess.getInstance().call(process, reservation.getId(),
        parameters);

    return OBMessageUtils.getProcessInstanceMessage(pinstance);
  }

  /**
   * Returns a non closed reservation from given Sales Order Line. If no reservation exists it
   * creates a new one in draft status.
   * 
   * @param salesOrderLine  
   *          Sales Order Line owner of the reservation.
   * @return a Reservation related to the Sales Order Line
   */
  public static Reservation getReservationFromOrder(OrderLine salesOrderLine) {
    OBDal.getInstance().refresh(salesOrderLine);
    for (Reservation res : salesOrderLine.getMaterialMgmtReservationList()) {
      return res;
    }
    return ReservationUtils.createReserveFromSalesOrderLine(salesOrderLine, false);
  }
  
  public static Reservation getReservationFromShipment(ShipmentInOutLine ioline) {
	    OBDal.getInstance().refresh(ioline);
	    
	     OBCriteria<Reservation> res = OBDal.getInstance().createCriteria(Reservation.class);
	     res.add(Restrictions.eq(Reservation.PROPERTY_SWAMINOUTLINE, ioline));
	     res.add(Restrictions.eq(Reservation.PROPERTY_CLIENT, ioline.getClient()));
		 List<Reservation> resList = res.list();
		 
		 if(resList.size()==0)
			 return null;
		 return resList.get(0);
	  }
  
  /**
   * Returns a non closed reservation from given Purchase Service Order Line. If no reservation exists it
   * creates a new one in draft status.
   * 
   * VAFASTER WAS HERE :)  :)  :)
   * 
   */
  
  
  public static Reservation getReservationFromServiceOrder(SREServiceorderline RepositionLine) {
	    OBDal.getInstance().refresh(RepositionLine);
	    
		OBCriteria<Reservation> OBJReservation = OBDal.getInstance().createCriteria(Reservation.class);
		OBJReservation.add(Restrictions.eq(Reservation.PROPERTY_SWASERVICEORDERLINE, RepositionLine));
		List<Reservation> reservationList = OBJReservation.list();
		
		for(Reservation res : reservationList){
			return res;
		}
		
		/*
		 * Si no existe una reserva, entonces creamos una en estado borrador
		 * */
	    return ReservationUtils.createReserveFromPurchaseServiceOrderLine(RepositionLine, false);
  }
  
  /**
   * Returns a non closed reservation from given Reposition Order Line. If no reservation exists it
   * creates a new one in draft status.
   * 
   * VAFASTER WAS HERE :)  :)  :)
   * 
   */
  
  
  public static Reservation getReservationFromReposition(SwaRequerimientoReposicionDetail RepositionLine) {
	    OBDal.getInstance().refresh(RepositionLine);
	    
		OBCriteria<Reservation> OBJReservation = OBDal.getInstance().createCriteria(Reservation.class);
		OBJReservation.add(Restrictions.eq(Reservation.PROPERTY_SWAREQUEREPODETAIL, RepositionLine));
		List<Reservation> reservationList = OBJReservation.list();
		
		for(Reservation res : reservationList){
			return res;
		}
		
		/*
		 * Si no existe una reserva, entonces creamos una en estado borrador
		 * */
	    return ReservationUtils.createReserveFromRepositionOrderLine(RepositionLine, false);
  }
  
  public static Reservation createReserveFromRepositionOrderLine(SwaRequerimientoReposicionDetail reLine, boolean doProcess){
	  
	  
     Reservation res = OBProvider.getInstance().get(Reservation.class);
     res.setOrganization(reLine.getOrganization());
     res.setProduct(reLine.getProduct());
     res.setWarehouse(reLine.getSWARequerimientoreposicion().getFromMWarehouse());
     res.setQuantity(reLine.getOrderedQuantity());
     res.setSwaRequerepoDetail(reLine);
     res.setRESStatus("DR");
     res.setRESProcess("PR");
     res.setReservedQty(reLine.getOrderedQuantity());
     OBDal.getInstance().save(res);
     return res;
  }
  
  
  public static Reservation createReserveFromPurchaseServiceOrderLine(SREServiceorderline reLine, boolean doProcess){
	  
	     Reservation res = OBProvider.getInstance().get(Reservation.class);
	     res.setOrganization(reLine.getOrganization());
	     res.setProduct(reLine.getProduct());
	     res.setUOM(reLine.getUOM());
	     res.setQuantity(reLine.getOrderedQuantity());
	     res.setSwaServiceorderline(reLine);
	     res.setRESStatus("DR");
	     res.setRESProcess("PR");
	     res.setWarehouse(reLine.getSalesOrder().getWarehouse());
	     res.setOBWPLGeneratedByPickingList(true);

	     OBDal.getInstance().save(res);
	     return res;
	  }
  
  

  /**
   * Function to reallocate given reservation stock on given attributes and storage bin.
   */

  public static OBError reallocateStock(Reservation reservation, Locator storageBin,
      AttributeSetInstance asi, BigDecimal quantity) throws OBException {

    OBDal.getInstance().flush();
    CSResponse cs = null;
    try {
      cs = ReservationUtilsData.reallocateStock(OBDal.getInstance().getConnection(false),
          new DalConnectionProvider(false), reservation.getId(), storageBin.getId(), asi.getId(),
          quantity.toPlainString(), (String) DalUtil.getId(OBContext.getOBContext().getUser()));
    } catch (ServletException e) {
      String message = OBMessageUtils.translateError(e.getMessage()).getMessage();
      throw new OBException(message, e);
    }

    OBError result = new OBError();
    if (cs == null || StringUtils.isEmpty(cs.returnValue)) {
      throw new OBException(OBMessageUtils.messageBD("Error"));
    }
    result.setType("Success");
    result.setMessage(OBMessageUtils.messageBD("Success"));
    if (cs.returnValue == "0") {
      result.setType("Error");
    } else if (cs.returnValue == "2") {
      result.setType("Warning");
    }
    if (StringUtils.isNotEmpty(cs.returnValueMsg)) {
      result.setMessage(OBMessageUtils.parseTranslation(cs.returnValueMsg));
    }
    return result;
  }

}
