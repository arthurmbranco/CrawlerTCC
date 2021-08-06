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
import java.util.UUID;

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
	static String listaAutores;
	static int anoLattes;
	static String url1;
	static Integer anoRG;
	static String urlProf;
	static String tipo;
	static ResultSet myRs;
	static int id = 0;
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
	    		Thread.sleep(3000);
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
		    	String str = doc.getElementsByClass("nova-o-stack__item pagination--top").first().text(); //Quantidade de artigos
		    	int quantidade = Integer.parseInt(str.substring(str.indexOf("(")+1,str.indexOf(")")));
		    	
				while(itensPagina < quantidade) { //Iterando sobre as varias paginas se o professor tiver > 100 artigos
					cont++;
					itensPagina += 100;
					boolean artigoEncontrado = false;
					int contador = 0;
					//int testecont = 0;
					Elements list = doc.getElementsByClass("nova-v-publication-item__stack nova-v-publication-item__stack--gutter-m");
					for (org.jsoup.nodes.Element element : list) { //Itera sobre todos os artigos do research gate
						//testecont++;
			   		//if(testecont >= 90 ) {
							listaAutores = "";
							tipo = prepararDados(element);
							if(tipo.contains("Article") || tipo.contains("Technical Report") || tipo.contains("Conference Paper") || tipo.contains("Chapter") || tipo.contains("Book") ) {
								if(iterarArtigos(artigo, element)) {
									artigoEncontrado = true;
				
				//					if(!element.child(2).text().isEmpty())
				//						System.out.println("Ano : " + element.child(2).text().substring(element.child(2).text().length()-4));
				//					System.out.println("Artigo: " + element.child(0).text());
				//					System.out.println(" - Autores: " + element.child(1).text());
				//					System.out.println(" - Conferência: " + element.child(2).text() +"\n");
								}
								if(iterarTrabalhos(trabalho, element)) {
								artigoEncontrado = true;
								
				//					if(!element.child(2).text().isEmpty())
				//						System.out.println("Ano : " + element.child(2).text().substring(element.child(2).text().length()-4));
				//					System.out.println("Artigo: " + element.child(0).text());
				//					System.out.println(" - Autores: " + element.child(1).text());
				//					System.out.println(" - Conferência: " + element.child(2).text() +"\n");
								}
								else if(livro!= null && iterarLivros(livro, element)) {
									artigoEncontrado = true;
				
				//					if(!element.child(2).text().isEmpty())
				//						System.out.println("Ano : " + element.child(2).text().substring(element.child(2).text().length()-4));
				//					System.out.println("Artigo: " + element.child(0).text());
				//					System.out.println(" - Autores: " + element.child(1).text());
				//					System.out.println(" - Conferência: " + element.child(2).text() +"\n");
								}
								else if(capitulo!= null && iterarCapitulos(capitulo, element)) {
									artigoEncontrado = true;
				
				//					if(!element.child(2).text().isEmpty())
				//						System.out.println("Ano : " + element.child(2).text().substring(element.child(2).text().length()-4));
				//					System.out.println("Artigo: " + element.child(0).text());
				//					System.out.println(" - Autores: " + element.child(1).text());
				//					System.out.println(" - Conferência: " + element.child(2).text() +"\n");
								}
								else if(outraproducao!= null && iterarOutrasProducoes(outraproducao, element)) {
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
				//	  } //teste cont
						} //for RG articles
					System.out.println("\n NÃO FORAM ENCONTRADOS "+contador+" ARTIGOS!"); //print missing articles for all teachers
					if(itensPagina < quantidade) { //Só conecta na proxima pagina caso ela exista
						Thread.sleep(3000);
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
		boolean alive = false;
		while(!alive) {
			try {
				doc = Jsoup.connect(url).proxy(ip, porta).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.140 Safari/537.36 Edge/17.17134").get();
				alive = true;
				System.out.println("Conectou-se através do ip:porta > " + ip+":"+porta);
			}catch(IOException ioe){
				nConexoes = 0;
				list.remove(0);
				String[] aux = list.get(0).split(":");
	    		ip = aux[0];
	    		porta = Integer.parseInt(aux[1]);
				System.out.println("Falhou!");
	        }
		}
		return doc;
	}
	
	private static String prepararDados(org.jsoup.nodes.Element element) {
		String tipo = "";
		url1 = "";
		anoRG = null;
		if((element.child(0).child(0).child(0).attr("class").contains("nova-c-image-strip")) && element.child(0).child(0).child(0).child(0).attr("class").contains("nova-c-image-strip__container")) {
			url1 = ((org.jsoup.nodes.Element) element).child(1).child(0).child(0).attr("href");
			//org.jsoup.nodes.Element autores = ((org.jsoup.nodes.Element) element).child(3).child(0);
			org.jsoup.nodes.Element autores = null;
			for(int i = 0; i < element.childNodeSize(); i++) {
				if(element.child(i).child(0).attr("class").contains("nova-e-list")) {
					autores = ((org.jsoup.nodes.Element) element).child(i).child(0);
				}
			}
			tituloResearchGate = element.child(1).child(0).text();
			tipo = element.child(2).child(0).child(0).child(0).child(0).text();
			anoRG = null;
			if(element.child(2).child(0).childNodeSize() >= 2 && element.child(2).child(0).child(2).childNodeSize() != 0) {
				String auxAno = element.child(2).child(0).child(2).child(0).child(0).text();
                if(auxAno.substring(auxAno.length()-4).matches("[0-9]+"))
                    anoRG = Integer.parseInt(auxAno.substring(auxAno.length()-4));
			}
			if(autores != null) {
				for(int j = 0; j < autores.childNodeSize(); j++) {
					if(!autores.child(j).text().contains("[")) {
						listaAutores += autores.child(j).text() + ", ";
					}
				}
				listaAutores = listaAutores.substring(0, listaAutores.length() - 2);
			}
			
		}
		else {
			url1 = ((org.jsoup.nodes.Element) element).child(0).child(0).child(0).attr("href");
			org.jsoup.nodes.Element autores = null;
			for(int i = 0; i < element.childNodeSize(); i++) {
				if(element.child(i).child(0).attr("class").contains("nova-e-list")) {
					autores = ((org.jsoup.nodes.Element) element).child(i).child(0);
				}
			}
			tituloResearchGate = element.child(0).child(0).text();
			tipo = element.child(1).child(0).child(0).child(0).child(0).text();
//			System.out.println("Nome: "+ element.className());
//			System.out.println("Filhos: "+ element.childNodeSize());
//			System.out.println("Texto: " + element.text());
//			for(int i = 0; i < element.childNodeSize(); i++) {
//				System.out.println("Texto do filho: " + element.child(i).text());
//				System.out.println("Classe do filho: " + element.child(i).className());
//			}
			anoRG = null;
			if(element.child(1).child(0).childNodeSize() > 2 && element.child(1).child(0).child(2).childNodeSize() != 0) {
				 String auxAno = element.child(1).child(0).child(2).child(0).child(0).text();
	             if(auxAno.substring(auxAno.length()-4).matches("[0-9]+"))
	            	 anoRG = Integer.parseInt(auxAno.substring(auxAno.length()-4));
			}
			if(autores != null) {
				for(int j = 0; j < autores.childNodeSize(); j++) {
					if(!autores.child(j).text().contains("[")) {
						listaAutores += autores.child(j).text() + ", ";
					}
				}
				listaAutores = listaAutores.substring(0, listaAutores.length() - 2);
			}
		}
		return tipo;
	}

	private static void inserir(Connection conn, org.jsoup.nodes.Element element) {
		 id++;
		 String url = "https://www.researchgate.net/";
		 try {
			 Thread.sleep(3000);
			 doc = getProxy(url+url1);
			 //doc = Jsoup.connect(url+url1).proxy("187.32.7.131", 80).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.140 Safari/537.36 Edge/17.17134").get();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		 Elements list = doc.getElementsByClass("publication-meta-secondary");
		 String doi = null;
		 String conferencia = null;
		 for (org.jsoup.nodes.Element element1 : list) { 
			 doi = element1.ownText();
			 if(element1.children().size() > 0 && element1.child(0).nodeName() == "span") {
				 if(element1.child(0).children().size() != 1) {
					 conferencia = element1.child(0).child(0).text(); //span tem elemento filho
				 }
				 else {
					 conferencia = element1.child(0).text(); //span sem filho
				 }
					 
			 }
		 }
		 if(conferencia == null) { //Caso nao ache conferencia logo acima
			 list = doc.getElementsByClass("publication-meta-journal");
			 if(list.size() > 1) { 
				 conferencia = list.get(1).text();
			 }
		 }
		 list = doc.getElementsByClass("nova-e-text nova-e-text--size-m nova-e-text--family-sans-serif nova-e-text--spacing-auto nova-e-text--color-inherit");
		 String _abstract = null;
		 if(!list.isEmpty())
			 _abstract = list.first().text();
		 list = doc.getElementsByClass("publication-meta-stats");
		 int reads = 0;
		 for (org.jsoup.nodes.Element element1 : list) { 
			 String[] aux = element1.text().split(" ");
			 if(aux[0].contains(","))
				 reads = Integer.parseInt(aux[0].replaceAll(",", ""));
			 else
				 reads = Integer.parseInt(aux[0]);
		 }
		 System.out.println("Artigo: " + tituloResearchGate);
		 System.out.println("Autores: "+ listaAutores);
		 System.out.println("Conferencia: "+ conferencia);
		 System.out.println("Tipo: "+ tipo);
		 System.out.println("DOI: "+ doi);
		 if(_abstract == null)
			 System.out.println("Sem abstract \n");
		 else
			 System.out.println("Tem abstract \n");
		 
		 try {
			PreparedStatement stmt = conn.prepareStatement("INSERT INTO researchgate (id, title, authors, conference, _year, _event, doi, abstract, id_prof, reads) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			stmt.setInt(1, id);
			stmt.setString(2, tituloResearchGate);
			stmt.setString(3,  listaAutores);
			stmt.setString(4, conferencia);
			if(anoRG == null)
				stmt.setNull(5, java.sql.Types.INTEGER);
			else
				stmt.setInt(5, anoRG);
			stmt.setString(6, tipo);
			stmt.setString(7, doi);
			stmt.setString(8, _abstract);
			stmt.setInt(9, idProf);
			stmt.setInt(10, reads);
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
			myRs = myStmt.executeQuery("SELECT EXISTS(select * from researchgate where id_prof="+ idProf  +")");
    		myRs.next();
    		if(myRs.getBoolean(1)) //Se ja fez o parsing do professor pula para outro
    			return null;
			return linkRG;
    	} catch (SQLException e) {
    		e.printStackTrace(); //Não pode criar arquivo
    	}
		return null;
	}

	private static boolean procurarObra(org.jsoup.nodes.Element element, String tituloLattes) {
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
			 	if(element.child(0).child(0).child(0).attr("class").contains("nova-c-image-strip")) { //Artigo para download
					org.jsoup.nodes.Element autores = ((org.jsoup.nodes.Element) element).child(3).child(0);
					for(int j = 0; j < autores.childNodeSize(); j++) {
						if(!autores.child(j).text().contains("[")) {
							String[] autoresAux = autores.child(j).text().split(" ");
							String sobrenome = autoresAux[autoresAux.length-1].toLowerCase();
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
					}
			    	 if(achouSobrenome == autoresLattes.getLength())
			    		 return true;
				}
				else {
					org.jsoup.nodes.Element autores = ((org.jsoup.nodes.Element) element).child(2).child(0);
					for(int j = 0; j < autores.childNodeSize(); j++) {
						if(!autores.child(j).text().contains("[")) {
							String[] autoresAux = autores.child(j).text().split(" ");
							String sobrenome = autoresAux[autoresAux.length-1].toLowerCase();
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
					}
			    	 if(achouSobrenome == autoresLattes.getLength())
			    		 return true;
				}
		 }
		 }
		return false;
	}
	
	private static boolean iterarArtigos(NodeList artigo, org.jsoup.nodes.Element element) {
		for(int a = 0; a < artigo.getLength(); a++) {
			//Verificar se o artigo contido no GS também esta no lattes
			rootLattes = artigo.item(a); //ARTIGOS
 			Element l = (Element) rootLattes;
 			Element dadosBasicos = (Element) l.getElementsByTagName("DADOS-BASICOS-DO-ARTIGO").item(0); //DADOS BASICOS
 			tituloLattes = dadosBasicos.getAttribute("TITULO-DO-ARTIGO");
 			anoLattes = Integer.parseInt(dadosBasicos.getAttribute("ANO-DO-ARTIGO"));
			 if(procurarObra(element, tituloLattes))
				 return true;
		}	
		return false;
	}
	
	private static boolean iterarTrabalhos(NodeList trabalho, org.jsoup.nodes.Element element) {
		for(int a = 0; a < trabalho.getLength(); a++) {
			//Verificar se o trabalho contido no GS também esta no lattes
			rootLattes = trabalho.item(a); //ARTIGOS
 			Element l = (Element) rootLattes;
 			Element dadosBasicos = (Element) l.getElementsByTagName("DADOS-BASICOS-DO-TRABALHO").item(0); //DADOS BASICOS
 			tituloLattes = dadosBasicos.getAttribute("TITULO-DO-TRABALHO");
 			anoLattes = Integer.parseInt(dadosBasicos.getAttribute("ANO-DO-TRABALHO"));
			 if(procurarObra(element, tituloLattes))
				 return true;
		}	
		return false;
	}
	
	private static boolean iterarLivros(NodeList livro, org.jsoup.nodes.Element element) {
		for(int a = 0; a < livro.getLength(); a++) {
			//Verificar se o livro contido no GS também esta no lattes
			rootLattes = livro.item(a);
 			Element l = (Element) rootLattes;
 			Element dadosBasicos = (Element) l.getElementsByTagName("DADOS-BASICOS-DO-LIVRO").item(0); //DADOS BASICOS
 			tituloLattes = dadosBasicos.getAttribute("TITULO-DO-LIVRO");
 			anoLattes = Integer.parseInt(dadosBasicos.getAttribute("ANO"));
			 if(procurarObra(element, tituloLattes))
				 return true;
		}	
		return false;
	}
	
	private static boolean iterarCapitulos(NodeList capitulo, org.jsoup.nodes.Element element) {
		for(int a = 0; a < capitulo.getLength(); a++) {
			//Verificar se o capitulo do livro contido no GS também esta no lattes
			rootLattes = capitulo.item(a); //ARTIGOS
 			Element l = (Element) rootLattes;
 			Element dadosBasicos = (Element) l.getElementsByTagName("DADOS-BASICOS-DO-CAPITULO").item(0); //DADOS BASICOS
 			tituloLattes = dadosBasicos.getAttribute("TITULO-DO-CAPITULO-DO-LIVRO");
 			anoLattes = Integer.parseInt(dadosBasicos.getAttribute("ANO"));
			 if(procurarObra(element, tituloLattes))
				 return true;
		}	
		return false;
	}
	
	private static boolean iterarOutrasProducoes(NodeList outraproducao, org.jsoup.nodes.Element element) {
		for(int a = 0; a < outraproducao.getLength(); a++) {
			//Verificar se "OutraProducao" contido no GS também esta no lattes
			rootLattes = outraproducao.item(a); //ARTIGOS
 			Element l = (Element) rootLattes;
 			Element dadosBasicos = (Element) l.getElementsByTagName("DADOS-BASICOS-DE-OUTRA-PRODUCAO").item(0); //DADOS BASICOS
 			tituloLattes = dadosBasicos.getAttribute("TITULO");
 			anoLattes = Integer.parseInt(dadosBasicos.getAttribute("ANO"));
			 if(procurarObra(element, tituloLattes))
				 return true;
		}	
		return false;
	}
		
	}