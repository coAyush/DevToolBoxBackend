package com.devtoolbox.DevToolBoxRevamp.users.service;

import com.devtoolbox.DevToolBoxRevamp.authentication.repo.UserRepository;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.User;
import com.devtoolbox.DevToolBoxRevamp.comm.ApiResponse;
import com.devtoolbox.DevToolBoxRevamp.comm.GetDetails;
import com.devtoolbox.DevToolBoxRevamp.comm.ResponseHelper;
import com.devtoolbox.DevToolBoxRevamp.common.CurrentUserPrincipal;
import com.devtoolbox.DevToolBoxRevamp.common.ToolsUsageLogger;
import com.devtoolbox.DevToolBoxRevamp.users.dto.redisDto.CachedUrlDto;
import com.devtoolbox.DevToolBoxRevamp.users.dto.responseDto.UrlDashboardResponseDto;
import com.devtoolbox.DevToolBoxRevamp.users.repo.ToolUsageRepository;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.ToolUsage;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.Url;
import com.devtoolbox.DevToolBoxRevamp.users.repo.userToolsRepo.UrlRepository;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.unbrokendome.base62.Base62;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UrlService {
    private final UrlRepository urlRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final ToolUsageRepository toolUsageRepository;
    private final UrlClickPersistenceService urlClickPersistenceService;
    @Value("${url-redirect:http://localhost:8080/r}")
    private String urlRedirect;
    @SneakyThrows
    @Transactional
    public ApiResponse createUrl(CurrentUserPrincipal user,String url,String alias,Integer toolId,Integer subToolId) {
        User u=userRepository.findById(user.userId).orElse(null);
        if(u==null) {
            log.error("User not found for id {}",user.userId);
            throw new RuntimeException("User not found for id "+user.userId);
        }
        ToolUsage toolUsage=ToolUsage.builder()
                .userId(user.userId)
                .toolId(toolId)
                .toolSubId(subToolId)
                .build();
        toolUsageRepository.save(toolUsage);
        Url existingUrl=urlRepository.findByUserAndOriginalUrlAndDeletedAtIsNull(u,url).orElse(null);
        if(existingUrl!=null) {
            return ResponseHelper.commonSuccess(
                    "URL already exists",
                    GetDetails.<String>builder()
                            .details(urlRedirect + existingUrl.getShortCode())
                            .build()
            );
        }

        Long id=urlRepository.nextId();
        boolean isAlias = alias != null && !alias.isBlank();
        String shortCode=!isAlias?shorCodeGeneration(id):alias.trim();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(7, ChronoUnit.DAYS);
        Url urlEntity=Url.builder()
                .id(id)
                .user(u)
                .shortCode(shortCode)
                .originalUrl(url)
                .customAlias(isAlias)
                .clickCount(0)
                .lastClickedAt(null)
                .deletedAt(null)
                .createdAt(now)
                .updatedAt(now)
                .expiresAt(expiresAt)
                .build();
       Url saved= urlRepository.save(urlEntity);
       String redisKey="url:"+shortCode;
       Duration remaining=Duration.between(Instant.now(),expiresAt);
        CachedUrlDto cache=CachedUrlDto.builder()
                .id(id)
                .originalUrl(url)
                .build();
        String cachedData=objectMapper.writeValueAsString(cache);
       stringRedisTemplate.opsForValue().set(redisKey,cachedData,remaining);
       if(saved==null) {
           log.error("Could not save url {}",url);
           throw new RuntimeException("Error occurred while saving url"+url);
       }
       return ResponseHelper.commonSuccess("Url saved ind db", GetDetails.<String>builder().details(urlRedirect+shortCode).build());
    }

    private String shorCodeGeneration(Long id){
        log.debug("Generating short code for id {}",id);
        String shortcode= Base62.encode(id);
        log.debug("Generated short code is {}",shortcode);
        return shortcode;
    }

    @Transactional
    @SneakyThrows
    public String redirect(String shortCode) {
        String cached_url=stringRedisTemplate.opsForValue().get("url:"+shortCode);
        if(cached_url!=null && !cached_url.isBlank()) {
             CachedUrlDto dto=objectMapper.readValue(cached_url, CachedUrlDto.class);
             stringRedisTemplate.opsForValue().increment("url-clicks:"+dto.getId());
             stringRedisTemplate.opsForValue().set("url-last-clicked:"+dto.getId(), String.valueOf(Instant.now()));
             return dto.getOriginalUrl();
        }
        Url existingUrl=urlRepository.findByShortCodeAndDeletedAtIsNull(shortCode).orElse(null);
        if(existingUrl==null) {
            log.error("Url not found for id {}",shortCode);
            throw new RuntimeException("Url not found for id "+shortCode);
        }
        boolean isExpired=existingUrl.getExpiresAt()!=null &&!existingUrl.getExpiresAt().isAfter(Instant.now());
        if(isExpired) {
            throw new RuntimeException("Url expired");
        }
        Duration remaining=null;
        if(existingUrl.getExpiresAt()!=null) {
            remaining=Duration.between(Instant.now(),existingUrl.getExpiresAt());
        }else {
            remaining = Duration.of(2, ChronoUnit.HOURS);
        }
        CachedUrlDto cache=CachedUrlDto.builder().id(existingUrl.getId()).originalUrl(existingUrl.getOriginalUrl()).build();
        String cachedData=objectMapper.writeValueAsString(cache);
        stringRedisTemplate.opsForValue().set("url:"+shortCode,cachedData ,remaining);
        stringRedisTemplate.opsForValue().increment("url-clicks:"+existingUrl.getId());
        stringRedisTemplate.opsForValue().set("url-last-clicked:"+existingUrl.getId(), String.valueOf(Instant.now()));
        return existingUrl.getOriginalUrl();
    }


    @Transactional
    public ApiResponse getUrlDashboardAnalytics(CurrentUserPrincipal user,Integer toolId,Integer subToolId){
        List<Url> urls=urlRepository.findAllByUserId(user.userId);
        List<UrlDashboardResponseDto>dashboardResponse=new ArrayList<>();
        for(Url url:urls){
            urlClickPersistenceService.preserveUrlClicks(url.getId());
        }
        urls=urlRepository.findAllByUserId(user.userId);

        for (Url url : urls) {
            UrlDashboardResponseDto response =
                    UrlDashboardResponseDto.builder()
                            .id(url.getId())
                            .originalUrl(url.getOriginalUrl())
                            .shortUrl(urlRedirect + url.getShortCode())
                            .clickCount(url.getClickCount())
                            .lastClickedAt(url.getLastClickedAt())
                            .createdAt(url.getCreatedAt())
                            .expiresAt(url.getExpiresAt())
                            .build();

            dashboardResponse.add(response);
        }

        return ResponseHelper.commonSuccess(
                "Url Dashboard analytics data :",
                GetDetails.<List<UrlDashboardResponseDto>>builder()
                        .details(dashboardResponse)
                        .build()
        );
    }
}
