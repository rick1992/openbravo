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
 * All portions are Copyright (C) 2001-2010 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.ad_forms;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.financialmgmt.assetmgmt.Amortization;
import org.openbravo.model.financialmgmt.assetmgmt.AmortizationLine;
import org.openbravo.model.financialmgmt.assetmgmt.Asset;

import pe.com.unifiedgo.accounting.data.SCOAssetCostcenter;

public class DocAmortization extends AcctServer {
  private static final long serialVersionUID = 1L;
  static Logger log4jDocAmortization = Logger.getLogger(DocAmortization.class);

  private String SeqNo = "0";
  /** Account Type - Asset */
  private static final String ACCTTYPE_Depreciation = "1";

  /**
   * @return the aCCTTYPE_Depreciation
   */
  public static String getACCTTYPE_Depreciation() {
    return ACCTTYPE_Depreciation;
  }

  /**
   * @return the aCCTTYPE_AccumDepreciation
   */
  public static String getACCTTYPE_AccumDepreciation() {
    return ACCTTYPE_AccumDepreciation;
  }

  public static final String ACCTTYPE_AccumDepreciation = "2";

  /**
   * Constructor
   * 
   * @param AD_Client_ID
   *          AD_Client_ID
   */
  public DocAmortization(String AD_Client_ID, String AD_Org_ID, ConnectionProvider connectionProvider) {
    super(AD_Client_ID, AD_Org_ID, connectionProvider);
  }

  /**
   * @return the log4jDocAmortization
   */
  public static Logger getLog4jDocAmortization() {
    return log4jDocAmortization;
  }

  /**
   * @param log4jDocAmortization
   *          the log4jDocAmortization to set
   */
  public static void setLog4jDocAmortization(Logger log4jDocAmortization) {
    DocAmortization.log4jDocAmortization = log4jDocAmortization;
  }

  /**
   * @return the seqNo
   */
  public String getSeqNo() {
    return SeqNo;
  }

  /**
   * @param seqNo
   *          the seqNo to set
   */
  public void setSeqNo(String seqNo) {
    SeqNo = seqNo;
  }

  /**
   * @return the serialVersionUID
   */
  public static long getSerialVersionUID() {
    return serialVersionUID;
  }

  public void loadObjectFieldProvider(ConnectionProvider conn, String AD_Client_ID, String Id) throws ServletException {
    setObjectFieldProvider(DocAmortizationData.selectRegistro(conn, AD_Client_ID, Id));
  }

  /**
   * Load Specific Document Details
   * 
   * @return true if loadDocumentType was set
   */
  public boolean loadDocumentDetails(FieldProvider[] data, ConnectionProvider conn) {
    DocumentType = AcctServer.DOCTYPE_Amortization;
    DateDoc = data[0].getField("Dateacct");
    loadDocumentType(); // lines require doc type
    // Contained Objects
    p_lines = loadLines(conn);
    log4jDocAmortization.debug("Record_ID = " + Record_ID + " - Lines=" + p_lines.length);
    return false;
  } // loadDocumentDetails

  /**
   * Load AmortizationLine Line.
   * 
   * @return DocLine Array
   */
  private DocLine[] loadLines(ConnectionProvider conn) {
    ArrayList<Object> list = new ArrayList<Object>();
    DocLineAmortizationData[] data = null;
    try {
      data = DocLineAmortizationData.select(conn, Record_ID);
    } catch (ServletException e) {
      log4jDocAmortization.warn(e);
    }
    if (p_lines != null) {
      log4jDocAmortization.debug("Record_ID = " + Record_ID + " - Lines=" + p_lines.length);
    } else {
      log4jDocAmortization.debug("Record_ID = " + Record_ID + " - Lines= **p_lines is null**");
    }
    
    for (int i = 0; data != null && i < data.length; i++) {
      String Line_ID = data[i].getField("A_AMORTIZATIONLINE_ID");
      DocLine_Amortization docLine = new DocLine_Amortization(DocumentType, Record_ID, Line_ID);
      docLine.loadAttributes(data[i], this);
      docLine.Amount = data[i].getField("AMORTIZATIONAMT");
      list.add(docLine);
    }

    // Return Array
    DocLine[] dl = new DocLine[list.size()];
    list.toArray(dl);
    return dl;
  } // loadLines

