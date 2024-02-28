package com.bookstore.controller;

import com.bookstore.controller.filters.UserFilter;
import com.bookstore.dto.user.UpdateUserRequestDto;
import com.bookstore.dto.user.UserDto;
import com.bookstore.dto.user.UserRequestDto;
import com.bookstore.exception.ConflictException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
@WebMvcTest(UserController.class)
public class UserControllerTests {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService service;

    @Autowired
    private ObjectMapper om;

    @Test
    void shouldReturnUsersList_whenGetAll() throws Exception {
        Page<UserDto> allUsers = new PageImpl<>(List.of(
                UserDto.ofAge(15), UserDto.ofAge(16)
        ));

        given(service.findAll(any(UserFilter.class), any(PageRequest.class))).willReturn(allUsers);

        mvc.perform(get("/v1/users").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", hasSize(2)))
                .andExpect(jsonPath("content[0].age", is(15)))
                .andExpect(jsonPath("content[1].age", is(16)));

        verify(service, times(1)).findAll(any(UserFilter.class), any(Pageable.class));
    }

    @Test
    void shouldReturnUserDto_whenGetById() throws Exception {
        UserDto user = UserDto.ofAge(15);
        ;
        given(service.findById(any(Long.class))).willReturn(user);

        mvc.perform(get("/v1/users/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("age", is(user.getAge())));
    }

    @Test
    void shouldReturnNotFound_whenGetByNonExistentId() throws Exception {
        when(service.findById(2L)).thenThrow(new ResourceNotFoundException("No User found by id"));
        mvc.perform(get("/v1/users/2").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("error", is("No User found by id")));
    }

    @Test
    public void shouldCreateNewUser() throws Exception {
        var requestBody = UserRequestDto.builder().age(20).country("Ukraine").build();
        var userDto = UserDto.builder().id(1L).age(20).country("Ukraine").build();

        when(service.create(any(UserRequestDto.class))).thenReturn(userDto);

        mvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt())
                        .content(om.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userDto.getId()))
                .andExpect(jsonPath("$.country").value(userDto.getCountry()));

        verify(service, times(1)).create(any(UserRequestDto.class));
    }

    @Test
    public void shouldNotCreateNewUser_entityExists() throws Exception {
        UserRequestDto requestBody = UserRequestDto.builder().age(20).city("Kyiv").build();

        when(service.create(any(UserRequestDto.class)))
                .thenThrow(new ConflictException("User with name [John Doe] already exists."));

        mvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt())
                        .content(om.writeValueAsString(requestBody)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("User with name [John Doe] already exists."));

        verify(service, times(1)).create(any(UserRequestDto.class));
    }

    @Test
    public void shouldReturn_BadRequest_IdMissmatch() throws Exception {
        var requestBody = UpdateUserRequestDto.builder().id(2L).age(20).country("Ukraine").build();

        mvc.perform(put("/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt())
                        .content(om.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Id in path should be equal to id in request body!"));

        verify(service, never()).update(any(Long.class), any(UpdateUserRequestDto.class));
    }

    @Test
    public void shouldUpdateUser_isOk() throws Exception {
        var requestBody = UpdateUserRequestDto.builder().id(2L).age(20).country("Ukraine").build();

        when(service.update(any(Long.class), any(UpdateUserRequestDto.class)))
                .thenReturn(UserDto.builder().age(20).country("Ukraine").build());

        mvc.perform(put("/v1/users/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt())
                        .content(om.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.country").value("Ukraine"));

        verify(service, times(1)).update(any(Long.class), any(UpdateUserRequestDto.class));
    }

    @Test
    public void shouldNotUpdateUser_NameAlreadyExist_Conflict() throws Exception {
        var requestBody = UpdateUserRequestDto.builder().id(2L).age(20).country("Ukraine").build();

        var errorMessage = "User with name [John Doe Updated] already exists!";
        when(service.update(any(Long.class), any(UpdateUserRequestDto.class)))
                .thenThrow(new ConflictException(errorMessage));

        mvc.perform(put("/v1/users/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(jwt())
                        .content(om.writeValueAsString(requestBody)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(errorMessage));

        verify(service, times(1)).update(any(Long.class), any(UpdateUserRequestDto.class));
    }

    @Test
    public void shouldNotDelete_UserNotFound() throws Exception {
        var errorMessage = "User not found by id: [2]";
        doThrow(new ResourceNotFoundException(errorMessage)).when(service).delete(any(Long.class));

        mvc.perform(delete("/v1/users/2").with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(errorMessage));

        verify(service, times(1)).delete(any(Long.class));
    }

    @Test
    public void shouldNotDelete_UserHasBooks_Conflict() throws Exception {
        var errorMessage = "Can't delete User: [2]. Books not empty!";
        doThrow(new ConflictException(errorMessage)).when(service).delete(any(Long.class));

        mvc.perform(delete("/v1/users/2").with(jwt()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(errorMessage));

        verify(service, times(1)).delete(any(Long.class));
    }
}
