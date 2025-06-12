package com.steamanalytics.model.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "friendships",
        uniqueConstraints = @UniqueConstraint(columnNames = {"requester_id", "addressee_id"}))
@EntityListeners(AuditingEntityListener.class)
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "friendship_seq")
    @SequenceGenerator(name = "friendship_seq", sequenceName = "friendships_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addressee_id", nullable = false)
    private User addressee;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private FriendshipStatus status = FriendshipStatus.PENDING;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    // Enum para status da amizade
    public enum FriendshipStatus {
        PENDING("pending"),
        ACCEPTED("accepted"),
        BLOCKED("blocked"),
        DECLINED("declined");

        private final String value;

        FriendshipStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static FriendshipStatus fromValue(String value) {
            for (FriendshipStatus status : FriendshipStatus.values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid status value: " + value);
        }
    }

    // Construtores
    public Friendship() {}

    public Friendship(User requester, User addressee) {
        this.requester = requester;
        this.addressee = addressee;
        this.status = FriendshipStatus.PENDING;
    }

    public Friendship(User requester, User addressee, FriendshipStatus status) {
        this.requester = requester;
        this.addressee = addressee;
        this.status = status;
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public User getAddressee() {
        return addressee;
    }

    public void setAddressee(User addressee) {
        this.addressee = addressee;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public void setStatus(FriendshipStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Métodos utilitários
    public void accept() {
        this.status = FriendshipStatus.ACCEPTED;
    }

    public void decline() {
        this.status = FriendshipStatus.DECLINED;
    }

    public void block() {
        this.status = FriendshipStatus.BLOCKED;
    }

    public boolean isPending() {
        return status == FriendshipStatus.PENDING;
    }

    public boolean isAccepted() {
        return status == FriendshipStatus.ACCEPTED;
    }

    public boolean isBlocked() {
        return status == FriendshipStatus.BLOCKED;
    }

    public boolean isDeclined() {
        return status == FriendshipStatus.DECLINED;
    }

    public User getOtherUser(User currentUser) {
        if (currentUser.equals(requester)) {
            return addressee;
        } else if (currentUser.equals(addressee)) {
            return requester;
        } else {
            throw new IllegalArgumentException("User is not part of this friendship");
        }
    }

    public boolean involves(User user) {
        return user.equals(requester) || user.equals(addressee);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Friendship)) return false;
        Friendship that = (Friendship) o;
        return requester != null && addressee != null &&
                requester.equals(that.requester) && addressee.equals(that.addressee);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Friendship{" +
                "id=" + id +
                ", requester=" + (requester != null ? requester.getUsername() : null) +
                ", addressee=" + (addressee != null ? addressee.getUsername() : null) +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}