package com.manifest.concurrency.exception;

import java.io.Serial;

/**
 * @author Erdem Yusuf
 */
public class SimulationExecutionException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -6191018889083755865L;

    public SimulationExecutionException(String message) {
        super(message);
    }

    public SimulationExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

}
