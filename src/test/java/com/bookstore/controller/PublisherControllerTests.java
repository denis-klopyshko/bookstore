package com.bookstore.controller;

import com.bookstore.dto.publisher.PublisherDto;
import com.bookstore.dto.publisher.PublisherRequestDto;
import com.bookstore.dto.publisher.PublisherUpdateRequestDto;
import com.bookstore.exception.ConflictException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.service.PublisherService;
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
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser
@WebMvcTest(PublisherController.class)
public class PublisherControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PublisherService service;

    @Autowired
    private ObjectMapper om;

    @Test
    void shouldReturnPublishersList_whenGetAll() throws Exception {
        PublisherDto publisher = new PublisherDto(1L, "Alex");
        Page<PublisherDto> allPublishers = new PageImpl<>(List.of(publisher));

        given(service.findAll(any(PageRequest.class))).willReturn(allPublishers);

        mvc.perform(get("/v1/publishers").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", hasSize(1)))
                .andExpect(jsonPath("content[0].name", is(publisher.getName())));
    }

    @Test
    void shouldReturnPublisherDto_whenGetById() throws Exception {
        PublisherDto Publisher = new PublisherDto(1L, "Alex");
        given(service.findById(any(Long.class))).willReturn(Publisher);

        mvc.perform(get("/v1/publishers/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("name", is(Publisher.getName())));
    }

    @Test
    void shouldReturnNotFound_whenGetByNonExistentId() throws Exception {
        when(service.findById(2L)).thenThrow(new ResourceNotFoundException("No Publisher found by id"));
        mvc.perform(get("/v1/publishers/2").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("error", is("No Publisher found by id")));
    }

    @Test
    public void shouldCreateNewPublisher() throws Exception {
        PublisherRequestDto requestBody = new PublisherRequestDto("John Doe");

        when(service.create(any(PublisherRequestDto.class)))
                .thenReturn(new PublisherDto(1L, "John Doe"));

        mvc.perform(post("/v1/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt())
                        .content(om.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(service, times(1)).create(any(PublisherRequestDto.class));
    }

    @Test
    public void shouldNotCreateNewPublisher_entityExists() throws Exception {
        PublisherRequestDto requestBody = new PublisherRequestDto("John Doe");

        when(service.create(any(PublisherRequestDto.class)))
                .thenThrow(new ConflictException("Publisher with name [John Doe] already exists."));

        mvc.perform(post("/v1/publishers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt())
                        .content(om.writeValueAsString(requestBody)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Publisher with name [John Doe] already exists."));

        verify(service, times(1)).create(any(PublisherRequestDto.class));
    }

    @Test
    public void shouldReturn_BadRequest_IdMissmatch() throws Exception {
        var requestBody = new PublisherUpdateRequestDto(2L, "John Doe");

        mvc.perform(put("/v1/publishers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt())
                        .content(om.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Id in path should be equal to id in request body!"));

        verify(service, times(0)).update(any(Long.class), any(PublisherUpdateRequestDto.class));
    }

    @Test
    public void shouldUpdatePublisher_isOk() throws Exception {
        var requestBody = new PublisherUpdateRequestDto(2L, "John Doe Updated");

        when(service.update(any(Long.class), any(PublisherUpdateRequestDto.class)))
                .thenReturn(new PublisherDto(2L, "John Doe Updated"));

        mvc.perform(put("/v1/publishers/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt())
                        .content(om.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe Updated"));

        verify(service, times(1)).update(any(Long.class), any(PublisherUpdateRequestDto.class));
    }

    @Test
    public void shouldNotUpdatePublisher_NameAlreadyExist_Conflict() throws Exception {
        var requestBody = new PublisherUpdateRequestDto(2L, "John Doe Updated");

        var errorMessage = "Publisher with name [John Doe Updated] already exists!";
        when(service.update(any(Long.class), any(PublisherUpdateRequestDto.class)))
                .thenThrow(new ConflictException(errorMessage));

        mvc.perform(put("/v1/publishers/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt())
                        .content(om.writeValueAsString(requestBody)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(errorMessage));

        verify(service, times(1)).update(any(Long.class), any(PublisherUpdateRequestDto.class));
    }

    @Test
    public void shouldNotDelete_PublisherNotFound() throws Exception {
        var errorMessage = "Publisher not found by id: [2]";
        doThrow(new ResourceNotFoundException(errorMessage)).when(service).delete(any(Long.class));

        mvc.perform(delete("/v1/publishers/2").with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(errorMessage));

        verify(service, times(1)).delete(any(Long.class));
    }

    @Test
    public void shouldNotDelete_PublisherHasBooks_Conflict() throws Exception {
        var errorMessage = "Can't delete Publisher: [2]. Books not empty!";
        doThrow(new ConflictException(errorMessage)).when(service).delete(any(Long.class));

        mvc.perform(delete("/v1/publishers/2").with(jwt()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(errorMessage));

        verify(service, times(1)).delete(any(Long.class));
    }
}
