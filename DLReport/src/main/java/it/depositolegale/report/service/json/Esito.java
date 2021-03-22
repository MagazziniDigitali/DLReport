/**
 * 
 */
package it.depositolegale.report.service.json;

/**
 * @author massi
 *
 */
public class Esito {

	private String testo;
	
	private String pdf;
	
	private String csv;
	
	private String xls;
	
	/**
	 * 
	 */
	public Esito() {
	}

	public String getTesto() {
		return testo;
	}

	public void setTesto(String testo) {
		this.testo = testo;
	}

	public String getPdf() {
		return pdf;
	}

	public void setPdf(String pdf) {
		this.pdf = pdf;
	}

	public String getCsv() {
		return csv;
	}

	public void setCsv(String csv) {
		this.csv = csv;
	}

	public String getXls() {
		return xls;
	}

	public void setXls(String xls) {
		this.xls = xls;
	}

}
