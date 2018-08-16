package pe.com.unifiedgo.accounting.process;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.ad.utility.Attachment;
import org.openbravo.model.financialmgmt.accounting.Costcenter;
import org.openbravo.model.financialmgmt.accounting.coa.AccountingCombination;
import org.openbravo.model.financialmgmt.gl.GLJournal;
import org.openbravo.model.financialmgmt.gl.GLJournalLine;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.service.db.DbUtility;

import pe.com.unifiedgo.accounting.data.SCOInternalDoc;

public class GeneratePayRollEntries extends DalBaseProcess {
  public void doExecute(ProcessBundle bundle) throws Exception {
    int entryno = 0;
    try {

      /*
       * Set<String> params = bundle.getParams().keySet(); for (int i = 0; i < params.size(); i++) {
       * System.out.println(params.toArray()[i]); }
       */

      final String GL_JOURNAL_ID = (String) bundle.getParams().get("GL_Journal_ID");
      final String strcFileId = (String) bundle.getParams().get("cfileid");
      final VariablesSecureApp vars = bundle.getContext().toVars();
      OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
      final ConnectionProvider conProvider = bundle.getConnection();
      final String language = bundle.getContext().getLanguage();
      User user = OBDal.getInstance().get(User.class, vars.getUser());

      int count = 0;

      GLJournal journal = OBDal.getInstance().get(GLJournal.class, GL_JOURNAL_ID);
      if (journal == null) {
        throw new OBException("Internal Error Null");
      }
      Attachment attachment = OBDal.getInstance().get(Attachment.class, strcFileId);
      if (attachment == null) {
        throw new OBException("Internal Error Null");
      }

      DecimalFormat df = new DecimalFormat("#0.00");
      DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
      otherSymbols.setDecimalSeparator('.');
      otherSymbols.setGroupingSeparator(',');
      df.setDecimalFormatSymbols(otherSymbols);

      String attachPath = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("attach.path");
      String payrollfilepath = attachPath + "/" + attachment.getPath() + "/" + attachment.getName();

      FileInputStream fstream = new FileInputStream(payrollfilepath);
      BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

      // Get the default payroll costcenter
      Costcenter defaultprcostcenter = null;
      String defaultCostCenterId = GeneratePayRollEntriesData.getDefaultPayRollCostCenterFromValue(conProvider, bundle.getContext().getUserClient(), bundle.getContext().getUserOrganization(), journal.getClient().getId(), journal.getOrganization().getId(), "7010010500");
      if (defaultCostCenterId != null && !defaultCostCenterId.isEmpty())
        defaultprcostcenter = OBDal.getInstance().get(Costcenter.class, defaultCostCenterId);

      String strLine;
      // Read PayRoll File

      while ((strLine = br.readLine()) != null) {
        entryno++;
        // start of file
        if (strLine.substring(0, 7).compareTo("DSUBDIA") == 0)
          continue;

        String[] parts = strLine.split(",", -1);

        /*
         * for (int ii = 0; ii < parts.length; ii++) { System.out.print("'" + parts[ii] + "' - "); }
         * System.out.print("\n");
         */

        if (parts.length != 23) {
          System.out.println("incorrect: strLine: '" + strLine + "' - parts.length:" + parts.length);
          throw new OBException("@SCO_GenPayRollEntries_IncorrectEntryLen@ " + entryno);
        }

        // parts[4]=accountno, parts[5]=dni, parts[6]=costcenter, parts[8]=(D,H), parts[9]=amount
        AccountingCombination validcomb = null;
        SCOInternalDoc internaldoc = null;
        Costcenter costcenter = null;
        BigDecimal amount = null;
        boolean isdebit = false;

        // GET VALIDCOMBINATION IF EXISTS
        if (!parts[4].isEmpty()) {
          String cValidcombinationId = GeneratePayRollEntriesData.getValidCombinationFromAccountno(conProvider, journal.getAccountingSchema().getId(), parts[4]);
          if (cValidcombinationId != null && !cValidcombinationId.isEmpty()) {
            validcomb = OBDal.getInstance().get(AccountingCombination.class, cValidcombinationId);
          }
        }

        if (validcomb == null) {
          throw new OBException("@SCO_GenPayRollEntries_EntryNoAccount@ " + entryno);
        }

        // GET INTERNAL DOCUMENT IF EXISTS
        if (!parts[5].isEmpty()) {
          GeneratePayRollEntriesData[] data = GeneratePayRollEntriesData.getWorkerPartnerFromTaxId(conProvider, journal.getClient().getId(), journal.getOrganization().getId(), parts[5]);
          if (data != null && data.length > 0) {
            for (int i = 0; i < data.length; i++) {

              String scoInternalDocId = GeneratePayRollEntriesData.getWorkerVARIOSInternalDoc(conProvider, bundle.getContext().getUserClient(), bundle.getContext().getUserOrganization(), journal.getClient().getId(), journal.getOrganization().getId(), data[i].cBpartnerId);

              if (scoInternalDocId != null && !scoInternalDocId.isEmpty()) {
                internaldoc = OBDal.getInstance().get(SCOInternalDoc.class, scoInternalDocId);
              } else {
                scoInternalDocId = GeneratePayRollEntriesData.getWorkerAnyInternalDoc(conProvider, bundle.getContext().getUserClient(), bundle.getContext().getUserOrganization(), journal.getClient().getId(), journal.getOrganization().getId(), data[i].cBpartnerId);
                if (scoInternalDocId != null && !scoInternalDocId.isEmpty()) {
                  internaldoc = OBDal.getInstance().get(SCOInternalDoc.class, scoInternalDocId);
                }
              }

              if (internaldoc != null)
                break;
            }
          }
        }

        // GET COSTCENTER IF EXISTS
        if (!parts[6].isEmpty()) {
          if (parts[6].length() < 10)
            parts[6] = parts[6] + "00";

          String cCostcenterId = GeneratePayRollEntriesData.getCostCenterFromValue(conProvider, journal.getClient().getId(), journal.getOrganization().getId(), parts[6]);
          if (cCostcenterId != null && !cCostcenterId.isEmpty()) {
            costcenter = OBDal.getInstance().get(Costcenter.class, cCostcenterId);
          }
        }

        // CHECK ACCOUNTING REQUIREMENTS FOR THE VALIDCOMBINATION
        if (validcomb.getAccount().isScoIscostcenter()) {
          // put default costcenter
          if (costcenter == null) {
            costcenter = defaultprcostcenter;
          }
        }

        if (validcomb.getAccount().isScoIscostcenter() && costcenter == null) {
          throw new OBException("@SCO_GenPayRollEntries_EntryNeedsCostcenter@ " + entryno);
        }

        if (validcomb.getAccount().isScoRequiresanalytics() && internaldoc == null) {
          throw new OBException("@SCO_GenPayRollEntries_EntryNeedsInternalDoc@ " + entryno);
        }

        // GET DEBIT/CREDIT
        if (parts[8].compareTo("D") == 0) {
          isdebit = true;
        } else if (parts[8].compareTo("H") == 0) {
          isdebit = false;
        } else {
          throw new OBException("@SCO_GenPayRollEntries_EntryNoDH@ " + entryno);
        }

        // GET AMOUNT
        amount = new BigDecimal(df.parse(parts[9]).doubleValue()).setScale(2, RoundingMode.HALF_UP);

        GLJournalLine journalline = OBProvider.getInstance().get(GLJournalLine.class);
        journalline.setClient(journal.getClient());
        journalline.setOrganization(journal.getOrganization());
        journalline.setUpdated(new Date());
        journalline.setUpdatedBy(user);
        journalline.setCreatedBy(user);
        journalline.setCreationDate(new Date());
        journalline.setActive(true);

        journalline.setJournalEntry(journal);
        journalline.setLineNo(new Long(entryno));

        if (isdebit) {
          journalline.setCredit(new BigDecimal(0));
          journalline.setForeignCurrencyCredit(new BigDecimal(0));

          journalline.setDebit(amount);
          journalline.setForeignCurrencyDebit(amount);
        } else {
          journalline.setCredit(amount);
          journalline.setForeignCurrencyCredit(amount);

          journalline.setDebit(new BigDecimal(0));
          journalline.setForeignCurrencyDebit(new BigDecimal(0));
        }

        journalline.setCurrency(journal.getCurrency());
        journalline.setCurrencyRateType("S");
        journalline.setRate(new BigDecimal(1));
        journalline.setAccountingDate(journal.getAccountingDate());
        journalline.setAccountingCombination(validcomb);

        if (internaldoc != null) {
          journalline.setBusinessPartner(internaldoc.getBusinessPartner());
          journalline.setScoInternalDoc(internaldoc);
        }

        journalline.setCostCenter(costcenter);

        OBDal.getInstance().save(journalline);

      }

      br.close();
      OBDal.getInstance().flush();

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
    } catch (final Exception e) {
      OBDal.getInstance().rollbackAndClose();

      String message = DbUtility.getUnderlyingSQLException(e).getMessage();
      if (message.contains("@"))
        message = OBMessageUtils.parseTranslation(message);

      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(message);
      msg.setTitle("@Error@");
      bundle.setResult(msg);
    }
  }
}