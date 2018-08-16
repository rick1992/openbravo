package pe.com.unifiedgo.accounting.process;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.advpaymentmngt.process.FIN_AddPayment;
import org.openbravo.base.exception.OBException;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.enterprise.DocumentType;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_FinaccTransaction;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.model.financialmgmt.payment.FIN_Payment;
import org.openbravo.model.financialmgmt.payment.FIN_PaymentMethod;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.service.db.DalConnectionProvider;

import pe.com.unifiedgo.migration.SMG_Utils;

public class SCO_GenericMigrationProcess extends DalBaseProcess {

  static public String directorioExcels = "/opt/BorraTemp/NEW/COAM/excel/";

  static public String finacc_PEN_ID = "F46507B554EE4BAA89F26DFBBFE09D32";
  static public String finacc_USD_ID = "7FD7A47D076541AFA82D69C41D0FE57C";
  static public String finacc_EUR_ID = "43CED77B5A51497DA3FED41CADB9E3AC";

  static public String finPaymentMethodNotDefinedId = "43CED77B5A51497DA3FED41CADB9E3AC";

  static public String ArReceiptDocTypeId = "E2EA28441DCF44AB9EDB8B2A249857BE";
  static public String APPaymentDocTypeId = "E57A1F90E0514111BFD2CF5EE619E3F1";

