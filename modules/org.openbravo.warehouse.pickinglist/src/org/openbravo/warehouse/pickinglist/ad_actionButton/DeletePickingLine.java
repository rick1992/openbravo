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
 * All portions are Copyright (C) 2011 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.warehouse.pickinglist.ad_actionButton;

import java.math.BigDecimal;
import java.util.Map;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.materialmgmt.ReservationUtils;
import org.openbravo.model.common.enterprise.Locator;
import org.openbravo.model.common.plm.AttributeSetInstance;
import org.openbravo.model.common.plm.Product;
import org.openbravo.model.materialmgmt.onhandquantity.Reservation;
import org.openbravo.model.materialmgmt.onhandquantity.ReservationStock;
import org.openbravo.model.materialmgmt.onhandquantity.StorageDetail;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.scheduling.Process;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalConnectionProvider;
import org.openbravo.warehouse.pickinglist.PickingList;

public class DeletePickingLine implements Process {

  @Override
  public void execute(ProcessBundle bundle) throws Exception {

    VariablesSecureApp vars = bundle.getContext().toVars();
    Map<String, Object> params = bundle.getParams();
    OBError msg = new OBError();

    String strKey = (String) params.get("M_InOutLine_ID");
    ShipmentInOutLine inoutLine = OBDal.getInstance().get(ShipmentInOutLine.class, strKey);
    PickingList picking = inoutLine.getObwplPickinglist();
    try {

      StorageDetail sd = getStorageDetail(inoutLine.getProduct(), inoutLine.getAttributeSetValue(),
          inoutLine.getStorageBin());

      Reservation reservation = ReservationUtils.getReservationFromOrder(inoutLine
          .getSalesOrderLine());
      ReservationStock reservationStock = ReservationUtils.reserveStockManual(reservation, sd,
          inoutLine.getMovementQuantity().negate(), "Y");
      if (reservationStock.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
        OBDal.getInstance().remove(reservationStock);
      }

      OBDal.getInstance().flush();

      reservation = OBDal.getInstance().get(Reservation.class, reservation.getId());
      OBDal.getInstance().refresh(reservation);

      if (reservation.getReservedQty().equals(BigDecimal.ZERO)
          && reservation.getMaterialMgmtReservationStockList().isEmpty()) {
        if (reservation.getRESStatus().equals("CO")) {
          // Unprocess reservation
          ReservationUtils.processReserve(reservation, "RE");
          OBDal.getInstance().remove(reservation);
          OBDal.getInstance().flush();
        }
      }
      picking.getOBWPLPickinglistManualPickEditList().remove(inoutLine);
      picking.getMaterialMgmtShipmentInOutLineEMObwplPickinglistIDList().remove(inoutLine);

      if (inoutLine.getShipmentReceipt().getMaterialMgmtShipmentInOutLineList().size() == 1) {
        inoutLine.getSalesOrderLine().getSalesOrder().setObwplIsinpickinglist(false);
        OBDal.getInstance().save(inoutLine.getSalesOrderLine().getSalesOrder());
        OBDal.getInstance().remove(inoutLine);
        OBDal.getInstance().remove(inoutLine.getShipmentReceipt());
      } else {
        inoutLine.getShipmentReceipt().getMaterialMgmtShipmentInOutLineList().remove(inoutLine);
        OBDal.getInstance().remove(inoutLine);
      }

      OBDal.getInstance().flush();

      msg.setType("Success");
      bundle.setResult(msg);
    } catch (Exception e) {
      throw new OBException(Utility.messageBD(new DalConnectionProvider(false),
          "OBWPL_ErrorDeletingLine", vars.getLanguage()), e);
    }
  }

  private StorageDetail getStorageDetail(Product product, AttributeSetInstance attributeSetInst,
      Locator storageBin) {
    OBCriteria<StorageDetail> sdCriteria = OBDal.getInstance().createCriteria(StorageDetail.class);
    sdCriteria.add(Restrictions.eq(StorageDetail.PROPERTY_PRODUCT, product));
    sdCriteria.add(Restrictions.eq(StorageDetail.PROPERTY_ATTRIBUTESETVALUE, attributeSetInst));
    sdCriteria.add(Restrictions.eq(StorageDetail.PROPERTY_STORAGEBIN, storageBin));
    sdCriteria.add(Restrictions.isNull(StorageDetail.PROPERTY_ORDERUOM));
    sdCriteria.setMaxResults(1);
    return (StorageDetail) sdCriteria.uniqueResult();
  }
}
