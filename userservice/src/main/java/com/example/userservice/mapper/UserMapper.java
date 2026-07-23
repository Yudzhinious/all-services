package com.example.userservice.mapper;

import com.example.userservice.dto.UserDto;
import com.example.userservice.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Named("toDto")
    @Mapping(target = "paymentCards", ignore = true)
    UserDto toDto(User user);

    @Named("toDtoWithCards")
    UserDto toDtoWithCards(User user);

    @Mapping(target = "payCards", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserDto dto);

    @IterableMapping(qualifiedByName = "toDto")
    List<UserDto> toDtoList(List<User> users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "payCards", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFromDto(UserDto dto, @MappingTarget User entity);

    void updateUserFromDto(UserDto userDto, User user);
}