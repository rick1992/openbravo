package pe.com.unifiedgo.report.ad_reports;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import javax.servlet.ServletException;

import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.model.common.businesspartner.BusinessPartner;
import org.openbravo.model.common.invoice.Invoice;
import org.openbravo.model.common.invoice.InvoiceLine;
import org.openbravo.model.common.order.OrderLine;
import org.openbravo.model.materialmgmt.transaction.ShipmentInOutLine;

class VersusLine {
  double unidadesInout;
  double unidadesInvoice;
  double unidadesDeffered;
  String mProductId;
  String cInvoiceId;
  String mInout;
  String productValue;
  String invoiceValue;
  String mInoutValue;
  String fechaInvoice;
  String fechaInout;
  String bPartnerId;
  String bPartnerValue;
  String orderId;
  String orderValue;

  String orderlineId;
  String inoutlineId;
  String invoicelineId;

  String descProducto;
  String uom;
  double precioUnitario;
  double precioTotal;
  double tc;
  String moneda;
}

class VersusLineInout {
  String mInout;
  String mInoutValue;
  String orderlineId;
  String inoutlineId;
  double unidadesInout;
  String mProductId;
  String bPartnerId;
  String fechaInout;

  double repartidasOut;

  String orderId;
  String orderValue;
  String productValue;
  String bPartnerValue;
  String descProducto;
  String uom;

}

public class VersusCompras {

