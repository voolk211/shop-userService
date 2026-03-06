package org.example.shopuserservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.shopuserservice.model.dto.CardDto;
import org.example.shopuserservice.model.entities.Card;
import org.example.shopuserservice.model.mappers.CardMapper;
import org.example.shopuserservice.model.patchdto.CardPatchDto;
import org.example.shopuserservice.service.CardService;
import org.example.shopuserservice.service.UserService;
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

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final UserService userService;
    private final CardService cardService;

    private final CardMapper cardMapper;

    @PreAuthorize("hasRole('ADMIN') or @cardSecurityServiceImpl.isOwner(#id, authentication.principal)")
    @GetMapping("/{id}")
    public ResponseEntity<CardDto> getCardById(@PathVariable Long id) {
        Card card = cardService.getCardById(id);
        return ResponseEntity.ok(cardMapper.toDto(card));
    }

    @PreAuthorize("hasRole('ADMIN') or #cardDto.userId == authentication.principal")
    @PostMapping
    public ResponseEntity<CardDto> createCard(@Valid @RequestBody CardDto cardDto) {
        Card card = cardService.createCard(cardMapper.toEntity(cardDto));
        return ResponseEntity.status(HttpStatus.CREATED).body(cardMapper.toDto(card));
    }

    @PreAuthorize("hasRole('ADMIN') or @cardSecurityServiceImpl.isOwner(#id, authentication.principal)")
    @PutMapping("/{id}")
    public ResponseEntity<CardDto> updateCard(@Valid @RequestBody CardDto cardDto, @PathVariable Long id) {
        if (cardDto.getId() != null && !cardDto.getId().equals(id)) {
            throw new IllegalArgumentException("ID mismatch");
        }
        cardDto.setId(id);
        Card card = cardService.updateCard(cardMapper.toEntity(cardDto));
        return ResponseEntity.ok(cardMapper.toDto(card));
    }

    @PreAuthorize("hasRole('ADMIN') or @cardSecurityServiceImpl.isOwner(#id, authentication.principal)")
    @PatchMapping("/{id}")
    public ResponseEntity<CardDto> patchCard(@Valid @RequestBody CardPatchDto cardPatchDto, @PathVariable Long id) {
        Card card = cardService.patchCard(id, cardPatchDto.getActive());
        return ResponseEntity.ok(cardMapper.toDto(card));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<CardDto>> getAllCardsByUserNameAndSurname(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            Pageable pageable
    ) {
        Page<Card> cards = userService.getAllCardsByUserNameAndSurname(pageable, name, surname);
        return ResponseEntity.ok(cardMapper.toDto(cards));
    }

    @PreAuthorize("hasRole('ADMIN') or @cardSecurityServiceImpl.isOwner(#id, authentication.principal)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCardById(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}
