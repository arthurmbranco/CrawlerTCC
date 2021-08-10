package model;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import view.GUI;

public class Dblp {
	static String tituloLattes;
	static int anoLattes;
	static int anoDBLP;
	static int volumeLattes;
	static int pgInicLattes;
	static int pgFinalLattes;
	static int volume;
	static boolean existe;
	static String tituloDBLP;
	static NodeList autores;
	static Node rootDBLP;
	static Node rootLattes;
	static int idProf;
	static ResultSet myRs;
	static info.debatty.java.stringsimilarity.MetricLCS lcs = new info.debatty.java.stringsimilarity.MetricLCS();
	
	public static void parse(Connection conn, Statement myStmt) {
		
		try {
			
	    	File folder = new File("files/lattes/");
	    	File[] listOfFiles = folder.listFiles();
	    	for (int k = 0; k < listOfFiles.length; k++) {
	    	GUI.incrementarBarraDBLP();
	    	if (listOfFiles[k].isFile()) {
	    	existe = true;
    		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    		Document lattes = dBuilder.parse(new File("files/lattes/" + listOfFiles[k].getName()));

			// Dados Lattes
    		
    		NodeList curriculo = lattes.getElementsByTagName("CURRICULO-VITAE");
    		NodeList dadosGerais = ((Element)curriculo.item(0)).getElementsByTagName("DADOS-GERAIS");
    		NodeList producao = ((Element)curriculo.item(0)).getElementsByTagName("PRODUCAO-BIBLIOGRAFICA");
    		NodeList livrosecapitulos = ((Element)curriculo.item(0)).getElementsByTagName("LIVROS-E-CAPITULOS");
    		
    		//NodeList demaistipos = ((Element)curriculo.item(0)).getElementsByTagName("DEMAIS-TIPOS-DE-PRODUCAO-BIBLIOGRAFICA");
			
    		// Montar documento dblp a partir do nome do lattes
    		
    		// Pesquisar a URL no Banco
    		rootLattes = dadosGerais.item(0);
    		Element l = (Element) rootLattes;
			String nome = l.getAttribute("NOME-COMPLETO");
			String docDBLP = docDBLP(nome, myStmt);
    		if(existe) {
    		
    		Document dblp = dBuilder.parse(new File(docDBLP));
    		
    		System.out.println("DBLP PARSING PROFESSOR: "+nome);

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
//    		NodeList outraproducao = null;
//    		if(demaistipos.getLength() != 0)
//    			outraproducao = ((Element)demaistipos.item(0)).getElementsByTagName("OUTRA-PRODUCAO-BIBLIOGRAFICA");
			
    		NodeList livro = null;
    		NodeList capitulo = null;

			if(livros != null && livros.getLength() != 0) {
				livro =  ((Element)livros.item(0)).getElementsByTagName("LIVRO-PUBLICADO-OU-ORGANIZADO");
			}
			if(capitulos != null && capitulos.getLength() != 0) {
				capitulo =  ((Element)capitulos.item(0)).getElementsByTagName("CAPITULO-DE-LIVRO-PUBLICADO");
			}
			//Dados DBLP
			NodeList dblpperson = dblp.getElementsByTagName("dblpperson");
    		NodeList r =  ((Element)dblpperson.item(0)).getElementsByTagName("r");
    		
			boolean artigoEncontrado = false;
			int contador = 0;
			int cont = 0;
			for (int i = 0; i < r.getLength(); i++) {
				cont++;
				Node n = r.item(i);
	       		rootDBLP = n.getFirstChild();
	    		
				 if(rootDBLP.getNodeName() == "article") { //Tratar artigos
					 Element t = (Element) rootDBLP;
			         Element ano = (Element) t.getElementsByTagName("year").item(0);
			         Element titulo = (Element) t.getElementsByTagName("title").item(0);
			         Element vol = (Element) t.getElementsByTagName("volume").item(0);
			         Element pages = (Element) t.getElementsByTagName("pages").item(0);
			         String aux = "";
						if(pages != null)
							aux = pages.getTextContent();
					 if(vol.getTextContent().matches("[0-9]+"))
						 volume = Integer.parseInt(vol.getTextContent());
					 else {
						 volume = -999;
					 }
		           	 autores = t.getElementsByTagName("author");
					 tituloDBLP = titulo.getTextContent();
					 anoDBLP = Integer.parseInt(ano.getTextContent());
					 tituloDBLP = tituloDBLP.toLowerCase();

					 if(iterarArtigos(artigo, aux))
						 artigoEncontrado = true;
					 if(iterarTrabalhosInProceedings(trabalho, aux))
						 artigoEncontrado = true; 
					 if(livro != null && iterarLivros(livro))
						 artigoEncontrado = true; 
					 if(capitulo != null && iterarCapitulos(capitulo))
						 artigoEncontrado = true; 
				 }
				 else if(rootDBLP.getNodeName() == "inproceedings") {
					 //Tratar trabalhos de congressos
					 	Element t = (Element) rootDBLP;
					 	Element ano = (Element) t.getElementsByTagName("year").item(0);
					 	Element titulo = (Element) t.getElementsByTagName("title").item(0);
					 	Element pages = (Element) t.getElementsByTagName("pages").item(0);
					 	autores = t.getElementsByTagName("author");
						tituloDBLP = titulo.getTextContent();
						tituloDBLP = tituloDBLP.toLowerCase();
						anoDBLP = Integer.parseInt(ano.getTextContent());
						String aux = "";
						if(pages != null)
							aux = pages.getTextContent();
						 if(iterarArtigos(artigo, aux))
							 artigoEncontrado = true; 
						 if(iterarTrabalhosProceedings(trabalho))
							 artigoEncontrado = true; 
						if(iterarTrabalhosInProceedings(trabalho, aux))
							 artigoEncontrado = true; 
						if(livro != null && iterarLivros(livro))
							 artigoEncontrado = true; 
						if(capitulo != null && iterarCapitulos(capitulo))
							 artigoEncontrado = true; 
				 }
				 else if(rootDBLP.getNodeName() == "proceedings") {
					 //Tratar livros publicados/edições
					 Element t = (Element) rootDBLP;
					 Element ano = (Element) t.getElementsByTagName("year").item(0);
					 Element titulo = (Element) t.getElementsByTagName("title").item(0);
					 autores = t.getElementsByTagName("editor");
					 tituloDBLP = titulo.getTextContent();
					 tituloDBLP = tituloDBLP.toLowerCase();
					 anoDBLP = Integer.parseInt(ano.getTextContent());
					 if(iterarTrabalhosProceedings(trabalho)) {
						 artigoEncontrado = true; 
					 }
					 if(livro != null && iterarLivros(livro)) {
						 artigoEncontrado = true; 
					 }
					 if(capitulo != null && iterarCapitulos(capitulo)) {
						 artigoEncontrado = true; 
					 }
				 }
				 else {
					 artigoEncontrado = true;
				 }
				 if(!artigoEncontrado) {
					 contador++;
					 //Artigo/Paper da DBLP nao encontrado no lattes, então tem que ser adicionado
					 System.out.println("Não achou o artigo na ordem > " + (i+1));
					 //Adicionar no banco os artigos da dblp não encontrados
					 inserir(conn);
				 }
				 artigoEncontrado = false;
			}
			
			System.out.println("NÃO FORAM ENCONTRADOS "+contador+" ARTIGOS DE UM TOTAL DE "+cont +"\n");
	    	      } 
	    	}
			}
		} //Se existir url dblp
			catch(Exception e) {
				e.printStackTrace();
			}
		
	}


