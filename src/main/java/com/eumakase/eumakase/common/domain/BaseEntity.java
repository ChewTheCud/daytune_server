package com.eumakase.eumakase.common.domain;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class BaseEntity {

    @CreatedDate
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedDate;

    public void updateCreateDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public void updateModifiedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }
}
