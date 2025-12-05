package com.malak.chatapp.service;

import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.malak.chatapp.domain.RefreshToken;
import com.malak.chatapp.domain.Role;
import com.malak.chatapp.repository.RefreshTokenRepository;
import com.malak.chatapp.secuirty.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtService jwtService;

	@Value("${jwt.refresh-token-expiration}")
	private Long refreshTokenExpiration;

	@Transactional
	public RefreshToken createRefreshToken(String username, Role role) {
		String token = jwtService.generateRefreshToken(username, role);
		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setUsername(username);
		refreshToken.setToken(token);
		refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenExpiration));
		refreshToken.setRevoked(false);

		return refreshTokenRepository.save(refreshToken);
	}

	public Optional<RefreshToken> findByToken(String token) {
		return refreshTokenRepository.findByToken(token);
	}

	public boolean verifyExpiration(RefreshToken token) {
		if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
			refreshTokenRepository.delete(token);
			return false;
		}
		return true;
	}

	public boolean isTokenRevoked(String token) {
		return refreshTokenRepository.findByToken(token).map(RefreshToken::isRevoked).orElse(true);
	}

	@Transactional
	public void revokeToken(String token) {
		refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
			refreshToken.setRevoked(true);
			refreshTokenRepository.save(refreshToken);
		});
	}

	@Transactional
	public void revokeAllUserTokens(String username) {
		refreshTokenRepository.findByUsername(username).forEach(token -> {
			token.setRevoked(true);
			refreshTokenRepository.save(token);
		});
	}

	@Transactional
	public void deleteExpiredTokens() {
		refreshTokenRepository.deleteByExpiryDateBefore(Instant.now());
	}
}