	private static boolean iterarArtigos(NodeList artigo, String pages) {
		for(int a = 0; a < artigo.getLength(); a++) {
			//Verificar se o artigo contido na DBLP também esta no lattes
	         
	        rootLattes = artigo.item(a); //ARTIGOS
 			Element l = (Element) rootLattes;
 			Element dadosBasicos = (Element) l.getElementsByTagName("DADOS-BASICOS-DO-ARTIGO").item(0); //DADOS BASICOS
 			Element detalhamento = (Element) l.getElementsByTagName("DETALHAMENTO-DO-ARTIGO").item(0); //DADOS BASICOS
			 	
			 tituloLattes = dadosBasicos.getAttribute("TITULO-DO-ARTIGO");
			 anoLattes = Integer.parseInt(dadosBasicos.getAttribute("ANO-DO-ARTIGO"));
			 tituloLattes = tituloLattes.toLowerCase();
			 
			 
					 
			 if(lcs.distance(tituloLattes, tituloDBLP) < 0.3 ) { // Os titulos são muito similares logo os artigos são provavelmente os mesmos
				 return true;
			 }
			 else if(lcs.distance(tituloLattes, tituloDBLP) < 0.5){// Os titulos são similares e os anos iguais logo os artigos são provavelmente os mesmos
				 if(anoLattes == anoDBLP)
					 return true;
			 }
			 else if(lcs.distance(tituloLattes, tituloDBLP) < 0.8) { // Os titulos são bem diferentes mas todos os outros dados são iguais (Talvez o titulo esteja em ingles e no outro em portugues)
				if(pages != "" && !pages.contains(":")) {
				String aux = pages;
				String[] paginas = aux.split("-");
				int paginaInicial = Integer.parseInt(paginas[0]);
				int paginaFinal = -500;
				if(paginas.length > 1)
					paginaFinal = Integer.parseInt(paginas[1]);
				if(detalhamento.getAttribute("VOLUME").matches("[0-9]+"))
					volumeLattes = Integer.parseInt(detalhamento.getAttribute("VOLUME"));
				if(!detalhamento.getAttribute("PAGINA-INICIAL").isEmpty() && detalhamento.getAttribute("PAGINA-INICIAL").matches("[0-9]+")) //Nao esta vazio e contem só numeros
					pgInicLattes =  Integer.parseInt(detalhamento.getAttribute("PAGINA-INICIAL"));
				if(!detalhamento.getAttribute("PAGINA-FINAL").isEmpty() && detalhamento.getAttribute("PAGINA-FINAL").matches("[0-9]+"))//Nao esta vazio e contem só numeros
					pgFinalLattes =  Integer.parseInt(detalhamento.getAttribute("PAGINA-FINAL"));
				if(volume == -999) {
					if(anoLattes == anoDBLP && paginaInicial == pgInicLattes && paginaFinal == pgFinalLattes)
						return true;
				}
				else {
					if(anoLattes == anoDBLP && volumeLattes == volume && paginaInicial == pgInicLattes && paginaFinal == pgFinalLattes)
						return true;
				}
				}
				
			 }
	}
		return false;
  }
	
