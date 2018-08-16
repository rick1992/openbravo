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
 * All portions are Copyright (C) 2011 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  _________
 ************************************************************************
 */

// put within a function to hide local vars etc.
(function () {	
  var buttonProps_Clnt = {
      action: function(){
        var callback, taxid, me = this, view = this.view, grid = view.viewGrid, selectedRecords = grid.getSelectedRecords(), form = view.viewForm;
           
        taxid = view.viewForm.getFieldFromInpColumnName("inptaxid").getValue();
        
        // define the callback function which shows the result to the user
        callback = function(rpcResponse, data, rpcRequest) {        
        	var view = rpcRequest.clientContext.view;
        	
        	if (data.message) { // error
                view.messageBar.setMessage(data.message.severity, data.message.title, data.message.text);               
            } else { // success
        	  // setting result
        	  view.viewForm.getFieldFromInpColumnName("inpemScoFirstname").setValue(data.inpemScoFirstname);
        	  view.viewForm.getFieldFromInpColumnName("inpemScoLastname").setValue(data.inpemScoLastname);
        	  view.viewForm.getFieldFromInpColumnName("inpemScoLastname2").setValue(data.inpemScoLastname2);
        	  view.viewForm.getFieldFromInpColumnName("inpdescription").setValue(data.inpdescription);
        	  view.viewForm.getFieldFromInpColumnName("inpname").setValue(data.inpname);
            }
        	
        	// re-enable toolbar button
        	me.enable();
        	me.setDisabled(false);
        	
        	// re-enable form
            view.viewForm.readOnly = false;
            
            // setting taxid field as editable
        	view.viewForm.getFieldFromInpColumnName("inptaxid").readOnly = false;
        	
        	// refresh toolbar
            view.toolBar.refreshCustomButtons(); 
            
            // hinding processing popup
            isc.clearPrompt();
        }
        
    	// disable toolbar button        
        me.disable();
        me.setDisabled(true);
        
        // disable form
        view.viewForm.readOnly = true;
        
        // setting taxid field as readonly       
        view.viewForm.getFieldFromInpColumnName("inptaxid").readOnly = true;               
    	
        // showing processing popup
    	isc.showPrompt(OB.I18N.getLabel('OBUIAPP_PROCESSING') + isc.Canvas.imgHTML({
            src: OB.Styles.LoadingPrompt.loadingImage.src
        }));
                
        // and call the server
        OB.RemoteCallManager.call('pe.com.unifiedgo.core.BPInfoActionHandler', {taxid: taxid}, {}, callback, {view: view});              
      },
      
      buttonType: 'scr_clntinfo',
      prompt: OB.I18N.getLabel('SCR_BPToolbarBtnInfo'),
      updateState: function(){
          var view = this.view, form = view.viewForm, grid = view.viewGrid, selectedRecords = grid.getSelectedRecords();
          if (view.isShowingForm && form.isNew) {
        	this.setDisabled(false); //orig: this.setDisabled(true);
          }	else if(view.isShowingForm) {
        	this.setDisabled(false);	       
          } else if (view.isEditingGrid && grid.getEditForm().isNew) {
            this.setDisabled(true); //orig: this.setDisabled(true);
          } else {
        	this.setDisabled(true); //orig: this.setDisabled(selectedRecords.length === 0);
          }
      }
    };
  
  // register the button for the business partner tab
  OB.ToolbarRegistry.registerButton(buttonProps_Clnt.buttonType, isc.OBToolbarIconButton, buttonProps_Clnt, 100, '7698137AF675474996F3F6A8D583F2C5');
}());
