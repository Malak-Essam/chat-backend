package com.malak.chatapp.secuirty;

import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.malak.chatapp.domain.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
	@Value("${jwt.secret}")
	private String secretKey;

	@Value("${jwt.access-token-expiration}")
	private long accessTokenExpiration;

	@Value("${jwt.refresh-token-expiration}")
	private long refreshTokenExpiration;

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(secretKey.getBytes());
	}

	public String generateAccessToken(String username, Role role) {
		return Jwts.builder()
				.subject(username)
				.claim("role", role)
				.claim("type", "access")
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + accessTokenExpiration)).signWith(getSigningKey()).compact();
	}
	
	public String generateRefreshToken(String username, Role role) {
		return Jwts.builder()
				.subject(username)
				.claim("role", role)
				.claim("type", "refresh")
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration)).signWith(getSigningKey()).compact();
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload();
	}

	private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		return claimsResolver.apply(extractAllClaims(token));
	}

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public String extractRole(String token) {
		return extractAllClaims(token).get("role", String.class);
	}

	public String extractTokenType(String token) {
		return extractAllClaims(token).get("type", String.class);
	}
	
	public boolean isTokenValid(String token, String username) {
		return username.equals(extractUsername(token)) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}
	
	public Boolean isAccessToken(String token) {
		return "access".equals(extractTokenType(token));
	}
	
	public Boolean isRefreshToken(String token) {
		return "refresh".equals(extractTokenType(token));
	}

}
