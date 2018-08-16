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
package org.openbravo.erpCommon.ad_callouts;

import java.math.BigDecimal;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringUtils;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.BpartnerMiscData;
import org.openbravo.erpCommon.utility.CashVATUtil;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;

import pe.com.unifiedgo.accounting.SCO_Utils;

public class SE_Order_BPartner extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    // General data

    /*
     * Enumeration e = info.vars.getParameterNames(); while (e.hasMoreElements()) {
     * System.out.println(e.nextElement()); }
     */

    String strChanged = info.vars.getStringParameter("inpLastFieldChanged");
    if (log4j.isDebugEnabled())
      log4j.debug("CHANGED: " + strChanged);
    if ("inpfinPaymentmethodId".equals(strChanged)) { // Payment Method
      // changed
      printPagePaymentMethod(info);
    } else {
      printPage(info);
    }

  }

  protected void printPage(CalloutInfo info) throws ServletException {
    String strBPartner = info.vars.getStringParameter("inpcBpartnerId");
    String strIsSOTrx = Utility.getContext(this, info.vars, "isSOTrx", info.getWindowId());
    String strOrgId = info.vars.getStringParameter("inpadOrgId");
    String strPriceList = "";
    String strUserRep = "";
    String strInvoiceRule = "";
    String strFinPaymentMethodId = "";
    String strPaymentrule = "";
    String strDeliveryViaRule = "";
    String strPaymentterm = "";
    String strDeliveryRule = "";
    String strDocTypeTarget = info.vars.getStringParameter("inpcDoctypetargetId");
    String docSubTypeSO = "";

    BpartnerMiscData[] data = BpartnerMiscData.select(this, strBPartner);
    if (data != null && data.length > 0) {
      strDeliveryRule = data[0].deliveryrule.equals("")
          ? info.vars.getStringParameter("inpdeliveryrule")
          : data[0].deliveryrule;
      // strUserRep = SEOrderBPartnerData.userIdSalesRep(this, data[0].salesrepId);
      strUserRep = data[0].salesrepId;
      SLOrderDocTypeData[] docTypeData = SLOrderDocTypeData.select(this, strDocTypeTarget);
      if (docTypeData != null && docTypeData.length > 0) {
        docSubTypeSO = docTypeData[0].docsubtypeso;
      }
      strInvoiceRule = (docSubTypeSO.equals("PR") || docSubTypeSO.equals("WI")
          || data[0].invoicerule.equals("") ? info.vars.getStringParameter("inpinvoicerule")
              : data[0].invoicerule);
      strPaymentrule = (strIsSOTrx.equals("Y") ? data[0].paymentrule : data[0].paymentrulepo);
      strPaymentrule = strPaymentrule.equals("") ? info.vars.getStringParameter("inppaymentrule")
          : strPaymentrule;

      strPaymentterm = (strIsSOTrx.equals("Y") ? data[0].cPaymenttermId : data[0].poPaymenttermId);
      if (strPaymentterm.equalsIgnoreCase("")) {
        BpartnerMiscData[] paymentTerm = BpartnerMiscData.selectPaymentTerm(this, strOrgId,
            info.vars.getClient());
        if (paymentTerm.length != 0) {
          strPaymentterm = strPaymentterm.equals("") ? paymentTerm[0].cPaymenttermId
              : strPaymentterm;
        }
      }
      strPaymentterm = strPaymentterm.equals("") ? info.vars.getStringParameter("inpcPaymenttermId")
          : strPaymentterm;

      strFinPaymentMethodId = (strIsSOTrx.equals("Y") ? data[0].finPaymentmethodId
          : data[0].poPaymentmethodId);

      strPriceList = (strIsSOTrx.equals("Y") ? data[0].mPricelistId : data[0].poPricelistId);
      if (strPriceList.equalsIgnoreCase("")) {
        strPriceList = SEOrderBPartnerData.defaultPriceList(this, strIsSOTrx,
            info.vars.getClient());
      }
      strPriceList = strPriceList.equals("") ? info.vars.getStringParameter("inpmPricelistId")
          : strPriceList;
      strDeliveryViaRule = data[0].deliveryviarule.equals("")
          ? info.vars.getStringParameter("inpdeliveryviarule")
          : data[0].deliveryviarule;
    }

    // Price list
    info.addResult("inpmPricelistId",
        strPriceList.equals("")
            ? Utility.getContext(this, info.vars, "#M_PriceList_ID", info.getWindowId())
            : strPriceList);

    // BPartner Location

    FieldProvider[] tdv = null;

    String strLocation = info.vars.getStringParameter("inpcBpartnerId_LOC");
    if (strLocation != null && !strLocation.isEmpty()) {
      info.addResult("inpcBpartnerLocationId", strLocation);
    } else {
      // C_Bpartner_Location
      FieldProvider[] tld = null;
      try {
        ComboTableData comboTableData = new ComboTableData(info.vars, this, "TABLEDIR",
            "C_BPartner_Location_ID", "", "C_BPartner Location - Ship To",
            Utility.getContext(this, info.vars, "#AccessibleOrgTree", info.getWindowId()),
            Utility.getContext(this, info.vars, "#User_Client", info.getWindowId()), 0);
        Utility.fillSQLParameters(this, info.vars, null, comboTableData, "SEOrderBPartner", "");
        tld = comboTableData.select(false);
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      if (tld != null && tld.length > 0) {
        info.addSelect("inpcBpartnerLocationId");
        for (int i = 0; i < tld.length; i++) {
          info.addSelectResult(tld[i].getField("id"), tld[i].getField("name"), true);
        }
        info.endSelect();
      } else {
        BpartnerMiscData[] dataloc = BpartnerMiscData.selectNullLocation(this,
            info.vars.getClient(), strBPartner);
        if (dataloc != null && dataloc.length != 0) {
          if (dataloc[0].cBpartnerLocationId != null) {
            info.addSelect("inpcBpartnerLocationId");
            info.addSelectResult(dataloc[0].cBpartnerLocationId, dataloc[0].locationname, true);
            info.endSelect();
          } else {
            info.addResult("inpcBpartnerLocationId", null);
          }
        } else {
          info.addResult("inpcBpartnerLocationId", null);
        }
      }
    }

    // Warehouses

    FieldProvider[] td = null;
    try {
      ComboTableData comboTableData = new ComboTableData(info.vars, this, "18", "M_Warehouse_ID",
          "197", strIsSOTrx.equals("Y") ? "C4053C0CD3DC420A9924F24FC1F860A0" : "",
          Utility.getReferenceableOrg(info.vars, info.vars.getStringParameter("inpadOrgId")),
          Utility.getContext(this, info.vars, "#User_Client", info.getWindowId()), 0);
      Utility.fillSQLParameters(this, info.vars, null, comboTableData, info.getWindowId(), "");
      td = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    if (td != null && td.length > 0) {
      // info.addResult("inpmWarehouseId", null);

      // OPENBRAVO CODE
      /*
       * info.addSelect("inpmWarehouseId"); String strMwarehouse = strIsSOTrx.equals("N") ?
       * SEOrderBPartnerData.mWarehouse(this, strBPartner) :
       * SEOrderBPartnerData.mWarehouseOnhand(this, strOrgId);
       * 
       * if (strMwarehouse.equals("")) { strMwarehouse = info.vars.getWarehouse(); }
       * 
       * for (int i = 0; i < td.length; i++) { info.addSelectResult(td[i].getField("id"),
       * td[i].getField("name"), td[i].getField("id").equalsIgnoreCase(strMwarehouse)); }
       * info.endSelect();
       */

    } else {
      info.addResult("inpmWarehouseId", null);
    }

    // Sales Representative

    info.addResult("inpsalesrepId", strUserRep);

    // FieldProvider[] tld = null;
    // try {
    // ComboTableData comboTableData = new ComboTableData(info.vars, this,
    // "TABLE", "",
    // "AD_User SalesRep", "", Utility.getContext(this, info.vars,
    // "#AccessibleOrgTree",
    // "SEOrderBPartner"), Utility.getContext(this, info.vars,
    // "#User_Client",
    // "SEOrderBPartner"), 0);
    // Utility.fillSQLParameters(this, info.vars, null, comboTableData,
    // "SEOrderBPartner", "");
    // tld = comboTableData.select(false);
    // comboTableData = null;
    // } catch (Exception ex) {
    // throw new ServletException(ex);
    // }

    // if (tld != null && tld.length > 0) {
    // info.addSelect("inpsalesrepId");
    // for (int i = 0; i < tld.length; i++) {
    // info.addSelectResult(tld[i].getField("id"), tld[i].getField("name"),
    // tld[i].getField("id")
    // .equalsIgnoreCase(strUserRep));
    // }
    //
    // info.endSelect();
    //
    // } else {
    // info.addResult("inpsalesrepId", null);
    // }

    // Invoice Rule
    FieldProvider[] l = null;
    try {
      ComboTableData comboTableData = null;
      if ("WR".equals(docSubTypeSO)) {
        comboTableData = new ComboTableData(info.vars, this, "LIST", "", "C_Order InvoiceRule",
            "Values for Invoice Rules for POS Sales orders",
            Utility.getContext(this, info.vars, "#AccessibleOrgTree", "SEOrderBPartner"),
            Utility.getContext(this, info.vars, "#User_Client", "SEOrderBPartner"), 0);
      } else {
        comboTableData = new ComboTableData(info.vars, this, "LIST", "", "C_Order InvoiceRule", "",
            Utility.getContext(this, info.vars, "#AccessibleOrgTree", "SEOrderBPartner"),
            Utility.getContext(this, info.vars, "#User_Client", "SEOrderBPartner"), 0);
      }
      Utility.fillSQLParameters(this, info.vars, null, comboTableData, "SEOrderBPartner", "");
      l = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    /*
     * if (l != null && l.length > 0) { info.addSelect("inpinvoicerule"); for (int i = 0; i <
     * l.length; i++) { info.addSelectResult(l[i].getField("id"), l[i].getField("name"),
     * l[i].getField("id") .equalsIgnoreCase(strInvoiceRule)); }
     * 
     * info.endSelect();
     * 
     * } else { info.addResult("inpinvoicerule", null); }
     */
    info.addResult("inpinvoicerule", "I");

    // Project

    info.addResult("inpcProjectId", "");

    // Project R

    info.addResult("inpcProjectId_R", "");

    // Financial Payment
    FIN_PaymentMethod paymentmethod = OBDal.getInstance().get(FIN_PaymentMethod.class,
        strFinPaymentMethodId);
    info.addResult("inpfinPaymentmethodId", "");
    if (!"".equals(strFinPaymentMethodId)) {
      info.addResult("inpfinPaymentmethodId", strFinPaymentMethodId);
    }

    String specialmethod = "";
    if (strFinPaymentMethodId != null) {
      specialmethod = paymentmethod.getScoSpecialmethod();
    }
    info.addResult("inpemScoSpecialmethod", specialmethod);

    // Bill to

    if (strLocation != null && !strLocation.isEmpty()) {
      info.addResult("inpbilltoId", strLocation);
    } else {
      // C_Bpartner_Location
      FieldProvider[] tldbt = null;
      try {
        ComboTableData comboTableData = new ComboTableData(info.vars, this, "TABLEDIR",
            "C_BPartner_Location_ID", "", "C_BPartner Location - Bill To",
            Utility.getContext(this, info.vars, "#AccessibleOrgTree", info.getWindowId()),
            Utility.getContext(this, info.vars, "#User_Client", info.getWindowId()), 0);
        Utility.fillSQLParameters(this, info.vars, null, comboTableData, "SEOrderBPartner", "");
        tldbt = comboTableData.select(false);
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      if (tldbt != null && tldbt.length > 0) {
        info.addSelect("inpbilltoId");
        for (int i = 0; i < tldbt.length; i++) {
          info.addSelectResult(tldbt[i].getField("id"), tldbt[i].getField("name"), true);
        }
        info.endSelect();
      } else {
        BpartnerMiscData[] dataloc = BpartnerMiscData.selectNullLocation(this,
            info.vars.getClient(), strBPartner);
        if (dataloc != null && dataloc.length != 0) {
          if (dataloc[0].cBpartnerLocationId != null) {
            info.addSelect("inpbilltoId");
            info.addSelectResult(dataloc[0].cBpartnerLocationId, dataloc[0].locationname, true);
            info.endSelect();
          } else {
            info.addResult("inpbilltoId", null);
          }
        } else {
          info.addResult("inpbilltoId", null);
        }
      }
    }

    // Payment rule

    // Temporal FIX UGO
    // info.addResult("inppaymentrule", strPaymentrule);
    info.addResult("inppaymentrule", "4");

    // Delivery via rule
    info.addResult("inpdeliveryviarule", strDeliveryViaRule);

    // Discount printed

    info.addResult("inpisdiscountprinted",
        SEOrderBPartnerData.getIsDicountPrinted(this, strBPartner));

    // Payment term
    info.addResult("inpcPaymenttermId", "");

    System.out.println("strPaymentterm:" + strPaymentterm);
    info.addResult("inpcPaymenttermId", strPaymentterm);

    // Delivery rule

    try {
      ComboTableData comboTableData = new ComboTableData(info.vars, this, "LIST", "",
          "C_Order DeliveryRule", "",
          Utility.getContext(this, info.vars, "#AccessibleOrgTree", "SEOrderBPartner"),
          Utility.getContext(this, info.vars, "#User_Client", "SEOrderBPartner"), 0);
      Utility.fillSQLParameters(this, info.vars, null, comboTableData, "SEOrderBPartner", "");
      l = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    if (strIsSOTrx.equals("N")) {
      info.addResult("inpdeliveryrule",
          strDeliveryRule.equals("") ? ((l != null && l.length > 0) ? l[0].getField("id") : "null")
              : strDeliveryRule);
    } else {

      /*
       * if (l != null && l.length > 0) { info.addSelect("inpdeliveryrule");
       * 
       * for (int i = 0; i < l.length; i++) { info.addSelectResult(l[i].getField("id"),
       * l[i].getField("name"), l[i].getField("id") .equalsIgnoreCase(strDeliveryRule)); }
       * 
       * info.endSelect(); } else { info.addResult("inpdeliveryrule", null); }
       */
      info.addResult("inpdeliveryrule", "O");
    }

    // Ad User

    try {
      ComboTableData comboTableData = new ComboTableData(info.vars, this, "TABLEDIR", "AD_User_ID",
          "", "AD_User C_BPartner User/Contacts",
          Utility.getContext(this, info.vars, "#AccessibleOrgTree", info.getWindowId()),
          Utility.getContext(this, info.vars, "#User_Client", info.getWindowId()), 0);
      Utility.fillSQLParameters(this, info.vars, null, comboTableData, info.getWindowId(), "");
      tdv = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

    if (tdv != null && tdv.length > 0) {
      info.addSelect("inpadUserId");

      String contactID = info.vars.getStringParameter("inpcBpartnerId_CON");
      if (contactID.isEmpty()) {
        // If a contactID has not been specified, the first one is
        // selected
        info.addSelectResult(tdv[0].getField("id"), tdv[0].getField("name"), true);
        for (int i = 1; i < tdv.length; i++) {
          info.addSelectResult(tdv[i].getField("id"), tdv[i].getField("name"), false);
        }
      } else {
        for (int i = 0; i < tdv.length; i++) {
          info.addSelectResult(tdv[i].getField("id"), tdv[i].getField("name"), tdv[i].getField("id")
              .equalsIgnoreCase(info.vars.getStringParameter("inpcBpartnerId_CON")));
        }
      }
      info.endSelect();

    } else {
      info.addResult("inpadUserId", null);
    }

    // Message

    BusinessPartner bPartner = OBDal.getInstance().get(BusinessPartner.class, strBPartner);
    StringBuilder message = new StringBuilder();
    final String rtvendor = "C50A8AEE6F044825B5EF54FAAE76826F";
    final String rfcustomer = "FF808081330213E60133021822E40007";
    String strwindow = info.getStringParameter("inpwindowId", null);

    if (!(strwindow.equals(rtvendor) || strwindow.equals(rfcustomer))) {
      if ((!strBPartner.equals(""))
          && (FIN_Utility.isBlockedBusinessPartner(strBPartner, "Y".equals(strIsSOTrx), 1))) {
        // If the Business Partner is blocked for this document, show an
        // information message.
        if (message.length() > 0) {
          message.append("<br>");
        }

        String detail_msg = "";
        if (bPartner.getScrMsgBlocking() != null && !"".equals(bPartner.getScrMsgBlocking().trim()))
          detail_msg = bPartner.getScrMsgBlocking();
        else
          detail_msg = OBMessageUtils.messageBD("SSA_BusinessPartnerBlocked");
        message.append(OBMessageUtils.messageBD("ThebusinessPartner") + " "
            + bPartner.getIdentifier() + " " + detail_msg);
      }

    }

    if (SCO_Utils.isOverDuedClient(strOrgId, bPartner.getClient().getId(), strBPartner)) {
      message.append(Utility.messageBD(this, "SSA_OverDuedClient", info.vars.getLanguage()));
    }

    Currency curr = OBDal.getInstance().get(Currency.class, data[0].creditcurrency);
    if (data != null && data.length > 0
        && new BigDecimal(data[0].creditavailable).compareTo(BigDecimal.ZERO) < 0
        && strIsSOTrx.equals("Y")) {
      if (message.length() > 0) {
        message.append("<br>");
      }
      String creditLimitExceed = "" + Double.parseDouble(data[0].creditavailable) * -1;
      message.append(Utility.messageBD(this, "CreditLimitOver", info.vars.getLanguage()) + "en "
          + creditLimitExceed + " " + ((curr != null) ? curr.getISOCode() : ""));

    } else if (data != null && data.length > 0
        && new BigDecimal(data[0].creditavailable).compareTo(BigDecimal.ZERO) == 0
        && strIsSOTrx.equals("Y")) {
      message.append(Utility.messageBD(this, "SSA_CreditOnLimit", info.vars.getLanguage()));

    } else if (data != null && data.length > 0
        && new BigDecimal(data[0].creditavailable).compareTo(BigDecimal.ZERO) > 0
        && strIsSOTrx.equals("Y")) {
      message.append(Utility.messageBD(this, "SSA_CreditAvailable", info.vars.getLanguage())
          + Double.parseDouble(data[0].creditavailable) + " "
          + ((curr != null) ? curr.getISOCode() : ""));
    }

    info.addResult("MESSAGE", message.toString());

    // Cash VAT
    // Purchase flow only (from Business Partner OR organization)
    // "double cash"
    if (StringUtils.equals("N", strIsSOTrx)) {
      final String bpCashVAT = CashVATUtil.getBusinessPartnerIsCashVAT(strBPartner);
      if (StringUtils.equals("Y", bpCashVAT)) {
        info.addResult("inpiscashvat", "Y");
      } else {
        final String orgCashVAT = CashVATUtil.getOrganizationIsCashVAT(strOrgId);
        final String orgDoubleCash = CashVATUtil.getOrganizationIsDoubleCash(strOrgId);
        info.addResult("inpiscashvat",
            StringUtils.equals("Y", orgCashVAT) && StringUtils.equals("Y", orgDoubleCash) ? "Y"
                : "N");
      }
    }
  }

  protected void printPagePaymentMethod(CalloutInfo info) throws ServletException {
    System.out.println("cambio el payment methoddd");
    String strFinPaymentMethodId = info.getStringParameter("inpfinPaymentmethodId",
        IsIDFilter.instance);
    String specialmethod = null;
    FIN_PaymentMethod paymentmethod = OBDal.getInstance().get(FIN_PaymentMethod.class,
        strFinPaymentMethodId);
    if (strFinPaymentMethodId != null) {
      specialmethod = paymentmethod.getScoSpecialmethod();
    }

    if (specialmethod != null) {
      info.addResult("inpemScoSpecialmethod", specialmethod);
    } else {
      info.addResult("inpemScoSpecialmethod", "");
    }

  }

}
