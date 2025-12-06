package com.malak.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.malak.chatapp.domain.Friendship;

public interface FriendshipRepository extends JpaRepository<Friendship, Long>{
	
}
