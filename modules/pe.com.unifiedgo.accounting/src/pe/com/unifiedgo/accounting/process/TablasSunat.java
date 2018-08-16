package pe.com.unifiedgo.accounting.process;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.database.ConnectionProvider;

public class TablasSunat extends HttpSecureAppServlet{
	
	private static final long serialVersionUID = 1L;
	  private static final BigDecimal ZERO = BigDecimal.ZERO;
	  
	  @Override
	  public void init(ServletConfig config) {
	    super.init(config);
	    boolHist = false;
	  }
	
	public static final String REQUIREDFIELDMESSAGE = "Campo Obligatorio. ";
	public static final String INVALIDVALUEMESSAGE = "Valor inválido. ";
	public static final String INVALIDCHARACTERMESSAGE = "Carácter inválido. ";
	public static final String WRONGLENGTHMESSAGE = "Longitud incorrecta. ";
	public static final String LENGTHEXCEEDINGLIMITMESSAGE = "Longitud superior al límite. ";
	public static final String INCORRECTDATEFORMATMESSAGE = "Formato de fecha incorrecto. ";
	public static final String INCORRECTPERIODFORMATMESSAGE = "Formato de periodo incorrecto. ";
	public static final String INCORRECTFORMATMESSAGE = "Formato incorrecto. ";
	public static final String VALUESHOULDBEPOSITIVEMESSAGE = "El valor debe ser positivo. ";
	public static final String VALUESHOULDBENEGATIVEMESSAGE = "El valor debe ser negativo. ";
	
	public static final String DATEPATTERN = "^(0[0-9]|1[0-9]|2[0-9]|3[0-1])\\/(0[1-9]|1[0-2])\\/(19|20)\\d{2}$";
	public static final String CORRELATIVENUMBERPATTERN = "^(A|B|C)(\\w|\\(|\\)|\\,|\\.|\\-)((\\w|\\(|\\)|\\,|\\.|\\-){0,8})$";
	public static final String NUMERICAMOUNTPATTERN = "^\\d{1,12}(\\.\\d{1,2}){0,1}$";
	public static final String NEGATIVENUMERICAMOUNTPATTERN = "^-{0,1}\\d{1,12}(\\.\\d{1,2}){0,1}$";
	public static final String PERIODPATTERN = "^(19|20)\\d\\d(0[1-9]|1[0-2])(00)$";
	public static final String CONVERSIONRATEPATTERN = "^\\d\\.\\d{3}$";
	
	/* expresiones para formatos de documentos */
	public static final String PLEFORMATOSERIEFACTURA = "^\\d{4}|E001|F(\\d|\\w)(\\d|\\w)(\\d|\\w)$";
	public static final String PLEFORMATOSERIEBOLETA = "^\\d{4}|EB01|B(\\d|\\w)(\\d|\\w)(\\d|\\w)$";
	
	public static final String PLEFORMATOLIQUIDACIONCOMPRA = "^$";
	
	public static final String PLEFORMATONOTADECREDITO = "^\\d{4}|E001|EB01|F(\\d|\\w)(\\d|\\w)(\\d|\\w)|B(\\d|\\w)(\\d|\\w)(\\d|\\w)$";
	
	public static final String PLEFORMATONOTADEDEBITO = "^\\d{4}|E001|EB01|F(\\d|\\w)(\\d|\\w)(\\d|\\w)|B(\\d|\\w)(\\d|\\w)(\\d|\\w)$";
	
	
	/* variables para los valores de los PLE */
	
	public static final String PLEREGISTROVENTAS = "PLEREGISTROVENTAS";
	public static final String PLEREGISTROCOMPRAS = "PLEREGISTROCOMPRAS";
	public static final String PLEOTROSLIBROS = "PLEOTROSLIBROS";
	
	/* documentos SUNAT */
	
	public static final String PLEOTROS00 = "Otros";
	public static final String PLEFACTURA01 = "Factura";
	public static final String PLERECIBOPORHONORARIOS02 = "Recibo por Honorarios";
	public static final String PLEBOLETADEVENTA03 = "Boleta de Venta";
	public static final String PLELIQUIDACIONDECOMPRA04 = "Liquidación de compra";
	public static final String PLEBOLETOSDETRANSPORTEAEREO05 = "Boletos de Transporte Aéreo ...";
	public static final String PLECARTADEPORTEAEREO06 = "Carta de porte aéreo ...";
	public static final String PLENOTADECREDITO07 = "Nota de crédito";
	public static final String PLENOTADEDEBITO08 = "Nota de débito";
	public static final String PLEGUIADEREMISIONREMITENTE09 = "Guía de remisión - Remitente";
	public static final String PLERECIBOPORARRENDAMIENTO10 = "Recibo por Arrendamiento";
	public static final String PLEPOLIZAEMITIDAPORLASBOLSAS11 = "Póliza emitida por las Bolsas de Valores ...";
	public static final String PLETICKETOCINTA12 = "Ticket o cinta emitido ...";
	public static final String PLEDOCUMENTOSEMITIDOSPORLASEMPRESAS13 = "Documentos emitidos por las empresas ...";
	public static final String PLERECIBOPORSERVICIOSPUBLICOS14 = "Recibo por servicios públicos ...";
	public static final String PLEBOLETOSEMITIDOSPORELSERVICIO15 = "Boletos emitidos por el servicio ...";
	public static final String PLEBOLETOSDEVIAJEEMITIDOS16 = "Boletos de viaje emitidos ...";
	
