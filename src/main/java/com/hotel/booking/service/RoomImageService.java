package com.hotel.booking.service;

import com.hotel.booking.entity.RoomCategory;
import com.hotel.booking.entity.RoomImage;
import com.hotel.booking.repository.RoomImageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Service class for managing room images in the hotel booking system.
 * <p>
 * This service provides functionality to upload, store, delete, and manage images associated with room categories.
 * It handles both file system operations (creating, deleting image files) and database persistence through the repository.
 * Ensures data integrity by maintaining only one primary image per room category.
 * </p>
 *
 * @author Artur Derr
 */
@Service
public class RoomImageService {

    private static final String WEB_PATH_PREFIX = "/images/rooms/";

    private final RoomImageRepository roomImageRepository;
    private final Path imageDirectory;

    /**
     * Constructs a new RoomImageService with the specified dependencies.
     *
     * @param roomImageRepository the repository for accessing room image data
     * @param imagesRootDir the root directory for storing images, configured via application properties
     *                      (defaults to "data/images" if not specified)
     */
    public RoomImageService(RoomImageRepository roomImageRepository,
                            @Value("${app.images.root-dir:data/images}") String imagesRootDir) {
        this.roomImageRepository = roomImageRepository;
        this.imageDirectory = Paths.get(imagesRootDir, "rooms");
    }

    /**
     * Ensures that the image directory exists on the file system.
     * Creates the directory structure if it does not already exist.
     *
     * @throws IOException if an error occurs while creating the directory
     */
    public void ensureImageDirectoryExists() throws IOException {
        Files.createDirectories(imageDirectory);
    }

    /**
     * Creates a target file for storing an uploaded image with a unique name.
     * <p>
     * Sanitizes the original file name and appends a UUID prefix to ensure uniqueness.
     * Automatically ensures the image directory exists before creating the file.
     * </p>
     *
     * @param originalFileName the original name of the uploaded file
     * @return a File object representing the target location for the image
     * @throws IllegalStateException if the image directory cannot be created
     */
    public File createTargetFile(String originalFileName) {
        try {
            ensureImageDirectoryExists();
        } catch (IOException e) {
            throw new IllegalStateException("Error creating image directory: " + e.getMessage(), e);
        }

        String cleanFileName = sanitizeFileName(originalFileName);
        String uniqueFileName = UUID.randomUUID() + "_" + cleanFileName;
        return imageDirectory.resolve(uniqueFileName).toFile();
    }

    /**
     * Creates and persists a new room image record in the database.
     * <p>
     * Constructs the web path by combining the web path prefix with the stored file name,
     * and saves the image entity with both the original file name as title and the web-accessible path.
     * </p>
     *
     * @param originalFileName the original name of the uploaded file to use as the image title
     * @param storedFileName the name of the file as stored on the file system
     * @return the created and persisted RoomImage entity
     */
    @Transactional
    public RoomImage createAndSaveUploadedImage(String originalFileName, String storedFileName) {
        String webPath = WEB_PATH_PREFIX + storedFileName;

        RoomImage roomImage = new RoomImage(null);
        roomImage.setImagePath(webPath);
        roomImage.setTitle(originalFileName);

        return roomImageRepository.save(roomImage);
    }

    /**
     * Assigns an image to a room category.
     * <p>
     * Associates the given room image with the specified room category and persists the changes.
     * </p>
     *
     * @param roomImage the room image to assign to a category
     * @param category the room category to assign the image to
     * @return the updated RoomImage entity
     * @throws NullPointerException if roomImage is null
     */
    @Transactional
    public RoomImage assignImageToCategory(RoomImage roomImage, RoomCategory category) {
        Objects.requireNonNull(roomImage, "roomImage must not be null");
        roomImage.setCategory(category);
        return roomImageRepository.save(roomImage);
    }

    /**
     * Updates an existing room image record.
     * <p>
     * Persists changes to the image and ensures that only one primary image exists per room category.
     * If the image is marked as primary, any other primary images in the same category will be unmarked.
     * </p>
     *
     * @param updatedImage the room image with updated information
     * @return the updated and persisted RoomImage entity
     * @throws NullPointerException if updatedImage is null
     */
    @Transactional
    public RoomImage updateImage(RoomImage updatedImage) {
        Objects.requireNonNull(updatedImage, "updatedImage must not be null");
        enforceSinglePrimaryPerCategory(updatedImage);
        return roomImageRepository.save(updatedImage);
    }

