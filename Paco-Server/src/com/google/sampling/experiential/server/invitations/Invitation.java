package com.google.sampling.experiential.server.invitations;

public class Invitation {

    private Integer participantId = 0;
    private Long experimentId = 0l;
    private String adminId;
    private String code;


    public Invitation(Integer participantId, Long experimentId, String adminId, String code) {
	super();
	if (participantId == null || experimentId == null) {
	    throw new IllegalArgumentException("Invalid code");
	}
	this.participantId = participantId;
	this.experimentId = experimentId;
	this.adminId = adminId;
	this.code = code;
    }

    public Integer getParticipantId() {
	return participantId ;
    }

    public Long getExperimentId() {
	return experimentId;
    }

    public String getAdminId() {
	return adminId;
    }

    public String getCode() {
	return code;
    }

}