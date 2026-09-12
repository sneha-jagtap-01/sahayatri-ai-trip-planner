package com.sahayatri.sahayatribackend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripExpenseRepository extends JpaRepository<TripExpense, Long> {

    List<TripExpense> findByGroupIdOrderByCreatedAtAsc(Long groupId);

    boolean existsByPaidByMemberId(Long memberId);
}