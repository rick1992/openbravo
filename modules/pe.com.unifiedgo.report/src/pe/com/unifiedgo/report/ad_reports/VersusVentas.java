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

class VersusLineV {
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

class VersusLineInoutV {
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

public class VersusVentas {

  public static void matchingInvoiceInout(List<VersusLineV> lsInvoice,
      List<VersusLineInoutV> lsInout) {
    // primero si invoice tiene minoutline asociado
    for (int i = 0; i < lsInvoice.size(); i++) {
      VersusLineV vl = lsInvoice.get(i);

      for (int j = 0; j < lsInout.size(); j++) {
        VersusLineInoutV vli = lsInout.get(j);

        if (vli.inoutlineId.compareTo(vl.inoutlineId) == 0) {
          vl.fechaInout = vli.fechaInout;
          vl.mInout = vli.mInout;
          vl.mInoutValue = vli.mInoutValue;

          double toSubstract = (vli.unidadesInout - vli.repartidasOut);
          double max = vl.unidadesInvoice + vl.unidadesDeffered - vl.unidadesInout;
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
        VersusLineV vl1 = lsInvoice.get(i);
        VersusLineV vl2 = lsInvoice.get(j);
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
        VersusLineV vl = lsInvoice.get(i);

        if ((vl.unidadesInvoice + vl.unidadesDeffered) <= vl.unidadesInout)
          continue;

        for (int j = 0; j < lsInout.size(); j++) {
          VersusLineInoutV vli = lsInout.get(j);
          if (vli.unidadesInout <= vli.repartidasOut)
            continue;

          if (vli.orderlineId.compareTo(vl.orderlineId) == 0) {

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
                VersusLineInoutV vliAux = lsInout.get(k);
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
      }
    }

    // luego excesos
    found = true;
    while (found) {
      found = false;

      for (int i = 0; i < lsInvoice.size(); i++) {
        VersusLineV vl = lsInvoice.get(i);

        if ((vl.unidadesInvoice + vl.unidadesDeffered) <= vl.unidadesInout)
          continue;

        for (int j = 0; j < lsInout.size(); j++) {
          VersusLineInoutV vli = lsInout.get(j);
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

  public static List<VersusLineV> getVersusVentas(ConnectionProvider connectionProvider,
      Date startingDate, Date endingDate, String strOrgId, String partnerId, String productId) {

    List<VersusLineV> lsVersus = new ArrayList<VersusLineV>();

    String strDateFormat = OBPropertiesProvider.getInstance().getOpenbravoProperties()
        .getProperty("dateFormat.java");
    final SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);

    HashMap<String, List<VersusLineInoutV>> hashGuia = new HashMap<String, List<VersusLineInoutV>>();
    HashMap<String, List<VersusLineV>> hashFactura = new HashMap<String, List<VersusLineV>>();

    String partnerTax = null;

    if (partnerId != null && !partnerId.equals("")) {
      BusinessPartner bp = OBDal.getInstance().get(BusinessPartner.class, partnerId);
      partnerTax = bp.getTaxID();
    }

    String prodId = null;
    if (productId != null && !productId.equals("")) {
      prodId = productId;
    }

    VersusVentasData[] data = null;
    try {
      // paso 1.1, 1.2, 1.3
      data = VersusVentasData.select(connectionProvider, dateFormat.format(startingDate),
          dateFormat.format(endingDate), strOrgId, partnerTax, prodId);
    } catch (ServletException e) {

    }
    if (data != null && data.length > 0) {

      for (int i = 0; i < data.length; i++) {

        VersusLineV line = new VersusLineV();

        line.bPartnerId = data[i].cBpartnerId;
        line.bPartnerValue = data[i].tercero;
        line.cInvoiceId = data[i].cInvoiceId;
        line.fechaInout = "--";
        line.fechaInvoice = data[i].fecha;
        line.invoiceValue = data[i].emScrPhysicalDocumentno;
        line.mInout = "--";
        line.mInoutValue = "--";
        line.mProductId = data[i].mProductId;
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
          List<VersusLineV> arr = new ArrayList<VersusLineV>();
          hashFactura.put(code, arr);
          arr.add(line);
        } else {
          List<VersusLineV> arr = hashFactura.get(code);
          arr.add(line);
        }

      }
    }

    VersusVentasData[] dataInout = null;
    try {
      // paso 1.1, 1.2, 1.3
      dataInout = VersusVentasData.selectInout(connectionProvider, dateFormat.format(startingDate),
          dateFormat.format(endingDate), strOrgId, partnerTax, prodId);
    } catch (ServletException e) {

    }

    if (dataInout != null && dataInout.length > 0) {

      for (int i = 0; i < dataInout.length; i++) {

        VersusLineInoutV line = new VersusLineInoutV();

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
          List<VersusLineInoutV> arr = new ArrayList<VersusLineInoutV>();
          hashGuia.put(code, arr);
          arr.add(line);
        } else {
          List<VersusLineInoutV> arr = hashGuia.get(code);
          arr.add(line);
        }

      }

    }

    for (Entry<String, List<VersusLineV>> entry : hashFactura.entrySet()) {

      List<VersusLineV> lsVersusLine = entry.getValue();
      String code = entry.getKey();

      List<VersusLineInoutV> lsVersusInout = hashGuia.get(code);
      if (lsVersusInout != null)
        matchingInvoiceInout(lsVersusLine, lsVersusInout);
      lsVersus.addAll(lsVersusLine);
    }

    // Los inout que sobran van solos
    for (Entry<String, List<VersusLineInoutV>> entry : hashGuia.entrySet()) {

      List<VersusLineInoutV> lsVersusLine = entry.getValue();

      for (int i = 0; i < lsVersusLine.size(); i++) {
        VersusLineInoutV inout = lsVersusLine.get(i);
        VersusLineV line = new VersusLineV();

        if (inout.unidadesInout - inout.repartidasOut == 0)
          continue;

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