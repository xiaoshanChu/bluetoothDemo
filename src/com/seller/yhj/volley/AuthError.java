package com.seller.yhj.volley;

public class AuthError extends VolleyError{
	
	private static final long serialVersionUID = 1L;

	public AuthError(NetworkResponse networkResponse) {
        super(networkResponse);
    }

    public AuthError() {
        super();
    }
}
