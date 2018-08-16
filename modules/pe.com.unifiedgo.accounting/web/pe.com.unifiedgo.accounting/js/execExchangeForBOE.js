isc.defineClass('SCO_ExchangeForBOEButtomPopup', isc.OBPopup);
 
isc.SCO_ExchangeForBOEButtomPopup.addProperties({
  width: 420,
  height: 250,
  title: null,
  showMinimizeButton: false,
  showMaximizeButton: false,
  
  view: null,
  params: null,
  actionHandler: null,
  invoice_id_array: null,
  
  confirmation: null,
  okButton: null,
  cancelButton: null,
  
  initWidget: function () {
	  
	  this.confirmation = isc.Label.create({
		   height: 10,
		   padding: 5,
		   align: "center",
		   valign: "center",
		   wrap: false,
		   showEdges: false,
		   contents: OB.I18N.getLabel('SCO_ExchangeForBOE_conf')
		})
			
		this.mainform = isc.DynamicForm.create({
	      numCols: 2,
	      colWidths: ['50%', '50%'],
	      fields: [{ 
	    	  name:"NumBOES",
	    	  title: OB.I18N.getLabel('SCO_NumBOES'),
	    	  height: 20,
	    	  width: 255,
	          type: "text",
	          width: 100,
	          keyPressFilter: "[0-9.]"

	    	  }]
	    });

    // OK Button
    this.okButton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBUISC_Dialog.OK_BUTTON_TITLE'),
      popup: this,
      action: function () {
        var callback = function (rpcResponse, data, rpcRequest) {
          isc.say(data.result);
          OB.SCO.execExchangeForBOE.popup.params.button.closeProcessPopup();
          rpcRequest.clientContext.popup.closeClick();
        };
 
        OB.RemoteCallManager.call(this.popup.actionHandler, {
        	invoice_id_array: this.popup.invoice_id_array,
        	num_boes : this.popup.mainform.getField('NumBOES').getValue(),
            action: this.popup.params.action,
          }, {}, callback, {popup: this.popup}); 
      }
   });
   
   // Cancel Button
   this.cancelButton = isc.OBFormButton.create({
     title: OB.I18N.getLabel('OBUISC_Dialog.CANCEL_BUTTON_TITLE'),
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
                   height: 230,
                   layoutMargin: 5,
                   membersMargin: 6,
                   members: [
            		isc.HLayout.create({
            		    defaultLayoutAlign: "center",
            		    align: "center",
            		    layoutMargin: 0,
            		    membersMargin: 3,
            		    members: this.confirmation
            		  }), 
            		  isc.HLayout.create({
            			    defaultLayoutAlign: "center",
            			    align: "center",
            			    layoutMargin: 0,
            			    membersMargin: 3,
            			    members: this.mainform
            			  }),
                     isc.HLayout.create({
                       defaultLayoutAlign: "center",
                       align: "center",
                       membersMargin: 10,
                       members: [this.okButton, this.cancelButton]
                     })
              ]
        })
    ];
   
   this.Super('initWidget', arguments);
  }
 
});

OB.SCO = OB.SCO || {};

OB.SCO.execExchangeForBOE = {
  execute: function (params, view) {
    var i, selection = params.button.contextView.viewGrid.getSelectedRecords(),
    invoice_id_array = [];
 
    for (i = 0; i < selection.length; i++) {
    	invoice_id_array.push(selection[i].id);
    };
 
 // Create the PopUp
    this.popup = isc.SCO_ExchangeForBOEButtomPopup.create({
      invoice_id_array: invoice_id_array,
      view: view,
      params: params,
      actionHandler: 'pe.com.unifiedgo.accounting.ExchangeForBOEActionHandler'
    });
    
    this.popup.show();
  },
 
  execFunction: function (params, view) {
    params.action = 'execute';
    OB.SCO.execExchangeForBOE.execute(params, view);
  },
};