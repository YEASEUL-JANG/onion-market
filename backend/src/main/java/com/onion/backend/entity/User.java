package com.onion.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.onion.backend.config.DeviceListConverter;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class) // Auditing 기능을 사용하기 위한 설정
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    private LocalDateTime lastLogin;

    // 유저가 처음 생성된 날짜 (자동 설정)
    @CreatedDate
    @Column(insertable = true)
    private LocalDateTime createdDate;

    // 유저 정보가 갱신될 때마다 자동으로 설정
    @LastModifiedDate //코드레벨에서만 적용
    private LocalDateTime updatedDate;

    @Column(columnDefinition = "json")
    @Convert(converter = DeviceListConverter.class)
    private List<Device> deviceList = new ArrayList<>();

    @PrePersist
    public void setDefaultValues() {
        if (deviceList == null) {
            deviceList = new ArrayList<>();
        }
    }
    @PostLoad
    public void initializeDeviceList() {
        if (deviceList == null) {
            deviceList = new ArrayList<>();
        }
    }
}
