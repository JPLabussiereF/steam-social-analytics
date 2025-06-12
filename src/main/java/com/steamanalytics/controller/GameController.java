package com.steamanalytics.controller;

import com.steamanalytics.model.dto.GameDto;
import com.steamanalytics.model.entity.Game;
import com.steamanalytics.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/games")
@CrossOrigin(origins = {"http://localhost:3000"})
public class GameController {

    private final GameService gameService;

    @Autowired
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    /**
     * Busca jogo por ID
     */
    @GetMapping("/{gameId}")
    public ResponseEntity<GameDto> getGameById(@PathVariable Long gameId) {
        return gameService.findById(gameId)
                .map(game -> ResponseEntity.ok(GameDto.from(game)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Busca jogo por Steam App ID
     */
    @GetMapping("/steam/{steamAppId}")
    public ResponseEntity<GameDto> getGameBySteamAppId(@PathVariable Integer steamAppId) {
        return gameService.findBySteamAppId(steamAppId)
                .map(game -> ResponseEntity.ok(GameDto.from(game)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Busca todos os jogos com paginação
     */
    @GetMapping
    public ResponseEntity<Page<GameDto>> getAllGames(Pageable pageable) {
        Page<Game> games = gameService.findAllGames(pageable);
        Page<GameDto> gameDtos = games.map(GameDto::from);
        return ResponseEntity.ok(gameDtos);
    }

    /**
     * Busca jogos por nome
     */
    @GetMapping("/search")
    public ResponseEntity<List<GameDto>> searchGamesByName(@RequestParam String name) {
        if (name.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<Game> games = gameService.searchGamesByName(name);
        List<GameDto> gameDtos = games.stream().map(GameDto::from).toList();
        return ResponseEntity.ok(gameDtos);
    }

    /**
     * Busca jogos por desenvolvedor
     */
    @GetMapping("/developer/{developer}")
    public ResponseEntity<List<GameDto>> getGamesByDeveloper(@PathVariable String developer) {
        List<Game> games = gameService.findGamesByDeveloper(developer);
        List<GameDto> gameDtos = games.stream().map(GameDto::from).toList();
        return ResponseEntity.ok(gameDtos);
    }

    /**
     * Busca jogos por publisher
     */
    @GetMapping("/publisher/{publisher}")
    public ResponseEntity<List<GameDto>> getGamesByPublisher(@PathVariable String publisher) {
        List<Game> games = gameService.findGamesByPublisher(publisher);
        List<GameDto> gameDtos = games.stream().map(GameDto::from).toList();
        return ResponseEntity.ok(gameDtos);
    }

    /**
     * Busca jogos lançados após uma data
     */
    @GetMapping("/released-after")
    public ResponseEntity<List<GameDto>> getGamesReleasedAfter(@RequestParam String date) {
        try {
            LocalDate releaseDate = LocalDate.parse(date);
            List<Game> games = gameService.findGamesReleasedAfter(releaseDate);
            List<GameDto> gameDtos = games.stream().map(GameDto::from).toList();
            return ResponseEntity.ok(gameDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Busca jogos lançados em um período
     */
    @GetMapping("/released-between")
    public ResponseEntity<List<GameDto>> getGamesReleasedBetween(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            List<Game> games = gameService.findGamesReleasedBetween(start, end);
            List<GameDto> gameDtos = games.stream().map(GameDto::from).toList();
            return ResponseEntity.ok(gameDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Busca jogos mais populares
     */
    @GetMapping("/popular")
    public ResponseEntity<List<GameDto>> getMostPopularGames() {
        List<Game> games = gameService.findMostPopularGames();
        List<GameDto> gameDtos = games.stream().map(GameDto::from).toList();
        return ResponseEntity.ok(gameDtos);
    }

    /**
     * Busca jogos comuns entre dois usuários
     */
    @GetMapping("/common/{userId1}/{userId2}")
    public ResponseEntity<List<GameDto>> getCommonGamesBetweenUsers(
            @PathVariable Long userId1,
            @PathVariable Long userId2) {
        List<Game> games = gameService.findCommonGamesBetweenUsers(userId1, userId2);
        List<GameDto> gameDtos = games.stream().map(GameDto::from).toList();
        return ResponseEntity.ok(gameDtos);
    }

    /**
     * Busca jogos por múltiplos Steam App IDs
     */
    @PostMapping("/bulk")
    public ResponseEntity<List<GameDto>> getGamesBySteamAppIds(@RequestBody BulkGamesRequest request) {
        List<Game> games = gameService.findGamesBySteamAppIds(request.getSteamAppIds());
        List<GameDto> gameDtos = games.stream().map(GameDto::from).toList();
        return ResponseEntity.ok(gameDtos);
    }

    /**
     * Busca jogos com muito tempo total jogado
     */
    @GetMapping("/high-playtime")
    public ResponseEntity<List<GameDto>> getGamesWithHighPlaytime(
            @RequestParam(defaultValue = "10000") Integer minPlaytime) {
        List<Game> games = gameService.findGamesWithHighPlaytime(minPlaytime);
        List<GameDto> gameDtos = games.stream().map(GameDto::from).toList();
        return ResponseEntity.ok(gameDtos);
    }

    /**
     * Busca jogos recentemente adicionados
     */
    @GetMapping("/recent")
    public ResponseEntity<List<GameDto>> getRecentlyAddedGames() {
        List<Game> games = gameService.findRecentlyAddedGames();
        List<GameDto> gameDtos = games.stream().map(GameDto::from).toList();
        return ResponseEntity.ok(gameDtos);
    }

    /**
     * Busca jogos recentemente lançados
     */
    @GetMapping("/recently-released")
    public ResponseEntity<List<GameDto>> getRecentlyReleasedGames() {
        List<Game> games = gameService.findRecentlyReleasedGames();
        List<GameDto> gameDtos = games.stream().map(GameDto::from).toList();
        return ResponseEntity.ok(gameDtos);
    }

    /**
     * Busca jogos gratuitos
     */
    @GetMapping("/free")
    public ResponseEntity<List<GameDto>> getFreeGames() {
        List<Game> games = gameService.findFreeGames();
        List<GameDto> gameDtos = games.stream().map(GameDto::from).toList();
        return ResponseEntity.ok(gameDtos);
    }

    /**
     * Busca jogos por faixa de preço
     */
    @GetMapping("/price-range")
    public ResponseEntity<List<GameDto>> getGamesByPriceRange(
            @RequestParam(defaultValue = "0.0") Double minPrice,
            @RequestParam(defaultValue = "999.99") Double maxPrice) {
        List<Game> games = gameService.findGamesByPriceRange(minPrice, maxPrice);
        List<GameDto> gameDtos = games.stream().map(GameDto::from).toList();
        return ResponseEntity.ok(gameDtos);
    }

    /**
     * Busca jogos por critérios múltiplos
     */
    @GetMapping("/filter")
    public ResponseEntity<List<GameDto>> getGamesByCriteria(
            @RequestParam(required = false) String developer,
            @RequestParam(required = false) String publisher,
            @RequestParam(required = false) String minReleaseDate,
            @RequestParam(required = false) BigDecimal maxPrice) {

        LocalDate releaseDate = null;
        if (minReleaseDate != null && !minReleaseDate.isEmpty()) {
            try {
                releaseDate = LocalDate.parse(minReleaseDate);
            } catch (Exception e) {
                return ResponseEntity.badRequest().build();
            }
        }

        List<Game> games = gameService.findGamesByCriteria(developer, publisher, releaseDate, maxPrice);
        List<GameDto> gameDtos = games.stream().map(GameDto::from).toList();
        return ResponseEntity.ok(gameDtos);
    }

    /**
     * Cria novo jogo
     */
    @PostMapping
    public ResponseEntity<GameDto> createGame(@Valid @RequestBody CreateGameRequest request) {
        try {
            Game game = gameService.findOrCreateGame(
                    request.getSteamAppId(),
                    request.getName(),
                    request.getDescription()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(GameDto.from(game));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Atualiza informações do jogo
     */
    @PutMapping("/{steamAppId}")
    public ResponseEntity<GameDto> updateGameInfo(
            @PathVariable Integer steamAppId,
            @Valid @RequestBody UpdateGameRequest request) {
        try {
            Game game = gameService.updateGameInfo(
                    steamAppId,
                    request.getName(),
                    request.getDescription(),
                    request.getDeveloper(),
                    request.getPublisher(),
                    request.getReleaseDate()
            );
            return ResponseEntity.ok(GameDto.from(game));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Atualiza preços do jogo
     */
    @PutMapping("/{steamAppId}/prices")
    public ResponseEntity<GameDto> updateGamePrices(
            @PathVariable Integer steamAppId,
            @Valid @RequestBody UpdateGamePricesRequest request) {
        try {
            Game game = gameService.updateGamePrices(
                    steamAppId,
                    request.getPriceInitial(),
                    request.getPriceCurrent()
            );
            return ResponseEntity.ok(GameDto.from(game));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Atualiza metadados do jogo
     */
    @PutMapping("/{steamAppId}/metadata")
    public ResponseEntity<GameDto> updateGameMetadata(
            @PathVariable Integer steamAppId,
            @Valid @RequestBody UpdateGameMetadataRequest request) {
        try {
            Game game = gameService.updateGameMetadata(
                    steamAppId,
                    request.getTags(),
                    request.getCategories(),
                    request.getGenres()
            );
            return ResponseEntity.ok(GameDto.from(game));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Remove jogo
     */
    @DeleteMapping("/{gameId}")
    public ResponseEntity<Void> deleteGame(@PathVariable Long gameId) {
        try {
            gameService.deleteGame(gameId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Conta total de jogos únicos
     */
    @GetMapping("/count/unique")
    public ResponseEntity<Long> countUniqueGames() {
        Long count = gameService.countUniqueGames();
        return ResponseEntity.ok(count);
    }

    /**
     * Conta total de jogos
     */
    @GetMapping("/count/total")
    public ResponseEntity<Long> countTotalGames() {
        Long count = gameService.countTotalGames();
        return ResponseEntity.ok(count);
    }

    /**
     * Verifica se jogo existe por Steam App ID
     */
    @GetMapping("/exists/{steamAppId}")
    public ResponseEntity<Boolean> existsBySteamAppId(@PathVariable Integer steamAppId) {
        boolean exists = gameService.existsBySteamAppId(steamAppId);
        return ResponseEntity.ok(exists);
    }

    // DTOs para requests

    public static class CreateGameRequest {
        private Integer steamAppId;
        private String name;
        private String description;

        // Getters e Setters
        public Integer getSteamAppId() { return steamAppId; }
        public void setSteamAppId(Integer steamAppId) { this.steamAppId = steamAppId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class UpdateGameRequest {
        private String name;
        private String description;
        private String developer;
        private String publisher;
        private LocalDate releaseDate;

        // Getters e Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getDeveloper() { return developer; }
        public void setDeveloper(String developer) { this.developer = developer; }
        public String getPublisher() { return publisher; }
        public void setPublisher(String publisher) { this.publisher = publisher; }
        public LocalDate getReleaseDate() { return releaseDate; }
        public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }
    }

    public static class UpdateGamePricesRequest {
        private BigDecimal priceInitial;
        private BigDecimal priceCurrent;

        // Getters e Setters
        public BigDecimal getPriceInitial() { return priceInitial; }
        public void setPriceInitial(BigDecimal priceInitial) { this.priceInitial = priceInitial; }
        public BigDecimal getPriceCurrent() { return priceCurrent; }
        public void setPriceCurrent(BigDecimal priceCurrent) { this.priceCurrent = priceCurrent; }
    }

    public static class UpdateGameMetadataRequest {
        private Map<String, Object> tags;
        private Map<String, Object> categories;
        private Map<String, Object> genres;

        // Getters e Setters
        public Map<String, Object> getTags() { return tags; }
        public void setTags(Map<String, Object> tags) { this.tags = tags; }
        public Map<String, Object> getCategories() { return categories; }
        public void setCategories(Map<String, Object> categories) { this.categories = categories; }
        public Map<String, Object> getGenres() { return genres; }
        public void setGenres(Map<String, Object> genres) { this.genres = genres; }
    }

    public static class BulkGamesRequest {
        private List<Integer> steamAppIds;

        // Getters e Setters
        public List<Integer> getSteamAppIds() { return steamAppIds; }
        public void setSteamAppIds(List<Integer> steamAppIds) { this.steamAppIds = steamAppIds; }
    }
}