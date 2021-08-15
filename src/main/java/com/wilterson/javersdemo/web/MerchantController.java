package com.wilterson.javersdemo.web;

import com.wilterson.javersdemo.domain.Merchant;
import com.wilterson.javersdemo.service.MerchantService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class MerchantController {

	private final MerchantService merchantService;

	public MerchantController(MerchantService merchantService) {
		this.merchantService = merchantService;
	}

	@PostMapping("/merchants")
	public ResponseEntity<Merchant> createMerchant(@RequestBody Merchant merchant) {
		return ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(merchantService.createMerchant(merchant));
	}

	@PutMapping("/merchants/{merchantId}/live")
	public void checkIn(@PathVariable Integer merchantId) {
		merchantService.checkIn(merchantId);
	}

	@PutMapping("/merchants/{merchantId}")
	public void updateMerchant(@PathVariable Integer merchantId, @RequestBody Merchant updatedMerchant) {
		merchantService.update(merchantId, updatedMerchant);
	}

	@PutMapping("/merchants/{merchantId}/config")
	public ResponseEntity<Merchant> checkOut(@PathVariable Integer merchantId) {
		return ResponseEntity
				.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(merchantService.checkOut(merchantId));
	}
}
