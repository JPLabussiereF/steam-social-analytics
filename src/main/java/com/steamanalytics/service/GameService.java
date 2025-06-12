package com.steamanalytics.service;

import com.steamanalytics.model.entity.Game;
import com.steamanalytics.model.entity.User;
import com.steamanalytics.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class GameService {

    private final GameRepository gameRepository;

    @Autowired
    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    /**
     * Busca jogo por ID
     */
    @Cacheable(value = "gameInfo", key = "#gameId")
    public Optional<Game> findById(Long gameId) {
        return gameRepository.findById(gameId);
    }

    /**
     * Busca jogo por Steam App ID
     */
    @Cacheable(value = "gameInfo", key = "'steam_' + #steamAppId")
    public Optional<Game> findBySteamAppId(Integer steamAppId) {
        return gameRepository.findBySteamAppId(steamAppId);
    }

    /**
     * Salva ou atualiza jogo
     */
    @CacheEvict(value = "gameInfo", allEntries = true)
    public Game save(Game game) {
        return gameRepository.save(game);
    }

    /**
     * Cria novo jogo ou retorna existente
     */
    public Game findOrCreateGame(Integer steamAppId, String name, String description) {
        return gameRepository.findBySteamAppId(steamAppId)
                .orElseGet(() -> {
                    Game newGame = new Game(steamAppId, name, description);
                    return gameRepository.save(newGame);
                });
    }

    /**
     * Atualiza informações do jogo
     */
    @CacheEvict(value = "gameInfo", key = "'steam_' + #steamAppId")
    public Game updateGameInfo(Integer steamAppId, String name, String description,
                               String developer, String publisher, LocalDate releaseDate) {
        Game game = gameRepository.findBySteamAppId(steamAppId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        game.setName(name);
        game.setDescription(description);
        game.setDeveloper(developer);
        game.setPublisher(publisher);
        game.setReleaseDate(releaseDate);

        return gameRepository.save(game);
    }

    /**
     * Atualiza preços do jogo
     */
    @CacheEvict(value = "gameInfo", key = "'steam_' + #steamAppId")
    public Game updateGamePrices(Integer steamAppId, BigDecimal priceInitial, BigDecimal priceCurrent) {
        Game game = gameRepository.findBySteamAppId(steamAppId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        game.setPriceInitial(priceInitial);
        game.setPriceCurrent(priceCurrent);

        return gameRepository.save(game);
    }

    /**
     * Atualiza metadados do jogo (tags, categorias, gêneros)
     */
    @CacheEvict(value = "gameInfo", key = "'steam_' + #steamAppId")
    public Game updateGameMetadata(Integer steamAppId, Map<String, Object> tags,
                                   Map<String, Object> categories, Map<String, Object> genres) {
        Game game = gameRepository.findBySteamAppId(steamAppId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        game.setTags(tags);
        game.setCategories(categories);
        game.setGenres(genres);

        return gameRepository.save(game);
    }

    /**
     * Busca jogos por nome (busca parcial)
     */
    public List<Game> searchGamesByName(String name) {
        return gameRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * Busca jogos por desenvolvedor
     */
    public List<Game> findGamesByDeveloper(String developer) {
        return gameRepository.findByDeveloper(developer);
    }

    /**
     * Busca jogos por publisher
     */
    public List<Game> findGamesByPublisher(String publisher) {
        return gameRepository.findByPublisher(publisher);
    }

    /**
     * Busca jogos lançados após uma data
     */
    public List<Game> findGamesReleasedAfter(LocalDate date) {
        return gameRepository.findByReleaseDateAfter(date);
    }

    /**
     * Busca jogos lançados em um período
     */
    public List<Game> findGamesReleasedBetween(LocalDate startDate, LocalDate endDate) {
        return gameRepository.findByReleaseDateBetween(startDate, endDate);
    }

    /**
     * Busca jogos mais populares (com mais jogadores)
     */
    @Cacheable(value = "gameInfo", key = "'popular_games'")
    public List<Game> findMostPopularGames() {
        return gameRepository.findMostPopularGames();
    }

    /**
     * Busca jogos comuns entre dois usuários
     */
    public List<Game> findCommonGamesBetweenUsers(Long userId1, Long userId2) {
        return gameRepository.findCommonGamesBetweenUsers(userId1, userId2);
    }

    /**
     * Busca jogos populares entre amigos (para recomendações)
     */
    public List<Object[]> findPopularGamesAmongFriends(List<Long> friendIds, Long userId) {
        return gameRepository.findPopularGamesAmongFriends(friendIds, userId);
    }

    /**
     * Busca múltiplos jogos por Steam App IDs
     */
    public List<Game> findGamesBySteamAppIds(List<Integer> steamAppIds) {
        return gameRepository.findBySteamAppIdIn(steamAppIds);
    }

    /**
     * Busca jogos com muito tempo total jogado
     */
    public List<Game> findGamesWithHighPlaytime(Integer minTotalPlaytime) {
        return gameRepository.findGamesWithTotalPlaytimeGreaterThan(minTotalPlaytime);
    }

    /**
     * Busca jogos recentemente adicionados
     */
    public List<Game> findRecentlyAddedGames() {
        return gameRepository.findRecentlyAddedGames();
    }

    /**
     * Busca jogos gratuitos
     */
    @Cacheable(value = "gameInfo", key = "'free_games'")
    public List<Game> findFreeGames() {
        return gameRepository.findFreeGames();
    }

    /**
     * Busca jogos por faixa de preço
     */
    public List<Game> findGamesByPriceRange(Double minPrice, Double maxPrice) {
        return gameRepository.findGamesByPriceRange(minPrice, maxPrice);
    }

    /**
     * Conta total de jogos únicos
     */
    public Long countUniqueGames() {
        return gameRepository.countUniqueGames();
    }

    /**
     * Verifica se jogo existe por Steam App ID
     */
    public boolean existsBySteamAppId(Integer steamAppId) {
        return gameRepository.existsBySteamAppId(steamAppId);
    }

    /**
     * Busca todos os jogos (com paginação)
     */
    public Page<Game> findAllGames(Pageable pageable) {
        return gameRepository.findAll(pageable);
    }

    /**
     * Remove jogo
     */
    @CacheEvict(value = "gameInfo", allEntries = true)
    public void deleteGame(Long gameId) {
        if (!gameRepository.existsById(gameId)) {
            throw new RuntimeException("Game not found");
        }
        gameRepository.deleteById(gameId);
    }

    /**
     * Salva múltiplos jogos em lote
     */
    @CacheEvict(value = "gameInfo", allEntries = true)
    public List<Game> saveAll(List<Game> games) {
        return gameRepository.saveAll(games);
    }

    /**
     * Conta total de jogos
     */
    public Long countTotalGames() {
        return gameRepository.count();
    }

    /**
     * Busca jogos recentemente lançados (último mês)
     */
    public List<Game> findRecentlyReleasedGames() {
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        return gameRepository.findByReleaseDateAfter(oneMonthAgo);
    }

    /**
     * Busca jogos por múltiplos critérios
     */
    public List<Game> findGamesByCriteria(String developer, String publisher, LocalDate minReleaseDate,
                                          BigDecimal maxPrice) {
        // Implementação customizada pode ser adicionada aqui
        // Por enquanto, use os métodos individuais
        List<Game> games = gameRepository.findAll();

        return games.stream()
                .filter(game -> developer == null || developer.equals(game.getDeveloper()))
                .filter(game -> publisher == null || publisher.equals(game.getPublisher()))
                .filter(game -> minReleaseDate == null ||
                        (game.getReleaseDate() != null && game.getReleaseDate().isAfter(minReleaseDate)))
                .filter(game -> maxPrice == null ||
                        (game.getPriceCurrent() != null && game.getPriceCurrent().compareTo(maxPrice) <= 0))
                .toList();
    }
}