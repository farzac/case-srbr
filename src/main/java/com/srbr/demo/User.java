package com.srbr.demo;

/**
 * @author fabioz
 *
 * POJO User
 */
public class User {

    private final String name;
    
    private final String passwd;

    public User(String name, String passwd) {
        this.name = name;
        this.passwd = passwd;
    }

	public String getName() {
		return name;
	}

	public String getPasswd() {
		return passwd;
	}
}