package pe.com.unifiedgo.imports.ad_callouts;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.hibernate.criterion.Restrictions;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.model.common.order.Order;

import pe.com.unifiedgo.imports.data.SimFolioImport;

public class sim_folio_exchange_rate extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    // example
    String mOrgId = info.vars.getStringParameter("inpadOrgId").replace(",", "");
    String mFolioId = info.vars.getStringParameter("inpsimFolioimportId").replace(",", "");
    String mVolumen = info.vars.getStringParameter("inptotalCubicaje").replace(",", "");
    String mFactorOne = info.vars.getStringParameter("inpfleteFactorOne").replace(",", "");
    String mFactorBl = info.vars.getStringParameter("inpfleteBl").replace(",", "");
    String mFleteOtros = info.vars.getStringParameter("inpfleteOtros").replace(",", "");

    String mSeguroPercentfob = info.vars.getStringParameter("inpcifSeguroPercentfob").replace(",", "");

    String mCurrencyFolio = "308"; // Soles
    String mCurrencyDollar = "100"; // Dólar
    String mFactorTwo = info.vars.getStringParameter("inpaduanaFactorTwo").replace(",", "");
    String mWeight = info.vars.getStringParameter("inptotalPeso").replace(",", "");
    String mFactorThree = info.vars.getStringParameter("inpaduanaFactorThree").replace(",", "");
    String mPercenttoCif = info.vars.getStringParameter("inpagentPercentCif").replace(",", "");
    String mShipper = info.vars.getStringParameter("inpshippingTotal").replace(",", "");
    String mConvertionRate = info.vars.getStringParameter("inpaduanaExchangeRate").replace(",", "");

    mVolumen = verifyNumber(info, "inptotalCubicaje", mVolumen);
    mFactorOne = verifyNumber(info, "inpfleteFactorOne", mFactorOne);
    mFactorBl = verifyNumber(info, "inpfleteBl", mFactorBl);
    mFleteOtros = verifyNumber(info, "inpfleteOtros", mFleteOtros);
    mFactorTwo = verifyNumber(info, "inpaduanaFactorTwo", mFactorTwo);
    mWeight = verifyNumber(info, "inptotalPeso", mWeight);
    mFactorThree = verifyNumber(info, "inpaduanaFactorThree", mFactorThree);
    mPercenttoCif = verifyNumber(info, "inpagentPercentCif", mPercenttoCif);
    mShipper = verifyNumber(info, "inpshippingTotal", mShipper);
    mSeguroPercentfob = verifyNumber(info, "inpshippingTotal", mSeguroPercentfob);
    mConvertionRate = verifyNumber(info, "inpaduanaExchangeRate", mConvertionRate);
    BigDecimal valor_fob = getFobfromFolio(mFolioId);

    if (valor_fob.compareTo(BigDecimal.ZERO) == 0) {
      String mvalor_fob_field = "";
      mvalor_fob_field = info.vars.getStringParameter("inpcifFob").replace(",", "");
      mvalor_fob_field = verifyNumber(info, "inpagentPercentCif", mvalor_fob_field);
      valor_fob = new BigDecimal(mvalor_fob_field);
    }

    BigDecimal tmp_flete = (new BigDecimal(mVolumen)).multiply(new BigDecimal(mFactorOne));
    BigDecimal total_flete = tmp_flete.add(new BigDecimal(mFactorBl)).add(new BigDecimal(mFleteOtros));
    // Cálculo del Porcentaje de FLete
    if (!valor_fob.equals(BigDecimal.ZERO)) {
      BigDecimal percetFleteforFob = (tmp_flete.divide(valor_fob, 3, RoundingMode.HALF_UP)).multiply(new BigDecimal(100));

    }

    BigDecimal SeguroPercentFob = (new BigDecimal(mSeguroPercentfob)).divide(new BigDecimal(100), 5, RoundingMode.HALF_UP);

    BigDecimal cif_seguro = SeguroPercentFob.multiply(valor_fob.add(total_flete));
    BigDecimal cif_total = valor_fob.add(total_flete).add(cif_seguro);

    BigDecimal tmp_desaduanaje = (new BigDecimal(mFactorTwo)).multiply(new BigDecimal(mWeight)).divide(new BigDecimal(1000), 3, RoundingMode.HALF_UP);
    tmp_desaduanaje = tmp_desaduanaje.add(new BigDecimal(mFactorThree));

    if (mConvertionRate == null || mConvertionRate.equals("")) {
      mConvertionRate = "1";
    } else if (mConvertionRate.equals("0")) {
      mConvertionRate = "1";
    }

    // System.out.println("mConvertionRate " + mConvertionRate);

    BigDecimal tmp_exchangeRate = new BigDecimal(mConvertionRate);

    // System.out.println("tmp_exchangeRate : " +tmp_exchangeRate);

    if (tmp_exchangeRate == null || tmp_exchangeRate.compareTo(BigDecimal.ZERO) == 0) {
      tmp_exchangeRate = new BigDecimal(1);
      info.addResult("inpaduanaExchangeRate", tmp_exchangeRate);
    }

    // System.out.println("tmp_exchangeRate : " +tmp_exchangeRate);

    BigDecimal tmp_aduana = tmp_desaduanaje.divide(tmp_exchangeRate, 3, RoundingMode.HALF_UP);

    // Desaduanaje
    info.addResult("inpaduanaTotal", tmp_aduana);
    // mPercenttoCif
    BigDecimal AgenteAduana = (new BigDecimal(mPercenttoCif)).multiply(cif_total).divide(new BigDecimal(100), 3, RoundingMode.HALF_UP);
    info.addResult("inpagentTotal", AgenteAduana);

    BigDecimal transportista = new BigDecimal(mShipper);
    BigDecimal TotalDesaduanaje = tmp_aduana.add(AgenteAduana).add(transportista);

    info.addResult("inptotalDesaduanaje", TotalDesaduanaje);

    if (!valor_fob.equals(BigDecimal.ZERO)) {
      BigDecimal porcentajeDesaduanajedeFOB = (TotalDesaduanaje.divide(valor_fob, 3, RoundingMode.HALF_UP)).multiply(new BigDecimal(100));
      info.addResult("inpaduanaPercentToFob", porcentajeDesaduanajedeFOB);
    } else {
      info.addResult("inpaduanaPercentToFob", BigDecimal.ZERO);
    }

  }

  public String verifyNumber(CalloutInfo info, String inpParameter, String cadena) {
    try {
      if (cadena == null || cadena.equals("") || Float.parseFloat(cadena) == 0)
        cadena = "0";
    } catch (Exception e) {
      cadena = "0";
      info.addResult(inpParameter, cadena);
    }
    return cadena;
  }

  public BigDecimal getFobfromFolio(String FolioID) {
    SimFolioImport folio = OBDal.getInstance().get(SimFolioImport.class, FolioID);
    OBCriteria<Order> partials = OBDal.getInstance().createCriteria(Order.class);
    partials.add(Restrictions.eq(Order.PROPERTY_SIMFOLIOIMPORT, folio));
    List<Order> partialList = partials.list();
    BigDecimal grantotal = BigDecimal.ZERO;
    for (int i = 0; i < partialList.size(); i++) {
      grantotal = grantotal.add(partialList.get(i).getGrandTotalAmount());
    }
    return grantotal;
  }

  private ConversionRate getConversionRate(Currency fromCurrency, Currency toCurrency, Date _conversionDate, Organization org) {

    // TRUNCATE DATE
    Date conversionDate = null;
    try {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      conversionDate = sdf.parse(sdf.format(_conversionDate));
    } catch (Exception e) {
      return null;
    }

    java.util.List<ConversionRate> conversionRateList;
    ConversionRate conversionRate;
    OBContext.setAdminMode(true);
    try {
      final OBCriteria<ConversionRate> obcConvRate = OBDal.getInstance().createCriteria(ConversionRate.class);
      obcConvRate.setFilterOnReadableOrganization(false);
      obcConvRate.add(Restrictions.eq(ConversionRate.PROPERTY_ORGANIZATION, org));
      obcConvRate.add(Restrictions.eq(ConversionRate.PROPERTY_CURRENCY, fromCurrency));
      obcConvRate.add(Restrictions.eq(ConversionRate.PROPERTY_TOCURRENCY, toCurrency));
      obcConvRate.add(Restrictions.le(ConversionRate.PROPERTY_VALIDFROMDATE, conversionDate));
      obcConvRate.add(Restrictions.ge(ConversionRate.PROPERTY_VALIDTODATE, conversionDate));
      conversionRateList = obcConvRate.list();
      if ((conversionRateList != null) && (conversionRateList.size() != 0)) {
        conversionRate = conversionRateList.get(0);
      } else {
        if ("0".equals(org.getId())) {
          conversionRate = null;
        } else {
          return getConversionRate(fromCurrency, toCurrency, conversionDate, OBDal.getInstance().get(Organization.class, OBContext.getOBContext().getOrganizationStructureProvider().getParentOrg(org.getId())));
        }
      }
    } catch (Exception e) {
      log4j.error(e);
      return null;
    } finally {
      OBContext.restorePreviousMode();
    }
    return conversionRate;
  }

}
