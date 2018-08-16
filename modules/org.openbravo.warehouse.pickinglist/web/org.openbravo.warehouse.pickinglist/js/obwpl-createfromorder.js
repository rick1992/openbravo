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
isc.defineClass('OBWPL_CreateFromOrderPopup', isc.OBPopup);

isc.OBWPL_CreateFromOrderPopup.addProperties({

  width: 580,
  height: 350,
  title: null,
  showMinimizeButton: false,
  showMaximizeButton: false,

  orders: [],
  warehouses: [],

  //Form
  mainform: null,
  //Buttons
  okButton: null,
  cancelButton: null,

  fields: [],
  origfields: [{
	    name: 'PLType',
	    title: OB.I18N.getLabel('OBWPL_PLType_Label'),
	    //height: 20,
	    width: '100',
	    showIf:"0 == 1",
	    required: true,
	    type: '_id_17',
	    //defaultToFirstOption: true,
	    redrawOnChange: true,
	    valueMap: {
	      'NO-OUT': OB.I18N.getLabel('OBWPL_PLType_NoOut_Label'),
	      'OUT': OB.I18N.getLabel('OBWPL_PLType_Out_Label')
	    }
    }, {
        name: 'Grouping',
        title: OB.I18N.getLabel('OBWPL_Grouping_Label'),
        height: 20,
        width: 100,
        showIf:"0 == 1",
        required: true,
        type: '_id_17',
      //defaultToFirstOption: true
      //showIf: function (item, value, form, currentValues, context) {
      //  return form.getField('PLType').getValue() === 'NO-OUT';
      //}
  }],

  fieldsSection1: [],
  origfieldsSection1: [{      
  	  autoExpand: true,
	  type: '_id_10',
      name: 'physicalDocumentNo',
      title: OB.I18N.getLabel('SSA_PL_PhysicalDocumentNo_GR'),
      height: 20,
      width: 150,
      editorType: 'OBTextItem',      
      titleOrientation: 'top',
      titleStyle: 'OBFormFieldLabel',
      required: true,
      escapeHTML: true,
      referencedKeyColumnName: '',
      targetEntity: '',
      defaultToFirstOption: true 
  }],
  
  fieldsSection2: [],
  origfieldsSection2: [{
      autoExpand: false,
      type: '_id_15',
      cellAlign: 'left',
      name: 'movementDate',
      canExport: true,
      canHide: true,
      editorType: 'OBDateItem',
      filterOnKeypress: false,
      canFilter: true,
      filterEditorType: 'OBMiniDateRangeItem',
      title: OB.I18N.getLabel('SSA_PL_MovementDate'),
      height: 20,
      width: 150,
      defaultValue: new Date(),
      required: true,
      escapeHTML: true,
      referencedKeyColumnName: '',
      targetEntity: ''
  }],
  
  getGroupingCriteriaList: function (form) {
    var groupingField, popup = this,
        send = {
        orders: this.orders,
        action: 'getGroupingCriteria'
        };
    OB.RemoteCallManager.call('org.openbravo.warehouse.pickinglist.CreateActionHandler', send, {}, function (response, data, request) {
      if (response) {
        groupingField = form.getField('Grouping');
        if (response.data) {
          groupingField.setValueMap(response.data.valueMap);
          groupingField.setDefaultValue(response.data.defaultValue);
        }
      }
    });
  },

  createLocatorField: function (warehouse) {
    var i, field, formFields = [];
    field = {
      name: 'Loc' + warehouse.id,
      title: warehouse.name,
      height: 10,
      width: 255,
      required: true,
      type: '_id_17',
      defaultToFirstOption: true,
      showIf: function (item, value, form, currentValues, context) {
        return form.getField('PLType').getValue() === 'OUT';
      }
    };
    this.fields.push(field);
    formFields = this.mainform.getFields();
    formFields.push(field);
    this.mainform.setFields(formFields);
  },

  getOutboundLocatorLists: function (form) {
    var i, warehouses = this.warehouses,
        locatorField, popup = this,
        send = {
        orders: this.orders,
        action: 'getOutboundLocatorLists'
        };
    OB.RemoteCallManager.call('org.openbravo.warehouse.pickinglist.CreateActionHandler', send, {}, function (response, data, request) {
      var whs = [],
          warehouse;
      if (response && response.data) {
        whs = response.data.warehouses;
        for (i = 0; i < whs.length; i++) {
          warehouse = {
            id: whs[i].warehouseId,
            name: whs[i].warehouseName
          };
          warehouses.push(warehouse);
          popup.createLocatorField(warehouse);
          locatorField = form.getField('Loc' + whs[i].warehouseId);
          locatorField.setValueMap(whs[i].valueMap);
        }
      }
    });
  },

  initWidget: function () {
    var orders = this.orders,
        originalView = this.view,
        params = this.params,
        me = this;
    this.fields = isc.shallowClone(this.origfields);
    this.fieldsSection1 = isc.shallowClone(this.origfieldsSection1);
    this.fieldsSection2 = isc.shallowClone(this.origfieldsSection2);
    
    this.mainform = isc.DynamicForm.create({
      numCols: 2,
      colWidths: ['*', '*'],
      titleOrientation: 'top',      
      fields: this.fields
    });
    
    this.mainformSection1 = isc.DynamicForm.create({
        numCols: 1,
        titleOrientation: 'top',
        fields: this.fieldsSection1
    });
    
    this.mainformSection2 = isc.DynamicForm.create({
        numCols: 1,
        colWidths: ['*', '*'],
        titleOrientation: 'top',
        fields: this.fieldsSection2
    });
    
    this.setTitle(OB.I18N.getLabel('OBWPL_CreatePL'));

    this.okButton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBUISC_Dialog.OK_BUTTON_TITLE'),
      popup: this,
      action: function () {
        var i, callback, groupingCrit, plType, locators = {},
            warehouse, me = this, physicalDocumentNo, movementDate;

        callback = function (rpcResponse, data, rpcRequest) {
          var status = rpcResponse.status,
              context = rpcRequest.clientContext,
              view = context.originalView.getProcessOwnerView(context.popup.params.processId);
          me.enable();
          if (data.message) {
            view.messageBar.setMessage(data.message.severity, data.message.title, data.message.text);
          }
          rpcRequest.clientContext.popup.closeClick();
          rpcRequest.clientContext.originalView.refresh(false, false);
        };
        me.disable();
        
        plType = this.popup.mainform.getItem('PLType').getValue();
        groupingCrit = this.popup.mainform.getItem('Grouping').getValue();
        physicalDocumentNo = this.popup.mainformSection1.getItem('physicalDocumentNo').getValue();
        movementDate = this.popup.mainformSection2.getItem('movementDate').getValue();

        if(this.popup.mainformSection1.getItem('physicalDocumentNo').isVisible()) {
          //Validating Physical Document No   
          if(physicalDocumentNo==null || physicalDocumentNo == "") {
           	isc.showMessage(OB.I18N.getLabel('SCR_PhyDocNo_IncorrectFormat'), OB.I18N.getLabel('OBUIAPP_Error'));
           	this.popup.closeClick();
            return;
          }
          
          var phydocno_reg = new RegExp("^[0-9]{3}-[0-9]{7}", "i");
          if(!physicalDocumentNo.match(phydocno_reg) || physicalDocumentNo.length != 11) {
        	isc.showMessage(OB.I18N.getLabel('SCR_PhyDocNo_IncorrectFormat'), OB.I18N.getLabel('OBUIAPP_Error'));
        	this.popup.closeClick();
            return;
          } 
        }
        
        if (plType === 'OUT') {
          for (i = 0; i < this.popup.warehouses.length; i++) {
            warehouse = this.popup.warehouses[i];
            locators[warehouse.id] = this.popup.mainform.getItem('Loc' + warehouse.id).getValue();
          }
        }

        OB.RemoteCallManager.call('org.openbravo.warehouse.pickinglist.CreateActionHandler', {
          orders: orders,
          action: 'create',
          plType: plType,
          groupingCrit: groupingCrit,
          locators: locators,
          physicalDocumentNo: physicalDocumentNo,
          movementDate: movementDate
        }, {}, callback, {
          originalView: this.popup.view,
          popup: this.popup
        });
      }
    });

    this.cancelButton = isc.OBFormButton.create({
      title: OB.I18N.getLabel('OBUISC_Dialog.CANCEL_BUTTON_TITLE'),
      popup: this,
      action: function () {
        this.popup.closeClick();
      }
    });
     

    this.getGroupingCriteriaList(this.mainform);
    this.getOutboundLocatorLists(this.mainform);      
    this.items = [
    isc.VLayout.create({
      defaultLayoutAlign: 'center',
      align: 'center',
      width: '100%',
      layoutMargin: 15,
      membersMargin: 1,
      members: [
      isc.HLayout.create({
          defaultLayoutAlign: 'center',
          align: 'center',
          layoutMargin: 1,
          membersMargin: 1,
          members: this.mainformSection1
      }), isc.HLayout.create({
          defaultLayoutAlign: 'center',
          align: 'center',
          layoutMargin: 1,
          membersMargin: 1,
          members: this.mainformSection2
      }), isc.HLayout.create({
        defaultLayoutAlign: 'center',
        align: 'center',
        layoutMargin: 100,
        membersMargin: 20,
        members: [this.okButton, this.cancelButton]
      })]
    })];

    this.Super('initWidget', arguments);
  }

});