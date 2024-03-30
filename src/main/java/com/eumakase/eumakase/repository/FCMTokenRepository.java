package com.eumakase.eumakase.repository;

import com.eumakase.eumakase.domain.FCMToken;
import com.eumakase.eumakase.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FCMTokenRepository extends JpaRepository<FCMToken, Long> {

    Optional<FCMToken> findByUser(User user);
}
