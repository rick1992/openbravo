//Sqlc generated V1.O00-1
package pe.com.unifiedgo.report.ad_reports;

import java.math.BigDecimal;
import java.util.*;

import java.util.List;
import java.util.Vector;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.openbravo.dal.service.OBDal;
import org.openbravo.data.FieldProvider;

class ReporteDetraccionComprasJRData implements FieldProvider {
	static Logger log4j = Logger
			.getLogger(ReporteDetraccionComprasJRData.class);
	private String InitRecordNumber = "0";
	public String NRO_FACTURA;
	public String FECHA;
	public String CLIENTE;
	public BigDecimal PORC;
	public BigDecimal MONTO;
	public String ORG;
	public String ORG_RUC;
	public BigDecimal MONTO_DETRACCION;
	public BigDecimal DETRACCION_CONVERTIDA;
	public String MONEDA_FACTURA;


	public String rownum;

	public String getInitRecordNumber() {
		return InitRecordNumber;
	}

	@Override
	public String getField(String fieldName) {
		if (fieldName.equalsIgnoreCase("NRO_FACTURA"))
			return NRO_FACTURA;
		else if (fieldName.equalsIgnoreCase("FECHA"))
			return FECHA;
		else if (fieldName.equalsIgnoreCase("CLIENTE"))
			return CLIENTE;
		else if (fieldName.equalsIgnoreCase("PORC"))
			return PORC.toString();
		else if (fieldName.equalsIgnoreCase("MONTO"))
			return MONTO.toString();
		else if (fieldName.equalsIgnoreCase("MONTO_DETRACCION"))
			return MONTO_DETRACCION.toString();
		else if (fieldName.equalsIgnoreCase("DETRACCION_CONVERTIDA"))
			return DETRACCION_CONVERTIDA.toString();
		if (fieldName.equalsIgnoreCase("ORG"))
			return ORG;
		else if (fieldName.equalsIgnoreCase("MONEDA_FACTURA"))
			return MONEDA_FACTURA;
		else if (fieldName.equalsIgnoreCase("ORG_RUC"))
			return ORG_RUC;
		else if (fieldName.equals("rownum"))
			return rownum;
		else {
			log4j.debug("Field does not exist: " + fieldName);
			return null;
		}
	}

	public static ReporteDetraccionComprasJRData[] select(String adClientId,
			String startDate, String endDate, String strAD_Org_ID)
			throws ServletException {
		return select(adClientId, startDate, endDate, strAD_Org_ID, 0, 0);
	}

