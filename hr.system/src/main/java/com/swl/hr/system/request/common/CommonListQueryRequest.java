package com.swl.hr.system.request.common;

import jakarta.validation.constraints.Size;
import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import org.springframework.data.domain.Sort;

import java.io.Serializable;

@Data
public class CommonListQueryRequest implements Serializable {

    @Size(max = 100, message = "sort length must be <= 100 characters")
    private String sort;

    private Sort.Direction sortType;

    @Min(value = 0, message = "page must be >= 0")
    private Integer page;

    @Min(value = 1, message = "size must be >= 1")
    @Max(value = 100, message = "size must be <= 100")
    private Integer size;
}