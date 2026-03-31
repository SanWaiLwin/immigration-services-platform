package com.swl.hr.system.util;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class CommonUtil { 

	public static Pageable buildPageable(Integer page, Integer size, List<String> sortFields, Sort.Direction dir, String defaultSortField) {
		int p = (page != null && page >= 0) ? page : 0;
		int s = (size != null && size > 0) ? size : 10;
		Sort.Direction direction = (dir != null) ? dir : Sort.Direction.ASC;
		List<String> fields = (sortFields != null && !sortFields.isEmpty()) ? sortFields : List.of(defaultSortField);
		List<Sort.Order> orders = fields.stream().map(f -> new Sort.Order(direction, f)).collect(Collectors.toList());
		return PageRequest.of(p, s, Sort.by(orders));
	}
}
