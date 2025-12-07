package com.malak.chatapp.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "friendships",
    indexes = {
        @Index(name = "idx_user1", columnList = "user1_id"),
        @Index(name = "idx_user2", columnList = "user2_id"),
        @Index(name = "idx_both_users", columnList = "user1_id, user2_id")
    },
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_user1_user2",
            columnNames = {"user1_id", "user2_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Friendship {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // RULE: user1_id must ALWAYS be less than user2_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        
        // Automatically ensure user1 has smaller ID than user2
        if (user1 != null && user2 != null && user1.getId() > user2.getId()) {
            User temp = user1;
            user1 = user2;
            user2 = temp;
        }
    }
    
    // Static factory method to create friendship with correct ordering
    public static Friendship create(User userA, User userB) {
        Friendship friendship = new Friendship();
        if (userA.getId() < userB.getId()) {
            friendship.setUser1(userA);
            friendship.setUser2(userB);
        } else {
            friendship.setUser1(userB);
            friendship.setUser2(userA);
        }
        return friendship;
    }
}