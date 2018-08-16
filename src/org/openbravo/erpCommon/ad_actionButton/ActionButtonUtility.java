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
 * All portions are Copyright (C) 2001-2012 Openbravo SLU 
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_actionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.DalUtil;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.SQLReturnObject;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.ui.Tab;

public class ActionButtonUtility {
  static Logger log4j = Logger.getLogger(ActionButtonUtility.class);

  public static FieldProvider[] docAction(ConnectionProvider conn, VariablesSecureApp vars,
      String strDocAction, String strReference, String strDocStatus, String strProcessing,
      String strTable) {
    return docAction(conn, vars, strDocAction, strReference, "", strDocStatus, strProcessing,
        strTable, null);
  }

  public static FieldProvider[] docAction(ConnectionProvider conn, VariablesSecureApp vars,
      String strDocAction, String strReference, String strDocStatus, String strProcessing,
      String strTable, String tabId) {
    return docAction(conn, vars, strDocAction, strReference, "", strDocStatus, strProcessing,
        strTable, tabId);
  }

  public static FieldProvider[] docAction(ConnectionProvider conn, VariablesSecureApp vars,
      String strDocAction, String strReference, String validationRule, String strDocStatus,
      String strProcessing, String strTable, String tabId) {

    FieldProvider[] ld = null;
    boolean isQuotation = false;
    String windowId = "";
    if (tabId != null) {
      OBContext.setAdminMode(true);
      try {
        Tab tab = OBDal.getInstance().get(Tab.class, tabId);
        windowId = DalUtil.getId(tab.getWindow()).toString();
      } finally {
        OBContext.restorePreviousMode();
      }
    }

    System.out.println("DocAction: " + strDocAction);

    if (log4j.isDebugEnabled())
      log4j.debug("DocAction - generating combo elements for table: " + strTable
          + " - actual status: " + strDocStatus);
    try {
      ComboTableData comboTableData = new ComboTableData(vars, conn, "LIST", "DocAction",
          strReference, validationRule,
          Utility.getContext(conn, vars, "#AccessibleOrgTree", "ActionButtonUtility"),
          Utility.getContext(conn, vars, "#User_Client", "ActionButtonUtility"), 0);
      Utility.fillSQLParameters(conn, vars, null, comboTableData, "ActionButtonUtility", "");
      ld = comboTableData.select(false);
      comboTableData = null;
      isQuotation = "Y"
          .equals(vars.getGlobalVariable("inpisQuotation", windowId + "|isQuotation", "N"));

    } catch (Exception e) {
      return null;
    }
    SQLReturnObject[] data = null;
    if (ld != null) {
      System.out.println("Docstatus: " + strDocStatus + "Processin: " + strProcessing);
      Vector<Object> v = new Vector<Object>();
      SQLReturnObject data1 = new SQLReturnObject();
      if (!strProcessing.equals("") && strProcessing.equals("Y")) {
        data1.setData("ID", "XL");
        v.addElement(data1);
      } else if (strDocStatus.equals("NA")) {
        data1.setData("ID", "AP");
        v.addElement(data1);
        data1 = new SQLReturnObject();
        data1.setData("ID", "RJ");
        v.addElement(data1);
        data1 = new SQLReturnObject();
        data1.setData("ID", "VO");
        v.addElement(data1);
      } else if (strTable.equals("319") && ((strDocStatus.equals("DR")
          && (strDocAction.equals("RC") || strDocAction.equals("VO")))
          || strDocStatus.equals("IP"))) {// ADD
                                          // by
                                          // vafaster-
                                          // que
                                          // permita
                                          // anular
                                          // en
                                          // borrador
                                          // solo
                                          // en
                                          // m_inout.
        data1.setData("ID", "RC");
        v.addElement(data1);
      } else if (strDocStatus.equals("DR") || strDocStatus.equals("IP")) {
        data1.setData("ID", "CO");
        v.addElement(data1);
        if (!strTable.equals("319") && !strTable.equals("800212") && !isQuotation
            && !strTable.equals("34DFF9B0BB2049F99A27E15BC5414AB8")
            && !strTable.equals("29987BE302E04781BE3868E573123BDE")
            && !strTable.equals("E0AFCD80B2324D9BA19B59B8022F617D")
            && !strTable.equals("205928D6E57C4C8BA3C0A7D171BB6B2D")
            && !strTable.equals("A20EE72856B54B809674D555A833F35C")
            && !strTable.equals("135FAE2571DB4028A90D5CAA6FAC154C")
            && !strTable.equals("E39A858849824832BB2E29CF50E77FC7")
            && !strTable.equals("D4D911187F9A4A82BA9711B336A1FA48")
            && !strTable.equals("8E8797CA9AC74273AEF3BBCBDFFC5268")
            && !strTable.equals("D88C8D9118BF4EC39EDB66737B995BAF")
            && !strTable.equals("F90F4E012DF74D2B92BACC79473FF588")
            && !strTable.equals("A64BF5FB928C4EC1BACC023D6DC87F3C")
            && !strTable.equals("89B85662FE7E44A28E18BD87D50B4E09")
            && !strTable.equals("6207EB14058E4FC1825FBFD76B177448") && !strTable.equals("259")
            && !strTable.equals("9845154D73A340FDB9F4ACD8A53B75A4")) {
          // Exclude Void for tables because it has
          // no sense for them
          if (!tabId.equals("2A33566839434530AA02750DCEE58553")
              & !tabId.equals("7BCF30AEED0D4693B0848E97DC2A5EA6")
              & !tabId.equals("9FB63E7E3D8C49989EE16DBD724E5DD0")
              & !tabId.equals("8C184028462E478190184EC8C4D3757F")) {
            // Exclude Void for tab id and others because it has
            // no sense for them
            data1 = new SQLReturnObject();
            data1.setData("ID", "VO");
            v.addElement(data1);
          }
        }
      } else if (strDocStatus.equals("SCO_OP")) {
        data1.setData("ID", "CO");
        v.addElement(data1);
      } else if ((strDocStatus.equals("CO")) && !(strTable.equals("318")) // C_Invoice
          && !(strTable.equals("319")) // M_InOut
          && !(strTable.equals("35231661FFD74509BC01C34087475A62"))
          && !(strTable.equals("205928D6E57C4C8BA3C0A7D171BB6B2D"))
          && !(strTable.equals("135FAE2571DB4028A90D5CAA6FAC154C"))
          && !(strTable.equals("E39A858849824832BB2E29CF50E77FC7"))
          && !(strTable.equals("D4D911187F9A4A82BA9711B336A1FA48"))
          && !(strTable.equals("8E8797CA9AC74273AEF3BBCBDFFC5268"))
          && !(strTable.equals("D88C8D9118BF4EC39EDB66737B995BAF"))
          && !(strTable.equals("F90F4E012DF74D2B92BACC79473FF588"))
          && !(strTable.equals("A64BF5FB928C4EC1BACC023D6DC87F3C"))
          && !(strTable.equals("89B85662FE7E44A28E18BD87D50B4E09"))
          && !(strTable.equals("6207EB14058E4FC1825FBFD76B177448")) && !(strTable.equals("224"))
          && !(strTable.equals("29987BE302E04781BE3868E573123BDE"))
          && !(strTable.equals("A20EE72856B54B809674D555A833F35C"))) {
        // Exclude Close for tables C_Invoice and others because it has
        // no sense for them

        data1.setData("ID", "CL");
        v.addElement(data1);
      }

      else if ((strDocStatus.equals("CO"))
          && (strTable.equals("A20EE72856B54B809674D555A833F35C"))) {
        data1.setData("ID", "RC");
        v.addElement(data1);
        data1.setData("ID", "VO");
        v.addElement(data1);
      }

      data1 = new SQLReturnObject();
      if (strTable.equals("259") && !isQuotation) { // C_Order
        if (strDocStatus.equals("DR")) {
          // data1.setData("ID", "PR");
          // v.addElement(data1);
        } else if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("259") && isQuotation) { // Quotations (C_Order)
        if (strDocStatus.equals("UE")) {
          data1.setData("ID", "RJ");
          v.addElement(data1);
          data1 = new SQLReturnObject();
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("318")) { // C_Invoice
        if (strDocStatus.equals("CO")) {

          // do not allow to void completed documents
          /*
           * if (tabId != null) { if (!tabId.equals("A541868EEDE74E7F9ED523A9EB7A5364")) {
           * data1.setData("ID", "RC"); v.addElement(data1); } } else { data1.setData("ID", "RC");
           * v.addElement(data1); }
           */

          data1 = new SQLReturnObject();
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("319")) { // M_InOut
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RC");
          v.addElement(data1);
        }
      } else if (strTable.equals("224")) { // GL_Journal
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("800212")) { // M_Requisition
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("F90F4E012DF74D2B92BACC79473FF588")) { // SCO_Rendicion_Cuentas
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("E39A858849824832BB2E29CF50E77FC7")) { // SPR_Budget
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        } else if (strDocStatus.equals("PD")) {
          data1.setData("ID", "AP");
          v.addElement(data1);
          data1 = new SQLReturnObject();
          data1.setData("ID", "DP");
          v.addElement(data1);
        } else if (strDocStatus.equals("DP")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("D4D911187F9A4A82BA9711B336A1FA48")) { // SSA_ProjProp_Contract
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("8E8797CA9AC74273AEF3BBCBDFFC5268")) { // SRE_Purchase_Contract
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("D88C8D9118BF4EC39EDB66737B995BAF")) { // SRE_Contract_Payschedule
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("135FAE2571DB4028A90D5CAA6FAC154C")) { // SCO_Prepayment
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("F90F4E012DF74D2B92BACC79473FF588")) { // SCO_Rendicioncuentas
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("251100CF447643CFAAFCF7F6A4975DA8")) { // SCO_Swithholding_Receipt
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("6E25DC026D874BD58792E1C3994CEF16")) { // SCO_Pwithholding_Receipt
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("34DFF9B0BB2049F99A27E15BC5414AB8")) { // SCO_Percep_Purch
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("8E77C2B5AAC6499EA7799BAC36AE19AE")) { // SCO_Percep_Purch
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("29987BE302E04781BE3868E573123BDE")) { // SCO_Billofexchange
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RC");
          v.addElement(data1);

          data1 = new SQLReturnObject();
          data1.setData("ID", "RE");
          v.addElement(data1);

          System.out.println("Enter Here");
        }
      } else if (strTable.equals("E0AFCD80B2324D9BA19B59B8022F617D")) { // SCO_Boe_To_Discount
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("35231661FFD74509BC01C34087475A62")) { // SCO_Telecredit
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("A20EE72856B54B809674D555A833F35C")) { // SCO_Transferinout
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("3501CEBEF3F94D609E66F3B4CF8A7BBB")) { // SCO_Compensationpmt
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("3E2A6A3E5B954E75A5317EFF18C1C4DD")) { // SSA_Checkmanagement
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("8FE32797DCD14BF782E24B6CA4AB7ECF")) {
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("3EB7AFFC840945E5A960DC47625960F9")) {
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("A64BF5FB928C4EC1BACC023D6DC87F3C")) { // SCO_Internal_Doc
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("6207EB14058E4FC1825FBFD76B177448")) { // SCO_Autodetrac_Doc
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("B9A0AAF2D01E4BB5B2F76830DD7C570F")) { // SSA_Pricechange_Doc
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("9845154D73A340FDB9F4ACD8A53B75A4")) { // SCO_Factoringinvoice
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("205928D6E57C4C8BA3C0A7D171BB6B2D")) { // SCO_Loan_Doc
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      } else if (strTable.equals("89B85662FE7E44A28E18BD87D50B4E09")) { // SCO_Ple8_14_Reg
        if (strDocStatus.equals("CO")) {
          data1.setData("ID", "RE");
          v.addElement(data1);
        }
      }

      data = new SQLReturnObject[v.size()];
      if (log4j.isDebugEnabled())
        log4j.debug("DocAction - total combo elements: " + data.length);
      int ind1 = 0, ind2 = 0;
      while (ind1 < ld.length && ind2 < v.size()) {
        for (int j = 0; j < v.size(); j++) {
          SQLReturnObject sqlro = (SQLReturnObject) v.get(j);
          if (sqlro.getField("ID").equals(ld[ind1].getField("ID"))) {
            if (log4j.isDebugEnabled())
              log4j.debug("DocAction - Element: " + ind1 + " - ID: " + sqlro.getField("ID"));
            data[ind2] = sqlro;
            data[ind2].setData("NAME", ld[ind1].getField("NAME"));
            data[ind2].setData("DESCRIPTION", ld[ind1].getField("DESCRIPTION"));
            ind2++;
            break;
          }
        }
        ind1++;
      }
      // Exclude null values in the array
      List<SQLReturnObject> result = new ArrayList<SQLReturnObject>();
      for (SQLReturnObject sqlr : data) {
        if (sqlr != null) {
          result.add(sqlr);
        }
      }
      data = result.toArray(new SQLReturnObject[0]);

    }

    /*
     * for (int i = 0; i < data.length; i++) { System.out.println(data[i].getField("id") + " - " +
     * data[i].getField("name") + " - " + data[i].getField("description")); }
     */

    return data;
  }

