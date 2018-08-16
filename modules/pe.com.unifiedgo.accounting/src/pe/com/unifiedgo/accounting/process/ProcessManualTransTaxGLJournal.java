package pe.com.unifiedgo.accounting.process;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.ad_forms.AcctServer;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceTax;
import org.openbravo.model.common.uom.UOM;
import org.openbravo.model.financialmgmt.accounting.coa.AccountingCombination;
import org.openbravo.model.financialmgmt.accounting.coa.AcctSchema;
import org.openbravo.model.financialmgmt.calendar.Period;
import org.openbravo.model.financialmgmt.gl.GLCategory;
import org.openbravo.model.financialmgmt.gl.GLJournal;
import org.openbravo.model.financialmgmt.gl.GLJournalLine;
import org.openbravo.model.financialmgmt.tax.TaxRateAccounts;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

import pe.com.unifiedgo.accounting.SCO_Utils;

public class ProcessManualTransTaxGLJournal extends DalBaseProcess {

  public void doExecute(ProcessBundle bundle) throws Exception {
    try {

      /*
       * Set<String> params = bundle.getParams().keySet(); for (int i = 0; i < params.size(); i++) {
       * System.out.println(params.toArray()[i]); }
       */

      String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("dateFormat.java");
      SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);

      final String C_Invoice_ID = (String) bundle.getParams().get("C_Invoice_ID");
      final String strDateacct = (String) bundle.getParams().get("dateacct");
      final Date dateacct = sdf.parse(strDateacct);

      final VariablesSecureApp vars = bundle.getContext().toVars();
      OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
      final ConnectionProvider conProvider = bundle.getConnection();
      final String language = bundle.getContext().getLanguage();
      User user = OBDal.getInstance().get(User.class, vars.getUser());

      Invoice invoice = OBDal.getInstance().get(Invoice.class, C_Invoice_ID);
      if (invoice == null) {
        throw new OBException("@SCO_InternalError@");
      }

      if (invoice.getDocumentStatus().compareTo("CO") != 0) {
        throw new OBException("@ActionNotAllowedHere@");
      }

      DocumentType gljournal_doctype = SCO_Utils.getDocTypeFromSpecial(invoice.getOrganization(),
          "SCOGLJOURNAL");
      if (gljournal_doctype == null) {
        throw new OBException("@SCO_ProcessMTrTaxJournalNoDoctype@");
      }

      GLCategory glcategory = null;
      OBCriteria<GLCategory> obc = OBDal.getInstance().createCriteria(GLCategory.class);
      obc.add(Restrictions.eq(GLCategory.PROPERTY_CATEGORYTYPE, "M"));
      obc.add(Restrictions.eq(GLCategory.PROPERTY_DEFAULT, true));

      List<GLCategory> glcats = obc.list();
      if (glcats.size() == 0) {
        throw new OBException("@SCO_ProcessMTrTaxJournalNoGLCategory@");
      }
      glcategory = glcats.get(0);
      if (glcategory == null) {
        throw new OBException("@SCO_ProcessMTrTaxJournalNoGLCategory@");
      }

      UOM unit_uom = OBDal.getInstance().get(UOM.class, "100");
      if (unit_uom == null) {
        throw new OBException("@SCO_ProcessMTrTaxJournalNoUOM@");
      }

      OBCriteria<AcctSchema> oc = OBDal.getInstance().createCriteria(AcctSchema.class);
      oc.add(Restrictions.eq(AcctSchema.PROPERTY_CLIENT, invoice.getClient()));
      oc.add(Restrictions.sqlRestriction(
          "EXISTS (SELECT 1 FROM AD_ORG_ACCTSCHEMA orgacct WHERE orgacct.C_ACCTSCHEMA_ID = this_.C_ACCTSCHEMA_ID AND AD_ISORGINCLUDED('"
              + invoice.getOrganization().getId()
              + "', this_.AD_ORG_ID, this_.AD_CLIENT_ID)<>-1)"));
      List<AcctSchema> acctschemas = oc.list();
      if (acctschemas.size() == 0) {
        throw new OBException("@SCO_ProcessMTrTaxJournalNoAcctSchema@");
      }

      AcctSchema acctschema = acctschemas.get(0);

      BigDecimal tc = new BigDecimal(1);
      if (!invoice.getCurrency().getId().equals(acctschema.getCurrency().getId())) {
        String strConvertedAmt = AcctServer.getConvertedAmtGeneral("1000000",
            invoice.getCurrency().getId(), acctschema.getCurrency().getId(),
            sdf.format(invoice.getAccountingDate()), "S", invoice.getClient().getId(),
            invoice.getOrganization().getId(), conProvider, !invoice.isSalesTransaction(), false);
        if (strConvertedAmt == null) {
          throw new OBException("@SCO_ProcessMTrTaxJournalNoConvertAmt@");
        }
        if (strConvertedAmt.isEmpty()) {
          throw new OBException("@SCO_ProcessMTrTaxJournalNoConvertAmt@");
        }
        BigDecimal convertedTaxAmount = new BigDecimal(strConvertedAmt);
        tc = convertedTaxAmount.divide(new BigDecimal("1000000"), 16, RoundingMode.HALF_UP);
      }

      String description = "ASIENTO IMPUESTO DIFERIDO DOCUMENTO: "
          + invoice.getScrPhysicalDocumentno();
      GLJournal gljournal = createGLJournalForTransTax(conProvider, invoice, dateacct, acctschema,
          gljournal_doctype, glcategory, tc, description, user);
      OBDal.getInstance().save(gljournal);

      List<InvoiceTax> invoicetaxs = invoice.getInvoiceTaxList();
      int ncreated = 0;
      for (int j = 0; j < invoicetaxs.size(); j++) {
        InvoiceTax invoicetax = invoicetaxs.get(j);

        boolean ismigrated = invoice.isScoIsmigrated() != null ? invoice.isScoIsmigrated() : false;
        BigDecimal migrated_taxamt = invoice.getScoMigratedTaxamt() != null
            ? invoice.getScoMigratedTaxamt() : new BigDecimal(0);

        if (ismigrated && migrated_taxamt.compareTo(new BigDecimal(0)) == 0)
          continue;
        if (!ismigrated && invoicetax.getTaxAmount().compareTo(new BigDecimal(0)) == 0)
          continue;

        OBCriteria<TaxRateAccounts> aoc = OBDal.getInstance().createCriteria(TaxRateAccounts.class);
        aoc.add(Restrictions.eq(TaxRateAccounts.PROPERTY_CLIENT, invoice.getClient()));
        aoc.add(Restrictions.eq(TaxRateAccounts.PROPERTY_ACCOUNTINGSCHEMA, acctschema));
        aoc.add(Restrictions.eq(TaxRateAccounts.PROPERTY_TAX, invoicetax.getTax()));
        List<TaxRateAccounts> taxrateaccs = aoc.list();
        if (taxrateaccs.size() == 0) {
          throw new OBException("@SCO_ProcessMTrTaxJournalNoAccts@");
        }

        TaxRateAccounts taxrateacc = taxrateaccs.get(0);

        GLJournalLine gllinecr = null;
        GLJournalLine gllinedr = null;

        BigDecimal taxamt = invoicetax.getTaxAmount();
        if (ismigrated) {
          taxamt = migrated_taxamt;
        }

        if (invoice.isSalesTransaction()) {
          if (taxrateacc.getScoTManDueTransAcct() == null || taxrateacc.getTaxDue() == null) {
            throw new OBException("@SCO_ProcessMTrTaxJournalNoAccts@");
          }
          gllinedr = createGLJournalLine(gljournal, user, unit_uom, new Long(10),
              taxrateacc.getScoTManDueTransAcct(), taxamt, invoice, true);
          gllinecr = createGLJournalLine(gljournal, user, unit_uom, new Long(20),
              taxrateacc.getTaxDue(), taxamt, invoice, false);
        } else {
          if (taxrateacc.getScoTManCredTransAcct() == null || taxrateacc.getTaxCredit() == null) {
            throw new OBException("@SCO_ProcessMTrTaxJournalNoAccts@");
          }
          gllinedr = createGLJournalLine(gljournal, user, unit_uom, new Long(10),
              taxrateacc.getTaxCredit(), taxamt, invoice, true);
          gllinecr = createGLJournalLine(gljournal, user, unit_uom, new Long(30),
              taxrateacc.getScoTManCredTransAcct(), taxamt, invoice, false);
        }
        ncreated++;
        OBDal.getInstance().save(gllinedr);
        OBDal.getInstance().save(gllinecr);
      }
      OBDal.getInstance().flush();

      if (ncreated == 0) {
        throw new OBException("@SCO_ProcessMTrTaxJournalNoTaxLine@");
      }

      invoice.setScoMtrtaxGljournal(gljournal);
      OBDal.getInstance().save(invoice);
      OBDal.getInstance().flush();

      /*
       * System.out.println("inv:" + invoice.getIdentifier() + " - strDateacct:" + strDateacct +
       * " - dateacct:" + dateacct.toString());
       */

      final OBError msg = new OBError();
      msg.setType("Success");
      msg.setTitle("@Success@");
      msg.setMessage("@SCO_ProcessMTrTaxJournalSuccess@" + gljournal.getDocumentNo());
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

  GLJournal createGLJournalForTransTax(ConnectionProvider conProvider, Invoice invoice,
      Date dateacct, AcctSchema acctschema, DocumentType gljournal_doctype, GLCategory glcategory,
      BigDecimal tc, String description, User user) throws OBException, ServletException {
    GLJournal gljournal = OBProvider.getInstance().get(GLJournal.class);
    gljournal.setClient(invoice.getClient());
    gljournal.setOrganization(invoice.getOrganization());
    gljournal.setCreatedBy(user);
    gljournal.setUpdatedBy(user);
    gljournal.setCreationDate(new Date());
    gljournal.setUpdated(new Date());
    gljournal.setActive(true);
    gljournal.setAccountingSchema(acctschema);
    gljournal.setDocumentType(gljournal_doctype);
    gljournal.setDocumentNo(SCO_Utils.getDocumentNo(gljournal.getDocumentType(), "GL_Journal"));
    gljournal.setDocumentStatus("DR");
    gljournal.setDocumentAction("CO");
    gljournal.setApproved(true);
    gljournal.setPrint(false);
    gljournal.setDescription("");
    gljournal.setPostingType("A");
    gljournal.setGLCategory(glcategory);
    gljournal.setDocumentDate(dateacct);
    gljournal.setAccountingDate(dateacct);
    gljournal.setCurrency(invoice.getCurrency());
    gljournal.setRate(tc);
    gljournal.setCurrencyRateType("S");
    gljournal.setDescription(description);

    String DATE_FORMAT = "dd-MM-yyyy";
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
    String strcPeriodId = null;

    strcPeriodId = RetireAssetData.period(conProvider, gljournal.getClient().getId(),
        gljournal.getOrganization().getId(), sdf.format(gljournal.getAccountingDate()));

    Period period = OBDal.getInstance().get(Period.class, strcPeriodId);
    gljournal.setPeriod(period);
    gljournal.setControlAmount(new BigDecimal(0));
    gljournal.setTotalCreditAmount(new BigDecimal(0));
    gljournal.setTotalDebitAmount(new BigDecimal(0));
    gljournal.setProcessed(false);
    gljournal.setProcessNow(false);
    gljournal.setOpening(false);

    return gljournal;
  }

  GLJournalLine createGLJournalLine(GLJournal gljournal, User user, UOM uom, Long lineno,
      AccountingCombination validcombination, BigDecimal amount, Invoice invoice, boolean isDebit) {
    GLJournalLine gljournalline = OBProvider.getInstance().get(GLJournalLine.class);

    gljournalline.setClient(gljournal.getClient());
    gljournalline.setOrganization(gljournal.getOrganization());
    gljournalline.setCreatedBy(user);
    gljournalline.setUpdatedBy(user);
    gljournalline.setCreationDate(new Date());
    gljournalline.setUpdated(new Date());
    gljournalline.setActive(true);
    gljournalline.setJournalEntry(gljournal);
    gljournalline.setLineNo(lineno);
    gljournalline.setGenerated(false);
    gljournalline.setDescription(gljournal.getDescription());
    gljournalline.setRate(gljournal.getRate());

    if (isDebit) {
      gljournalline.setForeignCurrencyDebit(amount);
      gljournalline.setForeignCurrencyCredit(new BigDecimal(0));

    } else {
      gljournalline.setForeignCurrencyDebit(new BigDecimal(0));
      gljournalline.setForeignCurrencyCredit(amount);
    }

    gljournalline.setCurrency(gljournal.getCurrency());
    gljournalline.setCurrencyRateType(gljournal.getCurrencyRateType());
    gljournalline.setAccountingDate(gljournalline.getAccountingDate());

    gljournalline.setUOM(uom);
    gljournalline.setQuantity(new BigDecimal(0));
    gljournalline.setAccountingCombination(validcombination);
    gljournalline.setOpenItems(false);
    gljournalline.setAPRMAddPayment(true);

    gljournalline.setProject(gljournal.getProject());
    gljournalline.setProduct(gljournal.getProduct());
    gljournalline.setCostCenter(gljournal.getCostCenter());
    gljournalline.setNdDimension(gljournal.getNdDimension());
    gljournalline.setStDimension(gljournal.getStDimension());
    gljournalline.setAsset(gljournal.getAsset());
    gljournalline.setBusinessPartner(invoice.getBusinessPartner());
    gljournalline.setScoCInvoice(invoice);

    return gljournalline;
  }
}