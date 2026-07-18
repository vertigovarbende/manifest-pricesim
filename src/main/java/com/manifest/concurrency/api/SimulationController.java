package com.manifest.concurrency.api;

import com.manifest.concurrency.api.response.ConResponse;
import com.manifest.concurrency.metrics.stats.SimulationResult;
import com.manifest.concurrency.model.CoinSnapshot;
import com.manifest.concurrency.service.SimulationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Batuhan
 */

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class SimulationController implements SimulationControllerDocs {

    private final SimulationService service;

    @PostMapping("/simulate")
    public ConResponse<SimulationResult> simulate(int updates, int workers, Long seed, String threadMode) {
        return ConResponse.successOf(service.simulate(updates, workers, seed, threadMode));
    }

    @GetMapping("/coins")
    public ConResponse<List<CoinSnapshot>> coins() {
        return ConResponse.successOf(service.coins());
    }

    @GetMapping("/stats")
    public ConResponse<SimulationResult> stats() {
        return ConResponse.successOf(service.stats());
    }

}

