package com.steamanalytics.model.dto;

import java.util.List;

public class DashboardData {
    private UserStatistics userStatistics;
    private List<GameWithPlaytime> topGames;
    private List<GameWithPlaytime> recentGames;
    private List<GameRecommendation> recommendations;
    private List<FriendActivity> friendsActivity;

    public static DashboardDataBuilder builder() {
        return new DashboardDataBuilder();
    }

    // Getters e Setters
    public UserStatistics getUserStatistics() { return userStatistics; }
    public void setUserStatistics(UserStatistics userStatistics) { this.userStatistics = userStatistics; }
    public List<GameWithPlaytime> getTopGames() { return topGames; }
    public void setTopGames(List<GameWithPlaytime> topGames) { this.topGames = topGames; }
    public List<GameWithPlaytime> getRecentGames() { return recentGames; }
    public void setRecentGames(List<GameWithPlaytime> recentGames) { this.recentGames = recentGames; }
    public List<GameRecommendation> getRecommendations() { return recommendations; }
    public void setRecommendations(List<GameRecommendation> recommendations) { this.recommendations = recommendations; }
    public List<FriendActivity> getFriendsActivity() { return friendsActivity; }
    public void setFriendsActivity(List<FriendActivity> friendsActivity) { this.friendsActivity = friendsActivity; }

    public static class DashboardDataBuilder {
        private UserStatistics userStatistics;
        private List<GameWithPlaytime> topGames;
        private List<GameWithPlaytime> recentGames;
        private List<GameRecommendation> recommendations;
        private List<FriendActivity> friendsActivity;

        public DashboardDataBuilder userStatistics(UserStatistics userStatistics) { this.userStatistics = userStatistics; return this; }
        public DashboardDataBuilder topGames(List<GameWithPlaytime> topGames) { this.topGames = topGames; return this; }
        public DashboardDataBuilder recentGames(List<GameWithPlaytime> recentGames) { this.recentGames = recentGames; return this; }
        public DashboardDataBuilder recommendations(List<GameRecommendation> recommendations) { this.recommendations = recommendations; return this; }
        public DashboardDataBuilder friendsActivity(List<FriendActivity> friendsActivity) { this.friendsActivity = friendsActivity; return this; }

        public DashboardData build() {
            DashboardData data = new DashboardData();
            data.userStatistics = this.userStatistics;
            data.topGames = this.topGames;
            data.recentGames = this.recentGames;
            data.recommendations = this.recommendations;
            data.friendsActivity = this.friendsActivity;
            return data;
        }
    }
}