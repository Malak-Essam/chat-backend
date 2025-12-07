package com.malak.chatapp.service;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.malak.chatapp.domain.Role;
import com.malak.chatapp.domain.User;
import com.malak.chatapp.dto.CreateUserDto;
import com.malak.chatapp.dto.UserDto;
import com.malak.chatapp.exception.ResourceAlreadyExistsException;
import com.malak.chatapp.exception.ResourceNotFoundException;
import com.malak.chatapp.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	
	public User createUser(CreateUserDto createUserDto, Role role) {
		Optional<User> optionalUser =  userRepository.findByUsername(createUserDto.getUsername());
		if(optionalUser.isPresent()) {
			throw new ResourceAlreadyExistsException("Email is Already taken");
		}
		
		User user = User
				.builder()
				.username(createUserDto.getUsername())
				.password(passwordEncoder.encode(createUserDto.getPassword()))
				.role(role).build();
		
		return userRepository.save(user);
	}

	public UserDto getUserById(Long userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("no user with this id"));
		return UserDto
				.builder()
				.id(user.getId())
				.username(user.getUsername())
				.role(user.getRole())
				.build();
	}
	public User findUserByUsername(String username) {
		return userRepository.findByUsername(username).orElseThrow(
				() -> new ResourceNotFoundException("no user with this username"));
	}
	public User findUserById(long id) {
		return userRepository.findById(id).orElseThrow(
				() -> new ResourceNotFoundException("no user with this id"));
	}
	

	public List<UserDto> findAllUsers() {
		List<User> users = userRepository.findAll();
		List<UserDto> usersDto = users.stream().map(user -> 
			UserDto
			.builder()
			.id(user.getId())
			.username(user.getUsername())
			.role(user.getRole())
			.build()).toList();
		return usersDto;
	}

}
