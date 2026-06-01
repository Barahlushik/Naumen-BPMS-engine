package ru.naumen.bpms.controller.mapper;

import org.mapstruct.Mapper;
import ru.naumen.bpms.controller.dto.UserResponseDto;
import ru.naumen.bpms.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto toDto(User user);

    List<UserResponseDto> toDtoList(List<User> users);
}

