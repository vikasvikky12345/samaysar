package com.clinicq.clinic;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

class SlugGeneratorTest {

	@Test
	void slugFromName() {
		assertThat(SlugGenerator.generate("Apollo Dental", "Indore", s -> false)).isEqualTo("apollo-dental");
	}

	@Test
	void clashAppendsCityThenNumber() {
		assertThat(SlugGenerator.generate("Apollo Dental", "Indore", Set.of("apollo-dental")::contains))
			.isEqualTo("apollo-dental-indore");
		assertThat(SlugGenerator.generate("Apollo Dental", "Indore",
				Set.of("apollo-dental", "apollo-dental-indore", "apollo-dental-indore-2")::contains))
			.isEqualTo("apollo-dental-indore-3");
	}

	@Test
	void reservedWordGetsSuffix() {
		assertThat(SlugGenerator.generate("Admin", "Pune", s -> false)).isEqualTo("admin-pune");
		assertThat(SlugGenerator.generate("API", "", s -> false)).isEqualTo("api-2");
	}

	@Test
	void nonAsciiStripped() {
		assertThat(SlugGenerator.generate("Café Clinic डॉक्टर!", "Indore", s -> false)).isEqualTo("cafe-clinic");
		assertThat(SlugGenerator.generate("डॉक्टर", "Indore", s -> false)).isEqualTo("clinic");
	}
}
