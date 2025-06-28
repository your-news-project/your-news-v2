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
    boolean existsByUsernameAndEmail(String username, String email);

    @Query(value = "SELECT * FROM user WHERE email = :email", nativeQuery = true)
    Optional<User> findByEmailIncludeDeleted(@Param("email") String email);

    @Query(value = "SELECT * FROM user WHERE username = :username", nativeQuery = true)
    Optional<User> findByUsernameIncludeDeleted(@Param("username") String username);

    @Query(value = "SELECT id FROM user WHERE deleted_at IS NOT NULL AND deleted_at <= :dateTime", nativeQuery = true)
    List<Long> findSoftDeletedUserIdsBefore(@Param("dateTime") LocalDate dateTime);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM user WHERE email = :email)", nativeQuery = true)
    Long existsByEmailIncludeDeleted(@Param("email") String email);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM user WHERE username = :username)", nativeQuery = true)
    Long existsByUsernameIncludeDeleted(@Param("username") String username);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM user WHERE nickname = :nickname)", nativeQuery = true)
    Long existsByNicknameIncludeDeleted(@Param("nickname") String nickname);

    @Modifying
    @Query(value = "DELETE FROM user WHERE id IN :ids", nativeQuery = true)
    void deleteAllByIds(@Param("ids") List<Long> ids);
}
