package com.google.sampling.experiential.server.invitations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class InvitationGeneratorTest {

  @Test
      public void testGenerateCodeForExperiment() {
      Long experimentId = new Long("4608527852634113");
      Integer participantId = 1;
      final InvitationGenerator invitationGenerator = new InvitationGenerator();
      final String actualCode = invitationGenerator.generateCode(experimentId, participantId);

      assertNotNull(actualCode);
      assertEquals("59xw-07wf-1wzj-7l", actualCode);

      Invitation reversed = invitationGenerator.decode(actualCode);
      assertEquals(participantId, reversed.getParticipantId());
      assertEquals(experimentId, reversed.getExperimentId());
  }

}

