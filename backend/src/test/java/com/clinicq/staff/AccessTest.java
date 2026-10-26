package com.clinicq.staff;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.clinicq.IntegrationTest;

class AccessTest extends IntegrationTest {

	private ResultActions as(String token, MockHttpServletRequestBuilder req) throws Exception {
		return mvc.perform(req.header("Authorization", "Bearer " + token));
	}

	private ResultActions as(String token, MockHttpServletRequestBuilder req, String json) throws Exception {
		return as(token, req.contentType(MediaType.APPLICATION_JSON).content(json));
	}

	private String addStaff(String ownerToken, String mobile, String role) throws Exception {
		String body = as(ownerToken, post("/api/staff"), """
				{"name":"Staff","mobile":"%s","role":"%s"}""".formatted(mobile, role))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getContentAsString();
		return JsonPath.read(body, "$.id");
	}

	@Test
	void receptionCannotEditClinicOrStaff() throws Exception {
		String owner = signupOwner(newMobile(), "Role Clinic", "Pune");
		String receptionMobile = newMobile();
		addStaff(owner, receptionMobile, "RECEPTION");
		String reception = login(receptionMobile);

		as(reception, get("/api/me")).andExpect(status().isOk()).andExpect(jsonPath("$.role").value("RECEPTION"));
		as(reception, get("/api/clinic")).andExpect(status().isOk());
		as(reception, put("/api/clinic"), "{\"name\":\"Hacked\"}")
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("forbidden"));
		as(reception, get("/api/staff")).andExpect(status().isForbidden());
		as(reception, post("/api/staff"), "{\"name\":\"X\",\"mobile\":\"%s\",\"role\":\"DOCTOR\"}".formatted(newMobile()))
			.andExpect(status().isForbidden());
	}

	@Test
	void ownerManagesStaff() throws Exception {
		String ownerMobile = newMobile();
		String owner = signupOwner(ownerMobile, "Staff Clinic", "Pune");
		String doctorMobile = newMobile();
		String doctorId = addStaff(owner, doctorMobile, "DOCTOR");

		as(owner, post("/api/staff"), "{\"name\":\"Dup\",\"mobile\":\"%s\",\"role\":\"DOCTOR\"}".formatted(doctorMobile))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("mobile_already_registered"));
		as(owner, post("/api/staff"), "{\"name\":\"X\",\"mobile\":\"%s\",\"role\":\"OWNER\"}".formatted(newMobile()))
			.andExpect(status().isBadRequest());

		as(owner, patch("/api/staff/" + doctorId), "{\"role\":\"RECEPTION\"}")
			.andExpect(status().isOk()).andExpect(jsonPath("$.role").value("RECEPTION"));
		as(owner, patch("/api/staff/" + doctorId), "{\"active\":false}")
			.andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false));
		// deactivated user cannot log in
		verify(doctorMobile, sendOtp(doctorMobile)).andExpect(status().isForbidden())
			.andExpect(jsonPath("$.code").value("user_inactive"));

		String ownerId = JsonPath.read(as(owner, get("/api/me")).andReturn().getResponse().getContentAsString(), "$.id");
		as(owner, patch("/api/staff/" + ownerId), "{\"active\":false}")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("cannot_modify_self"));

		as(owner, put("/api/clinic"), """
				{"name":"Staff Clinic 2","phone":"0731123456","mapUrl":"http://insecure"}""")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("validation_failed"));
		as(owner, put("/api/clinic"), """
				{"name":"Staff Clinic 2","phone":"0731123456","mapUrl":"https://maps.example/x","about":"Hi"}""")
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Staff Clinic 2"))
			.andExpect(jsonPath("$.timezone").value("Asia/Kolkata"));
	}

	@Test
	void tenantIsolation() throws Exception {
		String ownerA = signupOwner(newMobile(), "Clinic A", "Indore");
		String ownerB = signupOwner(newMobile(), "Clinic B", "Bhopal");
		String staffB = addStaff(ownerB, newMobile(), "DOCTOR");

		as(ownerA, patch("/api/staff/" + staffB), "{\"active\":false}")
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value("not_found"));
		as(ownerA, get("/api/staff"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[*].id", Matchers.not(Matchers.hasItem(staffB))))
			.andExpect(jsonPath("$.length()").value(1));
		as(ownerA, get("/api/clinic")).andExpect(jsonPath("$.name").value("Clinic A"));
		as(ownerA, put("/api/clinic"), "{\"name\":\"A Renamed\"}").andExpect(status().isOk());
		as(ownerB, get("/api/clinic")).andExpect(jsonPath("$.name").value("Clinic B"));
		// B's staff untouched
		as(ownerB, get("/api/staff")).andExpect(jsonPath("$[?(@.id=='%s')].active".formatted(staffB)).value(true));
	}
}