	public static plecomprobante getValuesFromPLE(String plevalue, String CodDocumento){
		plecomprobante objvalidate = new plecomprobante();
		
		if(plevalue.equalsIgnoreCase("PLEREGISTROVENTAS")){
			
			if(CodDocumento.equalsIgnoreCase("00")){
				objvalidate.Documento = PLEOTROS00;
				
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 20;
				objvalidate.ObliSerie = false;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 20;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = true;
				
			}else if(CodDocumento.equalsIgnoreCase("01")){
				objvalidate.Documento = PLEFACTURA01;
				
				objvalidate.formatoSerie = PLEFORMATOSERIEFACTURA;
				
				objvalidate.LongFijSerie = true;
				objvalidate.LongSerie = 4;
				objvalidate.ObliSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 8;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("03")){
				objvalidate.Documento = PLEBOLETADEVENTA03;
				
				objvalidate.LongFijSerie = true;
				objvalidate.LongSerie = 4;
				objvalidate.ObliSerie = true;
				
				objvalidate.formatoSerie = PLEFORMATOSERIEBOLETA;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 8;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("04")){
				objvalidate.Documento = PLELIQUIDACIONDECOMPRA04;
				
				objvalidate.LongFijSerie = true;
				objvalidate.LongSerie = 4;
				objvalidate.ObliSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 8;
				objvalidate.ObliNumero = true;
				
			}else if(CodDocumento.equalsIgnoreCase("05")){
				objvalidate.Documento = PLEBOLETOSDETRANSPORTEAEREO05;
				
				objvalidate.LongFijSerie = true;
				objvalidate.LongSerie = 1;
				objvalidate.ObliSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 11;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("06")){
				objvalidate.Documento = PLECARTADEPORTEAEREO06;
				
				objvalidate.LongFijSerie = true;
				objvalidate.LongSerie = 4;
				objvalidate.ObliSerie = true;
				objvalidate.AlfNumSerie = false;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 8;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("07")){
				objvalidate.Documento = PLENOTADECREDITO07;
				
				objvalidate.LongFijSerie = true;
				objvalidate.LongSerie = 4;
				objvalidate.ObliSerie = true;
				
				objvalidate.formatoSerie = PLEFORMATONOTADECREDITO;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 8;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("08")){
				objvalidate.Documento = PLENOTADEDEBITO08;
				
				objvalidate.LongFijSerie = true;
				objvalidate.LongSerie = 4;
				objvalidate.ObliSerie = true;
				
				objvalidate.formatoSerie = PLEFORMATONOTADEDEBITO;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 8;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("11")){
				objvalidate.Documento = PLEPOLIZAEMITIDAPORLASBOLSAS11;
				
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 20;
				objvalidate.ObliSerie = false;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = true;
				objvalidate.LongNumero = 15;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("12")){
				objvalidate.Documento = PLETICKETOCINTA12;
				
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 20;
				objvalidate.ObliSerie = true;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 20;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("13")){
				objvalidate.Documento = PLEDOCUMENTOSEMITIDOSPORLASEMPRESAS13;
				
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 20;
				objvalidate.ObliSerie = false;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 20;
				objvalidate.ObliNumero = true;
				
			}else if(CodDocumento.equalsIgnoreCase("14")){
				objvalidate.Documento = PLERECIBOPORSERVICIOSPUBLICOS14;
				
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 20;
				objvalidate.ObliSerie = false;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 20;
				objvalidate.ObliNumero = true;
				
			}else if(CodDocumento.equalsIgnoreCase("15")){
				objvalidate.Documento = PLEBOLETOSEMITIDOSPORELSERVICIO15;
				
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 20;
				objvalidate.ObliSerie = false;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 20;
				objvalidate.ObliNumero = true;
				
			}else if(CodDocumento.equalsIgnoreCase("16")){
				objvalidate.Documento = PLEBOLETOSDEVIAJEEMITIDOS16;
				
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 20;
				objvalidate.ObliSerie = false;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 20;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("17")){
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 20;
				objvalidate.ObliSerie = false;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 20;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("18")){
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 20;
				objvalidate.ObliSerie = false;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 20;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("19")){
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 20;
				objvalidate.ObliSerie = false;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 20;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
			}
			
		}else if(plevalue.equalsIgnoreCase("PLEREGISTROCOMPRAS")){
			
			if(CodDocumento.equalsIgnoreCase("00")){
				objvalidate.Documento = PLEOTROS00;
				
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 20;
				objvalidate.ObliSerie = false;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 20;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = true;
				
			}else if(CodDocumento.equalsIgnoreCase("01")){
				objvalidate.Documento = PLEFACTURA01;
				
				objvalidate.LongFijSerie = true;
				objvalidate.LongSerie = 4;
				objvalidate.ObliSerie = true;
				
				objvalidate.formatoSerie = PLEFORMATOSERIEFACTURA;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 8;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("02")){
				objvalidate.Documento = PLERECIBOPORHONORARIOS02;
				
				objvalidate.LongFijSerie = true;
				objvalidate.LongSerie = 4;
				objvalidate.ObliSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 7;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("03")){
				objvalidate.Documento = PLEBOLETADEVENTA03;
				
				objvalidate.LongFijSerie = true;
				objvalidate.LongSerie = 4;
				objvalidate.ObliSerie = true;
				
				objvalidate.formatoSerie = PLEFORMATOSERIEBOLETA;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 8;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
				
			}else if(CodDocumento.equalsIgnoreCase("04")){
				objvalidate.Documento = PLELIQUIDACIONDECOMPRA04;
				
				objvalidate.LongFijSerie = true;
				objvalidate.LongSerie = 4;
				objvalidate.ObliSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 8;
				objvalidate.ObliNumero = true;
				
			}else if(CodDocumento.equalsIgnoreCase("05")){
				objvalidate.Documento = PLEBOLETOSDETRANSPORTEAEREO05;
				
				objvalidate.LongFijSerie = true;
				objvalidate.LongSerie = 4;
				objvalidate.ObliSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 11;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("06")){
				objvalidate.Documento = PLECARTADEPORTEAEREO06;
				
				objvalidate.LongFijSerie = true;
				objvalidate.LongSerie = 4;
				objvalidate.ObliSerie = true;
				objvalidate.AlfNumSerie = false;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 8;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("07")){
				objvalidate.Documento = PLENOTADECREDITO07;
				
				objvalidate.LongFijSerie = true;
				objvalidate.LongSerie = 4;
				objvalidate.ObliSerie = true;
				
				objvalidate.formatoSerie = PLEFORMATONOTADECREDITO;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 8;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("08")){
				objvalidate.Documento = PLENOTADEDEBITO08;
				
				objvalidate.LongFijSerie = true;
				objvalidate.LongSerie = 4;
				objvalidate.ObliSerie = true;
				
				objvalidate.formatoSerie = PLEFORMATONOTADEDEBITO;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 8;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("10")){
				objvalidate.Documento = PLERECIBOPORARRENDAMIENTO10;
				
				objvalidate.LongFijSerie = true;
				objvalidate.LongSerie = 4;
				objvalidate.ObliSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 20;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("11")){
				objvalidate.Documento = PLEPOLIZAEMITIDAPORLASBOLSAS11;
				
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 20;
				objvalidate.ObliSerie = false;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = true;
				objvalidate.LongNumero = 15;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("12")){
				objvalidate.Documento = PLETICKETOCINTA12;
				
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 20;
				objvalidate.ObliSerie = true;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 20;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("13")){
				objvalidate.Documento = PLEDOCUMENTOSEMITIDOSPORLASEMPRESAS13;
				
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 42;
				objvalidate.ObliSerie = false;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 20;
				objvalidate.ObliNumero = true;
				
			}else if(CodDocumento.equalsIgnoreCase("14")){
				objvalidate.Documento = PLERECIBOPORSERVICIOSPUBLICOS14;
				
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 20;
				objvalidate.ObliSerie = false;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 20;
				objvalidate.ObliNumero = true;
				
			}else if(CodDocumento.equalsIgnoreCase("15")){
				objvalidate.Documento = PLEBOLETOSEMITIDOSPORELSERVICIO15;
				
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 20;
				objvalidate.ObliSerie = false;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 20;
				objvalidate.ObliNumero = true;
				
			}else if(CodDocumento.equalsIgnoreCase("16")){
				objvalidate.Documento = PLEBOLETOSDEVIAJEEMITIDOS16;
				
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 20;
				objvalidate.ObliSerie = false;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 20;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("17")){
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 20;
				objvalidate.ObliSerie = false;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 20;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("18")){
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 20;
				objvalidate.ObliSerie = false;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 20;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
				
			}else if(CodDocumento.equalsIgnoreCase("19")){
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 20;
				objvalidate.ObliSerie = false;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 20;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
				
			}
			
		}else if(plevalue.equalsIgnoreCase("PLEOTROSLIBROS")){
			
			if(CodDocumento.equalsIgnoreCase("00")){
				objvalidate.Documento = PLEOTROS00;
				
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 20;
				objvalidate.ObliSerie = false;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 20;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = true;
				
			}else if(CodDocumento.equalsIgnoreCase("01")){
				objvalidate.Documento = PLEFACTURA01;
				
				objvalidate.LongFijSerie = true;
				objvalidate.LongSerie = 4;
				objvalidate.ObliSerie = true;
				
				objvalidate.formatoSerie = PLEFORMATOSERIEFACTURA;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 8;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
				
			}else if(CodDocumento.equalsIgnoreCase("02")){
				objvalidate.Documento = PLERECIBOPORHONORARIOS02;
				
				objvalidate.LongFijSerie = true;
				objvalidate.LongSerie = 4;
				objvalidate.ObliSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 7;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
				
			}else if(CodDocumento.equalsIgnoreCase("03")){
				objvalidate.Documento = PLEBOLETADEVENTA03;
				
				objvalidate.LongFijSerie = true;
				objvalidate.LongSerie = 4;
				objvalidate.ObliSerie = true;
				
				objvalidate.formatoSerie = PLEFORMATOSERIEBOLETA;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 8;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("04")){
				objvalidate.Documento = PLELIQUIDACIONDECOMPRA04;
				
				objvalidate.LongFijSerie = true;
				objvalidate.LongSerie = 4;
				objvalidate.ObliSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 8;
				objvalidate.ObliNumero = true;
				
			}else if(CodDocumento.equalsIgnoreCase("05")){
				objvalidate.Documento = PLEBOLETOSDETRANSPORTEAEREO05;
				
				objvalidate.LongFijSerie = true;
				objvalidate.LongSerie = 1;
				objvalidate.ObliSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 11;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("06")){
				objvalidate.Documento = PLECARTADEPORTEAEREO06;
				
				objvalidate.LongFijSerie = true;
				objvalidate.LongSerie = 4;
				objvalidate.ObliSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 8;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("07")){
				objvalidate.Documento = PLENOTADECREDITO07;
				
				objvalidate.LongFijSerie = true;
				objvalidate.LongSerie = 4;
				objvalidate.ObliSerie = true;
				
				objvalidate.formatoSerie = PLEFORMATONOTADECREDITO;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 8;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("08")){
				objvalidate.Documento = PLENOTADEDEBITO08;
				
				objvalidate.LongFijSerie = true;
				objvalidate.LongSerie = 4;
				objvalidate.ObliSerie = true;
				
				objvalidate.formatoSerie = PLEFORMATONOTADEDEBITO;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 8;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
				
			}else if(CodDocumento.equalsIgnoreCase("09")){
				objvalidate.Documento = PLEGUIADEREMISIONREMITENTE09;
				
				objvalidate.LongFijSerie = true;
				objvalidate.LongSerie = 4;
				objvalidate.ObliSerie = true;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 8;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = true;
				
				
			}else if(CodDocumento.equalsIgnoreCase("10")){
				objvalidate.Documento = PLERECIBOPORARRENDAMIENTO10;
				
				objvalidate.LongFijSerie = true;
				objvalidate.LongSerie = 4;
				objvalidate.ObliSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 20;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
				
			}else if(CodDocumento.equalsIgnoreCase("11")){
				objvalidate.Documento = PLEPOLIZAEMITIDAPORLASBOLSAS11;
				
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 20;
				objvalidate.ObliSerie = false;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = true;
				objvalidate.LongNumero = 15;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
				
			}else if(CodDocumento.equalsIgnoreCase("12")){
				objvalidate.Documento = PLETICKETOCINTA12;
				
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 20;
				objvalidate.ObliSerie = true;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 20;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
				
			}else if(CodDocumento.equalsIgnoreCase("13")){
				objvalidate.Documento = PLEDOCUMENTOSEMITIDOSPORLASEMPRESAS13;
				
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 20;
				objvalidate.ObliSerie = false;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 20;
				objvalidate.ObliNumero = true;
				
				
			}else if(CodDocumento.equalsIgnoreCase("14")){
				objvalidate.Documento = PLERECIBOPORSERVICIOSPUBLICOS14;
				
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 20;
				objvalidate.ObliSerie = false;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 20;
				objvalidate.ObliNumero = true;
				
				
			}else if(CodDocumento.equalsIgnoreCase("15")){
				objvalidate.Documento = PLEBOLETOSEMITIDOSPORELSERVICIO15;
				
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 20;
				objvalidate.ObliSerie = false;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 20;
				objvalidate.ObliNumero = true;
				
				
			}else if(CodDocumento.equalsIgnoreCase("16")){
				objvalidate.Documento = PLEBOLETOSDEVIAJEEMITIDOS16;
				
				objvalidate.LongFijSerie = false;
				objvalidate.LongSerie = 20;
				objvalidate.ObliSerie = false;
				objvalidate.AlfNumSerie = true;
				
				objvalidate.LongFijNumero = false;
				objvalidate.LongNumero = 20;
				objvalidate.ObliNumero = true;
				objvalidate.AlfNumMumero = false;
				
			}else if(CodDocumento.equalsIgnoreCase("17")){
				
			}else if(CodDocumento.equalsIgnoreCase("18")){
				
			}else if(CodDocumento.equalsIgnoreCase("19")){
				
			}else if(CodDocumento.equalsIgnoreCase("20")){
				
			}
		}
		
		return objvalidate;
	}
	
	
	
