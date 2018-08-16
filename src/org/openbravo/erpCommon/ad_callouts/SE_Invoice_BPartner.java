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

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.filter.IsIDFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.businessUtility.BpartnerMiscData;
import org.openbravo.erpCommon.utility.CashVATUtil;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.model.financialmgmt.payment.FinAccPaymentMethod;
import org.openbravo.model.financialmgmt.payment.PaymentTerm;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.xmlEngine.XmlDocument;

public class SE_Invoice_BPartner extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;

  public void init(ServletConfig config) {
    super.init(config);
    boolHist = false;
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    if (vars.commandIn("DEFAULT")) {
      String strChanged = vars.getStringParameter("inpLastFieldChanged");
      if (log4j.isDebugEnabled())
        log4j.debug("CHANGED: " + strChanged);
      String strBPartner = vars.getStringParameter("inpcBpartnerId");
      String strDocType = vars.getStringParameter("inpcDoctypetargetId");
      String strLocation = vars.getStringParameter("inpcBpartnerId_LOC");
      String strContact = vars.getStringParameter("inpcBpartnerId_CON");
      String strWindowId = vars.getStringParameter("inpwindowId");
      String strProjectId = vars.getStringParameter("inpcProjectId");
      String strIsSOTrx = Utility.getContext(this, vars, "isSOTrx", strWindowId);
      String strTabId = vars.getStringParameter("inpTabId");
      String strfinPaymentmethodId = vars.getStringParameter("inpfinPaymentmethodId");
      String strOrgId = vars.getStringParameter("inpadOrgId");

      // HttpSession mysessions = request.getSession();
      // String[] mysess_array = mysessions.getValueNames();
      // for (Integer i = 0; i < mysess_array.length; i++) {
      // System.out.println("session var-> Name: " + mysess_array[i] + " - Value: "
      // + mysessions.getValue(mysess_array[i]));
      // }

      // Enumeration<String> params = vars.getParameterNames();
      // while (params.hasMoreElements()) {
      // System.out.println(params.nextElement());
      // }

      try {
        if ("inpfinPaymentmethodId".equals(strChanged)) { // Payment Method changed
          printPagePaymentMethod(response, vars, strBPartner, strIsSOTrx, strfinPaymentmethodId,
              strOrgId);
        } else {
          printPage(response, vars, strBPartner, strDocType, strIsSOTrx, strWindowId, strLocation,
              strContact, strProjectId, strTabId, strOrgId);
        }
      } catch (ServletException ex) {
        pageErrorCallOut(response);
      }
    } else
      pageError(response);
  }

  private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strBPartner,
      String strDocType, String strIsSOTrx, String strWindowId, String strLocation,
      String strContact, String strProjectId, String strTabId, String strOrgId)
      throws IOException, ServletException {
    if (log4j.isDebugEnabled())
      log4j.debug("Output: dataSheet");
    XmlDocument xmlDocument = xmlEngine
        .readXmlTemplate("org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();

    if (strBPartner.equals(""))
      vars.removeSessionValue(strWindowId + "|C_BPartner_ID");

    BpartnerMiscData[] data = BpartnerMiscData.select(this, strBPartner);
    String strUserRep;
    String DocBaseType = SEInvoiceBPartnerData.docBaseType(this, strDocType);
    StringBuffer resultado = new StringBuffer();
    String PaymentTerm = "";
    BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, strBPartner);

    resultado.append("var calloutName='SE_Invoice_BPartner';\n\n");
    if (data == null || data.length == 0)
      resultado.append("var respuesta = new Array(new Array(\"inpcBpartnerLocationId\", null));");

    else {
      resultado.append("var respuesta = new Array(");
      // strUserRep = SEOrderBPartnerData.userIdSalesRep(this, data[0].salesrepId);
      strUserRep = data[0].salesrepId;
      String strPriceList = (strIsSOTrx.equals("Y") ? data[0].mPricelistId : data[0].poPricelistId);
      if (strPriceList.equalsIgnoreCase("")) {
        strPriceList = SEOrderBPartnerData.defaultPriceList(this, strIsSOTrx, vars.getClient());
      }
      resultado.append("new Array(\"inpmPricelistId\", \"" + (strPriceList.equals("")
          ? Utility.getContext(this, vars, "#M_PriceList_ID", strWindowId)
          : strPriceList) + "\"),");

      String strPaymentRule = (strIsSOTrx.equals("Y") ? data[0].paymentrule
          : data[0].paymentrulepo);
      if (strPaymentRule.equals("") && DocBaseType.endsWith("C"))
        strPaymentRule = "P";
      else if (strPaymentRule.equals("S") || strPaymentRule.equals("U") && strIsSOTrx.equals("Y"))
        strPaymentRule = "P";
      resultado.append("new Array(\"inppaymentrule\", \"" + strPaymentRule + "\"),");
      String strFinPaymentMethodId = (strIsSOTrx.equals("Y") ? data[0].finPaymentmethodId
          : data[0].poPaymentmethodId);

      String C_SpecialDocType = OBDal.getInstance().get(DocumentType.class, strDocType)
          .getScoSpecialdoctype();
      FIN_PaymentMethod paymentmethod = OBDal.getInstance().get(FIN_PaymentMethod.class,
          strFinPaymentMethodId);
      String C_SpecialMethod = null;
      if (paymentmethod != null)
        C_SpecialMethod = paymentmethod.getScoSpecialmethod();

      Query q = OBDal.getInstance().getSession().createSQLQuery(
          "SELECT c_paymentterm_id FROM c_paymentterm WHERE em_sco_specialpayterm='SCOINMEDIATETERM' AND ad_client_id='"
              + paymentmethod.getClient().getId() + "' LIMIT 1");
      String C_PaymentTerm_ID_Inmediate = (String) q.uniqueResult();
      Boolean isMemo = false;

      if (C_PaymentTerm_ID_Inmediate != null && C_SpecialDocType != null
          && (C_SpecialDocType.compareTo("SCOARTICKET") == 0
              || C_SpecialDocType.compareTo("SCOARCREDITMEMO") == 0
              || C_SpecialDocType.compareTo("SCOARDEBITMEMO") == 0
              || C_SpecialDocType.compareTo("SCOAPCREDITMEMO") == 0
              || C_SpecialDocType.compareTo("SCOAPDEBITMEMO") == 0)) {
        PaymentTerm = C_PaymentTerm_ID_Inmediate;
        isMemo = true;
      } else {
        PaymentTerm = (strIsSOTrx.equals("Y") ? data[0].cPaymenttermId : data[0].poPaymenttermId);
      }

      // PAYMENT METHOD
      // if (C_SpecialMethod != null && isMemo && C_SpecialMethod.compareTo("SCOBILLOFEXCHANGE") ==
      // 0) {
      // resultado.append("new Array(\"inpfinPaymentmethodId\", \"\"),");
      // } else {
      // resultado.append("new Array(\"inpfinPaymentmethodId\", \"" + strFinPaymentMethodId +
      // "\"),");
      // }
      resultado.append("new Array(\"inpfinPaymentmethodId\", \"" + strFinPaymentMethodId + "\"),");
      String specialmethod = "";
      if (strFinPaymentMethodId != null) {
        specialmethod = paymentmethod.getScoSpecialmethod();
      }
      resultado.append("new Array(\"inpemScoSpecialmethod\", \"" + specialmethod + "\"),");

      // AVAL
      if (data[0].emScoAvalId != null) {
        resultado.append("new Array(\"inpemScoAvalId\", \"" + data[0].emScoAvalId + "\"),");
      }

      // ///

      // PAYMENT TERM
      resultado.append("new Array(\"inpcPaymenttermId\", \"" + "" + "\"),");
      if (PaymentTerm.equalsIgnoreCase("")) {
        BpartnerMiscData[] paymentTerm = BpartnerMiscData.selectPaymentTerm(this, strOrgId,
            vars.getClient());
        if (paymentTerm.length != 0) {
          PaymentTerm = PaymentTerm.equals("") ? paymentTerm[0].cPaymenttermId : PaymentTerm;
        }
      }
      resultado.append("new Array(\"inpcPaymenttermId\", \"" + PaymentTerm + "\"),");

      // Invoice Ref in MEMO's
      resultado.append("new Array(\"inpemScoInvoicerefId\", \"\"),");

      FieldProvider[] tdv = null;
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR",
            "C_BPartner_Location_ID", "", "C_BPartner Location - Bill To",
            Utility.getContext(this, vars, "#AccessibleOrgTree", strWindowId),
            Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, "");
        tdv = comboTableData.select(false);
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      resultado.append("new Array(\"inpemScrTaxid\", \"" + bp.getTaxID() + "\"),");

      resultado.append("new Array(\"inpcBpartnerLocationId\", ");

      if (tdv != null && tdv.length > 0) {
        resultado.append("new Array(");
        if (strLocation.isEmpty()) {
          // If no location is provided, the first one is selected
          resultado.append("new Array(\"" + tdv[0].getField("id") + "\", \""
              + FormatUtilities.replaceJS(tdv[0].getField("name")) + "\", \"" + "true" + "\")");
          if (tdv.length > 1) {
            resultado.append(",\n");
          }
          for (int i = 1; i < tdv.length; i++) {
            resultado.append("new Array(\"" + tdv[i].getField("id") + "\", \""
                + FormatUtilities.replaceJS(tdv[i].getField("name")) + "\", \"" + "false" + "\")");
            if (i < tdv.length - 1)
              resultado.append(",\n");
          }
        } else {
          // If a location is provided, it is selected
          for (int i = 0; i < tdv.length; i++) {
            resultado.append("new Array(\"" + tdv[i].getField("id") + "\", \""
                + FormatUtilities.replaceJS(tdv[i].getField("name")) + "\", \""
                + (tdv[i].getField("id").equalsIgnoreCase(strLocation) ? "true" : "false") + "\")");
            if (i < tdv.length - 1) {
              resultado.append(",\n");
            }
          }
        }
        resultado.append("\n)");
      } else {
        // If not location search for nulllocation
        BpartnerMiscData[] dataloc = BpartnerMiscData.selectNullLocation(this, vars.getClient(),
            strBPartner);
        if (dataloc != null && dataloc.length != 0) {
          if (dataloc[0].cBpartnerLocationId != null) {
            resultado.append("new Array(");
            resultado.append("new Array(\"" + dataloc[0].cBpartnerLocationId + "\", \""
                + FormatUtilities.replaceJS(dataloc[0].locationname) + "\", \"" + "true" + "\")");
            resultado.append("\n)");

          } else {
            resultado.append("null");
          }
        } else {
          resultado.append("null");
        }

      }

      resultado.append("\n),");
      // resultado.append("new Array(\"inpsalesrepId\", ");
      // FieldProvider[] tld = null;
      // try {
      // ComboTableData comboTableData = new ComboTableData(vars, this, "TABLE", "",
      // "AD_User SalesRep", "", Utility.getContext(this, vars, "#AccessibleOrgTree",
      // "SEInvoiceBPartner"), Utility.getContext(this, vars, "#User_Client",
      // "SEInvoiceBPartner"), 0);
      // Utility.fillSQLParameters(this, vars, null, comboTableData, "SEInvoiceBPartner", "");
      // tld = comboTableData.select(false);
      // comboTableData = null;
      // } catch (Exception ex) {
      // throw new ServletException(ex);
      // }

      // if (tld != null && tld.length > 0) {
      // resultado.append("new Array(");
      // for (int i = 0; i < tld.length; i++) {
      // resultado.append("new Array(\"" + tld[i].getField("id") + "\", \""
      // + FormatUtilities.replaceJS(tld[i].getField("name")) + "\", \""
      // + (tld[i].getField("id").equalsIgnoreCase(strUserRep) ? "true" : "false") + "\")");
      // if (i < tld.length - 1)
      // resultado.append(",\n");
      // }
      // resultado.append("\n)");
      // } else
      // resultado.append("null");
      // resultado.append("\n),");

      resultado.append("new Array(\"inpsalesrepId\", \"" + strUserRep + "\"),");

      resultado.append("new Array(\"inpcProjectId\", \"\"),");
      resultado.append("new Array(\"inpcProjectId_R\", \"\"),");
      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_User_ID", "",
            "AD_User C_BPartner User/Contacts",
            Utility.getContext(this, vars, "#AccessibleOrgTree", strWindowId),
            Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, "");
        tdv = comboTableData.select(false);
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      resultado.append("new Array(\"inpcBpartnerContactId\", ");
      if (tdv != null && tdv.length > 0) {
        resultado.append("new Array(");
        if (strContact.isEmpty()) {
          // If a contactID has not been specified, the first one is selected
          resultado.append("new Array(\"" + tdv[0].getField("id") + "\", \""
              + FormatUtilities.replaceJS(tdv[0].getField("name")) + "\", \"" + "true" + "\")");
          if (tdv.length > 1) {
            resultado.append(",\n");
          }
          for (int i = 1; i < tdv.length; i++) {
            resultado.append("new Array(\"" + tdv[i].getField("id") + "\", \""
                + FormatUtilities.replaceJS(tdv[i].getField("name")) + "\", \"" + "false" + "\")");
            if (i < tdv.length - 1) {
              resultado.append(",\n");
            }
          }
        } else {
          for (int i = 0; i < tdv.length; i++) {
            resultado.append("new Array(\"" + tdv[i].getField("id") + "\", \""
                + FormatUtilities.replaceJS(tdv[i].getField("name")) + "\", \""
                + (tdv[i].getField("id").equalsIgnoreCase(strContact) ? "true" : "false") + "\")");
            if (i < tdv.length - 1) {
              resultado.append(",\n");
            }
          }
        }
        resultado.append("\n)");
      } else
        resultado.append("null");
      resultado.append("\n),");

      try {
        ComboTableData comboTableData = new ComboTableData(vars, this, "TABLEDIR", "AD_User_ID", "",
            "AD_User C_BPartner User/Contacts",
            Utility.getContext(this, vars, "#AccessibleOrgTree", strWindowId),
            Utility.getContext(this, vars, "#User_Client", strWindowId), 0);
        Utility.fillSQLParameters(this, vars, null, comboTableData, strWindowId, "");
        tdv = comboTableData.select(false);
        comboTableData = null;
      } catch (Exception ex) {
        throw new ServletException(ex);
      }

      resultado.append("new Array(\"inpadUserId\", ");
      if (tdv != null && tdv.length > 0) {
        resultado.append("new Array(");

        if (strContact.isEmpty()) {
          resultado.append("new Array(\"" + tdv[0].getField("id") + "\", \""
              + FormatUtilities.replaceJS(tdv[0].getField("name")) + "\", \"" + "true" + "\")");
          if (tdv.length > 1) {
            resultado.append(",\n");
          }
          for (int i = 1; i < tdv.length; i++) {
            resultado.append("new Array(\"" + tdv[i].getField("id") + "\", \""
                + FormatUtilities.replaceJS(tdv[i].getField("name")) + "\", \"" + "false" + "\")");
            if (i < tdv.length - 1) {
              resultado.append(",\n");
            }
          }
        } else {
          for (int i = 0; i < tdv.length; i++) {
            resultado.append("new Array(\"" + tdv[i].getField("id") + "\", \""
                + FormatUtilities.replaceJS(tdv[i].getField("name")) + "\", \""
                + (tdv[i].getField("id").equalsIgnoreCase(strContact) ? "true" : "false") + "\")");
            if (i < tdv.length - 1) {
              resultado.append(",\n");
            }
          }
        }

        resultado.append("\n)");
      } else
        resultado.append("null");
      resultado.append("\n),");
      String strWithHolding = SEInvoiceBPartnerData.WithHolding(this, strBPartner);
      resultado.append("new Array(\"inpcWithholdingId\", \"" + strWithHolding + "\"),");
      resultado
          .append("new Array(\"inpisdiscountprinted\", \"" + data[0].isdiscountprinted + "\")");
      String message = "";
      if (FIN_Utility.isBlockedBusinessPartner(strBPartner, "Y".equals(strIsSOTrx), 3)) {
        // If the Business Partner is blocked for this document, show an information message.
        BusinessPartner bPartner = OBDal.getInstance().get(BusinessPartner.class, strBPartner);
        if (message.length() > 0) {
          message = message + "<br>";
        }

        String detail_msg = "";
        if (bPartner.getScrMsgBlocking() != null && !"".equals(bPartner.getScrMsgBlocking().trim()))
          detail_msg = bPartner.getScrMsgBlocking();
        else
          detail_msg = OBMessageUtils.messageBD("SSA_BusinessPartnerBlocked");
        message = message + OBMessageUtils.messageBD("ThebusinessPartner") + " "
            + bPartner.getIdentifier() + " " + detail_msg;
      }
      if (data != null && data.length > 0
          && new BigDecimal(data[0].creditavailable).compareTo(BigDecimal.ZERO) < 0
          && strIsSOTrx.equals("Y")) {
        String creditLimitExceed = "" + Double.parseDouble(data[0].creditavailable) * -1;
        String automationPaymentMethod = isAutomaticCombination(vars, strBPartner, strIsSOTrx,
            strFinPaymentMethodId, strOrgId);
        if (message.length() > 0) {
          message = message + "<br>";
        }
        message = message + Utility.messageBD(this, "CreditLimitOver", vars.getLanguage())
            + creditLimitExceed + "<br/>" + automationPaymentMethod;
      }

      resultado.append(", new Array('MESSAGE', \"" + message + "\")");

      // Cash VAT
      // Purchase flow only (from Business Partner OR organization) "double cash"
      if (StringUtils.equals("N", strIsSOTrx)) {
        final String bpCashVAT = CashVATUtil.getBusinessPartnerIsCashVAT(strBPartner);
        resultado.append(", \nnew Array(\"");
        resultado.append("inpiscashvat");
        resultado.append("\", \"");
        if (StringUtils.equals("Y", bpCashVAT)) {
          resultado.append("Y");
        } else {
          final String orgCashVAT = CashVATUtil.getOrganizationIsCashVAT(strOrgId);
          final String orgDoubleCash = CashVATUtil.getOrganizationIsDoubleCash(strOrgId);
          resultado.append(
              StringUtils.equals("Y", orgCashVAT) && StringUtils.equals("Y", orgDoubleCash) ? "Y"
                  : "N");
        }
        resultado.append("\")");
      }

      // Percepcion warning//
      if (strIsSOTrx.equals("Y")) {
        // Percepcion warning//
        if (PaymentTerm.equals("")) {
          PaymentTerm = vars.getStringParameter("inpcPaymenttermId", IsIDFilter.instance);
        }
        PaymentTerm paymentTerm = OBDal.getInstance().get(PaymentTerm.class, PaymentTerm);
        DocumentType doctype = OBDal.getInstance().get(DocumentType.class, strDocType);
        Organization org = OBDal.getInstance().get(Organization.class, strOrgId);
        if (org != null && paymentTerm != null && doctype != null
            && doctype.getScoSpecialdoctype() != null && org.isScoPercepcionagent()
            && (doctype.getScoSpecialdoctype().equals("SCOARINVOICE"))) {

          BusinessPartner bpartner = OBDal.getInstance().get(BusinessPartner.class, strBPartner);
          if (bpartner != null) {
            // partner retention agent
            resultado.append(", new Array(\"inpemScrRetentionaffected\", \""
                + ((data[0].emscoretencionagent != null) ? data[0].emscoretencionagent : "N")
                + "\")");

            if (bpartner.isScoRetencionagent()) {
              // No se aplica percepcion

            } else if (bpartner.isScoPercepcionagent()) {
              // Se aplica 0.5% de percepcion
              if (paymentTerm.getScoSpecialpayterm() != null
                  && paymentTerm.getScoSpecialpayterm().equals("SCOINMEDIATETERM")) {
                resultado.append(", new Array('MESSAGE', \""
                    + OBMessageUtils.getI18NMessage("SCO_SalesHalfPercepcionInmediate", null)
                    + "\")");
              } else {
                resultado.append(", new Array('MESSAGE', \""
                    + OBMessageUtils.getI18NMessage("SCO_SalesHalfPercepcionCredit", null) + "\")");
              }
            } else {
              // Se aplica percepcion
              if (paymentTerm.getScoSpecialpayterm() != null
                  && paymentTerm.getScoSpecialpayterm().equals("SCOINMEDIATETERM")) {
                resultado.append(", new Array('MESSAGE', \""
                    + OBMessageUtils.getI18NMessage("SCO_SalesPercepcionInmediate", null) + "\")");
              } else {
                resultado.append(", new Array('MESSAGE', \""
                    + OBMessageUtils.getI18NMessage("SCO_SalesPercepcionCredit", null) + "\")");
              }
            }
          }
        }
      }

      resultado.append(");");
    }
    xmlDocument.setParameter("array", resultado.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPagePaymentMethod(HttpServletResponse response, VariablesSecureApp vars,
      String strBPartnerId, String strIsSOTrx, String strfinPaymentmethodId, String strOrgId)
      throws IOException, ServletException {

    FIN_PaymentMethod paymentmethod = OBDal.getInstance().get(FIN_PaymentMethod.class,
        strfinPaymentmethodId);
    String specialmethod = null;
    if (paymentmethod != null)
      specialmethod = paymentmethod.getScoSpecialmethod();

    XmlDocument xmlDocument = xmlEngine
        .readXmlTemplate("org/openbravo/erpCommon/ad_callouts/CallOut").createXmlDocument();
    StringBuilder result = new StringBuilder();

    String message = isAutomaticCombination(vars, strBPartnerId, strIsSOTrx, strfinPaymentmethodId,
        strOrgId);

    result.append("var calloutName='SE_Invoice_BPartner';\n\n");
    result.append("var respuesta = new Array(new Array(\"MESSAGE\", ");
    result.append("\"" + message + "\")");

    if (specialmethod != null) {
      result.append(",new Array(\"inpemScoSpecialmethod\", \"" + specialmethod + "\")");
      result.append(",new Array(\"inpspecialmethod\", \"" + specialmethod + "\")");
    } else {
      result.append(",new Array(\"inpemScoSpecialmethod\", \"\")");
      result.append(",new Array(\"inpspecialmethod\", \"\")");
    }

    result.append(");");

    xmlDocument.setParameter("array", result.toString());
    xmlDocument.setParameter("frameName", "appFrame");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  /**
   * Verifies if the given payment method belongs to the default financial account of the given
   * business partner.
   * 
   * @param vars
   *          VariablesSecureApp.
   * @param strBPartnerId
   *          Business Partner id.
   * @param strIsSOTrx
   *          Sales ('Y') or purchase ('N') transaction.
   * @param strfinPaymentmethodId
   *          Payment Method id.
   * @return Message to be displayed in the application warning the user that automatic actions
   *         could not be performed because given payment method does not belong to the default
   *         financial account of the given business partner.
   */
  private String isAutomaticCombination(VariablesSecureApp vars, String strBPartnerId,
      String strIsSOTrx, String strfinPaymentmethodId, String strOrgId) {
    BusinessPartner bpartner = OBDal.getInstance().get(BusinessPartner.class, strBPartnerId);
    FIN_PaymentMethod selectedPaymentMethod = OBDal.getInstance().get(FIN_PaymentMethod.class,
        strfinPaymentmethodId);
    OBContext.setAdminMode(true);
    try {
      boolean isSales = "Y".equals(strIsSOTrx);
      FIN_FinancialAccount account = null;
      String message = "";

      if (bpartner != null && selectedPaymentMethod != null && !"".equals(strOrgId)) {
        account = (isSales) ? bpartner.getAccount() : bpartner.getPOFinancialAccount();
        if (account != null) {
          OBCriteria<FinAccPaymentMethod> obc = OBDal.getInstance()
              .createCriteria(FinAccPaymentMethod.class);
          obc.setFilterOnReadableOrganization(false);
          obc.add(Restrictions.eq(FinAccPaymentMethod.PROPERTY_ACCOUNT, account));
          obc.add(
              Restrictions.eq(FinAccPaymentMethod.PROPERTY_PAYMENTMETHOD, selectedPaymentMethod));
          obc.add(Restrictions.in(FinAccPaymentMethod.PROPERTY_ORGANIZATION + ".id", OBContext
              .getOBContext().getOrganizationStructureProvider().getNaturalTree(strOrgId)));

          if (obc.list() == null || obc.list().size() == 0) {
            message = Utility.messageBD(this, "PaymentmethodNotbelongsFinAccount",
                vars.getLanguage());
          }
        } else {
          message = Utility.messageBD(this, "PaymentmethodNotbelongsFinAccount",
              vars.getLanguage());
        }
      }
      return message;
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
