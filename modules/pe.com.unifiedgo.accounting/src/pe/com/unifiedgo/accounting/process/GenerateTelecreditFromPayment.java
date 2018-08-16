package pe.com.unifiedgo.accounting.process;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.businesspartner.BankAccount;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.gl.GLItem;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetailV;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

import pe.com.unifiedgo.accounting.SCO_Utils;
import pe.com.unifiedgo.accounting.data.SCOTelecredit;
import pe.com.unifiedgo.accounting.data.SCOTelecreditLine;

public class GenerateTelecreditFromPayment extends DalBaseProcess {
  public void doExecute(ProcessBundle bundle) throws Exception {
    try {

      /*
       * Set<String> params = bundle.getParams().keySet(); for (int i = 0; i < params.size(); i++) {
       * System.out.println(params.toArray()[i]); }
       */

      final String FIN_Payment_ID = (String) bundle.getParams().get("Fin_Payment_ID");
      final VariablesSecureApp vars = bundle.getContext().toVars();
      OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
      final ConnectionProvider conProvider = bundle.getConnection();
      final String language = bundle.getContext().getLanguage();
      User user = OBDal.getInstance().get(User.class, vars.getUser());

      FIN_Payment payment = OBDal.getInstance().get(FIN_Payment.class, FIN_Payment_ID);
      if (payment == null) {
        throw new OBException("@SCO_InternalError@");
      }

      String specialmethod = payment.getPaymentMethod().getScoSpecialmethod();
      if (specialmethod == null)
        specialmethod = "";

      if ((!payment.getStatus().equals("PPM") && !payment.getStatus().equals("PWNC") && !payment
          .getStatus().equals("RPAE")) || specialmethod.compareTo("SCOWIRETRANSFER") != 0) {
        throw new OBException("@ActionNotAllowedHere@");
      }

      SCOTelecredit telecredit = createTelecredit(payment, user);
      OBDal.getInstance().save(telecredit);
      OBDal.getInstance().flush();

      List<FIN_PaymentDetailV> pdvs = payment.getFINPaymentDetailVList();
      List<PaymentDetail> PDs = PaymentDetail.getPDfromFINPD(pdvs);

      // Fill the bankaccounts of the business partners that have been
      // selected for telecredit.
      for (int i = 0; i < PDs.size(); i++) {
        PaymentDetail PD = PDs.get(i);

        OBCriteria<BankAccount> obc = OBDal.getInstance().createCriteria(BankAccount.class);
        obc.add(Restrictions.eq(BankAccount.PROPERTY_BUSINESSPARTNER, PD.respbpartner));
        obc.add(Restrictions.eq(BankAccount.PROPERTY_SCOFORTELECREDIT, true));
        obc.add(Restrictions.eq(BankAccount.PROPERTY_SCOCCURRENCY, payment.getAccount()
            .getCurrency()));
        obc.setMaxResults(1);
        List<BankAccount> bankaccs = obc.list();

        if (bankaccs.size() > 0) {
          PD.bankacct = bankaccs.get(0);
        } else {
          throw new OBException("@SCO_GenTelecreditFPError_BankAcctNotFound@ "
              + PD.respbpartner.getIdentifier());
        }
      }

      long lineno = 10;
      for (int i = 0; i < PDs.size(); i++) {
        PaymentDetail PD = PDs.get(i);
        SCOTelecreditLine telecreditline = createTelecreditLine(telecredit, lineno, PD, user);
        lineno = lineno + 10;

        OBDal.getInstance().save(telecreditline);
      }
      OBDal.getInstance().flush();

      final OBError msg = new OBError();
      msg.setType("Success");
      msg.setTitle("@Success@");
      msg.setMessage("@SCO_GenTelecreditFPCreated@" + telecredit.getDocumentNo());

      bundle.setResult(msg);
      OBDal.getInstance().commitAndClose();
    } catch (final OBException e) {
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(e.getMessage());
      msg.setTitle("@Error@");
      OBDal.getInstance().rollbackAndClose();
      bundle.setResult(msg);
    }
  }

