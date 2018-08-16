package pe.com.unifiedgo.accounting.process;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletException;

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
import org.openbravo.erpCommon.businessUtility.Tree;
import org.openbravo.erpCommon.businessUtility.TreeData;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.SequenceIdData;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.financialmgmt.calendar.Period;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

import pe.com.unifiedgo.accounting.StructPle;
import pe.com.unifiedgo.accounting.SunatUtil;
import pe.com.unifiedgo.accounting.data.SCOPle;
import pe.com.unifiedgo.accounting.data.SCOPleDetail;
import pe.com.unifiedgo.report.ad_reports.GeneralLedgerJournal;
import pe.com.unifiedgo.report.ad_reports.ReportCashBank;
import pe.com.unifiedgo.report.ad_reports.ReportGeneralLedger;
import pe.com.unifiedgo.report.ad_reports.ReportLibroConsignaciones;
import pe.com.unifiedgo.report.ad_reports.ReportLibroInventariosYBalance10;
import pe.com.unifiedgo.report.ad_reports.ReportLibroInventariosYBalance12y13;
import pe.com.unifiedgo.report.ad_reports.ReportLibroInventariosYBalance14;
import pe.com.unifiedgo.report.ad_reports.ReportLibroInventariosYBalance16;
import pe.com.unifiedgo.report.ad_reports.ReportLibroInventariosYBalance19;
import pe.com.unifiedgo.report.ad_reports.ReportLibroInventariosYBalance31;
import pe.com.unifiedgo.report.ad_reports.ReportLibroInventariosYBalance34;
import pe.com.unifiedgo.report.ad_reports.ReportLibroInventariosYBalance37y49;
import pe.com.unifiedgo.report.ad_reports.ReportLibroInventariosYBalance42y43;
import pe.com.unifiedgo.report.ad_reports.ReportLibroInventariosYBalance46;
import pe.com.unifiedgo.report.ad_reports.ReportPurchasingRecords;
import pe.com.unifiedgo.report.ad_reports.ReportRegistroActivosFijos;
import pe.com.unifiedgo.report.ad_reports.ReportSalesRevenueRecords;
import pe.com.unifiedgo.report.ad_reports.ReportTrialBalance;
import pe.com.unifiedgo.report.ad_reports.ReportValuedInventory;
import pe.com.unifiedgo.report.ad_reports.ReporteControlPatrimonio;
import pe.com.unifiedgo.report.ad_reports.ReporteDetalleCuenta50Capital;
import pe.com.unifiedgo.report.ad_reports.ReporteEstadoFlujoEfectivo;
import pe.com.unifiedgo.report.ad_reports.ReporteEstadoResultados;
import pe.com.unifiedgo.report.ad_reports.ReporteEstadoResultadosIntegrales;
import pe.com.unifiedgo.report.ad_reports.ReporteEstadoSituacionFinanciera;
import pe.com.unifiedgo.report.ad_reports.ReporteInventarioValorizadoProductoTerminado;

