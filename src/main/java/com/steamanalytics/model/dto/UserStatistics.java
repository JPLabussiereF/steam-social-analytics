package com.steamanalytics.model.dto;

import java.util.List;
import java.util.Map;

public class UserStatistics {
    private Long userId;
    private Integer totalGames;
    private Integer playedGames;
    private Integer unplayedGames;
    private Long totalPlaytimeMinutes;
    private Double totalPlaytimeHours;
    private Double averagePlaytimeMinutes;
    private Double averagePlaytimeHours;
    private Double playedPercentage;
    private GameDto mostPlayedGame;
    private List<GameDto> recentlyPlayed;
    private Map<String, Integer> genreDistribution;
    private Integer friendCount;

    public static UserStatisticsBuilder builder() {
        return new UserStatisticsBuilder();
    }

    // Getters e Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getTotalGames() { return totalGames; }
    public void setTotalGames(Integer totalGames) { this.totalGames = totalGames; }
    public Integer getPlayedGames() { return playedGames; }
    public void setPlayedGames(Integer playedGames) { this.playedGames = playedGames; }
    public Integer getUnplayedGames() { return unplayedGames; }
    public void setUnplayedGames(Integer unplayedGames) { this.unplayedGames = unplayedGames; }
    public Long getTotalPlaytimeMinutes() { return totalPlaytimeMinutes; }
    public void setTotalPlaytimeMinutes(Long totalPlaytimeMinutes) { this.totalPlaytimeMinutes = totalPlaytimeMinutes; }
    public Double getTotalPlaytimeHours() { return totalPlaytimeHours; }
    public void setTotalPlaytimeHours(Double totalPlaytimeHours) { this.totalPlaytimeHours = totalPlaytimeHours; }
    public Double getAveragePlaytimeMinutes() { return averagePlaytimeMinutes; }
    public void setAveragePlaytimeMinutes(Double averagePlaytimeMinutes) { this.averagePlaytimeMinutes = averagePlaytimeMinutes; }
    public Double getAveragePlaytimeHours() { return averagePlaytimeHours; }
    public void setAveragePlaytimeHours(Double averagePlaytimeHours) { this.averagePlaytimeHours = averagePlaytimeHours; }
    public Double getPlayedPercentage() { return playedPercentage; }
    public void setPlayedPercentage(Double playedPercentage) { this.playedPercentage = playedPercentage; }
    public GameDto getMostPlayedGame() { return mostPlayedGame; }
    public void setMostPlayedGame(GameDto mostPlayedGame) { this.mostPlayedGame = mostPlayedGame; }
    public List<GameDto> getRecentlyPlayed() { return recentlyPlayed; }
    public void setRecentlyPlayed(List<GameDto> recentlyPlayed) { this.recentlyPlayed = recentlyPlayed; }
    public Map<String, Integer> getGenreDistribution() { return genreDistribution; }
    public void setGenreDistribution(Map<String, Integer> genreDistribution) { this.genreDistribution = genreDistribution; }
    public Integer getFriendCount() { return friendCount; }
    public void setFriendCount(Integer friendCount) { this.friendCount = friendCount; }

    public static class UserStatisticsBuilder {
        private Long userId;
        private Integer totalGames;
        private Integer playedGames;
        private Integer unplayedGames;
        private Long totalPlaytimeMinutes;
        private Double totalPlaytimeHours;
        private Double averagePlaytimeMinutes;
        private Double averagePlaytimeHours;
        private Double playedPercentage;
        private GameDto mostPlayedGame;
        private List<GameDto> recentlyPlayed;
        private Map<String, Integer> genreDistribution;
        private Integer friendCount;

        public UserStatisticsBuilder userId(Long userId) { this.userId = userId; return this; }
        public UserStatisticsBuilder totalGames(Integer totalGames) { this.totalGames = totalGames; return this; }
        public UserStatisticsBuilder playedGames(Integer playedGames) { this.playedGames = playedGames; return this; }
        public UserStatisticsBuilder unplayedGames(Integer unplayedGames) { this.unplayedGames = unplayedGames; return this; }
        public UserStatisticsBuilder totalPlaytimeMinutes(Long totalPlaytimeMinutes) { this.totalPlaytimeMinutes = totalPlaytimeMinutes; return this; }
        public UserStatisticsBuilder totalPlaytimeHours(Double totalPlaytimeHours) { this.totalPlaytimeHours = totalPlaytimeHours; return this; }
        public UserStatisticsBuilder averagePlaytimeMinutes(Double averagePlaytimeMinutes) { this.averagePlaytimeMinutes = averagePlaytimeMinutes; return this; }
        public UserStatisticsBuilder averagePlaytimeHours(Double averagePlaytimeHours) { this.averagePlaytimeHours = averagePlaytimeHours; return this; }
        public UserStatisticsBuilder playedPercentage(Double playedPercentage) { this.playedPercentage = playedPercentage; return this; }
        public UserStatisticsBuilder mostPlayedGame(GameDto mostPlayedGame) { this.mostPlayedGame = mostPlayedGame; return this; }
        public UserStatisticsBuilder recentlyPlayed(List<GameDto> recentlyPlayed) { this.recentlyPlayed = recentlyPlayed; return this; }
        public UserStatisticsBuilder genreDistribution(Map<String, Integer> genreDistribution) { this.genreDistribution = genreDistribution; return this; }
        public UserStatisticsBuilder friendCount(Integer friendCount) { this.friendCount = friendCount; return this; }

        public UserStatistics build() {
            UserStatistics stats = new UserStatistics();
            stats.userId = this.userId;
            stats.totalGames = this.totalGames;
            stats.playedGames = this.playedGames;
            stats.unplayedGames = this.unplayedGames;
            stats.totalPlaytimeMinutes = this.totalPlaytimeMinutes;
            stats.totalPlaytimeHours = this.totalPlaytimeHours;
            stats.averagePlaytimeMinutes = this.averagePlaytimeMinutes;
            stats.averagePlaytimeHours = this.averagePlaytimeHours;
            stats.playedPercentage = this.playedPercentage;
            stats.mostPlayedGame = this.mostPlayedGame;
            stats.recentlyPlayed = this.recentlyPlayed;
            stats.genreDistribution = this.genreDistribution;
            stats.friendCount = this.friendCount;
            return stats;
        }
    }
}