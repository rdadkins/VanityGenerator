package co.bitsquared.vanitygenerator.core.exceptions;

public class P2SHNotInitializedException extends RuntimeException {

    public P2SHNotInitializedException(String networkName) {
        super(String.format("P2SH Header not initialized; %s does not define a P2SH Header.", networkName));
    }

}
