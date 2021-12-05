package exceptions;

import java.io.IOException;

public class UnauthorizedException extends Throwable {
    public UnauthorizedException(IOException e) {
        super(e);
    }
    public UnauthorizedException(String message){
        super(message);
    }
}
