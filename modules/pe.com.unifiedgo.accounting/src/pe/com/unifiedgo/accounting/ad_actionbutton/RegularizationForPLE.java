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
 * All portions are Copyright (C) 2010-2013 Openbravo SLU
 * All Rights Reserved.
 * Contributor(s):  ______________________________________.
 *************************************************************************
 */
package pe.com.unifiedgo.accounting.ad_actionbutton;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.dao.AdvPaymentMngtDao;
import org.openbravo.advpaymentmngt.utility.FIN_Utility;
import org.openbravo.base.filter.RequestFilter;
import org.openbravo.base.filter.ValueListFilter;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.utility.ComboTableData;
import org.openbravo.erpCommon.utility.DimensionDisplayUtility;
import org.openbravo.erpCommon.utility.FieldProviderFactory;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.accounting.AccountingFact;
import org.openbravo.model.financialmgmt.calendar.Period;
import org.openbravo.xmlEngine.XmlDocument;

import pe.com.unifiedgo.accounting.AccountDocumentInfo;
import pe.com.unifiedgo.accounting.AccountingDoc;
import pe.com.unifiedgo.accounting.data.SCOPle5_6_Reg;
import pe.com.unifiedgo.accounting.data.SCOPle8_14_Reg;
import pe.com.unifiedgo.core.data.SCRComboItem;
import pe.com.unifiedgo.requirement.ad_reports.SREDateTimeData;

public class RegularizationForPLE extends HttpSecureAppServlet {
	private static final long serialVersionUID = 1L;
	private AdvPaymentMngtDao dao;
	private static final RequestFilter filterYesNo = new ValueListFilter("Y",
			"N", "");

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		VariablesSecureApp vars = new VariablesSecureApp(request);
		dao = new AdvPaymentMngtDao();

