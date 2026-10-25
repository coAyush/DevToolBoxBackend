package com.devtoolbox.DevToolBoxRevamp.authentication.service;

import com.devtoolbox.DevToolBoxRevamp.authentication.event.RefreshTokenEvent;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.UserRepository;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.AuthProvider;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.Roles;
import com.devtoolbox.DevToolBoxRevamp.comm.ApiResponse;
import com.devtoolbox.DevToolBoxRevamp.comm.GetDetails;
import com.devtoolbox.DevToolBoxRevamp.comm.ResponseHelper;
import com.devtoolbox.DevToolBoxRevamp.common.EmailService;
import com.devtoolbox.DevToolBoxRevamp.common.OtpService;
import com.devtoolbox.DevToolBoxRevamp.authentication.dto.requestdto.LoginRequestDto;
import com.devtoolbox.DevToolBoxRevamp.authentication.dto.requestdto.OtpVerificationDto;
import com.devtoolbox.DevToolBoxRevamp.authentication.dto.requestdto.UserRegistrationRequestDto;
import com.devtoolbox.DevToolBoxRevamp.authentication.dto.responseDto.OtpVerificationResponseDto;
import com.devtoolbox.DevToolBoxRevamp.exception.GenericException;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.User;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.AccountType;
import com.devtoolbox.DevToolBoxRevamp.security.JwtService;
import com.devtoolbox.DevToolBoxRevamp.users.repo.SubscriptionRepository;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.Subscription;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.UserTier;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisConnectionFactory redisConnectionFactory;
    private final OtpService  otpService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private static final String REDIS_KEY="Otp:user_id:";
    @Value("${token.access-timeout}")
    private Duration accessTimeOut;
    private static final String VALID_ACCESS_TOKEN = "access-token:userId:";
    private static final Duration OTP_EXPIRY = Duration.ofMinutes(5);
    private final JwtService jwtService;
    private final ApplicationEventPublisher eventPublisher;
    private final SubscriptionRepository subscriptionRepository;
    @Value("${frontend.url:http://localhost:5173}")
    private String frontendUrl;
    @Transactional
    public ApiResponse registerUser (UserRegistrationRequestDto userRegistrationRequestDto) throws GenericException {
        log.info("register endpoint hit for email : {}",userRegistrationRequestDto.getEmail());
        if(userRepository.existsByEmail(userRegistrationRequestDto.getEmail())){
            throw new GenericException("Email already exists");
        }
        String password="";
        if(userRegistrationRequestDto.getAccountType().equals(AccountType.LOCAL)){
            password= passwordEncoder.encode(userRegistrationRequestDto.getPassword());
        }
        User user=User.builder()
                .name(userRegistrationRequestDto.getName())
                .email(userRegistrationRequestDto.getEmail())
                .phone(userRegistrationRequestDto.getPhone())
                .password(password)
                .role(String.valueOf(userRegistrationRequestDto.getRole()))
                .accountType(userRegistrationRequestDto.getAccountType())
                .build();
        User savedUser=userRepository.saveAndFlush(user);
        Subscription subscription= Subscription.builder()
                .userId(savedUser.getId())
                .isActive(false)
                .tier(UserTier.FREE.name())
                .build();
        subscriptionRepository.save(subscription);
        String key=REDIS_KEY+savedUser.getId();
        if(Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))){
            throw new GenericException("Wait before requesting for another otp");
        }
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            String otp = otpService.generateOtp();
                            log.info("Saving OTP to Redis key [{}]", key);
                            stringRedisTemplate.opsForValue().set(key, otp, OTP_EXPIRY);

                            //temp

                            String savedOtp = stringRedisTemplate.opsForValue()
                                    .get(key);

                        //    Long ttl = stringRedisTemplate.getExpire(key);

                        //    log.info("Redis key exists: {}",
                         //           Boolean.TRUE.equals(stringRedisTemplate.hasKey(key)));

                        //    log.info("Redis TTL remaining: {} seconds", ttl);

                       //     log.info("OTP read-back successful: {}",
                           //        otp.equals(savedOtp));

                            log.info("Sending OTP email to [{}]", userRegistrationRequestDto.getEmail());
                            emailService.sendOtp(
                                    userRegistrationRequestDto.getEmail(),
                                    "One Time Password for validation",
                                    otp
                            );
                        } catch (Exception ex) {
                            log.error("Error during post-commit OTP generation or dispatch: ", ex);
                        }
                    //    diagnoseRedis();
                    }
                }

        );
        return ResponseHelper.success("Otp sent to your mail successfully");
    }

    @SneakyThrows
    public ApiResponse resendOtp(String email){
       User user= userRepository.findByEmail(email);
       String key=REDIS_KEY+user.getId();
       if(Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))){
           throw new GenericException("Wait for 5 minutes before resending the otp");
       }
       String otp= otpService.generateOtp();
       stringRedisTemplate.opsForValue().set(key, otp, OTP_EXPIRY);
       emailService.sendOtp(user.getEmail(), "One Time Password for validation", otp);
       return ResponseHelper.success("Otp sent to your mail successfully");
    }


    @SneakyThrows
    @Transactional
    public ApiResponse verifyOtp(OtpVerificationDto otpVerificationDto, HttpServletRequest request, HttpServletResponse response){
        log.info("verify endpoint hit for email : {}",otpVerificationDto.getEmail());
        String email=otpVerificationDto.getEmail();
        String otp=otpVerificationDto.getOtp();

        if(email==null||email.isBlank()){
            throw new GenericException("Invalid email or email not entered ");
        }
        User user= userRepository.findByEmail(email);
        if(user==null){
            throw new  GenericException("User does not exists");
        }
        String Key=REDIS_KEY+user.getId();
        String savedOtp=stringRedisTemplate.opsForValue().get(Key);
        if(savedOtp==null || !savedOtp.equals(otp)){
            throw new GenericException("Otp verification failed");
        }
        int flag=userRepository.updateIsVerifiedByiD(user.getId());
        stringRedisTemplate.delete(Key);
        if(flag<0){
            throw new RuntimeException("Internal Server error");
        }
        String accessToken=jwtService.generateAccessToken(user);
        String accessTokenKey=VALID_ACCESS_TOKEN+user.getId();
        String refreshToken=jwtService.generateRefreshToken(user);
        stringRedisTemplate.opsForValue().set(accessTokenKey,accessToken,accessTimeOut.getSeconds());
        eventPublisher.publishEvent(RefreshTokenEvent.builder()
                        .user(user)
                        .refreshToken(refreshToken)
                .build());

        boolean isHttps=frontendUrl.toLowerCase().startsWith("https");
        String sameSite = isHttps ? "None" : "Lax";
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .sameSite(sameSite)
                .maxAge(15*60)
                .secure(isHttps)
                .path("/api")
                .build();


        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // Use true with HTTPS
                .path("/api")
                .sameSite(sameSite)
                .maxAge(7 * 24 * 60 * 60)
                .build();


        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        response.setStatus(HttpServletResponse.SC_OK);

        return ResponseHelper.commonSuccess("Otp verified successfully",GetDetails.<OtpVerificationResponseDto>builder()
                        .details(OtpVerificationResponseDto.builder()
                                .role(user.getRole())
                                .build())
                .build());
    }

    public void diagnoseRedis() {
        try (var connection = redisConnectionFactory.getConnection()) {
            log.info("Spring Redis database size: {}",
                    connection.serverCommands().dbSize());

            log.info("Spring Redis server info: {}",
                    connection.serverCommands().info("server"));
        } catch (Exception e) {
            log.error("Redis diagnostic failed", e);
        }
    }

    @Transactional(readOnly = true)
    @SneakyThrows
    public ApiResponse login(LoginRequestDto loginRequestDto) {
        String email=loginRequestDto.getEmail();
        String password=loginRequestDto.getPassword();
        if(email.isBlank() || password.isBlank()){
            throw new GenericException("Invalid email or password");
        }
        User user= userRepository.findByEmail(email);
        if(user==null){
            throw new GenericException("User does not exists");
        }
        if(!passwordEncoder.matches(loginRequestDto.getPassword(),user.getPassword())){
            throw new GenericException("Invalid password");
        }
        String otp= otpService.generateOtp();
        String key=REDIS_KEY+user.getId();
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        stringRedisTemplate.opsForValue().set(key, otp, OTP_EXPIRY);
                        emailService.sendOtp(user.getEmail(), "One Time Password for validation", otp);
                    }
                }
        );
        return ResponseHelper.success("Otp sent to your mail successfully");
    }

    @SneakyThrows
    public void oatuhLogin(String role, String provider, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Roles roles;
        AuthProvider authProvider;
        try{
            authProvider = AuthProvider.valueOf(provider.toUpperCase());
        }catch(IllegalArgumentException e){
            throw new GenericException("Invalid provider ");
        }
        try{
            roles=Roles.valueOf(role.toUpperCase());
        }catch(IllegalArgumentException e){
            throw new GenericException("Invalid role ");
        }
        HttpSession session=request.getSession(true);
        session.setAttribute("authProvider",authProvider);
        session.setAttribute("roles",roles);
        log.info("Context path {}",request.getContextPath());
        log.info("OAuth initiation reached");
        log.info("Provider: {}", provider);
        log.info("Role: {}", roles);
        log.info("Context path: {}", request.getContextPath());

        String redirectUrl = request.getContextPath()
                + "/oauth2/authorization/"
                + provider.toLowerCase();

        log.info("Redirect URL: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
        response.sendRedirect(
                request.getContextPath()
                        + "/oauth2/authorization/"
                        + provider.toLowerCase()
        );    }

    @Transactional
    @SneakyThrows
    public void regenerateAccessToken(HttpServletRequest request, HttpServletResponse response, String refreshCookie) {
       log.debug("In regenerate access token");
       boolean flag= jwtService.isRefreshToken(refreshCookie);
       log.debug("Is Refresh token valid {}",flag);
       if(!flag){
           response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
           response.setContentType("application/json");
           response.setCharacterEncoding("UTF-8");
           response.getWriter().write("""
                   {
                        "error": "invalid_grant",
                        "error_description": "The refresh token is invalid, expired, or revoked."
                   }
                   """);
           return;
       }
       String email= jwtService.extractEmail(refreshCookie);
       log.debug("email : {}",email);
       User user= userRepository.findByEmail(email);
       log.debug("user : {}",user);
       if(user==null){
           response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
           response.setContentType("application/json");
           response.setCharacterEncoding("UTF-8");
           return;
       }
       String accessToken=jwtService.generateAccessToken(user);

       String accessTokenKey=VALID_ACCESS_TOKEN+user.getId();
       stringRedisTemplate.opsForValue().set(accessTokenKey,accessToken,accessTimeOut.getSeconds());
       log.debug("Access token has been set to {}",accessToken);
        Cookie accessCookie = new Cookie("accessToken", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(false);
        accessCookie.setPath("/api");
        accessCookie.setMaxAge(15 * 60);

        response.addCookie(accessCookie);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