	public String CompletarLongitud(int LongitudPLE, String valor){
		String formato = "%-"+LongitudPLE+"s";
		String cadena = String.format(formato, valor);
		return cadena;
	}
	
	
	
	public static HashMap<String, String> getTabla02Sunat(){
		HashMap<String, String> map = new HashMap<String, String>();
		
		map.put("0", "OTROS TIPOS DE DOCUMENTOS");
		map.put("1", "DOCUMENTO NACIONAL DE IDENTIDAD (DNI)");
		map.put("4", "CARNET DE EXTRANJERIA");
		map.put("6", "REGISTRO ÚNICO DE CONTRIBUYENTES");
		map.put("7", "PASAPORTE");
		map.put("A", "CÉDULA DIPLOMÁTICA DE IDENTIDAD");
		
		return map;
	}

	public static HashMap<String, String> getTabla04Sunat(){
		HashMap<String, String> map = new HashMap<String, String>();

		map.put("AED", "UAE Dirham");
		map.put("AFN", "Afghani");
		map.put("ALL", "Lek");
		map.put("AMD", "Armenian Dram");
		map.put("ANG", "Netherlands Antillian Guilder");
		map.put("AOA", "Kwanza");
		map.put("ARS", "Argentine Peso");
		map.put("AUD", "Australian Dollar");
		map.put("AWG", "Aruban Guilder");
		map.put("AZN", "Azerbaijanian Manat");
		
		map.put("BAM", "Convertible Marks");
		map.put("BBD", "Barbados Dollar");
		map.put("BDT", "Taka");
		map.put("BGN", "Bulgarian Lev");
		map.put("BHD", "Bahraini Dinar");
		map.put("BIF", "Burundi Franc");
		map.put("BMD", "Bermudian Dollar (customarily known as Bermuda Dollar)");
		map.put("BND", "Brunei Dollar");
		map.put("BOB", "Boliviano");
		map.put("BOV", "Mvdol");
		
		map.put("BRL", "Brazilian Real");
		map.put("BSD", "Bahamian Dollar");
		map.put("BTN", "Ngultrum");
		map.put("BWP", "Pula");
		map.put("BYR", "Belarussian Ruble");
		map.put("BZD", "Belize Dollar");
		map.put("CAD", "Canadian Dollar");
		map.put("CDF", "Congolese Franc");
		map.put("CHE", "WIR Euro");
		map.put("CHF", "Swiss Franc");
		
		map.put("CHW", "WIR Franc");
		map.put("CLF", "Unidades de fomento");
		map.put("CLP", "Chilean Peso");
		map.put("CNY", "Yuan Renminbi");
		map.put("COP", "Colombian Peso");
		map.put("COU", "Unidad de Valor Real");
		map.put("CRC", "Costa Rican Colon");
		map.put("CUC", "Peso Convertible");
		map.put("CUP", "Cuban Peso");
		map.put("CVE", "Cape Verde Escudo");
		
		map.put("CZK", "Czech Koruna");
		map.put("DJF", "Djibouti Franc");
		map.put("DKK", "Danish Krone");
		map.put("DOP", "Dominican Peso");
		map.put("DZD", "Algerian Dinar");
		map.put("EEK", "Kroon");
		map.put("EGP", "Egyptian Pound");
		map.put("ERN", "Nakfa");
		map.put("ETB", "Ethiopian Birr");
		map.put("EUR", "Euro");
		
		map.put("FJD", "Fiji Dollar");
		map.put("FKP", "Falkland Islands Pound");
		map.put("GBP", "Pound Sterling");
		map.put("GEL", "Lari");
		map.put("GHS", "Cedi");
		map.put("GIP", "Gibraltar Pound");
		map.put("GMD", "Dalasi");
		map.put("GNF", "Guinea Franc");
		map.put("GTQ", "Quetzal");
		map.put("GYD", "Guyana Dollar");
		
		map.put("HKD", "Hong Kong Dollar");
		map.put("HNL", "Lempira");
		map.put("HRK", "Croatian Kuna");
		map.put("HTG", "Gourde");
		map.put("HUF", "Forint");
		map.put("IDR", "Rupiah");
		map.put("ILS", "New Israeli Sheqel");
		map.put("INR", "Indian Rupee");
		map.put("IQD", "Iraqi Dinar");
		map.put("IRR", "Iranian Rial");
		
		map.put("ISK", "Iceland Krona");
		map.put("JMD", "Jamaican Dollar");
		map.put("JOD", "Jordanian Dinar");
		map.put("JPY", "Yen");
		map.put("KES", "Kenyan Shilling");
		map.put("KGS", "Som");
		map.put("KHR", "Riel");
		map.put("KMF", "Comoro Franc");
		map.put("KPW", "North Korean Won");
		map.put("KRW", "Won");
		
		map.put("KWD", "Kuwaiti Dinar");
		map.put("KYD", "Cayman Islands Dollar");
		map.put("KZT", "Tenge");
		map.put("LAK", "Kip");
		map.put("LBP", "Lebanese Pound");
		map.put("LKR", "Sri Lanka Rupee");
		map.put("LRD", "Liberian Dollar");
		map.put("LSL", "Loti");
		map.put("LTL", "Lithuanian Litas");
		map.put("LVL", "Latvian Lats");
		
		map.put("LYD", "Libyan Dinar");
		map.put("MAD", "Moroccan Dirham");
		map.put("MDL", "Moldovan Leu");
		map.put("MGA", "Malagasy Ariary");
		map.put("MKD", "Denar");
		map.put("MMK", "Kyat");
		map.put("MNT", "Tugrik");
		map.put("MOP", "Pataca");
		map.put("MRO", "Ouguiya");
		map.put("MUR", "Mauritius Rupee");
		
		map.put("MVR", "Rufiyaa");
		map.put("MWK", "Kwacha");
		map.put("MXN", "Mexican Peso");
		map.put("MXV", "Mexican Unidad de Inversion (UDI)");
		map.put("MYR", "Malaysian Ringgit");
		map.put("MZN", "Metical");
		map.put("NAD", "Namibia Dollar");
		map.put("NGN", "Naira");
		map.put("NIO", "Cordoba Oro");
		map.put("NOK", "Norwegian Krone");
		
		map.put("NPR", "Nepalese Rupee");
		map.put("NZD", "New Zealand Dollar");
		map.put("OMR", "Rial Omani");
		map.put("PAB", "Balboa");
		map.put("PEN", "Nuevo Sol o Sol");
		map.put("PGK", "Kina");
		map.put("PHP", "Philippine Peso");
		map.put("PKR", "Pakistan Rupee");
		map.put("PLN", "Zloty");
		map.put("PYG", "Guarani");
		
		map.put("QAR", "Qatari Rial");
		map.put("RON", "New Leu");
		map.put("RSD", "Serbian Dinar");
		map.put("RUB", "Russian Ruble");
		map.put("RWF", "Rwanda Franc");
		map.put("SAR", "Saudi Riyal");
		map.put("SBD", "Solomon Islands Dollar");
		map.put("SCR", "Seychelles Rupee");
		map.put("SDG", "Sudanese Pound");
		map.put("SEK", "Swedish Krona");
		
		map.put("SGD", "Singapore Dollar");
		map.put("SHP", "Saint Helena Pound");
		map.put("SLL", "Leone");
		map.put("SOS", "Somali Shilling");
		map.put("SRD", "Surinam Dollar");
		map.put("STD", "Dobra");
		map.put("SVC", "El Salvador Colon");
		map.put("SYP", "Syrian Pound");
		map.put("SZL", "Lilangeni");
		map.put("THB", "Baht");
		
		map.put("TJS", "Somoni");
		map.put("TMT", "Manat");
		map.put("TND", "Tunisian Dinar");
		map.put("TOP", "Pa'anga");
		map.put("TRY", "Turkish Lira");
		map.put("TTD", "Trinidad and Tobago Dollar");
		map.put("TWD", "New Taiwan Dollar");
		map.put("TZS", "Tanzanian Shilling");
		map.put("UAH", "Hryvnia");
		map.put("UGX", "Uganda Shilling");
		
		map.put("USD", "US Dollar");
		map.put("USN", "US Dollar (Next day)");
		map.put("USS", "US Dollar (Same day)");
		map.put("UYI", "Uruguay Peso en Unidades Indexadas");
		map.put("UYU", "Peso Uruguayo");
		map.put("UZS", "Uzbekistan Sum");
		map.put("VEF", "Bolivar Fuerte");
		map.put("VND", "Dong");
		map.put("VUV", "Vatu");
		map.put("WST", "Tala");
		
		map.put("XAF", "CFA Franc BEAC ‡");
		map.put("XAG", "Silver");
		map.put("XAU", "Gold");
		map.put("XBA", "Bond Markets Units European Composite Unit (EURCO)");
		map.put("XBB", "European Monetary Unit (E.M.U.-6)");
		map.put("XBC", "European Unit of Account 9(E.U.A.-9)");
		map.put("XBD", "European Unit of Account 17(E.U.A.-17)");
		map.put("XCD", "East Caribbean Dollar");
		map.put("XDR", "SDR");
		map.put("XFU", "UIC-Franc");
		
		map.put("XOF", "CFA Franc BCEAO †");
		map.put("XPD", "Palladium");
		map.put("XPF", "CFP Franc");
		map.put("XPT", "Platinum");
		map.put("YER", "Yemeni Rial");
		map.put("ZAR", "Rand");
		map.put("ZMK", "Zambian Kwacha");
		map.put("ZWL", "Zimbabwe Dollar");
		
		return map;
	}

