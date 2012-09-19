package com.google.sampling.experiential.model;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class ExperimentReference {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;

  @Persistent
  private Long referringId;
  
  @Persistent
  private Long referredId;

  public ExperimentReference(Long referringExperimentId, Long referencedExperimentId) {
    this.referringId = referringExperimentId;
    this.referredId = referencedExperimentId;
  }

  public Long getReferringExperimentId() {
    return referringId;  
  }
  
  public Long getReferencedExperimentId() {
    return referredId;
  }

}
