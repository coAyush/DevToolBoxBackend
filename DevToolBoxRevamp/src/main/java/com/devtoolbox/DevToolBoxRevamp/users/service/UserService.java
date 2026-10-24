package com.devtoolbox.DevToolBoxRevamp.users.service;

import com.devtoolbox.DevToolBoxRevamp.authentication.repo.UserRepository;
import com.devtoolbox.DevToolBoxRevamp.authentication.repo.model.User;
import com.devtoolbox.DevToolBoxRevamp.comm.ApiResponse;
import com.devtoolbox.DevToolBoxRevamp.comm.GetDetails;
import com.devtoolbox.DevToolBoxRevamp.comm.ResponseHelper;
import com.devtoolbox.DevToolBoxRevamp.common.CurrentUserPrincipal;
import com.devtoolbox.DevToolBoxRevamp.exception.GenericException;
import com.devtoolbox.DevToolBoxRevamp.users.dto.responseDto.UserDashboardDetailsDto;
import com.devtoolbox.DevToolBoxRevamp.users.dto.responseDto.UserDashboardResponseDto;
import com.devtoolbox.DevToolBoxRevamp.users.dto.responseDto.UserMonthlyStatsDto;
import com.devtoolbox.DevToolBoxRevamp.users.repo.SubscriptionRepository;
import com.devtoolbox.DevToolBoxRevamp.users.repo.ToolUsageRepository;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.Subscription;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ToolUsageRepository toolUsageRepository;
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
}
