package com.test.security.offer;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "offers")
public class Offer {

    @Id
    @GeneratedValue
    private Long id;

    private String firstName;
    private String lastName;
    private String mobile;
    private String email;
    private String requirements;
    private String homeType;
    @Enumerated(EnumType.STRING)
    private HomeStatusEnum homeStatus;
    private Double homeSize;
    private Integer numberOfLevels;
    private Integer numberOfRooms;
    private LocalDateTime installationDate;

    //status default is pending
    @Enumerated(EnumType.STRING )
    @Builder.Default
    private OfferStatus status = OfferStatus.PENDING;

    private List<String> smartSensors;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> optionalFeatures;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> address;



}
