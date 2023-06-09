package com.swef.cookcode.recipe.domain;

import com.swef.cookcode.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "step_photo")
@Getter
public class StepPhoto extends BaseEntity {

    private static final int MAX_URL_LENGTH = 500;

    @Id
    @Column(name = "step_photo_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id", nullable = false)
    private Step step;

    @Column(nullable = false, length = 300)
    private String photoUrl;

    @Builder
    public StepPhoto(Step step, String photoUrl){
        this.step = step;
        this.photoUrl = photoUrl;
    }

    public void setStep(Step step) {
        if (Objects.nonNull(this.step)) {
            this.step.getPhotos().remove(this);
        }
        this.step = step;
        if (!this.step.getPhotos().contains(this)) {
            this.step.addPhoto(this);
        }
    }
}
