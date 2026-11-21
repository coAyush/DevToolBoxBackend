package com.devtoolbox.DevToolBoxRevamp.users.service;

import com.devtoolbox.DevToolBoxRevamp.authentication.repo.UserRepository;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.AccountType;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.SubscriptionDetails;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.User;
import com.devtoolbox.DevToolBoxRevamp.comm.ApiResponse;
import com.devtoolbox.DevToolBoxRevamp.comm.GetDetails;
import com.devtoolbox.DevToolBoxRevamp.comm.ResponseHelper;
import com.devtoolbox.DevToolBoxRevamp.common.CurrentUserPrincipal;
import com.devtoolbox.DevToolBoxRevamp.exception.customException.GenericException;
import com.devtoolbox.DevToolBoxRevamp.landing.dto.responseDto.PriceResponseDto;
import com.devtoolbox.DevToolBoxRevamp.landing.repo.PriceRepository;
import com.devtoolbox.DevToolBoxRevamp.landing.repo.ToolsRepository;
import com.devtoolbox.DevToolBoxRevamp.landing.repo.model.PricingPackage;
import com.devtoolbox.DevToolBoxRevamp.landing.repo.model.Tools;
import com.devtoolbox.DevToolBoxRevamp.users.dto.responseDto.*;
import com.devtoolbox.DevToolBoxRevamp.users.repo.SubscriptionRepository;
import com.devtoolbox.DevToolBoxRevamp.users.repo.ToolUsageRepository;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.Subscription;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ToolUsageRepository toolUsageRepository;
    private final ToolsRepository toolsRepository;
    private final PriceRepository priceRepository;
    @Transactional
    @SneakyThrows
    public ApiResponse dashboardDetails(CurrentUserPrincipal principal){
        log.info("fetching dashboard for user with mail {}",principal.getEmail());
        User user=userRepository.findById(principal.userId).orElse(null);
        if(user==null){
            throw new GenericException("User not exists");
        }
        Subscription subscription=subscriptionRepository.findByUserId(user.getId());
        YearMonth currentMonth = YearMonth.now();

        Instant startDate = currentMonth
                .atDay(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        Instant endDate = currentMonth
                .plusMonths(1)
                .atDay(1)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();

        List<Object[]> toolsUsages=toolUsageRepository.findToolUsageByUserId(user.getId(), startDate, endDate);
        String month=currentMonth.getMonth().toString();
        String formatedMonth =month.charAt(0)+month.substring(1).toLowerCase();
        List<Integer>days=toolsUsages.stream().map(usage->{
            return ((Number)usage[0]).intValue();
        }).toList();
        List<Integer>count=toolsUsages.stream().map(usage->{
            return ((Number)usage[1]).intValue();
        }).toList();
        UserMonthlyStatsDto monthlyStats=UserMonthlyStatsDto.builder()
                .month(formatedMonth)
                .days(days)
                .usages(count)
                .build();
        UserDashboardDetailsDto userDetails=UserDashboardDetailsDto.builder()
                .name(user.getName())
                .imageUrl(user.getImageUrl()!=null?user.getImageUrl():"")
                .isActive(user.isVerified())
                .subscriptionStatus(subscription!= null && subscription.getIsActive())
                .build();
        UserDashboardResponseDto dashboardResponseDto=UserDashboardResponseDto.builder()
                .userDetails(userDetails)
                .userMonthlyStatsDto(monthlyStats)
                .build();

        return ResponseHelper.commonSuccess("User Dashboard Details are ", GetDetails.<UserDashboardResponseDto>builder()
                        .details(dashboardResponseDto)
                .build());
    }
    @Transactional
    public ApiResponse toolSuite(CurrentUserPrincipal currentUserPrincipal) {
        List<Tools> toolsList=toolsRepository.findByCategory(currentUserPrincipal.role);
        List<ToolsSuiteResponseDto>toolsResponse=toolsList.stream()
                .map(tool->ToolsSuiteResponseDto.builder()
                        .toolId(tool.getId())
                        .toolCategory(tool.getToolCategory())
                        .build())
                .toList();
        return ResponseHelper.commonSuccess("Tools Category Wise",GetDetails.<List<ToolsSuiteResponseDto>>builder()
                        .details(toolsResponse)
                .build());
    }

    @Transactional
    public ApiResponse subtoolSuite(CurrentUserPrincipal currentUserPrincipal,Integer toolId) {
        List<Tools> toolsList=toolsRepository.findByCategoryAndId(currentUserPrincipal.role,toolId);
        List<SubToolsSuiteResponseDto>toolsResponse=toolsList.stream()
                .map(tool->SubToolsSuiteResponseDto.builder()
                        .subToolId(tool.getId())
                        .subToolCategory(tool.getToolCategory())
                        .build())
                .toList();
        return ResponseHelper.commonSuccess("Tools Category Wise",GetDetails.<List<SubToolsSuiteResponseDto>>builder()
                .details(toolsResponse)
                .build());
    }

    @Transactional
    public ApiResponse getPlansDetails(CurrentUserPrincipal user) {
        List<PricingPackage>pricings= priceRepository.findAllByIsActiveTrueAndRole("USER");
        List<PriceResponseDto>prices=new ArrayList<>();
                pricings.stream().forEach(price-> {
                            PriceResponseDto p = PriceResponseDto.builder()
                                    .id(price.getId())
                                    .amount(price.getAmount())
                                    .role(price.getRole().toString())
                                    .tier(price.getTier())
                                    .duration(price.getDuration())
                                    .build();
                            prices.add(p);
                        }
                );
        return ResponseHelper.commonSuccess("Package details are : ", GetDetails.<List<PriceResponseDto>>builder()
                .details(prices)
                .build());
    }

    @Transactional
    @SneakyThrows
    public  ApiResponse getProfileData(CurrentUserPrincipal user) {
        User u=userRepository.findById(user.userId).orElseThrow(()->new BadRequestException("User did not exists"));
        Subscription s=subscriptionRepository.findByUserId(u.getId());
        SubscriptionDetails details=toolUsageRepository.getSubscriptionDetails(user.userId);
        String tier=s.getTier();
        Long limit=switch(tier){
            case "FREE" -> 200l;
            case "PRO" -> 500l;
            default -> throw new IllegalStateException("Unexpected value: " + tier);
        };
        boolean isExpired=tier.equalsIgnoreCase("FREE") || details.getEndDate().isAfter(Instant.now());
        Long remaining= details.getUsageCount()==null || limit- details.getUsageCount()<0?0L:limit-details.getUsageCount();
        UserProfileResponseDto profile=UserProfileResponseDto.builder()
                .name(u.getName())
                .email(u.getEmail())
                .profileImageUrl(u.getImageUrl()!=null?u.getImageUrl():u.getName().substring(0,1))
                .phoneNumber(u.getPhone())
                .role(u.getRole().toString())
                .loginProvider(u.getAccountType().equals(AccountType.LOCAL)?"Login via email":"GOOGLE")
                .emailVerified(u.isVerified())
                .createdAt(u.getCreatedAt())
                .subscriptionTier(tier)
                .subscriptionStatus(isExpired)
                .subscriptionStartDate(s.getUpdatedAt())
                .subscriptionEndDate(s.getEndDate())
                .monthlyUsage(details.getUsageCount()==null?0:details.getUsageCount())
                .remainingUsage(remaining)
                .usageLimit(limit)
                .build();
        return ResponseHelper.commonSuccess("Profile Details",GetDetails.<UserProfileResponseDto>builder()
                        .details(profile)
                .build());
    }
}

