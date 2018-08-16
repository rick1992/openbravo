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
 * All portions are Copyright (C) 2012-2014 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.warehouse.pickinglist;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.security.OrganizationStructureProvider;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.materialmgmt.onhandquantity.ReservationStock;
import org.openbravo.model.materialmgmt.transaction.InternalMovement;
import org.openbravo.model.materialmgmt.transaction.InternalMovementLine;

public class OBWPL_Utils {
  final private static Date now = DateUtils.truncate(new Date(), Calendar.DATE);
  final private static String MOVEMENT_NAME = OBMessageUtils.messageBD("OBWPL_MovementName");

  /**
   * Returns the DocumentType defined for the Organization (or parent organization tree) and
   * document category.
   * 
   * @param org
   *          the Organization for which the Document Type is defined. The Document Type can belong
   *          to the parent organization tree of the specified Organization.
   * @param docCategory
   *          the document category of the Document Type.
   * @return the Document Type
   */
  public static DocumentType getDocumentType(Organization org, String docCategory) {
    return getDocumentType(org, docCategory, false);
  }

  public static DocumentType getDocumentType(Organization org, String docCategory, boolean useOutbound) {
    Client client = null;

    if ("0".equals(org.getId())) {
      client = OBContext.getOBContext().getCurrentClient();
      if ("0".equals(client.getId())) {
        return null;
      }
    } else {
      client = org.getClient();
    }
    OrganizationStructureProvider osp = OBContext.getOBContext().getOrganizationStructureProvider(client.getId());

    OBCriteria<DocumentType> critDoc = OBDal.getInstance().createCriteria(DocumentType.class);
    critDoc.setFilterOnReadableClients(false);
    critDoc.setFilterOnReadableOrganization(false);
    critDoc.add(Restrictions.eq(DocumentType.PROPERTY_CLIENT, client));
    critDoc.add(Restrictions.in("organization.id", osp.getParentTree(org.getId(), true)));
    critDoc.add(Restrictions.eq(DocumentType.PROPERTY_DOCUMENTCATEGORY, docCategory));
    critDoc.add(Restrictions.eq(DocumentType.PROPERTY_OBWPLUSEOUTBOUND, useOutbound));
    critDoc.addOrderBy(DocumentType.PROPERTY_DEFAULT, false);
    critDoc.addOrderBy(DocumentType.PROPERTY_ID, false);
    critDoc.setMaxResults(1);
    return (DocumentType) critDoc.uniqueResult();
  }

  public static DocumentType getDocumentType(Organization org, String docCategory, boolean useOutbound, boolean isReturn) {
    Client client = null;

    if ("0".equals(org.getId())) {
      client = OBContext.getOBContext().getCurrentClient();
      if ("0".equals(client.getId())) {
        return null;
      }
    } else {
      client = org.getClient();
    }
    OrganizationStructureProvider osp = OBContext.getOBContext().getOrganizationStructureProvider(client.getId());

    OBCriteria<DocumentType> critDoc = OBDal.getInstance().createCriteria(DocumentType.class);
    critDoc.setFilterOnReadableClients(false);
    critDoc.setFilterOnReadableOrganization(false);
    critDoc.add(Restrictions.eq(DocumentType.PROPERTY_CLIENT, client));
    critDoc.add(Restrictions.in("organization.id", osp.getParentTree(org.getId(), true)));
    critDoc.add(Restrictions.eq(DocumentType.PROPERTY_DOCUMENTCATEGORY, docCategory));
    critDoc.add(Restrictions.eq(DocumentType.PROPERTY_OBWPLUSEOUTBOUND, useOutbound));
    critDoc.add(Restrictions.eq(DocumentType.PROPERTY_RETURN, isReturn));
    critDoc.addOrderBy(DocumentType.PROPERTY_DEFAULT, false);
    critDoc.addOrderBy(DocumentType.PROPERTY_ID, false);
    critDoc.setMaxResults(1);
    return (DocumentType) critDoc.uniqueResult();
  }

  public static DocumentType getGroupPLDocumentType(Organization org) {
    Client client = null;

    if ("0".equals(org.getId())) {
      client = OBContext.getOBContext().getCurrentClient();
      if ("0".equals(client.getId())) {
        return null;
      }
    } else {
      client = org.getClient();
    }
    OrganizationStructureProvider osp = OBContext.getOBContext().getOrganizationStructureProvider(client.getId());

    OBCriteria<DocumentType> critDoc = OBDal.getInstance().createCriteria(DocumentType.class);
    critDoc.setFilterOnReadableClients(false);
    critDoc.setFilterOnReadableOrganization(false);
    critDoc.add(Restrictions.eq(DocumentType.PROPERTY_CLIENT, client));
    critDoc.add(Restrictions.in("organization.id", osp.getParentTree(org.getId(), true)));
    critDoc.add(Restrictions.eq(DocumentType.PROPERTY_DOCUMENTCATEGORY, "OBWPL_doctype"));
    critDoc.add(Restrictions.eq(DocumentType.PROPERTY_OBWPLISGROUP, true));
    critDoc.addOrderBy(DocumentType.PROPERTY_DEFAULT, false);
    critDoc.addOrderBy(DocumentType.PROPERTY_ID, false);
    critDoc.setMaxResults(1);
    return (DocumentType) critDoc.uniqueResult();
  }

  /**
   * Returns the next sequence number of the Document Type defined for the Organization and document
   * category. The current number of the sequence is also updated.
   * 
   * @param docType
   *          Document type of the document
   * @return the next sequence number of the Document Type defined for the Organization and document
   *         category. Empty String if no sequence is found.
   */
  public static String getDocumentNo(DocumentType docType, String strTableName) {
    return getDocumentNo(docType, strTableName, true);
  }

  public static String getDocumentNo(DocumentType docType, String strTableName, boolean updateNext) {
    return FIN_Utility.getDocumentNo(docType, strTableName, updateNext);
  }

  public static void createGoodMovement(ReservationStock resStock, PickingList picking, PickingList groupPicking, BigDecimal quantity) {
    InternalMovement move = OBProvider.getInstance().get(InternalMovement.class);
    Map<String, String> map = new HashMap<String, String>();
    map.put("picking", picking.getDocumentNo());

    move.setOrganization(picking.getOrganization());
    move.setMovementDate(now);
    move.setName(OBMessageUtils.parseTranslation(MOVEMENT_NAME, map));
    OBDal.getInstance().save(move);

    InternalMovementLine mvLine = OBProvider.getInstance().get(InternalMovementLine.class);
    mvLine.setOrganization(picking.getOrganization());
    mvLine.setMovement(move);
    mvLine.setLineNo(10L);
    mvLine.setStockReservation(resStock.getReservation());
    mvLine.setOBWPLWarehousePickingList(picking);
    mvLine.setOBWPLGroupPickinglist(groupPicking);
    mvLine.setOBWPLAllowDelete(false);

    mvLine.setProduct(resStock.getReservation().getProduct());
    mvLine.setUOM(resStock.getReservation().getUOM());
    mvLine.setAttributeSetValue(resStock.getAttributeSetValue());
    mvLine.setStorageBin(resStock.getStorageBin());
    mvLine.setNewStorageBin(picking.getOutboundStorageBin());
    BigDecimal resStockReleasedQty = resStock.getReleased() == null ? BigDecimal.ZERO : resStock.getReleased();
    BigDecimal qty = quantity;
    if (qty == null) {
      qty = resStock.getQuantity().subtract(resStockReleasedQty);
    }
    mvLine.setMovementQuantity(qty);
    OBDal.getInstance().save(mvLine);
  }
}