  /**
   * Get Source Currency Balance - always zero
   * 
   * @return Zero (always balanced)
   */
  public BigDecimal getBalance() {
    BigDecimal retValue = ZERO;

    return retValue;
  } // getBalance

  /**
   * Create Facts (the accounting logic) for
   * 
   * @param as
   *          accounting schema
   * @return Fact
   */
  public Fact createFact(AcctSchema as, ConnectionProvider conn, Connection con, VariablesSecureApp vars) throws ServletException {
    log4jDocAmortization.debug("createFact - Inicio");
    // Select specific definition
    String strClassname = AcctServerData.selectTemplateDoc(conn, as.m_C_AcctSchema_ID, DocumentType);
    if (strClassname.equals(""))
      strClassname = AcctServerData.selectTemplate(conn, as.m_C_AcctSchema_ID, AD_Table_ID);
    if (!strClassname.equals("")) {
      try {
        DocAmortizationTemplate newTemplate = (DocAmortizationTemplate) Class.forName(strClassname).newInstance();
        return newTemplate.createFact(this, as, conn, con, vars);
      } catch (Exception e) {
        log4j.error("Error while creating new instance for DocAmortizationTemplate - " + e);
      }
    }
    // create Fact Header
    Fact fact = null;
    String Fact_Acct_Group_ID = SequenceIdData.getUUID();
    log4jDocAmortization.debug("createFact - object created");
    log4jDocAmortization.debug("createFact - p_lines.length - " + p_lines.length);
    // Lines
    fact = new Fact(this, as, Fact.POST_Actual);

    Amortization amort = OBDal.getInstance().get(Amortization.class, this.Record_ID);
    if (amort != null) {
      List<AmortizationLine> amortlines = amort.getFinancialMgmtAmortizationLineList();
      Asset asset = null;
      for (int i = 0; i < amortlines.size(); i++) {
        asset = amortlines.get(i).getAsset();
        if (asset != null) {
        	
            /*List<SCOAssetCostcenter> costcenters = asset.getSCOAssetCostcenterList();
            for (int j = 0; j < costcenters.size(); j++) {
              fact.vCostCentersAux.add(costcenters.get(j).getCostCenter().getId());
              fact.vCostCentersPercentageAux.add(costcenters.get(j).getPercentage());
              fact.vCostCenterAsset.add(asset.getId());
            }*/
         }
      }
    }

    for (int i = 0; p_lines != null && i < p_lines.length; i++) {
      DocLine_Amortization line = (DocLine_Amortization) p_lines[i];
      
      Asset asset = OBDal.getInstance().get(Asset.class, line.m_A_Asset_ID);
      List<SCOAssetCostcenter> costcenters = asset.getSCOAssetCostcenterList();
      if(costcenters.size()==0){
    	  throw new ServletException("Necesita de costcenter para contabilizar");
      }

      BigDecimal totalAmt = new BigDecimal(line.Amount).setScale(2, RoundingMode.HALF_UP);
      BigDecimal totalPct = new BigDecimal(0).setScale(2, RoundingMode.HALF_UP);
      
      for(int j=0; j<costcenters.size(); j++){
    	  totalPct = totalPct.add(costcenters.get(j).getPercentage());
      }
      
      for(int j=0; j<costcenters.size(); j++){
	      BigDecimal amt = null;
    	  if(j==costcenters.size()-1){
    		  amt=totalAmt;
    	  }else{
    		  amt = new BigDecimal(line.Amount).setScale(2, RoundingMode.HALF_UP).multiply(costcenters.get(j).getPercentage()).divide(totalPct, 2, RoundingMode.HALF_UP);
    		  totalAmt = totalAmt.subtract(amt);
    	  }
    	  
    	  line.m_C_Costcenter_ID = costcenters.get(j).getCostCenter().getId(); 
	    	
	      isReceiptForConversion=false;
	      String purchaseDate = OBDateUtils.formatDate(asset.getPurchaseDate());
	      line.m_description= asset.getSearchKey()+" "+asset.getName();
	      FactLine fl = fact.createLine(line, getAccount(ACCTTYPE_AccumDepreciation, line.m_A_Asset_ID, as, conn), line.m_C_Currency_ID,  "", amt.toString(), Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, purchaseDate,conn);
	      fl.descripcion = asset.getSearchKey()+" "+asset.getName();
	      if(costcenters.get(j).getCostcenterAcc()==null){
	    	  fl=fact.createLine(line,getAccount(ACCTTYPE_Depreciation, line.m_A_Asset_ID, as, conn), line.m_C_Currency_ID, amt.toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, purchaseDate, conn);
	    	  fl.descripcion = asset.getSearchKey()+" "+asset.getName();
	      }else{
	    	  fl=fact.createLine(line, Account.getAccount(conn, costcenters.get(j).getCostcenterAcc().getId()), line.m_C_Currency_ID, amt.toString(), "", Fact_Acct_Group_ID, nextSeqNo(SeqNo), DocumentType, purchaseDate, conn);
	    	  fl.descripcion = asset.getSearchKey()+" "+asset.getName();
	      }
	      isReceiptForConversion=true;
      }
    }
    SeqNo = "0";
    return fact;
  } // createFact