	public static HashMap<String, String> getTabla10Sunat(){
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("00", "Otros");
		map.put("01", "Factura");
		map.put("02", "Recibo por Honorarios");
		map.put("03", "Boleta de Venta");
		map.put("04", "Liquidación de compra");
		map.put("05", "Boletos de Transporte Aéreo que emiten las Compañías de Aviación Comercial por el servicio de transporte aéreo regular de pasajeros, emitido de manera manual, mecanizada o por medios electrónicos (BME)");
		map.put("06", "Carta de porte aéreo por el servicio de transporte de carga aérea");
		map.put("07", "Nota de crédito");
		map.put("08", "Nota de débito");
		map.put("09", "Guía de remisión - Remitente");
		map.put("10", "Recibo por Arrendamiento");
		map.put("11", "Póliza emitida por las Bolsas de Valores, Bolsas de Productos o Agentes de Intermediación por operaciones realizadas en las Bolsas de Valores o Productos o fuera de las mismas, autorizadas por SMV");
		map.put("12", "Ticket o cinta emitido por máquina registradora");
		map.put("13", "Documentos emitidos por las empresas del sistema financiero y de seguros, y por las cooperativas de ahorro y crédito no autorizadas a captar recursos del público, que se encuentren bajo el control de la Superintendencia de Banca, Seguros y AFP.");
		map.put("14", "Recibo por servicios públicos de suministro de energía eléctrica, agua, teléfono, telex y telegráficos y otros servicios complementarios que se incluyan en el recibo de servicio público");
		map.put("15", "Boletos emitidos por el servicio de transporte terrestre regular urbano de pasajeros y el ferroviario público de pasajeros prestado en vía férrea local.");
		map.put("16", "Boletos de viaje emitidos por las empresas de transporte nacional de pasajeros, siempre que cuenten con la autorización de la autoridad competente, en las rutas autorizadas. Vía terrestre o ferroviario público no emitido por medios electrónicos (BVME)");
		map.put("17", "Documento emitido por la Iglesia Católica por el arrendamiento de bienes inmuebles");
		map.put("18", "Documento emitido por las Administradoras Privadas de Fondo de Pensiones que se encuentran bajo la supervisión de la Superintendencia de Banca, Seguros y AFP");
		map.put("19", "Boleto o entrada por atracciones y espectáculos públicos");
		map.put("20", "Comprobante de Retención");
		map.put("21", "Conocimiento de embarque por el servicio de transporte de carga marítima");
		map.put("22", "Comprobante por Operaciones No Habituales");
		map.put("23", "Pólizas de Adjudicación emitidas con ocasión del remate o adjudicación de bienes por venta forzada, por los martilleros o las entidades que rematen o subasten bienes por cuenta de terceros");
		map.put("24", "Certificado de pago de regalías emitidas por PERUPETRO S.A");
		map.put("25", "Documento de Atribución (Ley del Impuesto General a las Ventas e Impuesto Selectivo al Consumo, Art. 19º, último párrafo, R.S. N° 022-98-SUNAT).");
		map.put("26", "Recibo por el Pago de la Tarifa por Uso de Agua Superficial con fines agrarios y por el pago de la Cuota para la ejecución de una determinada obra o actividad acordada por la Asamblea General de la Comisión de Regantes o Resolución expedida por el Jefe de la Unidad de Aguas y de Riego (Decreto Supremo N° 003-90-AG, Arts. 28 y 48)");
		map.put("27", "Seguro Complementario de Trabajo de Riesgo");
		map.put("28", "Documentos emitidos por los servicios aeroportuarios prestados a favor de los pasajeros, mediante mecanismo de etiquetas autoadhesivas.");
		map.put("29", "Documentos emitidos por la COFOPRI en calidad de oferta de venta de terrenos, los correspondientes a las subastas públicas y a la retribución de los servicios que presta");
		map.put("30", "Documentos emitidos por las empresas que desempeñan el rol adquirente en los sistemas de pago mediante tarjetas de crédito y débito, emitidas por bancos e instituciones financieras o crediticias, domiciliados o no en el país.");
		map.put("31", "Guía de Remisión - Transportista");
		map.put("32", "Documentos emitidos por las empresas recaudadoras de la denominada Garantía de Red Principal a la que hace referencia el numeral 7.6 del artículo 7° de la Ley N° 27133 – Ley de Promoción del Desarrollo de la Industria del Gas Natural");
		map.put("33", "Manifiesto de Pasajeros");
		map.put("34", "Documento del Operador");
		map.put("35", "Documento del Partícipe");
		map.put("36", "Recibo de Distribución de Gas Natural");
		map.put("37", "Documentos que emitan los concesionarios del servicio de revisiones técnicas vehiculares, por la prestación de dicho servicio");
		map.put("40", "Comprobante de Percepción");
		map.put("41", "Comprobante de Percepción - Venta interna");
		map.put("42", "Documentos emitidos por las empresas que desempeñan el rol adquiriente en los sistemas de pago mediante tarjetas de crédito emitidas por ellas mismas");
		map.put("43", "Boletos emitidos por las Compañías de Aviación Comercial que prestan servicios de transporte aéreo no regular de pasajeros y transporte aéreo especial de pasajeros.");
		map.put("44", "Billetes de lotería, rifas y apuestas.");
		map.put("45", "Documentos emitidos por centros educativos y culturales, universidades, asociaciones y fundaciones, en lo referente a actividades no gravadas con tributos administrados por la SUNAT.");
		map.put("46", "Formulario de Declaración - pago o Boleta de pago de tributos Internos");
		map.put("48", "Comprobante de Operaciones - Ley N° 29972");
		map.put("49", "Constancia de Depósito - IVAP (Ley 28211)");
		map.put("50", "Declaración Única de Aduanas - Importación definitiva");
		map.put("51", "Póliza o DUI Fraccionada");
		map.put("52", "Despacho Simplificado - Importación Simplificada");
		map.put("53", "Declaración de Mensajería o Courier");
		map.put("54", "Liquidación de Cobranza");
		map.put("55", "BVME para transporte ferroviario de pasajeros");
		map.put("56", "Comprobante de pago SEAE");
		map.put("87", "Nota de Crédito Especial");
		map.put("88", "Nota de Débito Especial");
		map.put("89", "Nota de Ajuste de Operaciones - Ley N° 29972");
		map.put("91", "Comprobante de No Domiciliado");
		map.put("96", "Exceso de crédito fiscal por retiro de bienes");
		map.put("97", "Nota de Crédito - No Domiciliado");
		map.put("98", "Nota de Débito - No Domiciliado");
		
		return map;
	}
	
