package pe.com.unifiedgo.accounting.process;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
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
import org.openbravo.model.common.businesspartner.BankAccount;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentDetail;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

import pe.com.unifiedgo.accounting.SunatUtil;

public class ExportDetraction extends DalBaseProcess {
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

      boolean detractionpayment = false;
      if (payment.isScoDetractionpayment() != null)
        detractionpayment = payment.isScoDetractionpayment();

      if ((!payment.getStatus().equals("PPM") && !payment.getStatus().equals("PWNC")
          && !payment.getStatus().equals("RPAE")) || !detractionpayment) {
        throw new OBException("@ActionNotAllowedHere@");
      }

      String file = "";

      SimpleDateFormat dateformatyy = new SimpleDateFormat("yyyy");

      String nroLote = "";

      if (payment.getScoDetractionlot() == null || payment.getScoDetractionlot() == 0) { // buscar
        // lote
        String strSql = "SELECT COALESCE(MAX(em_sco_detractionlot),0) AS toplot FROM FIN_PAYMENT INNER JOIN"
            + " AD_ORGINFO ON FIN_PAYMENT.AD_ORG_ID = AD_ORGINFO.AD_ORG_ID"
            + " WHERE AD_ORGINFO.taxid='"
            + payment.getOrganization().getOrganizationInformationList().get(0).getTaxID()
            + "' AND extract(year from FIN_PAYMENT.paymentdate)="
            + dateformatyy.format(payment.getPaymentDate());

        Query q = OBDal.getInstance().getSession().createSQLQuery(strSql);
        BigDecimal count_result = (BigDecimal) q.uniqueResult();

        nroLote = SunatUtil.LPad((count_result.intValue() + 1) + "", 4, '0');

        payment.setScoDetractionlot(new Long(nroLote));
        OBDal.getInstance().save(payment);
        OBDal.getInstance().flush();
      } else {
        nroLote = SunatUtil.LPad(payment.getScoDetractionlot().toString(), 4, '0');
      }

      String header = "";
      String indMaestra = "*";

      String rucAdq = SunatUtil.LPad(
          payment.getOrganization().getOrganizationInformationList().get(0).getTaxID(), 11, '0');
      if (rucAdq.length() != 11)
        throw new Exception();

      BigDecimal[] montoTotal = new BigDecimal[1];
      montoTotal[0] = new BigDecimal(0);
      BigDecimal importeTotal = new BigDecimal(0);

      String line = "";
      line = getFileDetracciones(payment, montoTotal);
      file = file + line;
      importeTotal = importeTotal.add(montoTotal[0]);

      String nameOrg = SunatUtil.RPad(payment.getOrganization().getSocialName(), 35, ' ');
      if (nameOrg.length() > 35)
        nameOrg.substring(0, 35);
      SimpleDateFormat dateformatOnly_yy = new SimpleDateFormat("yy");
      String importeTotalStr = SunatUtil.LPad(importeTotal.intValue() + "", 13, '0')
          + SunatUtil.LPad(
              importeTotal.remainder(BigDecimal.ONE).multiply(new BigDecimal(100)).intValue() + "",
              2, '0');

      header = indMaestra + rucAdq + nameOrg + dateformatOnly_yy.format(payment.getPaymentDate())
          + nroLote + importeTotalStr;
      file = header + file;

      String directory = TabAttachments.getAttachmentDirectoryForNewAttachments(
          "D1A97202E832470285C9B1EB026D54E2", payment.getId());
      String attachPath = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("attach.path");

      File attachDirectory = new File(attachPath + "/" + directory);

      Date dateNow = new Date();
      SimpleDateFormat dateformatyyyyMMdd = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
      String strDate = dateformatyyyyMMdd.format(dateNow);

      String filename = "D" + rucAdq + dateformatOnly_yy.format(payment.getPaymentDate()) + nroLote
          + ".txt";

      // Lookup for the filename in the attachments of this detail.If it exists then do nothing
      // else create new attachment

      String strFileId = TabAttachmentsData.selectUniq(conProvider, payment.getClient().getId(),
          payment.getOrganization().getId(), "D1A97202E832470285C9B1EB026D54E2", payment.getId(),
          directory, filename);
      if (strFileId == null) {
        attachDirectory.mkdirs();
      }

      byte[] b = file.getBytes("windows-1252");
      FileOutputStream fileOuputStream = null;
      try {
        fileOuputStream = new FileOutputStream(attachPath + "/" + directory + "/" + filename);
        fileOuputStream.write(b);
      } finally {
        fileOuputStream.close();
      }

      ConnectionProvider connProvider = bundle.getConnection();
      Connection conn = connProvider.getConnection();
      if (strFileId == null) {
        final String newFileId = SequenceIdData.getUUID();
        try {
          TabAttachmentsData.insert(conn, conProvider, newFileId, payment.getClient().getId(),
              payment.getOrganization().getId(), user.getId(), "D1A97202E832470285C9B1EB026D54E2",
              payment.getId(), "100", "Generated by UGO ", filename, directory);
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

    } catch (final OBException e) {
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(e.getMessage());
      msg.setTitle("@Error@");
      OBDal.getInstance().rollbackAndClose();
      bundle.setResult(msg);
    } catch (Exception ex) {
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(ex.getMessage());
      msg.setTitle("@Error@");
      OBDal.getInstance().rollbackAndClose();
      bundle.setResult(msg);
    }
  }