  private SCOTelecredit createTelecredit(FIN_Payment payment, User user) {
    SCOTelecredit telecredit = OBProvider.getInstance().get(SCOTelecredit.class);

    telecredit.setClient(payment.getClient());
    telecredit.setOrganization(payment.getOrganization());
    telecredit.setCreatedBy(user);
    telecredit.setCreationDate(new Date());
    telecredit.setUpdatedBy(user);
    telecredit.setUpdated(new Date());
    telecredit.setActive(true);

    telecredit.setSalesTransaction(false);
    telecredit.setDocumentNo(SCO_Utils.getDocumentNo(telecredit.getOrganization(),
        "SCOAPTELECREDIT", "SCO_Telecredit"));
    telecredit.setDocumentStatus("DR");
    telecredit.setDocumentAction("CO");
    telecredit.setProcessNow(false);
    telecredit.setProcessed(false);
    telecredit.setDocumentType(OBDal.getInstance().get(DocumentType.class, "0"));
    telecredit.setTransactionDocument(SCO_Utils.getDocTypeFromSpecial(telecredit.getOrganization(),
        "SCOAPTELECREDIT"));
    telecredit.setGeneratedDate(new Date());
    telecredit.setFinancialAccount(payment.getAccount());
    telecredit.setCurrency(payment.getAccount().getCurrency());
    telecredit.setProcesstelecredit("CO");
    telecredit.setPaymentref(payment.getDocumentNo());

    return telecredit;
  }

  private SCOTelecreditLine createTelecreditLine(SCOTelecredit telecredit, Long lineno,
      PaymentDetail PD, User user) {
    SCOTelecreditLine telecreditline = OBProvider.getInstance().get(SCOTelecreditLine.class);

    telecreditline.setClient(telecredit.getClient());
    telecreditline.setOrganization(telecredit.getOrganization());
    telecreditline.setCreatedBy(user);
    telecreditline.setCreationDate(new Date());
    telecreditline.setUpdatedBy(user);
    telecreditline.setUpdated(new Date());
    telecreditline.setActive(true);
    telecreditline.setTelecredit(telecredit);

    telecreditline.setLineNo(lineno);
    telecreditline.setRespbpartner(PD.respbpartner);
    telecreditline.setBusinessPartner(PD.bpartner);
    telecreditline.setAmount(PD.amount);
    telecreditline.setBpBankaccount(PD.bankacct);

    if (PD.type == 0) {
      telecreditline.setTipoDePago("SCO_AP");
      telecreditline.setDocTypeRef(PD.invoice.getTransactionDocument());
      telecreditline.setDocumentReference(PD.invoice);
    } else {
      telecreditline.setTipoDePago("SCO_OTHER");
      telecreditline.setGLItem(PD.glitem);
    }

    return telecreditline;
  }
}

class PaymentDetail {
  public FIN_PaymentDetail pd;
  public GLItem glitem;
  public Invoice invoice;
  public int type;

  // Info for telecredit pmt
  public BigDecimal amount;
  public BusinessPartner bpartner;
  public BusinessPartner respbpartner;
  public BankAccount bankacct;

  public PaymentDetail() {
    pd = null;
    glitem = null;
    invoice = null;
    type = -1;
    amount = new BigDecimal(0);
    bpartner = null;
    respbpartner = null;
    bankacct = null;
  }

  // in the case of type=1 the uniq is (glitem, respbpartner,businesspartner)
  public static int getPos(List<PaymentDetail> PDs, GLItem glitem, BusinessPartner bpartner,
      BusinessPartner respbpartner, Currency currency) {
    for (int i = 0; i < PDs.size(); i++) {

      PaymentDetail PD = PDs.get(i);
      if (PD.type != 1)
        continue;

      if (PD.glitem.getId().compareTo(glitem.getId()) == 0
          && PD.bpartner.getId().compareTo(bpartner.getId()) == 0
          && PD.respbpartner.getId().compareTo(respbpartner.getId()) == 0) {
        return i;
      }
    }
    return -1;
  }

