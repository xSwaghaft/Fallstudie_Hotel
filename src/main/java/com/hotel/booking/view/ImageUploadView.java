package com.hotel.booking.view;

import com.hotel.booking.entity.UserRole;
import com.hotel.booking.security.SessionService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Route(value = "upload-images", layout = MainLayout.class)
@PageTitle("Image Upload")
@CssImport("./themes/hotel/styles.css")
public class ImageUploadView extends VerticalLayout implements BeforeEnterObserver {

    private final SessionService sessionService;
    private VerticalLayout uploadedFilesList;
    private final List<UploadedFileInfo> uploadedFiles;
    
    private static final String IMAGE_DIRECTORY = "src/main/resources/static/images";
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    public ImageUploadView(SessionService sessionService) {
        this.sessionService = sessionService;
        this.uploadedFiles = new ArrayList<>();
        
        setSpacing(true);
        setPadding(true);
        setWidthFull();
        
        add(createHeader(), createUploadSection(), createUploadedFilesSection());
    }

    private Component createHeader() {
        H1 title = new H1("Image Upload");
        Paragraph description = new Paragraph("Upload images to the images directory");
        VerticalLayout header = new VerticalLayout(title, description);
        header.setSpacing(false);
        header.setPadding(false);
        return header;
    }

    private Component createUploadSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(true);
        section.addClassName("upload-section");
        
        // Create upload directory if it doesn't exist
        Path imagePath = Paths.get(IMAGE_DIRECTORY);
        try {
            Files.createDirectories(imagePath);
        } catch (IOException e) {
            Notification.show("Error creating image directory: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
        
        // Use MemoryBuffer to receive uploads
        MemoryBuffer buffer = new MemoryBuffer();
        
        // Create Upload component
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp");
        upload.setMaxFiles(10);
        upload.setDropAllowed(true);
        
        // Handle successful upload
        upload.addSucceededListener(event -> {
            String fileName = event.getFileName();
            String sanitizedFileName = sanitizeFileName(fileName);
            
            try {
                // Get file data from buffer
                InputStream inputStream = buffer.getInputStream();
                
                // Create target file
                File targetFile = new File(IMAGE_DIRECTORY, sanitizedFileName);
                
                // Copy file to target location
                try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
                    byte[] bufferBytes = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(bufferBytes)) != -1) {
                        outputStream.write(bufferBytes, 0, bytesRead);
                    }
                }
                
                // Get file info
                long fileSize = targetFile.length();
                String mimeType = event.getMIMEType();
                
                // Create relative path for web access
                String relativePath = "/images/" + sanitizedFileName;
                
                // Store file info
                UploadedFileInfo fileInfo = new UploadedFileInfo(
                    fileName,
                    relativePath,
                    targetFile.getAbsolutePath(),
                    mimeType,
                    fileSize
                );
                uploadedFiles.add(fileInfo);
                
                // Update UI
                updateUploadedFilesList();
                
                Notification.show("File uploaded successfully: " + fileName, 3000, Notification.Position.BOTTOM_START);
            } catch (IOException e) {
                Notification.show("Error saving file: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
            }
        });
        
        // Handle upload errors
        upload.addFileRejectedListener(event -> {
            Notification.show("File rejected: " + event.getErrorMessage(), 5000, Notification.Position.MIDDLE);
        });
        
        Paragraph info = new Paragraph("Accepted formats: JPEG, PNG, GIF, WebP. Max file size: 10 MB");
        info.getStyle().set("font-size", "var(--lumo-font-size-s)");
        info.getStyle().set("color", "var(--lumo-secondary-text-color)");
        
