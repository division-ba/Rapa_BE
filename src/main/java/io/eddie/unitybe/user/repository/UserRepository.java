package io.eddie.unitybe.user.repository;

import io.eddie.unitybe.user.domain.User;
import io.eddie.unitybe.user.dto.UserAuthInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
}