  public static String getFileDetracciones(FIN_Payment paymentDetracciones, BigDecimal[] montoTotal)
      throws OBException, Exception {

    StringBuffer sb = new StringBuffer();

    List<DetractionLine> detractionlines = new ArrayList<DetractionLine>();
    try {
      OBContext.setAdminMode(true);
      List<FIN_PaymentDetail> details = paymentDetracciones.getFINPaymentDetailList();
      for (int i = 0; i < details.size(); i++) {
        FIN_PaymentDetail detail = details.get(i);
        if (detail.getFINPaymentScheduleDetailList().get(0).getInvoicePaymentSchedule() != null) {
          Invoice invoice = detail.getFINPaymentScheduleDetailList().get(0)
              .getInvoicePaymentSchedule().getInvoice();
          int pos = DetractionLine.find(detractionlines, invoice);
          if (pos == -1) {
            DetractionLine detractionline = new DetractionLine();
            detractionline.invoice = invoice;
            detractionline.amount = detail.getScoPaymentamount();

            detractionlines.add(detractionline);
          } else {
            detractionlines.get(pos).amount = detractionlines.get(pos).amount
                .add(detail.getScoPaymentamount());
          }
        }
      }
    } finally {
      OBContext.restorePreviousMode();
    }

    BigDecimal importeTotal = new BigDecimal(0);

    for (int i = 0; i < detractionlines.size(); i++) {
      DetractionLine detractionline = detractionlines.get(i);

      String tipoDocProveedor = "6";
      String rucProveedor = SunatUtil.LPad(detractionline.invoice.getBusinessPartner().getTaxID(),
          11, '0');

      if (rucProveedor.length() != 11)
        throw new OBException("@SCO_ExportDetractionInvalidBPRUC@"
            + detractionline.invoice.getBusinessPartner().getIdentifier());

      String nameProveedor = SunatUtil.LPad("", 35, ' ');
      if (nameProveedor.length() > 35)
        nameProveedor.substring(0, 35);

      String proforma = "000000000";

      String goodCode = "";
      if (detractionline.invoice.getSCODetractionAffectedCategory() == null)
        goodCode = "";
      else
        goodCode = SunatUtil
            .LPad(detractionline.invoice.getSCODetractionAffectedCategory().getCode(), 3, '0');

      if (goodCode.length() != 03)
        throw new OBException("@SCO_ExportDetractionInvalidDetracCat@"
            + detractionline.invoice.getScrPhysicalDocumentno()
            + " tiene categoria de detraccion asociada con codigo diferente a 3 digitos");

      String cuentaBN = "";
      List<BankAccount> bas = detractionline.invoice.getBusinessPartner()
          .getBusinessPartnerBankAccountList();
      boolean drtracbnkfound = false;
      for (int j = 0; j < bas.size(); j++) {
        if (bas.get(j).isScoIsdetraction()) {
          cuentaBN = SunatUtil.LPad(bas.get(j).getAccountNo(), 11, '0');
          drtracbnkfound = true;
          break;
        }
      }

      if (!drtracbnkfound) {
        throw new OBException("@SCO_ExportDetractionNoDetraccBKFound@"
            + detractionline.invoice.getBusinessPartner().getIdentifier());
      }

      cuentaBN = StringUtils.remove(cuentaBN, '-');

      if (cuentaBN.length() != 11) {
        throw new OBException("@SCO_ExportDetractionInvalidDetraccBK@"
            + detractionline.invoice.getBusinessPartner().getIdentifier());

      }

      importeTotal = importeTotal.add(detractionline.amount);
      String importe = SunatUtil.LPad(detractionline.amount.intValue() + "", 13, '0')
          + SunatUtil.LPad(detractionline.amount.remainder(BigDecimal.ONE)
              .multiply(new BigDecimal(100)).intValue() + "", 2, '0');

      String codigoOp = "01"; // fix-me
      SimpleDateFormat dt = new SimpleDateFormat("yyyyMM");
      String periodoTrib = dt.format(detractionline.invoice.getAccountingDate());

      String tipoDoc = "01";
      String doctype = detractionline.invoice.getDocumentType().getScoSpecialdoctype();
      if (doctype != null) {
        if (doctype.equals("SCOAPINVOICE") || doctype.equals("SCOAPSIMPLEPROVISIONINVOICE")
            || doctype.equals("SCOAPBOEINVOICE") || doctype.equals("SCOAPLOANINVOICE"))
          tipoDoc = detractionline.invoice.getScoPodoctypeComboitem().getCode(); // factura
      }

      StringTokenizer st = new StringTokenizer(detractionline.invoice.getScrPhysicalDocumentno(),
          "-");

      String serieInvoice = SunatUtil.LPad(st.nextToken().replaceAll("[^a-zA-Z0-9]", ""), 4, '0');

      if (serieInvoice.length() != 4) {
        throw new OBException("@SCO_ExportDetractionInvalidInvPhysicalDocno@"
            + detractionline.invoice.getScrPhysicalDocumentno());

      }

      String nroInvoice = "";

      try {
        nroInvoice = SunatUtil.LPad(st.nextToken().replaceAll("\\D+", ""), 8, '0');
      } catch (Exception ex) {
      }

      if (nroInvoice.length() != 8)
        nroInvoice.substring(0, 8);

      String line = tipoDocProveedor + rucProveedor + nameProveedor + proforma + goodCode + cuentaBN
          + importe + codigoOp + periodoTrib + tipoDoc + serieInvoice + nroInvoice;
      sb.append("\r\n" + line);
    }

    montoTotal[0] = importeTotal;

    return sb.toString();
  }
}

class DetractionLine {
  Invoice invoice;
  BigDecimal amount;

  static public int find(List<DetractionLine> detractionlines, Invoice invoice) {
    for (int i = 0; i < detractionlines.size(); i++) {
      if (detractionlines.get(i).invoice.getId().equals(invoice.getId())) {
        return i;
      }
    }

    return -1;
  }
}