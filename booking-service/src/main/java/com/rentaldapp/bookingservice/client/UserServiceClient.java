package com.rentaldapp.bookingservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "user-service", url = "${services.user.url:http://localhost:8081}")
public interface UserServiceClient {

    @GetMapping("/users/{id}")
    Map<String, Object> getUserById(@PathVariable("id") Integer id);
}