package com.skillshare.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skillshare.model.MediaFile;

public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {
}