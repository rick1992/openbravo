package pe.com.unifiedgo.accounting.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.businessUtility.TabAttachments;
import org.openbravo.erpCommon.businessUtility.TabAttachmentsData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentSchedule;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

import pe.com.unifiedgo.accounting.data.SCOFactinvLine;
import pe.com.unifiedgo.accounting.data.SCOFactoringinvoice;
import pe.com.unifiedgo.core.data.SCRComboItem;

public class FactoringInvoiceGenerateFormat extends DalBaseProcess {
  public void doExecute(ProcessBundle bundle) throws Exception {
    String filepath = "";
    final ConnectionProvider conProvider = bundle.getConnection();
    Connection conn = conProvider.getTransactionConnection();

    try {

      Set<String> params = bundle.getParams().keySet();
      for (int i = 0; i < params.size(); i++) {
        System.out.println(params.toArray()[i]);
      }

      final String SCO_Factoringinvoice = (String) bundle.getParams().get("SCO_Factoringinvoice_ID");
      final VariablesSecureApp vars = bundle.getContext().toVars();
      OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
      final String language = bundle.getContext().getLanguage();
      User user = OBDal.getInstance().get(User.class, vars.getUser());

      int count = 0;

      SCOFactoringinvoice factinv = OBDal.getInstance().get(SCOFactoringinvoice.class, SCO_Factoringinvoice);
      if (factinv == null) {
        throw new OBException("@SCO_InternalError@");
      }

      Date dateNow = new Date();
      SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss");
      String strDate = dateformat.format(dateNow);

      SimpleDateFormat dateformatddMMyy = new SimpleDateFormat("ddMMyy");
      DecimalFormat df = new DecimalFormat("0.00##");

      String data = "";
      String header = "nombre_razon_social;apellido_paterno;apellido_materno;tipodocumento;numero_documentoid;numero_fact;fecha_vencimiento;importe";
      String body = "";
      List<SCOFactinvLine> factinv_lines = factinv.getSCOFactinvLineList();
      if (factinv_lines.size() == 0) {
        throw new OBException("@SCO_FactoringInvGenFormatWithoutLines@");
      }

      for (int i = 0; i < factinv_lines.size(); i++) {
        String factinvline = "";
        SCOFactinvLine factinv_line = factinv_lines.get(i);
        Invoice inv = factinv_line.getInvoiceref();
        BusinessPartner bp = inv.getBusinessPartner();

        String nombre_fiscal = bp.getName(); // 0:nombre_fiscal
        String apellido_paterno = "";// 1:apellido_paterno
        String apellido_materno = "";// 2:apellido_materno

        boolean isCompany = bp.isScoIscompany() != null ? bp.isScoIscompany() : false;
        if (!isCompany) {
          nombre_fiscal = bp.getScoFirstname() != null ? bp.getScoFirstname() : "";
          apellido_paterno = bp.getScoLastname() != null ? bp.getScoLastname() : "";
          apellido_materno = bp.getScoLastname2() != null ? bp.getScoLastname2() : "";
        }

        String numero_documentoid = bp.getTaxID(); // 4:numero_documentoid

        SCRComboItem tipodoc = bp.getScrComboItem();
        if (tipodoc == null) {
          throw new OBException("@SCO_FactoringInvGenFormat_InvalidData@");
        }
        String tipodocumento = tipodoc.getCode(); // 3:tipodocumento
        if (tipodocumento == null) {
          tipodocumento = "ND";
        }

        String numero_fact = inv.getScrPhysicalDocumentno(); // 5:numero_fact
        if (numero_fact == null) {
          numero_fact = "ND";
        } else {
          String[] parts = numero_fact.split("-");
          if (parts.length == 2) {
            numero_fact = parts[1];
          }
        }
        numero_fact = numero_fact.replaceFirst("^0+(?!$)", "");

        List<FIN_PaymentSchedule> paysheds = inv.getFINPaymentScheduleList();
        if (paysheds.size() == 0) {
          throw new OBException("@SCO_FactoringInvGenFormat_InvalidData@");
        }
        FIN_PaymentSchedule payshed = paysheds.get(0);

        String fecha_vencimiento = dateformatddMMyy.format(payshed.getDueDate()); // 6:fecha_vencimiento

        String importe = df.format(inv.getGrandTotalAmount()); // 7:importe

        factinvline = "\n" + nombre_fiscal + ";" + apellido_paterno + ";" + apellido_materno + ";" + tipodocumento + ";" + numero_documentoid + ";" + numero_fact + ";" + fecha_vencimiento + ";" + importe;
        body = body + factinvline;
      }

      data = header + body;
      // save as attachment
      String directory = TabAttachments.getAttachmentDirectoryForNewAttachments("9845154D73A340FDB9F4ACD8A53B75A4", factinv.getId());
      String attachPath = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("attach.path");

      File attachDirectory = new File(attachPath + "/" + directory);
      String formatfilename = factinv.getIdentifier() + ".csv";

      // Lookup for the filename in the attachments of this record.If it
      // exists then do nothing
      // else create new attachment
      String strFileId = TabAttachmentsData.selectUniq(conProvider, factinv.getClient().getId(), factinv.getOrganization().getId(), "9845154D73A340FDB9F4ACD8A53B75A4", factinv.getId(), directory, formatfilename);
      if (strFileId == null) {
        attachDirectory.mkdirs();
      }

      BufferedWriter writer = null;
      String factinvfilepath = attachPath + "/" + directory + "/" + formatfilename;

      byte[] b = data.getBytes("windows-1252");
      FileOutputStream fileOuputStream = null;
      try {
        fileOuputStream = new FileOutputStream(factinvfilepath);
        fileOuputStream.write(b);
      } finally {
        fileOuputStream.close();
      }

      if (strFileId == null) {
        filepath = factinvfilepath;
        final String newFileId = SequenceIdData.getUUID();
        try {
          TabAttachmentsData.insert(conn, conProvider, newFileId, factinv.getClient().getId(), factinv.getOrganization().getId(), OBContext.getOBContext().getUser().getId(), "9845154D73A340FDB9F4ACD8A53B75A4", factinv.getId(), user.getId(), " ", formatfilename, directory);
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

      // remove attachment
      File file = new File(filepath);
      file.delete();

      bundle.setResult(msg);
    }
  }
}