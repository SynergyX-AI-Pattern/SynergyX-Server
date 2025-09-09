package com.synergyx.trading.service.userService;

import com.synergyx.trading.dto.user.LoginResponseDTO;
import com.synergyx.trading.dto.user.SignupRequestDTO;

public interface UserAuthService {

    // 회원가입
    void createUser(SignupRequestDTO request);

    // 로그인
    LoginResponseDTO login(String email, String password);

    // 로그아웃
    void logout(Long userId);
}
