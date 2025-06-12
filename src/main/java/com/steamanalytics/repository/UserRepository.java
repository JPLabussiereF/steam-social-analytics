package com.steamanalytics.repository;

import com.steamanalytics.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca usuário pelo Steam ID
     */
    Optional<User> findBySteamId(Long steamId);

    /**
     * Busca usuário pelo username
     */
    Optional<User> findByUsername(String username);

    /**
     * Verifica se existe usuário com o Steam ID
     */
    boolean existsBySteamId(Long steamId);

    /**
     * Verifica se existe usuário com o username
     */
    boolean existsByUsername(String username);

    /**
     * Busca usuários ativos
     */
    List<User> findByIsActiveTrue();

    /**
     * Busca usuários por país
     */
    List<User> findByCountryCode(String countryCode);

    /**
     * Busca usuários que fizeram login recentemente
     */
    @Query("SELECT u FROM User u WHERE u.lastLogin >= :since AND u.isActive = true")
    List<User> findActiveUsersLoggedInSince(@Param("since") Instant since);

    /**
     * Busca usuários para sincronização (ativos que fizeram login recentemente)
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true AND " +
            "(u.lastLogin >= :recentThreshold OR u.lastLogin IS NULL) " +
            "ORDER BY u.lastLogin DESC NULLS LAST")
    List<User> findActiveUsersForSync(@Param("recentThreshold") Instant recentThreshold);

    /**
     * Busca usuários por parte do display name
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true AND " +
            "(LOWER(u.displayName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            " LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<User> searchActiveUsersByName(@Param("searchTerm") String searchTerm);

    /**
     * Conta usuários ativos
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    Long countActiveUsers();

    /**
     * Busca usuários com mais jogos na biblioteca
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true AND SIZE(u.gameLibrary) > 0 " +
            "ORDER BY SIZE(u.gameLibrary) DESC")
    List<User> findUsersWithMostGames();

    /**
     * Busca usuários que possuem um jogo específico
     */
    @Query("SELECT DISTINCT ugl.user FROM UserGameLibrary ugl WHERE ugl.game.steamAppId = :steamAppId")
    List<User> findUsersByGameAppId(@Param("steamAppId") Integer steamAppId);

    /**
     * Busca usuários com tempo total de jogo acima de um limite
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true AND " +
            "(SELECT COALESCE(SUM(ugl.playtimeTotal), 0) FROM UserGameLibrary ugl WHERE ugl.user = u) > :minPlaytime")
    List<User> findUsersWithPlaytimeGreaterThan(@Param("minPlaytime") Integer minPlaytime);
}