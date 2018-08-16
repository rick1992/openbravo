package pe.com.unifiedgo.accounting.process;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.access.User;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.service.db.DbUtility;

import pe.com.unifiedgo.accounting.data.SCOInternalDoc;

class MigracionMaestras {

  public String docTypeFactura = "";
  public String docTypeFacturaCompras = "";
  public String docTypeNotaAbonoCompras = "";
  public String docTypeLetraCompras = "";
  public String docTypeBoleta = "";
  public String docTypeDocInterno = "";
  public String docTypeFacturaxLetraMngmt = "";
  public String docTypeLetraxLetraMngmt = "";
  public String docTypeLetra = "";
  public String docTypeNotaCargo = "";
  public String docTypeNotaAbono = "";
  public String docTypeEntregasRendir = "";
  public String docTypeGlJournal = "";
  public String pmEfectivo = "";
  public String pmDeposito = "";
  public String pmCheque = "";
  public String pmLetra = "";
  public String pmSinDefinir = "";
  public String pmCredito = "";
  public String pmCargoEnCuenta = "";
  public String pmDepositoEnCuenta = "";
  public String comboIGVCreditoFiscal = "";
  public String comboIGVNoCreditoFiscal = "";
  public String comboExentoInafecto = "";
  public String comboExentoInafectoVentas = "";
  public String comboExentoOtrosTributos = "";
  public String taxIGV = "";
  public String taxPercepcionCredito = "";
  public String taxPercepcionContado = "";
  public String taxExento = "";
  public String cpFechaFija = "";

  public String pricelistIdSO = "";
  public String pricelistIdPO = "";
  public String prefix = "";

