package com.steamanalytics.controller;

import com.steamanalytics.model.dto.UserDto;
import com.steamanalytics.model.entity.User;
import com.steamanalytics.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:3000"})
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Busca usuário por ID
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        return userService.findById(userId)
                .map(user -> ResponseEntity.ok(UserDto.from(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Busca usuário por Steam ID
     */
    @GetMapping("/steam/{steamId}")
    public ResponseEntity<UserDto> getUserBySteamId(@PathVariable Long steamId) {
        return userService.findBySteamId(steamId)
                .map(user -> ResponseEntity.ok(UserDto.from(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Busca usuário por username
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
        return userService.findByUsername(username)
                .map(user -> ResponseEntity.ok(UserDto.from(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Busca todos os usuários com paginação
     */
    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllUsers(Pageable pageable) {
        Page<User> users = userService.findAllUsers(pageable);
        Page<UserDto> userDtos = users.map(UserDto::from);
        return ResponseEntity.ok(userDtos);
    }

    /**
     * Busca usuários ativos
     */
    @GetMapping("/active")
    public ResponseEntity<List<UserDto>> getActiveUsers() {
        List<User> users = userService.findActiveUsers();
        List<UserDto> userDtos = users.stream().map(UserDto::from).toList();
        return ResponseEntity.ok(userDtos);
    }

    /**
     * Busca usuários por país
     */
    @GetMapping("/country/{countryCode}")
    public ResponseEntity<List<UserDto>> getUsersByCountry(@PathVariable String countryCode) {
        List<User> users = userService.findUsersByCountry(countryCode);
        List<UserDto> userDtos = users.stream().map(UserDto::from).toList();
        return ResponseEntity.ok(userDtos);
    }

    /**
     * Busca usuários recentemente ativos
     */
    @GetMapping("/recent")
    public ResponseEntity<List<UserDto>> getRecentlyActiveUsers(@RequestParam(defaultValue = "24") int hours) {
        List<User> users = userService.findRecentlyActiveUsers(hours);
        List<UserDto> userDtos = users.stream().map(UserDto::from).toList();
        return ResponseEntity.ok(userDtos);
    }

    /**
     * Busca usuários por nome (search)
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> searchUsers(@RequestParam String query) {
        if (query.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        List<User> users = userService.searchUsersByName(query);
        List<UserDto> userDtos = users.stream().map(UserDto::from).toList();
        return ResponseEntity.ok(userDtos);
    }

    /**
     * Busca usuários com mais jogos
     */
    @GetMapping("/top-gamers")
    public ResponseEntity<List<UserDto>> getUsersWithMostGames() {
        List<User> users = userService.findUsersWithMostGames();
        List<UserDto> userDtos = users.stream().map(UserDto::from).toList();
        return ResponseEntity.ok(userDtos);
    }

    /**
     * Busca usuários que possuem um jogo específico
     */
    @GetMapping("/game/{steamAppId}")
    public ResponseEntity<List<UserDto>> getUsersByGame(@PathVariable Integer steamAppId) {
        List<User> users = userService.findUsersByGame(steamAppId);
        List<UserDto> userDtos = users.stream().map(UserDto::from).toList();
        return ResponseEntity.ok(userDtos);
    }

    /**
     * Cria novo usuário
     */
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            User user = userService.createUser(
                    request.getSteamId(),
                    request.getUsername(),
                    request.getDisplayName()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(UserDto.from(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cria ou busca usuário por Steam ID
     */
    @PostMapping("/find-or-create")
    public ResponseEntity<UserDto> findOrCreateUser(@Valid @RequestBody FindOrCreateUserRequest request) {
        User user = userService.findOrCreateUser(request.getSteamId());
        return ResponseEntity.ok(UserDto.from(user));
    }

    /**
     * Atualiza perfil do usuário
     */
    @PutMapping("/{steamId}/profile")
    public ResponseEntity<UserDto> updateUserProfile(
            @PathVariable Long steamId,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        try {
            User user = userService.updateUserProfile(
                    steamId,
                    request.getDisplayName(),
                    request.getProfileUrl(),
                    request.getAvatarUrl(),
                    request.getCountryCode()
            );
            return ResponseEntity.ok(UserDto.from(user));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Atualiza último login
     */
    @PutMapping("/{steamId}/last-login")
    public ResponseEntity<UserDto> updateLastLogin(@PathVariable Long steamId) {
        try {
            User user = userService.updateLastLogin(steamId);
            return ResponseEntity.ok(UserDto.from(user));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Atualiza visibilidade do perfil
     */
    @PutMapping("/{steamId}/visibility")
    public ResponseEntity<UserDto> updateProfileVisibility(
            @PathVariable Long steamId,
            @RequestParam Integer visibility) {
        try {
            User user = userService.updateProfileVisibility(steamId, visibility);
            return ResponseEntity.ok(UserDto.from(user));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Desativa usuário
     */
    @PutMapping("/{steamId}/deactivate")
    public ResponseEntity<UserDto> deactivateUser(@PathVariable Long steamId) {
        try {
            User user = userService.deactivateUser(steamId);
            return ResponseEntity.ok(UserDto.from(user));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Reativa usuário
     */
    @PutMapping("/{steamId}/reactivate")
    public ResponseEntity<UserDto> reactivateUser(@PathVariable Long steamId) {
        try {
            User user = userService.reactivateUser(steamId);
            return ResponseEntity.ok(UserDto.from(user));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Remove usuário (soft delete)
     */
    @DeleteMapping("/{steamId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long steamId) {
        try {
            userService.deleteUser(steamId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Conta usuários ativos
     */
    @GetMapping("/count/active")
    public ResponseEntity<Long> countActiveUsers() {
        Long count = userService.countActiveUsers();
        return ResponseEntity.ok(count);
    }

    /**
     * Conta total de usuários
     */
    @GetMapping("/count/total")
    public ResponseEntity<Long> countTotalUsers() {
        Long count = userService.countTotalUsers();
        return ResponseEntity.ok(count);
    }

    /**
     * Verifica se Steam ID existe
     */
    @GetMapping("/exists/steam/{steamId}")
    public ResponseEntity<Boolean> existsBySteamId(@PathVariable Long steamId) {
        boolean exists = userService.existsBySteamId(steamId);
        return ResponseEntity.ok(exists);
    }

    /**
     * Verifica se username existe
     */
    @GetMapping("/exists/username/{username}")
    public ResponseEntity<Boolean> existsByUsername(@PathVariable String username) {
        boolean exists = userService.existsByUsername(username);
        return ResponseEntity.ok(exists);
    }

    // DTOs para requests
    public static class CreateUserRequest {
        private Long steamId;
        private String username;
        private String displayName;

        // Getters e Setters
        public Long getSteamId() { return steamId; }
        public void setSteamId(Long steamId) { this.steamId = steamId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
    }

    public static class FindOrCreateUserRequest {
        private Long steamId;

        // Getters e Setters
        public Long getSteamId() { return steamId; }
        public void setSteamId(Long steamId) { this.steamId = steamId; }
    }

    public static class UpdateUserProfileRequest {
        private String displayName;
        private String profileUrl;
        private String avatarUrl;
        private String countryCode;

        // Getters e Setters
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public String getProfileUrl() { return profileUrl; }
        public void setProfileUrl(String profileUrl) { this.profileUrl = profileUrl; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    }
}