package acervo.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import acervo.model.Emprestimo;
import acervo.util.ConexaoDB;

public class EmprestimoDAO {
	
	private Emprestimo mapear(ResultSet rs) throws SQLException {
		Emprestimo e = new Emprestimo();
		
		e.setId				(rs.getInt("id"));
		e.setLivroId		(rs.getInt("livro_id"));
		e.setOcupacaoLeitor (rs.getString("ocupacao_leitor"));
		e.setNomeLeitor		(rs.getString("nome_leitor"));
		e.setAlunoAno		(rs.getInt("aluno_ano"));
		e.setAlunoTurma		(rs.getString("aluno_turma").charAt(0));
		e.setDataEmprestimo (rs.getDate("data_emprestimo").toLocalDate());
		e.setDataPrevista	(rs.getDate("data_prevista").toLocalDate());
		
		Date dev = rs.getDate("data_devolucao");
		if(dev != null) e.setDataDevolucao(dev.toLocalDate());
		
		try { e.setTituloLivro (rs.getString("titulo_livro")); }
		catch (SQLException ignored) {}
		
		return e;
	}
	
	
	//INSERI EMPRESTIMOS
	public Emprestimo inserir (Emprestimo e) throws SQLException {
		String sql = "INSERT INTO emprestimos (livro_id, ocupacao_leitor, nome_leitor,aluno_ano,"
				+ " aluno_turma, data_emprestimo, data_prevista) "
				+ "VALUES (?, ?, ?, ?, ?, ?,?)";
		
		try (Connection conn = ConexaoDB.getConexao();
				PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
					
			ps.setInt		 (1, e.getLivroId());
			ps.setString 	 (2, e.getOcupacaoLeitor());
			ps.setString 	 (3, e.getNomeLeitor());
			ps.setInt	 	 (4, e.getAlunoAno());
			ps.setString	 (5, String.valueOf(e.getAlunoTurma()));
			ps.setDate	 	 (6, Date.valueOf(e.getDataEmpretismo()));
			ps.setDate	 	 (7, Date.valueOf(e.getDataPrevista()));
			
			ps.executeUpdate();
			
			try (ResultSet rs = ps.getGeneratedKeys()) {
				if (rs.next()) e.setId(rs.getInt(1));
			}
		}
		return e;
	}
	
	
	//REGSITRAR DEVOLUÇAO
	public boolean registrarDevolucao (int emprestimoId) throws SQLException {
		String sql = "UPDATE EMPRESTIMOS SET data_devolucao = CURDATE() WHERE id = ?"
				+ " AND data_devolucao IS NULL";
		
		try (Connection conn = ConexaoDB.getConexao();
				PreparedStatement ps = conn.prepareStatement(sql)){
			
			ps.setInt(1,  emprestimoId);
			return ps.executeUpdate() > 0 ;
		}
	}
	
	//LISTA -> LIVROS NÂO DEVOLVIDOS
	public List<Emprestimo> listarEmAberto () throws SQLException {
		String sql = "SELECT e.*, l.titulo AS titulo_livro FROM emprestimos e "
				+ "JOIN livros l on l.id = e.livro_id WHERE e.data_devolucao IS NULL "
				+ "ORDER BY e.data_prevista";
		
		return executarLista(sql);
	}
	
	//LISTA TODOS
	public List<Emprestimo> listarTodos() throws SQLException {
		String sql = "SELECT e.*, l.titulo AS titulo_livro FROM emprestimos e "
				+ "JOIN livros l ON l.id = e.livro_id ORDER BY e.data_emprestimo DESC";
		
		return executarLista(sql);
				
	}
	
	//BUSCAR POR ID
	public Optional<Emprestimo> buscarPorId(int id) throws SQLException {
		String sql = "SELECT e.*, l.titulo AS titulo_livro FROM emprestimos e "
				+ "JOIN livros l ON l.id = e.livro_id WHERE e.id = ?";
		
		try (Connection conn = ConexaoDB.getConexao();
				PreparedStatement ps = conn.prepareStatement(sql)) {
			
			ps.setInt(1,  id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) return Optional.of(mapear(rs));
			}
		}
		return Optional.empty();
	}
	
	//------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------
//  LISTAR EMPRÉSTIMOS DE UM LIVRO ESPECÍFICO
   // --------------------------------------------------
   public List<Emprestimo> listarPorLivro(int livroId) throws SQLException {
       String sql = "SELECT e.*, l.titulo AS titulo_livro " +
                    "FROM emprestimos e " +
                    "JOIN livros l ON l.id = e.livro_id " +
                    "WHERE e.livro_id = ? " +
                    "ORDER BY e.data_emprestimo DESC";
 
       List<Emprestimo> lista = new ArrayList<>();
 
       try (Connection conn = ConexaoDB.getConexao();
            PreparedStatement ps = conn.prepareStatement(sql)) {
 
           ps.setInt(1, livroId);
           try (ResultSet rs = ps.executeQuery()) {
               while (rs.next()) lista.add(mapear(rs));
           }
       }
       return lista;
   }
 
   // --------------------------------------------------
   //  CONTAR EMPRÉSTIMOS EM ABERTO DE UM LIVRO
   //  Usado para saber se ainda tem exemplar disponível
   // --------------------------------------------------
   public int contarEmAberto(int livroId) throws SQLException {
       String sql = "SELECT COUNT(*) FROM emprestimos " +
                    "WHERE livro_id = ? AND data_devolucao IS NULL";
 
       try (Connection conn = ConexaoDB.getConexao();
            PreparedStatement ps = conn.prepareStatement(sql)) {
 
           ps.setInt(1, livroId);
           try (ResultSet rs = ps.executeQuery()) {
               if (rs.next()) return rs.getInt(1);
           }
       }
       return 0;
   }
 
//--------------------------------------------------------------------------------------------------
   //-------------------------------------------------------------------------------------------------
	private List<Emprestimo> executarLista (String sql) throws SQLException {
		List<Emprestimo> lista = new ArrayList<>();
		
		try (Connection conn = ConexaoDB.getConexao();
				Statement st = conn. createStatement();
				ResultSet rs = st.executeQuery(sql)){
			while (rs.next()) lista.add(mapear(rs));
		}
		return lista;
	}

}
