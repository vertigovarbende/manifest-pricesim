package com.manifest.concurrency.api;


import com.manifest.concurrency.api.response.ConErrorResponse;
import com.manifest.concurrency.api.response.ConResponse;
import com.manifest.concurrency.model.CoinSnapshot;
import com.manifest.concurrency.metrics.stats.SimulationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Simulation", description = "Eşzamanlı kripto fiyat simülasyonu için endpoint'ler")
public interface SimulationControllerDocs {

    @Operation(
            summary = "Simülasyonu çalıştırır (safe vs unsafe)",
            description = "Verilen updates, workers ve seed parametreleriyle aynı immutable görev listesini " +
                    "unsafe (kilitsiz) ve safe (ReentrantLock/Atomic tabanlı) olarak işler ve karşılaştırmalı " +
                    "sonucu döner. Aynı anda başka bir simülasyon çalışıyorsa 409 döner."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Simülasyon başarıyla tamamlandı",
                    content = @Content(schema = @Schema(implementation = SimulationResult.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Geçersiz parametre (validation hatası)",
                    content = @Content(schema = @Schema(implementation = ConErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Başka bir simülasyon zaten çalışıyor",
                    content = @Content(schema = @Schema(implementation = ConErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Simülasyon çalıştırılırken hata oluştu veya zaman aşımına uğradı",
                    content = @Content(schema = @Schema(implementation = ConErrorResponse.class))
            )
    })
    ConResponse<SimulationResult> simulate(
            @Parameter(description = "İşlenecek fiyat güncelleme sayısı (1 - 100.000)", example = "10000")
            @RequestParam
            @Min(value = 1, message = "Values min 1 olacak")
            @Max(value = 100_000, message = "Values max 1 olacak") int updates,

            @Parameter(description = "Kullanılacak worker (thread) sayısı (1 - 16)", example = "4")
            @RequestParam
            @Min(value = 1, message = "values min 1")
            @Max(value = 16, message = "values max 1") int workers,

            @Parameter(description = "Tekrarlanabilir görev üretimi için seed değeri (1 - 42, varsayılan 42)", example = "42")
            @RequestParam(required = false, defaultValue = "42")
            @Min(value = 1, message = "values min 1")
            @Max(value = 42, message = "values max 42") Long seed
    );


    @Operation(
            summary = "Son simülasyonun coin durumlarını getirir",
            description = "En son çalıştırılan simülasyonun safe (güvenli) coin snapshot listesini döner. " +
                    "Henüz simülasyon çalıştırılmadıysa 404 döner."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Coin listesi başarıyla getirildi",
                    content = @Content(schema = @Schema(implementation = CoinSnapshot.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Henüz çalıştırılmış bir simülasyon bulunamadı",
                    content = @Content(schema = @Schema(implementation = ConErrorResponse.class))
            )
    })
    ConResponse<List<CoinSnapshot>> coins();

    @Operation(
            summary = "Son simülasyon sonucunu getirir",
            description = "En son çalıştırılan /simulate isteğine ait tüm sonuç detaylarını (seed, süre, " +
                    "throughput, invariant sonucu, coin karşılaştırmaları vb.) döner. " +
                    "Henüz simülasyon çalıştırılmadıysa 404 döner."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Son simülasyon sonucu başarıyla getirildi",
                    content = @Content(schema = @Schema(implementation = SimulationResult.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Henüz çalıştırılmış bir simülasyon bulunamadı",
                    content = @Content(schema = @Schema(implementation = ConErrorResponse.class))
            )
    })
    ConResponse<SimulationResult> stats();
}



