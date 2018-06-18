package nl.riebie.mcclans.persistence.exceptions;

/**
 * Created by K.Volkers on 5-4-2016.
 */
public class GetDataVersionFailedException extends DataException {

    private static final long serialVersionUID = 1L;

    public GetDataVersionFailedException(String reason) {
        super("Failed to retrieve the data version! Reason: " + reason);
    }
}
