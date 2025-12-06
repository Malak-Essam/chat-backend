package com.malak.chatapp.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Friendship {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user1_id", nullable = false)
	private User user1;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user2_id", nullable = false)
	private User user2;
	
	private LocalDateTime createdAt;
	
	private LocalDateTime updatedAt;
	
	@PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
	
	@PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
	}
    
}
