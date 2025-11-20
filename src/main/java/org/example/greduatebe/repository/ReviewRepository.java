package org.example.greduatebe.repository;

import org.example.greduatebe.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Review Repository
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * attemptId로 리뷰 조회
     * @param attemptId 미션 시도 ID
     * @return Review
     */
    Optional<Review> findByAttemptId(String attemptId);

    /**
     * reviewId로 리뷰 조회
     * @param reviewId 리뷰 ID
     * @return Review
     */
    Optional<Review> findByReviewId(String reviewId);

    /**
     * attemptId로 리뷰 존재 여부 확인
     * @param attemptId 미션 시도 ID
     * @return 존재 여부
     */
    boolean existsByAttemptId(String attemptId);
}
