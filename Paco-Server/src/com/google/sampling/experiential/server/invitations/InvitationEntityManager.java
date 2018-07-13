package com.google.sampling.experiential.server.invitations;

import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.common.collect.Lists;

public class InvitationEntityManager {
    private static final Logger LOG = Logger.getLogger(InvitationEntityManager.class.getName());

    public static final String KIND = "invitations";

    public static final String ADMIN_ID_PROPERTY = "admin_id";
    public static final String EXPERIMENT_ID_PROPERTY = "experiment_id";
    public static final String PARTICIPANT_ID_PROPERTY = "participant_id";
    public static final String CODE_PROPERTY = "code";
    public static final String USED_PROPERTY = "used";

    public void addInvitations(List<Invitation> invitations) {
	List<Entity> invites = Lists.newArrayList();
	for (Invitation invitation : invitations) {
	    Entity newInvite = new Entity(KIND);
	    newInvite.setProperty(ADMIN_ID_PROPERTY, invitation.getAdminId());
	    newInvite.setProperty(EXPERIMENT_ID_PROPERTY, invitation.getExperimentId());
	    newInvite.setProperty(PARTICIPANT_ID_PROPERTY, invitation.getParticipantId());
	    newInvite.setProperty(CODE_PROPERTY, invitation.getCode());
	    // don't set used_property until it is redeemed
	    invites.add(newInvite);
	}
	DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
	ds.put(invites);

    }

    public Invitation redeem(String code) {
	Query query = new Query(KIND);
	FilterPredicate filter = new FilterPredicate(CODE_PROPERTY, FilterOperator.EQUAL, code);
	query.setFilter(filter);
	DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
	Entity entity = ds.prepare(query).asSingleEntity();
	if (entity == null) {
	    return null;
	} else if (entity.getProperty(USED_PROPERTY) == null) {
	    entity.setProperty(USED_PROPERTY, true);
	    ds.put(entity);
	    return newInvitation(entity);
	} else {
	    return null;
	}
    }

    private Invitation newInvitation(Entity entity) {
	return new Invitation((int)(long)entity.getProperty(PARTICIPANT_ID_PROPERTY),
			      (Long) entity.getProperty(EXPERIMENT_ID_PROPERTY),
			      (String) entity.getProperty(ADMIN_ID_PROPERTY),
			      (String) entity.getProperty(CODE_PROPERTY));
    }

}