package exceptions;

public class PersonException extends RuntimeException{

    public PersonException(Exception e){
        super(e);
    }

    public PersonException(String message){
        super(message);
    }
}
