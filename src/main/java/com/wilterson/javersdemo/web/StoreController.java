package com.wilterson.javersdemo.web;

import com.wilterson.javersdemo.domain.Product;
import com.wilterson.javersdemo.domain.Store;
import com.wilterson.javersdemo.service.ConfigurationStoreService;
import com.wilterson.javersdemo.service.StoreService;
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

@RestController
public class StoreController {

	private final StoreService storeService;
	private final ConfigurationStoreService configurationStoreService;
	private final Javers javers;

	public StoreController(StoreService customerService, ConfigurationStoreService configurationStoreService, Javers javers) {
		this.storeService = customerService;
		this.javers = javers;
		this.configurationStoreService = configurationStoreService;
	}

	@PostMapping("/stores")
	public ResponseEntity<Store> createStore(@RequestBody Store store) {
		return ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(configurationStoreService.createStore(store));
	}

	@PutMapping("/stores/{storeId}")
	public void updateStore(@PathVariable Integer storeId, @RequestBody Store updatedStore) {
		configurationStoreService.update(storeId, updatedStore);
	}

	@PutMapping("/stores/{storeId}/live")
	public void checkIn(@PathVariable Integer storeId) {
		configurationStoreService.checkIn(storeId);
	}

	@PutMapping("/stores/{storeId}/config")
	public ResponseEntity<Store> checkOut(@PathVariable Integer storeId) {
		return ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
//				.body(configurationStoreService.createStore(configurationStoreService.checkOut(storeId)));
				.body(configurationStoreService.checkOut(storeId));
	}

	@PostMapping("/stores/{storeId}/products/random")
	public void createRandomProduct(@PathVariable final Integer storeId) {
		storeService.createRandomProduct(storeId);
	}

	@PostMapping("/stores/{storeId}/rebrand")
	public void rebrandStore(@PathVariable final Integer storeId, @RequestBody RebrandStoreDto rebrandStoreDto) {
		storeService.rebrandStore(storeId, rebrandStoreDto.getName());
	}

	@PostMapping(value = "/stores/{storeId}/products/{productId}/price", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void updateProductPrice(@PathVariable final Integer productId, @PathVariable String storeId, @RequestBody UpdatePriceDto priceDto) {
		storeService.updateProductPrice(productId, priceDto.getPrice());
	}

	@GetMapping("/products/{productId}/changes")
	public String getProductChanges(@PathVariable int productId) {
		Product product = storeService.findProductById(productId);
		QueryBuilder jqlQuery = QueryBuilder.byInstance(product);
		Changes changes = javers.findChanges(jqlQuery.build());
		return javers.getJsonConverter().toJson(changes);
	}

	@GetMapping("/products/snapshots")
	public String getProductSnapshots() {
		QueryBuilder jqlQuery = QueryBuilder.byClass(Product.class);
		List<CdoSnapshot> snapshots = javers.findSnapshots(jqlQuery.build());
		return javers.getJsonConverter().toJson(snapshots);
	}

	@GetMapping("/stores/{storeId}/shadows")
	public String getStoreShadows(@PathVariable int storeId) {
		Store store = storeService.findStoreById(storeId);
		JqlQuery jqlQuery = QueryBuilder.byInstance(store)
				.withChildValueObjects().build();
		List<Shadow<Store>> shadows = javers.findShadows(jqlQuery);
		return javers.getJsonConverter().toJson(shadows.get(0));
	}

	@GetMapping("/stores/snapshots")
	public String getStoresSnapshots() {
		QueryBuilder jqlQuery = QueryBuilder.byClass(Store.class);
		List<CdoSnapshot> snapshots = javers.findSnapshots(jqlQuery.build());
		return javers.getJsonConverter().toJson(snapshots);
	}
}
