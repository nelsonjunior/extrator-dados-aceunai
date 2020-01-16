package br.com.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import br.com.app.builder.ContatoSqlBuilder;
import br.com.app.model.Contato;
import br.com.app.model.Registro;
import br.com.app.sql.BaseDao;
import br.com.app.util.TelefoneUtil;

public class ExtratorListaBase {

	public static void main(String[] args) throws IOException {

		System.out.println("##### RECUPERAR ARQUIVO!");
		Path caminho = Paths.get(System.getProperty("user.home"), Constantes.ARQUIVO_ENTRADA_BASE_DADOS);
		Stream<String> baseDados = Files.lines(caminho);

		System.out.println("##### REMOVER LINHAS EM BRANCO!");
		Stream<String> linhas = baseDados.filter(lin -> !lin.isEmpty());

		System.out.println("##### OBTER REGISTROS!");
		List<Registro> registros = new ArrayList<Registro>();
		Iterator<String> iterator = linhas.iterator();
		while (iterator.hasNext()) {
			String linha = iterator.next();

			Registro registro = new Registro();
			registro.setNome(StringUtils.substringBefore(linha, Constantes.SEPARACAO_ENDERECO));
			registro.setEndereco(
					StringUtils.substringBetween(linha, Constantes.SEPARACAO_ENDERECO, Constantes.SEPERACAO_TELEFONE));
			registro.setTelefones(StringUtils.substringAfter(linha, Constantes.SEPERACAO_TELEFONE));

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

			contato.getTelefones().addAll(TelefoneUtil.processarTelefone(contato.getTelefoneRaw()));

			if (StringUtils.isNoneBlank(contato.getTelefoneRaw()) && contato.getTelefones().isEmpty()) {
				processamentoManualPharserTelefone.add(registro);
			}

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

		System.out.println("##### PROCESSAMENTO MANUAL TELEFONE #####");
		processamentoManualPharserTelefone.forEach(c -> System.out.println(c.getNome() + " - " + c.getTelefones()));

		System.out.println("##### PROCESSAMENTO CONTATOS ERRO #####");
		contatosErro.forEach(c -> System.out.println(c.getLinha() + ": " + c.getNome()));

		List<String> linhasSQL = new ArrayList<String>();
		for (int i = 1; i <= contatos.size(); i++) {
			linhasSQL.addAll(ContatoSqlBuilder.builderSQL(contatos.get(i - 1)));
		}
		linhasSQL.forEach(System.out::println);

		System.out.println("##### GRAVANDO CONTATOS NA BASE #####");
		BaseDao.getInstance().executarComando("DELETE FROM TELEFONE");
		BaseDao.getInstance().executarComando("DELETE FROM CONTATO");
		BaseDao.getInstance().executarComandos(linhasSQL);
		System.out.println("##### GRAVANDO CONCLUIDA #####");
	}

}
