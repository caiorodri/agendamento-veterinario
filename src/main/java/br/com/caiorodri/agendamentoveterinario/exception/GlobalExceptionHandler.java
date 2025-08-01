package br.com.caiorodri.agendamentoveterinario.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.persistence.EntityNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException ex) {
        
    	return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        
    	return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    
    }
	
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {

    	return new ResponseEntity<>("Erro interno no servidor: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    	
    }
    
}
