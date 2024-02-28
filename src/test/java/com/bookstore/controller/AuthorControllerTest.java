package com.bookstore.controller;

import com.bookstore.dto.author.AuthorDto;
import com.bookstore.dto.author.AuthorRequestDto;
import com.bookstore.dto.author.AuthorUpdateRequestDto;
import com.bookstore.exception.ConflictException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.service.AuthorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser
@WebMvcTest(AuthorController.class)
public class AuthorControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AuthorService service;

    @Autowired
    private ObjectMapper om;

    @Test
    void shouldReturnAuthorsList_whenGetAll() throws Exception {
        AuthorDto author = new AuthorDto(1L, "Alex");
        Page<AuthorDto> allAuthors = new PageImpl<>(List.of(author));

        given(service.findAll(any(PageRequest.class))).willReturn(allAuthors);

        mvc.perform(get("/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", hasSize(1)))
                .andExpect(jsonPath("content[0].name", is(author.getName())));
    }

    @Test
    void shouldReturnAuthorDto_whenGetById() throws Exception {
        AuthorDto author = new AuthorDto(1L, "Alex");
        given(service.findById(any(Long.class))).willReturn(author);

        mvc.perform(get("/v1/authors/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is(author.getName())));
    }

    @Test
    void shouldReturnNotFound_whenGetByNonExistentId() throws Exception {
        when(service.findById(2L)).thenThrow(new ResourceNotFoundException("No Author found by id"));
        mvc.perform(get("/v1/authors/2").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("error", is("No Author found by id")));
    }

    @Test
    public void shouldCreateNewAuthor() throws Exception {
        AuthorRequestDto requestBody = new AuthorRequestDto("John Doe");

        when(service.create(any(AuthorRequestDto.class)))
                .thenReturn(new AuthorDto(1L, "John Doe"));

        mvc.perform(post("/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt())
                        .content(om.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(service, times(1)).create(any(AuthorRequestDto.class));
    }

    @Test
    public void shouldNotCreateNewAuthor_entityExists() throws Exception {
        AuthorRequestDto requestBody = new AuthorRequestDto("John Doe");

        when(service.create(any(AuthorRequestDto.class)))
                .thenThrow(new ConflictException("Author with name [John Doe] already exists."));

        mvc.perform(post("/v1/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt())
                        .content(om.writeValueAsString(requestBody)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Author with name [John Doe] already exists."));

        verify(service, times(1)).create(any(AuthorRequestDto.class));
    }

    @Test
    public void shouldReturn_BadRequest_IdMissmatch() throws Exception {
        var requestBody = new AuthorUpdateRequestDto(2L, "John Doe");

        mvc.perform(put("/v1/authors/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt())
                        .content(om.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Id in path should be equal to id in request body!"));

        verify(service, times(0)).update(any(Long.class), any(AuthorUpdateRequestDto.class));
    }

    @Test
    public void shouldUpdateAuthor_isOk() throws Exception {
        var requestBody = new AuthorUpdateRequestDto(2L, "John Doe Updated");

        when(service.update(any(Long.class), any(AuthorUpdateRequestDto.class)))
                .thenReturn(new AuthorDto(2L, "John Doe Updated"));

        mvc.perform(put("/v1/authors/{id}", requestBody.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt())
                        .content(om.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe Updated"));

        verify(service, times(1)).update(any(Long.class), any(AuthorUpdateRequestDto.class));
    }

    @Test
    public void shouldNotUpdateAuthor_NameAlreadyExist_Conflict() throws Exception {
        var requestBody = new AuthorUpdateRequestDto(2L, "John Doe Updated");

        var errorMessage = "Author with name [John Doe Updated] already exists!";
        when(service.update(any(Long.class), any(AuthorUpdateRequestDto.class)))
                .thenThrow(new ConflictException(errorMessage));

        mvc.perform(put("/v1/authors/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt())
                        .content(om.writeValueAsString(requestBody)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(errorMessage));

        verify(service, times(1)).update(any(Long.class), any(AuthorUpdateRequestDto.class));
    }

    @Test
    public void shouldNotDelete_AuthorNotFound() throws Exception {
        var errorMessage = "Author not found by id: [2]";
        doThrow(new ResourceNotFoundException(errorMessage)).when(service).delete(any(Long.class));

        mvc.perform(delete("/v1/authors/2").with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(errorMessage));

        verify(service, times(1)).delete(any(Long.class));
    }

    @Test
    public void shouldNotDelete_AuthorHasBooks_Conflict() throws Exception {
        var errorMessage = "Can't delete author: [2]. Books not empty!";
        doThrow(new ConflictException(errorMessage)).when(service).delete(any(Long.class));

        mvc.perform(delete("/v1/authors/2").with(jwt()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(errorMessage));

        verify(service, times(1)).delete(any(Long.class));
    }
}
