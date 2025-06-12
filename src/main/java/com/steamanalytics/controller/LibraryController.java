package com.steamanalytics.controller;

import com.steamanalytics.model.dto.GameDto;
import com.steamanalytics.model.entity.UserGameLibrary;
import com.steamanalytics.service.UserGameLibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/library")
@CrossOrigin(origins = {"http://localhost:3000"})
public class LibraryController {

    private final UserGameLibraryService libraryService;

    @Autowired
    public LibraryController(UserGameLibraryService libraryService) {
        this.libraryService = libraryService;
    }

    /**
     * Busca biblioteca completa do usuário
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<UserGameLibraryResponse>> getUserLibrary(@PathVariable Long userId) {
        List<UserGameLibrary> library = libraryService.getUserLibrary(userId);
        List<UserGameLibraryResponse> responses = library.stream()
                .map(UserGameLibraryResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Busca biblioteca ordenada por tempo de jogo
     */
    @GetMapping("/users/{userId}/by-playtime")
    public ResponseEntity<List<UserGameLibraryResponse>> getUserLibraryByPlaytime(@PathVariable Long userId) {
        List<UserGameLibrary> library = libraryService.getUserLibraryByPlaytime(userId);
        List<UserGameLibraryResponse> responses = library.stream()
                .map(UserGameLibraryResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Busca jogos jogados recentemente
     */
    @GetMapping("/users/{userId}/recently-played")
    public ResponseEntity<List<UserGameLibraryResponse>> getRecentlyPlayedGames(@PathVariable Long userId) {
        List<UserGameLibrary> games = libraryService.getRecentlyPlayedGames(userId);
        List<UserGameLibraryResponse> responses = games.stream()
                .map(UserGameLibraryResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Busca últimos jogos jogados com limite
     */
    @GetMapping("/users/{userId}/last-played")
    public ResponseEntity<List<UserGameLibraryResponse>> getLastPlayedGames(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        List<UserGameLibrary> games = libraryService.getLastPlayedGames(userId, limit);
        List<UserGameLibraryResponse> responses = games.stream()
                .map(UserGameLibraryResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Busca jogos por tempo mínimo de jogo
     */
    @GetMapping("/users/{userId}/min-playtime")
    public ResponseEntity<List<UserGameLibraryResponse>> getGamesByMinPlaytime(
            @PathVariable Long userId,
            @RequestParam Integer minPlaytimeMinutes) {
        List<UserGameLibrary> games = libraryService.getGamesByMinPlaytime(userId, minPlaytimeMinutes);
        List<UserGameLibraryResponse> responses = games.stream()
                .map(UserGameLibraryResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Busca o jogo mais jogado do usuário
     */
    @GetMapping("/users/{userId}/most-played")
    public ResponseEntity<UserGameLibraryResponse> getMostPlayedGame(@PathVariable Long userId) {
        return libraryService.getMostPlayedGame(userId)
                .map(game -> ResponseEntity.ok(UserGameLibraryResponse.from(game)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Busca compras recentes
     */
    @GetMapping("/users/{userId}/recent-purchases")
    public ResponseEntity<List<UserGameLibraryResponse>> getRecentPurchases(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "30") int days) {
        List<UserGameLibrary> purchases = libraryService.getRecentPurchases(userId, days);
        List<UserGameLibraryResponse> responses = purchases.stream()
                .map(UserGameLibraryResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Busca entrada específica da biblioteca
     */
    @GetMapping("/users/{userId}/games/{gameId}")
    public ResponseEntity<UserGameLibraryResponse> getUserGameEntry(
            @PathVariable Long userId,
            @PathVariable Long gameId) {
        return libraryService.getUserGameEntry(userId, gameId)
                .map(entry -> ResponseEntity.ok(UserGameLibraryResponse.from(entry)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Busca top players de um jogo específico
     */
    @GetMapping("/games/{gameId}/top-players")
    public ResponseEntity<List<UserGameLibraryResponse>> getTopPlayersByGame(
            @PathVariable Long gameId,
            @RequestParam(defaultValue = "10") int limit) {
        List<UserGameLibrary> topPlayers = libraryService.getTopPlayersByGame(gameId, limit);
        List<UserGameLibraryResponse> responses = topPlayers.stream()
                .map(UserGameLibraryResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Adiciona jogo à biblioteca do usuário
     */
    @PostMapping("/users/{userId}/games/{gameId}")
    public ResponseEntity<UserGameLibraryResponse> addGameToLibrary(
            @PathVariable Long userId,
            @PathVariable Long gameId,
            @Valid @RequestBody AddGameToLibraryRequest request) {
        try {
            UserGameLibrary userGame = libraryService.addGameToLibrary(
                    userId,
                    gameId,
                    request.getPlaytimeTotal()
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(UserGameLibraryResponse.from(userGame));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Adiciona jogo por Steam App ID
     */
    @PostMapping("/users/{userId}/games/steam/{steamAppId}")
    public ResponseEntity<UserGameLibraryResponse> addGameToLibraryBySteamAppId(
            @PathVariable Long userId,
            @PathVariable Integer steamAppId,
            @Valid @RequestBody AddGameBySteamAppIdRequest request) {
        try {
            UserGameLibrary userGame = libraryService.addGameToLibraryBySteamAppId(
                    userId,
                    steamAppId,
                    request.getPlaytimeTotal(),
                    request.getPlaytimeTwoWeeks()
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(UserGameLibraryResponse.from(userGame));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Atualiza tempo de jogo
     */
    @PutMapping("/users/{userId}/games/{gameId}/playtime")
    public ResponseEntity<UserGameLibraryResponse> updatePlaytime(
            @PathVariable Long userId,
            @PathVariable Long gameId,
            @Valid @RequestBody UpdatePlaytimeRequest request) {
        try {
            UserGameLibrary userGame = libraryService.updatePlaytime(
                    userId,
                    gameId,
                    request.getPlaytimeTotal(),
                    request.getPlaytimeTwoWeeks()
            );
            return ResponseEntity.ok(UserGameLibraryResponse.from(userGame));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Atualiza última vez jogado
     */
    @PutMapping("/users/{userId}/games/{gameId}/last-played")
    public ResponseEntity<UserGameLibraryResponse> updateLastPlayed(
            @PathVariable Long userId,
            @PathVariable Long gameId) {
        try {
            UserGameLibrary userGame = libraryService.updateLastPlayed(userId, gameId);
            return ResponseEntity.ok(UserGameLibraryResponse.from(userGame));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Remove jogo da biblioteca
     */
    @DeleteMapping("/users/{userId}/games/{gameId}")
    public ResponseEntity<Void> removeGameFromLibrary(
            @PathVariable Long userId,
            @PathVariable Long gameId) {
        try {
            libraryService.removeGameFromLibrary(userId, gameId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Sincroniza biblioteca completa do usuário
     */
    @PostMapping("/users/{userId}/sync")
    public ResponseEntity<List<UserGameLibraryResponse>> syncUserLibrary(
            @PathVariable Long userId,
            @Valid @RequestBody SyncLibraryRequest request) {
        try {
            List<UserGameLibrary> syncedGames = libraryService.syncUserLibrary(userId, request.getGamesData());
            List<UserGameLibraryResponse> responses = syncedGames.stream()
                    .map(UserGameLibraryResponse::from)
                    .toList();
            return ResponseEntity.ok(responses);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Calcula estatísticas da biblioteca do usuário
     */
    @GetMapping("/users/{userId}/statistics")
    public ResponseEntity<Map<String, Object>> getLibraryStatistics(@PathVariable Long userId) {
        Map<String, Object> stats = libraryService.calculateLibraryStatistics(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Busca estatísticas resumidas da biblioteca
     */
    @GetMapping("/users/{userId}/summary")
    public ResponseEntity<Map<String, Object>> getLibrarySummary(@PathVariable Long userId) {
        Map<String, Object> summary = libraryService.getLibrarySummary(userId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Verifica se usuário possui jogo
     */
    @GetMapping("/users/{userId}/owns/{gameId}")
    public ResponseEntity<Boolean> userOwnsGame(
            @PathVariable Long userId,
            @PathVariable Long gameId) {
        boolean owns = libraryService.userOwnsGame(userId, gameId);
        return ResponseEntity.ok(owns);
    }

    /**
     * Verifica se usuário possui jogo por Steam App ID
     */
    @GetMapping("/users/{userId}/owns/steam/{steamAppId}")
    public ResponseEntity<Boolean> userOwnsGameBySteamAppId(
            @PathVariable Long userId,
            @PathVariable Integer steamAppId) {
        boolean owns = libraryService.userOwnsGameBySteamAppId(userId, steamAppId);
        return ResponseEntity.ok(owns);
    }

    /**
     * Conta total de jogos na biblioteca
     */
    @GetMapping("/users/{userId}/count/total")
    public ResponseEntity<Long> countUserGames(@PathVariable Long userId) {
        Long count = libraryService.countUserGames(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * Conta jogos jogados
     */
    @GetMapping("/users/{userId}/count/played")
    public ResponseEntity<Long> countPlayedGames(@PathVariable Long userId) {
        Long count = libraryService.countPlayedGames(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * Busca jogos similares baseado em jogadores comuns
     */
    @GetMapping("/games/{gameId}/similar")
    public ResponseEntity<List<SimilarGameResponse>> findSimilarGames(@PathVariable Long gameId) {
        List<Object[]> results = libraryService.findSimilarGames(gameId);
        List<SimilarGameResponse> similarities = results.stream()
                .map(result -> new SimilarGameResponse(
                        (Long) result[0],
                        ((Number) result[1]).intValue()
                ))
                .toList();
        return ResponseEntity.ok(similarities);
    }

    // DTOs para requests e responses

    public static class UserGameLibraryResponse {
        private Long id;
        private GameDto game;
        private Integer playtimeTotal;
        private Integer playtimeTwoWeeks;
        private Double playtimeHours;
        private Double playtimeTwoWeeksHours;
        private Instant purchasedAt;
        private Instant lastPlayed;

        public static UserGameLibraryResponse from(UserGameLibrary userGameLibrary) {
            UserGameLibraryResponse response = new UserGameLibraryResponse();
            response.id = userGameLibrary.getId();
            response.game = GameDto.from(userGameLibrary.getGame());
            response.playtimeTotal = userGameLibrary.getPlaytimeTotal();
            response.playtimeTwoWeeks = userGameLibrary.getPlaytimeTwoWeeks();
            response.playtimeHours = userGameLibrary.getPlaytimeHours();
            response.playtimeTwoWeeksHours = userGameLibrary.getPlaytimeTwoWeeksHours();
            response.purchasedAt = userGameLibrary.getPurchasedAt();
            response.lastPlayed = userGameLibrary.getLastPlayed();
            return response;
        }

        // Getters e Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public GameDto getGame() { return game; }
        public void setGame(GameDto game) { this.game = game; }
        public Integer getPlaytimeTotal() { return playtimeTotal; }
        public void setPlaytimeTotal(Integer playtimeTotal) { this.playtimeTotal = playtimeTotal; }
        public Integer getPlaytimeTwoWeeks() { return playtimeTwoWeeks; }
        public void setPlaytimeTwoWeeks(Integer playtimeTwoWeeks) { this.playtimeTwoWeeks = playtimeTwoWeeks; }
        public Double getPlaytimeHours() { return playtimeHours; }
        public void setPlaytimeHours(Double playtimeHours) { this.playtimeHours = playtimeHours; }
        public Double getPlaytimeTwoWeeksHours() { return playtimeTwoWeeksHours; }
        public void setPlaytimeTwoWeeksHours(Double playtimeTwoWeeksHours) { this.playtimeTwoWeeksHours = playtimeTwoWeeksHours; }
        public Instant getPurchasedAt() { return purchasedAt; }
        public void setPurchasedAt(Instant purchasedAt) { this.purchasedAt = purchasedAt; }
        public Instant getLastPlayed() { return lastPlayed; }
        public void setLastPlayed(Instant lastPlayed) { this.lastPlayed = lastPlayed; }
    }

    public static class AddGameToLibraryRequest {
        private Integer playtimeTotal;

        // Getters e Setters
        public Integer getPlaytimeTotal() { return playtimeTotal; }
        public void setPlaytimeTotal(Integer playtimeTotal) { this.playtimeTotal = playtimeTotal; }
    }

    public static class AddGameBySteamAppIdRequest {
        private Integer playtimeTotal;
        private Integer playtimeTwoWeeks;

        // Getters e Setters
        public Integer getPlaytimeTotal() { return playtimeTotal; }
        public void setPlaytimeTotal(Integer playtimeTotal) { this.playtimeTotal = playtimeTotal; }
        public Integer getPlaytimeTwoWeeks() { return playtimeTwoWeeks; }
        public void setPlaytimeTwoWeeks(Integer playtimeTwoWeeks) { this.playtimeTwoWeeks = playtimeTwoWeeks; }
    }

    public static class UpdatePlaytimeRequest {
        private Integer playtimeTotal;
        private Integer playtimeTwoWeeks;

        // Getters e Setters
        public Integer getPlaytimeTotal() { return playtimeTotal; }
        public void setPlaytimeTotal(Integer playtimeTotal) { this.playtimeTotal = playtimeTotal; }
        public Integer getPlaytimeTwoWeeks() { return playtimeTwoWeeks; }
        public void setPlaytimeTwoWeeks(Integer playtimeTwoWeeks) { this.playtimeTwoWeeks = playtimeTwoWeeks; }
    }

    public static class SyncLibraryRequest {
        private List<Map<String, Object>> gamesData;

        // Getters e Setters
        public List<Map<String, Object>> getGamesData() { return gamesData; }
        public void setGamesData(List<Map<String, Object>> gamesData) { this.gamesData = gamesData; }
    }

    public static class SimilarGameResponse {
        private Long gameId;
        private Integer commonPlayers;

        public SimilarGameResponse(Long gameId, Integer commonPlayers) {
            this.gameId = gameId;
            this.commonPlayers = commonPlayers;
        }

        // Getters e Setters
        public Long getGameId() { return gameId; }
        public void setGameId(Long gameId) { this.gameId = gameId; }
        public Integer getCommonPlayers() { return commonPlayers; }
        public void setCommonPlayers(Integer commonPlayers) { this.commonPlayers = commonPlayers; }
    }
}