package kr.co.yournews.domain.user.repository;

import java.util.List;

public interface CustomUserRepository {
    List<Long> findUserIdsByNewsNameAndSubStatusTrue(String newsName);
}
