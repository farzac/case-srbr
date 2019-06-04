package com.srbr.demo;

import java.io.IOException;

import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApplicationController {
	
	@RequestMapping("/login")
    public String do_login(HttpServletRequest req, HttpSession session,  @RequestParam(value="user", defaultValue="null") String user,
    					@RequestParam(value="passwd", defaultValue="null") String passwd) {
    	
    	// Recursos do LDAP
		LdapResources ldap = new LdapResources();
		
		//Usuario
    	User usuario;
    	
		try {
    		// Valido se o usuário ja possui uma sessão ativa, se sim, apenas retorna que o usuário é valido.  
    		if (session.getAttribute("username") != null && session.getAttribute("username").equals(user)) {
				return "Usuário valido";
			}
		
    		/**
    		 * Valida no servidor LDAP se o usuário esta cadastrado.
    		 */
			if (ldap.validateUser(user, passwd)) {
    			
    			// Instancia a classe User.
				usuario = new User(user,passwd);
				
				// Salva a sessão por 60 segundos.
				session.setAttribute("username", usuario.getName());
				session.setMaxInactiveInterval(60);
				
	            return "Usuário valido";
			} else {
				return "Usuário inválido";
			}
		} catch (NamingException | IOException e) {
			return "Usuário invalido";
		} 
    }
}

