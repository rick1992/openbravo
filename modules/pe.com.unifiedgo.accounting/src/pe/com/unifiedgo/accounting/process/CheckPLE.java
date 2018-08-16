package pe.com.unifiedgo.accounting.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openbravo.base.exception.OBException;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.dal.core.OBContext;
import org.openbravo.dal.service.OBDal;
import org.openbravo.database.ConnectionProvider;
import org.openbravo.erpCommon.businessUtility.TabAttachments;
import org.openbravo.erpCommon.utility.OBError;
import org.openbravo.model.ad.access.User;
import org.openbravo.model.common.enterprise.Organization;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.service.db.DalBaseProcess;

import pe.com.unifiedgo.accounting.data.SCOPle;
import pe.com.unifiedgo.accounting.data.SCOPleDetail;

public class CheckPLE extends DalBaseProcess {
  public void doExecute(ProcessBundle bundle) throws Exception {
    final ConnectionProvider conProvider = bundle.getConnection();
    Connection conn = conProvider.getTransactionConnection();

    try {

      // retrieve the parameters from the bundle
      final String sco_ple_detail_id = (String) bundle.getParams().get("SCO_Ple_Detail_ID");
      final String strRowNo = (String) bundle.getParams().get("rowno");
      final String strColumnNo = (String) bundle.getParams().get("columnno");

      System.out.println("strRowNo:" + strRowNo + " - strColumnNo:" + strColumnNo);

      final VariablesSecureApp vars = bundle.getContext().toVars();
      OBContext.setOBContext(vars.getUser(), vars.getRole(), vars.getClient(), vars.getOrg());
      final String language = bundle.getContext().getLanguage();
      User user = OBDal.getInstance().get(User.class, vars.getUser());

      SCOPleDetail pleDetail = OBDal.getInstance().get(SCOPleDetail.class, sco_ple_detail_id);

      if (pleDetail == null) {
        throw new OBException("@SCO_InternalError@");
      }
      
      SCOPle scople = pleDetail.getSCOPle();
      
      String nameple = scople.getName();
      
      String attachPath = OBPropertiesProvider.getInstance().getOpenbravoProperties().getProperty("attach.path");
      
      // Lookup for the filename in the attachments of this detail.
      String directory = TabAttachments.getAttachmentDirectoryForNewAttachments(
          "9B239A921B544D9B8CCE9AFDC32EE203", pleDetail.getId());
      
      File folder = new File(attachPath + "/" + directory);
      
      File pletxt = folder.listFiles()[0];
      
      ArrayList<String> lineas = new ArrayList<String>();
      
      FileReader fr = null;
      BufferedReader br = null;
      
      try{
    	  fr = new FileReader(pletxt);
    	  br = new BufferedReader(fr);
    	  
    	  String linea;
    	  
    	  while((linea=br.readLine())!=null){
    		  lineas.add(linea);
    	  }  
    	  
      }catch(Exception ex){
    	  
      }finally{
    	  
      }
      
      OBError Result = new OBError();
      
      if(lineas.size()==0){
    	  Result.setType("info");
          Result.setTitle("El archivo seleccionado no contiene datos");
          bundle.setResult(Result);
          
      }
      else
      {
    	  objValidate validador = new objValidate();
    	  String message = "";
          
          String currentline = lineas.get(Integer.parseInt(strRowNo)-1);
          
          if(nameple.equalsIgnoreCase("Libro Mayor")){
        	  
        	  validador = validateLibroMayor(currentline, Integer.parseInt(strColumnNo), "", "");
        	  
          }else if(nameple.equalsIgnoreCase("Libro Diario")){
        	  
        	  validador = validateLibroDiario(currentline, Integer.parseInt(strColumnNo), "", "");
        	  
          }else if(nameple.equalsIgnoreCase("Libro Diario Plan Contable")){
        	  
        	  validador = validateLibroDiarioPlanContable(currentline, Integer.parseInt(strColumnNo), "", "");
        	  
          }else if(nameple.equalsIgnoreCase("Registro de Compras")){
        	  
        	  validador = validateRegistroCompras(currentline, Integer.parseInt(strColumnNo), "", "");
        	  
          }else if(nameple.equalsIgnoreCase("Registro de Ventas")){
        	  
        	  validador = validateRegistroVentas(currentline, Integer.parseInt(strColumnNo), "", "");
          }
          
          OBError alert = new OBError();
          if(validador.hayerror){
        	  
        	  CheckPLEData.UpdateComents(conn, conProvider, message, sco_ple_detail_id);
        	  //TablasSunat.updateComments(conn, message, sco_ple_detail_id);//actualizar el comentario
        	  
        	  alert.setType("warning");
              alert.setTitle(validador.message + ". En Fila: " + strRowNo + " y columna: " + strColumnNo);
          }
          else{
        	  alert.setType("success");
              alert.setTitle(validador.message + ". La fila " + strRowNo + " y columna " + strColumnNo + " no presenta errores.");
          }
          bundle.setResult(alert);
          
      }
      
      
      OBDal.getInstance().commitAndClose();
      conProvider.releaseCommitConnection(conn);
      
      System.out.println("directory:" + directory);
      
      String adOrgId_LE = AccProcessData.getOrgLeBu(conProvider,
          pleDetail.getSCOPle().getOrganization().getId(), "LE");
      if (adOrgId_LE == null) {
        throw new OBException("@SCO_InternalError@");
      }

      Organization orgle = OBDal.getInstance().get(Organization.class, adOrgId_LE);

    } catch (final OBException e) {
      final OBError msg = new OBError();
      msg.setType("Error");
      msg.setMessage(e.getMessage());
      msg.setTitle("@Error@");
      OBDal.getInstance().rollbackAndClose();
      conProvider.releaseRollbackConnection(conn);

      bundle.setResult(msg);
    }
  }
  
  
  /* Functions */
  private objValidate validateLibroMayor(String linea, int column, String yearPeriod, String monthPeriod){
	  String[] fields = linea.split("\\|");
	  
	  StringBuilder msgError = new StringBuilder("");
	  
	  HashMap<String, String> tabla02Sunat;
	  HashMap<String, String> tabla04Sunat;
	  HashMap<String, String> tabla10Sunat;
	  
	  Pattern pt;
	  Matcher mt;
	  
	  msgError.append("Número de registro: " + (fields[1].equals("") || fields[1]==null?"":fields[1]) + ", Correlativo: " 
	  + (fields[2].equals("") || fields[2]==null ? "":fields[2]) + " Serie y Número: " 
			  + (fields[10].equals("") || fields[10]==null?"":fields[10]) + "-" 
	  + (fields[11].equals("") || fields[11]==null?"":fields[11]) + " Documento "
	  + TablasSunat.getTabla10Sunat().get((fields[9].equals("") || fields[9]==null?"":fields[9])) + ". ");
	  
	  Boolean hayerror = false;
	  
	  plecomprobante objvalidate = TablasSunat.getValuesFromPLE(TablasSunat.PLEOTROSLIBROS, fields[9]);
	  
	  switch(column){
	  case 1:
		  if(fields[0] == null || fields[0].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  if(fields[0].length()!=8){
				  msgError.append(TablasSunat.WRONGLENGTHMESSAGE);
				  hayerror = true;
			  }
			  
			  pt = Pattern.compile(TablasSunat.PERIODPATTERN);
			  mt = pt.matcher(fields[0]);
			  
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTPERIODFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 2:/* Código Único de Operación */
		  if(fields[1] == null || fields[1].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  if(fields[1].length() > 40){
				  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 3: /* Número Correlativo */
		  if(fields[2] == null || fields[2].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  pt = Pattern.compile(TablasSunat.CORRELATIVENUMBERPATTERN);
			  mt = pt.matcher(fields[2]);
			  
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 4:
		  if(fields[3] == null || fields[3].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  if(fields[3].length()>24){
				  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 5:
		  if(fields[4] != null && fields[4].compareToIgnoreCase("") != 0){
			  if(fields[4].length() > 24){
				  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 6:
		  if(fields[5] != null && !(fields[5].compareToIgnoreCase("") == 0)){
			  if(fields[5].length() > 24){
				  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 7:/* Tipo de Moneda de Origen */
		  tabla04Sunat = TablasSunat.getTabla04Sunat();
		  if(fields[6] == null || fields[6].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  if(!tabla04Sunat.containsKey(fields[6])){
				  msgError.append(TablasSunat.INVALIDVALUEMESSAGE);
				  hayerror = true;
			  }
			  
			  if(fields[6].length() != 3){
				  msgError.append(TablasSunat.WRONGLENGTHMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 8:/* Tipo de Documento del Emisor */
		  if(fields[7] != null && fields[7].compareToIgnoreCase("") != 0){
			  tabla02Sunat = TablasSunat.getTabla02Sunat();
			  if(!tabla02Sunat.containsKey(fields[7])){
				  msgError.append(TablasSunat.INVALIDVALUEMESSAGE);
				  hayerror = true;
			  }
			  
			  if(fields[7].length()!= 1){
				  msgError.append(TablasSunat.WRONGLENGTHMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 9:/* Número de Documento del Emisor */
		  if(fields[7] != null && fields[7].compareToIgnoreCase("") != 0){
			  if(fields[8] == null || fields[8].compareToIgnoreCase("") == 0){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
			  else
			  {
				  if(fields[8].length() > 15){
					  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
					  hayerror = true;
				  }
				  /* Aplicar validación General */
			  }
		  }
		  break;
		  
	  case 10: /* Tipo de Comprobante */
		  if(fields[9] ==null || fields[9].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  tabla10Sunat = TablasSunat.getTabla10Sunat();
			  if(!tabla10Sunat.containsKey(fields[9])){
				  msgError.append(TablasSunat.INVALIDVALUEMESSAGE);
				  hayerror = true;
			  }
			  
			  if(fields[9].length()!=2){
				  msgError.append(TablasSunat.WRONGLENGTHMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 11:/* Número de serie */
		  if(fields[10] != null && fields[10].compareToIgnoreCase("") != 0){
			  /*if(fields[10].length()>20){
				  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
				  hayerror = true;
			  }*/
			  
			  
			  if(fields[10].length()>objvalidate.LongSerie){
				  msgError.append("El número de serie del documento " + objvalidate.Documento + (objvalidate.LongFijSerie?" debe tener ": " debe tener máximo ") + objvalidate.LongSerie + " digitos.");
				  hayerror = true;
			  }
		  }
		  
		  if(objvalidate.ObliSerie && (fields[10]==null || fields[10].compareToIgnoreCase("")==0)){
			  msgError.append("El documento " + objvalidate.Documento + " requiere el número de serie. ");
			  hayerror = true;
		  }
		  
		  break;
		  
	  case 12: /* Número de comprobante */
		  if(fields[11] ==null || fields[11].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  /*if(fields[11].length()>20){
				  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
				  hayerror = true;
			  }*/
			  
			  if(fields[11].length()>objvalidate.LongNumero){
				  msgError.append("El número del documento " + objvalidate.Documento + (objvalidate.LongFijNumero?" debe tener ": " debe tener máximo ") + objvalidate.LongNumero + " digitos.");
				  hayerror = true;
			  }
		  }
		  
		  if(objvalidate.ObliNumero && (fields[11] == null || fields[11].compareToIgnoreCase("") == 0)){
			  msgError.append("El documento " + objvalidate.Documento + " requiere el número de comprobante. ");
			  hayerror = true;
		  }
		  
		  break;
		  
	  case 13:/* Fecha Contable */
		  if(fields[12] != null && fields[12].compareToIgnoreCase("") != 0){
			  pt = Pattern.compile(TablasSunat.DATEPATTERN);
			  mt = pt.matcher(fields[12]);
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTDATEFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 14:/* Fecha de Vencimiento */
		  if(fields[13] != null && fields[13].compareToIgnoreCase("") != 0){
			  pt = Pattern.compile(TablasSunat.DATEPATTERN);
			  mt = pt.matcher(fields[13]);
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTDATEFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 15:/* Fecha de Operación o Emisión */
		  if(fields[14] ==null || fields[14].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  pt = Pattern.compile(TablasSunat.DATEPATTERN);
			  mt = pt.matcher(fields[14]);
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTDATEFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 16:/* Glosa o Descripción */
		  if(fields[15] == null || fields[15].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  if(fields[15].length()>200){
				  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 17:/* Glosa Referencial */
		  if(fields[16] != null && fields[16].compareToIgnoreCase("") != 0){
			  if(fields[16].length()>200){
				  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 18:/* Movimientos del Debe */
		  if(fields[17] ==null || fields[17].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  pt = Pattern.compile(TablasSunat.NUMERICAMOUNTPATTERN);
			  mt = pt.matcher(fields[17]);
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 19:/* Moviemientos del Haber */
		  if(fields[18] ==null || fields[18].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  pt = Pattern.compile(TablasSunat.NUMERICAMOUNTPATTERN);
			  mt = pt.matcher(fields[18]);
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 20:
		  
		  break;
		  
	  case 21:
		  if(fields[20] == null || fields[20].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  if(fields[20].length() != 1)
			  {
				  msgError.append(TablasSunat.WRONGLENGTHMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
		  default:
			  if(column>=22 && column<=44){
				  if(fields[column].length()>200){
					  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
					  hayerror = true;
				  }
			  }
			break;
	  }
	  objValidate obj = new objValidate();
	  obj.message = msgError.toString();
	  obj.hayerror = hayerror;
	  
	  return obj;
  }
  
  private objValidate validateLibroDiario(String linea, int column, String yearPeriod, String monthPeriod){
	  String[] fields = linea.split("\\|");
	  
	  HashMap<String, String> tabla02Sunat;
	  HashMap<String, String> tabla04Sunat;
	  HashMap<String, String> tabla10Sunat;
	  
	  Pattern pt;
	  Matcher mt;
	  
	  StringBuilder msgError = new StringBuilder("");
	  
	  msgError.append("Número de registro: " + (fields[1].equals("") || fields[1] ==null?"":fields[1]) + ", Correlativo: " 
	  + (fields[2].equals("") || fields[2]==null ? "":fields[2]) + " " + ", Serie y Número: " 
			  + (fields[10].equals("") || fields[10]==null?"":fields[10]) + "-" 
	  + (fields[11].equals("") || fields[11]==null?"":fields[11]) + " Documento:  " 
	  + TablasSunat.getTabla10Sunat().get((fields[9].equals("") || fields[9]==null?"":fields[9])) + ". ");
	  
	  Boolean hayerror = false;
	  
	  plecomprobante objvalidate = TablasSunat.getValuesFromPLE(TablasSunat.PLEOTROSLIBROS, fields[9]);
	  
	  switch(column){
	  case 1: /* Periodo */
		  if(fields[0] == null || fields[0].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  pt = Pattern.compile(TablasSunat.PERIODPATTERN);
			  mt = pt.matcher(fields[0]);
			  
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTPERIODFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 2:/* Código Único de Operación */
		  if(fields[1] == null || fields[1].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  if(fields[1].length()>40){
				  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 3:/* Número Correlativo */
		  if(fields[2] == null || fields[2].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  pt = Pattern.compile(TablasSunat.CORRELATIVENUMBERPATTERN);
			  mt = pt.matcher(fields[2]);
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 4:/* Código de la cuenta contable */
		  if(fields[3] == null || fields[3].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  if(fields[3].length()>24){
				  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 5:/* Código de la unidad de Operación */
		  if(fields[4] != null && fields[4].compareToIgnoreCase("") != 0){
			  if(fields[4].length()>24){
				  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 6:/* Código del centro de costos */
		  if(fields[5] != null && fields[5].compareToIgnoreCase("") != 0){
			  if(fields[5].length()>24){
				  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 7:/* Tipo de moneda de origen */
		  if(fields[6] == null || fields[6].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  tabla04Sunat = TablasSunat.getTabla04Sunat();
			  if(!tabla04Sunat.containsKey(fields[6])){
				  msgError.append(TablasSunat.INVALIDVALUEMESSAGE);
				  hayerror = true;
			  }
			  if(fields[6].length() != 3){
				  msgError.append(TablasSunat.WRONGLENGTHMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 8:/* Tipo de documento de identidad del emisor */
		  if(fields[7] != null && fields[7].compareToIgnoreCase("") != 0){
			  tabla02Sunat = TablasSunat.getTabla02Sunat();
			  if(!tabla02Sunat.containsKey(fields[07])){
				  msgError.append(TablasSunat.INVALIDVALUEMESSAGE);
				  hayerror = true;
			  }
			  if(fields[7].length() != 1){
				  msgError.append(TablasSunat.WRONGLENGTHMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 9:/* numero de documento de identidad del emisor */
		  if(fields[7] != null && fields[7].compareToIgnoreCase("")!=0){
			  if(fields[8] == null || fields[8].compareToIgnoreCase("")==0){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
			  else
			  {
				  if(fields[8].length()!=8){
					  msgError.append(TablasSunat.WRONGLENGTHMESSAGE);
					  hayerror = true;
				  }
				  /* validación tipo de documento */
			  }
		  }
		  break;
		  
	  case 10: /* Tipo de Comprobante de Pago o Documento */
		  if(fields[9] == null || fields[9].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  tabla10Sunat = TablasSunat.getTabla10Sunat();
			  if(!tabla10Sunat.containsKey(fields[9])){
				  msgError.append(TablasSunat.INVALIDVALUEMESSAGE);
				  hayerror = true;
			  }
			  if(fields[9].length()!=2){
				  msgError.append(TablasSunat.WRONGLENGTHMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 11:/* Número de serie del comprobante */
		  if(fields[10]!=null && fields[10].compareToIgnoreCase("")!=0){
			  /*if(fields[10].length()>20){
				  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
				  hayerror = true;
			  }*/
			  
			  if(fields[10].length()>objvalidate.LongSerie){
				  msgError.append("El número de serie del documento " + objvalidate.Documento + (objvalidate.LongFijSerie?" debe tener ": " debe tener máximo ") + objvalidate.LongSerie + " digitos. ");
				  hayerror = true;
			  }
		  }
		  
		  if(objvalidate.ObliSerie && (fields[10]==null || fields[10].compareToIgnoreCase("")==0)){
			  msgError.append("El documento " + objvalidate.Documento + " requiere el número de serie. ");
			  hayerror = true;
		  }
		  
		  break;
		  
	  case 12:/* Número del comprobante */
		  if(fields[11] == null || fields[11].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  /*if(fields[11].length()>20){
				  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
				  hayerror = true;
			  }*/
			  
			  if(fields[11].length()>objvalidate.LongNumero){
				  msgError.append("El número del documento " + objvalidate.Documento + (objvalidate.LongFijNumero?" debe tener ": " debe tener máximo ") + objvalidate.LongNumero + " digitos. ");
				  hayerror = true;
			  }
			  
		  }
		  
		  if(objvalidate.ObliNumero && (fields[11] == null || fields[11].compareToIgnoreCase("") == 0)){
			  msgError.append("El documento " + objvalidate.Documento + " requiere el número de comprobante. ");
			  hayerror = true;
		  }
		  break;
		  
	  case 13:/* Fecha contable */
		  if(fields[12] != null && fields[12].compareToIgnoreCase("") != 0){
			  pt = Pattern.compile(TablasSunat.DATEPATTERN);
			  mt = pt.matcher(fields[12]);
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTDATEFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 14:/* Fecha de vencimiento */
		  if(fields[13] != null && fields[13].compareToIgnoreCase("") != 0){
			  pt = Pattern.compile(TablasSunat.DATEPATTERN);
			  mt = pt.matcher(fields[13]);
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTDATEFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 15:/* Fecha de Operación o Emisión */
		  if(fields[14]==null || fields[14].compareToIgnoreCase("")==0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  pt = Pattern.compile(TablasSunat.DATEPATTERN);
			  mt = pt.matcher(fields[14]);
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTDATEFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 16:/* Glosa */
		  if(fields[15]==null || fields[15].compareToIgnoreCase("")==0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  if(fields[15].length()>200){
				  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 17:/* Glosa Referencial */
		  if(fields[16]!= null && fields[16].compareToIgnoreCase("")!=0){
			  if(fields[16].length()>200){
				  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 18:/* Debe */
		  if(fields[17]==null || fields[17].compareToIgnoreCase("")==0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  pt = Pattern.compile(TablasSunat.NUMERICAMOUNTPATTERN);
			  mt = pt.matcher(fields[17]);
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 19:/* Haber */
		  if(fields[18]==null || fields[18].compareToIgnoreCase("")==0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  pt = Pattern.compile(TablasSunat.NUMERICAMOUNTPATTERN);
			  mt = pt.matcher(fields[18]);
			  
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 20:
		  /* consultar como hacer la validacion */
		  
		  break;
		  
	  case 21:/* Estado de la operación */
		  if(fields[21]==null || fields[21].compareToIgnoreCase("")==0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  String[] column21values = {"1", "8", "9"};
			  if(!Arrays.asList(column21values).contains(fields[20])){
				  msgError.append(TablasSunat.INVALIDVALUEMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  default:
			  if(column>=22 && column<=44){
				  if(fields[column].length()>200){
					  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
					  hayerror = true;
				  }
			  }
			  break;
	  }
	  
	  objValidate obj = new objValidate();
	  obj.message = msgError.toString();
	  obj.hayerror = hayerror;
	  
	  return obj;
  }
  
  private objValidate validateLibroDiarioPlanContable(String linea, int column, String yearPeriod, String monthPeriod){
	  String[] fields = linea.split("\\|");
	  
	  Pattern pt;
	  Matcher mt;
	  
	  HashMap<String, String> tabla17Sunat;
	  
	  StringBuilder msgError = new StringBuilder("");
	  
	  Boolean hayerror = false;
	  
	  switch(column){
	  
	  case 1:/*Periodo*/
		  if(fields[0] == null || fields[0].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  pt = Pattern.compile(TablasSunat.PERIODPATTERN);
			  mt = pt.matcher(fields[0]);
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTPERIODFORMATMESSAGE);
				  hayerror = true;
			  }
			  if(fields[0].length()!=8){
				  msgError.append(TablasSunat.WRONGLENGTHMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 2:/* Código de la cuenta contable */
		  if(fields[1] == null || fields[0].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  if(fields[1].length()>24){
				  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 3:/* Descripcion de la cuenta */
		  if(fields[2] == null || fields[2].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  if(fields[2].length()>100){
				  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 4:/* Código del plan de cuentas */
		  if(fields[3] == null || fields[3].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  tabla17Sunat = TablasSunat.getTabla17Sunat();
			  if(!tabla17Sunat.containsKey(fields[3])){
				  msgError.append(TablasSunat.INVALIDVALUEMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 5:/* Descripcion del plan de cuentas */
		  if(fields[3].compareToIgnoreCase("99") == 0){
			  if(fields[4] == null || fields[4].compareToIgnoreCase("") == 0){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 6:/* Código de la cuenta contable corporativa */
		  if(fields[5] != null && fields[5].compareToIgnoreCase("") != 0){
			  if(fields[5].length()>24){
				  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 7:/* Descripcion de la cuenta contable corporativa */
		  if(fields[5]!=null && fields[5].compareToIgnoreCase("")!=0){
			  if(fields[6] == null || fields[6].compareToIgnoreCase("") == 0){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
			  else
			  {
				  if(fields[6].length()>100){
					  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
					  hayerror = true;
				  }
			  }
		  }
		  break;
		  
	  case 8: /*Estado de la operación */
		  
		  String[] column8values = {"1", "8", "9"};
		  
		  if(fields[7] == null || fields[7].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  if(!Arrays.asList(column8values).contains(fields[7])){
				  msgError.append(TablasSunat.INVALIDVALUEMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
		  default:
			  if(column>=9 && column<=16){
				  if(fields[column].length()>200){
					  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
					  hayerror = true;
				  }
			  }
			  break;
	  }
	  
	  objValidate obj = new objValidate();
	  obj.message = msgError.toString();
	  obj.hayerror = hayerror;
	  
	  return obj;
  }

  private objValidate validateRegistroCompras(String linea, int column, String yearPeriod, String monthPeriod){
	  
	  HashMap<String, String> tabla02Sunat = TablasSunat.getTabla02Sunat();
	  HashMap<String, String> tabla04Sunat = TablasSunat.getTabla04Sunat();
	  HashMap<String, String> tabla10Sunat = TablasSunat.getTabla10Sunat();
	  HashMap<String, String> tabla11Sunat = TablasSunat.getTabla11Sunat();
	  HashMap<String, String> tabla30Sunat;
	  
	  String[] fields = linea.split("\\|");
	  StringBuilder msgError = new StringBuilder("");
	  
	  String periodo = fields[0].length() == 8 ? fields[0].substring(0, 3) : "";
	  
	  Double temp;
	  Double curr;
	  
	  Pattern pt;
	  Matcher mt;
	  
	  msgError.append("Serie y Número: " + (fields[6].equals("") || fields[6]==null?"":fields[6]) 
			  + "-" + (fields[8].equals("") || fields[8]==null?"":fields[8]) + ", Documento: " 
			  + TablasSunat.getTabla10Sunat().get((fields[5].equals("") || fields[5]==null?"":fields[5])) + ". ");
	  
	  Boolean hayerror = false;
	  
	  plecomprobante objvalidate = TablasSunat.getValuesFromPLE(TablasSunat.PLEOTROSLIBROS, fields[5]);
	  
	  switch(column){
	  case 1:/*Periodo*/
		  
		  if(fields[0] == null || fields[0] == ""){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  if(fields[0].length() != 8){
				  msgError.append(TablasSunat.WRONGLENGTHMESSAGE);
				  hayerror = true;
			  }
			  pt = Pattern.compile(TablasSunat.PERIODPATTERN);
			  mt = pt.matcher(fields[0]);
			  
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTPERIODFORMATMESSAGE);
				  hayerror = true;
			  }
		  }

		  break;
		  
	  case 2:/*Número Correlativo del mes o Código Único de la operación*/
		  
		  if(fields[1] == null || fields[1] == ""){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  if(fields[1].length() > 40){
				  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
	  case 3:/*Número correlativo del asiento*/
		  
		  if(fields[2] == null || fields[2] == ""){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  if(fields[2].length() < 2 || fields[2].length() > 10){
				  msgError.append(TablasSunat.WRONGLENGTHMESSAGE);
				  hayerror = true;
			  }
			  
			  pt = Pattern.compile(TablasSunat.CORRELATIVENUMBERPATTERN);
			  mt = pt.matcher(fields[2]);
			  
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
				  hayerror = true;
			  }
			  
			  if(fields[2].contains("&")){
				  msgError.append(TablasSunat.INVALIDCHARACTERMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
	  case 4:/*fecha emision*/
		  
		  if(fields[3] == null || fields[3] == ""){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  if(fields[3].length() != 10){
				  msgError.append(TablasSunat.WRONGLENGTHMESSAGE);
				  hayerror = true;
			  }
			  
			  pt = Pattern.compile(TablasSunat.DATEPATTERN);
			  mt = pt.matcher(fields[3]);
			  
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTDATEFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
	  case 5:/*fecha vencimiento*/
		  
		  if(fields[5] == "14"){
			  if(fields[4]==null || fields[4].compareToIgnoreCase("")==0){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
			  else
			  {
				  pt = Pattern.compile(TablasSunat.DATEPATTERN);
				  mt = pt.matcher(fields[4]);
				  
				  if(!mt.matches()){
					  msgError.append(TablasSunat.INCORRECTDATEFORMATMESSAGE);
					  hayerror = true;
				  }
			  }
		  }
		  
		  break;
		  
	  case 6:/*tipo de comprobante*/
		  
		  if(fields[5] == null || fields[5] == ""){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  if(!tabla10Sunat.containsKey(fields[5]) || fields[5].equalsIgnoreCase("91") || fields[5].equalsIgnoreCase("97") 
					  || fields[5].equalsIgnoreCase("98")){
				  msgError.append(TablasSunat.INVALIDVALUEMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
	  case 7:/* Serie del comprobante */
		  
		  if(fields[6] != null && !fields[6].equalsIgnoreCase("")){
			  /*if(fields[6].length() > 20){
				  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
				  hayerror = true;
			  }*/
			  
			  if(fields[6].length()>objvalidate.LongSerie){
				  msgError.append("El número de serie del documento " + objvalidate.Documento + (objvalidate.LongFijSerie?" debe tener ": " debe tener máximo ") + objvalidate.LongSerie + " digitos. ");
				  hayerror = true;
			  }
		  }
		  
		  if(objvalidate.ObliSerie && (fields[6]==null || fields[6].equalsIgnoreCase(""))){
			  msgError.append("El documento " + objvalidate.Documento + " requiere el número de serie. ");
			  hayerror = true;
		  }
		  
		  break;
		  
	  case 8:/*Año de emision de la dua*/
		  
		  break;
		  
	  case 9:
		  
		  if(fields[8] == null || fields[8].equalsIgnoreCase("")){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  /*if(fields[8].length()>20){
				  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
				  hayerror = true;
			  }*/
			  
			  if(fields[8].length()>objvalidate.LongNumero){
				  msgError.append("El número del documento " + objvalidate.Documento + (objvalidate.LongFijNumero?" debe tener ": " debe tener máximo ") + objvalidate.LongNumero + " digitos. ");
				  hayerror = true;
			  }
			  
		  }
		  
		  if(objvalidate.ObliNumero && (fields[8] == null || fields[8].compareToIgnoreCase("") == 0)){
			  msgError.append("El documento " + objvalidate.Documento + " requiere el número de comprobante. ");
			  hayerror = true;
		  }
		  
		  break;
		  
	  case 10:/*Numero del comprobante*/
		  
		  String[] column10campo6a = {"00", "03", "05", "06", "07", "08", "11", "12", "13", "14", "15", "16", "18", "19", 
				  "23", "26", "28", "30", "34", "35", "36", "37", "55", "56", "87", "88"};
		  if(fields[8]!=null && fields[8].compareToIgnoreCase("")!=0 && Arrays.asList(column10campo6a).contains(fields[5])){
			  if(fields[9]==null || fields[9].compareToIgnoreCase("")==0){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
			  else
			  {
				  if(fields[9].length()>20){
					  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
					  hayerror = true;
				  }
			  }
		  }
		  
		  break;
		  
	  case 11:/*tipo de documento del proveedor*/
		  
		  String[] column11campo6a = {"00", "03", "05", "06", "07", "08", "11", "12", "13", "14", "15", "16", "18", "19", 
				  "22", "23", "26", "28", "30", "34", "35", "36", "37", "55", "56", "87", "88", "91", "97", "98"};
		  String[] column11campo6b = {"07", "08", "87", "88", "97", "98"};
		  String[] column11campo27b = {"03", "12", "13", "14", "36"};
		  
		  if(!Arrays.asList(column11campo6a).contains(fields[5]) 
				  && !(Arrays.asList(column11campo6b).contains(fields[5]) && Arrays.asList(column11campo27b).contains(fields[26]))){
			  if(fields[10] == null || fields[10].compareToIgnoreCase("") == 0){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
			  else
			  {
				  if(!tabla02Sunat.containsKey(fields[10])){
					  msgError.append(TablasSunat.INVALIDVALUEMESSAGE);
					  hayerror = true;
				  }
			  }
		  }
		  
		  break;
		  
	  case 12:/*Número de RUC del proveedor o del Documento de Identidad*/
		  
		  String[] column12campo6a = {"00", "03", "05", "06", "07", "08", "11", "12", "13", "14", "15", "16", "18", "19", 
				  "22", "23", "26", "28", "30", "34", "35", "36", "37", "55", "56", "87", "88", "91", "97", "98"};
		  String[] column12campo6b = {"07", "08", "87", "88", "97", "98"};
		  String[] column12campo27b = {"03", "12", "13", "14", "36"};
		  
		  if(!Arrays.asList(column12campo6a).contains(fields[5]) 
				  && !(Arrays.asList(column12campo6b).contains(fields[5]) && Arrays.asList(column12campo27b).contains(fields[26]))){
			  if(fields[11] == null || fields[11].equalsIgnoreCase("")){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
			  else
			  {
				  if(fields[11].length() > 15){
					  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
					  hayerror = true;
				  }
			  }
		  }
		  
		  break;
		  
	  case 13:/*Apellidos y nombres, denominación o razón social del proveedor*/
		  
		  String[] column13campo6a = {"00", "03", "05", "06", "07", "08", "11", "12", "13", "14", "15", "16", "18", "19", 
				  "22", "23", "26", "28", "30", "34", "35", "36", "37", "55", "56", "87", "88", "91", "97", "98"};
		  String[] column13campo6b = {"07", "08", "87", "88", "97", "98"};
		  String[] column13campo27b = {"03", "12", "13", "14", "36"};
		  
		  if(!Arrays.asList(column13campo6a).contains(fields[6]) 
				  && !(Arrays.asList(column13campo6b).contains(fields[5]) && Arrays.asList(column13campo27b).contains(fields[26]))){
			  if(fields[12] == null || fields[12].equalsIgnoreCase("")){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
			  else
			  {
				  if(fields[12].length() > 100){
					  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
					  hayerror = true;
				  }
			  }
		  }
		  
		  break;
		  
	  case 14:/* Base Imponible */
		  
		  if(fields[13] != null && fields[13].equalsIgnoreCase("")){
			  pt = Pattern.compile(TablasSunat.NEGATIVENUMERICAMOUNTPATTERN);
			  mt = pt.matcher(fields[13]);
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
	  case 15:/* Monto del Impuesto */
		  
		  if(fields[13]!=null && fields[13].compareToIgnoreCase("")!=0){
			  if(fields[14]==null || fields[14].equalsIgnoreCase("")){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
			  else
			  {
				  temp = Double.parseDouble(fields[13]);
				  curr = Double.parseDouble(fields[14]);
				  
				  if(temp>0 && curr<0){
					  msgError.append(TablasSunat.VALUESHOULDBEPOSITIVEMESSAGE);
					  hayerror = true;
				  }
				  
				  if(temp<0 && curr>0){
					  msgError.append(TablasSunat.VALUESHOULDBENEGATIVEMESSAGE);
					  hayerror = true;
				  }
			  }
		  }
		  
		  break;
		  
	  case 16:/* Base imponible */
		  
		  if(fields[15]!= null && !(fields[15].equalsIgnoreCase(""))){
			  pt = Pattern.compile(TablasSunat.NEGATIVENUMERICAMOUNTPATTERN);
			  mt = pt.matcher(fields[15]);
			  
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
	  case 17:
		  if(fields[15]!= null && fields[15]!=""){
			  
			  if(fields[16]==null || fields[16].equalsIgnoreCase("")){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
			  else
			  {
				  temp = Double.parseDouble(fields[15]);
				  curr = Double.parseDouble(fields[16]);
				  
				  if(temp>0 && curr<0){
					  msgError.append(TablasSunat.VALUESHOULDBEPOSITIVEMESSAGE);
					  hayerror = true;
				  }
				  
				  if(temp<0 && curr>0){
					  msgError.append(TablasSunat.VALUESHOULDBENEGATIVEMESSAGE);
					  hayerror = true;
				  }
			  }
		  }
		  
		  break;
		  
	  case 18:
		  
		  if(fields[17] != null && !(fields[17].equalsIgnoreCase(""))){
			  pt = Pattern.compile(TablasSunat.NEGATIVENUMERICAMOUNTPATTERN);
			  mt = pt.matcher(fields[17]);
			  
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
	  case 19:/*Monto del IGV*/
		  if(fields[17]!= null && fields[17]!=""){
			  if(fields[18]==null || fields[18].equalsIgnoreCase("")){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
			  else
			  {
				  temp = Double.parseDouble(fields[17]);
				  curr = Double.parseDouble(fields[18]);
				  
				  if(temp>0 && curr<0){
					  msgError.append(TablasSunat.VALUESHOULDBEPOSITIVEMESSAGE);
					  hayerror = true;
				  }
				  
				  if(temp<0 && curr>0){
					  msgError.append(TablasSunat.VALUESHOULDBENEGATIVEMESSAGE);
					  hayerror = true;
				  }
			  }
		  }
		  
		  break;
		  
	  case 20:/* Valor de las adquisiones no gravadas */
		  
		  if(fields[19] != null && !(fields[19].equalsIgnoreCase(""))){
			  pt = Pattern.compile(TablasSunat.NEGATIVENUMERICAMOUNTPATTERN);
			  mt = pt.matcher(fields[19]);
			  
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
	  case 21:/* Monto del inpuesto selectivo al consumo */
		  
		  if(fields[20] != null && !(fields[20].equalsIgnoreCase(""))){
			  pt = Pattern.compile(TablasSunat.NEGATIVENUMERICAMOUNTPATTERN);
			  mt = pt.matcher(fields[20]);
			  
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
	  case 22:/*Otros conceptos*/
		  
		  if(fields[21] != null && !(fields[21].equalsIgnoreCase(""))){
			  pt = Pattern.compile(TablasSunat.NEGATIVENUMERICAMOUNTPATTERN);
			  mt = pt.matcher(fields[21]);
			  
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
	  case 23:/* Importe total de las adquisiones */
		  
		  if(fields[22]==null || fields[22].equalsIgnoreCase("")){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  pt = Pattern.compile(TablasSunat.NEGATIVENUMERICAMOUNTPATTERN);
			  mt = pt.matcher(fields[22]);
			  
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
	  case 24:/* Código de la moneda */
		  
		  if(fields[23] != null && fields[23].compareToIgnoreCase("") != 0){
			  
			  if(!tabla04Sunat.containsKey(fields[23])){
				  msgError.append(TablasSunat.INVALIDVALUEMESSAGE);
				  hayerror = true;
			  }
			  if(fields[23].length() != 3){
				  msgError.append(TablasSunat.WRONGLENGTHMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
	  case 25:/* Tipo de Cambio */
		  
		  if(fields[24]!=null && fields[24].compareToIgnoreCase("")!=0){
			  pt = Pattern.compile(TablasSunat.CONVERSIONRATEPATTERN);
			  mt = pt.matcher(fields[24]);
			  
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
	  case 26:/* Fecha de Emisión */
		  
		  String[] column26campo6 = {"07", "08", "87", "88", "97", "98"};
		  
		  if(Arrays.asList(column26campo6).contains(fields[5])){
			  if(fields[25] == null || fields[25].equalsIgnoreCase("")){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
			  else
			  {
				  if(fields[25].length() != 10){
					  msgError.append(TablasSunat.WRONGLENGTHMESSAGE);
					  hayerror = true;
				  }
				  
				  pt = Pattern.compile(TablasSunat.DATEPATTERN);
				  mt = pt.matcher(fields[25]);
				  
				  if(!mt.matches()){
					  msgError.append(TablasSunat.INCORRECTDATEFORMATMESSAGE);
					  hayerror = true;
				  }
			  }
		  }
		  
		  break;
		  
	  case 27:/* Tipo de Comprobante */
		  
		  String[] column27campo6 = {"07", "08", "87", "88", "97", "98"};
		  
		  if(Arrays.asList(column27campo6).contains(fields[5])){
			  if(fields[26] == null || fields[26].compareToIgnoreCase("") == 0){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
			  else
			  {
				  if(!tabla10Sunat.containsKey(fields[26])){
					  msgError.append(TablasSunat.INVALIDVALUEMESSAGE);
					  hayerror = true;
				  }
				  
				  if(fields[26].length() != 2){
					  msgError.append(TablasSunat.WRONGLENGTHMESSAGE);
					  hayerror = true;
				  }
			  }
		  }
		  
		  break;
		  
	  case 28:/* Número de serie */
		  
		  String[] column28campo6 = {"07", "08", "87", "88", "97", "98"};
		  
		  if(Arrays.asList(column28campo6).contains(fields[5])){
			  if(fields[27] == null || fields[27].equalsIgnoreCase("")){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
			  else
			  {
				  /*if(fields[27].length()>20){
					  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
					  hayerror = true;
				  }*/
				  plecomprobante objtemp = TablasSunat.getValuesFromPLE(TablasSunat.PLEREGISTROCOMPRAS, fields[26]);
				  
				  if(fields[27].length()>objtemp.LongNumero){
					  msgError.append("El número del documento " + objtemp.Documento + (objtemp.LongFijNumero?" debe tener ": " debe tener máximo ") + objtemp.LongNumero + " digitos. ");
					  hayerror = true;
				  }
			  }
		  }
		  
		  break;
		  
	  case 29:/* Código de dependencia aduanera */
		  
		  String[] column29campo27 = {"50", "52"};
		  
		  if(Arrays.asList(column29campo27).contains(fields[26])){
			  if(fields[28] == null || fields[28].equalsIgnoreCase("")){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
			  else
			  {
				  if(!tabla11Sunat.containsKey(fields[28])){
					  msgError.append(TablasSunat.INVALIDVALUEMESSAGE);
					  hayerror = true;
				  }
				  if(fields[29].length() != 3){
					  msgError.append(TablasSunat.WRONGLENGTHMESSAGE);
					  hayerror = true;
				  }
			  }
		  }
		  
		  break;
		  
	  case 30:/* Número del comprobante que se modifica */
		  String[] column30campo6a = {"07", "08", "87", "88", "97", "98"};
		  
		  if(Arrays.asList(column30campo6a).contains(fields[5])){
			  if(fields[29] == null || fields[29].equalsIgnoreCase("")){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
			  else
			  {
				  if(fields[29].length() > 20){
					  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
					  hayerror = true;
				  }
			  }
		  }
		  
		  break;
		  
	  case 31:/* Fecha de emision de la constancia */
		  
		  if(fields[30]!=null && fields[30].equalsIgnoreCase("")){
			  pt = Pattern.compile(TablasSunat.DATEPATTERN);
			  mt = pt.matcher(fields[29]);
			  
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTDATEFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
	  case 32:/* Número de la constancia de depósito */
		  
		  if(fields[31]!=null && fields[31].equalsIgnoreCase("")){
			  if(fields[31].length()>24){
				  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
	  case 33:/* Marca del comprobante */
		  
		  if(fields[32] != null && !(fields[32].equalsIgnoreCase(""))){
			  if(!(fields[32].compareToIgnoreCase("1") == 0)){
				  msgError.append(TablasSunat.INVALIDVALUEMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
	  case 34:
		  if(fields[33]!=null && fields[33].compareToIgnoreCase("")!=0){
			  tabla30Sunat = TablasSunat.getTabla30Sunat();
			  
			  if(!tabla30Sunat.containsKey(fields[33])){
				  msgError.append(TablasSunat.INVALIDVALUEMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
	  case 35:/* Identificación del contrato */
		  
		  if(fields[34] != null && fields[34].compareTo("") != 0){
			  if(fields[34].length() != 12){
				  msgError.append(TablasSunat.WRONGLENGTHMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
	  case 36:/* Error tipo 1 */
		  
		  break;
		  
	  case 37:/* Error tipo 2 */
		  
		  break;
		  
	  case 38:/* Error tipo 3 */
		  
		  break;
		  
	  case 39:/* Error tipo 4 */
		  
		  break;
		  
	  case 40:/* Indicador de comprobantes */
		  
		  break;
		  
	  case 41:
		  String[] field41values = {"0", "1", "6", "7", "9"};
		  
		  if(fields[40] == null || fields[40].equalsIgnoreCase("")){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  if(fields[40].length() != 1){
				  msgError.append("Longitud incorrecta. ");
				  hayerror = true;
			  }
			  
			  if(!Arrays.asList(field41values).contains(fields[40])){
				  msgError.append(TablasSunat.INVALIDVALUEMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  default:
			  if(column>=42 && column<=82){
				  if(fields[column].length()>200){
					  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
					  hayerror = true;
				  }
			  }
			  break;
		  
	  }
	  
	  
	  objValidate obj = new objValidate();
	  obj.message = msgError.toString();
	  obj.hayerror = hayerror;
	  
	  return obj;
  }

  private objValidate validateRegistroVentas(String linea, int column, String yearPeriod, String monthPeriod){
	  
	  HashMap<String, String> tabla02Sunat = TablasSunat.getTabla02Sunat();
	  HashMap<String, String> tabla04Sunat = TablasSunat.getTabla04Sunat();
	  HashMap<String, String> tabla10Sunat = TablasSunat.getTabla10Sunat();
	  
	  String[] fields = linea.split("\\|");
	  
	  StringBuilder msgError = new StringBuilder();
	  
	  //obtener campo de periodo
	  String period = fields[0];
	  //String yearPeriod = period.substring(0,4);
	  //String monthPeriod = period.substring(4,6);
	  
	  String NumericPtrn = "^\\d{1,12}(\\.\\d{1,2}){0,1}$";
	  String NumericNegPtrn = "^-{0,1}\\d{1,12}(\\.\\d{1,2}){0,1}$";
	  
	  String TipoCambioPtrn = "^\\d{1}\\.\\d{3}$";
	  
	  Pattern pt;
	  Matcher mt;
	  
	  msgError.append("Serie y Número: " + (fields[6].equals("") || fields[6]==null?"":fields[6]) + "-" 
	  + (fields[7].equals("") || fields[7]==null?"":fields[7]) + ", Documento: " 
			  + TablasSunat.getTabla10Sunat().get((fields[5].equals("") || fields[5]==null?"":fields[5])));
	  
	  Boolean hayerror = false;
	  
	  plecomprobante objvalidate = TablasSunat.getValuesFromPLE(TablasSunat.PLEREGISTROVENTAS, fields[5]);
	   
	  switch(column){
	  case 1:/* Periodo */
		  
		  if(fields[0] == null || fields[0].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  if(fields[0].length() != 8){
				  msgError.append(TablasSunat.WRONGLENGTHMESSAGE);
				  hayerror = true;
			  }
			  
			  pt = Pattern.compile(TablasSunat.PERIODPATTERN);
			  mt = pt.matcher(fields[0]);
			  
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTPERIODFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
	  case 2:/* CUO */
		  
		  if(fields[1] == null || fields[1].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  if(fields[1].length() > 40){
				  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
				  hayerror = true;
			  }
			  
			  if(fields[1].contains("&")){
				  msgError.append(TablasSunat.INVALIDCHARACTERMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
	  case 3:/*Número correlativo*/
		  
		  if(fields[2] == null || fields[2].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  if(fields[2].length()<2 || fields[2].length()>10){
				  msgError.append(TablasSunat.WRONGLENGTHMESSAGE);
				  hayerror = true;
			  }
			  
			  if(fields[2].contains("&")){
				  msgError.append(TablasSunat.INVALIDCHARACTERMESSAGE);
				  hayerror = true;
			  }
			  
			  pt = Pattern.compile(TablasSunat.CORRELATIVENUMBERPATTERN);
			  mt = pt.matcher(fields[2]);
			  
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
	  case 4:/*Fecha de emision*/
		  
		  if(fields[33] != "2"){
			  if(fields[3] == null || fields[3].compareToIgnoreCase("") == 0){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
			  else
			  {
				  pt = Pattern.compile(TablasSunat.DATEPATTERN);
				  mt = pt.matcher(fields[3]);
				  
				  if(!mt.matches()){
					  msgError.append(TablasSunat.INCORRECTDATEFORMATMESSAGE);
					  hayerror = true;
				  }
			  }
		  }
		  
		  break;
		  
	  case 5:/*Fecha de vencimiento*/
		  
		  if(fields[5].compareToIgnoreCase("14") == 0 && !(fields[33].compareToIgnoreCase("2") == 0)){
			  if(fields[4] == null || fields[4].compareToIgnoreCase("") == 0){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
			  else
			  {
				  if(fields[4].length()!=10){
					  msgError.append(TablasSunat.WRONGLENGTHMESSAGE);
					  hayerror = true;
				  }
				  
				  pt = Pattern.compile(TablasSunat.DATEPATTERN);
				  mt = pt.matcher(fields[4]);
				  
				  if(!mt.matches()){
					  msgError.append(TablasSunat.INCORRECTDATEFORMATMESSAGE);
					  hayerror = true;
				  }
			  }
		  }
		  
		  break;
		  
	  case 6:/*Tipo de comprobante*/
		  
		  if(fields[5] == null || fields[5].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  if(!tabla10Sunat.containsKey(fields[5])){
				  msgError.append(TablasSunat.INVALIDVALUEMESSAGE);
				  hayerror = true;
			  }
			  
			  if(fields[5].length() != 2){
				  msgError.append(TablasSunat.WRONGLENGTHMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
	  case 7:/*Numero de serie*/
		  
		  if(fields[6] == null || fields[6].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  /*if(fields[6].length() > 20){
				  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
				  hayerror = true;
			  }*/
			  
			  if(fields[6].length()>objvalidate.LongSerie){
				  msgError.append("El número de serie del documento " + objvalidate.Documento + (objvalidate.LongFijSerie?" debe tener ": " debe tener máximo ") + objvalidate.LongSerie + " digitos. ");
				  hayerror = true;
			  }
		  }
		  
		  if(objvalidate.ObliSerie && (fields[6]==null || fields[6].compareToIgnoreCase("")==0)){
			  msgError.append("El documento " + objvalidate.Documento + " requiere el número de serie. ");
			  hayerror = true;
		  }
		  
		  break;
		  
	  case 8:/* Número de Documento */
		  
		  if(fields[7] == null || fields[7].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  else
		  {
			  /*if(fields[7].length()>20){
				  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
				  hayerror = true;
			  }*/
			  
			  if(fields[7].length()>objvalidate.LongNumero){
				  msgError.append("El número del documento " + objvalidate.Documento + (objvalidate.LongFijNumero?" debe tener ": " debe tener máximo ") + objvalidate.LongNumero + " digitos. ");
				  hayerror = true;
			  }
		  }
		  
		  if(objvalidate.ObliNumero && (fields[7] == null || fields[7].compareToIgnoreCase("") == 0)){
			  msgError.append("El documento " + objvalidate.Documento + " requiere el número de comprobante. ");
			  hayerror = true;
		  }
		  
		  break;
		  
	  case 9:
		  String[] column9campo6a = {"00", "03", "12", "13", "87"};
		  
		  if(Arrays.asList(column9campo6a).contains(fields[5])){
			  if(fields[8] == null || fields[8].compareToIgnoreCase("") == 0){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
			  else
			  {
				  if(fields[8].length() > 20){
					  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
					  hayerror = true;
				  }
			  }
		  }
		  
		  break;
		  
	  case 10:/* Tipo de documento de identidad */
		  
		  String[] column10campo6a = {"00", "05", "06", "07", "08", "11", "12", "13", "14", "15", "16", "18", "19", "23", 
				  "26", "28", "30", "34", "35", "36", "37", "55", "56", "87", "88"};
		  String[] column10campo34b = {"2"};
		  String[] column10campo6c = {"07", "08", "87", "88"};
		  String[] column10campo28c = {"03", "12", "13", "14", "36"};
		  String[] column10campo6e = {"03", "12"};
		  
		  if(!Arrays.asList(column10campo6a).contains(fields[5]) 
				  && !Arrays.asList(column10campo34b).contains(fields[33]) 
				  && !(Arrays.asList(column10campo6c).contains(fields[5]) && Arrays.asList(column10campo28c).contains(fields[27])) 
				  && !(Double.parseDouble(fields[12]) > 13.00)
				  && !(Double.parseDouble(fields[23]) < 700.00 && Arrays.asList(column10campo6e).contains(fields[5]))
				  && !(fields[8] != null && fields[8] != "")){
			  
			  if(fields[9] == null || fields[9].compareToIgnoreCase("") == 0){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
			  else
			  {
				  if(!tabla02Sunat.containsKey(fields[9]) || fields[9].compareToIgnoreCase("0") == 0){
					  msgError.append(TablasSunat.INVALIDVALUEMESSAGE);
					  hayerror = true;
				  }
			  }
		  }
		  
		  break;
		  
	  case 11:/* Número de documento de identidad */
		  
		  String[] column11campo6a = {"00", "05", "06", "07", "08", "11", "12", "13", "14", "15", "16", "18", "19", "23", "26", 
				  "28", "30", "34", "35", "36", "37", "55", "56", "87", "88"};
		  String[] column11campo34b = {"2"};
		  String[] column11campo6c = {"07", "08", "87", "88"};
		  String[] column11campo28c = {"03", "12", "13", "14", "36"};
		  String[] column11campo6e = {"03", "12"};
		  
		  if(!Arrays.asList(column11campo6a).contains(fields[5]) 
				  && !Arrays.asList(column11campo34b).contains(fields[33]) 
				  && !(Arrays.asList(column11campo6c).contains(fields[5]) && Arrays.asList(column11campo28c).contains(fields[27])) 
				  && !(Double.parseDouble(fields[12]) > 13.00)
				  && !(Double.parseDouble(fields[23]) < 700.00 && Arrays.asList(column11campo6e).contains(fields[5]))
				  && !(fields[8] != null && fields[8] != "")){
			  
			  if(fields[10] == null || fields[10].compareToIgnoreCase("") == 0){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
	  case 12: /* Apellidos y nombres */
		  
		  String[] column12campo6a = {"00", "05", "06", "07", "08", "11", "12", "13", "14", "15", "16", "18", "19", "23", "26", "28", 
				  "30", "34", "35", "36", "37", "55", "56", "87", "88"};
		  String[] column12campo34b = {"2"};
		  String[] column12campo6c = {"07", "08", "87", "88"};
		  String[] column12campo28c = {"03", "12", "13", "14", "36"};
		  String[] column12campo6e = {"03", "12"};
		  
		  if(!Arrays.asList(column12campo6a).contains(fields[5]) 
				  && !Arrays.asList(column12campo34b).contains(fields[33]) 
				  && !(Arrays.asList(column12campo6c).contains(fields[5]) && Arrays.asList(column12campo28c).contains(fields[27])) 
				  && !(Double.parseDouble(fields[12]) > 13.00)
				  && !(Double.parseDouble(fields[23]) < 700.00 && Arrays.asList(column12campo6e).contains(fields[5]))
				  && !(fields[8] != null && !(fields[8].compareToIgnoreCase("") == 0)))
			  
			  if(fields[11] == null || fields[11].compareToIgnoreCase("") == 0){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
		  
		  break;
		  
	  case 13:/*valor facturado de la exportación*/
		  pt = Pattern.compile(TablasSunat.NUMERICAMOUNTPATTERN);
		  mt = pt.matcher(fields[12]);
		  if(!mt.matches()){
			  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
			  hayerror = true;
		  }
		  
		  break;
		  
	  case 14:
		  
		  pt = Pattern.compile(TablasSunat.NUMERICAMOUNTPATTERN);
		  mt = pt.matcher(fields[13]);
		  if(!mt.matches()){
			  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
			  hayerror = true;
		  }
		  
		  break;
		  
	  case 15:
		  pt = Pattern.compile(TablasSunat.NEGATIVENUMERICAMOUNTPATTERN);
		  mt = pt.matcher(fields[14]);
		  if(!mt.matches()){
			  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
			  hayerror = true;
		  }
		  
		  break;
		  
	  case 16:
		  
		  pt = Pattern.compile(TablasSunat.NEGATIVENUMERICAMOUNTPATTERN);
		  mt = pt.matcher(fields[15]);
		  if(!mt.matches()){
			  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
			  hayerror = true;
		  }
		  
		  break;
		  
	  case 17:
		  
		  pt = Pattern.compile(TablasSunat.NEGATIVENUMERICAMOUNTPATTERN);
		  mt = pt.matcher(fields[16]);
		  if(!mt.matches()){
			  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
			  hayerror = true;
		  }
		  
		  break;
		  
	  case 18:
		  
		  pt = Pattern.compile(TablasSunat.NEGATIVENUMERICAMOUNTPATTERN);
		  mt = pt.matcher(fields[17]);
		  if(!mt.matches()){
			  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
			  hayerror = true;
		  }
		  
		  break;
		  
	  case 19:
		  
		  pt = Pattern.compile(TablasSunat.NEGATIVENUMERICAMOUNTPATTERN);
		  mt = pt.matcher(fields[18]);
		  if(!mt.matches()){
			  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
			  hayerror = true;
		  }
		  
		  break;
		  
	  case 20:
		  
		  pt = Pattern.compile(TablasSunat.NEGATIVENUMERICAMOUNTPATTERN);
		  mt = pt.matcher(fields[19]);
		  if(!mt.matches()){
			  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
			  hayerror = true;
		  }
		  
		  break;
		  
	  case 21:
		  
		  if(fields[5].compareToIgnoreCase("49") == 0 && !(fields[33].compareToIgnoreCase("2") == 0) ){
			  if(fields[20] == null || fields[20].compareToIgnoreCase("") == 0){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
			  else
			  {
				  pt = Pattern.compile(TablasSunat.NEGATIVENUMERICAMOUNTPATTERN);
				  mt = pt.matcher(fields[12]);
				  
				  if(!mt.matches()){
					  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
					  hayerror = true;
				  }
			  }
		  }
		  
		  break;
		  
	  case 22:
		  
		  if(fields[5].compareToIgnoreCase("49") == 0 && !(fields[33].compareToIgnoreCase("2") == 0) ){
			  if(fields[21] == null || fields[21].compareToIgnoreCase("") == 0){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
			  else
			  {
				  pt = Pattern.compile(TablasSunat.NEGATIVENUMERICAMOUNTPATTERN);
				  mt = pt.matcher(fields[21]);
				  if(!mt.matches()){
					  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
					  hayerror = true;
				  }
			  }
		  }
		  
		  break;
		  
	  case 23:
		  
		  if(fields[22] != null && !(fields[22].compareToIgnoreCase("") == 0)){
			  pt = Pattern.compile(TablasSunat.NUMERICAMOUNTPATTERN);
			  mt = pt.matcher(fields[22]);
			  
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 24:
		  
		  if(fields[23] != null && !(fields[23].compareToIgnoreCase("") == 0)){
			  pt = Pattern.compile(TablasSunat.NEGATIVENUMERICAMOUNTPATTERN);
			  mt = pt.matcher(fields[23]);
			  
			  if(!mt.matches()){
				  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
	  case 25:
		  
		  if(fields[24] != null && !(fields[24].compareToIgnoreCase("") == 0)){
			  if(!tabla04Sunat.containsKey(fields[23])){
				  msgError.append(TablasSunat.INVALIDVALUEMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
	  case 26:
		  
		  if(fields[24] != null && !(fields[24].compareToIgnoreCase("") == 0)){
			  if(fields[25] == null || fields[25].compareToIgnoreCase("") == 0){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
			  else
			  {
				  pt = Pattern.compile(TablasSunat.CONVERSIONRATEPATTERN);
				  mt = pt.matcher(fields[25]);
				  
				  if(!mt.matches()){
					  msgError.append(TablasSunat.INCORRECTFORMATMESSAGE);
					  hayerror = true;
				  }
			  }
		  }
		  
		  break;
		  
	  case 27:
		  
		  String[] column27campo6a = {"07", "08", "87", "88"};
		  
		  if(Arrays.asList(column27campo6a).contains(fields[5]) && !(fields[33].compareToIgnoreCase("2") == 0)){
			  if(fields[26] == null || fields[26].compareToIgnoreCase("") == 0){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
		  }
		  
		  break;
		  
	  case 28:
		  
		  String[] column28campo6a = {"07", "08", "87", "88"};
		  
		  if(Arrays.asList(column28campo6a).contains(fields[5]) && !(fields[33].compareToIgnoreCase("2") == 0)){
			  if(fields[27] == null || fields[27].compareToIgnoreCase("") == 0){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
			  else
			  {
				  if(!tabla10Sunat.containsKey(fields[27])){
					  msgError.append(TablasSunat.INVALIDVALUEMESSAGE);
					  hayerror = true;
				  }
			  }
		  }
		  
		  break;
		  
	  case 29:
		  
		  String[] column29campo6a = {"07", "08", "87", "88"};
		  
		  if(Arrays.asList(column29campo6a).contains(fields[5]) && !(fields[33].compareToIgnoreCase("2") == 0)){
			  if(fields[28] == null || fields[28].compareToIgnoreCase("") == 0){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
			  else
			  {
				  if(fields[28].length() > 20){
					  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
					  hayerror = true;
				  }
			  }
		  }
		  
		  break;
		  
	  case 30:
		  
		  String[] column30campo6a = {"07", "08", "87", "88"};
		  
		  if(Arrays.asList(column30campo6a).contains(fields[5]) && !(fields[33].compareToIgnoreCase("2") == 0)){
			  if(fields[29] == null || fields[29].compareToIgnoreCase("") == 0){
				  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
				  hayerror = true;
			  }
			  else
			  {
				  if(fields[29].length() > 20){
					  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
					  hayerror = true;
				  }
			  }
		  }
		  break;
		  
	  case 31:
		  
		  if(fields[30] != null && !(fields[30].compareToIgnoreCase("") == 0)){
			  if(fields[30].length() != 12){
				  msgError.append(TablasSunat.WRONGLENGTHMESSAGE);
				  hayerror = true;
			  }
		  }
		  break;
		  
	  case 32:
		  
		  break;
		  
	  case 33:
		  
		  break;
		  
	  case 34:
		  
		  if(fields[33] == null || fields[33].compareToIgnoreCase("") == 0){
			  msgError.append(TablasSunat.REQUIREDFIELDMESSAGE);
			  hayerror = true;
		  }
		  
		  if(fields[33].length()!=1){
			  msgError.append(TablasSunat.WRONGLENGTHMESSAGE);
			  hayerror = true;
		  }
		  
		  break;
		  
		  default:
			  if(column>=35 && column<=68){
				  if(fields[column].length()>200){
					  msgError.append(TablasSunat.LENGTHEXCEEDINGLIMITMESSAGE);
					  hayerror = true;
				  }
			  }
			  break;
	  }
	  
	  objValidate obj = new objValidate();
	  obj.message = msgError.toString();
	  obj.hayerror = hayerror;
	  
	  return obj;
  }

}

class objValidate{
	public String message;
	public Boolean hayerror;
	
	public objValidate(){
		message = "";
		hayerror = false;
	}
}

/* Documentos de Identidad */
class pleDocumento{
	public String message;
	public Boolean hayerror;
	
	public pleDocumento(){
		message = "";
		hayerror = false;
	}
}

/* Comprobantes */
class plecomprobante{
	Boolean ValoresPorDefecto;
	
	/* Campos de serie */
	Boolean LongFijSerie;
	int LongSerie;
	Boolean ObliSerie;
	Boolean AlfNumSerie;
	
	String formatoSerie;
	
	
	/* campos de numero */
	Boolean LongFijNumero;
	int LongNumero;
	Boolean ObliNumero;
	Boolean AlfNumMumero;
	
	String formatoNumero;
	
	String Documento;
	
	Boolean esElectronica;
	
	/* campos de validacion */
	public StringBuilder message;
	public Boolean hayerror;
	
	public plecomprobante(){
		
	}
	
	public void validarSerieComprobante(){
		message = new StringBuilder("");
	}
	
	public void validarNumeroComprobante(){
		message = new StringBuilder("");
	}
	
}

