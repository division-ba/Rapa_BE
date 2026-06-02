package io.eddie.unitybe.user.service;

import io.eddie.unitybe.common.exception.DiversionException;
import io.eddie.unitybe.common.exception.ErrorCode;
import io.eddie.unitybe.user.domain.User;
import io.eddie.unitybe.user.dto.SignUpRequestDto;
import io.eddie.unitybe.user.dto.SignUpResponseDto;
import io.eddie.unitybe.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignUpResponseDto signup(@Valid SignUpRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.email())) throw new DiversionException(ErrorCode.EXIST_EMAIL);
        User user = new User(requestDto.email(),
                passwordEncoder.encode(requestDto.password()),
                requestDto.nickname());
        User savedUser = userRepository.save(user);
        return new SignUpResponseDto(savedUser);
    }
}