  public String nextSeqNo(String oldSeqNo) {
    log4jDocAmortization.debug("DocAmortization - oldSeqNo = " + oldSeqNo);
    BigDecimal seqNo = new BigDecimal(oldSeqNo);
    SeqNo = (seqNo.add(new BigDecimal("10"))).toString();
    log4jDocAmortization.debug("DocAmortization - nextSeqNo = " + SeqNo);
    return SeqNo;
  }

  /**
   * Get Document Confirmation
   * 
   * not used
   */
  public boolean getDocumentConfirmation(ConnectionProvider conn, String strRecordId) {
    return true;
  }

  /**
   * Line Account from Asset
   * 
   * @param AcctType
   *          see ACCTTYPE_* (1..8)
   * @param as
   *          Accounting Schema
   * @return Requested Asset Account
   */
  public Account getAccount(String AcctType, String A_Asset_ID, AcctSchema as, ConnectionProvider conn) {
    if (Integer.parseInt(AcctType) < 1 || Integer.parseInt(AcctType) > 2)
      return null;
    // No Product - get Default from Product Category
    /*
     * if (A_Asset_ID.equals("")) return getAccountDefault(AcctType, as, conn);
     */
    DocAmortizationData[] data = null;
    Account acc = null;
    try {
      data = DocAmortizationData.selectAssetAcct(conn, A_Asset_ID, as.getC_AcctSchema_ID());
      if (data == null || data.length == 0)
        return null;
      String validCombination_ID = "";
      switch (Integer.parseInt(AcctType)) {
      case 1:
        validCombination_ID = data[0].depreciation;
        break;
      case 2:
        validCombination_ID = data[0].accumdepreciation;
        break;
      }
      if (validCombination_ID.equals(""))
        return null;
      acc = Account.getAccount(conn, validCombination_ID);
      log4jDocAmortization.debug("DocAmortization - getAccount - " + acc.Account_ID);
    } catch (ServletException e) {
      log4jDocAmortization.warn(e);
    }
    return acc;
  } // getAccount

  public String getServletInfo() {
    return "Servlet for the accounting";
  } // end of getServletInfo() method
}
