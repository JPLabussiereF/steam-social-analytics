package com.steamanalytics.controller;

import com.steamanalytics.model.dto.*;
import com.steamanalytics.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = {"http://localhost:3000"})
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Autowired
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Busca dados completos do dashboard do usuário
     */
    @GetMapping("/dashboard/{userId}")
    public ResponseEntity<DashboardData> getDashboard(@PathVariable Long userId) {
        try {
            DashboardData dashboard = analyticsService.buildDashboard(userId);
            return ResponseEntity.ok(dashboard);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Busca estatísticas completas do usuário
     */
    @GetMapping("/users/{userId}/statistics")
    public ResponseEntity<UserStatistics> getUserStatistics(@PathVariable Long userId) {
        try {
            UserStatistics stats = analyticsService.calculateUserStatistics(userId);
            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Busca jogos comuns entre dois usuários
     */
    @GetMapping("/users/{userId}/common-games/{friendId}")
    public ResponseEntity<AnalyticsService.CommonGamesResponse> getCommonGames(
            @PathVariable Long userId,
            @PathVariable Long friendId) {
        try {
            AnalyticsService.CommonGamesResponse commonGames = analyticsService.findCommonGames(userId, friendId);
            return ResponseEntity.ok(commonGames);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Gera recomendações de jogos para um usuário
     */
    @GetMapping("/users/{userId}/recommendations")
    public ResponseEntity<List<GameRecommendation>> getGameRecommendations(@PathVariable Long userId) {
        try {
            List<GameRecommendation> recommendations = analyticsService.generateRecommendations(userId);
            return ResponseEntity.ok(recommendations);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Busca recomendações limitadas
     */
    @GetMapping("/users/{userId}/recommendations/top")
    public ResponseEntity<List<GameRecommendation>> getTopRecommendations(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<GameRecommendation> recommendations = analyticsService.generateRecommendations(userId)
                    .stream()
                    .limit(limit)
                    .toList();
            return ResponseEntity.ok(recommendations);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Busca dados para comparação entre usuários
     */
    @GetMapping("/compare/{userId1}/{userId2}")
    public ResponseEntity<UserComparisonData> compareUsers(
            @PathVariable Long userId1,
            @PathVariable Long userId2) {
        try {
            // Buscar estatísticas de ambos usuários
            UserStatistics stats1 = analyticsService.calculateUserStatistics(userId1);
            UserStatistics stats2 = analyticsService.calculateUserStatistics(userId2);

            // Buscar jogos comuns
            AnalyticsService.CommonGamesResponse commonGames = analyticsService.findCommonGames(userId1, userId2);

            UserComparisonData comparison = UserComparisonData.builder()
                    .user1Stats(stats1)
                    .user2Stats(stats2)
                    .commonGames(commonGames)
                    .build();

            return ResponseEntity.ok(comparison);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Busca estatísticas simplificadas para múltiplos usuários
     */
    @PostMapping("/users/bulk-stats")
    public ResponseEntity<List<UserStatsSummary>> getBulkUserStats(@RequestBody BulkStatsRequest request) {
        List<UserStatsSummary> statsList = request.getUserIds().stream()
                .map(userId -> {
                    try {
                        UserStatistics stats = analyticsService.calculateUserStatistics(userId);
                        return UserStatsSummary.builder()
                                .userId(userId)
                                .totalGames(stats.getTotalGames())
                                .totalPlaytimeHours(stats.getTotalPlaytimeHours())
                                .friendCount(stats.getFriendCount())
                                .build();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .toList();

        return ResponseEntity.ok(statsList);
    }

    /**
     * Endpoint para insights rápidos do usuário
     */
    @GetMapping("/users/{userId}/insights")
    public ResponseEntity<UserInsights> getUserInsights(@PathVariable Long userId) {
        try {
            UserStatistics stats = analyticsService.calculateUserStatistics(userId);
            List<GameRecommendation> recommendations = analyticsService.generateRecommendations(userId)
                    .stream()
                    .limit(3)
                    .toList();

            UserInsights insights = UserInsights.builder()
                    .totalGames(stats.getTotalGames())
                    .totalPlaytimeHours(stats.getTotalPlaytimeHours())
                    .playedPercentage(stats.getPlayedPercentage())
                    .friendCount(stats.getFriendCount())
                    .mostPlayedGame(stats.getMostPlayedGame())
                    .topRecommendations(recommendations)
                    .build();

            return ResponseEntity.ok(insights);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DTOs para responses específicos do Analytics

    public static class UserComparisonData {
        private UserStatistics user1Stats;
        private UserStatistics user2Stats;
        private AnalyticsService.CommonGamesResponse commonGames;

        public static UserComparisonDataBuilder builder() {
            return new UserComparisonDataBuilder();
        }

        // Getters e Setters
        public UserStatistics getUser1Stats() { return user1Stats; }
        public void setUser1Stats(UserStatistics user1Stats) { this.user1Stats = user1Stats; }
        public UserStatistics getUser2Stats() { return user2Stats; }
        public void setUser2Stats(UserStatistics user2Stats) { this.user2Stats = user2Stats; }
        public AnalyticsService.CommonGamesResponse getCommonGames() { return commonGames; }
        public void setCommonGames(AnalyticsService.CommonGamesResponse commonGames) { this.commonGames = commonGames; }

        public static class UserComparisonDataBuilder {
            private UserStatistics user1Stats;
            private UserStatistics user2Stats;
            private AnalyticsService.CommonGamesResponse commonGames;

            public UserComparisonDataBuilder user1Stats(UserStatistics user1Stats) { this.user1Stats = user1Stats; return this; }
            public UserComparisonDataBuilder user2Stats(UserStatistics user2Stats) { this.user2Stats = user2Stats; return this; }
            public UserComparisonDataBuilder commonGames(AnalyticsService.CommonGamesResponse commonGames) { this.commonGames = commonGames; return this; }

            public UserComparisonData build() {
                UserComparisonData data = new UserComparisonData();
                data.user1Stats = this.user1Stats;
                data.user2Stats = this.user2Stats;
                data.commonGames = this.commonGames;
                return data;
            }
        }
    }

    public static class UserStatsSummary {
        private Long userId;
        private Integer totalGames;
        private Double totalPlaytimeHours;
        private Integer friendCount;

        public static UserStatsSummaryBuilder builder() {
            return new UserStatsSummaryBuilder();
        }

        // Getters e Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Integer getTotalGames() { return totalGames; }
        public void setTotalGames(Integer totalGames) { this.totalGames = totalGames; }
        public Double getTotalPlaytimeHours() { return totalPlaytimeHours; }
        public void setTotalPlaytimeHours(Double totalPlaytimeHours) { this.totalPlaytimeHours = totalPlaytimeHours; }
        public Integer getFriendCount() { return friendCount; }
        public void setFriendCount(Integer friendCount) { this.friendCount = friendCount; }

        public static class UserStatsSummaryBuilder {
            private Long userId;
            private Integer totalGames;
            private Double totalPlaytimeHours;
            private Integer friendCount;

            public UserStatsSummaryBuilder userId(Long userId) { this.userId = userId; return this; }
            public UserStatsSummaryBuilder totalGames(Integer totalGames) { this.totalGames = totalGames; return this; }
            public UserStatsSummaryBuilder totalPlaytimeHours(Double totalPlaytimeHours) { this.totalPlaytimeHours = totalPlaytimeHours; return this; }
            public UserStatsSummaryBuilder friendCount(Integer friendCount) { this.friendCount = friendCount; return this; }

            public UserStatsSummary build() {
                UserStatsSummary summary = new UserStatsSummary();
                summary.userId = this.userId;
                summary.totalGames = this.totalGames;
                summary.totalPlaytimeHours = this.totalPlaytimeHours;
                summary.friendCount = this.friendCount;
                return summary;
            }
        }
    }

    public static class UserInsights {
        private Integer totalGames;
        private Double totalPlaytimeHours;
        private Double playedPercentage;
        private Integer friendCount;
        private GameDto mostPlayedGame;
        private List<GameRecommendation> topRecommendations;

        public static UserInsightsBuilder builder() {
            return new UserInsightsBuilder();
        }

        // Getters e Setters
        public Integer getTotalGames() { return totalGames; }
        public void setTotalGames(Integer totalGames) { this.totalGames = totalGames; }
        public Double getTotalPlaytimeHours() { return totalPlaytimeHours; }
        public void setTotalPlaytimeHours(Double totalPlaytimeHours) { this.totalPlaytimeHours = totalPlaytimeHours; }
        public Double getPlayedPercentage() { return playedPercentage; }
        public void setPlayedPercentage(Double playedPercentage) { this.playedPercentage = playedPercentage; }
        public Integer getFriendCount() { return friendCount; }
        public void setFriendCount(Integer friendCount) { this.friendCount = friendCount; }
        public GameDto getMostPlayedGame() { return mostPlayedGame; }
        public void setMostPlayedGame(GameDto mostPlayedGame) { this.mostPlayedGame = mostPlayedGame; }
        public List<GameRecommendation> getTopRecommendations() { return topRecommendations; }
        public void setTopRecommendations(List<GameRecommendation> topRecommendations) { this.topRecommendations = topRecommendations; }

        public static class UserInsightsBuilder {
            private Integer totalGames;
            private Double totalPlaytimeHours;
            private Double playedPercentage;
            private Integer friendCount;
            private GameDto mostPlayedGame;
            private List<GameRecommendation> topRecommendations;

            public UserInsightsBuilder totalGames(Integer totalGames) { this.totalGames = totalGames; return this; }
            public UserInsightsBuilder totalPlaytimeHours(Double totalPlaytimeHours) { this.totalPlaytimeHours = totalPlaytimeHours; return this; }
            public UserInsightsBuilder playedPercentage(Double playedPercentage) { this.playedPercentage = playedPercentage; return this; }
            public UserInsightsBuilder friendCount(Integer friendCount) { this.friendCount = friendCount; return this; }
            public UserInsightsBuilder mostPlayedGame(GameDto mostPlayedGame) { this.mostPlayedGame = mostPlayedGame; return this; }
            public UserInsightsBuilder topRecommendations(List<GameRecommendation> topRecommendations) { this.topRecommendations = topRecommendations; return this; }

            public UserInsights build() {
                UserInsights insights = new UserInsights();
                insights.totalGames = this.totalGames;
                insights.totalPlaytimeHours = this.totalPlaytimeHours;
                insights.playedPercentage = this.playedPercentage;
                insights.friendCount = this.friendCount;
                insights.mostPlayedGame = this.mostPlayedGame;
                insights.topRecommendations = this.topRecommendations;
                return insights;
            }
        }
    }

    // DTOs para requests
    public static class BulkStatsRequest {
        private List<Long> userIds;

        // Getters e Setters
        public List<Long> getUserIds() { return userIds; }
        public void setUserIds(List<Long> userIds) { this.userIds = userIds; }
    }
}