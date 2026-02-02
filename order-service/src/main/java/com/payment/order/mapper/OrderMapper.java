package com.payment.order.mapper;

import com.payment.order.dto.OrderRequest;
import com.payment.order.entity.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    Order toEntity(OrderRequest orderRequest);

    OrderRequest toDto(Order order);
}
