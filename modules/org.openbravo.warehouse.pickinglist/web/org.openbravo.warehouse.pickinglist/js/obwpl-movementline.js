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
 * All portions are Copyright (C) 2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
OB.OBWPL = OB.OBWPL || {};
OB.OBWPL.MovementLine = {

  complete: function (params, view) {
    var i, selection = params.button.contextView.viewGrid.getSelectedRecords(),
        movementlines = [],
        callback;

    callback = function (rpcResponse, data, rpcRequest) {
      var message = data.message;
      view.activeView.messageBar.setMessage(message.severity, message.title, message.text);
      // close process to refresh current view
      params.button.closeProcessPopup();
      isc.clearPrompt();
    };
    for (i = 0; i < selection.length; i++) {
      movementlines.push(selection[i].id);
    }
    isc.confirm(OB.I18N.getLabel('OBWPL_LineCompleteConfirm'), function (clickedOK) {
      if (clickedOK) {
        isc.showPrompt(OB.I18N.getLabel('OBUIAPP_PROCESSING') + isc.Canvas.imgHTML({
          src: OB.Styles.LoadingPrompt.loadingImage.src
        }));
        OB.RemoteCallManager.call('org.openbravo.warehouse.pickinglist.MovementLineHandler', {
          movementlines: movementlines,
          action: 'confirm'
        }, {}, callback);
      }
    });
  },
  reject: function (params, view) {
    var i, selection = params.button.contextView.viewGrid.getSelectedRecords(),
        movementlines = [],
        callback;

    callback = function (rpcResponse, data, rpcRequest) {
      var message = data.message;
      view.activeView.messageBar.setMessage(message.severity, message.title, message.text);
      // close process to refresh current view
      params.button.closeProcessPopup();
      isc.clearPrompt();
    };
    for (i = 0; i < selection.length; i++) {
      movementlines.push(selection[i].id);
    }
    isc.confirm(OB.I18N.getLabel('OBWPL_LineRejectConfirm'), function (clickedOK) {
      if (clickedOK) {
        isc.showPrompt(OB.I18N.getLabel('OBUIAPP_PROCESSING') + isc.Canvas.imgHTML({
          src: OB.Styles.LoadingPrompt.loadingImage.src
        }));
        OB.RemoteCallManager.call('org.openbravo.warehouse.pickinglist.MovementLineHandler', {
          movementlines: movementlines,
          action: 'reject'
        }, {}, callback);
      }
    });
  }
};