  public static void matchingInvoiceInout(List<VersusLine> lsInvoice,
      List<VersusLineInout> lsInout) {
    // primero si invoice tiene minoutline asociado
    for (int i = 0; i < lsInvoice.size(); i++) {
      VersusLine vl = lsInvoice.get(i);

      for (int j = 0; j < lsInout.size(); j++) {
        VersusLineInout vli = lsInout.get(j);
        if (vli.inoutlineId.equals(vl.inoutlineId)) {
          vl.fechaInout = vli.fechaInout;
          vl.mInout = vli.mInout;
          vl.mInoutValue = vli.mInoutValue;
          
          double toSubstract = (vli.unidadesInout - vli.repartidasOut);
          double max = vl.unidadesInvoice + vl.unidadesDeffered - vl.unidadesInout ;
          if (max >= toSubstract) {
            vl.unidadesInout += toSubstract;
            vli.repartidasOut += toSubstract;
          } else {
            vl.unidadesInout += max;
            vli.repartidasOut += max;
          }

          
          break;
        }
      }
    }

    // ordenar invoices por deferred ascendente
    for (int i = 0; i < lsInvoice.size(); i++) {
      for (int j = i + 1; j < lsInvoice.size(); j++) {
        VersusLine vl1 = lsInvoice.get(i);
        VersusLine vl2 = lsInvoice.get(j);
        if (vl1.unidadesDeffered > vl2.unidadesDeffered) {
          lsInvoice.set(i, vl2);
          lsInvoice.set(j, vl1);
        }
      }
    }

    // luego por orderline
    boolean found = true;
    while (found) {
      found = false;

      for (int i = 0; i < lsInvoice.size(); i++) {
        VersusLine vl = lsInvoice.get(i);

        if ((vl.unidadesInvoice + vl.unidadesDeffered) <= vl.unidadesInout)
          continue;

        for (int j = 0; j < lsInout.size(); j++) {
          VersusLineInout vli = lsInout.get(j);
          if (vli.unidadesInout <= vli.repartidasOut)
            continue;

          if (vli.orderlineId.equals(vl.orderlineId)) {

            // caso unidadesinout=unidadesordered
            if ((vli.unidadesInout - vli.repartidasOut) == (vl.unidadesInvoice
                + vl.unidadesDeffered - vl.unidadesInout)) {
              vl.fechaInout = vli.fechaInout;

              if (vl.mInout.equals("--")) {
                vl.mInout = vli.mInout;
                vl.mInoutValue = vli.mInoutValue;
              } else {
                vl.mInout = " / " + vli.mInout;
                vl.mInoutValue = " / " + vli.mInoutValue;
              }
              vl.unidadesInout += (vli.unidadesInout - vli.repartidasOut);
              vli.repartidasOut += (vli.unidadesInout - vli.repartidasOut);

              found = true;
              break;
            }

            // caso 2: no hay mas con ese orderline
            else {

              boolean foundAux = false;
              Vector<Double> vQtyAux = new Vector<Double>();
              Vector<Integer> vIndexAux = new Vector<Integer>();
              for (int k = 0; k < lsInout.size(); k++) {
                if (k == j)
                  continue;
                VersusLineInout vliAux = lsInout.get(k);
                if (vliAux.unidadesInout <= vliAux.repartidasOut)
                  continue;

                if (vliAux.orderlineId.equals(vl.orderlineId)) {
                  foundAux = true;
                  vIndexAux.add(k);
                  vQtyAux.add(vliAux.unidadesInout - vliAux.repartidasOut);
                }

              }

              if (foundAux == false) {// caso 2.1 unico
                vl.fechaInout = vli.fechaInout;

                if (vl.mInout.equals("--")) {
                  vl.mInout = vli.mInout;
                  vl.mInoutValue = vli.mInoutValue;
                } else {
                  vl.mInout = " / " + vli.mInout;
                  vl.mInoutValue = " / " + vli.mInoutValue;
                }
                vl.unidadesInout += (vli.unidadesInout - vli.repartidasOut);
                vli.repartidasOut += (vli.unidadesInout - vli.repartidasOut);

                found = true;
                break;

              } else {// caso 2.2 solo lo que le corresponde

                vl.fechaInout = vli.fechaInout;
                System.out.println("Caso especial 1");
                if (vl.mInout.equals("--")) {
                  vl.mInout = vli.mInout;
                  vl.mInoutValue = vli.mInoutValue;
                } else {
                  vl.mInout = " / " + vli.mInout;
                  vl.mInoutValue = " / " + vli.mInoutValue;
                }

                double toSubstract = (vli.unidadesInout - vli.repartidasOut);
                double max = vl.unidadesInvoice + vl.unidadesDeffered - vl.unidadesInout ;
                if (max >= toSubstract) {
                  vl.unidadesInout += toSubstract;
                  vli.repartidasOut += toSubstract;
                } else {
                  vl.unidadesInout += max;
                  vli.repartidasOut += max;
                }

                found = true;
                break;

              }
            }
          }

        }
      }
    }

    // luego excesos
    found = true;
    while (found) {
      found = false;

      for (int i = 0; i < lsInvoice.size(); i++) {
        VersusLine vl = lsInvoice.get(i);

        if ((vl.unidadesInvoice + vl.unidadesDeffered) <= vl.unidadesInout)
          continue;

        for (int j = 0; j < lsInout.size(); j++) {
          VersusLineInout vli = lsInout.get(j);
          if (vli.unidadesInout <= vli.repartidasOut)
            continue;

          vl.fechaInout = vli.fechaInout;
          System.out.println("Caso especial 2");
          if (vl.mInout.equals("--")) {
            vl.mInout = vli.mInout;
            vl.mInoutValue = vli.mInoutValue;
          } else {
            vl.mInout = " / " + vli.mInout;
            vl.mInoutValue = " / " + vli.mInoutValue;
          }

          double toSubstract = (vli.unidadesInout - vli.repartidasOut);
          double max = vl.unidadesInvoice + vl.unidadesDeffered - vl.unidadesInout;
          if (max >= toSubstract) {
            vl.unidadesInout += toSubstract;
            vli.repartidasOut += toSubstract;
          } else {
            vl.unidadesInout += max;
            vli.repartidasOut += max;
          }

          found = true;
          break;

        }
      }
    }

  }

  public static List<VersusLine> getVersusCompras(ConnectionProvider connectionProvider,
      Date startingDate, Date endingDate, String strOrgId, String partnerId, String productId) {

    List<VersusLine> lsVersus = new ArrayList<VersusLine>();

    String strDateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    final SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);

