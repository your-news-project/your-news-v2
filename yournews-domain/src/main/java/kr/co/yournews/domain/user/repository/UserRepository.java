package kr.co.yournews.domain.user.repository;

import kr.co.yournews.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, CustomUserRepository {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByNickname(String nickname);
    boolean existsByUsernameAndEmail(String username, String email);

    @Query(value = "SELECT * FROM user WHERE username = :username", nativeQuery = true)
    Optional<User> findByUsernameIncludeDeleted(@Param("username") String username);

    @Query(value = "SELECT id FROM user WHERE deleted_at IS NOT NULL AND deleted_at <= :dateTime", nativeQuery = true)
    List<Long> findSoftDeletedUserIdsBefore(@Param("dateTime") LocalDate dateTime);

    @Modifying
    @Query(value = "DELETE FROM user WHERE id IN :ids", nativeQuery = true)
    void deleteAllByIds(@Param("ids") List<Long> ids);
}
