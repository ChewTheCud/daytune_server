package com.eumakase.eumakase.repository;

import com.eumakase.eumakase.domain.User;
import com.eumakase.eumakase.util.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findBySnsId(String snsId);

    // 관리자 로그인·부트스트랩용. 소셜 유저와 이메일이 겹칠 수 있어 역할까지 함께 조회한다
    List<User> findByEmailAndRole(String email, Role role);

    long countByDeletedDateIsNull();

    // 관리자 유저 검색: id 일치 또는 이메일·닉네임 부분 일치
    // id는 숫자 검색어가 아니면 -1(존재하지 않는 id), pattern은 소문자 + '!' 이스케이프 완료 값
    @Query("select u from User u where u.id = :id"
            + " or lower(u.email) like :pattern escape '!'"
            + " or lower(u.nickname) like :pattern escape '!'")
    Page<User> searchForAdmin(@Param("id") Long id, @Param("pattern") String pattern, Pageable pageable);

    // 관리자 이용 현황: 일별 가입 수 [from, to)
    @Query("select cast(u.createdDate as LocalDate), count(u) from User u"
            + " where u.createdDate >= :from and u.createdDate < :to"
            + " group by cast(u.createdDate as LocalDate)")
    List<Object[]> countDailySignups(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}