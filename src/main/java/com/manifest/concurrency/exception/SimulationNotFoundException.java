package com.manifest.concurrency.exception;

import java.io.Serial;

/**
 * @author Batuhan
 */
public class SimulationNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -6191018889083755865L;

    public SimulationNotFoundException(String message) {
        super(message);
    }

    public SimulationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
