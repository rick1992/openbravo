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
 * All portions are Copyright (C) 2013 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.warehouse.pickinglist.callout;

import javax.servlet.ServletException;

import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.warehouse.pickinglist.OBWPL_Utils;
import org.openbravo.warehouse.pickinglist.PickingList;

public class PickingListDoctype extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {
    final String strDocTypeId = info.getStringParameter("inpcDoctypeId", IsIDFilter.instance);
    final DocumentType docType = OBDal.getInstance().get(DocumentType.class, strDocTypeId);
    final String strDocumentNo = OBWPL_Utils.getDocumentNo(docType, PickingList.TABLE_NAME);
    info.addResult("inpdocumentno", "<" + strDocumentNo + ">");
    info.addResult("DOCTYPE_USEOUTBOUND", docType.isOBWPLUseOutbound() ? "Y" : "N");
  }

}
