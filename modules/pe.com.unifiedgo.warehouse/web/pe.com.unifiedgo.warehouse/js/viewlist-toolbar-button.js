(function () {
  var buttonProps = {
      action: function(){
     	   var callback, orders = [], i, view = this.view, grid = view.viewGrid, selectedRecords = grid.getSelectedRecords();
    	    // collect the order ids
    	   for (i = 0; i < selectedRecords.length; i++) {
    	      orders.push(selectedRecords[i].id);
    	   }
     	   callback = function(rpcResponse, data, rpcRequest) {
     		  console.log(data);
    	      isc.say(OB.I18N.getLabel('swa_OBEXAPP_ViewResumen', [data.total,data.cubicaje,data.cajas]));
    	   }
     	  OB.RemoteCallManager.call('pe.com.unifiedgo.warehouse.viewListActionHandler', {orders: orders}, {}, callback);
      },
      buttonType: 'swa_viewList',
      prompt: OB.I18N.getLabel('swa_showPesoCubicajeCajas'),
      updateState: function(){
          var view = this.view, form = view.viewForm, grid = view.viewGrid, selectedRecords = grid.getSelectedRecords();
          if (view.isShowingForm && form.isNew) {
            this.setDisabled(true);
          } else if (view.isEditingGrid && grid.getEditForm().isNew) {
            this.setDisabled(true);
          } else {
            this.setDisabled(selectedRecords.length === 0);
          }
      }
    };
	

    
    // and call the server
  
  // register the button for the sales order tab
  // the first parameter is a unique identification so that one button can not be registered multiple times.
  OB.ToolbarRegistry.registerButton(buttonProps.buttonType, isc.OBToolbarIconButton, buttonProps, 90, '257');
}());