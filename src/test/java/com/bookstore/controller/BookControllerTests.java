package com.bookstore.controller;

import com.bookstore.controller.filters.BookFilter;
import com.bookstore.dto.author.AuthorDto;
import com.bookstore.dto.book.BookDto;
import com.bookstore.dto.book.BookRequestDto;
import com.bookstore.dto.publisher.PublisherDto;
import com.bookstore.dto.rating.BookRatingDto;
import com.bookstore.exception.ConflictException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.service.BookService;
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
@WebMvcTest(BookController.class)
public class BookControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookService service;

    @Autowired
    private ObjectMapper om;

    @Test
    void shouldReturnBooksList_whenGetAll() throws Exception {
        var book = BookDto.builder()
                .isbn("12794867XHS4")
                .title("Book about sports")
                .rating(7.85)
                .build();

        Page<BookDto> allBooks = new PageImpl<>(List.of(book));

        given(service.findAll(any(BookFilter.class), any(PageRequest.class))).willReturn(allBooks);

        mvc.perform(get("/v1/books").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", hasSize(1)))
                .andExpect(jsonPath("content[0].isbn", is(book.getIsbn())))
                .andExpect(jsonPath("content[0].title", is(book.getTitle())))
                .andExpect(jsonPath("content[0].rating", is(7.85)));
    }

    @Test
    void shouldReturnBookDto_whenGetById() throws Exception {
        BookDto book = BookDto.builder().isbn("12794867XHS4").title("Book about sports").build();
        given(service.findByIsbn(any(String.class))).willReturn(book);

        mvc.perform(get("/v1/books/12794867XHS4").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("title", is(book.getTitle())));
    }

    @Test
    void shouldReturnNotFound_whenGetByNonExistentId() throws Exception {
        when(service.findByIsbn(any(String.class))).thenThrow(new ResourceNotFoundException("No Book found by id"));
        mvc.perform(get("/v1/books/83839XHS1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("error", is("No Book found by id")));
    }

    @Test
    void shouldReturnBookRatings_isOk() throws Exception {
        Page<BookRatingDto> bookRatings = new PageImpl<>(List.of(
                BookRatingDto.builder().userId(1L).score(1).build(),
                BookRatingDto.builder().userId(2L).score(6).build()
        ));

        given(service.findRatingsByBookIsbn(any(String.class), any(PageRequest.class))).willReturn(bookRatings);
        when(service.findByIsbn(any(String.class))).thenThrow(new ResourceNotFoundException("No Book found by id"));

        mvc.perform(get("/v1/books/83839XHS1/ratings").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].userId", is(1)))
                .andExpect(jsonPath("$.content[0].score", is(1)))
                .andExpect(jsonPath("$.content[1].userId", is(2)))
                .andExpect(jsonPath("$.content[1].score", is(6)));
    }

    @Test
    public void shouldCreateNewBook() throws Exception {
        var requestBody = defaultBookRequest().build();
        var bookDto = BookDto.builder()
                .isbn(requestBody.getIsbn())
                .title(requestBody.getTitle())
                .build();

        when(service.create(any(BookRequestDto.class))).thenReturn(bookDto);

        mvc.perform(post("/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt())
                        .content(om.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isbn").value(bookDto.getIsbn()))
                .andExpect(jsonPath("$.title").value(bookDto.getTitle()));

        verify(service, times(1)).create(any(BookRequestDto.class));
    }

    @Test
    public void shouldNotCreateNewBook_entityExists() throws Exception {
        var requestBody = defaultBookRequest().build();
        var errorMessage = String.format("Book with isbn [%s] already exists.", requestBody.getIsbn());

        when(service.create(any(BookRequestDto.class))).thenThrow(new ConflictException(errorMessage));

        mvc.perform(post("/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt())
                        .content(om.writeValueAsString(requestBody)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(errorMessage));

        verify(service, times(1)).create(any(BookRequestDto.class));
    }

    @Test
    public void shouldReturn_BadRequest_IdMissmatch() throws Exception {
        var requestBody = defaultBookRequest().build();

        mvc.perform(put("/v1/books/X6218-211762HS")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt())
                        .content(om.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ISBN in path should be equal to ISBN in request body!"));

        verify(service, times(0)).update(any(String.class), any(BookRequestDto.class));
    }

    @Test
    public void shouldUpdateAuthor_isOk() throws Exception {
        var requestBody = defaultBookRequest().build();
        var bookDto = BookDto.builder()
                .isbn(requestBody.getIsbn())
                .title(requestBody.getTitle())
                .build();

        when(service.update(any(String.class), any(BookRequestDto.class)))
                .thenReturn(bookDto);

        mvc.perform(put("/v1/books/{isbn}", requestBody.getIsbn())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt())
                        .content(om.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(bookDto.getTitle()));

        verify(service, times(1)).update(any(String.class), any(BookRequestDto.class));
    }

    @Test
    public void shouldNotUpdateBook_IsbnAlreadyExist_Conflict() throws Exception {
        var requestBody = defaultBookRequest().build();

        var errorMessage = String.format("Book with isbn [%s] already exists!", requestBody.getIsbn());
        when(service.update(any(String.class), any(BookRequestDto.class)))
                .thenThrow(new ConflictException(errorMessage));

        mvc.perform(put("/v1/books/{isbn}", requestBody.getIsbn())
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt())
                        .content(om.writeValueAsString(requestBody)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(errorMessage));

        verify(service, times(1)).update(any(String.class), any(BookRequestDto.class));
    }

    @Test
    public void shouldNotDelete_BookNotFound() throws Exception {
        var errorMessage = "Book not found by isbn: [X7236HS93]";
        doThrow(new ResourceNotFoundException(errorMessage)).when(service).delete(any(String.class));

        mvc.perform(delete("/v1/books/X7236HS93").with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(errorMessage));

        verify(service, times(1)).delete(any(String.class));
    }

    private BookRequestDto.BookRequestDtoBuilder defaultBookRequest() {
        var author = AuthorDto.builder().id(1L).name("John Doe").build();
        var publisher = PublisherDto.builder().id(1L).name("IT Books Publishing").build();
        return BookRequestDto.builder()
                .isbn("168XHS28-HST12-1JSG8")
                .title("Unit Tests for dummies")
                .author(author)
                .publisher(publisher)
                .year(2019);
    }
}