  // in the case of type=0 the uniq is (invoice,respbpartner)
  public static int getPos(List<PaymentDetail> PDs, Invoice invoice, BusinessPartner respbpartner) {
    for (int i = 0; i < PDs.size(); i++) {
      PaymentDetail PD = PDs.get(i);
      if (PD.type != 0)
        continue;

      if (PD.invoice.getId().compareTo(invoice.getId()) == 0
          && PD.respbpartner.getId().compareTo(respbpartner.getId()) == 0) {
        return i;
      }
    }
    return -1;
  }

  public static List<PaymentDetail> getPDfromFINPD(List<FIN_PaymentDetailV> pdvs)
      throws OBException {

    List<PaymentDetail> PDs = new ArrayList<PaymentDetail>();

    for (int i = 0; i < pdvs.size(); i++) {
      FIN_PaymentDetailV pdv = pdvs.get(i);

      if (pdv.getScoPd().getGLItem() != null) {
        // Type 1
        GLItem glitem = pdv.getScoPd().getGLItem();
        BusinessPartner bpartner = null;
        if (pdv.getScoInvoiceGlref() != null) {
          bpartner = pdv.getScoInvoiceGlref().getBusinessPartner();
        } else if (pdv.getScoInternalDoc() != null) {
          bpartner = pdv.getScoInternalDoc().getBusinessPartner();
        } else {
          bpartner = pdv.getBusinessPartnerdim();
        }

        // if glitem has no related bpartner throw error
        if (bpartner == null) {
          throw new OBException("@SCO_GenTelecreditFPError_GLNoBP@" + glitem.getIdentifier());
        }
        // if there is not respbpartner the bpartner will receive the
        // money
        BusinessPartner respbpartner = pdv.getScoRespbpartner() != null ? pdv.getScoRespbpartner()
            : bpartner;
        Currency currency = pdv.getSCOMonedaDelDocumento();

        /*
         * int pos = PaymentDetail.getPos(PDs, glitem, bpartner, currency); if (pos == -1) { //
         * Create new PaymentDetail PaymentDetail PD = new PaymentDetail(); PD.pd = pd.getScoPd();
         * PD.glitem = glitem; PD.type = 1; PD.amount = pd.getSCOImporteDelCobro(); PD.bpartner =
         * bpartner; PDs.add(PD);
         * 
         * } else { PDs.get(pos).amount = PDs.get(pos).amount.add(pd.getSCOImporteDelCobro()); }
         */
        // ALWAYS CREATE A NEW PAYMENTDETAIL
        // Create new PaymentDetail
        PaymentDetail PD = new PaymentDetail();
        PD.glitem = glitem;
        PD.type = 1;
        PD.amount = pdv.getSCOImporteDelCobro();
        PD.bpartner = bpartner;
        PD.respbpartner = respbpartner;
        PDs.add(PD);

      } else {
        // Type 0
        FIN_PaymentSchedule ps = pdv.getPaymentPlanInvoice();
        if (ps == null) {
          // order psd cannot exists
          throw new OBException("@SCO_GenTelecreditFPError_Ord@" + pdv.getIdentifier());
        }

        Invoice inv = ps.getInvoice();
        // if there is not respbpartner the bpartner will receive the
        // money
        BusinessPartner respbpartner = pdv.getScoRespbpartner() != null ? pdv.getScoRespbpartner()
            : inv.getBusinessPartner();
        int pos = PaymentDetail.getPos(PDs, inv, respbpartner);
        if (pos == -1) {
          // Create new PaymentDetail
          PaymentDetail PD = new PaymentDetail();
          PD.invoice = inv;
          PD.type = 0;
          PD.amount = pdv.getSCOImporteDelCobro();
          PD.bpartner = inv.getBusinessPartner();
          PD.respbpartner = respbpartner;
          PDs.add(PD);
        } else {
          PDs.get(pos).amount = PDs.get(pos).amount.add(pdv.getSCOImporteDelCobro());
        }
      }

    }

    return PDs;
  }
}