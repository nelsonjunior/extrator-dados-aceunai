package br.com.app.old;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import br.com.app.builder.CategoriaSqlBuilder;
import br.com.app.builder.ContatoSqlBuilder;
import br.com.app.model.CategoriaRegistro;
import br.com.app.model.Contato;
import br.com.app.model.Registro;
import br.com.app.model.Telefone;

public class Extrator2018Classificado {

	private static final Pattern TELEFONE_9_DIG = Pattern.compile("[8-9]{0,1}[0-9]{4}-[0-9]{4}");
	private static final Pattern TELEFONE = Pattern.compile("[0-9]{4}-[0-9]{4}");
	private static final Pattern TELEFONE_PART = Pattern.compile("[0-9]{4}");

	public static void main(String[] args) throws IOException {

		System.out.println("##### RECUPERAR ARQUIVO!");
		Path caminho = Paths.get(System.getProperty("user.home"),
				"/Dropbox/Dev/App Guia ACE/Arquivos/Extrator/entrada/2018/ListaClassificado2.txt");
		Stream<String> baseDados = Files.lines(caminho);

		System.out.println("##### REMOVER LINHAS EM BRANCO!");
		Stream<String> linhas = baseDados.filter(lin -> !lin.isEmpty());

		System.out.println("##### OBTER REGISTROS!");
		List<CategoriaRegistro> registroClassficados = new ArrayList<>();
		String categoriaAtual = null;
		Iterator<String> iterator = linhas.iterator();
		
		while (iterator.hasNext()) {
			String linha = iterator.next();

			if(linha.startsWith("@@@@")){
				categoriaAtual = linha.trim().replace("@@@@", "");
				registroClassficados.add(new CategoriaRegistro(categoriaAtual));
				continue;
			}
			
			Registro registro = new Registro();
			registro.setNome(StringUtils.substringBefore(linha, "###"));
			registro.setEndereco(StringUtils.substringBetween(linha, "###", "$$"));
			registro.setTelefones(StringUtils.substringAfter(linha, "$$"));

			registroClassficados.get(registroClassficados.size()-1).getRegistros().add(registro);

		}
		
		System.out.println("##### CATEGORIAS ENCONTRADOS: " + registroClassficados.size());
	
		List<String> linhasSQL = new ArrayList<String>();
		Integer linha = 1;
		int ordem = 1;
		List<Contato> contatosErro = new ArrayList<Contato>();
		
		for (CategoriaRegistro categoria : registroClassficados) {
			
			System.out.println("##### OBTER CONTATOS!");
			List<Contato> contatos = new ArrayList<Contato>();
			
			
			for (Registro registro : categoria.getRegistros()) {
				
				Contato contato = new Contato(registro.getNome());
				contato.setEndereco(registro.getEndereco());
				contato.setTelefoneRaw(registro.getTelefones());
				
				contato.getTelefones().addAll(processarTelefone(contato.getTelefoneRaw()));
				
				
				contato.setLinha(linha++);
				
				if (StringUtils.isBlank(contato.getNome()) || StringUtils.isBlank(contato.getEndereco())
						|| StringUtils.isBlank(contato.getTelefoneRaw())) {
					contatosErro.add(contato);
				} else {
					contatos.add(contato);
				}
				
			}
			
			System.out.println("##### CONTATOS ENCONTRADOS: " + contatos.size());
			System.out.println("##### CONTATOS COM ERRO ENCONTRADOS: " + contatosErro.size());

			linhasSQL.add(CategoriaSqlBuilder.builderSQL(categoria.getNome(), ordem++));
	
			for (int i = 1; i <= contatos.size(); i++) {
				linhasSQL.addAll(ContatoSqlBuilder.builderClassificadoSQL(contatos.get(i - 1)));
			}
			
		}
		
		if(contatosErro.isEmpty()){
			linhasSQL.forEach(System.out::println);
		}else{
			System.out.println("##### PROCESSAMENTO CONTATOS ERRO #####");
			contatosErro.forEach(c -> System.out.println(c.getLinha() + ": " + c.getNome()));
		}
		
	}

	private static List<Telefone> processarTelefone(String telefoneRaw) {
		List<Telefone> retorno = new ArrayList<Telefone>();
		try {
			String[] split = telefoneRaw.split("/");
			for (String part : split) {
				Matcher mTelefone = TELEFONE.matcher(part);
				Matcher mTelefone9Dig = TELEFONE_9_DIG.matcher(part);
				if (mTelefone.matches() || mTelefone9Dig.matches()) {
					retorno.add(new Telefone(part));
				} else {
					Matcher mTelefonePart = TELEFONE_PART.matcher(part);
					if (mTelefonePart.matches()) {
						retorno.add(
								new Telefone(retorno.get(retorno.size() - 1).getTelefone().substring(0, 5).concat(part)));
					}
				}
			}
		} catch (Exception e) {
			System.out.println("ERRO RECUPERAR TELEFONES: " + telefoneRaw);
			e.printStackTrace();
		}
		return retorno;
	}

}
