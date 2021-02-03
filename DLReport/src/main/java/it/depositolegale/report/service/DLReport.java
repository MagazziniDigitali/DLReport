package it.depositolegale.report.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import it.depositolegale.report.service.json.Esito;
import it.depositolegale.report.service.json.Istituto;
import it.depositolegale.report.service.json.ListaIstituti;
import it.depositolegale.report.service.json.ListaReport;
import it.depositolegale.report.service.json.Report;
import mx.randalf.configuration.Configuration;
import mx.randalf.configuration.exception.ConfigurationException;

public class DLReport extends HttpServlet implements Servlet {

	private ListaIstituti listaIstituti = null;

	/**
	 * 
	 */
	private static final long serialVersionUID = 8341586698705647279L;

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init() throws ServletException {
		super.init();
		init(this.getServletContext().getInitParameter("nomeCatalogo"));
	}

	private void init(String nomeCatalogos) throws ServletException {
		String pathProperties = "";
		String[] st = null;
		File f= null;

		try {
			if (!Configuration.isInizialize()) {
				st = nomeCatalogos.split("\\|");
				for(String nomeCatalogo : st) {
					if (nomeCatalogo != null && nomeCatalogo.startsWith("file://"))
						pathProperties = nomeCatalogo.replace("file:///", "");
					else {
						pathProperties = System.getProperty("catalina.base")
								+ File.separator;
						if (nomeCatalogo == null)
							pathProperties += "conf/teca_digitale";
						else
							pathProperties += nomeCatalogo;
					}
	
					f = new File(pathProperties);
					if (f.exists()) {
						Configuration.init(f.getAbsolutePath());
						if (Configuration.getValue("pathListe")!= null) {
							initListe(new File(Configuration.getValue("pathListe")));
						}
						break;
					}
				}
			}
		} catch (ConfigurationException e) {
			throw new ServletException(e.getMessage(), e);
		}
	}

	private void initListe(File file) throws ServletException {
		File[] fl = null;
		FileReader fr = null;
		BufferedReader br = null;
		String testo = null;
		String[] st = null;
		String istituto ="";
		
		fl = file.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File pathname) {
				boolean result = false;
				
				if (pathname.isFile()) {
					if (pathname.getName().toLowerCase().endsWith(".csv")) {
						result = true;
					}
				}
				return result;
			}
		});
		
		listaIstituti = new ListaIstituti();
		for(File f:fl) {
			try {
				fr = new FileReader(f);
				br = new BufferedReader(fr);
				while((testo = br.readLine())!= null) {
					st = testo.split(";");
					istituto = st[1];
					if (st.length>2) {
						for(int x=2;x<st.length; x++) {
							istituto +=";"+st[x];
						}
					}
					listaIstituti.add(istituto(istituto, st[0].replace("\uFEFF", ""))); 
						
				}
			} catch (FileNotFoundException e) {
				throw new ServletException(e.getMessage(),e);
			} catch (IOException e) {
				throw new ServletException(e.getMessage(),e);
			} catch (ServletException e) {
				throw e;
			} finally {
				try {
					if (br != null) {
						br.close();
					}
					if (fr != null) {
						fr.close();
					}
				} catch (IOException e) {
					throw new ServletException(e.getMessage(),e);
				}
			}
		}
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		esegui(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		esegui(request, response);
	}
	
	private void esegui(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String azione = "";
		PrintWriter out  = null;

		azione = request.getParameter("azione");
		
		if (azione !=null) {
			switch (azione) {
			case "listaIstituti":
				
				out = response.getWriter();
				response.setContentType("application/json; charset=UTF-8");
				response.setCharacterEncoding("UTF-8");
				out.print(listaIstituti());
				out.flush();
				break;

			case "listaReport":
				
				out = response.getWriter();
				response.setContentType("application/json; charset=UTF-8");
				response.setCharacterEncoding("UTF-8");
				out.print(listaReport(request.getParameter("sigla")));
				out.flush();
				break;

			default:
				throw new ServletException("L'azione ["+azione+"] non risulta gestibie");
			}
		} else {
			throw new ServletException("Indicare il tipo di azione che si vuole eseguire");
		}
	}

	private String listaReport(String sigla) throws ServletException {
		String pathReport = null;
		File folder = null;
		File[] fl = null;
		Hashtable<String, Report> reports = null;
		Report report = null;
		Esito  esito = null;
		String[] st = null;
		String[] st2 = null;
		ListaReport listaReport = null;
		Enumeration<String> keys = null;
		String key = null;
		boolean archived = false;
		
		try {
			listaReport = new ListaReport();
			pathReport = Configuration.getValue("pathReport");
			if (pathReport != null) {
				folder = new File(pathReport+File.separator+sigla);
				if (folder.exists()) {
					fl = folder.listFiles(new FileFilter() {
						
						@Override
						public boolean accept(File pathname) {
							boolean result = false;
							
							if (!pathname.getName().startsWith(".") &&
									(pathname.getName().toLowerCase().endsWith(".txt") ||
											pathname.getName().toLowerCase().endsWith(".pdf") ||
											pathname.getName().toLowerCase().endsWith(".csv") ||
											pathname.getName().toLowerCase().endsWith(".xls"))) {
								result = true;
							}
							return result;
						}
					});
					
					Arrays.sort(fl);
					reports = new Hashtable<String, Report>();
					for (File f: fl) {
						st = f.getName().split("_");
						
						key = "";
						archived=false;
						
						for (int x=0; x<st.length; x++) {
							if (x==st.length-1) {
								st2 = st[x].split("\\.");
								if (x!= 4) {
									key+=st2[0];
									if (st2[0].trim().equalsIgnoreCase("archived")) {
										archived = true;
									}
								}
							} else if (x!= 4) {
								key+=st[x];
							}
						}
						
						
						if (reports.containsKey(key)) {
							report = reports.get(key);
						} else {
							report = new Report();
						}
						report.setData(st[2]+"/"+st[1]+"/"+st[0]);
						report.setTipo(st[3]);
						report.setArchived(archived);

						if (report.getEsito()==null) {
							esito = new Esito();
						} else {
							esito = report.getEsito();
						}
						if (st2[1].equalsIgnoreCase("txt")) {
							esito.setTesto("report/"+sigla+"/"+f.getName());
						} 
						if (st2[1].equalsIgnoreCase("pdf")) {
							esito.setPdf("report/"+sigla+"/"+f.getName());
						} 
						if (st2[1].equalsIgnoreCase("csv")) {
							esito.setCsv("report/"+sigla+"/"+f.getName());
						} 
						if (st2[1].equalsIgnoreCase("xls")) {
							esito.setXls("report/"+sigla+"/"+f.getName());
						} 
						report.setEsito(esito);
						reports.put(key, report);
					}
					
					keys = reports.keys();
					List<String> list = Collections.list(keys);
			        Collections.sort(list);

			        // Prints out all generated number after sorted.
			        for (String nkey : list) {
			        	listaReport.add(reports.get(nkey));
			        }
				}
			}
		} catch (ConfigurationException e) {
			throw new ServletException(e.getMessage(), e);
		}
		return new Gson().toJson(listaReport);
	}

	private String listaIstituti() throws ServletException {
		
		return new Gson().toJson(listaIstituti);
	}

	private Istituto istituto(String nome, String sigla) throws ServletException {
		Istituto istituto = null;
		
		istituto = new Istituto();
		istituto.setNome(nome);
		istituto.setSigla(sigla);
		return istituto;
	}
}
