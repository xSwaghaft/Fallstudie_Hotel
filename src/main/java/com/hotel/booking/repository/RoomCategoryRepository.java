package com.hotel.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.google.common.base.Optional;
import com.hotel.booking.entity.Room;
import com.hotel.booking.entity.RoomCategory;
//Matthias Lohr
@Repository
public interface RoomCategoryRepository extends JpaRepository<RoomCategory, Long> {

    //Sucht eine 
    Optional<RoomCategory> findByName(String name);

    List<Room> findByCategoryIn(List<RoomCategory> categories);
    
}