    HashMap<String, List<VersusLineInout>> hashGuia = new HashMap<String, List<VersusLineInout>>();
    HashMap<String, List<VersusLine>> hashFactura = new HashMap<String, List<VersusLine>>();

    String partnerTax = null;

    if (partnerId != null && !partnerId.equals("")) {
      BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, partnerId);
      partnerTax = bp.getTaxID();
    }

    String prodId = null;
    if (productId != null && !productId.equals("")) {
      prodId = productId;
    }

    VersusComprasData[] data = null;
    try {
      // paso 1.1, 1.2, 1.3
      data = VersusComprasData.select(connectionProvider, dateFormat.format(startingDate),
          dateFormat.format(endingDate), strOrgId, partnerTax, prodId);
    } catch (ServletException e) {

    }
    if (data != null && data.length > 0) {

      for (int i = 0; i < data.length; i++) {

        VersusLine line = new VersusLine();

        line.bPartnerId = data[i].cBpartnerId;
        line.bPartnerValue = data[i].tercero;
        line.cInvoiceId = data[i].cInvoiceId;
        line.fechaInout = "--";
        line.fechaInvoice = data[i].fecha;
        line.invoiceValue = data[i].emScrPhysicalDocumentno;
        line.mInout = "--";
        line.mInoutValue = "--";
        line.mProductId = data[i].mProductId;
        
        if(!data[i].pserviciopk.equals(""))
        	line.mProductId = data[i].pserviciopk;
        
        line.orderId = data[i].orderid;
        line.orderValue = data[i].orderno;
        line.productValue = data[i].productpk;

        String deferred = data[i].isdeferred;

        line.unidadesDeffered = 0;
        line.unidadesInout = 0;
        line.unidadesInvoice = 0;
        if (deferred.equals("Y")) {
          line.unidadesDeffered = Double.valueOf(data[i].qty);
        } else {
          line.unidadesInvoice = Double.valueOf(data[i].qty);
        }

        line.descProducto = data[i].productname;
        line.uom = data[i].uomsymbol;

        line.precioUnitario = Double.valueOf(data[i].priceactual);
        line.precioTotal = Double.valueOf(data[i].totalamt);
        line.tc = Double.valueOf(data[i].tc);
        line.moneda = data[i].moneda;

        if (!line.moneda.equals("PEN")) {
          line.precioUnitario = line.precioUnitario * line.tc;
          line.precioTotal = line.precioTotal * line.tc;
        }

        line.invoicelineId = data[i].cInvoicelineId;
        line.inoutlineId = data[i].mInoutlineId;
        line.orderlineId = data[i].cOrderlineId;

        String code = line.mProductId + "-" + line.bPartnerId;

        if (hashFactura.get(code) == null) {
          List<VersusLine> arr = new ArrayList<VersusLine>();
          hashFactura.put(code, arr);
          arr.add(line);
        } else {
          List<VersusLine> arr = hashFactura.get(code);
          arr.add(line);
        }

      }
    }

    VersusComprasData[] dataInout = null;
    try {
      // paso 1.1, 1.2, 1.3
      dataInout = VersusComprasData.selectInout(connectionProvider, dateFormat.format(startingDate),
          dateFormat.format(endingDate), strOrgId, partnerTax, prodId);
    } catch (ServletException e) {

    }

    if (dataInout != null && dataInout.length > 0) {

      for (int i = 0; i < dataInout.length; i++) {

        VersusLineInout line = new VersusLineInout();

        line.mInout = dataInout[i].mInoutId;
        line.mInoutValue = dataInout[i].phydocno;
        line.orderlineId = dataInout[i].cOrderlineId;
        line.inoutlineId = dataInout[i].mInoutlineId;
        line.unidadesInout = Double.valueOf(dataInout[i].movqty);
        line.fechaInout = dataInout[i].movementdate;
        line.mProductId = dataInout[i].mProductId;
        line.bPartnerId = dataInout[i].cBpartnerId;

        line.orderId = dataInout[i].orderid;
        line.orderValue = dataInout[i].orderno;
        line.productValue = dataInout[i].productpk;
        line.bPartnerValue = dataInout[i].tercero;
        line.descProducto = dataInout[i].productname;
        line.uom = dataInout[i].uomsymbol;

        line.repartidasOut = 0;

        String code = line.mProductId + "-" + line.bPartnerId;

        if (hashGuia.get(code) == null) {
          List<VersusLineInout> arr = new ArrayList<VersusLineInout>();
          hashGuia.put(code, arr);
          arr.add(line);
        } else {
          List<VersusLineInout> arr = hashGuia.get(code);
          arr.add(line);
        }

      }

    }

    for (Entry<String, List<VersusLine>> entry : hashFactura.entrySet()) {

      List<VersusLine> lsVersusLine = entry.getValue();
      String code = entry.getKey();

      List<VersusLineInout> lsVersusInout = hashGuia.get(code);
      if (lsVersusInout != null)
        matchingInvoiceInout(lsVersusLine, lsVersusInout);
      lsVersus.addAll(lsVersusLine);
    }

    
    
    // Los inout que sobran van solos
    for (Entry<String, List<VersusLineInout>> entry : hashGuia.entrySet()) {

      List<VersusLineInout> lsVersusLine = entry.getValue();

      for (int i = 0; i < lsVersusLine.size(); i++) {
        VersusLineInout inout = lsVersusLine.get(i);

        if (inout.unidadesInout - inout.repartidasOut == 0)
          continue;

        ShipmentInOutLine siol = OBDal.getInstance().get(ShipmentInOutLine.class, inout.inoutlineId);
        List<InvoiceLine> invLineList = siol.getInvoiceLineList();
        
        if(invLineList.size()!=0){
	        for(int k=0; k<invLineList.size(); k++){
	        	InvoiceLine invline = invLineList.get(k);
	        	Invoice invoice = invline.getInvoice();
	        	if(invoice.getDocumentStatus().equals("CO") &&  invoice.getAccountingDate().compareTo(startingDate)<0){
	        		
	        		VersusLine line = new VersusLine();
	                
	                line.bPartnerId = inout.bPartnerId;
	                line.bPartnerValue = inout.bPartnerValue;
	                line.cInvoiceId = invoice.getId();
	                line.fechaInout = inout.fechaInout;
	                line.fechaInvoice = dateFormat.format( invoice.getScoNewdateinvoiced() );
	                line.invoiceValue = invoice.getScrPhysicalDocumentno();
	                line.mInout = inout.mInout;
	                line.mInoutValue = inout.mInoutValue;
	                line.mProductId = inout.mProductId;
	                line.orderId = inout.orderId;
	                line.orderValue = inout.orderValue;
	                line.productValue = inout.productValue;
	                
	                double inoutQty = Double.valueOf(inout.unidadesInout - inout.repartidasOut);
	                double minQty = (invline.getInvoicedQuantity().doubleValue()>inoutQty) ?  inoutQty :  invline.getInvoicedQuantity().doubleValue();
	                
	                line.unidadesDeffered = 0;
	                line.unidadesInvoice = minQty;
	
	                line.unidadesInout = minQty;
	
	                inout.repartidasOut = inout.repartidasOut+ minQty;
	                
	                line.descProducto = inout.descProducto;
	                line.uom = inout.uom;
	
	                line.precioUnitario = 0;
	                line.precioTotal = 0;
	                line.tc = 0;
	                line.moneda = invoice.getCurrency().getId().equals("308") ? "PEN" : "USD";
	
	                lsVersus.add(line);
	        	}
	        }
        }else{//BUSCAR ENLACE INDIRECTO
        	OrderLine orderline = siol.getSalesOrderLine();
        	
        	if(orderline!=null){
        		
        		
        		List<InvoiceLine> lsInvlineAux = orderline.getInvoiceLineList();
        		List<ShipmentInOutLine> lsShiplineAux = orderline.getMaterialMgmtShipmentInOutLineList();
        		
        		List<InvoiceLine> lsInvline = new ArrayList<InvoiceLine>();
        		List<ShipmentInOutLine> lsShipline = new ArrayList<ShipmentInOutLine>();

        		for(int k=0; k<lsInvlineAux.size(); k++){
        			if(lsInvlineAux.get(k).getInvoice().getDocumentStatus().equals("CO"))
        				lsInvline.add(lsInvlineAux.get(k));
        		}
        		
        		for(int k=0; k<lsShiplineAux.size(); k++){
        			if(lsShiplineAux.get(k).getShipmentReceipt().getDocumentStatus().equals("CO"))
        				lsShipline.add(lsShiplineAux.get(k));
        		}
        		
        		if(lsInvline.size()==1 && lsShipline.size()==1){
        			
        			InvoiceLine invline = lsInvline.get(0);
    	        	Invoice invoice = invline.getInvoice();
    	        	if(invoice.getAccountingDate().compareTo(startingDate)<0){
    	        		
    	        		VersusLine line = new VersusLine();
    	                
    	                line.bPartnerId = inout.bPartnerId;
    	                line.bPartnerValue = inout.bPartnerValue;
    	                line.cInvoiceId = invoice.getId();
    	                line.fechaInout = inout.fechaInout;
    	                line.fechaInvoice = dateFormat.format( invoice.getScoNewdateinvoiced() );
    	                line.invoiceValue = invoice.getScrPhysicalDocumentno();
    	                line.mInout = inout.mInout;
    	                line.mInoutValue = inout.mInoutValue;
    	                line.mProductId = inout.mProductId;
    	                line.orderId = inout.orderId;
    	                line.orderValue = inout.orderValue;
    	                line.productValue = inout.productValue;
    	                
    	                double inoutQty = Double.valueOf(inout.unidadesInout - inout.repartidasOut);
    	                double minQty = (invline.getInvoicedQuantity().doubleValue()>inoutQty) ?  inoutQty :  invline.getInvoicedQuantity().doubleValue();
    	                
    	                line.unidadesDeffered = 0;
    	                line.unidadesInvoice = minQty;
    	
    	                line.unidadesInout = minQty;
    	
    	                inout.repartidasOut = inout.repartidasOut+ minQty;
    	                
    	                line.descProducto = inout.descProducto;
    	                line.uom = inout.uom;
    	
    	                line.precioUnitario = 0;
    	                line.precioTotal = 0;
    	                line.tc = 0;
    	                line.moneda = invoice.getCurrency().getId().equals("308") ? "PEN" : "USD";
    	
    	                lsVersus.add(line);
    	        	}
        			
        		}
        	}
        }
        
        if (inout.unidadesInout - inout.repartidasOut == 0)
            continue;
        
        VersusLine line = new VersusLine();
        
        line.bPartnerId = inout.bPartnerId;
        line.bPartnerValue = inout.bPartnerValue;
        line.cInvoiceId = "";
        line.fechaInout = inout.fechaInout;
        line.fechaInvoice = "--";
        line.invoiceValue = "--";
        line.mInout = inout.mInout;
        line.mInoutValue = inout.mInoutValue;
        line.mProductId = inout.mProductId;
        line.orderId = inout.orderId;
        line.orderValue = inout.orderValue;
        line.productValue = inout.productValue;

        line.unidadesDeffered = 0;
        line.unidadesInvoice = 0;

        line.unidadesInout = Double.valueOf(inout.unidadesInout - inout.repartidasOut);

        line.descProducto = inout.descProducto;
        line.uom = inout.uom;

        line.precioUnitario = 0;
        line.precioTotal = 0;
        line.tc = 0;
        line.moneda = "--";

        lsVersus.add(line);
      }
    }

    return lsVersus;

  }
}