	public static ReporteDetraccionComprasJRData[] select(String adClientId,
			String startDate, String endDate, String strAD_Org_ID,
			int firstRegister, int numberRegisters) throws ServletException {

		String strSql = "";
		strSql = "select coalesce(inv.em_scr_physical_documentno,'') as NRO_FACTURA,"
				+ "          to_char(inv.dateinvoiced) as FECHA,"
				+ "          coalesce(bp.name,'') as CLIENTE,"
				+ "          coalesce(inv.em_sco_detraction_percent,0.00) as PORC,"
				+ "          coalesce(inv.grandtotal,0.00) as MONTO,"
				+ "          coalesce(org.name,'') as ORG,"
				+ "          coalesce(inf.taxid,'') as ORG_RUC"
				+ "     from c_invoice inv"
				+ "          join c_bpartner bp on inv.c_bpartner_id = bp.c_bpartner_id"
				+ "          join c_doctype doc on inv.c_doctypetarget_id = doc.c_doctype_id,"
				+ "          ad_org org join ad_orgtype otp using (ad_orgtype_id)"
				+ "          join ad_orginfo inf on org.ad_org_id = inf.ad_org_id"
				+ "    where AD_ISORGINCLUDED(inv.ad_org_id, '"
				+ strAD_Org_ID
				+ "', inv.ad_client_id)<>-1"
				+ "      and(otp.IsLegalEntity='Y' or otp.IsAcctLegalEntity='Y')"
				+ "      and inv.docstatus = 'CO'"
				+ "      and inv.em_sco_isdetraction_affected = 'Y'"
				+ "      and doc.em_sco_specialdoctype = 'SCOAPINVOICE'"

				+ "      and 'Y' not in (select pay.em_sco_detractionpayment "
				+ "                        from fin_payment_schedule sch"
				+ "                             left join fin_payment_scheduledetail det on sch.fin_payment_schedule_id = det.fin_payment_schedule_invoice"
				+ "                             left join fin_payment_detail pd on det.fin_payment_detail_id = pd.fin_payment_detail_id"
				+ "                             left join fin_payment pay on pd.fin_payment_id = pay.fin_payment_id"
				+ "                       where pay.isactive = 'Y'"
				+ "                         and sch.c_invoice_id = inv.c_invoice_id)"

				+ "      and inv.dateinvoiced between TO_DATE('"
				+ startDate
				+ "', 'DD-MM-YYYY') and TO_DATE('"
				+ endDate
				+ "', 'DD-MM-YYYY')"
				+ "      and org.ad_org_id in ('"
				+ strAD_Org_ID + "');";

		Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

		long countRecord = 0;

		try {
			Query sqlQuery = OBDal.getInstance().getSession()
					.createSQLQuery(strSql);
			List<Object> data = sqlQuery.list();

			for (int k = 0; k < data.size(); k++) {
				Object[] obj = (Object[]) data.get(k);
				countRecord++;
				ReporteDetraccionComprasJRData objectReporteDetraccionComprasJRData = new ReporteDetraccionComprasJRData();
				objectReporteDetraccionComprasJRData.NRO_FACTURA = (String) obj[0];
				objectReporteDetraccionComprasJRData.FECHA = (String) obj[1];
				objectReporteDetraccionComprasJRData.CLIENTE = (String) obj[2];
				objectReporteDetraccionComprasJRData.PORC = ((BigDecimal) (obj[3]))
						.setScale(3, BigDecimal.ROUND_HALF_UP);
				objectReporteDetraccionComprasJRData.MONTO = ((BigDecimal) (obj[4]))
						.setScale(3, BigDecimal.ROUND_HALF_UP);
				objectReporteDetraccionComprasJRData.ORG = (String) obj[5];
				objectReporteDetraccionComprasJRData.ORG_RUC = (String) obj[6];

				objectReporteDetraccionComprasJRData.rownum = Long
						.toString(countRecord);
				objectReporteDetraccionComprasJRData.InitRecordNumber = Integer
						.toString(firstRegister);

				vector.addElement(objectReporteDetraccionComprasJRData);

			}
		} catch (Exception ex) {
			log4j.error("Exception in query: " + strSql + "Exception:" + ex);
			throw new ServletException("@CODE=@" + ex.getMessage());
		}

		ReporteDetraccionComprasJRData objectReporteDetraccionComprasJRData[] = new ReporteDetraccionComprasJRData[vector
				.size()];
		vector.copyInto(objectReporteDetraccionComprasJRData);

		return (objectReporteDetraccionComprasJRData);
	}

	public static ReporteDetraccionComprasJRData[] selectMejorado(
			String adClientId, String startDate, String endDate,
			String strAD_Org_ID) throws ServletException {
		return selectMejorado(adClientId, startDate, endDate, strAD_Org_ID, 0,
				0);
	}

