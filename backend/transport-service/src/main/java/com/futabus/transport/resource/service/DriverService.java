package com.futabus.transport.resource.service;

import com.futabus.transport.common.exception.BusinessException;
import com.futabus.transport.resource.dto.request.DriverCreationRequest;
import com.futabus.transport.resource.dto.request.DriverUpdateRequest;
import com.futabus.transport.resource.dto.response.DriverResponse;
import com.futabus.transport.resource.entity.Driver;
import com.futabus.transport.resource.enums.DriverStatus;
import com.futabus.transport.resource.exception.ResourceErrorCode;
import com.futabus.transport.resource.mapper.DriverMapper;
import com.futabus.transport.resource.repository.DriverRepository;
import com.futabus.transport.trip.repository.TripDriverRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DriverService {
    DriverRepository driverRepository;
    TripDriverRepository tripDriverRepository;
    DriverMapper driverMapper;

    @Transactional
    public DriverResponse create(DriverCreationRequest request) {
        Driver driver = driverMapper.toDriver(request);

        driver.setStatus(DriverStatus.ACTIVE);
        Driver savedDriver = driverRepository.save(driver);

        return driverMapper.toDriverResponse(savedDriver);
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> findAll() {
        return findAll(null);
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> findAll(DriverStatus status) {
        var stream = status != null
                ? driverRepository.findByStatus(status).stream()
                : driverRepository.findAll().stream();

        return stream.map(driverMapper::toDriverResponse).toList();
    }

    @Transactional(readOnly = true)
    public DriverResponse findById(Long id) {
        Driver driver = getDriver(id);

        return driverMapper.toDriverResponse(driver);
    }

    @Transactional
    public DriverResponse update(Long id, DriverUpdateRequest request) {
        Driver driver = driverRepository.findLockedById(id)
                .orElseThrow(() -> new BusinessException(ResourceErrorCode.TRANSPORT_DRIVER_NOT_FOUND));
        driverMapper.updateDriver(request, driver);
        return driverMapper.toDriverResponse(driver);
    }

    @Transactional
    public void delete(Long id) {
        Driver driver = driverRepository.findLockedById(id)
                .orElseThrow(() -> new BusinessException(ResourceErrorCode.TRANSPORT_DRIVER_NOT_FOUND));

        if (tripDriverRepository.existsByDriverId(id)) {
            throw new BusinessException(ResourceErrorCode.TRANSPORT_DRIVER_IN_USE);
        }
        driverRepository.delete(driver);
    }

    private Driver getDriver(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResourceErrorCode.TRANSPORT_DRIVER_NOT_FOUND));
    }
}
