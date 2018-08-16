
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
 * All portions are Copyright (C) 2011-2012 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */

OB.SPR = OB.SPR || {};

/**
 * Check that entered order quantity (neworderedquantity) is less than original ordered qty.
 */
OB.SPR.SPROrderQtyValidate = function (item, validator, value, record) {
  if (!isc.isA.Number(value)) {
    return false;
  }
  var availOrderedQty = record.maxqtyordered !== null ? new BigDecimal(String(record.maxqtyordered)) : BigDecimal.prototype.ZERO,
      newRequiredQty = new BigDecimal(String(value));
  if ((value !== null) && (newRequiredQty.compareTo(availOrderedQty)) <= 0 && (value > 0)) {
    return true;
  } else {
    item.grid.view.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, OB.I18N.getLabel('SPR_ReqPick_OutOfRange', [availOrderedQty.toString()]));
    return false;
  }
};
