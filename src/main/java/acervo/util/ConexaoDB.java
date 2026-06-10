package acervo.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexaoDB {
	
	private static String url;
	private static String usuario;
	private static String senha; 
	
	static {
		Properties props = new Properties();
		
		
		try (InputStream is = ConexaoDB.class
				.getClassLoader()
				.getResourceAsStream("config.properties")){
			if (is == null) throw new RuntimeException(
					"Arquivo config.properties não emcontrado!\n" + "Coloque-o em src/main/resources/");
			
			props.load(is);
			url = props.getProperty("db.url");
			usuario = props.getProperty("db.usuario");
			senha = props.getProperty("db.senha");
			
		}catch (IOException e) {
			throw new RuntimeException("Erro ao ler config.properties: "+ e.getMessage());
		}
	}
	
	private ConexaoDB() {}
	
	public static Connection getConexao () throws SQLException {
		return DriverManager.getConnection(url, usuario, senha);
		
	}
	
	public static boolean testarConexao() {
		try(Connection conn = getConexao ()) {
			return conn != null && !conn.isClosed();
		} catch (SQLException e) {
			System.out.println("Erro de conexão: "+ e.getMessage());
			return false;
		}
	}
}
