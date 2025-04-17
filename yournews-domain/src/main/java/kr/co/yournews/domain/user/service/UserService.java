package kr.co.yournews.domain.user.service;

import kr.co.yournews.domain.user.entity.User;
import kr.co.yournews.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByUsernameAndEmail(String username, String email) {
        return userRepository.existsByUsernameAndEmail(username, email);
    }
}
