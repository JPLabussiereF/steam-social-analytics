package com.steamanalytics.service;

import com.steamanalytics.model.dto.*;
import com.steamanalytics.model.entity.Game;
import com.steamanalytics.model.entity.User;
import com.steamanalytics.model.entity.UserGameLibrary;
import com.steamanalytics.repository.FriendshipRepository;
import com.steamanalytics.repository.GameRepository;
import com.steamanalytics.repository.UserGameLibraryRepository;
import com.steamanalytics.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final UserGameLibraryRepository userGameLibraryRepository;
    private final FriendshipRepository friendshipRepository;

    @Autowired
    public AnalyticsService(UserRepository userRepository,
                            GameRepository gameRepository,
                            UserGameLibraryRepository userGameLibraryRepository,
                            FriendshipRepository friendshipRepository) {
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
        this.userGameLibraryRepository = userGameLibraryRepository;
        this.friendshipRepository = friendshipRepository;
    }

    /**
     * Busca jogos comuns entre dois usuários
     */
    @Cacheable(value = "commonGames", key = "#userId + '_' + #friendId")
    public CommonGamesResponse findCommonGames(Long userId, Long friendId) {
        List<Game> commonGames = gameRepository.findCommonGamesBetweenUsers(userId, friendId);

        List<CommonGameDto> gameAnalysis = commonGames.stream()
                .map(game -> {
                    UserGameLibrary userGame = findUserGame(userId, game.getGameId());
                    UserGameLibrary friendGame = findUserGame(friendId, game.getGameId());

                    return CommonGameDto.builder()
                            .game(GameDto.from(game))
                            .userPlaytime(userGame != null ? userGame.getPlaytimeTotal() : 0)
                            .friendPlaytime(friendGame != null ? friendGame.getPlaytimeTotal() : 0)
                            .totalPlaytime((userGame != null ? userGame.getPlaytimeTotal() : 0) +
                                    (friendGame != null ? friendGame.getPlaytimeTotal() : 0))
                            .build();
                })
                .sorted((a, b) -> Integer.compare(b.getTotalPlaytime(), a.getTotalPlaytime()))
                .collect(Collectors.toList());

        return CommonGamesResponse.builder()
                .commonGames(gameAnalysis)
                .totalCommonGames(gameAnalysis.size())
                .build();
    }

    /**
     * Gera recomendações de jogos para um usuário
     */
    @Cacheable(value = "recommendations", key = "#userId")
    public List<GameRecommendation> generateRecommendations(Long userId) {
        // Buscar amigos do usuário
        List<Long> friendIds = friendshipRepository.findAcceptedFriendIds(userId);

        if (friendIds.isEmpty()) {
            return generatePopularGamesRecommendations();
        }

        // Algoritmo de recomendação baseado em amigos
        Map<Long, Integer> gamePopularity = gameRepository
                .findPopularGamesAmongFriends(friendIds, userId)
                .stream()
                .collect(Collectors.toMap(
                        result -> (Long) result[0],
                        result -> ((Number) result[1]).intValue()
                ));

        return gamePopularity.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    Game game = gameRepository.findById(entry.getKey()).orElse(null);
                    return GameRecommendation.builder()
                            .game(GameDto.from(game))
                            .friendsWhoPlay(entry.getValue())
                            .score(calculateRecommendationScore(entry.getValue(), game))
                            .reason("Jogado por " + entry.getValue() + " amigo(s)")
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Calcula estatísticas completas do usuário
     */
    @Cacheable(value = "userStats", key = "#userId")
    public UserStatistics calculateUserStatistics(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Estatísticas da biblioteca
        Map<String, Object> libraryStats = calculateLibraryStatistics(userId);

        // Jogo mais jogado
        UserGameLibrary mostPlayedGame = userGameLibraryRepository.findMostPlayedGameByUser(userId)
                .orElse(null);

        // Jogos jogados recentemente
        List<UserGameLibrary> recentlyPlayed = userGameLibraryRepository
                .findLastPlayedGamesByUser(userId)
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        // Distribuição por gênero
        Map<String, Integer> genreDistribution = calculateGenreDistribution(userId);

        // Contagem de amigos
        Long friendCount = friendshipRepository.countAcceptedFriends(userId);

        return UserStatistics.builder()
                .userId(userId)
                .totalGames(((Number) libraryStats.get("totalGames")).intValue())
                .playedGames(((Number) libraryStats.get("playedGames")).intValue())
                .unplayedGames(((Number) libraryStats.get("unplayedGames")).intValue())
                .totalPlaytimeMinutes(((Number) libraryStats.get("totalPlaytimeMinutes")).longValue())
                .totalPlaytimeHours(((Number) libraryStats.get("totalPlaytimeHours")).doubleValue())
                .averagePlaytimeMinutes(((Number) libraryStats.get("averagePlaytimeMinutes")).doubleValue())
                .averagePlaytimeHours(((Number) libraryStats.get("averagePlaytimeHours")).doubleValue())
                .playedPercentage(((Number) libraryStats.get("playedPercentage")).doubleValue())
                .mostPlayedGame(mostPlayedGame != null ? GameDto.from(mostPlayedGame.getGame()) : null)
                .recentlyPlayed(recentlyPlayed.stream()
                        .map(ugl -> GameDto.from(ugl.getGame()))
                        .collect(Collectors.toList()))
                .genreDistribution(genreDistribution)
                .friendCount(friendCount.intValue())
                .build();
    }

    /**
     * Constrói dados completos do dashboard
     */
    @Cacheable(value = "dashboard", key = "#userId")
    public DashboardData buildDashboard(Long userId) {
        UserStatistics stats = calculateUserStatistics(userId);

        // Top 5 jogos mais jogados
        List<UserGameLibrary> topGames = userGameLibraryRepository
                .findMostPlayedGamesByUser(userId)
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        // Jogos recentes (últimas 2 semanas)
        List<UserGameLibrary> recentGames = userGameLibraryRepository
                .findRecentlyPlayedGamesByUser(userId)
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        // Recomendações
        List<GameRecommendation> recommendations = generateRecommendations(userId)
                .stream()
                .limit(3)
                .collect(Collectors.toList());

        // Atividade dos amigos (se houver)
        List<FriendActivity> friendsActivity = getFriendsActivity(userId);

        return DashboardData.builder()
                .userStatistics(stats)
                .topGames(topGames.stream()
                        .map(this::mapToGameWithPlaytime)
                        .collect(Collectors.toList()))
                .recentGames(recentGames.stream()
                        .map(this::mapToGameWithPlaytime)
                        .collect(Collectors.toList()))
                .recommendations(recommendations)
                .friendsActivity(friendsActivity)
                .build();
    }

    /**
     * Calcula estatísticas da biblioteca do usuário
     */
    private Map<String, Object> calculateLibraryStatistics(Long userId) {
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
     * Calcula distribuição por gênero
     */
    private Map<String, Integer> calculateGenreDistribution(Long userId) {
        List<UserGameLibrary> userLibrary = userGameLibraryRepository.findUserLibraryWithGames(userId);
        Map<String, Integer> genreCount = new HashMap<>();

        for (UserGameLibrary ugl : userLibrary) {
            Game game = ugl.getGame();
            if (game.getGenres() != null) {
                Map<String, Object> genres = game.getGenres();
                for (String genre : genres.keySet()) {
                    genreCount.merge(genre, 1, Integer::sum);
                }
            }
        }

        return genreCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * Busca entrada do jogo na biblioteca do usuário
     */
    private UserGameLibrary findUserGame(Long userId, Long gameId) {
        return userGameLibraryRepository.findByUserUserIdAndGameGameId(userId, gameId)
                .orElse(null);
    }

    /**
     * Calcula score de recomendação
     */
    private double calculateRecommendationScore(Integer friendsWhoPlay, Game game) {
        double baseScore = friendsWhoPlay * 10.0;

        // Bonus para jogos gratuitos
        if (game.getPriceCurrent() != null && game.getPriceCurrent().doubleValue() == 0) {
            baseScore += 5.0;
        }

        // Bonus para jogos populares
        int totalPlayers = game.getUserLibraries().size();
        if (totalPlayers > 1000) {
            baseScore += 5.0;
        }

        return Math.min(baseScore, 100.0);
    }

    /**
     * Gera recomendações baseadas em jogos populares (fallback)
     */
    private List<GameRecommendation> generatePopularGamesRecommendations() {
        List<Game> popularGames = gameRepository.findMostPopularGames()
                .stream()
                .limit(10)
                .collect(Collectors.toList());

        return popularGames.stream()
                .map(game -> GameRecommendation.builder()
                        .game(GameDto.from(game))
                        .friendsWhoPlay(0)
                        .score(50.0)
                        .reason("Jogo popular")
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Busca atividade dos amigos
     */
    private List<FriendActivity> getFriendsActivity(Long userId) {
        List<Long> friendIds = friendshipRepository.findAcceptedFriendIds(userId);

        return friendIds.stream()
                .limit(5)
                .map(friendId -> {
                    User friend = userRepository.findById(friendId).orElse(null);
                    if (friend == null) return null;

                    List<UserGameLibrary> recentGames = userGameLibraryRepository
                            .findRecentlyPlayedGamesByUser(friendId)
                            .stream()
                            .limit(3)
                            .collect(Collectors.toList());

                    return FriendActivity.builder()
                            .friend(UserDto.from(friend))
                            .recentGames(recentGames.stream()
                                    .map(ugl -> GameDto.from(ugl.getGame()))
                                    .collect(Collectors.toList()))
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Mapeia UserGameLibrary para GameWithPlaytime
     */
    private GameWithPlaytime mapToGameWithPlaytime(UserGameLibrary ugl) {
        return GameWithPlaytime.builder()
                .game(GameDto.from(ugl.getGame()))
                .playtimeMinutes(ugl.getPlaytimeTotal())
                .playtimeHours(ugl.getPlaytimeHours())
                .lastPlayed(ugl.getLastPlayed())
                .build();
    }

    // DTOs Response classes que são específicos do Analytics
    public static class CommonGamesResponse {
        private List<CommonGameDto> commonGames;
        private Integer totalCommonGames;

        public static CommonGamesResponseBuilder builder() {
            return new CommonGamesResponseBuilder();
        }

        // getters e setters
        public List<CommonGameDto> getCommonGames() { return commonGames; }
        public void setCommonGames(List<CommonGameDto> commonGames) { this.commonGames = commonGames; }
        public Integer getTotalCommonGames() { return totalCommonGames; }
        public void setTotalCommonGames(Integer totalCommonGames) { this.totalCommonGames = totalCommonGames; }

        public static class CommonGamesResponseBuilder {
            private List<CommonGameDto> commonGames;
            private Integer totalCommonGames;

            public CommonGamesResponseBuilder commonGames(List<CommonGameDto> commonGames) {
                this.commonGames = commonGames;
                return this;
            }

            public CommonGamesResponseBuilder totalCommonGames(Integer totalCommonGames) {
                this.totalCommonGames = totalCommonGames;
                return this;
            }

            public CommonGamesResponse build() {
                CommonGamesResponse response = new CommonGamesResponse();
                response.commonGames = this.commonGames;
                response.totalCommonGames = this.totalCommonGames;
                return response;
            }
        }
    }

    public static class CommonGameDto {
        private GameDto game;
        private Integer userPlaytime;
        private Integer friendPlaytime;
        private Integer totalPlaytime;

        public static CommonGameDtoBuilder builder() {
            return new CommonGameDtoBuilder();
        }

        // getters e setters
        public GameDto getGame() { return game; }
        public void setGame(GameDto game) { this.game = game; }
        public Integer getUserPlaytime() { return userPlaytime; }
        public void setUserPlaytime(Integer userPlaytime) { this.userPlaytime = userPlaytime; }
        public Integer getFriendPlaytime() { return friendPlaytime; }
        public void setFriendPlaytime(Integer friendPlaytime) { this.friendPlaytime = friendPlaytime; }
        public Integer getTotalPlaytime() { return totalPlaytime; }
        public void setTotalPlaytime(Integer totalPlaytime) { this.totalPlaytime = totalPlaytime; }

        public static class CommonGameDtoBuilder {
            private GameDto game;
            private Integer userPlaytime;
            private Integer friendPlaytime;
            private Integer totalPlaytime;

            public CommonGameDtoBuilder game(GameDto game) {
                this.game = game;
                return this;
            }

            public CommonGameDtoBuilder userPlaytime(Integer userPlaytime) {
                this.userPlaytime = userPlaytime;
                return this;
            }

            public CommonGameDtoBuilder friendPlaytime(Integer friendPlaytime) {
                this.friendPlaytime = friendPlaytime;
                return this;
            }

            public CommonGameDtoBuilder totalPlaytime(Integer totalPlaytime) {
                this.totalPlaytime = totalPlaytime;
                return this;
            }

            public CommonGameDto build() {
                CommonGameDto dto = new CommonGameDto();
                dto.game = this.game;
                dto.userPlaytime = this.userPlaytime;
                dto.friendPlaytime = this.friendPlaytime;
                dto.totalPlaytime = this.totalPlaytime;
                return dto;
            }
        }
    }
}