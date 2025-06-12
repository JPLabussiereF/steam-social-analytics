package com.steamanalytics.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "games")
@EntityListeners(AuditingEntityListener.class)
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "game_seq")
    @SequenceGenerator(name = "game_seq", sequenceName = "games_game_id_seq", allocationSize = 1)
    @Column(name = "game_id")
    private Long gameId;

    @Column(name = "steam_app_id", unique = true, nullable = false)
    private Integer steamAppId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "developer", length = 255)
    private String developer;

    @Column(name = "publisher", length = 255)
    private String publisher;

    @Column(name = "price_initial", precision = 10, scale = 2)
    private BigDecimal priceInitial;

    @Column(name = "price_current", precision = 10, scale = 2)
    private BigDecimal priceCurrent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "jsonb")
    private Map<String, Object> tags;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "categories", columnDefinition = "jsonb")
    private Map<String, Object> categories;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "genres", columnDefinition = "jsonb")
    private Map<String, Object> genres;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "game", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<UserGameLibrary> userLibraries = new HashSet<>();

    // Construtores
    public Game() {}

    public Game(Integer steamAppId, String name) {
        this.steamAppId = steamAppId;
        this.name = name;
    }

    public Game(Integer steamAppId, String name, String description) {
        this.steamAppId = steamAppId;
        this.name = name;
        this.description = description;
    }

    // Getters e Setters
    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public Integer getSteamAppId() {
        return steamAppId;
    }

    public void setSteamAppId(Integer steamAppId) {
        this.steamAppId = steamAppId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public BigDecimal getPriceInitial() {
        return priceInitial;
    }

    public void setPriceInitial(BigDecimal priceInitial) {
        this.priceInitial = priceInitial;
    }

    public BigDecimal getPriceCurrent() {
        return priceCurrent;
    }

    public void setPriceCurrent(BigDecimal priceCurrent) {
        this.priceCurrent = priceCurrent;
    }

    public Map<String, Object> getTags() {
        return tags;
    }

    public void setTags(Map<String, Object> tags) {
        this.tags = tags;
    }

    public Map<String, Object> getCategories() {
        return categories;
    }

    public void setCategories(Map<String, Object> categories) {
        this.categories = categories;
    }

    public Map<String, Object> getGenres() {
        return genres;
    }

    public void setGenres(Map<String, Object> genres) {
        this.genres = genres;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<UserGameLibrary> getUserLibraries() {
        return userLibraries;
    }

    public void setUserLibraries(Set<UserGameLibrary> userLibraries) {
        this.userLibraries = userLibraries;
    }

    // Métodos utilitários
    public void addUserLibrary(UserGameLibrary userLibrary) {
        this.userLibraries.add(userLibrary);
        userLibrary.setGame(this);
    }

    public void removeUserLibrary(UserGameLibrary userLibrary) {
        this.userLibraries.remove(userLibrary);
        userLibrary.setGame(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Game)) return false;
        Game game = (Game) o;
        return steamAppId != null && steamAppId.equals(game.steamAppId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Game{" +
                "gameId=" + gameId +
                ", steamAppId=" + steamAppId +
                ", name='" + name + '\'' +
                ", developer='" + developer + '\'' +
                ", publisher='" + publisher + '\'' +
                '}';
    }
}