public class GeneratePLE extends DalBaseProcess {
  public void doExecute(ProcessBundle bundle) throws Exception {
    final ConnectionProvider conProvider = bundle.getConnection();
    Connection conn = conProvider.getTransactionConnection();

    try {

      // retrieve the parameters from the bundle
      final String c_period_id = (String) bundle.getParams().get("cperiodid");
      final String sco_ple_id = (String) bundle.getParams().get("SCO_Ple_ID");
      final String strValidfrom = (String) bundle.getParams().get("validfrom");
      final String strValidto = (String) bundle.getParams().get("validto");

      final VariablesSecureApp vars = bundle.getContext().toVars();
      OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
      final String language = bundle.getContext().getLanguage();
      User user = OBDal.getInstance().get(User.class, vars.getUser());

      SCOPle ple = OBDal.getInstance().get(SCOPle.class, sco_ple_id);
      Period period = OBDal.getInstance().get(Period.class, c_period_id);

      if (ple == null || period == null) {
        throw new OBException("@SCO_InternalError@");
      }

      Calendar cal = Calendar.getInstance();
      cal.setTime(period.getStartingDate());
      String month = String.format("%1$2s", String.valueOf(cal.get(Calendar.MONTH) + 1)).replace(
          ' ', '0');

      String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("dateFormat.java");
      SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);

      String strDttFrom = sdf.format(period.getStartingDate());
      String strDttTo = sdf.format(period.getEndingDate());

      StructPle pleData = null;

      String adOrgId_LE = AccProcessData.getOrgLeBu(conProvider, ple.getOrganization().getId(),
          "LE");
      if (adOrgId_LE == null) {
        throw new OBException("@SCO_InternalError@");
      }

      Organization orgle = OBDal.getInstance().get(Organization.class, adOrgId_LE);
      String cAcctSchemaId = orgle.getGeneralLedger().getId();
      boolean ismanualdate = false;
      Date rv_validfrom = period.getStartingDate();
      Date rv_validto = period.getEndingDate();

      if (ple.getName().equals("Libro Diario")) {
        OBContext.setAdminMode();

        pleData = GeneralLedgerJournal.getPLE(conProvider, vars, bundle.getContext()
            .getUserClient(), bundle.getContext().getUserOrganization(), strDttFrom, strDttTo, "",
            ple.getOrganization().getId(), "", "", "", cAcctSchemaId, "Y", "Y", "Y", "1", "1", "Y",
            "Y", "Y", "Y", "");
        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Libro Diario Plan Contable")) {
        OBContext.setAdminMode();

        pleData = GeneralLedgerJournal.getPLE(conProvider, vars, bundle.getContext()
            .getUserClient(), bundle.getContext().getUserOrganization(), strDttFrom, strDttTo, "",
            ple.getOrganization().getId(), "", "", "", cAcctSchemaId, "Y", "Y", "Y", "1", "1", "Y",
            "Y", "Y", "", "Y");
        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Libro Mayor")) {
        OBContext.setAdminMode();

        pleData = ReportGeneralLedger.getPLE(conProvider, vars,
            bundle.getContext().getUserClient(), bundle.getContext().getUserOrganization(),
            strDttFrom, strDttTo, "", "", "", "", ple.getOrganization().getId(), "", "", "", "",
            cAcctSchemaId, "1", "Y");
        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Registro de Compras")) {
        OBContext.setAdminMode();

        pleData = ReportPurchasingRecords.getPLE(conProvider, vars, bundle.getContext()
            .getUserClient(), bundle.getContext().getUserOrganization(), strDttFrom, strDttTo, ple
            .getOrganization().getId(), "", "", "", "Y", c_period_id);
        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Registro de Ventas")) {
        OBContext.setAdminMode();

        pleData = ReportSalesRevenueRecords.getPLE(conProvider, vars, bundle.getContext()
            .getUserClient(), bundle.getContext().getUserOrganization(), strDttFrom, strDttTo, ple
            .getOrganization().getId(), "", "", "", "Y", c_period_id);
        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Registro No Domiciliados")) {
        OBContext.setAdminMode();

        pleData = ReportPurchasingRecords.getPLENoDomiciliado(conProvider, vars, bundle
            .getContext().getUserClient(), bundle.getContext().getUserOrganization(), strDttFrom,
            strDttTo, ple.getOrganization().getId(), "", "", "", "Y", c_period_id);
        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Libro Caja y Bancos - Efectivo")) {
        OBContext.setAdminMode();

        pleData = ReportCashBank.getPLE(conProvider, vars, strDttFrom, strDttTo, bundle
            .getContext().getUserClient(), ple.getOrganization().getId(), "", "EFECTIVO");
        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Libro Caja y Bancos - Cuenta corriente")) {
        OBContext.setAdminMode();

        pleData = ReportCashBank.getPLE(conProvider, vars, strDttFrom, strDttTo, bundle
            .getContext().getUserClient(), ple.getOrganization().getId(), "", "CTACORRIENTE");
        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Activos Fijos")) {
        OBContext.setAdminMode();

        pleData = ReportRegistroActivosFijos.getPLE(conProvider, vars, strDttFrom, strDttTo, bundle
            .getContext().getUserClient(), ple.getOrganization().getId());
        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Inventario Valorizado")) {
        OBContext.setAdminMode();

        pleData = ReportValuedInventory.getPLE(conProvider, vars, strDttFrom, strDttTo, bundle
            .getContext().getUserClient(), ple.getOrganization().getId());
        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Consignaciones - Consignatario")) {
        OBContext.setAdminMode();

        pleData = ReportLibroConsignaciones.getPLE(conProvider, vars, strDttFrom, strDttTo, bundle
            .getContext().getUserClient(), ple.getOrganization().getId(), "");
        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Consignaciones - Consignador")) {
        OBContext.setAdminMode();

        pleData = ReportLibroConsignaciones.getPLE(conProvider, vars, strDttFrom, strDttTo, bundle
            .getContext().getUserClient(), ple.getOrganization().getId(), "Consignador");
        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("FACE Resumen Ventas")) {
        OBContext.setAdminMode();

        rv_validfrom = period.getStartingDate();
        rv_validto = period.getEndingDate();

        if (strValidfrom != null && !strValidfrom.isEmpty() && strValidto != null
            && !strValidto.isEmpty()) {
          Date validfrom = sdf.parse(strValidfrom);
          Date validto = sdf.parse(strValidto);
          if (validfrom != null && validto != null) {
            if (validto.compareTo(validfrom) < 0) {
              throw new OBException("@SCO_GeneratePLEValidToLValidFrom@");
            }

            rv_validfrom = validfrom;
            rv_validto = validto;
            ismanualdate = true;

          }
        }
        pleData = SunatUtil.getFACEResumenVentas(conProvider, rv_validfrom, rv_validto,
            ple.getOrganization());

        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("FACE Resumen Retenciones")) {
        OBContext.setAdminMode();

        rv_validfrom = period.getStartingDate();
        rv_validto = period.getEndingDate();
        if (strValidfrom != null && !strValidfrom.isEmpty() && strValidto != null
            && !strValidto.isEmpty()) {
          Date validfrom = sdf.parse(strValidfrom);
          Date validto = sdf.parse(strValidto);
          if (validfrom != null && validto != null) {
            if (validto.compareTo(validfrom) < 0) {
              throw new OBException("@SCO_GeneratePLEValidToLValidFrom@");
            }

            rv_validfrom = validfrom;
            rv_validto = validto;
            ismanualdate = true;

          }
        }
        pleData = SunatUtil.getFACEResumenRetenciones(conProvider, rv_validfrom, rv_validto,
            ple.getOrganization());

        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Detalle del saldo de la cuenta 10")) {
        OBContext.setAdminMode();

        pleData = ReportLibroInventariosYBalance10.getStructPLECuenta10(conProvider, vars, rv_validfrom, rv_validto, ple.getOrganization().getId());

        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Cuentas por Cobrar Comerciales/Relacionadas -  12 y 13")) {
        OBContext.setAdminMode();

        pleData = ReportLibroInventariosYBalance12y13.getStructPLECuenta12y13(conProvider, vars, rv_validfrom, rv_validto, ple.getOrganization().getId());

        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Cuentas por Cobrar a Personal/Socios/Directores - 14")) {
        OBContext.setAdminMode();

        rv_validto = period.getEndingDate();
        if (strValidto != null && !strValidto.isEmpty()) {
          Date validto = sdf.parse(strValidto);
          if (validto != null) {
            rv_validto = validto;
            ismanualdate = true;
          }
        }
        pleData = ReportLibroInventariosYBalance14.getStructPLECuenta14(conProvider, vars, rv_validfrom, rv_validto, ple.getOrganization().getId());
        /*pleData = SunatUtil.getCuentasxCobrarComercialesRelacionadas_14(conProvider, rv_validto,
            ple.getOrganization());*/

        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Cuentas por Cobrar Diversas - Relacionadas-16-17")) {
        OBContext.setAdminMode();

        rv_validto = period.getEndingDate();
        if (strValidto != null && !strValidto.isEmpty()) {
          Date validto = sdf.parse(strValidto);
          if (validto != null) {
            rv_validto = validto;
            ismanualdate = true;
          }
        }
        pleData = ReportLibroInventariosYBalance16.getStructPLECuenta16y17(conProvider, vars,rv_validfrom, rv_validto, ple.getOrganization().getId());

        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Cuentas de Estimacion Cobranzas Dudodas-19")) {
        OBContext.setAdminMode();

        rv_validto = period.getEndingDate();
        if (strValidto != null && !strValidto.isEmpty()) {
          Date validto = sdf.parse(strValidto);
          if (validto != null) {
            rv_validto = validto;
            ismanualdate = true;
          }
        }
        pleData = ReportLibroInventariosYBalance19.getStructPLECuenta19(conProvider, vars, rv_validfrom, rv_validto, ple.getOrganization().getId());
        
        /*pleData = SunatUtil.getCuentasCobranzaDudosa_19(conProvider, rv_validto,
            ple.getOrganization());*/

        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Cuentas Mercaderias y Productos Terminados 20-21")) {
        OBContext.setAdminMode();

        rv_validto = period.getEndingDate();
        if (strValidto != null && !strValidto.isEmpty()) {
          Date validto = sdf.parse(strValidto);
          if (validto != null) {
            rv_validto = validto;
            ismanualdate = true;
          }
        }

        // conProvider

        String strTreeOrg = TreeData.getTreeOrg(conProvider, ple.getClient().getId());
        String strOrgFamily = getFamily(conProvider, strTreeOrg, ple.getOrganization().getId());

        pleData = ReporteInventarioValorizadoProductoTerminado.getSaldoCuenta20(ple.getClient()
            .getId(), rv_validto, ple.getOrganization(), strOrgFamily);

        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Detalle del saldo de la cuenta 30")) {
        OBContext.setAdminMode();

        pleData = ReportLibroInventariosYBalance31.getPLECuenta30(conProvider, vars, strDttFrom,
            strDttTo, bundle.getContext().getUserClient(), ple.getOrganization().getId());

        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Saldo de Intangibles - 34")) {
        OBContext.setAdminMode();

        rv_validto = period.getEndingDate();
        if (strValidto != null && !strValidto.isEmpty()) {
          Date validto = sdf.parse(strValidto);
          if (validto != null) {
            rv_validto = validto;
            ismanualdate = true;
          }
        }
        pleData = ReportLibroInventariosYBalance34.getPLECuenta34(conProvider, vars, strDttFrom,
            strDttTo, bundle.getContext().getUserClient(), ple.getOrganization().getId());

        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Detalle del Saldo de la cuenta - 37 y 49")) {
        OBContext.setAdminMode();

        pleData = ReportLibroInventariosYBalance37y49.getPLECuenta37y49(conProvider, vars,
            strDttFrom, strDttTo, bundle.getContext().getUserClient(), ple.getOrganization()
                .getId());

        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Cuentas por Pagar Remuneraciones/Participaciones - 41")) {
        OBContext.setAdminMode();

        rv_validto = period.getEndingDate();
        if (strValidto != null && !strValidto.isEmpty()) {
          Date validto = sdf.parse(strValidto);
          if (validto != null) {
            rv_validto = validto;
            ismanualdate = true;
          }
        }
        pleData = SunatUtil.getCuentasxPagarComercialesRelacionadas_41(conProvider, rv_validto,
            ple.getOrganization());

        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Cuentas por Pagar Comerciales/Relacionadas - 42 y 43")) {
        OBContext.setAdminMode();

        pleData = ReportLibroInventariosYBalance42y43.getStructPLECuenta42y43(conProvider, vars,
            rv_validfrom, rv_validto, ple.getOrganization().getId());

        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Cuentas por Pagar Diversas - Terceros -46-47")) {
        OBContext.setAdminMode();

        rv_validto = period.getEndingDate();
        if (strValidto != null && !strValidto.isEmpty()) {
          Date validto = sdf.parse(strValidto);
          if (validto != null) {
            rv_validto = validto;
            ismanualdate = true;
          }
        }
        pleData = ReportLibroInventariosYBalance46.getStructPLECuenta46y47(conProvider, vars, rv_validfrom,
            rv_validto, ple.getOrganization().getId());

        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Detalle Saldo de la Cuenta 50-1")) {
        OBContext.setAdminMode();

        rv_validto = period.getEndingDate();
        if (strValidto != null && !strValidto.isEmpty()) {
          Date validto = sdf.parse(strValidto);
          if (validto != null) {
            rv_validto = validto;
            ismanualdate = true;
          }
        }

        String strTreeOrg = TreeData.getTreeOrg(conProvider, ple.getClient().getId());
        String strOrgFamily = getFamilyWith0(conProvider, strTreeOrg, ple.getOrganization().getId());

        pleData = ReporteDetalleCuenta50Capital.getPLE_detalle50_1(conProvider, vars, ple
            .getClient().getId(), rv_validto, ple.getOrganization(), strOrgFamily);

        OBContext.restorePreviousMode();
      } else if (ple.getName().equals("Estructura Saldo de la Cuenta 50-2")) {
        OBContext.setAdminMode();

        rv_validto = period.getEndingDate();
        if (strValidto != null && !strValidto.isEmpty()) {
          Date validto = sdf.parse(strValidto);
          if (validto != null) {
            rv_validto = validto;
            ismanualdate = true;
          }
        }

        String strTreeOrg = TreeData.getTreeOrg(conProvider, ple.getClient().getId());
        String strOrgFamily = getFamilyWith0(conProvider, strTreeOrg, ple.getOrganization().getId());

        pleData = ReporteDetalleCuenta50Capital.getPLE_detalle50_2(conProvider, vars, ple
            .getClient().getId(), rv_validto, ple.getOrganization(), strOrgFamily);

        OBContext.restorePreviousMode();
      } else if (ple.getName().equals("Estado de Situación Financiera")) {
        OBContext.setAdminMode();

        pleData = ReporteEstadoSituacionFinanciera.getPLE(conProvider, vars, strDttFrom, strDttTo,
            bundle.getContext().getUserClient(), ple.getOrganization().getId());

        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Estado de Resultados")) {
        OBContext.setAdminMode();

        rv_validto = period.getEndingDate();
        if (strValidto != null && !strValidto.isEmpty()) {
          Date validto = sdf.parse(strValidto);
          if (validto != null) {
            rv_validto = validto;
            ismanualdate = true;
          }
        }

        String strTreeOrg = TreeData.getTreeOrg(conProvider, ple.getClient().getId());
        String strOrgFamily = getFamilyWith0(conProvider, strTreeOrg, ple.getOrganization().getId());

        pleData = ReporteEstadoResultados.getEstadoResultadosToPLE(conProvider, vars, ple
            .getClient().getId(), rv_validto, ple.getOrganization(), strOrgFamily);

        /*
         * pleData = SunatUtil.getCuentasxCobrarComercialesRelacionadas_12_13(conProvider,
         * rv_validto, ple.getOrganization());
         */

        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Balance de Comprobación")) {
        OBContext.setAdminMode();

        pleData = ReportTrialBalance.getPLE(conProvider, vars, strDttFrom, strDttTo, bundle
            .getContext().getUserClient(), ple.getOrganization().getId());

        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Estado de Cambios en el Patrimonio Neto")) {
        OBContext.setAdminMode();

        pleData = ReporteControlPatrimonio.getPLE(conProvider, vars, strDttFrom, strDttTo, bundle
            .getContext().getUserClient(), ple.getOrganization().getId());

        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Estado de Resultados Integrales")) {
        OBContext.setAdminMode();

        rv_validto = period.getEndingDate();
        if (strValidto != null && !strValidto.isEmpty()) {
          Date validto = sdf.parse(strValidto);
          if (validto != null) {
            rv_validto = validto;
            ismanualdate = true;
          }
        }

        String strTreeOrg = TreeData.getTreeOrg(conProvider, ple.getClient().getId());
        String strOrgFamily = getFamilyWith0(conProvider, strTreeOrg, ple.getOrganization().getId());

        pleData = ReporteEstadoResultadosIntegrales.getEstadoResultadosIntegralesToPLE(conProvider,
            vars, ple.getClient().getId(), rv_validto, ple.getOrganization(), strOrgFamily);

        OBContext.restorePreviousMode();

      } else if (ple.getName().equals("Estado de Flujos de Efectivo")) {
        OBContext.setAdminMode();

        pleData = ReporteEstadoFlujoEfectivo.getPLE(conProvider, vars, strDttFrom, strDttTo, bundle
            .getContext().getUserClient(), ple.getOrganization().getId());

        OBContext.restorePreviousMode();

      } else {
        System.out.println("ple.getName(): " + ple.getName());
        throw new OBException("@SCO_ErrorNoExistsPLE@");
      }

      // save detail
      OBCriteria<SCOPleDetail> obCriteria = OBDal.getInstance().createCriteria(SCOPleDetail.class);

      Query q = OBDal
          .getInstance()
          .getSession()
          .createSQLQuery(
              "SELECT COALESCE(MAX(LINE),0)+10 AS DefaultValue FROM SCO_PLE_DETAIL WHERE SCO_PLE_ID='"
                  + ple.getId() + "'");
      int lineno = ((BigDecimal) q.uniqueResult()).intValue();

      obCriteria.add(Restrictions.eq(SCOPleDetail.PROPERTY_SCOPLE, ple));
      obCriteria.add(Restrictions.eq(SCOPleDetail.PROPERTY_PERIOD, period));
      if (ple.getName().startsWith("FACE") && ismanualdate) {
        obCriteria.add(Restrictions.eq(SCOPleDetail.PROPERTY_VALIDFROMDATE, rv_validfrom));
      } else {
        obCriteria.add(Restrictions.isNull(SCOPleDetail.PROPERTY_VALIDFROMDATE));

      }
      SCOPleDetail pleDetail = null;
      if (obCriteria.list().size() != 0) {
        pleDetail = obCriteria.list().get(0);

      } else {
        pleDetail = OBProvider.getInstance().get(SCOPleDetail.class);
        pleDetail.setLineNo((long) lineno);
        pleDetail.setSCOPle(ple);
        pleDetail.setPeriod(period);
        pleDetail.setOrganization(ple.getOrganization());

        if (ple.getName().startsWith("FACE") && ismanualdate) {
          pleDetail.setValidFromDate(rv_validfrom);
          pleDetail.setValidToDate(rv_validto);
        }
      }

      pleDetail.setLastGeneratedDate(new Date());
      pleDetail.setNumEntries((long) pleData.numEntries);
      OBDal.getInstance().save(pleDetail);

      // save as attachment
      String directory = TabAttachments.getAttachmentDirectoryForNewAttachments(
          "9B239A921B544D9B8CCE9AFDC32EE203", pleDetail.getId());
      String attachPath = OBPropertiesProvider.getInstance().getOpenbravoProperties()
          .getProperty("attach.path");

      File attachDirectory = new File(attachPath + "/" + directory);

      Date dateNow = new Date();
      SimpleDateFormat dateformatyyyyMMdd = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
      String strDate = dateformatyyyyMMdd.format(dateNow);

      String filename = pleData.filename;

      // Lookup for the filename in the attachments of this detail.If it
      // exists then do nothing
      // else create new attachment

      String strFileId = TabAttachmentsData.selectUniq(conProvider, pleDetail.getClient().getId(),
          pleDetail.getOrganization().getId(), "9B239A921B544D9B8CCE9AFDC32EE203",
          pleDetail.getId(), directory, filename);
      if (strFileId == null) {
        attachDirectory.mkdirs();
      }

      BufferedWriter writer = null;
      writer = new BufferedWriter(new FileWriter(attachPath + "/" + directory + "/" + filename));
      writer.write(pleData.data);
      writer.close();

      if (strFileId == null) {
        final String newFileId = SequenceIdData.getUUID();
        try {
          TabAttachmentsData.insert(conn, conProvider, newFileId, pleDetail.getClient().getId(),
              pleDetail.getOrganization().getId(), user.getId(),
              "9B239A921B544D9B8CCE9AFDC32EE203", pleDetail.getId(), "100", "Generated by UGO ",
              filename, directory);
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

  private String getFamily(ConnectionProvider connProvider, String strTree, String strChild)
      throws IOException, ServletException {
    return Tree.getMembers(connProvider, strTree, (strChild == null || strChild.equals("")) ? "0"
        : strChild);

  }

  private String getFamilyWith0(ConnectionProvider connProvider, String strTree, String strChild)
      throws IOException, ServletException {
    return Tree.getMembers(connProvider, strTree, (strChild == null || strChild.equals("")) ? "0"
        : strChild)
        + ",'0'";

  }

}