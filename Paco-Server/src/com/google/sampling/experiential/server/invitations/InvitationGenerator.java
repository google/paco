package com.google.sampling.experiential.server.invitations;

import java.math.BigInteger;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

public class InvitationGenerator {

    private static final int PARTICIPANT_ID_DIGITS_MAX = 4;
    private static final String START_CODE = "9";

    // convert an encoded tuple of numbers in a shorter code for users
    // 9ppppeeeeeeeeeeeee 9 start + 4 digits for participant (p) + experimentlong id (e)
    //
    // ruby version:
    //      %> 90001555123567890.to_s(36).scan(/.{1,4}/).join("-")
    //      => "om6v-zmta-51u"
    String generateCode(Long experimentId, int participantId) {
	BigInteger combined = new BigInteger(START_CODE + pad(participantId) + experimentId.toString());
	final String combinedStringInBase36 = combined.toString(36);
	return formatInGroupsOf4(combinedStringInBase36);
    }

    public List<Invitation> generateNInvitations(String adminId, Long experimentId, int numberOfParticipants, int startOffset) {
	List<Invitation> codes = Lists.newArrayList();
	for (int i=startOffset; i < startOffset + numberOfParticipants; i++) {
	    codes.add(new Invitation(i, experimentId, adminId, generateCode(experimentId, i)));
	}
	return codes;
    }

    private String formatInGroupsOf4(final String combinedString) {
	return Joiner.on('-').join(Splitter.fixedLength(4).split(combinedString));
    }

    private String pad(int participantId) {
	return String.format("%0" + PARTICIPANT_ID_DIGITS_MAX + "d", participantId);
    }

    public Invitation decode(String actualCode) {
	if (actualCode != null) {
	    String base36Number = actualCode.replaceAll("-", "");
	    BigInteger combined = new BigInteger(base36Number, 36);
	    String combinedString = combined.toString();
	    String participantId = combinedString.substring(1, PARTICIPANT_ID_DIGITS_MAX + 1);
	    String experimentId = combinedString.substring(PARTICIPANT_ID_DIGITS_MAX + 1);
	    int pId = Integer.parseInt(participantId);
	    long eId = Long.parseLong(experimentId);
	    return new Invitation(pId, eId, "bob", base36Number);
	}
	return null;
    }

}
