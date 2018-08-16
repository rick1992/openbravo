package pe.com.unifiedgo.accounting;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import jxl.biff.drawing.ObjRecord;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.dal.service.OBDao;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.utility.OBDateUtils;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.businesspartner.Location;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.invoice.InvoiceTax;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.gl.GLItemAccounts;
import org.openbravo.model.financialmgmt.gl.GLJournal;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.model.financialmgmt.tax.TaxRate;
import org.openbravo.service.db.DalConnectionProvider;

import pe.com.unifiedgo.accounting.data.SCOBillofexchange;
import pe.com.unifiedgo.accounting.data.SCOBoeFrom;
import pe.com.unifiedgo.accounting.data.SCOInternalDoc;
import pe.com.unifiedgo.accounting.data.SCOPaymentHistory;
import pe.com.unifiedgo.accounting.data.SCOPercepPurch;
import pe.com.unifiedgo.accounting.data.SCOPercepSales;
import pe.com.unifiedgo.accounting.data.SCOPerceppurchDetail;
import pe.com.unifiedgo.accounting.data.SCOPercepsalesDetail;
import pe.com.unifiedgo.accounting.data.SCOPrepayment;
import pe.com.unifiedgo.accounting.data.SCOPwithhoRecLine;
import pe.com.unifiedgo.accounting.data.SCOPwithholdingReceipt;
import pe.com.unifiedgo.accounting.data.SCOSwithhoRecLine;
import pe.com.unifiedgo.accounting.data.SCOSwithholdingReceipt;
import pe.com.unifiedgo.core.data.SCRComboItem;

public class AccountingDoc {

  public static final BigDecimal _100 = new BigDecimal("100");
  
  public static AccountDocumentInfo getDocumentInformation(String strAdTableId, String recordId){
	  AccountDocumentInfo obj = new AccountDocumentInfo();
	  
	  if(!recordId.equalsIgnoreCase("") && recordId!=null){
		  obj.documentid = recordId;
		  obj.tableId = strAdTableId;
		  if(strAdTableId.equalsIgnoreCase("318")){/*C_Invoice*/
			  Invoice invoice = OBDal.getInstance().get(Invoice.class, recordId);
			  
			  obj.documentnumber = invoice.getScrPhysicalDocumentno();
			  obj.partnername = invoice.getBusinessPartner().getName();
			  obj.partnerdocumentnumber = invoice.getBusinessPartner().getTaxID();
			  obj.documenttypeid = invoice.getTransactionDocument().getId();
			  
		  }else if(strAdTableId.equalsIgnoreCase("224")){/*GL_Journal*/
			  GLJournal journal = OBDal.getInstance().get(GLJournal.class, recordId);
			  
			  obj.documentnumber = journal.getDocumentNo();
			  if(journal.getBusinessPartner()!=null){
				  obj.partnername = journal.getBusinessPartner().getName();
				  obj.partnerdocumentnumber = journal.getBusinessPartner().getTaxID();
			  }
			  obj.documenttypeid = journal.getDocumentType().getId();
			  
		  }else if(strAdTableId.equalsIgnoreCase("D1A97202E832470285C9B1EB026D54E2")){/*FIN_Payment*/
			  FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, recordId);
			  
			  obj.documentnumber = payment.getDocumentNo();
			  if(payment.getBusinessPartner()!=null){
				  obj.partnername = payment.getBusinessPartner().getName();
				  obj.partnerdocumentnumber = payment.getBusinessPartner().getTaxID();
			  }
			  obj.documenttypeid = payment.getDocumentType().getId();
			  
		  }else if(strAdTableId.equalsIgnoreCase("4D8C3B3C31D1410DA046140C9F024D17")){/*FIN_Finacc_Transaction*/
			  FIN_FinaccTransaction transaction = OBDal.getInstance().get(FIN_FinaccTransaction.class, recordId);
			  
			  obj.documentnumber = transaction.getDocumentNo();
			  if(transaction.getBusinessPartner()!=null){
				  obj.partnername = transaction.getBusinessPartner().getName();
				  obj.partnerdocumentnumber = transaction.getBusinessPartner().getTaxID();
			  }
			  obj.documenttypeid = transaction.getDocumentType().getId();
			  
		  }else if(strAdTableId.equalsIgnoreCase("6E25DC026D874BD58792E1C3994CEF16")){/*SCO_Pwithholding_Receipt*/
			  SCOPwithholdingReceipt pwreceipt = OBDal.getInstance().get(SCOPwithholdingReceipt.class, recordId);
			  
			  obj.documentnumber = pwreceipt.getDocumentNo();
			  obj.partnername = pwreceipt.getBusinessPartner().getName();
			  obj.partnerdocumentnumber = pwreceipt.getBusinessPartner().getTaxID();
			  obj.documenttypeid = pwreceipt.getTransactionDocument().getId();
			  
		  }else if(strAdTableId.equalsIgnoreCase("29987BE302E04781BE3868E573123BDE")){/*SCO_Billofexchange*/
			  SCOBillofexchange boechange = OBDal.getInstance().get(SCOBillofexchange.class, recordId);
			  
			  obj.documentnumber = boechange.getDocumentNo();
			  obj.partnername = boechange.getBusinessPartner().getName();
			  obj.partnerdocumentnumber = boechange.getBusinessPartner().getTaxID();
			  obj.documenttypeid = boechange.getTransactionDocument().getId();
			  
		  }else if(strAdTableId.equalsIgnoreCase("135FAE2571DB4028A90D5CAA6FAC154C")){/*SCO_Prepayment*/
			  SCOPrepayment prepayment = OBDal.getInstance().get(SCOPrepayment.class, recordId);
			  
			  obj.documentnumber = prepayment.getDocumentNo();
			  obj.partnername = prepayment.getBusinessPartner().getName();
			  obj.partnerdocumentnumber = prepayment.getBusinessPartner().getTaxID();
			  obj.documenttypeid = prepayment.getTransactionDocument().getId();
			  
		  }else if(strAdTableId.equalsIgnoreCase("A64BF5FB928C4EC1BACC023D6DC87F3C")){/*SCO_Internal_Doc*/
			  SCOInternalDoc interdoc = OBDal.getInstance().get(SCOInternalDoc.class, recordId);
			  
			  obj.documentnumber = interdoc.getDocumentNo();
			  if(interdoc.getBusinessPartner()!=null){
				  obj.partnername = interdoc.getBusinessPartner().getName();
				  obj.partnerdocumentnumber = interdoc.getBusinessPartner().getTaxID();
			  }
			  obj.documenttypeid = interdoc.getTransactionDocument().getId();
		  }
	  }
	  return obj;
  }
  
}




