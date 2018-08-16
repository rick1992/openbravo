package pe.com.unifiedgo.accounting.process;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.provider.OBProvider;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.model.ad.system.Client;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessLogger;
import org.openbravo.service.db.DalBaseProcess;
import org.openbravo.service.db.DalConnectionProvider;
import org.quartz.JobExecutionException;

import pe.com.unifiedgo.core.addons.AddonsCentral;
import pe.com.unifiedgo.core.addons.TipoCambio;

public class SCO_AutomaticConvertRateProcess extends DalBaseProcess {
  private static ProcessLogger logger;
  private ConnectionProvider connection;

  private final static String SYSTEM_CLIENT_ID = "0";

  public void doExecute(ProcessBundle bundle) throws Exception {

    logger = bundle.getLogger();
    connection = bundle.getConnection();

    VariablesSecureApp vars = bundle.getContext().toVars();
    if (vars.getClient().equals(SYSTEM_CLIENT_ID)) {
      OBCriteria<Client> obc = OBDal.getInstance().createCriteria(Client.class);
      obc.add(Restrictions.not(Restrictions.eq(Client.PROPERTY_ID, SYSTEM_CLIENT_ID)));
      for (Client c : obc.list()) {
        final VariablesSecureApp vars1 = new VariablesSecureApp(bundle.getContext().getUser(),
            c.getId(), bundle.getContext().getOrganization());
        processClient(vars1, bundle);
      }
    } else {
      processClient(vars, bundle);
    }

  }

  private TipoCambio getTipoCambio(List<TipoCambio> lsTipoCambio, int mes, int dia) {
    for (int i = 0; i < lsTipoCambio.size(); i++) {
      if (lsTipoCambio.get(i).day == dia && lsTipoCambio.get(i).month == mes)
        return lsTipoCambio.get(i);
    }
    return null;
  }

