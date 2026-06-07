package acervo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import acervo.util.ConexaoDB;

public class AdminDAO {
	
	public Optional<String> verificarLogin (String usuario, String senha) throws SQLException {
		
		String sql = "SELECT nome FROM administradores WHERE usuario = ? ANd senha = ?";
				
		try(Connection conn = ConexaoDB.getConexao();
				PreparedStatement ps = conn.prepareStatement(sql)) {
			
			ps.setString(1, usuario);
			ps.setString(2, senha);
			
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					
					return Optional.of(rs.getString("nome"));
				}
			}
		}
		return Optional.empty();
	}
}
