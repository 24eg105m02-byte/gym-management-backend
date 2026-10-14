package com.gym.service;

import com.gym.dto.*;
import com.gym.entity.Member;
import com.gym.entity.User;
import com.gym.enums.EntityStatus;
import com.gym.enums.Role;
import com.gym.enums.UserStatus;
import com.gym.exception.DuplicateResourceException;
import com.gym.exception.UnauthorizedException;
import com.gym.repository.MemberRepository;
import com.gym.repository.TrainerRepository;
import com.gym.repository.UserRepository;
import com.gym.security.CustomUserDetails;
import com.gym.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final TrainerRepository trainerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            MemberRepository memberRepository,
            TrainerRepository trainerRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.trainerRepository = trainerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public LoginResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase().trim())) {
            throw new DuplicateResourceException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(Role.MEMBER);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        Member member = new Member();
        member.setUser(user);
        member.setStatus(EntityStatus.ACTIVE);
        memberRepository.save(member);

        CustomUserDetails details = new CustomUserDetails(user);
        String token = jwtService.generateToken(details);
        return new LoginResponse(token, buildUserResponse(user));
    }

    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        User existing = userRepository.findByEmail(email).orElse(null);
        if (existing != null && existing.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is inactive");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid email or password");
        }

        User user = existing != null ? existing : userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        String token = jwtService.generateToken(new CustomUserDetails(user));
        return new LoginResponse(token, buildUserResponse(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        return buildUserResponse(user);
    }

    @Transactional
    public UserResponse updateAccount(Long userId, java.util.Map<String, String> payload) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        if (payload.containsKey("name") && payload.get("name") != null && !payload.get("name").isBlank()) {
            user.setName(payload.get("name").trim());
        }
        if (payload.containsKey("phone") && payload.get("phone") != null) {
            user.setPhone(payload.get("phone").trim());
        }
        if (payload.containsKey("newPassword") && payload.get("newPassword") != null && !payload.get("newPassword").isBlank()) {
            String newPwd = payload.get("newPassword");
            if (newPwd.length() < 6) {
                throw new com.gym.exception.BadRequestException("Password must be at least 6 characters");
            }
            String currentPwd = payload.get("currentPassword");
            if (currentPwd != null && !currentPwd.isBlank() && !passwordEncoder.matches(currentPwd, user.getPassword())) {
                throw new com.gym.exception.BadRequestException("Current password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(newPwd));
        }

        userRepository.save(user);
        return buildUserResponse(user);
    }

    private UserResponse buildUserResponse(User user) {
        UserResponse response = UserResponse.from(user);
        if (user.getRole() == Role.MEMBER) {
            memberRepository.findByUserId(user.getId())
                    .ifPresent(m -> response.setMemberId(m.getId()));
        } else if (user.getRole() == Role.TRAINER) {
            trainerRepository.findByUserId(user.getId())
                    .ifPresent(t -> response.setTrainerId(t.getId()));
        }
        return response;
    }
}
