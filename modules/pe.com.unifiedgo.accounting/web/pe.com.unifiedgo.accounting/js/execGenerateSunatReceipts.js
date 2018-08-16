isc.defineClass('SCO_GenerateSunatReceiptPopup', isc.OBPopup);
 
isc.SCO_GenerateSunatReceiptPopup.addProperties({
  width: 420,
  height: 250,
  title: null,
  showMinimizeButton: false,
  showMaximizeButton: false,
  
  view: null,
  params: null,
  actionHandler: null,
  receipts_array: null,
  
  okButton: null,
  cancelButton: null,
  mainfrom: null,
  
  initWidget: function () {
	  
	this.serieSunatDS = OB.Datasource.create({ 
	  destroy: function () { 
	  this.Super('destroy', arguments); }, 
	  dataURL: OB.Application.contextUrl + 'org.openbravo.service.datasource/SCO_Serie_Sunat' 
    }); 
	  
	this.confirmation = isc.Label.create({
	   height: 10,
	   padding: 5,
	   align: "center",
	   valign: "center",
	   wrap: false,
	   showEdges: false,
	   contents: OB.I18N.getLabel('SCO_GenSunatReceipts_conf')
	})
		
	this.mainform = isc.DynamicForm.create({
      numCols: 2,
      colWidths: ['50%', '50%'],
      fields: [{ 
    	  name:"SunatSerie",
    	  title: OB.I18N.getLabel('SCO_SunatSerie'),
    	  height: 20,
    	  width: 255,
    	  type: '_id_17',// equal to select in smartclient type 
    	  optionDataSource: this.serieSunatDS, 
    	  displayField: 'identifierName', 
    	  editorType: "select", 
    	  sortField: 'identifierName', 
    	  required:true,	
    	  valueField: 'id', 
    	  titleClassName: 'OBFormFieldLabel', 
    	  textBoxStyle: 'OBFormFieldSelectInput', 
    	  cellClassName: 'OBFormField', 
    	  defaultToFirstOption:true, 
    	  autoFetchData: true , 
    	  redrawOnChange: true, 

    	  }]
    });

	  
	
	  
    // OK Button
    this.okButton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBUISC_Dialog.OK_BUTTON_TITLE'),
      popup: this,
      action: function () {
        var callback = function (rpcResponse, data, rpcRequest) {
          isc.say(data.result);
          OB.SCO.execGenerateSunatReceipt.popup.params.button.closeProcessPopup();
          rpcRequest.clientContext.popup.closeClick();
        };
 
        OB.RemoteCallManager.call(this.popup.actionHandler, {
        	receipts_array : this.popup.receipts_array,
        	serie : this.popup.mainform.getField('SunatSerie').getValue(),
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

OB.SCO.execGenerateSunatReceipt = {
  execute: function (params, view) {
    var i, selection = params.button.contextView.viewGrid.getSelectedRecords(),
    receipts_array = [];
 
    for (i = 0; i < selection.length; i++) {
    	receipts_array.push(selection[i].id);
    };
 
 // Create the PopUp
    this.popup = isc.SCO_GenerateSunatReceiptPopup.create({
      receipts_array: receipts_array,
      view: view,
      params: params,
      actionHandler: 'pe.com.unifiedgo.accounting.GenerateSunatReceiptHandler'
    });
    
    this.popup.show();
  },
 
  execFunction: function (params, view) {
    params.action = 'execute';
    OB.SCO.execGenerateSunatReceipt.execute(params, view);
  },
};