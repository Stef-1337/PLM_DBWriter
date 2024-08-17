package de.plm.db.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

//@Entity
//@Table(name = "tasks", schema = "plm", catalog = "")
public class TasksEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "fields_id")
    private int fieldsId;

    private FieldsEntity field;

    public int getFieldsId() {
        return fieldsId;
    }

    public void setFieldsId(int fieldsId) {
        this.fieldsId = fieldsId;
    }

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "vehicles_id")
    private int vehiclesId;

    public int getVehiclesId() {
        return vehiclesId;
    }

    public void setVehiclesId(int vehiclesId) {
        this.vehiclesId = vehiclesId;
    }

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "attachments_id")
    private int attachmentsId;

    private AttachmentsEntity attachment;

    public int getAttachmentsId() {
        return attachmentsId;
    }

    public void setAttachment(AttachmentsEntity attachment){
        this.attachment = attachment;
    }

    public AttachmentsEntity getAttachment(){
        return attachment;
    }

    public void setAttachmentsId(int attachmentsId) {
        this.attachmentsId = attachmentsId;
    }

    @Basic
    @Column(name = "description")
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime begin, end;

    public LocalDateTime getBegin() {
        return begin;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setBegin(LocalDateTime begin) {
        this.begin = begin;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    @Basic
    @Column(name = "timestamp")
    private String timestamp;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Basic
    @Column(name = "duration")
    private Long duration;

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public void setField(FieldsEntity field){
        this.field = field;
    }

    public FieldsEntity getField(){
        return field;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TasksEntity that = (TasksEntity) o;

        if (id != that.id) return false;
        if (fieldsId != that.fieldsId) return false;
        if (vehiclesId != that.vehiclesId) return false;
        if (attachmentsId != that.attachmentsId) return false;
        if (!Objects.equals(description, that.description)) return false;
        if (!Objects.equals(timestamp, that.timestamp)) return false;
        return Objects.equals(duration, that.duration);
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + fieldsId;
        result = 31 * result + vehiclesId;
        result = 31 * result + attachmentsId;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (duration != null ? duration.hashCode() : 0);
        return result;
    }
}
