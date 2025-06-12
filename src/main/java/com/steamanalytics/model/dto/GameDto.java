package com.steamanalytics.model.dto;

import com.steamanalytics.model.entity.Game;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public class GameDto {
    private Long gameId;
    private Integer steamAppId;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private String developer;
    private String publisher;
    private BigDecimal priceCurrent;
    private Map<String, Object> tags;
    private Map<String, Object> categories;
    private Map<String, Object> genres;

    public static GameDto from(Game game) {
        if (game == null) return null;

        GameDto dto = new GameDto();
        dto.gameId = game.getGameId();
        dto.steamAppId = game.getSteamAppId();
        dto.name = game.getName();
        dto.description = game.getDescription();
        dto.releaseDate = game.getReleaseDate();
        dto.developer = game.getDeveloper();
        dto.publisher = game.getPublisher();
        dto.priceCurrent = game.getPriceCurrent();
        dto.tags = game.getTags();
        dto.categories = game.getCategories();
        dto.genres = game.getGenres();
        return dto;
    }

    // Getters e Setters
    public Long getGameId() { return gameId; }
    public void setGameId(Long gameId) { this.gameId = gameId; }
    public Integer getSteamAppId() { return steamAppId; }
    public void setSteamAppId(Integer steamAppId) { this.steamAppId = steamAppId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
    public String getDeveloper() { return developer; }
    public void setDeveloper(String developer) { this.developer = developer; }
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    public BigDecimal getPriceCurrent() { return priceCurrent; }
    public void setPriceCurrent(BigDecimal priceCurrent) { this.priceCurrent = priceCurrent; }
    public Map<String, Object> getTags() { return tags; }
    public void setTags(Map<String, Object> tags) { this.tags = tags; }
    public Map<String, Object> getCategories() { return categories; }
    public void setCategories(Map<String, Object> categories) { this.categories = categories; }
    public Map<String, Object> getGenres() { return genres; }
    public void setGenres(Map<String, Object> genres) { this.genres = genres; }
}
