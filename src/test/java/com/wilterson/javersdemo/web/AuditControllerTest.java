package com.wilterson.javersdemo.web;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wilterson.javersdemo.domain.Address;
import com.wilterson.javersdemo.domain.Merchant;
import com.wilterson.javersdemo.domain.SearchParameter;
import com.wilterson.javersdemo.service.*;
import org.javers.core.diff.changetype.PropertyChangeType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuditControllerTest {

	private static final String MERCHANT_CHANGES = "/merchants/{merchantId}/changes";

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	private MerchantService merchantService;

	private Merchant createLiveMerchant() {

		Merchant merchant = Merchant
				.builder()
				.name("Some Store")
				.address(Address
						.builder()
						.address("999 Blue Street")
						.postalCode("A1B 2C3")
						.build())
				.searchParameters(Stream.of(SearchParameter
						.builder()
						.name("TRANSACTION_ID")
						.required(false)
						.build()).collect(Collectors.toList()))
				.build();

		// creates a merchant in CONFIGURATION status
		merchant = merchantService.createMerchant(merchant);

		// checks the merchant in to LIVE status
		return merchantService.checkIn(merchant.getId());
	}

	private Merchant updateSearchParameter(Merchant merchant) {

		// checks the merchant out to CONFIGURATION status
		Merchant configMerchant = merchantService.checkOut(merchant.getId());

		Merchant updatedMerchant = new Merchant();
		BeanUtils.copyProperties(configMerchant, updatedMerchant);
		updatedMerchant.getSearchParameters().get(0).setName("ARN");
		updatedMerchant.getSearchParameters().get(0).setRequired(true);

		// makes a change against the CONFIGURATION merchant
		updatedMerchant = merchantService.update(merchant.getId(), updatedMerchant);

		return merchantService.checkIn(updatedMerchant.getId());
	}

	@Test
	void getMerchantChanges() throws Exception {

		// given
		Merchant liveMerchant = createLiveMerchant();
		liveMerchant = updateSearchParameter(liveMerchant);

		// when
		MvcResult result = mockMvc.perform(get(MERCHANT_CHANGES, liveMerchant.getId())
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(liveMerchant))
		)
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andReturn();

		// then
		JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, AuditReport.class);
		List<AuditReport> resp = objectMapper.readValue(result.getResponse().getContentAsString(), type);

		List<AuditReport> expected = Stream.of(
				AuditReport.builder().build())
				.collect(Collectors.toList());

		// Three changes:
		// 0. new merchant entity
		// 1. new searchParameter entity
		// 2. searchParameter update (name TRANSACTION_ID to ARN and required true to false)
		assertThat(resp).hasSize(3);

		AuditReport auditReport = resp.get(0);

		assertThat(auditReport.getChangeType()).isEqualTo(ChangeType.NewObject);

		assertThat(auditReport.getEntityRef().getEntity()).isEqualTo("com.wilterson.javersdemo.domain.Merchant");
		assertThat(auditReport.getEntityRef().getEntityId()).isEqualTo(1);

		assertThat(auditReport.getMetadata().getAuthor()).isEqualTo("Wilterson Test");
		assertThat(auditReport.getMetadata().getCommitId()).isEqualTo("1.00");
		assertThat(auditReport.getMetadata().getCommitDatetime()).isBeforeOrEqualTo(Instant.now());

		assertThat(auditReport.getPropertyChanges()).contains(PropertyChange
				.builder()
				.type(PropertyChangeType.PROPERTY_VALUE_CHANGED)
				.property("searchParameters")
				.left(null)
				.right(null)
				.elementChanges(Collections.singletonList(AuditReport
						.builder()
						.changeType(ChangeType.NewObject)
						.metadata(null)
						.entityRef(EntityRef
								.builder()
								.entity("com.wilterson.javersdemo.domain.SearchParameter")
								.entityId(2)
								.build())
						.propertyChanges(null)
						.build()))
				.build());

	}
}
