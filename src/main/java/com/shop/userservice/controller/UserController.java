package com.shop.userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.shop.userservice.model.dto.CardDto;
import com.shop.userservice.model.dto.UserDto;
import com.shop.userservice.model.entities.Card;
import com.shop.userservice.model.entities.User;
import com.shop.userservice.model.mappers.CardMapper;
import com.shop.userservice.model.mappers.UserMapper;
import com.shop.userservice.model.patchdto.UserPatchDto;
import com.shop.userservice.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final UserMapper userMapper;
    private final CardMapper cardMapper;


    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        User user = userService.createUser(userMapper.toEntity(userDto));
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toDto(user));
    }

    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@Valid @RequestBody UserDto userDto, @PathVariable Long id) {
        if (userDto.getId() != null && !userDto.getId().equals(id)) {
            throw new IllegalArgumentException("ID mismatch");
        }
        userDto.setId(id);
        User user = userService.updateUser(userMapper.toEntity(userDto));
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")
    @GetMapping("/{id}/cards")
    public ResponseEntity<List<CardDto>> getCardsByUserId(@PathVariable Long id) {
        List<Card> cards = userService.getCardsByUserId(id);
        return ResponseEntity.ok(cardMapper.toDto(cards));
    }

    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")
    @PatchMapping("/{id}")
    public ResponseEntity<UserDto> patchUser(@Valid @RequestBody UserPatchDto userPatchDto, @PathVariable Long id) {
        User user = userService.patchUser(id, userPatchDto.getActive());
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllUsersByNameAndSurname(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            Pageable pageable
    ) {
        Page<User> users = userService.getAllUsersByNameAndSurname(pageable, name, surname);
        return ResponseEntity.ok(userMapper.toDto(users));
    }

    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}
