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
 * All portions are Copyright (C) 2012-2016 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package org.openbravo.costing;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.core.OBInterceptor;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBQuery;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.materialmgmt.cost.CostingRule;
import org.openbravo.model.materialmgmt.transaction.MaterialTransaction;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOut;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;

import pe.com.unifiedgo.core.data.SCRComboItem;

/**
 * @author gorkaion
 * 
 */

class ImportException extends Exception {
  public ImportException(String msg) {
    super(msg);
  }
}

public class CostingBackground extends DalBaseProcess {
  private static final Logger log4j = Logger.getLogger(CostingBackground.class);
  private ProcessLogger logger;
  private int maxTransactions = 0;
  private final int BATCH_SIZE = 2000;

  HashMap<String, String> productWithoutCostingImp = new HashMap<String, String>();

  @Override
  protected void doExecute(ProcessBundle bundle) throws Exception {

    logger = bundle.getLogger();
    OBError result = new OBError();
    List<String> orgsWithRule = new ArrayList<String>();
    List<String> trxs = null;
    try {

      String strDateTo = (String) bundle.getParams().get("dateto");
      if (strDateTo == null)
        strDateTo = "";

      String strAdOrgId = (String) bundle.getParams().get("adOrgId");
      if (strAdOrgId == null)
        strAdOrgId = "";

      System.out.println("strDateTo:" + strDateTo + " -- strAdOrgId:" + strAdOrgId);

      String borgId = bundle.getContext().getOrganization();
      if (!strAdOrgId.isEmpty()) {
        borgId = strAdOrgId;
      }

      Date dateto = new Date();
      if (!strDateTo.isEmpty()) {
        dateto = FIN_Utility.getDate(strDateTo);
      }

      System.out.println("final borgId:" + borgId + " - dateto:" + dateto);
      OBContext.setAdminMode(false);
      OBInterceptor.setPreventUpdateInfoChange(true);

      result.setType("Success");
      result.setTitle(OBMessageUtils.messageBD("Success"));

      // Get organizations with costing rules.
      StringBuffer where = new StringBuffer();
      where.append(" as o");
      where.append(" where exists (");
      where.append("    select 1 from " + CostingRule.ENTITY_NAME + " as cr");
      where.append("    where ad_isorgincluded(o.id, cr." + CostingRule.PROPERTY_ORGANIZATION + ".id, " + CostingRule.PROPERTY_CLIENT + ".id) <> -1 ");
      where.append("      and cr." + CostingRule.PROPERTY_VALIDATED + " is true");
      where.append(" )");
      where.append("    and ad_isorgincluded(o.id, '" + borgId + "', '" + bundle.getContext().getClient() + "') <> -1 ");
      OBQuery<Organization> orgQry = OBDal.getInstance().createQuery(Organization.class, where.toString());
      List<Organization> orgs = orgQry.list();
      if (orgs.size() == 0) {
        log4j.debug("No organizations with Costing Rule defined");
        logger.logln(OBMessageUtils.messageBD("Success"));
        bundle.setResult(result);
        return;
      }
      for (Organization org : orgs) {
        orgsWithRule.add(org.getId());
      }

      // Fix the Not Processed flag for those Transactions with Cost Not Calculated
      setNotProcessedWhenNotCalculatedTransactions(orgsWithRule);

      int batch = 0;
      int counter = 0;
      int counterBatch;
      maxTransactions = getTransactionsRowCount(orgsWithRule, dateto);
      System.out.println("maxTransactions:" + maxTransactions);
      trxs = getTransactionsBatch(orgsWithRule, dateto);
      int total = trxs.size();
      int maxIterations = 50;
      boolean bTrxActivo = false;
      while (!trxs.isEmpty() && counter < maxTransactions) {
        long t1 = System.currentTimeMillis();
        batch++;
        maxIterations--;

        counterBatch = 0;
        bTrxActivo = false;
        for (String trxId : trxs) {
          long t3 = System.currentTimeMillis();
          log4j.debug("Starting transaction process: " + trxId);
          counter++;
          counterBatch++;

          MaterialTransaction transaction = OBDal.getInstance().get(MaterialTransaction.class, trxId);

          if (productWithoutCostingImp.containsKey(transaction.getProduct().getId())) {
            System.out.println("No costear este producto aun " + transaction.getProduct().getSearchKey() + " " + transaction.getId());
            continue;
          }

          bTrxActivo = true;
          System.out.println("counter costing: " + counter + " total:" + total);

          try {
            log4j.debug("Start transaction process: " + transaction.getId());
            CostingServer transactionCost = new CostingServer(transaction);
            transactionCost.process();
            log4j.debug("Transaction processed: " + counter + "/" + total + " batch: " + batch);

          } catch (ImportException e) {
            productWithoutCostingImp.put(transaction.getProduct().getId(), transaction.getProduct().getId());
          } catch (OBException e) {

            String resultMsg = OBMessageUtils.parseTranslation(e.getMessage());
            log4j.error(e);
            logger.logln(resultMsg);
            result.setType("Error");
            result.setTitle(OBMessageUtils.messageBD("Error"));
            result.setMessage(resultMsg);
            bundle.setResult(result);

            System.out.println("trans:" + transaction.getId() + " " + e.getMessage() + "\n");

            return;
          } catch (Exception e) {

            result = OBMessageUtils.translateError(bundle.getConnection(), bundle.getContext().toVars(), OBContext.getOBContext().getLanguage().getLanguage(), e.getMessage());
            log4j.error(result.getMessage(), e);
            logger.logln(result.getMessage());
            bundle.setResult(result);

            e.printStackTrace();

            return;
          }
          long t4 = System.currentTimeMillis();
          log4j.debug("Ending transaction process: transaction: " + counter + ", batch: " + batch + ". Took: " + (t4 - t3) + " ms.");

          // If cost has been calculated successfully do a commit.
          OBDal.getInstance().getSession().clear();
          OBDal.getInstance().getConnection(true).commit();

        }

        if (bTrxActivo) {
          System.out.println("bTrx");
          if (maxIterations == 0)
            break;

          trxs = getTransactionsBatch(orgsWithRule, dateto);
          total += trxs.size();
        }
        long t2 = System.currentTimeMillis();
        log4j.debug("Processing batch: " + batch + " (" + counterBatch + " transactions) took: " + (t2 - t1) + " ms.");
      }

      logger.logln(OBMessageUtils.messageBD("Success"));
      bundle.setResult(result);
    } catch (OBException e) {
      OBDal.getInstance().rollbackAndClose();
      String message = OBMessageUtils.parseTranslation(bundle.getConnection(), bundle.getContext().toVars(), OBContext.getOBContext().getLanguage().getLanguage(), e.getMessage());
      result.setMessage(message);
      result.setType("Error");
      log4j.error(message, e);
      logger.logln(message);
      bundle.setResult(result);
      return;
    } catch (Exception e) {
      OBDal.getInstance().rollbackAndClose();
      result = OBMessageUtils.translateError(bundle.getConnection(), bundle.getContext().toVars(), OBContext.getOBContext().getLanguage().getLanguage(), e.getMessage());
      result.setType("Error");
      result.setTitle(OBMessageUtils.messageBD("Error"));
      log4j.error(result.getMessage(), e);
      logger.logln(result.getMessage());
      bundle.setResult(result);
      return;
    } finally {
      OBInterceptor.setPreventUpdateInfoChange(false);
      OBContext.restorePreviousMode();
    }
  }

