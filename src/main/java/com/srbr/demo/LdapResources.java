package com.srbr.demo;

import java.io.Console;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

public class LdapResources {

	private static final String USER_NAME_ATTR = "USER_NAME_ATTR";
	private static final String USER_EMAIL_ATTR = "USER_EMAIL_ATTR";
	private static final String USER_PHONE_ATTR = "USER_PHONE_ATTR";
	private static final String USER_GROUP_CTX = "USER_GROUP_CTX";
	private static final String USER_GROUP_ATTR = "USER_GROUP_ATTR";
	private static final String USER_ENTRY_SEARCH_CTX = "USER_ENTRY_SEARCH_CTX";
	private static final String USER_ENTRY_SEARCH = "USER_ENTRY_SEARCH";
	private static final String BIND_USER = "BIND_USER";
	private static final String BIND_PASSWORD = "BIND_PASSWORD";
	private static final String BIND_USER_KEY = "BIND_USER_KEY";
	private static final String PADRAO_3 = "3";
	private static final String PADRAO_2 = "2";
	private static final String PADRAO_1 = "1";
	private static final String BIND_USER_CONTEXT = "BIND_USER_CONTEXT";
	private static final String BIND_DOMAIN = "BIND_DOMAIN";
	private static final String BIND_TYPE = "BIND_TYPE";
	private static final String PROVIDER_URL = "PROVIDER_URL";
	private static final String CFG_FILE = "application.properties";

	
	public boolean validateUser(String user, String passwd) throws NamingException, IOException {
		if (user == null || passwd == null) {
			return false;
		}
		
		Properties propCfgFile = loadPropCfgFile();
		Hashtable<String, String> ldapCtx = buildLdapCtx(propCfgFile);

		// Le o endereco do servidor LDAP
		readLdapUrl(propCfgFile, ldapCtx);

		// Le o usuario e a senha
		//String principal = readPrincipal(propCfgFile);
		String principal = user;
		
		readPassword(ldapCtx);

		// Configura o 'principal' no formato correto
		setupSecurityPrincipal(propCfgFile, ldapCtx, principal);

		// Fazendo bind com o LDAP
		InitialDirContext idir = connect2Ldap(ldapCtx);

		// Buscando usuario
		Map<String, String> attrs = getAllUserAttributes(propCfgFile);
		SearchResult resItem = findUser(propCfgFile, ldapCtx, principal, idir, attrs);

		if(resItem != null) {
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * public static void main(String[] args) throws NamingException, IOException {
	 * Properties propCfgFile = loadPropCfgFile(); Hashtable<String, String> ldapCtx
	 * = buildLdapCtx(propCfgFile);
	 * 
	 * // Le o endereco do servidor LDAP readLdapUrl(propCfgFile, ldapCtx);
	 * 
	 * // Le o usuario e a senha String principal = readPrincipal(propCfgFile);
	 * readPassword(ldapCtx);
	 * 
	 * // Configura o 'principal' no formato correto
	 * setupSecurityPrincipal(propCfgFile, ldapCtx, principal);
	 * 
	 * // Fazendo bind com o LDAP InitialDirContext idir = connect2Ldap(ldapCtx);
	 * 
	 * // Buscando usuario Map<String, String> attrs =
	 * getAllUserAttributes(propCfgFile); SearchResult resItem =
	 * findUser(propCfgFile, ldapCtx, principal, idir, attrs);
	 * 
	 * // Buscando grupos do usuario List<String> groups =
	 * findUserGroups(propCfgFile, attrs.get(USER_GROUP_ATTR), resItem);
	 * 
	 * // Extraindo codigos dos grupos do usuario extractGroupCodes(groups);
	 * 
	 * System.out.println("\n\nFim"); }
	 */
	private static Properties loadPropCfgFile() throws IOException {
		Properties propCfgFile = new Properties();
		propCfgFile.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(CFG_FILE));
		return propCfgFile;
	}

	private static void readLdapUrl(Properties prop, Hashtable<String, String> ctx) {
		ctx.put(Context.PROVIDER_URL, readConfig(prop, PROVIDER_URL, "PROVIDER URL", "\n"));
	}

	private static void setupSecurityPrincipal(Properties prop, Hashtable<String, String> ctx, String principal) {
		String padrao = readBindType(prop, principal);
		if (padrao.equals(PADRAO_1) || padrao.equals(PADRAO_2)) {
			String domain = readDomain(prop);
			ctx.put(Context.SECURITY_PRINCIPAL,
					padrao.equals(PADRAO_1) ? buildBindPadrao1(principal, domain) : buildBindParao2(principal, domain));

		} else if (padrao.equals(PADRAO_3)) {
			String userKey = readBindUserKey(prop);
			String usersCtx = readBindUserContext(prop);
			ctx.put(Context.SECURITY_PRINCIPAL, buildBindPadrao3(principal, userKey, usersCtx));
		}
	}

	/*
	 * private static List<String> extractGroupCodes(List<String> groups) {
	 * System.out.println("\nExtraindo codigo dos grupos:"); List<String> groupCodes
	 * = new ArrayList<>(); for (String grp : groups) { String nameRDN =
	 * grp.split(",")[0]; String code = nameRDN.substring(nameRDN.indexOf('=') + 1);
	 * groupCodes.add(code); } for (String code : groupCodes) {
	 * System.out.println("\"" + code + "\""); } return groupCodes; }
	 */
	/*
	 * private static List<String> findUserGroups(Properties prop, String
	 * userGroupAttr, SearchResult resItem) throws NamingException {
	 * System.out.println("Buscar grupos do usuario:");
	 * System.out.println(MessageFormat.
	 * format("Buscando os grupos do usuario no atributo \"{0}\"...",
	 * userGroupAttr)); NamingEnumeration<?> usrGroups =
	 * resItem.getAttributes().get(userGroupAttr).getAll(); if
	 * (!usrGroups.hasMore()) { throw new
	 * RuntimeException("Nenhum grupo encontrado!"); }
	 * System.out.println("Grupos encontrados!");
	 * System.out.println("Filtrar grupos do usuario:"); String userGroupCtx =
	 * readConfig(prop, USER_GROUP_CTX, "USER GROUP CTX", "\n").toUpperCase();
	 * System.out.println(MessageFormat.
	 * format("Filtrando os grupos do usuario pertencentes ao contexto \"{0}\"...",
	 * userGroupCtx)); List<String> groups = new ArrayList<>(); while
	 * (usrGroups.hasMore()) { Object grupoObj = usrGroups.next(); if (grupoObj ==
	 * null) { continue; } String grupo = grupoObj.toString(); if
	 * (grupo.toUpperCase().endsWith(userGroupCtx)) { groups.add(grupo); } } for
	 * (String grp : groups) { System.out.println(grp); } return groups; }
	 */

	private static SearchResult findUser(Properties prop, Hashtable<String, String> ctx, String principal,
			InitialDirContext idir, Map<String, String> attrs) throws NamingException {
		System.out.println("Buscar usuario:");
		String userSearchCtx = readConfig(prop, USER_ENTRY_SEARCH_CTX, "USER ENTRY SEARCH CTX", "\n");
		String userSearch = readConfig(prop, USER_ENTRY_SEARCH, "USER ENTRY SEARCH", "\n");
		System.out.println(MessageFormat.format("\nBuscando o usuario \"{0}\" com a busca \"{1}\" em \"{2}\"...",
				principal, userSearch, userSearchCtx));

		SearchControls cons = new SearchControls();
		cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
		cons.setReturningAttributes(attrs.values().toArray(new String[0]));

		NamingEnumeration<SearchResult> res = idir.search(userSearchCtx, userSearch, new Object[] { principal }, cons);
		if (!res.hasMore()) {
			throw new RuntimeException("Usuario não encontrado!");
		}
		SearchResult resItem = res.next();
		System.out.println("Usuario encontrado!");

		Attribute usrName = resItem.getAttributes().get(attrs.get(USER_NAME_ATTR));
		if (usrName == null) {
			throw new RuntimeException("Atributo nome do Usuario não foi encontrado!");
		}
		System.out.println(MessageFormat.format("Nome: {0}", usrName.get()));

		Attribute usrEmail = resItem.getAttributes().get(attrs.get(USER_EMAIL_ATTR));
		if (usrEmail == null) {
			throw new RuntimeException("Atributo email do Usuario não foi encontrado!");
		}
		System.out.println(MessageFormat.format("Email: {0}", usrEmail.get()));

		Attribute usrPhone = resItem.getAttributes().get(attrs.get(USER_PHONE_ATTR));
		if (usrPhone == null) {
			System.out.println("Atributo telefone do Usuario não foi encontrado!");
		}
		System.out.println(MessageFormat.format("Telefone: {0}", usrPhone.get()));

		return resItem;
	}

	private static Map<String, String> getAllUserAttributes(Properties prop) {
		Map<String, String> attrs = new HashMap<>();
		attrs.put(USER_GROUP_ATTR, readConfig(prop, USER_GROUP_ATTR, "USER GROUP ATTR", "\n"));
		attrs.put(USER_NAME_ATTR, readConfig(prop, USER_NAME_ATTR, "USER NAME ATTR", "\n"));
		attrs.put(USER_EMAIL_ATTR, readConfig(prop, USER_EMAIL_ATTR, "USER EMAIL ATTR", "\n"));
		attrs.put(USER_PHONE_ATTR, readConfig(prop, USER_PHONE_ATTR, "USER PHONE ATTR", "\n"));
		return attrs;
	}

	private static InitialDirContext connect2Ldap(Hashtable<String, String> ctx) throws NamingException {
		System.out.println(MessageFormat.format("\nTentativa de bind (user:\"{0}\";passwd:\"{1}\") em \"{2}\".",
				ctx.get(Context.SECURITY_PRINCIPAL), retrieveHiddenPassword(ctx), ctx.get(Context.PROVIDER_URL)));
		InitialDirContext idir = new InitialDirContext(ctx);
		System.out.println("Conectado!");
		return idir;
	}

	private static String retrieveHiddenPassword(Hashtable<String, String> ctx) {
		String hiddenPwd;
		if (ctx.get(Context.SECURITY_CREDENTIALS) == null || ctx.get(Context.SECURITY_CREDENTIALS).isEmpty()) {
			hiddenPwd = "[sem password]";
		} else {
			hiddenPwd = "****";
		}
		return hiddenPwd;
	}

	private static String buildBindPadrao3(String principal, String userKey, String usersCtx) {
		return userKey + "=" + principal + "," + usersCtx;
	}

	private static String buildBindParao2(String principal, String domain) {
		return domain + "\\" + principal;
	}

	private static String buildBindPadrao1(String principal, String domain) {
		return principal + "@" + domain;
	}

	private static String readBindUserContext(Properties prop) {
		String usersCtx = readConfig(prop, BIND_USER_CONTEXT, "BIND USER CONTEXT", "\n");
		return usersCtx;
	}

	private static String readBindUserKey(Properties prop) {
		String userKey = readConfig(prop, BIND_USER_KEY, "BIND USER KEY", "\n");
		return userKey;
	}

	private static String readDomain(Properties prop) {
		String domain = readConfig(prop, BIND_DOMAIN, "BIND DOMAIN", "\n");
		return domain;
	}

	private static String readBindType(Properties prop, String principal) {
		String padrao = readConfig(prop, BIND_TYPE, "BIND TYPE", MessageFormat.format("\n" + "1- {0}@[BIND_DOMAIN]\n"
				+ "2- [BIND_DOMAIN]\\{0}\n" + "3- [BIND_USER_KEY]={0},[BIND_USER_CONTEXT]\n", principal));
		if (!Arrays.asList(PADRAO_1, PADRAO_2, PADRAO_3).contains(padrao)) {
			throw new RuntimeException("Opção invalida: " + padrao);
		}
		return padrao;
	}

	private static String readPrincipal(Properties prop) {
		String principal = readConfig(prop, BIND_USER, "BIND USER", "\n");
		return principal;
	}

	private static Hashtable<String, String> buildLdapCtx(Properties prop) {
		Hashtable<String, String> props = new Hashtable<>();
		props.put(Context.SECURITY_AUTHENTICATION, "simple");
		props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		props.put(Context.REFERRAL, "follow");
		props.put(Context.SECURITY_CREDENTIALS, prop.getProperty(BIND_PASSWORD));
		return props;
	}

	private static String readConfig(Properties prop, String key, String label, String doc) {
		Console cons = System.console();
		if (prop.getProperty(key) == null) {
			if (cons == null) {
				throw new RuntimeException(
						"Preencha o " + label + " em \"config.properties\" ou " + "utilize um console para executar.");
			}
		}
		String val = prop.getProperty(key);
		if (cons != null) {
			val = cons.readLine("%s%s [%s]:", doc, label, prop.getProperty(key));
			if (val == null) {
				throw new RuntimeException("Operação cancelada!");
			}
			if (!val.isEmpty()) {
				prop.put(key, val);
			} else {
				val = prop.getProperty(key);
			}
		}
		return val;
	}

	private static void readPassword(Hashtable<String, String> ctx) {
		Console cons = System.console();
		if (ctx.get(Context.SECURITY_CREDENTIALS) == null || ctx.get(Context.SECURITY_CREDENTIALS).isEmpty()) {
			if (cons == null) {
				throw new RuntimeException("Preencha o SECURITY_CREDENTIALS em \"config.properties\" ou "
						+ "utilize um console para executar.");
			}
		}
		if (cons != null) {
			char[] pwd = cons.readPassword("\nBIND PASSWORD [%s]:", ctx.get(Context.SECURITY_CREDENTIALS));
			if (pwd == null) {
				ctx.put(Context.SECURITY_CREDENTIALS, null);
			} else if (pwd.length > 0) {
				ctx.put(Context.SECURITY_CREDENTIALS, String.valueOf(pwd));
			} // senao, deixa o que ja foi lido do arquivo
		}
	}
}
