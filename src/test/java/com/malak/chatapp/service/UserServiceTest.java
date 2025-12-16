package com.malak.chatapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.malak.chatapp.domain.Role;
import com.malak.chatapp.domain.User;
import com.malak.chatapp.dto.CreateUserDto;
import com.malak.chatapp.exception.ResourceAlreadyExistsException;
import com.malak.chatapp.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
	@Mock 
	UserRepository userRepository;
	
	@Mock
	PasswordEncoder passwordEncoder;
	
	@InjectMocks
	UserService userService;
	
	
	@Test
	void createUser_UsernameTaken_throwsException() {
		CreateUserDto userDto = CreateUserDto.builder().username("test").build();
		User user = User.builder().username(userDto.getUsername()).build();
		
		when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
		
		assertThrows(ResourceAlreadyExistsException.class, () -> 
		userService.createUser(userDto, Role.USER));
		
		verify(userRepository, never()).save(any());
	}
	@Test
	void createUser_validCall_savesEncodedUser() {
	    CreateUserDto userDto = CreateUserDto.builder()
	            .username("test")
	            .password("rawPassword")
	            .build();

	    when(userRepository.findByUsername("test"))
	            .thenReturn(Optional.empty());
	    when(passwordEncoder.encode("rawPassword"))
	            .thenReturn("encodedPassword");

	    userService.createUser(userDto, Role.USER);

	    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
	    verify(userRepository).save(captor.capture());

	    User savedUser = captor.getValue();
	    assertEquals("test", savedUser.getUsername());
	    assertEquals("encodedPassword", savedUser.getPassword());
	    assertEquals(Role.USER, savedUser.getRole());
	}

}
