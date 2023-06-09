package com.swef.cookcode.common;

import com.swef.cookcode.common.error.exception.InvalidRequestException;
import com.swef.cookcode.common.util.S3Util;
import com.swef.cookcode.recipe.domain.Recipe;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class Util {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final S3Util s3Util;

    public static <T> void validateDuplication(List<T> list1, List<T> list2) {
        Set<T> mergedSets = new HashSet<>() {{
            addAll(list1);
            addAll(list2);
        }};
        if (mergedSets.size() < list1.size() + list2.size()) {
            throw new InvalidRequestException(ErrorCode.DUPLICATED);
        }
    }

    public static <T> boolean includesAll(List<T> list1, List<T> list2) {
        if (list1.size() < list2.size()) return false;
        for (T t : list2) {
            if (!list1.contains(t)) return false;
        }
        return true;
    }

    public UrlResponse uploadFilesToS3(String directory, List<MultipartFile> files) {
        List<String> urls = files.stream().map(file -> s3Util.upload(file, directory)).toList();
        return UrlResponse.builder()
                .urls(urls)
                .build();
    }
    public void deleteFilesInS3(List<String> urls) {
        urls.forEach(s3Util::deleteFile);
    }
}
