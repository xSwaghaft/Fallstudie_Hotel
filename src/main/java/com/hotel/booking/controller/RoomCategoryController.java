package com.hotel.booking.controller;

import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.service.RoomCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/room-categories")
public class RoomCategoryController {

    private final RoomCategoryService roomCategoryService;

    @Autowired
    public RoomCategoryController(RoomCategoryService roomCategoryService) {
        this.roomCategoryService = roomCategoryService;
    }

    @GetMapping
    public List<RoomCategory> getAllRoomCategories() {
        return roomCategoryService.getAllRoomCategories();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomCategory> getRoomCategoryById(@PathVariable Long id) {
        Optional<RoomCategory> roomCategory = roomCategoryService.getRoomCategoryById(id);
        return roomCategory.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public RoomCategory createRoomCategory(@RequestBody RoomCategory roomCategory) {
        return roomCategoryService.saveRoomCategory(roomCategory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomCategory> updateRoomCategory(@PathVariable Long id, @RequestBody RoomCategory updatedRoomCategory) {
        if (roomCategoryService.getRoomCategoryById(id).isPresent()) {
            updatedRoomCategory.setCategory_id(id);
            return ResponseEntity.ok(roomCategoryService.saveRoomCategory(updatedRoomCategory));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoomCategory(@PathVariable Long id) {
        if (roomCategoryService.getRoomCategoryById(id).isPresent()) {
            roomCategoryService.deleteRoomCategory(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}