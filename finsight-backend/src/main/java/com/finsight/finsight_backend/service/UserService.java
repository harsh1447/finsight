package com.finsight.finsight_backend.service;

import com.finsight.finsight_backend.entity.User;
import java.util.Optional;

public interface UserService {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
