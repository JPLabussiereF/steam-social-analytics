package com.steamanalytics.repository;

import com.steamanalytics.model.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

    /**
     * Busca jogo pelo Steam App ID
     */
    Optional<Game> findBySteamAppId(Integer steamAppId);

    /**
     * Verifica se existe jogo com o Steam App ID
     */
    boolean existsBySteamAppId(Integer steamAppId);

    /**
     * Busca jogos por nome (case-insensitive)
     */
    @Query("SELECT g FROM Game g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Game> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Busca jogos por desenvolvedor
     */
    List<Game> findByDeveloper(String developer);

    /**
     * Busca jogos por publisher
     */
    List<Game> findByPublisher(String publisher);

    /**
     * Busca jogos lançados após uma data
     */
    List<Game> findByReleaseDateAfter(LocalDate date);

    /**
     * Busca jogos lançados entre duas datas
     */
    List<Game> findByReleaseDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Busca jogos mais populares (com mais usuários)
     */
    @Query("SELECT g FROM Game g WHERE SIZE(g.userLibraries) > 0 " +
            "ORDER BY SIZE(g.userLibraries) DESC")
    List<Game> findMostPopularGames();

    /**
     * Busca jogos comuns entre dois usuários
     */
    @Query("SELECT DISTINCT g FROM Game g " +
            "JOIN g.userLibraries ugl1 " +
            "JOIN g.userLibraries ugl2 " +
            "WHERE ugl1.user.userId = :userId1 AND ugl2.user.userId = :userId2")
    List<Game> findCommonGamesBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * Busca jogos populares entre amigos de um usuário (para recomendações)
     */
    @Query("SELECT g.gameId, COUNT(DISTINCT ugl.user.userId) as userCount " +
            "FROM Game g " +
            "JOIN g.userLibraries ugl " +
            "WHERE ugl.user.userId IN :friendIds " +
            "AND g.gameId NOT IN (" +
            "    SELECT ugl2.game.gameId FROM UserGameLibrary ugl2 WHERE ugl2.user.userId = :userId" +
            ") " +
            "GROUP BY g.gameId " +
            "ORDER BY userCount DESC")
    List<Object[]> findPopularGamesAmongFriends(@Param("friendIds") List<Long> friendIds, @Param("userId") Long userId);

    /**
     * Busca jogos por múltiplos Steam App IDs
     */
    List<Game> findBySteamAppIdIn(List<Integer> steamAppIds);

    /**
     * Busca jogos com tempo total jogado pelos usuários maior que um valor
     */
    @Query("SELECT g FROM Game g WHERE " +
            "(SELECT COALESCE(SUM(ugl.playtimeTotal), 0) FROM UserGameLibrary ugl WHERE ugl.game = g) > :minTotalPlaytime")
    List<Game> findGamesWithTotalPlaytimeGreaterThan(@Param("minTotalPlaytime") Integer minTotalPlaytime);

    /**
     * Conta total de jogos únicos no sistema
     */
    @Query("SELECT COUNT(DISTINCT g) FROM Game g")
    Long countUniqueGames();

    /**
     * Busca jogos recentemente adicionados
     */
    @Query("SELECT g FROM Game g ORDER BY g.createdAt DESC")
    List<Game> findRecentlyAddedGames();

    /**
     * Busca jogos gratuitos (preço atual = 0)
     */
    @Query("SELECT g FROM Game g WHERE g.priceCurrent = 0 OR g.priceCurrent IS NULL")
    List<Game> findFreeGames();

    /**
     * Busca jogos por range de preço
     */
    @Query("SELECT g FROM Game g WHERE g.priceCurrent BETWEEN :minPrice AND :maxPrice")
    List<Game> findGamesByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);
}