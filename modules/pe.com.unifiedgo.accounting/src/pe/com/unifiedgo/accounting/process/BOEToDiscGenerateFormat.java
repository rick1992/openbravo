package pe.com.unifiedgo.accounting.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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

import pe.com.unifiedgo.accounting.data.SCOBoeToDiscount;
import pe.com.unifiedgo.accounting.data.SCOBoetodiscLine;
import pe.com.unifiedgo.core.data.SCRComboItem;

public class BOEToDiscGenerateFormat extends DalBaseProcess {
	public void doExecute(ProcessBundle bundle) throws Exception {
		String filepath = "";
		final ConnectionProvider conProvider = bundle.getConnection();
		Connection conn = conProvider.getTransactionConnection();

		try {

			/*
			 * Set<String> params = bundle.getParams().keySet(); for (int i = 0;
			 * i < params.size(); i++) {
			 * System.out.println(params.toArray()[i]); }
			 */

			final String SCO_Boe_To_Discount = (String) bundle.getParams().get("SCO_Boe_To_Discount_ID");
			final VariablesSecureApp vars = bundle.getContext().toVars();
			OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
			final String language = bundle.getContext().getLanguage();
			User user = OBDal.getInstance().get(User.class, vars.getUser());

			int count = 0;

			SCOBoeToDiscount boetodisc = OBDal.getInstance().get(SCOBoeToDiscount.class, SCO_Boe_To_Discount);
			if (boetodisc == null) {
				throw new OBException("@SCO_InternalError@");
			}

			Date dateNow = new Date();
			SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss");
			String strDate = dateformat.format(dateNow);

			SimpleDateFormat dateformatddMMyy = new SimpleDateFormat("ddMMyy");
			DecimalFormat df = new DecimalFormat("0.00##");

			String data = "";
			String header = "nombre_razon_social;apellido_paterno;apellido_materno;tipodocumento;numero_documentoid;numero_letra;fecha_vencimiento;importe";
			String body = "";
			List<SCOBoetodiscLine> boetodisc_lines = boetodisc.getSCOBoetodiscLineList();
			if (boetodisc_lines.size() == 0) {
				throw new OBException("@SCO_BEOTODiscGenFormatWithoutLines@");
			}

			for (int i = 0; i < boetodisc_lines.size(); i++) {
				String boeline = "";
				SCOBoetodiscLine boetodisc_line = boetodisc_lines.get(i);
				Invoice boe = boetodisc_line.getInvoiceref();
				BusinessPartner bp = boe.getBusinessPartner();

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
					throw new OBException("@SCO_BEOTODiscGenFormat_InvalidData@");
				}
				String tipodocumento = tipodoc.getCode(); // 3:tipodocumento
				if (tipodocumento == null) {
					tipodocumento = "ND";
				}

				String numero_letra = boe.getScrPhysicalDocumentno(); // 5:numero_letra
				if (numero_letra == null) {
					numero_letra = "ND";
				} else {
					String[] parts = numero_letra.split("-");
					if (parts.length == 2) {
						numero_letra = parts[1];
					}
				}
				numero_letra = numero_letra.replaceFirst("^0+(?!$)", "");

				List<FIN_PaymentSchedule> paysheds = boe.getFINPaymentScheduleList();
				if (paysheds.size() == 0) {
					throw new OBException("@SCO_BEOTODiscGenFormat_InvalidData@");
				}
				FIN_PaymentSchedule payshed = paysheds.get(0);

				String fecha_vencimiento = dateformatddMMyy.format(payshed.getDueDate()); // 6:fecha_vencimiento

				String importe = df.format(boe.getGrandTotalAmount()); // 7:importe

				boeline = "\n" + nombre_fiscal + ";" + apellido_paterno + ";" + apellido_materno + ";" + tipodocumento + ";" + numero_documentoid + ";" + numero_letra + ";" + fecha_vencimiento + ";" + importe;
				body = body + boeline;
			}

			data = header + body;
			// save as attachment
			String directory = TabAttachments.getAttachmentDirectoryForNewAttachments("E0AFCD80B2324D9BA19B59B8022F617D", boetodisc.getId());
			String attachPath = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("attach.path");

			File attachDirectory = new File(attachPath + "/" + directory);
			String formatfilename = boetodisc.getIdentifier() + ".csv";

			// Lookup for the filename in the attachments of this record.If it
			// exists then do nothing
			// else create new attachment
			String strFileId = TabAttachmentsData.selectUniq(conProvider, boetodisc.getClient().getId(), boetodisc.getOrganization().getId(), "E0AFCD80B2324D9BA19B59B8022F617D", boetodisc.getId(), directory, formatfilename);
			if (strFileId == null) {
				attachDirectory.mkdirs();
			}

			BufferedWriter writer = null;
			String boetodiscfilepath = attachPath + "/" + directory + "/" + formatfilename;

			byte [] b= data.getBytes("windows-1252");        
			FileOutputStream fileOuputStream = null;
			try { 
				    fileOuputStream = new FileOutputStream(boetodiscfilepath); 
				    fileOuputStream.write(b);
			} finally {
				    fileOuputStream.close();
			}
			
			if (strFileId == null) {
				filepath = boetodiscfilepath;
				final String newFileId = SequenceIdData.getUUID();
				try {
					TabAttachmentsData.insert(conn, conProvider, newFileId, boetodisc.getClient().getId(), boetodisc.getOrganization().getId(), OBContext.getOBContext().getUser().getId(), "E0AFCD80B2324D9BA19B59B8022F617D", boetodisc.getId(), user.getId(), " ", formatfilename, directory);
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