	public static HashMap<String, String> getTabla11Sunat(){
		HashMap<String, String> map = new HashMap<String, String>();
		
		map.put("019", "TUMBES");
		map.put("028", "TALARA");
		map.put("046", "PAITA");
		map.put("055", "CHICLAYO");
		map.put("082", "SALAVERRY");
		map.put("91", "CHIMBOTE");
		map.put("118", "MARÍTIMA DEL CALLAO");
		map.put("127", "PISCO");
		map.put("145", "MOLLENDO MATARANI");
		map.put("154", "AREQUIPA");
		map.put("163", "ILO");
		map.put("172", "TACNA");
		map.put("181", "PUNO");
		map.put("190", "CUZCO");
		map.put("217", "PUCALLPA");
		map.put("226", "IQUITOS");
		map.put("235", "AÉREA DEL CALLAO");
		map.put("244", "POSTAL DE LIMA");
		map.put("262", "DESAGUADERO");
		map.put("271", "TARAPOTO");
		map.put("280", "PUERTO MALDONADO");
		map.put("299", "LA TINA");
		map.put("884", "DEPENDENCIA FERROVIARIA TACNA");
		map.put("893", "DEPENDENCIA POSTAL TACNA");
		map.put("910", "DEPENDENCIA POSTAL AREQUIPA");
		map.put("929", "COMPLEJO FRONTERIZO STA ROSA TACNA");
		map.put("938", "TERMINAL TERRESTRE TACNA");
		map.put("947", "AEROPUERTO TACNA");
		map.put("956", "CETICOS TACNA");
		map.put("965", "DEPENDENCIA POSTAL DE SALAVERRY");		
		
		return map;
	}
	
	
	public static HashMap<String, String> getTabla17Sunat(){
		HashMap<String, String> map = new HashMap<String, String>();
		
		map.put("01", "PLAN CONTABLE GENERAL EMPRESARIAL");
		map.put("02", "PLAN CONTABLE GENERAL REVISADO");
		map.put("03", "PLAN DE CUENTAS PARA EMPRESAS DEL SISTEMA FINANCIERO, SUPERVISADAS POR SBS");
		map.put("04", "PLAN DE CUENTAS PARA ENTIDADES PRESTADORAS DE SALUD, SUPERVISADAS POR SBS");
		map.put("05", "PLAN DE CUENTAS PARA EMPRESAS DEL SISTEMA ASEGURADOR, SUPERVISADAS POR SBS");
		map.put("06", "PLAN DE CUENTAS DE LAS ADMINISTRADORAS PRIVADAS DE FONDOS DE PENSIONES, SUPERVISADAS POR SBS");
		map.put("07", "PLAN CONTABLE GUBERNAMENTAL");
		map.put("99", "OTROS");
		
		return map;
	}
	
