package pt.tecnico.bicloin.hub.Exceptions;

public class InvalidDepositValueException extends Exception{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public InvalidDepositValueException(String message){
        super(message);
    }

}
