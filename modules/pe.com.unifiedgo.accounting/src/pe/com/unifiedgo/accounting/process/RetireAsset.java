package pe.com.unifiedgo.accounting.process;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.process.FIN_AddPaymentFromJournal;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.uom.UOM;
import org.openbravo.model.financialmgmt.accounting.coa.AccountingCombination;
import org.openbravo.model.financialmgmt.assetmgmt.AmortizationLine;
import org.openbravo.model.financialmgmt.assetmgmt.Asset;
import org.openbravo.model.financialmgmt.assetmgmt.AssetAccounts;
import org.openbravo.model.financialmgmt.calendar.Period;
import org.openbravo.model.financialmgmt.gl.GLCategory;
import org.openbravo.model.financialmgmt.gl.GLJournal;
import org.openbravo.model.financialmgmt.gl.GLJournalLine;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

import pe.com.unifiedgo.accounting.SCO_Utils;
import pe.com.unifiedgo.accounting.data.SCOAAssetGljourLine;
import pe.com.unifiedgo.accounting.data.SCOAssetCostcenter;
import pe.com.unifiedgo.accounting.data.SCOAssetImprov;
import pe.com.unifiedgo.accounting.data.SCOAssetReeval;

public class RetireAsset extends DalBaseProcess {

  static public String OBErrorString = null;