	private static boolean iterarTrabalhosInProceedings(NodeList trabalho, String aux) {
		for(int a = 0; a < trabalho.getLength(); a++) {
			//Verificar se o artigo contido na DBLP também esta no lattes
			rootLattes = trabalho.item(a); //ARTIGOS
 			Element l = (Element) rootLattes;
 			Element dadosBasicos = (Element) l.getElementsByTagName("DADOS-BASICOS-DO-TRABALHO").item(0); //DADOS BASICOS
 			Element detalhamento = (Element) l.getElementsByTagName("DETALHAMENTO-DO-TRABALHO").item(0); //DETALHAMENTO	 
			tituloLattes = dadosBasicos.getAttribute("TITULO-DO-TRABALHO");
			anoLattes = Integer.parseInt(dadosBasicos.getAttribute("ANO-DO-TRABALHO"));
			tituloLattes = tituloLattes.toLowerCase();

			 if(lcs.distance(tituloLattes, tituloDBLP) < 0.3 ) { // Os titulos são muito similares logo os artigos são provavelmente os mesmos
				 return true;
			 }
			 else if(lcs.distance(tituloLattes, tituloDBLP) < 0.5){// Os titulos são similares e os anos iguais logo os artigos são provavelmente os mesmos
				 if(anoLattes == anoDBLP)
					 return true;
			 }
			 else if(lcs.distance(tituloLattes, tituloDBLP) < 0.8) { // Os titulos são bem diferentes mas todos os outros dados são iguais (Talvez o titulo esteja em ingles e no outro em portugues)
				 
				String[] paginas = aux.split("-");
				if(paginas.length > 1 &&  !aux.contains(":")) {
				int paginaInicial =Integer.parseInt(paginas[0]);
				int paginaFinal =Integer.parseInt(paginas[1]);
				if(!detalhamento.getAttribute("PAGINA-INICIAL").isEmpty() && detalhamento.getAttribute("PAGINA-INICIAL").matches("[0-9]+"))
					pgInicLattes =  Integer.parseInt(detalhamento.getAttribute("PAGINA-INICIAL"));
				if(!detalhamento.getAttribute("PAGINA-FINAL").isEmpty() && detalhamento.getAttribute("PAGINA-FINAL").matches("[0-9]+"))
					pgFinalLattes = Integer.parseInt(detalhamento.getAttribute("PAGINA-FINAL"));
				if(anoLattes == anoDBLP && paginaInicial == pgInicLattes && paginaFinal == pgFinalLattes)
					return true;
				}
				else {
				    if(anoLattes == anoDBLP) { //Aqui achou todos os autores, ano é o mesmo, e o texto é semi parecido então o artigo é o mesmo
		           		
		           		NodeList autoresLattes = l.getElementsByTagName("AUTORES");
						 
					 	String[] separar;
					 	String autorLattes;
					 	String autorDBLP;
					 	int achouSobrenome = 0;
					     for (int x=0; x < autores.getLength() ; x++) { // Procurando pelos sobrenomes para comparar com o Lattes
					    	 Element autor = (Element) autores.item(x);
					    	 String auxiliar = (String) autor.getTextContent();
					    	 separar = (auxiliar.replaceAll("[0-9]","")).split(" "); //Tem um sobrenome bugado com numeros no nome
					    	 autorDBLP = separar[separar.length-1];
					    	 autorDBLP = autorDBLP.toLowerCase();
					    	 for(int i = 0; i < autoresLattes.getLength(); i++ ) {
				    			Element aux2 = (Element) autoresLattes.item(i);
				    			autorLattes = aux2.getAttribute("NOME-COMPLETO-DO-AUTOR");

					    		 autorLattes = autorLattes.toLowerCase();
					    		 if(autorLattes.contains(autorDBLP)) { //Se achar o sobrenome de um autor da DBLP no Lattes então vai para o proximo autor
					    			 achouSobrenome++;
					    			 break;
					    		 }	 
					    	
					    	 }
					     }
				    	 if(achouSobrenome == autoresLattes.getLength())
				    		 return true;
					 } 
				}
			 }
	   }
	return false;
	}
	
