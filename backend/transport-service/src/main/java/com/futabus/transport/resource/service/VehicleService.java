package com.futabus.transport.resource.service;

import com.futabus.transport.common.exception.BusinessException;
import com.futabus.transport.resource.dto.request.VehicleCreationRequest;
import com.futabus.transport.resource.dto.response.VehicleResponse;
import com.futabus.transport.resource.entity.Vehicle;
import com.futabus.transport.resource.enums.VehicleStatus;
import com.futabus.transport.resource.exception.ResourceErrorCode;
import com.futabus.transport.resource.mapper.VehicleMapper;
import com.futabus.transport.resource.repository.VehicleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VehicleService {
    VehicleRepository vehicleRepository;
    VehicleMapper vehicleMapper;

    @Transactional
    public VehicleResponse create(VehicleCreationRequest request) {
        Vehicle vehicle = vehicleMapper.toVehicle(request);

        vehicle.setStatus(VehicleStatus.ACTIVE);
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        return vehicleMapper.toVehicleResponse(savedVehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> findAll() {
        return vehicleRepository.findAll()
                .stream()
                .map(vehicleMapper::toVehicleResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VehicleResponse findById(Long id) {
        Vehicle vehicle = getVehicle(id);

        return vehicleMapper.toVehicleResponse(vehicle);
    }

    @Transactional
    public VehicleResponse changeStatus(Long id, VehicleStatus status) {
        Vehicle vehicle = vehicleRepository.findLockedById(id)
                .orElseThrow(() -> new BusinessException(ResourceErrorCode.TRANSPORT_VEHICLE_NOT_FOUND));
        vehicle.setStatus(status);
        return vehicleMapper.toVehicleResponse(vehicle);
    }

    @Transactional
    public void delete(Long id) {
        Vehicle vehicle = getVehicle(id);
        vehicleRepository.delete(vehicle);
    }

    private Vehicle getVehicle(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResourceErrorCode.TRANSPORT_VEHICLE_NOT_FOUND));
    }
}
