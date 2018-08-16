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
 * All portions are Copyright (C) 2001-2014 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package pe.com.unifiedgo.warehouse.ad_actionButton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.time.DateUtils;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.businessUtility.Preferences;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.PropertyException;
import org.openbravo.erpCommon.utility.PropertyNotFoundException;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.materialmgmt.transaction.InventoryCount;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.procurement.Requisition;
import org.openbravo.model.procurement.RequisitionLine;
import org.openbravo.service.db.CallProcess;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.SCO_Utils;

public class CreateInternalOrderRequest extends HttpSecureAppServlet {
  final private Date now = DateUtils.truncate(new Date(), Calendar.DATE);
  private static final long serialVersionUID = 1L;
  private static final BigDecimal ZERO = BigDecimal.ZERO;

  private String strClientId = "";
  private String strOrgId = "";

  private String strTabId = "";
  private String strWindowId = "";

  private String strInventoryId = "";

  long lineNo;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    // Order Order = null;
    OBError myMessage = null;
    myMessage = new OBError();
    myMessage.setTitle("");
    if (vars.commandIn("DEFAULT")) {

      strWindowId = vars.getStringParameter("inpwindowId");
      strClientId = vars.getStringParameter("inpadClientId");
      strOrgId = vars.getStringParameter("inpadOrgId");
      strTabId = vars.getStringParameter("inpTabId");
      String strRequisitionId = vars.getStringParameter("inpmRequisitionId");

      vars.setSessionValue("strRequisitionId", strRequisitionId);
      String strWarehouseId = vars.getStringParameter("inpmWarehouseId");

      InventoryCount inventory = OBDal.getInstance().get(InventoryCount.class, strInventoryId);
      OBCriteria<ShipmentInOut> shipmentIO = OBDal.getInstance()
          .createCriteria(ShipmentInOut.class);
      shipmentIO.add(Restrictions.ne(ShipmentInOut.PROPERTY_DOCUMENTSTATUS, "RC"));

      if (shipmentIO.list().size() > 0) {
        OBError myError;
        myError = new OBError();
        myError.setType("Error");
        myError.setTitle(OBMessageUtils.messageBD("Error"));
        myError.setMessage(OBMessageUtils.messageBD(this, "swa_inventory_withgoodreceipt",
            vars.getLanguage())
            + " " + shipmentIO.list().get(0).getDocumentNo());

        String strWindowPath = Utility.getTabURL(strTabId, "R", true, getDireccion());
        if (strWindowPath.equals(""))
          strWindowPath = strDefaultServlet;
        vars.setMessage(strTabId, myError);
        printPageClosePopUp(response, vars, strWindowPath);
      }

      String strAdOrgId = "";
      String strCheckedbyrbtn = "pending";
      printPageDataSheet(response, vars, strWindowId, strTabId, "--", strAdOrgId, strCheckedbyrbtn);

    } else if (vars.commandIn("SAVE")) {

      Connection conn = null;
      String[] selectedLines = null;
      String strLocatorID = null;
      int tmp = 0;

      String strWindowPath = Utility.getTabURL(strTabId, "R", true);
      if (strWindowPath.equals(""))
        strWindowPath = strDefaultServlet;

      OBError myError;
      myError = new OBError();
      myError = processRequisition(vars, conn);
      vars.setMessage(strTabId, myError);

      printPageClosePopUp(response, vars, strWindowPath);
    }
  }

  private OBError processRequisition(VariablesSecureApp vars, Connection conn) {
    OBError myMessage;

    myMessage = new OBError();
    myMessage.setType("Success");
    myMessage.setTitle(OBMessageUtils.messageBD("Success"));
    String strDocumentNo = "";
    String strBPartnerId = "";
    String strPriceListId = "";
    String strOrderDate = "";
    String strCurrencyId = "";
    String strWarehouseId = "";
    SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

    try {
      conn = getTransactionConnection();
      String strRequisitionId = vars.getSessionValue("strRequisitionId");
      Requisition requisition = OBDal.getInstance().get(Requisition.class, strRequisitionId);
      String strAdOrgId = requisition.getOrganization().getId();
      String OrderType_CmbValue = "ReposiciondecompraentreEmpresas";
      String strCOrderId = SequenceIdData.getUUID();
      strBPartnerId = requisition.getBusinessPartner().getId();
      strCurrencyId = requisition.getCurrency().getId();
      strWarehouseId = requisition.getSwaWareshouse().getId();
      strOrderDate = format.format(requisition.getCreationDate());// Temporal
      strPriceListId = requisition.getPriceList().getId();

      // Actualizando requisition como completado
      CreateInternalOrderRequestData.updateRequisition(conn, this, strRequisitionId);

      // Parámetros Básico para insertar una Orden de Compra Interna
      CreateInternalOrderRequestData[] data1 = CreateInternalOrderRequestData.selectVendorData(
          this, strBPartnerId);
      if (data1[0].poPaymenttermId == null || data1[0].poPaymenttermId.equals("")) {
        myMessage.setType("Error");
        myMessage
            .setMessage(Utility.messageBD(this, "VendorWithNoPaymentTerm", vars.getLanguage()));
        return myMessage;
      }

      String strSpecialdoctype = "SCOINTERNALPURCHASEREQUISITION";
      DocumentType c_doctype = SCO_Utils.getDocTypeFromSpecial(requisition.getOrganization(),
          strSpecialdoctype);
      if (c_doctype == null) {
        myMessage.setType("Error");
        myMessage.setMessage(Utility.messageBD(this, "SRE_DoctypeMissing", vars.getLanguage()));
        return myMessage;
      }

      String strCboItemId = CreateInternalOrderRequestData.selectCboItemByValue(this,
          OrderType_CmbValue);
      if (strCboItemId == null || strCboItemId.equals("")) {
        myMessage.setType("Error");
        myMessage.setMessage(Utility.messageBD(this, "SRE_DoctypeMissing", vars.getLanguage()));
        return myMessage;
      }

      try {
        strDocumentNo = SCO_Utils.getDocumentNo(c_doctype, "C_ORDER");

        // Creando solicitud de Compra Interna
        CreateInternalOrderRequestData
            .insertCOrderInternalRequisition(
                conn,
                this,
                strCOrderId,
                vars.getClient(),
                strAdOrgId,
                vars.getUser(),
                strDocumentNo,
                "DR",
                "CO",
                "0",
                c_doctype.getId(),
                strOrderDate,
                strOrderDate,
                strOrderDate,
                strBPartnerId,
                CreateInternalOrderRequestData.cBPartnerLocationId(this, strBPartnerId),
                CreateInternalOrderRequestData.billto(this, strBPartnerId).equals("") ? CreateInternalOrderRequestData
                    .cBPartnerLocationId(this, strBPartnerId) : CreateInternalOrderRequestData
                    .billto(this, strBPartnerId), strCurrencyId, isAlternativeFinancialFlow() ? "P"
                    : data1[0].paymentrulepo, data1[0].poPaymenttermId, data1[0].invoicerule
                    .equals("") ? "I" : data1[0].invoicerule,
                data1[0].deliveryrule.equals("") ? "A" : data1[0].deliveryrule, "I",
                data1[0].deliveryviarule.equals("") ? "D" : data1[0].deliveryviarule,
                strWarehouseId, strPriceListId, "", "", "", data1[0].poPaymentmethodId,
                strCboItemId, OrderType_CmbValue, strSpecialdoctype);
      } catch (ServletException ex) {
        try {
          if (conn != null)
            releaseRollbackConnection(conn);
        } catch (Exception ignored) {
        }
        myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
        return myMessage;
      }

      int line = 0;
      String strCOrderlineID = "";
      BigDecimal qty = new BigDecimal("0");
      BigDecimal qtyOrder = new BigDecimal("0");
      boolean insertLine = false;

      String strPriceListVersionId = CreateInternalOrderRequestData.getPricelistVersion(this,
          strPriceListId, strOrderDate);

      CreateInternalOrderRequestData[] lines = CreateInternalOrderRequestData
          .linesToOrder(
              this,
              strOrderDate,
              strAdOrgId,
              strWarehouseId,
              CreateInternalOrderRequestData.billto(this, strBPartnerId).equals("") ? CreateInternalOrderRequestData
                  .cBPartnerLocationId(this, strBPartnerId) : CreateInternalOrderRequestData
                  .billto(this, strBPartnerId), CreateInternalOrderRequestData.cBPartnerLocationId(
                  this, strBPartnerId), strCurrencyId, strPriceListVersionId, "", strRequisitionId);

      for (int i = 0; lines != null && i < lines.length; i++) {
        if ("".equals(lines[i].tax)) {
          RequisitionLine rl = OBDal.getInstance().get(RequisitionLine.class,
              lines[i].mRequisitionlineId);
          myMessage.setType("Error");
          myMessage.setMessage(String.format(OBMessageUtils.messageBD("NoTaxRequisition"),
              rl.getLineNo(), rl.getRequisition().getDocumentNo()));
          releaseRollbackConnection(conn);
          return myMessage;
        }

        if (i == 0)
          strCOrderlineID = SequenceIdData.getUUID();
        if (i == lines.length - 1) {
          insertLine = true;
          qtyOrder = qty;
        } else if (!lines[i + 1].mProductId.equals(lines[i].mProductId)
            || !lines[i + 1].mAttributesetinstanceId.equals(lines[i].mAttributesetinstanceId)
            || !lines[i + 1].description.equals(lines[i].description)
            || !lines[i + 1].priceactual.equals(lines[i].priceactual)) {
          insertLine = true;
          qtyOrder = qty;
          qty = new BigDecimal(0);
        } else {
          qty = qty.add(new BigDecimal(lines[i].lockqty));
        }
        lines[i].cOrderlineId = strCOrderlineID;
        if (insertLine) {
          insertLine = false;
          line += 1;
          BigDecimal qtyAux = new BigDecimal(lines[i].lockqty);
          qtyOrder = qtyOrder.add(qtyAux);
          if (log4j.isDebugEnabled())
            log4j.debug("Lockqty: " + lines[i].lockqty + " qtyorder: " + qtyOrder.toPlainString()
                + " new BigDecimal: " + (new BigDecimal(lines[i].lockqty)).toString() + " qtyAux: "
                + qtyAux.toString());
          try {
            CreateInternalOrderRequestData.insertCOrderline(conn, this, strCOrderlineID,
                vars.getClient(), strAdOrgId, vars.getUser(), strCOrderId, Integer.toString(line),
                strBPartnerId,
                CreateInternalOrderRequestData.cBPartnerLocationId(this, strBPartnerId),
                strOrderDate, lines[i].needbydate, lines[i].description, lines[i].mProductId,
                lines[i].mAttributesetinstanceId, strWarehouseId, lines[i].mProductUomId,
                lines[i].cUomId, lines[i].quantityorder, qtyOrder.toPlainString(), strCurrencyId,
                lines[i].pricelist, lines[i].priceactual, strPriceListId, lines[i].pricelimit,
                lines[i].tax, "", lines[i].discount, lines[i].grossUnit, lines[i].grossAmt);

          } catch (ServletException ex) {
            myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            releaseRollbackConnection(conn);
            return myMessage;
          }
          strCOrderlineID = SequenceIdData.getUUID();
        }

        CreateInternalOrderRequestData.updateLock(this, lines[i].lockqty, lines[i].priceactual,
            lines[i].mRequisitionlineId);

      }

      for (int i = 0; lines != null && i < lines.length; i++) {
        String strRequisitionOrderId = SequenceIdData.getUUID();
        try {
          CreateInternalOrderRequestData.insertRequisitionOrder(conn, this, strRequisitionOrderId,
              vars.getClient(), strAdOrgId, vars.getUser(), lines[i].mRequisitionlineId,
              lines[i].cOrderlineId, lines[i].lockqty);
        } catch (ServletException ex) {
          myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
          releaseRollbackConnection(conn);
          return myMessage;
        }

        if (lines[i].toClose.equals("Y")) {

          CreateInternalOrderRequestData.requisitionStatus(conn, this, lines[i].mRequisitionlineId,
              vars.getUser());
        }

      }
      // releaseRollbackConnection(conn);
      releaseCommitConnection(conn);

      Process process = null;
      try {
        process = OBDal.getInstance().get(Process.class, "104");
      } catch (OBException e) {
        myMessage.setMessage(Utility.messageBD(this, "ProcessRunError", vars.getLanguage()));
        return myMessage;
      }
      Map<String, String> parameters = null;
      parameters = new HashMap<String, String>();

      ProcessInstance pinstance = CallProcess.getInstance().call(process, strCOrderId, parameters);

      if (pinstance.getResult() == 0) {
        throw new OBException(OBMessageUtils.messageBD(pinstance.getErrorMsg()));
      }

    } catch (Exception e) {
      try {
        if (conn != null)
          releaseRollbackConnection(conn);
      } catch (Exception ignored) {
      }
      myMessage.setMessage(Utility.messageBD(this, "ProcessRunError", vars.getLanguage()));
      return myMessage;
    }

    myMessage.setMessage(OBMessageUtils.messageBD("SCR_orderInternalPReqCreated") + strDocumentNo);
    return myMessage;

  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars,
      String strWindowId, String strTabId, String strWarehouse, String strBpartnerId,
      String strCheckedbyrbtn) throws IOException, ServletException {

    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "pe/com/unifiedgo/warehouse/ad_actionButton/CreateInternalOrderRequest")
        .createXmlDocument();

    OBError myMessage = null;
    myMessage = new OBError();
    try {
      System.out.println("111");
      // data = CreateInternalOrderRequestData.selectLocator(this, strWarehouseId,strMProductId,
      // strChkLocatorOut,strCheckedbyrbtn );
    } catch (OBException ex) {
      myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
    }

    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("theme", vars.getTheme());
    xmlDocument.setParameter("windowId", strWindowId);
    xmlDocument.setParameter("tabId", strTabId);

    // xmlDocument.setData("structure5", data);

    /*
     * try { ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_ORG_ID",
     * "", "0C754881EAD94243A161111916E9B9C6", Utility.getContext(this, vars, "#AccessibleOrgTree",
     * "GenerateAnaliticKardex"), Utility.getContext(this, vars, "#User_Client",
     * "GenerateAnaliticKardex"), 0); // comboTableData.fillParameters(null,
     * "GenerateAnaliticKardex", ""); Utility.fillSQLParameters(this, vars, null, comboTableData,
     * "CreateInternalOrderRequest", strOrgId); xmlDocument.setData("reportAD_Org_ID",
     * "liststructure", comboTableData.select(false)); } catch (Exception ex) { throw new
     * ServletException(ex); }
     */

    /*
     * OrganizationStructureProvider osp =
     * OBContext.getOBContext().getOrganizationStructureProvider( strClientId);
     * 
     * 
     * String AdOrgPadreId = osp.getParentOrg(strOrgId);
     */

    // System.out.println(osp.getParentTree(strOrgId, true));

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private boolean isAlternativeFinancialFlow() {
    try {
      try {
        Preferences.getPreferenceValue("FinancialManagement", true, null, null, OBContext
            .getOBContext().getUser(), null, null);
      } catch (PropertyNotFoundException e) {
        return false;
      }
    } catch (PropertyException e) {
      return false;
    }
    return true;
  }

  public String getServletInfo() {
    return "Servlet Copy from order";
  } // end of getServletInfo() method
}
