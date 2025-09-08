package com.synergyx.trading.service.userService;

import com.synergyx.trading.apiPayload.code.status.ErrorStatus;
import com.synergyx.trading.apiPayload.exception.GeneralException;
import com.synergyx.trading.config.security.UserAuthentication;
import com.synergyx.trading.config.token.JwtTokenProvider;
import com.synergyx.trading.dto.user.LoginResponseDTO;
import com.synergyx.trading.dto.user.SignupRequestDTO;
import com.synergyx.trading.model.User;
import com.synergyx.trading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserAuthServiceImpl implements UserAuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     */
    @Override
    @Transactional
    public void createUser(SignupRequestDTO request) {

        // 이메일 중복 검사
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new GeneralException(ErrorStatus.AUTH_DUPLICATE_EMAIL);
        }

        // 새 사용자 생성
        User user = User.builder()
                .username(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .agreeMarketing(request.marketing())
                .agreeEvent(request.event())
                .build();

        userRepository.save(user);
        log.info("[SIGNUP] 신규 회원가입 완료 - email={}", user.getEmail());
    }

    /**
     * 로그인
     */
    @Override
    @Transactional
    public LoginResponseDTO login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.AUTH_INVALID_CREDENTIALS));

        // 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new GeneralException(ErrorStatus.AUTH_INVALID_CREDENTIALS);
        }

        // 인증 객체 등록
        setAuthentication(user);

        // 토큰 발급
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        Long expiresIn = jwtTokenProvider.getAccessTokenExpirySeconds();

        // DB에 AccessToken 저장
        user.setAccessToken(accessToken);
        userRepository.save(user);

        log.info("[LOGIN] 토큰 발급 완료 - userId={}", user.getId());

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .expiresIn(expiresIn)
                .userInfo(LoginResponseDTO.LoginUserInfo.builder()
                        .userId(user.getId())
                        .username(user.getUsername())
                        .isNewUser(false)
                        .build())
                .build();
    }

    /**
     * 로그아웃
     */
    @Override
    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._UNAUTHORIZED));

        // DB에서 AccessToken 제거
        user.setAccessToken(null);
        userRepository.save(user);

        // SecurityContext 비우기
        SecurityContextHolder.clearContext();

        log.info("[LOGOUT] 로그아웃 완료 - userId={}", user.getId());
    }

    /**
     * SecurityContext에 인증 객체를 설정
     */
    private void setAuthentication(User user) {
        Authentication authentication = new UserAuthentication(user.getId(), null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("[AUTH] SecurityContext에 인증 객체 설정 완료 - userId={}", user.getId());
    }
}
