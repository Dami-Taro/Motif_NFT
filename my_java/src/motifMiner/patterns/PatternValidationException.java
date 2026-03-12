package src.motifMiner.patterns;

public class PatternValidationException extends RuntimeException {

    public PatternValidationException(String message) {
        super(message);
    }

    public PatternValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