        section.add(upload, info);
        return section;
    }

    private Component createUploadedFilesSection() {
        VerticalLayout section = new VerticalLayout();
        section.setSpacing(true);
        section.setPadding(true);
        section.setWidthFull();
        section.addClassName("uploaded-files-section");
        
        H1 sectionTitle = new H1("Uploaded Files");
        sectionTitle.getStyle().set("font-size", "var(--lumo-font-size-l)");
        
        uploadedFilesList = new VerticalLayout();
        uploadedFilesList.setSpacing(true);
        uploadedFilesList.setWidthFull();
        
        section.add(sectionTitle, uploadedFilesList);
        return section;
    }

    private void updateUploadedFilesList() {
        uploadedFilesList.removeAll();
        
        if (uploadedFiles.isEmpty()) {
            Paragraph emptyMessage = new Paragraph("No files uploaded yet.");
            emptyMessage.getStyle().set("color", "var(--lumo-secondary-text-color)");
            uploadedFilesList.add(emptyMessage);
            return;
        }
        
        for (UploadedFileInfo fileInfo : uploadedFiles) {
            Div fileCard = createFileCard(fileInfo);
            uploadedFilesList.add(fileCard);
        }
    }

    private Div createFileCard(UploadedFileInfo fileInfo) {
        Div card = new Div();
        card.addClassName("uploaded-file-card");
        card.getStyle().set("padding", "var(--lumo-space-m)");
        card.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)");
        card.getStyle().set("border-radius", "var(--lumo-border-radius-m)");
        card.getStyle().set("margin-bottom", "var(--lumo-space-s)");
        
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);
        layout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
        layout.setWidthFull();
        
        // Image preview
        Image preview = new Image(fileInfo.getRelativePath(), fileInfo.getFileName());
        preview.setWidth("100px");
        preview.setHeight("100px");
        preview.getStyle().set("object-fit", "cover");
        preview.getStyle().set("border-radius", "var(--lumo-border-radius-s)");
        
        // File info
        VerticalLayout info = new VerticalLayout();
        info.setSpacing(false);
        info.setPadding(false);
        
        Paragraph fileName = new Paragraph(fileInfo.getFileName());
        fileName.getStyle().set("font-weight", "bold");
        fileName.getStyle().set("margin", "0");
        
        Paragraph filePath = new Paragraph("Path: " + fileInfo.getRelativePath());
        filePath.getStyle().set("font-size", "var(--lumo-font-size-s)");
        filePath.getStyle().set("color", "var(--lumo-secondary-text-color)");
        filePath.getStyle().set("margin", "0");
        
        Paragraph fileSize = new Paragraph("Size: " + formatFileSize(fileInfo.getFileSize()));
        fileSize.getStyle().set("font-size", "var(--lumo-font-size-s)");
        fileSize.getStyle().set("color", "var(--lumo-secondary-text-color)");
        fileSize.getStyle().set("margin", "0");
        
        info.add(fileName, filePath, fileSize);
        
        layout.add(preview, info);
        layout.setFlexGrow(1, info);
        
        card.add(layout);
        return card;
    }

    private String sanitizeFileName(String fileName) {
        // Remove path separators and replace with underscore
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!sessionService.isLoggedIn() || 
            !sessionService.hasAnyRole(UserRole.RECEPTIONIST, UserRole.MANAGER)) {
            event.rerouteTo(LoginView.class);
        }
    }

    // Inner class to store uploaded file information
    private static class UploadedFileInfo implements Serializable {
        private final String fileName;
        private final String relativePath;
        private final String absolutePath;
        private final String mimeType;
        private final long fileSize;

        public UploadedFileInfo(String fileName, String relativePath, String absolutePath, 
                              String mimeType, long fileSize) {
            this.fileName = fileName;
            this.relativePath = relativePath;
            this.absolutePath = absolutePath;
            this.mimeType = mimeType;
            this.fileSize = fileSize;
        }

        public String getFileName() {
            return fileName;
        }

        public String getRelativePath() {
            return relativePath;
        }

        public String getAbsolutePath() {
            return absolutePath;
        }

        public String getMimeType() {
            return mimeType;
        }

        public long getFileSize() {
            return fileSize;
        }
    }
}
