package acervo.model;

import java.time.LocalDate;

public class Emprestimo {//emprestimo de livro
	//fazer a msm coisa que no Livro.java
	
	private int id;
	private int livroId;
	private String ocupacaoLeitor;
	private String nomeLeitor;
	private int alunoAno;
	private char alunoTurma;
	private LocalDate dataEmprestimo;
	private LocalDate dataPrevista;
	private LocalDate dataDevolucao;
	
	private String tituloLivro;
	
	public Emprestimo() {}
	
	public Emprestimo (int id, int livroId, String ocupacaoLeitor, String nomeLeitor, int alunoAno, 
			char alunoTurma,LocalDate dataEmprestimo, LocalDate dataPrevista, LocalDate dataDevolucao) {
		this.id = id;
		this.livroId = livroId;
		this.ocupacaoLeitor = ocupacaoLeitor;
		this.nomeLeitor = nomeLeitor;
		this.alunoAno = alunoAno;
		this.alunoTurma = alunoTurma;
		this.dataEmprestimo = dataEmprestimo;
		this.dataPrevista = dataPrevista;
		this.dataDevolucao = dataDevolucao;
	}
	
	public boolean isAtrasado() {//ve se ta atrasado
		return dataDevolucao == null && LocalDate.now().isAfter(dataPrevista);
	}
	
	public boolean isDevolvido() {//ve se foi devolvido
		return dataDevolucao != null;
	}
	
	
	public int getId() 						{ return id; } 
	public int getLivroId() 				{ return livroId; }
	public String getOcupacaoLeitor()		{ return ocupacaoLeitor; }
	public String getNomeLeitor ()			{ return nomeLeitor; }
	public int getAlunoAno() 				{ return alunoAno; }
	public char getAlunoTurma() 			{ return alunoTurma; }
	public LocalDate getDataEmpretismo() 	{ return dataEmprestimo; }
	public LocalDate getDataPrevista() 		{ return dataPrevista; }
	public LocalDate getDataDevolucao()		{ return dataDevolucao; }
	
	
	public void setId 			  (int id)					 { this.id = id; }
	public void setLivroId		  (int livroId)				 { this.livroId = livroId; }
	public void setOcupacaoLeitor (String ocupacaoLeitor)	 { this.ocupacaoLeitor = ocupacaoLeitor; }
	public void setNomeLeitor	  (String nomeLeitor)		 { this.nomeLeitor = nomeLeitor; }
	public void setAlunoAno		  (int alunoAno)			 { this.alunoAno = alunoAno; }
	public void setAlunoTurma	  (char alunoTurma)			 { this.alunoTurma = alunoTurma; }
	public void setDataEmprestimo (LocalDate dataEmprestimo) { this.dataEmprestimo = dataEmprestimo; }
	public void setDataPrevista   (LocalDate dataPrevista)	 { this.dataPrevista = dataPrevista; }
	public void setDataDevolucao  (LocalDate dataDevolucao)	 { this.dataDevolucao = dataDevolucao; }
	
	public void setTituloLivro	  (String tituloLivro) 		 { this.tituloLivro = tituloLivro; }

}
