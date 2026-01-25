package org.example.shop_userservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.shop_userservice.model.dto.CardDto;
import org.example.shop_userservice.model.dto.UserDto;
import org.example.shop_userservice.model.entities.Card;
import org.example.shop_userservice.model.entities.User;
import org.example.shop_userservice.model.mappers.CardMapper;
import org.example.shop_userservice.model.mappers.UserMapper;
import org.example.shop_userservice.model.patchDto.CardPatchDto;
import org.example.shop_userservice.model.patchDto.UserPatchDto;
import org.example.shop_userservice.service.CardService;
import org.example.shop_userservice.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {
    private final UserService userService;
    private final CardService cardService;

    private final CardMapper cardMapper;

    @GetMapping("/{id}")
    public ResponseEntity<CardDto> getCardById(@PathVariable Long id) {
        Card card = cardService.getCardById(id);
        return ResponseEntity.ok(cardMapper.toDto(card));
    }

    @PostMapping
    public ResponseEntity<CardDto> createCard(@Valid @RequestBody CardDto cardDto) {
        Card card = cardService.createCard(cardMapper.toEntity(cardDto));
        return ResponseEntity.status(HttpStatus.CREATED).body(cardMapper.toDto(card));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardDto> updateCard(@Valid @RequestBody CardDto cardDto, @PathVariable Long id) {
        if (cardDto.getId() != null && !cardDto.getId().equals(id)) {
            throw new IllegalArgumentException("ID mismatch");
        }
        cardDto.setId(id);
        Card card = cardService.updateCard(cardMapper.toEntity(cardDto));
        return ResponseEntity.ok(cardMapper.toDto(card));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CardDto> patchCard(@Valid @RequestBody CardPatchDto cardPatchDto, @PathVariable Long id) {
        Card card = cardService.patchCard(id, cardPatchDto.getActive());
        return ResponseEntity.ok(cardMapper.toDto(card));
    }

    @GetMapping
    public ResponseEntity<Page<CardDto>> getAllCardsByUserNameAndSurname(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            Pageable pageable
    ) {
        Page<Card> cards = userService.getAllCardsByUserNameAndSurname(pageable, name, surname);
        return ResponseEntity.ok(cardMapper.toDto(cards));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCardById(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

}