		if (vars.commandIn("DEFAULT")) {
			String strRegnumber = vars.getGlobalVariable("inpregnumber", "RegularizationForPLE|Regnumber");
			String strCorrelNumber = vars.getGlobalVariable("inpcorrelnumber", "RegularizationForPLE|Correlnumber");
			String strAdTableId = vars.getGlobalVariable("inpadTableId", "RegularizationForPLE|Table_ID");
			String strRecord1 = vars.getGlobalVariable("inprecord1", "RegularizationForPLE|Record1");
			String strViewId = vars.getGlobalVariable("inpscoDocForRegpleVId", "RegularizationForPLE|SCODocForRegpleVID");
			String strAdOrgId = vars.getGlobalVariable("inpadOrgId", "RegularizationForPLE|AD_Org_ID");
			
			String strTabId = vars.getGlobalVariable("inpTabId", "RegularizationForPLE|Tab_ID");

			try {
				printPage(response, vars, strRegnumber, strCorrelNumber, strAdTableId, strRecord1, strViewId, "", "", "", "", true, strTabId);
			} catch (Exception e) {
				bdErrorGeneralPopUp(request, response, "Error", e.getMessage());
			}

		} else if (vars.commandIn("GRIDLIST")) {
			String strRegnumber = vars.getGlobalVariable("inpregnumber", "RegularizationForPLE|Regnumber");
			String strCorrelNumber = vars.getGlobalVariable("inpcorrelnumber", "RegularizationForPLE|Correlnumber");
			String strAdTableId = vars.getGlobalVariable("inpadTableId", "RegularizationForPLE|Table_ID");
			String strRecord1 = vars.getGlobalVariable("inprecord1", "RegularizationForPLE|Record1");
			
			printGrid(response, vars, strRegnumber, strCorrelNumber, strAdTableId, strRecord1, "", "", "", "", "", "", "",
					false, false, "", false, "", "", "");

		} else if (vars.commandIn("BPARTNERBLOCK")) {
			String strRegnumber = vars.getGlobalVariable("inpregnumber", "RegularizationForPLE|Regnumber");
			String strCorrelNumber = vars.getGlobalVariable("inpcorrelnumber", "RegularizationForPLE|Correlnumber");
			String strAdTableId = vars.getGlobalVariable("inpadTableId", "RegularizationForPLE|Table_ID");
			String strRecord1 = vars.getGlobalVariable("inprecord1", "RegularizationForPLE|Record1");
			String strViewId = vars.getGlobalVariable("inpscoDocForRegpleVId", "RegularizationForPLE|SCODocForRegpleVID");
			
			String strAdOrgId = vars.getGlobalVariable("inpadOrgId", "RegularizationForPLE|AD_Org_ID");
			String strPeriod = vars.getStringParameter("inpPeriodo");
			String strSunatStatus = vars.getStringParameter("inpStatusOp");
			String strRegnumberForReg = vars.getStringParameter("inpTxtRegNumber");
			String strCorrelNumberForReg = vars.getStringParameter("inpTxtCorrelNumber");
			
			String strTabId = vars.getRequiredStringParameter("inpTabId");
			
			org.openbravo.model.common.enterprise.Organization org;
			Period period;
			SCRComboItem item;
			SCOPle8_14_Reg factacct814;
			SCOPle8_14_Reg factacct814_temp;
			SCOPle5_6_Reg factAcctForReg;
			
			AccountingFact factAcct;
			Invoice invoice;
			RegularizationForPLEData[] dataids = null;
			
			StringBuilder mensajeFinal = new StringBuilder();
			
			try{
				org = OBDal.getInstance().get(org.openbravo.model.common.enterprise.Organization.class, strAdOrgId);
				period = OBDal.getInstance().get(Period.class, strPeriod);
				item = OBDal.getInstance().get(SCRComboItem.class, strSunatStatus);
				
				dataids = RegularizationForPLEData.selectfactacctids(this, strRecord1, strAdTableId);
				
				/* Si es un invoice, regularizar 8-14 */
				if(strAdTableId.equalsIgnoreCase("318")){
					invoice =OBDal.getInstance().get(Invoice.class, strRecord1);
					
					/* verificar si existe una regularización 8-14 */
					OBCriteria<SCOPle8_14_Reg> regularizaciones = OBDal.getInstance().createCriteria(SCOPle8_14_Reg.class);
					regularizaciones.add(Restrictions.eq(SCOPle8_14_Reg.PROPERTY_INVOICE + ".id", strRecord1))
					.add(Restrictions.eq(SCOPle8_14_Reg.PROPERTY_PERIOD + ".id", strPeriod));
					
					if(regularizaciones.list().size()>0){/* hay una regularizacion, completar la primera */
						factacct814_temp = OBDal.getInstance().get(SCOPle8_14_Reg.class,regularizaciones.list().get(0).getId());
						
						/* completar */
						factacct814_temp.setNewOBObject(true);
						
						factacct814_temp.setProcessed(true);
						factacct814_temp.setDocumentStatus("CO");
						factacct814_temp.setDocumentAction("RE");
						
						OBDal.getInstance().save(factacct814_temp);
						OBDal.getInstance().flush();
						
						mensajeFinal.append("Regularización 8-14 completada para el documento " + factacct814_temp.getInvoice().getScrPhysicalDocumentno() + 
								" en el periodo " + factacct814_temp.getPeriod().getName());
						
					}else{/* No hay regularización, crear una nueva */
						factacct814 = new SCOPle8_14_Reg();
						
						factacct814.setNewOBObject(true);
						factacct814.setOrganization(org);
						factacct814.setActive(true);
						factacct814.setDocumentAction("CO");
						factacct814.setDocumentStatus("DR");
						factacct814.setProcessed(false);
						factacct814.setProcessNow(false);
						factacct814.setPeriod(period);
						factacct814.setInvoice(invoice);
						factacct814.setSCORegnumber(strRegnumberForReg);
						factacct814.setSCOSeqno(Long.parseLong(strCorrelNumberForReg));
						factacct814.setSunatopstatusCmbItem(item);
						
						OBDal.getInstance().save(factacct814);
						OBDal.getInstance().flush();
						
						factacct814.setProcessed(true);
						factacct814.setDocumentStatus("CO");
						factacct814.setDocumentAction("RE");
						
						OBDal.getInstance().save(factacct814);
						OBDal.getInstance().flush();
						
						mensajeFinal.append("Regularización 8-14 creada para el documento " + invoice.getScrPhysicalDocumentno() + 
								" en el periodo " + period.getName() + ". ");
					}
				}
				
				
				
				
				
				
				
				
				/* regularizacion 5-6 */

				for (int i = 0; i < dataids.length; i++) {
					factAcctForReg = new SCOPle5_6_Reg();
					
					String factacctid = dataids[i].factacctid;
					/* recuperar los fact_acct */
					factAcct = OBDal.getInstance().get(AccountingFact.class,factacctid);
					/* insertarlos en la nueva tabla */
					
					factAcctForReg.setNewOBObject(true);

					factAcctForReg.setId(factAcct.getId());
					factAcctForReg.setClient(factAcct.getClient());
					factAcctForReg.setOrganization(factAcct.getOrganization());
					factAcctForReg.setActive(true);
					factAcctForReg.setCreationDate(factAcct.getCreationDate());
					factAcctForReg.setCreatedBy(factAcct.getCreatedBy());
					factAcctForReg.setUpdated(factAcct.getUpdated());
					factAcctForReg.setUpdatedBy(factAcct.getUpdatedBy());
					factAcctForReg.setAccountingSchema(factAcct.getAccountingSchema());
					factAcctForReg.setAccount(factAcct.getAccount());
					factAcctForReg.setTransactionDate(factAcct.getTransactionDate());
					factAcctForReg.setAccountingDate(factAcct.getAccountingDate());
					factAcctForReg.setPeriod(factAcct.getPeriod());
					factAcctForReg.setTable(factAcct.getTable());
					factAcctForReg.setRecordID(factAcct.getRecordID());
					factAcctForReg.setLineID(factAcct.getLineID());
					factAcctForReg.setGLCategory(factAcct.getGLCategory());
					factAcctForReg.setTax(factAcct.getTax());
					factAcctForReg.setStorageBin(factAcct.getStorageBin());
					factAcctForReg.setPostingType(factAcct.getPostingType());
					factAcctForReg.setCurrency(factAcct.getCurrency());
					factAcctForReg.setForeignCurrencyDebit(factAcct.getForeignCurrencyDebit());
					factAcctForReg.setForeignCurrencyCredit(factAcct.getForeignCurrencyCredit());
					factAcctForReg.setDebit(factAcct.getDebit());
					factAcctForReg.setCredit(factAcct.getCredit());
					factAcctForReg.setUOM(factAcct.getUOM());
					factAcctForReg.setQuantity(factAcct.getQuantity());
					factAcctForReg.setProduct(factAcct.getProduct());
					factAcctForReg.setBusinessPartner(factAcct.getBusinessPartner());
					factAcctForReg.setTrxOrganization(factAcct.getTrxOrganization());
					factAcctForReg.setLocationFromAddress(factAcct.getLocationFromAddress());
					factAcctForReg.setLocationToAddress(factAcct.getLocationToAddress());
					factAcctForReg.setSalesRegion(factAcct.getSalesRegion());
					factAcctForReg.setProject(factAcct.getProject());
					factAcctForReg.setSalesCampaign(factAcct.getSalesCampaign());
					factAcctForReg.setActivity(factAcct.getActivity());
					factAcctForReg.setStDimension(factAcct.getStDimension());
					factAcctForReg.setNdDimension(factAcct.getNdDimension());
					factAcctForReg.setDescription(factAcct.getDescription());
					factAcctForReg.setAsset(factAcct.getAsset());
					factAcctForReg.setGroupID(factAcct.getGroupID());
					factAcctForReg.setSequenceNumber(factAcct.getSequenceNumber());
					factAcctForReg.setType(factAcct.getType());
					factAcctForReg.setDocumentCategory(factAcct.getDocumentCategory());
					factAcctForReg.setValue(factAcct.getValue());
					factAcctForReg.setAccountingEntryDescription(factAcct.getAccountingEntryDescription());
					factAcctForReg.setRecordID2(factAcctForReg.getRecordID2());
					factAcctForReg.setWithholding(factAcct.getWithholding());
					factAcctForReg.setDocumentType(factAcct.getDocumentType());
					factAcctForReg.setCostcenter(factAcct.getCostcenter());
					factAcctForReg.setModify(true);
					factAcctForReg.setNmeroDeRegistro(factAcct.getScoRegnumber());
					factAcctForReg.setSCOIsauto(true);
					factAcctForReg.setSCOTipdoc(factAcct.getSCODocType());
					factAcctForReg.setSCORecord3(factAcct.getScoRecord3());
					factAcctForReg.setSCOFechavenc(factAcct.getScoFechavenc());
					factAcctForReg.setSCOReplaceanalyticsProc(false);
					factAcctForReg.setSCORecord3Table(factAcct.getScoRecord3Table());
					factAcctForReg.setRubroCambioPatrimonio(factAcct.getScoEeffPatrimchange());
					factAcctForReg.setRubroFlujoDeEfectivo(factAcct.getScoEeffCashflow());
					factAcctForReg.setCorrelativoDelAsiento(factAcct.getScoSeqno());
					factAcctForReg.setSCOIsmigrated(false);
					factAcctForReg.setSCOReevalCurrency(factAcct.getScoReevalCurrency());
					factAcctForReg.setSCOCorrelnum(factAcct.getScoCorrelnum());
					factAcctForReg.setOrganizationArea(factAcct.getScoArea());
					factAcctForReg.setSCOMgrInternalDoc(factAcct.getScoMgrInternalDoc());
					
					factAcctForReg.setSCORelFactacct(strViewId);
					factAcctForReg.setSCOReg56Regnumber(strRegnumberForReg);
					factAcctForReg.setSCOReg56Seqno(Long.parseLong(strCorrelNumberForReg));
					factAcctForReg.setDocumentStatus("DR");
					factAcctForReg.setDocumentAction("CO");
					factAcctForReg.setReg56Period(period);
					
					OBDal.getInstance().save(factAcctForReg);
				}
				
				mensajeFinal.append("Se crearón líneas para regularizar el asiento " + strRegnumber + ". ");
				
				final OBError createmsg = new OBError();
				createmsg.setType("Success");
				createmsg.setTitle("Proceso terminado");
				createmsg.setMessage(mensajeFinal.toString());
				
				vars.setMessage(strTabId, createmsg);
		        printPageClosePopUpAndRefreshParent(response, vars);
				
			}catch(Exception ex){
				final OBError errormsg = new OBError();
				errormsg.setType("Error");
				errormsg.setTitle("Error");
				errormsg.setMessage(ex.getMessage());
				
				OBDal.getInstance().rollbackAndClose();
				
				vars.setMessage(strTabId, errormsg);
				printPageClosePopUp(response, vars);
				
			}

			printPageClosePopUpAndRefreshParent(response, vars);

		} else if (vars.commandIn("SAVE") || vars.commandIn("SAVEANDPROCESS")) {
			printPageClosePopUpAndRefreshParent(response, vars);
		}
	}

	private void printPage(HttpServletResponse response, VariablesSecureApp vars, String strRegnumber, 
			String strCorrelNumber, String strAdTableId, String strRecord1, String strViewId,
			String strWindowId, String strTabId2, String strFinancialAccountId,
			String strSsaPaymentInDocType, boolean isFirstLoad, String strTabId)
			throws Exception {
		log4j.debug("Output: Add Payment button pressed on Make / Receipt Payment windows");

		OBContext.setAdminMode(true);

		try {

			String[] discard = {};

			XmlDocument xmlDocument = xmlEngine.readXmlTemplate("pe/com/unifiedgo/accounting/ad_actionbutton/RegularizationForPLE",discard).createXmlDocument();
			
			xmlDocument.setParameter("tableId", strAdTableId);
			xmlDocument.setParameter("StrRegnumber", strRegnumber);
			xmlDocument.setParameter("StrCorrelnumber", strCorrelNumber);
			xmlDocument.setParameter("regNumber", strRegnumber);
			xmlDocument.setParameter("correlNumber", strCorrelNumber);

			xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
			xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
			xmlDocument.setParameter("theme", vars.getTheme());
			xmlDocument.setParameter("paymentInDocType", "");
			xmlDocument.setParameter("title", Utility.messageBD(this, "SCO_RegulationPLE", vars.getLanguage()));
			xmlDocument.setParameter("expectedDateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
			xmlDocument.setParameter("expectedDateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));

			xmlDocument.setParameter("businessPartner", "");
			xmlDocument.setParameter("businessPartnerId", "");
			xmlDocument.setParameter("credit", "");
			xmlDocument.setParameter("customerBalance", "");
			xmlDocument.setParameter("expectedDateFrom", SREDateTimeData.nDaysBefore(this, SREDateTimeData.today(this), "5"));
			xmlDocument.setParameter("expectedDateFromdisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
			xmlDocument.setParameter("expectedDateFromsaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
			xmlDocument.setParameter("expectedDateTo", SREDateTimeData.today(this));
			xmlDocument.setParameter("expectedDateTodisplayFormat", vars.getSessionValue("#AD_SqlDateFormat"));
			xmlDocument.setParameter("expectedDateTosaveFormat", vars.getSessionValue("#AD_SqlDateFormat"));
			
			xmlDocument.setParameter("windowId", strWindowId);
			xmlDocument.setParameter("tabId", strTabId);
			xmlDocument.setParameter("strTabId", "strTabId =  \"" + strTabId + "\";\r\n");
			xmlDocument.setParameter("orgId", "");
			xmlDocument.setParameter("adOrgId", vars.getSessionValue("#AD_ORG_ID"));
			xmlDocument.setParameter("actualPayment", "0.0");
			xmlDocument.setParameter("headerAmount", "0.0");
			xmlDocument.setParameter("isSimple", "N");
			xmlDocument.setParameter("isApp", "N");
			xmlDocument.setParameter("APApplicationType", "");
			xmlDocument.setParameter("ARApplicationType", "");
			xmlDocument.setParameter("retencion", "N");
			xmlDocument.setParameter("generatedCredit", BigDecimal.ZERO.toString());
			xmlDocument.setParameter("exchangeRate", "");
			xmlDocument.setParameter("altExchangeRate", "");
			xmlDocument.setParameter("actualConverted", "");
			xmlDocument.setParameter("expectedConverted", "");
			xmlDocument.setParameter("currencyId", "308");
			xmlDocument.setParameter("currencyName", "PEN");

			boolean forcedFinancialAccountTransaction = false;
			forcedFinancialAccountTransaction = false;
			// Action Regarding Document
			xmlDocument.setParameter("ActionDocument", "PPP");
			try {
				ComboTableData comboTableData = new ComboTableData(
						vars, this, "LIST", "", "F903F726B41A49D3860243101CEEBA25",
						forcedFinancialAccountTransaction ? "29010995FD39439D97A5C0CE8CE27D70"
								: "", Utility.getContext(this, vars, "#AccessibleOrgTree", "AddPaymentFromInvoice"),
						Utility.getContext(this, vars, "#User_Client", "AddPaymentFromInvoice"), 0);
				Utility.fillSQLParameters(this, vars, null, comboTableData, "RegularizationForPLE", "");
				xmlDocument.setData("reportActionDocument", "liststructure", comboTableData.select(false));
				comboTableData = null;
			} catch (Exception ex) {
				throw new ServletException(ex);
			}
			String doctype;
			doctype = AcctServer.DOCTYPE_APPayment;

			final String strCentrally = Utility.getContext(this, vars, DimensionDisplayUtility.IsAcctDimCentrally, strWindowId);
			final String strElement_BP = "Y";
			Utility.getContext(this, vars, DimensionDisplayUtility.displayAcctDimensions(strCentrally, DimensionDisplayUtility.DIM_BPartner, doctype,
							DimensionDisplayUtility.DIM_Header), strWindowId);
			final String strElement_PR = Utility.getContext(this, vars, DimensionDisplayUtility.displayAcctDimensions(strCentrally,
							DimensionDisplayUtility.DIM_Product, doctype, DimensionDisplayUtility.DIM_Header), strWindowId);
			String strElement_PJ = Utility.getContext(this, vars, DimensionDisplayUtility.displayAcctDimensions(strCentrally,
							DimensionDisplayUtility.DIM_Project, doctype, DimensionDisplayUtility.DIM_Header), strWindowId);
			final String strElement_AY = Utility.getContext(this, vars, "$Element_AY", strWindowId);
			String strElement_CC = Utility.getContext(this, vars, DimensionDisplayUtility.displayAcctDimensions(strCentrally,
							DimensionDisplayUtility.DIM_CostCenter, doctype, DimensionDisplayUtility.DIM_Header), strWindowId);

			final String strElement_ID = "Y"; // always show the internal
												// document dimension
			final String strElement_IG = "Y"; // always show the purchase/sales
												// document dimension
			final String strElement_CF = "Y"; // always show the cashflow
												// dimension
			strElement_PJ = "N"; // never show Project dimension
			strElement_CC = "Y"; // always show the costcenter dimension

			final String strElement_MC = Utility.getContext(this, vars, "$Element_MC", strWindowId);
			final String strElement_U1 = Utility.getContext(this, vars, DimensionDisplayUtility.displayAcctDimensions(strCentrally,
							DimensionDisplayUtility.DIM_User1, doctype, DimensionDisplayUtility.DIM_Header), strWindowId);
			final String strElement_U2 = Utility.getContext(this, vars, DimensionDisplayUtility.displayAcctDimensions(strCentrally,
							DimensionDisplayUtility.DIM_User2, doctype, DimensionDisplayUtility.DIM_Header), strWindowId);

			final String strNotAllowExchange = Utility.getContext(this, vars, "NotAllowChangeExchange", strWindowId);
			xmlDocument.setParameter("strNotAllowExchange", strNotAllowExchange);
			dao = new AdvPaymentMngtDao();

			if (isFirstLoad) {
				xmlDocument.setParameter("FirstLoad", "FirstLoad = \"YES\"; ");
			} else {
				xmlDocument.setParameter("FirstLoad", "FirstLoad = \"NO\"; ");
			}

			RegularizationForPLEData[] periodos = RegularizationForPLEData.select_periodos(this);

			xmlDocument.setParameter("paramPeriodosArray", Utility.arrayInfinitasEntradas("idperiodo;periodo;fechainicial;fechafinal;idorganizacion", "arrPeriodos", periodos));

			response.setContentType("text/html; charset=UTF-8");

			PrintWriter out = response.getWriter();
			out.println(xmlDocument.print());
			out.close();
		} finally {
			OBContext.restorePreviousMode();
		}
	}

	private void printGrid(HttpServletResponse response, VariablesSecureApp vars, String strRegnumber,
			String strCorrelNumber, String strAdTableId, String strRecord1, String strBusinessPartnerId, String strPaymentId, String strOrgId,
			String strExpectedDateFrom, String strExpectedDateTo, String strDocumentType, String strSelectedPaymentDetails,
			boolean isReceipt, boolean showAlternativePM, String strDocNo,
			boolean showAllFinancialAcounts, String strTabId, String strCContable, String strOperationType) throws IOException,
			ServletException {

		log4j.debug("Output: Grid with pending payments");
		dao = new AdvPaymentMngtDao();
		String[] discard = {};

		ArrayList<RegularizationForPLEData> datagridlist = new ArrayList<RegularizationForPLEData>();

		RegularizationForPLEData[] docs = RegularizationForPLEData.selectids(this, strRegnumber, strCorrelNumber);

		AccountDocumentInfo obj;

		for (int i = 0; i < docs.length; i++) {
			RegularizationForPLEData temp = new RegularizationForPLEData();

			obj = AccountingDoc.getDocumentInformation(docs[i].tableid, docs[i].recordid);
			temp.documentnum = obj.documentnumber;
			temp.bpartnername = obj.partnerdocumentnumber + " - " + obj.partnername;
			temp.documenttype = RegularizationForPLEData.selectDocWithTranslate(this, obj.documenttypeid);
			temp.docreltype = docs[i].docreltype;

			datagridlist.add(temp);
		}

		RegularizationForPLEData[] dataGrid = new RegularizationForPLEData[datagridlist.size()];
		datagridlist.toArray(dataGrid);

		XmlDocument xmlDocument = xmlEngine.readXmlTemplate("pe/com/unifiedgo/accounting/ad_actionbutton/ShowDocumentGrid", discard).createXmlDocument();

		xmlDocument.setData("structure", dataGrid);

		response.setContentType("text/html; charset=UTF-8");
		PrintWriter out = response.getWriter();
		out.println(xmlDocument.print());
		out.close();
	}

	

	public static String getListInvoiceRef(String strOrgId, String partnerId,
			boolean isMandatory, boolean isReceipt) {

		OBCriteria<Invoice> psdFilter = OBDal.getInstance().createCriteria(
				Invoice.class);
		psdFilter.add(Restrictions.eq(Invoice.PROPERTY_BUSINESSPARTNER, OBDal.getInstance().get(BusinessPartner.class, partnerId)));

		if (isReceipt)
			psdFilter.add(Restrictions.eq(Invoice.PROPERTY_SALESTRANSACTION, true));
		else
			psdFilter.add(Restrictions.eq(Invoice.PROPERTY_SALESTRANSACTION, false));

		List<Invoice> invoices = psdFilter.list();

		String options = FIN_Utility.getOptionsList(invoices, "", isMandatory);
		return options;
	}

	public String getServletInfo() {
		return "Servlet that presents the payment proposal";
		// end of getServletInfo() method
	}

}
