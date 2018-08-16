package pe.com.unifiedgo.report.common;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class Utility {

	static Logger log4j = Logger.getLogger(Utility.class);

	private final static String[] UNIDADES = { "", "un ", "dos ", "tres ", "cuatro ", "cinco ", "seis ", "siete ",
			"ocho ", "nueve " };
	private final static String[] DECENAS = { "diez ", "once ", "doce ", "trece ", "catorce ", "quince ", "dieciseis ",
			"diecisiete ", "dieciocho ", "diecinueve", "veinte ", "treinta ", "cuarenta ", "cincuenta ", "sesenta ",
			"setenta ", "ochenta ", "noventa " };
	private final static String[] CENTENAS = { "", "ciento ", "doscientos ", "trecientos ", "cuatrocientos ",
			"quinientos ", "seiscientos ", "setecientos ", "ochocientos ", "novecientos " };

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

	public static String ConvertirCheque(String numero, String currency, String idCurrencyBanco, String idCurrencyPago,
			String posicion) {

		// if(idCurrencyBanco.equalsIgnoreCase(idCurrencyPago)){

		if (idCurrencyBanco.equalsIgnoreCase("100"))
			currency = "DOLARES";
		else if (idCurrencyBanco.equalsIgnoreCase("308"))
			currency = "SOLES";
		else
			currency = "EUROS";

		if (posicion.equals("footer")) {
			currency = "";
		}
		return Convertir(numero, currency);
		// }else if (idcu)

		// return "";
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
			u=u.equalsIgnoreCase("un ")?"uno ":u;
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

	public static Date ParseFecha(String fecha, String DateFormat) {
		SimpleDateFormat formato = new SimpleDateFormat(DateFormat);
		Date fechaDate = null;
		try {
			fechaDate = formato.parse(fecha);
		} catch (ParseException ex) {
			System.out.println(ex);
		}
		return fechaDate;
	}

	public static String MonthToSpanish(Date fecha) {
		String spanishDate = new SimpleDateFormat("MMMM", new Locale("es", "ES")).format(fecha);
		spanishDate = spanishDate.toLowerCase();
		spanishDate = Character.toString(spanishDate.charAt(0)).toUpperCase() + spanishDate.substring(1);
		spanishDate = spanishDate + "-" + new SimpleDateFormat("yyyy").format(fecha);
		return spanishDate;
	}

	public static String OnlyMonthToSpanish(Date fecha) {
		String spanishDate = new SimpleDateFormat("MMMM", new Locale("es", "ES")).format(fecha);
		spanishDate = spanishDate.toLowerCase();
		spanishDate = Character.toString(spanishDate.charAt(0)).toUpperCase() + spanishDate.substring(1);
		return spanishDate;
	}

	public static String dateToSpanish(Date fecha, String dateFormat) {
		String spanishDate = new SimpleDateFormat(dateFormat, new Locale("es", "ES")).format(fecha);
		return spanishDate;
	}

	public static String nombreBancoPlanilla(String codigoBanco) {
		String name = "BCO. " + nombreBanco(codigoBanco);
		return name;
	}

	public static String nombreBanco(String codigoBanco) {
		String name = new String();
		if (codigoBanco.equals("01"))
			name = "CENTRAL RESERVA DEL PERU";
		else if (codigoBanco.equals("02"))
			name = "DE CREDITO DEL PERU";
		else if (codigoBanco.equals("03"))
			name = "INTERNACIONAL DEL PERU";
		else if (codigoBanco.equals("05"))
			name = "LATINO";
		else if (codigoBanco.equals("07"))
			name = "CITIBANK DEL PERU S.A";
		else if (codigoBanco.equals("08"))
			name = "STANDARD CHARTERED";
		else if (codigoBanco.equals("09"))
			name = "SCOTIABANK PERU";
		else if (codigoBanco.equals("11"))
			name = "CONTINENTAL";
		else if (codigoBanco.equals("12"))
			name = "DE LIMA";
		else if (codigoBanco.equals("16"))
			name = "MERCANTIL";
		else if (codigoBanco.equals("18"))
			name = "NACION";
		else if (codigoBanco.equals("22"))
			name = "SANTANDER CENTRAL HISPANO";
		else if (codigoBanco.equals("23"))
			name = "DE COMERCIO";
		else if (codigoBanco.equals("25"))
			name = "REPUBLICA";
		else if (codigoBanco.equals("26"))
			name = "NBK BANK";
		else if (codigoBanco.equals("29"))
			name = "BANCOSUR";
		else if (codigoBanco.equals("35"))
			name = "FINANCIERO DEL PERU";
		else if (codigoBanco.equals("37"))
			name = "DEL PROGRESO";
		else if (codigoBanco.equals("38"))
			name = "INTERAMERICANO FINANZAS";
		else if (codigoBanco.equals("39"))
			name = "BANEX";
		else if (codigoBanco.equals("40"))
			name = "NUEVO MUNDO";
		else if (codigoBanco.equals("41"))
			name = "SUDAMERICANO";
		else if (codigoBanco.equals("42"))
			name = "DEL LIBERTADOR";
		else if (codigoBanco.equals("43"))
			name = "DEL TRABAJO";
		else if (codigoBanco.equals("44"))
			name = "SOLVENTA";
		else if (codigoBanco.equals("45"))
			name = "SERBANCO SA.";
		else if (codigoBanco.equals("46"))
			name = "BANK OF BOSTON";
		else if (codigoBanco.equals("47"))
			name = "ORION";
		else if (codigoBanco.equals("48"))
			name = "DEL PAIS";
		else if (codigoBanco.equals("49"))
			name = "MI BANCO";
		else if (codigoBanco.equals("50"))
			name = "BNP PARIBAS";
		else if (codigoBanco.equals("51"))
			name = "AGROBANCO";
		else if (codigoBanco.equals("53"))
			name = "HSBC BANK PERU S.A.";
		else if (codigoBanco.equals("54"))
			name = "BANCO FALABELLA S.A.";
		else if (codigoBanco.equals("55"))
			name = "BANCO RIPLEY";
		else if (codigoBanco.equals("56"))
			name = "BANCO SANTANDER PERU S.A.";
		else if (codigoBanco.equals("58"))
			name = "BANCO AZTECA DEL PERU";
		else if (codigoBanco.equals("99"))
			name = "OTROS";
		else
			name = "";
		return name;
	}

	public static boolean isNumeric(String str) {
		try {
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static String tipoDocumento(String str) {
		return "CUALQUIERA";
	}

	public static Date sumarRestarDiasFecha(Date fecha, int dias) {

		Calendar calendar = Calendar.getInstance();

		calendar.setTime(fecha); // Configuramos la fecha que se recibe

		calendar.add(Calendar.DAY_OF_YEAR, dias); // numero de días a añadir, o
		// restar en caso de días<0

		return calendar.getTime(); // Devuelve el objeto Date con los nuevos
		// días añadidos
	}

	public static String formatearNumeroFisico(String numero, String es_renovacion) {
		
		String ultimoCaracter = numero.substring(numero.length()-1, numero.length());
		
		numero = numero.replaceAll("[^0-9]", "");

		if (es_renovacion.compareTo("Y") == 0){

			if(Character.isDigit(ultimoCaracter.charAt(0)))
				ultimoCaracter="A";
			
			numero += ultimoCaracter;
		}
		
		Integer longNumero = numero.length();
		String preCadena = "";

		if (longNumero <= 7) {
			for (int k = 0; k < 7 - longNumero; k++) {
				preCadena += "0";
			}
		} else {
			numero = numero.substring(longNumero - 7, longNumero);
		}

		return (preCadena + numero);		
	}

	public static String rellenaAsteriscos(String numero) {
		if (!numero.contains("."))
			numero = "0.00";

		String[] partNumero = numero.split(Pattern.quote("."));

		String cadAsteriscos = "";

		for (int i = 0; i < 10 - partNumero[0].length(); i++) {
			cadAsteriscos = cadAsteriscos + "*";
		}

		return cadAsteriscos + partNumero[0] + "." + partNumero[1];

	}

	public static String extracFromRegNumber(String numero) {

		if (numero == null)
			return "";
		if(numero.contains("-")){
			
			String [] numeroParts = numero.split("-");
			
			return new Integer (numeroParts[1]).toString();
			
		}else 
			return "";
	}
	
	public static String dataToString(Date fecha , String distrito){
		
		SimpleDateFormat dt = new SimpleDateFormat("dd-MM-yyyy");
		String strFecha = dt.format(fecha);
		
		String []	partesFecha= strFecha.split("-");
		
		if(distrito!=null){
			return distrito+ " "+partesFecha[0]+" de "+nombreMes(partesFecha[1])+ " del "+partesFecha[2];
		}else {
			return partesFecha[0]+" de "+nombreMes(partesFecha[1])+ " del "+partesFecha[2];
		}
		
	}
	
	private static String nombreMes(String mes) {

		Integer mesInt = new Integer(mes);

		String[] meses = { "Enero", "Febrero", "Marzo", "Abril", "Mayo",
				"Junio", "Julio", "Agosto", "Septiembre", "Octubre",
				"Noviembre", "Diciembre" };
		String retornaMes = "";

		retornaMes = meses[mesInt - 1];
		return retornaMes;

	}
	
	
	public static String spacingCharacters(String texto){
		
		String nuevoTexto = texto.replaceAll("", " ");
		String [] otroTexto  = texto.split("");
		String textoFinal="";
		for(int i=0;i<otroTexto.length;i++){
			textoFinal=textoFinal+" ";
		}
		return nuevoTexto;
	}
	
	public static String completeConZeros(BigDecimal numero){
		
		if(numero==null){
			numero=BigDecimal.ZERO;
		}
		Integer num= numero.intValue();
		
		String txtNumero = String.valueOf(num);

		for(int i=txtNumero.length();i<7;i++){
			txtNumero="0"+txtNumero;
		}
		return txtNumero;
	}

	// Factura de Venta nº FV102137-16 creado nn.SKP-201602658
	// PL101242-16, Documento de Salida: SAL101531-16

}
