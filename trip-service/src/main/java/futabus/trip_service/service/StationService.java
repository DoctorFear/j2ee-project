package futabus.trip_service.service;

import futabus.trip_service.dto.request.StationCreationRequest;
import futabus.trip_service.dto.request.StationUpdateRequest;
import futabus.trip_service.dto.response.StationResponse;
import futabus.trip_service.entity.Station;
import futabus.trip_service.enums.StationStatus;
import futabus.trip_service.mapper.StationMapper;
import futabus.trip_service.repository.StationRepository;
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
public class StationService {
    StationRepository stationRepository;
    StationMapper stationMapper;

    @Transactional
    public StationResponse create(StationCreationRequest request) {
        Station station = stationMapper.toStation(request);

        station.setStatus(StationStatus.INACTIVE);

        return stationMapper.toStationResponse(stationRepository.save(station));
    }

    @Transactional(readOnly = true)
    public List<StationResponse> findAll() {
        return stationRepository.findAll()
                .stream()
                .map(stationMapper::toStationResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StationResponse findById(Long id) {
        return stationMapper.toStationResponse(getStation(id));
    }

    @Transactional
    public StationResponse update(Long id, StationUpdateRequest request) {
        Station station = getStation(id);

        stationMapper.updateStation(request, station);

        return stationMapper.toStationResponse(stationRepository.save(station));
    }

    @Transactional
    public void delete(Long id) {
        Station station = getStation(id);
        stationRepository.delete(station);
    }

    private Station getStation(Long id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Station not found: " + id));
    }
}
