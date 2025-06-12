package com.steamanalytics.repository;

import com.steamanalytics.model.entity.UserGameLibrary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserGameLibraryRepository extends JpaRepository<UserGameLibrary, Long> {

    /**
     * Busca biblioteca de um usuário específico
     */
    List<UserGameLibrary> findByUserUserId(Long userId);

    /**
     * Busca todos os usuários que possuem um jogo específico
     */
    List<UserGameLibrary> findByGameGameId(Long gameId);

    /**
     * Busca entrada específica da biblioteca (usuário + jogo)
     */
    Optional<UserGameLibrary> findByUserUserIdAndGameGameId(Long userId, Long gameId);

    /**
     * Verifica se usuário possui um jogo
     */
    boolean existsByUserUserIdAndGameGameId(Long userId, Long gameId);

    /**
     * Busca jogos mais jogados de um usuário (ordenado por tempo total)
     */
    @Query("SELECT ugl FROM UserGameLibrary ugl WHERE ugl.user.userId = :userId " +
            "AND ugl.playtimeTotal > 0 ORDER BY ugl.playtimeTotal DESC")
    List<UserGameLibrary> findMostPlayedGamesByUser(@Param("userId") Long userId);

    /**
     * Busca jogos jogados recentemente por um usuário
     */
    @Query("SELECT ugl FROM UserGameLibrary ugl WHERE ugl.user.userId = :userId " +
            "AND ugl.playtimeTwoWeeks > 0 ORDER BY ugl.playtimeTwoWeeks DESC")
    List<UserGameLibrary> findRecentlyPlayedGamesByUser(@Param("userId") Long userId);

    /**
     * Busca jogos por tempo de jogo mínimo
     */
    @Query("SELECT ugl FROM UserGameLibrary ugl WHERE ugl.user.userId = :userId " +
            "AND ugl.playtimeTotal >= :minPlaytime ORDER BY ugl.playtimeTotal DESC")
    List<UserGameLibrary> findGamesByMinPlaytime(@Param("userId") Long userId, @Param("minPlaytime") Integer minPlaytime);

    /**
     * Calcula estatísticas de tempo total jogado por usuário
     */
    @Query("SELECT " +
            "COALESCE(SUM(ugl.playtimeTotal), 0) as totalPlaytime, " +
            "COALESCE(AVG(ugl.playtimeTotal), 0) as avgPlaytime, " +
            "COUNT(ugl) as totalGames " +
            "FROM UserGameLibrary ugl WHERE ugl.user.userId = :userId")
    Object[] calculateUserPlaytimeStats(@Param("userId") Long userId);

    /**
     * Busca biblioteca completa de um usuário com informações do jogo
     */
    @Query("SELECT ugl FROM UserGameLibrary ugl " +
            "JOIN FETCH ugl.game " +
            "WHERE ugl.user.userId = :userId " +
            "ORDER BY ugl.playtimeTotal DESC")
    List<UserGameLibrary> findUserLibraryWithGames(@Param("userId") Long userId);

    /**
     * Busca jogos comprados recentemente
     */
    @Query("SELECT ugl FROM UserGameLibrary ugl WHERE ugl.user.userId = :userId " +
            "AND ugl.purchasedAt >= :since ORDER BY ugl.purchasedAt DESC")
    List<UserGameLibrary> findRecentPurchases(@Param("userId") Long userId, @Param("since") Instant since);

    /**
     * Busca o jogo mais jogado de um usuário
     */
    @Query("SELECT ugl FROM UserGameLibrary ugl WHERE ugl.user.userId = :userId " +
            "AND ugl.playtimeTotal = (SELECT MAX(ugl2.playtimeTotal) FROM UserGameLibrary ugl2 WHERE ugl2.user.userId = :userId)")
    Optional<UserGameLibrary> findMostPlayedGameByUser(@Param("userId") Long userId);

    /**
     * Busca usuários que mais jogaram um jogo específico
     */
    @Query("SELECT ugl FROM UserGameLibrary ugl WHERE ugl.game.gameId = :gameId " +
            "AND ugl.playtimeTotal > 0 ORDER BY ugl.playtimeTotal DESC")
    List<UserGameLibrary> findTopPlayersByGame(@Param("gameId") Long gameId);

    /**
     * Conta total de jogos na biblioteca de um usuário
     */
    @Query("SELECT COUNT(ugl) FROM UserGameLibrary ugl WHERE ugl.user.userId = :userId")
    Long countGamesByUser(@Param("userId") Long userId);

    /**
     * Conta total de jogos jogados (com tempo > 0) por um usuário
     */
    @Query("SELECT COUNT(ugl) FROM UserGameLibrary ugl WHERE ugl.user.userId = :userId AND ugl.playtimeTotal > 0")
    Long countPlayedGamesByUser(@Param("userId") Long userId);

    /**
     * Busca jogos similares baseado em jogadores comuns
     */
    @Query("SELECT DISTINCT ugl2.game.gameId, COUNT(DISTINCT ugl2.user.userId) as commonPlayers " +
            "FROM UserGameLibrary ugl1 " +
            "JOIN UserGameLibrary ugl2 ON ugl1.user.userId = ugl2.user.userId " +
            "WHERE ugl1.game.gameId = :gameId AND ugl2.game.gameId != :gameId " +
            "GROUP BY ugl2.game.gameId " +
            "ORDER BY commonPlayers DESC")
    List<Object[]> findSimilarGamesByCommonPlayers(@Param("gameId") Long gameId);

    /**
     * Busca últimos jogos jogados por um usuário
     */
    @Query("SELECT ugl FROM UserGameLibrary ugl WHERE ugl.user.userId = :userId " +
            "AND ugl.lastPlayed IS NOT NULL ORDER BY ugl.lastPlayed DESC")
    List<UserGameLibrary> findLastPlayedGamesByUser(@Param("userId") Long userId);
}