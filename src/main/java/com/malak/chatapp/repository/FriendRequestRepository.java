package com.malak.chatapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.malak.chatapp.domain.FriendRequest;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long>{

}