	public static HashMap<String, String> getTabla30Sunat(){
		HashMap<String, String> map = new HashMap<String, String>();
		
		map.put("1", "MERCADERIA, MATERIA PRIMA, SUMINISTRO, ENVASES Y EMBALAJES");
		map.put("2", "ACTIVO FIJO");
		map.put("3", "OTROS ACTIVOS NO CONSIDERADOS EN LOS NUMERALES 1 Y 2");
		map.put("4", "GASTOS DE EDUCACIÓN, RECREACIÓN, SALUD, CULTURALES. REPRESENTACIÓN, CAPACITACIÓN, DE VIAJE, MANTENIMIENTO DE VEHICULO Y  DE PREMIOS");
		map.put("5", "OTROS GASTOS NO INCLUIDOS EN EL NUMERAL 4");
		
		return map;
	}
	
	
	public static String getMessageValidateDocument(String Document, String TypeDocument){
		
		StringBuilder msgError = new StringBuilder("");
		
		if(TypeDocument.compareToIgnoreCase("0")==0){/*OTROS TIPOS DE DOCUMENTOS*/
			if(!Document.matches("")){
				msgError.append("Formato inválido. ");
			}
			if(Document.length()>15){
				msgError.append("Longitud incorrecta. ");
			}
			
		}else if(TypeDocument.compareToIgnoreCase("1")==0){/*DOCUMENTO NACIONAL DE IDENTIDAD (DNI)*/
			if(!Document.matches("")){
				msgError.append("Formato inválido. ");
			}
			if(Document.length()!=8){
				msgError.append("Longitud incorrecta. ");
			}
			
		}else if(TypeDocument.compareToIgnoreCase("4")==0){/*CARNET DE EXTRANJERIA*/
			
			if(!Document.matches("")){
				msgError.append("Formato inválido. ");
			}
			if(Document.length()>12){
				msgError.append("Longitud incorrecta. ");
			}
			
		}else if(TypeDocument.compareToIgnoreCase("6")==0){/*REGISTRO ÚNICO DE CONTRIBUYENTES*/
			
			if(!Document.matches("")){
				msgError.append("Formato inválido. ");
			}
			if(Document.length()!=11){
				msgError.append("Longitud incorrecta. ");
			}
			
		}else if(TypeDocument.compareToIgnoreCase("7")==0){/*PASAPORTE*/
			
			if(!Document.matches("")){
				msgError.append("Formato inválido. ");
			}
			if(Document.length()>12){
				msgError.append("Longitud incorrecta. ");
			}
			
		}else if(TypeDocument.compareToIgnoreCase("A")==0){/*CÉDULA DIPLOMÁTICA DE IDENTIDAD*/
			
			if(!Document.matches("")){
				msgError.append("Formato inválido. ");
			}
			if(Document.length()!=15){
				msgError.append("Longitud incorrecta. ");
			}
			
		}

		return msgError.toString();
	}
	
}
