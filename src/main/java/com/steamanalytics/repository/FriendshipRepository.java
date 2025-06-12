package com.steamanalytics.repository;

import com.steamanalytics.model.entity.Friendship;
import com.steamanalytics.model.entity.Friendship.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    /**
     * Busca amizade específica entre dois usuários
     */
    Optional<Friendship> findByRequesterUserIdAndAddresseeUserId(Long requesterId, Long addresseeId);

    /**
     * Busca amizade entre dois usuários (em qualquer direção)
     */
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requester.userId = :userId1 AND f.addressee.userId = :userId2) OR " +
            "(f.requester.userId = :userId2 AND f.addressee.userId = :userId1)")
    Optional<Friendship> findFriendshipBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * Busca todas as amizades de um usuário (como requester ou addressee)
     */
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requester.userId = :userId OR f.addressee.userId = :userId)")
    List<Friendship> findAllFriendshipsByUser(@Param("userId") Long userId);

    /**
     * Busca amizades aceitas de um usuário
     */
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requester.userId = :userId OR f.addressee.userId = :userId) " +
            "AND f.status = :status")
    List<Friendship> findFriendshipsByUserAndStatus(@Param("userId") Long userId, @Param("status") FriendshipStatus status);

    /**
     * Busca IDs dos amigos aceitos de um usuário
     */
    @Query("SELECT CASE WHEN f.requester.userId = :userId THEN f.addressee.userId " +
            "ELSE f.requester.userId END " +
            "FROM Friendship f WHERE " +
            "(f.requester.userId = :userId OR f.addressee.userId = :userId) " +
            "AND f.status = 'ACCEPTED'")
    List<Long> findAcceptedFriendIds(@Param("userId") Long userId);

    /**
     * Busca solicitações de amizade pendentes recebidas por um usuário
     */
    @Query("SELECT f FROM Friendship f WHERE f.addressee.userId = :userId AND f.status = 'PENDING'")
    List<Friendship> findPendingReceivedRequests(@Param("userId") Long userId);

    /**
     * Busca solicitações de amizade pendentes enviadas por um usuário
     */
    @Query("SELECT f FROM Friendship f WHERE f.requester.userId = :userId AND f.status = 'PENDING'")
    List<Friendship> findPendingSentRequests(@Param("userId") Long userId);

    /**
     * Conta amigos aceitos de um usuário
     */
    @Query("SELECT COUNT(f) FROM Friendship f WHERE " +
            "(f.requester.userId = :userId OR f.addressee.userId = :userId) " +
            "AND f.status = 'ACCEPTED'")
    Long countAcceptedFriends(@Param("userId") Long userId);

    /**
     * Conta solicitações pendentes recebidas
     */
    @Query("SELECT COUNT(f) FROM Friendship f WHERE f.addressee.userId = :userId AND f.status = 'PENDING'")
    Long countPendingReceivedRequests(@Param("userId") Long userId);

    /**
     * Conta solicitações pendentes enviadas
     */
    @Query("SELECT COUNT(f) FROM Friendship f WHERE f.requester.userId = :userId AND f.status = 'PENDING'")
    Long countPendingSentRequests(@Param("userId") Long userId);

    /**
     * Verifica se existe amizade entre dois usuários
     */
    @Query("SELECT COUNT(f) > 0 FROM Friendship f WHERE " +
            "((f.requester.userId = :userId1 AND f.addressee.userId = :userId2) OR " +
            " (f.requester.userId = :userId2 AND f.addressee.userId = :userId1))")
    boolean existsFriendshipBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * Verifica se dois usuários são amigos (amizade aceita)
     */
    @Query("SELECT COUNT(f) > 0 FROM Friendship f WHERE " +
            "((f.requester.userId = :userId1 AND f.addressee.userId = :userId2) OR " +
            " (f.requester.userId = :userId2 AND f.addressee.userId = :userId1)) " +
            "AND f.status = 'ACCEPTED'")
    boolean areUsersFriends(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * Busca amigos mútuos entre dois usuários
     */
    @Query("SELECT DISTINCT f1.addressee.userId FROM Friendship f1 " +
            "JOIN Friendship f2 ON f1.addressee.userId = f2.addressee.userId " +
            "WHERE f1.requester.userId = :userId1 AND f2.requester.userId = :userId2 " +
            "AND f1.status = 'ACCEPTED' AND f2.status = 'ACCEPTED' " +
            "UNION " +
            "SELECT DISTINCT f1.requester.userId FROM Friendship f1 " +
            "JOIN Friendship f2 ON f1.requester.userId = f2.requester.userId " +
            "WHERE f1.addressee.userId = :userId1 AND f2.addressee.userId = :userId2 " +
            "AND f1.status = 'ACCEPTED' AND f2.status = 'ACCEPTED'")
    List<Long> findMutualFriendIds(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * Busca usuários com mais amigos
     */
    @Query("SELECT f.requester.userId, COUNT(f) as friendCount FROM Friendship f " +
            "WHERE f.status = 'ACCEPTED' " +
            "GROUP BY f.requester.userId " +
            "ORDER BY friendCount DESC")
    List<Object[]> findUsersWithMostFriends();

    /**
     * Remove amizades bloqueadas ou recusadas antigas
     */
    @Query("DELETE FROM Friendship f WHERE f.status IN ('BLOCKED', 'DECLINED') " +
            "AND f.updatedAt < :cutoffDate")
    void deleteOldRejectedFriendships(@Param("cutoffDate") java.time.Instant cutoffDate);
}