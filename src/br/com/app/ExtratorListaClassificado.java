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

import br.com.app.builder.CategoriaSqlBuilder;
import br.com.app.builder.ContatoSqlBuilder;
import br.com.app.model.CategoriaRegistro;
import br.com.app.model.Contato;
import br.com.app.model.Registro;
import br.com.app.sql.BaseDao;
import br.com.app.util.TelefoneUtil;

public class ExtratorListaClassificado {

	public static void main(String[] args) throws IOException {

		System.out.println("##### RECUPERAR ARQUIVO!");
		Path caminho = Paths.get(System.getProperty("user.home"), Constantes.ARQUIVO_ENTRADA_BASE_DADOS_CLASSIFICADOS);
		Stream<String> baseDados = Files.lines(caminho);

		System.out.println("##### REMOVER LINHAS EM BRANCO!");
		Stream<String> linhas = baseDados.filter(lin -> !lin.isEmpty());

		System.out.println("##### OBTER REGISTROS!");
		List<CategoriaRegistro> registroClassficados = new ArrayList<>();
		String categoriaAtual = null;
		Iterator<String> iterator = linhas.iterator();
		
		while (iterator.hasNext()) {
			String linha = iterator.next();
			

			if(linha.startsWith(Constantes.SEPARACAO_CATEGORIA)){
				categoriaAtual = linha.trim().replace(Constantes.SEPARACAO_CATEGORIA, "");
				registroClassficados.add(new CategoriaRegistro(categoriaAtual));
				continue;
			}
			
			Registro registro = new Registro();
			registro.setNome(StringUtils.substringBefore(linha, Constantes.SEPARACAO_ENDERECO));
			registro.setEndereco(StringUtils.substringBetween(linha, Constantes.SEPARACAO_ENDERECO, Constantes.SEPERACAO_TELEFONE));
			registro.setTelefones(StringUtils.substringAfter(linha, Constantes.SEPERACAO_TELEFONE));

			registroClassficados.get(registroClassficados.size()-1).getRegistros().add(registro);

		}
		
		System.out.println("##### CATEGORIAS ENCONTRADOS: " + registroClassficados.size());
	
		List<String> linhasSQL = new ArrayList<String>();
		Integer linha = 1;
		int ordem = 1;
		List<Contato> contatosErro = new ArrayList<Contato>();
		
		for (CategoriaRegistro categoria : registroClassficados) {
			
			System.out.println("##### OBTER CONTATOS! " +  categoria.getNome());
			List<Contato> contatos = new ArrayList<Contato>();
			
			
			for (Registro registro : categoria.getRegistros()) {
				
				Contato contato = new Contato(registro.getNome());
				contato.setEndereco(registro.getEndereco());
				contato.setTelefoneRaw(registro.getTelefones());
				
				contato.getTelefones().addAll(TelefoneUtil.processarTelefone(contato.getTelefoneRaw()));
				
				contato.setLinha(linha++);
				
				if(linha == 391) {
					System.out.println();
				}
				
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
			System.out.println("##### GRAVANDO CONTATOS NA BASE #####");
			BaseDao.getInstance().executarComando("DELETE FROM CLASSIFICADO");
			BaseDao.getInstance().executarComando("DELETE FROM CATEGORIA");
			BaseDao.getInstance().executarComandos(linhasSQL);
			System.out.println("##### GRAVANDO CONCLUIDA #####");
		}else{
			System.out.println("##### PROCESSAMENTO CONTATOS ERRO #####");
			contatosErro.forEach(c -> System.out.println(c.getLinha() + ": " + c.getNome() + c.getEndereco()));
		}
		
	}

}
