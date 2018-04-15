package com.example.demo.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.demo.user.ApplicationUser;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import static com.example.demo.security.SecurityConstants.EXPIRATION_TIME;
import static com.example.demo.security.SecurityConstants.SECRET;
import static com.example.demo.security.SecurityConstants.HEADER_STRING;
import static com.example.demo.security.SecurityConstants.TOKEN_PREFIX;

public class JWTAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	private AuthenticationManager authenticationManager;

	public JWTAuthenticationFilter(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest req,
												HttpServletResponse res) throws AuthenticationException {
		try {
			ApplicationUser creds = new ObjectMapper().
					readValue(req.getInputStream(), ApplicationUser.class);

			return authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(
							creds.getUserName(),
							creds.getPassword(),
							new ArrayList<>()));

		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	@Override
	protected void successfulAuthentication(HttpServletRequest req,
											HttpServletResponse res,
											FilterChain chain,
											Authentication auth) {
		String token = Jwts.builder()
				.setSubject(((User)auth.getPrincipal()).getUsername())
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
				.signWith(SignatureAlgorithm.HS512, SECRET.getBytes())
				.compact();
		res.addHeader(HEADER_STRING, TOKEN_PREFIX + token);
				
		
	}

}
