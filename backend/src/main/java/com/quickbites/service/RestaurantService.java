package com.quickbites.service;

import com.quickbites.dto.RestaurantResponse;
import com.quickbites.exception.NotFoundException;
import com.quickbites.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    public List<RestaurantResponse> listRestaurants() {
        return restaurantRepository.findAll().stream().map(RestaurantResponse::from).toList();
    }

    public RestaurantResponse getRestaurant(Long id) {
        return restaurantRepository.findById(id)
                .map(RestaurantResponse::from)
                .orElseThrow(() -> new NotFoundException("Restaurant " + id + " not found"));
    }
}