	public static ReporteDetraccionComprasJRData[] selectMejorado(
			String adClientId, String startDate, String endDate,
			String strAD_Org_ID, int firstRegister, int numberRegisters)
			throws ServletException {

		String strSql = "";
		strSql = "select coalesce(inv.em_scr_physical_documentno,'') as NRO_FACTURA,"
				+ "          to_char(inv.dateinvoiced) as FECHA,"
				+ "          coalesce(bp.name,'') as CLIENTE,"
				+ "          coalesce(inv.em_sco_detraction_percent,0.00) as PORC,"
				+ "          coalesce(inv.grandtotal,0.00) as MONTO,"
				+ "          coalesce(org.name,'') as ORG,"
				+ "          coalesce(inf.taxid,'') as ORG_RUC"

				+ "  , COALESCE (("

				+ " 	SELECT"
				+ " 			sum(det.amount)"
				+ " 	FROM"
				+ " 		fin_payment_schedule sch"
				+ " 	LEFT JOIN fin_payment_scheduledetail det ON sch.fin_payment_schedule_id = det.fin_payment_schedule_invoice"
				+ " 	LEFT JOIN fin_payment_detail pd ON det.fin_payment_detail_id = pd.fin_payment_detail_id"
				+ " LEFT JOIN fin_payment pay ON pd.fin_payment_id = pay.fin_payment_id"
				+ " 	WHERE"
				+ " 		pay.isactive = 'Y' AND pay.em_sco_detractionpayment='Y'"
				+ " ),0.0) AS MONTO_DETRACCION "

				+ "     from c_invoice inv"
				+ "          join c_bpartner bp on inv.c_bpartner_id = bp.c_bpartner_id"
				+ "          join c_doctype doc on inv.c_doctypetarget_id = doc.c_doctype_id,"
				+ "          ad_org org join ad_orgtype otp using (ad_orgtype_id)"
				+ "          join ad_orginfo inf on org.ad_org_id = inf.ad_org_id"

				+ "  , COALESCE (("

				+ " 	SELECT"
				+ " 			sum(det.amount)"
				+ " 	FROM"
				+ " 		fin_payment_schedule sch"
				+ " 	LEFT JOIN fin_payment_scheduledetail det ON sch.fin_payment_schedule_id = det.fin_payment_schedule_invoice"
				+ " 	LEFT JOIN fin_payment_detail pd ON det.fin_payment_detail_id = pd.fin_payment_detail_id"
				+ " LEFT JOIN fin_payment pay ON pd.fin_payment_id = pay.fin_payment_id"
				+ " 	WHERE"
				+ " 		pay.isactive = 'Y' AND pay.em_sco_detractionpayment='Y'"
				+ " ),0.0) AS MONTO_DETRACCION "

				+ "    where AD_ISORGINCLUDED(inv.ad_org_id, '"
				+ strAD_Org_ID
				+ "', inv.ad_client_id)<>-1"
				+ "      and(otp.IsLegalEntity='Y' or otp.IsAcctLegalEntity='Y')"
				+ "      and inv.docstatus = 'CO'"
				+ "      and inv.em_sco_isdetraction_affected = 'Y'"
				+ "      and doc.em_sco_specialdoctype = 'SCOAPINVOICE'"

				+ "      and inv.c_invoice_id in (select sch.c_invoice_id "
				+ "                        from fin_payment_schedule sch"
				+ "                             left join fin_payment_scheduledetail det on sch.fin_payment_schedule_id = det.fin_payment_schedule_invoice"
				+ "                             left join fin_payment_detail pd on det.fin_payment_detail_id = pd.fin_payment_detail_id"
				+ "                             left join fin_payment pay on pd.fin_payment_id = pay.fin_payment_id"
				+ "                       where pay.isactive = 'Y' AND pay.em_sco_detractionpayment='Y' )"

				+ "      and inv.dateinvoiced between TO_DATE('"
				+ startDate
				+ "', 'DD-MM-YYYY') and TO_DATE('"
				+ endDate
				+ "', 'DD-MM-YYYY')"
				+ "      and org.ad_org_id in ('"
				+ strAD_Org_ID + "');";

		Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

		long countRecord = 0;

		try {
			Query sqlQuery = OBDal.getInstance().getSession()
					.createSQLQuery(strSql);
			List<Object> data = sqlQuery.list();

			for (int k = 0; k < data.size(); k++) {
				Object[] obj = (Object[]) data.get(k);
				countRecord++;
				ReporteDetraccionComprasJRData objectReporteDetraccionComprasJRData = new ReporteDetraccionComprasJRData();
				objectReporteDetraccionComprasJRData.NRO_FACTURA = (String) obj[0];
				objectReporteDetraccionComprasJRData.FECHA = (String) obj[1];
				objectReporteDetraccionComprasJRData.CLIENTE = (String) obj[2];
				objectReporteDetraccionComprasJRData.PORC = ((BigDecimal) (obj[3]))
						.setScale(3, BigDecimal.ROUND_HALF_UP);
				objectReporteDetraccionComprasJRData.MONTO = ((BigDecimal) (obj[4]))
						.setScale(3, BigDecimal.ROUND_HALF_UP);
				objectReporteDetraccionComprasJRData.ORG = (String) obj[5];
				objectReporteDetraccionComprasJRData.ORG_RUC = (String) obj[6];
				objectReporteDetraccionComprasJRData.MONTO_DETRACCION = ((BigDecimal) (obj[7]))
						.setScale(3, BigDecimal.ROUND_HALF_UP);

				objectReporteDetraccionComprasJRData.rownum = Long
						.toString(countRecord);
				objectReporteDetraccionComprasJRData.InitRecordNumber = Integer
						.toString(firstRegister);

				vector.addElement(objectReporteDetraccionComprasJRData);

			}
		} catch (Exception ex) {
			log4j.error("Exception in query: " + strSql + "Exception:" + ex);
			throw new ServletException("@CODE=@" + ex.getMessage());
		}

		ReporteDetraccionComprasJRData objectReporteDetraccionComprasJRData[] = new ReporteDetraccionComprasJRData[vector
				.size()];
		vector.copyInto(objectReporteDetraccionComprasJRData);

		return (objectReporteDetraccionComprasJRData);
	}

