OB.SCR = OB.SCR || {};

OB.SCR.execUncheckInvToDiscountComission = {
  execute: function (params, view) {
    var i, selection = params.button.contextView.viewGrid.getSelectedRecords(),
        recordIdList = [],
        messageBar = view.getView(params.adTabId).messageBar,
        callback, validationMessage, validationOK = true;

    callback = function (rpcResponse, data, rpcRequest) {
      var status = rpcResponse.status,
          view = rpcRequest.clientContext.view.getView(params.adTabId);
      view.messageBar.setMessage(data.message.severity, null, data.message.text);

      // close process to refresh current view
      params.button.closeProcessPopup();
    };

    for (i = 0; i < selection.length; i++) {
      recordIdList.push(selection[i].id);
    }

     isc.SCR_UncheckInvToDiscountComissionPopup.create({
       recordIdList: recordIdList,
       view: view,
       params: params
     }).show();
  },

  execFunction: function (params, view) {
    params.actionHandler = 'pe.com.unifiedgo.core.UncheckInvToDiscountComissionActionHandler';
    params.adTabId = view.activeView.tabId;
    OB.SCR.execUncheckInvToDiscountComission.execute(params, view);
  }

};

isc.defineClass('SCR_UncheckInvToDiscountComissionPopup', isc.OBPopup);

isc.SCR_UncheckInvToDiscountComissionPopup.addProperties({

  width: 320,
  height: 200,
  title: null,
  showMinimizeButton: false,
  showMaximizeButton: false,

  //Form
  mainform: null,

  //Button
  okButton: null,
  cancelButton: null,

  initWidget: function () {

    var recordIdList = this.recordIdList,
        originalView = this.view,
        params = this.params;

	this.confirmation = isc.Label.create({
		    height: 10,
		    padding: 5,
		    align: "center",
		    valign: "center",
		    wrap: false,
		    showEdges: false,
		    contents: OB.I18N.getLabel('SCR_UnchkInvToDiscComission_confirm')
    });

    this.okButton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OK'),
      popup: this,
      action: function () {
        var callback, action;

        callback = function (rpcResponse, data, rpcRequest) {
          var status = rpcResponse.status,
              view = rpcRequest.clientContext.originalView.getView(params.adTabId);
          if (data.message) {
            view.messageBar.setMessage(data.message.severity, null, data.message.text);
          }

          rpcRequest.clientContext.popup.closeClick();
          rpcRequest.clientContext.originalView.refresh(false, false);
        };

        OB.RemoteCallManager.call(params.actionHandler, {
          recordIdList:recordIdList,
          action: action
        }, {}, callback, {
          originalView: this.popup.view,
          popup: this.popup
        });
      }
    });
    
    this.cancelButton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('Cancel'),
      popup: this,
      action: function () {
        this.popup.closeClick();
      }
    });

    this.items = [
    isc.VLayout.create({
      defaultLayoutAlign: "center",
      align: "center",
      width: "100%",
      layoutMargin: 10,
      membersMargin: 6,
      members: [
      isc.HLayout.create({
        defaultLayoutAlign: "center",
        align: "center",
        layoutMargin: 30,
        membersMargin: 6,
        members: this.confirmation
      }), isc.HLayout.create({
        defaultLayoutAlign: "center",
        align: "center",
        membersMargin: 10,
        members: [this.okButton, this.cancelButton]
      })]
    })];

    this.Super('initWidget', arguments);
  }

});