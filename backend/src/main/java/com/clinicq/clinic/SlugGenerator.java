package com.clinicq.clinic;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

final class SlugGenerator {

	static final Set<String> RESERVED = Set.of("api", "admin", "login", "app", "v", "services", "help", "signup",
			"static", "assets", "www");

	private SlugGenerator() {
	}

	/** name → slug; on clash or reserved word try name-city, then name-city-2, -3… */
	static String generate(String name, String city, Predicate<String> taken) {
		Predicate<String> free = s -> !RESERVED.contains(s) && !taken.test(s);
		String base = slugify(name, 60);
		if (base.isEmpty()) {
			base = "clinic";
		}
		if (free.test(base)) {
			return base;
		}
		String citySlug = slugify(city, 30);
		if (!citySlug.isEmpty()) {
			base = base + "-" + citySlug;
			if (free.test(base)) {
				return base;
			}
		}
		for (int i = 2;; i++) {
			if (free.test(base + "-" + i)) {
				return base + "-" + i;
			}
		}
	}

	static String slugify(String text, int maxLength) {
		if (text == null) {
			return "";
		}
		String ascii = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
		String slug = ascii.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
		if (slug.length() > maxLength) {
			slug = slug.substring(0, maxLength).replaceAll("-$", "");
		}
		return slug;
	}
}
