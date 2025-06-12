package com.steamanalytics.model.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "user_game_library",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "game_id"}))
public class UserGameLibrary {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_game_library_seq")
    @SequenceGenerator(name = "user_game_library_seq", sequenceName = "user_game_library_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(name = "playtime_total")
    private Integer playtimeTotal = 0; // em minutos

    @Column(name = "playtime_2weeks")
    private Integer playtimeTwoWeeks = 0; // em minutos

    @Column(name = "purchased_at")
    private Instant purchasedAt;

    @Column(name = "last_played")
    private Instant lastPlayed;

    // Construtores
    public UserGameLibrary() {}

    public UserGameLibrary(User user, Game game) {
        this.user = user;
        this.game = game;
    }

    public UserGameLibrary(User user, Game game, Integer playtimeTotal) {
        this.user = user;
        this.game = game;
        this.playtimeTotal = playtimeTotal;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Integer getPlaytimeTotal() {
        return playtimeTotal;
    }

    public void setPlaytimeTotal(Integer playtimeTotal) {
        this.playtimeTotal = playtimeTotal;
    }

    public Integer getPlaytimeTwoWeeks() {
        return playtimeTwoWeeks;
    }

    public void setPlaytimeTwoWeeks(Integer playtimeTwoWeeks) {
        this.playtimeTwoWeeks = playtimeTwoWeeks;
    }

    public Instant getPurchasedAt() {
        return purchasedAt;
    }

    public void setPurchasedAt(Instant purchasedAt) {
        this.purchasedAt = purchasedAt;
    }

    public Instant getLastPlayed() {
        return lastPlayed;
    }

    public void setLastPlayed(Instant lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    // Métodos utilitários
    public Double getPlaytimeHours() {
        return playtimeTotal != null ? playtimeTotal / 60.0 : 0.0;
    }

    public Double getPlaytimeTwoWeeksHours() {
        return playtimeTwoWeeks != null ? playtimeTwoWeeks / 60.0 : 0.0;
    }

    public void addPlaytime(Integer minutes) {
        if (minutes != null && minutes > 0) {
            this.playtimeTotal = (this.playtimeTotal != null ? this.playtimeTotal : 0) + minutes;
        }
    }

    public boolean hasBeenPlayedRecently() {
        return playtimeTwoWeeks != null && playtimeTwoWeeks > 0;
    }

    public boolean hasBeenPlayed() {
        return playtimeTotal != null && playtimeTotal > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserGameLibrary)) return false;
        UserGameLibrary that = (UserGameLibrary) o;
        return user != null && game != null &&
                user.equals(that.user) && game.equals(that.game);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "UserGameLibrary{" +
                "id=" + id +
                ", user=" + (user != null ? user.getUsername() : null) +
                ", game=" + (game != null ? game.getName() : null) +
                ", playtimeTotal=" + playtimeTotal +
                ", playtimeTwoWeeks=" + playtimeTwoWeeks +
                '}';
    }
}