    /**
     * Deletes a room image from both the database and the file system.
     * <p>
     * Removes the image file from disk if it exists, then deletes the corresponding database record.
     * If the file cannot be found on disk, the database record is still deleted.
     * </p>
     *
     * @param roomImage the room image to delete
     * @throws NullPointerException if roomImage is null
     * @throws IllegalStateException if an error occurs while deleting the file from disk
     */
    @Transactional
    public void deleteImage(RoomImage roomImage) {
        Objects.requireNonNull(roomImage, "roomImage must not be null");

        Path diskPath = resolveDiskPath(roomImage.getImagePath());
        if (diskPath != null) {
            try {
                Files.deleteIfExists(diskPath);
            } catch (IOException e) {
                throw new IllegalStateException("Could not delete file from disk", e);
            }
        }

        roomImageRepository.delete(roomImage);
    }

    /**
     * Retrieves all room images from the database with their associated categories.
     * <p>
     * Efficiently fetches all images with their related category information in a single query.
     * </p>
     *
     * @return a list of all RoomImage entities with their categories loaded
     */
    public List<RoomImage> findAllWithCategory() {
        return roomImageRepository.findAllWithCategory();
    }

    /**
     * Enforces the constraint that only one primary image exists per room category.
     * <p>
     * If the updated image is marked as primary, this method unmarks all other primary images
     * in the same category. If the category is null or invalid, the image is unmarked as primary.
     * Changes are persisted to the database.
     * </p>
     *
     * @param updatedImage the image being updated, which may be marked as primary
     */
    private void enforceSinglePrimaryPerCategory(RoomImage updatedImage) {
        if (!Boolean.TRUE.equals(updatedImage.getIsPrimary())) {
            return;
        }

        RoomCategory category = updatedImage.getCategory();
        if (category == null || category.getCategory_id() == null) {
            updatedImage.setIsPrimary(false);
            return;
        }

        Long categoryId = category.getCategory_id();
        List<RoomImage> primaries = roomImageRepository.findPrimaryByCategoryId(categoryId);
        for (RoomImage other : primaries) {
            if (updatedImage.getId() != null && updatedImage.getId().equals(other.getId())) {
                continue;
            }
            other.setIsPrimary(false);
        }
        roomImageRepository.saveAll(primaries);
    }

    /**
     * Sanitizes a file name by removing or replacing illegal file system characters.
     * <p>
     * Replaces characters that are not allowed in file names (such as backslash, slash, colon, asterisk, etc.)
     * with underscores. Returns a default name if the input is null or blank.
     * </p>
     *
     * @param originalFileName the original file name to sanitize
     * @return the sanitized file name, or "upload" if the input is null or blank
     */
    private static String sanitizeFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            return "upload";
        }
        return originalFileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    /**
     * Resolves the disk file path from a web path.
     * <p>
     * Extracts the file name from the web path and resolves it within the image directory.
     * Returns null if the web path is null, blank, or does not contain a valid file name.
     * </p>
     *
     * @param imagePath the web path of the image (e.g., "/images/rooms/filename.jpg")
     * @return the resolved Path to the image file on disk, or null if the path is invalid
     */
    private Path resolveDiskPath(String imagePath) {
        String fileName = extractFileName(imagePath);
        if (fileName == null || fileName.isBlank()) {
            return null;
        }
        return imageDirectory.resolve(fileName);
    }

    /**
     * Extracts the file name from a web path.
     * <p>
     * Parses the web path and returns the file name portion after the last forward slash.
     * Returns null if the path is null, blank, or ends with a slash.
     * If the path contains no slashes, returns the entire path as the file name.
     * </p>
     *
     * @param imagePath the web path to extract the file name from
     * @return the file name, or null if the path is invalid
     */
    private static String extractFileName(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) {
            return null;
        }
        int lastSlash = imagePath.lastIndexOf('/');
        if (lastSlash < 0) {
            return imagePath;
        }
        if (lastSlash == imagePath.length() - 1) {
            return null;
        }
        return imagePath.substring(lastSlash + 1);
    }
}
