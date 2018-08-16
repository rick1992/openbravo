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
 * All portions are Copyright (C) 2012-2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
OB.OBWPL = OB.OBWPL || {};
OB.OBWPL.Process = {
  create: function (params, view) {
    var i, j, selection = params.button.contextView.viewGrid.getSelectedRecords(),
        orders = [];

    for (i = 0; i < selection.length; i++) {
      orders.push(selection[i].id);
    }

    
    var CreateFromOrderPopup = isc.OBWPL_CreateFromOrderPopup.create({
      orders: orders,
      view: view,
      params: params
    });
    CreateFromOrderPopup.show();
        
    var phydocno = CreateFromOrderPopup.mainformSection1.getItem('physicalDocumentNo');
    var movDate = CreateFromOrderPopup.mainformSection2.getItem('movementDate');
    var callback, action;
    //Callback to Show/Hide Shipment Parameters
    callback = function (rpcResponse, data, rpcRequest) {
      if (data.isSalesShipment=='true') {
    	  phydocno.show();
    	  movDate.show();  
      } else { 
    	  phydocno.hide();  
    	  movDate.hide();  
      }      
    };    
    OB.RemoteCallManager.call('org.openbravo.warehouse.pickinglist.CreateActionHandler', {
  	  orders: orders,
      action: 'showGRFields'
    }, {}, callback, {}); 
    
    //Callback to Set Default Physical Document No
    if(phydocno.isVisible()) {
       callback = function (rpcResponse, data, rpcRequest) {
         if (data.physicalDocNoGR != null && data.physicalDocNoGR != "") {
    	     phydocno.setValue(data.physicalDocNoGR);
    	 }   
       };    
       OB.RemoteCallManager.call('org.openbravo.warehouse.pickinglist.CreateActionHandler', {
         orders: orders,
         action: 'getPhysicalDocNo'
       }, {}, callback, {}); 
    }
    
        
  },


  assignProcess: function (params, view, doGroup) {
    var i, selection = params.button.contextView.viewGrid.getSelectedRecords(),
        pickings = [],
        org = selection[0].organization;

    for (i = 0; i < selection.length; i++) {
      if (org !== selection[i].organization) {
        isc.showMessage(OB.I18N.getLabel('OBWPL_MultipleOrgs'), OB.I18N.getLabel('OBUIAPP_Error'));
        return;
      }
      pickings.push(selection[i].id);
    }
    if (selection.length === 1) {
      doGroup = false;
    }

    isc.OBWPL_AssignPopup.create({
      pickings: pickings,
      view: view,
      params: params,
      organization: org,
      doGroup: doGroup
    }).show();
  },

  assign: function (params, view) {
    OB.OBWPL.Process.assignProcess(params, view, true);
  },
  reassign: function (params, view) {
    OB.OBWPL.Process.assignProcess(params, view, false);
  },

  validate: function (params, view) {
    var recordId = params.button.contextView.viewGrid.getSelectedRecords()[0].id,
        processOwnerView = view.getProcessOwnerView(params.processId),
        processAction, callback;

    processAction = function () {
      OB.OBWPL.Process.process(params, view);
    };
    callback = function (rpcResponse, data, rpcRequest) {
      var processLayout, popupTitle;
      if (!data) {
        view.activeView.messageBar.setMessage(isc.OBMessageBar.TYPE_ERROR, null, null);
        return;
      }
      if (data.message || !data.data) {
        view.activeView.messageBar.setMessage(isc.OBMessageBar[data.message.severity], null, data.message.text);
        return;
      }
      popupTitle = OB.I18N.getLabel('OBWPL_PickingList') + ' - ' + params.button.contextView.viewGrid.getSelectedRecords()[0]._identifier;
      processLayout = isc.OBPickValidateProcess.create({
        parentWindow: view,
        sourceView: view.activeView,
        buttonOwnerView: processOwnerView,
        pickGridData: data.data,
        processAction: processAction
      });
      view.openPopupInTab(processLayout, popupTitle, OB.Styles.OBWPL.PickValidateProcess.popupWidth, OB.Styles.OBWPL.PickValidateProcess.popupHeight, true, true, true, true);
    };
    OB.RemoteCallManager.call('org.openbravo.warehouse.pickinglist.ValidateActionHandler', {
      recordId: recordId,
      action: 'validate'
    }, {}, callback);
  },

  pickingHandlerCall: function (params, view, action, confirmMsg) {
    var i, selection = params.button.contextView.viewGrid.getSelectedRecords(),
        pickinglist = [],
        callback;

    callback = function (rpcResponse, data, rpcRequest) {
      var message = data.message,
          processView = view.getProcessOwnerView(params.processId);
      processView.messageBar.setMessage(message.severity, message.title, message.text);
      // close process to refresh current view
      params.button.closeProcessPopup();
      isc.clearPrompt();
    };
    for (i = 0; i < selection.length; i++) {
      pickinglist.push(selection[i].id);
    }
    isc.confirm(OB.I18N.getLabel(confirmMsg), function (clickedOK) {
      if (clickedOK) {
        isc.showPrompt(OB.I18N.getLabel('OBUIAPP_PROCESSING') + isc.Canvas.imgHTML({
          src: OB.Styles.LoadingPrompt.loadingImage.src
        }));
        OB.RemoteCallManager.call('org.openbravo.warehouse.pickinglist.PickingListActionHandler', {
          pickinglist: pickinglist,
          action: action
        }, {}, callback);
      }
    });
  },

  cancel: function (params, view) {
    OB.OBWPL.Process.pickingHandlerCall(params, view, 'cancel', 'OBWPL_CancelConfirm');
  },

  process: function (params, view) {
    OB.OBWPL.Process.pickingHandlerCall(params, view, 'process', 'OBWPL_ProcessConfirm');
  },

  close: function (params, view) {
    OB.OBWPL.Process.pickingHandlerCall(params, view, 'close', 'OBWPL_CloseConfirm');
  }

};