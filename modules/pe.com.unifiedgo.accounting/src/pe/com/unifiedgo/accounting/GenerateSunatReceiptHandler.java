package pe.com.unifiedgo.accounting;

import java.util.Date;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.openbravo.base.exception.OBException;
import org.openbravo.client.kernel.BaseActionHandler;
import org.openbravo.dal.service.OBDal;
import org.openbravo.erpCommon.utility.OBMessageUtils;

import pe.com.unifiedgo.accounting.data.SerieSunat;

public class GenerateSunatReceiptHandler extends BaseActionHandler {

  @Deprecated
  protected JSONObject execute(Map<String, Object> parameters, String data) {
	  return null;
    /*try {
      final JSONObject jsonData = new JSONObject(data);
      final JSONArray receipts_array = jsonData.getJSONArray("receipts_array");
      final String serieId = jsonData.getString("serie");
      final String action = jsonData.getString("action");

      SerieSunat serieSunat = OBDal.getInstance().get(SerieSunat.class, serieId);

      long nextNumber = serieSunat.getCurrentNumber();

      for (int i = 0; i < receipts_array.length(); i++) {
        String receipt = receipts_array.getString(i);

        InvoiceTaxSunat receiptSunat = OBDal.getInstance().get(InvoiceTaxSunat.class, receipt);
        if (receiptSunat.getReceiptNumber() != null) {
          continue;
        }
        receiptSunat.setReceiptNumber(++nextNumber);
        receiptSunat.setReceiptSerie(serieSunat.getSerieName());
        receiptSunat.setSunatSerial(serieSunat);
        receiptSunat.setDategen(new Date());
        OBDal.getInstance().save(receiptSunat);

      }

      serieSunat.setCurrentNumber(nextNumber);
      OBDal.getInstance().save(serieSunat);

      JSONObject result = new JSONObject();
      result.put("result", OBMessageUtils.getI18NMessage("SCO_GenerateSunat_Success", null));

      return result;
    } catch (Exception e) {
      throw new OBException(e);
    }*/
  }
}