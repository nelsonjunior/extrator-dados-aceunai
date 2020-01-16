package br.com.app.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import br.com.app.Constantes;
import br.com.app.model.Contato;
import br.com.app.util.StringUtil;

public class BaseDao {
	
	private static BaseDao instance;
	
	public static BaseDao getInstance() {
		if (instance == null) {
			instance = new BaseDao();
		}
		return instance;
	}
	
	private BaseDao(){}
	

	public Integer obterIDContato(Contato contato){
	
		Connection con = obterConexao();
		ResultSet rs = null;
		try {
			PreparedStatement preparedStatement = con.prepareStatement("SELECT * FROM Contato WHERE NOME = ? AND ENDERECO = ? AND TELEFONE = ?");
			preparedStatement.setString(1, contato.getNome());
			preparedStatement.setString(2, contato.getEndereco());
			preparedStatement.setString(3, contato.getTelefoneRaw());
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				System.out.println("name = " + rs.getString("nome"));
				return rs.getInt("id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			fecharConexao(con);
			fecharResultSet(rs);
		}
		return null;
	}
	
	public Integer obterIDContatoPorHash(Contato contato){
		
		Connection con = obterConexao();
		ResultSet rs = null;
		try {
			PreparedStatement preparedStatement = con.prepareStatement("SELECT * FROM Contato C WHERE LOWER(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(C.NOME_ASCII || C.ENDERECO || C.TELEFONE, ' ', ''),'.', ''),'-', ''),'/', ''),',', '')) = ?");
			
			String hash = StringUtil.normatizarString(contato.getNome()).concat(contato.getEndereco()).concat(contato.getTelefoneRaw());
			
			hash = hash.replace(" ", "").replace(".", "").replace("-", "").replace("/", "").replace(",", "");
			
			preparedStatement.setString(1, hash.toLowerCase());
			rs = preparedStatement.executeQuery();
			while (rs.next()) {
				System.out.println("name = " + rs.getString("nome"));
				return rs.getInt("id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			fecharConexao(con);
			fecharResultSet(rs);
		}
		return null;
	}
	
	
	public boolean executarComando(String comando){
		
		Connection con = obterConexao();
		ResultSet rs = null;
		try {
			Statement statament = con.createStatement();
			return statament.execute(comando);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			fecharConexao(con);
			fecharResultSet(rs);
		}
		return false;
	}
	
	public boolean executarComandos(List<String> comandos){
		
		Connection con = obterConexao();
		ResultSet rs = null;
		try {
			Statement statament = con.createStatement();
			
			comandos.forEach(c -> {
				try {
					statament.addBatch(c);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			});
			
			return statament.executeBatch().length > 0;
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			fecharConexao(con);
			fecharResultSet(rs);
		}
		return false;
	}
	
	
	private Connection obterConexao() {
		try {
			return DriverManager.getConnection(Constantes.CONEXAO_JDBC_BASE_DADOS);
		} catch (SQLException e) {
			System.err.println(e.getMessage());
		} 
		return null;
	}
	
	private void fecharConexao(Connection connection){
		try {
			if (connection != null)
				connection.close();
		} catch (SQLException e) {
			System.err.println(e);
		}
	}
	
	private void fecharResultSet(ResultSet rs){
		try {
			if (rs != null)
				rs.close();
		} catch (SQLException e) {
			System.err.println(e);
		}
	}

	public static void main(String[] args) throws ClassNotFoundException {
		// load the sqlite-JDBC driver using the current class loader
		Class.forName("org.sqlite.JDBC");

		Connection connection = null;
		try {
			// create a database connection
//			connection = DriverManager.getConnection("jdbc:sqlite:sample.db");
			connection = DriverManager.getConnection(Constantes.CONEXAO_JDBC_BASE_DADOS);
			Statement statement = connection.createStatement();
			statement.setQueryTimeout(30); // set timeout to 30 sec.
			
			ResultSet rs = statement.executeQuery("select * from contato where id = 1");
			while (rs.next()) {
				// read the result set
				System.out.println("name = " + rs.getString("nome"));
				System.out.println("id = " + rs.getInt("id"));
			}
		} catch (SQLException e) {
			// if the error message is "out of memory",
			// it probably means no database file is found
			System.err.println(e.getMessage());
		} finally {
			try {
				if (connection != null)
					connection.close();
			} catch (SQLException e) {
				// connection close failed.
				System.err.println(e);
			}
		}
	}

}
