package com.steamanalytics.model.dto;

public class GameRecommendation {
    private GameDto game;
    private Integer friendsWhoPlay;
    private Double score;
    private String reason;

    public static GameRecommendationBuilder builder() {
        return new GameRecommendationBuilder();
    }

    // Getters e Setters
    public GameDto getGame() { return game; }
    public void setGame(GameDto game) { this.game = game; }
    public Integer getFriendsWhoPlay() { return friendsWhoPlay; }
    public void setFriendsWhoPlay(Integer friendsWhoPlay) { this.friendsWhoPlay = friendsWhoPlay; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public static class GameRecommendationBuilder {
        private GameDto game;
        private Integer friendsWhoPlay;
        private Double score;
        private String reason;

        public GameRecommendationBuilder game(GameDto game) {
            this.game = game;
            return this;
        }

        public GameRecommendationBuilder friendsWhoPlay(Integer friendsWhoPlay) {
            this.friendsWhoPlay = friendsWhoPlay;
            return this;
        }

        public GameRecommendationBuilder score(Double score) {
            this.score = score;
            return this;
        }

        public GameRecommendationBuilder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public GameRecommendation build() {
            GameRecommendation recommendation = new GameRecommendation();
            recommendation.game = this.game;
            recommendation.friendsWhoPlay = this.friendsWhoPlay;
            recommendation.score = this.score;
            recommendation.reason = this.reason;
            return recommendation;
        }
    }
}