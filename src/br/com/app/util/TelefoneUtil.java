package br.com.app.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import br.com.app.model.Telefone;

public class TelefoneUtil {

	private static final Pattern TELEFONE_0800 = Pattern.compile("[0]{1}[8]{1}[0]{2} [0-9]{7}");
	private static final Pattern TELEFONE_9_DIG = Pattern.compile("[8-9]{0,1}[0-9]{4}-[0-9]{4}");
	private static final Pattern TELEFONE_9_DIG_DDD = Pattern.compile("[0-9]{2} [8-9]{0,1}[0-9]{4}-[0-9]{4}");
	private static final Pattern TELEFONE = Pattern.compile("[0-9]{4}-[0-9]{4}");
	private static final Pattern TELEFONE_PART = Pattern.compile("[0-9]{4}");

	private static final Pattern TELEFONE_ESPECIAL_PART = Pattern.compile("[1]{1}[9]{1}[0-9]{1}");

	public static List<Telefone> processarTelefone(String telefoneRaw) {
		List<Telefone> retorno = new ArrayList<Telefone>();
		try {
			String[] split = telefoneRaw.split("/");
			for (String part : split) {

				part = StringUtils.trim(part);

				Matcher mTelefone = TELEFONE.matcher(part);
				Matcher mTelefone9Dig = TELEFONE_9_DIG.matcher(part);
				Matcher mTelefone9DigDDD = TELEFONE_9_DIG_DDD.matcher(part);
				Matcher mTelefoneEspecial = TELEFONE_ESPECIAL_PART.matcher(part);
				Matcher mTelefone0800 = TELEFONE_0800.matcher(part);

				if (mTelefone.matches() || mTelefone9Dig.matches() || mTelefone9DigDDD.matches()
						|| mTelefoneEspecial.matches() || mTelefone0800.matches()) {
					retorno.add(new Telefone(part));
				} else {
					Matcher mTelefonePart = TELEFONE_PART.matcher(part);
					if (mTelefonePart.matches()) {
						retorno.add(new Telefone(
								retorno.get(retorno.size() - 1).getTelefone().substring(0, 5).concat(part)));
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
