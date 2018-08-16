package pe.com.unifiedgo.sales;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Map;
import java.util.regex.Pattern;

import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.client.kernel.reference.UIDefinitionController;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.erpCommon.utility.OBMessageUtils;
import org.openbravo.erpCommon.utility.Utility;
import org.openbravo.model.ad.process.ProcessInstance;
import org.openbravo.model.ad.ui.Process;
import org.openbravo.service.db.CallProcess;

import pe.com.unifiedgo.sales.data.SSAProjPropContract;

public class SSA_Utils {

  private final static String[] UNIDADES = { "", "un ", "dos ", "tres ", "cuatro ", "cinco ",
      "seis ", "siete ", "ocho ", "nueve " };
  private final static String[] DECENAS = { "diez ", "once ", "doce ", "trece ", "catorce ",
      "quince ", "dieciseis ", "diecisiete ", "dieciocho ", "diecinueve", "veinte ", "treinta ",
      "cuarenta ", "cincuenta ", "sesenta ", "setenta ", "ochenta ", "noventa " };
  private final static String[] CENTENAS = { "", "ciento ", "doscientos ", "trecientos ",
      "cuatrocientos ", "quinientos ", "seiscientos ", "setecientos ", "ochocientos ",
      "novecientos " };

  public static String formatNumber(String number) {
    final UIDefinitionController.FormatDefinition formatDef = UIDefinitionController.getInstance()
        .getFormatDefinition("euro", "Edition");

    String formatWithDot = formatDef.getFormat();
    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    DecimalFormat amountFormatter;
    try {
      dfs.setDecimalSeparator(formatDef.getDecimalSymbol().charAt(0));
      dfs.setGroupingSeparator(formatDef.getGroupingSymbol().charAt(0));
      // Use . as decimal separator
      final String DOT = ".";
      if (!DOT.equals(formatDef.getDecimalSymbol())) {
        formatWithDot = formatWithDot.replace(formatDef.getGroupingSymbol(), "@");
        formatWithDot = formatWithDot.replace(formatDef.getDecimalSymbol(), ".");
        formatWithDot = formatWithDot.replace("@", ",");
      }
      amountFormatter = new DecimalFormat(formatWithDot, dfs);
    } catch (Exception e) {
      // If any error use euroEdition default format
      amountFormatter = new DecimalFormat("#0.00", dfs);
    }
    return amountFormatter.format(new BigDecimal(number));
  }

  public static String Convertir(String numero, String currency) {
    String literal = "";
    String parte_decimal;
    // si el numero utiliza (.) en lugar de (,) -> se reemplaza
    numero = numero.replace(".", ",");
    // si el numero no tiene parte decimal, se le agrega ,00
    if (numero.indexOf(",") == -1) {
      numero = numero + ",00";
    }
    // se valida formato de entrada -> 0,00 y 999 999 999,00
    if (Pattern.matches("\\d{1,9},\\d{1,2}", numero)) {
      // se divide el numero 0000000,00 -> entero y decimal
      String Num[] = numero.split(",");
      // de da formato al numero decimal
      parte_decimal = Num[1] + "/100 " + currency;
      // se convierte el numero a literal
      if (Integer.parseInt(Num[0]) == 0) {// si el valor es cero
        literal = "cero ";
      } else if (Integer.parseInt(Num[0]) > 999999) {// si es millon
        literal = getMillones(Num[0]);
      } else if (Integer.parseInt(Num[0]) > 999) {// si es miles
        literal = getMiles(Num[0]);
      } else if (Integer.parseInt(Num[0]) > 99) {// si es centena
        literal = getCentenas(Num[0]);
      } else if (Integer.parseInt(Num[0]) > 9) {// si es decena
        literal = getDecenas(Num[0]);
      } else {// sino unidades -> 9
        literal = getUnidades(Num[0]);
      }
      // devuelve el resultado en mayusculas o minusculas
      // if (mayusculas) {
      return (literal + "Y " + parte_decimal).toUpperCase();
      // } else {
      // return (literal + parte_decimal);
      // }
    } else {// error, no se puede convertir
      return literal = null;
    }
  }

