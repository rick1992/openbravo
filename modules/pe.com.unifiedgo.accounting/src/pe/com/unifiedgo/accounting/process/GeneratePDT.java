package pe.com.unifiedgo.accounting.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.businessUtility.TabAttachments;
import org.openbravo.erpCommon.businessUtility.TabAttachmentsData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.financialmgmt.calendar.Period;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

import pe.com.unifiedgo.accounting.StructPdt;
import pe.com.unifiedgo.accounting.SunatUtil;
import pe.com.unifiedgo.accounting.data.SCOPdt;
import pe.com.unifiedgo.accounting.data.SCOPdtDetail;

public class GeneratePDT extends DalBaseProcess {
  public void doExecute(ProcessBundle bundle) throws Exception {
    final ConnectionProvider conProvider = bundle.getConnection();
    Connection conn = conProvider.getTransactionConnection();
    try {

      // retrieve the parameters from the bundle
      final String c_period_id = (String) bundle.getParams().get("cperiodid");
      final String sco_pdt_id = (String) bundle.getParams().get("SCO_Pdt_ID");

      final VariablesSecureApp vars = bundle.getContext().toVars();
      OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
      final String language = bundle.getContext().getLanguage();
      User user = OBDal.getInstance().get(User.class, vars.getUser());

      SCOPdt pdt = OBDal.getInstance().get(SCOPdt.class, sco_pdt_id);
      Period period = OBDal.getInstance().get(Period.class, c_period_id);

      Calendar cal = Calendar.getInstance();
      cal.setTime(period.getStartingDate());
      String month = String.format("%1$2s", String.valueOf(cal.get(Calendar.MONTH) + 1)).replace(' ', '0');

      String pdtName = "";
      StructPdt pdtData = null;

      if (pdt.getName().equals("PDT 621P")) {
        OBContext.setAdminMode();

        pdtData = SunatUtil.getPDT621Percepciones(conProvider, period.getStartingDate(), period.getEndingDate(), pdt.getOrganization());
        OBContext.restorePreviousMode();

        pdtName = "0621" + pdt.getOrganization().getOrganizationInformationList().get(0).getTaxID() + period.getYear().getFiscalYear() + month + "P.TXT";
      } else if (pdt.getName().equals("PDT 621P S/C")) {
        OBContext.setAdminMode();

        pdtData = SunatUtil.getPDT621PercepcionesSC(conProvider, period.getStartingDate(), period.getEndingDate(), pdt.getOrganization());
        OBContext.restorePreviousMode();

        pdtName = "0621" + pdt.getOrganization().getOrganizationInformationList().get(0).getTaxID() + period.getYear().getFiscalYear() + month + "P.TXT";
      } else if (pdt.getName().equals("PDT 621R")) {
        OBContext.setAdminMode();

        pdtData = SunatUtil.getPDT621Retenciones(conProvider, period.getStartingDate(), period.getEndingDate(), pdt.getOrganization());
        OBContext.restorePreviousMode();

        pdtName = "0621" + pdt.getOrganization().getOrganizationInformationList().get(0).getTaxID() + period.getYear().getFiscalYear() + month + "R.TXT";
      } else if (pdt.getName().equals("PDT 626")) {
        OBContext.setAdminMode();

        pdtData = SunatUtil.getPDT626Retenciones(conProvider, period.getStartingDate(), period.getEndingDate(), pdt.getOrganization());
        OBContext.restorePreviousMode();

        pdtName = "0626" + pdt.getOrganization().getOrganizationInformationList().get(0).getTaxID() + period.getYear().getFiscalYear() + month + ".TXT";

      } else if (pdt.getName().equals("PDT 697")) {
        OBContext.setAdminMode();

        pdtData = SunatUtil.getPDT697Percepciones(conProvider, period.getStartingDate(), period.getEndingDate(), pdt.getOrganization());
        OBContext.restorePreviousMode();

        pdtName = "0697" + pdt.getOrganization().getOrganizationInformationList().get(0).getTaxID() + period.getYear().getFiscalYear() + month + ".TXT";

      } else if (pdt.getName().equals("PDT 697 S/C")) {
        OBContext.setAdminMode();

        pdtData = SunatUtil.getPDT697PercepcionesSC(conProvider, period.getStartingDate(), period.getEndingDate(), pdt.getOrganization());
        OBContext.restorePreviousMode();

        pdtName = "0697" + pdt.getOrganization().getOrganizationInformationList().get(0).getTaxID() + period.getYear().getFiscalYear() + month + ".TXT";

      } else if (pdt.getName().equals("PDT 3550")) {
        OBContext.setAdminMode();

        pdtData = SunatUtil.getPDT3550DetalleDeVentas(conProvider, period.getStartingDate(), period.getEndingDate(), pdt.getOrganization());
        OBContext.restorePreviousMode();

        pdtName = "3550" + pdt.getOrganization().getOrganizationInformationList().get(0).getTaxID() + period.getYear().getFiscalYear() + ".TXT";

      } else if (pdt.getName().equals("PDT 617 2da")) {
        OBContext.setAdminMode();

        pdtData = SunatUtil.getPDT617_2da(conProvider, period.getStartingDate(), period.getEndingDate(), pdt.getOrganization());
        OBContext.restorePreviousMode();

        pdtName = "0617" + period.getYear().getFiscalYear() + month + pdt.getOrganization().getOrganizationInformationList().get(0).getTaxID() + ".2da";

      } else if (pdt.getName().equals("PDT 617 Dividendos")) {
          OBContext.setAdminMode();

          pdtData = SunatUtil.getPDT617_Dividendos(conProvider, period.getStartingDate(), period.getEndingDate(), pdt.getOrganization());
          OBContext.restorePreviousMode();

          pdtName = "0617" + period.getYear().getFiscalYear() + month + pdt.getOrganization().getOrganizationInformationList().get(0).getTaxID() + ".div";

      } else if (pdt.getName().equals("PDT 617 No Domiciliados")) {
          OBContext.setAdminMode();

          pdtData = SunatUtil.getPDT617_NoDomiciliado(conProvider, period.getStartingDate(), period.getEndingDate(), pdt.getOrganization());
          OBContext.restorePreviousMode();

          pdtName = "0617" + period.getYear().getFiscalYear() + month + pdt.getOrganization().getOrganizationInformationList().get(0).getTaxID() + ".ndo";
        
      } else if (pdt.getName().equals("PDT 3500 Costos")) {
        // this will only consider posted invoices
        OBContext.setAdminMode();

        pdtData = SunatUtil.getPDT3500_costos(conProvider, period.getStartingDate(), period.getEndingDate(), pdt.getOrganization());
        OBContext.restorePreviousMode();

        pdtName = "Costos.txt";

      } else if (pdt.getName().equals("PDT 3500 Ingresos")) {
        // this will only consider posted invoices
        OBContext.setAdminMode();

        pdtData = SunatUtil.getPDT3500_ingresos(conProvider, period.getStartingDate(), period.getEndingDate(), pdt.getOrganization());
        OBContext.restorePreviousMode();

        pdtName = "Ingresos.txt";
        
      } else {
        throw new OBException("@SCO_ErrorNoExistsPDT@");
      }

      // save detail
      OBCriteria<SCOPdtDetail> obCriteria = OBDal.getInstance().createCriteria(SCOPdtDetail.class);

      Query q = OBDal.getInstance().getSession().createSQLQuery("SELECT COALESCE(MAX(LINE),0)+10 AS DefaultValue FROM SCO_PDT_DETAIL WHERE SCO_PDT_ID='" + pdt.getId() + "'");
      int lineno = ((BigDecimal) q.uniqueResult()).intValue();

      obCriteria.add(Restrictions.eq(SCOPdtDetail.PROPERTY_PDT, pdt));
      obCriteria.add(Restrictions.eq(SCOPdtDetail.PROPERTY_PERIOD, period));

      SCOPdtDetail pdtDetail = null;
      if (obCriteria.list().size() != 0) {
        pdtDetail = obCriteria.list().get(0);

      } else {
        pdtDetail = OBProvider.getInstance().get(SCOPdtDetail.class);
        pdtDetail.setLineNo((long) lineno);
        pdtDetail.setPDT(pdt);
        pdtDetail.setPeriod(period);
        pdtDetail.setOrganization(pdt.getOrganization());
      }

      pdtDetail.setLastDategen(new Date());
      pdtDetail.setNEGAmount(BigDecimal.valueOf(pdtData.negAmount));
      pdtDetail.setPOSAmount(BigDecimal.valueOf(pdtData.posAmount));
      pdtDetail.setNUMEntries((long) pdtData.numEntries);
      OBDal.getInstance().save(pdtDetail);

      // save as attachment
      String directory = TabAttachments.getAttachmentDirectoryForNewAttachments("4A68DA8917E44963B16B17C43D15B08D", pdtDetail.getId());
      String attachPath = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("attach.path");

      File attachDirectory = new File(attachPath + "/" + directory);

      Date dateNow = new Date();
      SimpleDateFormat dateformatyyyyMMdd = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
      String strDate = dateformatyyyyMMdd.format(dateNow);

      String filename = pdtName;

      // Lookup for the filename in the attachments of this detail.If it exists then do nothing
      // else create new attachment

      String strFileId = TabAttachmentsData.selectUniq(conProvider, pdtDetail.getClient().getId(), pdtDetail.getOrganization().getId(), "4A68DA8917E44963B16B17C43D15B08D", pdtDetail.getId(), directory, filename);
      if (strFileId == null) {
        attachDirectory.mkdirs();
      }

      BufferedWriter writer = null;
      writer = new BufferedWriter(new FileWriter(attachPath + "/" + directory + "/" + filename));
      writer.write(pdtData.data);
      writer.close();

      if (strFileId == null) {
        final String newFileId = SequenceIdData.getUUID();
        try {
          TabAttachmentsData.insert(conn, conProvider, newFileId, pdtDetail.getClient().getId(), pdtDetail.getOrganization().getId(), user.getId(), "4A68DA8917E44963B16B17C43D15B08D", pdtDetail.getId(), "100", "Generated by UGO ", filename, directory);
        } catch (Exception ex) {
        }
      } else {
        TabAttachmentsData.update(conProvider, user.getId(), "100", "Generated by UGO ", strFileId);
      }

      final OBError msg = new OBError();
      msg.setType("Success");
      msg.setTitle("@Success@");

      bundle.setResult(msg);
      OBDal.getInstance().commitAndClose();
      conProvider.releaseCommitConnection(conn);

    } catch (final OBException e) {
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(e.getMessage());
      msg.setTitle("@Error@");
      OBDal.getInstance().rollbackAndClose();
      conProvider.releaseRollbackConnection(conn);

      bundle.setResult(msg);
    }
  }

  Date addDays(Date date, int days) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.add(Calendar.DATE, 1);
    return cal.getTime();
  }
}