package br.com.caiorodri.agendamentoveterinario.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException e) {
        
    	return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        
    	return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    
    }
	
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<String> handleSecurityException(SecurityException e) {
        
    	return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    
    }
	
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {

    	return new ResponseEntity<>("Erro interno no servidor: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    	
    }
}
