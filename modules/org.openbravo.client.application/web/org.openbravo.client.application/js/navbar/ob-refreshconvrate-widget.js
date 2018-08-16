isc.ClassFactory.defineClass('OBRefreshConvrate', isc.ImgButton);

isc.OBRefreshConvrate.addProperties({
  showInPortal: true,
  showRefreshConvrateText: false,
  padding : 0,
  initWidget: function () {
    if (this.showRefreshConvrateText) {
      this.baseStyle = isc.OBAlertIcon.getInstanceProperty('baseStyle');
      this.height = '100%';
      this.autoFit = true;
      this.showTitle = true;
      this.src = '';
      this.title = OB.I18N.getLabel('SCO_RefreshConvRate');
    }
    this.Super('initWidget', arguments);
  },

  draw: function () {
    this.Super("draw", arguments);

    if (!this.showRefreshConvrateText) {
      this.setPrompt(OB.I18N.getLabel('SCO_RefreshConvRate'));
    }
    
    OB.TestRegistry.register('org.openbravo.client.application.navigationbarcomponents.RefreshConvrateButton', this);
  },

  click: function () {
    var handle = this.getHandle();
    
    var rcvcallback = function (rpcResponse, data, rpcRequest) {
        var status = rpcResponse.status;
        if(data.showconvrate && data.showconvrate == 'Y'){
          if (data.message) {
            OB.TopLayout.ConvRate.setContents(data.message.text);
          }
        }
    };
    
    OB.RemoteCallManager.call('org.openbravo.client.application.RefreshConvRateButtonActionHandler', {}, {},  rcvcallback);
  }
});