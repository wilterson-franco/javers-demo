package com.wilterson.javersdemo.web;

import com.wilterson.javersdemo.domain.Merchant;
import com.wilterson.javersdemo.domain.SearchParameter;
import com.wilterson.javersdemo.service.AuditReportService;
import com.wilterson.javersdemo.service.ConfigMerchantService;
import com.wilterson.javersdemo.service.MerchantService;
import org.javers.core.Changes;
import org.javers.core.Javers;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.repository.jql.JqlQuery;
import org.javers.repository.jql.QueryBuilder;
import org.javers.shadow.Shadow;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
public class MerchantController {

	private final MerchantService merchantService;
	private final ConfigMerchantService configMerchantService;
	private final Javers javers;
	private final AuditReportService auditReportService;

	public MerchantController(MerchantService customerService, ConfigMerchantService configMerchantService, Javers javers, AuditReportService auditReportService) {
		this.merchantService = customerService;
		this.javers = javers;
		this.configMerchantService = configMerchantService;
		this.auditReportService = auditReportService;
	}

	@PostMapping("/merchants")
	public ResponseEntity<Merchant> createMerchant(@RequestBody Merchant merchant) {
		return ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(configMerchantService.createMerchant(merchant));
	}

	@PutMapping("/merchants/{merchantId}")
	public void updateMerchant(@PathVariable Integer merchantId, @RequestBody Merchant updatedMerchant) {
		configMerchantService.update(merchantId, updatedMerchant);
	}

	@PutMapping("/merchants/{merchantId}/live")
	public void checkIn(@PathVariable Integer merchantId) {
		configMerchantService.checkIn(merchantId);
	}

	@PutMapping("/merchants/{merchantId}/config")
	public ResponseEntity<Merchant> checkOut(@PathVariable Integer merchantId) {
		return ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(configMerchantService.checkOut(merchantId));
	}

	@GetMapping("/searchParameters/{searchParameterId}/changes")
	public ResponseEntity<List<AuditReport>> getSearchParameterChanges(@PathVariable int searchParameterId) {
		SearchParameter searchParameter = merchantService.findSearchParameterById(searchParameterId);
		QueryBuilder jqlQuery = QueryBuilder.byInstance(searchParameter);
		Changes changes = javers.findChanges(jqlQuery.build());
		return ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(auditReportService.generateAuditReport(changes));
	}

	@GetMapping("/searchParameters/snapshots")
	public String getSearchParameterSnapshots() {
		QueryBuilder jqlQuery = QueryBuilder.byClass(SearchParameter.class);
		List<CdoSnapshot> snapshots = javers.findSnapshots(jqlQuery.build());
		return javers.getJsonConverter().toJson(snapshots);
	}

	@GetMapping("/merchants/{merchantId}/shadows")
	public String getMerchantShadows(@PathVariable int merchantId) {
		Merchant merchant = merchantService.findMerchantById(merchantId);
		JqlQuery jqlQuery = QueryBuilder.byInstance(merchant)
				.withChildValueObjects().build();
		List<Shadow<Merchant>> shadows = javers.findShadows(jqlQuery);
		return javers.getJsonConverter().toJson(shadows.get(0));
	}

	@GetMapping("/merchants/snapshots")
	public String getMerchantsSnapshots() {
		QueryBuilder jqlQuery = QueryBuilder.byClass(Merchant.class);
		List<CdoSnapshot> snapshots = javers.findSnapshots(jqlQuery.build());
		return javers.getJsonConverter().toJson(snapshots);
	}
}
