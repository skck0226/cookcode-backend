package com.swef.cookcode.user.service;

import static com.swef.cookcode.common.ErrorCode.USER_NOT_FOUND;

import com.swef.cookcode.common.error.exception.NotFoundException;
import com.swef.cookcode.user.domain.User;
import com.swef.cookcode.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSimpleService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findByIdAndIsQuit(userId, false).orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
    }
}
