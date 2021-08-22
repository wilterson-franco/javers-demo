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
import java.util.Arrays;
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

		// 0. new merchant entity
		AuditReport auditReport = resp.get(0);
		assertThat(auditReport.getChangeType()).isEqualTo(ChangeType.NewObject);
		assertEntityRef(auditReport, "com.wilterson.javersdemo.domain.Merchant", 1);
		assertMetadata(auditReport, "Wilterson Test", "1.00", Instant.now());
		EntityRef searchParameterRef = getEntityRef("com.wilterson.javersdemo.domain.SearchParameter", 2);
		AuditReport auditReportSearchParamter = getAuditReport(ChangeType.NewObject, null, searchParameterRef, null);
		assertPropertyChanges(auditReport, Arrays.asList(getPropertyChange(
				PropertyChangeType.PROPERTY_VALUE_CHANGED,
				"searchParameters",
				null,
				null,
				Collections.singletonList(auditReportSearchParamter)),
				getPropertyChange(
						PropertyChangeType.PROPERTY_VALUE_CHANGED,
						"name",
						null,
						"Some Store",
						Collections.emptyList()),
				getPropertyChange(
						PropertyChangeType.PROPERTY_VALUE_CHANGED,
						"status",
						null,
						"LIVE",
						Collections.emptyList()),
				getPropertyChange(
						PropertyChangeType.PROPERTY_VALUE_CHANGED,
						"address",
						null,
						"999 Blue Street",
						Collections.emptyList()),
				getPropertyChange(
						PropertyChangeType.PROPERTY_VALUE_CHANGED,
						"postalCode",
						null,
						"A1B 2C3",
						Collections.emptyList())));

		// 1. new searchParameter entity
		auditReport = resp.get(1);
		assertThat(auditReport.getChangeType()).isEqualTo(ChangeType.NewObject);
		assertEntityRef(auditReport, "com.wilterson.javersdemo.domain.SearchParameter", 2);
		assertMetadata(auditReport, "Wilterson Test", "1.00", Instant.now());
		assertPropertyChanges(auditReport, Arrays.asList(getPropertyChange(
				PropertyChangeType.PROPERTY_VALUE_CHANGED,
				"name",
				null,
				"TRANSACTION_ID",
				Collections.emptyList()),
				getPropertyChange(
						PropertyChangeType.PROPERTY_VALUE_CHANGED,
						"required",
						null,
						true,
						Collections.emptyList())
		));

		// 2. searchParameter update (name TRANSACTION_ID to ARN and required true to false)
		auditReport = resp.get(2);
		assertThat(auditReport.getChangeType()).isEqualTo(ChangeType.ValueChange);
		assertEntityRef(auditReport, "com.wilterson.javersdemo.domain.SearchParameter", 2);
		assertMetadata(auditReport, "Wilterson Test", "2.00", Instant.now());
		assertPropertyChanges(auditReport, Arrays.asList(getPropertyChange(
				PropertyChangeType.PROPERTY_VALUE_CHANGED,
				"name",
				"TRANSACTION_ID",
				"ARN",
				Collections.emptyList()),
				getPropertyChange(
						PropertyChangeType.PROPERTY_VALUE_CHANGED,
						"required",
						false,
						true,
						Collections.emptyList())));
	}

	private void assertPropertyChanges(AuditReport auditReport, List<PropertyChange> propertyChangeList) {
		assertThat(auditReport.getPropertyChanges()).containsAll(propertyChangeList);
	}

	private void assertEntityRef(AuditReport auditReport, String entityRef, Integer entityId) {
		assertThat(auditReport.getEntityRef().getEntity()).isEqualTo(entityRef);
		assertThat(auditReport.getEntityRef().getEntityId()).isEqualTo(entityId);
	}

	private void assertMetadata(AuditReport auditReport, String author, String commitId, Instant commitDatetime) {
		assertThat(auditReport.getMetadata().getAuthor()).isEqualTo(author);
		assertThat(auditReport.getMetadata().getCommitId()).isEqualTo(commitId);
		assertThat(auditReport.getMetadata().getCommitDatetime()).isBeforeOrEqualTo(commitDatetime);
	}

	private PropertyChange getPropertyChange(PropertyChangeType type, String propertyName, Object left, Object right, List<AuditReport> elementChanges) {
		return PropertyChange
				.builder()
				.property(propertyName)
				.type(type)
				.left(left)
				.right(right)
				.elementChanges(elementChanges)
				.build();
	}

	private AuditReport getAuditReport(ChangeType changeType, Metadata metadata, EntityRef entityRef, List<PropertyChange> propertyChanges) {
		return AuditReport
				.builder()
				.changeType(changeType)
				.metadata(metadata)
				.entityRef(entityRef)
				.propertyChanges(propertyChanges)
				.build();
	}

	private EntityRef getEntityRef(String entity, Integer entityId) {
		return EntityRef
				.builder()
				.entity(entity)
				.entityId(entityId)
				.build();
	}
}
