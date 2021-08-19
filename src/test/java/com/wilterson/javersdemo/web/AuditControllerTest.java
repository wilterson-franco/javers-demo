package com.wilterson.javersdemo.web;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wilterson.javersdemo.domain.Address;
import com.wilterson.javersdemo.domain.Merchant;
import com.wilterson.javersdemo.domain.SearchParameter;
import com.wilterson.javersdemo.repo.MerchantRepository;
import com.wilterson.javersdemo.service.AuditReport;
import com.wilterson.javersdemo.service.AuditReportService;
import com.wilterson.javersdemo.service.ChangeType;
import com.wilterson.javersdemo.service.MerchantService;
import org.javers.core.Javers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;
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

	private static final String MERCHANT_CHANGES = "/merchants/{merchantId}/propertyChanges";

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	private MerchantService merchantService;

	@Autowired
	private MerchantRepository merchantRepository;

	@Autowired
	private AuditReportService auditReportService;

	@Autowired
	private Javers javers;

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

		assertThat(resp).hasSize(7);

		// new merchant entity
		assertThat(resp.get(0).getChangeType()).isEqualTo(ChangeType.NEW_ENTITY);
		assertThat(resp.get(0).getEntity()).isEqualTo("com.wilterson.javersdemo.domain.Merchant");
		assertThat(resp.get(0).getAuthor()).isEqualTo("Wilterson Test");
		assertThat(resp.get(0).getCommitDatetime()).isBeforeOrEqualTo(Instant.now());

		// merchant.name
		assertThat(resp.get(1).getChangeType()).isEqualTo(ChangeType.PROPERTY_VALUE_CHANGED);
		assertThat(resp.get(1).getEntity()).isEqualTo("com.wilterson.javersdemo.domain.Merchant");
		assertThat(resp.get(1).getOldPropertyValue()).isNull();
		assertThat(resp.get(1).getPropertyName()).isEqualTo("name");
		assertThat(resp.get(1).getNewPropertyValue()).isEqualTo("Some Store");
		assertThat(resp.get(1).getAuthor()).isEqualTo("Wilterson Test");
		assertThat(resp.get(1).getCommitDatetime()).isBeforeOrEqualTo(Instant.now());
	}
}
