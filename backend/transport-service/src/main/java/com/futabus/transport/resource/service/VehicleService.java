package com.futabus.transport.resource.service;

import com.futabus.transport.common.exception.BusinessException;
import com.futabus.transport.resource.dto.request.VehicleCreationRequest;
import com.futabus.transport.resource.dto.request.VehicleUpdateRequest;
import com.futabus.transport.resource.dto.response.VehicleResponse;
import com.futabus.transport.resource.entity.Vehicle;
import com.futabus.transport.resource.enums.ServiceType;
import com.futabus.transport.resource.enums.VehicleStatus;
import com.futabus.transport.resource.exception.ResourceErrorCode;
import com.futabus.transport.resource.mapper.VehicleMapper;
import com.futabus.transport.resource.repository.VehicleRepository;
import com.futabus.transport.trip.repository.TripRepository;
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
    TripRepository tripRepository;
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
        return findAll(null, null);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> findAll(VehicleStatus status, ServiceType type) {
        List<Vehicle> vehicles;
        if (status != null && type != null) {
            vehicles = vehicleRepository.findByStatusAndType(status, type);
        } else if (status != null) {
            vehicles = vehicleRepository.findByStatus(status);
        } else if (type != null) {
            vehicles = vehicleRepository.findByType(type);
        } else {
            vehicles = vehicleRepository.findAll();
        }

        return vehicles.stream()
                .map(vehicleMapper::toVehicleResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VehicleResponse findById(Long id) {
        Vehicle vehicle = getVehicle(id);

        return vehicleMapper.toVehicleResponse(vehicle);
    }

    @Transactional
    public VehicleResponse update(Long id, VehicleUpdateRequest request) {
        Vehicle vehicle = vehicleRepository.findLockedById(id)
                .orElseThrow(() -> new BusinessException(ResourceErrorCode.TRANSPORT_VEHICLE_NOT_FOUND));

        vehicleMapper.updateVehicle(request, vehicle);

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
        Vehicle vehicle = vehicleRepository.findLockedById(id)
                .orElseThrow(() -> new BusinessException(ResourceErrorCode.TRANSPORT_VEHICLE_NOT_FOUND));

        if (tripRepository.existsByVehicleId(id)) {
            throw new BusinessException(ResourceErrorCode.TRANSPORT_VEHICLE_IN_USE);
        }
        vehicleRepository.delete(vehicle);
    }

    private Vehicle getVehicle(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResourceErrorCode.TRANSPORT_VEHICLE_NOT_FOUND));
    }
}
