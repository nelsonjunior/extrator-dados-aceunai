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

import br.com.app.builder.ContatoSqlBuilder;
import br.com.app.model.Contato;
import br.com.app.model.Registro;
import br.com.app.model.Telefone;

public class Extrator2018 {

	private static final Pattern TELEFONE_9_DIG = Pattern.compile("[8-9]{0,1}[0-9]{4}-[0-9]{4}");
	private static final Pattern TELEFONE = Pattern.compile("[0-9]{4}-[0-9]{4}");
	private static final Pattern TELEFONE_PART = Pattern.compile("[0-9]{4}");

	public static void main(String[] args) throws IOException {

		System.out.println("##### RECUPERAR ARQUIVO!");
		Path caminho = Paths.get(System.getProperty("user.home"),
				"/Dropbox/Dev/App Guia ACE/Arquivos/Extrator/entrada/2018/ListaBase.txt");
		Stream<String> baseDados = Files.lines(caminho);

		System.out.println("##### REMOVER LINHAS EM BRANCO!");
		Stream<String> linhas = baseDados.filter(lin -> !lin.isEmpty());

		System.out.println("##### OBTER REGISTROS!");
		List<Registro> registros = new ArrayList<Registro>();
		Iterator<String> iterator = linhas.iterator();
		while (iterator.hasNext()) {
			String linha = iterator.next();

			Registro registro = new Registro();
			registro.setNome(StringUtils.substringBefore(linha, "###"));
			registro.setEndereco(StringUtils.substringBetween(linha, "###", "$$"));
			registro.setTelefones(StringUtils.substringAfter(linha, "$$"));

			registros.add(registro);

		}
		System.out.println("##### REGISTROS ENCONTRADOS: " + registros.size());

		System.out.println("##### OBTER CONTATOS!");
		List<Contato> contatos = new ArrayList<Contato>();
		List<Contato> contatosErro = new ArrayList<Contato>();

		List<Registro> processamentoManual = new ArrayList<Registro>();
		List<Registro> processamentoManualPharserTelefone = new ArrayList<Registro>();

		Integer linha = 1;
		for (Registro registro : registros) {

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
		System.out.println("##### REGISTROS PROCESSAMENTO MANUAL: " + processamentoManual.size());
		System.out
				.println("##### REGISTROS PROCESSAMENTO MANUAL TELEFONE: " + processamentoManualPharserTelefone.size());

		// System.out.println("##### PROCESSAMENTO MANUAL TELEFONE #####");
		// processamentoManualPharserTelefone.forEach(c ->
		// System.out.println(c.getNome()));

		System.out.println("##### PROCESSAMENTO CONTATOS ERRO #####");
		contatosErro.forEach(c -> System.out.println(c.getLinha() + ": " + c.getNome()));

		List<String> linhasSQL = new ArrayList<String>();
		for (int i = 1; i <= contatos.size(); i++) {
			linhasSQL.addAll(ContatoSqlBuilder.builderSQL(contatos.get(i - 1)));
		}
		linhasSQL.forEach(System.out::println);
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
