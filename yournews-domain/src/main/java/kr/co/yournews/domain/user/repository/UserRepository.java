package kr.co.yournews.domain.user.repository;

import kr.co.yournews.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
