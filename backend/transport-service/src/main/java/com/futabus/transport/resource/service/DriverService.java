package com.futabus.transport.resource.service;

import com.futabus.transport.resource.dto.request.DriverCreationRequest;
import com.futabus.transport.resource.dto.response.DriverResponse;
import com.futabus.transport.resource.entity.Driver;
import com.futabus.transport.resource.enums.DriverStatus;
import com.futabus.transport.resource.mapper.DriverMapper;
import com.futabus.transport.resource.repository.DriverRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DriverService {
    DriverRepository driverRepository;
    DriverMapper driverMapper;

    @Transactional
    public DriverResponse create(DriverCreationRequest request) {
        Driver driver = driverMapper.toDriver(request);

        driver.setStatus(DriverStatus.INACTIVE);

        return driverMapper.toDriverResponse(driverRepository.save(driver));
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> findAll() {
        return driverRepository.findAll()
                .stream()
                .map(driverMapper::toDriverResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public DriverResponse findById(Long id) {
        return driverMapper.toDriverResponse(getDriver(id));
    }

    @Transactional
    public void delete(Long id) {
        Driver driver = getDriver(id);
        driverRepository.delete(driver);
    }

    private Driver getDriver(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Driver not found: " + id));
    }
}
