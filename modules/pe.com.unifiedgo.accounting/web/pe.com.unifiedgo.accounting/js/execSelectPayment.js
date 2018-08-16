OB.SCO = OB.SCO || {};

OB.SCO.execSelectPayment  = {
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

     isc.SCO_SelectPaymentPopup.create({
       recordIdList: recordIdList,
       view: view,
       params: params
     }).show();
  },

  execFunction: function (params, view) {
    params.actionHandler = 'pe.com.unifiedgo.accounting.SelectPaymentActionHandler';
    params.adTabId = view.activeView.tabId;
    OB.SCO.execSelectPayment.execute(params, view);
  }

};

isc.defineClass('SCO_SelectPaymentPopup', isc.OBPopup);

isc.SCO_SelectPaymentPopup.addProperties({

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

  getActionList: function (form) {
    var send = {
      recordIdList: this.recordIdList
    },
        actionField, popup = this;
    send.action = 'ACTION_COMBO';
    OB.RemoteCallManager.call('pe.com.unifiedgo.accounting.SelectPaymentActionHandler', send, {}, function (response, data, request) {
      if (response) {
        actionField = form.getField('Action');
        if (response.data) {
          popup.setTitle('Seleccionar/Deseleccionar');
          actionField.closePeriodStepId = response.data.nextStepId;
          actionField.setValueMap(response.data.actionComboBox.valueMap);
          actionField.setDefaultValue(response.data.actionComboBox.defaultValue);
        }
      }
    });
  },

  initWidget: function () {

  OB.TestRegistry.register('pe.com.unifiedgo.accounting.SelectPayment.popup', this);

    var recordIdList = this.recordIdList,
        originalView = this.view,
        params = this.params;

    this.mainform = isc.DynamicForm.create({
      numCols: 2,
      colWidths: ['50%', '50%'],
      fields: [{
        name: 'Action',
        title: OB.I18N.getLabel('Action'),
        height: 20,
        width: 255,
        required: true,
        type: '_id_17',
        closePeriodStepId: null,
        defaultToFirstOption: true
      }]
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

        action = this.popup.mainform.getItem('Action').getValue();

        OB.RemoteCallManager.call(params.actionHandler, {
          closePeriodStepId: this.popup.mainform.getItem('Action').closePeriodStepId,
          recordIdList:recordIdList,
          action: action
        }, {}, callback, {
          originalView: this.popup.view,
          popup: this.popup
        });
      }
    });
    
    OB.TestRegistry.register('pe.com.unifiedgo.accounting.SelectPayment.popup.okButton', this.okButton);

    this.cancelButton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('Cancel'),
      popup: this,
      action: function () {
        this.popup.closeClick();
      }
    });

    this.getActionList(this.mainform);

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
        members: this.mainform
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
