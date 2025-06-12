package com.steamanalytics.model.dto;

import com.steamanalytics.model.entity.User;
import java.time.Instant;

public class UserDto {
    private Long userId;
    private Long steamId;
    private String username;
    private String displayName;
    private String avatarUrl;
    private String countryCode;
    private Instant lastLogin;
    private Boolean isActive;

    public static UserDto from(User user) {
        if (user == null) return null;

        UserDto dto = new UserDto();
        dto.userId = user.getUserId();
        dto.steamId = user.getSteamId();
        dto.username = user.getUsername();
        dto.displayName = user.getDisplayName();
        dto.avatarUrl = user.getAvatarUrl();
        dto.countryCode = user.getCountryCode();
        dto.lastLogin = user.getLastLogin();
        dto.isActive = user.getIsActive();
        return dto;
    }

    // Getters e Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getSteamId() { return steamId; }
    public void setSteamId(Long steamId) { this.steamId = steamId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public Instant getLastLogin() { return lastLogin; }
    public void setLastLogin(Instant lastLogin) { this.lastLogin = lastLogin; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}