package com.swef.cookcode.cookie.repository;

import com.swef.cookcode.cookie.domain.CookieComment;
import com.swef.cookcode.cookie.dto.CookieCommentResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CookieCommentRepository extends JpaRepository<CookieComment, Long> {

    @Query(value = "SELECT c FROM CookieComment c join fetch c.user WHERE c.cookie.id = :cookieId")
    List<CookieComment> findCookieComments(Long cookieId);
}