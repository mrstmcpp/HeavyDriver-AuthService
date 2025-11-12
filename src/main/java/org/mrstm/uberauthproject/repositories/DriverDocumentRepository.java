package org.mrstm.uberauthproject.repositories;

import org.mrstm.uberentityservice.models.DocumentType;
import org.mrstm.uberentityservice.models.DriverDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverDocumentRepository extends JpaRepository<DriverDocument , Long> {

    List<DriverDocument> findByDriverVerification_Driver_Id(Long driverId);

    Optional<DriverDocument> findByDriverVerificationIdAndDocumentType(Long verificationId, DocumentType documentType);
}
