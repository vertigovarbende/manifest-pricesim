package com.manifest.concurrency.deadlock;

import com.manifest.concurrency.api.response.ConResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DeadlockController {

    private final DeadlockService deadlockService;

    @PostMapping("/deadlock/demo")
    public ConResponse<DeadlockResult> runDeadlockDemo(
            @RequestParam(defaultValue = "UNSAFE") DeadlockMode mode
    ) {
        return ConResponse.successOf(deadlockService.run(mode));
    }
}
