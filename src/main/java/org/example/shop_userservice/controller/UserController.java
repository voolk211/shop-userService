package org.example.shop_userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.shop_userservice.model.dto.CardDto;
import org.example.shop_userservice.model.dto.UserDto;
import org.example.shop_userservice.model.entities.Card;
import org.example.shop_userservice.model.entities.User;
import org.example.shop_userservice.model.mappers.CardMapper;
import org.example.shop_userservice.model.mappers.UserMapper;
import org.example.shop_userservice.model.patchDto.UserPatchDto;
import org.example.shop_userservice.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final UserMapper userMapper;
    private final CardMapper cardMapper;



    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        User user = userService.createUser(userMapper.toEntity(userDto));
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toDto(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@Valid @RequestBody UserDto userDto, @PathVariable Long id) {
        if (userDto.getId() != null && !userDto.getId().equals(id)) {
            throw new IllegalArgumentException("ID mismatch");
        }
        userDto.setId(id);
        User user = userService.updateUser(userMapper.toEntity(userDto));
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @GetMapping("/{id}/cards")
    public ResponseEntity<List<CardDto>> getCardsByUserId(@PathVariable Long id) {
        List<Card> cards = userService.getCardsByUserId(id);
        return ResponseEntity.ok(cardMapper.toDto(cards));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> patchUser(@Valid @RequestBody UserPatchDto userPatchDto, @PathVariable Long id) {
        User user = userService.patchUser(id, userPatchDto.getActive());
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllUsersByNameAndSurname(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            Pageable pageable
    ) {
        Page<User> users = userService.getAllUsersByNameAndSurname(pageable, name, surname);
        return ResponseEntity.ok(userMapper.toDto(users));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}
