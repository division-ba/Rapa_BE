package io.eddie.unitybe.user.repository;

import io.eddie.unitybe.user.domain.User;
import io.eddie.unitybe.user.dto.UserAuthInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Query("""
    select new io.eddie.unitybe.user.dto.UserAuthInfo(
        u.id,
        u.email,
        u.password,
        u.role
    )
    from User u
    where u.email = :email
""")
    Optional<UserAuthInfo> findAuthInfoByEmail(String email);

    // 대시보드: 일자별 가입자 그래프용. 전체 유저의 가입시각만.
    @Query("select u.createdAt from User u")
    List<LocalDateTime> findAllCreatedAt();
}
