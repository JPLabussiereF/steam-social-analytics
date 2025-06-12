package com.steamanalytics.service;

import com.steamanalytics.model.entity.User;
import com.steamanalytics.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Busca usuário por ID
     */
    @Cacheable(value = "userProfile", key = "#userId")
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Busca usuário por Steam ID
     */
    @Cacheable(value = "userProfile", key = "'steam_' + #steamId")
    public Optional<User> findBySteamId(Long steamId) {
        return userRepository.findBySteamId(steamId);
    }

    /**
     * Busca usuário por username
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Cria novo usuário ou retorna existente
     */
    @CacheEvict(value = "userProfile", allEntries = true)
    public User findOrCreateUser(Long steamId) {
        return userRepository.findBySteamId(steamId)
                .orElseGet(() -> {
                    User newUser = new User(steamId, "user_" + steamId);
                    return userRepository.save(newUser);
                });
    }

    /**
     * Cria novo usuário
     */
    @CacheEvict(value = "userProfile", allEntries = true)
    public User createUser(Long steamId, String username, String displayName) {
        if (userRepository.existsBySteamId(steamId)) {
            throw new IllegalArgumentException("User with Steam ID already exists: " + steamId);
        }

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        User user = new User(steamId, username, displayName);
        return userRepository.save(user);
    }

    /**
     * Atualiza perfil do usuário
     */
    @CacheEvict(value = "userProfile", key = "'steam_' + #user.steamId")
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Atualiza informações básicas do usuário
     */
    @CacheEvict(value = "userProfile", key = "'steam_' + #steamId")
    public User updateUserProfile(Long steamId, String displayName, String profileUrl,
                                  String avatarUrl, String countryCode) {
        User user = userRepository.findBySteamId(steamId)
                .orElseThrow(() -> new RuntimeException("User not found with Steam ID: " + steamId));

        user.setDisplayName(displayName);
        user.setProfileUrl(profileUrl);
        user.setAvatarUrl(avatarUrl);
        user.setCountryCode(countryCode);

        return userRepository.save(user);
    }

    /**
     * Atualiza último login do usuário
     */
    @CacheEvict(value = "userProfile", key = "'steam_' + #steamId")
    public User updateLastLogin(Long steamId) {
        User user = userRepository.findBySteamId(steamId)
                .orElseThrow(() -> new RuntimeException("User not found with Steam ID: " + steamId));

        user.setLastLogin(Instant.now());
        return userRepository.save(user);
    }

    /**
     * Desativa usuário
     */
    @CacheEvict(value = "userProfile", key = "'steam_' + #steamId")
    public User deactivateUser(Long steamId) {
        User user = userRepository.findBySteamId(steamId)
                .orElseThrow(() -> new RuntimeException("User not found with Steam ID: " + steamId));

        user.setIsActive(false);
        return userRepository.save(user);
    }

    /**
     * Reativa usuário
     */
    @CacheEvict(value = "userProfile", key = "'steam_' + #steamId")
    public User reactivateUser(Long steamId) {
        User user = userRepository.findBySteamId(steamId)
                .orElseThrow(() -> new RuntimeException("User not found with Steam ID: " + steamId));

        user.setIsActive(true);
        user.setLastLogin(Instant.now());
        return userRepository.save(user);
    }

    /**
     * Busca usuários ativos
     */
    public List<User> findActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }

    /**
     * Busca usuários por país
     */
    public List<User> findUsersByCountry(String countryCode) {
        return userRepository.findByCountryCode(countryCode);
    }

    /**
     * Busca usuários que fizeram login recentemente
     */
    public List<User> findRecentlyActiveUsers(int hours) {
        Instant since = Instant.now().minus(hours, ChronoUnit.HOURS);
        return userRepository.findActiveUsersLoggedInSince(since);
    }

    /**
     * Busca usuários para sincronização
     */
    public List<User> findUsersForSync() {
        Instant recentThreshold = Instant.now().minus(24, ChronoUnit.HOURS);
        return userRepository.findActiveUsersForSync(recentThreshold);
    }

    /**
     * Busca usuários por nome
     */
    public List<User> searchUsersByName(String searchTerm) {
        return userRepository.searchActiveUsersByName(searchTerm);
    }

    /**
     * Busca usuários com mais jogos
     */
    public List<User> findUsersWithMostGames() {
        return userRepository.findUsersWithMostGames();
    }

    /**
     * Busca usuários que possuem um jogo específico
     */
    public List<User> findUsersByGame(Integer steamAppId) {
        return userRepository.findUsersByGameAppId(steamAppId);
    }

    /**
     * Busca usuários com tempo de jogo acima de um limite
     */
    public List<User> findUsersWithHighPlaytime(Integer minPlaytimeMinutes) {
        return userRepository.findUsersWithPlaytimeGreaterThan(minPlaytimeMinutes);
    }

    /**
     * Conta usuários ativos
     */
    public Long countActiveUsers() {
        return userRepository.countActiveUsers();
    }

    /**
     * Verifica se usuário existe por Steam ID
     */
    public boolean existsBySteamId(Long steamId) {
        return userRepository.existsBySteamId(steamId);
    }

    /**
     * Verifica se username existe
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Busca todos os usuários (com paginação)
     */
    public Page<User> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Remove usuário (soft delete - apenas desativa)
     */
    @CacheEvict(value = "userProfile", key = "'steam_' + #steamId")
    public void deleteUser(Long steamId) {
        deactivateUser(steamId);
    }

    /**
     * Remove usuário permanentemente
     */
    @CacheEvict(value = "userProfile", allEntries = true)
    public void deleteUserPermanently(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(userId);
    }

    /**
     * Salva múltiplos usuários
     */
    @CacheEvict(value = "userProfile", allEntries = true)
    public List<User> saveAll(List<User> users) {
        return userRepository.saveAll(users);
    }

    /**
     * Atualiza visibilidade do perfil
     */
    @CacheEvict(value = "userProfile", key = "'steam_' + #steamId")
    public User updateProfileVisibility(Long steamId, Integer visibility) {
        User user = userRepository.findBySteamId(steamId)
                .orElseThrow(() -> new RuntimeException("User not found with Steam ID: " + steamId));

        user.setProfileVisibility(visibility);
        return userRepository.save(user);
    }

    /**
     * Conta total de usuários
     */
    public Long countTotalUsers() {
        return userRepository.count();
    }

    /**
     * Validações e utilitários
     */
    public boolean isValidSteamId(Long steamId) {
        return steamId != null && steamId > 0;
    }

    public boolean isValidUsername(String username) {
        return username != null && !username.trim().isEmpty() && username.length() <= 100;
    }
}