package com.malak.chatapp.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.malak.chatapp.domain.RefreshToken;



@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>{
Optional<RefreshToken> findByToken(String token);

List<RefreshToken> findByUsername(String username);

void deleteByUsername(String username);

void deleteByToken(String token);

void deleteByExpiryDateBefore(Instant now);
}