  public static FieldProvider[] projectAction(ConnectionProvider conn, VariablesSecureApp vars,
      String strProjectAction, String strReference, String strProjectStatus) {
    FieldProvider[] ld = null;
    try {
      ComboTableData comboTableData = new ComboTableData(vars, conn, "LIST", "ProjectAction",
          strReference, "",
          Utility.getContext(conn, vars, "#AccessibleOrgTree", "ActionButtonUtility"),
          Utility.getContext(conn, vars, "#User_Client", "ActionButtonUtility"), 0);
      Utility.fillSQLParameters(conn, vars, null, comboTableData, "ActionButtonUtility", "");
      ld = comboTableData.select(false);
      comboTableData = null;
    } catch (Exception e) {
      return null;
    }
    SQLReturnObject[] data = null;
    if (ld != null) {
      Vector<Object> v = new Vector<Object>();
      SQLReturnObject data1 = new SQLReturnObject();
      if (strProjectStatus.equals("NF") || strProjectStatus.equals("OP")) {
        data1.setData("ID", "OR");
        v.addElement(data1);
        data1 = new SQLReturnObject();
        data1.setData("ID", "OC");
        v.addElement(data1);
      } else if (strProjectStatus.equals("OR")) {
        data1.setData("ID", "OC");
        v.addElement(data1);
      }

      if (v.size() > 0) {
        data = new SQLReturnObject[v.size()];
        v.copyInto(data);
        for (int i = 0; i < data.length; i++) {
          for (int j = 0; j < ld.length; j++) {
            if (data[i].getField("ID").equals(ld[j].getField("ID"))) {
              data[i].setData("NAME", ld[j].getField("NAME"));
              data[i].setData("DESCRIPTION", ld[j].getField("DESCRIPTION"));
              break;
            }
          }
        }
      }
    }
    return data;
  }
}