  public static String getAmountDescription(String numero) {
    String literal = "";
    String parte_decimal;
    // si el numero es negativo quitarle el -
    numero = numero.replace("-", "");
    // si el numero utiliza (.) en lugar de (,) -> se reemplaza
    numero = numero.replace(".", ",");
    // si el numero no tiene parte decimal, se le agrega ,00
    if (numero.indexOf(",") == -1) {
      numero = numero + ",00";
    }
    // se valida formato de entrada -> 0,00 y 999 999 999,00
    if (Pattern.matches("\\d{1,9},\\d{1,2}", numero)) {
      // se divide el numero 0000000,00 -> entero y decimal
      String Num[] = numero.split(",");
      // de da formato al numero decimal
      parte_decimal = Num[1] + "/100";
      // se convierte el numero a literal
      if (Integer.parseInt(Num[0]) == 0) {// si el valor es cero
        literal = "cero ";
      } else if (Integer.parseInt(Num[0]) > 999999) {// si es millon
        literal = getMillones(Num[0]);
      } else if (Integer.parseInt(Num[0]) > 999) {// si es miles
        literal = getMiles(Num[0]);
      } else if (Integer.parseInt(Num[0]) > 99) {// si es centena
        literal = getCentenas(Num[0]);
      } else if (Integer.parseInt(Num[0]) > 9) {// si es decena
        literal = getDecenas(Num[0]);
      } else {// sino unidades -> 9
        literal = getUnidades(Num[0]);
      }
      // devuelve el resultado en mayusculas o minusculas
      // if (mayusculas) {
      return (literal + "Y " + parte_decimal).toUpperCase();
      // } else {
      // return (literal + parte_decimal);
      // }
    } else {// error, no se puede convertir
      return literal = null;
    }
  }

  /* funciones para convertir los numeros a literales */

  private static String getUnidades(String numero) {// 1 - 9
    // si tuviera algun 0 antes se lo quita -> 09 = 9 o 009=9
    String num = numero.substring(numero.length() - 1);
    return UNIDADES[Integer.parseInt(num)];
  }

  private static String getDecenas(String num) {// 99
    int n = Integer.parseInt(num);
    if (n < 10) {// para casos como -> 01 - 09
      return getUnidades(num);
    } else if (n > 19) {// para 20...99
      String u = getUnidades(num);
      u = u.equalsIgnoreCase("un ") ? "uno " : u;
      if (u.equals("")) { // para 20,30,40,50,60,70,80,90
        return DECENAS[Integer.parseInt(num.substring(0, 1)) + 8];
      } else {
        return DECENAS[Integer.parseInt(num.substring(0, 1)) + 8] + "y " + u;
      }
    } else {// numeros entre 11 y 19
      return DECENAS[n - 10];
    }
  }

  private static String getCentenas(String num) {// 999 o 099
    if (Integer.parseInt(num) > 99) {// es centena
      if (Integer.parseInt(num) == 100) {// caso especial
        return " cien ";
      } else {
        return CENTENAS[Integer.parseInt(num.substring(0, 1))] + getDecenas(num.substring(1));
      }
    } else {// por Ej. 099
            // se quita el 0 antes de convertir a decenas
      return getDecenas(Integer.parseInt(num) + "");
    }
  }

  private static String getMiles(String numero) {// 999 999
    // obtiene las centenas
    String c = numero.substring(numero.length() - 3);
    // obtiene los miles
    String m = numero.substring(0, numero.length() - 3);
    String n = "";
    // se comprueba que miles tenga valor entero
    if (Integer.parseInt(m) > 0) {
      n = getCentenas(m);
      return n + "mil " + getCentenas(c);
    } else {
      return "" + getCentenas(c);
    }

  }

  private static String getMillones(String numero) { // 000 000 000
    // se obtiene los miles
    String miles = numero.substring(numero.length() - 6);
    // se obtiene los millones
    String millon = numero.substring(0, numero.length() - 6);
    String n = "";
    if (millon.length() > 1) {
      n = getCentenas(millon) + "millones ";
    } else {
      n = getUnidades(millon) + "millon ";
    }
    return n + getMiles(miles);
  }

  static public OBError completeProspectOrContract(ConnectionProvider connProvider,
      SSAProjPropContract contract) {
    OBError myMessage = null;
    VariablesSecureApp vars = new VariablesSecureApp(OBContext.getOBContext().getUser().getId(),
        OBContext.getOBContext().getCurrentClient().getId(),
        OBContext.getOBContext().getCurrentOrganization().getId(),
        OBContext.getOBContext().getRole().getId(),
        OBContext.getOBContext().getLanguage().getLanguage());
    try {
      contract.setDocumentAction("CO");
      OBDal.getInstance().save(contract);
      OBDal.getInstance().flush();

      OBContext.setAdminMode(true);
      Process process = null;
      try {
        process = OBDal.getInstance().get(Process.class, "195AD5D7C26A4A689AF3C415827E497F");
      } finally {
        OBContext.restorePreviousMode();
      }

      Map<String, String> parameters = null;
      final ProcessInstance pinstance = CallProcess.getInstance().call(process, contract.getId(),
          parameters);

      myMessage = OBMessageUtils.getProcessInstanceMessage(pinstance);
    } catch (Exception ex) {
      myMessage = Utility.translateError(connProvider, vars, vars.getLanguage(), ex.getMessage());
    }
    return myMessage;
  }

}
