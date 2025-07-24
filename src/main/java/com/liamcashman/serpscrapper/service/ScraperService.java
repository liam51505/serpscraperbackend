package com.liamcashman.serpscrapper.service;

import com.liamcashman.serpscrapper.dto.ClientDto;
import com.liamcashman.serpscrapper.dto.KeywordDto;
import com.liamcashman.serpscrapper.dto.ResultDto;
import com.liamcashman.serpscrapper.model.Keyword;
import com.liamcashman.serpscrapper.model.Domain;
import com.liamcashman.serpscrapper.model.Result;
import com.liamcashman.serpscrapper.repository.KeywordRepository;
import com.liamcashman.serpscrapper.repository.DomainRepository;
import com.liamcashman.serpscrapper.repository.ResultRepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScraperService {

    @Value("${serpapi.key}")
    private String serpApiKey;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .callTimeout(Duration.ofSeconds(60))
            .connectTimeout(Duration.ofSeconds(30))
            .readTimeout(Duration.ofSeconds(30))
            .writeTimeout(Duration.ofSeconds(30))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger logger = LoggerFactory.getLogger(ScraperService.class);

    private final KeywordRepository keywordRepository;
    private final DomainRepository domainRepository;
    private final ResultRepository resultRepository;

    private String normalizeName(String input) {
        if (input == null) return "";
        return input
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");
    }

    public ScraperService(
            KeywordRepository keywordRepository,
            DomainRepository domainRepository,
            ResultRepository resultRepository) {
        this.keywordRepository = keywordRepository;
        this.domainRepository = domainRepository;
        this.resultRepository = resultRepository;
    }

    // -----------------------------------------
    // Main scrape method
    // -----------------------------------------
    @Transactional
    public Result scrapeAndSave(String keyword, String domain, String location, String businessName) {
        logger.info("scrapeAndSave called with keyword='{}', domain='{}', location='{}', businessName='{}'",
                keyword, domain, location, businessName);

        logger.info("Using raw location='{}'", location);

        Integer organicPosition = scrapeOrganicPosition(keyword, domain, location);
        Integer mapsPosition = scrapeMapsPosition(keyword, businessName, location);

        logger.info("Scraped positions - Organic: {}, Maps: {}", organicPosition, mapsPosition);

        // Lookup or create keyword/domain
        Keyword keywordEntity = keywordRepository.findByKeyword(keyword)
                .orElseGet(() -> keywordRepository.save(new Keyword(keyword)));

        Domain domainEntity = domainRepository.findByUrl(domain)
                .orElseGet(() -> {
                    Domain newDomain = new Domain();
                    newDomain.setUrl(domain);
                    newDomain.setBusinessName(businessName);
                    return domainRepository.save(newDomain);
                });

        // Create and save result
        Result newResult = new Result();
        newResult.setKeyword(keywordEntity);
        newResult.setDomain(domainEntity);
        newResult.setOrganicPosition(organicPosition);
        newResult.setMapsPosition(mapsPosition);
        newResult.setTimestamp(LocalDateTime.now());

        logger.info("Saving result for keyword='{}', domain='{}'", keyword, domain);
        return resultRepository.save(newResult);
    }

    // -----------------------------------------
    // Fallback for scheduled scraping (no business name)
    // -----------------------------------------
    @Transactional
    public Result scrapeAndSave(String keyword, String domain, String location) {
        return scrapeAndSave(keyword, domain, location, null);
    }

    @Transactional
    public Result scrapeAndSave(String keyword, String domain) {
        return scrapeAndSave(keyword, domain, null, null);
    }

    // -----------------------------------------
    // Organic scraping
    // -----------------------------------------
    private Integer scrapeOrganicPosition(String keywordString, String domainString, String location) {
        final int resultsPerPage = 10;
        final int maxPages = 1;
        Integer organicPosition = null;

        try {
            logger.info("Starting organic scrape for '{}'", keywordString);
            logger.info("Using location='{}'", location);

            for (int page = 0; page < maxPages && organicPosition == null; page++) {
                int start = page * resultsPerPage;

                StringBuilder apiUrl = new StringBuilder("https://serpapi.com/search.json?q=")
                        .append(URLEncoder.encode(keywordString, StandardCharsets.UTF_8))
                        .append("&engine=google")
                        .append("&api_key=").append(serpApiKey)
                        .append("&start=").append(start);

                if (location != null && !location.isBlank()) {
                    apiUrl.append("&location=").append(URLEncoder.encode(location, StandardCharsets.UTF_8));
                }

                logger.debug("Calling SerpAPI Search: {}", apiUrl);

                Request request = new Request.Builder().url(apiUrl.toString()).build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        logger.error("SerpAPI Search HTTP error {} on page {}", response.code(), page + 1);
                        continue;
                    }

                    String json = response.body().string();
                    JsonNode root = objectMapper.readTree(json);
                    JsonNode organicResults = root.path("organic_results");

                    int counter = 1;
                    for (JsonNode resultNode : organicResults) {
                        String link = resultNode.path("link").asText("").toLowerCase();
                        String domainLower = domainString
                                .replaceAll("^https?://", "")
                                .replaceAll("^www\\.", "")
                                .toLowerCase();

                        try {
                            URI linkUri = new URI(link);
                            String linkHost = linkUri.getHost();
                            if (linkHost != null) {
                                linkHost = linkHost.replaceFirst("^www\\.", "").toLowerCase();
                                if (linkHost.equals(domainLower)) {
                                    organicPosition = start + counter;
                                    logger.info("Matched ORGANIC rank {} on page {}", organicPosition, page + 1);
                                    break;
                                }
                            }
                        } catch (Exception e) {
                            logger.warn("Error parsing link URI: {}", link, e);
                        }
                        counter++;
                    }

                } catch (Exception ex) {
                    logger.error("Error parsing SerpAPI Search response on page {}: {}", page + 1, ex.getMessage(), ex);
                }
            }
        } catch (Exception e) {
            logger.error("Error in scrapeOrganicPosition: {}", e.getMessage(), e);
        }

        return organicPosition;
    }

    // -----------------------------------------
    // Maps scraping (using businessName)
    // -----------------------------------------
    private Integer scrapeMapsPosition(String keywordString, String businessName, String location) {
        final int resultsPerPage = 20;
        final int maxPages = 3;
        Integer mapsPosition = null;

        try {
            logger.info("Starting Maps scrape for keyword='{}', businessName='{}', location='{}'",
                    keywordString, businessName, location);

            for (int page = 0; page < maxPages && mapsPosition == null; page++) {
                int start = page * resultsPerPage;

                StringBuilder apiUrl = new StringBuilder("https://serpapi.com/search.json?engine=google_local&q=")
                        .append(URLEncoder.encode(keywordString, StandardCharsets.UTF_8))
                        .append("&api_key=").append(serpApiKey);

                if (location != null && !location.isBlank()) {
                    apiUrl.append("&location=").append(URLEncoder.encode(location, StandardCharsets.UTF_8));
                }

                if (start > 0) {
                    apiUrl.append("&start=").append(start);
                }

                logger.debug("Calling SerpAPI Maps page {}: {}", page + 1, apiUrl);

                Request request = new Request.Builder().url(apiUrl.toString()).build();
                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        logger.error("SerpAPI Maps HTTP error {} on page {}", response.code(), page + 1);
                        continue;
                    }

                    String json = response.body().string();
                    JsonNode root = objectMapper.readTree(json);
                    JsonNode placesResults = root.path("local_results");

                    if (!placesResults.isArray() || placesResults.isEmpty()) {
                        logger.info("No local_results found on page {}", page + 1);
                        break;
                    }

                    int counter = 1;
                    for (JsonNode node : placesResults) {
                        String name = node.path("title").asText("").toLowerCase();

                        if (businessName != null && !businessName.isBlank()
                                && normalizeName(name).contains(normalizeName(businessName))) {
                            mapsPosition = start + counter;
                            logger.info("Found MAPS rank {} (page {}) for business '{}'", mapsPosition, page + 1, businessName);
                            break;
                        }
                        counter++;
                    }

                } catch (Exception ex) {
                    logger.error("Error parsing SerpAPI Maps response on page {}: {}", page + 1, ex.getMessage(), ex);
                }
            }

        } catch (Exception e) {
            logger.error("Error in scrapeMapsPosition: {}", e.getMessage(), e);
        }

        return mapsPosition;
    }

    // -----------------------------------------
    // Scheduled batch scraping
    // -----------------------------------------
    public void scrapeAllKeywordsForAllDomains() {
        logger.info("Scheduled scrape started!");

        List<Keyword> allKeywords = keywordRepository.findAll();
        List<Domain> allDomains = domainRepository.findAll();

        for (Keyword keyword : allKeywords) {
            String location = keyword.getLocation();
            for (Domain domain : allDomains) {
                Result result = scrapeAndSave(keyword.getKeyword(), domain.getUrl(), location);
                if (result != null) {
                    logger.info("Saved result: {} for keyword '{}' and domain '{}'",
                            result.getId(), keyword.getKeyword(), domain.getUrl());
                }
            }
        }

        logger.info("Scheduled scrape completed!");
    }

    // -----------------------------------------
    // Expose for API
    // -----------------------------------------
    @Transactional(readOnly = true)
    public List<ClientDto> getClientsWithKeywordsAndResults() {
        List<Domain> domains = domainRepository.findAll();

        return domains.stream().map(domain -> {
            List<Keyword> keywords = keywordRepository.findByDomain(domain);
            List<KeywordDto> keywordDtos = keywords.stream().map(keyword -> {
                Result latest = resultRepository.findTopByKeywordOrderByTimestampDesc(keyword).orElse(null);
                return new KeywordDto(keyword.getId(), keyword.getKeyword(), keyword.getLocation(),
                        latest != null ? new ResultDto(latest.getOrganicPosition(), latest.getMapsPosition(), latest.getTimestamp()) : null);
            }).collect(Collectors.toList());

            return new ClientDto(domain.getId(), domain.getUrl(), keywordDtos);
        }).collect(Collectors.toList());
    }
}