  private void processClient(VariablesSecureApp vars, ProcessBundle bundle) throws Exception {
    ConnectionProvider conn = new DalConnectionProvider();
    Client client = OBDal.getInstance().get(Client.class, vars.getClient());
    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    Date todaydate = formatter.parse(formatter.format(new Date()));
    Calendar ctoday = Calendar.getInstance();
    ctoday.setTime(todaydate);

    try {
      OBContext.setAdminMode(false);

      List<TipoCambio> lsTipoCambio = null;
      Calendar calendarBefore = null;

      boolean noerror = false;
      int numTries = 0;
      while (!noerror && numTries < 5) {
        try {
          noerror = true;
          numTries++;
          lsTipoCambio = AddonsCentral.getListTipoCambio(String.valueOf(ctoday.get(Calendar.YEAR)),
              String.valueOf(ctoday.get(Calendar.MONTH) + 1));

          calendarBefore = Calendar.getInstance();
          calendarBefore.setTime(ctoday.getTime());
          calendarBefore.add(Calendar.DAY_OF_MONTH, -7);

          // analizar los ultimos 7 dias
          if (ctoday.get(Calendar.MONTH) != calendarBefore.get(Calendar.MONTH)) {
            List<TipoCambio> lsTipoCambioMonthBefore = AddonsCentral.getListTipoCambio(
                String.valueOf(calendarBefore.get(Calendar.YEAR)),
                String.valueOf(calendarBefore.get(Calendar.MONTH) + 1));
            lsTipoCambioMonthBefore.addAll(lsTipoCambio);
            lsTipoCambio = lsTipoCambioMonthBefore;
          }

        } catch (Exception exx) {
          noerror = false;
        }
      }

      if (noerror && lsTipoCambio.size() != 0) {

        String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
            .getProperty("dateFormat.java");
        SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);

        Currency currPen = OBDal.getInstance().get(Currency.class, "308");
        Currency currUsd = OBDal.getInstance().get(Currency.class, "100");

        TipoCambio tcBehind = null;
        int numDiasBehind = 0;// dias sin tipo de cambio hacia atras (sabados y domingos o feriados)
        for (int i = 0; i < 7; i++) {// solo 7 dias
          calendarBefore.add(Calendar.DAY_OF_MONTH, 1);
          TipoCambio tc = getTipoCambio(lsTipoCambio, calendarBefore.get(Calendar.MONTH) + 1,
              calendarBefore.get(Calendar.DAY_OF_MONTH));
          if (tc == null) {
            numDiasBehind++;
            continue;
          }

          // poner tc
          if (tcBehind != null) {
            for (int j = 1; j <= numDiasBehind; j++) {
              Calendar c = Calendar.getInstance();
              c.setTime(calendarBefore.getTime());
              c.add(Calendar.DAY_OF_MONTH, -j);

              // Si no existe el tipo de cambio
              String strConvrateDate = sdf.format(c.getTime());
              SCOAutomaticConvertRateProcessData[] data = SCOAutomaticConvertRateProcessData
                  .getExchangeRateUSDPurch(connection, vars.getClient(), vars.getOrg(),
                      strConvrateDate);

              if (data == null || data.length == 0) {

                ConversionRate cr = OBProvider.getInstance().get(ConversionRate.class);

                cr.setClient(client);
                cr.setOrganization(OBDal.getInstance().get(Organization.class, "0"));
                cr.setConversionRateType("S");
                cr.setCurrency(currPen);
                cr.setToCurrency(currUsd);
                cr.setValidFromDate(c.getTime());
                cr.setValidToDate(c.getTime());
                cr.setDivideRateBy(new BigDecimal(tcBehind.tcCompra));
                cr.setMultipleRateBy(BigDecimal.ONE.divide(new BigDecimal(tcBehind.tcCompra), 15,
                    RoundingMode.HALF_UP));
                cr.setSCODescripcin("T/C Compra a Dólares");
                OBDal.getInstance().save(cr);

                cr = OBProvider.getInstance().get(ConversionRate.class);

                cr.setClient(client);
                cr.setOrganization(OBDal.getInstance().get(Organization.class, "0"));
                cr.setConversionRateType("S");
                cr.setCurrency(currUsd);
                cr.setToCurrency(currPen);
                cr.setValidFromDate(c.getTime());
                cr.setValidToDate(c.getTime());
                cr.setDivideRateBy(BigDecimal.ONE.divide(new BigDecimal(tcBehind.tcVenta), 15,
                    RoundingMode.HALF_UP));
                cr.setMultipleRateBy(new BigDecimal(tcBehind.tcVenta));
                cr.setSCODescripcin("T/C Venta a Dólares");
                OBDal.getInstance().save(cr);
              }
            }
          }

          // Ahora el tc del dia

          // Si no existe el tipo de cambio
          String strConvrateDate = sdf.format(calendarBefore.getTime());
          SCOAutomaticConvertRateProcessData[] data = SCOAutomaticConvertRateProcessData
              .getExchangeRateUSDPurch(connection, vars.getClient(), vars.getOrg(),
                  strConvrateDate);

          if (data == null || data.length == 0) {

            ConversionRate cr = OBProvider.getInstance().get(ConversionRate.class);

            cr.setClient(client);
            cr.setOrganization(OBDal.getInstance().get(Organization.class, "0"));
            cr.setConversionRateType("S");
            cr.setCurrency(currPen);
            cr.setToCurrency(currUsd);
            cr.setValidFromDate(calendarBefore.getTime());
            cr.setValidToDate(calendarBefore.getTime());
            cr.setDivideRateBy(new BigDecimal(tc.tcCompra).setScale(3, RoundingMode.HALF_UP));
            cr.setMultipleRateBy(
                BigDecimal.ONE.divide(new BigDecimal(tc.tcCompra).setScale(3, RoundingMode.HALF_UP),
                    15, RoundingMode.HALF_UP));
            cr.setSCODescripcin("T/C Compra a Dólares");
            OBDal.getInstance().save(cr);

            cr = OBProvider.getInstance().get(ConversionRate.class);

            cr.setClient(client);
            cr.setOrganization(OBDal.getInstance().get(Organization.class, "0"));
            cr.setConversionRateType("S");
            cr.setCurrency(currUsd);
            cr.setToCurrency(currPen);
            cr.setValidFromDate(calendarBefore.getTime());
            cr.setValidToDate(calendarBefore.getTime());
            cr.setDivideRateBy(
                BigDecimal.ONE.divide(new BigDecimal(tc.tcVenta).setScale(3, RoundingMode.HALF_UP),
                    15, RoundingMode.HALF_UP));
            cr.setMultipleRateBy(new BigDecimal(tc.tcVenta).setScale(3, RoundingMode.HALF_UP));
            cr.setSCODescripcin("T/C Venta a Dólares");
            OBDal.getInstance().save(cr);
          }

          tcBehind = tc;
          numDiasBehind = 0;
        }

        OBDal.getInstance().flush();
        logger.log("Automatic Convert Rate Process was Executed for Client:"
            + client.getIdentifier() + "\n");
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new JobExecutionException(e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

  private void processClient_bk(VariablesSecureApp vars, ProcessBundle bundle) throws Exception {
    ConnectionProvider conn = new DalConnectionProvider();
    System.out.println("**** SCO_AutomaticConvertRateProcess...Inicio");
    String DATEFORMAT_JAVA = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT_JAVA);

    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

    Client client = OBDal.getInstance().get(Client.class, vars.getClient());
    Date todaydate = formatter.parse(formatter.format(new Date()));
    System.out.println("**** SCO_AutomaticConvertRateProcess...todaydate:" + todaydate);
    Calendar ctoday = Calendar.getInstance();
    ctoday.setTime(todaydate);
    System.out.println("**** SCO_AutomaticConvertRateProcess...ctoday:" + ctoday.toString());
    try {
      OBContext.setAdminMode(false);

      List<TipoCambio> lsTipoCambio = null;
      Calendar calendarBefore = null;
      System.out.println("**** SCO_AutomaticConvertRateProcess...Processing");
      boolean noerror = false;
      int numTries = 0;
      while (!noerror && numTries < 5) {
        // Leyendo los tipo de cambio de la pagina sunat en lsTipoCambio
        try {
          noerror = true;
          numTries++;
          System.out.println("**** SCO_AutomaticConvertRateProcess...try " + numTries);

          System.out
              .println("**** SCO_AutomaticConvertRateProcess...Leyendo tipo de cambio del mes");
          lsTipoCambio = AddonsCentral.getListTipoCambio(String.valueOf(ctoday.get(Calendar.YEAR)),
              String.valueOf(ctoday.get(Calendar.MONTH) + 1));
          for (int i = 0; i < lsTipoCambio.size(); i++) {
            System.out.println("**** SCO_AutomaticConvertRateProcess...lsTipoCambio[" + i + "]"
                + " - T/C:" + lsTipoCambio.get(i).toString());
          }
          calendarBefore = Calendar.getInstance();
          calendarBefore.setTime(ctoday.getTime());
          calendarBefore.add(Calendar.DAY_OF_MONTH, -7);
          System.out.println("**** SCO_AutomaticConvertRateProcess...calendar before: "
              + calendarBefore.toString());
          // analizar los ultimos 7 dias
          if (ctoday.get(Calendar.MONTH) != calendarBefore.get(Calendar.MONTH)) {
            System.out.println(
                "**** SCO_AutomaticConvertRateProcess...Leyendo tipo de cambio del mes pasado");
            List<TipoCambio> lsTipoCambioMonthBefore = AddonsCentral.getListTipoCambio(
                String.valueOf(calendarBefore.get(Calendar.YEAR)),
                String.valueOf(calendarBefore.get(Calendar.MONTH) + 1));
            for (int i = 0; i < lsTipoCambioMonthBefore.size(); i++) {
              System.out.println("**** SCO_AutomaticConvertRateProcess...lsTipoCambioMonthBefore["
                  + i + "]" + " - T/C:" + lsTipoCambioMonthBefore.get(i).toString());
            }
            lsTipoCambioMonthBefore.addAll(lsTipoCambio);
            lsTipoCambio = lsTipoCambioMonthBefore;
            for (int i = 0; i < lsTipoCambio.size(); i++) {
              System.out.println("**** SCO_AutomaticConvertRateProcess...FINAL-lsTipoCambio[" + i
                  + "]" + " - T/C:" + lsTipoCambio.get(i).toString());
            }
          }

        } catch (Exception exx) {
          noerror = false;
          System.out.println(
              "**** SCO_AutomaticConvertRateProcess...catch ... error:" + exx.getMessage());
        }
      }

      String strConvrateDate;
      SCOAutomaticConvertRateProcessData[] data;
      System.out
          .println("**** SCO_AutomaticConvertRateProcess...calculando tipo de cambio para hoy");
      if (noerror && lsTipoCambio.size() != 0) {
        Currency currPen = OBDal.getInstance().get(Currency.class, "308");
        Currency currUsd = OBDal.getInstance().get(Currency.class, "100");
        System.out.println(
            "**** SCO_AutomaticConvertRateProcess...catch ... ctoday:" + ctoday.toString());
        // Revisando si existe tipo de cambio para hoy, que ya este insertado en el sistema
        strConvrateDate = sdf.format(ctoday.getTime());
        data = SCOAutomaticConvertRateProcessData.getExchangeRateUSDPurch(connection,
            vars.getClient(), vars.getOrg(), strConvrateDate);

        // No existe tipo de cambio para hoy
        if (data == null || data.length == 0) {
          System.out.println(
              "**** SCO_AutomaticConvertRateProcess...No existe tipo de cambio para hoy.. buscar en SUNAT");
          // Leyendo el tipo de cambio de la sunat para hoy
          TipoCambio tcHoy = getTipoCambio(lsTipoCambio, ctoday.get(Calendar.MONTH) + 1,
              ctoday.get(Calendar.DAY_OF_MONTH));
          if (tcHoy != null) {
            // Seteando tipo de cambio de sunat
            System.out
                .println("**** SCO_AutomaticConvertRateProcess...tcHoy SUNAT:" + tcHoy.toString());
            ConversionRate cr = OBProvider.getInstance().get(ConversionRate.class);

            cr.setClient(client);
            cr.setOrganization(OBDal.getInstance().get(Organization.class, "0"));
            cr.setConversionRateType("S");
            cr.setCurrency(currPen);
            cr.setToCurrency(currUsd);
            cr.setValidFromDate(ctoday.getTime());
            cr.setValidToDate(ctoday.getTime());
            cr.setDivideRateBy(new BigDecimal(tcHoy.tcCompra).setScale(3, RoundingMode.HALF_UP));
            cr.setMultipleRateBy(BigDecimal.ONE.divide(
                new BigDecimal(tcHoy.tcCompra).setScale(3, RoundingMode.HALF_UP), 15,
                RoundingMode.HALF_UP));
            cr.setSCODescripcin("T/C Compra a Dólares");
            OBDal.getInstance().save(cr);

            cr = OBProvider.getInstance().get(ConversionRate.class);

            cr.setClient(client);
            cr.setOrganization(OBDal.getInstance().get(Organization.class, "0"));
            cr.setConversionRateType("S");
            cr.setCurrency(currUsd);
            cr.setToCurrency(currPen);
            cr.setValidFromDate(ctoday.getTime());
            cr.setValidToDate(ctoday.getTime());
            cr.setDivideRateBy(BigDecimal.ONE.divide(
                new BigDecimal(tcHoy.tcVenta).setScale(3, RoundingMode.HALF_UP), 15,
                RoundingMode.HALF_UP));
            cr.setMultipleRateBy(new BigDecimal(tcHoy.tcVenta).setScale(3, RoundingMode.HALF_UP));
            cr.setSCODescripcin("T/C Venta a Dólares");
            OBDal.getInstance().save(cr);

            System.out.println("**** SCO_AutomaticConvertRateProcess...tcHoy SUNAT Seteado");

          } else {
            // Tipo de cambio de la sunat no encontrado para hoy
            System.out.println("**** SCO_AutomaticConvertRateProcess...NO tcHoy SUNAT");
            calendarBefore = Calendar.getInstance();
            calendarBefore.setTime(ctoday.getTime());
            boolean existsTC = false;
            System.out.println(
                "**** SCO_AutomaticConvertRateProcess...calendarBefore(nuevo tcHOY variable):"
                    + calendarBefore.toString());
            // Buscar tc de hasta 7 dias atras
            System.out.println(
                "**** SCO_AutomaticConvertRateProcess...Buscando en SUNAT tipo de cambio de dias anteriores");
            for (int i = 0; i < 7; i++) {
              System.out
                  .println("**** SCO_AutomaticConvertRateProcess...SUNAT tipo de cambio - menos "
                      + i + 1 + " dias");
              if (existsTC) {
                continue;
              }
              System.out.println("**** SCO_AutomaticConvertRateProcess...existsTC:" + existsTC);
              calendarBefore.add(Calendar.DAY_OF_MONTH, -1);
              System.out.println("**** SCO_AutomaticConvertRateProcess...nuevo calendar:"
                  + calendarBefore.toString());
              // Verificar si existe tipo de cambio de la sunat para el dia anterior
              TipoCambio tcBefore = getTipoCambio(lsTipoCambio,
                  calendarBefore.get(Calendar.MONTH) + 1,
                  calendarBefore.get(Calendar.DAY_OF_MONTH));
              if (tcBefore != null) {
                // Seteando tipo de cambio de sunat
                System.out.println(
                    "**** SCO_AutomaticConvertRateProcess...tc calendar before encontrado EN SUNAT");

                System.out.println("**** SCO_AutomaticConvertRateProcess...tc calendar before:"
                    + tcBefore.toString());
                ConversionRate cr = OBProvider.getInstance().get(ConversionRate.class);

                cr.setClient(client);
                cr.setOrganization(OBDal.getInstance().get(Organization.class, "0"));
                cr.setConversionRateType("S");
                cr.setCurrency(currPen);
                cr.setToCurrency(currUsd);
                cr.setValidFromDate(ctoday.getTime());
                cr.setValidToDate(ctoday.getTime());
                cr.setDivideRateBy(
                    new BigDecimal(tcBefore.tcCompra).setScale(3, RoundingMode.HALF_UP));
                cr.setMultipleRateBy(BigDecimal.ONE.divide(
                    new BigDecimal(tcBefore.tcCompra).setScale(3, RoundingMode.HALF_UP), 15,
                    RoundingMode.HALF_UP));
                cr.setSCODescripcin("T/C Compra a Dólares");
                OBDal.getInstance().save(cr);

                cr = OBProvider.getInstance().get(ConversionRate.class);

                cr.setClient(client);
                cr.setOrganization(OBDal.getInstance().get(Organization.class, "0"));
                cr.setConversionRateType("S");
                cr.setCurrency(currUsd);
                cr.setToCurrency(currPen);
                cr.setValidFromDate(ctoday.getTime());
                cr.setValidToDate(ctoday.getTime());
                cr.setDivideRateBy(BigDecimal.ONE.divide(
                    new BigDecimal(tcBefore.tcVenta).setScale(3, RoundingMode.HALF_UP), 15,
                    RoundingMode.HALF_UP));
                cr.setMultipleRateBy(
                    new BigDecimal(tcBefore.tcVenta).setScale(3, RoundingMode.HALF_UP));
                cr.setSCODescripcin("T/C Venta a Dólares");
                OBDal.getInstance().save(cr);

                existsTC = true;

                System.out.println("**** SCO_AutomaticConvertRateProcess...tcHoy SUNAT Seteado");

              } else {
                System.out
                    .println("**** SCO_AutomaticConvertRateProcess...tc no encontrado en SUNAT");
              }

            }
          }

        }

        OBDal.getInstance().flush();
        logger.log("Automatic Convert Rate Process was Executed for Client:"
            + client.getIdentifier() + "\n");
      }

    } catch (Exception e) {
      e.printStackTrace();
      throw new JobExecutionException(e.getMessage(), e);
    } finally {
      OBContext.restorePreviousMode();
    }
  }

}
