package com.booking.project.repository.inteface;

import com.booking.project.model.Accommodation;
import com.booking.project.model.CommentAboutAcc;
import com.booking.project.model.enums.AccommodationApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

public interface ICommentAboutAccRepository extends JpaRepository<CommentAboutAcc,Long> {
    Collection<CommentAboutAcc> findAllByAccommodation(Optional<Accommodation> accommodation);
    @Query("select c from CommentAboutAcc c where c.isReported = true")
    Collection<CommentAboutAcc> findByReportedTrue();
    @Query("select distinct a " +
            "from CommentAboutAcc  c " +
            "join c.accommodation a " +
            "where a.accommodationApprovalStatus= :approved ")
    Collection<Accommodation> findAccomodationByRating(@Param("approved") AccommodationApprovalStatus approved);
}
