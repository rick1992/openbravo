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
 * Contributor(s):  __________
 ************************************************************************
 */
package pe.com.unifiedgo.core;

import java.util.Map;

import org.codehaus.jettison.json.JSONObject;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.service.db.DbUtility;

import pe.com.unifiedgo.core.ad_callouts.SCR_BPartner_IsCompany;
import pe.com.unifiedgo.core.addons.AddonsCentral;

/**
 * Sums the orders passed in through a json array and returns the result.
 */
public class BPInfoActionHandler extends BaseActionHandler {

  private String nombres = "";
  private String apPaterno = "";
  private String apMaterno = "";
  private final Object lock = new Object();

  class ReniecQuery implements Runnable {

    private String taxId = "";

    public ReniecQuery(String taxId) {
      this.taxId = taxId;
    }

    @Override
    public void run() {
      try {
        String[] strData = AddonsCentral.getNamesReniec(taxId);

        if (strData != null && strData[0] != null && strData[0].length() > 0) {
          synchronized (lock) {
            nombres = strData[0];
            apPaterno = strData[1];
            apMaterno = strData[2];
          }
        }
      } catch (Exception ex) {
      }
    }
  }

  @Override
  protected JSONObject execute(Map<String, Object> parameters, String data) {
    JSONObject jsonResponse = new JSONObject();

    try {
      // get the data as json
      final JSONObject jsonData = new JSONObject(data);
      final String strTaxID = jsonData.getString("taxid");

      String razonSocial = "";
      String nombres = "";
      String apMaterno = "";
      String apPaterno = "";
      String direccion = "";
      String localStrApPaterno = "";
      String localStrApMaterno = "";

      int numTries = 0;
      int maxTries = 3;

      /* **** DNI *****/
      if (strTaxID.length() == 8) {
        try {
          while (numTries < maxTries) {
            String[] strData = AddonsCentral.getNamesReniec(strTaxID);
            if (strData != null && strData[0] != null && strData[0].length() > 0) {
              nombres = strData[0];
              apPaterno = strData[1];
              apMaterno = strData[2];
              break;
            }
            // Thread th1 = new Thread(new ReniecQuery(strTaxID), "th1");
            // Thread th2 = new Thread(new ReniecQuery(strTaxID), "th2");
            // Thread th3 = new Thread(new ReniecQuery(strTaxID), "th3");
            //
            // th1.start();
            // Thread.sleep(1000);
            // th2.start();
            // Thread.sleep(1000);
            // th3.start();
            //
            // th1.join();
            // th2.join();
            // th3.join();
            //
            // numTries++;
            //
            // if (this.nombres != null && this.nombres != null && this.nombres.length() > 0) {
            // nombres = this.nombres;
            // apPaterno = this.apPaterno;
            // apMaterno = this.apMaterno;
            // break;
            // }
          }

          // getting total name
          localStrApPaterno = apPaterno;
          localStrApMaterno = apMaterno;
          if (localStrApPaterno != null && !"".equals(localStrApPaterno)) {
            localStrApPaterno = " " + localStrApPaterno;
          }
          if (localStrApMaterno != null && !"".equals(localStrApMaterno)) {
            localStrApMaterno = " " + localStrApMaterno;
          }
          razonSocial = SCR_BPartner_IsCompany.getbpname(nombres, localStrApPaterno,
              localStrApMaterno, SCR_BPartner_IsCompany.maxCharBPName);

          jsonResponse.put("inpemScoFirstname", nombres);
          jsonResponse.put("inpemScoLastname", apPaterno);
          jsonResponse.put("inpemScoLastname2", apMaterno);
          jsonResponse.put("inpdescription", direccion);
          jsonResponse.put("inpname", razonSocial);

        } catch (Exception exx) {
          throw new Exception(exx.getMessage());
        }

        /* **** RUC *****/
      } else if (strTaxID.length() == 11) {
        try {
          while (numTries < maxTries) {
            String[] strData = AddonsCentral.getNamesSunat(strTaxID);
            numTries++;
            if (strData != null && strData[0] != null && strData[0].length() > 0) {
              razonSocial = strData[0];
              direccion = strData[1];
              break;
            }
          }

          jsonResponse.put("inpname", razonSocial);
          jsonResponse.put("inpdescription", direccion);
          jsonResponse.put("inpemScoFirstname", "");
          jsonResponse.put("inpemScoLastname", "");
          jsonResponse.put("inpemScoLastname2", "");

        } catch (Exception exx) {
          throw new Exception(exx.getMessage());
        }

        /* **** OTHERS *****/
      } else {
        throw new Exception("@SCR_DNI_maxlength@" + " / " + "@SCR_RUC_maxlength@");
      }

    } catch (Exception e) {
      System.out.println(e.getMessage());
      try {
        Throwable ex = DbUtility.getUnderlyingSQLException(e);
        String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();

        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text", message);
        jsonResponse.put("message", errorMessage);
      } catch (Exception e2) {
        System.out.println(e.getMessage());
      }
    }

    return jsonResponse;
  }

