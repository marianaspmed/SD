package pt.tecnico.bicloin.hub.Exceptions;

public class NotEnoughBalanceException extends Exception{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public NotEnoughBalanceException(String message){
        super(message);
    }
}
