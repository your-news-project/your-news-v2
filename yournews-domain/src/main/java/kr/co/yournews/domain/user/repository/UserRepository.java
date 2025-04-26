package kr.co.yournews.domain.user.repository;

import kr.co.yournews.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByNickname(String nickname);
    boolean existsByUsernameAndEmail(String username, String email);

    @Query("""
                SELECT u.id
                FROM user u
                JOIN sub_news sn ON u.id = sn.user.id
                WHERE u.subStatus = true
                  AND sn.newsName = :newsName
            """)
    List<Long> findUserIdsByNewsNameAndSubStatusTrue(@Param("newsName") String newsName);
}
