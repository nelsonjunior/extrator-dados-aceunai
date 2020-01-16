package br.com.app.model;

import java.util.ArrayList;
import java.util.List;

public class Contato {

	private String nome;
	private String endereco;
	private String telefoneRaw;
	private Integer linha;
	private List<Telefone> telefones;

	public Contato(String nome) {
		super();
		this.nome = nome;
		this.telefones = new ArrayList<Telefone>();
	}

	public Contato(String nome, String endereco, String telefone) {
		super();
		this.nome = nome;
		this.endereco = endereco;
		this.telefoneRaw = telefone;
		this.telefones = new ArrayList<Telefone>();
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	public String getTelefoneRaw() {
		return telefoneRaw;
	}

	public void setTelefoneRaw(String telefoneRaw) {
		this.telefoneRaw = telefoneRaw;
	}

	public List<Telefone> getTelefones() {
		return telefones;
	}

	public void setTelefones(List<Telefone> telefones) {
		this.telefones = telefones;
	}

	public Integer getLinha() {
		return linha;
	}

	public void setLinha(Integer linha) {
		this.linha = linha;
	}

}
