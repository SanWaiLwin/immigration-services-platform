package com.swl.hr.system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.validation.ConstraintViolationException;
import com.swl.hr.system.response.ApiResponse;
import com.swl.hr.system.util.CommonConstant;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.dao.DataAccessException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
		StringBuilder errorMessage = new StringBuilder("Validation failed: ");
		ex.getBindingResult().getFieldErrors().forEach(error -> errorMessage.append(error.getField()).append(" ")
				.append(error.getDefaultMessage()).append("; "));
		log.warn("MethodArgumentNotValidException: {}", errorMessage);
		return new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, errorMessage.toString());
	}

	@ExceptionHandler(BindException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiResponse<Void> handleBindException(BindException ex) {
		StringBuilder errorMessage = new StringBuilder("Validation failed: ");
		ex.getBindingResult().getFieldErrors().forEach(error -> errorMessage.append(error.getField()).append(" ")
				.append(error.getDefaultMessage()).append("; "));
		log.warn("BindException: {}", errorMessage);
		return new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, errorMessage.toString());
	}

	@ExceptionHandler(ConstraintViolationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiResponse<Void> handleConstraintViolationException(ConstraintViolationException ex) {
		StringBuilder errorMessage = new StringBuilder("Validation failed: ");
		ex.getConstraintViolations().forEach(violation -> errorMessage.append(violation.getPropertyPath()).append(" ")
				.append(violation.getMessage()).append("; "));
		log.warn("ConstraintViolationException: {}", errorMessage);
		return new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, errorMessage.toString());
	}

	@ExceptionHandler(AlreadyExitException.class)
	public ResponseEntity<ApiResponse<Void>> handleInvalidInputException(AlreadyExitException ex) {
		log.warn("AlreadyExitException: {}", ex.getMessage());
		ApiResponse<Void> response = new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(ResponseInfoException.class)
	public ResponseEntity<ApiResponse<Void>> handleResponseInfoException(ResponseInfoException ex) {
		log.warn("ResponseInfoException: {}", ex.getMessage());
		ApiResponse<Void> response = new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}
 
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
		log.warn("IllegalArgumentException: {}", e.getMessage());
		ApiResponse<Void> response = new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, e.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException e) {
		log.warn("IllegalStateException: {}", e.getMessage());
		ApiResponse<Void> response = new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, e.getMessage());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
	}

	// Added: malformed JSON body
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
		log.warn("HttpMessageNotReadableException: {}", ex.getMessage());
		ApiResponse<Void> response = new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, "Malformed request body: " + ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	// Added: type mismatch for request parameters/path variables
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
		String msg = String.format("Parameter '%s' value '%s' is of incorrect type. Expected: %s",
				ex.getName(), ex.getValue(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Unknown");
		log.warn("MethodArgumentTypeMismatchException: {}", msg);
		ApiResponse<Void> response = new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, msg);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	// Added: missing required request parameter
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException ex) {
		String msg = String.format("Missing required parameter '%s'", ex.getParameterName());
		log.warn("MissingServletRequestParameterException: {}", msg);
		ApiResponse<Void> response = new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, msg);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
	}

	// Added: HTTP method not supported
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
		log.warn("HttpRequestMethodNotSupportedException: {}", ex.getMessage());
		ApiResponse<Void> response = new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, ex.getMessage());
		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
	}

	// Added: database-related exceptions
	@ExceptionHandler(DataAccessException.class)
	public ResponseEntity<ApiResponse<Void>> handleDataAccess(DataAccessException ex) {
		log.error("DataAccessException: {}", ex.getMessage(), ex);
		ApiResponse<Void> response = new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, "Database error: " + ex.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

	// Added: generic fallback to avoid leaking stack traces
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleUnhandledException(Exception ex) {
		log.error("Unhandled exception: {}", ex.getMessage(), ex);
		ApiResponse<Void> response = new ApiResponse<>(CommonConstant.MSG_PREFIX_FAILED, "Unexpected error: " + ex.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
}
