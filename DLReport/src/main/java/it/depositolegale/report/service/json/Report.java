package it.depositolegale.report.service.json;

public class Report {

	private String data;

	private String tipo;
	
	private Esito esito;
	
	private boolean archived = false;

	private String rivista;

	
	public String getRivista() {
		return rivista;
	}

	public void setRivista(String rivista) {
		this.rivista = rivista;
	}

	public Report() {
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public Esito getEsito() {
		return esito;
	}

	public void setEsito(Esito esito) {
		this.esito = esito;
	}

	public boolean getArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

}
