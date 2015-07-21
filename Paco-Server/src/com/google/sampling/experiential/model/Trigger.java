package com.google.sampling.experiential.model;

import java.io.Serializable;
import java.util.List;

import javax.jdo.annotations.EmbeddedOnly;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;


@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable = "true")
public class Trigger {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key id;

  @Deprecated
  @Persistent
  private Integer eventCode;

  @Deprecated
  @Persistent
  private String sourceIdentifier;

  @Deprecated
  @Persistent
  private Long delay;

  @Deprecated
  @Persistent
  private Integer timeout;

  @Deprecated
  @Persistent
  private Integer snoozeCount;

  @Deprecated
  @Persistent
  private Integer snoozeTime;

  // existing properties to keep
  @Persistent
  private Integer minimumBuffer;
  // end existing keepers

  // New trigger properties
  @PersistenceCapable
  @EmbeddedOnly
  public static class TriggerCue implements Serializable {

    @Persistent
    private Integer cueCode;

    @Persistent
    private String cueSource;

    public Integer getCueCode() {
      return cueCode;
    }

    public void setCueCode(Integer cueCode) {
      this.cueCode = cueCode;
    }

    public String getCueSource() {
      return cueSource;
    }

    public void setCueSource(String cueSource) {
      this.cueSource = cueSource;
    }
  }

  @PersistenceCapable
  @EmbeddedOnly
  public static class TriggerAction implements Serializable {

    @Persistent
    private Integer actionCode;

    @Persistent
    private String msgText;

    @Persistent
    private String customScript;

    // these are only for notification type actions

    @Persistent
    private Long notificationDelay;

    @Persistent
    private Integer notificationTimeout;

    @Persistent
    private Integer notificationSnoozeCount;

    @Persistent
    private Integer notificationSnoozeTime;

    public Integer getActionCode() {
      return actionCode;
    }

    public void setActionCode(Integer actionCode) {
      this.actionCode = actionCode;
    }

    public String getMsgText() {
      return msgText;
    }

    public void setMsgText(String msgText) {
      this.msgText = msgText;
    }

    public String getCustomScript() {
      return customScript;
    }

    public void setCustomScript(String customScript) {
      this.customScript = customScript;
    }

    public Long getNotificationDelay() {
      return notificationDelay;
    }

    public void setNotificationDelay(Long notificationDelay) {
      this.notificationDelay = notificationDelay;
    }

    public Integer getNotificationTimeout() {
      return notificationTimeout;
    }

    public void setNotificationTimeout(Integer notificationTimeout) {
      this.notificationTimeout = notificationTimeout;
    }

    public Integer getNotificationSnoozeCount() {
      return notificationSnoozeCount;
    }

    public void setNotificationSnoozeCount(Integer notificationSnoozeCount) {
      this.notificationSnoozeCount = notificationSnoozeCount;
    }

    public Integer getNotificationSnoozeTime() {
      return notificationSnoozeTime;
    }

    public void setNotificationSnoozeTime(Integer notificationSnoozeTime) {
      this.notificationSnoozeTime = notificationSnoozeTime;
    }

  }


  /**
   * likelihood from 0-100 that this trigger will fire.
   */
  @Persistent
  private Integer triggerProbability;
  /**
   * List of cues that define when this trigger is active.
   * If any cue matches, then the trigger actions are executed.
   */
  @Persistent
  private List<TriggerCue> cues;

  @Persistent
  private List<TriggerAction> actions;

  // end new trigger properties

  public Integer getTimeout() {
    return timeout;
  }

  public void setTimeout(Integer timeout) {
    this.timeout = timeout;
  }

  public Trigger(Key ownerKey, Long id, Integer event, String sourceIdentifier, Long millisecondDelay, Integer timeout, Integer minimumBuffer, Integer snoozeCount, Integer snoozeTime) {
    super();
    if (id != null) {
      this.id = KeyFactory.createKey(ownerKey, Trigger.class.getSimpleName(), id);
    }
    this.eventCode = event;
    this.sourceIdentifier = sourceIdentifier;
    this.delay = millisecondDelay;
    this.timeout = timeout;
    this.minimumBuffer = minimumBuffer;
    this.snoozeCount = snoozeCount;
    this.snoozeTime = snoozeTime;
  }

  public Key getId() {
    return id;
  }

  public void setId(Key id) {
    this.id = id;
  }

  public Integer getEventCode() {
    return eventCode;
  }

  public void setEventCode(Integer eventCode) {
    this.eventCode = eventCode;
  }

  public Long getDelay() {
    return delay;
  }

  public void setDelay(Long delay) {
    this.delay = delay;
  }

  public String getSourceIdentifier() {
    return sourceIdentifier;
  }

  public void setSourceIdentifier(String sourceIdentifier) {
    this.sourceIdentifier = sourceIdentifier;
  }

  public Integer getMinimumBuffer() {
    return minimumBuffer;
  }

  public void setMinimumBuffer(Integer minimumBuffer) {
    this.minimumBuffer = minimumBuffer;
  }

  public Integer getSnoozeCount() {
    return snoozeCount;
  }

  public void setSnoozeCount(Integer snoozeCount) {
    this.snoozeCount = snoozeCount;
  }

  public Integer getSnoozeTime() {
    return snoozeTime;
  }

  public void setSnoozeTime(Integer snoozeTime) {
    this.snoozeTime = snoozeTime;
  }

  @Override
  public String toString() {
    return "Trigger";
  }

  public Integer getTriggerProbability() {
    return triggerProbability;
  }

  public void setTriggerProbability(Integer triggerProbability) {
    this.triggerProbability = triggerProbability;
  }

  public List<TriggerCue> getCues() {
    return cues;
  }

  public void setCues(List<TriggerCue> cues) {
    this.cues = cues;
  }

  public List<TriggerAction> getActions() {
    return actions;
  }

  public void setActions(List<TriggerAction> actions) {
    this.actions = actions;
  }


}
