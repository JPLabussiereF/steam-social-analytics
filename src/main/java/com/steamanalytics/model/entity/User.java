package com.steamanalytics.model.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "users_user_id_seq", allocationSize = 1)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "steam_id", unique = true, nullable = false)
    private Long steamId;

    @Column(name = "username", nullable = false, length = 100)
    private String username;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "profile_url")
    private String profileUrl;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "country_code", length = 2)
    private String countryCode;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_login")
    private Instant lastLogin;

    @Column(name = "profile_visibility")
    private Integer profileVisibility = 1;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Version
    private Long version = 0L;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<UserGameLibrary> gameLibrary = new HashSet<>();

    @OneToMany(mappedBy = "requester", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Friendship> sentFriendRequests = new HashSet<>();

    @OneToMany(mappedBy = "addressee", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Friendship> receivedFriendRequests = new HashSet<>();

    // Construtores
    public User() {}

    public User(Long steamId, String username) {
        this.steamId = steamId;
        this.username = username;
    }

    public User(Long steamId, String username, String displayName) {
        this.steamId = steamId;
        this.username = username;
        this.displayName = displayName;
    }

    // Getters e Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getSteamId() {
        return steamId;
    }

    public void setSteamId(Long steamId) {
        this.steamId = steamId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Instant lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Integer getProfileVisibility() {
        return profileVisibility;
    }

    public void setProfileVisibility(Integer profileVisibility) {
        this.profileVisibility = profileVisibility;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Set<UserGameLibrary> getGameLibrary() {
        return gameLibrary;
    }

    public void setGameLibrary(Set<UserGameLibrary> gameLibrary) {
        this.gameLibrary = gameLibrary;
    }

    public Set<Friendship> getSentFriendRequests() {
        return sentFriendRequests;
    }

    public void setSentFriendRequests(Set<Friendship> sentFriendRequests) {
        this.sentFriendRequests = sentFriendRequests;
    }

    public Set<Friendship> getReceivedFriendRequests() {
        return receivedFriendRequests;
    }

    public void setReceivedFriendRequests(Set<Friendship> receivedFriendRequests) {
        this.receivedFriendRequests = receivedFriendRequests;
    }

    // Métodos utilitários
    public void addGameToLibrary(UserGameLibrary gameLibrary) {
        this.gameLibrary.add(gameLibrary);
        gameLibrary.setUser(this);
    }

    public void removeGameFromLibrary(UserGameLibrary gameLibrary) {
        this.gameLibrary.remove(gameLibrary);
        gameLibrary.setUser(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return steamId != null && steamId.equals(user.steamId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", steamId=" + steamId +
                ", username='" + username + '\'' +
                ", displayName='" + displayName + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}