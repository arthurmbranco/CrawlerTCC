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



public class GoogleScholar {
	
	static Node rootLattes;
	static String tituloLattes;
	static String tituloGoogle;
	static String tituloGoogleAux;
	static int anoLattes;
	static int anoGoogle;
	static String[] arrayAutores;
	static org.jsoup.nodes.Element contaPaginas;
	static String first = "&cstart=";
	static int first1;
	static int contador1 = 0;
	static String last = "&pagesize=";
	static int last1 = 100;
	static ResultSet myRs;
	static int idProf;
	static int porta;
	static int nConexoes = 0;
	static String ip;
	static Document doc;
	static String url;
	static ArrayList<String> list = new ArrayList<String>();
	static info.debatty.java.stringsimilarity.MetricLCS lcs = new info.debatty.java.stringsimilarity.MetricLCS();
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
				e.printStackTrace();
			} 
			
			String[] aux = list.get(0).split(":");
			ip = aux[0];
			porta = Integer.parseInt(aux[1]);
	    	
	    	for (int k = 0; k < listOfFiles.length; k++) {
	    		GUI.incrementarBarraGS();
	    		first1 = 0;
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
				String urlInicial = pesquisarURL(nome, myStmt);
				url = urlInicial + first + first1 + last + last1;
				
	    		if (listOfFiles[k].isFile() && urlInicial != null) {
		    		System.out.println("GOOGLE SCHLOLAR PARSING PROFESSOR: "+nome);
		    		doc = getProxy(url);
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
					
					//Google Scholar
					contaPaginas = doc.getElementById("gsc_lwp");
					
					while(contaPaginas.child(0).text().contains("Artigos")) {	
					boolean artigoEncontrado = false;
					int contador = 0;
					Elements list = doc.getElementsByClass("gsc_a_tr");
					for (org.jsoup.nodes.Element element : list) { //Itera sobre todos os artigos do google scholar
						if(((org.jsoup.nodes.Element) element).child(0).childNodeSize() > 2) {
							tituloGoogle = ((org.jsoup.nodes.Element) element).child(0).child(0).text();
							tituloGoogleAux = tituloGoogle.toLowerCase();
							anoGoogle = 0;
							 if(!element.child(2).child(0).text().isEmpty()) { // Se existir ANO
								anoGoogle = Integer.parseInt(element.child(2).child(0).text());
							 }
							 arrayAutores  = ((org.jsoup.nodes.Element) element).child(0).child(1).text().split(",");
							 
							if(iterarArtigos(artigo)) {
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
								//if(!element.child(2).child(0).text().isEmpty())  // Se existir ANO no valor da conferência
								//	 System.out.println("Ano : " + anoGoogle);
								//System.out.println("Artigo: " + tituloGoogle);
								//System.out.println(" - Autores: " + arrayAutores);
								//System.out.println(" - Conferência: " + ((org.jsoup.nodes.Element) element).child(0).child(2).text() +"\n");
								inserir(conn, element);
							}
						}
							
					
						artigoEncontrado = false;
					}
					System.out.println("\n NÃO FORAM ENCONTRADOS "+contador+" ARTIGOS! \n");
					first1 += 100;
					url = urlInicial + first + first1 + last + last1;
					doc = getProxy(url);
					contaPaginas = doc.getElementById("gsc_lwp");
					Thread.sleep(1000);
					}
	    		}
	    	}
		}
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
		boolean alive = false;
		while(!alive) {
			try {
				doc = Jsoup.connect(url).proxy(ip, porta).timeout(timeout).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.140 Safari/537.36 Edge/17.17134").get();
				alive = true;
				System.out.println("Conectou-se através do ip:porta > " + ip+":"+porta);
			}catch(IOException ioe){
				nConexoes = 0;
				list.remove(0);
				String[] aux = list.get(0).split(":");
	    		ip = aux[0];
	    		porta = Integer.parseInt(aux[1]);
				System.out.println("Falhou! \n");
	        }
		}
		return doc;
	}
	
	private static void inserir(Connection conn, org.jsoup.nodes.Element element) {
		 Integer ano = null;
		 
		 if(!element.child(2).child(0).text().isEmpty())   // Se existir ANO no valor da conferência
			ano = Integer.parseInt(element.child(2).child(0).text());
//		 String autores  = ((org.jsoup.nodes.Element) element).child(0).child(1).text();
//		 String conferencia = ((org.jsoup.nodes.Element) element).child(0).child(2).text();
		 int citations = 0;
		 if(!element.child(1).child(0).text().isEmpty()) 
			 citations =  Integer.parseInt(((org.jsoup.nodes.Element) element).child(1).child(0).text());
		 
		 String url = "https://scholar.google.com.br";
		 String url1 = ((org.jsoup.nodes.Element) element).child(0).child(0).attr("data-href");

		 doc = getProxy(url+url1);

		
		 Elements list = doc.getElementsByClass("gs_scl");
		 String autores = null;
		 String pages = null;
		 String publisher = null;
		 String _abstract = null;
		 String volume = null;
		 String _number = null;
		 String conference = null;
		 
		 for (org.jsoup.nodes.Element element1 : list) { 
			 if(element1.child(0).text().contains("Autores")) {
				 autores = element1.child(1).text();
			 }
			 else if(element1.child(0).text().contains("Páginas")) {
				 pages = element1.child(1).text();
			 }
			 else if(element1.child(0).text().contains("Editora")) {
				 publisher = element1.child(1).text();
			 }
			 else if(element1.child(0).text().contains("Descrição")) {
				 _abstract = element1.child(1).text();
			 }
			 else if(element1.child(0).text().contains("Volume")) {
				 volume = element1.child(1).text();
			 }
			 else if(element1.child(0).text().contains("Edição")) {
				 _number = element1.child(1).text();
			 }
			 else if(element1.child(0).text().contains("Publicações") || element1.child(0).text().contains("Conferência") || element1.child(0).text().contains("Livro")) {
				 conference = element1.child(1).text();
			 }
		 }
		 
		 
		 try {
			PreparedStatement stmt = conn.prepareStatement("INSERT INTO googlescholar (title, authors, conference, pages, publisher, citations, abstract, volume, _number, _year, id_prof) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			stmt.setString(1, tituloGoogle);
			stmt.setString(2,  autores);
			stmt.setString(3, conference);
			stmt.setString(4, pages);
			stmt.setString(5, publisher);
			stmt.setInt(6, citations);
			stmt.setString(7, _abstract);
			stmt.setString(8, volume);
			stmt.setString(9, _number);
			if(ano == null)
				stmt.setNull(10, java.sql.Types.INTEGER);
			else
				stmt.setInt(10, ano);
			stmt.setInt(11, idProf);
			stmt.executeUpdate();
			conn.commit();
		 } catch(SQLException e) {
			 e.printStackTrace();
		 }
	}

	private static String pesquisarURL(String nome, Statement myStmt) {
		try {
			myRs = myStmt.executeQuery("SELECT linkgooglescholar, id FROM Professores WHERE nome LIKE " +"'"+ nome + "'");
			while(myRs.next()) {
				idProf =  myRs.getInt("id");
				return myRs.getString("linkgooglescholar");
				
			}
    	} catch (SQLException e) {
    		e.printStackTrace(); //Não pode criar arquivo
    	}
		return null;
	}

	private static boolean procurarObra(String tituloLattes) {
		 tituloLattes.toLowerCase();
		 
		 if(lcs.distance(tituloLattes, tituloGoogleAux) < 0.3) { // Os titulos são praticamente iguais ou iguais
			 return true;
		 }
		 else if(lcs.distance(tituloLattes, tituloGoogleAux) < 0.5) { // Os titulos são similares logo os artigos são provavelmente os mesmos
				 if(anoGoogle == 0 || anoGoogle == anoLattes) { //Ano igual e texto similiar = Mesmo artigo
					 return true;
				 }
		 }
				 
		 
		 else if(lcs.distance(tituloLattes, tituloGoogleAux) < 0.8) { //Os titulos não são muito similares, mas ainda é necessario verificar outras variaveis para ter certeza
			    if(anoGoogle == anoLattes) { //Aqui achou todos os autores, ano é o mesmo, e o texto é semi parecido então o artigo é o mesmo
			    Element l = (Element) rootLattes;
			    NodeList autoresLattes = l.getElementsByTagName("AUTORES");
			 	String autorLattes;
			 	int achouSobrenome = 0;
			     for (int x=0; x<arrayAutores.length; x++) { // Procurando pelos sobrenomes para comparar com o Lattes
			    	 String[] autores = arrayAutores[x].split(" ");
			    	 String sobrenome = arrayAutores[x].split(" ")[autores.length-1];
			    	 sobrenome.toLowerCase();
			    	 for(int i = 0; i < autoresLattes.getLength(); i++ ) {
			    		 Element aux = (Element) autoresLattes.item(i);
			    		 autorLattes = aux.getAttribute("NOME-COMPLETO-DO-AUTOR");
			    		 autorLattes.toLowerCase();
			    		 if(autorLattes.contains(sobrenome)) { //Se achar o sobrenome de um autor do GS no Lattes então vai para o proximo autor
			    			 achouSobrenome++;
			    			 break;
			    		 }	 
			    	
			    	 }
			     }
		    	 if(achouSobrenome == autoresLattes.getLength())
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