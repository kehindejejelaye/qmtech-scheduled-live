package com.qmtech.scheduledlive.repo;

import com.qmtech.scheduledlive.entity.Poll;
import com.qmtech.scheduledlive.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PollRepository extends JpaRepository<Poll, UUID> {
//    long countByIdAndOptionChosen(UUID pollId, String optionChosen);
}