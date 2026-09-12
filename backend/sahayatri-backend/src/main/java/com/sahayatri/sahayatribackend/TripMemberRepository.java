package com.sahayatri.sahayatribackend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripMemberRepository extends JpaRepository<TripMember, Long> {

    List<TripMember> findByGroupId(Long groupId);

    List<TripMember> findByGroupIdOrderByIdAsc(Long groupId);
}