	public static ReporteDetraccionComprasJRData[] selectMejoradoFinal(
			String adClientId, String startDate, String endDate,
			String strAD_Org_ID) throws ServletException {
		return selectMejoradoFinal(adClientId, startDate, endDate,
				strAD_Org_ID, 0, 0);
	}

	public static ReporteDetraccionComprasJRData[] selectMejoradoFinal(
			String adClientId, String startDate, String endDate,
			String strAD_Org_ID, int firstRegister, int numberRegisters)
			throws ServletException {

		String strSql = "";
		strSql = " select coalesce(ci.em_scr_physical_documentno,'') as NRO_FACTURA,"
				+ " to_char(ci.dateinvoiced) AS FECHA, "
				+ " COALESCE (cbp. NAME, '') AS CLIENTE, "

				+ " COALESCE ( "
				+ "	ci.em_sco_detraction_percent, "
				+ "	0 "
				+ " ) AS PORC, "
				+ " 	COALESCE (ci.grandtotal, 0.00) AS MONTO, "
				+ "          coalesce(ao.name,'') as ORG, "
				+ "          coalesce(inf.taxid,'') as ORG_RUC , "
				+ " COALESCE(SUM(fpd.amount)) as MONTO_DETRACCION "
				+ " ,(SELECT cc.cursymbol FROM c_currency cc where cc.c_currency_id=ci.c_currency_id) as MONEDA_FACTURA "
				+ " ,COALESCE (SUM(fpd.em_sco_paymentamount)) AS DETRACCION_CONVERTIDA "

				+ " from fin_payment fp "
				+ " inner join fin_payment_detail fpd on fp.fin_payment_id=fpd.fin_payment_id  "
				+ " inner join fin_payment_scheduledetail fpsd on fpd.fin_payment_detail_id=fpsd.fin_payment_detail_id "
				+ "	inner join fin_payment_schedule  fps on fpsd.fin_payment_schedule_invoice=fps.fin_payment_schedule_id "
				+ " inner join c_invoice ci on fps.c_invoice_id=ci.c_invoice_id "
				+ "	INNER JOIN c_bpartner cbp on ci.c_bpartner_id=cbp.c_bpartner_id "
				+ "	INNER JOIN ad_org ao on ci.ad_org_id=ao.ad_org_id "
				+ "	inner JOIN ad_orginfo inf ON ao.ad_org_id = inf.ad_org_id "

				+ " where "+
			      "        ci.AD_CLIENT_ID IN (";
			    strSql = strSql + ((adClientId==null || adClientId.equals(""))?"":adClientId);
			    strSql = strSql + 
			      ")" +
			      "        AND ci.AD_ORG_ID IN(";
			    strSql = strSql + ((strAD_Org_ID==null || strAD_Org_ID.equals(""))?"":strAD_Org_ID);
			    strSql = strSql + 
			      ") and " 

				+ "fp.em_sco_detractionpayment='Y' "
				+ " and ci.docstatus = 'CO' "
//				+ " --and inv.em_sco_isdetraction_affected = 'Y' "
				+ " and ci.em_sco_specialdoctype = 'SCOAPINVOICE' "
				+ "      and cast ( ci.dateinvoiced as date) between TO_DATE('" + startDate
				+ "', 'DD-MM-YYYY') and TO_DATE('" + endDate
				+ "', 'DD-MM-YYYY')" 
				+ " GROUP BY ci.em_sco_detraction_percent,ci.dateinvoiced,cbp. NAME,ci.grandtotal,ci.c_invoice_id "
				+ " ,ci.em_scr_physical_documentno,ao. NAME,inf.taxid, ci.c_currency_id"

				+ " order by ci.dateinvoiced,ci.c_invoice_id  ";



		Vector<java.lang.Object> vector = new Vector<java.lang.Object>(0);

		long countRecord = 0;

		try {
			Query sqlQuery = OBDal.getInstance().getSession()
					.createSQLQuery(strSql);
			List<Object> data = sqlQuery.list();

			for (int k = 0; k < data.size(); k++) {
				Object[] obj = (Object[]) data.get(k);
				countRecord++;
				ReporteDetraccionComprasJRData objectReporteDetraccionComprasJRData = new ReporteDetraccionComprasJRData();
				objectReporteDetraccionComprasJRData.NRO_FACTURA = (String) obj[0];
				objectReporteDetraccionComprasJRData.FECHA = (String) obj[1];
				objectReporteDetraccionComprasJRData.CLIENTE = (String) obj[2];
				objectReporteDetraccionComprasJRData.PORC = ((BigDecimal) (obj[3]))
						.setScale(3, BigDecimal.ROUND_HALF_UP);
				objectReporteDetraccionComprasJRData.MONTO = ((BigDecimal) (obj[4]))
						.setScale(3, BigDecimal.ROUND_HALF_UP);
				objectReporteDetraccionComprasJRData.ORG = (String) obj[5];
				objectReporteDetraccionComprasJRData.ORG_RUC = (String) obj[6];
				objectReporteDetraccionComprasJRData.MONTO_DETRACCION = ((BigDecimal) (obj[7]))
						.setScale(3, BigDecimal.ROUND_HALF_UP);
				objectReporteDetraccionComprasJRData.MONEDA_FACTURA = (String) obj[8];
				objectReporteDetraccionComprasJRData.DETRACCION_CONVERTIDA = ((BigDecimal) (obj[9]))
						.setScale(3, BigDecimal.ROUND_HALF_UP);

				

				objectReporteDetraccionComprasJRData.rownum = Long
						.toString(countRecord);
				objectReporteDetraccionComprasJRData.InitRecordNumber = Integer
						.toString(firstRegister);

				vector.addElement(objectReporteDetraccionComprasJRData);

			}
		} catch (Exception ex) {
			log4j.error("Exception in query: " + strSql + "Exception:" + ex);
			throw new ServletException("@CODE=@" + ex.getMessage());
		}

		ReporteDetraccionComprasJRData objectReporteDetraccionComprasJRData[] = new ReporteDetraccionComprasJRData[vector
				.size()];
		vector.copyInto(objectReporteDetraccionComprasJRData);

		return (objectReporteDetraccionComprasJRData);
	}

	public static ReporteDetraccionComprasJRData[] set()
			throws ServletException {
		ReporteDetraccionComprasJRData objectReporteDetraccionComprasJRData[] = new ReporteDetraccionComprasJRData[1];
		objectReporteDetraccionComprasJRData[0] = new ReporteDetraccionComprasJRData();
		objectReporteDetraccionComprasJRData[0].NRO_FACTURA = "";
		objectReporteDetraccionComprasJRData[0].FECHA = "";
		objectReporteDetraccionComprasJRData[0].CLIENTE = "";
		objectReporteDetraccionComprasJRData[0].PORC = new BigDecimal(0);
		objectReporteDetraccionComprasJRData[0].MONTO = new BigDecimal(0);
		objectReporteDetraccionComprasJRData[0].ORG = "";
		objectReporteDetraccionComprasJRData[0].ORG_RUC = "";
		objectReporteDetraccionComprasJRData[0].MONTO_DETRACCION = new BigDecimal(
				0);

		return objectReporteDetraccionComprasJRData;
	}

}
