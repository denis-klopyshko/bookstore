package com.bookstore.dto.author;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AuthorUpdateRequestDto extends AuthorRequestDto {

    @NotNull
    private Long id;

    public AuthorUpdateRequestDto(Long id, @NotBlank String name) {
        super(name);
        this.id = id;
    }
}
