package kr.co.yournews.domain.user.service;

import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User save(User user) {
        return userRepository.save(user);
    }

    public Optional<User> readById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> readByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> readByEmail(String email) {
        return userRepository.findByEmailIncludeDeleted(email);
    }

    public List<Long> readAllUserIdsByNewsNameAndSubStatusTrue(String newsName) {
        return userRepository.findUserIdsByNewsNameAndSubStatusTrue(newsName);
    }

    public List<Long> readAllUserIdsByNewsNameAndDailySubStatusTrue(String newsName) {
        return userRepository.findUserIdsByNewsNameAndDailySubStatus(newsName);
    }

    public List<Long> readSoftDeleteUsersBefore(LocalDate localDate) {
        return userRepository.findSoftDeletedUserIdsBefore(localDate);
    }

    public Optional<User> readByUsernameIncludeDeleted(String username) {
        return userRepository.findByUsernameIncludeDeleted(username);
    }

    public Page<User> readAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailIncludeDeleted(email) == 1;
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsernameIncludeDeleted(username) == 1;
    }

    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNicknameIncludeDeleted(nickname) == 1;
    }

    public boolean existsByUsernameAndEmail(String username, String email) {
        return userRepository.existsByUsernameAndEmail(username, email);
    }

    public void deleteAllByIds(List<Long> userIds) {
        userRepository.deleteAllByIds(userIds);
    }
}