	private static boolean iterarTrabalhosProceedings(NodeList trabalho) {
		for(int a = 0; a < trabalho.getLength(); a++) {
			//Verificar se o artigo contido na DBLP também esta no lattes
			rootLattes = trabalho.item(a); //ARTIGOS
 			Element l = (Element) rootLattes;
 			Element dadosBasicos = (Element) l.getElementsByTagName("DADOS-BASICOS-DO-TRABALHO").item(0); //DADOS BASICOS
			tituloLattes = dadosBasicos.getAttribute("TITULO-DO-TRABALHO");
			anoLattes = Integer.parseInt(dadosBasicos.getAttribute("ANO-DO-TRABALHO"));
			tituloLattes = tituloLattes.toLowerCase();
			 
			 if(lcs.distance(tituloLattes, tituloDBLP) < 0.3 ) { // Os titulos são muito similares logo os artigos são provavelmente os mesmos
				 return true;
			 }
			 else if(lcs.distance(tituloLattes, tituloDBLP) < 0.5){// Os titulos são similares e os anos iguais logo os artigos são provavelmente os mesmos
				 if(anoLattes == anoDBLP)
					 return true;
			 }
			 else if(lcs.distance(tituloLattes, tituloDBLP) < 0.8) { // Os titulos são bem diferentes mas todos os outros dados são iguais (Talvez o titulo esteja em ingles e no outro em portugues)
				    if(anoLattes == anoDBLP) { //Aqui achou todos os autores, ano é o mesmo, e o texto é semi parecido então o artigo é o mesmo
		           		NodeList autoresLattes = l.getElementsByTagName("AUTORES");
					 	String[] separar;
					 	String autorLattes;
					 	String autorDBLP;
					 	int achouSobrenome = 0;
					     for (int x=0; x < autores.getLength() ; x++) { // Procurando pelos sobrenomes para comparar com o Lattes
					    	 Element autor = (Element) autores.item(x);
					    	 separar = (String[]) autor.getTextContent().split(" ");
					    	 autorDBLP = separar[separar.length-1];
					    	 autorDBLP = autorDBLP.toLowerCase();
					    	 for(int i = 0; i < autoresLattes.getLength(); i++ ) {
					    		 Element aux2 = (Element) autoresLattes.item(i);
					    		 autorLattes = aux2.getAttribute("NOME-COMPLETO-DO-AUTOR");
					    		 autorLattes = autorLattes.toLowerCase();
					    		 if(autorLattes.contains(autorDBLP)) { //Se achar o sobrenome de um autor da DBLP no Lattes então vai para o proximo autor
					    			 achouSobrenome++;
					    			 break;
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
	
	private static boolean iterarLivros(NodeList livro) {
		for(int a = 0; a < livro.getLength(); a++) {
			//Verificar se o artigo contido na DBLP também esta no lattes
			
			rootLattes = livro.item(a); //ARTIGOS
	 		Element l = (Element) rootLattes;
	 		Element dadosBasicos = (Element) l.getElementsByTagName("DADOS-BASICOS-DO-LIVRO").item(0); //DADOS BASICOS
			tituloLattes = dadosBasicos.getAttribute("TITULO-DO-LIVRO");
			anoLattes = Integer.parseInt(dadosBasicos.getAttribute("ANO"));
		    tituloLattes = tituloLattes.toLowerCase();
			 
					 
			 if(lcs.distance(tituloLattes, tituloDBLP) < 0.3 ) { // Os titulos são muito similares logo os artigos são provavelmente os mesmos
				 return true;
			 }
			 else if(lcs.distance(tituloLattes, tituloDBLP) < 0.5){// Os titulos são similares e os anos iguais logo os artigos são provavelmente os mesmos
				 if(anoLattes == anoDBLP)
					 return true;
			 }
			 else if(lcs.distance(tituloLattes, tituloDBLP) < 0.8) { // Os titulos são bem diferentes mas todos os outros dados são iguais (Talvez o titulo esteja em ingles e no outro em portugues)	
				    if(anoLattes == anoDBLP) { //Aqui achou todos os autores, ano é o mesmo, e o texto é semi parecido então o artigo é o mesmo
		           		NodeList autoresLattes = l.getElementsByTagName("AUTORES");
					 	String[] separar;
					 	String autorLattes;
					 	String autorDBLP;
					 	int achouSobrenome = 0;
					     for (int x=0; x < autores.getLength() ; x++) { // Procurando pelos sobrenomes para comparar com o Lattes
					    	 Element autor = (Element) autores.item(x);
					    	 separar = (String[]) autor.getTextContent().split(" ");
					    	 autorDBLP = separar[separar.length-1];
					    	 autorDBLP = autorDBLP.toLowerCase();
					    	 for(int i = 0; i < autoresLattes.getLength(); i++ ) {
					    		 Element aux2 = (Element) autoresLattes.item(i);
						    	 autorLattes = aux2.getAttribute("NOME-COMPLETO-DO-AUTOR");
						    	 autorLattes = autorLattes.toLowerCase();
					    		 if(autorLattes.contains(autorDBLP)) { //Se achar o sobrenome de um autor da DBLP no Lattes então vai para o proximo autor
					    			 achouSobrenome++;
					    			 break;
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
	
	private static boolean iterarCapitulos(NodeList capitulo) {
		for(int a = 0; a < capitulo.getLength(); a++) {
			//Verificar se o artigo contido na DBLP também esta no lattes
	         
	         rootLattes = capitulo.item(a); //ARTIGOS
		 	 Element l = (Element) rootLattes;
		 	 Element dadosBasicos = (Element) l.getElementsByTagName("DADOS-BASICOS-DO-CAPITULO").item(0); //DADOS BASICOS
		 	 tituloLattes = dadosBasicos.getAttribute("TITULO-DO-CAPITULO-DO-LIVRO");
		 	 anoLattes = Integer.parseInt(dadosBasicos.getAttribute("ANO"));
		 	 tituloLattes = tituloLattes.toLowerCase();
			 
					 
			 if(lcs.distance(tituloLattes, tituloDBLP) < 0.3 ) { // Os titulos são muito similares logo os artigos são provavelmente os mesmos
				 return true;
			 }
			 else if(lcs.distance(tituloLattes, tituloDBLP) < 0.5){// Os titulos são similares e os anos iguais logo os artigos são provavelmente os mesmos
				 if(anoLattes == anoDBLP)
					 return true;
			 }
			 else if(lcs.distance(tituloLattes, tituloDBLP) < 0.8) { // Os titulos são bem diferentes mas todos os outros dados são iguais (Talvez o titulo esteja em ingles e no outro em portugues)	
				    if(anoLattes == anoDBLP) { //Aqui achou todos os autores, ano é o mesmo, e o texto é semi parecido então o artigo é o mesmo
					 	NodeList autoresLattes = l.getElementsByTagName("AUTORES");
					 	
					 	String[] separar;
					 	String autorLattes;
					 	String autorDBLP;
					 	int achouSobrenome = 0;
					     for (int x=0; x < autores.getLength() ; x++) { // Procurando pelos sobrenomes para comparar com o Lattes
					    	 Element autor = (Element) autores.item(x);
					    	 separar = (String[]) autor.getTextContent().split(" ");
					    	 autorDBLP = separar[separar.length-1];
					    	 autorDBLP = autorDBLP.toLowerCase();
					    	 for(int i = 0; i < autoresLattes.getLength(); i++ ) {
					    		 Element aux2 = (Element) autoresLattes.item(i);
						    	 autorLattes = aux2.getAttribute("NOME-COMPLETO-DO-AUTOR");
						    	 autorLattes = autorLattes.toLowerCase();
					    		 if(autorLattes.contains(autorDBLP)) { //Se achar o sobrenome de um autor da DBLP no Lattes então vai para o proximo autor
					    			 achouSobrenome++;
					    			 break;
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
	
	public static void inserir(Connection conn) {
		 Element t = (Element) rootDBLP;
		 String key = t.getAttribute("key");
		 String mdate = t.getAttribute("mdate");
		 DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		 java.sql.Date sqlDate = null;
		try {
			sqlDate = new java.sql.Date(df.parse(mdate).getTime());
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		 Element titulo = (Element) t.getElementsByTagName("title").item(0);
		 String event = rootDBLP.getNodeName();
		 NodeList autoreseditor = t.getElementsByTagName("editor");
		 String autores1 = "";
		 for (int x=0; x < autoreseditor.getLength() ; x++) { // Procurando pelos sobrenomes para comparar com o Lattes
				 Element autor = (Element) autoreseditor.item(x);
				 autores1 += autor.getTextContent();
				 if(autoreseditor.getLength()-1 != x)
					 autores1 += ",";
		}
		 NodeList autores = t.getElementsByTagName("author");
		 for (int x=0; x < autores.getLength() ; x++) { // Procurando pelos sobrenomes para comparar com o Lattes
				 Element autor = (Element) autores.item(x);
				 autores1 += autor.getTextContent();
				 if(autores.getLength()-1 != x)
					 autores1 += ",";
		}
		 
		Element pages = (Element) t.getElementsByTagName("pages").item(0);
		String paginas = null;
		if(pages != null) {
			paginas = pages.getTextContent();
		}
		String ee2 = null;
		String crossref2 = null;
		String booktitle2 = null;
		String publisher2 = null;
		String isbn2 = null;
		Integer year2 = null;
		String volume2 = null;
		String journal2 = null;
		String number2 = null;
		String url2 = null;
		Element year = (Element) t.getElementsByTagName("year").item(0);
		if(year != null) {
			year2 = Integer.parseInt(year.getTextContent());
		}
		Element volume = (Element) t.getElementsByTagName("volume").item(0);
		if(volume != null) {
			volume2 = volume.getTextContent();
		}
		Element journal = (Element) t.getElementsByTagName("journal").item(0);
		if(journal != null) {
			journal2 = journal.getTextContent();
		}
		Element number = (Element) t.getElementsByTagName("number").item(0);
		if(number != null) {
			number2 = number.getTextContent();
		}
		Element url = (Element) t.getElementsByTagName("url").item(0);
		if(url != null) {
			url2 = url.getTextContent();
		}
		Element ee = (Element) t.getElementsByTagName("ee").item(0);
		if(ee != null) {
			ee2 = ee.getTextContent();
		}
		Element crossref = (Element) t.getElementsByTagName("crossref").item(0);
		if(crossref != null) {
			crossref2 = crossref.getTextContent();
		}
		Element booktitle = (Element) t.getElementsByTagName("booktitle").item(0);
		if(booktitle != null) {
			booktitle2 = booktitle.getTextContent();
		}
		Element publisher = (Element) t.getElementsByTagName("publisher").item(0);
		if(publisher != null) {
			publisher2 = publisher.getTextContent();
		}
		Element isbn = (Element) t.getElementsByTagName("isbn").item(0);
		if(isbn != null) {
			isbn2 = isbn.getTextContent();
		}
		try {
			PreparedStatement stmt = conn.prepareStatement("INSERT INTO dblp (_event, title, authors, pages, _year, volume, journal, _number, ee, url, _key, _mdate, booktitle, publisher, isbn, crossref, id_prof) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			stmt.setString(1, event);
			stmt.setString(2,  titulo.getTextContent());
			stmt.setString(3, autores1);
			stmt.setString(4, paginas);
			stmt.setInt(5, year2);
			stmt.setString(6, volume2);
			stmt.setString(7, journal2);
			stmt.setString(8, number2);
			stmt.setString(9, ee2);
			stmt.setString(10, url2);
			stmt.setString(11, key);
			stmt.setDate(12, sqlDate);
			stmt.setString(13, booktitle2);
			stmt.setString(14, publisher2);
			stmt.setString(15, isbn2);
			stmt.setString(16, crossref2);
			stmt.setInt(17, idProf);
			stmt.executeUpdate();
			conn.commit();
		 
		 } catch (SQLException e) {
				 // TODO Auto-generated catch block
				 e.printStackTrace();
		}
	}
	
	public static String docDBLP(String nome, Statement myStmt) {
		String doc = "";
		String nomeAux = nome;
		System.out.println(nomeAux);
		try {
    		File folder = new File("files/dblp/");
	    	File[] listOfFiles = folder.listFiles();
	    	nome = nome.replaceAll(" ", "_").toLowerCase();
	    	doc = "files/dblp/"+ nome + "-dblp.xml";
	    	
	    	for (int i = 0; i < listOfFiles.length; i++) { //Pesquisa se o arquivo ja foi criado e retorna apenas o nome
	    		if (listOfFiles[i].isFile() && listOfFiles[i].getName() == doc) {
	    			return listOfFiles[i].getName();
	    		}
	    	}

	    	try {
				myRs = myStmt.executeQuery("SELECT linkdblp, id FROM Professores WHERE nome LIKE " +"'"+ nomeAux + "'");
				String link = "";
				while(myRs.next()) {
					idProf =  myRs.getInt("id");
					link = myRs.getString("linkdblp");
				}
				if(link == null) {
					existe = false;
				}
				else { //Se nao existir URL nao cria arquivo
		    	//Se não achou o nome cria o arquivo
	    		byte[] bytes = Jsoup.connect(link).ignoreContentType(true).execute().bodyAsBytes();
	    		FileOutputStream fos = new FileOutputStream(new File(doc));
	    		fos.write(bytes);
	    		fos.close();
				}
	    	} catch (SQLException e) {
	    		e.printStackTrace(); //Não pode criar arquivo
	    	}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); //Problemas na query sql
		}
		return doc;
	}
}
