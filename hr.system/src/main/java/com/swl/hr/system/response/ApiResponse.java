package com.swl.hr.system.response;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@JsonInclude(Include.NON_NULL)
public class ApiResponse<T> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7713056373920733302L;
	private String status;
	private String message;
	private T data;
	private Map<String, String> errors;

	public ApiResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

}