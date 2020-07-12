package com.thanhtam.backend.repository;

import com.thanhtam.backend.entity.Exam;
import com.thanhtam.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    List<Exam> findAllByPart_Course_Id(Long courseId);
    public Page<Exam> findAll(Pageable pageable);

}
