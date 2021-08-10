package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import view.GUI;



public class ResearchGate {
	
	static Node rootLattes;
	static String tituloLattes;
	static String tituloResearchGate;
	static ArrayList<String> listaAutores;
	static String listaAutoresString;
	static int anoLattes;
	static String url1;
	static Integer anoRG;
	static String urlProf;
	static String tipo;
	static ResultSet myRs;
	static int idProf;
	static int cont;
	static int itensPagina;
	static Document doc;
	static info.debatty.java.stringsimilarity.MetricLCS lcs = new info.debatty.java.stringsimilarity.MetricLCS();
	static int porta;
	static int nConexoes = 0;
	static String ip;
	static String url;
	static ArrayList<String> list = new ArrayList<String>();
	static final int timeout = 5000;
	
	public static void parse(Connection conn, Statement myStmt) {
		
		try {
	    	
	    	File folder = new File("files/lattes/");
	    	File[] listOfFiles = folder.listFiles();
	    	
			File file = new File("files/proxy80.txt"); 
			try {
				BufferedReader br = new BufferedReader(new FileReader(file)); 
				String st; 
					while ((st = br.readLine()) != null) {
					   list.add(st);
					}
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
			String[] aux = list.get(0).split(":");
			ip = aux[0];
			porta = Integer.parseInt(aux[1]);
	    	
	    	for (int k = 0; k < listOfFiles.length; k++) {
	    		GUI.incrementarBarraRG();
	    		cont = 1;
	    		itensPagina = 0;
	    		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    		org.w3c.dom.Document lattes = dBuilder.parse(new File("files/lattes/" + listOfFiles[k].getName()));
	    		
	    		//Nome do professor
	    		NodeList curriculo = lattes.getElementsByTagName("CURRICULO-VITAE");
	    		NodeList dadosGerais = ((Element)curriculo.item(0)).getElementsByTagName("DADOS-GERAIS");
	    		// Pesquisar a URL no Banco
	    		rootLattes = dadosGerais.item(0);
	    		Element l = (Element) rootLattes;
				String nome = l.getAttribute("NOME-COMPLETO");
				
				urlProf = pesquisarURL(nome, myStmt);
				
	    		if (listOfFiles[k].isFile() && urlProf != null) {
	    		System.out.println("RESEARCH GATE PARSING PROFESSOR: "+nome);
	    		Thread.sleep(timeout);
	    		doc = getProxy(urlProf+"/"+cont);
	    		//doc = Jsoup.connect(urlProf+"/"+cont).proxy("187.32.7.131", 80).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.140 Safari/537.36 Edge/17.17134").get(); //temporario
	    		// Dados Lattes
	    		NodeList producao = ((Element)curriculo.item(0)).getElementsByTagName("PRODUCAO-BIBLIOGRAFICA");
	    		NodeList livrosecapitulos = ((Element)curriculo.item(0)).getElementsByTagName("LIVROS-E-CAPITULOS");		
	    		NodeList demaistipos = ((Element)curriculo.item(0)).getElementsByTagName("DEMAIS-TIPOS-DE-PRODUCAO-BIBLIOGRAFICA");
	    		
				//Papers
	    		NodeList trabalhos = ((Element)producao.item(0)).getElementsByTagName("TRABALHOS-EM-EVENTOS");
	    		NodeList trabalho = ((Element)trabalhos.item(0)).getElementsByTagName("TRABALHO-EM-EVENTOS");
				//Artigos
	    		NodeList artigos = ((Element)producao.item(0)).getElementsByTagName("ARTIGOS-PUBLICADOS");
	    		NodeList artigo = ((Element)artigos.item(0)).getElementsByTagName("ARTIGO-PUBLICADO");
				//Livros e capitulos
	    		NodeList livros = null;
	    		NodeList capitulos = null;
	    		if(livrosecapitulos.getLength() != 0)
	    			livros = ((Element)livrosecapitulos.item(0)).getElementsByTagName("LIVROS-PUBLICADOS-OU-ORGANIZADOS");
	    		if(livrosecapitulos.getLength() != 0)
	    			capitulos = ((Element)livrosecapitulos.item(0)).getElementsByTagName("CAPITULOS-DE-LIVROS-PUBLICADOS");
	
				//Demais tipos
	    		NodeList outraproducao = null;
	    		if(demaistipos.getLength() != 0)
	    			outraproducao = ((Element)demaistipos.item(0)).getElementsByTagName("OUTRA-PRODUCAO-BIBLIOGRAFICA");
				
	    		NodeList livro = null;
	    		NodeList capitulo = null;
	
				if(livros != null && livros.getLength() != 0) {
					livro =  ((Element)livros.item(0)).getElementsByTagName("LIVRO-PUBLICADO-OU-ORGANIZADO");
				}
				if(capitulos != null && capitulos.getLength() != 0) {
					capitulo =  ((Element)capitulos.item(0)).getElementsByTagName("CAPITULO-DE-LIVRO-PUBLICADO");
				}
				
				//ResearchGate
		    	Elements elements = doc.getElementsByClass("nova-e-text nova-e-text--size-l nova-e-text--family-sans-serif nova-e-text--spacing-none nova-e-text--color-grey-600"); //Quantidade de artigos
		    	int quantidade = 0;
		    	for(org.jsoup.nodes.Element element : elements) {
		    		if(element.text().contains("Publications"))
		    			quantidade = Integer.parseInt(element.text().substring(element.text().indexOf("(")+1,element.text().indexOf(")")));
		    	}
		    	
				while(itensPagina < quantidade) { //Iterando sobre as varias paginas se o professor tiver > 100 artigos
					cont++;
					itensPagina += 100;
					boolean artigoEncontrado = false;
					int contador = 0;
					Elements list = doc.getElementsByClass("nova-v-publication-item__stack nova-v-publication-item__stack--gutter-m");
					for (org.jsoup.nodes.Element element : list) { //Itera sobre todos os artigos do research gate
							listaAutores = new ArrayList<String>();
							listaAutoresString = "";
							tipo = prepararDados(element);
							if(tipo.contains("Article") || tipo.contains("Technical Report") || tipo.contains("Conference Paper") || tipo.contains("Chapter") || tipo.contains("Book") ) {
								if(iterarArtigos(artigo)) {
									System.out.println("Artigo encontrado: " + tituloLattes);
									artigoEncontrado = true;
				
				//					if(!element.child(2).text().isEmpty())
				//						System.out.println("Ano : " + element.child(2).text().substring(element.child(2).text().length()-4));
				//					System.out.println("Artigo: " + element.child(0).text());
				//					System.out.println(" - Autores: " + element.child(1).text());
				//					System.out.println(" - Conferência: " + element.child(2).text() +"\n");
								}
								if(iterarTrabalhos(trabalho)) {
								artigoEncontrado = true;
								
				//					if(!element.child(2).text().isEmpty())
				//						System.out.println("Ano : " + element.child(2).text().substring(element.child(2).text().length()-4));
				//					System.out.println("Artigo: " + element.child(0).text());
				//					System.out.println(" - Autores: " + element.child(1).text());
				//					System.out.println(" - Conferência: " + element.child(2).text() +"\n");
								}
								else if(livro!= null && iterarLivros(livro)) {
									artigoEncontrado = true;
				
				//					if(!element.child(2).text().isEmpty())
				//						System.out.println("Ano : " + element.child(2).text().substring(element.child(2).text().length()-4));
				//					System.out.println("Artigo: " + element.child(0).text());
				//					System.out.println(" - Autores: " + element.child(1).text());
				//					System.out.println(" - Conferência: " + element.child(2).text() +"\n");
								}
								else if(capitulo!= null && iterarCapitulos(capitulo)) {
									artigoEncontrado = true;
				
				//					if(!element.child(2).text().isEmpty())
				//						System.out.println("Ano : " + element.child(2).text().substring(element.child(2).text().length()-4));
				//					System.out.println("Artigo: " + element.child(0).text());
				//					System.out.println(" - Autores: " + element.child(1).text());
				//					System.out.println(" - Conferência: " + element.child(2).text() +"\n");
								}
								else if(outraproducao!= null && iterarOutrasProducoes(outraproducao)) {
									artigoEncontrado = true;
				
				//					if(!element.child(2).text().isEmpty())
				//						System.out.println("Ano : " + element.child(2).text().substring(element.child(2).text().length()-4));
				//					System.out.println("Artigo: " + element.child(0).text());
				//					System.out.println(" - Autores: " + element.child(1).text());
				//					System.out.println(" - Conferência: " + element.child(2).text() +"\n");
								}
								if(!artigoEncontrado) {
									contador++;
									//System.out.println("Artigo: " + tituloResearchGate);
									inserir(conn, element);
								}
							} // Se é um artigo de interesse
							artigoEncontrado = false;
						} //for RG articles
					System.out.println("\n NÃO FORAM ENCONTRADOS "+contador+" ARTIGOS!"); //print missing articles for all teachers
					if(itensPagina < quantidade) { //Só conecta na proxima pagina caso ela exista
						Thread.sleep(timeout);
						doc = getProxy(urlProf+"/"+cont);
						//doc = Jsoup.connect(urlProf+"/"+cont).proxy("187.32.7.131", 80).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.140 Safari/537.36 Edge/17.17134").get();
					}
				}// while all pages
	    		} // if is a file and url not null
	    	} // for all files
		} //try
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static Document getProxy(String url) {
		if(nConexoes > 80) {
			list.remove(0);
			String[] aux = list.get(0).split(":");
			ip = aux[0];
			porta = Integer.parseInt(aux[1]);
			nConexoes = 0;
		}
		nConexoes++;
		org.jsoup.nodes.Document doc = null;
		org.jsoup.Connection con = null;
		boolean alive = false;
		while(!alive) {
			try {
				con = Jsoup.connect(url).proxy(ip, porta).timeout(timeout).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36");
				doc = con.get();
				alive = true;
				System.out.println("Conectou-se através do ip:porta > " + ip+":"+porta);
			} catch(Exception e){
				nConexoes = 0;
				list.remove(0);
				String[] aux = list.get(0).split(":");
	    		ip = aux[0];
	    		porta = Integer.parseInt(aux[1]);
				//System.out.println("Falhou!  \n");
	        }
		}
		return doc;
	}
	
	private static String prepararDados(org.jsoup.nodes.Element element) {
		String tipo = "";
		url1 = "";
		anoRG = null;
		if(element.childNodeSize() > 1)
			url1 = ((org.jsoup.nodes.Element) element).child(1).child(0).child(0).attr("href");
		else
			return "";
		for(int i = 0; i < element.childNodeSize(); i++) {
			if(element.child(i).childNodeSize() != 0 && element.child(i).child(0).attr("class").contains("nova-e-list")) {
				org.jsoup.nodes.Element autores = element.child(i).child(0); // Lista de autores
				for(int j = 0; j < autores.childNodeSize(); j++) {
//						System.out.println(autores.child(j).text() + "\n");
//						System.out.println(autores.child(j).getElementsByClass("nova-v-person-inline-item__fullname").first() + "\n");
//						System.out.println(autores.child(j).getElementsByClass("nova-v-person-inline-item__fullname").first().text() + "\n");
					org.jsoup.nodes.Element autor = autores.child(j).getElementsByClass("nova-v-person-inline-item__fullname").first();
					if(autor != null) {
						listaAutores.add(autor.text());
						if(j+1 != autores.childNodeSize())
							listaAutoresString += autor.text() + ", ";
						else
							listaAutoresString += autor.text();	
					}	
				}
				
			}
		}
		if(element.child(1).childNodeSize() > 0)
			tituloResearchGate = element.child(1).child(0).text();
		else
			return "";
		tipo = element.child(2).child(0).child(0).text();
		anoRG = null;
		org.jsoup.nodes.Element divAno = element.child(2).child(0);
		if(divAno.childNodeSize() >= 2) {
			String auxAno = divAno.child(divAno.childNodeSize() - 1).text();
            if(!auxAno.isEmpty() && auxAno.substring(auxAno.length()-4).matches("[0-9]+"))
                anoRG = Integer.parseInt(auxAno.substring(auxAno.length()-4));
		}
			
		return tipo;
	}

	private static void inserir(Connection conn, org.jsoup.nodes.Element element) {
		if(url1.isEmpty())
			return;
		 try {
			 Thread.sleep(timeout);
			 doc = getProxy(url1);
			 //doc = Jsoup.connect(url+url1).proxy("187.32.7.131", 80).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.140 Safari/537.36 Edge/17.17134").get();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		 org.jsoup.nodes.Element header = doc.getElementsByClass("research-detail-header-section__metadata").first();
		 String doi = null;
		 String conferencia = null;
		 
		 if(header.getElementsByClass("nova-e-link nova-e-link--color-inherit nova-e-link--theme-decorated").first() != null)
			 conferencia = header.getElementsByClass("nova-e-link nova-e-link--color-inherit nova-e-link--theme-decorated").first().text();
		 
		 for(int i = 0; i < header.childNodeSize(); i++) {
			 if(header.child(i).text().contains("DOI:"))
				 doi = header.child(i).text();
		 }

		 org.jsoup.nodes.Element abstractElement = doc.getElementsByClass("nova-e-text nova-e-text--size-m nova-e-text--family-sans-serif nova-e-text--spacing-auto nova-e-text--color-inherit").first();
		 String _abstract = null;
		 if(abstractElement != null)
			 _abstract = abstractElement.text();
		 
		 int reads = 0;
		 System.out.println("Artigo: " + tituloResearchGate);
		 System.out.println("Autor: "+ listaAutoresString);
		 System.out.println("Conferencia: "+ conferencia);
		 System.out.println("Tipo: "+ tipo);
		 System.out.println("DOI: "+ doi);
		 if(_abstract == null)
			 System.out.println("Sem abstract \n");
		 else
			 System.out.println("Tem abstract \n");
		 
		 try {
			PreparedStatement stmt = conn.prepareStatement("INSERT INTO researchgate (title, authors, conference, _year, _event, doi, abstract, id_prof, reads) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
			stmt.setString(1, tituloResearchGate);
			stmt.setString(2,  listaAutoresString);
			stmt.setString(3, conferencia);
			if(anoRG == null)
				stmt.setNull(4, java.sql.Types.INTEGER);
			else
				stmt.setInt(4, anoRG);
			stmt.setString(5, tipo);
			stmt.setString(6, doi);
			stmt.setString(7, _abstract);
			stmt.setInt(8, idProf);
			stmt.setInt(9, reads);
			stmt.executeUpdate();
			conn.commit();
		 } catch(SQLException e) {
			 e.printStackTrace();
		 }
	}

	private static String pesquisarURL(String nome, Statement myStmt) {
		try {
			myRs = myStmt.executeQuery("SELECT linkresearchgate, id FROM Professores WHERE nome LIKE " +"'"+ nome + "'");
			myRs.next();
			idProf =  myRs.getInt("id");
			String linkRG = myRs.getString("linkresearchgate");
			return linkRG;
    	} catch (SQLException e) {
    		e.printStackTrace(); //Não pode criar arquivo
    	}
		return null;
	}

	private static boolean procurarObra(String tituloLattes) {
		 tituloLattes.toLowerCase();
		 String auxTitulo = tituloResearchGate.toLowerCase();
//		 if(tituloResearchGate.contains("Design of distributed multimedia applications (DAMD)")) {
//		 System.out.println("Ano > "+ ano + " - AnoLattes > :" + anoLattes);
//		 System.out.println("Lattes > "+ tituloLattes);
//		 System.out.println("Google > "+ tituloGoogle);
//		 System.out.println("Distancia > " + lcs.distance(tituloLattes, tituloGoogle) +"\n");
//		 }
		 
		 if(lcs.distance(tituloLattes, auxTitulo) < 0.3) { // Os titulos são praticamente iguais ou iguais
			 return true;
		 }
		 else if(lcs.distance(tituloLattes, auxTitulo) < 0.5) { // Os titulos são similares logo os artigos são provavelmente os mesmos
				 if(anoRG == null || anoRG == anoLattes) { //Ano igual e texto similiar = Mesmo artigo
					 return true;
				 }
		 }
				 
		 
		 else if(lcs.distance(tituloLattes, auxTitulo) < 0.8) { //Os titulos não são muito similares, mas ainda é necessario verificar outras variaveis para ter certeza
			    if(anoRG != null && anoRG == anoLattes) { //Aqui achou todos os autores, ano é o mesmo, e o texto é semi parecido então o artigo é o mesmo
				    Element l = (Element) rootLattes;
				    NodeList autoresLattes = l.getElementsByTagName("AUTORES");
				 	String autorLattes;
				 	int achouSobrenome = 0;
				 	for(int i = 0; i < listaAutores.size(); i++) {
				 		String[] autoresAux = listaAutores.get(i).split(" ");
						String sobrenome = autoresAux[autoresAux.length-1].toLowerCase();
				    	 for(int j = 0; j < autoresLattes.getLength(); j++ ) {
				    		 Element aux = (Element) autoresLattes.item(j);
				    		 autorLattes = aux.getAttribute("NOME-COMPLETO-DO-AUTOR");
				    		 autorLattes.toLowerCase();
				    		 if(autorLattes.contains(sobrenome)) { //Se achar o sobrenome de um autor do RG no Lattes então vai para o proximo autor
				    			 achouSobrenome++;
				    			 break;
				    		 }	 
				    	
				    	 }
				 	}
			    	if(achouSobrenome == autoresLattes.getLength()) //Se achou TODOS os autores
			    		return true;
			    }
		}
		
		return false;
	}
	
	private static boolean iterarArtigos(NodeList artigo) {
		for(int a = 0; a < artigo.getLength(); a++) {
			//Verificar se o artigo contido no GS também esta no lattes
			rootLattes = artigo.item(a); //ARTIGOS
 			Element l = (Element) rootLattes;
 			Element dadosBasicos = (Element) l.getElementsByTagName("DADOS-BASICOS-DO-ARTIGO").item(0); //DADOS BASICOS
 			tituloLattes = dadosBasicos.getAttribute("TITULO-DO-ARTIGO");
 			anoLattes = Integer.parseInt(dadosBasicos.getAttribute("ANO-DO-ARTIGO"));
			 if(procurarObra(tituloLattes))
				 return true;
		}
		return false;
	}
	
	private static boolean iterarTrabalhos(NodeList trabalho) {
		for(int a = 0; a < trabalho.getLength(); a++) {
			//Verificar se o trabalho contido no GS também esta no lattes
			rootLattes = trabalho.item(a); //ARTIGOS
 			Element l = (Element) rootLattes;
 			Element dadosBasicos = (Element) l.getElementsByTagName("DADOS-BASICOS-DO-TRABALHO").item(0); //DADOS BASICOS
 			tituloLattes = dadosBasicos.getAttribute("TITULO-DO-TRABALHO");
 			anoLattes = Integer.parseInt(dadosBasicos.getAttribute("ANO-DO-TRABALHO"));
			 if(procurarObra(tituloLattes))
				 return true;
		}	
		return false;
	}
	
	private static boolean iterarLivros(NodeList livro) {
		for(int a = 0; a < livro.getLength(); a++) {
			//Verificar se o livro contido no GS também esta no lattes
			rootLattes = livro.item(a);
 			Element l = (Element) rootLattes;
 			Element dadosBasicos = (Element) l.getElementsByTagName("DADOS-BASICOS-DO-LIVRO").item(0); //DADOS BASICOS
 			tituloLattes = dadosBasicos.getAttribute("TITULO-DO-LIVRO");
 			anoLattes = Integer.parseInt(dadosBasicos.getAttribute("ANO"));
			 if(procurarObra(tituloLattes))
				 return true;
		}	
		return false;
	}
	
	private static boolean iterarCapitulos(NodeList capitulo) {
		for(int a = 0; a < capitulo.getLength(); a++) {
			//Verificar se o capitulo do livro contido no GS também esta no lattes
			rootLattes = capitulo.item(a); //ARTIGOS
 			Element l = (Element) rootLattes;
 			Element dadosBasicos = (Element) l.getElementsByTagName("DADOS-BASICOS-DO-CAPITULO").item(0); //DADOS BASICOS
 			tituloLattes = dadosBasicos.getAttribute("TITULO-DO-CAPITULO-DO-LIVRO");
 			anoLattes = Integer.parseInt(dadosBasicos.getAttribute("ANO"));
			 if(procurarObra(tituloLattes))
				 return true;
		}	
		return false;
	}
	
	private static boolean iterarOutrasProducoes(NodeList outraproducao) {
		for(int a = 0; a < outraproducao.getLength(); a++) {
			//Verificar se "OutraProducao" contido no GS também esta no lattes
			rootLattes = outraproducao.item(a); //ARTIGOS
 			Element l = (Element) rootLattes;
 			Element dadosBasicos = (Element) l.getElementsByTagName("DADOS-BASICOS-DE-OUTRA-PRODUCAO").item(0); //DADOS BASICOS
 			tituloLattes = dadosBasicos.getAttribute("TITULO");
 			anoLattes = Integer.parseInt(dadosBasicos.getAttribute("ANO"));
			 if(procurarObra(tituloLattes))
				 return true;
		}	
		return false;
	}
		
	}