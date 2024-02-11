package com.eumakase.eumakase.service;

import com.eumakase.eumakase.domain.User;
import com.eumakase.eumakase.repository.UserRepository;
import com.eumakase.eumakase.util.SecurityUtils;
import org.springframework.stereotype.Service;


import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Long getCurrentUserId() {
        String username = SecurityUtils.getCurrentUsername(); // 위 메서드를 호출하여 사용자명 가져오기
        if (username != null) {
            Optional<User> userOptional = userRepository.findByEmail(username);
            if (userOptional.isPresent()) {
                User user = userOptional.get(); // Optional에서 User 인스턴스를 가져옴
                return user.getId(); // 사용자 ID 반환
            }
        }
        return null; // 사용자 정보를 찾을 수 없는 경우
    }
}