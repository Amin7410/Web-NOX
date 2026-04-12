package com.nox.platform.shared.infrastructure.util;

import com.nox.platform.shared.util.SlugGenerator;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;

@Component
public class DefaultSlugGenerator implements SlugGenerator {

    @Override
    public String generate(String input) {
        if (input == null) {
            return "";
        }
        String nonLatin = "[^\\w-]";
        String whiteSpace = "[\\s]";
        String nowhitespace = input.replaceAll(whiteSpace, "-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = normalized.replaceAll(nonLatin, "");
        return slug.toLowerCase(Locale.ENGLISH)
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
