package pe.com.unifiedgo.report.ad_reports;

import java.io.Serializable;
import java.math.BigDecimal;

@SuppressWarnings("serial")
public class ReporteEstadoSituacionFinancieraBean implements Serializable {
  private String tipo;
  private String order1;
  private String order2;
  private String categoria;
  private String categoriaSunatCode;
  private String subCategoriaId;
  private String subCategoria;
  private String subCategoriaSunatCode;
  private String tipoGrupo;
  private String greaterThanAYear;
  private String consider;
  private String considerwhen;
  private BigDecimal saldoAnioAnterior;
  private BigDecimal saldoAnioActual;
  private String acctvalue;
  private String considerar;
  private String grupoCategoria;
  private String idperiodo;
  private String periodo;
  private String fechainicial;
  private String fechafinal;
  private String idorganizacion;
  private String factAmount;

  public String getTipo() {
    return tipo;
  }

  public void setTipo(String tipo) {
    this.tipo = tipo;
  }

  public String getOrder1() {
    return order1;
  }

  public void setOrder1(String order1) {
    this.order1 = order1;
  }

  public String getOrder2() {
    return order2;
  }

  public void setOrder2(String order2) {
    this.order2 = order2;
  }

  public String getCategoria() {
    return categoria;
  }

  public void setCategoria(String categoria) {
    this.categoria = categoria;
  }

  public String getSubCategoria() {
    return subCategoria;
  }

  public void setSubCategoria(String subCategoria) {
    this.subCategoria = subCategoria;
  }

  public String getSubCategoriaId() {
    return subCategoriaId;
  }

  public void setSubCategoriaId(String subCategoriaId) {
    this.subCategoriaId = subCategoriaId;
  }

  public String getTipoGrupo() {
    return tipoGrupo;
  }

  public void setTipoGrupo(String tipoGrupo) {
    this.tipoGrupo = tipoGrupo;
  }

  public String getGreaterThanAYear() {
    return greaterThanAYear;
  }

  public void setGreaterThanAYear(String greaterThanAYear) {
    this.greaterThanAYear = greaterThanAYear;
  }

  public String getConsider() {
    return consider;
  }

  public void setConsider(String consider) {
    this.consider = consider;
  }

  public String getConsiderwhen() {
    return considerwhen;
  }

  public void setConsiderwhen(String considerwhen) {
    this.considerwhen = considerwhen;
  }

  public BigDecimal getSaldoAnioAnterior() {
    return saldoAnioAnterior;
  }

  public void setSaldoAnioAnterior(BigDecimal saldoAnioAnterior) {
    this.saldoAnioAnterior = saldoAnioAnterior;
  }

  public BigDecimal getSaldoAnioActual() {
    return saldoAnioActual;
  }

  public void setSaldoAnioActual(BigDecimal saldoAnioActual) {
    this.saldoAnioActual = saldoAnioActual;
  }

  public String getAcctvalue() {
    return acctvalue;
  }

  public void setAcctvalue(String acctvalue) {
    this.acctvalue = acctvalue;
  }

  public String getConsiderar() {
    return considerar;
  }

  public void setConsiderar(String considerar) {
    this.considerar = considerar;
  }

  public String getGrupoCategoria() {
    return grupoCategoria;
  }

  public void setGrupoCategoria(String grupoCategoria) {
    this.grupoCategoria = grupoCategoria;
  }

  public String getIdperiodo() {
    return idperiodo;
  }

  public void setIdperiodo(String idperiodo) {
    this.idperiodo = idperiodo;
  }

  public String getPeriodo() {
    return periodo;
  }

  public void setPeriodo(String periodo) {
    this.periodo = periodo;
  }

  public String getFechainicial() {
    return fechainicial;
  }

  public void setFechainicial(String fechainicial) {
    this.fechainicial = fechainicial;
  }

  public String getFechafinal() {
    return fechafinal;
  }

  public void setFechafinal(String fechafinal) {
    this.fechafinal = fechafinal;
  }

  public String getIdorganizacion() {
    return idorganizacion;
  }

  public void setIdorganizacion(String idorganizacion) {
    this.idorganizacion = idorganizacion;
  }

  public String getFactAmount() {
    return factAmount;
  }

  public void setFactAmount(String factAmount) {
    this.factAmount = factAmount;
  }

  public String getSubCategoriaSunatCode() {
    return subCategoriaSunatCode;
  }

  public void setSubCategoriaSunatCode(String subCategoriaSunatCode) {
    this.subCategoriaSunatCode = subCategoriaSunatCode;
  }

  public String getCategoriaSunatCode() {
    return categoriaSunatCode;
  }

  public void setCategoriaSunatCode(String categoriaSunatCode) {
    this.categoriaSunatCode = categoriaSunatCode;
  }

  public ReporteEstadoSituacionFinancieraBean() {

  }

}