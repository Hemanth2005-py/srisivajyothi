package com.mymart.service;

import com.mymart.dto.BrandRequest;
import com.mymart.exception.BadRequestException;
import com.mymart.exception.ResourceNotFoundException;
import com.mymart.model.Brand;
import com.mymart.repository.BrandRepository;
import com.mymart.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    public List<Brand> getAllActiveBrands() {
        return brandRepository.findByActiveTrueOrderByNameAsc();
    }

    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    public Brand getBrandById(String id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));
    }

    public Brand createBrand(BrandRequest request) {
        if (brandRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new BadRequestException("Brand already exists with name: " + request.getName());
        }

        Brand brand = Brand.builder()
                .name(request.getName().trim())
                .logoUrl(request.getLogoUrl())
                .description(request.getDescription())
                .active(request.isActive())
                .createdAt(Instant.now())
                .build();

        return brandRepository.save(brand);
    }

    public Brand updateBrand(String id, BrandRequest request) {
        Brand brand = getBrandById(id);

        if (!brand.getName().equalsIgnoreCase(request.getName().trim()) &&
                brandRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new BadRequestException("Brand already exists with name: " + request.getName());
        }

        brand.setName(request.getName().trim());
        brand.setLogoUrl(request.getLogoUrl());
        brand.setDescription(request.getDescription());
        brand.setActive(request.isActive());

        return brandRepository.save(brand);
    }

    public void deleteBrand(String id) {
        Brand brand = getBrandById(id);
        long productCount = productRepository.findByBrandId(id).size();
        if (productCount > 0) {
            brand.setActive(false);
            brandRepository.save(brand);
            log.info("Deactivated brand {} because it has {} associated products", id, productCount);
        } else {
            brandRepository.delete(brand);
            log.info("Deleted empty brand {}", id);
        }
    }
}
