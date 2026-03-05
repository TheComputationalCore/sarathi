package com.busbooking.bus_booking_system.service;

import com.busbooking.bus_booking_system.dto.response.BusResponseDTO;
import com.busbooking.bus_booking_system.entity.Bus;
import com.busbooking.bus_booking_system.repository.BusRepository;
import com.busbooking.bus_booking_system.repository.CircuitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.busbooking.bus_booking_system.IntegrationTestBase;

@ExtendWith(MockitoExtension.class)
class BusServiceTest extends IntegrationTestBase{

    @InjectMocks
    private BusService busService;

    @Mock
    private BusRepository busRepository;

    @Mock
    private CircuitRepository circuitRepository;

    @Test
    void testFindBuses_returnsDTOList_whenNoFilters() {

        Bus bus = new Bus();
        bus.setId(1L);
        bus.setFromLocation("Delhi");
        bus.setToLocation("Mumbai");
        bus.setDepartureTime(LocalDateTime.now());
        bus.setArrivalTime(LocalDateTime.now().plusHours(2));
        bus.setPrice(1200);
        bus.setTotalSeats(40);

        when(busRepository.findAllActive()).thenReturn(List.of(bus));

        List<BusResponseDTO> result =
                busService.findBuses(null, null, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Delhi", result.get(0).getFromLocation());
        assertEquals(40, result.get(0).getTotalSeats());

        verify(busRepository, times(1)).findAllActive();
    }
}