  public void doExecute(ProcessBundle bundle) throws Exception {
    try {

      final VariablesSecureApp vars = bundle.getContext().toVars();
      final String strAction = (String) bundle.getParams().get("migaction");

      String adClientId = "9FD4BC8EEDDA412CB57595C75F208E04";
      String adOrgId = "DBEFDABBF0B343AFA7EC488E0F4DBE93";
      String adUserId = "100";
      String adRoleId = "917CFFE7AD71466CB2D0FEA7A5045616";

      OBContext.setOBContext(adUserId, adRoleId, adClientId, adOrgId);
      final ConnectionProvider conProvider = bundle.getConnection();
      final String language = bundle.getContext().getLanguage();
      User user = OBDal.getInstance().get(User.class, adUserId);

      if (strAction.equals("MAKEPAYMENTS")) {
        String csvfilepath = directorioExcels + "/" + "MigPaymentInvoice.csv";
        FileInputStream fstream = new FileInputStream(csvfilepath);
        BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

        DecimalFormat df = new DecimalFormat("#0.00");
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
        df.setDecimalFormatSymbols(otherSymbols);

        // SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date migDate = sdf.parse("01/01/2018");

        String strLine;
        int entryno = 0;
        while ((strLine = br.readLine()) != null) {
          String[] parts = strLine.split(";", -1);
          if (parts.length != 2) {
            System.out.println("linea mal formateada: " + entryno);
            throw new OBException("linea mal formateada: " + entryno);
          }

          String cInvoiceId = parts[0];
          BigDecimal paymentamt = new BigDecimal(df.parse(parts[1]).doubleValue()).setScale(2,
              RoundingMode.HALF_UP);

          Invoice inv = OBDal.getInstance().get(Invoice.class, cInvoiceId);
          if (inv == null) {
            System.out
                .println("c_invoice_id no encontrado: " + cInvoiceId + " - entryno:" + entryno);
            throw new OBException(
                "c_invoice_id no encontrado: " + cInvoiceId + " - entryno:" + entryno);
          }
          System.out.println("pagando:" + paymentamt + " a invoice:" + inv.getIdentifier());

          FIN_Payment payment = OBProvider.getInstance().get(FIN_Payment.class);
          payment.setClient(inv.getClient());
          payment.setOrganization(inv.getOrganization());
          payment.setCreationDate(migDate);
          payment.setCreatedBy(user);
          payment.setUpdated(migDate);
          payment.setUpdatedBy(user);
          payment.setActive(true);
          payment.setReceipt(false);

          payment.setBusinessPartner(inv.getBusinessPartner());

          payment.setPaymentDate(migDate);
          payment.setCurrency(inv.getCurrency());
          payment.setPaymentMethod(
              OBDal.getInstance().get(FIN_PaymentMethod.class, finPaymentMethodNotDefinedId));
          payment.setDocumentNo("MIGRACION");
          payment.setStatus("RPAP");
          payment.setProcessed(false);
          payment.setProcessNow(false);
          payment.setPosted("sco_M");

          if (inv.getCurrency().getId().equals("308"))
            payment.setAccount(OBDal.getInstance().get(FIN_FinancialAccount.class, finacc_PEN_ID));
          else if (inv.getCurrency().getId().equals("100"))
            payment.setAccount(OBDal.getInstance().get(FIN_FinancialAccount.class, finacc_USD_ID));
          else if (inv.getCurrency().getId().equals("102"))
            payment.setAccount(OBDal.getInstance().get(FIN_FinancialAccount.class, finacc_EUR_ID));
          else {
            throw new OBException("moneda no reconocida:" + inv.getCurrency().getId());
          }

          if (inv.isSalesTransaction())
            payment
                .setDocumentType(OBDal.getInstance().get(DocumentType.class, ArReceiptDocTypeId));
          else
            payment
                .setDocumentType(OBDal.getInstance().get(DocumentType.class, APPaymentDocTypeId));

          payment.setFinancialTransactionConvertRate(new BigDecimal(1));
          payment.setAPRMProcessPayment("P");
          payment.setSCOIsSimpleProvision(false);
          String description = "DOCID/" + inv.getId();
          payment.setDescription(description);

          OBDal.getInstance().save(payment);
          OBDal.getInstance().flush();

          List<Invoice> invoices = new ArrayList<Invoice>();
          List<BigDecimal> amts = new ArrayList<BigDecimal>();
          invoices.add(inv);

          BigDecimal totalamt = new BigDecimal(0);
          amts.add(paymentamt);
          totalamt = totalamt.add(paymentamt);

          payment = SMG_Utils.execSimplePaymentMultiple(vars, new DalConnectionProvider(), payment,
              invoices, amts, totalamt, false, payment.getCurrency(), new BigDecimal(1), totalamt,
              "P");

          OBDal.getInstance().flush();
          payment = OBDal.getInstance().get(FIN_Payment.class, payment.getId());
          List<FIN_FinaccTransaction> finaccs = payment.getFINFinaccTransactionList();
          for (int i = 0; i < finaccs.size(); i++) {
            SMG_Utils.deleteTransaction(vars, conProvider, finaccs.get(i));
          }
          finaccs.clear();
          OBDal.getInstance().flush();

          payment = OBDal.getInstance().get(FIN_Payment.class, payment.getId());
          if (!payment.getStatus().equals("PPM") && !payment.getStatus().equals("RPR")) {
            throw new OBException("error pagando invoice: " + inv.getIdentifier()
                + " - pago en estado:" + payment.getStatus());
          }
          payment.setStatus("RPPC");
          OBDal.getInstance().save(payment);
          OBDal.getInstance().flush();
          entryno++;
        }
      } else if (strAction.equals("REMOVEPAYMENTS")) {
        OBCriteria<FIN_Payment> obcPSD = OBDal.getInstance().createCriteria(FIN_Payment.class);
        obcPSD.add(Restrictions.eq(FIN_Payment.PROPERTY_DOCUMENTNO, "MIGRACION"));
        obcPSD.add(Restrictions.eq(FIN_Payment.PROPERTY_POSTED, "sco_M"));
        obcPSD.add(Restrictions.eq(FIN_Payment.PROPERTY_STATUS, "RPPC"));

        List<FIN_Payment> payments = obcPSD.list();
        for (int i = 0; i < payments.size(); i++) {
          FIN_Payment payment = payments.get(i);
          System.out.println("Eliminando pago:" + payment.getId() + " - " + payment.getIdentifier()
              + " - " + payment.getDescription());
          OBError message = FIN_AddPayment.processPayment(vars, new DalConnectionProvider(), "R",
              payment);
          if (message.getType().equals("Error")) {
            throw new OBException(
                "Error eliminando pago:" + payment.getId() + " - " + payment.getIdentifier() + " - "
                    + payment.getDescription() + " - " + message.getMessage());
          }
          payment = OBDal.getInstance().get(FIN_Payment.class, payment.getId());
          if (payment.getStatus().compareTo("RPAP") != 0) {
            throw new OBException("Error eliminando pago2:" + payment.getId() + " - "
                + payment.getIdentifier() + " - " + payment.getDescription());
          }
          OBDal.getInstance().remove(payment);
          OBDal.getInstance().flush();
        }

      } else {
        throw new OBException("Accion no reconocida.");
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
}