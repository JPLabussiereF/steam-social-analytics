package com.steamanalytics.service;

import com.steamanalytics.model.entity.Game;
import com.steamanalytics.model.entity.User;
import com.steamanalytics.model.entity.UserGameLibrary;
import com.steamanalytics.repository.UserGameLibraryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class UserGameLibraryService {

    private final UserGameLibraryRepository userGameLibraryRepository;
    private final UserService userService;
    private final GameService gameService;

    @Autowired
    public UserGameLibraryService(UserGameLibraryRepository userGameLibraryRepository,
                                  UserService userService,
                                  GameService gameService) {
        this.userGameLibraryRepository = userGameLibraryRepository;
        this.userService = userService;
        this.gameService = gameService;
    }

    /**
     * Adiciona jogo à biblioteca do usuário
     */
    @CacheEvict(value = {"userGames", "userStats"}, allEntries = true)
    public UserGameLibrary addGameToLibrary(Long userId, Long gameId, Integer playtimeTotal) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Game game = gameService.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        // Verificar se já existe
        Optional<UserGameLibrary> existing = userGameLibraryRepository
                .findByUserUserIdAndGameGameId(userId, gameId);

        if (existing.isPresent()) {
            // Atualizar existente
            UserGameLibrary userGame = existing.get();
            if (playtimeTotal != null) {
                userGame.setPlaytimeTotal(playtimeTotal);
            }
            return userGameLibraryRepository.save(userGame);
        } else {
            // Criar novo
            UserGameLibrary userGame = new UserGameLibrary(user, game, playtimeTotal);
            userGame.setPurchasedAt(Instant.now());
            return userGameLibraryRepository.save(userGame);
        }
    }

    /**
     * Adiciona jogo à biblioteca usando Steam App ID
     */
    @CacheEvict(value = {"userGames", "userStats"}, allEntries = true)
    public UserGameLibrary addGameToLibraryBySteamAppId(Long userId, Integer steamAppId,
                                                        Integer playtimeTotal, Integer playtimeTwoWeeks) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Game game = gameService.findBySteamAppId(steamAppId)
                .orElseThrow(() -> new RuntimeException("Game not found with Steam App ID: " + steamAppId));

        return addGameToLibraryWithDetails(user, game, playtimeTotal, playtimeTwoWeeks, null, null);
    }

    /**
     * Adiciona jogo com detalhes completos
     */
    @CacheEvict(value = {"userGames", "userStats"}, allEntries = true)
    public UserGameLibrary addGameToLibraryWithDetails(User user, Game game,
                                                       Integer playtimeTotal, Integer playtimeTwoWeeks,
                                                       Instant purchasedAt, Instant lastPlayed) {
        Optional<UserGameLibrary> existing = userGameLibraryRepository
                .findByUserUserIdAndGameGameId(user.getUserId(), game.getGameId());

        UserGameLibrary userGame;
        if (existing.isPresent()) {
            userGame = existing.get();
            userGame.setPlaytimeTotal(playtimeTotal != null ? playtimeTotal : userGame.getPlaytimeTotal());
            userGame.setPlaytimeTwoWeeks(playtimeTwoWeeks != null ? playtimeTwoWeeks : userGame.getPlaytimeTwoWeeks());
            userGame.setLastPlayed(lastPlayed != null ? lastPlayed : userGame.getLastPlayed());
        } else {
            userGame = new UserGameLibrary(user, game, playtimeTotal);
            userGame.setPlaytimeTwoWeeks(playtimeTwoWeeks);
            userGame.setPurchasedAt(purchasedAt != null ? purchasedAt : Instant.now());
            userGame.setLastPlayed(lastPlayed);
        }

        return userGameLibraryRepository.save(userGame);
    }

    /**
     * Remove jogo da biblioteca
     */
    @CacheEvict(value = {"userGames", "userStats"}, allEntries = true)
    public void removeGameFromLibrary(Long userId, Long gameId) {
        UserGameLibrary userGame = userGameLibraryRepository
                .findByUserUserIdAndGameGameId(userId, gameId)
                .orElseThrow(() -> new RuntimeException("Game not found in user's library"));

        userGameLibraryRepository.delete(userGame);
    }

    /**
     * Atualiza tempo de jogo
     */
    @CacheEvict(value = {"userGames", "userStats"}, key = "#userId")
    public UserGameLibrary updatePlaytime(Long userId, Long gameId,
                                          Integer playtimeTotal, Integer playtimeTwoWeeks) {
        UserGameLibrary userGame = userGameLibraryRepository
                .findByUserUserIdAndGameGameId(userId, gameId)
                .orElseThrow(() -> new RuntimeException("Game not found in user's library"));

        if (playtimeTotal != null) {
            userGame.setPlaytimeTotal(playtimeTotal);
        }
        if (playtimeTwoWeeks != null) {
            userGame.setPlaytimeTwoWeeks(playtimeTwoWeeks);
        }
        userGame.setLastPlayed(Instant.now());

        return userGameLibraryRepository.save(userGame);
    }

    /**
     * Busca biblioteca completa do usuário
     */
    @Cacheable(value = "userGames", key = "#userId")
    public List<UserGameLibrary> getUserLibrary(Long userId) {
        return userGameLibraryRepository.findUserLibraryWithGames(userId);
    }

    /**
     * Busca biblioteca do usuário ordenada por tempo de jogo
     */
    public List<UserGameLibrary> getUserLibraryByPlaytime(Long userId) {
        return userGameLibraryRepository.findMostPlayedGamesByUser(userId);
    }

    /**
     * Busca jogos jogados recentemente
     */
    public List<UserGameLibrary> getRecentlyPlayedGames(Long userId) {
        return userGameLibraryRepository.findRecentlyPlayedGamesByUser(userId);
    }

    /**
     * Busca jogos por tempo mínimo de jogo
     */
    public List<UserGameLibrary> getGamesByMinPlaytime(Long userId, Integer minPlaytimeMinutes) {
        return userGameLibraryRepository.findGamesByMinPlaytime(userId, minPlaytimeMinutes);
    }

    /**
     * Busca o jogo mais jogado do usuário
     */
    public Optional<UserGameLibrary> getMostPlayedGame(Long userId) {
        return userGameLibraryRepository.findMostPlayedGameByUser(userId);
    }

    /**
     * Busca últimos jogos jogados
     */
    public List<UserGameLibrary> getLastPlayedGames(Long userId, int limit) {
        return userGameLibraryRepository.findLastPlayedGamesByUser(userId)
                .stream()
                .limit(limit)
                .toList();
    }

    /**
     * Busca compras recentes
     */
    public List<UserGameLibrary> getRecentPurchases(Long userId, int days) {
        Instant since = Instant.now().minus(days, ChronoUnit.DAYS);
        return userGameLibraryRepository.findRecentPurchases(userId, since);
    }

    /**
     * Calcula estatísticas da biblioteca do usuário
     */
    public Map<String, Object> calculateLibraryStatistics(Long userId) {
        Object[] stats = userGameLibraryRepository.calculateUserPlaytimeStats(userId);
        Long totalGames = userGameLibraryRepository.countGamesByUser(userId);
        Long playedGames = userGameLibraryRepository.countPlayedGamesByUser(userId);

        Long totalPlaytime = stats.length > 0 ? ((Number) stats[0]).longValue() : 0L;
        Double avgPlaytime = stats.length > 1 ? ((Number) stats[1]).doubleValue() : 0.0;

        return Map.of(
                "totalGames", totalGames,
                "playedGames", playedGames,
                "unplayedGames", totalGames - playedGames,
                "totalPlaytimeMinutes", totalPlaytime,
                "totalPlaytimeHours", totalPlaytime / 60.0,
                "averagePlaytimeMinutes", avgPlaytime,
                "averagePlaytimeHours", avgPlaytime / 60.0,
                "playedPercentage", totalGames > 0 ? (playedGames.doubleValue() / totalGames) * 100 : 0
        );
    }

    /**
     * Verifica se usuário possui jogo
     */
    public boolean userOwnsGame(Long userId, Long gameId) {
        return userGameLibraryRepository.existsByUserUserIdAndGameGameId(userId, gameId);
    }

    /**
     * Verifica se usuário possui jogo por Steam App ID
     */
    public boolean userOwnsGameBySteamAppId(Long userId, Integer steamAppId) {
        Game game = gameService.findBySteamAppId(steamAppId).orElse(null);
        return game != null && userOwnsGame(userId, game.getGameId());
    }

    /**
     * Busca entrada específica da biblioteca
     */
    public Optional<UserGameLibrary> getUserGameEntry(Long userId, Long gameId) {
        return userGameLibraryRepository.findByUserUserIdAndGameGameId(userId, gameId);
    }

    /**
     * Busca top players de um jogo específico
     */
    public List<UserGameLibrary> getTopPlayersByGame(Long gameId, int limit) {
        return userGameLibraryRepository.findTopPlayersByGame(gameId)
                .stream()
                .limit(limit)
                .toList();
    }

    /**
     * Sincroniza biblioteca completa do usuário (batch operation)
     */
    @CacheEvict(value = {"userGames", "userStats"}, allEntries = true)
    public List<UserGameLibrary> syncUserLibrary(Long userId, List<Map<String, Object>> gamesData) {
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return gamesData.stream()
                .map(gameData -> {
                    Integer steamAppId = (Integer) gameData.get("steamAppId");
                    Integer playtimeTotal = (Integer) gameData.get("playtimeTotal");
                    Integer playtimeTwoWeeks = (Integer) gameData.get("playtimeTwoWeeks");

                    // Buscar ou criar jogo
                    Game game = gameService.findBySteamAppId(steamAppId).orElse(null);
                    if (game == null) {
                        // Se o jogo não existe, criar um básico
                        String gameName = (String) gameData.getOrDefault("name", "Unknown Game");
                        game = gameService.findOrCreateGame(steamAppId, gameName, null);
                    }

                    return addGameToLibraryWithDetails(user, game, playtimeTotal, playtimeTwoWeeks, null, null);
                })
                .toList();
    }

    /**
     * Salva múltiplas entradas da biblioteca
     */
    @CacheEvict(value = {"userGames", "userStats"}, allEntries = true)
    public List<UserGameLibrary> saveAll(List<UserGameLibrary> userGames) {
        return userGameLibraryRepository.saveAll(userGames);
    }

    /**
     * Conta total de jogos na biblioteca do usuário
     */
    public Long countUserGames(Long userId) {
        return userGameLibraryRepository.countGamesByUser(userId);
    }

    /**
     * Conta jogos jogados pelo usuário
     */
    public Long countPlayedGames(Long userId) {
        return userGameLibraryRepository.countPlayedGamesByUser(userId);
    }

    /**
     * Busca jogos similares baseado em jogadores comuns
     */
    public List<Object[]> findSimilarGames(Long gameId) {
        return userGameLibraryRepository.findSimilarGamesByCommonPlayers(gameId);
    }

    /**
     * Atualiza última vez jogado
     */
    @CacheEvict(value = {"userGames", "userStats"}, key = "#userId")
    public UserGameLibrary updateLastPlayed(Long userId, Long gameId) {
        UserGameLibrary userGame = userGameLibraryRepository
                .findByUserUserIdAndGameGameId(userId, gameId)
                .orElseThrow(() -> new RuntimeException("Game not found in user's library"));

        userGame.setLastPlayed(Instant.now());
        return userGameLibraryRepository.save(userGame);
    }

    /**
     * Busca estatísticas resumidas da biblioteca
     */
    @Cacheable(value = "userStats", key = "#userId")
    public Map<String, Object> getLibrarySummary(Long userId) {
        return calculateLibraryStatistics(userId);
    }
}