package acervo.model;

public class Livro {
//Livro.java — representa um livro do sistema

	private int id;
	private String isbn;
	private String titulo;
	private String autor;
	private String genero;
	private int ano;
	private String faixaEtaria;
	private boolean disponivel; //true = 1 ou mais
	private int quantidade;
	
	public Livro() {}
	
	public Livro(String isbn, String titulo, String autor, String genero, int ano,
			String faixaEtaria, int quantidade) {
		this.isbn = isbn;
		this.titulo = titulo;
		this.genero = genero;
		this.ano = ano;
		this.faixaEtaria = faixaEtaria;
		this.quantidade = quantidade;
		this.disponivel = true;
	}
	
	
	public int 	  getId()			{ return id; }
	public String getIsbn() 		{ return isbn; }
	public String getTitulo() 		{ return titulo; }
	public String getAutor() 		{ return autor; }
	public String getGenero()		{ return genero; }
	public int 	  getAno() 			{ return ano; }
	public String getFaixaEtaria()  { return faixaEtaria; }
	public boolean isDisponivel() 	{ return disponivel; }
	public int getQuantidade() 		{ return quantidade; }
	
	
	public void setId (int id)						 { this.id = id; }
	public void setIsbn (String isbn)				 { this.isbn = isbn; }
	public void setTitulo (String titulo)			 { this.titulo = titulo; }
	public void setAutor (String autor)				 { this.autor = autor; }
	public void setGenero (String genero) 			 { this.genero = genero; }
	public void setAno (int ano)					 { this.ano = ano; }
	public void setFaixaEtaria (String faixaEtaria)  { this.faixaEtaria = faixaEtaria; }
	public void setDisponivel (boolean disponivel)	 { this.disponivel = disponivel; }
	public void setQuantidade (int quantidade)		 { this.quantidade = quantidade; }
	
	
	
	@Override
	public String toString() {
		return titulo + " - " + autor + " (" + faixaEtaria + "," + quantidade + "exemplar " + 
	(quantidade > 1? "es" : "") + ")";
	}
}
