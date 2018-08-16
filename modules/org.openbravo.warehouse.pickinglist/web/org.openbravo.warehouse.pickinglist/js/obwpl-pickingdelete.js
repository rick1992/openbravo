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
isc.OBToolbar.addClassProperties({
  OBWPL_Original_DELETE_BUTTON_PROPERTIES_action: isc.OBToolbar.DELETE_BUTTON_PROPERTIES.action
});

isc.addProperties(isc.OBToolbar.DELETE_BUTTON_PROPERTIES, isc.OBToolbar.DELETE_BUTTON_PROPERTIES, {
  action: function () {
    var a = isc.OBMessageBar.TYPE_SUCCESS;
    if (this.view.tabId === '2C7235D821114C619D8205C99F4ECCEA') {
      var msg, callback, mvmtLines = [],
          i, view = this.view,
          grid = view.viewGrid,
          selectedRecords = grid.getSelectedRecords();
      // collect the remittanceLines ids
      for (i = 0; i < selectedRecords.length; i++) {
        mvmtLines.push(selectedRecords[i].id);
      }

      // define the callback function which shows the result to the user
      callback = function (rpcResponse, data, rpcRequest) {
        var view = rpcRequest.clientContext.view;
        view.refresh();
        view.messageBar.setMessage(data.message.severity, data.message.title, data.message.text);
      };
      if (selectedRecords.length === 1) {
        msg = OB.I18N.getLabel('OBUIAPP_DeleteConfirmationSingle');
      } else {
        msg = OB.I18N.getLabel('OBUIAPP_DeleteConfirmationMultiple', [selectedRecords.length]);
      }
      // and call the server
      isc.confirm(msg, function (clickedOK) {
        if (clickedOK) {
          OB.RemoteCallManager.call('org.openbravo.warehouse.pickinglist.actionhandler.MvmtLineDeleteHandler', {
            mvmtLines: mvmtLines
          }, {}, callback, {
            view: view
          });
        }
      });

    } else {
      isc.OBToolbar.OBWPL_Original_DELETE_BUTTON_PROPERTIES_action.call(this, arguments);
    }
  }
});