  /**
   * Get Transactions with Processed flag = 'Y' and it's cost is Not Calculated and set Processed
   * flag = 'N'
   */
  private void setNotProcessedWhenNotCalculatedTransactions(List<String> orgsWithRule) {
    final StringBuilder hqlTransactions = new StringBuilder();
    hqlTransactions.append(" update " + MaterialTransaction.ENTITY_NAME + " as trx set trx." + MaterialTransaction.PROPERTY_ISPROCESSED + " = false ");
    hqlTransactions.append(" where trx." + MaterialTransaction.PROPERTY_ISPROCESSED + " = true");
    hqlTransactions.append("   and trx." + MaterialTransaction.PROPERTY_ISCOSTCALCULATED + " = false");
    hqlTransactions.append("   and trx." + MaterialTransaction.PROPERTY_COSTINGSTATUS + " <> 'S'");
    hqlTransactions.append("   and trx." + MaterialTransaction.PROPERTY_ORGANIZATION + ".id in (:orgs)");
    Query updateTransactions = OBDal.getInstance().getSession().createQuery(hqlTransactions.toString());
    updateTransactions.setParameterList("orgs", orgsWithRule);
    updateTransactions.executeUpdate();

    OBDal.getInstance().flush();
  }

  @SuppressWarnings("unchecked")
  private List<String> getTransactionsBatch(List<String> orgsWithRule, Date dateto) {
    StringBuffer where = new StringBuffer();
    where.append(" select trx." + MaterialTransaction.PROPERTY_ID + " as id ");
    where.append(" from " + MaterialTransaction.ENTITY_NAME + " as trx");
    where.append(" join trx." + MaterialTransaction.PROPERTY_PRODUCT + " as p");
    where.append(" left join trx." + MaterialTransaction.PROPERTY_GOODSSHIPMENTLINE + " as iol");
    where.append(" left join iol." + ShipmentInOutLine.PROPERTY_SHIPMENTRECEIPT + " as io");
    
    where.append(" left join trx." +  MaterialTransaction.PROPERTY_SSAORDERTYPE + " as cbo");
    
    where.append(" where trx." + MaterialTransaction.PROPERTY_ISPROCESSED + " = false");
    where.append("   and trx." + MaterialTransaction.PROPERTY_COSTINGSTATUS + " <> 'S'");
    
   // where.append(" and trx."+MaterialTransaction.PROPERTY_PRODUCT+".id = 'GG-417N1504'");
    
    // where.append("   and p." + Product.PROPERTY_PRODUCTTYPE + " = 'I'");
    // where.append("   and p." + Product.PROPERTY_STOCKED + " = true");
    where.append("   and trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE + " <= :dateto");
    where.append("   and trx." + MaterialTransaction.PROPERTY_ORGANIZATION + ".id in (:orgs)");
    
    
 //   where.append(" order by trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE + ", trx."+ MaterialTransaction.PROPERTY_CREATIONDATE ); //Deprecated
    where.append(" order by trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE);
    
    
    // where.append("   , trx." + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY+" desc ");
    // where.append("   , trx." + MaterialTransaction.PROPERTY_MOVEMENTLINE);
    // This makes M- to go before M+. In Oracle it must go with desc as if not, M+ would go before
    // M-. CORRECCION BY PALK, INVERTI EL ORDEN PARA QUE SIEMPRE INGRESE ANTES DE SALIDAS Y ASI
    // EVITAR NEGATIVOS

    where.append(" ,  CASE WHEN trx." + MaterialTransaction.PROPERTY_MOVEMENTQUANTITY + "<0 THEN 1 ELSE 0 END");
    where.append("   , io." + ShipmentInOut.PROPERTY_DOCUMENTSTATUS);
    
    where.append("   , CASE WHEN cbo is not null and cbo." + SCRComboItem.PROPERTY_SEARCHKEY + " in ('CompraNacional','CompraImportacion') THEN 0 ELSE 1 END "); //ADD BY VAFASTER
        
        
    if (OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("bbdd.rdbms").equalsIgnoreCase("oracle")) {
      where.append("   , trx." + MaterialTransaction.PROPERTY_MOVEMENTTYPE);
    } else {
      where.append("   , trx." + MaterialTransaction.PROPERTY_MOVEMENTTYPE + " desc ");
    }

    where.append("   , trx." + MaterialTransaction.PROPERTY_CREATIONDATE);
    
    //System.out.println("where.toString(): " + where.toString());

    Query trxQry = OBDal.getInstance().getSession().createQuery(where.toString());
    
    System.out.println("trxQry: " + trxQry);
    trxQry.setParameter("dateto", dateto);
    trxQry.setParameterList("orgs", orgsWithRule);
    trxQry.setMaxResults(BATCH_SIZE);
    return trxQry.list();
  }

  private int getTransactionsRowCount(List<String> orgsWithRule, Date dateto) {
    StringBuffer where = new StringBuffer();
    where.append(" select count(*)");
    where.append(" from " + MaterialTransaction.ENTITY_NAME + " as trx");
    where.append(" join trx." + MaterialTransaction.PROPERTY_PRODUCT + " as p");
    where.append(" left join trx." + MaterialTransaction.PROPERTY_GOODSSHIPMENTLINE + " as iol");
    where.append(" left join iol." + ShipmentInOutLine.PROPERTY_SHIPMENTRECEIPT + " as io");
    where.append(" where trx." + MaterialTransaction.PROPERTY_ISPROCESSED + " = false");
    where.append("   and trx." + MaterialTransaction.PROPERTY_COSTINGSTATUS + " <> 'S'");
    // where.append("   and p." + Product.PROPERTY_PRODUCTTYPE + " = 'I'");
    // where.append("   and p." + Product.PROPERTY_STOCKED + " = true");
    where.append("   and trx." + MaterialTransaction.PROPERTY_TRANSACTIONPROCESSDATE + " <= :dateto");
    where.append("   and trx." + MaterialTransaction.PROPERTY_ORGANIZATION + ".id in (:orgs)");

    final Query qry = OBDal.getInstance().getSession().createQuery(where.toString());
    qry.setParameter("dateto", dateto);
    qry.setParameterList("orgs", orgsWithRule);
    return ((Number) qry.uniqueResult()).intValue();

  }
}