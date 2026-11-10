package com.example.PortalMedical.DTO;

public class ResetPassword {
    private String token;
    private String password;


    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public ResetPassword() {
        super();
    }
	public String getPassword() {
	        return password;
	    }
	
}