  public JSONObject getbpinfo(String strTaxID) {
    return getbpinfo(strTaxID, null);
  }

  public JSONObject getbpinfo(String strTaxID, String strIsNotCompany) {
    JSONObject jsonResponse = new JSONObject();

    try {
      String razonSocial = "";
      String nombres = "";
      String apMaterno = "";
      String apPaterno = "";
      String direccion = "";
      String localStrApPaterno = "";
      String localStrApMaterno = "";

      int numTries = 0;
      int maxTries = 3;

      /* **** DNI *****/
      if (strTaxID.length() == 8) {
        try {
          while (numTries < maxTries) {
            String[] strData = AddonsCentral.getNamesReniec(strTaxID);
            if (strData != null && strData[0] != null && strData[0].length() > 0) {
              nombres = strData[0];
              apPaterno = strData[1];
              apMaterno = strData[2];
              break;
            }
            // Thread th1 = new Thread(new ReniecQuery(strTaxID), "th1");
            // Thread th2 = new Thread(new ReniecQuery(strTaxID), "th2");
            // Thread th3 = new Thread(new ReniecQuery(strTaxID), "th3");
            //
            // th1.start();
            // Thread.sleep(1000);
            // th2.start();
            // Thread.sleep(1000);
            // th3.start();
            //
            // th1.join();
            // th2.join();
            // th3.join();
            //
            // numTries++;
            //
            // if (this.nombres != null && this.nombres != null && this.nombres.length() > 0) {
            // nombres = this.nombres;
            // apPaterno = this.apPaterno;
            // apMaterno = this.apMaterno;
            // break;
            // }
          }

          // getting total name
          localStrApPaterno = apPaterno;
          localStrApMaterno = apMaterno;
          if (localStrApPaterno != null && !"".equals(localStrApPaterno)) {
            localStrApPaterno = " " + localStrApPaterno;
          }
          if (localStrApMaterno != null && !"".equals(localStrApMaterno)) {
            localStrApMaterno = " " + localStrApMaterno;
          }
          razonSocial = SCR_BPartner_IsCompany.getbpname(nombres, localStrApPaterno,
              localStrApMaterno, SCR_BPartner_IsCompany.maxCharBPName);

          jsonResponse.put("inpemScoFirstname", nombres);
          jsonResponse.put("inpemScoLastname", apPaterno);
          jsonResponse.put("inpemScoLastname2", apMaterno);
          jsonResponse.put("inpdescription", direccion);
          jsonResponse.put("inpname", razonSocial);

        } catch (Exception exx) {
          throw new Exception(exx.getMessage());
        }

        /* **** RUC *****/
      } else if (strTaxID.length() == 11) {
        try {
          while (numTries < maxTries) {
            String[] strData = AddonsCentral.getNamesSunat(strTaxID);
            numTries++;
            if (strData != null && strData[0] != null && strData[0].length() > 0) {
              razonSocial = strData[0];
              direccion = strData[1];
              break;
            }
          }

          if (strIsNotCompany != null && !strIsNotCompany.isEmpty()
              && "Y".equals(strIsNotCompany)) {
            String[] personWithRUC = razonSocial.split(" ");
            if (personWithRUC.length >= 3) {
              apPaterno = personWithRUC[0];
              apMaterno = personWithRUC[1];
              for (int i = 2; i < personWithRUC.length; i++) {
                nombres = nombres.concat(personWithRUC[i] + " ");
              }
              nombres = nombres.substring(0, nombres.length() - 1);
            }
          }

          jsonResponse.put("inpname", razonSocial);
          jsonResponse.put("inpdescription", direccion);
          jsonResponse.put("inpemScoFirstname", nombres);
          jsonResponse.put("inpemScoLastname", apMaterno);
          jsonResponse.put("inpemScoLastname2", apPaterno);

        } catch (Exception exx) {
          throw new Exception(exx.getMessage());
        }

        /* **** OTHERS *****/
      } else {
        throw new Exception("@SCR_DNI_maxlength@" + " / " + "@SCR_RUC_maxlength@");
      }

    } catch (Exception e) {
      System.out.println(e.getMessage());
      try {
        Throwable ex = DbUtility.getUnderlyingSQLException(e);
        String message = OBMessageUtils.translateError(ex.getMessage()).getMessage();

        JSONObject errorMessage = new JSONObject();
        errorMessage.put("severity", "error");
        errorMessage.put("text", message);
        jsonResponse.put("message", errorMessage);
      } catch (Exception e2) {
        System.out.println(e.getMessage());
      }
    }

    return jsonResponse;
  }

}
