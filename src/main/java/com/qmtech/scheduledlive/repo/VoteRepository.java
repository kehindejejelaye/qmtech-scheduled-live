package com.qmtech.scheduledlive.repo;

import com.qmtech.scheduledlive.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VoteRepository extends JpaRepository<Vote, UUID> {
    @Query("SELECT v.optionChosen, COUNT(v) FROM Vote v WHERE v.poll.id = :pollId GROUP BY v.optionChosen")
    List<Object[]> countVotesByOption(@Param("pollId") UUID pollId);
}
