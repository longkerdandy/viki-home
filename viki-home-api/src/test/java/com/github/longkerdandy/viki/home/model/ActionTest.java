package com.github.longkerdandy.viki.home.model;

import com.github.longkerdandy.viki.home.schema.ActionSchema;
import com.github.longkerdandy.viki.home.schema.PropertySchema;
import com.github.longkerdandy.viki.home.util.Jacksons;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import org.junit.Test;

public class ActionTest {

  @Test
  public void Test() throws IOException {
    PropertySchema<Long> level = PropertySchema.createIntegerProperty(
        "level", null, 1L, 10L, 3L,
        new Long[]{1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L});
    PropertySchema<Boolean> on = PropertySchema.createBooleanProperty("on", null, null);
    PropertySchema<LocalDateTime> time = PropertySchema.createDateTimeProperty(
        "time", null, null, LocalDateTime.now(),
        LocalDateTime.of(1983, 5, 16, 17, 0));
    Map<String, PropertySchema> inputs = Map.of("level", level, "on", on, "time", time);

    PropertySchema<Double> brightness = PropertySchema.createNumberProperty(
        "brightness", null, 0.0d, 100.0d, null, null);
    PropertySchema<Long> color = PropertySchema.createIntegerProperty(
        "color", null, 0L, 255L, null, null);
    PropertySchema<long[]> rgb = PropertySchema.createIntegerArrayProperty(
        "rgb", null, 3, 3, color, null);
    PropertySchema lamp = PropertySchema.createObjectProperty(
        "lamp", null, Map.of("brightness", brightness, "rgb", rgb));
    PropertySchema<String> error = PropertySchema.createStringProperty(
        "error", null, 1, 100, null, null, null);
    Map<String, PropertySchema> outputs = Map.of("lamp", lamp, "error", error);

    ActionSchema schema = ActionSchema.create("turnOn", null, inputs, outputs);

    Action action = new Action(
        "turnOn",
        new Property[]{
            new Property<>("level", DataType.INTEGER, 3L),
            new Property<>("on", DataType.BOOLEAN, false),
            new Property<>("time", DataType.DATETIME,
                LocalDateTime.of(1983, 5, 16, 17, 0))},
        new Property[]{
            new Property<>("lamp", DataType.OBJECT, new Property[]{
                new Property<>("brightness", DataType.NUMBER, 66.6d),
                new Property<>("rgb", DataType.ARRAY_INTEGER, new long[]{255, 125, 125})}
            )}, LocalDateTime.now());

    assert action.validate(schema);
    String json = Jacksons.getWriter().writeValueAsString(action);
    assert json != null;
    action = Jacksons.getReader(Action.class).readValue(json);
    assert action.getName().equals("turnOn");
    assert action.getCreatedAt() != null;
    assert (Long) action.getInputs()[0].getValue() == 3L;
    assert !((Boolean) action.getInputs()[1].getValue());
    assert ((LocalDateTime) action.getInputs()[2].getValue()).isEqual(
        LocalDateTime.of(1983, 5, 16, 17, 0));
    assert (Double) ((Property[]) action.getOutputs()[0].getValue())[0].getValue() == 66.6d;
    assert Arrays.equals((long[]) ((Property[]) action.getOutputs()[0].getValue())[1].getValue(),
        new long[]{255, 125, 125});
    assert action.validate(schema);
  }
}