  public boolean loadPreliminares(ConnectionProvider conn, String client, String orgParent)
      throws Exception {
    boolean returnvalue = true;

    try {

      // COAM
      if (orgParent.equals("DBEFDABBF0B343AFA7EC488E0F4DBE93")) {
        pricelistIdSO = "DF_PLDS_SO_COAM";
        pricelistIdPO = "DF_PLDS_PO_COAM";
        prefix = "CM";
      } else {
        returnvalue = false;
      }

      Statement statementOB = conn.getStatement();
      ;
      ResultSet rsOB = statementOB
          .executeQuery("SELECT c_doctype_id from c_doctype WHERE ad_org_id='" + orgParent
              + "' AND em_sco_specialdoctype='SCOARINVOICE'");
      if (rsOB.next()) {
        docTypeFactura = rsOB.getString("c_doctype_id");

      } else {
        System.out.println("Tipo de documento no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB.executeQuery("SELECT c_doctype_id from c_doctype WHERE ad_org_id='"
          + orgParent + "' AND em_sco_specialdoctype='SCOAPINVOICE'");
      if (rsOB.next()) {
        docTypeFacturaCompras = rsOB.getString("c_doctype_id");

      } else {
        System.out.println("Tipo de documento no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB.executeQuery("SELECT c_doctype_id from c_doctype WHERE ad_org_id='"
          + orgParent + "' AND em_sco_specialdoctype='SCOAPCREDITMEMO'");
      if (rsOB.next()) {
        docTypeNotaAbonoCompras = rsOB.getString("c_doctype_id");

      } else {
        System.out.println("Tipo de documento no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB.executeQuery("SELECT c_doctype_id from c_doctype WHERE ad_org_id='"
          + orgParent + "' AND em_sco_specialdoctype='SCOAPBOEINVOICE'");
      if (rsOB.next()) {
        docTypeLetraCompras = rsOB.getString("c_doctype_id");

      } else {
        System.out.println("Tipo de documento no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB.executeQuery("SELECT c_doctype_id from c_doctype WHERE ad_org_id='"
          + orgParent + "' AND em_sco_specialdoctype='SCOARTICKET'");
      if (rsOB.next()) {
        docTypeBoleta = rsOB.getString("c_doctype_id");

      } else {
        System.out.println("Tipo de documento no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB.executeQuery("SELECT c_doctype_id from c_doctype WHERE ad_org_id='"
          + orgParent + "' AND em_sco_specialdoctype='SCOINTERNALDOCUMENT'");
      if (rsOB.next()) {
        docTypeDocInterno = rsOB.getString("c_doctype_id");

      } else {
        System.out.println("Tipo de documento no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB.executeQuery("SELECT c_doctype_id from c_doctype WHERE ad_org_id='"
          + orgParent + "' AND em_sco_specialdoctype='SCOINVOICEXBOE'");
      if (rsOB.next()) {
        docTypeFacturaxLetraMngmt = rsOB.getString("c_doctype_id");

      } else {
        System.out.println("Tipo de documento no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB.executeQuery("SELECT c_doctype_id from c_doctype WHERE ad_org_id='"
          + orgParent + "' AND em_sco_specialdoctype='SCOBOEXBOE'");
      if (rsOB.next()) {
        docTypeLetraxLetraMngmt = rsOB.getString("c_doctype_id");

      } else {
        System.out.println("Tipo de documento no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB.executeQuery("SELECT c_doctype_id from c_doctype WHERE ad_org_id='"
          + orgParent + "' AND em_sco_specialdoctype='SCOARBOEINVOICE'");
      if (rsOB.next()) {
        docTypeLetra = rsOB.getString("c_doctype_id");

      } else {
        System.out.println("Tipo de documento no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB.executeQuery("SELECT c_doctype_id from c_doctype WHERE ad_org_id='"
          + orgParent + "' AND em_sco_specialdoctype='SCOARDEBITMEMO'");
      if (rsOB.next()) {
        docTypeNotaCargo = rsOB.getString("c_doctype_id");

      } else {
        System.out.println("Tipo de documento no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB.executeQuery("SELECT c_doctype_id from c_doctype WHERE ad_org_id='"
          + orgParent + "' AND em_sco_specialdoctype='SCOARCREDITMEMO'");
      if (rsOB.next()) {
        docTypeNotaAbono = rsOB.getString("c_doctype_id");

      } else {
        System.out.println("Tipo de documento no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB.executeQuery("SELECT c_doctype_id from c_doctype WHERE ad_org_id='"
          + orgParent + "' AND em_sco_specialdoctype='SCOACCOUNTABILITY'");
      if (rsOB.next()) {
        docTypeEntregasRendir = rsOB.getString("c_doctype_id");

      } else {
        System.out.println("Tipo de documento no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB.executeQuery("SELECT c_doctype_id from c_doctype WHERE ad_org_id='"
          + orgParent + "' AND em_sco_specialdoctype='SCOGLJOURNAL'");
      if (rsOB.next()) {
        docTypeGlJournal = rsOB.getString("c_doctype_id");

      } else {
        System.out.println("Tipo de documento no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      // Payment methods
      statementOB = conn.getStatement();
      ;
      rsOB = statementOB
          .executeQuery("SELECT fin_paymentmethod_id from fin_paymentmethod WHERE ad_client_id='"
              + client + "' AND em_sco_specialmethod='SCOCASH'");
      if (rsOB.next()) {
        pmEfectivo = rsOB.getString("fin_paymentmethod_id");

      } else {
        System.out.println("Metodo de pago no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB
          .executeQuery("SELECT fin_paymentmethod_id from fin_paymentmethod WHERE ad_client_id='"
              + client + "' AND em_sco_specialmethod='SCOWIRETRANSFER'");
      if (rsOB.next()) {
        pmDeposito = rsOB.getString("fin_paymentmethod_id");

      } else {
        System.out.println("Metodo de pago no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB
          .executeQuery("SELECT fin_paymentmethod_id from fin_paymentmethod WHERE ad_client_id='"
              + client + "' AND em_sco_specialmethod='SCOCHECK'");
      if (rsOB.next()) {
        pmCheque = rsOB.getString("fin_paymentmethod_id");

      } else {
        System.out.println("Metodo de pago no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB
          .executeQuery("SELECT fin_paymentmethod_id from fin_paymentmethod WHERE ad_client_id='"
              + client + "' AND em_sco_specialmethod='SCOBILLOFEXCHANGE'");
      if (rsOB.next()) {
        pmLetra = rsOB.getString("fin_paymentmethod_id");

      } else {
        System.out.println("Metodo de pago no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB
          .executeQuery("SELECT fin_paymentmethod_id from fin_paymentmethod WHERE ad_client_id='"
              + client + "' AND em_sco_specialmethod='SCONOTDEFINED'");
      if (rsOB.next()) {
        pmSinDefinir = rsOB.getString("fin_paymentmethod_id");

      } else {
        System.out.println("Metodo de pago no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB
          .executeQuery("SELECT fin_paymentmethod_id from fin_paymentmethod WHERE ad_client_id='"
              + client + "' AND em_sco_specialmethod='SCOCREDIT'");
      if (rsOB.next()) {
        pmCredito = rsOB.getString("fin_paymentmethod_id");

      } else {
        System.out.println("Metodo de pago no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB
          .executeQuery("SELECT fin_paymentmethod_id from fin_paymentmethod WHERE ad_client_id='"
              + client + "' AND fin_paymentmethod_id='A53841AA60DA471697C6A8EDABEE184D'");
      if (rsOB.next()) {
        pmCargoEnCuenta = rsOB.getString("fin_paymentmethod_id");

      } else {
        System.out.println("Metodo de pago no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB
          .executeQuery("SELECT fin_paymentmethod_id from fin_paymentmethod WHERE ad_client_id='"
              + client + "' AND fin_paymentmethod_id='E0D848C98832499B9B9B0802B71CA25E'");
      if (rsOB.next()) {
        pmDepositoEnCuenta = rsOB.getString("fin_paymentmethod_id");

      } else {
        System.out.println("Metodo de pago no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB.executeQuery(
          "SELECT scr_combo_item_id from scr_combo_item WHERE value='PCTIGV-Oper100-grab-export'");
      if (rsOB.next()) {
        comboIGVCreditoFiscal = rsOB.getString("scr_combo_item_id");

      } else {
        System.out.println("Tipo de combo no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB.executeQuery(
          "SELECT scr_combo_item_id from scr_combo_item WHERE value='PCTIGV-Oper-NoGrab'");
      if (rsOB.next()) {
        comboIGVNoCreditoFiscal = rsOB.getString("scr_combo_item_id");

      } else {
        System.out.println("Tipo de combo no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB.executeQuery(
          "SELECT scr_combo_item_id from scr_combo_item WHERE value='PCTE-Inafecto-porDefecto'");
      if (rsOB.next()) {
        comboExentoInafecto = rsOB.getString("scr_combo_item_id");

      } else {
        System.out.println("Tipo de combo no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB.executeQuery(
          "SELECT scr_combo_item_id from scr_combo_item WHERE value='PVTE-Inafecto-porDefecto'");
      if (rsOB.next()) {
        comboExentoInafectoVentas = rsOB.getString("scr_combo_item_id");

      } else {
        System.out.println("Tipo de combo no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB.executeQuery(
          "SELECT scr_combo_item_id from scr_combo_item WHERE value='PCTE-OtrosTributos-Cargos'");
      if (rsOB.next()) {
        comboExentoOtrosTributos = rsOB.getString("scr_combo_item_id");

      } else {
        System.out.println("Tipo de combo no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      // TAXES
      statementOB = conn.getStatement();
      ;
      rsOB = statementOB
          .executeQuery("select c_tax_id from c_tax where em_sco_specialtax = 'SCOIGV'");
      if (rsOB.next()) {
        taxIGV = rsOB.getString("c_tax_id");

      } else {
        System.out.println("IGV no encontrado.");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB.executeQuery(
          "select c_tax_id from c_tax where em_sco_specialtax = 'SCOSCREDITPERCEPTION'");
      if (rsOB.next()) {
        taxPercepcionCredito = rsOB.getString("c_tax_id");

      } else {
        System.out.println("IGV no encontrado.");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB.executeQuery(
          "select c_tax_id from c_tax where em_sco_specialtax = 'SCOSINMEDIATEPERCEPTION'");
      if (rsOB.next()) {
        taxPercepcionContado = rsOB.getString("c_tax_id");

      } else {
        System.out.println("IGV no encontrado.");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      ;
      rsOB = statementOB
          .executeQuery("select c_tax_id from c_tax where em_sco_specialtax = 'SCOEXEMPT'");
      if (rsOB.next()) {
        taxExento = rsOB.getString("c_tax_id");

      } else {
        System.out.println("Exentos no encontrado.");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

      statementOB = conn.getStatement();
      rsOB = statementOB
          .executeQuery("SELECT c_paymentterm_id from c_paymentterm WHERE ad_client_id='" + client
              + "' AND em_sco_specialpayterm='SCOFIXEDDUEDATE'");
      if (rsOB.next()) {
        cpFechaFija = rsOB.getString("c_paymentterm_id");

      } else {
        System.out.println("Condicion de pago no encontrado");
        returnvalue = false;
      }

      statementOB.close();
      rsOB.close();

    } catch (SQLException e) {
      e.printStackTrace();
    }

    return returnvalue;

  }
}

public class SCO_GenerateMigInv extends DalBaseProcess {

  public void doExecute(ProcessBundle bundle) throws Exception {

    final ConnectionProvider conProvider = bundle.getConnection();
    final VariablesSecureApp vars = bundle.getContext().toVars();
    try {

      /*
       * Set<String> params = bundle.getParams().keySet(); for (int i = 0; i < params.size(); i++) {
       * System.out.println(params.toArray()[i]); }
       */

      final String SCO_Internal_Doc_ID = (String) bundle.getParams().get("SCO_Internal_Doc_ID");
      final String strIssotrx = (String) bundle.getParams().get("issotrx");

      OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
      final String language = bundle.getContext().getLanguage();
      User user = OBDal.getInstance().get(User.class, vars.getUser());

      SCOInternalDoc intdoc = OBDal.getInstance().get(SCOInternalDoc.class, SCO_Internal_Doc_ID);
      if (intdoc == null) {
        throw new OBException("@SCO_InternalError@");
      }

      if (intdoc.getDocumentStatus().compareTo("CO") != 0 || !intdoc.isMigrated()) {
        throw new OBException("@ActionNotAllowedHere@");
      }

      // read default values
      MigracionMaestras mm = new MigracionMaestras();
      boolean success = mm.loadPreliminares(conProvider, intdoc.getClient().getId(),
          OBContext.getOBContext().getOrganizationStructureProvider()
              .getLegalEntity(intdoc.getOrganization()).getId());

      if (!success) {
        throw new OBException("Error cargando datos iniciales");
      }

      String client = intdoc.getClient().getId();
      Timestamp timestampNow = new Timestamp(new Date().getTime());
      String createdUid = user.getId();
      Connection obConn = conProvider.getConnection();
      obConn.setAutoCommit(false);
      String glitemPrefix = mm.prefix + "-";
      String org = intdoc.getOrganization().getId();

      String selectTableSQL = "SELECT int.sco_internal_doc_id, int.ad_org_id, int.documentno, int.podoctype_comboitem_id, int.physical_documentno, int.dateinvoiced, int.c_bpartner_id, int.description, int.c_currency_id, int.migrated_doctype_cmbitem_id, cmbi.value as podtcmb_value, cmbi2.value as mgdtcmb_value FROM sco_internal_doc int "
          + "INNER JOIN  scr_combo_item cmbi ON int.podoctype_comboitem_id=cmbi.scr_combo_item_id INNER JOIN scr_combo_item cmbi2 ON int.migrated_doctype_cmbitem_id = cmbi2.scr_combo_item_id WHERE int.ad_client_id='"
          + client + "' AND int.sco_internal_doc_id = '" + intdoc.getId() + "';";

      Statement statement = conProvider.getStatement();
      ResultSet rs = statement.executeQuery(selectTableSQL);
      if (!rs.next()) {
        throw new OBException("INTERNALERROR");
      }

      if (strIssotrx.equals("Y")) {

        String stm = "SELECT MAX(acctvalue) as acctvalue, SUM(amtsourcedr-amtsourcecr) AS saldo FROM fact_acct WHERE ad_client_id = '"
            + client + "' AND em_sco_record3_id = '" + rs.getString("sco_internal_doc_id")
            + "' AND em_sco_ismigrated='Y' AND c_currency_id = '" + rs.getString("c_currency_id")
            + "' AND (acctvalue LIKE '12%' OR acctvalue LIKE '13%' OR acctvalue LIKE '14%' OR acctvalue LIKE '15%' OR acctvalue LIKE '16%' OR acctvalue LIKE '17%')";
        double saldo = 0;
        String acctvalue = "";
        Statement statementOB = obConn.createStatement();
        ResultSet rsOB = statementOB.executeQuery(stm);
        if (rsOB.next()) {
          saldo = rsOB.getDouble("saldo");
          acctvalue = rsOB.getString("acctvalue");

          if (rsOB.next()) {
            rsOB.close();
            statementOB.close();
            throw new OBException("el documento interno:" + rs.getString("physical_documentno")
                + " tiene diferentes 1%");
          }
        }

        rsOB.close();
        statementOB.close();

        String podoctype = "FV";
        String docType = mm.docTypeFactura;
        String podtcmb_value = rs.getString("podtcmb_value");
        String mgdtcmb_value = rs.getString("mgdtcmb_value");
        if (podtcmb_value.equals("tabla10_03")) {
          podoctype = "BV";
          docType = mm.docTypeBoleta;
        } else if (podtcmb_value.equals("tabla10_07") || podtcmb_value.equals("tabla10_87")
            || podtcmb_value.equals("tabla10_97")) {
          podoctype = "NC";
          docType = mm.docTypeNotaAbono;
        } else if (podtcmb_value.equals("tabla10_08") || podtcmb_value.equals("tabla10_88")
            || podtcmb_value.equals("tabla10_98")) {
          podoctype = "ND";
          docType = mm.docTypeNotaCargo;
        } else if (podtcmb_value.equals("tabla10_00")
            && (mgdtcmb_value.equals("migraciontipodoc_LE")
                || mgdtcmb_value.equals("migraciontipodoc_22"))) {
          podoctype = "LC";
          docType = mm.docTypeLetra;
        }

        if (podoctype.equals("NC")) {
          saldo = 0 - saldo;
        }

        // buscar si ya existe este documento
        double grandtotal = 0;
        String pstm = "select c_invoice_id,grandtotal from c_invoice where ad_client_id = '"
            + client + "' AND ad_org_id = '" + rs.getString("ad_org_id") + "' "
            + "AND c_bpartner_id = '" + rs.getString("c_bpartner_id") + "' AND c_currency_id = '"
            + rs.getString("c_currency_id") + "' AND COALESCE(em_scr_physical_documentno,'') = '"
            + rs.getString("physical_documentno") + "' AND em_sco_migrated_dt_cmbitem_id='"
            + rs.getString("migrated_doctype_cmbitem_id") + "' AND dateinvoiced=? LIMIT 1";
        PreparedStatement pstatementOB = obConn.prepareStatement(pstm);
        pstatementOB.setTimestamp(1, rs.getTimestamp("dateinvoiced"));
        rsOB = pstatementOB.executeQuery();
        if (rsOB.next()) {
          grandtotal = rsOB.getDouble("grandtotal");
          rsOB.close();
          pstatementOB.close();
          throw new OBException("Documento de venta ya existe!!!:"
              + rs.getString("physical_documentno") + " - bpartner:" + rs.getString("c_bpartner_id")
              + " - grandtotal:" + grandtotal + " - saldo:" + saldo + " - SALDOCHANGE:"
              + (grandtotal != saldo ? "YES" : "NO") + " - DIFF:" + (grandtotal - saldo));
        }

        rsOB.close();
        pstatementOB.close();

        if (saldo < 0) {
          throw new OBException("saldo es negativo");
        }

        System.out.println("Insertando documento de venta:" + rs.getString("physical_documentno")
            + " con saldo: " + saldo);

        String glitempk = glitemPrefix + acctvalue;

        String cinvoiceId = java.util.UUID.randomUUID().toString().replaceAll("-", "");
        PreparedStatement pst = null;
        stm = "INSERT INTO c_invoice ("
            + "c_invoice_id, ad_client_id, ad_org_id, isactive, created ,createdby ,updated ,updatedby, "
            + "issotrx, documentno , docaction ,docstatus ,posted ,processing ,processed ,c_doctype_id, c_doctypetarget_id ,description, isprinted, salesrep_id, dateinvoiced, dateprinted, dateacct, "
            + "c_bpartner_id, c_bpartner_location_id, poreference, c_currency_id, paymentrule, c_paymentterm_id, c_charge_id, m_pricelist_id, withholdingamount, taxdate, "
            + "lastcalculatedondate, fin_paymentmethod_id, finalsettlement, daysoutstanding, percentageoverdue, c_costcenter_id, calculate_promotions, a_asset_id,"
            + "em_aprm_processinvoice, em_sco_invoiceref_id, em_sco_doctyperef_id, em_sco_aval_id, em_sco_duatype, em_sco_duaexpensetype, em_sco_duaexpensemode, em_sco_podoctype_comboitem_id,"
            + "em_sco_dua_id, em_sco_isimportation, em_sco_issimpleprovision, em_sco_skipreeval, em_sco_boe_to_id, em_sco_boe_type, em_ssa_isboerefinancing, em_sco_percepcionagent,"
            + "em_sco_specialdoctype, em_sco_diverse_acc_glitem_id, em_sco_detraction_percent, em_sco_voidmotive_cmbitem_id, em_sco_codtabla5_cmb_item_id, em_sco_newdateinvoiced,"
            + "em_sco_purchaseinvoicetype, em_sco_recalpaysched_proc, em_sco_boe_id, em_sco_specialmethod, em_scr_physical_documentno, em_scr_taxid, em_scr_datereception,"
            + "em_scr_fin_finaccount_id, em_sim_imp_costing_id, em_ssa_is_consignment, em_sco_isforfree, em_sco_ismanualref, em_sco_manualinvoiceref, em_sco_manualinvoicerefdate, em_sco_fixedduedate,em_sco_ismigrated,em_sco_migrated_dt_cmbitem_id"
            + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        // 84
        pst = obConn.prepareStatement(stm);
        pst.setString(1, cinvoiceId);
        pst.setString(2, client);
        pst.setString(3, rs.getString("ad_org_id"));

        pst.setString(4, "Y");

        pst.setTimestamp(5, timestampNow);
        pst.setString(6, createdUid);
        pst.setTimestamp(7, timestampNow);
        pst.setString(8, createdUid);

        pst.setString(9, "Y");// issotrx

        pst.setString(10, rs.getString("documentno"));// documentno
        pst.setString(11, "CO");// docaction
        pst.setString(12, "DR");// docstatus
        pst.setString(13, "sco_M");
        pst.setString(14, "N");
        pst.setString(15, "N");

        pst.setString(16, docType);
        pst.setString(17, docType);

        pst.setString(18, "");
        pst.setString(19, "N");// ya impreso

        pst.setString(20, "100");// SalesRep

        pst.setTimestamp(21, rs.getTimestamp("dateinvoiced"));// date reception
        pst.setTimestamp(22, null);
        pst.setTimestamp(23, rs.getTimestamp("dateinvoiced"));// dateacct

        pst.setString(24, rs.getString("c_bpartner_id"));

        // LOCATION
        statementOB = obConn.createStatement();
        ;
        rsOB = statementOB.executeQuery(
            "SELECT bl.c_bpartner_location_id from c_location l INNER JOIN c_bpartner_location bl ON l.c_location_id=bl.c_location_id  WHERE bl.c_bpartner_id='"
                + rs.getString("c_bpartner_id") + "'");

        String c_location_id = "";
        if (rsOB.next()) {
          c_location_id = rsOB.getString("c_bpartner_location_id");

        } else {
          statementOB.close();
          rsOB.close();

          // buscar el null location
          statementOB = obConn.createStatement();
          ;
          rsOB = statementOB.executeQuery(
              "SELECT bl.c_bpartner_location_id from c_location l INNER JOIN c_bpartner_location bl ON l.c_location_id=bl.c_location_id  WHERE bl.ad_client_id='"
                  + client + "' AND bl.c_bpartner_id='" + rs.getString("c_bpartner_id")
                  + "' AND  bl.em_sco_nulllocation = 'Y' LIMIT 1");
          if (rsOB.next()) {
            c_location_id = rsOB.getString("c_bpartner_location_id");
            statementOB.close();
            rsOB.close();
          } else {
            statementOB.close();
            rsOB.close();
            throw new OBException("Location NO existe! " + rs.getString("c_bpartner_id"));
          }

        }

        statementOB.close();
        rsOB.close();

        pst.setString(25, c_location_id);
        pst.setString(26, "");
        pst.setString(27, rs.getString("c_currency_id"));
        pst.setString(28, "P");
        pst.setString(29, mm.cpFechaFija);
        pst.setString(30, null);

        String moneda_abr = "PEN";
        if (rs.getString("c_currency_id").equals("100")) {
          moneda_abr = "USD";
        }

        pst.setString(31, mm.pricelistIdSO + moneda_abr);

        pst.setInt(32, 0);// withholding
        pst.setTimestamp(33, null);// taxdate

        pst.setTimestamp(34, null);
        pst.setString(35, mm.pmSinDefinir); // fin_paymentmethod_id

        pst.setTimestamp(36, null);
        pst.setInt(37, 0);

        pst.setInt(38, 0);
        pst.setString(39, null);

        pst.setString(40, null);
        pst.setString(41, null);

        pst.setString(42, "CO");// em_aprm_processinvoice
        pst.setString(43, null);// em_sco_invoiceref_id
        pst.setString(44, null);// em_sco_doctyperef_id

        pst.setString(45, null);
        pst.setString(46, null);// em_sco_duatype
        pst.setString(47, null);// em_sco_duaexpensetype
        pst.setString(48, null);// duaexpensemode
        pst.setString(49, rs.getString("podoctype_comboitem_id"));
        pst.setString(50, null);// em_sco_dua_id
        pst.setString(51, "N");

        pst.setString(52, "N");// simple provision
        pst.setString(53, "Y");

        pst.setString(54, null);
        pst.setString(55, "SCO_POR");

        pst.setString(56, "N");

        pst.setString(57, "N");
        pst.setString(58, null);
        pst.setString(59, glitempk);// em_sco_diverse_acc_glitem_id

        pst.setInt(60, 0);
        pst.setString(61, null);

        pst.setString(62, null);
        pst.setTimestamp(63, rs.getTimestamp("dateinvoiced"));
        pst.setString(64, "SCO_PURNA");

        pst.setString(65, null);
        pst.setString(66, null);
        pst.setString(67, null);
        pst.setString(68, rs.getString("physical_documentno"));
        pst.setString(69, null);

        pst.setTimestamp(70, rs.getTimestamp("dateinvoiced"));
        pst.setString(71, null);
        pst.setString(72, null);
        pst.setString(73, "N");
        pst.setString(74, "N");// em_sco_isforfree

        pst.setString(75, "Y");
        pst.setString(76, rs.getString("physical_documentno"));
        pst.setTimestamp(77, rs.getTimestamp("dateinvoiced"));

        pst.setTimestamp(78, rs.getTimestamp("dateinvoiced"));

        pst.setString(79, "Y");
        pst.setString(80, rs.getString("migrated_doctype_cmbitem_id"));

        // System.out.println(pst);

        pst.executeUpdate();
        pst.close();

        // insertar linea unica con importe
        pst = null;
        stm = "INSERT INTO c_invoiceline ("
            + "c_invoiceline_id, ad_client_id, ad_org_id, isactive, created, createdby, updated, updatedby,"
            + "line, description, account_id, m_product_id, qtyinvoiced, pricelist, priceactual,"
            + "pricelimit, linenetamt, c_charge_id, c_uom_id, c_tax_id, s_resourceassignment_id, taxamt, m_attributesetinstance_id, quantityorder, m_product_uom_id, c_invoice_discount_id, "
            + "c_projectline_id, m_offer_id, pricestd, excludeforwithholding, taxbaseamt, c_bpartner_id, periodnumber, a_asset_id, defplantype, c_project_id, "
            + "c_period_id, c_costcenter_id, user1_id, user2_id, bom_parent_id, em_sco_perceptiontaxed, em_sco_creditused, em_sco_invoiceline_prepay_id, "
            + "em_sco_specialdoctype, em_sco_auto_acc_glitem_id, em_scr_specialtax, em_scr_combo_item_id"
            + ",c_invoice_id, financial_invoice_line, em_sco_import_expenses_id, em_sco_prorrateo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?);";
        // 53
        String invoicelineId = java.util.UUID.randomUUID().toString().replaceAll("-", "");

        pst = obConn.prepareStatement(stm);
        pst.setString(1, invoicelineId);
        pst.setString(2, client);
        pst.setString(3, org);

        pst.setString(4, "Y");

        pst.setTimestamp(5, timestampNow);
        pst.setString(6, createdUid);
        pst.setTimestamp(7, timestampNow);
        pst.setString(8, createdUid);

        pst.setInt(9, 10);
        pst.setString(10, "");// description
        pst.setString(11, glitempk);

        pst.setString(12, null);
        pst.setDouble(13, 1);
        pst.setDouble(14, 0);
        pst.setDouble(15, saldo);
        pst.setDouble(16, 0.00);
        pst.setDouble(17, saldo);
        pst.setString(18, null);

        pst.setString(19, "100");
        pst.setString(20, mm.taxExento);
        pst.setDouble(22, 0);
        pst.setString(47, "SCOEXEMPT");
        pst.setString(48, mm.comboExentoOtrosTributos);

        pst.setString(21, null);

        pst.setString(23, null);
        pst.setObject(24, null);
        pst.setObject(25, null);
        pst.setString(26, null);
        pst.setString(27, null);
        pst.setString(28, null);
        pst.setDouble(29, saldo);// pricestd
        pst.setString(30, "N");

        pst.setDouble(31, saldo);
        pst.setString(32, rs.getString("c_bpartner_id"));
        pst.setInt(33, 0);
        pst.setString(34, null);
        pst.setString(35, null);
        pst.setString(36, null);
        pst.setString(37, null);
        pst.setString(38, null);
        pst.setString(39, null);
        pst.setString(40, null);
        pst.setString(41, null);

        pst.setString(42, "N");
        pst.setDouble(43, 0);// em_sco_creditused
        pst.setString(44, null);
        pst.setString(45, null);
        pst.setString(46, null);

        pst.setString(49, cinvoiceId);
        pst.setString(50, "Y");// finantial invoice
        pst.setString(51, null);
        pst.setString(52, "Y");// em_sco_prorrateo

        pst.executeUpdate();
        pst.close();

        pst = null;
        stm = "SELECT c_invoice_post(null, ?);";
        pst = obConn.prepareStatement(stm);
        pst.setString(1, cinvoiceId);
        pst.executeQuery();
        pst.close();

        obConn.commit();
      } else {
        String stm = "SELECT MAX(acctvalue) as acctvalue, SUM(amtsourcecr-amtsourcedr) AS saldo FROM fact_acct WHERE ad_client_id = '"
            + client + "' AND em_sco_record3_id = '" + rs.getString("sco_internal_doc_id")
            + "' AND em_sco_ismigrated='Y' AND c_currency_id = '" + rs.getString("c_currency_id")
            + "' AND (acctvalue LIKE '42%' OR acctvalue LIKE '43%' OR acctvalue LIKE '44%' OR acctvalue LIKE '45%' OR acctvalue LIKE '46%' OR acctvalue LIKE '47%')";
        double saldo = 0;
        String acctvalue = "";
        Statement statementOB = obConn.createStatement();
        ResultSet rsOB = statementOB.executeQuery(stm);
        if (rsOB.next()) {
          saldo = rsOB.getDouble("saldo");
          acctvalue = rsOB.getString("acctvalue");

          if (rsOB.next()) {
            ;
            rsOB.close();
            statementOB.close();
            throw new OBException("el documento interno:" + rs.getString("physical_documentno")
                + " tiene diferentes 4%");
          }
        }

        rsOB.close();
        statementOB.close();

        String podoctype = "FV";
        String docType = mm.docTypeFacturaCompras;
        String podtcmb_value = rs.getString("podtcmb_value");
        String mgdtcmb_value = rs.getString("mgdtcmb_value");
        if (podtcmb_value.equals("tabla10_03")) {
          podoctype = "BV";
          docType = mm.docTypeFacturaCompras;
        } else if (podtcmb_value.equals("tabla10_07") || podtcmb_value.equals("tabla10_87")
            || podtcmb_value.equals("tabla10_97")) {
          podoctype = "NC";
          docType = mm.docTypeNotaAbonoCompras;
        } else if (podtcmb_value.equals("tabla10_08") || podtcmb_value.equals("tabla10_88")
            || podtcmb_value.equals("tabla10_98")) {
          podoctype = "ND";
          docType = mm.docTypeFacturaCompras;
        } else if (podtcmb_value.equals("tabla10_00")
            && (mgdtcmb_value.equals("migraciontipodoc_LE")
                || mgdtcmb_value.equals("migraciontipodoc_22"))) {
          podoctype = "LC";
          docType = mm.docTypeLetraCompras;
        }

        if (podoctype.equals("NC")) {
          saldo = 0 - saldo;
        }

        // buscar si ya existe este documento
        double grandtotal = 0;
        String pstm = "select c_invoice_id,grandtotal from c_invoice where ad_client_id = '"
            + client + "' AND ad_org_id = '" + rs.getString("ad_org_id") + "' "
            + "AND c_bpartner_id = '" + rs.getString("c_bpartner_id") + "' AND c_currency_id = '"
            + rs.getString("c_currency_id") + "' AND COALESCE(em_scr_physical_documentno,'') = '"
            + rs.getString("physical_documentno") + "' AND em_sco_migrated_dt_cmbitem_id='"
            + rs.getString("migrated_doctype_cmbitem_id") + "' AND dateinvoiced=? LIMIT 1";
        PreparedStatement pstatementOB = obConn.prepareStatement(pstm);
        pstatementOB.setTimestamp(1, rs.getTimestamp("dateinvoiced"));
        rsOB = pstatementOB.executeQuery();
        if (rsOB.next()) {
          grandtotal = rsOB.getDouble("grandtotal");
          rsOB.close();
          pstatementOB.close();
          throw new OBException("Documento de compra ya existe!!!:"
              + rs.getString("physical_documentno") + " - bpartner:" + rs.getString("c_bpartner_id")
              + " - grandtotal:" + grandtotal + " - saldo:" + saldo + " - SALDOCHANGE:"
              + (grandtotal != saldo ? "YES" : "NO") + " - DIFF:" + (grandtotal - saldo));
        }

        rsOB.close();
        pstatementOB.close();

        if (podoctype.equals("FV")
            && (acctvalue.startsWith("4221") || acctvalue.startsWith("4321"))) {
          // Anticipo. el saldo debe ser negativo.
          if (saldo > 0) {
            throw new OBException("saldo es positivo en un anticipo");
          }
        } else {
          if (saldo < 0) {
            throw new OBException("saldo es negativo");
          }
        }

        // averiguar totallines, taxamt para impuestos diferidos
        pstm = "SELECT fact_acct_group_id  FROM fact_acct WHERE ad_client_id = '" + client
            + "' AND em_sco_record3_id = '" + rs.getString("sco_internal_doc_id")
            + "' AND acctvalue='3791' ORDER BY dateacct ASC LIMIT 1;";

        String provfactAcctGroupId = "";
        pstatementOB = obConn.prepareStatement(pstm);
        rsOB = pstatementOB.executeQuery();
        if (rsOB.next()) {
          provfactAcctGroupId = rsOB.getString("fact_acct_group_id");
        }
        rsOB.close();
        pstatementOB.close();

        double mgrTaxamt = 0;
        double mgrGrandTotal = 0;
        if (!provfactAcctGroupId.isEmpty()) {
          // se encontro asiento impuesto diferido
          pstm = "SELECT sum(amtsourcedr - amtsourcecr) as taxamt FROM fact_acct WHERE ad_client_id = '"
              + client + "'" + " AND em_sco_record3_id = '" + rs.getString("sco_internal_doc_id")
              + "' AND fact_acct_group_id='" + provfactAcctGroupId + "' AND acctvalue='3791'";

          pstatementOB = obConn.prepareStatement(pstm);
          rsOB = pstatementOB.executeQuery();

          if (rsOB.next()) {
            mgrTaxamt = rsOB.getDouble("taxamt");
          }
          rsOB.close();
          pstatementOB.close();

          pstm = "SELECT sum(amtsourcecr - amtsourcedr) as grandtotal FROM fact_acct WHERE ad_client_id = '"
              + client + "'" + " AND em_sco_record3_id = '" + rs.getString("sco_internal_doc_id")
              + "' AND fact_acct_group_id='" + provfactAcctGroupId
              + "' AND (acctvalue LIKE '42%' OR acctvalue LIKE '43%' OR acctvalue LIKE '44%' OR acctvalue LIKE '45%' OR acctvalue LIKE '46%' OR acctvalue LIKE '47%') AND acctvalue NOT LIKE '422%' AND acctvalue NOT LIKE '432%' AND acctvalue NOT LIKE '473%' AND acctvalue NOT LIKE '474%' AND acctvalue NOT LIKE '475%'";

          pstatementOB = obConn.prepareStatement(pstm);
          rsOB = pstatementOB.executeQuery();

          if (rsOB.next()) {
            mgrGrandTotal = rsOB.getDouble("grandtotal");
          }
          rsOB.close();
          pstatementOB.close();

          if (podoctype.equals("NC")) {
            mgrTaxamt = 0 - mgrTaxamt;
            mgrGrandTotal = 0 - mgrGrandTotal;
          }

          if (mgrTaxamt <= 0 || mgrGrandTotal <= 0) {
            throw new OBException("el documento interno:" + rs.getString("physical_documentno")
                + " tiene impuesto diferido pero no ha sido calculado correctamente: mgrTaxamt:"
                + mgrTaxamt + " - mgrGrandTotal:" + mgrGrandTotal + " - factacctgroupid:"
                + provfactAcctGroupId);

          }

        }

        System.out.println("Insertando documento de compra:" + rs.getString("physical_documentno")
            + " con saldo: " + saldo);

        String glitempk = glitemPrefix + acctvalue;

        String cinvoiceId = java.util.UUID.randomUUID().toString().replaceAll("-", "");
        PreparedStatement pst = null;
        stm = "INSERT INTO c_invoice ("
            + "c_invoice_id, ad_client_id, ad_org_id, isactive, created ,createdby ,updated ,updatedby, "
            + "issotrx, documentno , docaction ,docstatus ,posted ,processing ,processed ,c_doctype_id, c_doctypetarget_id ,description, isprinted, salesrep_id, dateinvoiced, dateprinted, dateacct, "
            + "c_bpartner_id, c_bpartner_location_id, poreference, c_currency_id, paymentrule, c_paymentterm_id, c_charge_id, m_pricelist_id, withholdingamount, taxdate, "
            + "lastcalculatedondate, fin_paymentmethod_id, finalsettlement, daysoutstanding, percentageoverdue, c_costcenter_id, calculate_promotions, a_asset_id,"
            + "em_aprm_processinvoice, em_sco_invoiceref_id, em_sco_doctyperef_id, em_sco_aval_id, em_sco_duatype, em_sco_duaexpensetype, em_sco_duaexpensemode, em_sco_podoctype_comboitem_id,"
            + "em_sco_dua_id, em_sco_isimportation, em_sco_issimpleprovision, em_sco_skipreeval, em_sco_boe_to_id, em_sco_boe_type, em_ssa_isboerefinancing, em_sco_percepcionagent,"
            + "em_sco_specialdoctype, em_sco_diverse_acc_glitem_id, em_sco_detraction_percent, em_sco_voidmotive_cmbitem_id, em_sco_codtabla5_cmb_item_id, em_sco_newdateinvoiced,"
            + "em_sco_purchaseinvoicetype, em_sco_recalpaysched_proc, em_sco_boe_id, em_sco_specialmethod, em_scr_physical_documentno, em_scr_taxid, em_scr_datereception,"
            + "em_scr_fin_finaccount_id, em_sim_imp_costing_id, em_ssa_is_consignment, em_sco_isforfree, em_sco_ismanualref, em_sco_manualinvoiceref, em_sco_manualinvoicerefdate, em_sco_fixedduedate,em_sco_ismigrated,em_sco_migrated_dt_cmbitem_id, em_sco_migrated_totallines, em_sco_migrated_taxamt"
            + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
        // 84
        pst = obConn.prepareStatement(stm);
        pst.setString(1, cinvoiceId);
        pst.setString(2, client);
        pst.setString(3, rs.getString("ad_org_id"));

        pst.setString(4, "Y");

        pst.setTimestamp(5, timestampNow);
        pst.setString(6, createdUid);
        pst.setTimestamp(7, timestampNow);
        pst.setString(8, createdUid);

        pst.setString(9, "N");// issotrx

        pst.setString(10, rs.getString("documentno"));// documentno
        pst.setString(11, "CO");// docaction
        pst.setString(12, "DR");// docstatus
        pst.setString(13, "sco_M");
        pst.setString(14, "N");
        pst.setString(15, "N");

        pst.setString(16, docType);
        pst.setString(17, docType);

        pst.setString(18, "");
        pst.setString(19, "N");// ya impreso

        pst.setString(20, null);// SalesRep

        pst.setTimestamp(21, rs.getTimestamp("dateinvoiced"));// date reception
        pst.setTimestamp(22, null);
        pst.setTimestamp(23, rs.getTimestamp("dateinvoiced"));// dateacct

        pst.setString(24, rs.getString("c_bpartner_id"));

        // LOCATION
        statementOB = obConn.createStatement();
        rsOB = statementOB.executeQuery(
            "SELECT bl.c_bpartner_location_id from c_location l INNER JOIN c_bpartner_location bl ON l.c_location_id=bl.c_location_id  WHERE bl.c_bpartner_id='"
                + rs.getString("c_bpartner_id") + "'");

        String c_location_id = "";
        if (rsOB.next()) {
          c_location_id = rsOB.getString("c_bpartner_location_id");

        } else {
          statementOB.close();
          rsOB.close();

          // buscar el null location
          statementOB = obConn.createStatement();
          rsOB = statementOB.executeQuery(
              "SELECT bl.c_bpartner_location_id from c_location l INNER JOIN c_bpartner_location bl ON l.c_location_id=bl.c_location_id  WHERE bl.ad_client_id='"
                  + client + "' AND bl.c_bpartner_id='" + rs.getString("c_bpartner_id")
                  + "' AND  bl.em_sco_nulllocation = 'Y' LIMIT 1");
          if (rsOB.next()) {
            c_location_id = rsOB.getString("c_bpartner_location_id");
            statementOB.close();
            rsOB.close();
          } else {
            statementOB.close();
            rsOB.close();
            throw new OBException("Location NO existe! " + rs.getString("c_bpartner_id"));
          }

        }

        statementOB.close();
        rsOB.close();

        pst.setString(25, c_location_id);
        pst.setString(26, "");
        pst.setString(27, rs.getString("c_currency_id"));
        pst.setString(28, "P");
        pst.setString(29, mm.cpFechaFija);
        pst.setString(30, null);

        String moneda_abr = "PEN";
        if (rs.getString("c_currency_id").equals("100")) {
          moneda_abr = "USD";
        }

        pst.setString(31, mm.pricelistIdPO + moneda_abr);

        pst.setInt(32, 0);// withholding
        pst.setTimestamp(33, null);// taxdate

        pst.setTimestamp(34, null);
        pst.setString(35, mm.pmSinDefinir); // fin_paymentmethod_id

        pst.setTimestamp(36, null);
        pst.setInt(37, 0);

        pst.setInt(38, 0);
        pst.setString(39, null);

        pst.setString(40, null);
        pst.setString(41, null);

        pst.setString(42, "CO");// em_aprm_processinvoice
        pst.setString(43, null);// em_sco_invoiceref_id
        pst.setString(44, null);// em_sco_doctyperef_id

        pst.setString(45, null);
        pst.setString(46, null);// em_sco_duatype
        pst.setString(47, null);// em_sco_duaexpensetype
        pst.setString(48, null);// duaexpensemode
        pst.setString(49, rs.getString("podoctype_comboitem_id"));
        pst.setString(50, null);// em_sco_dua_id
        pst.setString(51, "N");

        pst.setString(52, "N");// simple provision
        pst.setString(53, "Y");

        pst.setString(54, null);
        pst.setString(55, "SCO_POR");

        pst.setString(56, "N");

        pst.setString(57, "N");
        pst.setString(58, null);
        pst.setString(59, glitempk);// em_sco_diverse_acc_glitem_id

        pst.setInt(60, 0);
        pst.setString(61, null);

        pst.setString(62, null);
        pst.setTimestamp(63, rs.getTimestamp("dateinvoiced"));
        pst.setString(64, "SCO_PURNA");

        pst.setString(65, null);
        pst.setString(66, null);
        pst.setString(67, null);
        pst.setString(68, rs.getString("physical_documentno"));
        pst.setString(69, null);

        pst.setTimestamp(70, rs.getTimestamp("dateinvoiced"));
        pst.setString(71, null);
        pst.setString(72, null);
        pst.setString(73, "N");
        pst.setString(74, "N");// em_sco_isforfree

        pst.setString(75, "Y");
        pst.setString(76, rs.getString("physical_documentno"));
        pst.setTimestamp(77, rs.getTimestamp("dateinvoiced"));

        pst.setTimestamp(78, rs.getTimestamp("dateinvoiced"));

        pst.setString(79, "Y");
        pst.setString(80, rs.getString("migrated_doctype_cmbitem_id"));

        if (!provfactAcctGroupId.isEmpty()) {
          pst.setDouble(81, mgrGrandTotal - mgrTaxamt);
          pst.setDouble(82, mgrTaxamt);
        } else {
          pst.setNull(81, Types.DOUBLE);
          pst.setNull(82, Types.DOUBLE);
        }
        // System.out.println(pst);

        pst.executeUpdate();
        pst.close();

        // insertar linea unica con importe
        pst = null;
        stm = "INSERT INTO c_invoiceline ("
            + "c_invoiceline_id, ad_client_id, ad_org_id, isactive, created, createdby, updated, updatedby,"
            + "line, description, account_id, m_product_id, qtyinvoiced, pricelist, priceactual,"
            + "pricelimit, linenetamt, c_charge_id, c_uom_id, c_tax_id, s_resourceassignment_id, taxamt, m_attributesetinstance_id, quantityorder, m_product_uom_id, c_invoice_discount_id, "
            + "c_projectline_id, m_offer_id, pricestd, excludeforwithholding, taxbaseamt, c_bpartner_id, periodnumber, a_asset_id, defplantype, c_project_id, "
            + "c_period_id, c_costcenter_id, user1_id, user2_id, bom_parent_id, em_sco_perceptiontaxed, em_sco_creditused, em_sco_invoiceline_prepay_id, "
            + "em_sco_specialdoctype, em_sco_auto_acc_glitem_id, em_scr_specialtax, em_scr_combo_item_id"
            + ",c_invoice_id, financial_invoice_line, em_sco_import_expenses_id, em_sco_prorrateo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?);";
        // 53
        String invoicelineId = java.util.UUID.randomUUID().toString().replaceAll("-", "");

        pst = obConn.prepareStatement(stm);
        pst.setString(1, invoicelineId);
        pst.setString(2, client);
        pst.setString(3, org);

        pst.setString(4, "Y");

        pst.setTimestamp(5, timestampNow);
        pst.setString(6, createdUid);
        pst.setTimestamp(7, timestampNow);
        pst.setString(8, createdUid);

        pst.setInt(9, 10);
        pst.setString(10, "");// description
        pst.setString(11, glitempk);

        pst.setString(12, null);
        pst.setDouble(13, 1);
        pst.setDouble(14, 0);
        pst.setDouble(15, saldo);
        pst.setDouble(16, 0.00);
        pst.setDouble(17, saldo);
        pst.setString(18, null);

        pst.setString(19, "100");
        pst.setString(20, mm.taxExento);
        pst.setDouble(22, 0);
        pst.setString(47, "SCOEXEMPT");
        pst.setString(48, mm.comboExentoOtrosTributos);

        pst.setString(21, null);

        pst.setString(23, null);
        pst.setObject(24, null);
        pst.setObject(25, null);
        pst.setString(26, null);
        pst.setString(27, null);
        pst.setString(28, null);
        pst.setDouble(29, saldo);// pricestd
        pst.setString(30, "N");

        pst.setDouble(31, saldo);
        pst.setString(32, rs.getString("c_bpartner_id"));
        pst.setInt(33, 0);
        pst.setString(34, null);
        pst.setString(35, null);
        pst.setString(36, null);
        pst.setString(37, null);
        pst.setString(38, null);
        pst.setString(39, null);
        pst.setString(40, null);
        pst.setString(41, null);

        pst.setString(42, "N");
        pst.setDouble(43, 0);// em_sco_creditused
        pst.setString(44, null);
        pst.setString(45, null);
        pst.setString(46, null);

        pst.setString(49, cinvoiceId);
        pst.setString(50, "Y");// finantial invoice
        pst.setString(51, null);
        pst.setString(52, "Y");// em_sco_prorrateo

        pst.executeUpdate();
        pst.close();

        pst = null;
        stm = "SELECT c_invoice_post(null, ?);";
        pst = obConn.prepareStatement(stm);
        pst.setString(1, cinvoiceId);
        pst.executeQuery();
        pst.close();

        obConn.commit();
      }

      statement.close();
      rs.close();
      System.out.println("intdoc:" + intdoc.getIdentifier());

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
      String msgType = "Error";
      String message = "";
      OBDal.getInstance().rollbackAndClose();
      Throwable exx = DbUtility.getUnderlyingSQLException(ex);
      msgType = "Error";
      message = OBMessageUtils.translateError(exx.getMessage()).getMessage();
      if (message.contains("@")) {
        message = OBMessageUtils.parseTranslation(message);
      }

      message = Utility.messageBD(conProvider, message, vars.getLanguage());
      // remove mysql message
      int pos = message.toLowerCase().indexOf("where: sql statement");
      if (pos != -1) {
        message = message.substring(0, pos);
      }

      OBError errmsg = new OBError();
      errmsg.setType(msgType);
      errmsg.setMessage(message);
      bundle.setResult(errmsg);
    }
  }
}