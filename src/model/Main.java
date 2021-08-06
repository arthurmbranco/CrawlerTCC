package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringWriter;
import java.sql.*;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import view.GUI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class Main {
	static String url = "jdbc:postgresql://localhost:5432/crawler";
	static String user = "postgres";
	static String password = "admin";
	
    public static void main(String[] args) throws Exception {

    	try {
    		Connection conn = DriverManager.getConnection(url, user, password);
    		conn.setAutoCommit(false);
    		Statement myStmt =  conn.createStatement();
    		
    		File file = new File("files/proxy80.txt"); 		  
    		BufferedReader br = new BufferedReader(new FileReader(file)); 
    		ArrayList<String> list = new ArrayList<String>();
    		String st; 
    		while ((st = br.readLine()) != null) {
    		   list.add(st);
    		} 
    		
    		checarNovosCurriculos(conn, myStmt);
    		GUI.init(conn, myStmt);
    		myStmt.executeUpdate("DELETE FROM dblp");
    		myStmt.executeUpdate("DELETE FROM googlescholar");
    		myStmt.executeUpdate("DELETE FROM researchgate");
    		conn.commit();
    		Dblp.parse(conn, myStmt);
    		GoogleScholar.parse(conn, myStmt);
    		ResearchGate.parse(conn, myStmt);
    		br.close();
    		
    	} catch(Exception e){
    		e.printStackTrace();
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
			if(myRs.next())
				id = myRs.getInt("id");
			
		} catch (SQLException e1) {
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
				System.out.println("Os dados do professor '"+ nome+ "' foram adicionados a tabela professores!");
				id++;
				PreparedStatement stmt = conn.prepareStatement("INSERT INTO professores (id, nome, lattes) VALUES (?, ?, XML(?))");
				stmt.setInt(1, id);
				stmt.setString(2,  nome);
				stmt.setString(3,  toString(lattes));
				stmt.executeUpdate();
				conn.commit();
			}
			
			} catch(Exception e) {
				 e.printStackTrace();
			}
    	}
    }
    
    public static String toString(Document doc) {
    	 try {
    	       DOMSource domSource = new DOMSource(doc);
    	       StringWriter writer = new StringWriter();
    	       StreamResult result = new StreamResult(writer);
    	       TransformerFactory tf = TransformerFactory.newInstance();
    	       Transformer transformer = tf.newTransformer();
    	       transformer.transform(domSource, result);
    	       return writer.toString();
    	 } catch(TransformerException ex) {
    	       ex.printStackTrace();
    	       return null;
    	 }
    }

}