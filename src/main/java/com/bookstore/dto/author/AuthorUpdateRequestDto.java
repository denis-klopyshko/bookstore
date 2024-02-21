package com.bookstore.dto.author;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class AuthorUpdateRequestDto extends AuthorRequestDto {

    @NotNull
    private Long id;
}
