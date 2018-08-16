package pe.com.unifiedgo.accounting.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.financialmgmt.payment.FIN_FinancialAccount;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

import pe.com.unifiedgo.accounting.data.SCOTelecredit;
import pe.com.unifiedgo.accounting.data.SCOTelecreditLine;
import pe.com.unifiedgo.core.data.SCRComboItem;

public class TelecreditGenerateFormat extends DalBaseProcess {
	public void doExecute(ProcessBundle bundle) throws Exception {
		final ConnectionProvider conProvider = bundle.getConnection();
		Connection conn = conProvider.getTransactionConnection();
		String filepath = "";

		try {
			/*
			 * Set<String> params = bundle.getParams().keySet(); for (int i = 0;
			 * i < params.size(); i++) {
			 * System.out.println(params.toArray()[i]); }
			 */

			final String SCO_Telecredit_ID = (String) bundle.getParams().get("SCO_Telecredit_ID");
			final VariablesSecureApp vars = bundle.getContext().toVars();
			OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
			final String language = bundle.getContext().getLanguage();
			User user = OBDal.getInstance().get(User.class, vars.getUser());

			int count = 0;

			SCOTelecredit telecredit = OBDal.getInstance().get(SCOTelecredit.class, SCO_Telecredit_ID);
			if (telecredit == null) {
				throw new OBException("@SCO_InternalError@");
			}

			Date dateNow = new Date();
			SimpleDateFormat dateformat = new SimpleDateFormat("dd-MM-yyyy_hh-mm-ss");
			String strDate = dateformat.format(dateNow);

			SimpleDateFormat dateformatyyyyMMdd = new SimpleDateFormat("yyyyMMdd");
			String strDateyyyyMMdd = dateformatyyyyMMdd.format(dateNow);

			String bankcode = telecredit.getFinancialAccount().getBankCode();

			if (bankcode == null) {
				throw new OBException("@SCO_TelecreditGenFormat_NoBankCode@");
			}

			String nombreBanco = pe.com.unifiedgo.report.common.Utility.nombreBanco(bankcode);

			String data = "";
			if (nombreBanco.equals("DE CREDITO DEL PERU")) {
				data = TelecreditGenerateFormat.getBCPFormat(telecredit, dateNow);
			} else {
				throw new OBException("@SCO_TelecreditGenFormat_BankNotSupported@");
			}

			// save as attachment
			String directory = TabAttachments.getAttachmentDirectoryForNewAttachments("35231661FFD74509BC01C34087475A62", telecredit.getId());
			String attachPath = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("attach.path");

			File attachDirectory = new File(attachPath + "/" + directory);

			SimpleDateFormat fdateFormat = new SimpleDateFormat("dd_mm_yyyy HH_mm_ss");
			String telecreditformatfilename = "telecredito_" + telecredit.getDocumentNo() + "_" + fdateFormat.format(new Date()) + ".txt";

			// Lookup for the filename in the attachments of this record.If it
			// exists then do nothing
			// else create new attachment

			String strFileId = TabAttachmentsData.selectUniq(conProvider, telecredit.getClient().getId(), telecredit.getOrganization().getId(), "35231661FFD74509BC01C34087475A62", telecredit.getId(), directory, telecreditformatfilename);
			if (strFileId == null) {
				attachDirectory.mkdirs();
			}
			String telecreditlinefilepath = attachPath + "/" + directory + "/" + telecreditformatfilename;

			byte [] b= data.getBytes("windows-1252");        
			FileOutputStream fileOuputStream = null;
			try { 
			    fileOuputStream = new FileOutputStream(telecreditlinefilepath); 
			    fileOuputStream.write(b);
			 } finally {
			    fileOuputStream.close();
			 }

			if (strFileId == null) {
				filepath = telecreditlinefilepath;
				final String newFileId = SequenceIdData.getUUID();
				try {
					TabAttachmentsData.insert(conn, conProvider, newFileId, telecredit.getClient().getId(), telecredit.getOrganization().getId(), user.getId(), "35231661FFD74509BC01C34087475A62", telecredit.getId(), "100", "Generated by UGO ", telecreditformatfilename, directory);
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

	static String getBCPFormat(SCOTelecredit telecredit, Date datenow) throws OBException {

		FIN_FinancialAccount finacc = telecredit.getFinancialAccount();
		SimpleDateFormat dateformatyyyyMMdd = new SimpleDateFormat("yyyyMMdd");
		String strDateyyyyMMdd = dateformatyyyyMMdd.format(datenow);

		// check for valid accounts in the lines ( EM_Sco_Codebank must be
		// 02(BCP) or em_sco_accounttype
		// = SCO_INTER)
		List<SCOTelecreditLine> telecredit_lines = telecredit.getSCOTelecreditLineList();
		for (int i = 0; i < telecredit_lines.size(); i++) {
			BankAccount bank = telecredit_lines.get(i).getBpBankaccount();
			if (bank.getScoCodebank() == null) {
				throw new OBException("@SCO_TelecreditGenFormat_BCPInvalidAcc@ " + telecredit_lines.get(i).getIdentifier());
			}

			if (bank.getScoCodebank().compareTo("02") != 0) {
				if (bank.getScoAccounttype() == null) {
					throw new OBException("@SCO_TelecreditGenFormat_BCPInvalidAcc@ " + telecredit_lines.get(i).getIdentifier());
				}
				if (bank.getScoAccounttype().compareTo("SCO_INTER") != 0) {
					throw new OBException("@SCO_TelecreditGenFormat_BCPInvalidAcc@ " + telecredit_lines.get(i).getIdentifier());
				}
			}
		}

		List<FinancialAccountTo> finacctos = TelecreditGenerateFormat.orderbankaccts(telecredit_lines);
		String header = "";
		String detail = "";

		String tiporeg_1_01 = "1"; // cabecera tipo reg
		String nplanilla_1_02 = StringUtils.leftPad(String.valueOf(finacctos.size()), 6, '0'); // cantidad_de_abonos
		String fechap_1_03 = strDateyyyyMMdd; // fecha de operacion
		String tccargo_1_04 = "C"; // tipo de cuenta de cargo
		String moneda_1_05 = "0001"; // moneda
		if (finacc.getCurrency().getId().equals("308")) {
			moneda_1_05 = "0001";
		} else if (finacc.getCurrency().getId().equals("100")) {
			moneda_1_05 = "1001";
		} else {
			throw new OBException("@SCO_TelecreditGenFormat_InvalidData@");
		}

		String ncuentacargo_1_06 = finacc.getGenericAccountNo(); // nro cuenta
																	// cargo
		if (ncuentacargo_1_06 == null) {
			throw new OBException("@SCO_TelecreditGenFormat_InvalidFinAcc@");
		}

		ncuentacargo_1_06 = StringUtils.remove(finacc.getGenericAccountNo(), '-');
		String ncuentacargo = ncuentacargo_1_06;

		if (ncuentacargo_1_06.length() != 13) {
			throw new OBException("@SCO_TelecreditGenFormat_InvalidFinAcc@");
		}

		ncuentacargo_1_06 = StringUtils.rightPad(ncuentacargo_1_06, 20, ' ');

		DecimalFormat df = new DecimalFormat("0.00##");
		String montototal_1_07 = StringUtils.leftPad(df.format(telecredit.getTotalAmount()), 17, '0'); // monto_total_a_pagar
		String ref_1_08 = telecredit.getDocumentNo(); // referencia a planilla
		if (ref_1_08 == null) {
			throw new OBException("@SCO_TelecreditGenFormat_InvalidData@");
		}
		if (ref_1_08.isEmpty()) {
			throw new OBException("@SCO_TelecreditGenFormat_InvalidData@");
		}
		ref_1_08 = StringUtils.rightPad(ref_1_08, 40, ' ');

		String itfexo_1_09 = "S";

		BigDecimal checksum = new BigDecimal(ncuentacargo.substring(3));
		for (int i = 0; i < finacctos.size(); i++) {
			FinancialAccountTo finaccto = finacctos.get(i);

			String ncuenta = finaccto.bankacct.getAccountNo();
			if (ncuenta == null) {
				throw new OBException("@SCO_TelecreditGenFormat_InvalidBankTo@ " + finaccto.bankacct.getIdentifier() + " - " + finaccto.bankacct.getBusinessPartner().getIdentifier());
			}
			if (finaccto.bankacct.getScoCodebank() == null) {
				throw new OBException("@SCO_TelecreditGenFormat_InvalidBankTo@ " + finaccto.bankacct.getIdentifier() + " - " + finaccto.bankacct.getBusinessPartner().getIdentifier());
			}
			if (finaccto.bankacct.getScoAccounttype() == null) {
				throw new OBException("@SCO_TelecreditGenFormat_InvalidBankTo@ " + finaccto.bankacct.getIdentifier() + " - " + finaccto.bankacct.getBusinessPartner().getIdentifier());
			}

			ncuenta = StringUtils.remove(ncuenta, '-');

			if (finaccto.bankacct.getScoCodebank().compareTo("02") == 0) {
				if (finaccto.bankacct.getScoAccounttype().compareTo("SCO_ACC") == 0 || finaccto.bankacct.getScoAccounttype().compareTo("SCO_MAS") == 0) {
					// cuenta corriente o maestra SSSCCCCCCCMDD contar solo
					// CCCCCCCMDD
					if (ncuenta.length() != 13) {
						throw new OBException("@SCO_TelecreditGenFormat_InvalidBankTo@ " + finaccto.bankacct.getIdentifier() + " - " + finaccto.bankacct.getBusinessPartner().getIdentifier());
					}
					ncuenta = ncuenta.substring(3);

				} else if (finaccto.bankacct.getScoAccounttype().compareTo("SCO_SAV") == 0) {
					// cuenta ahorros SSSCCCCCCCCMDD contar solo CCCCCCCCMDD
					if (ncuenta.length() != 14) {
						throw new OBException("@SCO_TelecreditGenFormat_InvalidBankTo@ " + finaccto.bankacct.getIdentifier() + " - " + finaccto.bankacct.getBusinessPartner().getIdentifier());
					}
					ncuenta = ncuenta.substring(3);
				} else {
					// cuenta interbancaria BBBSSSCCCCCCCCCCCCDD contar solo
					// CCCCCCCCCCCCDD
					if (ncuenta.length() != 20) {
						throw new OBException("@SCO_TelecreditGenFormat_InvalidBankTo@ " + finaccto.bankacct.getIdentifier() + " - " + finaccto.bankacct.getBusinessPartner().getIdentifier());
					}
					ncuenta = ncuenta.substring(6);
				}
			} else {
				if (finaccto.bankacct.getScoAccounttype().compareTo("SCO_INTER") == 0) {
					// cuenta interbancaria BBBSSSCCCCCCCCCCCCDD contar solo
					// CCCCCCCCCCCCDD
					if (ncuenta.length() != 20) {
						throw new OBException("@SCO_TelecreditGenFormat_InvalidBankTo@ " + finaccto.bankacct.getIdentifier() + " - " + finaccto.bankacct.getBusinessPartner().getIdentifier());
					}
					ncuenta = ncuenta.substring(6);
				} else {
					throw new OBException("@SCO_TelecreditGenFormat_InvalidBankTo@ " + finaccto.bankacct.getIdentifier() + " - " + finaccto.bankacct.getBusinessPartner().getIdentifier());
				}
			}

			checksum = checksum.add(new BigDecimal(ncuenta));
		}

		String schecksum_1_10 = StringUtils.leftPad(checksum.toPlainString(), 15, '0');

		/*
		 * System.out.println(tiporeg_1_01 + " - " + tiporeg_1_01.length());
		 * System.out.println(nplanilla_1_02 + " - " + nplanilla_1_02.length());
		 * System.out.println(fechap_1_03 + " - " + fechap_1_03.length());
		 * System.out.println(tccargo_1_04 + " - " + tccargo_1_04.length());
		 * System.out.println(moneda_1_05 + " - " + moneda_1_05.length());
		 * System.out.println(ncuentacargo_1_06 + " - " +
		 * ncuentacargo_1_06.length()); System.out.println(montototal_1_07 +
		 * " - " + montototal_1_07.length()); System.out.println(ref_1_08 +
		 * " - " + ref_1_08.length()); System.out.println(itfexo_1_09 + " - " +
		 * itfexo_1_09.length()); System.out.println(schecksum_1_10 + " - " +
		 * schecksum_1_10.length());
		 */

		if (tiporeg_1_01.length() != 1 || nplanilla_1_02.length() != 6 || fechap_1_03.length() != 8 || tccargo_1_04.length() != 1 || moneda_1_05.length() != 4 || ncuentacargo_1_06.length() != 20 || montototal_1_07.length() != 17 || ref_1_08.length() != 40 || itfexo_1_09.length() != 1
				|| schecksum_1_10.length() != 15) {
			throw new OBException("@SCO_TelecreditGenFormat_InvalidHeader@");
		}

		header = tiporeg_1_01 + nplanilla_1_02 + fechap_1_03 + tccargo_1_04 + moneda_1_05 + ncuentacargo_1_06 + montototal_1_07 + ref_1_08 + itfexo_1_09 + schecksum_1_10;

		for (int i = 0; i < finacctos.size(); i++) {

			String prodetail = "";

			FinancialAccountTo finaccto = finacctos.get(i);
			BankAccount bankacct = finaccto.bankacct;
			BusinessPartner vendor = finaccto.bankacct.getBusinessPartner();
			User bankcontactuser = bankacct.getUserContact();

			if (vendor == null) {
				throw new OBException("@SCO_TelecreditGenFormat_InvalidBankAcct@");
			}

			String dptiporeg_2_01 = "2"; // detalle-proveedores tipo reg

			String dptipocuenta_2_02 = "C"; // tipo de cuenta
			if (bankacct.getScoCodebank().compareTo("02") == 0) {
				if (bankacct.getScoAccounttype().compareTo("SCO_ACC") == 0) {
					dptipocuenta_2_02 = "C";
				} else if (bankacct.getScoAccounttype().compareTo("SCO_MAS") == 0) {
					dptipocuenta_2_02 = "M";
				} else if (bankacct.getScoAccounttype().compareTo("SCO_SAV") == 0) {
					dptipocuenta_2_02 = "A";
				}
			} else {
				if (bankacct.getScoAccounttype().compareTo("SCO_INTER") == 0) {
					dptipocuenta_2_02 = "B";
				}
			}

			String dpncuentaabono_2_03 = StringUtils.remove(bankacct.getAccountNo(), '-'); // ncuenta
																							// abono

			dpncuentaabono_2_03 = StringUtils.rightPad(dpncuentaabono_2_03, 20, ' ');
			String dpmodpago_2_04 = "1"; // tipo de pago

			// tipo doc
			SCRComboItem tipodoc = vendor.getScrComboItem();
			if (bankcontactuser != null)
				tipodoc = bankcontactuser.getScoIddoctypeCmbItem();
			if (tipodoc == null) {
				throw new OBException("@SCO_TelecreditGenFormat_InvalidData@");
			}

			String dp_tipodoc_2_05 = ""; // tipo documento
			String tipodoc_value = tipodoc.getSearchKey();
			if (tipodoc_value.equals("DNI")) {
				dp_tipodoc_2_05 = "1";
			} else if (tipodoc_value.equals("CARNET DE EXTRANJERIA")) {
				dp_tipodoc_2_05 = "3";
			} else if (tipodoc_value.equals("PASAPORTE")) {
				dp_tipodoc_2_05 = "4";
			} else if (tipodoc_value.equals("REGISTRO UNICO DE CONTRIBUYENTES")) {
				dp_tipodoc_2_05 = "6";
			} else {
				throw new OBException("@SCO_TelecreditGenFormat_TipoDocNotSupported@");
			}

			String dpndoc_2_06 = vendor.getTaxID();
			if (bankcontactuser != null)
				dpndoc_2_06 = bankcontactuser.getScoIdentificationID();// numero
																		// de
																		// documento

			if (dpndoc_2_06 == null) {
				throw new OBException("@SCO_TelecreditGenFormat_InvalidData@");
			}
			dpndoc_2_06 = StringUtils.rightPad(dpndoc_2_06, 12, ' ');

			String docorr_2_07 = "   "; // correlativo

			String nombreprov_2_08 = vendor.getName(); // nombre del proveedor
			if (bankcontactuser != null) {
				nombreprov_2_08 = "";
				if (bankcontactuser.getLastName() != null) {
					nombreprov_2_08 = nombreprov_2_08 + bankcontactuser.getLastName() + " ";
				}
				if (bankcontactuser.getScoLastname2() != null) {
					nombreprov_2_08 = nombreprov_2_08 + bankcontactuser.getScoLastname2() + " ";
				}
				if (bankcontactuser.getFirstName() != null) {
					nombreprov_2_08 = nombreprov_2_08 + bankcontactuser.getFirstName() + " ";
				}
			}

			if(nombreprov_2_08.length() > 75){
                          throw new OBException("@SCO_TelecreditGenFormat_Invalid_2_08_length@" + nombreprov_2_08);
			}
			nombreprov_2_08 = StringUtils.rightPad(nombreprov_2_08, 75, ' ');

			String proref_2_09 = StringUtils.rightPad((telecredit.getReferenceNo() != null ? telecredit.getReferenceNo() : ""), 40, ' '); // referencia
			// para el
			// proveedor
			String ref_2_10 = StringUtils.rightPad((telecredit.getReferenceNo() != null ? telecredit.getReferenceNo() : ""), 20, ' ');// referencia
			// para
			// la empresa
			String monedaabono_2_11 = "0001"; // moneda de abono
			if (bankacct.getScoCCurrency() == null) {
				throw new OBException("@SCO_TelecreditGenFormat_InvalidData@");
			} else if (bankacct.getScoCCurrency().getId().equals("308")) {
				monedaabono_2_11 = "0001";
			} else if (bankacct.getScoCCurrency().getId().equals("100")) {
				monedaabono_2_11 = "1001";
			} else {
				throw new OBException("@SCO_TelecreditGenFormat_InvalidData@");
			}

			if (!monedaabono_2_11.equals(moneda_1_05)) {
				throw new OBException("@SCO_TelecreditGenFormat_InvalidCurrency@ " + bankacct.getIdentifier() + " - " + bankacct.getBusinessPartner().getIdentifier());
			}

			String importeabono_2_12 = ""; // importe a abonar
			String validarIDC_2_13 = "N";

			BigDecimal paymentamount = new BigDecimal(0);

			String spaydetail = "";
			for (int j = 0; j < finaccto.telecredit_lines.size(); j++) {
				SCOTelecreditLine telecredit_line = finaccto.telecredit_lines.get(j);
				paymentamount = paymentamount.add(telecredit_line.getAmount());

				String spaydetail_detail = "";

				String pdtiporeg_3_01 = "3"; // tipo de registro
				String pdtipodoc_3_02 = ""; // tipo doc a pagar
				String pdnodoc_3_03 = ""; // nro doc a pagar

				if (telecredit_line.getTipoDePago().compareTo("SCO_AP") == 0) {
					// invoice
					Invoice inv = telecredit_line.getDocumentReference();
					String specialdoctype = inv.getTransactionDocument().getScoSpecialdoctype();
					if (specialdoctype == null) {
						pdtipodoc_3_02 = "D";
					} else if (specialdoctype.equals("SCOAPINVOICE")) {
						pdtipodoc_3_02 = "F";
					} else if (specialdoctype.equals("SCOAPCREDITMEMO")) {
						pdtipodoc_3_02 = "M";
					} else {
						pdtipodoc_3_02 = "D";
					}

					pdnodoc_3_03 = inv.getScrPhysicalDocumentno();
					if (pdnodoc_3_03 == null) {
						throw new OBException("@SCO_TelecreditGenFormat_NoVendorDocno@");
					}
					pdnodoc_3_03 = StringUtils.leftPad(pdnodoc_3_03.replaceAll("[^a-zA-Z0-9]", ""), 15, '0');

				} else {
					pdtipodoc_3_02 = "D";
					pdnodoc_3_03 = StringUtils.leftPad("", 15, '0');
				}

				String dpamount_3_04 = ""; // importe a pagar
				dpamount_3_04 = StringUtils.leftPad(df.format(telecredit_line.getAmount()), 17, '0');

				/*
				 * System.out.println("Doc Detail:");
				 * System.out.println(pdtiporeg_3_01 + " - " +
				 * pdtiporeg_3_01.length()); System.out.println(pdtipodoc_3_02 +
				 * " - " + pdtipodoc_3_02.length());
				 * System.out.println(pdnodoc_3_03 + " - " +
				 * pdnodoc_3_03.length()); System.out.println(dpamount_3_04 +
				 * " - " + dpamount_3_04.length());
				 * System.out.println("End DocDetail.");
				 */

				if (pdtiporeg_3_01.length() != 1 || pdtipodoc_3_02.length() != 1 || pdnodoc_3_03.length() != 15 || dpamount_3_04.length() != 17) {
					throw new OBException("@SCO_TelecreditGenFormat_InvalidDocDetail@");
				}
				String sspaydetail = pdtiporeg_3_01 + pdtipodoc_3_02 + pdnodoc_3_03 + dpamount_3_04;
				spaydetail_detail = spaydetail_detail + "\n" + sspaydetail;

				spaydetail = spaydetail + spaydetail_detail;
			}

			importeabono_2_12 = StringUtils.leftPad(df.format(paymentamount), 17, '0');

			/*
			 * System.out.println("ACCOUNT:" + bankacct.getAccountNo() +
			 * " -  bp:" + vendor.getIdentifier());
			 * System.out.println(dptiporeg_2_01 + " - " +
			 * dptiporeg_2_01.length()); System.out.println(dptipocuenta_2_02 +
			 * " - " + dptipocuenta_2_02.length());
			 * System.out.println(dpncuentaabono_2_03 + " - " +
			 * dpncuentaabono_2_03.length()); System.out.println(dpmodpago_2_04
			 * + " - " + dpmodpago_2_04.length());
			 * System.out.println(dp_tipodoc_2_05 + " - " +
			 * dp_tipodoc_2_05.length()); System.out.println(dpndoc_2_06 + " - "
			 * + dpndoc_2_06.length()); System.out.println(docorr_2_07 + " - " +
			 * docorr_2_07.length()); System.out.println(nombreprov_2_08 + " - "
			 * + nombreprov_2_08.length()); System.out.println(proref_2_09 +
			 * " - " + proref_2_09.length()); System.out.println(ref_2_10 +
			 * " - " + ref_2_10.length()); System.out.println(monedaabono_2_11 +
			 * " - " + monedaabono_2_11.length());
			 * System.out.println(importeabono_2_12 + " - " +
			 * importeabono_2_12.length()); System.out.println(validarIDC_2_13 +
			 * " - " + validarIDC_2_13.length());
			 * System.out.println("END ACCOUNT ---------");
			 */

			if (dptiporeg_2_01.length() != 1 || dptipocuenta_2_02.length() != 1 || dpncuentaabono_2_03.length() != 20 || dpmodpago_2_04.length() != 1 || dp_tipodoc_2_05.length() != 1 || dpndoc_2_06.length() != 12 || docorr_2_07.length() != 3 || nombreprov_2_08.length() != 75
					|| proref_2_09.length() != 40 || ref_2_10.length() != 20 || monedaabono_2_11.length() != 4 || importeabono_2_12.length() != 17 || validarIDC_2_13.length() != 1) {
				throw new OBException("@SCO_TelecreditGenFormat_InvalidVendorDetail@");
			}
			prodetail = dptiporeg_2_01 + dptipocuenta_2_02 + dpncuentaabono_2_03 + dpmodpago_2_04 + dp_tipodoc_2_05 + dpndoc_2_06 + docorr_2_07 + nombreprov_2_08 + proref_2_09 + ref_2_10 + monedaabono_2_11 + importeabono_2_12 + validarIDC_2_13;
			prodetail = prodetail + spaydetail;
			detail = detail + "\n" + prodetail;
		}

		return header + detail;
	}

	static List<FinancialAccountTo> orderbankaccts(List<SCOTelecreditLine> telecredit_lines) throws OBException {
		List<FinancialAccountTo> finacctos = new ArrayList<FinancialAccountTo>();
		int pos = -1;

		for (int i = 0; i < telecredit_lines.size(); i++) {
			SCOTelecreditLine telecredit_line = telecredit_lines.get(i);
			BankAccount bankacc = telecredit_line.getBpBankaccount();

			String Accountno = bankacc.getAccountNo();
			if (Accountno == null) {
				throw new OBException("@SCO_TelecreditGenFormat_BCPInvalidAcc@ " + telecredit_lines.get(i).getIdentifier());
			}
			if (Accountno.isEmpty()) {
				throw new OBException("@SCO_TelecreditGenFormat_BCPInvalidAcc@ " + telecredit_lines.get(i).getIdentifier());
			}

			pos = FinancialAccountTo.getPos(finacctos, bankacc);
			if (pos == -1) {
				FinancialAccountTo finaccto = new FinancialAccountTo();
				finaccto.bankacct = bankacc;
				finaccto.telecredit_lines.add(telecredit_line);

				finacctos.add(finaccto);

			} else {
				finacctos.get(pos).telecredit_lines.add(telecredit_line);
			}
		}

		return finacctos;
	}

}

class FinancialAccountTo {
	public BankAccount bankacct;
	public List<SCOTelecreditLine> telecredit_lines;

	public FinancialAccountTo() {
		bankacct = null;
		telecredit_lines = new ArrayList<SCOTelecreditLine>();
	}

	static int getPos(List<FinancialAccountTo> finacctos, BankAccount ba) {
		for (int i = 0; i < finacctos.size(); i++) {
			if (finacctos.get(i).bankacct.getAccountNo().equals(ba.getAccountNo()) && finacctos.get(i).bankacct.getBusinessPartner().getId().equals(ba.getBusinessPartner().getId())) {
				return i;
			}
		}
		return -1;
	}
}
