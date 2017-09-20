package com.google.sampling.experiential.server.migration.mr.eventctr;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.User;
import com.google.appengine.tools.mapreduce.MapReduceJob;
import com.google.appengine.tools.mapreduce.MapReduceSettings;
import com.google.appengine.tools.mapreduce.MapReduceSpecification;
import com.google.appengine.tools.mapreduce.Mapper;
import com.google.appengine.tools.mapreduce.Marshaller;
import com.google.appengine.tools.mapreduce.Marshallers;
import com.google.appengine.tools.mapreduce.inputs.DatastoreInput;
import com.google.appengine.tools.mapreduce.outputs.DatastoreOutput;
import com.google.sampling.experiential.server.AuthUtil;

public class EventCounterMRServlet extends HttpServlet {

  private static final long serialVersionUID = 1L;

  public static final Logger log = Logger.getLogger(EventCounterMRServlet.class.getName());

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String queue = "mapreduce-workers";
    String module = "mapreduce";
    String bucket = "default";
    User user = AuthUtil.getWhoFromLogin();
    if (user == null) {
      AuthUtil.redirectUserToLogin(req, resp);
    } else if (AuthUtil.isUserAdmin()){
      MapReduceSpecification<Entity, String, Integer, Entity, Void> mapReduceSpec = createMapReduceSpec();
      MapReduceSettings settings = getSettings(bucket, queue, module);
  
      String id = MapReduceJob.start(mapReduceSpec, settings);
  
      resp.sendRedirect("/_ah/pipeline/status.html?root=" + id);
    } else {
      resp.sendError(403);
    }
  }

  public static MapReduceSpecification<Entity, String, Integer, Entity, Void> createMapReduceSpec() {
    Mapper<Entity, String, Integer> mapper = new EventCounterMapper();
    EventCounterReducer reducer = new EventCounterReducer();

    Marshaller<String> intermediateKeyMarshaller = Marshallers.getStringMarshaller();
    Marshaller<Integer> intermediateValueMarshaller = Marshallers.getSerializationMarshaller();


    Query query = new Query("Event");
    DatastoreInput input = new DatastoreInput(query, 100);

    MapReduceSpecification<Entity,
      String,
      Integer,
      Entity,
      Void> spec =
            new MapReduceSpecification.Builder<Entity,
            String,
            Integer,
            Entity,
            Void>(input,
                    mapper,
                    reducer,
                    new DatastoreOutput())
      .setKeyMarshaller(intermediateKeyMarshaller)
      .setValueMarshaller(intermediateValueMarshaller)
      .setJobName("EventCounter")
      .setNumReducers(1)
      .build();
    return spec;
  }

  public static MapReduceSettings getSettings(String bucket, String queue, String module) {
    return new MapReduceSettings.Builder()
      //.setBucketName(bucket)
      .setWorkerQueueName(queue)
      .setModule(module)
      .build();
  }

}
