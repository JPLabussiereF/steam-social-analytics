package com.steamanalytics.model.dto;

import java.time.Instant;

public class GameWithPlaytime {
    private GameDto game;
    private Integer playtimeMinutes;
    private Double playtimeHours;
    private Instant lastPlayed;

    public static GameWithPlaytimeBuilder builder() {
        return new GameWithPlaytimeBuilder();
    }

    // Getters e Setters
    public GameDto getGame() { return game; }
    public void setGame(GameDto game) { this.game = game; }
    public Integer getPlaytimeMinutes() { return playtimeMinutes; }
    public void setPlaytimeMinutes(Integer playtimeMinutes) { this.playtimeMinutes = playtimeMinutes; }
    public Double getPlaytimeHours() { return playtimeHours; }
    public void setPlaytimeHours(Double playtimeHours) { this.playtimeHours = playtimeHours; }
    public Instant getLastPlayed() { return lastPlayed; }
    public void setLastPlayed(Instant lastPlayed) { this.lastPlayed = lastPlayed; }

    public static class GameWithPlaytimeBuilder {
        private GameDto game;
        private Integer playtimeMinutes;
        private Double playtimeHours;
        private Instant lastPlayed;

        public GameWithPlaytimeBuilder game(GameDto game) { this.game = game; return this; }
        public GameWithPlaytimeBuilder playtimeMinutes(Integer playtimeMinutes) { this.playtimeMinutes = playtimeMinutes; return this; }
        public GameWithPlaytimeBuilder playtimeHours(Double playtimeHours) { this.playtimeHours = playtimeHours; return this; }
        public GameWithPlaytimeBuilder lastPlayed(Instant lastPlayed) { this.lastPlayed = lastPlayed; return this; }

        public GameWithPlaytime build() {
            GameWithPlaytime gameWithPlaytime = new GameWithPlaytime();
            gameWithPlaytime.game = this.game;
            gameWithPlaytime.playtimeMinutes = this.playtimeMinutes;
            gameWithPlaytime.playtimeHours = this.playtimeHours;
            gameWithPlaytime.lastPlayed = this.lastPlayed;
            return gameWithPlaytime;
        }
    }
}