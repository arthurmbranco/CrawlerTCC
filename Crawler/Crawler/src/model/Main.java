package model;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import view.GUI;
import java.io.InputStreamReader;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class Main {
	static String url = "jdbc:postgresql://localhost:5432/crawler";
	static String user = "postgres";
	static String password = "admin";
	
    public static void main(String[] args) throws IOException {

    	try {
    		Connection conn = DriverManager.getConnection(url, user, password);
    		conn.setAutoCommit(false);
    		Statement myStmt =  conn.createStatement();
    		
//    		File file = new File("files/proxy80.txt"); 		  
//    		BufferedReader br = new BufferedReader(new FileReader(file)); 
//    		ArrayList<String> list = new ArrayList<String>();
//    		String st; 
//    		while ((st = br.readLine()) != null) {
//    		   list.add(st);
//    		} 
//    		
//    		int index = (int) (Math.random()*list.size()-1);
//    		String[] aux = list.get(index).split(":");
//    		String ip = aux[0];
//    		int porta = Integer.parseInt(aux[1]);
//    		org.jsoup.nodes.Document doc = null;
//    		boolean alive = false;
//    		while(!alive) {
//    		try {
//    			System.out.println(list.get(index));
//    			doc = Jsoup.connect("https://www.meuip.com.br").proxy(ip, porta).timeout(5000).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.81 Safari/537.36").get();
//    			System.out.println(doc.getElementsByTag("h1").text());
//    			alive = true;
//    		}catch(IOException ioe){
//    			list.remove(index);
//    			index = (int) (Math.random()*list.size()-1);
//        		aux = list.get(index).split(":");
//        		ip = aux[0];
//        		porta = Integer.parseInt(aux[1]);
//    			System.out.println("Falhou!");
//            }
//    		}
    		
    		//checarNovosCurriculos(conn, myStmt);
    		GUI.init(conn, myStmt);
    		//myStmt.executeUpdate("DELETE FROM dblp");
    		//myStmt.executeUpdate("DELETE FROM googlescholar");
    		//myStmt.executeUpdate("DELETE FROM researchgate");
    		//conn.commit();
    		//Dblp.parse(conn, myStmt);
    		//GoogleScholar.parse(conn, myStmt);
    		//ResearchGate.parse(conn, myStmt);
    		
    	}
    	catch(Exception exc){
    		exc.printStackTrace();
    	}

    }


	public static void checarNovosCurriculos(Connection conn, Statement myStmt) {
    	File folder = new File("files/lattes/");
    	File[] listOfFiles = folder.listFiles();    
    	ResultSet myRs;
    	int id = 0;
		try {
			myRs = myStmt.executeQuery("SELECT * FROM professores\n" + 
					"WHERE id = (\n" + 
					"    SELECT MAX(id) FROM professores)");
			if(myRs.next()) {
				id = myRs.getInt("id");
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
    	for (int k = 0; k < listOfFiles.length; k++) {
    		try {
    		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    		org.w3c.dom.Document lattes = dBuilder.parse(new File("files/lattes/" + listOfFiles[k].getName()));
    		
    		//Nome do professor
    		NodeList curriculo = lattes.getElementsByTagName("CURRICULO-VITAE");
    		NodeList dadosGerais = ((Element)curriculo.item(0)).getElementsByTagName("DADOS-GERAIS");
    		// Pesquisar a URL no Banco
    		Node rootLattes = dadosGerais.item(0);
    		Element l = (Element) rootLattes;
			String nome = l.getAttribute("NOME-COMPLETO");
			myRs = myStmt.executeQuery("SELECT nome FROM professores WHERE nome = "+ "'"+nome+"'");
			if(!myRs.next()) {
				System.out.println("Os dados do professor '"+ nome+ "' foram adicionado a tabela professores!");
				id++;
				PreparedStatement stmt = conn.prepareStatement("INSERT INTO professores (id, nome, lattes) VALUES (?, ?, XML(?))");
				stmt.setInt(1, id);
				stmt.setString(2,  nome);
				stmt.setString(3,  toString(lattes));
				stmt.executeUpdate();
				conn.commit();
			}
			 } catch(SQLException e) {
				 e.printStackTrace();
			 } catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    public static String toString(Document doc) {
    	 try
    	    {
    	       DOMSource domSource = new DOMSource(doc);
    	       StringWriter writer = new StringWriter();
    	       StreamResult result = new StreamResult(writer);
    	       TransformerFactory tf = TransformerFactory.newInstance();
    	       Transformer transformer = tf.newTransformer();
    	       transformer.transform(domSource, result);
    	       return writer.toString();
    	    }
    	    catch(TransformerException ex)
    	    {
    	       ex.printStackTrace();
    	       return null;
    	    }
    }

}