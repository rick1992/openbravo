package pe.com.unifiedgo.imports.ad_callouts;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import org.hibernate.criterion.Restrictions;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBCriteria;
//import org.apache.jasper.tagplugins.jstl.core.Out;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.ad_callouts.SimpleCallout;
import org.openbravo.model.common.currency.ConversionRate;
import org.openbravo.model.common.currency.Currency;
import org.openbravo.model.common.enterprise.Organization;

import pe.com.unifiedgo.imports.data.SimImpSettlement;
import pe.com.unifiedgo.imports.data.SimImpSettlementLines;

public class sim_imp_settlement_process extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

    // Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
    // { System.out.println(params.nextElement()); }

    // SIM_Imp_Settlement_ID

    String mFob = info.vars.getStringParameter("inpvalorFob").replace(",", "");
    String mFreight = info.vars.getStringParameter("inpvalorFreight").replace(",", "");
    String mInsurance = info.vars.getStringParameter("inpvalorInsurance").replace(",", "");
    String strOrgId = info.vars.getStringParameter("inpadOrgId");
    String m_settlement_id = info.vars.getStringParameter("SIM_Imp_Settlement_ID");
    String mderechosDatetoPay = info.vars.getStringParameter("inpdateForPay");
    String mperceptionDatetoPay = info.vars.getStringParameter("inppercepctionDateToPay");

    if (mFob.equals(""))
      mFob = "0";
    if (mFreight.equals(""))
      mFreight = "0";
    if (mInsurance.equals(""))
      mInsurance = "0";

    float newCif = Float.parseFloat(mFob) + Float.parseFloat(mFreight) + Float.parseFloat(mInsurance);

    info.addResult("inptotalCif", newCif); // DERECHOS
    info.addResult("inpvalorCif", newCif); // GLOBAL
    info.addResult("inppercepcionCif", newCif); // PERCEPCION

    String mtotalIsc = info.vars.getStringParameter("inptotalIsc").replace(",", "");
    String mtotalAdv = info.vars.getStringParameter("inptotalAdv").replace(",", "");
    if (mtotalIsc.equals(""))
      mtotalIsc = "0";
    if (mtotalAdv.equals(""))
      mtotalAdv = "0";

    // SimFolioImport folioImport = OBDal.getInstance().get(SimFolioImport.class, mFolioId.trim());
    SimImpSettlement preliquidation = OBDal.getInstance().get(SimImpSettlement.class, m_settlement_id.trim());
    List<SimImpSettlementLines> lista = preliquidation.getSimImpSettlementlinesList();

    float suma_ADV_perception = 0;
    for (int i = 0; i < lista.size(); i++) {
      if (!lista.get(i).isPerception())
        suma_ADV_perception = suma_ADV_perception + lista.get(i).getTotalAdvalorem().floatValue();

    }

    suma_ADV_perception = (float) Math.ceil(suma_ADV_perception);
    float newSubTotal = newCif + (float) Math.ceil(Float.parseFloat(mtotalAdv));
    info.addResult("inpsubtotal", newSubTotal);
    String mIGV = info.vars.getStringParameter("inpimpuesto").replace(",", "");
    if (mIGV.equals(""))
      mIGV = "0";

    // float newtotalIGV = newSubTotal*Float.parseFloat(mIGV)/100;
    float newtotalIGV = (float) Math.ceil((newSubTotal * 0.16)) + Math.round((newSubTotal * 0.02));
    info.addResult("inptotal", newtotalIGV);

    String mServicioDespacho = info.vars.getStringParameter("inpderechosSrvDespacho").replace(",", "");
    String mAntidumping = info.vars.getStringParameter("inpantidumping").replace(",", "");
    if (mServicioDespacho.equals(""))
      mServicioDespacho = "0";
    if (mAntidumping.equals(""))
      mAntidumping = "0";

    float newDerechosDollar = (float) Math.ceil(Float.parseFloat(mtotalAdv)) + newtotalIGV + Float.parseFloat(mServicioDespacho) + Float.parseFloat(mtotalIsc);
    info.addResult("inpderechosTotalDollar", newDerechosDollar);

    // inptotalDerechos
    BigDecimal RateDerechos = BigDecimal.ONE;
    final ConversionRate conversionRate = getConversionRate(OBDal.getInstance().get(Currency.class, "100"), OBDal.getInstance().get(Currency.class, "308"), toDate(mderechosDatetoPay), OBDal.getInstance().get(Organization.class, strOrgId));
    if (conversionRate != null) {
      RateDerechos = conversionRate.getMultipleRateBy();
      info.addResult("inptipoCambio", RateDerechos);
    } else {
      info.addResult("inptipoCambio", "");
    }
    String mTipoCambio = info.vars.getStringParameter("inptipoCambio").replace(",", "");
    if (mTipoCambio.equals(""))
      mTipoCambio = "0";

    BigDecimal amount = new BigDecimal(newDerechosDollar);
    BigDecimal converted = amount.multiply(conversionRate.getMultipleRateBy());
    info.addResult("inptotalDerechos", converted);

    // /////////////PERCEPCION
    String mExonerateCif = info.vars.getStringParameter("inppercepcionCifExonerado").replace(",", "");
    if (mExonerateCif.equals(""))
      mExonerateCif = "0";
    float newbaseCifPercepcion = newCif - Float.parseFloat(mExonerateCif);
    info.addResult("inppercepcionBaseCif", newbaseCifPercepcion);

    // info.addResult("inppercepcionAdv", Float.parseFloat(mtotalAdv));suma_ADV_perception
    info.addResult("inppercepcionAdv", suma_ADV_perception);

    // float newIGVPercepcion = (newbaseCifPercepcion +
    // Float.parseFloat(mtotalAdv))*Float.parseFloat(mIGV)/100;
    // float newIGVPercepcion = (newbaseCifPercepcion +
    // suma_ADV_perception)*Float.parseFloat(mIGV)/100;
    float newIGVPercepcion = Math.round((newbaseCifPercepcion + suma_ADV_perception) * 0.16) + Math.round((newbaseCifPercepcion + suma_ADV_perception) * 0.02);
    info.addResult("inppercepcionIgvTotal", newIGVPercepcion);

    String ServDespachoEnPercepcion = info.vars.getStringParameter("inpincServDespacho").replace(",", "");
    // float newpercepcion_total = newbaseCifPercepcion + Float.parseFloat(mtotalAdv) +
    // newIGVPercepcion;
    float newpercepcion_total = newbaseCifPercepcion + suma_ADV_perception + newIGVPercepcion;

    if (ServDespachoEnPercepcion.charAt(0) == 'Y') {
      newpercepcion_total = newpercepcion_total + Float.parseFloat(mServicioDespacho);
    }
    info.addResult("inppercepcionTotal", newpercepcion_total);

    String incluirAntidumping = info.vars.getStringParameter("inpincAntidumping").replace(",", "");
    if (incluirAntidumping.charAt(0) == 'Y') {
      newpercepcion_total = newpercepcion_total + Float.parseFloat(mAntidumping);
    }

    String newPercepctionPercent = info.vars.getStringParameter("inppercepctionPercent").replace(",", "");
    if (newPercepctionPercent.equals(""))
      newPercepctionPercent = "0";

    float newtotalPercepcion = newpercepcion_total * Float.parseFloat(newPercepctionPercent) / 100;
    info.addResult("inptotalPercepcion", newtotalPercepcion);

    BigDecimal RatePercepcion = BigDecimal.ONE;
    final ConversionRate conversionRate2 = getConversionRate(OBDal.getInstance().get(Currency.class, "100"), OBDal.getInstance().get(Currency.class, "308"), toDate(mperceptionDatetoPay), OBDal.getInstance().get(Organization.class, strOrgId));
    if (conversionRate2 != null) {
      RatePercepcion = conversionRate2.getMultipleRateBy();
    } else {
      info.addResult("inptipoCambio", "0");
    }
    BigDecimal amountPerception = new BigDecimal(newtotalPercepcion);
    BigDecimal convertedPerception = amountPerception.multiply(conversionRate2.getMultipleRateBy());
    info.addResult("inppercepcionTotalSoles", convertedPerception);
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

  public static Date toDate(String strDate) {
    if (strDate == null || strDate.isEmpty())
      return null;
    try {
      String dateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("dateFormat.java");
      SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormat);
      return (outputFormat.parse(strDate));
    } catch (ParseException e) {
      // log4j.error(e.getMessage(), e);
      return null;
    }
  }

}