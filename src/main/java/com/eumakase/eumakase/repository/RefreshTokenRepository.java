package com.eumakase.eumakase.repository;

import com.eumakase.eumakase.domain.RefreshToken;
import com.eumakase.eumakase.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByRefreshToken(String refreshToken);
    Optional<RefreshToken> findByUser(User user);

    void deleteByUser(User user);

    boolean existsByUser(User user);

    void deleteByUserId(Long userId);
}