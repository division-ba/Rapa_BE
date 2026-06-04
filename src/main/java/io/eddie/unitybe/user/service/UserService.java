package io.eddie.unitybe.user.service;

import io.eddie.unitybe.common.exception.DiversionException;
import io.eddie.unitybe.common.exception.ErrorCode;
import io.eddie.unitybe.user.domain.User;
import io.eddie.unitybe.user.dto.*;
import io.eddie.unitybe.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @Transactional
    public SignUpResponseDto signup(@Valid SignUpRequestDto requestDto) {
        if (userRepository.existsByEmail(requestDto.email())) throw new DiversionException(ErrorCode.EXIST_EMAIL);
        User user = new User(requestDto.email(),
                passwordEncoder.encode(requestDto.password()),
                requestDto.nickname());
        User savedUser = userRepository.save(user);
        return new SignUpResponseDto(savedUser);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new DiversionException(ErrorCode.USER_NOT_FOUND_BY_EMAIL));
        return new UserDetailsImpl(user.getEmail(), user.getPassword(), user.getRole().toString());
    }


    //로그인
    @Transactional(readOnly = true)
    public KeyPair login(LogInRequestDto request) {
        //이메일 확인
        User user = userRepository.findByEmail(request.email()).orElseThrow(() -> new DiversionException(ErrorCode.LOGIN_NOT_MACH));
        // 비밀번호 확인
        if (!passwordEncoder.matches(request.password(), user.getPassword()))
            throw new DiversionException(ErrorCode.LOGIN_NOT_MACH);
        // 토큰 발급
        return tokenProvider.issueKeyPair(user.getId(), user.getEmail(), user.getNickname(),  user.getRole());
    }
}
