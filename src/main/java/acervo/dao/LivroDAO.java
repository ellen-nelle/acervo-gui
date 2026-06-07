package acervo.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import acervo.model.Livro;
import acervo.util.ConexaoDB;

public class LivroDAO {
	
	
	//CADASTRAR LIVROS
	public Livro inserir(Livro livro) throws SQLException {//cadastra
		
		String sql = "INSET INTO livros (titulo,autor,isbn,ano,quantidade) " +
					 "VALUES (?, ?, ?, ?, ?)";
		
		try(Connection conn = ConexaoDB.getConexao();
				PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
			
			ps.setString(1, livro.getTitulo());
			ps.setString(2, livro.getAutor());
			ps.setString(3, livro.getIsbn());
			ps.setInt	(4, livro.getAno());
			ps.setInt	(5, livro.getQuantidade());
			
			
			try(ResultSet rs = ps.getGeneratedKeys()) {
				if(rs.next()) livro.setId(rs.getInt(1));
			}
		}
		
		return livro;
	}
	//LISTAR TODOS
	public List<Livro> listarTodos() throws SQLException {
		String sql = "SELECT * FROM livros ORDER BY titulo";
		
		List<Livro> lista = new ArrayList<>();
		
		try (Connection conn = ConexaoDB.getConexao();
				Statement st = conn.createStatement();
						ResultSet rs = st.executeQuery(sql)){
			
			while (rs.next()) {
				lista.add(mapear(rs));
			}
		}
		return lista;
	}
	//LISTAR DISPONIVEIS
	public List<Livro> listarDisponiveis()throws SQLException {
		
		String sql = "SELECT * FROM livros WHERE disponivel = 1 ORDER BY titulo";
		List<Livro> lista = new ArrayList<>();
		
		try (Connection conn = ConexaoDB.getConexao();
				Statement st = conn.createStatement();
				ResultSet rs = st.executeQuery(sql)) {
			
			while (rs.next()) lista.add(mapear(rs));
		}
		return lista;
	}
	
	
	//BUSCAR POR TEXTO 
	public List<Livro> buscar(String termo) throws SQLException {
		
		String sql = "SELECT * FROM livros WHERE titulo LIKE ? OR autor LIKE ? ORDER BY titulo";
		List<Livro> lista  = new ArrayList<>();
		String like = "%" + termo + "%";
		
		try (Connection conn = ConexaoDB.getConexao();
				PreparedStatement ps = conn.prepareStatement(sql)) {
			
			ps.setString(1, like);
			ps.setString(2, like);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) lista.add(mapear(rs));
			}
		}
		
		return lista;
	}
	
	//BUSCAR POR ID
	public Optional<Livro> buscarPorId(int id) throws SQLException {
		String sql = "SELECT * FROM livros WHERE id = ?";
		
		try (Connection conn = ConexaoDB.getConexao();
				PreparedStatement ps = conn.prepareStatement(sql)) {
			
			ps.setInt(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) return Optional.of(mapear(rs));
			}
		}
		return Optional.empty();
	}
	
	//ATUALIZAR
	public void atualizar(Livro livro) throws SQLException {
		String sql = "UPDATE livros SET titulo=?, autor=?, isbn=?, ano=?, quantidade=? WHERE id=? ";
		
		try (Connection conn = ConexaoDB.getConexao();
				PreparedStatement ps = conn.prepareStatement(sql)) {
			
			ps.setString(1, livro.getTitulo());
			ps.setString(2, livro.getAutor());
			ps.setString(3, livro.getIsbn());
			ps.setInt	(4, livro.getAno());
			ps.setInt	(5, livro.getQuantidade());
			ps.setInt	(6, livro.getId());
			ps.executeUpdate();
		}
	}
	
	//ATUALIZAR DISPONIBILIDADE
	public void atualizarDiponibilidade(int livroId,boolean disponivel)throws SQLException{
		String sql = "UPDATE livros SET diponivel = ? WHERE id = ?";
		
		try (Connection conn = ConexaoDB.getConexao();
				PreparedStatement ps = conn.prepareStatement (sql)){
			ps.setBoolean(1, disponivel);
			ps.setInt	 (2, livroId);
			ps.executeUpdate();
		}
	}
	
	//REMOVER
	public boolean remover(int id) throws SQLException {
		String sql = "DELETE FROM livros WHERE id = ?";
		
		try (Connection conn = ConexaoDB.getConexao();
				PreparedStatement ps = conn.prepareStatement(sql)) {
			
			ps.setInt(1, id);
			
			return ps.executeUpdate() > 0;
		}
	}
	
	//MAPEAR - Converte uma linha do DB em um objeto livro
	private Livro mapear(ResultSet rs) throws SQLException {
		Livro l = new Livro ();
		l.setId 	 	(rs.getInt		("id"));
		l.setTitulo	 	(rs.getString	("titulo"));
		l.setAutor	 	(rs.getString	("autor"));
		l.setIsbn		(rs.getString	("isbn"));
		l.setAno	 	(rs.getInt		("ano"));
		l.setQuantidade (rs.getInt		("quantidade"));
		l.setDisponivel (rs.getBoolean  ("disponivel"));
		
		return l;
	}
		
	public void atualizarDisponibilidade(int livroId, boolean disponivel) throws SQLException {
	    String sql = "UPDATE livros SET disponivel = ? WHERE id = ?";

	    try (Connection conn = ConexaoDB.getConexao();
	         PreparedStatement ps = conn.prepareStatement(sql)) {

	        ps.setBoolean(1, disponivel);
	        ps.setInt    (2, livroId);
	        ps.executeUpdate();
	    }
	}
}
