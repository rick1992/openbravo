package pe.com.unifiedgo.core.addons;

public class TipoCambio {
  public int day;
  public int month;
  public int year;
  public double tcCompra;
  public double tcVenta;

  public String toString() {
    return day + "/" + month + "/" + year + " - tcCompra:" + tcCompra + " - tcVenta:" + tcVenta;
  }
}
