package com.clinicq.clinic;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Clinic {

	@Id
	@GeneratedValue
	private UUID id;
	private String slug;
	private String name;
	private String address;
	private String city;
	private String phone;
	private String whatsapp;
	private String mapUrl;
	private String about;
	private String timezone = "Asia/Kolkata";
	private String plan;
	private String planStatus = "TRIAL";
	private Instant trialEndsAt;
	private int messageCredits;
	private Instant createdAt = Instant.now();

	protected Clinic() {
	}

	public Clinic(String slug, String name, String city, Instant trialEndsAt) {
		this.slug = slug;
		this.name = name;
		this.city = city;
		this.trialEndsAt = trialEndsAt;
	}

	void update(String name, String address, String city, String phone, String whatsapp, String mapUrl, String about) {
		this.name = name;
		this.address = address;
		this.city = city;
		this.phone = phone;
		this.whatsapp = whatsapp;
		this.mapUrl = mapUrl;
		this.about = about;
	}

	public UUID getId() { return id; }
	public String getSlug() { return slug; }
	public String getName() { return name; }
	public String getAddress() { return address; }
	public String getCity() { return city; }
	public String getPhone() { return phone; }
	public String getWhatsapp() { return whatsapp; }
	public String getMapUrl() { return mapUrl; }
	public String getAbout() { return about; }
	public String getTimezone() { return timezone; }
	public String getPlanStatus() { return planStatus; }
	public Instant getTrialEndsAt() { return trialEndsAt; }
}
