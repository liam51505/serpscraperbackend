package com.liamcashman.serpscrapper.controller;

import com.liamcashman.serpscrapper.model.Domain;
import com.liamcashman.serpscrapper.repository.DomainRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import com.liamcashman.serpscrapper.dto.ScrapeRequest;
import com.liamcashman.serpscrapper.model.Result;
import com.liamcashman.serpscrapper.repository.ResultRepository;
import com.liamcashman.serpscrapper.repository.KeywordRepository;
import com.liamcashman.serpscrapper.service.ScraperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.liamcashman.serpscrapper.dto.ClientDto;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/results")
public class ResultController {

    private static final Logger logger = LoggerFactory.getLogger(ResultController.class);
    private final ResultRepository resultRepository;
    private final ScraperService scraperService;
    private final KeywordRepository keywordRepository;
    private final DomainRepository domainRepository;

    @Autowired
    public ResultController(
            ResultRepository resultRepository,
            ScraperService scraperService,
            KeywordRepository keywordRepository,
            DomainRepository domainRepository
    ) {
        this.resultRepository = resultRepository;
        this.scraperService = scraperService;
        this.keywordRepository = keywordRepository;
        this.domainRepository = domainRepository;
    }


    // GET all results
    @GetMapping
    public List<Result> getAllResults() {
        return resultRepository.findAll();
    }

    // POST new result (for manual testing)
    @PostMapping
    public Result saveResult(@RequestBody Result result) {
        return resultRepository.save(result);
    }

    // Scrape single
    @GetMapping("/scrape")
    public ResponseEntity<Result> scrapeAndSave(
            @RequestParam String keyword,
            @RequestParam String domain,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String businessName
    ) {
        Result result = scraperService.scrapeAndSave(keyword, domain, location, businessName);
        if (result == null) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/scrapeBatch")
    public ResponseEntity<List<Result>> scrapeBatch(@RequestBody List<ScrapeRequest> requests) {
        List<Result> results = new ArrayList<>();

        for (ScrapeRequest req : requests) {
            // Log what was received
            System.out.printf(
                    "Batch Scrape:: Controller received: keyword='%s', domain='%s', location='%s', businessName='%s'%n",
                    req.getKeyword(),
                    req.getDomain(),
                    req.getLocation(),
                    req.getBusinessName()
            );

            Result result = scraperService.scrapeAndSave(
                    req.getKeyword(),
                    req.getDomain(),
                    req.getLocation(),
                    req.getBusinessName()
            );

            if (result != null) {
                results.add(result);
            }
        }

        return ResponseEntity.ok(results);
    }

    // New: Get results by keyword
    @GetMapping("/byKeyword")
    public ResponseEntity<List<Result>> getResultsByKeyword(@RequestParam String keyword) {
        var optionalKeyword = keywordRepository.findByKeyword(keyword);
        if (optionalKeyword.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var results = resultRepository.findByKeyword(optionalKeyword.get());
        return ResponseEntity.ok(results);
    }
    @GetMapping("/clients")
    public List<ClientDto> getClients() {
        return scraperService.getClientsWithKeywordsAndResults();
    }

    // Health check
    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @PutMapping("/clients/{id}")
    public ResponseEntity<?> updateClient(@PathVariable Long id, @RequestBody Map<String, String> updates) {
        Optional<Domain> opt = domainRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Domain domain = opt.get();
        if (updates.containsKey("url")) {
            domain.setUrl(updates.get("url"));
        }
        if (updates.containsKey("businessName")) {
            domain.setBusinessName(updates.get("businessName"));
        }

        domainRepository.save(domain);
        return ResponseEntity.ok().build();
    }
}
