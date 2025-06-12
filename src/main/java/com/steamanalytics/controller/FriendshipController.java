package com.steamanalytics.controller;

import com.steamanalytics.model.dto.UserDto;
import com.steamanalytics.model.entity.Friendship;
import com.steamanalytics.model.entity.Friendship.FriendshipStatus;
import com.steamanalytics.service.FriendshipService;
import com.steamanalytics.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friendships")
@CrossOrigin(origins = {"http://localhost:3000"})
public class FriendshipController {

    private final FriendshipService friendshipService;
    private final UserService userService;

    @Autowired
    public FriendshipController(FriendshipService friendshipService, UserService userService) {
        this.friendshipService = friendshipService;
        this.userService = userService;
    }

    /**
     * Envia solicitação de amizade
     */
    @PostMapping("/request")
    public ResponseEntity<FriendshipResponse> sendFriendRequest(@Valid @RequestBody FriendRequestDto request) {
        try {
            Friendship friendship = friendshipService.sendFriendRequest(request.getRequesterId(), request.getAddresseeId());
            FriendshipResponse response = FriendshipResponse.from(friendship);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Aceita solicitação de amizade
     */
    @PutMapping("/{friendshipId}/accept")
    public ResponseEntity<FriendshipResponse> acceptFriendRequest(
            @PathVariable Long friendshipId,
            @RequestParam Long userId) {
        try {
            Friendship friendship = friendshipService.acceptFriendRequest(friendshipId, userId);
            FriendshipResponse response = FriendshipResponse.from(friendship);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Rejeita solicitação de amizade
     */
    @PutMapping("/{friendshipId}/decline")
    public ResponseEntity<FriendshipResponse> declineFriendRequest(
            @PathVariable Long friendshipId,
            @RequestParam Long userId) {
        try {
            Friendship friendship = friendshipService.declineFriendRequest(friendshipId, userId);
            FriendshipResponse response = FriendshipResponse.from(friendship);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Bloqueia usuário
     */
    @PostMapping("/block")
    public ResponseEntity<FriendshipResponse> blockUser(@Valid @RequestBody BlockUserDto request) {
        try {
            Friendship friendship = friendshipService.blockUser(request.getBlockerId(), request.getBlockedId());
            FriendshipResponse response = FriendshipResponse.from(friendship);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Remove amizade
     */
    @DeleteMapping("/{userId1}/{userId2}")
    public ResponseEntity<Void> removeFriendship(
            @PathVariable Long userId1,
            @PathVariable Long userId2) {
        try {
            friendshipService.removeFriendship(userId1, userId2);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Busca amigos aceitos de um usuário
     */
    @GetMapping("/users/{userId}/friends")
    public ResponseEntity<List<UserDto>> getFriends(@PathVariable Long userId) {
        List<Long> friendIds = friendshipService.findAcceptedFriendIds(userId);
        List<UserDto> friends = friendIds.stream()
                .map(friendId -> userService.findById(friendId))
                .filter(optionalUser -> optionalUser.isPresent())
                .map(optionalUser -> UserDto.from(optionalUser.get()))
                .toList();
        return ResponseEntity.ok(friends);
    }

    /**
     * Busca IDs dos amigos aceitos
     */
    @GetMapping("/users/{userId}/friend-ids")
    public ResponseEntity<List<Long>> getFriendIds(@PathVariable Long userId) {
        List<Long> friendIds = friendshipService.findAcceptedFriendIds(userId);
        return ResponseEntity.ok(friendIds);
    }

    /**
     * Busca solicitações pendentes recebidas
     */
    @GetMapping("/users/{userId}/pending-received")
    public ResponseEntity<List<FriendshipResponse>> getPendingReceivedRequests(@PathVariable Long userId) {
        List<Friendship> friendships = friendshipService.findPendingReceivedRequests(userId);
        List<FriendshipResponse> responses = friendships.stream()
                .map(FriendshipResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Busca solicitações pendentes enviadas
     */
    @GetMapping("/users/{userId}/pending-sent")
    public ResponseEntity<List<FriendshipResponse>> getPendingSentRequests(@PathVariable Long userId) {
        List<Friendship> friendships = friendshipService.findPendingSentRequests(userId);
        List<FriendshipResponse> responses = friendships.stream()
                .map(FriendshipResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Busca todas as amizades de um usuário
     */
    @GetMapping("/users/{userId}/all")
    public ResponseEntity<List<FriendshipResponse>> getAllFriendships(@PathVariable Long userId) {
        List<Friendship> friendships = friendshipService.findAllFriendshipsByUser(userId);
        List<FriendshipResponse> responses = friendships.stream()
                .map(FriendshipResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    /**
     * Busca amizades por status
     */
    @GetMapping("/users/{userId}/status/{status}")
    public ResponseEntity<List<FriendshipResponse>> getFriendshipsByStatus(
            @PathVariable Long userId,
            @PathVariable String status) {
        try {
            FriendshipStatus friendshipStatus = FriendshipStatus.valueOf(status.toUpperCase());
            List<Friendship> friendships = friendshipService.findFriendshipsByStatus(userId, friendshipStatus);
            List<FriendshipResponse> responses = friendships.stream()
                    .map(FriendshipResponse::from)
                    .toList();
            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Busca amigos mútuos entre dois usuários
     */
    @GetMapping("/users/{userId1}/mutual/{userId2}")
    public ResponseEntity<List<UserDto>> getMutualFriends(
            @PathVariable Long userId1,
            @PathVariable Long userId2) {
        List<Long> mutualFriendIds = friendshipService.findMutualFriendIds(userId1, userId2);
        List<UserDto> mutualFriends = mutualFriendIds.stream()
                .map(friendId -> userService.findById(friendId))
                .filter(optionalUser -> optionalUser.isPresent())
                .map(optionalUser -> UserDto.from(optionalUser.get()))
                .toList();
        return ResponseEntity.ok(mutualFriends);
    }

    /**
     * Busca status da amizade entre dois usuários
     */
    @GetMapping("/users/{userId1}/status-with/{userId2}")
    public ResponseEntity<FriendshipStatusResponse> getFriendshipStatus(
            @PathVariable Long userId1,
            @PathVariable Long userId2) {

        FriendshipStatusResponse response = new FriendshipStatusResponse();
        response.setUserId1(userId1);
        response.setUserId2(userId2);
        response.setAreFriends(friendshipService.areUsersFriends(userId1, userId2));
        response.setStatus(friendshipService.getFriendshipStatus(userId1, userId2).orElse(null));
        response.setCanSendRequest(friendshipService.canSendFriendRequest(userId1, userId2));

        return ResponseEntity.ok(response);
    }

    /**
     * Verifica se dois usuários são amigos
     */
    @GetMapping("/users/{userId1}/are-friends/{userId2}")
    public ResponseEntity<Boolean> areUsersFriends(
            @PathVariable Long userId1,
            @PathVariable Long userId2) {
        boolean areFriends = friendshipService.areUsersFriends(userId1, userId2);
        return ResponseEntity.ok(areFriends);
    }

    /**
     * Verifica se usuário pode enviar solicitação
     */
    @GetMapping("/users/{requesterId}/can-request/{addresseeId}")
    public ResponseEntity<Boolean> canSendFriendRequest(
            @PathVariable Long requesterId,
            @PathVariable Long addresseeId) {
        boolean canSendRequest = friendshipService.canSendFriendRequest(requesterId, addresseeId);
        return ResponseEntity.ok(canSendRequest);
    }

    /**
     * Busca estatísticas de amizade do usuário
     */
    @GetMapping("/users/{userId}/stats")
    public ResponseEntity<Map<String, Long>> getUserFriendshipStats(@PathVariable Long userId) {
        Map<String, Long> stats = friendshipService.getUserFriendshipStats(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Conta amigos aceitos
     */
    @GetMapping("/users/{userId}/count/friends")
    public ResponseEntity<Long> countFriends(@PathVariable Long userId) {
        Long count = friendshipService.countAcceptedFriends(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * Conta solicitações pendentes recebidas
     */
    @GetMapping("/users/{userId}/count/pending-received")
    public ResponseEntity<Long> countPendingReceivedRequests(@PathVariable Long userId) {
        Long count = friendshipService.countPendingReceivedRequests(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * Conta solicitações pendentes enviadas
     */
    @GetMapping("/users/{userId}/count/pending-sent")
    public ResponseEntity<Long> countPendingSentRequests(@PathVariable Long userId) {
        Long count = friendshipService.countPendingSentRequests(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * Busca usuários com mais amigos
     */
    @GetMapping("/leaderboard/most-friends")
    public ResponseEntity<List<UserFriendCountDto>> getUsersWithMostFriends() {
        List<Object[]> results = friendshipService.findUsersWithMostFriends();
        List<UserFriendCountDto> leaderboard = results.stream()
                .map(result -> {
                    Long userId = (Long) result[0];
                    Long friendCount = (Long) result[1];
                    return new UserFriendCountDto(userId, friendCount);
                })
                .toList();
        return ResponseEntity.ok(leaderboard);
    }

    /**
     * Limpeza de amizades antigas rejeitadas
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<Void> cleanupOldRejectedFriendships(@RequestParam(defaultValue = "30") int daysOld) {
        friendshipService.cleanupOldRejectedFriendships(daysOld);
        return ResponseEntity.noContent().build();
    }

    // DTOs para requests e responses

    public static class FriendRequestDto {
        private Long requesterId;
        private Long addresseeId;

        // Getters e Setters
        public Long getRequesterId() { return requesterId; }
        public void setRequesterId(Long requesterId) { this.requesterId = requesterId; }
        public Long getAddresseeId() { return addresseeId; }
        public void setAddresseeId(Long addresseeId) { this.addresseeId = addresseeId; }
    }

    public static class BlockUserDto {
        private Long blockerId;
        private Long blockedId;

        // Getters e Setters
        public Long getBlockerId() { return blockerId; }
        public void setBlockerId(Long blockerId) { this.blockerId = blockerId; }
        public Long getBlockedId() { return blockedId; }
        public void setBlockedId(Long blockedId) { this.blockedId = blockedId; }
    }

    public static class FriendshipResponse {
        private Long id;
        private UserDto requester;
        private UserDto addressee;
        private FriendshipStatus status;
        private java.time.Instant createdAt;
        private java.time.Instant updatedAt;

        public static FriendshipResponse from(Friendship friendship) {
            FriendshipResponse response = new FriendshipResponse();
            response.id = friendship.getId();
            response.requester = UserDto.from(friendship.getRequester());
            response.addressee = UserDto.from(friendship.getAddressee());
            response.status = friendship.getStatus();
            response.createdAt = friendship.getCreatedAt();
            response.updatedAt = friendship.getUpdatedAt();
            return response;
        }

        // Getters e Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public UserDto getRequester() { return requester; }
        public void setRequester(UserDto requester) { this.requester = requester; }
        public UserDto getAddressee() { return addressee; }
        public void setAddressee(UserDto addressee) { this.addressee = addressee; }
        public FriendshipStatus getStatus() { return status; }
        public void setStatus(FriendshipStatus status) { this.status = status; }
        public java.time.Instant getCreatedAt() { return createdAt; }
        public void setCreatedAt(java.time.Instant createdAt) { this.createdAt = createdAt; }
        public java.time.Instant getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(java.time.Instant updatedAt) { this.updatedAt = updatedAt; }
    }

    public static class FriendshipStatusResponse {
        private Long userId1;
        private Long userId2;
        private Boolean areFriends;
        private FriendshipStatus status;
        private Boolean canSendRequest;

        // Getters e Setters
        public Long getUserId1() { return userId1; }
        public void setUserId1(Long userId1) { this.userId1 = userId1; }
        public Long getUserId2() { return userId2; }
        public void setUserId2(Long userId2) { this.userId2 = userId2; }
        public Boolean getAreFriends() { return areFriends; }
        public void setAreFriends(Boolean areFriends) { this.areFriends = areFriends; }
        public FriendshipStatus getStatus() { return status; }
        public void setStatus(FriendshipStatus status) { this.status = status; }
        public Boolean getCanSendRequest() { return canSendRequest; }
        public void setCanSendRequest(Boolean canSendRequest) { this.canSendRequest = canSendRequest; }
    }

    public static class UserFriendCountDto {
        private Long userId;
        private Long friendCount;

        public UserFriendCountDto(Long userId, Long friendCount) {
            this.userId = userId;
            this.friendCount = friendCount;
        }

        // Getters e Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getFriendCount() { return friendCount; }
        public void setFriendCount(Long friendCount) { this.friendCount = friendCount; }
    }
}