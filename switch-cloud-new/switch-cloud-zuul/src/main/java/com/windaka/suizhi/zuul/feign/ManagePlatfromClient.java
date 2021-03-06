package com.windaka.suizhi.zuul.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Set;

@FeignClient("sjwl-plat")
public interface ManagePlatfromClient {

	@GetMapping("/oss-plat-anon/internal/blackIPs")
	Set<String> findAllBlackIPs(@RequestParam("params") Map<String, Object> params);
}
