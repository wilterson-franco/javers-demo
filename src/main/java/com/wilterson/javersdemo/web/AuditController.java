package com.wilterson.javersdemo.web;

import com.wilterson.javersdemo.domain.Merchant;
import com.wilterson.javersdemo.domain.SearchParameter;
import com.wilterson.javersdemo.service.AuditReport;
import com.wilterson.javersdemo.service.AuditReportService;
import com.wilterson.javersdemo.service.MerchantService;
import org.javers.core.Changes;
import org.javers.core.Javers;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.repository.jql.JqlQuery;
import org.javers.repository.jql.QueryBuilder;
import org.javers.shadow.Shadow;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AuditController {

	private final MerchantService merchantService;
	private final AuditReportService auditReportService;
	private final Javers javers;

	public AuditController(MerchantService merchantService, AuditReportService auditReportService, Javers javers) {
		this.merchantService = merchantService;
		this.auditReportService = auditReportService;
		this.javers = javers;
	}

	/*****************************************************************************************************
	 * RAW CHANGES
	 *****************************************************************************************************/

	@GetMapping("/merchants/{merchantId}/raw-changes")
	public ResponseEntity<String> getMerchantRawChanges(@PathVariable int merchantId) {
		Merchant merchant = merchantService.findMerchantById(merchantId);
		QueryBuilder jqlQuery = QueryBuilder.byInstance(merchant);
		Changes changes = javers.findChanges(jqlQuery.build());

		return ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(javers.getJsonConverter().toJson(changes));

//		return ResponseEntity
//				.ok()
//				.contentType(MediaType.APPLICATION_JSON)
//				.body(changes.prettyPrint());
	}

	@GetMapping("/searchParameters/{searchParameterId}/raw-changes")
	public ResponseEntity<String> getSearchParameterRawChanges(@PathVariable int searchParameterId) {
		SearchParameter searchParameter = merchantService.findSearchParameterById(searchParameterId);
		QueryBuilder jqlQuery = QueryBuilder.byInstance(searchParameter);
		Changes changes = javers.findChanges(jqlQuery.build());

		return ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(javers.getJsonConverter().toJson(changes));

//		return ResponseEntity
//				.ok()
//				.contentType(MediaType.APPLICATION_JSON)
//				.body(changes.prettyPrint());
	}

	/*****************************************************************************************************
	 * SNAPSHOTS
	 *****************************************************************************************************/

	@GetMapping("/merchants/snapshots")
	public ResponseEntity<String> getMerchantsSnapshots() {
		QueryBuilder jqlQuery = QueryBuilder.byClass(Merchant.class);
		List<CdoSnapshot> snapshots = javers.findSnapshots(jqlQuery.build());

		return ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(javers.getJsonConverter().toJson(snapshots));
	}

	@GetMapping("/merchants/{merchantId}/snapshot")
	public ResponseEntity<String> getMerchantSnapshot(@PathVariable int merchantId) {
		Merchant merchant = merchantService.findMerchantById(merchantId);
		QueryBuilder jqlQuery = QueryBuilder.byInstance(merchant);
		List<CdoSnapshot> snapshots = javers.findSnapshots(jqlQuery.build());

		return ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(javers.getJsonConverter().toJson(snapshots));
	}

	@GetMapping("/searchParameters/snapshots")
	public ResponseEntity<String> getSearchParameterSnapshots() {
		QueryBuilder jqlQuery = QueryBuilder.byClass(SearchParameter.class);
		List<CdoSnapshot> snapshots = javers.findSnapshots(jqlQuery.build());

		return ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(javers.getJsonConverter().toJson(snapshots));
	}

	/*****************************************************************************************************
	 * SHADOWS
	 *****************************************************************************************************/

	@GetMapping("/merchants/{merchantId}/shadows")
	public ResponseEntity<String> getMerchantShadows(@PathVariable int merchantId) {
		Merchant merchant = merchantService.findMerchantById(merchantId);
		JqlQuery jqlQuery = QueryBuilder.byInstance(merchant).withScopeDeepPlus()
				.withChildValueObjects().build();
		List<Shadow<Merchant>> shadows = javers.findShadows(jqlQuery);

		return ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(javers.getJsonConverter().toJson(shadows.get(0)));
	}

	/*****************************************************************************************************
	 * AUDIT CHANGES
	 *****************************************************************************************************/

	@GetMapping("/merchants/{merchantId}/changes")
	public ResponseEntity<List<AuditReport>> getMerchantChanges(@PathVariable int merchantId) {
		Merchant merchant = merchantService.findMerchantById(merchantId);
		return ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(auditReportService.auditReport(merchant.getId(), merchant.getClass().getName()));
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
}
