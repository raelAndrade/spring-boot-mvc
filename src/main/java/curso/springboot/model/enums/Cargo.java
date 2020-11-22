package curso.springboot.model.enums;

public enum Cargo {
	
	JUNIOR(1, "Júnior"),
	PLENO(2, "Pleno"),
	SENIOR(3, "Sênior");
	
	private Integer id;
	private String nome;
	private String valor;
	
	private Cargo(Integer id, String nome) {
		this.id = id;
		this.nome = nome;
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}
	
	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getValor() {
		return valor = this.name();
	}
	
	public void setValor(String valor) {
		this.valor = valor;
	}
	
//	@Override
//	public String toString() {
//		return this.name();
//	}
	
}