  public void doExecute(ProcessBundle bundle) throws Exception {
    try {

      /*
       * Set<String> params = bundle.getParams().keySet(); for (int i = 0; i < params.size(); i++) {
       * System.out.println(params.toArray()[i]); }
       */

      final String A_Asset_ID = (String) bundle.getParams().get("A_Asset_ID");
      final String retirementaction = (String) bundle.getParams().get("retirementaction");

      final VariablesSecureApp vars = bundle.getContext().toVars();
      OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
      final ConnectionProvider conProvider = bundle.getConnection();
      final String language = bundle.getContext().getLanguage();
      User user = OBDal.getInstance().get(User.class, vars.getUser());

      int count = 0;

      Asset asset = OBDal.getInstance().get(Asset.class, A_Asset_ID);
      if (asset == null) {
        throw new OBException("Internal Error Null");
      }

      if (asset.getScoAssetStatus().equals("SCO_ACT") && retirementaction.equals("SCO_RET")) {
        DocumentType gljournal_doctype = SCO_Utils.getDocTypeFromSpecial(asset.getOrganization(), "SCOGLJOURNAL");
        if (gljournal_doctype == null) {
          throw new OBException("@SCO_RetireAssetNoDoctype@");
        }

        if (asset.getCurrency() == null) {
          throw new OBException("@SCO_RetireAssetNoCurrency@");
        }

        GLCategory glcategory = null;
        OBCriteria<GLCategory> obc = OBDal.getInstance().createCriteria(GLCategory.class);
        obc.add(Restrictions.eq(GLCategory.PROPERTY_CATEGORYTYPE, "M"));
        obc.add(Restrictions.eq(GLCategory.PROPERTY_DEFAULT, true));

        List<GLCategory> glcats = obc.list();
        if (glcats.size() == 0) {
          throw new OBException("@SCO_RetireAssetNoGLCategory@");
        }
        glcategory = glcats.get(0);
        if (glcategory == null) {
          throw new OBException("@SCO_RetireAssetNoGLCategory@");
        }

        UOM unit_uom = OBDal.getInstance().get(UOM.class, "100");
        if (unit_uom == null) {
          throw new OBException("@SCO_RetireAssetNoUOM@");
        }

        Date cancellationdate = asset.getCancellationDate();
        if (cancellationdate == null) {
          throw new OBException("@SCO_RetireAssetAssetNoCancelationDate@");
        }

        BigDecimal asset_value = asset.getAssetValue();
        if (asset_value == null) {
          throw new OBException("@SCO_RetireAssetAssetValueLessEqualThanZero@");
        }

        // Add improvements to asset value
        List<SCOAssetImprov> improvs = asset.getSCOAssetImprovList();
        for (int i = 0; i < improvs.size(); i++) {
          asset_value = asset_value.add(improvs.get(i).getAmount());
        }

        // Add reevaluations to asset value
        List<SCOAssetReeval> reevals = asset.getSCOAssetReevalList();
        for (int i = 0; i < reevals.size(); i++) {
          asset_value = asset_value.add(reevals.get(i).getAmount());
        }

        if (asset_value.compareTo(new BigDecimal(0)) <= 0) {
          throw new OBException("@SCO_RetireAssetAssetValueLessEqualThanZero@");
        }

        List<AmortizationLine> amortizationlines = asset.getFinancialMgmtAmortizationLineList();
        BigDecimal acumm_amortization_amt = new BigDecimal(0);

        for (int i = 0; i < amortizationlines.size(); i++) {
          AmortizationLine amortizationline = amortizationlines.get(i);

          if (amortizationline == null) {
            throw new OBException("@SCO_RetireAssetLineNotAmortization@");
          }

          if (amortizationline.getAmortization().getProcessed().equals("Y")) {
            if (amortizationline.getAmortizationAmount() != null) {
              acumm_amortization_amt = acumm_amortization_amt.add(amortizationline.getAmortizationAmount());
            }
          } else {
            throw new OBException("@SCO_RetireAssetAmortizationNotProccessed@");
          }
        }

        BigDecimal asset_PreviouslyDepreciatedAmt = asset.getPreviouslyDepreciatedAmt() != null ? asset.getPreviouslyDepreciatedAmt() : new BigDecimal(0);
        if (asset_PreviouslyDepreciatedAmt.compareTo(new BigDecimal(0)) < 0) {
          throw new OBException("@SCO_RetireAssetPreviouslyDepAmtLessThanZero@");
        }

        acumm_amortization_amt = acumm_amortization_amt.add(asset_PreviouslyDepreciatedAmt);
        if (acumm_amortization_amt.compareTo(new BigDecimal(0)) < 0) {
          throw new OBException("@SCO_RetireAssetAcumAmortLessThanZero@");
        }

        BigDecimal alienation_exp_acc = asset_value.subtract(acumm_amortization_amt);

        if (alienation_exp_acc.compareTo(new BigDecimal(0)) < 0) {
          throw new OBException("@SCO_RetireAssetAlienationLessThanZero@");
        }

        List<AssetAccounts> asset_accs = asset.getFinancialMgmtAssetAccountsList();
        int lineno = 10;
        for (int i = 0; i < asset_accs.size(); i++) {

          AssetAccounts asset_acc = asset_accs.get(i);

          GLJournal gljournal = createGLJournalForRetirementAsset(conProvider, asset, cancellationdate, asset_acc, gljournal_doctype, glcategory, user);
          if (gljournal == null) {
            if (OBErrorString != null) {
              throw new OBException(OBErrorString);
            } else {
              throw new OBException("Internal Error GLJournal Null");
            }
          }

          OBDal.getInstance().save(gljournal);
          OBDal.getInstance().flush();

          AccountingCombination asset_acct = asset_acc.getSCOAssetsAccount();
          AccountingCombination alienation_exp_acct = asset_acc.getSCOAlienationExpensesAccount();
          AccountingCombination accum_depre_acct = asset_acc.getAccumulatedDepreciation();

          if (asset_acct == null || alienation_exp_acct == null || accum_depre_acct == null) {
            throw new OBException("@SCO_RetireAssetNoAccount@");
          }

          GLJournalLine gljournalline_asset = createGLJournalLine(gljournal, user, unit_uom, new Long(10), asset_acct, asset_value, false);
          OBDal.getInstance().save(gljournalline_asset);
          OBDal.getInstance().flush();
          if (acumm_amortization_amt.compareTo(new BigDecimal(0)) != 0) {
            GLJournalLine gljournalline_accum_depre = createGLJournalLine(gljournal, user, unit_uom, new Long(20), accum_depre_acct, acumm_amortization_amt, true);
            OBDal.getInstance().save(gljournalline_accum_depre);
            OBDal.getInstance().flush();
          }
          if (alienation_exp_acc.compareTo(new BigDecimal(0)) != 0) {
            GLJournalLine gljournalline_alienation = createGLJournalLine(gljournal, user, unit_uom, new Long(30), alienation_exp_acct, alienation_exp_acc, true);
            OBDal.getInstance().save(gljournalline_alienation);
            OBDal.getInstance().flush();
          }
          SCOAAssetGljourLine assetgljournalline = OBProvider.getInstance().get(SCOAAssetGljourLine.class);
          assetgljournalline.setClient(asset.getClient());
          assetgljournalline.setOrganization(asset.getOrganization());
          assetgljournalline.setCreatedBy(user);
          assetgljournalline.setUpdatedBy(user);
          assetgljournalline.setCreationDate(new Date());
          assetgljournalline.setUpdated(new Date());
          assetgljournalline.setActive(true);
          assetgljournalline.setLineNo(new Long(lineno));
          assetgljournalline.setAsset(asset);
          assetgljournalline.setJournalEntry(gljournal);

          OBDal.getInstance().save(assetgljournalline);
          OBDal.getInstance().flush();

          // Try to complete the gljournal
          VariablesSecureApp covars = new VariablesSecureApp(vars.getUser(), vars.getClient(), vars.getOrg(), vars.getRole(), vars.getLanguage());
          ProcessBundle pb = new ProcessBundle("5BE14AA10165490A9ADEFB7532F7FA94", covars).init(conProvider);
          HashMap<String, Object> parameters = new HashMap<String, Object>();
          parameters.put("GL_Journal_ID", gljournal.getId());
          parameters.put("inpdocaction", "CO");
          pb.setParams(parameters);
          OBError myMessage = null;
          // Create a Payment for the Journal
          FIN_AddPaymentFromJournal myProcess = new FIN_AddPaymentFromJournal();
          myProcess.setDoCommit(false);
          myProcess.execute(pb);
          myMessage = (OBError) pb.getResult();

          if (myMessage.getType().equals("Error")) {
            throw new OBException("@SCO_RetireAssetErrorCOJournal@ " + lineno + " - " + myMessage.getMessage());
          }

          lineno = lineno + 10;
        }

        asset.setScoAssetStatus("SCO_RET");
        asset.setScoRetireAssetProc("SCO_RE");
        OBDal.getInstance().save(asset);

      } else if (asset.getScoAssetStatus().equals("SCO_RET") && retirementaction.equals("SCO_RE")) {
        List<SCOAAssetGljourLine> asset_gljournal_lines = asset.getSCOAAssetGljourLineList();
        List<GLJournal> journalentries = new ArrayList<GLJournal>();
        // try to remove the gljournals
        try {
          for (int i = 0; i < asset_gljournal_lines.size(); i++) {
            journalentries.add(asset_gljournal_lines.get(i).getJournalEntry());
            asset_gljournal_lines.remove(i);
          }

          OBDal.getInstance().flush();

          for (int i = 0; i < journalentries.size(); i++) {
            OBDal.getInstance().remove(journalentries.get(i));
          }

          OBDal.getInstance().flush();

        } catch (Exception e) {
          e.printStackTrace();
          throw new OBException("@SCO_RetireAssetRECantDELGLJournal@");
        }

        asset.setScoAssetStatus("SCO_ACT");
        asset.setScoRetireAssetProc("SCO_RET");
        OBDal.getInstance().save(asset);

      } else {
        throw new OBException("@ActionNotAllowedHere@");
      }

      final OBError msg = new OBError();
      msg.setType("Success");
      msg.setTitle("@Success@");

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

  GLJournal createGLJournalForRetirementAsset(ConnectionProvider conProvider, Asset asset, Date cancellationdate, AssetAccounts asset_acc, DocumentType gljournal_doctype, GLCategory glcategory, User user) throws OBException {
    GLJournal gljournal = OBProvider.getInstance().get(GLJournal.class);
    gljournal.setClient(asset.getClient());
    gljournal.setOrganization(asset.getOrganization());
    gljournal.setCreatedBy(user);
    gljournal.setUpdatedBy(user);
    gljournal.setCreationDate(new Date());
    gljournal.setUpdated(new Date());
    gljournal.setActive(true);
    gljournal.setAccountingSchema(asset_acc.getAccountingSchema());
    gljournal.setDocumentType(gljournal_doctype);
    gljournal.setDocumentNo(SCO_Utils.getDocumentNo(gljournal.getDocumentType(), "GL_Journal"));
    gljournal.setDocumentStatus("DR");
    gljournal.setDocumentAction("CO");
    gljournal.setApproved(true);
    gljournal.setPrint(false);
    gljournal.setDescription("");
    gljournal.setPostingType("A");
    gljournal.setGLCategory(glcategory);
    gljournal.setDocumentDate(cancellationdate);
    gljournal.setAccountingDate(cancellationdate);
    gljournal.setCurrency(asset.getCurrency());

    List<SCOAssetCostcenter> lsCostcenter = asset.getSCOAssetCostcenterList();
    if (lsCostcenter.size() == 0) {
      OBErrorString = "@SCO_RetireAssetNoCostCenter@";
      return null;
    }

    gljournal.setCostCenter(lsCostcenter.get(0).getCostCenter());

    if (gljournal.getCurrency().getId().equals(gljournal.getAccountingSchema().getCurrency().getId())) {
      gljournal.setRate(new BigDecimal(1));
      gljournal.setCurrencyRateType("S");
    } else {
      ConversionRate convrate = SCO_Utils.getConversionRate(gljournal.getCurrency(), gljournal.getAccountingSchema().getCurrency(), asset.getPurchaseDate());
      if (convrate == null) {
        OBErrorString = "@SCO_RetireAssetNoConvRate@";
        return null;
      }

      gljournal.setRate(convrate.getMultipleRateBy());
      gljournal.setCurrencyRateType(convrate.getConversionRateType());
    }

    String DATE_FORMAT = "dd-MM-yyyy";
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
    String strcPeriodId = null;
    try {

      strcPeriodId = RetireAssetData.period(conProvider, gljournal.getClient().getId(), gljournal.getOrganization().getId(), sdf.format(gljournal.getAccountingDate()));

    } catch (Exception e) {
      OBErrorString = "@SCO_RetireAssetNoOpenPeriod@";
      return null;
    }
    if (strcPeriodId == null) {
      OBErrorString = "@SCO_RetireAssetNoOpenPeriod@";
      return null;
    }
    Period period = OBDal.getInstance().get(Period.class, strcPeriodId);
    if (period == null) {
      OBErrorString = "@SCO_RetireAssetNoOpenPeriod@";
      return null;
    }
    gljournal.setPeriod(period);
    gljournal.setControlAmount(new BigDecimal(0));
    gljournal.setTotalCreditAmount(new BigDecimal(0));
    gljournal.setTotalDebitAmount(new BigDecimal(0));
    gljournal.setProcessed(false);
    gljournal.setProcessNow(false);
    gljournal.setOpening(false);

    return gljournal;
  }

  GLJournalLine createGLJournalLine(GLJournal gljournal, User user, UOM uom, Long lineno, AccountingCombination validcombination, BigDecimal amount, boolean isDebit) {
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

    Long stdPrecision = gljournal.getAccountingSchema().getCurrency().getStandardPrecision();
    BigDecimal CurrencyRateValue = gljournal.getRate();
    if (isDebit) {
      gljournalline.setForeignCurrencyDebit(amount);
      gljournalline.setForeignCurrencyCredit(new BigDecimal(0));

      gljournalline.setDebit(amount.multiply(CurrencyRateValue).setScale(stdPrecision.intValue(), BigDecimal.ROUND_HALF_UP));
      gljournalline.setCredit(new BigDecimal(0));
    } else {
      gljournalline.setForeignCurrencyDebit(new BigDecimal(0));
      gljournalline.setForeignCurrencyCredit(amount);

      gljournalline.setDebit(new BigDecimal(0));
      gljournalline.setCredit(amount.multiply(CurrencyRateValue).setScale(stdPrecision.intValue(), BigDecimal.ROUND_HALF_UP));
    }

    CurrencyRateValue = CurrencyRateValue.setScale(stdPrecision.intValue(), BigDecimal.ROUND_HALF_UP);
    gljournalline.setRate(CurrencyRateValue);

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
    gljournalline.setBusinessPartner(gljournal.getBusinessPartner());
    gljournalline.setCostCenter(gljournal.getCostCenter());
    gljournalline.setNdDimension(gljournal.getNdDimension());
    gljournalline.setStDimension(gljournal.getStDimension());
    gljournalline.setAsset(gljournal.getAsset());

    return gljournalline;
  }

}