package com.manifest.concurrency.exception;

import java.io.Serial;

/**
 * @author Erdem Yusuf
 */
public class SimulationAlreadyRunningException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -6191018889083755865L;

    public SimulationAlreadyRunningException(String message) {
        super(message);
    }

    public SimulationAlreadyRunningException(String message, Throwable cause) {
        super(message, cause);
    }

}
