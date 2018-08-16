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

public class sim_calculo_folio_fob extends SimpleCallout {
  private static final long serialVersionUID = 1L;

  @Override
  protected void execute(CalloutInfo info) throws ServletException {

	  String lastFieldChanged = info.getLastFieldChanged();
    /*
     * Enumeration<String> params = info.vars.getParameterNames(); while (params.hasMoreElements())
     * { System.out.println(params.nextElement()); }
     */

    // example
    String mOrgId = info.vars.getStringParameter("inpadOrgId").replace(",", "");
    String mFolioId = info.vars.getStringParameter("inpsimFolioimportId").replace(",", "");
    String strVolumen = info.vars.getStringParameter("inptotalCubicaje").replace(",", "");
    String strFactorOne = info.vars.getStringParameter("inpfleteFactorOne").replace(",", "");
    String strFactorBl = info.vars.getStringParameter("inpfleteBl").replace(",", "");
    String strFleteOtros = info.vars.getStringParameter("inpfleteOtros").replace(",", "");
    String strFleteTotal = info.vars.getStringParameter("inpfleteTotal").replace(",", "");
    String strCifFob = info.vars.getStringParameter("inpcifFob").replace(",", "");
    String strSeguroPercentfob = info.vars.getStringParameter("inpcifSeguroPercentfob").replace(",", "");
    String strSeguro = info.vars.getStringParameter("inpcifSeguro").replace(",", "");
    String mCurrencyFolio = "308"; // Soles
    String mCurrencyDollar = "100"; // Dólar
    String strCifTotal = info.vars.getStringParameter("inpcifTotal").replace(",", "");
    String strFactorTwo = info.vars.getStringParameter("inpaduanaFactorTwo").replace(",", "");
    String strWeight = info.vars.getStringParameter("inptotalPeso").replace(",", "");
    String strFactorThree = info.vars.getStringParameter("inpaduanaFactorThree").replace(",", "");
    String strAplicaFactorThree = info.vars.getStringParameter("inpaduanaFThreeResult").replace(",", "");
    String strAduanaExchangeRate = info.vars.getStringParameter("inpaduanaExchangeRate").replace(",", "");
    String strAduanaTotal = info.vars.getStringParameter("inpaduanaTotal").replace(",", "");
    String strPercenttoCif = info.vars.getStringParameter("inpagentPercentCif").replace(",", "");
    String strAgenteTotal = info.vars.getStringParameter("inpagentTotal").replace(",", "");
    String strShipper = info.vars.getStringParameter("inpshippingTotal").replace(",", "");

    BigDecimal volumen = new BigDecimal((strVolumen.equals(""))?"0":strVolumen);
    BigDecimal factorOne = new BigDecimal((strFactorOne.equals(""))?"0":strFactorOne);
    BigDecimal factorBl = new BigDecimal((strFactorBl.equals(""))?"0":strFactorBl);
    BigDecimal fleteOtros = new BigDecimal((strFleteOtros.equals(""))?"0":strFleteOtros);
    BigDecimal totalFlete = new BigDecimal((strFleteTotal.equals(""))?"0":strFleteTotal);
    BigDecimal seguroPercentFob =  new BigDecimal((strSeguroPercentfob.equals(""))?"0":strSeguroPercentfob);
    BigDecimal valor_fob = new BigDecimal((strCifFob.equals(""))?"0":strCifFob);
    BigDecimal seguro =  new BigDecimal((strSeguro.equals(""))?"0":strSeguro);
    
    BigDecimal cif_total =  new BigDecimal((strCifTotal.equals(""))?"0":strCifTotal);
    BigDecimal factorTwo =  new BigDecimal((strFactorTwo.equals(""))?"0":strFactorTwo);
    BigDecimal weight = new BigDecimal((strWeight.equals(""))?"0":strWeight);
    BigDecimal factorThree =  new BigDecimal((strFactorThree.equals(""))?"0":strFactorThree);
    BigDecimal aplicafactor =  new BigDecimal((strAplicaFactorThree.equals(""))?"0":strAplicaFactorThree);
    BigDecimal aduanaExchangeRate = new BigDecimal((strAduanaExchangeRate.equals(""))?"0":strAduanaExchangeRate);
    BigDecimal desaduanajeDollar = new BigDecimal((strAduanaTotal.equals(""))?"0":strAduanaTotal);
    BigDecimal percentCif = new BigDecimal((strPercenttoCif.equals(""))?"0":strPercenttoCif);
    BigDecimal agenteAduana = new BigDecimal((strAgenteTotal.equals(""))?"0":strAgenteTotal);
    BigDecimal shipper = new BigDecimal((strShipper.equals(""))?"0":strShipper);
    
    BigDecimal factorVolumen = BigDecimal.ZERO;
    //BigDecimal totalFlete = BigDecimal.ZERO;
    //BigDecimal seguro = BigDecimal.ZERO;
   // BigDecimal valor_fob = BigDecimal.ZERO;
   // BigDecimal cif_total = BigDecimal.ZERO;
    BigDecimal percetFleteforFob = BigDecimal.ZERO;
    BigDecimal factorPeso = BigDecimal.ZERO;
    //BigDecimal aplicafactor = BigDecimal.ZERO;
    //BigDecimal desaduanajeDollar = BigDecimal.ZERO;
    //BigDecimal AgenteAduana = BigDecimal.ZERO;
    BigDecimal TotalDesaduanaje = BigDecimal.ZERO;
    
    if ("inpfleteFactorOne".equals(lastFieldChanged) ||
    	"inpfleteBl".equals(lastFieldChanged) ||
    	"inpfleteOtros".equals(lastFieldChanged) ||  
    	"inpcifSeguroPercentfob".equals(lastFieldChanged) ||
    	"inpcifFob".equals(lastFieldChanged) ||
    	"inpaduanaFactorThree".equals(lastFieldChanged) ||
    	"inpaduanaFactorTwo".equals(lastFieldChanged) 
    	) {
    	
    	factorVolumen = factorOne.multiply(volumen);
    	info.addResult("inpfleteFOneXVolumen", factorVolumen);
    	
    	totalFlete = factorVolumen.add(factorBl).add(fleteOtros);
    	info.addResult("inpfleteTotal", totalFlete);
    	info.addResult("inpcifFleteTotal", totalFlete);
    
    	/*
    	 * OBTENER EL FOB */
    	if(valor_fob.compareTo(BigDecimal.ZERO)==0)
    	    valor_fob = getFobfromFolio(mFolioId);       
        ///////////////////////////////////////////////////
    	info.addResult("inpcifFob", valor_fob);
    	
    	seguro = valor_fob.add(totalFlete).multiply(seguroPercentFob.divide(new BigDecimal(100), 5 , RoundingMode.HALF_UP));
    	info.addResult("inpcifSeguro", seguro);
    	
    	cif_total = valor_fob.add(totalFlete).add(seguro);
        info.addResult("inpcifTotal", cif_total);
    	
        //--FLete % from CIF
        if (!cif_total.equals(BigDecimal.ZERO)) {
            //percetFleteforFob = (cif_total.divide(valor_fob, 3, RoundingMode.HALF_UP)).multiply(new BigDecimal(100));
            percetFleteforFob = (totalFlete.divide(cif_total, 4, RoundingMode.HALF_UP)).multiply(new BigDecimal(100));
            info.addResult("inpfletePercentToFob", percetFleteforFob);
        } else {
            info.addResult("inpfletePercentToFob", BigDecimal.ZERO);
        }
    	
        //Desaduanaje
        
        factorPeso =  factorTwo.multiply(weight).divide(new BigDecimal(1000), 3, RoundingMode.HALF_UP);
        info.addResult("inpaduanaFTwoXWeight", factorPeso);
        aplicafactor = factorPeso.add(factorThree);
        info.addResult("inpaduanaFThreeResult", aplicafactor);

    	if(aduanaExchangeRate.compareTo(BigDecimal.ZERO)==0){
    		final ConversionRate conversionRate = getConversionRate(OBDal.getInstance().get(Currency.class, mCurrencyFolio.trim()), OBDal.getInstance().get(Currency.class, mCurrencyDollar.trim()), new Date(), OBDal.getInstance().get(Organization.class, mOrgId.trim()));
    	    if (conversionRate != null)
    	    	aduanaExchangeRate = conversionRate.getMultipleRateBy();
    	    info.addResult("aduana_exchange_rate", aduanaExchangeRate);
    	}
    	
    	desaduanajeDollar = aplicafactor.divide(aduanaExchangeRate, 3, RoundingMode.HALF_UP);
        info.addResult("inpaduanaTotal", desaduanajeDollar);
        
        
        // mPercenttoCif
        agenteAduana = percentCif.multiply(cif_total).divide(new BigDecimal(100), 4, RoundingMode.HALF_UP);
        info.addResult("inpagentTotal", agenteAduana);
        
        TotalDesaduanaje = desaduanajeDollar.add(agenteAduana).add(shipper);
        info.addResult("inptotalDesaduanaje", TotalDesaduanaje);
        
        
        
        
        
        if (!cif_total.equals(BigDecimal.ZERO)) {
            BigDecimal porcentajeDesaduanajedeFOB = (TotalDesaduanaje.divide(cif_total, 4, RoundingMode.HALF_UP)).multiply(new BigDecimal(100));
   
            info.addResult("inpaduanaPercentToFob", porcentajeDesaduanajedeFOB);
          } else {
            info.addResult("inpaduanaPercentToFob", BigDecimal.ZERO);
          }
    }
    
    if ("inpcifSeguro".equals(lastFieldChanged)) {
    	cif_total = valor_fob.add(totalFlete).add(seguro);
        info.addResult("inpcifTotal", cif_total);
        //--FLete % from CIF
        if (!cif_total.equals(BigDecimal.ZERO)) {
            percetFleteforFob = (totalFlete.divide(cif_total, 4, RoundingMode.HALF_UP)).multiply(new BigDecimal(100));
            info.addResult("inpfletePercentToFob", percetFleteforFob);
        } else {
            info.addResult("inpfletePercentToFob", BigDecimal.ZERO);
        }
    	
        agenteAduana = percentCif.multiply(cif_total).divide(new BigDecimal(100), 4, RoundingMode.HALF_UP);
        info.addResult("inpagentTotal", agenteAduana);
        
        TotalDesaduanaje = desaduanajeDollar.add(agenteAduana).add(shipper);
        info.addResult("inptotalDesaduanaje", TotalDesaduanaje);
        
        if (!cif_total.equals(BigDecimal.ZERO)) {
            BigDecimal porcentajeDesaduanajedeFOB = (TotalDesaduanaje.divide(cif_total, 4, RoundingMode.HALF_UP)).multiply(new BigDecimal(100));
            info.addResult("inpaduanaPercentToFob", porcentajeDesaduanajedeFOB);
          } else {
            info.addResult("inpaduanaPercentToFob", BigDecimal.ZERO);
          }
        
    	
    }
    
    if ("inpaduanaExchangeRate".equals(lastFieldChanged)) {
    	
    	desaduanajeDollar = aplicafactor.divide(aduanaExchangeRate,4, RoundingMode.HALF_UP);
        info.addResult("inpaduanaTotal", desaduanajeDollar);
        
        TotalDesaduanaje = desaduanajeDollar.add(agenteAduana).add(shipper);
        info.addResult("inptotalDesaduanaje", TotalDesaduanaje);
        
        if (!cif_total.equals(BigDecimal.ZERO)) {
            BigDecimal porcentajeDesaduanajedeFOB = (TotalDesaduanaje.divide(cif_total, 4, RoundingMode.HALF_UP)).multiply(new BigDecimal(100));
            info.addResult("inpaduanaPercentToFob", porcentajeDesaduanajedeFOB);
          } else {
            info.addResult("inpaduanaPercentToFob", BigDecimal.ZERO);
        }
        
    }
    
    
    
    if ("inpagentPercentCif".equals(lastFieldChanged)) {
    	 agenteAduana = percentCif.multiply(cif_total).divide(new BigDecimal(100), 4, RoundingMode.HALF_UP);
         info.addResult("inpagentTotal", agenteAduana);
         
         TotalDesaduanaje = agenteAduana.add(desaduanajeDollar).add(shipper);
         info.addResult("inptotalDesaduanaje", TotalDesaduanaje);
         
         
         if (!cif_total.equals(BigDecimal.ZERO)) {
             BigDecimal porcentajeDesaduanajedeFOB = (TotalDesaduanaje.divide(cif_total, 4, RoundingMode.HALF_UP)).multiply(new BigDecimal(100));
             info.addResult("inpaduanaPercentToFob", porcentajeDesaduanajedeFOB);
           } else {
             info.addResult("inpaduanaPercentToFob", BigDecimal.ZERO);
           }
    }
    
    
    if ("inpshippingTotal".equals(lastFieldChanged) || 
    	"inpagentTotal".equals(lastFieldChanged)	) {
    	
  
    	
    	TotalDesaduanaje = agenteAduana.add(desaduanajeDollar).add(shipper);
        info.addResult("inptotalDesaduanaje", TotalDesaduanaje);
        
        
        
        if (!cif_total.equals(BigDecimal.ZERO)) {
            BigDecimal porcentajeDesaduanajedeFOB = (TotalDesaduanaje.divide(cif_total, 4, RoundingMode.HALF_UP)).multiply(new BigDecimal(100));
            info.addResult("inpaduanaPercentToFob", porcentajeDesaduanajedeFOB);
          } else {
            info.addResult("inpaduanaPercentToFob", BigDecimal.ZERO);
          }
    }
    
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
