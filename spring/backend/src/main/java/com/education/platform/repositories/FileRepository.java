package com.education.platform.repositories;

import com.education.platform.entities.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByDriveId(Long driveId);
    List<File> findByNameContainingIgnoreCase(String name);
    List<File> findByDrive_IdAndNameContainingIgnoreCase(Long driveId, String keyword);

}
