package com.steamanalytics.service;

import com.steamanalytics.model.entity.Friendship;
import com.steamanalytics.model.entity.Friendship.FriendshipStatus;
import com.steamanalytics.model.entity.User;
import com.steamanalytics.repository.FriendshipRepository;
import com.steamanalytics.repository.UserRepository;
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
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    @Autowired
    public FriendshipService(FriendshipRepository friendshipRepository, UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    /**
     * Envia solicitação de amizade
     */
    @CacheEvict(value = "friendList", allEntries = true)
    public Friendship sendFriendRequest(Long requesterId, Long addresseeId) {
        if (requesterId.equals(addresseeId)) {
            throw new IllegalArgumentException("Cannot send friend request to yourself");
        }

        // Verificar se já existe amizade
        Optional<Friendship> existingFriendship = friendshipRepository
                .findFriendshipBetweenUsers(requesterId, addresseeId);

        if (existingFriendship.isPresent()) {
            Friendship friendship = existingFriendship.get();
            if (friendship.getStatus() == FriendshipStatus.ACCEPTED) {
                throw new IllegalStateException("Users are already friends");
            } else if (friendship.getStatus() == FriendshipStatus.PENDING) {
                throw new IllegalStateException("Friend request already pending");
            } else if (friendship.getStatus() == FriendshipStatus.BLOCKED) {
                throw new IllegalStateException("Cannot send request - user is blocked");
            }
        }

        // Buscar usuários
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found"));
        User addressee = userRepository.findById(addresseeId)
                .orElseThrow(() -> new RuntimeException("Addressee not found"));

        // Criar nova solicitação
        Friendship friendship = new Friendship(requester, addressee, FriendshipStatus.PENDING);
        return friendshipRepository.save(friendship);
    }

    /**
     * Aceita solicitação de amizade
     */
    @CacheEvict(value = "friendList", allEntries = true)
    public Friendship acceptFriendRequest(Long friendshipId, Long userId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Friendship not found"));

        // Verificar se o usuário pode aceitar (deve ser o addressee)
        if (!friendship.getAddressee().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Only the addressee can accept the request");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalStateException("Request is not in pending status");
        }

        friendship.accept();
        return friendshipRepository.save(friendship);
    }

    /**
     * Rejeita solicitação de amizade
     */
    @CacheEvict(value = "friendList", allEntries = true)
    public Friendship declineFriendRequest(Long friendshipId, Long userId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Friendship not found"));

        // Verificar se o usuário pode rejeitar (deve ser o addressee)
        if (!friendship.getAddressee().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Only the addressee can decline the request");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalStateException("Request is not in pending status");
        }

        friendship.decline();
        return friendshipRepository.save(friendship);
    }

    /**
     * Bloqueia usuário
     */
    @CacheEvict(value = "friendList", allEntries = true)
    public Friendship blockUser(Long blockerId, Long blockedId) {
        if (blockerId.equals(blockedId)) {
            throw new IllegalArgumentException("Cannot block yourself");
        }

        // Buscar amizade existente ou criar nova
        Optional<Friendship> existingFriendship = friendshipRepository
                .findFriendshipBetweenUsers(blockerId, blockedId);

        Friendship friendship;
        if (existingFriendship.isPresent()) {
            friendship = existingFriendship.get();
        } else {
            User blocker = userRepository.findById(blockerId)
                    .orElseThrow(() -> new RuntimeException("Blocker not found"));
            User blocked = userRepository.findById(blockedId)
                    .orElseThrow(() -> new RuntimeException("Blocked user not found"));

            friendship = new Friendship(blocker, blocked);
        }

        friendship.block();
        return friendshipRepository.save(friendship);
    }

    /**
     * Remove amizade (desfazer amizade)
     */
    @CacheEvict(value = "friendList", allEntries = true)
    public void removeFriendship(Long userId1, Long userId2) {
        Friendship friendship = friendshipRepository.findFriendshipBetweenUsers(userId1, userId2)
                .orElseThrow(() -> new RuntimeException("Friendship not found"));

        friendshipRepository.delete(friendship);
    }

    /**
     * Busca amizade entre dois usuários
     */
    public Optional<Friendship> findFriendshipBetweenUsers(Long userId1, Long userId2) {
        return friendshipRepository.findFriendshipBetweenUsers(userId1, userId2);
    }

    /**
     * Busca todas as amizades de um usuário
     */
    public List<Friendship> findAllFriendshipsByUser(Long userId) {
        return friendshipRepository.findAllFriendshipsByUser(userId);
    }

    /**
     * Busca amizades por status
     */
    public List<Friendship> findFriendshipsByStatus(Long userId, FriendshipStatus status) {
        return friendshipRepository.findFriendshipsByUserAndStatus(userId, status);
    }

    /**
     * Busca IDs dos amigos aceitos
     */
    @Cacheable(value = "friendList", key = "#userId")
    public List<Long> findAcceptedFriendIds(Long userId) {
        return friendshipRepository.findAcceptedFriendIds(userId);
    }

    /**
     * Busca solicitações pendentes recebidas
     */
    public List<Friendship> findPendingReceivedRequests(Long userId) {
        return friendshipRepository.findPendingReceivedRequests(userId);
    }

    /**
     * Busca solicitações pendentes enviadas
     */
    public List<Friendship> findPendingSentRequests(Long userId) {
        return friendshipRepository.findPendingSentRequests(userId);
    }

    /**
     * Conta amigos aceitos
     */
    public Long countAcceptedFriends(Long userId) {
        return friendshipRepository.countAcceptedFriends(userId);
    }

    /**
     * Conta solicitações pendentes recebidas
     */
    public Long countPendingReceivedRequests(Long userId) {
        return friendshipRepository.countPendingReceivedRequests(userId);
    }

    /**
     * Conta solicitações pendentes enviadas
     */
    public Long countPendingSentRequests(Long userId) {
        return friendshipRepository.countPendingSentRequests(userId);
    }

    /**
     * Verifica se existe amizade entre usuários
     */
    public boolean existsFriendshipBetweenUsers(Long userId1, Long userId2) {
        return friendshipRepository.existsFriendshipBetweenUsers(userId1, userId2);
    }

    /**
     * Verifica se dois usuários são amigos
     */
    public boolean areUsersFriends(Long userId1, Long userId2) {
        return friendshipRepository.areUsersFriends(userId1, userId2);
    }

    /**
     * Busca amigos mútuos entre dois usuários
     */
    public List<Long> findMutualFriendIds(Long userId1, Long userId2) {
        return friendshipRepository.findMutualFriendIds(userId1, userId2);
    }

    /**
     * Busca usuários com mais amigos
     */
    public List<Object[]> findUsersWithMostFriends() {
        return friendshipRepository.findUsersWithMostFriends();
    }

    /**
     * Busca status da amizade entre dois usuários
     */
    public Optional<FriendshipStatus> getFriendshipStatus(Long userId1, Long userId2) {
        return friendshipRepository.findFriendshipBetweenUsers(userId1, userId2)
                .map(Friendship::getStatus);
    }

    /**
     * Verifica se usuário pode enviar solicitação de amizade
     */
    public boolean canSendFriendRequest(Long requesterId, Long addresseeId) {
        if (requesterId.equals(addresseeId)) {
            return false;
        }

        Optional<Friendship> existingFriendship = friendshipRepository
                .findFriendshipBetweenUsers(requesterId, addresseeId);

        if (existingFriendship.isEmpty()) {
            return true;
        }

        FriendshipStatus status = existingFriendship.get().getStatus();
        return status == FriendshipStatus.DECLINED; // Pode tentar novamente se foi recusado
    }

    /**
     * Busca estatísticas de amizade do usuário
     */
    public Map<String, Long> getUserFriendshipStats(Long userId) {
        return Map.of(
                "acceptedFriends", countAcceptedFriends(userId),
                "pendingReceived", countPendingReceivedRequests(userId),
                "pendingSent", countPendingSentRequests(userId)
        );
    }

    /**
     * Remove amizades antigas rejeitadas/bloqueadas (limpeza)
     */
    @CacheEvict(value = "friendList", allEntries = true)
    public void cleanupOldRejectedFriendships(int daysOld) {
        Instant cutoffDate = Instant.now().minus(daysOld, ChronoUnit.DAYS);
        friendshipRepository.deleteOldRejectedFriendships(cutoffDate);
    }
}