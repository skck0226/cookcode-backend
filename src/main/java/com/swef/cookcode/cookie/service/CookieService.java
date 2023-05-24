package com.swef.cookcode.cookie.service;

import com.swef.cookcode.common.util.S3Util;
import com.swef.cookcode.cookie.domain.Cookie;
import com.swef.cookcode.cookie.dto.CookieCreateRequest;
import com.swef.cookcode.cookie.repository.CookieRepository;
import com.swef.cookcode.recipe.domain.Recipe;
import com.swef.cookcode.recipe.repository.RecipeRepository;
import com.swef.cookcode.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import com.swef.cookcode.common.error.exception.NotFoundException;
import com.swef.cookcode.common.ErrorCode;

@Service
@RequiredArgsConstructor
public class CookieService {

    private final CookieRepository cookieRepository;

    private final S3Util s3Util;

    private final RecipeRepository recipeRepository;


    @Transactional
    public Slice<Cookie> getRandomCookies(Pageable pageable) {
        return cookieRepository.findRandomCookies(pageable);
    }

    @Transactional
    public Cookie getCookieById(Long cookieId) {
        return cookieRepository.findById(cookieId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COOKIE_NOT_FOUND));
    }

    @Transactional
    public Slice<Cookie> getCookiesOfUser(Pageable pageable, Long userId) {
        return cookieRepository.findByUserId(pageable, userId);
    }

    @Transactional
    public void createCookie(User user, CookieCreateRequest request){
        String cookieUrl = s3Util.upload(request.getMultipartFile(), "cookie");

        Recipe recipe = getRecipeOrNull(request.getRecipeId());

        Cookie cookie = createCookieEntity(request, user, cookieUrl, recipe);

        cookieRepository.save(cookie);
    }

    private Recipe getRecipeOrNull(Long recipeId) {
        return recipeId == null ? null : recipeRepository.getReferenceById(recipeId);
    }

    private Cookie createCookieEntity(CookieCreateRequest cookieCreateRequest, User user, String cookieUrl, Recipe recipe) {
        return Cookie.builder()
                .title(cookieCreateRequest.getTitle())
                .description(cookieCreateRequest.getDesc())
                .videoUrl(cookieUrl)
                .user(user)
                .recipe(recipe)
                .build();
    }
}
