package com.srbr.demo;

import java.io.IOException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

/**
 * @author fabioz
 * 
 * Classe responsável pela integração com LDAP.
 *
 */
public class LdapResources {
	
	public boolean validateUser(String user, String passwd) throws NamingException, IOException {
		if (user == null || passwd == null) {
			return false;
		}
		LdapContext ctx = null;
		String username = user;
		String password = passwd;
		String PROVIDER_URL = "ldap://localhost:389";
		
		try {
			
			/**
			 * Regra para se comunicar com servidor LDAP e realizar a autenticação.
			*/
            Hashtable<String, String> env = new Hashtable<>();  
            env.put(Context.INITIAL_CONTEXT_FACTORY,  "com.sun.jndi.ldap.LdapCtxFactory");  
            env.put(Context.PROVIDER_URL, PROVIDER_URL);
            env.put(Context.SECURITY_AUTHENTICATION, "Simple");  
            env.put(Context.SECURITY_PRINCIPAL, "cn="+username+",dc=ldapserver,dc=com");
            env.put(Context.SECURITY_CREDENTIALS, password);  
            ctx = new InitialLdapContext(env, null);  
            System.out.println("Connection Successful.");
			ctx.close();
			return true;
		} catch (NamingException nex) {
			System.out.println("LDAP Connection: FAILED");
			return false;
		}
	}
}