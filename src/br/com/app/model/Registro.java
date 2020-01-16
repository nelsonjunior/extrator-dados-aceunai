package br.com.app.model;

import java.util.ArrayList;
import java.util.List;

public class Registro {

	private String nome;
	private String endereco;
	private String telefones;
	private List<String> complementos;

	public Registro() {
	}
	
	public Registro(String nome) {
		super();
		this.nome = nome;
		this.complementos = new ArrayList<String>();
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public List<String> getComplementos() {
		return complementos;
	}

	public void setComplementos(List<String> complementos) {
		this.complementos = complementos;
	}

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	public String getTelefones() {
		return telefones;
	}

	public void setTelefones(String telefones) {
		this.telefones = telefones;
	}

	@Override
	public String toString() {
		return nome;
	}

}
