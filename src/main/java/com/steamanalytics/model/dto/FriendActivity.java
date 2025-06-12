package com.steamanalytics.model.dto;

import java.util.List;

public class FriendActivity {
    private UserDto friend;
    private List<GameDto> recentGames;

    public static FriendActivityBuilder builder() {
        return new FriendActivityBuilder();
    }

    // Getters e Setters
    public UserDto getFriend() { return friend; }
    public void setFriend(UserDto friend) { this.friend = friend; }
    public List<GameDto> getRecentGames() { return recentGames; }
    public void setRecentGames(List<GameDto> recentGames) { this.recentGames = recentGames; }

    public static class FriendActivityBuilder {
        private UserDto friend;
        private List<GameDto> recentGames;

        public FriendActivityBuilder friend(UserDto friend) { this.friend = friend; return this; }
        public FriendActivityBuilder recentGames(List<GameDto> recentGames) { this.recentGames = recentGames; return this; }

        public FriendActivity build() {
            FriendActivity activity = new FriendActivity();
            activity.friend = this.friend;
            activity.recentGames = this.recentGames;
            return activity;
        }
    }
}