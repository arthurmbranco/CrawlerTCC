package view;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import model.*;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.JTabbedPane;
import javax.swing.JEditorPane;
import javax.swing.JTextPane;
import javax.swing.JProgressBar;

public class GUI extends JFrame {

	private static final long serialVersionUID = -8854175370771261994L;
	private JPanel contentPane;
	private JCheckBox dblp;
	private JCheckBox rg;
	private JCheckBox gs;
	private JButton pesquisar;
	private static Statement myStmt;
	private static Connection conn;
	private JList list;
	private JTabbedPane tabbedPane;
	private JTextPane textPane;
	private JTextPane textPane_1;
	private JTextPane textPane_2;
	private static SimpleAttributeSet normal;
	private static SimpleAttributeSet bold;
	private JTextPane textPane_3;
	private JTextPane textPane_4;
	private JTextPane textPane_5;
	private JTextPane textPane_6;
	private static JProgressBar progressBar;
	private static JProgressBar progressBar_1;
	private static JProgressBar progressBar_2;

	/**
	 * Launch the application.
	 * @param myStmt 
	 * @param conn 
	 */
	public static void init(Connection c, Statement m) {
		conn = c;
		myStmt = m;
		normal = new SimpleAttributeSet();
		StyleConstants.setFontFamily(normal, "Tahoma");
		StyleConstants.setFontSize(normal, 11);
		bold = new SimpleAttributeSet(normal);
		StyleConstants.setBold(bold, true);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI frame = new GUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GUI() {
		setResizable(false);
		initComponents();
		createEvents();
	}
	
	private void createEvents() {
		pesquisar.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) { // Clique no botão de pesquisa
				progressBar.setValue(0);
				progressBar_1.setValue(0);
				progressBar_2.setValue(0);
				if(dblp.isSelected()) {
					try {
						myStmt.executeUpdate("DELETE FROM dblp");
						conn.commit();
					} catch (SQLException e) {
						e.printStackTrace();
					}
		    		Dblp.parse(conn, myStmt);
				}
				if(rg.isSelected()) {
					try {
						myStmt.executeUpdate("DELETE FROM researchgate");
						conn.commit();
					} catch (SQLException e) {
						e.printStackTrace();
					}
					ResearchGate.parse(conn, myStmt);
				}
				if(gs.isSelected()) {
					try {
						myStmt.executeUpdate("DELETE FROM googlescholar");
						conn.commit();
					} catch (SQLException e) {
						e.printStackTrace();
					}
					GoogleScholar.parse(conn, myStmt);
				}
			}
		});
		
		list.addMouseListener(new MouseAdapter() { // Evento de duplo clique na lista de professores
			@Override
			public void mouseClicked(MouseEvent arg0) {
				 JList list = (JList)arg0.getSource();
			        if (arg0.getClickCount() == 2) {
			            // Double-click detected
			        	popularViewObrasRecuperadasProfessor();
			        	//JOptionPane.showMessageDialog(null, list.getSelectedValue());
			        }
			}
		});
	}
	
	private void initComponents() {
		setTitle("TCC - Crawler");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1080, 800);
		contentPane = new JPanel();
		contentPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		setContentPane(contentPane);
		
		dblp = new JCheckBox("DBLP");
		
		rg = new JCheckBox("ResearchGate");
		
		gs = new JCheckBox("GoogleScholar");
		
		JLabel lblNewLabel = new JLabel("Selecione os repositorios que deseja pesquisar.");
		
		pesquisar = new JButton("Pesquisar");
		
		JLabel lblNewLabel_1 = new JLabel("Obras do Lattes:");
		
		JLabel lblNewLabel_2 = new JLabel("Obras recuperadas de outros repositorios:");
		
		JScrollPane scrollPane_2 = new JScrollPane();
		
		JLabel lblSelecioneOProfessor = new JLabel("Selecione o professor do qual se deseja mostrar as obras.");
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		
		JTabbedPane tabbedPane_1 = new JTabbedPane(JTabbedPane.TOP);
		
		int max = 0;
		ResultSet myRs;
		try {
			myRs = myStmt.executeQuery("SELECT COUNT(*) FROM professores");
			if(myRs.next()) {
				max = myRs.getInt("count");
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setMaximum(max);
		
		progressBar_1 = new JProgressBar();
		progressBar_1.setStringPainted(true);
		progressBar_1.setMaximum(max);
		
		progressBar_2 = new JProgressBar();
		progressBar_2.setStringPainted(true);
		progressBar_2.setMaximum(max);

		
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGap(14)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_contentPane.createSequentialGroup()
									.addComponent(gs)
									.addGap(18)
									.addComponent(progressBar_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addComponent(lblNewLabel)
								.addComponent(pesquisar)
								.addGroup(gl_contentPane.createSequentialGroup()
									.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
										.addComponent(rg)
										.addComponent(dblp))
									.addGap(18)
									.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
										.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(progressBar_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
							.addPreferredGap(ComponentPlacement.RELATED, 86, Short.MAX_VALUE)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addComponent(lblSelecioneOProfessor)
								.addComponent(scrollPane_2, GroupLayout.PREFERRED_SIZE, 703, GroupLayout.PREFERRED_SIZE)))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addComponent(lblNewLabel_1)
								.addComponent(tabbedPane_1, GroupLayout.PREFERRED_SIZE, 462, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addComponent(lblNewLabel_2)
								.addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 550, GroupLayout.PREFERRED_SIZE))))
					.addContainerGap())
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(lblNewLabel)
							.addGap(6)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
								.addComponent(dblp)
								.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGap(2)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
								.addComponent(rg)
								.addComponent(progressBar_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addGap(2)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
								.addComponent(gs)
								.addComponent(progressBar_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(pesquisar))
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(lblSelecioneOProfessor)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(scrollPane_2, GroupLayout.PREFERRED_SIZE, 124, GroupLayout.PREFERRED_SIZE)))
					.addPreferredGap(ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_1)
						.addComponent(lblNewLabel_2))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false)
						.addComponent(tabbedPane_1)
						.addComponent(tabbedPane, GroupLayout.DEFAULT_SIZE, 357, Short.MAX_VALUE))
					.addGap(227))
		);
		
		JPanel panel_3 = new JPanel();
		tabbedPane_1.addTab("Artigos", null, panel_3, null);
		panel_3.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 0, 457, 329);
		panel_3.add(scrollPane);
		
		textPane_3 = new JTextPane();
		textPane_3.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		scrollPane.setViewportView(textPane_3);
		
		JPanel panel_4 = new JPanel();
		tabbedPane_1.addTab("Trabalhos", null, panel_4, null);
		panel_4.setLayout(null);
		
		JScrollPane scrollPane_5 = new JScrollPane();
		scrollPane_5.setBounds(0, 0, 457, 329);
		panel_4.add(scrollPane_5);
		
		textPane_4 = new JTextPane();
		textPane_4.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		scrollPane_5.setViewportView(textPane_4);
		
		JPanel panel_5 = new JPanel();
		tabbedPane_1.addTab("Livros", null, panel_5, null);
		panel_5.setLayout(null);
		
		JScrollPane scrollPane_6 = new JScrollPane();
		scrollPane_6.setBounds(0, 0, 457, 329);
		panel_5.add(scrollPane_6);
		
		textPane_5 = new JTextPane();
		textPane_5.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		scrollPane_6.setViewportView(textPane_5);
		
		JPanel panel_6 = new JPanel();
		tabbedPane_1.addTab("Outras produ\u00E7\u00F5es", null, panel_6, null);
		panel_6.setLayout(null);
		
		JScrollPane scrollPane_7 = new JScrollPane();
		scrollPane_7.setBounds(0, 0, 457, 329);
		panel_6.add(scrollPane_7);
		
		textPane_6 = new JTextPane();
		textPane_6.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		scrollPane_7.setViewportView(textPane_6);
		
		JPanel panel = new JPanel();
		tabbedPane.addTab("DBLP", null, panel, null);
		panel.setLayout(null);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(0, 0, 545, 329);
		panel.add(scrollPane_1);
		
		textPane = new JTextPane();
		textPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		scrollPane_1.setViewportView(textPane);
		
		JPanel panel_1 = new JPanel();
		tabbedPane.addTab("Google Scholar", null, panel_1, null);
		panel_1.setLayout(null);
		
		JScrollPane scrollPane_3 = new JScrollPane();
		scrollPane_3.setBounds(0, 0, 545, 329);
		panel_1.add(scrollPane_3);
		
		textPane_1 = new JTextPane();
		textPane_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		scrollPane_3.setViewportView(textPane_1);
		
		JPanel panel_2 = new JPanel();
		tabbedPane.addTab("ResearchGate", null, panel_2, null);
		panel_2.setLayout(null);
		
		JScrollPane scrollPane_4 = new JScrollPane();
		scrollPane_4.setBounds(0, 0, 545, 329);
		panel_2.add(scrollPane_4);
		
		textPane_2 = new JTextPane();
		textPane_2.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		scrollPane_4.setViewportView(textPane_2);
		list = new JList();
		list.setModel(new AbstractListModel() {
			String[] values = getProfessores();
			public int getSize() {
				return values.length;
			}
			public Object getElementAt(int index) {
				return values[index];
			}
		});
		scrollPane_2.setViewportView(list);
		contentPane.setLayout(gl_contentPane);
	}
	
	private void popularViewObrasRecuperadasProfessor() {
		ResultSet myRs;
		textPane.setText("");
		textPane_1.setText("");
		textPane_2.setText("");
		textPane_3.setText("");
		textPane_4.setText("");
		textPane_5.setText("");
		textPane_6.setText("");
		try {
			//Populando area de texto dos dados recuperados
			myRs = myStmt.executeQuery("SELECT COUNT(d.id) FROM dblp as d JOIN professores p ON p.id = d.id_prof WHERE nome = '"+ list.getSelectedValue() +"'");
			if(myRs.next()) {
				append(textPane, "Número de obras recuperadas: ", bold);
				append(textPane, myRs.getInt("count")+"\n\n", normal);
			}
			
			myRs = myStmt.executeQuery(" SELECT * FROM dblp as d JOIN professores p ON p.id = d.id_prof WHERE nome = '"+ list.getSelectedValue() +"'");
			while(myRs.next()) {	
				append(textPane, "Título: ", bold);
				append(textPane, myRs.getString("title") + "\n", normal);
				
				append(textPane, "Autores: ", bold);
				append(textPane, myRs.getString("authors") + "\n", normal);
				if(myRs.getString("booktitle") != null) {
					append(textPane, "Conferência: ", bold);
					append(textPane, myRs.getString("booktitle") + "\n", normal);
				}
				if(myRs.getString("journal") != null) {
					append(textPane, "Journal: ", bold);
					append(textPane, myRs.getString("journal") + "\n", normal);
				}
				if(myRs.getString("publisher") != null) {
					append(textPane, "Editora: ", bold);
					append(textPane, myRs.getString("publisher") + "\n", normal);
				}
				if(myRs.getString("pages") != null) {
					append(textPane, "Páginas: ", bold);
					append(textPane, myRs.getString("pages") + "\n", normal);
				}
				if(myRs.getString("_year") != null) {
					append(textPane, "Ano: ", bold);	
					append(textPane, myRs.getInt("_year") + "\n", normal);
				}
				if(myRs.getString("volume") != null) {
					append(textPane, "Volume: ", bold);
					append(textPane, myRs.getString("volume") + "\n", normal);
				}
				if(myRs.getString("_number") != null) {
					append(textPane, "Número: ", bold);
					append(textPane, myRs.getString("_number") + "\n", normal);
				}
				if(myRs.getString("isbn") != null) {
					append(textPane, "ISBN: ", normal);
					append(textPane, myRs.getString("isbn") + "\n", normal);
				}
				append(textPane, "\n", normal);
			}
			
			myRs = myStmt.executeQuery("SELECT COUNT(d.id) FROM googlescholar as d JOIN professores p ON p.id = d.id_prof WHERE nome = '"+ list.getSelectedValue() +"'");
			if(myRs.next()) {
				append(textPane_1, "Número de obras recuperadas: ", bold);
				append(textPane_1, myRs.getInt("count")+"\n\n", normal);
			}

			myRs = myStmt.executeQuery(" SELECT * FROM googlescholar as g JOIN professores p ON p.id = g.id_prof WHERE nome = '"+ list.getSelectedValue() +"'");
			while(myRs.next()) {
				append(textPane_1, "Título: ", bold);
				append(textPane_1, myRs.getString("title") + "\n", normal);
				
				append(textPane_1, "Autores: ", bold);
				append(textPane_1, myRs.getString("authors") + "\n", normal);
				if(myRs.getString("conference") != null) {
					append(textPane_1, "Conferência: ", bold);
					append(textPane_1, myRs.getString("conference") + "\n", normal);
				}
				if(myRs.getString("pages") != null) {
					append(textPane_1, "Páginas: ", bold);
					append(textPane_1, myRs.getString("pages") + "\n", normal);
				}
				if(myRs.getString("_year") != null){
					append(textPane_1, "Ano: ", bold);
					append(textPane_1, myRs.getInt("_year") + "\n", normal);
				}
				if(myRs.getString("volume") != null) {
					append(textPane_1, "Volume: ", bold);
					append(textPane_1, myRs.getString("volume") + "\n", normal);
				}
				if(myRs.getString("_number") != null) {
					append(textPane_1, "Número: ", bold);
					append(textPane_1, myRs.getString("_number") + "\n", normal);
				}
				if(myRs.getString("publisher") != null) {
					append(textPane_1, "Editora: ", bold);
					append(textPane_1, myRs.getString("publisher") + "\n", normal);
				}
				if(myRs.getString("citations") != null) {
					append(textPane_1, "Número de citações: ", bold);
					append(textPane_1, myRs.getString("citations") + "\n", normal);
				}
				if(myRs.getString("abstract") != null) {
					append(textPane_1, "Resumo: ", bold);
					append(textPane_1, myRs.getString("abstract") + "\n", normal);
				}
				append(textPane_1, "\n", normal);
			}
			
			myRs = myStmt.executeQuery("SELECT COUNT(d.id) FROM researchgate as d JOIN professores p ON p.id = d.id_prof WHERE nome = '"+ list.getSelectedValue() +"'");
			if(myRs.next()) {
				append(textPane_2, "Número de obras recuperadas: ", bold);
				append(textPane_2, myRs.getInt("count")+"\n\n", normal);
			}
			
			myRs = myStmt.executeQuery(" SELECT * FROM researchgate as r JOIN professores p ON p.id = r.id_prof WHERE nome = '"+ list.getSelectedValue() +"'");
			while(myRs.next()) {
				append(textPane_2, "Título: ", bold);
				append(textPane_2, myRs.getString("title") + "\n", normal);
				
				append(textPane_2, "Autores: ", bold);
				append(textPane_2, myRs.getString("authors") + "\n", normal);
				
				if(myRs.getString("conference") != null) {
					append(textPane_2, "Conferência: ", bold);
					append(textPane_2, myRs.getString("conference") + "\n", normal);
				}
				if(myRs.getString("_year") != null) {
					append(textPane_2, "Ano: ", bold);
					append(textPane_2, myRs.getInt("_year") + "\n", normal);
				}
				if(myRs.getString("_event") != null) {
					append(textPane_2, "Tipo de obra: ", bold);
					append(textPane_2, myRs.getString("_event") + "\n", normal);
				}
				if(myRs.getString("reads") != null) {
					append(textPane_2, "Leituras: ", bold);
					append(textPane_2, myRs.getString("reads") + "\n", normal);
				}
				if(myRs.getString("abstract") != null) {
					append(textPane_2, "Resumo: ", bold);
					append(textPane_2, myRs.getString("abstract") + "\n", normal);
				}
				append(textPane_2, "\n", normal);
			}
			
			//Populando área de texto do lattes
			File folder = new File("files/lattes/");
	    	File[] listOfFiles = folder.listFiles();
			//Papers
    		NodeList trabalho = null;
			//Artigos
    		NodeList artigo = null;
			//Livros e capitulos
    		NodeList livro = null;
    		NodeList capitulo = null;
			//Demais tipos
    		NodeList outraproducao = null;

	    	for (int k = 0; k < listOfFiles.length; k++) {
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
				if(nome.equals(list.getSelectedValue())) { //Se achou o nome do professor em algum arquivo
					NodeList producao = ((Element)curriculo.item(0)).getElementsByTagName("PRODUCAO-BIBLIOGRAFICA");
		    		NodeList livrosecapitulos = ((Element)curriculo.item(0)).getElementsByTagName("LIVROS-E-CAPITULOS");		
		    		NodeList demaistipos = ((Element)curriculo.item(0)).getElementsByTagName("DEMAIS-TIPOS-DE-PRODUCAO-BIBLIOGRAFICA");
					//Papers
		    		NodeList trabalhos = ((Element)producao.item(0)).getElementsByTagName("TRABALHOS-EM-EVENTOS");
		    		trabalho = ((Element)trabalhos.item(0)).getElementsByTagName("TRABALHO-EM-EVENTOS");
					//Artigos
		    		NodeList artigos = ((Element)producao.item(0)).getElementsByTagName("ARTIGOS-PUBLICADOS");
		    		artigo = ((Element)artigos.item(0)).getElementsByTagName("ARTIGO-PUBLICADO");
					//Livros e capitulos
		    		NodeList livros = null;
		    		NodeList capitulos = null;
		    		if(livrosecapitulos.getLength() != 0)
		    			livros = ((Element)livrosecapitulos.item(0)).getElementsByTagName("LIVROS-PUBLICADOS-OU-ORGANIZADOS");
		    		if(livrosecapitulos.getLength() != 0)
		    			capitulos = ((Element)livrosecapitulos.item(0)).getElementsByTagName("CAPITULOS-DE-LIVROS-PUBLICADOS");
		
					//Demais tipos
		    		if(demaistipos.getLength() != 0)
		    			outraproducao = ((Element)demaistipos.item(0)).getElementsByTagName("OUTRA-PRODUCAO-BIBLIOGRAFICA");
		
					if(livros != null && livros.getLength() != 0) {
						livro =  ((Element)livros.item(0)).getElementsByTagName("LIVRO-PUBLICADO-OU-ORGANIZADO");
					}
					if(capitulos != null && capitulos.getLength() != 0) {
						capitulo =  ((Element)capitulos.item(0)).getElementsByTagName("CAPITULO-DE-LIVRO-PUBLICADO");
					}
					break;
				}
	    	}
	    	
			for(int a = 0; a < artigo.getLength(); a++) {
				Node rootLattes = artigo.item(a);
	 			Element l = (Element) rootLattes;
	 			Element dadosBasicos = (Element) l.getElementsByTagName("DADOS-BASICOS-DO-ARTIGO").item(0); 
	 			append(textPane_3, "Título: ", bold);
	 			append(textPane_3, dadosBasicos.getAttribute("TITULO-DO-ARTIGO") +"\n", normal);
	 			
	 			append(textPane_3, "Ano: ", bold);
	 			append(textPane_3, Integer.parseInt(dadosBasicos.getAttribute("ANO-DO-ARTIGO"))  +"\n", normal);
	 			NodeList autoresLattes = l.getElementsByTagName("AUTORES");
	 			String listaAutores = "";
		    	for(int i = 0; i < autoresLattes.getLength(); i++ ) {
		    		Element aux = (Element) autoresLattes.item(i);
		    		listaAutores += aux.getAttribute("NOME-COMPLETO-DO-AUTOR") + ", ";
		    	}
		    	listaAutores = listaAutores.substring(0, listaAutores.length() - 2);
	 			append(textPane_3, "Autores: ", bold);
	 			append(textPane_3, listaAutores +"\n", normal);
	 			
	 			Element detalhamento = (Element) l.getElementsByTagName("DETALHAMENTO-DO-ARTIGO").item(0);
	 			if(detalhamento.getAttribute("TITULO-DO-PERIODICO-OU-REVISTA") != "") {
	 				append(textPane_3, "Periódico ou revista: ", bold);
	 				append(textPane_3, detalhamento.getAttribute("TITULO-DO-PERIODICO-OU-REVISTA") +"\n", normal);
	 			}
	 			if(detalhamento.getAttribute("VOLUME") != "") {
	 				append(textPane_3, "Volume: ", bold);
	 				append(textPane_3, detalhamento.getAttribute("VOLUME") +"\n", normal);
	 			}
	 			if(detalhamento.getAttribute("FASCICULO") != "") {
	 				append(textPane_3, "Número: ", bold);
	 				append(textPane_3, detalhamento.getAttribute("FASCICULO") +"\n", normal);
	 			}
	 			append(textPane_3, "\n", normal);
			}
			

			for(int a = 0; a < trabalho.getLength(); a++) {
				Node rootLattes = trabalho.item(a);
	 			Element l = (Element) rootLattes;
	 			Element dadosBasicos = (Element) l.getElementsByTagName("DADOS-BASICOS-DO-TRABALHO").item(0); 
	 			append(textPane_4, "Título: ", bold);
	 			append(textPane_4, dadosBasicos.getAttribute("TITULO-DO-TRABALHO") +"\n", normal);
	 			
	 			append(textPane_4, "Ano: ", bold);
	 			append(textPane_4, Integer.parseInt(dadosBasicos.getAttribute("ANO-DO-TRABALHO"))  +"\n", normal);
	 			NodeList autoresLattes = l.getElementsByTagName("AUTORES");
	 			String listaAutores = "";
		    	for(int i = 0; i < autoresLattes.getLength(); i++ ) {
		    		Element aux = (Element) autoresLattes.item(i);
		    		listaAutores += aux.getAttribute("NOME-COMPLETO-DO-AUTOR") + ", ";
		    	}
		    	listaAutores = listaAutores.substring(0, listaAutores.length() - 2);
	 			append(textPane_4, "Autores: ", bold);
	 			append(textPane_4, listaAutores +"\n", normal);
	 			
	 			Element detalhamento = (Element) l.getElementsByTagName("DETALHAMENTO-DO-TRABALHO").item(0);
	 			if(detalhamento.getAttribute("NOME-DO-EVENTO") != "") {
	 				append(textPane_4, "Congresso: ", bold);
	 				append(textPane_4, detalhamento.getAttribute("NOME-DO-EVENTO") +"\n", normal);
	 			}
	 			if(detalhamento.getAttribute("VOLUME") != "") {
	 				append(textPane_4, "Volume: ", bold);
	 				append(textPane_4, detalhamento.getAttribute("VOLUME") +"\n", normal);
	 			}
	 			if(detalhamento.getAttribute("FASCICULO") != "") {
	 				append(textPane_4, "Número: ", bold);
	 				append(textPane_4, detalhamento.getAttribute("FASCICULO") +"\n", normal);
	 			}
	 			append(textPane_4, "\n", normal);
			}
			
			if(livro != null) {
				for(int a = 0; a < livro.getLength(); a++) {
					Node rootLattes = livro.item(a);
		 			Element l = (Element) rootLattes;
		 			Element dadosBasicos = (Element) l.getElementsByTagName("DADOS-BASICOS-DO-LIVRO").item(0); 
		 			append(textPane_5, "Título: ", bold);
		 			append(textPane_5, dadosBasicos.getAttribute("TITULO-DO-LIVRO") +"\n", normal);
		 			
		 			append(textPane_5, "Ano: ", bold);
		 			append(textPane_5, Integer.parseInt(dadosBasicos.getAttribute("ANO"))  +"\n", normal);
		 			
		 			NodeList autoresLattes = l.getElementsByTagName("AUTORES");
		 			String listaAutores = "";
			    	for(int i = 0; i < autoresLattes.getLength(); i++ ) {
			    		Element aux = (Element) autoresLattes.item(i);
			    		listaAutores += aux.getAttribute("NOME-COMPLETO-DO-AUTOR") + ", ";
			    	}
			    	listaAutores = listaAutores.substring(0, listaAutores.length() - 2);
		 			append(textPane_5, "Autores: ", bold);
		 			append(textPane_5, listaAutores +"\n", normal);
		 			
		 			Element detalhamento = (Element) l.getElementsByTagName("DETALHAMENTO-DO-LIVRO").item(0);
		 			if(detalhamento.getAttribute("NOME-DA-EDITORA") != "") {
		 				append(textPane_5, "Editora: ", bold);
		 				append(textPane_5, detalhamento.getAttribute("NOME-DA-EDITORA") +"\n", normal);
		 			}
		 			if(detalhamento.getAttribute("NUMERO-DE-VOLUMES") != "") {
		 				append(textPane_5, "Volume: ", bold);
		 				append(textPane_5, detalhamento.getAttribute("NUMERO-DE-VOLUMES") +"\n", normal);
		 			}
		 			if(detalhamento.getAttribute("NUMERO-DE-PAGINAS") != "") {
		 				append(textPane_5, "Páginas: ", bold);
		 				append(textPane_5, detalhamento.getAttribute("NUMERO-DE-PAGINAS") +"\n", normal);
		 			}
		 			append(textPane_5, "\n", normal);
				}
			}
			
			if(outraproducao != null) {
				
				for(int a = 0; a < outraproducao.getLength(); a++) {
					Node rootLattes = outraproducao.item(a);
		 			Element l = (Element) rootLattes;
		 			Element dadosBasicos = (Element) l.getElementsByTagName("DADOS-BASICOS-DE-OUTRA-PRODUCAO").item(0); 
		 			append(textPane_6, "Título: ", bold);
		 			append(textPane_6, dadosBasicos.getAttribute("TITULO") +"\n", normal);
		 			
		 			append(textPane_6, "Ano: ", bold);
		 			append(textPane_6, Integer.parseInt(dadosBasicos.getAttribute("ANO"))  +"\n", normal);
		 			
		 			NodeList autoresLattes = l.getElementsByTagName("AUTORES");
		 			String listaAutores = "";
			    	for(int i = 0; i < autoresLattes.getLength(); i++ ) {
			    		Element aux = (Element) autoresLattes.item(i);
			    		listaAutores += aux.getAttribute("NOME-COMPLETO-DO-AUTOR") + ", ";
			    	}
			    	listaAutores = listaAutores.substring(0, listaAutores.length() - 2);
		 			append(textPane_6, "Autores: ", bold);
		 			append(textPane_6, listaAutores +"\n", normal);
		 			
		 			Element detalhamento = (Element) l.getElementsByTagName("DETALHAMENTO-DE-OUTRA-PRODUCAO").item(0);
		 			if(detalhamento.getAttribute("EDITORA") != "") {
		 				append(textPane_6, "Nome da editora: ", bold);
		 				append(textPane_6, detalhamento.getAttribute("EDITORA") +"\n", normal);
		 			}
		 			if(detalhamento.getAttribute("NUMERO-DE-PAGINAS") != "") {
		 				append(textPane_6, "Número de páginas: ", bold);
		 				append(textPane_6, detalhamento.getAttribute("NUMERO-DE-PAGINAS") +"\n", normal);
		 			}
		 			append(textPane_6, "\n", normal);
				}
			}
			textPane.setCaretPosition(0);
			textPane_1.setCaretPosition(0);
			textPane_2.setCaretPosition(0);
			textPane_3.setCaretPosition(0);
			textPane_4.setCaretPosition(0);
			textPane_5.setCaretPosition(0);
			textPane_6.setCaretPosition(0);
			
		} catch (SQLException | ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private String[] getProfessores() {
		ResultSet myRs;
		String[] listaProf = null;
		int cont = 0;
		try {
			myRs = myStmt.executeQuery("SELECT COUNT(*) FROM professores");
			if(myRs.next())
				listaProf = new String[myRs.getInt("count")];
			
			myRs = myStmt.executeQuery("SELECT nome FROM professores ORDER BY nome");
			while(myRs.next()) {
				listaProf[cont] = myRs.getString("nome");
				cont++;
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return listaProf;
	}
	public static void incrementarBarraDBLP() {
		progressBar.setValue(progressBar.getValue()+1);
		progressBar.update(progressBar.getGraphics());
	}
	
	public static void incrementarBarraGS() {
		progressBar_2.setValue(progressBar_2.getValue()+1);
		progressBar_2.update(progressBar_2.getGraphics());
	}
	
	public static void incrementarBarraRG() {
		progressBar_1.setValue(progressBar_1.getValue()+1);
		progressBar_1.update(progressBar_1.getGraphics());
	}
	
	private static void append(JEditorPane pane, String s, SimpleAttributeSet fonte) {
		   try {
			   Document doc = pane.getDocument();
			   doc.insertString(doc.getLength(), s, fonte);
		   } catch(BadLocationException exc) {
		      exc.printStackTrace();
		   }
	}
}
