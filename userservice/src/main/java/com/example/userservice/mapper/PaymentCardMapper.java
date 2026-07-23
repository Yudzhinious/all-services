package com.example.userservice.mapper;

import com.example.userservice.dto.PayCardDto;
import com.example.userservice.entity.PayCard;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {

    @Mapping(source = "user.id", target = "userId")
    PayCardDto toDto(PayCard card);

    @Mapping(source = "userId", target = "user.id")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PayCard toEntity(PayCardDto dto);

    List<PayCardDto> toDtoList(List<PayCard> cards);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFromDto(PayCardDto dto, @MappingTarget PayCard entity);

    void updateCardFromDto(PayCardDto cardDto, PayCard card);
}